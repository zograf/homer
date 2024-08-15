package org.placeholder.homerback.repositories;

import org.placeholder.homerback.entities.EPropertyStatus;
import org.placeholder.homerback.entities.Permission;
import org.placeholder.homerback.entities.Property;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IPermissionRepository extends JpaRepository<Permission, Integer> {
    Optional<Permission> findByUserIdAndDeviceId(Integer userId, Integer deviceId);
    Optional<Permission> findByUserIdAndPropertyId(Integer userId, Integer propertyId);

    List<Permission> findAllByUserId(Integer userId);
    List<Permission> findAllByOwnerId(Integer ownerId);
}
