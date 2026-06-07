package com.superclinic.doctorassistant.ai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.superclinic.doctorassistant.ai.client.dto.OpenAiResponsesRequest;
import com.superclinic.doctorassistant.ai.client.dto.OpenAiResponsesResponse;
import com.superclinic.doctorassistant.ai.config.OpenAiProperties;
import com.superclinic.doctorassistant.ai.exception.OpenAiServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
public class OpenAiResponsesClient {

    private final RestClient restClient;
    private final OpenAiProperties properties;
    private final ObjectMapper objectMapper;

    public OpenAiResponsesClient(
            RestClient.Builder restClientBuilder,
            OpenAiProperties properties,
            ObjectMapper objectMapper,
            @Value("${spring.ai.openai.base-url:https://api.openai.com}") String baseUrl) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    public OpenAiResponsesResponse createResponse(OpenAiResponsesRequest request) {
        log.debug("Creating OpenAI response for model={}", request.model());
        try {
            return restClient.post()
                    .uri(properties.getResponsesPath())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(OpenAiResponsesResponse.class);
        } catch (RestClientResponseException ex) {
            log.error("OpenAI Responses API error: status={}, body={}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            throw new OpenAiServiceException(
                    "OpenAI Responses API failed with status %d".formatted(ex.getStatusCode().value()),
                    ex.getStatusCode(),
                    ex);
        } catch (RestClientException ex) {
            log.error("OpenAI Responses API transport error: {}", ex.getMessage());
            throw new OpenAiServiceException("OpenAI Responses API transport error: " + ex.getMessage(), null, ex);
        }
    }

    public OpenAiResponsesRequest buildTextRequest(String model, String userMessage, String instructions, Double temperature) {
        JsonNode input = objectMapper.valueToTree(userMessage);
        return new OpenAiResponsesRequest(model, input, instructions, temperature, null, false, null);
    }
}
