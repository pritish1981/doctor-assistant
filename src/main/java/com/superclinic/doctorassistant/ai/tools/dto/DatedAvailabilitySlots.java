package com.superclinic.doctorassistant.ai.tools.dto;

import java.time.LocalDate;
import java.util.List;

public record DatedAvailabilitySlots(
        LocalDate date,
        List<AvailabilitySlotResult> slots
) {
}
