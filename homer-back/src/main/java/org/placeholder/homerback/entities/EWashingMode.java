package org.placeholder.homerback.entities;

public enum EWashingMode {
    WASH_40("40 degree wash", 120),
    WASH_90("90 degree wash", 180),
    RINSE("Rinse", 30),
    SPIN("Spin", 5);

    public final String name;
    public final Integer duration;

    private EWashingMode(String name, Integer duration) {
        this.name = name;
        this.duration = duration;
    }
}
