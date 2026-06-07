import type { ConversationMessage, ChatMessage } from '../types/conversation';

export function mapApiMessages(messages: ConversationMessage[]): ChatMessage[] {
  return messages
    .filter((message) => message.role === 'USER' || message.role === 'ASSISTANT')
    .map((message, index) => ({
      id: `${message.role}-${index}-${message.timestamp ?? index}`,
      role: message.role === 'USER' ? 'user' : 'assistant',
      content: message.content,
      timestamp: message.timestamp ? new Date(message.timestamp) : new Date(),
    }));
}

export function createUserMessage(content: string): ChatMessage {
  return {
    id: `user-${crypto.randomUUID()}`,
    role: 'user',
    content,
    timestamp: new Date(),
  };
}

export function createAssistantPlaceholder(): ChatMessage {
  return {
    id: `assistant-${crypto.randomUUID()}`,
    role: 'assistant',
    content: '',
    timestamp: new Date(),
    streaming: true,
  };
}

export function formatSessionTime(iso: string): string {
  return new Intl.DateTimeFormat(undefined, {
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  }).format(new Date(iso));
}

export function formatMessageTime(date: Date): string {
  return new Intl.DateTimeFormat(undefined, {
    hour: 'numeric',
    minute: '2-digit',
  }).format(date);
}
