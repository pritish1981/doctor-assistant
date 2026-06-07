package com.superclinic.doctorassistant.ai.agent;

import com.superclinic.doctorassistant.ai.agent.dto.AgentChatRequest;
import com.superclinic.doctorassistant.ai.agent.dto.AgentChatResponse;
import com.superclinic.doctorassistant.ai.config.DoctorAssistantAgentConfig;
import com.superclinic.doctorassistant.ai.memory.ConversationMemoryService;
import com.superclinic.doctorassistant.ai.memory.dto.ConversationMessageDto;
import com.superclinic.doctorassistant.ai.memory.dto.ConversationSessionResponse;
import com.superclinic.doctorassistant.ai.memory.dto.ResumeConversationResponse;
import com.superclinic.doctorassistant.ai.rag.RagService;
import com.superclinic.doctorassistant.ai.rag.dto.RagContext;
import com.superclinic.doctorassistant.common.exception.BusinessValidationException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@Validated
public class DoctorAssistantAgent {

    private final ChatClient chatClient;
    private final ConversationMemoryService conversationMemoryService;
    private final RagService ragService;

    public DoctorAssistantAgent(
            @Qualifier(DoctorAssistantAgentConfig.DOCTOR_ASSISTANT_AGENT_CHAT_CLIENT)
            ChatClient chatClient,
            ConversationMemoryService conversationMemoryService,
            RagService ragService) {
        this.chatClient = chatClient;
        this.conversationMemoryService = conversationMemoryService;
        this.ragService = ragService;
    }

    public ConversationSessionResponse startConversation(UUID patientId, String title) {
        return conversationMemoryService.createSession(patientId, title);
    }

    public ResumeConversationResponse resumeConversation(UUID sessionId) {
        return conversationMemoryService.resumeSession(sessionId);
    }

    public List<ConversationSessionResponse> listConversations(UUID patientId) {
        return conversationMemoryService.listPatientSessions(patientId);
    }

    public List<ConversationMessageDto> getConversationHistory(UUID sessionId) {
        return conversationMemoryService.getChatMemory(sessionId);
    }

    public AgentChatResponse chat(@Valid AgentChatRequest request) {
        log.info("Agent chat: sessionId={}, patientId={}", request.sessionId(), request.patientId());
        validateMessage(request.message());
        conversationMemoryService.validateActiveSession(request.sessionId());

        RagContext ragContext = ragService.retrieveContext(request.message());
        String userMessage = ragService.augmentUserMessage(buildUserMessage(request), ragContext);

        String reply = chatClient.prompt()
                .user(userMessage)
                .toolContext(buildToolContext(request))
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID,
                        conversationMemoryService.toConversationId(request.sessionId())))
                .call()
                .content();

        conversationMemoryService.recordExchange(request.sessionId(), request.message(), reply);
        log.info("Agent chat completed: sessionId={}", request.sessionId());
        return new AgentChatResponse(request.sessionId(), request.patientId(), reply);
    }

    public Flux<String> chatStream(@Valid AgentChatRequest request) {
        log.info("Agent chat stream: sessionId={}, patientId={}", request.sessionId(), request.patientId());
        validateMessage(request.message());
        conversationMemoryService.validateActiveSession(request.sessionId());

        RagContext ragContext = ragService.retrieveContext(request.message());
        String userMessage = ragService.augmentUserMessage(buildUserMessage(request), ragContext);

        StringBuilder assistantReply = new StringBuilder();

        return chatClient.prompt()
                .user(userMessage)
                .toolContext(buildToolContext(request))
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID,
                        conversationMemoryService.toConversationId(request.sessionId())))
                .stream()
                .content()
                .doOnNext(assistantReply::append)
                .doOnComplete(() -> conversationMemoryService.recordExchange(
                        request.sessionId(), request.message(), assistantReply.toString()));
    }

    public void closeConversation(UUID sessionId) {
        conversationMemoryService.closeSession(sessionId);
    }

    public void clearConversationMemory(UUID sessionId) {
        conversationMemoryService.clearChatMemory(sessionId);
    }

    private void validateMessage(String message) {
        if (!StringUtils.hasText(message)) {
            throw new BusinessValidationException("Message must not be blank");
        }
    }

    private String buildUserMessage(AgentChatRequest request) {
        if (request.patientId() == null) {
            return request.message();
        }
        return "[Known patient ID: %s]%n%s".formatted(request.patientId(), request.message());
    }

    private Map<String, Object> buildToolContext(AgentChatRequest request) {
        Map<String, Object> context = new HashMap<>();
        if (request.patientId() != null) {
            context.put("patientId", request.patientId().toString());
        }
        context.put("sessionId", request.sessionId().toString());
        return context;
    }
}
