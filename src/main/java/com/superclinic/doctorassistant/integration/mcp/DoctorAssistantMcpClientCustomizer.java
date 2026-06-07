package com.superclinic.doctorassistant.integration.mcp;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.customizer.McpSyncClientCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Logs MCP tool discovery and change notifications from connected servers.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "doctor-assistant.mcp.client", name = "enabled", havingValue = "true")
public class DoctorAssistantMcpClientCustomizer implements McpSyncClientCustomizer {

    @Override
    public void customize(String serverConfigurationName, McpClient.SyncSpec spec) {
        spec.toolsChangeConsumer(tools -> {
            String toolNames = tools.stream()
                    .map(McpSchema.Tool::name)
                    .collect(Collectors.joining(", "));
            log.info("MCP tool catalog updated on connection '{}': [{}]", serverConfigurationName, toolNames);
        });
    }
}
