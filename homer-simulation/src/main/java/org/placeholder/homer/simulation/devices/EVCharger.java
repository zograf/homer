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
import org.placeholder.homer.simulation.dto.modules.EVChargerModule;
import org.placeholder.homer.simulation.utils.Car;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class EVCharger {
    private static final Logger logger = LoggerFactory.getLogger(EVCharger.class);
    private static final Random random = new Random();
    private static final ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public static void startSimulatingEVCharger(DeviceDTO device) {
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

                EVChargerModule module = device.getEVChargerModule();
                module.setCars(new ArrayList<Car>());
                module.setIsAvailable(new ArrayList<>());
                for (int i = 0; i < module.getSlots(); i++) {
                    module.getIsAvailable().add(true);
                }

                long reportInterval = 60 * 1000;
                long lastReport = System.currentTimeMillis();

                while (true) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime > lastReport + reportInterval) {
                        List<Car> toRemove = new ArrayList<>();
                        for (Car car : module.getCars()) {
                            car.fill(module.getPower() / 60, module.getFillToPercent());
                            chargingProgress(client, device, car);
                            consume(client, device, module.getPower() / 60);
                            logger.info("Car {} -> {}%, Added power {}", car.getCapacity(), car.getPercent(), module.getPower() / 60);
                            if (car.getPercent() >= module.getFillToPercent()) {
                                stopCharging(client, device, module, car);
                                toRemove.add(car);
                            }
                        }
                        for(Car car : toRemove) {
                            module.getIsAvailable().set(car.getSlot(), true);
                            module.getCars().remove(car);
                            module.setOccupiedSlots(module.getCars().size());
                        }
                        lastReport = currentTime;
                    }

                    if (module.getOccupiedSlots() < module.getSlots()){
                        if (random.nextInt() % 5 == 0) {
                            int slot = module.getAvailableSlot();
                            Car car = new Car(module.getFillToPercent() / 2, slot);
                            module.getIsAvailable().set(slot, false);
                            startCharging(client, device, module, car);
                            module.getCars().add(car);
                            module.setOccupiedSlots(module.getCars().size());
                        }
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

    private static void chargingProgress(MqttClient client, DeviceDTO device, Car car) {
        String topic = "charging_progress";
        String message = device.getId().toString() + "," + car.getCapacity() + "," + car.getPercent() + "," + car.getSlot();
        logger.info("Sending to topic \"{}\" message {}", topic, message);
        try {
            client.publish(topic, new MqttMessage(message.getBytes()));
        } catch (MqttException e) {
            logger.error("Device {} failed to send a message", device.getId());
        }
    }

    private static void startCharging(MqttClient client, DeviceDTO device, EVChargerModule module, Car car) {
        String topic = "charging_start";
        String message = device.getId().toString() + "," + car.getCapacity() + "," + car.getPercent() + "," + car.getSlot();
        logger.info("Sending to topic \"{}\" message {}", topic, message);
        try {
            client.publish(topic, new MqttMessage(message.getBytes()));
        } catch (MqttException e) {
            logger.error("Device {} failed to send a message", device.getId());
        }
    }

    private static void stopCharging(MqttClient client, DeviceDTO device, EVChargerModule module, Car car) {
        String topic = "charging_end";
        String message = device.getId().toString() + "," + car.getCapacity() + "," + car.getConsumed() + "," + car.getSlot();
        logger.info("Sending to topic \"{}\" message {}", topic, message);
        try {
            client.publish(topic, new MqttMessage(message.getBytes()));
        } catch (MqttException e) {
            logger.error("Device {} failed to send a message", device.getId());
        }
    }

    private static void consume(MqttClient client, DeviceDTO device, double v) {
        String topic = "consumption";
        String message = device.getId().toString() + "," + v;
        logger.info("Sending to topic \"{}\" message {}", topic, message);
        try {
            client.publish(topic, new MqttMessage(message.getBytes()));
        } catch (MqttException e) {
            logger.error("Device {} failed to send a message", device.getId());
        }
    }

    public static void handleMessage(DeviceDTO device, String topic, String message) {
        double percent = Double.parseDouble(message);
        EVChargerModule module = device.getEVChargerModule();
        module.setFillToPercent(percent);
    }
}
