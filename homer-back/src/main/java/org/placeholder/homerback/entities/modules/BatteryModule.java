package org.placeholder.homerback.entities.modules;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("BATTERY")
public class BatteryModule extends AbstractModule{

    @Column
    private double capacity;
    @Column
    private double value;
    public BatteryModule(){
        super(EModuleType.BATTERY);
        this.value = 0.0;
    }

    public double getCapacity() {
        return capacity;
    }

    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getPercent(){
        return this.value / this.capacity * 100;
    }
}
