package org.placeholder.homerback.services;

import org.placeholder.homerback.entities.SlotInfo;
import org.placeholder.homerback.entities.modules.*;
import org.placeholder.homerback.repositories.IModuleRepository;
import org.placeholder.homerback.repositories.ISlotInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ModuleService {
    @Autowired
    IModuleRepository moduleRepository;
    @Autowired private ISlotInfoRepository slotInfoRepository;

    public AbstractModule createStatusModule(boolean on) {
        StatusModule module = new StatusModule();
        module.setOn(on);
        return module;
    }

    public AbstractModule createAmbientSensorModule() {
        AmbientSensorModule module = new AmbientSensorModule();
        return module;
    }

    public AbstractModule createAmbientLightModule(boolean autoStatus, int lightPresence) {
        AmbientLightModule module = new AmbientLightModule();
        module.setAutoStatus(autoStatus);
        module.setLightPresence(lightPresence);
        return module;
    }

    public AbstractModule createSolarPanelSystemModule(int numPanels, double area, double efficiency) {
        SolarPanelSystemModule module = new SolarPanelSystemModule();
        module.setNumPanels(numPanels);
        module.setArea(area);
        module.setEfficiency(efficiency);
        return module;
    }
    public AbstractModule createBatteryModule(Double capacity) {
        BatteryModule module = new BatteryModule();
        module.setCapacity(capacity);
        module.setValue(0.0);
        return module;
    }
    public AbstractModule createEVChargerModule(Double power, Integer slots) {
        EVChargerModule module = new EVChargerModule();
        module.setPower(power);
        module.setSlots(slots);
        return module;
    }

    public AbstractModule createAirConditionerModule() {
        AirConditionerModule module = new AirConditionerModule();
        return module;
    }

    public AbstractModule createSchedulingModule() {
        SchedulingModule module = new SchedulingModule();
        return module;
    }

    public AbstractModule createWashingMachineModule() {
        WashingMachineModule module = new WashingMachineModule();
        return module;
    }

    public AbstractModule save(AbstractModule module){
        return moduleRepository.save(module);
    }

    public void turnOnOff(StatusModule statusModule, Boolean on) {
        statusModule.setOn(on);
        save(statusModule);
    }

    public void changeAutoBrightness(AmbientLightModule module, boolean auto) {
        module.setAutoStatus(auto);
        save(module);
    }

    public AbstractModule createSlotsModule(Integer slots) {
        SlotsModule module = new SlotsModule();
        for (int i = 0; i < slots; i++) {
            SlotInfo slotInfo = new SlotInfo(i);
            slotInfoRepository.save(slotInfo);
            module.getSlots().add(slotInfo);
        }
        return module;
    }
}
