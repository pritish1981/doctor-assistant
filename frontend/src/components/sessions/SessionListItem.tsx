import ChatIcon from '@mui/icons-material/Chat';
import CloseIcon from '@mui/icons-material/Close';
import {
  Box,
  Chip,
  IconButton,
  ListItemButton,
  ListItemText,
} from '@mui/material';
import type { ConversationSession } from '../../types/conversation';
import { formatSessionTime } from '../../utils/messages';

interface SessionListItemProps {
  session: ConversationSession;
  selected: boolean;
  onSelect: () => void;
  onClose: () => void;
}

export function SessionListItem({
  session,
  selected,
  onSelect,
  onClose,
}: SessionListItemProps) {
  return (
    <ListItemButton
      selected={selected}
      onClick={onSelect}
      sx={{
        borderRadius: 2,
        mb: 0.5,
        alignItems: 'flex-start',
      }}
    >
      <ChatIcon
        fontSize="small"
        sx={{ mt: 0.5, mr: 1.5, opacity: 0.7 }}
      />
      <ListItemText
        primary={session.title}
        secondary={formatSessionTime(session.lastActiveAt)}
        slotProps={{
          primary: { noWrap: true, sx: { fontWeight: selected ? 600 : 500 } },
          secondary: { noWrap: true },
        }}
      />
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5, ml: 1 }}>
        <Chip
          label={session.status}
          size="small"
          color={session.status === 'ACTIVE' ? 'success' : 'default'}
          variant="outlined"
        />
        {session.status === 'ACTIVE' && (
          <IconButton
            size="small"
            aria-label="Close session"
            onClick={(event) => {
              event.stopPropagation();
              onClose();
            }}
          >
            <CloseIcon fontSize="small" />
          </IconButton>
        )}
      </Box>
    </ListItemButton>
  );
}
