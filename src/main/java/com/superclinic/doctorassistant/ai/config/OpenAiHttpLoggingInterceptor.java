package com.superclinic.doctorassistant.ai.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;

@Slf4j
@Component
public class OpenAiHttpLoggingInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution) throws IOException {
        URI uri = request.getURI();
        long start = System.currentTimeMillis();
        log.debug("OpenAI request: {} {}", request.getMethod(), sanitizeUri(uri));

        try {
            ClientHttpResponse response = execution.execute(request, body);
            long elapsed = System.currentTimeMillis() - start;
            log.info("OpenAI response: {} {} -> {} ({} ms)",
                    request.getMethod(), sanitizeUri(uri), response.getStatusCode().value(), elapsed);
            return response;
        } catch (IOException ex) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("OpenAI request failed: {} {} ({} ms): {}",
                    request.getMethod(), sanitizeUri(uri), elapsed, ex.getMessage());
            throw ex;
        }
    }

    private String sanitizeUri(URI uri) {
        return uri.getPath();
    }
}
