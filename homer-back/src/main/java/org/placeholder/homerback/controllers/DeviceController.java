package org.placeholder.homerback.controllers;

import org.eclipse.paho.mqttv5.client.IMqttClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.placeholder.homerback.dtos.*;
import org.placeholder.homerback.entities.*;
import org.placeholder.homerback.entities.modules.AmbientLightModule;
import org.placeholder.homerback.services.DeviceService;
import org.placeholder.homerback.services.InfluxDBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping(value = "api/device")
public class DeviceController {
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private IMqttClient mqttClient;
    @Autowired
    private InfluxDBService influxDBService;

    @GetMapping(value = "")
    public ResponseEntity<List<DeviceDTO>> getAll() {
        return ResponseEntity.ok(deviceService.getAllDevices());
    }

    @GetMapping(value = "property/{propertyId}")
    public ResponseEntity<List<DeviceDTO>> getAll(@PathVariable Integer propertyId) {
        return ResponseEntity.ok(deviceService.getAllDevicesForProperty(propertyId));
    }

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DeviceDTO> create(
            @AuthenticationPrincipal User user,
            @RequestParam(value = "name") String name,
            @RequestParam(value = "propertyId") Integer propertyId,
            @RequestParam(value = "type") String typeStr,
            @RequestParam(value = "powerSupply") String powerSupplyStr,
            @RequestParam(value = "consumption") Double consumption,
            @RequestParam(required = false, value = "numPanels") Integer numPanels,
            @RequestParam(required = false, value = "area") Double area,
            @RequestParam(required = false, value = "efficiency") Double efficiency,
            @RequestParam(required = false, value = "capacity") Double capacity,
            @RequestParam(required = false, value = "power") Double power,
            @RequestParam(required = false, value = "slots") Integer slots,
            @RequestParam(required = false, value = "plates") String plates,
            @RequestParam(required = true, value="image") MultipartFile image
    ) throws MqttException {
        EDeviceType type = EDeviceType.valueOf(typeStr);
        EPowerSupply powerSupply = EPowerSupply.valueOf(powerSupplyStr);
        NewDeviceDTO dto = new NewDeviceDTO(name, propertyId, type, powerSupply, consumption, numPanels, area,
                efficiency, capacity, power, slots, plates);
        Device device = deviceService.create(dto, image);
        mqttClient.subscribe(device.getId().toString(), 2);
        return ResponseEntity.ok(new DeviceDTO(device));
    }

    @PostMapping(value = "on")
    public ResponseEntity<DeviceDTO> turnOnOff(@AuthenticationPrincipal User user,
                                               @RequestParam(value = "deviceId") Integer deviceId,
                                               @RequestParam(value = "on") Boolean on) throws MqttException {
        return ResponseEntity.ok(new DeviceDTO(deviceService.turnOnOff(deviceId, on, mqttClient, user)));
    }

    @PostMapping(value = "percent")
    public ResponseEntity<DeviceDTO> fillToPercent(@AuthenticationPrincipal User user,
                                                   @RequestParam(value = "deviceId") Integer deviceId,
                                                   @RequestParam(value = "percent") Double percent) throws MqttException {
        return ResponseEntity.ok(new DeviceDTO(deviceService.fillToPercent(deviceId, percent, mqttClient, user)));
    }

    @PostMapping(value = "ambientLight/auto")
    public ResponseEntity<DeviceDTO> changeAutoStatus(@AuthenticationPrincipal User user,
                                                               @RequestParam(value = "deviceId") Integer deviceId,
                                                               @RequestParam(value = "autoStatus") Boolean auto) throws MqttException {
        return ResponseEntity.ok(new DeviceDTO(deviceService.changeAmbientModuleAutoBrightness(deviceId, auto, mqttClient, user)));
    }

    @PostMapping(value = "airConditioner/temperature")
    public ResponseEntity<DeviceDTO> changeTemperature(@AuthenticationPrincipal User user,
                                                      @RequestParam(value = "deviceId") Integer deviceId,
                                                      @RequestParam(value = "temperature") Integer temperature) throws MqttException {
        return ResponseEntity.ok(new DeviceDTO(deviceService.updateAirConditioner(deviceId, temperature, null, mqttClient, user)));
    }

