package com.superclinic.doctorassistant.ai.memory.dto;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;

import java.time.Instant;
import java.util.List;

public record ConversationMessageDto(
        String role,
        String content,
        Instant timestamp
) {

    public static ConversationMessageDto from(Message message) {
        return new ConversationMessageDto(
                message.getMessageType().name(),
                message.getText(),
                null);
    }

    public static ConversationMessageDto from(Message message, Instant timestamp) {
        return new ConversationMessageDto(
                message.getMessageType().name(),
                message.getText(),
                timestamp);
    }

    public static List<ConversationMessageDto> fromMessages(List<Message> messages) {
        return messages.stream()
                .filter(message -> message.getMessageType() != MessageType.SYSTEM)
                .map(ConversationMessageDto::from)
                .toList();
    }
}
