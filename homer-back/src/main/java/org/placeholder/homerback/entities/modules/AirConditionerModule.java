package org.placeholder.homerback.entities.modules;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import org.placeholder.homerback.entities.EACMode;

@Entity
@DiscriminatorValue("AIR_CONDITIONER")
public class AirConditionerModule extends AbstractModule {

    private final Integer MIN_TEMP = 15;
    private final Integer MAX_TEMP = 30;
    @Column
    private Integer currentTemperature;

    @Column
    private EACMode currentMode;

    public AirConditionerModule() {
        super(EModuleType.AIR_CONDITIONER);
        this.setCurrentTemperature(20);
        this.currentMode = EACMode.AUTOMATIC;
    }

    public AirConditionerModule(Integer currentTemperature, Integer currentMode) {
        super(EModuleType.AIR_CONDITIONER);
        this.setCurrentTemperature(currentTemperature);
    }

    public Integer getCurrentTemperature() {
        return currentTemperature;
    }

    public void setCurrentTemperature(Integer currentTemperature) {
        if (currentTemperature != null && currentTemperature >= MIN_TEMP && currentTemperature <= MAX_TEMP)
            this.currentTemperature = currentTemperature;
        if (currentTemperature == null)
            this.currentTemperature = null;
    }

    public EACMode getCurrentMode() {
        return currentMode;
    }

    public void setCurrentMode(EACMode currentMode) {
        this.currentMode = currentMode;
    }

    public Integer getMIN_TEMP() {
        return MIN_TEMP;
    }

    public Integer getMAX_TEMP() {
        return MAX_TEMP;
    }
}
