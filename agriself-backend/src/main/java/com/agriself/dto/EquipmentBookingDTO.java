package com.agriself.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class EquipmentBookingDTO {

    @NotBlank(message = "Equipment name is required")
    private String equipmentName;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private LocalDateTime endTime;

    @NotNull(message = "Hourly rate is required")
    private Double hourlyRate;

    private String farmLocation;
    private String notes;

    public EquipmentBookingDTO() {
    }

    public EquipmentBookingDTO(String equipmentName, String category, LocalDateTime startTime,
                               LocalDateTime endTime, Double hourlyRate, String farmLocation, String notes) {
        this.equipmentName = equipmentName;
        this.category = category;
        this.startTime = startTime;
        this.endTime = endTime;
        this.hourlyRate = hourlyRate;
        this.farmLocation = farmLocation;
        this.notes = notes;
    }

    public static EquipmentBookingDTOBuilder builder() {
        return new EquipmentBookingDTOBuilder();
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(Double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public String getFarmLocation() {
        return farmLocation;
    }

    public void setFarmLocation(String farmLocation) {
        this.farmLocation = farmLocation;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public static class EquipmentBookingDTOBuilder {
        private String equipmentName;
        private String category;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Double hourlyRate;
        private String farmLocation;
        private String notes;

        EquipmentBookingDTOBuilder() {
        }

        public EquipmentBookingDTOBuilder equipmentName(String equipmentName) {
            this.equipmentName = equipmentName;
            return this;
        }

        public EquipmentBookingDTOBuilder category(String category) {
            this.category = category;
            return this;
        }

        public EquipmentBookingDTOBuilder startTime(LocalDateTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public EquipmentBookingDTOBuilder endTime(LocalDateTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public EquipmentBookingDTOBuilder hourlyRate(Double hourlyRate) {
            this.hourlyRate = hourlyRate;
            return this;
        }

        public EquipmentBookingDTOBuilder farmLocation(String farmLocation) {
            this.farmLocation = farmLocation;
            return this;
        }

        public EquipmentBookingDTOBuilder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public EquipmentBookingDTO build() {
            return new EquipmentBookingDTO(equipmentName, category, startTime, endTime, hourlyRate, farmLocation, notes);
        }
    }
}
