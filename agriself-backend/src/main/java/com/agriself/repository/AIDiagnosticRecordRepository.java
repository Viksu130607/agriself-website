package com.agriself.repository;

import com.agriself.model.AIDiagnosticRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AIDiagnosticRecordRepository extends JpaRepository<AIDiagnosticRecord, Long> {

    List<AIDiagnosticRecord> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<AIDiagnosticRecord> findTop20ByOrderByCreatedAtDesc();
}
