package com.agriself.repository;

import com.agriself.model.Farm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface FarmRepository extends JpaRepository<Farm, Long> {

    List<Farm> findByFarmerId(Long farmerId);

    long countBySoilType(String soilType);

    @Query("SELECT f.soilType as soilType, COUNT(f) as count FROM Farm f GROUP BY f.soilType")
    List<Map<String, Object>> getSoilDistributionStats();
}
