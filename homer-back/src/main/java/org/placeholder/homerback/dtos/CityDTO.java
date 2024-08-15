package org.placeholder.homerback.dtos;

import org.placeholder.homerback.entities.City;

public class CityDTO {
    private Integer id;
    private String name;

    public CityDTO() { }

    public CityDTO(City city) {
        this.id = city.getId();
        this.name = city.getName();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
