package com.superclinic.doctorassistant.persistence.repository;

import com.superclinic.doctorassistant.persistence.entity.Availability;
import com.superclinic.doctorassistant.persistence.entity.enums.AvailabilityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, UUID> {

    @Query("""
            SELECT a FROM Availability a
            JOIN FETCH a.doctor d
            JOIN FETCH d.specialty
            WHERE a.status = :status
              AND (:doctorId IS NULL OR d.id = :doctorId)
              AND a.slotStart >= :startInclusive
              AND a.slotStart < :endExclusive
            ORDER BY a.slotStart ASC
            """)
    List<Availability> findAvailableSlots(
            @Param("doctorId") UUID doctorId,
            @Param("status") AvailabilityStatus status,
            @Param("startInclusive") Instant startInclusive,
            @Param("endExclusive") Instant endExclusive
    );

    default List<Availability> findAvailableSlots(
            UUID doctorId,
            Instant startInclusive,
            Instant endExclusive) {
        return findAvailableSlots(doctorId, AvailabilityStatus.OPEN, startInclusive, endExclusive);
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT a FROM Availability a
            JOIN FETCH a.doctor
            WHERE a.id = :id
              AND a.status = :status
            """)
    Optional<Availability> findByIdAndStatusForUpdate(
            @Param("id") UUID id,
            @Param("status") AvailabilityStatus status);

    @Query("""
            SELECT a FROM Availability a
            JOIN FETCH a.doctor d
            JOIN FETCH d.specialty s
            WHERE a.status = :status
              AND d.active = true
              AND a.slotStart >= :startInclusive
              AND a.slotStart < :endExclusive
              AND (
                    LOWER(s.code) = LOWER(:speciality)
                 OR LOWER(s.name) = LOWER(:speciality)
                 OR LOWER(s.code) LIKE LOWER(CONCAT('%', :speciality, '%'))
                 OR LOWER(s.name) LIKE LOWER(CONCAT('%', :speciality, '%'))
                 OR LOWER(:speciality) LIKE LOWER(CONCAT('%', s.name, '%'))
              )
            ORDER BY d.ratingAvg DESC, d.fullName ASC, a.slotStart ASC
            """)
    List<Availability> findOpenSlotsBySpeciality(
            @Param("speciality") String speciality,
            @Param("status") AvailabilityStatus status,
            @Param("startInclusive") Instant startInclusive,
            @Param("endExclusive") Instant endExclusive);
}
