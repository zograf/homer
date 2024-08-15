package org.placeholder.homerback.repositories;

import org.placeholder.homerback.entities.City;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ICityRepository extends JpaRepository<City, Integer> {
}
