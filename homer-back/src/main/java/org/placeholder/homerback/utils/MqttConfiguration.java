package org.placeholder.homerback.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.mqttv5.client.*;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.placeholder.homerback.services.DeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Objects;
import java.util.UUID;

@Configuration
public class MqttConfiguration {
    private final String broker;
    private final String username;
    private final String password;
    private final String uniqueClientIdentifier;
    private final DeviceService deviceService;

    public MqttConfiguration(Environment env, DeviceService deviceService) {
        this.broker = String.format("tcp://%s:%s", env.getProperty("mqtt.host"),
                env.getProperty("mqtt.port"));
        this.username = env.getProperty("mqtt.username");
        this.password = env.getProperty("mqtt.password");
        this.uniqueClientIdentifier = UUID.randomUUID().toString();
        this.deviceService = deviceService;
    }

    @Bean
    public IMqttClient mqttClient() throws Exception {
        MqttClient client = new MqttClient(this.broker, this.uniqueClientIdentifier, new MemoryPersistence());
        client.setCallback(new MessageCallback(this.deviceService, client));
        MqttConnectionOptions options = new MqttConnectionOptions();
        options.setCleanStart(false);
        options.setAutomaticReconnect(true);
        options.setUserName(this.username);
        options.setPassword(Objects.requireNonNull(this.password).getBytes());
        client.connect(options);
        return client;
    }

    public static class MessageCallback implements MqttCallback {
        private static final Logger LOGGER = LoggerFactory.getLogger(MessageCallback.class);
        private final DeviceService deviceService;
        private final IMqttClient client;
        public MessageCallback(DeviceService deviceService, IMqttClient client) {
            super();
            this.deviceService = deviceService;
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
            if (!topic.equals("heartbeat"))
                LOGGER.info("topic: {}, message: {}", topic, mqttMessage);
            String message = new String(mqttMessage.getPayload());
            switch (topic) {
                case "heartbeat" -> deviceService.heartbeat(message);
                case "production" -> deviceService.producePower(message, client);
                case "consumption" -> deviceService.consumePower(message, client);
                case "battery_state" -> deviceService.updateBatteryState(message);
                case "electricity_distribution" -> deviceService.electricityDistributionDelta(message);
                case "charging_start" -> deviceService.startCharging(message);
                case "charging_end" -> deviceService.stopCharging(message);
                case "charging_progress" -> deviceService.progressCharging(message);
                case "ambient_light" -> deviceService.changeAmbientLightAmount(message);
                case "status" -> deviceService.changeStatusModule(message);
                case "ambient_sensor" -> deviceService.readSensorValues(message);
                case "air_conditioner" -> deviceService.readAirConditionerMessage(message);
                case "ac_schedule_end" -> deviceService.airConditionerScheduleEnd(message);
                case "smart_gate" -> deviceService.gateAction(message);
                case "washing_machine" -> deviceService.readWashingMachineMessage(message);
                case "wm_schedule_end" -> deviceService.washingMachineScheduleEnd(message);
                case "ack_set_status" -> deviceService.setStatus(message);
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
