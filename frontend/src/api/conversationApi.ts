import { API_BASE, apiRequest } from './client';
import { parseSseEventData } from '../utils/sse';
import type {
  AgentChatResponse,
  ConversationMessage,
  ConversationSession,
  ResumeConversationResponse,
  SendMessageRequest,
  StartConversationRequest,
} from '../types/conversation';

export function startConversation(
  request: StartConversationRequest,
): Promise<ConversationSession> {
  return apiRequest('/conversations', {
    method: 'POST',
    body: JSON.stringify(request),
  });
}

export function listConversations(
  patientId: string,
): Promise<ConversationSession[]> {
  return apiRequest(`/conversations?patientId=${patientId}`);
}

export function resumeConversation(
  sessionId: string,
): Promise<ResumeConversationResponse> {
  return apiRequest(`/conversations/${sessionId}/resume`);
}

export function getMessages(sessionId: string): Promise<ConversationMessage[]> {
  return apiRequest(`/conversations/${sessionId}/messages`);
}

export function sendMessage(
  sessionId: string,
  request: SendMessageRequest,
): Promise<AgentChatResponse> {
  return apiRequest(`/conversations/${sessionId}/messages`, {
    method: 'POST',
    body: JSON.stringify(request),
  });
}

export function closeConversation(sessionId: string): Promise<void> {
  return apiRequest(`/conversations/${sessionId}/close`, { method: 'POST' });
}

export async function streamMessage(
  sessionId: string,
  request: SendMessageRequest,
  onChunk: (chunk: string) => void,
  signal?: AbortSignal,
): Promise<void> {
  const response = await fetch(
    `${API_BASE}/conversations/${sessionId}/messages/stream`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'text/event-stream',
      },
      body: JSON.stringify(request),
      signal,
    },
  );

  if (!response.ok) {
    let message = `Stream failed (${response.status})`;
    try {
      const body = await response.json();
      if (body?.detail) message = body.detail;
    } catch {
      /* ignore */
    }
    throw new Error(message);
  }

  const reader = response.body?.getReader();
  if (!reader) {
    throw new Error('Streaming is not supported by this browser');
  }

  const decoder = new TextDecoder();
  let buffer = '';

  while (true) {
    const { done, value } = await reader.read();
    if (done) break;

    buffer += decoder.decode(value, { stream: true });
    const events = buffer.split('\n\n');
    buffer = events.pop() ?? '';

    for (const event of events) {
      const chunk = parseSseEventData(event);
      if (chunk !== null) {
        onChunk(chunk);
      }
    }
  }

  if (buffer.trim()) {
    const chunk = parseSseEventData(buffer);
    if (chunk !== null) {
      onChunk(chunk);
    }
  }
}
