package org.placeholder.homer.simulation.dto.modules;

public class StatusModule extends AbstractModule {

    private boolean on;

    public StatusModule() {
        super(EModuleType.STATUS);
        this.on = false;
    }
    public StatusModule(boolean on) {
        super(EModuleType.STATUS);
        this.on = on;
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }
}
