package org.placeholder.homerback.services;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import org.placeholder.homerback.dtos.ActionDTO;
import org.placeholder.homerback.dtos.ActionsDTO;
import org.placeholder.homerback.dtos.GraphDatetimePointDTO;
import org.placeholder.homerback.entities.*;
import org.placeholder.homerback.internal_dtos.QueryWithInterval;
import org.placeholder.homerback.repositories.IDeviceRepository;
import org.placeholder.homerback.utils.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class InfluxDBService {

    private static final Logger logger = LoggerFactory.getLogger(InfluxDBService.class);
    @Autowired
    private InfluxDBClient client;
    @Autowired
    private IDeviceRepository deviceRepository;
    @Value("${influx.bucket}")
    private String bucket;

    public void saveFillToPercentAction(User user, Device device, Double percent) {
        WriteApiBlocking writeApi = client.getWriteApiBlocking();

        Point point = Point.measurement("action")
                .time(System.currentTimeMillis(), WritePrecision.MS)
                .addTag("type", "Fill to percent")
                .addTag("propertyId", device.getProperty().getId().toString())
                .addTag("deviceId", device.getId().toString())
                .addTag("userId", user.getId().toString())
                .addTag("userEmail", user.getEmail())
                .addTag("userName", user.getName())
                .addField("percent", String.format("%.2f%%", percent));

        writeApi.writePoint(point);
        logger.info("Saved fill to percent action to InfluxDB");
    }

    public void saveConsumedPower(Device device, double powerConsumed) {
        WriteApiBlocking writeApi = client.getWriteApiBlocking();

        Point point = Point.measurement("consumption")
                .time(System.currentTimeMillis(), WritePrecision.MS)
                .addTag("type", "Consumption")
                .addTag("propertyId", device.getProperty().getId().toString())
                .addTag("cityId", device.getProperty().getCity().getId().toString())
                .addTag("deviceId", device.getId().toString())
                .addField("value", powerConsumed);

        writeApi.writePoint(point);
        logger.info("Saved consumed power to InfluxDB");
    }

    public void saveElectricityDistributionDelta(Device device, double delta) {
        WriteApiBlocking writeApi = client.getWriteApiBlocking();

        Point point = Point.measurement("electricity_distribution")
                .time(System.currentTimeMillis(), WritePrecision.MS)
                .addTag("cityId", device.getProperty().getCity().getId().toString())
                .addTag("propertyId", device.getProperty().getId().toString())
                .addTag("deviceId", device.getId().toString())
                .addField("delta", delta);

        writeApi.writePoint(point);
        logger.info("Saved electricity distribution delta to InfluxDB");
    }

    public void saveBatteryState(Device device, double value) {
        WriteApiBlocking writeApi = client.getWriteApiBlocking();

        Point point = Point.measurement("battery_state")
                .time(System.currentTimeMillis(), WritePrecision.MS)
                .addTag("propertyId", device.getProperty().getId().toString())
                .addTag("deviceId", device.getId().toString())
                .addField("value", value);

        writeApi.writePoint(point);
        logger.info("Saved battery state to InfluxDB");
    }

    public void saveTurnOnOffAction(@Nullable User user, Device device, Boolean on) {
        WriteApiBlocking writeApi = client.getWriteApiBlocking();

        Point point = Point.measurement("action")
                .time(System.currentTimeMillis(), WritePrecision.MS)
                .addTag("type", "Turn on/off")
                .addTag("propertyId", device.getProperty().getId().toString())
                .addTag("deviceId", device.getId().toString())
                .addTag("userId", user == null ? null : user.getId().toString())
                .addTag("userEmail", user == null ? "(automatic)" : user.getEmail())
                .addTag("userName", user == null ? "(automatic)" : user.getName())
                .addField("on", on ? "On" : "Off");

        writeApi.writePoint(point);
        logger.info("Saved turn on/off action to InfluxDB");
    }

    public void saveLampAutoAction(@Nullable User user, Device device, Boolean enabled) {
        WriteApiBlocking writeApi = client.getWriteApiBlocking();

        Point point = Point.measurement("action")
                .time(System.currentTimeMillis(), WritePrecision.MS)
                .addTag("type", "Auto Brightness")
                .addTag("propertyId", device.getProperty().getId().toString())
                .addTag("deviceId", device.getId().toString())
                .addTag("userId", user == null ? null : user.getId().toString())
                .addTag("userEmail", user == null ? "(automatic)" : user.getEmail())
                .addTag("userName", user == null ? "(automatic)" : user.getName())
                .addField("enabled", enabled ? "Enabled" : "Disabled");

        writeApi.writePoint(point);
        logger.info("Saved Auto Brightness action to InfluxDB");
    }

    public void saveProducedPower(Device device, double powerProduced) {
        WriteApiBlocking writeApi = client.getWriteApiBlocking();

        Point point = Point.measurement("consumption")
                .time(System.currentTimeMillis(), WritePrecision.MS)
                .addTag("type", "Production")
                .addTag("propertyId", device.getProperty().getId().toString())
                .addTag("cityId", device.getProperty().getCity().getId().toString())
                .addTag("deviceId", device.getId().toString())
                .addField("value", -powerProduced);

        writeApi.writePoint(point);
        logger.info("Saved produced power to InfluxDB");
    }

    public void saveOnlineChange(Device device, long timestamp, boolean online) {
        WriteApiBlocking writeApi = client.getWriteApiBlocking();

        Point point = Point.measurement("online")
                .time(timestamp, WritePrecision.MS)
                .addTag("propertyId", device.getProperty().getId().toString())
                .addTag("deviceId", device.getId().toString())
                .addField("online", online ? 1.0 : 0.0);

        writeApi.writePoint(point);
        logger.info("Saved device online/offline state to InfluxDB");
    }

    public void saveStartChargingAction(Device device, Double capacity, Double percent, Integer slot) {
        WriteApiBlocking writeApi = client.getWriteApiBlocking();

        String value = String.format("Capacity: %.2f kWh, Filled to: %.2f%%",capacity,percent);
        logger.info("Saving start charging {}", value);

        Point point = Point.measurement("action")
                .time(System.currentTimeMillis(), WritePrecision.MS)
                .addTag("type", "Start charging")
                .addTag("propertyId", device.getProperty().getId().toString())
                .addTag("deviceId", device.getId().toString())
                .addTag("slot", slot.toString())
                .addTag("userId", null)
                .addTag("userEmail", "(automatic)")
                .addTag("userName", "(automatic)")
                .addField("value", value);

        writeApi.writePoint(point);
        logger.info("Saved start charging action to InfluxDB");
    }

    public void saveStopChargingAction(Device device, Double capacity, Double consumed, Integer slot) {
        WriteApiBlocking writeApi = client.getWriteApiBlocking();

        Point point = Point.measurement("action")
                .time(System.currentTimeMillis(), WritePrecision.MS)
                .addTag("type", "Stop charging")
                .addTag("propertyId", device.getProperty().getId().toString())
                .addTag("deviceId", device.getId().toString())
                .addTag("slot", slot.toString())
                .addTag("userId", null)
                .addTag("userEmail", "(automatic)")
                .addTag("userName", "(automatic)")
                .addField("value", String.format("Capacity: %.2f kWh, Consumed: %.2f kWh", capacity, consumed));

        writeApi.writePoint(point);
        logger.info("Saved stop charging action to InfluxDB");
    }

    public void saveChargingProgress(Device device, Double percent, Integer slot) {
        WriteApiBlocking writeApi = client.getWriteApiBlocking();

        Point point = Point.measurement("charging_progress")
                .time(System.currentTimeMillis(), WritePrecision.MS)
                .addTag("propertyId", device.getProperty().getId().toString())
                .addTag("deviceId", device.getId().toString())
                .addTag("slot", slot.toString())
                .addField("value", percent);

        writeApi.writePoint(point);
        logger.info("Saved charging progress to InfluxDB");
    }

    public void saveOccupiedSlots(Device device, Integer slots) {
        WriteApiBlocking writeApi = client.getWriteApiBlocking();

        Point point = Point.measurement("occupied_slots")
                .time(System.currentTimeMillis(), WritePrecision.MS)
                .addTag("propertyId", device.getProperty().getId().toString())
                .addTag("deviceId", device.getId().toString())
                .addField("value", slots);

        writeApi.writePoint(point);
        logger.info("Saved occupied slots to InfluxDB");
    }

    public void saveAmbientTemperatureReading(Device device, Double temperature) {
        WriteApiBlocking writeApi = client.getWriteApiBlocking();

        Point point = Point.measurement("ambient_sensor_temperature")
                .time(System.currentTimeMillis(), WritePrecision.MS)
                .addTag("propertyId", device.getProperty().getId().toString())
                .addTag("deviceId", device.getId().toString())
                .addField("temperature", temperature);

        writeApi.writePoint(point);
    }

    public void saveAmbientHumidityReading(Device device, Double humidityPercent) {
        WriteApiBlocking writeApi = client.getWriteApiBlocking();

        Point point = Point.measurement("ambient_sensor_humidity")
                .time(System.currentTimeMillis(), WritePrecision.MS)
                .addTag("propertyId", device.getProperty().getId().toString())
                .addTag("deviceId", device.getId().toString())
                .addField("humidity", humidityPercent);

        writeApi.writePoint(point);
    }

    public void saveAmbientSensorReading(Device device, Double temperature, Double humidityPercent) {
        saveAmbientTemperatureReading(device, temperature);
        saveAmbientHumidityReading(device, humidityPercent);
        logger.info("Saved ambient sensor reading to InfluxDB");
    }

    public void saveGateEnterExitAction(Device device, String plate, String action) {
        WriteApiBlocking writeApi = client.getWriteApiBlocking();

        Point point = Point.measurement("action")
                .time(System.currentTimeMillis(), WritePrecision.MS)
                .addTag("type", "Presence")
                .addTag("propertyId", device.getProperty().getId().toString())
                .addTag("deviceId", device.getId().toString())
                .addTag("userId", null)
                .addTag("userEmail", "(automatic)")
                .addTag("userName", plate)
                .addField("enterExit", action);

        writeApi.writePoint(point);
        logger.info("Saved turn on/off action to InfluxDB");
    }

    public void saveGateOpenCloseAction(@Nullable User user, Device device, Boolean isOpen) {
        WriteApiBlocking writeApi = client.getWriteApiBlocking();

        Point point = Point.measurement("action")
                .time(System.currentTimeMillis(), WritePrecision.MS)
                .addTag("type", "Open/Close")
                .addTag("propertyId", device.getProperty().getId().toString())
                .addTag("deviceId", device.getId().toString())
                .addTag("userId", user == null ? null : user.getId().toString())
                .addTag("userEmail", user == null ? "(automatic)" : user.getEmail())
                .addTag("userName", user == null ? "(automatic)" : user.getName())
                .addField("openClose", isOpen ? "Opened" : "Closed");

        writeApi.writePoint(point);
        logger.info("Saved turn on/off action to InfluxDB");
    }

    public void saveGateIsPrivateAction(@Nullable User user, Device device, Boolean isPrivate) {
        WriteApiBlocking writeApi = client.getWriteApiBlocking();

        Point point = Point.measurement("action")
                .time(System.currentTimeMillis(), WritePrecision.MS)
                .addTag("type", "Public/Private")
                .addTag("propertyId", device.getProperty().getId().toString())
                .addTag("deviceId", device.getId().toString())
                .addTag("userId", user == null ? null : user.getId().toString())
                .addTag("userEmail", user == null ? "(automatic)" : user.getEmail())
                .addTag("userName", user == null ? "(automatic)" : user.getName())
                .addField("enabled", isPrivate ? "Changed to Private" : "Changed to Public");

        writeApi.writePoint(point);
        logger.info("Saved Auto Brightness action to InfluxDB");
    }

    public void saveAcTemperatureAction(@Nullable User user, Device device, Integer temperature) {
        WriteApiBlocking writeApi = client.getWriteApiBlocking();

        Point point = Point.measurement("action")
                .time(System.currentTimeMillis(), WritePrecision.MS)
                .addTag("type", "Temperature changed")
                .addTag("propertyId", device.getProperty().getId().toString())
                .addTag("deviceId", device.getId().toString())
                .addTag("userId", user == null ? null : user.getId().toString())
                .addTag("userEmail", user == null ? "(automatic)" : user.getEmail())
                .addTag("userName", user == null ? "(automatic)" : user.getName())
                .addField("temperature", temperature.toString());

        writeApi.writePoint(point);
    }

    public void saveAcModeAction(@Nullable User user, Device device, EACMode mode) {
        WriteApiBlocking writeApi = client.getWriteApiBlocking();

        Point point = Point.measurement("action")
                .time(System.currentTimeMillis(), WritePrecision.MS)
                .addTag("type", "Mode changed")
                .addTag("propertyId", device.getProperty().getId().toString())
                .addTag("deviceId", device.getId().toString())
                .addTag("userId", user == null ? null : user.getId().toString())
                .addTag("userEmail", user == null ? "(automatic)" : user.getEmail())
                .addTag("userName", user == null ? "(automatic)" : user.getName())
                .addField("mode", mode.toString());

        writeApi.writePoint(point);
    }

    public void saveWMModeAction(@Nullable User user, Device device, EWashingMode mode) {
        WriteApiBlocking writeApi = client.getWriteApiBlocking();

        Point point = Point.measurement("action")
                .time(System.currentTimeMillis(), WritePrecision.MS)
                .addTag("type", "Mode changed")
                .addTag("propertyId", device.getProperty().getId().toString())
                .addTag("deviceId", device.getId().toString())
                .addTag("userId", user == null ? null : user.getId().toString())
                .addTag("userEmail", user == null ? "(automatic)" : user.getEmail())
                .addTag("userName", user == null ? "(automatic)" : user.getName())
                .addField("mode", mode == null ? "None" : mode.toString());

        writeApi.writePoint(point);
    }

    public void saveScheduleEndAction(@Nullable User user, Device device, Schedule s) {
        WriteApiBlocking writeApi = client.getWriteApiBlocking();

        LocalDateTime time = s.getStartTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        String value = time.format(formatter).replace(" ", " at ");

        Point point = Point.measurement("action")
                .time(System.currentTimeMillis(), WritePrecision.MS)
                .addTag("type", "Schedule end")
                .addTag("propertyId", device.getProperty().getId().toString())
                .addTag("deviceId", device.getId().toString())
                .addTag("userId", user == null ? null : user.getId().toString())
                .addTag("userEmail", user == null ? "(automatic)" : user.getEmail())
                .addTag("userName", user == null ? "(automatic)" : user.getName())
                .addField("endedOn", value);

        writeApi.writePoint(point);
    }

    public void saveScheduleStartAction(@Nullable User user, Device device, Schedule s) {
        WriteApiBlocking writeApi = client.getWriteApiBlocking();

        LocalDateTime time = s.getStartTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        String value = time.format(formatter).replace(" ", " at ");

        Point point = Point.measurement("action")
                .time(System.currentTimeMillis(), WritePrecision.MS)
                .addTag("type", "Schedule start")
                .addTag("propertyId", device.getProperty().getId().toString())
                .addTag("deviceId", device.getId().toString())
                .addTag("userId", user == null ? null : user.getId().toString())
                .addTag("userEmail", user == null ? "(automatic)" : user.getEmail())
                .addTag("userName", user == null ? "(automatic)" : user.getName())
                .addField("startedOn", value);

        writeApi.writePoint(point);
    }

    public void saveScheduleAddAction(@Nullable User user, Device device, Schedule s) {
        WriteApiBlocking writeApi = client.getWriteApiBlocking();

        LocalDateTime time = s.getStartTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        String value = time.format(formatter).replace(" ", " at ");

        Point point = Point.measurement("action")
                .time(System.currentTimeMillis(), WritePrecision.MS)
                .addTag("type", "Schedule add")
                .addTag("propertyId", device.getProperty().getId().toString())
                .addTag("deviceId", device.getId().toString())
                .addTag("userId", user == null ? null : user.getId().toString())
                .addTag("userEmail", user == null ? "(automatic)" : user.getEmail())
                .addTag("userName", user == null ? "(automatic)" : user.getName())
                .addField("startedOn", value);

        writeApi.writePoint(point);
    }

    public void saveAmbientLightPresence(Device device, Integer amount) {
        WriteApiBlocking writeApi = client.getWriteApiBlocking();

        Point point = Point.measurement("light_presence")
                .time(System.currentTimeMillis(), WritePrecision.MS)
                .addTag("type", "Presence")
                .addTag("propertyId", device.getProperty().getId().toString())
                .addTag("deviceId", device.getId().toString())
                .addField("value", amount * 1.0);

        writeApi.writePoint(point);
        logger.info("Saved produced power to InfluxDB");
    }

    public ActionsDTO getActions(Integer deviceId, Integer page, Integer pageSize, String usernameFilter, LocalDateTime start, LocalDateTime end, String actionType){
        List<ActionDTO> result = new ArrayList<>();
        QueryApi queryApi = client.getQueryApi();

        QueryBuilder query = QueryBuilder.from(this.bucket);
        if (start != null && end != null) {
            query.range(start, end);
        }else {
            query.range("-30d");
        }
        query.filterByMeasurement("action")
                .group()
                .filterByTag("deviceId", deviceId.toString());
        if (actionType != null && !actionType.isBlank()) {
            query.filterByTag("type", actionType);
        }
        if (usernameFilter != null) {
            query.filterByTag("userName", usernameFilter);
        }
        query.sortByTime(true);

        String countQuery = query.duplicate().count().build();

        logger.info("Executing query: {}", countQuery);

        List<FluxTable> tables = queryApi.query(countQuery);
        Long count = 0L;
        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord fluxRecord : records) {
                count += (Long)fluxRecord.getValueByKey("_value");
                logger.info("record {}, count {}", fluxRecord.getValues(), count);
            }
        }

        String fluxQuery = query.page(page, pageSize).build();

        logger.info("Executing query: {}", fluxQuery);

        tables = queryApi.query(fluxQuery);
        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord fluxRecord : records) {
                String userIdStr = (String)fluxRecord.getValueByKey("userId");
                Integer userId2 = userIdStr == null ? null : Integer.parseInt(userIdStr);
                String username = (String)fluxRecord.getValueByKey("userName");
                String email = (String)fluxRecord.getValueByKey("userEmail");
                String type = (String)fluxRecord.getValueByKey("type");
                Object value = fluxRecord.getValue();
                LocalDateTime dateTime = LocalDateTime.ofInstant(fluxRecord.getTime(), ZoneId.systemDefault());
                result.add(new ActionDTO(userId2, username, email, type, dateTime, value));
            }
        }
        return new ActionsDTO(count, page, result);
    }

    private QueryWithInterval getDateTimeQueryBuilder(boolean live, String lastXTimeUnits, LocalDateTime start, LocalDateTime end) {
        QueryBuilder query = QueryBuilder.from(this.bucket);
        String interval = "1m";

        if (live) {
            query.range("-1h");
        }
        else if (lastXTimeUnits != null) {
            String unit = lastXTimeUnits.substring(lastXTimeUnits.length()-1);
            int value = Integer.parseInt(lastXTimeUnits.substring(0, lastXTimeUnits.length()-1));
            if (unit.equals("M")){
                query.range("-" + (30 * value) + "d");
                if (value > 1) interval = "1w";
                else interval = "1d";
            }
            else if (unit.equals("w")) {
                query.range("-" + (7 * value) + "d");
                interval = "1d";
            }
            else {
                query.range("-" + lastXTimeUnits);
                if (value >= 10) interval = "1" + unit;
                else {
                    if (unit.equals("d")) {
                        if (value >= 5) interval = "6h";
                        else interval = "1h";
                    }
                    else if(unit.equals("h")){
                        if (value >= 5) interval = "30m";
                        else interval = "5m";
                    }
                    else interval = "1m";
                }
            }
        }
        else {
            assert(start != null && end != null);
            query.range(start, end);
            long days = ChronoUnit.DAYS.between(start, end);
            if (days >= 10) interval = "1d";
            else if (days >= 5) interval = "12h";
            else {
                long hours = ChronoUnit.HOURS.between(start, end);
                if (hours >= 48) interval = "6h";
                else if(hours >= 10) interval = "1h";
                else if(hours >= 5) interval = "30m";
                else {
                    long minutes = ChronoUnit.MINUTES.between(start, end);
                    if (minutes >= 120) interval = "15m";
                    else if (minutes > 60) interval = "10m";
                    else if (minutes > 30) interval = "5m";
                    else if (minutes > 20) interval = "20s";
                    else interval = "5s";
                }
            }
        }

        return new QueryWithInterval(query, interval);
    }
    private List<GraphDatetimePointDTO> getAggregatedData(QueryWithInterval query) {
        List<GraphDatetimePointDTO> result = new ArrayList<>();
        QueryApi queryApi = client.getQueryApi();
        String fluxQuery = query.getQuery().build();
        logger.info("Executing query: {}", fluxQuery);
        List<FluxTable> tables = queryApi.query(fluxQuery);
        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord fluxRecord : records) {
                logger.info("aggregated record {}", fluxRecord.getValues());
                //LocalDateTime intervalStart = LocalDateTime.ofInstant(fluxRecord.getTime(), ZoneId.systemDefault());
                //LocalDateTime intervalEnd = null;//fluxRecord.getValueByKey("_end");
                LocalDateTime dateTime = LocalDateTime.ofInstant(fluxRecord.getTime(), ZoneId.systemDefault());
                //LocalDateTime intervalEnd = LocalDateTime.ofInstant((Instant)fluxRecord.getValueByKey("_stop"), ZoneId.systemDefault());
                Double value = (Double)fluxRecord.getValue();
                if (value == null) value = 0.0;
                result.add(new GraphDatetimePointDTO(dateTime, value));
            }
        }
        return result;
    }
    private List<GraphDatetimePointDTO> getAggregatedPlateData(QueryWithInterval query) {
        List<GraphDatetimePointDTO> result = new ArrayList<>();
        QueryApi queryApi = client.getQueryApi();
        String fluxQuery = query.getQuery().build();
        logger.info("Executing query: {}", fluxQuery);
        List<FluxTable> tables = queryApi.query(fluxQuery);
        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord fluxRecord : records) {
                logger.info("aggregated record {}", fluxRecord.getValues());
                //LocalDateTime intervalStart = LocalDateTime.ofInstant(fluxRecord.getTime(), ZoneId.systemDefault());
                //LocalDateTime intervalEnd = null;//fluxRecord.getValueByKey("_end");
                LocalDateTime dateTime = LocalDateTime.ofInstant(fluxRecord.getTime(), ZoneId.systemDefault());
                //LocalDateTime intervalEnd = LocalDateTime.ofInstant((Instant)fluxRecord.getValueByKey("_stop"), ZoneId.systemDefault());
                String value = (String) fluxRecord.getValue();
                if (value == null || value.equals("Tried To Enter")) continue;
                result.add(new GraphDatetimePointDTO(dateTime, value.equals("Enter") ? 1.0 : 0.0));
            }
        }
        return result;
    }

    public List<GraphDatetimePointDTO> getConsumption(Integer propertyId, Integer cityId, Integer deviceId, boolean live, String lastXTimeUnits, LocalDateTime start, LocalDateTime end, boolean electricity_distribution) {
        QueryWithInterval query = getDateTimeQueryBuilder(live, lastXTimeUnits, start, end);

        if (electricity_distribution){
            query.getQuery().filterByMeasurement("electricity_distribution").group();
        }else {
            query.getQuery().filterByMeasurement("consumption").group();
        }
        if (propertyId != null) {
            query.getQuery().filterByTag("propertyId", propertyId.toString());
        }
        else if (cityId != null){
            query.getQuery().filterByTag("cityId", cityId.toString());
        }
        else{
            query.getQuery().filterByTag("deviceId", deviceId.toString());
        }
        query.getQuery().aggregateWindow(query.getInterval(), "sum");

        return getAggregatedData(query);
    }

    public List<GraphDatetimePointDTO> getLightAmount(Integer deviceId, boolean live, String lastXTimeUnits, LocalDateTime start, LocalDateTime end) {
        QueryWithInterval query = getDateTimeQueryBuilder(live, lastXTimeUnits, start, end);

        query.getQuery().filterByMeasurement("light_presence").group();
        query.getQuery().filterByTag("deviceId", deviceId.toString());
        query.getQuery().aggregateWindow(query.getInterval(), "mean");

        return getAggregatedData(query);
    }

    public List<GraphDatetimePointDTO> getStatusData(Integer deviceId, boolean live, String lastXTimeUnits, LocalDateTime start, LocalDateTime end) {
        // Eventual consistency
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (deviceOpt.isPresent()) {
            Device device = deviceOpt.get();
            boolean wasOnline = device.isOnline();
            if (device.getLastHeartbeat() != 0 && !wasOnline) {
                saveOnlineChange(device, device.getLastHeartbeat() + 30 * 1000, false);
            }
            device.setLastHeartbeat(0);
            device = deviceRepository.save(device);
        }

        QueryWithInterval query = getDateTimeQueryBuilder(live, lastXTimeUnits, start, end);

        query.getQuery().filterByMeasurement("online").group();
        query.getQuery().filterByTag("deviceId", deviceId.toString());

        List<GraphDatetimePointDTO> result = getAggregatedData(query);
        List<GraphDatetimePointDTO> filteredResult = new ArrayList<>();
        for (int i = 1; i < result.size(); i++) {
            if (result.get(i).getValue().equals(result.get(i-1).getValue())) {
                continue;
            }
            filteredResult.add(result.get(i));
        }
        return filteredResult;
    }

    public List<GraphDatetimePointDTO> getPlateData(Integer deviceId, String plate, String lastXTimeUnits, LocalDateTime start, LocalDateTime end) {
        QueryWithInterval query = getDateTimeQueryBuilder(false, lastXTimeUnits, start, end);

        query.getQuery().filterByMeasurement("action").group();
        query.getQuery().filterByTag("deviceId", deviceId.toString());
        query.getQuery().filterByTag("type", "Presence");
        query.getQuery().filterByTag("userName", plate);

        return getAggregatedPlateData(query);
    }

    public List<GraphDatetimePointDTO> getAmbientSensorTemperature(Integer deviceId, boolean live, String lastXTimeUnits, LocalDateTime start, LocalDateTime end) {
        QueryWithInterval query = getDateTimeQueryBuilder(live, lastXTimeUnits, start, end);

        query.getQuery().filterByMeasurement("ambient_sensor_temperature").group();
        query.getQuery().filterByTag("deviceId", deviceId.toString());
        query.getQuery().aggregateWindow(query.getInterval(), "mean");

        return getAggregatedData(query);
    }

    public List<GraphDatetimePointDTO> getAmbientSensorHumidity(Integer deviceId, boolean live, String lastXTimeUnits, LocalDateTime start, LocalDateTime end) {
        QueryWithInterval query = getDateTimeQueryBuilder(live, lastXTimeUnits, start, end);

        query.getQuery().filterByMeasurement("ambient_sensor_humidity").group();
        query.getQuery().filterByTag("deviceId", deviceId.toString());
        query.getQuery().aggregateWindow(query.getInterval(), "mean");

        return getAggregatedData(query);
    }

    public List<GraphDatetimePointDTO> getBatteryState(Integer deviceId, Boolean live, String lastXTimeUnits, LocalDateTime start, LocalDateTime end) {
        QueryWithInterval query = getDateTimeQueryBuilder(live, lastXTimeUnits, start, end);

        query.getQuery().filterByMeasurement("battery_state").group()
            .filterByTag("deviceId", deviceId.toString())
            .aggregateWindow(query.getInterval(), "mean");

        return getAggregatedData(query);
    }

    public List<GraphDatetimePointDTO> getOccupiedSlotsData(Integer deviceId, Boolean live, String lastXTimeUnits, LocalDateTime start, LocalDateTime end) {
        QueryWithInterval query = getDateTimeQueryBuilder(live, lastXTimeUnits, start, end);

        query.getQuery().filterByMeasurement("occupied_slots").group()
                .filterByTag("deviceId", deviceId.toString())
                .aggregateWindow(query.getInterval(), "mean");

        return getAggregatedData(query);
    }

    public List<GraphDatetimePointDTO> getChargingProgressData(Integer deviceId, Integer slot, Boolean live, String lastXTimeUnits, LocalDateTime start, LocalDateTime end) {
        QueryWithInterval query = getDateTimeQueryBuilder(live, lastXTimeUnits, start, end);

        query.getQuery().filterByMeasurement("charging_progress").group()
                .filterByTag("deviceId", deviceId.toString())
                .filterByTag("slot", slot.toString())
                .aggregateWindow(query.getInterval(), "mean");

        return getAggregatedData(query);
    }
}
