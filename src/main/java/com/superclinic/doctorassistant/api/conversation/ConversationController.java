package com.superclinic.doctorassistant.api.conversation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.superclinic.doctorassistant.ai.agent.DoctorAssistantAgent;
import com.superclinic.doctorassistant.ai.agent.dto.AgentChatRequest;
import com.superclinic.doctorassistant.ai.agent.dto.AgentChatResponse;
import com.superclinic.doctorassistant.ai.memory.dto.ConversationMessageDto;
import com.superclinic.doctorassistant.ai.memory.dto.ConversationSessionResponse;
import com.superclinic.doctorassistant.ai.memory.dto.ResumeConversationResponse;
import com.superclinic.doctorassistant.api.conversation.dto.SendMessageRequest;
import com.superclinic.doctorassistant.api.conversation.dto.StartConversationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
@Tag(name = "Conversations", description = "Doctor Assistant chat sessions and messaging")
public class ConversationController {

    private static final long SSE_TIMEOUT_MS = 120_000L;

    private final DoctorAssistantAgent doctorAssistantAgent;
    private final ObjectMapper objectMapper;

    @PostMapping
    @ResponseStatus(CREATED)
    @Operation(summary = "Start a new conversation session")
    public ConversationSessionResponse startConversation(@Valid @RequestBody StartConversationRequest request) {
        return doctorAssistantAgent.startConversation(request.patientId(), request.title());
    }

    @GetMapping
    @Operation(summary = "List conversation sessions for a patient")
    public List<ConversationSessionResponse> listConversations(@RequestParam UUID patientId) {
        return doctorAssistantAgent.listConversations(patientId);
    }

    @GetMapping("/{sessionId}/resume")
    @Operation(summary = "Resume a session with message history")
    public ResumeConversationResponse resumeConversation(@PathVariable UUID sessionId) {
        return doctorAssistantAgent.resumeConversation(sessionId);
    }

    @GetMapping("/{sessionId}/messages")
    @Operation(summary = "Get conversation message history")
    public List<ConversationMessageDto> getMessages(@PathVariable UUID sessionId) {
        return doctorAssistantAgent.getConversationHistory(sessionId);
    }

    @PostMapping("/{sessionId}/messages")
    @Operation(summary = "Send a message and receive a complete assistant reply")
    public AgentChatResponse sendMessage(
            @PathVariable UUID sessionId,
            @Valid @RequestBody SendMessageRequest request) {
        return doctorAssistantAgent.chat(new AgentChatRequest(sessionId, request.message(), request.patientId()));
    }

    @PostMapping(value = "/{sessionId}/messages/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Send a message and stream the assistant reply (SSE)")
    public SseEmitter streamMessage(
            @PathVariable UUID sessionId,
            @Valid @RequestBody SendMessageRequest request) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        AgentChatRequest chatRequest = new AgentChatRequest(sessionId, request.message(), request.patientId());

        doctorAssistantAgent.chatStream(chatRequest)
                .publishOn(Schedulers.boundedElastic())
                .subscribe(
                        chunk -> sendChunk(emitter, chunk),
                        emitter::completeWithError,
                        emitter::complete);

        return emitter;
    }

    @PostMapping("/{sessionId}/close")
    @ResponseStatus(NO_CONTENT)
    @Operation(summary = "Close a conversation session")
    public void closeConversation(@PathVariable UUID sessionId) {
        doctorAssistantAgent.closeConversation(sessionId);
    }

    private void sendChunk(SseEmitter emitter, String chunk) {
        try {
            emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(chunk)));
        } catch (IOException ex) {
            emitter.completeWithError(ex);
        }
    }
}
