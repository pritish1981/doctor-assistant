package com.superclinic.doctorassistant.ai.tools.dto;

import java.util.List;
import java.util.UUID;

public record DoctorWithOpenSlots(
        UUID doctorId,
        String doctorName,
        String specialtyCode,
        String specialtyName,
        int openSlotCount,
        List<AvailabilitySlotResult> slots
) {
}
