package org.placeholder.homer.simulation.dto.modules;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = StatusModule.class, name="STATUS"),
        @JsonSubTypes.Type(value = SolarPanelSystemModule.class, name="SOLAR_PANEL_SYSTEM"),
        @JsonSubTypes.Type(value = BatteryModule.class, name="BATTERY"),
        @JsonSubTypes.Type(value = EVChargerModule.class, name="EV_CHARGER"),
        @JsonSubTypes.Type(value = AmbientLightModule.class, name="AMBIENT_LIGHT"),
        @JsonSubTypes.Type(value = AmbientSensorModule.class, name="AMBIENT_SENSOR"),
        @JsonSubTypes.Type(value = AirConditionerModule.class, name="AIR_CONDITIONER"),
        @JsonSubTypes.Type(value = SchedulingModule.class, name="SCHEDULING"),
        @JsonSubTypes.Type(value = SmartGateModule.class, name="SMART_GATE"),
        @JsonSubTypes.Type(value = WashingMachineModule.class, name="WASHING_MACHINE"),
        @JsonSubTypes.Type(value = SlotsModule.class, name="SLOTS"),
        @JsonSubTypes.Type(value = SprinklerScheduleModule.class, name="SPRINKLER_SCHEDULE")
})
public abstract class AbstractModule {
    private Integer id;

    private EModuleType type;

    public AbstractModule(EModuleType type){
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public EModuleType getType() {
        return type;
    }

    public void setType(EModuleType type) {
        this.type = type;
    }
}