    @PostMapping(value = "airConditioner/mode")
    public ResponseEntity<DeviceDTO> changeMode(@AuthenticationPrincipal User user,
                                                       @RequestParam(value = "deviceId") Integer deviceId,
                                                       @RequestParam(value = "temperature") Integer temperature,
                                                       @RequestParam(value = "mode") EACMode mode) throws MqttException {
        return ResponseEntity.ok(new DeviceDTO(deviceService.updateAirConditioner(deviceId, temperature, mode, mqttClient, user)));
    }

    @PostMapping(value = "airConditioner/schedule/add")
    public ResponseEntity<DeviceDTO> addSchedule(@AuthenticationPrincipal User user,
                                                @RequestParam(value = "deviceId") Integer deviceId,
                                                @RequestParam(value = "temperature") Integer temperature,
                                                @RequestParam(value = "mode") EACMode mode,
                                                @RequestParam(value = "start") LocalDateTime start,
                                                @RequestParam(value = "end") LocalDateTime end,
                                                @RequestParam(value = "isRepeat") Boolean isRepeat) throws MqttException {
        return ResponseEntity.ok(new DeviceDTO(deviceService.addACSchedule(deviceId, temperature, mode, start, end, isRepeat, mqttClient, user)));
    }

    @PostMapping(value = "airConditioner/schedule/remove")
    public ResponseEntity<DeviceDTO> addSchedule(@AuthenticationPrincipal User user,
                                                 @RequestParam(value = "deviceId") Integer deviceId,
                                                 @RequestParam(value = "scheduleId") Integer scheduleId) throws MqttException {
        return ResponseEntity.ok(new DeviceDTO(deviceService.removeACSchedule(deviceId, scheduleId, mqttClient, user)));
    }

    @PostMapping(value = "sprinklers/schedule/set")
    public ResponseEntity<DeviceDTO> setSprinklerSchedule(@AuthenticationPrincipal User user,
                                                          @RequestParam(value = "deviceId") Integer deviceId,
                                                          @RequestParam(value = "from") LocalTime from,
                                                          @RequestParam(value = "to") LocalTime to,
                                                          @RequestParam(value = "days") String days) throws MqttException {
        return ResponseEntity.ok(new DeviceDTO(deviceService.setSprinklerSchedule(deviceId, from, to, days, mqttClient, user)));
    }

    @PostMapping(value = "sprinklers/schedule/remove")
    public ResponseEntity<DeviceDTO> removeSprinklerSchedule(@AuthenticationPrincipal User user,
                                                          @RequestParam(value = "deviceId") Integer deviceId,
                                                          @RequestParam(value = "day") Integer dayIdx) throws MqttException {
        return ResponseEntity.ok(new DeviceDTO(deviceService.removeSprinklerSchedule(deviceId, dayIdx, mqttClient, user)));
    }

    @PostMapping(value = "gate/isOpen")
    public ResponseEntity<DeviceDTO> changeGateIsOpen(@AuthenticationPrincipal User user,
                                                         @RequestParam(value = "deviceId") Integer deviceId,
                                                         @RequestParam(value = "isOpen") Boolean isOpen) throws MqttException {
        return ResponseEntity.ok(new DeviceDTO(deviceService.changeIsOpen(deviceId, isOpen, mqttClient, user)));
    }

    @PostMapping(value = "gate/isPrivate")
    public ResponseEntity<DeviceDTO> changeGateIsPrivate(@AuthenticationPrincipal User user,
                                                         @RequestParam(value = "deviceId") Integer deviceId,
                                                         @RequestParam(value = "isPrivate") Boolean isPrivate) throws MqttException {
        return ResponseEntity.ok(new DeviceDTO(deviceService.changeIsPrivate(deviceId, isPrivate, mqttClient, user)));
    }

    @PostMapping(value = "gate/plate/add")
    public ResponseEntity<DeviceDTO> addPlate(@AuthenticationPrincipal User user,
                                              @RequestParam(value = "deviceId") Integer deviceId,
                                              @RequestParam(value = "text") String text) throws MqttException {
        return ResponseEntity.ok(new DeviceDTO(deviceService.addPlate(deviceId, text, mqttClient, user)));
    }

