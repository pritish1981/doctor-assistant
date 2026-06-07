package com.superclinic.doctorassistant.ai.memory;

import com.superclinic.doctorassistant.ai.memory.dto.ConversationMessageDto;
import com.superclinic.doctorassistant.ai.memory.dto.ConversationSessionResponse;
import com.superclinic.doctorassistant.ai.memory.dto.ResumeConversationResponse;
import com.superclinic.doctorassistant.common.exception.BusinessValidationException;
import com.superclinic.doctorassistant.domain.conversation.ConversationSessionService;
import com.superclinic.doctorassistant.domain.conversation.CreateSessionCommand;
import com.superclinic.doctorassistant.persistence.entity.ConversationHistoryEntry;
import com.superclinic.doctorassistant.persistence.entity.ConversationSession;
import com.superclinic.doctorassistant.persistence.entity.enums.ConversationSessionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationMemoryService {

    private final ChatMemory chatMemory;
    private final ConversationSessionService sessionService;

    public ConversationSessionResponse createSession(UUID patientId, String title) {
        ConversationSession session = sessionService.createSession(new CreateSessionCommand(patientId, title));
        log.info("Created conversation session: sessionId={}, patientId={}", session.getId(), patientId);
        return ConversationSessionResponse.from(session, 0);
    }

    public ResumeConversationResponse resumeSession(UUID sessionId) {
        log.info("Resuming conversation session: {}", sessionId);
        ConversationSession session = sessionService.getSession(sessionId);

        List<Message> chatMemoryMessages = chatMemory.get(toConversationId(sessionId));
        List<ConversationMessageDto> memoryDtos = ConversationMessageDto.fromMessages(chatMemoryMessages);

        List<ConversationMessageDto> persistedDtos = sessionService.getPersistedHistory(sessionId).stream()
                .map(this::toDto)
                .toList();

        int messageCount = Math.max(memoryDtos.size(), persistedDtos.size());
        boolean resumable = session.getStatus() == ConversationSessionStatus.ACTIVE;

        return new ResumeConversationResponse(
                ConversationSessionResponse.from(session, messageCount),
                memoryDtos,
                persistedDtos,
                resumable);
    }

    public List<ConversationMessageDto> getChatMemory(UUID sessionId) {
        sessionService.getSession(sessionId);
        return ConversationMessageDto.fromMessages(chatMemory.get(toConversationId(sessionId)));
    }

    public List<ConversationSessionResponse> listPatientSessions(UUID patientId) {
        return sessionService.listPatientSessions(patientId).stream()
                .map(session -> {
                    int count = chatMemory.get(toConversationId(session.getId())).size();
                    return ConversationSessionResponse.from(session, count);
                })
                .toList();
    }

    public void validateActiveSession(UUID sessionId) {
        sessionService.getActiveSession(sessionId);
    }

    public void recordExchange(UUID sessionId, String userMessage, String assistantMessage) {
        if (!StringUtils.hasText(userMessage) || !StringUtils.hasText(assistantMessage)) {
            throw new BusinessValidationException("User and assistant messages must not be blank");
        }
        sessionService.recordExchange(sessionId, userMessage.trim(), assistantMessage.trim());
    }

    public void touchSession(UUID sessionId) {
        sessionService.touchSession(sessionId);
    }

    public void clearChatMemory(UUID sessionId) {
        log.info("Clearing Spring AI chat memory for sessionId={}", sessionId);
        chatMemory.clear(toConversationId(sessionId));
    }

    public void closeSession(UUID sessionId) {
        clearChatMemory(sessionId);
        sessionService.closeSession(sessionId);
        log.info("Closed and cleared conversation session: {}", sessionId);
    }

    public String toConversationId(UUID sessionId) {
        return sessionId.toString();
    }

    private ConversationMessageDto toDto(ConversationHistoryEntry entry) {
        return new ConversationMessageDto(
                entry.getRole().name(),
                entry.getContent(),
                entry.getCreatedAt());
    }
}
