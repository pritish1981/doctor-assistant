import { Box, keyframes } from '@mui/material';

const bounce = keyframes`
  0%, 80%, 100% { transform: translateY(0); opacity: 0.45; }
  40% { transform: translateY(-6px); opacity: 1; }
`;

export function TypingIndicator() {
  return (
    <Box
      aria-label="Assistant is typing"
      role="status"
      sx={{
        display: 'inline-flex',
        alignItems: 'center',
        gap: 0.75,
        px: 1.5,
        py: 1,
      }}
    >
      {[0, 1, 2].map((index) => (
        <Box
          key={index}
          sx={{
            width: 8,
            height: 8,
            borderRadius: '50%',
            bgcolor: 'text.secondary',
            animation: `${bounce} 1.2s infinite ease-in-out`,
            animationDelay: `${index * 0.15}s`,
          }}
        />
      ))}
    </Box>
  );
}
