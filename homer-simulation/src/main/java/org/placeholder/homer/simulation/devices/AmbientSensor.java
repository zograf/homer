package org.placeholder.homer.simulation.devices;

import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.placeholder.homer.simulation.Main;
import org.placeholder.homer.simulation.MqttConfiguration;
import org.placeholder.homer.simulation.dto.DeviceDTO;
import org.placeholder.homer.simulation.dto.modules.AmbientSensorModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Random;

public class AmbientSensor {
    private static final Logger logger = LoggerFactory.getLogger(AmbientSensor.class);

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

            AmbientSensorModule sensorModule = device.getSensorModule();

            long reportInterval = 1 * 100;
            long lastReport = System.currentTimeMillis();

            long consumptionInterval = 60 * 1000;
            long lastConsumption = System.currentTimeMillis();

            while (true) {
                long currentTime = System.currentTimeMillis();

                if (currentTime >= lastConsumption + consumptionInterval) {
                    Main.consumeEnergy(client, device);
                    lastConsumption = currentTime;
                }

                if (currentTime > lastReport + reportInterval) {
                    LocalDateTime now = LocalDateTime.now();
                    sensorModule.setTemperatureValue(calculateTemperature(now.getMonthValue(), now.getHour()));
                    sensorModule.setHumidityPercent(calculateHumidity(now.getMonthValue(), now.getHour()));

                    reportSensor(client, device, sensorModule);
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

    private static Double calculateTemperature(Integer month, Integer hour) {
        Random rand = new Random();
        double base_temp = 30.0;
        if (month < 3 || month > 10) {
            base_temp = 10.0;
        } else if (month < 6 || month > 8) {
            base_temp = 20.0;
        }

        int modifier = -2;
        if ((hour > 6 && hour < 10) || (hour > 18 && hour < 23)) {
            modifier = 1;
        } else if (hour >= 10 && hour <= 18) {
            modifier = 3;
        }

        return base_temp + modifier * rand.nextDouble() / 2;
    }

    private static Double calculateHumidity(Integer month, Integer hour) {
        Random rand = new Random();
        double base_humidity = 30.0;
        if (month < 3 || month > 10) {
            base_humidity = 10.0;
        } else if (month < 6 || month > 8) {
            base_humidity = 20.0;
        }

        int modifier = -2;
        if ((hour > 6 && hour < 10) || (hour > 18 && hour < 23)) {
            modifier = 1;
        } else if (hour >= 10 && hour <= 18) {
            modifier = 3;
        }

        return base_humidity + 15 + rand.nextDouble() + modifier;
    }

    private static void reportSensor(MqttClient client, DeviceDTO device, AmbientSensorModule module) {
        String topic = "ambient_sensor";
        String message = device.getId().toString() + "," + module.getTemperatureValue() + "," + module.getHumidityPercent();
        logger.info("Sending to topic \"{}\" message {}", topic, message);
        try {
            client.publish(topic, new MqttMessage(message.getBytes()));
        } catch (MqttException e) {
            logger.error("Device {} failed to send a message", device.getId());
        }
    }
}
