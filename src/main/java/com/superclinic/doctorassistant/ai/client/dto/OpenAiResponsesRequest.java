package com.superclinic.doctorassistant.ai.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OpenAiResponsesRequest(
        String model,
        JsonNode input,
        String instructions,
        Double temperature,
        List<Map<String, Object>> tools,
        Boolean store,
        String previousResponseId
) {
}
