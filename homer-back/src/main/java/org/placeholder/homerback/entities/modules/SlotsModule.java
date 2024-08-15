package org.placeholder.homerback.entities.modules;

import jakarta.persistence.*;
import org.placeholder.homerback.entities.SlotInfo;
import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@DiscriminatorValue("SLOTS")

public class SlotsModule extends AbstractModule {

    @OneToMany(fetch = FetchType.EAGER)
    private List<SlotInfo> slots;

    public SlotsModule(){
        super(EModuleType.SLOTS);
        slots = new ArrayList<>();
    }

    public List<SlotInfo> getSlots() {
        return slots;
    }

    public void setSlots(List<SlotInfo> slots) {
        this.slots = slots;
    }
}

