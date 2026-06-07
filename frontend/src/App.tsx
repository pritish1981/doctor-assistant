import CssBaseline from '@mui/material/CssBaseline';
import { ThemeProvider } from '@mui/material/styles';
import { SessionProvider } from './context/SessionContext';
import { DoctorAssistantPage } from './pages/DoctorAssistantPage';
import { theme } from './theme/theme';

export default function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <SessionProvider>
        <DoctorAssistantPage />
      </SessionProvider>
    </ThemeProvider>
  );
}
