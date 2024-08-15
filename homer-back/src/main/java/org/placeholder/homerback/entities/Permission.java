package org.placeholder.homerback.entities;

import jakarta.persistence.*;

@Entity
public class Permission {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;

    @Column
    private Integer ownerId;
    @Column
    private Integer userId;
    @Column
    private Integer deviceId;
    @Column
    private Integer propertyId;

    public Permission() {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Integer propertyId) {
        this.propertyId = propertyId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
    }
}
