package org.placeholder.homerback.repositories;

import org.placeholder.homerback.entities.Device;
import org.placeholder.homerback.entities.EDeviceType;
import org.placeholder.homerback.entities.EPropertyStatus;
import org.placeholder.homerback.entities.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IDeviceRepository extends JpaRepository<Device, Integer> {

    List<Device> findAll();
    List<Device> findAllByPropertyId(Integer propertyId);

    List<Device> findAllByTypeAndPropertyId(EDeviceType type, Integer propertyId);
}
