package com.superclinic.doctorassistant.persistence.repository;

import com.superclinic.doctorassistant.persistence.entity.Appointment;
import com.superclinic.doctorassistant.persistence.entity.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    Optional<Appointment> findByReferenceCode(String referenceCode);

    @Query("""
            SELECT a FROM Appointment a
            JOIN FETCH a.patient
            JOIN FETCH a.doctor d
            JOIN FETCH d.specialty
            JOIN FETCH a.availability
            WHERE a.referenceCode = :referenceCode
            """)
    Optional<Appointment> findByReferenceCodeWithDetails(@Param("referenceCode") String referenceCode);

    Optional<Appointment> findByIdempotencyKey(String idempotencyKey);

    @Query("""
            SELECT a FROM Appointment a
            JOIN FETCH a.patient p
            JOIN FETCH a.doctor d
            JOIN FETCH d.specialty
            JOIN FETCH a.availability
            WHERE a.scheduledAt >= :startInclusive
              AND a.scheduledAt < :endExclusive
              AND (:doctorId IS NULL OR d.id = :doctorId)
              AND (:patientId IS NULL OR p.id = :patientId)
              AND (:status IS NULL OR a.status = :status)
            ORDER BY a.scheduledAt ASC
            """)
    List<Appointment> findAppointmentByDate(
            @Param("startInclusive") Instant startInclusive,
            @Param("endExclusive") Instant endExclusive,
            @Param("doctorId") UUID doctorId,
            @Param("patientId") UUID patientId,
            @Param("status") AppointmentStatus status);

    default List<Appointment> findAppointmentByDate(
            Instant startInclusive,
            Instant endExclusive) {
        return findAppointmentByDate(startInclusive, endExclusive, null, null, null);
    }

    default List<Appointment> findAppointmentByDate(
            Instant startInclusive,
            Instant endExclusive,
            UUID doctorId) {
        return findAppointmentByDate(startInclusive, endExclusive, doctorId, null, null);
    }

    default List<Appointment> findAppointmentByDate(
            Instant startInclusive,
            Instant endExclusive,
            UUID doctorId,
            UUID patientId) {
        return findAppointmentByDate(startInclusive, endExclusive, doctorId, patientId, null);
    }
}
