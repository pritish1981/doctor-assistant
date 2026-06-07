package com.superclinic.doctorassistant.api.conversation.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record SendMessageRequest(
        @NotBlank String message,
        UUID patientId
) {
}
