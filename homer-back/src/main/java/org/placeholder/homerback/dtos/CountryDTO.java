package org.placeholder.homerback.dtos;

import org.placeholder.homerback.entities.Country;

import java.util.List;

public class CountryDTO {
    public Integer id;
    public String name;
    public List<CityDTO> cities;

    public CountryDTO() { }
    public CountryDTO(Country country) {
        this.id = country.getId();
        this.name = country.getName();
        this.cities = country.getCities().stream().map(CityDTO::new).toList();
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

    public List<CityDTO> getCities() {
        return cities;
    }

    public void setCities(List<CityDTO> cities) {
        this.cities = cities;
    }
}
