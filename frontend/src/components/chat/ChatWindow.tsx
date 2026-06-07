import LocalHospitalIcon from '@mui/icons-material/LocalHospital';
import { Alert, Box, Paper, Typography } from '@mui/material';
import { ChatInput } from './ChatInput';
import { ChatMessageList } from './ChatMessageList';
import { useChat } from '../../hooks/useChat';
import { useSession } from '../../context/SessionContext';

export function ChatWindow() {
  const { activeSessionId, sessions } = useSession();
  const { messages, isTyping, loadingHistory, error, sendMessage, clearError } =
    useChat();

  const activeSession = sessions.find(
    (session) => session.sessionId === activeSessionId,
  );

  return (
    <Paper
      elevation={0}
      sx={{
        flex: 1,
        display: 'flex',
        flexDirection: 'column',
        minWidth: 0,
        border: 1,
        borderColor: 'divider',
        overflow: 'hidden',
      }}
    >
      <Box
        sx={{
          px: 3,
          py: 2,
          borderBottom: 1,
          borderColor: 'divider',
          display: 'flex',
          alignItems: 'center',
          gap: 1.5,
        }}
      >
        <LocalHospitalIcon color="primary" />
        <Box>
          <Typography variant="h6">
            {activeSession?.title ?? 'Doctor Assistant'}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {activeSession
              ? `${activeSession.messageCount} messages · ${activeSession.status}`
              : 'Select or start a conversation'}
          </Typography>
        </Box>
      </Box>

      {error && (
        <Alert severity="error" onClose={clearError} sx={{ mx: 2, mt: 2 }}>
          {error}
        </Alert>
      )}

      {!activeSessionId ? (
        <Box
          sx={{
            flex: 1,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            px: 3,
          }}
        >
          <Typography color="text.secondary" align="center">
            Choose a conversation from the sidebar or start a new session to
            chat with the Super Clinic assistant.
          </Typography>
        </Box>
      ) : (
        <>
          <ChatMessageList messages={messages} loading={loadingHistory} />
          <ChatInput
            onSend={(message) => void sendMessage(message)}
            disabled={isTyping || loadingHistory}
          />
        </>
      )}
    </Paper>
  );
}
