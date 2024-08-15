package org.placeholder.homerback.entities;

import jakarta.persistence.*;

@Entity
public class City {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;
    @Column private String name;
    @ManyToOne private Country country;

    public City(String name, Country country) {
        this.name = name;
        this.country = country;
    }

    public City() { }

    public City(Integer id) {
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

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }
}
