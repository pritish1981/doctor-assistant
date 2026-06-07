package com.superclinic.doctorassistant.domain.conversation;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateSessionCommand(
        @NotNull UUID patientId,
        String title
) {
}
