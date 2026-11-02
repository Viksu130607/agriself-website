package com.agriself.service;

import com.agriself.model.Farm;
import com.agriself.model.User;
import com.agriself.repository.FarmRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FarmService {

    private final FarmRepository farmRepository;

    @Transactional(readOnly = true)
    public List<Farm> getFarmsByFarmerId(Long farmerId) {
        return farmRepository.findByFarmerId(farmerId);
    }

    @Transactional(readOnly = true)
    public List<Farm> getAllFarms() {
        return farmRepository.findAll();
    }

    @Transactional
    public Farm registerFarm(Farm farm, User farmer) {
        farm.setFarmer(farmer);
        return farmRepository.save(farm);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getSoilTypeDistribution() {
        List<String> typicalSoils = List.of("Black Cotton", "Alluvial", "Red Loam", "Clayey Loam", "Sandy Loam");
        Map<String, Long> distribution = new HashMap<>();

        for (String soil : typicalSoils) {
            long count = farmRepository.countBySoilType(soil);
            distribution.put(soil, count);
        }

        // Also query custom/others
        try {
            List<Map<String, Object>> stats = farmRepository.getSoilDistributionStats();
            for (Map<String, Object> stat : stats) {
                String type = (String) stat.get("soilType");
                Long count = ((Number) stat.get("count")).longValue();
                distribution.put(type, count);
            }
        } catch (Exception ignored) {
        }

        return distribution;
    }
}
