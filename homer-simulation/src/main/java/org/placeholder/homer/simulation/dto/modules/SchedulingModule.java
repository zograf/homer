package org.placeholder.homer.simulation.dto.modules;

import org.placeholder.homer.simulation.dto.Schedule;

import java.util.ArrayList;
import java.util.List;

public class SchedulingModule extends AbstractModule {

    private List<Schedule> scheduleList;
    public SchedulingModule() {
        super(EModuleType.SCHEDULING);
        this.scheduleList = new ArrayList<>();
    }

    public SchedulingModule(ArrayList<Schedule> schedule) {
        super(EModuleType.SCHEDULING);
        this.scheduleList = schedule;
    }

    public List<Schedule> getScheduleList() {
        return scheduleList;
    }

    public void setScheduleList(List<Schedule> scheduleList) {
        this.scheduleList = scheduleList;
    }
}