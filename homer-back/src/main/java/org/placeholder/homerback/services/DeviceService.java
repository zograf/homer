package org.placeholder.homerback.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.influxdb.client.JSON;
import org.apache.juli.logging.Log;
import org.eclipse.paho.mqttv5.client.IMqttClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.placeholder.homerback.dtos.DeviceDTO;
import org.placeholder.homerback.dtos.NewDeviceDTO;
import org.placeholder.homerback.entities.*;
import org.placeholder.homerback.entities.modules.*;
import org.placeholder.homerback.repositories.*;
import org.placeholder.homerback.websockets.WebsocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DeviceService {

    @Autowired private IDeviceRepository deviceRepository;
    @Autowired private IScheduleRepository scheduleRepository;
    @Autowired private IPropertyRepository propertyRepository;
    @Autowired private IUserRepository userRepository;
    @Autowired private ApplicationEventPublisher eventPublisher;
    @Autowired private ImageService imageService;
    @Autowired private ModuleService moduleService;
    @Autowired private InfluxDBService influxDBService;
    @Autowired private WebsocketHandler websocketHandler;

    @Autowired private ISlotInfoRepository slotInfoRepository;

    @Value("${imagePath}") private String imagePath;

    private static final Logger logger = LoggerFactory.getLogger(DeviceService.class);


    private void saveImage(Integer deviceId, MultipartFile image) throws IOException {
        String[] imageSlice = image.getOriginalFilename().split("\\.");
        String extension = imageSlice[imageSlice.length - 1];

        String name = imageService.getName(deviceId, EImageType.DEVICE_IMAGE);
        logger.info(name);
        File file = new File(imagePath,name + "." + extension);

        logger.info("Image {}, size {}", image.getOriginalFilename(), image.getSize());
        image.transferTo(file);
        logger.info("Transfered image to file");
    }

    public Device createDevice(NewDeviceDTO dto) {
        Device device = getDevice(dto);
        device = save(device);
        return device;
    }

    public Device create(NewDeviceDTO dto, MultipartFile image) {
        Device device = createDevice(dto);

        try{saveImage(device.getId(), image);}
        catch (Exception ex){
            logger.error("Failed to save image");
            deviceRepository.delete(device);
            ex.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid image");
        }

        logger.info("Device {} {} {} {} {} {}", device.getId(), device.getType().toString(), device.getPowerSupply().toString(),
                device.getConsumption(), device.getProperty().getId(), device.getLastHeartbeat());
        return device;
    }

    private Device getDevice(NewDeviceDTO dto) {
        Optional<Property> property = propertyRepository.findById(dto.getPropertyId());
        if(property.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Property not found!");

        Device device = new Device();
        device.setName(dto.getName());
        device.setProperty(property.get());
        device.setType(dto.getType());
        device.setPowerSupply(dto.getPowerSupply());
        device.setConsumption(dto.getConsumption());

        if (dto.getType().equals(EDeviceType.SOLAR_PANEL_SYSTEM)) {
            device.getModules().add(moduleService.createStatusModule(true));
            device.getModules().add(moduleService.createSolarPanelSystemModule(dto.getNumPanels(),dto.getArea(), dto.getEfficiency()));
        }
        else if(dto.getType().equals(EDeviceType.BATTERY)) {
            device.getModules().add(moduleService.createBatteryModule(dto.getCapacity()));
        }
        else if(dto.getType().equals(EDeviceType.EV_CHARGER)) {
            device.getModules().add(moduleService.createEVChargerModule(dto.getPower(), dto.getSlots()));
            device.getModules().add(moduleService.createSlotsModule(dto.getSlots()));
        }
        else if (dto.getType().equals(EDeviceType.LAMP)) {
            device.getModules().add(moduleService.createStatusModule(false));
            device.getModules().add(moduleService.createAmbientLightModule(true, 30));
        }
        else if (dto.getType().equals(EDeviceType.AMBIENT_SENSOR)) {
            device.getModules().add(moduleService.createAmbientSensorModule());
        }
        else if (dto.getType().equals(EDeviceType.AIR_CONDITIONER)) {
            device.getModules().add(moduleService.createAirConditionerModule());
            device.getModules().add(moduleService.createSchedulingModule());
            device.getModules().add(moduleService.createStatusModule(true));
        }
        else if (dto.getType().equals(EDeviceType.GATE)) {
            device.getModules().add(new SmartGateModule(dto.getPlates()));
        }
        else if (dto.getType().equals(EDeviceType.WASHING_MACHINE)) {
            device.getModules().add(moduleService.createWashingMachineModule());
            device.getModules().add(moduleService.createSchedulingModule());
            device.getModules().add(moduleService.createStatusModule(false));
        }
        else if (dto.getType().equals(EDeviceType.SPRINKLER_SYSTEM)) {
            device.getModules().add(moduleService.createStatusModule(false));
            device.getModules().add(new SprinklerScheduleModule());
        }

        return device;
    }

    public void heartbeat(String message) {
        int deviceId = Integer.parseInt(message);
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (!deviceOpt.isEmpty()) {
            Device device = deviceOpt.get();
            boolean wasOnline = device.isOnline();
            if (device.getLastHeartbeat() != 0 && !wasOnline) {
                influxDBService.saveOnlineChange(device, device.getLastHeartbeat() + 30 * 1000, false);
            }
            device.setLastHeartbeat(System.currentTimeMillis());
            save(device);

            if (!wasOnline) {
                influxDBService.saveOnlineChange(device, device.getLastHeartbeat(), true);
                sendWebsocketMessage(device);
            }
        }
    }

    public List<DeviceDTO> getAllDevices() {
        return deviceRepository.findAll().stream().map(DeviceDTO::new).toList();
    }

    public List<DeviceDTO> getAllDevicesForProperty(int propertyId) {
        return deviceRepository.findAllByPropertyId(propertyId).stream().map(DeviceDTO::new).toList();
    }

    public Device save(Device device) {
        return deviceRepository.save(device);
    }

    public void producePower(String message, IMqttClient client) {
        String[] tmp = message.split(",");
        int deviceId = Integer.parseInt(tmp[0]);
        double powerProduced = Double.parseDouble(tmp[1]);
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (!deviceOpt.isEmpty()) {
            Device device = deviceOpt.get();
            Property property = device.getProperty();
            List<Device> batteries = deviceRepository.findAllByTypeAndPropertyId(EDeviceType.BATTERY, property.getId());
            double totalCapacity = 0.0;
            for (Device battery : batteries) {
                BatteryModule batteryModule = (BatteryModule) battery.getModule(EModuleType.BATTERY);
                logger.info("Battery module {} {} {}", batteryModule.getCapacity(), batteryModule.getValue(), batteryModule.getPercent());
                totalCapacity += batteryModule.getCapacity();
            }
            logger.info("Total capacity {}", totalCapacity);
            for (Device battery : batteries) {
                BatteryModule batteryModule = (BatteryModule) battery.getModule(EModuleType.BATTERY);
                Double delta = powerProduced * batteryModule.getCapacity() / totalCapacity;
                logger.info("Delta {}, battery {}", delta, battery.getId());
                fillBattery(battery, delta);
            }

            logger.info("Device {} produced {} kWh", device.getId(), powerProduced);

            influxDBService.saveProducedPower(device, powerProduced);
            sendWebsocketMessage(device);
        }
    }

    public Device turnOnOff(Integer deviceId, Boolean on, IMqttClient client, User user) throws MqttException {
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (deviceOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        Device device = deviceOpt.get();
        StatusModule statusModule = null;
        for (AbstractModule module : device.getModules()) {
            if (module.getType().equals(EModuleType.STATUS)) {
                statusModule = (StatusModule) module;
                break;
            }
        }
        if (statusModule == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if (statusModule.isOn() != on) {
            //moduleService.turnOnOff(statusModule, on);
            //device = save(device);
            String topic = deviceId.toString();
            String message = "SET_STATUS|" + on + "|" + user.getId().toString();
            client.publish(topic, new MqttMessage(message.getBytes()));

            //influxDBService.saveTurnOnOffAction(user, device, on);
        }
        //sendWebsocketMessage(device);
        return device;
    }

    public void updateBatteryState(String message) {
        String[] tmp = message.split(",");
        int deviceId = Integer.parseInt(tmp[0]);
        double value = Double.parseDouble(tmp[1]);
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (!deviceOpt.isEmpty()) {
            Device device = deviceOpt.get();
            BatteryModule batteryModule = null;
            for (AbstractModule module : device.getModules()) {
                if (module.getType() == EModuleType.BATTERY) {
                    batteryModule = (BatteryModule) module;
                }
            }
            if (batteryModule != null) {
                value = batteryModule.getValue();
                //device = save(device);
                logger.info("Battery {} has value {}", device.getId(), value);

                influxDBService.saveBatteryState(device, value);
                sendWebsocketMessage(device);
            }
        }
    }

    public void electricityDistributionDelta(String message) {
        /*String[] tmp = message.split(",");
        int deviceId = Integer.parseInt(tmp[0]);
        double delta = Double.parseDouble(tmp[1]);
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (!deviceOpt.isEmpty()) {
            Device device = deviceOpt.get();

            logger.info("Battery {} forwarded {} energy to electricity distribution", device.getId(), delta);

            sendWebsocketMessage(device);
            influxDBService.saveElectricityDistributionDelta(device, delta);
        }*/
    }

    public void fillBattery(Device battery, Double delta) {
        BatteryModule batteryModule = (BatteryModule) battery.getModule(EModuleType.BATTERY);
        double value = batteryModule.getValue() + delta;
        if (value < 0.0) {
            influxDBService.saveElectricityDistributionDelta(battery, value);
            value = 0.0;
        } else if (value > batteryModule.getCapacity()) {
            influxDBService.saveElectricityDistributionDelta(battery, value - batteryModule.getCapacity());
            value = batteryModule.getCapacity();
        }
        batteryModule.setValue(value);
        battery = save(battery);
        sendWebsocketMessage(battery);
    }

    public void consumePower(String message, IMqttClient client) {
        String[] tmp = message.split(",");
        int deviceId = Integer.parseInt(tmp[0]);
        double powerConsumed = Double.parseDouble(tmp[1]);
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (!deviceOpt.isEmpty()) {
            Device device = deviceOpt.get();
            if (device.getPowerSupply() != EPowerSupply.HOME){
                return;
            }
            Property property = device.getProperty();
            List<Device> batteries = deviceRepository.findAllByTypeAndPropertyId(EDeviceType.BATTERY, property.getId());
            double totalCapacity = 0.0;
            for (Device battery : batteries) {
                BatteryModule batteryModule = (BatteryModule) battery.getModule(EModuleType.BATTERY);
                logger.info("Battery module {} {} {}", batteryModule.getCapacity(), batteryModule.getValue(), batteryModule.getPercent());
                totalCapacity += batteryModule.getCapacity();
            }
            logger.info("Total capacity {}", totalCapacity);
            for (Device battery : batteries) {
                BatteryModule batteryModule = (BatteryModule) battery.getModule(EModuleType.BATTERY);
                Double delta = -powerConsumed * batteryModule.getCapacity() / totalCapacity;
                logger.info("Delta {}, battery {}", delta, battery.getId());
                fillBattery(battery, delta);
            }

            logger.info("Device {} consumed {} kWh", device.getId(), powerConsumed);

            influxDBService.saveConsumedPower(device, powerConsumed);
            sendWebsocketMessage(device);
        }
    }

    public Device fillToPercent(Integer deviceId, Double percent, IMqttClient client, User user) throws MqttException {
        logger.info("Fill to percent deviceId {} percent {}", deviceId, percent);
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (deviceOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        Device device = deviceOpt.get();
        EVChargerModule evModule = (EVChargerModule) device.getModule(EModuleType.EV_CHARGER);
        if (evModule == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        evModule.setFillToPercent(percent);
        device = save(device);
        String topic = deviceId.toString();
        String message = percent.toString();
        client.publish(topic, new MqttMessage(message.getBytes()));

        influxDBService.saveFillToPercentAction(user, device, percent);

        sendWebsocketMessage(device);
        return device;
    }

    public void startCharging(String message) {
        String[] tmp = message.split(",");
        Integer chargerId = Integer.parseInt(tmp[0]);
        Double capacity = Double.parseDouble(tmp[1]);
        Double percent = Double.parseDouble(tmp[2]);
        Integer slot = Integer.parseInt(tmp[3]);

        Optional<Device> deviceOpt = deviceRepository.findById(chargerId);
        if (!deviceOpt.isEmpty()) {
            Device device = deviceOpt.get();
            EVChargerModule module = (EVChargerModule) device.getModule(EModuleType.EV_CHARGER);
            SlotsModule slots = (SlotsModule) device.getModule(EModuleType.SLOTS);
            if (module != null && slots != null) {
                logger.info("Charger {} started charging car with capacity {} at {}%", device.getId(), capacity, percent);

                influxDBService.saveStartChargingAction(device, capacity, percent, slot);
                influxDBService.saveOccupiedSlots(device, module.getOccupiedSlots() + 1);

                Optional<SlotInfo> slotInfoOpt = slots.getSlots().stream().filter((info) -> info.getSlot().equals(slot)).findFirst();
                if (slotInfoOpt.isPresent()) {
                    SlotInfo slotInfo = slotInfoOpt.get();

                    slots.getSlots().remove(slotInfo);

                    slotInfo.setCapacity(capacity);
                    slotInfo.setPercent(percent);
                    slotInfo.setOccupied(true);
                    slotInfo = slotInfoRepository.save(slotInfo);

                    slots.getSlots().add(slotInfo);

                    module.setOccupiedSlots(module.getOccupiedSlots() + 1);
                    device = save(device);
                    sendWebsocketMessage(device);
                }
            }
        }
    }

    public void progressCharging(String message) {
        String[] tmp = message.split(",");
        Integer chargerId = Integer.parseInt(tmp[0]);
        Double capacity = Double.parseDouble(tmp[1]);
        Double percent = Double.parseDouble(tmp[2]);
        Integer slot = Integer.parseInt(tmp[3]);

        Optional<Device> deviceOpt = deviceRepository.findById(chargerId);
        if (!deviceOpt.isEmpty()) {
            Device device = deviceOpt.get();
            EVChargerModule module = (EVChargerModule) device.getModule(EModuleType.EV_CHARGER);
            SlotsModule slots = (SlotsModule) device.getModule(EModuleType.SLOTS);
            if (module != null && slots != null) {
                logger.info("Charging progress for charger {} and car with capacity {} is {}%", device.getId(), capacity, percent);

                influxDBService.saveChargingProgress(device, percent, slot);

                Optional<SlotInfo> slotInfoOpt = slots.getSlots().stream().filter((info) -> info.getSlot().equals(slot)).findFirst();
                if (slotInfoOpt.isPresent()) {
                    SlotInfo slotInfo = slotInfoOpt.get();

                    slots.getSlots().remove(slotInfo);

                    slotInfo.setPercent(percent);
                    slotInfo = slotInfoRepository.save(slotInfo);

                    slots.getSlots().add(slotInfo);

                    device = save(device);
                    sendWebsocketMessage(device);
                }
            }
        }
    }

    public void stopCharging(String message) {
        String[] tmp = message.split(",");
        Integer chargerId = Integer.parseInt(tmp[0]);
        Double capacity = Double.parseDouble(tmp[1]);
        Double consumed = Double.parseDouble(tmp[2]);
        Integer slot = Integer.parseInt(tmp[3]);

        Optional<Device> deviceOpt = deviceRepository.findById(chargerId);
        if (!deviceOpt.isEmpty()) {
            Device device = deviceOpt.get();
            EVChargerModule module = (EVChargerModule) device.getModule(EModuleType.EV_CHARGER);
            SlotsModule slots = (SlotsModule) device.getModule(EModuleType.SLOTS);
            if (module != null && slots != null) {
                logger.info("Charger {} stopped charging car with capacity {}. Charging consumed {} kWh", device.getId(), capacity, consumed);

                influxDBService.saveStopChargingAction(device, capacity, consumed, slot);
                influxDBService.saveOccupiedSlots(device, module.getOccupiedSlots() - 1);

                Optional<SlotInfo> slotInfoOpt = slots.getSlots().stream().filter((info) -> info.getSlot().equals(slot)).findFirst();
                if (slotInfoOpt.isPresent()) {
                    SlotInfo slotInfo = slotInfoOpt.get();

                    slots.getSlots().remove(slotInfo);

                    slotInfo.setOccupied(false);
                    slotInfo = slotInfoRepository.save(slotInfo);

                    slots.getSlots().add(slotInfo);

                    module.setOccupiedSlots(module.getOccupiedSlots() - 1);
                    device = save(device);
                    sendWebsocketMessage(device);
                }
            }
        }
    }

    public Device changeAmbientModuleAutoBrightness(Integer deviceId, boolean auto, IMqttClient client, User user) throws MqttException {
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (deviceOpt.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        Device device = deviceOpt.get();

        AmbientLightModule module = (AmbientLightModule) device.getModule(EModuleType.AMBIENT_LIGHT);
        if (module == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        if (module.isAutoStatus() == auto) return device;

        moduleService.changeAutoBrightness(module, auto);
        String topic = deviceId.toString();
        String message = "SET_AUTO_BRIGHTNESS|" + auto;
        client.publish(topic, new MqttMessage(message.getBytes()));
        influxDBService.saveLampAutoAction(user, device, auto);

        sendWebsocketMessage(device);
        return device;
    }

    public Device updateAirConditioner(Integer deviceId, Integer temperature, EACMode mode, IMqttClient client, User user) throws MqttException {
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (deviceOpt.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        Device device = deviceOpt.get();

        AirConditionerModule module = (AirConditionerModule) device.getModule(EModuleType.AIR_CONDITIONER);
        SchedulingModule schedulingModule = (SchedulingModule) device.getModule(EModuleType.SCHEDULING);
        if (module == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        Integer t = temperature == null ? module.getCurrentTemperature() : temperature;
        EACMode m = mode == null ? module.getCurrentMode() : mode;

        if (temperature != null) {
            logger.info("SAVED TEMP AS USER");
            influxDBService.saveAcTemperatureAction(user, device, temperature);
            module.setCurrentTemperature(temperature);
        }
        if (mode != null) {
            influxDBService.saveAcModeAction(user, device, mode);
            module.setCurrentMode(mode);
        }

        Schedule found = null;
        LocalDateTime now = LocalDateTime.now();
        for (Schedule s : schedulingModule.getScheduleList()) {
            if (s.getStartTime().isBefore(now) && s.getEndTime().isAfter(now)) {
                found = s;
                break;
            }
        }
        if (found != null) {
            String topic = deviceId.toString();
            String message;
            if (found.isRepeat()) {
                found.setOverride(true);
                scheduleRepository.save(found);
                message = "OVERRIDE_SCHEDULE|" + found.getStartTime() + "|" + found.getEndTime();
            } else {
                schedulingModule.getScheduleList().remove(found);
                moduleService.save(schedulingModule);
                influxDBService.saveScheduleEndAction(user, device, found);
                scheduleRepository.delete(found);
                message = "REMOVE_SCHEDULE|" + found.getStartTime() + "|" + found.getEndTime();
            }
            client.publish(topic, new MqttMessage(message.getBytes()));
        }

        String topic = deviceId.toString();
        String message = "SET_MODE|" + m + "|SET_TEMPERATURE|" + t;
        client.publish(topic, new MqttMessage(message.getBytes()));
        moduleService.save(module);
        sendWebsocketMessage(device);
        return save(device);
    }
    public Device removeACSchedule(Integer deviceId, Integer scheduleId, IMqttClient client, User user) throws MqttException {
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (deviceOpt.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        Device device = deviceOpt.get();

        SchedulingModule module = (SchedulingModule) device.getModule(EModuleType.SCHEDULING);
        AirConditionerModule acModule = (AirConditionerModule) device.getModule(EModuleType.AIR_CONDITIONER);
        if (module == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        Schedule found = null;
        for (Schedule s : module.getScheduleList()) {
            if (s.getId() == scheduleId) {
                found = s;
                break;
            }
        }
        if (found != null) {
            String topic = deviceId.toString();
            String message;
            module.getScheduleList().remove(found);
            message = "REMOVE_SCHEDULE|" + found.getStartTime() + "|" + found.getEndTime();
            client.publish(topic, new MqttMessage(message.getBytes()));
            influxDBService.saveScheduleEndAction(user, device, found);
            if (found.getStartTime().isBefore(LocalDateTime.now()) && found.getEndTime().isAfter(LocalDateTime.now())) {
                acModule.setCurrentMode(null);
                acModule.setCurrentTemperature(null);
            }
            moduleService.save(module);
            scheduleRepository.delete(found);
        }
        sendWebsocketMessage(device);
        return save(device);
    }

    public Device addACSchedule(Integer deviceId, Integer temperature, EACMode mode, LocalDateTime start,
                                LocalDateTime end, Boolean isRepeat, IMqttClient client, User user) throws MqttException {
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (deviceOpt.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        Device device = deviceOpt.get();

        SchedulingModule module = (SchedulingModule) device.getModule(EModuleType.SCHEDULING);
        if (module == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        String topic = deviceId.toString();
        Schedule s = new Schedule();
        s.setStartTime(start);
        s.setEndTime(end);
        s.setOverride(false);
        s.setRepeat(isRepeat);
        s.setCommand("SET_MODE|" + mode + "|SET_TEMPERATURE|" + temperature);

        logger.info(s.isRepeat().toString());
        if (s.isOverlap(module.getScheduleList())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        scheduleRepository.save(s);
        influxDBService.saveScheduleAddAction(user, device, s);
        module.getScheduleList().add(s);
        moduleService.save(module);
        String message = "SET_SCHEDULE|" + temperature + "|" + mode + "|" + start + "|" + end + "|" + isRepeat + "|" + s.getCommand().replace("|", ",");
        client.publish(topic, new MqttMessage(message.getBytes()));

        sendWebsocketMessage(device);
        return save(device);
    }

    public Device setSprinklerSchedule(Integer deviceId, LocalTime from, LocalTime to, String days, IMqttClient mqttClient, User user) throws MqttException {
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (deviceOpt.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        Device device = deviceOpt.get();

        SprinklerScheduleModule module = (SprinklerScheduleModule) device.getModule(EModuleType.SPRINKLER_SCHEDULE);
        StringBuilder newDays = new StringBuilder();
        for(int i = 0; i < 7; i++) {
            if (days.toCharArray()[i] == '1' || module.getDays().toCharArray()[i] == '1') newDays.append('1');
            else newDays.append("0");

            if (days.toCharArray()[i] == '1') {
                switch (i) {
                    case 0:
                        module.setMonStartTime(from);
                        module.setMonEndTime(to);
                        break;
                    case 1:
                        module.setTueStartTime(from);
                        module.setTueEndTime(to);
                        break;
                    case 2:
                        module.setWedStartTime(from);
                        module.setWedEndTime(to);
                        break;
                    case 3:
                        module.setThuStartTime(from);
                        module.setThuEndTime(to);
                        break;
                    case 4:
                        module.setFriStartTime(from);
                        module.setFriEndTime(to);
                        break;
                    case 5:
                        module.setSatStartTime(from);
                        module.setSatEndTime(to);
                        break;
                    case 6:
                        module.setSunStartTime(from);
                        module.setSunEndTime(to);
                        break;
                }
            }
        }
        module.setDays(newDays.toString());
        moduleService.save(module);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        String topic = deviceId.toString();
        String message = null;
        try {
            message = "SET_SCHEDULE|" + mapper.writeValueAsString(module);
            mqttClient.publish(topic, new MqttMessage(message.getBytes()));
        }
        catch (JsonProcessingException e) {
            logger.error("Error sending schedule update. Msg: " + e.getMessage());
        }

        return save(device);
    }


    public Device removeSprinklerSchedule(Integer deviceId, Integer dayIdx, IMqttClient mqttClient, User user) throws MqttException {
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (deviceOpt.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        Device device = deviceOpt.get();

        SprinklerScheduleModule module = (SprinklerScheduleModule) device.getModule(EModuleType.SPRINKLER_SCHEDULE);
        StringBuilder newDays = new StringBuilder();
        for(int i = 0; i < 7; i++) {
            if (i != dayIdx) newDays.append(module.getDays().toCharArray()[i]);
            else newDays.append("0");
        }
        module.setDays(newDays.toString());
        moduleService.save(module);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        String topic = deviceId.toString();
        String message = null;
        try {
            message = "SET_SCHEDULE|" + mapper.writeValueAsString(module);
            mqttClient.publish(topic, new MqttMessage(message.getBytes()));
        }
        catch (JsonProcessingException e) {
            logger.error("Error sending schedule update. Msg: " + e.getMessage());
        }

        return save(device);
    }

    public void changeAmbientLightAmount(String message) {
        String[] tmp = message.split(",");
        Integer deviceId = Integer.parseInt(tmp[0]);
        Integer lightAmount = Integer.parseInt(tmp[1]);

        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (!deviceOpt.isEmpty()) {
            Device device = deviceOpt.get();
            AmbientLightModule module = (AmbientLightModule) device.getModule(EModuleType.AMBIENT_LIGHT);
            if(module != null) {
                module.setLightPresence(lightAmount);
                sendWebsocketMessage(device);
                device = save(device);
                logger.info("Light amount on device by name {} changed to {} ", device.getName(), lightAmount);
                influxDBService.saveAmbientLightPresence(device, lightAmount);
            }
        }
    }

    public void changeStatusModule(String message) {
        String[] tmp = message.split(",");
        Integer deviceId = Integer.parseInt(tmp[0]);
        boolean isOn = Boolean.parseBoolean(tmp[1]);

        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (!deviceOpt.isEmpty()) {
            Device device = deviceOpt.get();
            StatusModule module = (StatusModule) device.getModule(EModuleType.STATUS);
            if(module != null) {
                module.setOn(isOn);
                sendWebsocketMessage(device);
                device = save(device);
                logger.info("Changing the device ({}) status automatically..", device.getName());
                influxDBService.saveTurnOnOffAction(null, device, isOn);
            }
        }
    }

    public void readSensorValues(String message) {
        String[] tmp = message.split(",");
        Integer deviceId = Integer.parseInt(tmp[0]);
        Double temperature = Double.parseDouble(tmp[1]);
        Double humidityPercent = Double.parseDouble(tmp[2]);

        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (!deviceOpt.isEmpty()) {
            Device device = deviceOpt.get();
            AmbientSensorModule module = (AmbientSensorModule) device.getModule(EModuleType.AMBIENT_SENSOR);
            if(module != null) {
                module.setTemperatureValue(temperature);
                module.setHumidityPercent(humidityPercent);
                sendWebsocketMessage(device);
                device = save(device);
                logger.info("Sensor readings: device by name {} temperature: {}C, humidity: {}%", device.getName(), temperature, humidityPercent);
                influxDBService.saveAmbientSensorReading(device, temperature, humidityPercent);
            }
        }
    }

    public void readAirConditionerMessage(String message) {
        String[] tmp = message.split(",");
        Integer deviceId = Integer.parseInt(tmp[0]);
        EACMode mode = EACMode.valueOf(tmp[1]);
        Integer temperature = Integer.parseInt(tmp[2]);

        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (!deviceOpt.isEmpty()) {
            Device device = deviceOpt.get();
            AirConditionerModule module = (AirConditionerModule) device.getModule(EModuleType.AIR_CONDITIONER);
            if(module != null) {
                if (tmp[1].equals("null")) {
                    module.setCurrentTemperature(null);
                    module.setCurrentMode(null);
                } else {
                    if (temperature != module.getCurrentTemperature()) {
                        SchedulingModule schedulingModule = (SchedulingModule) device.getModule(EModuleType.SCHEDULING);
                        Schedule found = null;
                        for (Schedule s : schedulingModule.getScheduleList()) {
                            if (s.getStartTime().isBefore(LocalDateTime.now()) && s.getEndTime().isAfter(LocalDateTime.now())) {
                                found = s;
                                break;
                            }
                        }
                        if (found != null) {
                            influxDBService.saveScheduleStartAction(null, device, found);
                        }
                        influxDBService.saveAcTemperatureAction(null, device, temperature);
                    }
                    if (mode != module.getCurrentMode()) {
                        influxDBService.saveAcModeAction(null, device, mode);
                    }
                    module.setCurrentTemperature(temperature);
                    module.setCurrentMode(mode);
                }
                moduleService.save(module);
                sendWebsocketMessage(device);
                device = save(device);
            }
        }
    }

    public void airConditionerScheduleEnd(String message) {
        String[] tmp = message.split(",");
        Integer deviceId = Integer.parseInt(tmp[0]);
        LocalDateTime start = LocalDateTime.parse(tmp[1]);
        LocalDateTime end = LocalDateTime.parse(tmp[2]);

        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (!deviceOpt.isEmpty()) {
            Device device = deviceOpt.get();
            SchedulingModule module = (SchedulingModule) device.getModule(EModuleType.SCHEDULING);
            AirConditionerModule acModule = (AirConditionerModule) device.getModule(EModuleType.AIR_CONDITIONER);
            if(module != null) {
                Schedule found = null;
                for (Schedule s : module.getScheduleList()) {
                    if (s.getStartTime().equals(start) && s.getEndTime().equals(end)) {
                        found = s;
                        break;
                    }
                }
                if (found != null && !found.isRepeat()) {
                    module.getScheduleList().remove(found);
                }
                acModule.setCurrentTemperature(null);
                acModule.setCurrentMode(null);
                influxDBService.saveScheduleEndAction(null, device, found);
                moduleService.save(module);
                moduleService.save(acModule);
                scheduleRepository.delete(found);
                sendWebsocketMessage(device);
                device = save(device);
            }
        }
    }

    public Device changeIsOpen(Integer deviceId, Boolean isOpen, IMqttClient client, User user) throws MqttException {
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (deviceOpt.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        Device device = deviceOpt.get();

        SmartGateModule module = (SmartGateModule) device.getModule(EModuleType.SMART_GATE);
        if (module == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        if (module.isOpen() == isOpen) return device;

        module.setOpen(isOpen);
        moduleService.save(module);

        String topic = deviceId.toString();
        String message = "SET_IS_OPEN|" + isOpen;
        client.publish(topic, new MqttMessage(message.getBytes()));

        influxDBService.saveGateOpenCloseAction(user, device, isOpen);

        sendWebsocketMessage(device);
        return device;
    }

    public Device changeIsPrivate(Integer deviceId, Boolean isPrivate, IMqttClient client, User user) throws MqttException {
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (deviceOpt.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        Device device = deviceOpt.get();

        SmartGateModule module = (SmartGateModule) device.getModule(EModuleType.SMART_GATE);
        if (module == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        if (module.isPrivate() == isPrivate) return device;

        module.setPrivate(isPrivate);
        moduleService.save(module);

        String topic = deviceId.toString();
        String message = "SET_IS_PRIVATE|" + isPrivate;
        client.publish(topic, new MqttMessage(message.getBytes()));
        influxDBService.saveGateIsPrivateAction(user, device, isPrivate);

        sendWebsocketMessage(device);
        return device;
    }

    public Device addPlate(Integer deviceId, String text, IMqttClient client, User user) throws MqttException {
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (deviceOpt.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        Device device = deviceOpt.get();

        SmartGateModule module = (SmartGateModule) device.getModule(EModuleType.SMART_GATE);
        if (module == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        module.getPlates().add(new Plate(text));
        moduleService.save(module);

        String topic = deviceId.toString();
        String message = "SET_PLATES|" + module.getPlates().stream().map((plate -> plate.getNonNullId().toString() + "," + plate.getText() )).collect(Collectors.joining(";"));
        client.publish(topic, new MqttMessage(message.getBytes()));

        sendWebsocketMessage(device);
        return device;
    }

    public Device deletePlate(Integer deviceId, Integer plateId, IMqttClient client, User user) throws MqttException {
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (deviceOpt.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        Device device = deviceOpt.get();

        SmartGateModule module = (SmartGateModule) device.getModule(EModuleType.SMART_GATE);
        if (module == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        Optional<Plate> toDelete = module.getPlates().stream().filter(plate -> plate.getId().equals(plateId)).findFirst();
        if(toDelete.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        module.getPlates().remove(toDelete.get());
        moduleService.save(module);

        String topic = deviceId.toString();
        String message = "SET_PLATES|" + module.getPlates().stream().map((plate -> plate.getId().toString() + "," + plate.getText() )).collect(Collectors.joining(";"));
        client.publish(topic, new MqttMessage(message.getBytes()));

        sendWebsocketMessage(device);
        return device;
    }

    private void tryChangeGateIsOpen(Device device, boolean isOpen) {
        SmartGateModule module = (SmartGateModule) device.getModule(EModuleType.SMART_GATE);
        if (module == null) return;
        module.setOpen(isOpen);
        moduleService.save(module);
        influxDBService.saveGateOpenCloseAction(null, device, isOpen);
        sendWebsocketMessage(device);
    }

    public void gateAction(String message) {
        String[] tmp = message.split(",");
        Integer deviceId = Integer.parseInt(tmp[0]);
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (deviceOpt.isEmpty()) return;
        Device device = deviceOpt.get();

        String action = tmp[1];
        if(action.equals("ENTER")) {
            tryChangeGateIsOpen(device, true);
            influxDBService.saveGateEnterExitAction(device, tmp[2], "Enter");
        }
        else if (action.equals("EXIT")) {
            tryChangeGateIsOpen(device, true);
            influxDBService.saveGateEnterExitAction(device, tmp[2], "Exit");

        }
        else if (action.equals("FAIL")) {
            influxDBService.saveGateEnterExitAction(device, tmp[2], "Tried To Enter");
        }
        else if (action.equals("CLOSE")) {
            tryChangeGateIsOpen(device, false);
        }
    }

    public void readWashingMachineMessage(String message) {
        String[] tmp = message.split(",");
        Integer deviceId = Integer.parseInt(tmp[0]);
        if (tmp[1].equals("null")) {
            Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
            if (!deviceOpt.isEmpty()) {
                Device device = deviceOpt.get();
                WashingMachineModule module = (WashingMachineModule) device.getModule(EModuleType.WASHING_MACHINE);
                if(module != null) {
                    module.setCurrentMode(null);
                    module.setCurrentStart(null);
                    module.setCurrentEnd(null);
                    influxDBService.saveWMModeAction(null , device, null);
                    moduleService.save(module);
                    sendWebsocketMessage(device);
                    device = save(device);
                }
            }
            return;
        }
        EWashingMode currentMode = EWashingMode.valueOf(tmp[1]);
        LocalDateTime currentStart = LocalDateTime.parse(tmp[2]);
        LocalDateTime currentEnd = LocalDateTime.parse(tmp[3]);

        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (!deviceOpt.isEmpty()) {
            Device device = deviceOpt.get();
            WashingMachineModule module = (WashingMachineModule) device.getModule(EModuleType.WASHING_MACHINE);
            if(module != null) {
                if (currentMode != module.getCurrentMode()) {
                    influxDBService.saveWMModeAction(null , device, currentMode);
                }
                module.setCurrentMode(currentMode);
                module.setCurrentStart(currentStart);
                module.setCurrentEnd(currentEnd);
                moduleService.save(module);
                sendWebsocketMessage(device);
                device = save(device);
            }
        }
    }

    public void washingMachineScheduleEnd(String message) {
        String[] tmp = message.split(",");
        Integer deviceId = Integer.parseInt(tmp[0]);
        LocalDateTime start = LocalDateTime.parse(tmp[1]);
        LocalDateTime end = LocalDateTime.parse(tmp[2]);

        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (!deviceOpt.isEmpty()) {
            Device device = deviceOpt.get();
            SchedulingModule module = (SchedulingModule) device.getModule(EModuleType.SCHEDULING);
            WashingMachineModule wmModule = (WashingMachineModule) device.getModule(EModuleType.WASHING_MACHINE);
            if(module != null) {
                Schedule found = null;
                for (Schedule s : module.getScheduleList()) {
                    if (s.getStartTime().equals(start) && s.getEndTime().equals(end)) {
                        found = s;
                        break;
                    }
                }
                if (found != null && !found.isRepeat()) {
                    module.getScheduleList().remove(found);
                }
                wmModule.setCurrentStart(null);
                wmModule.setCurrentEnd(null);
                wmModule.setCurrentMode(null);
                moduleService.save(module);
                moduleService.save(wmModule);
                influxDBService.saveScheduleEndAction(null, device, found);
                scheduleRepository.delete(found);
                sendWebsocketMessage(device);
                device = save(device);
            }
        }
    }

    public Device addWMSchedule(Integer deviceId, EWashingMode mode, LocalDateTime start,
                                LocalDateTime end, Boolean isRepeat, IMqttClient client, User user, Boolean isNotSchedule) throws MqttException {
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (deviceOpt.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        Device device = deviceOpt.get();

        SchedulingModule module = (SchedulingModule) device.getModule(EModuleType.SCHEDULING);
        if (module == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        String topic = deviceId.toString();
        Schedule s = new Schedule();
        s.setStartTime(start);
        s.setEndTime(end);
        s.setOverride(false);
        s.setRepeat(isRepeat);
        s.setCommand("SET_MODE|" + mode + "|SET_START|" + start + "|SET_END|" + end);

        if (s.isOverlap(module.getScheduleList())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        if (isNotSchedule) {
            influxDBService.saveWMModeAction(user, device, mode);
        } else {
            influxDBService.saveScheduleAddAction(user, device, s);
        }

        scheduleRepository.save(s);
        module.getScheduleList().add(s);
        moduleService.save(module);
        String message = "SET_SCHEDULE|" + mode + "|" + start + "|" + end + "|" + isRepeat + "|" + s.getCommand().replace("|", ",");
        client.publish(topic, new MqttMessage(message.getBytes()));

        sendWebsocketMessage(device);
        return device;
    }

    public Device removeWMSchedule(Integer deviceId, Integer scheduleId, IMqttClient client, User user) throws MqttException {
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (deviceOpt.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        Device device = deviceOpt.get();

        SchedulingModule module = (SchedulingModule) device.getModule(EModuleType.SCHEDULING);
        WashingMachineModule wmModule = (WashingMachineModule) device.getModule(EModuleType.WASHING_MACHINE);
        if (module == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        Schedule found = null;
        for (Schedule s : module.getScheduleList()) {
            if (s.getId() == scheduleId) {
                found = s;
                break;
            }
        }
        if (found != null) {
            String topic = deviceId.toString();
            String message;
            module.getScheduleList().remove(found);
            message = "REMOVE_SCHEDULE|" + found.getStartTime() + "|" + found.getEndTime();
            wmModule.setCurrentMode(null);
            wmModule.setCurrentStart(null);
            wmModule.setCurrentEnd(null);
            influxDBService.saveScheduleEndAction(user, device, found);
            moduleService.save(module);
            scheduleRepository.delete(found);
            client.publish(topic, new MqttMessage(message.getBytes()));
            logger.info("Sent to washing machine message: " + message);
        }
        sendWebsocketMessage(device);
        return device;
    }

    public void setStatus(String message) {
        String[] tmp = message.split(",");
        Integer deviceId = Integer.parseInt(tmp[0]);
        Integer userId = Integer.parseInt(tmp[1]);
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (deviceOpt.isEmpty()) return;
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return;
        Device device = deviceOpt.get();
        User user = userOpt.get();
        StatusModule module = (StatusModule)device.getModule(EModuleType.STATUS);

        moduleService.turnOnOff(module, Boolean.parseBoolean(tmp[2]));
        device = save(device);
        influxDBService.saveTurnOnOffAction(user, device, module.isOn());
        sendWebsocketMessage(device);
    }

    public void sendWebsocketMessage(Device device) {
        websocketHandler.sendMessageProperty(device.getProperty().getId(), new DeviceDTO(device));
        websocketHandler.sendMessageDevice(device.getId(), new DeviceDTO(device));
    }

}