package org.placeholder.homerback.dtos;

import jakarta.persistence.Column;
import org.placeholder.homerback.entities.EDeviceType;
import org.placeholder.homerback.entities.EPowerSupply;

public class NewDeviceDTO {
    private String name;
    private Integer propertyId;
    private EDeviceType type;
    private EPowerSupply powerSupply;
    private Double consumption;

    // Solar panel system
    private Integer numPanels;
    private Double area;
    private Double efficiency;

    // Battery
    private Double capacity;

    // EV Charger
    private Double power;
    private Integer slots;

    // Gate
    private String plates;

    public NewDeviceDTO(){}
    public NewDeviceDTO(String name, Integer propertyId, EDeviceType type, EPowerSupply powerSupply, Double consumption) {
        this.name = name;
        this.propertyId = propertyId;
        this.type = type;
        this.powerSupply = powerSupply;
        this.consumption = consumption;
    }

    public NewDeviceDTO(String name, Integer propertyId, EDeviceType type, EPowerSupply powerSupply, Double consumption,
                        Integer numPanels, Double area, Double efficiency, Double capacity, Double power, Integer slots, String plates) {
        this.name = name;
        this.propertyId = propertyId;
        this.type = type;
        this.powerSupply = powerSupply;
        this.consumption = consumption;
        this.numPanels = numPanels;
        this.area = area;
        this.efficiency = efficiency;
        this.capacity = capacity;
        this.power = power;
        this.slots = slots;
        this.plates = plates;
    }

    public int getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(int propertyId) {
        this.propertyId = propertyId;
    }

    public EDeviceType getType() {
        return type;
    }

    public void setType(EDeviceType type) {
        this.type = type;
    }

    public EPowerSupply getPowerSupply() {
        return powerSupply;
    }

    public void setPowerSupply(EPowerSupply powerSupply) {
        this.powerSupply = powerSupply;
    }

    public Double getConsumption() {
        return consumption;
    }

    public void setConsumption(Double consumption) {
        this.consumption = consumption;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumPanels() {
        return numPanels;
    }

    public void setNumPanels(int numPanels) {
        this.numPanels = numPanels;
    }

    public double getArea() {
        return area;
    }

    public void setArea(double area) {
        this.area = area;
    }

    public double getEfficiency() {
        return efficiency;
    }

    public void setEfficiency(double efficiency) {
        this.efficiency = efficiency;
    }

    public Double getCapacity() {
        return capacity;
    }

    public void setCapacity(Double capacity) {
        this.capacity = capacity;
    }

    public void setPropertyId(Integer propertyId) {
        this.propertyId = propertyId;
    }

    public void setNumPanels(Integer numPanels) {
        this.numPanels = numPanels;
    }

    public void setArea(Double area) {
        this.area = area;
    }

    public void setEfficiency(Double efficiency) {
        this.efficiency = efficiency;
    }

    public Double getPower() {
        return power;
    }

    public void setPower(Double power) {
        this.power = power;
    }

    public Integer getSlots() {
        return slots;
    }

    public void setSlots(Integer slots) {
        this.slots = slots;
    }

    public String getPlates() {
        return plates;
    }

    public void setPlates(String plates) {
        this.plates = plates;
    }
}
