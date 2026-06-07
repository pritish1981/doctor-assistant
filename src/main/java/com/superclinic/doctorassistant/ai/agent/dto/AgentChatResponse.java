package com.superclinic.doctorassistant.ai.agent.dto;

import java.util.UUID;

public record AgentChatResponse(
        UUID sessionId,
        UUID patientId,
        String reply
) {
}
