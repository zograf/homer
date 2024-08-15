package org.placeholder.homer.simulation.dto;

import org.placeholder.homer.simulation.dto.modules.*;

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

    public DeviceDTO(){}

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
    public StatusModule getStatusModule(){
        for(AbstractModule module : this.modules) {
            if (module.getType() == EModuleType.STATUS) {
                return (StatusModule) module;
            }
        }
        return null;
    }
    public SolarPanelSystemModule getSolarPanelSystemModule(){
        for(AbstractModule module : this.modules) {
            if (module.getType() == EModuleType.SOLAR_PANEL_SYSTEM) {
                return (SolarPanelSystemModule) module;
            }
        }
        return null;
    }

    public BatteryModule getBatteryModule(){
        for(AbstractModule module : this.modules) {
            if (module.getType() == EModuleType.BATTERY) {
                return (BatteryModule) module;
            }
        }
        return null;
    }

    public EVChargerModule getEVChargerModule() {
        for(AbstractModule module : this.modules) {
            if (module.getType() == EModuleType.EV_CHARGER) {
                return (EVChargerModule) module;
            }
        }
        return null;
    }

    public AmbientLightModule getAmbientLightModule() {
        for(AbstractModule module : this.modules) {
            if (module.getType() == EModuleType.AMBIENT_LIGHT) {
                return (AmbientLightModule) module;
            }
        }
        return null;
    }

    public AmbientSensorModule getSensorModule() {
        for(AbstractModule module : this.modules) {
            if (module.getType() == EModuleType.AMBIENT_SENSOR) {
                return (AmbientSensorModule) module;
            }
        }
        return null;
    }

    public AirConditionerModule getAirConditionerModule() {
        for(AbstractModule module : this.modules) {
            if (module.getType() == EModuleType.AIR_CONDITIONER) {
                return (AirConditionerModule) module;
            }
        }
        return null;
    }

    public SchedulingModule getSchedulingModule() {
        for(AbstractModule module : this.modules) {
            if (module.getType() == EModuleType.SCHEDULING) {
                return (SchedulingModule) module;
            }
        }
        return null;
    }

    public WashingMachineModule getWashingMachineModule() {
        for(AbstractModule module : this.modules) {
            if (module.getType() == EModuleType.WASHING_MACHINE) {
                return (WashingMachineModule) module;
            }
        }
        return null;
    }

    public AbstractModule getModule(EModuleType type) {
        for(AbstractModule module : this.modules) {
            if (module.getType() == type) {
                return module;
            }
        }
        return null;
    }
}
