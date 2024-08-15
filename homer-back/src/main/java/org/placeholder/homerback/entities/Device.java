package org.placeholder.homerback.entities;

import jakarta.persistence.*;
import org.placeholder.homerback.entities.modules.AbstractModule;
import org.placeholder.homerback.entities.modules.EModuleType;
import org.springframework.security.core.parameters.P;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private Property property;
    @Column private String name;
    @Column private EDeviceType type;
    @Column private EPowerSupply powerSupply;
    @Column private Double consumption;

    @Column private long lastHeartbeat;

    @OneToMany(fetch=FetchType.EAGER, cascade={CascadeType.ALL})
    private List<AbstractModule> modules;

    public Device() {
        this.id = null;
        this.lastHeartbeat = 0;
        this.modules = new ArrayList<AbstractModule>();
    }
    public Device(String name, Property property, EDeviceType type, EPowerSupply powerSupply, Double consumption) {
        this.id = null;
        this.name = name;
        this.property = property;
        this.type = type;
        this.powerSupply = powerSupply;
        this.consumption = consumption;
        this.lastHeartbeat = 0;
        this.modules = new ArrayList<AbstractModule>();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
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

    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(long lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }
    public boolean isOnline() {
        return (lastHeartbeat >= System.currentTimeMillis() - 30 * 1000);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<AbstractModule> getModules() {
        return modules;
    }

    public void setModules(List<AbstractModule> modules) {
        this.modules = modules;
    }

    public AbstractModule getModule(EModuleType type) {
        for(AbstractModule module : modules) {
            if (module.getType() == type) {
                return module;
            }
        }
        return null;
    }
}
