package org.placeholder.homer.simulation.dto.modules;

public class BatteryModule extends AbstractModule{

    private double capacity;
    private double value;
    private double delta;
    public BatteryModule(){
        super(EModuleType.BATTERY);
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

    public double getDelta() {
        return delta;
    }

    public void setDelta(double delta) {
        this.delta = delta;
    }
}
