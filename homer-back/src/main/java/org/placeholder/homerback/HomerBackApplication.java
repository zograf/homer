package org.placeholder.homerback;

import jakarta.annotation.PostConstruct;
import org.eclipse.paho.mqttv5.client.IMqttClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.placeholder.homerback.dtos.NewDeviceDTO;
import org.placeholder.homerback.entities.*;
import org.placeholder.homerback.repositories.*;
import org.placeholder.homerback.services.DeviceService;
import org.placeholder.homerback.services.ModuleService;
import org.placeholder.homerback.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.*;

@SpringBootApplication
@EnableScheduling
public class HomerBackApplication {

    @Autowired
    private UserService userService;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private ModuleService moduleService;

    @Autowired
    private ICountryRepository countryRepo;

    @Autowired
    private ICityRepository cityRepository;
    @Autowired
    private IDeviceRepository deviceRepository;
    @Autowired
    private IPropertyRepository propertyRepository;
    @Autowired
    private IPermissionRepository permissionRepository;
    @Autowired
    private IUserRepository userRepository;
    @Autowired
    private IMqttClient mqttClient;
    @Autowired
    private ApplicationArguments applicationArguments;

    private static Logger logger = LoggerFactory.getLogger(HomerBackApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(HomerBackApplication.class, args);
    }

    @PostConstruct
    public void onApplicationStart() {
        userService.sendAdminPassword();
        populateLocations();
        if(applicationArguments.containsOption("test-size")) {
            createPerfTestData();
        } else {
            createNormalTestData();
        }
        subscribeToTopics();
    }

    private User createUser() {
        User user = new User();
        user.setId(2);
        user.setName("User");
        user.setEmail("user@gmail.com");
        user.setPassword(".GzfzQjNhmkHsaKDrMylP8jAKN3FDeO");
        user.setEnabled(true);
        user.setRole(ERole.ROLE_USER);
        user = userRepository.save(user);
        return user;
    }
    private Property createProperty(User user) {
        Property property = new Property();
        property.setStatus(EPropertyStatus.ACCEPTED);
        property.setArea(100);
        property.setName("Test Property 1");
        property.setCity(cityRepository.findAll().get(0));
        property.setCountry(countryRepo.findAll().get(0));
        property.setStatusDetails("");
        property.setFloors(2);
        property.setLat(0.0);
        property.setLon(0.0);
        property.setStreet("Test Ulica 5");
        property.setUser(user);
        property = propertyRepository.save(property);
        return property;
    }
    private static Random rng = new Random();
    private static String[] types = {"SOLAR_PANEL_SYSTEM", "BATTERY", "LAMP", "EV_CHARGER", "AMBIENT_SENSOR",
        "AIR_CONDITIONER", "GATE", "WASHING_MACHINE", "SPRINKLER_SYSTEM"};
    private Device createDevice(Property property, String type) {
        if(type.equals("ANY")) {
            type = types[rng.nextInt(types.length)];
        }
        switch(type) {
            case "SOLAR_PANEL_SYSTEM" -> {
                NewDeviceDTO dto = new NewDeviceDTO();
                dto.setPropertyId(property.getId());
                dto.setName("Solar panel");
                dto.setType(EDeviceType.SOLAR_PANEL_SYSTEM);
                dto.setPowerSupply(EPowerSupply.AUTONOMOUS);
                dto.setConsumption(0.0);
                dto.setNumPanels(4);
                dto.setArea(1.6);
                dto.setEfficiency(70.0);

                return deviceService.createDevice(dto);
            }
            case "BATTERY" -> {
                NewDeviceDTO dto = new NewDeviceDTO();
                dto.setPropertyId(property.getId());
                dto.setName("Battery");
                dto.setType(EDeviceType.BATTERY);
                dto.setPowerSupply(EPowerSupply.AUTONOMOUS);
                dto.setConsumption(0.0);
                dto.setCapacity(100.0);

                return deviceService.createDevice(dto);
            }
            case "LAMP" -> {
                NewDeviceDTO dto = new NewDeviceDTO();
                dto.setPropertyId(property.getId());
                dto.setName("Floor Lamp");
                dto.setType(EDeviceType.LAMP);
                dto.setPowerSupply(EPowerSupply.HOME);
                dto.setConsumption(0.5);

                return deviceService.createDevice(dto);
            }
            case "EV_CHARGER" -> {
                NewDeviceDTO dto = new NewDeviceDTO();
                dto.setPropertyId(property.getId());
                dto.setName("EV Charger");
                dto.setType(EDeviceType.EV_CHARGER);
                dto.setPowerSupply(EPowerSupply.HOME);
                dto.setConsumption(0.0);
                dto.setPower(15.0);
                dto.setSlots(3);

                return deviceService.createDevice(dto);
            }
            case "AMBIENT_SENSOR" -> {
                NewDeviceDTO dto = new NewDeviceDTO();
                dto.setPropertyId(property.getId());
                dto.setName("Temperature and Humidity Sensor");
                dto.setType(EDeviceType.AMBIENT_SENSOR);
                dto.setPowerSupply(EPowerSupply.HOME);
                dto.setConsumption(0.7);

                return deviceService.createDevice(dto);
            }
            case "AIR_CONDITIONER" -> {
                NewDeviceDTO dto = new NewDeviceDTO();
                dto.setPropertyId(property.getId());
                dto.setName("Air Conditioner");
                dto.setType(EDeviceType.AIR_CONDITIONER);
                dto.setPowerSupply(EPowerSupply.HOME);
                dto.setConsumption(2.4);

                return deviceService.createDevice(dto);
            }
            case "GATE" -> {
                NewDeviceDTO dto = new NewDeviceDTO();
                dto.setPropertyId(property.getId());
                dto.setName("Smart Gate");
                dto.setType(EDeviceType.GATE);
                dto.setPowerSupply(EPowerSupply.HOME);
                dto.setConsumption(5.2);

                return deviceService.createDevice(dto);
            }
            case "WASHING_MACHINE" -> {
                NewDeviceDTO dto = new NewDeviceDTO();
                dto.setPropertyId(property.getId());
                dto.setName("Washing machine");
                dto.setType(EDeviceType.WASHING_MACHINE);
                dto.setPowerSupply(EPowerSupply.HOME);
                dto.setConsumption(3.0);

                return deviceService.createDevice(dto);
            }
            case "SPRINKLER_SYSTEM" -> {
                NewDeviceDTO dto = new NewDeviceDTO();
                dto.setPropertyId(property.getId());
                dto.setName("Le Sprinklers");
                dto.setType(EDeviceType.SPRINKLER_SYSTEM);
                dto.setPowerSupply(EPowerSupply.HOME);
                dto.setConsumption(1.0);

                return deviceService.createDevice(dto);
            }
        }
        logger.error("Unknown device type {}", type);
        return null;
    }
    private void createPerfTestData() {
        User user = createUser();
        Property property = createProperty(user);
        Integer numDevices = Integer.parseInt(applicationArguments.getOptionValues("test-size").get(0));
        String type;
        if (applicationArguments.containsOption("device-type")) {
            type = applicationArguments.getOptionValues("device-type").get(0);
        } else {
            type = "ANY";
        }
        for (int i = 0; i < numDevices; i++) {
            if (i % 10 == 0 && i != 0) {
                property = createProperty(user);
            }
            createDevice(property, type);
        }
    }

