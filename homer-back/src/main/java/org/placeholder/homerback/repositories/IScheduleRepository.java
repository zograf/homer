package org.placeholder.homerback.repositories;

import org.placeholder.homerback.entities.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IScheduleRepository extends JpaRepository<Schedule, Integer> {
}
