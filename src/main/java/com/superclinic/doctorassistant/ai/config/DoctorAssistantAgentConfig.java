package com.superclinic.doctorassistant.ai.config;

import com.superclinic.doctorassistant.ai.prompt.DoctorAssistantAgentPrompt;
import com.superclinic.doctorassistant.ai.tools.AvailabilityTool;
import com.superclinic.doctorassistant.ai.tools.BookingTool;
import com.superclinic.doctorassistant.ai.tools.DoctorSearchTool;
import com.superclinic.doctorassistant.integration.mcp.McpClientIntegrationConfig;
import com.superclinic.doctorassistant.integration.mcp.McpProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class DoctorAssistantAgentConfig {

    public static final String DOCTOR_ASSISTANT_AGENT_CHAT_CLIENT = "doctorAssistantAgentChatClient";

    @Bean
    @Qualifier(DOCTOR_ASSISTANT_AGENT_CHAT_CLIENT)
    ChatClient doctorAssistantAgentChatClient(
            OpenAiChatModel chatModel,
            ChatMemory chatMemory,
            McpProperties mcpProperties,
            @Autowired(required = false)
            @Qualifier(McpClientIntegrationConfig.AGENT_MCP_TOOL_CALLBACK_PROVIDER)
            ToolCallbackProvider agentMcpToolCallbackProvider,
            DoctorSearchTool doctorSearchTool,
            AvailabilityTool availabilityTool,
            BookingTool bookingTool) {
        boolean mcpTools = mcpProperties.getClient().isEnabled();
        log.info("Configuring Doctor Assistant Agent ChatClient: toolSource={}",
                mcpTools ? "MCP" : "in-process");

        ChatClient.Builder builder = ChatClient.builder(chatModel)
                .defaultSystem(mcpTools
                        ? DoctorAssistantAgentPrompt.mcpSystemPrompt()
                        : DoctorAssistantAgentPrompt.systemPrompt())
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new SimpleLoggerAdvisor());

        if (mcpTools) {
            builder.defaultToolCallbacks(agentMcpToolCallbackProvider);
        } else {
            builder.defaultTools(doctorSearchTool, availabilityTool, bookingTool);
        }

        return builder.build();
    }
}
