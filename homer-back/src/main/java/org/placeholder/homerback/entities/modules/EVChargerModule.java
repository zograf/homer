package org.placeholder.homerback.entities.modules;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("EV_CHARGER")
public class EVChargerModule extends AbstractModule{

    @Column
    private double power;
    @Column
    private int slots;
    @Column
    private int occupiedSlots;
    @Column
    private double fillToPercent;

    public EVChargerModule(){
        super(EModuleType.EV_CHARGER);
        this.occupiedSlots = 0;
        this.fillToPercent = 100.0;
    }

    public double getPower() {
        return power;
    }

    public void setPower(double power) {
        this.power = power;
    }

    public int getSlots() {
        return slots;
    }

    public void setSlots(int slots) {
        this.slots = slots;
    }

    public int getOccupiedSlots() {
        return occupiedSlots;
    }

    public void setOccupiedSlots(int occupiedSlots) {
        this.occupiedSlots = occupiedSlots;
    }

    public double getFillToPercent() {
        return fillToPercent;
    }

    public void setFillToPercent(double fillToPercent) {
        this.fillToPercent = fillToPercent;
    }
}
