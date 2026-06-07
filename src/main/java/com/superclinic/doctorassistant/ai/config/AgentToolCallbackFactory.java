package com.superclinic.doctorassistant.ai.config;

import com.superclinic.doctorassistant.ai.tools.AvailabilityTool;
import com.superclinic.doctorassistant.ai.tools.BookingTool;
import com.superclinic.doctorassistant.ai.tools.DoctorSearchTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;

/**
 * Factory for in-process agent tools. Not registered as a Spring bean so the MCP server
 * auto-configuration does not expose internal agent tools to external MCP clients.
 */
public final class AgentToolCallbackFactory {

    private AgentToolCallbackFactory() {
    }

    public static ToolCallbackProvider create(
            DoctorSearchTool doctorSearchTool,
            AvailabilityTool availabilityTool,
            BookingTool bookingTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(doctorSearchTool, availabilityTool, bookingTool)
                .build();
    }
}