    private void populateLocations() {
        if(!countryRepo.findAll().isEmpty()) return;

        Map<String, List<String>> countries = new HashMap<>();
        countries.put("Serbia", Arrays.asList("Novi Sad", "Belgrade", "Sombor", "Nis", "Kraljevo"));
        countries.put("BiH", Arrays.asList("Sarajevo", "Banjaluka"));
        countries.put("Montenegro", Arrays.asList("Podgorica", "Budva", "Ulcinj"));
        countries.put("Croatia", Arrays.asList("Zagreb", "Split"));

        for(Map.Entry<String, List<String>> item : countries.entrySet()) {
            Country country = countryRepo.save(new Country(item.getKey()));
            for(String cityName : item.getValue()) {
                cityRepository.save(new City(cityName, country));
            }
        }
    }

    private void subscribeToTopics() {
        String[] topics = new String[]{"heartbeat", "production", "consumption", "battery_state", "charging_progress",
                "electricity_distribution", "charging_start", "charging_end", "ambient_light", "status", "ambient_sensor",
                "air_conditioner", "ac_schedule_end", "smart_gate", "washing_machine", "wm_schedule_end", "ack_set_status"};
        for (String topic : topics) {
            try {
                mqttClient.subscribe(topic, 2);
            } catch (MqttException e){
                logger.error("Failed to subscribe to topic {}", topic);
            }
        }
    }

