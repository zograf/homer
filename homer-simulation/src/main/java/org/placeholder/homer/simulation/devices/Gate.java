package org.placeholder.homer.simulation.devices;

import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.placeholder.homer.simulation.Main;
import org.placeholder.homer.simulation.MqttConfiguration;
import org.placeholder.homer.simulation.dto.DeviceDTO;
import org.placeholder.homer.simulation.dto.Plate;
import org.placeholder.homer.simulation.dto.modules.AmbientLightModule;
import org.placeholder.homer.simulation.dto.modules.EModuleType;
import org.placeholder.homer.simulation.dto.modules.SmartGateModule;
import org.placeholder.homer.simulation.dto.modules.StatusModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Gate {
    private static final Logger logger = LoggerFactory.getLogger(Gate.class);
    private static int getRandomInt(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("Min value must be less than or equal to max value");
        }
        Random random = new Random();
        return random.nextInt((max - min) + 1) + min;
    }
    private static String getRandomPlate() {
        List<String> alphabet = Arrays.asList(
                "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
                "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
                "U", "V", "W", "X", "Y", "Z"
        );
        return alphabet.get(getRandomInt(0, alphabet.size() - 1)) + alphabet.get(getRandomInt(0, alphabet.size() - 1)) + getRandomInt(0, 9) + getRandomInt(0, 9) + getRandomInt(0, 9) + alphabet.get(getRandomInt(0, alphabet.size() - 1)) + alphabet.get(getRandomInt(0, alphabet.size() - 1));
    }
    public static void startSimulating(DeviceDTO device) {
        Thread t = new Thread(() -> {
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

            SmartGateModule gateModule = (SmartGateModule) device.getModule(EModuleType.SMART_GATE);
            List<String> inGarage = new ArrayList<>();

            long carEnterInterval = 10 * 1000;
            long lastCarEnterTimestamp = System.currentTimeMillis();
            long keepOpenFor = 3 * 1000;
            long openedAt = 0L;

            long consumptionInterval = 60 * 1000;
            long lastConsumption = System.currentTimeMillis();

            while (true) {
                long currentTime = System.currentTimeMillis();
                gateModule = (SmartGateModule) device.getModule(EModuleType.SMART_GATE);

                if (currentTime >= lastConsumption + consumptionInterval) {
                    Main.consumeEnergy(client, device);
                    lastConsumption = currentTime;
                }

                if(currentTime > lastCarEnterTimestamp + carEnterInterval && !gateModule.isOpen()) {
                    Integer randomAction = getRandomInt(0, 2);
                    // Simulate random car trying to enter
                    if(randomAction == 0) {
                        String randomPlate = getRandomPlate();
                        if (!gateModule.isPrivate()) {
                            inGarage.add(randomPlate);
                            reportGateAction(client, device, "ENTER", randomPlate);
                            gateModule.setOpen(true);
                            openedAt = currentTime;
                        }
                        else {
                            reportGateAction(client, device, "FAIL", randomPlate);
                        }
                    }
                    // Simulate registered car trying to enter
                    else if(randomAction == 1) {
                        List<String> availablePlates = gateModule.getPlates().stream().map(Plate::getText).filter(text -> !inGarage.contains(text)).toList();
                        if(!availablePlates.isEmpty()) {
                            String plate = availablePlates.get(getRandomInt(0, availablePlates.size() - 1));
                            inGarage.add(plate);
                            reportGateAction(client, device, "ENTER", plate);
                            gateModule.setOpen(true);
                            openedAt = currentTime;
                        }
                    }
                    // Simulate car exiting
                    else {
                        if(!inGarage.isEmpty()) {
                            String plate = inGarage.remove(getRandomInt(0, inGarage.size() - 1));
                            reportGateAction(client, device, "EXIT", plate);
                            gateModule.setOpen(true);
                            openedAt = currentTime;
                        }
                    }
                    lastCarEnterTimestamp = currentTime;
                }

                if(currentTime > openedAt + keepOpenFor && gateModule.isOpen()) {
                    reportGateAction(client, device, "CLOSE", "");
                    gateModule.setOpen(false);
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

    private static void reportGateAction(MqttClient client, DeviceDTO device, String action, String plate) {
        String topic = "smart_gate";
        String message = device.getId().toString() + "," + action + "," + plate;
        logger.info("Sending to topic \"{}\" message {}", topic, message);
        try {
            client.publish(topic, new MqttMessage(message.getBytes()));
        } catch (MqttException e) {
            logger.error("Device {} failed to send a message", device.getId());
        }
    }

    public static void handleMessage(DeviceDTO device, String topic, String message) {
        String[] tmp = message.split("\\|");
        String action = tmp[0];
        if (action.equals("SET_IS_PRIVATE")) {
            boolean isPrivate = Boolean.parseBoolean(tmp[1]);
            SmartGateModule module = (SmartGateModule) device.getModule(EModuleType.SMART_GATE);
            module.setPrivate(isPrivate);
        }
        else if (action.equals("SET_IS_OPEN")) {
            boolean isOpen = Boolean.parseBoolean(tmp[1]);
            SmartGateModule module = (SmartGateModule) device.getModule(EModuleType.SMART_GATE);
            module.setOpen(isOpen);
        }
        else if (action.equals("SET_PLATES")) {
            SmartGateModule module = (SmartGateModule) device.getModule(EModuleType.SMART_GATE);
            module.setPlates(new ArrayList<>());
            String[] plates = tmp[1].split(";");
            for(String plate: plates) {
                String[] info = plate.split(",");
                module.getPlates().add(new Plate(Integer.parseInt(info[0]), info[1]));
            }
        }
    }
}
