package com.superclinic.doctorassistant.ai.tools.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AvailabilityResponse(
        UUID doctorId,
        LocalDate date,
        int totalSlots,
        List<AvailabilitySlotResult> slots
) {
}
