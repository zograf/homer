package org.placeholder.homer.simulation.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.placeholder.homer.simulation.Main;
import org.placeholder.homer.simulation.MqttConfiguration;
import org.placeholder.homer.simulation.dto.DeviceDTO;
import org.placeholder.homer.simulation.dto.modules.BatteryModule;
import org.placeholder.homer.simulation.dto.modules.SolarPanelSystemModule;
import org.placeholder.homer.simulation.dto.modules.StatusModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Random;

public class Battery {
    private static final Logger logger = LoggerFactory.getLogger(Battery.class);
    private static final Random random = new Random();
    private static final ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public static void startSimulatingBattery(DeviceDTO device) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                String topic = device.getId().toString();
                MqttConfiguration mqttConfiguration = null;
                try {
                    mqttConfiguration = new MqttConfiguration(device);
                } catch (Exception e) {
                    logger.error("Failed to connect device {} to mqtt broker", device.getId());
                    return;
                }
                MqttClient client = mqttConfiguration.getClient();
                try {
                    client.subscribe(topic, 2);
                } catch (MqttException e) {
                    logger.error("Failed to subscribe device {} to topic", device.getId());
                }

                BatteryModule batteryModule = device.getBatteryModule();
                batteryModule.setDelta(0.0);

                long reportInterval = 60 * 1000;
                long lastReport = System.currentTimeMillis();

                long stateReportInterval = 10 * 1000;
                long lastStateReport = System.currentTimeMillis();

                while (true) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime > lastReport + reportInterval) {
                        report(client, device, batteryModule);
                        lastReport = currentTime;
                        batteryModule.setDelta(0.0);
                    }

                    if (currentTime > lastStateReport + stateReportInterval) {
                        reportState(client, device, batteryModule);
                        lastStateReport = currentTime;
                    }

                    Main.sendHeartbeat(client, device);

                    try {
                        Main.sleep();
                    } catch (InterruptedException e) {
                        logger.error("Device {} sleep was interrupted", device.getId());
                        return;
                    }
                }
            }
        });
        t.start();
    }

    private static void report(MqttClient client, DeviceDTO device, BatteryModule batteryModule) {
        String topic = "electricity_distribution";
        String message = device.getId().toString() + "," + batteryModule.getDelta();
        logger.info("Sending to topic \"{}\" message {}", topic, message);
        try {
            client.publish(topic, new MqttMessage(message.getBytes()));
        } catch (MqttException e) {
            logger.error("Device {} failed to send a message", device.getId());
        }
    }

    private static void reportState(MqttClient client, DeviceDTO device, BatteryModule batteryModule) {
        String topic = "battery_state";
        String message = device.getId().toString() + "," + batteryModule.getValue();
        logger.info("Sending to topic \"{}\" message {}", topic, message);
        try {
            client.publish(topic, new MqttMessage(message.getBytes()));
        } catch (MqttException e) {
            logger.error("Device {} failed to send a message", device.getId());
        }
    }

    public static void handleMessage(DeviceDTO device, String topic, String message) {
        double consumed = Double.parseDouble(message);
        BatteryModule batteryModule = device.getBatteryModule();
        double value = batteryModule.getValue();
        value += consumed;
        if (value < 0) {
            batteryModule.setDelta(batteryModule.getDelta() + value);
            value = 0;
        }
        if (value > batteryModule.getCapacity()){
            batteryModule.setDelta(batteryModule.getDelta() + value - batteryModule.getCapacity());
            value = batteryModule.getCapacity();
        }
        batteryModule.setValue(value);
    }
}
