package com.superclinic.doctorassistant.persistence.repository;

import com.superclinic.doctorassistant.persistence.entity.ConversationSession;
import com.superclinic.doctorassistant.persistence.entity.enums.ConversationSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationSessionRepository extends JpaRepository<ConversationSession, UUID> {

    @Query("""
            SELECT s FROM ConversationSession s
            JOIN FETCH s.patient
            WHERE s.id = :id
            """)
    Optional<ConversationSession> findByIdWithPatient(@Param("id") UUID id);

    List<ConversationSession> findByPatientIdAndStatusOrderByLastActiveAtDesc(
            UUID patientId,
            ConversationSessionStatus status);

    List<ConversationSession> findByPatientIdOrderByLastActiveAtDesc(UUID patientId);
}
