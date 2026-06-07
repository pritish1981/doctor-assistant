package com.superclinic.doctorassistant.ai.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenAiResponsesResponse(
        String id,
        String object,
        String status,
        JsonNode output,
        String outputText,
        String previousResponseId,
        JsonNode usage,
        JsonNode error
) {
}
