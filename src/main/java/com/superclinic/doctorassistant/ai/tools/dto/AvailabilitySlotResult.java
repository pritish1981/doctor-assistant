package com.superclinic.doctorassistant.ai.tools.dto;

import com.superclinic.doctorassistant.persistence.entity.Availability;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AvailabilitySlotResult(
        UUID slotId,
        UUID doctorId,
        String doctorName,
        String specialtyName,
        Instant slotStart,
        Instant slotEnd,
        String status
) {

    public static AvailabilitySlotResult from(Availability availability) {
        return new AvailabilitySlotResult(
                availability.getId(),
                availability.getDoctor().getId(),
                availability.getDoctor().getFullName(),
                availability.getDoctor().getSpecialty().getName(),
                availability.getSlotStart(),
                availability.getSlotEnd(),
                availability.getStatus().name());
    }
}
