import { useCallback, useEffect, useRef, useState } from 'react';
import { getMessages, streamMessage } from '../api/conversationApi';
import { useSession } from '../context/SessionContext';
import type { ChatMessage } from '../types/conversation';
import {
  createAssistantPlaceholder,
  createUserMessage,
  mapApiMessages,
} from '../utils/messages';

export function useChat() {
  const { activeSessionId, patientId } = useSession();
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [isTyping, setIsTyping] = useState(false);
  const [loadingHistory, setLoadingHistory] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const abortRef = useRef<AbortController | null>(null);

  const loadHistory = useCallback(async (sessionId: string) => {
    setLoadingHistory(true);
    setError(null);
    try {
      const history = await getMessages(sessionId);
      setMessages(mapApiMessages(history));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load history');
      setMessages([]);
    } finally {
      setLoadingHistory(false);
    }
  }, []);

  useEffect(() => {
    if (activeSessionId) {
      void loadHistory(activeSessionId);
    } else {
      setMessages([]);
    }
    return () => abortRef.current?.abort();
  }, [activeSessionId, loadHistory]);

  const sendMessage = useCallback(
    async (text: string) => {
      if (!activeSessionId || !text.trim()) return;

      abortRef.current?.abort();
      const controller = new AbortController();
      abortRef.current = controller;

      const userMessage = createUserMessage(text.trim());
      const assistantMessage = createAssistantPlaceholder();

      setMessages((prev) => [...prev, userMessage, assistantMessage]);
      setIsTyping(true);
      setError(null);

      try {
        await streamMessage(
          activeSessionId,
          { message: text.trim(), patientId },
          (chunk) => {
            setMessages((prev) =>
              prev.map((message) =>
                message.id === assistantMessage.id
                  ? { ...message, content: message.content + chunk }
                  : message,
              ),
            );
          },
          controller.signal,
        );

        setMessages((prev) =>
          prev.map((message) =>
            message.id === assistantMessage.id
              ? { ...message, streaming: false }
              : message,
          ),
        );
      } catch (err) {
        if (controller.signal.aborted) return;
        setError(err instanceof Error ? err.message : 'Failed to send message');
        setMessages((prev) =>
          prev.filter((message) => message.id !== assistantMessage.id),
        );
      } finally {
        setIsTyping(false);
      }
    },
    [activeSessionId, patientId],
  );

  return {
    messages,
    isTyping,
    loadingHistory,
    error,
    sendMessage,
    clearError: () => setError(null),
  };
}
