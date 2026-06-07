import AddIcon from '@mui/icons-material/Add';
import HistoryIcon from '@mui/icons-material/History';
import RefreshIcon from '@mui/icons-material/Refresh';
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Divider,
  List,
  Paper,
  Typography,
} from '@mui/material';
import { useState } from 'react';
import { useSession } from '../../context/SessionContext';
import { SessionListItem } from './SessionListItem';

export function SessionSidebar() {
  const {
    sessions,
    activeSessionId,
    loadingSessions,
    error,
    selectSession,
    createSession,
    refreshSessions,
    closeSession,
    clearError,
  } = useSession();
  const [creating, setCreating] = useState(false);

  const handleCreate = async () => {
    setCreating(true);
    clearError();
    try {
      await createSession();
    } catch {
      /* error surfaced via SessionContext.error */
    } finally {
      setCreating(false);
    }
  };

  return (
    <Paper
      elevation={0}
      sx={{
        width: { xs: '100%', md: 320 },
        flexShrink: 0,
        display: 'flex',
        flexDirection: 'column',
        border: 1,
        borderColor: 'divider',
        overflow: 'hidden',
      }}
    >
      <Box sx={{ p: 2 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
          <HistoryIcon color="primary" />
          <Typography variant="h6">Conversations</Typography>
        </Box>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          Session history is persisted on the server.
        </Typography>
        <Box sx={{ display: 'flex', gap: 1 }}>
          <Button
            fullWidth
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => void handleCreate()}
            disabled={creating}
          >
            New chat
          </Button>
          <Button
            variant="outlined"
            aria-label="Refresh sessions"
            onClick={() => void refreshSessions()}
            disabled={loadingSessions}
          >
            <RefreshIcon />
          </Button>
        </Box>
      </Box>

      <Divider />

      {error && (
        <Alert severity="error" onClose={clearError} sx={{ m: 2 }}>
          {error}
        </Alert>
      )}

      <Box sx={{ flex: 1, overflowY: 'auto', p: 1 }}>
        {loadingSessions ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
            <CircularProgress size={28} />
          </Box>
        ) : sessions.length === 0 ? (
          <Typography color="text.secondary" sx={{ p: 2 }}>
            No conversations yet. Start a new chat to begin.
          </Typography>
        ) : (
          <List disablePadding>
            {sessions.map((session) => (
              <SessionListItem
                key={session.sessionId}
                session={session}
                selected={session.sessionId === activeSessionId}
                onSelect={() => void selectSession(session.sessionId)}
                onClose={() => void closeSession(session.sessionId)}
              />
            ))}
          </List>
        )}
      </Box>
    </Paper>
  );
}
