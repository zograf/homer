package org.placeholder.homer.simulation.devices;

import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.placeholder.homer.simulation.Main;
import org.placeholder.homer.simulation.MqttConfiguration;
import org.placeholder.homer.simulation.dto.DeviceDTO;
import org.placeholder.homer.simulation.dto.modules.AmbientLightModule;
import org.placeholder.homer.simulation.dto.modules.EVChargerModule;
import org.placeholder.homer.simulation.dto.modules.StatusModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Lamp {
    private static final Logger logger = LoggerFactory.getLogger(Lamp.class);

    public static void startSimulating(DeviceDTO device) {
        Thread t = new Thread(() -> {
            String topic = device.getId().toString();
            MqttConfiguration mqttConfiguration = null;
            try { mqttConfiguration = new MqttConfiguration(device); }
            catch (Exception e) {
                logger.error("Failed to connect device {} to mqtt broker", device.getId());
                return;
            }
            MqttClient client = mqttConfiguration.getClient();
            try { client.subscribe(topic, 2); }
            catch (MqttException e) { logger.error("Failed to subscribe device {} to topic", device.getId()); }

            AmbientLightModule ambientLightModule = device.getAmbientLightModule();
            StatusModule statusModule = device.getStatusModule();

            long reportInterval = 1 * 1000;
            long lastReport = System.currentTimeMillis();
            boolean increasing = true;
            int autoStatusTreshold = 50;

            long consumptionInterval = 60 * 1000;
            long lastConsumption = System.currentTimeMillis();

            while (true) {
                long currentTime = System.currentTimeMillis();

                if (currentTime >= lastConsumption + consumptionInterval) {
                    Main.consumeEnergy(client, device);
                    lastConsumption = currentTime;
                }

                if (currentTime > lastReport + reportInterval) {
                    ambientLightModule = device.getAmbientLightModule();

                    ambientLightModule.setLightPresence(ambientLightModule.getLightPresence() + (increasing ? 20 : -20));
                    if (ambientLightModule.getLightPresence() >= 100) {
                        increasing = false;
                        ambientLightModule.setLightPresence(100);
                    } else if (ambientLightModule.getLightPresence() <= 0) {
                        increasing = true;
                        ambientLightModule.setLightPresence(0);
                    }
                    reportAmbientLight(client, device, ambientLightModule);

                    logger.info("AUTO STATUS: {}", ambientLightModule.isAutoStatus());
                    if (ambientLightModule.isAutoStatus()) {
                        statusModule = device.getStatusModule();
                        if (ambientLightModule.getLightPresence() > autoStatusTreshold && statusModule.isOn()) {
                            statusModule.setOn(false);
                            changeStatus(client, device, statusModule);

                        } else if (ambientLightModule.getLightPresence() < autoStatusTreshold && !statusModule.isOn()) {
                            statusModule.setOn(true);
                            changeStatus(client, device, statusModule);
                        }
                    }

                    lastReport = currentTime;
                }

                Main.sendHeartbeat(client, device);

                try {
                    Main.sleep();
                } catch (InterruptedException e) {
                    logger.error("Device {} sleep was interrupted", device.getId());
                    return;
                }
            }
        });
        t.start();
    }

    private static void reportAmbientLight(MqttClient client, DeviceDTO device, AmbientLightModule module) {
        String topic = "ambient_light";
        String message = device.getId().toString() + "," + module.getLightPresence();
        logger.info("Sending to topic \"{}\" message {}", topic, message);
        try {
            client.publish(topic, new MqttMessage(message.getBytes()));
        } catch (MqttException e) {
            logger.error("Device {} failed to send a message", device.getId());
        }
    }

    private static void changeStatus(MqttClient client, DeviceDTO device, StatusModule module) {
        String topic = "status";
        String message = device.getId().toString() + "," + module.isOn();
        logger.info("Sending to topic \"{}\" message {}", topic, message);
        try {
            client.publish(topic, new MqttMessage(message.getBytes()));
        } catch (MqttException e) {
            logger.error("Device {} failed to send a message", device.getId());
        }
    }

    public static void handleMessage(DeviceDTO device, String topic, String message, MqttClient client) {
        String[] tmp = message.split("\\|");
        String action = tmp[0];
        if (action.equals("SET_AUTO_BRIGHTNESS")) {
            boolean auto = Boolean.parseBoolean(tmp[1]);
            AmbientLightModule module = device.getAmbientLightModule();
            module.setAutoStatus(auto);
        }
        else if (action.equals("SET_STATUS")) {
            boolean isOn = Boolean.parseBoolean(tmp[1]);
            StatusModule module = device.getStatusModule();
            module.setOn(isOn);
            Main.acknowledgeSetStatus(device, tmp[2], tmp[1], client);
        }
    }
}
