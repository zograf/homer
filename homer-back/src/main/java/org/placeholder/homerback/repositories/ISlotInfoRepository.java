package org.placeholder.homerback.repositories;

import org.placeholder.homerback.entities.Schedule;
import org.placeholder.homerback.entities.SlotInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ISlotInfoRepository extends JpaRepository<SlotInfo, Integer> {
}
