package org.placeholder.homerback.entities.modules;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("AMBIENT_LIGHT")
public class AmbientLightModule extends AbstractModule {

    @Column private boolean autoStatus;
    @Column private int lightPresence; // 0 - 100

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
