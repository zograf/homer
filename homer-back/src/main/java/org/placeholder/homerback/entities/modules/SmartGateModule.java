package org.placeholder.homerback.entities.modules;

import jakarta.persistence.*;
import org.placeholder.homerback.entities.Plate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@DiscriminatorValue("SMART_GATE")
public class SmartGateModule extends AbstractModule {
    @Column private boolean isOpen;
    @Column private boolean isPrivate;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL) private List<Plate> plates;

    public SmartGateModule() {
        super(EModuleType.SMART_GATE);
        this.isOpen = false;
        this.isPrivate = false;
        this.plates = new ArrayList<>();
    }

    public SmartGateModule(String plates) {
        super(EModuleType.SMART_GATE);
        this.isOpen = false;
        this.isPrivate = false;
        if(plates == null) this.plates = new ArrayList<>();
        else this.plates = Arrays.stream(plates.split(",")).map(Plate::new).toList();
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
