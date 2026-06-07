package com.superclinic.doctorassistant.ai.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "doctor-assistant.openai")
public class OpenAiProperties {

    @NotNull
    private Duration connectTimeout = Duration.ofSeconds(10);

    @NotNull
    private Duration readTimeout = Duration.ofSeconds(120);

    @NotNull
    private Duration writeTimeout = Duration.ofSeconds(30);

    @NotBlank
    private String responsesPath = "/v1/responses";

    @NotBlank
    private String defaultSystemPrompt = """
            You are the Super Clinic Doctor Assistant. Help patients search doctors, \
            check availability, book appointments, and answer clinic FAQs. \
            You provide informational guidance only — not medical diagnoses. \
            Recommend emergency care when symptoms are severe.""";
}
