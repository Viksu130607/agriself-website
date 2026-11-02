package com.agriself.repository;

import com.agriself.model.MandiPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MandiPriceRepository extends JpaRepository<MandiPrice, Long> {
    List<MandiPrice> findAllByOrderByModalPricePerQuintalDesc();
    List<MandiPrice> findByCommodityIgnoreCase(String commodity);
    List<MandiPrice> findByStateIgnoreCase(String state);
}
