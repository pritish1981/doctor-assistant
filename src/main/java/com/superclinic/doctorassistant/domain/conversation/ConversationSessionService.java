package com.superclinic.doctorassistant.domain.conversation;

import com.superclinic.doctorassistant.common.exception.BusinessValidationException;
import com.superclinic.doctorassistant.common.exception.ResourceNotFoundException;
import com.superclinic.doctorassistant.domain.patient.PatientService;
import com.superclinic.doctorassistant.persistence.entity.ConversationHistoryEntry;
import com.superclinic.doctorassistant.persistence.entity.ConversationSession;
import com.superclinic.doctorassistant.persistence.entity.Patient;
import com.superclinic.doctorassistant.persistence.entity.enums.ConversationMessageRole;
import com.superclinic.doctorassistant.persistence.entity.enums.ConversationSessionStatus;
import com.superclinic.doctorassistant.persistence.repository.ConversationHistoryRepository;
import com.superclinic.doctorassistant.persistence.repository.ConversationSessionRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class ConversationSessionService {

    private final ConversationSessionRepository sessionRepository;
    private final ConversationHistoryRepository historyRepository;
    private final PatientService patientService;

    @Transactional
    public ConversationSession createSession(@Valid CreateSessionCommand command) {
        log.info("Creating conversation session for patientId={}", command.patientId());
        Patient patient = patientService.getActivePatient(command.patientId());
        ConversationSession session = ConversationSession.create(patient, command.title());
        return sessionRepository.save(session);
    }

    @Transactional(readOnly = true)
    public ConversationSession getActiveSession(UUID sessionId) {
        ConversationSession session = sessionRepository.findByIdWithPatient(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("ConversationSession", sessionId));

        if (session.getStatus() != ConversationSessionStatus.ACTIVE) {
            throw new BusinessValidationException(
                    "Conversation session is not active: %s (status=%s)"
                            .formatted(sessionId, session.getStatus()));
        }
        return session;
    }

    @Transactional(readOnly = true)
    public ConversationSession getSession(UUID sessionId) {
        return sessionRepository.findByIdWithPatient(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("ConversationSession", sessionId));
    }

    @Transactional
    public ConversationSession touchSession(UUID sessionId) {
        ConversationSession session = getActiveSession(sessionId);
        session.touch();
        return sessionRepository.save(session);
    }

    @Transactional
    public ConversationSession closeSession(UUID sessionId) {
        ConversationSession session = getSession(sessionId);
        session.close();
        log.info("Closed conversation session: {}", sessionId);
        return sessionRepository.save(session);
    }

    @Transactional(readOnly = true)
    public List<ConversationSession> listPatientSessions(UUID patientId) {
        patientService.getActivePatient(patientId);
        return sessionRepository.findByPatientIdOrderByLastActiveAtDesc(patientId);
    }

    @Transactional(readOnly = true)
    public List<ConversationHistoryEntry> getPersistedHistory(UUID sessionId) {
        getSession(sessionId);
        return historyRepository.findBySessionIdOrderBySequenceNumberAsc(sessionId);
    }

    @Transactional
    public void recordExchange(UUID sessionId, String userMessage, String assistantMessage) {
        ConversationSession session = getActiveSession(sessionId);
        int nextSequence = historyRepository.findMaxSequenceNumber(sessionId);

        historyRepository.save(ConversationHistoryEntry.create(
                session, ConversationMessageRole.USER, userMessage, nextSequence + 1));
        historyRepository.save(ConversationHistoryEntry.create(
                session, ConversationMessageRole.ASSISTANT, assistantMessage, nextSequence + 2));

        session.touch();
        sessionRepository.save(session);
        log.debug("Recorded conversation exchange for sessionId={}", sessionId);
    }
}
