package org.placeholder.homerback.dtos;

import org.placeholder.homerback.entities.Device;
import org.placeholder.homerback.entities.Property;

import java.util.List;

public class UserPermissionsDTO {
    List<PropertyDTO> properties;
    List<DeviceDTO> devices;

    public UserPermissionsDTO() { }

    public List<PropertyDTO> getProperties() {
        return properties;
    }

    public void setProperties(List<PropertyDTO> properties) {
        this.properties = properties;
    }

    public List<DeviceDTO> getDevices() {
        return devices;
    }

    public void setDevices(List<DeviceDTO> devices) {
        this.devices = devices;
    }
}
