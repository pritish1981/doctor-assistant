package com.superclinic.doctorassistant.ai.tools;

import com.superclinic.doctorassistant.ai.tools.dto.AlternativeSlotsResponse;
import com.superclinic.doctorassistant.ai.tools.dto.AvailabilityResponse;
import com.superclinic.doctorassistant.ai.tools.dto.AvailabilitySlotResult;
import com.superclinic.doctorassistant.ai.tools.dto.DatedAvailabilitySlots;
import com.superclinic.doctorassistant.ai.tools.dto.DoctorWithOpenSlots;
import com.superclinic.doctorassistant.common.exception.BusinessValidationException;
import com.superclinic.doctorassistant.domain.availability.AvailabilityService;
import com.superclinic.doctorassistant.domain.doctor.DoctorService;
import com.superclinic.doctorassistant.persistence.entity.Availability;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AvailabilityTool {

    private static final int DEFAULT_SEARCH_DAYS = 7;
    private static final int DEFAULT_MAX_SLOTS_PER_DAY = 5;

    private final AvailabilityService availabilityService;
    private final DoctorService doctorService;

    @Tool(
            name = "getAvailability",
            description = """
                    Get open appointment slots for a doctor on a specific date. \
                    Returns slot IDs, start/end times, and doctor details. \
                    Use when the patient asks about a doctor's schedule or available times on a given day. \
                    Date format: ISO-8601 (yyyy-MM-dd), e.g. 2026-06-10."""
    )
    public AvailabilityResponse getAvailability(
            @ToolParam(description = "Doctor UUID from a prior doctor search result")
            UUID doctorId,
            @ToolParam(description = "Appointment date in ISO format yyyy-MM-dd")
            String date) {
        log.info("Tool invoked: getAvailability(doctorId={}, date={})", doctorId, date);

        doctorService.getActiveDoctor(doctorId);
        LocalDate appointmentDate = parseDate(date);

        List<AvailabilitySlotResult> slots = availabilityService
                .findAvailableSlots(doctorId, appointmentDate).stream()
                .map(AvailabilitySlotResult::from)
                .toList();

        log.info("Tool getAvailability returned {} slot(s) for doctorId={} on {}",
                slots.size(), doctorId, appointmentDate);

        return new AvailabilityResponse(doctorId, appointmentDate, slots.size(), slots);
    }

    @Tool(
            name = "suggestAlternativeSlots",
            description = """
                    Suggest alternative open appointment slots when the preferred date has no availability \
                    or the patient wants other options. Searches the preferred date and subsequent days. \
                    Returns slots grouped by date. Use after getAvailability returns no slots, \
                    or when the patient asks for other dates or times."""
    )
    public AlternativeSlotsResponse suggestAlternativeSlots(
            @ToolParam(description = "Doctor UUID from a prior doctor search result")
            UUID doctorId,
            @ToolParam(description = "Preferred appointment date in ISO format yyyy-MM-dd")
            String preferredDate,
            @ToolParam(description = "Number of days to search forward from preferred date (default 7)", required = false)
            Integer searchDays,
            @ToolParam(description = "Maximum slots to return per day (default 5)", required = false)
            Integer maxSlotsPerDay) {
        int days = searchDays != null ? searchDays : DEFAULT_SEARCH_DAYS;
        int maxPerDay = maxSlotsPerDay != null ? maxSlotsPerDay : DEFAULT_MAX_SLOTS_PER_DAY;

        log.info("Tool invoked: suggestAlternativeSlots(doctorId={}, preferredDate={}, searchDays={}, maxSlotsPerDay={})",
                doctorId, preferredDate, days, maxPerDay);

        doctorService.getActiveDoctor(doctorId);
        LocalDate requestedDate = parseDate(preferredDate);

        List<Availability> allSlots = availabilityService.suggestAlternativeSlots(
                doctorId, requestedDate, days, maxPerDay);

        List<Availability> requestedDaySlots = availabilityService.findAvailableSlots(doctorId, requestedDate);

        Map<LocalDate, List<AvailabilitySlotResult>> slotsByDate = new LinkedHashMap<>();
        for (Availability slot : allSlots) {
            LocalDate slotDate = slot.getSlotStart().atZone(java.time.ZoneId.of("UTC")).toLocalDate();
            slotsByDate.computeIfAbsent(slotDate, ignored -> new ArrayList<>())
                    .add(AvailabilitySlotResult.from(slot));
        }

        List<DatedAvailabilitySlots> alternatives = slotsByDate.entrySet().stream()
                .map(entry -> new DatedAvailabilitySlots(entry.getKey(), entry.getValue()))
                .toList();

        int totalAlternatives = alternatives.stream()
                .mapToInt(dated -> dated.slots().size())
                .sum();

        log.info("Tool suggestAlternativeSlots returned {} slot(s) across {} day(s)",
                totalAlternatives, alternatives.size());

        return new AlternativeSlotsResponse(
                doctorId,
                requestedDate,
                !requestedDaySlots.isEmpty(),
                totalAlternatives,
                alternatives);
    }

    @Tool(
            name = "findDoctorsWithOpenSlots",
            description = """
                    Find doctors in a specialty who have open appointment slots within a date range. \
                    Use when the preferred doctor has no availability, or the patient wants any available \
                    doctor in a specialty (e.g. Orthopedic, ORTHOPEDICS). Returns each doctor with slot IDs \
                    for booking. Date format: yyyy-MM-dd."""
    )
    public List<DoctorWithOpenSlots> findDoctorsWithOpenSlots(
            @ToolParam(description = "Medical specialty code or name, e.g. ORTHOPEDICS or Orthopedic")
            String speciality,
            @ToolParam(description = "Start date for search in ISO format yyyy-MM-dd (use today's date from system context)")
            String fromDate,
            @ToolParam(description = "Number of days to search forward (default 7)", required = false)
            Integer searchDays,
            @ToolParam(description = "Maximum slots to return per doctor (default 5)", required = false)
            Integer maxSlotsPerDoctor) {
        int days = searchDays != null ? searchDays : DEFAULT_SEARCH_DAYS;
        int maxPerDoctor = maxSlotsPerDoctor != null ? maxSlotsPerDoctor : DEFAULT_MAX_SLOTS_PER_DAY;
        LocalDate searchFrom = parseDate(fromDate);

        log.info("Tool invoked: findDoctorsWithOpenSlots(speciality={}, fromDate={}, searchDays={})",
                speciality, searchFrom, days);

        List<Availability> slots = availabilityService.findOpenSlotsBySpeciality(speciality, searchFrom, days);

        Map<UUID, DoctorWithOpenSlotsBuilder> grouped = new LinkedHashMap<>();
        for (Availability slot : slots) {
            UUID doctorId = slot.getDoctor().getId();
            grouped.computeIfAbsent(doctorId, ignored -> new DoctorWithOpenSlotsBuilder(slot.getDoctor()))
                    .addSlot(AvailabilitySlotResult.from(slot), maxPerDoctor);
        }

        List<DoctorWithOpenSlots> results = grouped.values().stream()
                .map(DoctorWithOpenSlotsBuilder::build)
                .toList();

        log.info("Tool findDoctorsWithOpenSlots returned {} doctor(s) with open slots", results.size());
        return results;
    }

    private static final class DoctorWithOpenSlotsBuilder {
        private final UUID doctorId;
        private final String doctorName;
        private final String specialtyCode;
        private final String specialtyName;
        private final List<AvailabilitySlotResult> slots = new ArrayList<>();

        DoctorWithOpenSlotsBuilder(com.superclinic.doctorassistant.persistence.entity.Doctor doctor) {
            this.doctorId = doctor.getId();
            this.doctorName = doctor.getFullName();
            this.specialtyCode = doctor.getSpecialty().getCode();
            this.specialtyName = doctor.getSpecialty().getName();
        }

        void addSlot(AvailabilitySlotResult slot, int maxSlots) {
            if (slots.size() < maxSlots) {
                slots.add(slot);
            }
        }

        DoctorWithOpenSlots build() {
            return new DoctorWithOpenSlots(
                    doctorId, doctorName, specialtyCode, specialtyName, slots.size(), List.copyOf(slots));
        }
    }

    private LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date);
        } catch (Exception ex) {
            throw new BusinessValidationException("Invalid date format. Expected yyyy-MM-dd, got: " + date);
        }
    }
}
