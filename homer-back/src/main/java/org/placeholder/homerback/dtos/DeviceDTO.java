package org.placeholder.homerback.dtos;

import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import org.placeholder.homerback.entities.*;
import org.placeholder.homerback.entities.modules.AbstractModule;
import org.placeholder.homerback.entities.modules.StatusModule;

import java.util.List;

public class DeviceDTO {

    private Integer id;
    private String name;
    private Integer propertyId;
    private String type;
    private String powerSupply;
    private Double consumption;
    private long lastHeartbeat;
    private boolean online;

    private List<AbstractModule> modules;

    public DeviceDTO(Device device){
        id = device.getId();
        name = device.getName();
        propertyId = device.getProperty().getId();
        type = device.getType().toString();
        powerSupply = device.getPowerSupply().toString();
        consumption = device.getConsumption();
        lastHeartbeat = device.getLastHeartbeat();
        online = device.isOnline();
        modules = device.getModules();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Integer propertyId) {
        this.propertyId = propertyId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPowerSupply() {
        return powerSupply;
    }

    public void setPowerSupply(String powerSupply) {
        this.powerSupply = powerSupply;
    }

    public Double getConsumption() {
        return consumption;
    }

    public void setConsumption(Double consumption) {
        this.consumption = consumption;
    }

    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(long lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<AbstractModule> getModules() {
        return modules;
    }

    public void setModules(List<AbstractModule> modules) {
        this.modules = modules;
    }
}
