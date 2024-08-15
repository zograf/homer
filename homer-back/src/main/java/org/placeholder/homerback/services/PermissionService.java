package org.placeholder.homerback.services;

import org.placeholder.homerback.dtos.DeviceDTO;
import org.placeholder.homerback.dtos.OwnerPermissionsDTO;
import org.placeholder.homerback.dtos.PropertyDTO;
import org.placeholder.homerback.dtos.UserPermissionsDTO;
import org.placeholder.homerback.entities.Device;
import org.placeholder.homerback.entities.Permission;
import org.placeholder.homerback.entities.Property;
import org.placeholder.homerback.entities.User;
import org.placeholder.homerback.repositories.IDeviceRepository;
import org.placeholder.homerback.repositories.IPermissionRepository;
import org.placeholder.homerback.repositories.IPropertyRepository;
import org.placeholder.homerback.repositories.IUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class PermissionService {

    Logger logger = LoggerFactory.getLogger(PermissionService.class);

    @Autowired
    private IPermissionRepository permissionRepository;

    @Autowired
    private IDeviceRepository deviceRepository;

    @Autowired
    private IPropertyRepository propertyRepository;

    @Autowired
    private IUserRepository userRepository;

    public Permission addDevicePermission(String ownerEmail, String userEmail, Integer deviceId) {
        Optional<User> userExists = userRepository.findByEmail(userEmail);
        if (userExists.isEmpty()) {
            logger.error("Error - User with email " + userEmail + " was not found");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "message: User was not found");
        }

        Optional<User> ownerExists = userRepository.findByEmail(ownerEmail);
        if (ownerExists.isEmpty()) {
            logger.error("Error - User with email " + ownerEmail + " was not found");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "message: User was not found");
        }

        User user = userExists.get();
        User owner = ownerExists.get();

        Optional<Permission> exists = permissionRepository.findByUserIdAndDeviceId(user.getId(), deviceId);
        if (exists.isPresent()) {
            logger.error("Error - Permission for deviceId " + deviceId + " on userId " + user.getId() + " already exists");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "message: Permission already exists");
        }

        Optional<Device> deviceExists = deviceRepository.findById(deviceId);
        if (deviceExists.isEmpty()) {
            logger.error("Error - Device with deviceId " + deviceId + " was not found");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "message: Device not found");
        }

        Device device = deviceExists.get();
        Integer deviceOwnerId = device.getProperty().getUser().getId();
        if (!Objects.equals(owner.getId(), deviceOwnerId)){
            logger.error("Error - Owner provided cannot assign permission for a device he does not own");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "message: Device not owned");
        }

        Integer propertyId = device.getProperty().getId();
        exists = permissionRepository.findByUserIdAndPropertyId(user.getId(), propertyId);
        if (exists.isPresent()) {
            logger.error("Error - Permission for deviceId " + deviceId + " on userId " + user.getId() + " already exists because the whole property is shared");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "message: Permission already exists");
        }

        Permission permission = new Permission();
        permission.setUserId(user.getId());
        permission.setDeviceId(deviceId);
        permission.setOwnerId(owner.getId());
        return permissionRepository.save(permission);
    }

    public Permission addPropertyPermission(String ownerEmail, String userEmail, Integer propertyId) {
        Optional<User> userExists = userRepository.findByEmail(userEmail);
        if (userExists.isEmpty()) {
            logger.error("Error - User with email " + userEmail + " was not found");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "message: User was not found");
        }

        Optional<User> ownerExists = userRepository.findByEmail(ownerEmail);
        if (ownerExists.isEmpty()) {
            logger.error("Error - User with email " + ownerEmail + " was not found");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "message: User was not found");
        }

        User user = userExists.get();
        User owner = ownerExists.get();

        Optional<Permission> exists = permissionRepository.findByUserIdAndPropertyId(user.getId(), propertyId);
        if (exists.isPresent()) {
            logger.error("Error - Permission for propertyId " + propertyId + " on userId " + user.getId() + " already exists");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "message: Permission already exists");
        }

        Optional<Property> propertyExists = propertyRepository.findById(propertyId);
        if (propertyExists.isEmpty()) {
            logger.error("Error - Property with propertyId " + propertyId + " was not found");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "message: Property not found");
        }

        Property property = propertyExists.get();
        Integer propertyOwnerId = property.getUser().getId();
        if (!Objects.equals(owner.getId(), propertyOwnerId)){
            logger.error("Error - Owner provided cannot assign permission for a property he does not own");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "message: Property not owned");
        }

        List<Device> devices = deviceRepository.findAllByPropertyId(propertyId);
        for (Device d : devices) {
            Optional<Permission> p = permissionRepository.findByUserIdAndDeviceId(user.getId(), d.getId());
            if (p.isPresent()) permissionRepository.delete(p.get());
        }

        Permission permission = new Permission();
        permission.setUserId(user.getId());
        permission.setPropertyId(propertyId);
        permission.setOwnerId(owner.getId());
        return permissionRepository.save(permission);
    }

    public void removePropertyPermission(String ownerEmail, String userEmail, Integer propertyId) {
        Optional<User> userExists = userRepository.findByEmail(userEmail);
        if (userExists.isEmpty()) {
            logger.error("Error - User with email " + userEmail + " was not found");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "message: User was not found");
        }

        Optional<User> ownerExists = userRepository.findByEmail(ownerEmail);
        if (ownerExists.isEmpty()) {
            logger.error("Error - User with email " + ownerEmail + " was not found");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "message: User was not found");
        }

        User user = userExists.get();
        User owner = ownerExists.get();

        List<Property> properties = propertyRepository.findAllByUserId(owner.getId());
        Boolean found = false;
        for (Property p : properties) {
            if (p.getId() == propertyId) found = true;
        }

        if (!found) {
            logger.error("Error - Owner provided cannot remove permission for a property he does not own");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "message: Property not owned");
        }

        Optional<Permission> exists = permissionRepository.findByUserIdAndPropertyId(user.getId(), propertyId);
        if (exists.isEmpty()) {
            logger.error("Error - Permission for propertyId " + propertyId + " on userId " + user.getId() + " was not found");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "message: Permission not found");
        }

        Permission permission = exists.get();
        permissionRepository.delete(permission);
    }

    public void removeDevicePermission(String ownerEmail, String userEmail, Integer deviceId) {
        Optional<User> userExists = userRepository.findByEmail(userEmail);
        if (userExists.isEmpty()) {
            logger.error("Error - User with email " + userEmail + " was not found");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "message: User was not found");
        }

        Optional<User> ownerExists = userRepository.findByEmail(ownerEmail);
        if (ownerExists.isEmpty()) {
            logger.error("Error - User with email " + ownerEmail + " was not found");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "message: User was not found");
        }

        User user = userExists.get();
        User owner = ownerExists.get();

        Optional<Permission> exists = permissionRepository.findByUserIdAndDeviceId(user.getId(), deviceId);
        if (exists.isEmpty()) {
            logger.error("Error - Permission for deviceId " + deviceId + " on userId " + user.getId() + " was not found");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "message: Permission not found");
        }

        Permission permission = exists.get();
        permissionRepository.delete(permission);
    }

    public UserPermissionsDTO getUserPermissions(Integer userId) {
        List<Permission> permissions = permissionRepository.findAllByUserId(userId);
        UserPermissionsDTO dto = new UserPermissionsDTO();
        List<PropertyDTO> propertyIds = new ArrayList<PropertyDTO>();
        List<DeviceDTO> deviceIds = new ArrayList<DeviceDTO>();

        for (Permission p : permissions) {
            if (p.getPropertyId() == null) deviceIds.add(new DeviceDTO(deviceRepository.findById(p.getDeviceId()).get()));
            else propertyIds.add(new PropertyDTO(propertyRepository.findById(p.getPropertyId()).get()));
        }

        dto.setDevices(deviceIds);
        dto.setProperties(propertyIds);
        return dto;
    }

    public List<OwnerPermissionsDTO> getOwnerPermissions(Integer ownerId) {
        List<Permission> permissions = permissionRepository.findAllByOwnerId(ownerId);
        List<OwnerPermissionsDTO> dtos = new ArrayList<OwnerPermissionsDTO>();
        for (Permission p : permissions) {
            OwnerPermissionsDTO dto = new OwnerPermissionsDTO();
            String userEmail = userRepository.findById(p.getUserId()).get().getEmail();
            if (p.getPropertyId() == null)
                dto.setDevice(new DeviceDTO(deviceRepository.findById(p.getDeviceId()).get()));
            else
                dto.setProperty(new PropertyDTO(propertyRepository.findById(p.getPropertyId()).get()));
            dto.setUserEmail(userEmail);
            dtos.add(dto);
        }
        return dtos;
    }
}
