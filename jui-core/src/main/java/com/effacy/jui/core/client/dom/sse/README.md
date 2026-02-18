# Package

This package provides client-side connectors for consuming Server-Sent Event (SSE) streams from GWT code. Both connectors use a fluent builder pattern for handler registration and include debug logging via `Logger` and exception guards around all callbacks.

# Usage

## Classes

### `SSEConnector`

Wraps the browser `EventSource` API to open a GET-based SSE connection. Suitable for simple streaming endpoints that accept no request body. Handlers are registered for message receipt, connection open, and error, then `connect(url)` initiates the stream. The browser manages reconnection automatically per the `EventSource` specification.

### `SSEPostConnector`

Uses `XMLHttpRequest` to POST a JSON body and progressively read the response as a `text/event-stream`. This is necessary when the server needs a request payload (e.g. a chat prompt) which the `EventSource` API does not support since it is GET-only. Handlers are registered for typed SSE events (with event name and data), errors (with HTTP status and message), and completion, then `send(url, jsonBody)` initiates the request.

## When to use which

Use `SSEConnector` when the endpoint is a simple GET stream with no request body. Use `SSEPostConnector` when a JSON payload must be sent to initiate the stream (e.g. AI chat requests where the prompt is submitted in the body).

# Implementation notes

`SSEPostConnector` performs its own incremental SSE parsing since `XMLHttpRequest` delivers raw text rather than parsed events. It reads new chunks via `onreadystatechange` at readyState 3 (LOADING), tracks the processed byte offset, and buffers partial lines across calls. Each SSE record is delimited by a blank line; the parser recognises `event:` and `data:` fields and ignores `id:`, `retry:`, and comment lines. Multi-line `data:` fields are concatenated with newlines.

`SSEConnector` delegates all parsing and reconnection to the browser's `EventSource` implementation and simply forwards the `onmessage`, `onopen`, and `onerror` callbacks through its handler interfaces.

Both connectors wrap handler invocations in try/catch blocks to prevent an uncaught exception in application code from disrupting the stream processing pipeline.
