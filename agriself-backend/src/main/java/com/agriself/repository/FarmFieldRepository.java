package com.agriself.repository;

import com.agriself.model.FarmField;
import com.agriself.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FarmFieldRepository extends JpaRepository<FarmField, Long> {
    List<FarmField> findByFarmer(User farmer);
    List<FarmField> findByFarmerEmail(String email);
}
