package org.placeholder.homerback.entities.modules;

import jakarta.persistence.*;
import org.placeholder.homerback.entities.EACMode;
import org.placeholder.homerback.entities.EWashingMode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("WASHING_MACHINE")
public class WashingMachineModule extends AbstractModule {

    @Column
    private LocalDateTime currentStart;
    @Column
    private LocalDateTime currentEnd;
    @Column
    EWashingMode currentMode;


    public WashingMachineModule() {
        super(EModuleType.WASHING_MACHINE);
    }

    public LocalDateTime getCurrentStart() {
        return currentStart;
    }

    public void setCurrentStart(LocalDateTime currentStart) {
        this.currentStart = currentStart;
    }

    public LocalDateTime getCurrentEnd() {
        return currentEnd;
    }

    public void setCurrentEnd(LocalDateTime currentEnd) {
        this.currentEnd = currentEnd;
    }

    public EWashingMode getCurrentMode() {
        return currentMode;
    }

    public void setCurrentMode(EWashingMode currentMode) {
        this.currentMode = currentMode;
    }
}
