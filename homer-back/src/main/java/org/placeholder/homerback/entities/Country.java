package org.placeholder.homerback.entities;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Country {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;
    @Column private String name;
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "country", cascade = CascadeType.ALL)
    private List<City> cities;

    public Country(String name) {
        this.name= name;
    }

    public Country() { }

    public Country(Integer id) {
        this.id = id;
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

    public List<City> getCities() {
        return cities;
    }

    public void setCities(List<City> cities) {
        this.cities = cities;
    }
}
