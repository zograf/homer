package org.placeholder.homerback.entities;

import jakarta.persistence.*;

@Entity
public class Property {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column private String name;
    @ManyToOne private User user;
    @Column private Integer floors;
    @Column private Integer area;
    @ManyToOne private Country country;
    @ManyToOne private City city;
    @Column private String street;
    @Column private Double lon;
    @Column private Double lat;
    @Column private EPropertyStatus status;
    @Column private String statusDetails;

    public Property() { }

    public Property(Integer id, String name, User user, Integer floors, Integer area, Country country, City city, String street, Double lon, Double lat, EPropertyStatus status, String statusDetails) {
        this.id = id;
        this.name = name;
        this.user = user;
        this.floors = floors;
        this.area = area;
        this.country = country;
        this.city = city;
        this.street = street;
        this.lon = lon;
        this.lat = lat;
        this.status = status;
        this.statusDetails = statusDetails;
    }
    public Property(Integer id, String name, User user, Integer floors, Integer area, Country country, City city, String street, Double lon, Double lat) {
        this.id = id;
        this.name = name;
        this.user = user;
        this.floors = floors;
        this.area = area;
        this.country = country;
        this.city = city;
        this.street = street;
        this.lon = lon;
        this.lat = lat;
        this.status = EPropertyStatus.REQUESTED;
        this.statusDetails = "";
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getFloors() {
        return floors;
    }

    public void setFloors(Integer floors) {
        this.floors = floors;
    }

    public Integer getArea() {
        return area;
    }

    public void setArea(Integer area) {
        this.area = area;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public EPropertyStatus getStatus() {
        return status;
    }

    public void setStatus(EPropertyStatus status) {
        this.status = status;
    }

    public String getStatusDetails() {
        return statusDetails;
    }

    public void setStatusDetails(String statusDetails) {
        this.statusDetails = statusDetails;
    }
}
