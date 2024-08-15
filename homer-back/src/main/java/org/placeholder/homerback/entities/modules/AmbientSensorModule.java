package org.placeholder.homerback.entities.modules;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("AMBIENT_SENSOR")
public class AmbientSensorModule extends AbstractModule {

    @Column private Double temperatureValue;
    @Column private Double humidityPercent;

    public AmbientSensorModule() {
        super(EModuleType.AMBIENT_SENSOR);
        this.temperatureValue = 0.0;
        this.humidityPercent = 0.0;
    }

    public AmbientSensorModule(Double temperatureValue, Double humidityPercent) {
        super(EModuleType.AMBIENT_SENSOR);
        this.temperatureValue = temperatureValue;
        this.humidityPercent = humidityPercent;
    }

    public Double getTemperatureValue() {
        return temperatureValue;
    }

    public void setTemperatureValue(Double temperatureValue) {
        this.temperatureValue = temperatureValue;
    }

    public Double getHumidityPercent() {
        return humidityPercent;
    }

    public void setHumidityPercent(Double humidityPercent) {
        this.humidityPercent = humidityPercent;
    }
}
