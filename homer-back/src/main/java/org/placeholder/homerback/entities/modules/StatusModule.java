package org.placeholder.homerback.entities.modules;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("STATUS")
public class StatusModule extends AbstractModule {

    @Column(name="is_on")
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
