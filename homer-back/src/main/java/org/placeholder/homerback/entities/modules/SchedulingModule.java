package org.placeholder.homerback.entities.modules;

import jakarta.persistence.*;
import org.placeholder.homerback.entities.EACMode;
import org.placeholder.homerback.entities.Schedule;

import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("SCHEDULING")
public class SchedulingModule extends AbstractModule {

    @OneToMany(fetch = FetchType.EAGER)
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
