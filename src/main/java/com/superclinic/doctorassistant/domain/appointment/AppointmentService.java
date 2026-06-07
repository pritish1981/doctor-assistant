package com.superclinic.doctorassistant.domain.appointment;

import com.superclinic.doctorassistant.common.exception.BusinessValidationException;
import com.superclinic.doctorassistant.common.exception.ConflictException;
import com.superclinic.doctorassistant.common.exception.ResourceNotFoundException;
import com.superclinic.doctorassistant.common.util.AppointmentReferenceGenerator;
import com.superclinic.doctorassistant.domain.availability.AvailabilityService;
import com.superclinic.doctorassistant.domain.doctor.DoctorService;
import com.superclinic.doctorassistant.domain.patient.PatientService;
import com.superclinic.doctorassistant.persistence.entity.Appointment;
import com.superclinic.doctorassistant.persistence.entity.Availability;
import com.superclinic.doctorassistant.persistence.entity.Doctor;
import com.superclinic.doctorassistant.persistence.entity.Patient;
import com.superclinic.doctorassistant.persistence.entity.enums.AppointmentStatus;
import com.superclinic.doctorassistant.persistence.repository.AppointmentRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class AppointmentService {

    private static final ZoneId CLINIC_ZONE = ZoneId.of("UTC");

    private final AppointmentRepository appointmentRepository;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final AvailabilityService availabilityService;

    @Transactional
    public Appointment bookAppointment(@Valid BookAppointmentCommand command) {
        log.info("Booking appointment for patientId={}, doctorId={}, availabilityId={}",
                command.patientId(), command.doctorId(), command.availabilityId());

        Optional<Appointment> existing = findByIdempotencyKey(command.idempotencyKey());
        if (existing.isPresent()) {
            log.info("Returning existing appointment for idempotency key: {}", command.idempotencyKey());
            return loadWithDetails(existing.get());
        }

        Patient patient = patientService.getActivePatient(command.patientId());
        Doctor doctor = doctorService.getActiveDoctor(command.doctorId());
        Availability slot = availabilityService.validateAndLockOpenSlot(
                command.availabilityId(), command.doctorId());

        short durationMinutes = calculateDurationMinutes(slot);
        String referenceCode = AppointmentReferenceGenerator.generate();

        Appointment appointment = Appointment.createBooking(
                referenceCode,
                patient,
                doctor,
                slot,
                durationMinutes,
                command.reason(),
                command.idempotencyKey());

        availabilityService.markSlotBooked(slot);
        Appointment saved = appointmentRepository.save(appointment);

        log.info("Appointment booked successfully: referenceCode={}, id={}",
                saved.getReferenceCode(), saved.getId());
        return loadWithDetails(saved);
    }

    @Transactional
    public Appointment cancelAppointment(@Valid CancelAppointmentCommand command) {
        log.info("Cancelling appointment referenceCode={} for patientId={}",
                command.referenceCode(), command.patientId());

        Appointment appointment = appointmentRepository
                .findByReferenceCodeWithDetails(command.referenceCode())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", command.referenceCode()));

        if (!appointment.getPatient().getId().equals(command.patientId())) {
            throw new BusinessValidationException(
                    "Appointment %s does not belong to patient %s"
                            .formatted(command.referenceCode(), command.patientId()));
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            log.info("Appointment already cancelled: {}", command.referenceCode());
            return appointment;
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED
                || appointment.getStatus() == AppointmentStatus.NO_SHOW) {
            throw new ConflictException(
                    "Cannot cancel appointment with status: " + appointment.getStatus());
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelledAt(Instant.now());
        appointment.setCancellationReason(command.cancellationReason());
        availabilityService.releaseSlot(appointment.getAvailability());

        Appointment saved = appointmentRepository.save(appointment);
        log.info("Appointment cancelled successfully: referenceCode={}", saved.getReferenceCode());
        return saved;
    }

    @Transactional(readOnly = true)
    public Appointment getByReferenceCode(String referenceCode) {
        log.debug("Fetching appointment by reference code: {}", referenceCode);
        return appointmentRepository.findByReferenceCode(referenceCode)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", referenceCode));
    }

    @Transactional(readOnly = true)
    public List<Appointment> findAppointmentByDate(LocalDate date) {
        return findAppointmentByDate(date, null, null, null);
    }

    @Transactional(readOnly = true)
    public List<Appointment> findAppointmentByDate(
            LocalDate date,
            UUID doctorId,
            UUID patientId,
            AppointmentStatus status) {
        if (date == null) {
            throw new BusinessValidationException("Date must not be null");
        }

        Instant startInclusive = date.atStartOfDay(CLINIC_ZONE).toInstant();
        Instant endExclusive = date.plusDays(1).atStartOfDay(CLINIC_ZONE).toInstant();

        log.debug("Finding appointments for date={}, doctorId={}, patientId={}, status={}",
                date, doctorId, patientId, status);
        List<Appointment> appointments = appointmentRepository.findAppointmentByDate(
                startInclusive, endExclusive, doctorId, patientId, status);
        log.info("Found {} appointment(s) for date={}", appointments.size(), date);
        return appointments;
    }

    private Optional<Appointment> findByIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return Optional.empty();
        }
        return appointmentRepository.findByIdempotencyKey(idempotencyKey);
    }

    private short calculateDurationMinutes(Availability slot) {
        long minutes = Duration.between(slot.getSlotStart(), slot.getSlotEnd()).toMinutes();
        if (minutes <= 0 || minutes > 480) {
            throw new BusinessValidationException(
                    "Invalid slot duration (%d minutes) for availability: %s".formatted(minutes, slot.getId()));
        }
        return (short) minutes;
    }

    private Appointment loadWithDetails(Appointment appointment) {
        return appointmentRepository
                .findByReferenceCodeWithDetails(appointment.getReferenceCode())
                .orElse(appointment);
    }
}
