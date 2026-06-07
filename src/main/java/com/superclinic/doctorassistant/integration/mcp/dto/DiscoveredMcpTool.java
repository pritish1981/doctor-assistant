package com.superclinic.doctorassistant.integration.mcp.dto;

public record DiscoveredMcpTool(
        String connectionName,
        String name,
        String description) {
}
