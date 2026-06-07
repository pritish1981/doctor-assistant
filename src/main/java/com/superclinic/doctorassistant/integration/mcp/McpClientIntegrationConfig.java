package com.superclinic.doctorassistant.integration.mcp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Wires Spring AI MCP client tooling into the Doctor Assistant Agent.
 * <p>
 * Disables the global {@code spring.ai.mcp.client.toolcallback} auto bean and registers a
 * dedicated {@link ToolCallbackProvider} for the agent only, avoiding duplicate tool
 * registration on the embedded MCP server.
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(McpProperties.class)
@ConditionalOnProperty(prefix = "doctor-assistant.mcp.client", name = "enabled", havingValue = "true")
public class McpClientIntegrationConfig {

    public static final String AGENT_MCP_TOOL_CALLBACK_PROVIDER = "agentMcpToolCallbackProvider";

    @Bean(name = AGENT_MCP_TOOL_CALLBACK_PROVIDER)
    ToolCallbackProvider agentMcpToolCallbackProvider(
            ObjectProvider<List<io.modelcontextprotocol.client.McpSyncClient>> mcpSyncClients) {
        List<io.modelcontextprotocol.client.McpSyncClient> clients =
                mcpSyncClients.getIfAvailable(List::of);
        if (clients.isEmpty()) {
            throw new IllegalStateException(
                    "doctor-assistant.mcp.client.enabled=true but no McpSyncClient beans were created. "
                            + "Check spring.ai.mcp.client.sse.connections and spring.ai.mcp.client.enabled.");
        }
        log.info("Creating agent MCP ToolCallbackProvider from {} client connection(s)", clients.size());
        return new SyncMcpToolCallbackProvider(clients);
    }

    @Bean
    ApplicationRunner mcpToolDiscoveryRunner(McpToolDiscoveryService discoveryService) {
        return new ApplicationRunner() {
            @Override
            public void run(ApplicationArguments args) {
                discoveryService.logDiscoveredTools();
            }
        };
    }
}
