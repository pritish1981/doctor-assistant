package com.superclinic.doctorassistant.integration.mcp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the MCP tool surface as the only {@link ToolCallbackProvider} bean
 * when the embedded MCP server is enabled, so agent-only tools are not exposed externally.
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(McpProperties.class)
@ConditionalOnProperty(prefix = "doctor-assistant.mcp", name = "enabled", havingValue = "true")
public class McpToolRegistrationConfig {

    public static final String MCP_TOOL_CALLBACK_PROVIDER = "mcpToolCallbackProvider";

    @Bean(name = MCP_TOOL_CALLBACK_PROVIDER)
    ToolCallbackProvider mcpToolCallbackProvider(DoctorAssistantMcpTools doctorAssistantMcpTools) {
        log.info("Registering MCP tools: findDoctor, getAvailability, bookAppointment");
        return MethodToolCallbackProvider.builder()
                .toolObjects(doctorAssistantMcpTools)
                .build();
    }
}
