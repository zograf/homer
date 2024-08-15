package org.placeholder.homer.simulation.devices;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.placeholder.homer.simulation.Main;
import org.placeholder.homer.simulation.MqttConfiguration;
import org.placeholder.homer.simulation.dto.DeviceDTO;
import org.placeholder.homer.simulation.dto.modules.AmbientLightModule;
import org.placeholder.homer.simulation.dto.modules.EModuleType;
import org.placeholder.homer.simulation.dto.modules.SprinklerScheduleModule;
import org.placeholder.homer.simulation.dto.modules.StatusModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalTime;

public class SprinklerSystem {
    private static final Logger logger = LoggerFactory.getLogger(SprinklerSystem.class);
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

            long consumptionInterval = 60 * 1000;
            long lastConsumption = System.currentTimeMillis();

            StatusModule statusModule = device.getStatusModule();
            SprinklerScheduleModule scheduleModule = (SprinklerScheduleModule) device.getModule(EModuleType.SPRINKLER_SCHEDULE);

            Double schedulingState = (double) ((LocalDate.now().getDayOfWeek().getValue() + 6) % 7);

            while (true) {
                long currentTime = System.currentTimeMillis();

                if (currentTime >= lastConsumption + consumptionInterval) {
                    if(statusModule.isOn()) Main.consumeEnergy(client, device);
                    lastConsumption = currentTime;
                }

                Integer currentDayIdx = (LocalDate.now().getDayOfWeek().getValue() + 6) % 7;
                if(scheduleModule.getDays().toCharArray()[currentDayIdx] == '1') {

                    LocalTime current = LocalTime.now();
                    LocalTime from = null;
                    LocalTime to = null;

                    if (currentDayIdx == 0) {
                        from = scheduleModule.getMonStartTime();
                        to = scheduleModule.getMonEndTime();
                    }
                    else if (currentDayIdx == 1) {
                        from = scheduleModule.getTueStartTime();
                        to = scheduleModule.getTueEndTime();
                    }
                    else if (currentDayIdx == 2) {
                        from = scheduleModule.getWedStartTime();
                        to = scheduleModule.getWedEndTime();
                    }
                    else if (currentDayIdx == 3) {
                        from = scheduleModule.getThuStartTime();
                        to = scheduleModule.getThuEndTime();
                    }
                    else if (currentDayIdx == 4) {
                        from = scheduleModule.getFriStartTime();
                        to = scheduleModule.getFriEndTime();
                    }
                    else if (currentDayIdx == 5) {
                        from = scheduleModule.getSatStartTime();
                        to = scheduleModule.getSatEndTime();
                    }
                    else if (currentDayIdx == 6) {
                        from = scheduleModule.getSunStartTime();
                        to = scheduleModule.getSunEndTime();
                    }

                    if (current.isAfter(from) && current.isBefore(to) && schedulingState == currentDayIdx.doubleValue()) {
                        statusModule.setOn(true);
                        changeStatus(client, device, statusModule);
                        schedulingState += 0.5;
                    }
                    else if (current.isAfter(from) && current.isAfter(to) && schedulingState == currentDayIdx.doubleValue() + 0.5) {
                        statusModule.setOn(false);
                        changeStatus(client, device, statusModule);
                        schedulingState = Double.valueOf(currentDayIdx);
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

        });
        t.start();
    }
    public static void handleMessage(DeviceDTO device, String topic, String message, MqttClient client) {
        String[] tmp = message.split("\\|");
        String action = tmp[0];
        if (action.equals("SET_STATUS")) {
            boolean isOn = Boolean.parseBoolean(tmp[1]);
            StatusModule module = device.getStatusModule();
            module.setOn(isOn);
            Main.acknowledgeSetStatus(device, tmp[2], tmp[1], client);
        }
        else if (action.equals("SET_SCHEDULE")) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            try {
                ((SprinklerScheduleModule) device.getModule(EModuleType.SPRINKLER_SCHEDULE)).update(mapper.readValue(tmp[1], SprinklerScheduleModule.class));
            } catch (JsonProcessingException e) {
                logger.error("Error parsing new module. Msg: " + e.getMessage());
            }
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

}