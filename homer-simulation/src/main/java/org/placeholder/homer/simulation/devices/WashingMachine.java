package org.placeholder.homer.simulation.devices;

import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.placeholder.homer.simulation.Main;
import org.placeholder.homer.simulation.MqttConfiguration;
import org.placeholder.homer.simulation.dto.DeviceDTO;
import org.placeholder.homer.simulation.dto.EACMode;
import org.placeholder.homer.simulation.dto.EWashingMode;
import org.placeholder.homer.simulation.dto.Schedule;
import org.placeholder.homer.simulation.dto.modules.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class WashingMachine {
    private static final Logger logger = LoggerFactory.getLogger(WashingMachine.class);

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

            WashingMachineModule washingMachineModule = device.getWashingMachineModule();
            SchedulingModule schedulingModule = device.getSchedulingModule();
            StatusModule statusModule = device.getStatusModule();

            long reportInterval = 1 * 1000;
            long lastReport = System.currentTimeMillis();
            Schedule current = null;

            long consumptionInterval = 60 * 1000;
            long lastConsumption = System.currentTimeMillis();

            while (true) {
                long currentTime = System.currentTimeMillis();

                if (currentTime >= lastConsumption + consumptionInterval) {
                    Main.consumeEnergy(client, device);
                    lastConsumption = currentTime;
                }

                if (currentTime > lastReport + reportInterval) {
                    lastReport = currentTime;
                }

                if (current == null) {
                    LocalDateTime now = LocalDateTime.now();
                    for (Schedule s : schedulingModule.getScheduleList()) {
                        if (s.getStartTime().isBefore(now) && s.getEndTime().isAfter(now) && !s.isOverride()) {
                            current = s;
                            break;
                        } else if (s.isRepeat() && !s.isOverride()) {
                            if (s.getStartTime().getHour() < now.getHour() || (s.getStartTime().getHour() == now.getHour() &&
                                    s.getStartTime().getMinute() <= now.getMinute())) {
                                if (s.getEndTime().getHour() > now.getHour() || s.getEndTime().getHour() == now.getHour() &&
                                        s.getEndTime().getMinute() >= now.getMinute()) {
                                    current = s;
                                    break;
                                }
                            }
                        }
                    }
                }
                if (current != null && !schedulingModule.getScheduleList().contains(current)) {
                    current = null;
                    washingMachineModule.setCurrentMode(null);
                    washingMachineModule.setCurrentStart(null);
                    washingMachineModule.setCurrentEnd(null);
                    statusModule.setOn(false);
                    reportWashingMachine(client, device, washingMachineModule);
                    changeStatus(client, device, statusModule);
                } else if (current != null && !(current.getStartTime().isBefore(LocalDateTime.now()) && current.getEndTime().isAfter(LocalDateTime.now()))) {
                    if (!current.isRepeat()) {
                        schedulingModule.getScheduleList().remove(current);
                    }
                    endSchedule(client, device, current);
                    current = null;
                    LocalDateTime now = LocalDateTime.now();
                    // Try to find another schedule or turn off
                    for (Schedule s : schedulingModule.getScheduleList()) {
                        if (s.getStartTime().isBefore(now) && s.getEndTime().isAfter(now) && !s.isOverride()) {
                            current = s;
                            break;
                        } else if (s.isRepeat() && !s.isOverride()) {
                            if (s.getStartTime().getHour() < now.getHour() || (s.getStartTime().getHour() == now.getHour() &&
                                    s.getStartTime().getMinute() <= now.getMinute())) {
                                if (s.getEndTime().getHour() > now.getHour() || s.getEndTime().getHour() == now.getHour() &&
                                        s.getEndTime().getMinute() >= now.getMinute()) {
                                    current = s;
                                    break;
                                }
                            }
                        }
                    }
                    if (current == null) {
                        statusModule.setOn(false);
                        changeStatus(client, device, statusModule);
                        washingMachineModule.setCurrentStart(null);
                        washingMachineModule.setCurrentEnd(null);
                        washingMachineModule.setCurrentMode(null);
                        reportWashingMachine(client, device, washingMachineModule);
                    }
                } else if (current != null && !current.isOverride()) {
                    if (!statusModule.isOn()) {
                        statusModule.setOn(true);
                        changeStatus(client, device, statusModule);
                    }
                    handleMessage(device, "", current.getCommand(), null);
                }

                if (statusModule.isOn()) {
                    reportWashingMachine(client, device, washingMachineModule);
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

    private static void reportWashingMachine(MqttClient client, DeviceDTO device, WashingMachineModule module) {
        String topic = "washing_machine";
        String message = device.getId().toString() + ","+ module.getCurrentMode() + "," + module.getCurrentStart() + "," + module.getCurrentEnd();
        logger.info("Sending to topic \"{}\" message {}", topic, message);
        try {
            client.publish(topic, new MqttMessage(message.getBytes()));
        } catch (MqttException e) {
            logger.error("Device {} failed to send a message", device.getId());
        }
    }

    private static void endSchedule(MqttClient client, DeviceDTO device, Schedule schedule) {
        String topic = "wm_schedule_end";
        String message = device.getId().toString() + "," + schedule.getStartTime() + "," + schedule.getEndTime();
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
        if (action.equals("SET_STATUS")) {
            boolean isOn = Boolean.parseBoolean(tmp[1]);
            StatusModule module = device.getStatusModule();
            module.setOn(isOn);
            Main.acknowledgeSetStatus(device, tmp[2], tmp[1], client);
        }
        else if (action.equals("SET_MODE")) {
            EWashingMode mode = EWashingMode.valueOf(tmp[1]);
            LocalDateTime start = LocalDateTime.parse(tmp[3]);
            LocalDateTime end = LocalDateTime.parse(tmp[5]);

            WashingMachineModule module = device.getWashingMachineModule();
            module.setCurrentStart(start);
            module.setCurrentEnd(end);
            module.setCurrentMode(mode);
        }
        else if (action.equals("SET_SCHEDULE")) {
            EWashingMode mode = EWashingMode.valueOf(tmp[1]);
            LocalDateTime start = LocalDateTime.parse(tmp[2]);
            LocalDateTime end = LocalDateTime.parse(tmp[3]);
            Boolean isRepeat = Boolean.parseBoolean(tmp[4]);
            String command = tmp[5].replace(",", "|");

            Schedule s = new Schedule();
            s.setCommand(command);
            s.setStartTime(start);
            s.setEndTime(end);
            s.setOverride(false);
            s.setRepeat(isRepeat);

            SchedulingModule module = device.getSchedulingModule();
            module.getScheduleList().add(s);
            logger.info("ADDED SCHEDULE WITH COMMAND: " + s.getCommand());
        }
        else if (action.equals("REMOVE_SCHEDULE")) {
            LocalDateTime start = LocalDateTime.parse(tmp[1]);
            LocalDateTime end = LocalDateTime.parse(tmp[2]);

            Schedule found = null;
            SchedulingModule module = device.getSchedulingModule();
            for (Schedule s : module.getScheduleList()) {
                if (s.getStartTime().equals(start) && s.getEndTime().equals(end)) {
                    found = s;
                    break;
                }
            }
            logger.info("SCHEDULE: " + found);
            if (found != null) {
                logger.info("Removed scheduled");
                module.getScheduleList().remove(found);
                WashingMachineModule m = device.getWashingMachineModule();
                m.setCurrentStart(null);
                m.setCurrentEnd(null);
                m.setCurrentMode(null);
            }
        }
    }
}
