export type ConversationSessionStatus = 'ACTIVE' | 'CLOSED' | 'ARCHIVED';

export type MessageRole = 'USER' | 'ASSISTANT' | 'SYSTEM' | 'TOOL';

export interface ConversationSession {
  sessionId: string;
  patientId: string;
  patientName: string;
  status: ConversationSessionStatus;
  title: string;
  startedAt: string;
  lastActiveAt: string;
  messageCount: number;
}

export interface ConversationMessage {
  role: MessageRole;
  content: string;
  timestamp: string | null;
}

export interface ResumeConversationResponse {
  session: ConversationSession;
  chatMemoryMessages: ConversationMessage[];
  persistedMessages: ConversationMessage[];
  resumable: boolean;
}

export interface ChatMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: Date;
  streaming?: boolean;
}

export interface StartConversationRequest {
  patientId: string;
  title?: string;
}

export interface SendMessageRequest {
  message: string;
  patientId?: string;
}

export interface AgentChatResponse {
  sessionId: string;
  patientId: string | null;
  reply: string;
}
