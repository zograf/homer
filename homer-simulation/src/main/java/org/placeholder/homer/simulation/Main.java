package org.placeholder.homer.simulation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.placeholder.homer.simulation.devices.*;
import org.placeholder.homer.simulation.dto.DeviceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Random;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final Random random = new Random();
    private static final ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String GET_URL = "http://localhost:8080/api/device";

    public static void main(String[] args) throws Exception {

        if (args.length > 0) {
            for (String arg : args){
                DeviceDTO device = new DeviceDTO();
                device.setId(Integer.parseInt(arg));
                startSimulatingDevice(device);
            }
        }else {

            List<DeviceDTO> devices = getDevices();

            if (devices == null) {
                logger.error("Failed to get devices to simulate!");
            } else {
                for (DeviceDTO device : devices) {
                    startSimulatingDevice(device);
                }
            }
        }
    }

    private static void startSimulatingDevice(DeviceDTO device){
        switch (device.getType()) {
            case "SOLAR_PANEL_SYSTEM" -> SolarPanelSystem.startSimulatingSolarPanelSystem(device);
            case "BATTERY" -> Battery.startSimulatingBattery(device);
            case "EV_CHARGER" -> EVCharger.startSimulatingEVCharger(device);
            case "LAMP" -> Lamp.startSimulating(device);
            case "AMBIENT_SENSOR" -> AmbientSensor.startSimulating(device);
            case "AIR_CONDITIONER" -> AirConditioner.startSimulating(device);
            case "WASHING_MACHINE" -> WashingMachine.startSimulating(device);
            case "GATE" -> Gate.startSimulating(device);
            case "SPRINKLER_SYSTEM" -> SprinklerSystem.startSimulating(device);
            default -> {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MqttConfiguration mqttConfiguration = null;
                        try {
                            mqttConfiguration = new MqttConfiguration(device);
                        } catch (Exception e) {
                            logger.error("Failed to connect device {} to mqtt broker", device.getId());
                            return;
                        }
                        MqttClient client = mqttConfiguration.getClient();

                        long consumptionInterval = 60 * 1000;
                        long lastConsumption = System.currentTimeMillis();

                        while (true) {
                            long currentTime = System.currentTimeMillis();

                            if (currentTime >= lastConsumption + consumptionInterval) {
                                consumeEnergy(client, device);
                                lastConsumption = currentTime;
                            }

                            sendHeartbeat(client, device);

                            try {
                                sleep();
                            } catch (InterruptedException e) {
                                logger.error("Device {} sleep was interrupted", device.getId());
                                return;
                            }
                        }
                    }
                });
                t.start();
            }
        }
    }

    public static void sendHeartbeat(MqttClient client, DeviceDTO device) {
        String topic = "heartbeat";
        String message = device.getId().toString();
        logger.info("Sending to topic \"{}\" message {}", topic, message);
        try {
            client.publish(topic, new MqttMessage(message.getBytes()));
        } catch (MqttException e) {
            logger.error("Device {} failed to send a message", device.getId());
        }
    }

    public static void consumeEnergy(MqttClient client, DeviceDTO device) {
        if (device.getPowerSupply().equals("HOME")) {
            String topic = "consumption";
            String message = device.getId().toString() + "," + (device.getConsumption() / 60);
            logger.info("Sending to topic \"{}\" message {}", topic, message);
            try {
                client.publish(topic, new MqttMessage(message.getBytes()));
            } catch (MqttException e) {
                logger.error("Device {} failed to send a message", device.getId());
            }
        }
    }

    public static void sleep() throws InterruptedException {
        int sleepInterval = 5;
        logger.info("Sleep for {} seconds", sleepInterval);
        Thread.sleep(sleepInterval * 1000L);
    }

    private static List<DeviceDTO> getDevices() throws IOException {
        objectMapper.findAndRegisterModules();
        URL obj = new URL(GET_URL);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        int responseCode = con.getResponseCode();
        System.out.println("GET Response Code :: " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // print result
            System.out.println(response.toString());
            List<DeviceDTO> devices = objectMapper.readValue(response.toString(), new TypeReference<List<DeviceDTO>>(){});
            return devices;
        } else {
            System.out.println("GET request did not work.");
            return null;
        }
    }

    public static void acknowledgeSetStatus(DeviceDTO device, String userId, String status, MqttClient client) {
        if (client != null) {
            try {
                String toSend = device.getId().toString() + "," + userId + "," + status;
                client.publish("ack_set_status", new MqttMessage(toSend.getBytes()));
            } catch (MqttException e) {
                logger.info("Failed to send action acknowledge message");
            }
        }
    }
}