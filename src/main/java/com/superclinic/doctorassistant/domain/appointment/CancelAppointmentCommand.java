package com.superclinic.doctorassistant.domain.appointment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CancelAppointmentCommand(
        @NotNull UUID patientId,
        @NotBlank String referenceCode,
        String cancellationReason
) {
}
