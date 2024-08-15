package org.placeholder.homerback.dtos;

import java.time.LocalDateTime;

public class GraphDatetimePointDTO {
    private LocalDateTime dateTime;
    private Double value;

    public GraphDatetimePointDTO(LocalDateTime dateTime, Double value) {
        this.dateTime = dateTime;
        this.value = value;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
}
