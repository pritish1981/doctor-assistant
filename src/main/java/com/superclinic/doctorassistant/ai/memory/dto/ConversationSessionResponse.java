package com.superclinic.doctorassistant.ai.memory.dto;

import com.superclinic.doctorassistant.persistence.entity.ConversationSession;
import com.superclinic.doctorassistant.persistence.entity.enums.ConversationSessionStatus;

import java.time.Instant;
import java.util.UUID;

public record ConversationSessionResponse(
        UUID sessionId,
        UUID patientId,
        String patientName,
        ConversationSessionStatus status,
        String title,
        Instant startedAt,
        Instant lastActiveAt,
        int messageCount
) {

    public static ConversationSessionResponse from(ConversationSession session, int messageCount) {
        return new ConversationSessionResponse(
                session.getId(),
                session.getPatient().getId(),
                session.getPatient().getFullName(),
                session.getStatus(),
                session.getTitle(),
                session.getStartedAt(),
                session.getLastActiveAt(),
                messageCount);
    }
}
