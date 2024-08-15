package org.placeholder.homer.simulation.dto.modules;

import org.placeholder.homer.simulation.dto.DeviceDTO;

public class SolarPanelSystemModule extends AbstractModule {
    private int numPanels;
    private double area;
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

