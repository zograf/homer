package org.placeholder.homer.simulation.dto.modules;

import org.placeholder.homer.simulation.utils.Car;

import java.util.List;

public class EVChargerModule extends AbstractModule{

    private double power;
    private int slots;
    private int occupiedSlots;
    private double fillToPercent;
    private List<Car> cars;
    private List<Boolean> isAvailable;

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

    public List<Car> getCars() {
        return cars;
    }

    public void setCars(List<Car> cars) {
        this.cars = cars;
    }

    public List<Boolean> getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(List<Boolean> isAvailable) {
        this.isAvailable = isAvailable;
    }
    public int getAvailableSlot(){
        for(int i = 0; i < slots; i++) {
            if (isAvailable.get(i)) {
                return i;
            }
        }
        return -1;
    }
}