    @PostMapping(value = "gate/plate/remove")
    public ResponseEntity<DeviceDTO> removePlate(@AuthenticationPrincipal User user,
                                              @RequestParam(value = "deviceId") Integer deviceId,
                                              @RequestParam(value = "plateId") Integer plateId) throws MqttException {
        return ResponseEntity.ok(new DeviceDTO(deviceService.deletePlate(deviceId, plateId, mqttClient, user)));
    }

    @PostMapping(value = "washingMachine/mode")
    public ResponseEntity<DeviceDTO> changeWashingMachineMode(@AuthenticationPrincipal User user,
                                                @RequestParam(value = "deviceId") Integer deviceId,
                                                @RequestParam(value = "mode") EWashingMode mode) throws MqttException {
        LocalDateTime now = LocalDateTime.now();
        now = now.minusNanos(now.getNano());
        now = now.minusSeconds(now.getSecond());
        return ResponseEntity.ok(new DeviceDTO(deviceService.addWMSchedule(deviceId, mode, now, now.plusMinutes(mode.duration), false, mqttClient, user, true)));
    }

    @PostMapping(value = "washingMachine/schedule/add")
    public ResponseEntity<DeviceDTO> addWashingMachineSchedule(@AuthenticationPrincipal User user,
                                                 @RequestParam(value = "deviceId") Integer deviceId,
                                                 @RequestParam(value = "mode") EWashingMode mode,
                                                 @RequestParam(value = "start") LocalDateTime start,
                                                 @RequestParam(value = "isRepeat") Boolean isRepeat) throws MqttException {
        return ResponseEntity.ok(new DeviceDTO(deviceService.addWMSchedule(deviceId, mode, start, start.plusMinutes(mode.duration), isRepeat, mqttClient, user, false)));
    }

    @PostMapping(value = "washingMachine/schedule/remove")
    public ResponseEntity<DeviceDTO> removeWashingMachineSchedule(@AuthenticationPrincipal User user,
                                                 @RequestParam(value = "deviceId") Integer deviceId,
                                                 @RequestParam(value = "scheduleId") Integer scheduleId) throws MqttException {
        return ResponseEntity.ok(new DeviceDTO(deviceService.removeWMSchedule(deviceId, scheduleId, mqttClient, user)));
    }

    @GetMapping(value = "{deviceId}/actions")
    public ResponseEntity<ActionsDTO> getActions(@PathVariable Integer deviceId,
                                                 @RequestParam(value = "pageSize") Integer pageSize,
                                                 @RequestParam(value = "page") Integer page,
                                                 @RequestParam(value = "start", required = false) LocalDateTime start,
                                                 @RequestParam(value = "end", required = false) LocalDateTime end,
                                                 @RequestParam(value = "username", required = false) String username,
                                                 @RequestParam(value = "type", required = false) String type) {
        ActionsDTO actions = influxDBService.getActions(deviceId, page, pageSize, username, start, end, type);
        return ResponseEntity.ok(actions);
    }

    @GetMapping(value = "/{deviceId}/light")
    public ResponseEntity<List<GraphDatetimePointDTO>> getLightAmount(@PathVariable Integer deviceId,
                                                                      @RequestParam(value = "live") Boolean live,
                                                                      @RequestParam(value = "lastXTimeUnits", required = false) String lastXTimeUnits,
                                                                      @RequestParam(value = "start", required = false) LocalDateTime start,
                                                                      @RequestParam(value = "end", required = false) LocalDateTime end) {
        return ResponseEntity.ok(influxDBService.getLightAmount(deviceId, live, lastXTimeUnits, start, end));
    }

    @GetMapping(value = "/{deviceId}/ambientTemperature")
    public ResponseEntity<List<GraphDatetimePointDTO>> getAmbientSensorTemperature(@PathVariable Integer deviceId,
                                                                      @RequestParam(value = "live") Boolean live,
                                                                      @RequestParam(value = "lastXTimeUnits", required = false) String lastXTimeUnits,
                                                                      @RequestParam(value = "start", required = false) LocalDateTime start,
                                                                      @RequestParam(value = "end", required = false) LocalDateTime end) {
        return ResponseEntity.ok(influxDBService.getAmbientSensorTemperature(deviceId, live, lastXTimeUnits, start, end));
    }

