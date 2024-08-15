package org.placeholder.homerback.dtos;

public class NewPropertyDTO {
    private String name;
    private Integer userId;
    private Integer floors;
    private Integer area;
    private Integer countryId;
    private Integer cityId;
    private String street;
    private Double lon;
    private Double lat;

    public NewPropertyDTO(String name, Integer userId, Integer floors, Integer area, Integer countryId, Integer cityId, String street, Double lon, Double lat) {
        this.name = name;
        this.userId = userId;
        this.floors = floors;
        this.area = area;
        this.countryId = countryId;
        this.cityId = cityId;
        this.street = street;
        this.lon = lon;
        this.lat = lat;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
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

    public Integer getCountryId() {
        return countryId;
    }

    public void setCountryId(Integer countryId) {
        this.countryId = countryId;
    }

    public Integer getCityId() {
        return cityId;
    }

    public void setCityId(Integer cityId) {
        this.cityId = cityId;
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
}
