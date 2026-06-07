package com.superclinic.doctorassistant.integration.mcp;

import com.superclinic.doctorassistant.integration.mcp.dto.DiscoveredMcpTool;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Discovers tools exposed by connected MCP servers via {@code tools/list}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "doctor-assistant.mcp.client", name = "enabled", havingValue = "true")
public class McpToolDiscoveryService {

    private final List<McpSyncClient> mcpSyncClients;
    private final McpProperties mcpProperties;

    public List<DiscoveredMcpTool> discoverTools() {
        List<DiscoveredMcpTool> discovered = new ArrayList<>();
        for (int index = 0; index < mcpSyncClients.size(); index++) {
            McpSyncClient client = mcpSyncClients.get(index);
            String connectionName = resolveConnectionName(client, index);
            discovered.addAll(listTools(connectionName, client));
        }
        return discovered;
    }

    public void logDiscoveredTools() {
        List<DiscoveredMcpTool> tools = discoverTools();
        if (tools.isEmpty()) {
            log.warn("MCP client enabled but no tools discovered from {} connection(s)", mcpSyncClients.size());
            return;
        }
        String summary = tools.stream()
                .map(tool -> "%s (%s)".formatted(tool.name(), tool.connectionName()))
                .collect(Collectors.joining(", "));
        log.info("MCP tool discovery complete: {} tool(s) — {}", tools.size(), summary);
    }

    private List<DiscoveredMcpTool> listTools(String connectionName, McpSyncClient client) {
        McpSchema.ListToolsResult result = client.listTools();
        return result.tools().stream()
                .map(tool -> new DiscoveredMcpTool(connectionName, tool.name(), tool.description()))
                .toList();
    }

    private String resolveConnectionName(McpSyncClient client, int index) {
        if (mcpSyncClients.size() == 1) {
            return mcpProperties.getClient().getConnectionName();
        }
        McpSchema.Implementation serverInfo = client.getServerInfo();
        if (serverInfo != null && serverInfo.name() != null) {
            return serverInfo.name();
        }
        return mcpProperties.getClient().getConnectionName() + "-" + index;
    }
}
