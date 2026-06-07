import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import {
  closeConversation,
  listConversations,
  resumeConversation,
  startConversation,
} from '../api/conversationApi';
import type { ConversationSession } from '../types/conversation';

interface SessionContextValue {
  patientId: string;
  sessions: ConversationSession[];
  activeSessionId: string | null;
  loadingSessions: boolean;
  error: string | null;
  selectSession: (sessionId: string) => Promise<void>;
  createSession: (title?: string) => Promise<string>;
  refreshSessions: () => Promise<void>;
  closeSession: (sessionId: string) => Promise<void>;
  clearError: () => void;
}

const SessionContext = createContext<SessionContextValue | null>(null);

const DEFAULT_PATIENT_ID =
  import.meta.env.VITE_DEFAULT_PATIENT_ID ??
  'b2000000-0000-4000-8000-000000000001';

interface SessionProviderProps {
  children: ReactNode;
}

export function SessionProvider({ children }: SessionProviderProps) {
  const [patientId] = useState(DEFAULT_PATIENT_ID);
  const [sessions, setSessions] = useState<ConversationSession[]>([]);
  const [activeSessionId, setActiveSessionId] = useState<string | null>(null);
  const [loadingSessions, setLoadingSessions] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const refreshSessions = useCallback(async () => {
    setLoadingSessions(true);
    try {
      const data = await listConversations(patientId);
      setSessions(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load sessions');
    } finally {
      setLoadingSessions(false);
    }
  }, [patientId]);

  useEffect(() => {
    void refreshSessions();
  }, [refreshSessions]);

  const selectSession = useCallback(async (sessionId: string) => {
    setError(null);
    try {
      await resumeConversation(sessionId);
      setActiveSessionId(sessionId);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to open session');
    }
  }, []);

  const createSession = useCallback(
    async (title?: string) => {
      setError(null);
      try {
        const session = await startConversation({
          patientId,
          title: title ?? 'New consultation',
        });
        setActiveSessionId(session.sessionId);
        await refreshSessions();
        return session.sessionId;
      } catch (err) {
        setError(
          err instanceof Error ? err.message : 'Failed to start conversation',
        );
        throw err;
      }
    },
    [patientId, refreshSessions],
  );

  const closeSession = useCallback(
    async (sessionId: string) => {
      setError(null);
      await closeConversation(sessionId);
      if (activeSessionId === sessionId) {
        setActiveSessionId(null);
      }
      await refreshSessions();
    },
    [activeSessionId, refreshSessions],
  );

  const value = useMemo<SessionContextValue>(
    () => ({
      patientId,
      sessions,
      activeSessionId,
      loadingSessions,
      error,
      selectSession,
      createSession,
      refreshSessions,
      closeSession,
      clearError: () => setError(null),
    }),
    [
      patientId,
      sessions,
      activeSessionId,
      loadingSessions,
      error,
      selectSession,
      createSession,
      refreshSessions,
      closeSession,
    ],
  );

  return (
    <SessionContext.Provider value={value}>{children}</SessionContext.Provider>
  );
}

export function useSession() {
  const context = useContext(SessionContext);
  if (!context) {
    throw new Error('useSession must be used within SessionProvider');
  }
  return context;
}
