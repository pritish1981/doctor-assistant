package com.superclinic.doctorassistant.domain.availability;

import com.superclinic.doctorassistant.common.exception.BusinessValidationException;
import com.superclinic.doctorassistant.common.exception.ConflictException;
import com.superclinic.doctorassistant.common.exception.ResourceNotFoundException;
import com.superclinic.doctorassistant.persistence.entity.Availability;
import com.superclinic.doctorassistant.persistence.entity.enums.AvailabilityStatus;
import com.superclinic.doctorassistant.persistence.repository.AvailabilityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AvailabilityService {

    private static final ZoneId CLINIC_ZONE = ZoneId.of("UTC");

    private final AvailabilityRepository availabilityRepository;

    public List<Availability> findAvailableSlots(UUID doctorId, LocalDate date) {
        return findAvailableSlots(doctorId, date, CLINIC_ZONE);
    }

    public List<Availability> findAvailableSlots(UUID doctorId, LocalDate date, ZoneId zone) {
        if (date == null) {
            throw new BusinessValidationException("Date must not be null");
        }
        Instant startInclusive = date.atStartOfDay(zone).toInstant();
        Instant endExclusive = date.plusDays(1).atStartOfDay(zone).toInstant();

        log.debug("Finding available slots for doctorId={}, date={}", doctorId, date);
        List<Availability> slots = availabilityRepository.findAvailableSlots(doctorId, startInclusive, endExclusive);
        log.info("Found {} available slot(s) for doctorId={} on {}", slots.size(), doctorId, date);
        return slots;
    }

    public List<Availability> suggestAlternativeSlots(
            UUID doctorId,
            LocalDate preferredDate,
            int searchDays,
            int maxSlotsPerDay) {
        if (preferredDate == null) {
            throw new BusinessValidationException("Preferred date must not be null");
        }
        if (searchDays < 1) {
            throw new BusinessValidationException("Search days must be at least 1");
        }
        if (maxSlotsPerDay < 1) {
            throw new BusinessValidationException("Max slots per day must be at least 1");
        }

        LocalDate searchStart = resolveSearchStartDate(preferredDate);

        log.debug("Suggesting alternative slots for doctorId={}, preferredDate={}, searchStart={}, searchDays={}",
                doctorId, preferredDate, searchStart, searchDays);

        ArrayList<Availability> alternatives = new ArrayList<>();
        for (int dayOffset = 0; dayOffset < searchDays; dayOffset++) {
            LocalDate searchDate = searchStart.plusDays(dayOffset);
            List<Availability> daySlots = findAvailableSlots(doctorId, searchDate);
            int limit = Math.min(daySlots.size(), maxSlotsPerDay);
            alternatives.addAll(daySlots.subList(0, limit));
        }

        log.info("Found {} alternative slot(s) for doctorId={} starting from {}",
                alternatives.size(), doctorId, preferredDate);
        return alternatives;
    }

    public List<Availability> findOpenSlotsBySpeciality(String speciality, LocalDate fromDate, int searchDays) {
        if (!org.springframework.util.StringUtils.hasText(speciality)) {
            throw new BusinessValidationException("Speciality must not be blank");
        }
        if (fromDate == null) {
            throw new BusinessValidationException("From date must not be null");
        }
        if (searchDays < 1) {
            throw new BusinessValidationException("Search days must be at least 1");
        }

        LocalDate searchStart = resolveSearchStartDate(fromDate);
        Instant startInclusive = searchStart.atStartOfDay(CLINIC_ZONE).toInstant();
        Instant endExclusive = searchStart.plusDays(searchDays).atStartOfDay(CLINIC_ZONE).toInstant();

        log.debug("Finding open slots for speciality={}, fromDate={}, searchStart={}, searchDays={}",
                speciality, fromDate, searchStart, searchDays);

        List<Availability> slots = availabilityRepository.findOpenSlotsBySpeciality(
                speciality.trim(),
                AvailabilityStatus.OPEN,
                startInclusive,
                endExclusive);

        log.info("Found {} open slot(s) for speciality '{}' between {} and {}",
                slots.size(), speciality, searchStart, searchStart.plusDays(searchDays - 1L));
        return slots;
    }

    /**
     * Multi-day availability searches always include today so slots on the current day
     * are not skipped when the model passes tomorrow as the start date.
     */
    static LocalDate resolveSearchStartDate(LocalDate requestedDate) {
        return resolveSearchStartDate(requestedDate, CLINIC_ZONE);
    }

    static LocalDate resolveSearchStartDate(LocalDate requestedDate, ZoneId zone) {
        LocalDate today = LocalDate.now(zone);
        if (requestedDate.isBefore(today)) {
            return today;
        }
        if (requestedDate.isAfter(today)) {
            return today;
        }
        return requestedDate;
    }

    @Transactional
    public Availability validateAndLockOpenSlot(UUID availabilityId, UUID doctorId) {
        log.debug("Validating and locking slot id={} for doctorId={}", availabilityId, doctorId);

        Availability slot = availabilityRepository
                .findByIdAndStatusForUpdate(availabilityId, AvailabilityStatus.OPEN)
                .orElseThrow(() -> {
                    if (availabilityRepository.existsById(availabilityId)) {
                        return new ConflictException("Availability slot is not open for booking: " + availabilityId);
                    }
                    return new ResourceNotFoundException("Availability", availabilityId);
                });

        if (!slot.getDoctor().getId().equals(doctorId)) {
            throw new BusinessValidationException(
                    "Availability slot %s does not belong to doctor %s".formatted(availabilityId, doctorId));
        }

        if (!slot.getSlotStart().isAfter(Instant.now())) {
            throw new BusinessValidationException("Cannot book a slot in the past: " + availabilityId);
        }

        log.debug("Slot id={} validated and locked for booking", availabilityId);
        return slot;
    }

    @Transactional
    public void markSlotBooked(Availability slot) {
        log.debug("Marking slot id={} as BOOKED", slot.getId());
        slot.setStatus(AvailabilityStatus.BOOKED);
        slot.setHoldExpiresAt(null);
        availabilityRepository.save(slot);
    }

    @Transactional
    public void releaseSlot(Availability slot) {
        log.debug("Releasing slot id={} back to OPEN", slot.getId());
        slot.setStatus(AvailabilityStatus.OPEN);
        slot.setHoldExpiresAt(null);
        availabilityRepository.save(slot);
    }
}
