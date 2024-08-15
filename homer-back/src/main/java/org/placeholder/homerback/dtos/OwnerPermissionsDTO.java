package org.placeholder.homerback.dtos;

public class OwnerPermissionsDTO {
    private String userEmail;
    private PropertyDTO property;
    private DeviceDTO device;

    public OwnerPermissionsDTO() {}

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public PropertyDTO getProperty() {
        return property;
    }

    public void setProperty(PropertyDTO property) {
        this.property = property;
    }

    public DeviceDTO getDevice() {
        return device;
    }

    public void setDevice(DeviceDTO device) {
        this.device = device;
    }
}