    @GetMapping(value = "/{deviceId}/ambientHumidity")
    public ResponseEntity<List<GraphDatetimePointDTO>> getAmbientSensorHumidity(@PathVariable Integer deviceId,
                                                                             @RequestParam(value = "live") Boolean live,
                                                                             @RequestParam(value = "lastXTimeUnits", required = false) String lastXTimeUnits,
                                                                             @RequestParam(value = "start", required = false) LocalDateTime start,
                                                                             @RequestParam(value = "end", required = false) LocalDateTime end) {
        return ResponseEntity.ok(influxDBService.getAmbientSensorHumidity(deviceId, live, lastXTimeUnits, start, end));
    }

    @PostMapping(value = "/{deviceId}/consumption")
    public ResponseEntity<List<GraphDatetimePointDTO>> getConsumption(@PathVariable Integer deviceId,
                                                                      @RequestParam(value = "live") Boolean live,
                                                                      @RequestParam(value = "lastXTimeUnits", required = false) String lastXTimeUnits,
                                                                      @RequestParam(value = "start", required = false) LocalDateTime start,
                                                                      @RequestParam(value = "end", required = false) LocalDateTime end) {
        return ResponseEntity.ok(influxDBService.getConsumption(null, null, deviceId, live, lastXTimeUnits, start, end, false));
    }

    @PostMapping(value = "/{deviceId}/battery")
    public ResponseEntity<List<GraphDatetimePointDTO>> getBatteryState(@PathVariable Integer deviceId,
                                                                       @RequestParam(value = "live") Boolean live,
                                                                       @RequestParam(value = "lastXTimeUnits", required = false) String lastXTimeUnits,
                                                                       @RequestParam(value = "start", required = false) LocalDateTime start,
                                                                       @RequestParam(value = "end", required = false) LocalDateTime end) {
        return ResponseEntity.ok(influxDBService.getBatteryState(deviceId, live, lastXTimeUnits, start, end));
    }

    @GetMapping(value = "/{deviceId}/status")
    public ResponseEntity<List<GraphDatetimePointDTO>> getStatus(@PathVariable Integer deviceId,
                                                                 @RequestParam(value = "live") Boolean live,
                                                                 @RequestParam(value = "lastXTimeUnits", required = false) String lastXTimeUnits,
                                                                 @RequestParam(value = "start", required = false) LocalDateTime start,
                                                                 @RequestParam(value = "end", required = false) LocalDateTime end) {
        return ResponseEntity.ok(influxDBService.getStatusData(deviceId, live, lastXTimeUnits, start, end));
    }
    @GetMapping(value = "/{deviceId}/data/plate")
    public ResponseEntity<List<GraphDatetimePointDTO>> getPlateData(@PathVariable Integer deviceId,
                                                                 @RequestParam(value = "lastXTimeUnits", required = false) String lastXTimeUnits,
                                                                 @RequestParam(value = "start", required = false) LocalDateTime start,
                                                                 @RequestParam(value = "end", required = false) LocalDateTime end,
                                                                 @RequestParam(value = "plate", required = false) String plate) {
        return ResponseEntity.ok(influxDBService.getPlateData(deviceId, plate, lastXTimeUnits, start, end));
    }

    @GetMapping(value = "/{deviceId}/slots")
    public ResponseEntity<List<GraphDatetimePointDTO>> getOccupiedSlots(@PathVariable Integer deviceId,
                                                                        @RequestParam(value = "live") Boolean live,
                                                                        @RequestParam(value = "lastXTimeUnits", required = false) String lastXTimeUnits,
                                                                        @RequestParam(value = "start", required = false) LocalDateTime start,
                                                                        @RequestParam(value = "end", required = false) LocalDateTime end) {
        return ResponseEntity.ok(influxDBService.getOccupiedSlotsData(deviceId, live, lastXTimeUnits, start, end));
    }

    @GetMapping(value = "/{deviceId}/progress")
    public ResponseEntity<List<GraphDatetimePointDTO>> getChargingProgress(@PathVariable Integer deviceId,
                                                                        @RequestParam(value = "slot") Integer slot,
                                                                        @RequestParam(value = "live") Boolean live,
                                                                        @RequestParam(value = "lastXTimeUnits", required = false) String lastXTimeUnits,
                                                                        @RequestParam(value = "start", required = false) LocalDateTime start,
                                                                        @RequestParam(value = "end", required = false) LocalDateTime end) {
        return ResponseEntity.ok(influxDBService.getChargingProgressData(deviceId, slot, live, lastXTimeUnits, start, end));
    }


}
