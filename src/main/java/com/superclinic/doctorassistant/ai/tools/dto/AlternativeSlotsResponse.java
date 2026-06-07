package com.superclinic.doctorassistant.ai.tools.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AlternativeSlotsResponse(
        UUID doctorId,
        LocalDate requestedDate,
        boolean slotsAvailableOnRequestedDate,
        int totalAlternatives,
        List<DatedAvailabilitySlots> alternatives
) {
}
