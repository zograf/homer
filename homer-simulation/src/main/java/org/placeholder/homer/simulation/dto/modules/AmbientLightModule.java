package org.placeholder.homer.simulation.dto.modules;


public class AmbientLightModule extends AbstractModule {

    private boolean autoStatus;
    private int lightPresence; // 0 - 100

    public AmbientLightModule() {
        super(EModuleType.AMBIENT_LIGHT);
        this.autoStatus = false;
        this.lightPresence = 0;
    }

    public AmbientLightModule(boolean autoStatus, int lightPresence) {
        super(EModuleType.AMBIENT_LIGHT);
        this.autoStatus = autoStatus;
        this.lightPresence = lightPresence;
    }

    public boolean isAutoStatus() {
        return autoStatus;
    }

    public void setAutoStatus(boolean autoStatus) {
        this.autoStatus = autoStatus;
    }

    public int getLightPresence() {
        return lightPresence;
    }

    public void setLightPresence(int lightPresence) {
        this.lightPresence = lightPresence;
    }
}
