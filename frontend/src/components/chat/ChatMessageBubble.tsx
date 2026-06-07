import { Box, Paper, Typography } from '@mui/material';
import type { ChatMessage } from '../../types/conversation';
import { formatMessageTime } from '../../utils/messages';
import { TypingIndicator } from './TypingIndicator';

interface ChatMessageBubbleProps {
  message: ChatMessage;
}

export function ChatMessageBubble({ message }: ChatMessageBubbleProps) {
  const isUser = message.role === 'user';

  return (
    <Box
      sx={{
        display: 'flex',
        justifyContent: isUser ? 'flex-end' : 'flex-start',
        mb: 1.5,
      }}
    >
      <Paper
        elevation={0}
        sx={{
          maxWidth: '78%',
          px: 2,
          py: 1.5,
          bgcolor: isUser ? 'primary.main' : 'grey.100',
          color: isUser ? 'primary.contrastText' : 'text.primary',
          borderTopLeftRadius: isUser ? 16 : 4,
          borderTopRightRadius: isUser ? 4 : 16,
        }}
      >
        {message.streaming && !message.content ? (
          <TypingIndicator />
        ) : (
          <Typography
            variant="body1"
            sx={{ whiteSpace: 'pre-wrap', wordBreak: 'break-word' }}
          >
            {message.content}
            {message.streaming ? '▍' : ''}
          </Typography>
        )}
        <Typography
          variant="caption"
          sx={{
            display: 'block',
            mt: 0.75,
            opacity: 0.72,
            textAlign: isUser ? 'right' : 'left',
          }}
        >
          {formatMessageTime(message.timestamp)}
        </Typography>
      </Paper>
    </Box>
  );
}
