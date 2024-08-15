package org.placeholder.homerback.entities.modules;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@DiscriminatorValue("SPRINKLER_SCHEDULE")
public class SprinklerScheduleModule extends AbstractModule {

    @Column private String days;

    @Column private LocalTime monStartTime;
    @Column private LocalTime monEndTime;

    @Column private LocalTime tueStartTime;
    @Column private LocalTime tueEndTime;

    @Column private LocalTime wedStartTime;
    @Column private LocalTime wedEndTime;

    @Column private LocalTime thuStartTime;
    @Column private LocalTime thuEndTime;

    @Column private LocalTime friStartTime;
    @Column private LocalTime friEndTime;

    @Column private LocalTime satStartTime;
    @Column private LocalTime satEndTime;

    @Column private LocalTime sunStartTime;
    @Column private LocalTime sunEndTime;

    public SprinklerScheduleModule() {
        super(EModuleType.SPRINKLER_SCHEDULE);
        this.days = "0000000";
    }

    public String getDays() {
        return days;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public LocalTime getMonStartTime() {
        return monStartTime;
    }

    public void setMonStartTime(LocalTime monStartTime) {
        this.monStartTime = monStartTime;
    }

    public LocalTime getMonEndTime() {
        return monEndTime;
    }

    public void setMonEndTime(LocalTime monEndTime) {
        this.monEndTime = monEndTime;
    }

    public LocalTime getTueStartTime() {
        return tueStartTime;
    }

    public void setTueStartTime(LocalTime tueStartTime) {
        this.tueStartTime = tueStartTime;
    }

    public LocalTime getTueEndTime() {
        return tueEndTime;
    }

    public void setTueEndTime(LocalTime tueEndTime) {
        this.tueEndTime = tueEndTime;
    }

    public LocalTime getWedStartTime() {
        return wedStartTime;
    }

    public void setWedStartTime(LocalTime wedStartTime) {
        this.wedStartTime = wedStartTime;
    }

    public LocalTime getWedEndTime() {
        return wedEndTime;
    }

    public void setWedEndTime(LocalTime wedEndTime) {
        this.wedEndTime = wedEndTime;
    }

    public LocalTime getThuStartTime() {
        return thuStartTime;
    }

    public void setThuStartTime(LocalTime thuStartTime) {
        this.thuStartTime = thuStartTime;
    }

    public LocalTime getThuEndTime() {
        return thuEndTime;
    }

    public void setThuEndTime(LocalTime thuEndTime) {
        this.thuEndTime = thuEndTime;
    }

    public LocalTime getFriStartTime() {
        return friStartTime;
    }

    public void setFriStartTime(LocalTime friStartTime) {
        this.friStartTime = friStartTime;
    }

    public LocalTime getFriEndTime() {
        return friEndTime;
    }

    public void setFriEndTime(LocalTime friEndTime) {
        this.friEndTime = friEndTime;
    }

    public LocalTime getSatStartTime() {
        return satStartTime;
    }

    public void setSatStartTime(LocalTime satStartTime) {
        this.satStartTime = satStartTime;
    }

    public LocalTime getSatEndTime() {
        return satEndTime;
    }

    public void setSatEndTime(LocalTime satEndTime) {
        this.satEndTime = satEndTime;
    }

    public LocalTime getSunStartTime() {
        return sunStartTime;
    }

    public void setSunStartTime(LocalTime sunStartTime) {
        this.sunStartTime = sunStartTime;
    }

    public LocalTime getSunEndTime() {
        return sunEndTime;
    }

    public void setSunEndTime(LocalTime sunEndTime) {
        this.sunEndTime = sunEndTime;
    }
}
