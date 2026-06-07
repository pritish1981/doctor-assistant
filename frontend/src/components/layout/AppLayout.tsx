import { AppBar, Box, Container, Toolbar, Typography } from '@mui/material';
import { SessionSidebar } from '../sessions/SessionSidebar';
import { ChatWindow } from '../chat/ChatWindow';

export function AppLayout() {
  return (
    <Box sx={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <AppBar position="static" elevation={0} color="transparent">
        <Toolbar sx={{ borderBottom: 1, borderColor: 'divider' }}>
          <Typography variant="h6" color="primary.main" sx={{ flexGrow: 1 }}>
            Super Clinic · Doctor Assistant
          </Typography>
          <Typography variant="body2" color="text.secondary">
            AI-powered appointments
          </Typography>
        </Toolbar>
      </AppBar>

      <Container
        maxWidth="xl"
        sx={{
          flex: 1,
          py: { xs: 2, md: 3 },
          display: 'flex',
          flexDirection: { xs: 'column', md: 'row' },
          gap: 2,
          minHeight: 0,
        }}
      >
        <SessionSidebar />
        <ChatWindow />
      </Container>
    </Box>
  );
}
