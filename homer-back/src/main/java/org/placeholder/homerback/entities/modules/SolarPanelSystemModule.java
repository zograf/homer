package org.placeholder.homerback.entities.modules;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("SOLAR_PANEL_SYSTEM")
public class SolarPanelSystemModule extends AbstractModule {
    @Column
    private int numPanels;
    @Column
    private double area;
    @Column
    private double efficiency;

    public SolarPanelSystemModule() {
        super(EModuleType.SOLAR_PANEL_SYSTEM);
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
}
