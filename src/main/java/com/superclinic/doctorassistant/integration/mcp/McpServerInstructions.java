package com.superclinic.doctorassistant.integration.mcp;

/**
 * Server instructions sent to MCP clients during capability negotiation.
 */
public final class McpServerInstructions {

    public static final String INSTRUCTIONS = """
            Doctor Assistant MCP Server for Super Clinic.

            Exposed tools:
            - findDoctor: search active doctors by name and/or specialty
            - getAvailability: list open appointment slots for a doctor on a date
            - bookAppointment: confirm a booking using patient, doctor, and slot IDs

            All doctor and slot identifiers are UUIDs returned by prior tool calls.
            Dates use ISO-8601 format (yyyy-MM-dd).
            Do not invent UUIDs — always use values from tool responses.
            """;

    private McpServerInstructions() {
    }
}
