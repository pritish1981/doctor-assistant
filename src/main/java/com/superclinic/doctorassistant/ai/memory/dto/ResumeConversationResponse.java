package com.superclinic.doctorassistant.ai.memory.dto;

import java.util.List;
import java.util.UUID;

public record ResumeConversationResponse(
        ConversationSessionResponse session,
        List<ConversationMessageDto> chatMemoryMessages,
        List<ConversationMessageDto> persistedMessages,
        boolean resumable
) {
}
