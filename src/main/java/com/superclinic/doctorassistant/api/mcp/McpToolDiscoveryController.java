package com.superclinic.doctorassistant.api.mcp;

import com.superclinic.doctorassistant.integration.mcp.McpToolDiscoveryService;
import com.superclinic.doctorassistant.integration.mcp.dto.DiscoveredMcpTool;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mcp/tools")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "doctor-assistant.mcp.client", name = "enabled", havingValue = "true")
@Tag(name = "MCP Client", description = "MCP tool discovery for the Doctor Assistant Agent")
public class McpToolDiscoveryController {

    private final McpToolDiscoveryService mcpToolDiscoveryService;

    @GetMapping
    @Operation(summary = "List tools discovered from connected MCP servers")
    public List<DiscoveredMcpTool> listDiscoveredTools() {
        return mcpToolDiscoveryService.discoverTools();
    }
}
