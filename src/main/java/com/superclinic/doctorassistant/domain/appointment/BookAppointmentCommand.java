package com.superclinic.doctorassistant.domain.appointment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record BookAppointmentCommand(
        @NotNull UUID patientId,
        @NotNull UUID doctorId,
        @NotNull UUID availabilityId,
        String reason,
        @Size(max = 100) String idempotencyKey
) {
}
