package org.placeholder.homer.simulation;

import org.eclipse.paho.mqttv5.client.*;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.placeholder.homer.simulation.devices.*;
import org.placeholder.homer.simulation.dto.DeviceDTO;
import org.placeholder.homer.simulation.dto.modules.EVChargerModule;
import org.placeholder.homer.simulation.dto.modules.WashingMachineModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Properties;
import java.util.UUID;

public class MqttConfiguration {
    private final Properties env;
    private final String broker;
    private final String uniqueClientIdentifier;
    private final MqttClient client;

    public MqttConfiguration(DeviceDTO device) throws Exception {
        this.env = new Properties();
        env.load(MqttConfiguration.class.getClassLoader().getResourceAsStream("application.properties"));
        this.broker = String.format("tcp://%s:%s", env.getProperty("mqtt.host"), env.getProperty("mqtt.port"));
        this.uniqueClientIdentifier = UUID.randomUUID().toString();
        this.client = this.mqttClient(device);
    }

    public MqttClient getClient() {
        return client;
    }

    private MqttClient mqttClient(DeviceDTO device) throws MqttException {
        MqttClient client = new MqttClient(this.broker, this.uniqueClientIdentifier, new MemoryPersistence());
        client.setCallback(new MessageCallback(device, client));
        MqttConnectionOptions options = new MqttConnectionOptions();
        options.setCleanStart(false);
        options.setAutomaticReconnect(true);
        options.setUserName(this.env.getProperty("mqtt.username"));
        options.setPassword(Objects.requireNonNull(this.env.getProperty("mqtt.password")).getBytes());
        client.connect(options);
        return client;
    }

    public static class MessageCallback implements MqttCallback {
        private static final Logger LOGGER = LoggerFactory.getLogger(MessageCallback.class);
        private DeviceDTO device;
        private MqttClient client;
        public MessageCallback(DeviceDTO device, MqttClient client) {
            this.device = device;
            this.client = client;
        }

        @Override
        public void disconnected(MqttDisconnectResponse mqttDisconnectResponse) {
            LOGGER.warn("disconnected: {}", mqttDisconnectResponse.getReasonString());
        }

        @Override
        public void mqttErrorOccurred(MqttException e) {
            LOGGER.error("error: {}", e.getMessage());
        }

        @Override public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
            LOGGER.info("topic: {}, message: {}", topic, mqttMessage);
            String message = new String(mqttMessage.getPayload());
            LOGGER.info(message);
            if (device.getType().equals("SOLAR_PANEL_SYSTEM")) {
                SolarPanelSystem.handleMessage(device, topic, message, client);
            } else if (device.getType().equals("BATTERY")){
                Battery.handleMessage(device, topic, message);
            } else if (device.getType().equals("EV_CHARGER")) {
                EVCharger.handleMessage(device, topic, message);
            } else if (device.getType().equals("LAMP")) {
                Lamp.handleMessage(device, topic, message, client);
            } else if (device.getType().equals("AIR_CONDITIONER")) {
                AirConditioner.handleMessage(device, topic, message, client);
            } else if (device.getType().equals("GATE")) {
                Gate.handleMessage(device, topic, message);
            } else if (device.getType().equals("WASHING_MACHINE")) {
                WashingMachine.handleMessage(device, topic, message, client);
            } else if (device.getType().equals("SPRINKLER_SYSTEM")) {
                SprinklerSystem.handleMessage(device, topic, message, client);
            }
        }

        @Override
        public void deliveryComplete(IMqttToken iMqttToken) {
            LOGGER.debug("delivery complete, message id: {}", iMqttToken.getMessageId());
        }

        @Override
        public void connectComplete(boolean b, String s) {
            LOGGER.debug("connect complete, status: {} {}", b, s);
        }

        @Override
        public void authPacketArrived(int i, MqttProperties mqttProperties) {
            LOGGER.debug("auth packet arrived , status: {} {}", i, mqttProperties.getAuthenticationMethod());
        }
    }
}
