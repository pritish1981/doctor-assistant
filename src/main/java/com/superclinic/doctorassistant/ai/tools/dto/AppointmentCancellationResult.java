package com.superclinic.doctorassistant.ai.tools.dto;

import com.superclinic.doctorassistant.persistence.entity.Appointment;

import java.time.Instant;
import java.util.UUID;

public record AppointmentCancellationResult(
        UUID appointmentId,
        String referenceCode,
        UUID patientId,
        String doctorName,
        Instant scheduledAt,
        String status,
        Instant cancelledAt,
        String message
) {

    public static AppointmentCancellationResult from(Appointment appointment, String message) {
        return new AppointmentCancellationResult(
                appointment.getId(),
                appointment.getReferenceCode(),
                appointment.getPatient().getId(),
                appointment.getDoctor().getFullName(),
                appointment.getScheduledAt(),
                appointment.getStatus().name(),
                appointment.getCancelledAt(),
                message);
    }
}
