package org.placeholder.homerback.dtos;

import org.placeholder.homerback.entities.Country;

public class CountrySlimDTO {
    public Integer id;
    public String name;

    public CountrySlimDTO(Country country) {
        this.id = country.getId();
        this.name = country.getName();
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
