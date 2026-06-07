/** Parse SSE event payload; backend sends JSON-encoded string chunks. */
export function parseSseEventData(event: string): string | null {
  const dataParts: string[] = [];

  for (const line of event.split('\n')) {
    if (!line.startsWith('data:')) continue;
    let payload = line.slice(5);
    // SSE spec: optional single space after "data:" is not part of the payload
    if (payload.startsWith(' ')) {
      payload = payload.slice(1);
    }
    dataParts.push(payload);
  }

  if (dataParts.length === 0) {
    return null;
  }

  const raw = dataParts.join('\n');
  try {
    return JSON.parse(raw) as string;
  } catch {
    return raw;
  }
}
