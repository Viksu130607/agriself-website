package com.agriself.repository;

import com.agriself.model.BookingStatus;
import com.agriself.model.EquipmentBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EquipmentBookingRepository extends JpaRepository<EquipmentBooking, Long> {

    List<EquipmentBooking> findByUserIdAndStartTimeAfter(Long userId, LocalDateTime time);

    List<EquipmentBooking> findByUserIdOrderByStartTimeDesc(Long userId);

    List<EquipmentBooking> findByStatus(BookingStatus status);

    @Query("SELECT b FROM EquipmentBooking b WHERE b.equipmentName = :equipmentName " +
           "AND b.status NOT IN ('CANCELLED') " +
           "AND (:startTime < b.endTime AND :endTime > b.startTime)")
    List<EquipmentBooking> findOverlappingBookings(
            @Param("equipmentName") String equipmentName,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}