    private void createNormalTestData() {
        User user = new User();
        user.setId(2);
        user.setName("User");
        user.setEmail("user@gmail.com");
        user.setPassword(".GzfzQjNhmkHsaKDrMylP8jAKN3FDeO");
        user.setEnabled(true);
        user.setRole(ERole.ROLE_USER);
        user = userRepository.save(user);

        User shared = new User();
        shared.setId(3);
        shared.setName("Shared");
        shared.setEmail("shared@gmail.com");
        shared.setPassword(".GzfzQjNhmkHsaKDrMylP8jAKN3FDeO");
        shared.setEnabled(true);
        shared.setRole(ERole.ROLE_USER);
        shared = userRepository.save(shared);

        for (int i = 1; i < 3; i++) {
            Property property = new Property();
            property.setStatus(EPropertyStatus.ACCEPTED);
            property.setArea(i * 100);
            property.setName("Test Property " + i);
            property.setCity(cityRepository.findById(3).get());
            property.setCountry(property.getCity().getCountry());
            property.setStatusDetails("");
            property.setFloors(2 * i);
            property.setLat(0.0);
            property.setLon(0.0);
            property.setStreet("Test Ulica " + i + 4);
            property.setUser(user);
            property = propertyRepository.save(property);


            {
                // Solar Panel System
                NewDeviceDTO dto = new NewDeviceDTO();
                dto.setPropertyId(property.getId());
                dto.setName("Solar panel");
                dto.setType(EDeviceType.SOLAR_PANEL_SYSTEM);
                dto.setPowerSupply(EPowerSupply.AUTONOMOUS);
                dto.setConsumption(0.0);
                dto.setNumPanels(4);
                dto.setArea(1.6);
                dto.setEfficiency(70.0);

                deviceService.createDevice(dto);
            }
            {
                NewDeviceDTO dto = new NewDeviceDTO();
                dto.setPropertyId(property.getId());
                dto.setName("Battery");
                dto.setType(EDeviceType.BATTERY);
                dto.setPowerSupply(EPowerSupply.AUTONOMOUS);
                dto.setConsumption(0.0);
                dto.setCapacity(100.0);

                deviceService.createDevice(dto);
            }
            {
                NewDeviceDTO dto = new NewDeviceDTO();
                dto.setPropertyId(property.getId());
                dto.setName("Floor Lamp");
                dto.setType(EDeviceType.LAMP);
                dto.setPowerSupply(EPowerSupply.HOME);
                dto.setConsumption(0.5);

                deviceService.createDevice(dto);
            }
            {
                NewDeviceDTO dto = new NewDeviceDTO();
                dto.setPropertyId(property.getId());
                dto.setName("EV Charger");
                dto.setType(EDeviceType.EV_CHARGER);
                dto.setPowerSupply(EPowerSupply.HOME);
                dto.setConsumption(0.0);
                dto.setPower(15.0);
                dto.setSlots(3);

                deviceService.createDevice(dto);
            }
            {
                NewDeviceDTO dto = new NewDeviceDTO();
                dto.setPropertyId(property.getId());
                dto.setName("Temperature and Humidity Sensor");
                dto.setType(EDeviceType.AMBIENT_SENSOR);
                dto.setPowerSupply(EPowerSupply.HOME);
                dto.setConsumption(0.7);

                deviceService.createDevice(dto);
            }
            {
                NewDeviceDTO dto = new NewDeviceDTO();
                dto.setPropertyId(property.getId());
                dto.setName("Air Conditioner");
                dto.setType(EDeviceType.AIR_CONDITIONER);
                dto.setPowerSupply(EPowerSupply.HOME);
                dto.setConsumption(2.4);

                deviceService.createDevice(dto);
            }
            {
                NewDeviceDTO dto = new NewDeviceDTO();
                dto.setPropertyId(property.getId());
                dto.setName("Smart Gate");
                dto.setType(EDeviceType.GATE);
                dto.setPowerSupply(EPowerSupply.HOME);
                dto.setConsumption(5.2);

                deviceService.createDevice(dto);
            }
            {
                NewDeviceDTO dto = new NewDeviceDTO();
                dto.setPropertyId(property.getId());
                dto.setName("Washing machine");
                dto.setType(EDeviceType.WASHING_MACHINE);
                dto.setPowerSupply(EPowerSupply.HOME);
                dto.setConsumption(3.0);

                deviceService.createDevice(dto);
            }
            {
                NewDeviceDTO dto = new NewDeviceDTO();
                dto.setPropertyId(property.getId());
                dto.setName("Le Sprinklers");
                dto.setType(EDeviceType.SPRINKLER_SYSTEM);
                dto.setPowerSupply(EPowerSupply.HOME);
                dto.setConsumption(1.0);

                deviceService.createDevice(dto);
            }
        }
    }
}

