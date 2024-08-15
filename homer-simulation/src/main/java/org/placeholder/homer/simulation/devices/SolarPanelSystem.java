package org.placeholder.homer.simulation.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.placeholder.homer.simulation.Main;
import org.placeholder.homer.simulation.MqttConfiguration;
import org.placeholder.homer.simulation.dto.DeviceDTO;
import org.placeholder.homer.simulation.dto.modules.SolarPanelSystemModule;
import org.placeholder.homer.simulation.dto.modules.StatusModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Random;

public class SolarPanelSystem {
    private static final Logger logger = LoggerFactory.getLogger(SolarPanelSystem.class);
    private static final Random random = new Random();
    private static final ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public static void startSimulatingSolarPanelSystem(DeviceDTO device) {
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

                StatusModule statusModule = device.getStatusModule();
                SolarPanelSystemModule solarPanelSystemModule = device.getSolarPanelSystemModule();

                long powerProductionReportInterval = 60 * 1000;
                long lastReport = System.currentTimeMillis();

                while (true) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime > lastReport + powerProductionReportInterval) {
                        if (statusModule.isOn()) {
                            reportPowerProduction(client, device, solarPanelSystemModule);
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
            }
        });
        t.start();
    }

    private static int noSunLow = 7;
    private static int firstSunnyHour = 10;
    private static int lastSunnyHour = 15;
    private static int sunnyHours = lastSunnyHour - firstSunnyHour + 1;
    private static int noSunHigh = 19;

    private static double integral(double x){
        if(x < noSunLow){
            return 0;
        }else if (x < firstSunnyHour){
            return 0.05 * (x - noSunLow) / (firstSunnyHour - noSunLow);
        }else if (x < lastSunnyHour + 1){
            return 0.05 + 0.9 * (x - firstSunnyHour) / sunnyHours;
        } else if (x < noSunHigh) {
            return 0.05 + 0.9 + 0.05 * (x - lastSunnyHour + 1) / (noSunHigh - lastSunnyHour - 1);
        }else {
            return 1;
        }
    }
    private static double getSunExposure(double hour) {
        return integral(hour) - integral(hour - 1.0/60);
    }
    private static double getProducedPower(SolarPanelSystemModule module) {
        Calendar calendar = Calendar.getInstance();
        double hour = calendar.get(Calendar.HOUR_OF_DAY);
        double minute = calendar.get(Calendar.MINUTE);
        hour += minute / 60;
        return module.getNumPanels() * module.getEfficiency() / 100 * module.getArea() * getSunExposure(hour) * sunnyHours;
    }
    private static void reportPowerProduction(MqttClient client, DeviceDTO device, SolarPanelSystemModule module) {
        String topic = "production";
        double produced = getProducedPower(module);
        String message = device.getId().toString() + "," + produced;
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
            device.getStatusModule().setOn(Boolean.parseBoolean(tmp[1]));
            Main.acknowledgeSetStatus(device, tmp[2], tmp[1], client);
        }
    }
}
