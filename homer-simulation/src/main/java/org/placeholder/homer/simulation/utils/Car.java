package org.placeholder.homer.simulation.utils;

import java.util.Random;

public class Car {

    private static Random random = new Random();
    private Double capacity;
    private Double value;
    private Double initialValue;
    private Integer slot;

    public Car(Double maxPercent, int slot) {
        double percent = random.nextDouble() * maxPercent;
        this.capacity = random.nextDouble() * 30 + 20;
        this.value = capacity * percent / 100.0;
        this.initialValue = this.value;
        this.slot = slot;
    }

    public Double getCapacity() {
        return capacity;
    }

    public void setCapacity(Double capacity) {
        this.capacity = capacity;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Double getPercent(){
        return value / capacity * 100;
    }

    public void fill(double energy, double maxPercent) {
        value += energy;
        value = Math.min(value, capacity);
    }

    public Double getConsumed(){
        return value - initialValue;
    }

    public Integer getSlot() {
        return slot;
    }

    public void setSlot(Integer slot) {
        this.slot = slot;
    }
}
