package com.agriself.repository;

import com.agriself.model.Machinery;
import com.agriself.model.MachineryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MachineryRepository extends JpaRepository<Machinery, Long> {
    Optional<Machinery> findByEquipmentCode(String equipmentCode);
    List<Machinery> findByStatus(MachineryStatus status);
    long countByStatus(MachineryStatus status);
    List<Machinery> findAllByOrderByEquipmentCodeAsc();
}
