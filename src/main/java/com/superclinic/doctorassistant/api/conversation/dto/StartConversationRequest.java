package com.superclinic.doctorassistant.api.conversation.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record StartConversationRequest(
        @NotNull UUID patientId,
        String title
) {
}
