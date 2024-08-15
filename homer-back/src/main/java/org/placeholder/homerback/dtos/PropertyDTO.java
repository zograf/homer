package org.placeholder.homerback.dtos;

import org.placeholder.homerback.entities.EPropertyStatus;
import org.placeholder.homerback.entities.Property;

public class PropertyDTO {
    private Integer id;
    private String name;
    private Integer floors;
    private Integer area;
    private CountrySlimDTO country;
    private CityDTO city;
    private String street;
    private Double lon;
    private Double lat;
    private EPropertyStatus status;
    private String statusDetails;

    public PropertyDTO(Property property) {
        this.id = property.getId();
        this.name = property.getName();
        this.floors = property.getFloors();
        this.area = property.getArea();
        this.country = new CountrySlimDTO(property.getCountry());
        this.city = new CityDTO(property.getCity());
        this.street = property.getStreet();
        this.lon = property.getLon();
        this.lat = property.getLat();
        this.status = property.getStatus();
        this.statusDetails = property.getStatusDetails();
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

    public CountrySlimDTO getCountry() {
        return country;
    }

    public void setCountry(CountrySlimDTO country) {
        this.country = country;
    }

    public CityDTO getCity() {
        return city;
    }

    public void setCity(CityDTO city) {
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
