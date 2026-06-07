package com.superclinic.doctorassistant.ai.tools;

import com.superclinic.doctorassistant.ai.tools.dto.AppointmentBookingResult;
import com.superclinic.doctorassistant.ai.tools.dto.AppointmentCancellationResult;
import com.superclinic.doctorassistant.domain.appointment.AppointmentService;
import com.superclinic.doctorassistant.domain.appointment.BookAppointmentCommand;
import com.superclinic.doctorassistant.domain.appointment.CancelAppointmentCommand;
import com.superclinic.doctorassistant.persistence.entity.Appointment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingTool {

    private final AppointmentService appointmentService;

    @Tool(
            name = "bookAppointment",
            description = """
                    Book an appointment for a patient with a doctor using an open availability slot. \
                    Requires patientId, doctorId, and availabilityId (slotId) from prior tool calls. \
                    Returns a confirmation reference code. \
                    Use only after the patient confirms the doctor and time slot."""
    )
    public AppointmentBookingResult bookAppointment(
            @ToolParam(description = "Patient UUID")
            UUID patientId,
            @ToolParam(description = "Doctor UUID from doctor search")
            UUID doctorId,
            @ToolParam(description = "Availability slot UUID from getAvailability")
            UUID availabilityId,
            @ToolParam(description = "Reason for visit or symptoms summary", required = false)
            String reason,
            @ToolParam(description = "Unique key to prevent duplicate bookings on retry", required = false)
            String idempotencyKey) {
        log.info("Tool invoked: bookAppointment(patientId={}, doctorId={}, availabilityId={})",
                patientId, doctorId, availabilityId);

        String resolvedIdempotencyKey = StringUtils.hasText(idempotencyKey)
                ? idempotencyKey
                : "tool-book-" + UUID.randomUUID();

        BookAppointmentCommand command = new BookAppointmentCommand(
                patientId,
                doctorId,
                availabilityId,
                reason,
                resolvedIdempotencyKey);

        Appointment appointment = appointmentService.bookAppointment(command);

        String message = "Appointment confirmed. Reference: %s. Scheduled at %s with %s."
                .formatted(
                        appointment.getReferenceCode(),
                        appointment.getScheduledAt(),
                        appointment.getDoctor().getFullName());

        log.info("Tool bookAppointment succeeded: referenceCode={}", appointment.getReferenceCode());
        return AppointmentBookingResult.from(appointment, message);
    }

    @Tool(
            name = "cancelAppointment",
            description = """
                    Cancel an existing appointment by reference code. \
                    Requires the patientId and reference code (e.g. APT-2026-ABC12345) from the booking confirmation. \
                    Releases the time slot back to availability. \
                    Use when the patient requests to cancel an appointment."""
    )
    public AppointmentCancellationResult cancelAppointment(
            @ToolParam(description = "Patient UUID who owns the appointment")
            UUID patientId,
            @ToolParam(description = "Appointment reference code from booking confirmation, e.g. APT-2026-ABC12345")
            String referenceCode,
            @ToolParam(description = "Reason for cancellation", required = false)
            String cancellationReason) {
        log.info("Tool invoked: cancelAppointment(patientId={}, referenceCode={})", patientId, referenceCode);

        CancelAppointmentCommand command = new CancelAppointmentCommand(
                patientId,
                referenceCode,
                cancellationReason);

        Appointment appointment = appointmentService.cancelAppointment(command);

        String message = appointment.getStatus().name().equals("CANCELLED")
                ? "Appointment %s has been cancelled.".formatted(appointment.getReferenceCode())
                : "Appointment %s is already cancelled.".formatted(appointment.getReferenceCode());

        log.info("Tool cancelAppointment succeeded: referenceCode={}", appointment.getReferenceCode());
        return AppointmentCancellationResult.from(appointment, message);
    }
}
