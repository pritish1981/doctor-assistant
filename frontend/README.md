# Doctor Assistant UI

React + Vite + TypeScript + Material UI chat client for the Super Clinic Doctor Assistant API.

## Stack

- **React 19** with TypeScript
- **Vite** dev server and build
- **Material UI** components and theming

## Features

- Chat window with streaming assistant replies (SSE)
- Conversation history sidebar with session list
- Typing indicator while the assistant responds
- Session support — create, resume, and close conversations

## Project structure

```
frontend/
├── index.html
├── vite.config.ts              # Dev proxy → http://localhost:8080
├── .env.example
└── src/
    ├── main.tsx
    ├── App.tsx
    ├── api/
    │   ├── client.ts           # Fetch wrapper + error handling
    │   └── conversationApi.ts  # REST + SSE streaming
    ├── components/
    │   ├── chat/
    │   │   ├── ChatWindow.tsx
    │   │   ├── ChatMessageList.tsx
    │   │   ├── ChatMessageBubble.tsx
    │   │   ├── ChatInput.tsx
    │   │   └── TypingIndicator.tsx
    │   ├── sessions/
    │   │   ├── SessionSidebar.tsx
    │   │   └── SessionListItem.tsx
    │   └── layout/
    │       └── AppLayout.tsx
    ├── context/
    │   └── SessionContext.tsx  # Patient + session state
    ├── hooks/
    │   └── useChat.ts          # Messages + streaming send
    ├── pages/
    │   └── DoctorAssistantPage.tsx
    ├── theme/
    │   └── theme.ts
    ├── types/
    │   └── conversation.ts
    └── utils/
        └── messages.ts
```

## Quick start

```bash
# Backend (from repo root)
./mvnw spring-boot:run

# Frontend
cd frontend
cp .env.example .env
npm install
npm run dev
```

Open [http://localhost:5173](http://localhost:5173).

## Environment

| Variable | Default | Description |
|----------|---------|-------------|
| `VITE_API_BASE_URL` | `/api/v1` | API base (proxied in dev) |
| `VITE_DEFAULT_PATIENT_ID` | seed patient UUID | Demo patient for sessions |

## API endpoints used

| Method | Path | Purpose |
|--------|------|---------|
| `POST` | `/conversations` | Start session |
| `GET` | `/conversations?patientId=` | List sessions |
| `GET` | `/conversations/{id}/messages` | Load history |
| `POST` | `/conversations/{id}/messages/stream` | Stream reply (SSE) |
| `POST` | `/conversations/{id}/close` | Close session |
