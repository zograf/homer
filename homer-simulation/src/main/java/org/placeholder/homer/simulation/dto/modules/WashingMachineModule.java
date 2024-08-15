package org.placeholder.homer.simulation.dto.modules;

import org.placeholder.homer.simulation.dto.EWashingMode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class WashingMachineModule extends AbstractModule {

    private LocalDateTime currentStart;
    private LocalDateTime currentEnd;
    private EWashingMode currentMode;

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
