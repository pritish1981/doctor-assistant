package com.superclinic.doctorassistant.ai.tools.dto;

import com.superclinic.doctorassistant.persistence.entity.Appointment;

import java.time.Instant;
import java.util.UUID;

public record AppointmentBookingResult(
        UUID appointmentId,
        String referenceCode,
        UUID patientId,
        UUID doctorId,
        String doctorName,
        String specialtyName,
        Instant scheduledAt,
        Short durationMinutes,
        String status,
        String message
) {

    public static AppointmentBookingResult from(Appointment appointment, String message) {
        return new AppointmentBookingResult(
                appointment.getId(),
                appointment.getReferenceCode(),
                appointment.getPatient().getId(),
                appointment.getDoctor().getId(),
                appointment.getDoctor().getFullName(),
                appointment.getDoctor().getSpecialty().getName(),
                appointment.getScheduledAt(),
                appointment.getDurationMinutes(),
                appointment.getStatus().name(),
                message);
    }
}
