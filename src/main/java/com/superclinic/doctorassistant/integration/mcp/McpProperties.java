package com.superclinic.doctorassistant.integration.mcp;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "doctor-assistant.mcp")
public class McpProperties {

    /**
     * Enables the embedded MCP server ({@code spring.ai.mcp.server}).
     */
    private boolean enabled = false;

    /**
     * MCP client settings for the Doctor Assistant Agent.
     */
    private Client client = new Client();

    /**
     * Public base URL of the MCP server the client connects to.
     * Example: http://localhost:8080/api/v1
     */
    private String serverUrl = "http://localhost:8080/api/v1";

    @NotBlank
    private String serverName = "doctor-assistant-mcp";

    @NotBlank
    private String serverVersion = "0.1.0";

    @Getter
    @Setter
    public static class Client {

        /**
         * When true, the agent discovers and invokes tools via MCP instead of in-process {@code @Tool} beans.
         */
        private boolean enabled = false;

        /**
         * Named SSE connection (maps to {@code spring.ai.mcp.client.sse.connections.*}).
         */
        @NotBlank
        private String connectionName = "doctor-assistant";

        /**
         * SSE endpoint path suffix appended to {@link #serverUrl}.
         */
        @NotBlank
        private String sseEndpoint = "/mcp/sse";

        private Duration requestTimeout = Duration.ofSeconds(30);
    }
}
