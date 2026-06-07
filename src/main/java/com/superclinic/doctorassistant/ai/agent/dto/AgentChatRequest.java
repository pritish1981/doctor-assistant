package com.superclinic.doctorassistant.ai.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AgentChatRequest(
        @NotNull UUID sessionId,
        @NotBlank String message,
        UUID patientId
) {
}
