package org.placeholder.homerback.repositories;

import org.placeholder.homerback.entities.modules.AbstractModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IModuleRepository extends JpaRepository<AbstractModule, Integer> {

}
