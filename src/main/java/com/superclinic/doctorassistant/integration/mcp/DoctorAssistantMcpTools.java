package com.superclinic.doctorassistant.integration.mcp;

import com.superclinic.doctorassistant.ai.tools.dto.AppointmentBookingResult;
import com.superclinic.doctorassistant.ai.tools.dto.AvailabilityResponse;
import com.superclinic.doctorassistant.ai.tools.dto.AvailabilitySlotResult;
import com.superclinic.doctorassistant.ai.tools.dto.DoctorSearchResult;
import com.superclinic.doctorassistant.common.exception.BusinessValidationException;
import com.superclinic.doctorassistant.domain.appointment.AppointmentService;
import com.superclinic.doctorassistant.domain.appointment.BookAppointmentCommand;
import com.superclinic.doctorassistant.domain.availability.AvailabilityService;
import com.superclinic.doctorassistant.domain.doctor.DoctorService;
import com.superclinic.doctorassistant.persistence.entity.Appointment;
import com.superclinic.doctorassistant.persistence.entity.Doctor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * MCP-facing tool surface for external AI hosts (Cursor, Claude Desktop, custom agents).
 * Exposes a curated subset of clinic operations with stable tool names.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DoctorAssistantMcpTools {

    private final DoctorService doctorService;
    private final AvailabilityService availabilityService;
    private final AppointmentService appointmentService;

    @Tool(
            name = "findDoctor",
            description = """
                    Search active doctors at Super Clinic by name and/or medical specialty. \
                    Provide at least one of name or specialty. \
                    Returns doctor UUID, specialty, rating, languages, and contact details. \
                    Use the returned doctor UUID with getAvailability."""
    )
    public List<DoctorSearchResult> findDoctor(
            @ToolParam(description = "Doctor full or partial name, e.g. 'Sharma'", required = false)
            String name,
            @ToolParam(description = "Specialty code or name, e.g. 'CARDIOLOGY'", required = false)
            String specialty) {
        log.info("MCP tool findDoctor(name={}, specialty={})", name, specialty);

        if (!StringUtils.hasText(name) && !StringUtils.hasText(specialty)) {
            throw new BusinessValidationException("Provide at least one of name or specialty");
        }

        List<Doctor> doctors;
        if (StringUtils.hasText(name) && StringUtils.hasText(specialty)) {
            String normalizedName = name.trim().toLowerCase(Locale.ROOT);
            doctors = doctorService.findDoctorsBySpeciality(specialty.trim()).stream()
                    .filter(doctor -> doctor.getFullName().toLowerCase(Locale.ROOT).contains(normalizedName))
                    .toList();
        } else if (StringUtils.hasText(name)) {
            doctors = doctorService.findDoctorByName(name.trim());
        } else {
            doctors = doctorService.findDoctorsBySpeciality(specialty.trim());
        }

        List<DoctorSearchResult> results = doctors.stream()
                .map(DoctorSearchResult::from)
                .toList();
        log.info("MCP tool findDoctor returned {} result(s)", results.size());
        return results;
    }

    @Tool(
            name = "getAvailability",
            description = """
                    Get open appointment slots for a doctor on a specific date. \
                    Requires doctorId from findDoctor. Date format: yyyy-MM-dd. \
                    Returns availability slot UUIDs required by bookAppointment."""
    )
    public AvailabilityResponse getAvailability(
            @ToolParam(description = "Doctor UUID from findDoctor")
            UUID doctorId,
            @ToolParam(description = "Appointment date in ISO format yyyy-MM-dd")
            String date) {
        log.info("MCP tool getAvailability(doctorId={}, date={})", doctorId, date);

        doctorService.getActiveDoctor(doctorId);
        LocalDate appointmentDate = parseDate(date);

        List<AvailabilitySlotResult> slots = availabilityService
                .findAvailableSlots(doctorId, appointmentDate).stream()
                .map(AvailabilitySlotResult::from)
                .toList();

        log.info("MCP tool getAvailability returned {} slot(s)", slots.size());
        return new AvailabilityResponse(doctorId, appointmentDate, slots.size(), slots);
    }

    @Tool(
            name = "bookAppointment",
            description = """
                    Book an appointment for a patient using an open slot. \
                    Requires patientId, doctorId, and availabilityId from prior tool calls. \
                    Returns confirmation reference code and scheduled time."""
    )
    public AppointmentBookingResult bookAppointment(
            @ToolParam(description = "Patient UUID")
            UUID patientId,
            @ToolParam(description = "Doctor UUID from findDoctor")
            UUID doctorId,
            @ToolParam(description = "Availability slot UUID from getAvailability")
            UUID availabilityId,
            @ToolParam(description = "Reason for visit or symptoms summary", required = false)
            String reason,
            @ToolParam(description = "Idempotency key to prevent duplicate bookings", required = false)
            String idempotencyKey) {
        log.info("MCP tool bookAppointment(patientId={}, doctorId={}, availabilityId={})",
                patientId, doctorId, availabilityId);

        String resolvedIdempotencyKey = StringUtils.hasText(idempotencyKey)
                ? idempotencyKey
                : "mcp-book-" + UUID.randomUUID();

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

        log.info("MCP tool bookAppointment succeeded: referenceCode={}", appointment.getReferenceCode());
        return AppointmentBookingResult.from(appointment, message);
    }

    private LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date);
        } catch (Exception ex) {
            throw new BusinessValidationException("Invalid date format. Expected yyyy-MM-dd, got: " + date);
        }
    }
}
