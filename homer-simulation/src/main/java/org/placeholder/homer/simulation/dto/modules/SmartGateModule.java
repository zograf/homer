package org.placeholder.homer.simulation.dto.modules;

import org.placeholder.homer.simulation.dto.Plate;

import java.util.ArrayList;
import java.util.List;

public class SmartGateModule extends AbstractModule {
    private boolean isOpen;
    private boolean isPrivate;
    private List<Plate> plates;

    public SmartGateModule() {
        super(EModuleType.SMART_GATE);
        this.isOpen = false;
        this.isPrivate = false;
        this.plates = new ArrayList<>();
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public List<Plate> getPlates() {
        return plates;
    }

    public void setPlates(List<Plate> plates) {
        this.plates = plates;
    }
}
