package com.superclinic.doctorassistant.ai.config;

import com.superclinic.doctorassistant.ai.exception.OpenAiServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class OpenAiResponseErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().isError();
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        HttpStatusCode status = response.getStatusCode();
        String body = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
        log.error("OpenAI API error: status={}, body={}", status.value(), sanitize(body));
        throw new OpenAiServiceException(
                "OpenAI request failed with status %d".formatted(status.value()),
                status);
    }

    private String sanitize(String body) {
        if (body == null || body.length() <= 500) {
            return body;
        }
        return body.substring(0, 500) + "...[truncated]";
    }
}
