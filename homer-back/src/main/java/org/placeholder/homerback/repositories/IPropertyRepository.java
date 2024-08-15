package org.placeholder.homerback.repositories;

import org.placeholder.homerback.entities.EPropertyStatus;
import org.placeholder.homerback.entities.Property;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface IPropertyRepository extends JpaRepository<Property, Integer> {

    List<Property> findAllByStatus(EPropertyStatus status);
    List<Property> findAllByStatusAndUserId(EPropertyStatus status, Integer userId);
    List<Property> findAllByUserId(Integer userId);
    List<Property> findByStatusIsInAndUserId(List<EPropertyStatus> statuses, Integer userId);
    List<Property> findByStatusIsIn(List<EPropertyStatus> statuses);
}
