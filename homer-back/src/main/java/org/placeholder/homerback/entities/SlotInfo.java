package org.placeholder.homerback.entities;

import jakarta.persistence.*;

@Entity
public class SlotInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column
    private Double capacity;
    @Column
    private Double percent;
    @Column
    private Boolean occupied;
    @Column
    private Integer slot;

    public SlotInfo(){
    }
    public SlotInfo(int slot){
        this.slot = slot;
        this.occupied = false;
    }

    public SlotInfo(int slot, Double capacity, Double percent) {
        this.slot = slot;
        this.capacity = capacity;
        this.percent = percent;
        this.occupied = true;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getCapacity() {
        return capacity;
    }

    public void setCapacity(Double capacity) {
        this.capacity = capacity;
    }

    public Double getPercent() {
        return percent;
    }

    public void setPercent(Double percent) {
        this.percent = percent;
    }

    public Boolean getOccupied() {
        return occupied;
    }

    public void setOccupied(Boolean occupied) {
        this.occupied = occupied;
    }

    public Integer getSlot() {
        return slot;
    }

    public void setSlot(Integer slot) {
        this.slot = slot;
    }
}
