# Package

This package contains classes that support the client-side implementation of a chat agent panel. The two principal classes are:

- `ChatPanel` — A self-contained, embeddable chat component with header bar (usage indicator, new chat, conversation picker), welcome screen, messages area, input bar, and tool/action activity display.
- `IChatPanelHost` — The contract between the chat panel and its host page, providing context entries and receiving notifications (actions, conversation events). Also declares the `ContextEntry` record as an inner type.

*Note that this is located in the this project since it makes use of markdown. Later this may be moved to a separate more suitable project.* 

# Usage

## Embedding the chat panel

Create a `ChatPanel` with a `Config` that provides an `IChatPanelHost`:

```java
ChatPanel chatPanel = new ChatPanel(new ChatPanel.Config(new IChatPanelHost() {
    @Override
    public List<ContextEntry> context() {
        return List.of(
            ContextEntry.of("organisation", String.valueOf(orgId)),
            ContextEntry.of("application", String.valueOf(appId))
        );
    }
}));
```

Insert into any layout using `Cpt.$`:

```java
Div.$(parent).css("width: 440px;").$(right -> {
    Cpt.$(right, chatPanel);
});
```

Call `chatPanel.refresh()` when the panel becomes visible (e.g. in `onNavigateTo`).

## Configuration

`ChatPanel.Config` supports:

| Method | Description |
|--------|-------------|
| `host(IChatPanelHost)` | The host interface (required). |
| `showAgentName(boolean)` | Display agent name above responses (default `true`). |
| `baseEndpoint(String)` | Base endpoint path for chat API calls (default `"/app/chat"`). All endpoints are resolved as `{baseEndpoint}/...`. |
| `variant(Variant)` | Apply a visual variant (e.g. `Variant.BUBBLE` for right-aligned rounded bubbles). |
| `css(String)` | Override CSS variables via inline styles on the root element. |

## Host interface (`IChatPanelHost`)

The host page implements `IChatPanelHost` to control the chat panel's behaviour:

| Method | Required | Description |
|--------|----------|-------------|
| `context()` | Yes | Returns `List<ContextEntry>` describing the scopes the agent operates in (e.g. organisation, application). The server uses these to resolve tools and guidance. |
| `agentRef()` | No | Agent reference (e.g. `"knowledgevibe"`). `null` for default agent. |
| `conversationId()` | No | Fixed conversation ID. `null` to let the panel manage conversations. |
| `scope()` | No | Binds conversations to a context (e.g. `"application:123"`). On refresh, the panel auto-loads the most recent conversation for this scope. New conversations are tagged with this scope. |
| `actions()` | No | List of action names available on this page (e.g. `["refresh_view"]`). Each must correspond to a registered server-side `IChatAction`. The `tool` SSE event is suppressed for these names since the `action` event handles the display. |
| `onConversationEstablished(String)` | No | Called when a conversation ID is assigned after the first message. |
| `onChatAction(String, String)` | No | Called when the AI agent invokes a client-side action. Receives the action name and JSON payload. |

### `ContextEntry`

`IChatPanelHost.ContextEntry` is a record with `type` and `id` fields. Use the `ContextEntry.of(type, id)` factory method:

```java
ContextEntry.of("organisation", "5")
ContextEntry.of("application", "123")
```

The server converts these into a flat map (type → id) used by `IChatContextProvider` implementations to resolve tools and system prompt guidance.

## Public API on `ChatPanel`

| Method | Description |
|--------|-------------|
| `postMessage(String)` | Programmatically send a message as if the user typed it. |
| `clear()` | Clear all messages and reset conversation state. |
| `loadConversation(String)` | Load and display an existing conversation by ID. |
| `refresh()` | Reload usage, conversations, and auto-load any scoped conversation. Call on activation. |
| `isStreaming()` | Whether the panel is currently streaming a response. |

## Server communication

### Request (POST)

The chat panel sends a `POST` to `{baseEndpoint}` (default `/app/chat`) with `Content-Type: application/json` and `Accept: text/event-stream`. The request body:

```json
{
    "message": "User's message text",
    "conversationId": "uuid-string-or-null",
    "agentRef": "knowledgevibe",
    "scope": "application:456",
    "context": [
        {"type": "organisation", "id": "5"},
        {"type": "application", "id": "456"}
    ],
    "actions": ["refresh_view"]
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `message` | string | Yes | The user's message. |
| `conversationId` | string | No | Existing conversation ID to continue, or omit for new. |
| `agentRef` | string | No | Agent reference. Omit for default agent. |
| `scope` | string | No | Scope to tag new conversations with. |
| `context` | array | Yes | Array of `{type, id}` context entries. |
| `actions` | string[] | No | Action names to register for this request. |

### Response (SSE)

The server responds with a `text/event-stream` containing the following SSE event types:

#### `meta` — Agent metadata

Sent once at the start. Displayed as an agent header above the response (if `showAgentName` is enabled).

```
event: meta
data: {"agentName": "Builder", "agentIcon": null}
```

#### `text` — Streamed response content

Sent incrementally (token by token). Rendered as markdown in the assistant bubble.

```
event: text
data: {"content": "Here is "}
```

#### `tool` — Tool invocation notification

Sent before a tool executes. Displayed as an expandable activity widget with a fan icon.

```
event: tool
data: {"toolName": "read_application_model", "label": "Reading current model", "argumentsJson": "{...}"}
```

| Field | Type | Description |
|-------|------|-------------|
| `toolName` | string | The tool name. |
| `label` | string | Optional human-readable display label. Falls back to `toolName` if absent. |
| `argumentsJson` | string | The tool arguments as an escaped JSON string. |

#### `action` — Client-side UI action

Sent when a tool emits an action for the host page to handle. Displayed with a binoculars icon. The host receives the action via `onChatAction()`.

```
event: action
data: {"action": "refresh_view", "label": "Refreshing view", "payload": {}}
```

| Field | Type | Description |
|-------|------|-------------|
| `action` | string | The action name. |
| `label` | string | Optional display label. |
| `payload` | object | Action-specific payload (passed to `onChatAction` as a JSON string). |

#### `continue` — Segment boundary

Sent between response segments during multi-turn tool calling. The client finalises the current text bubble (renders markdown as non-partial) and prepares a fresh bubble for the next segment. The tool activity group is **not** collapsed — tool and action events from subsequent turns continue accumulating into the same group.

```
event: continue
data: {}
```

#### `done` — Streaming complete

Sent when the assistant finishes responding. Carries the conversation ID.

```
event: done
data: {"conversationId": "uuid-string", "messageId": "uuid-string"}
```

#### `notice` — Informational message

Displayed as a warning-style banner (yellow background).

```
event: notice
data: {"message": "Your organisation has exceeded its usage limit."}
```

#### `error` — Error message

Displayed as an error bubble (red background).

```
event: error
data: {"message": "An error occurred: ..."}
```

### Auxiliary endpoints

All endpoints are relative to `{baseEndpoint}` (default `/app/chat`):

| Endpoint | Method | Description |
|----------|--------|-------------|
| `{baseEndpoint}/conversations?organisationId=X` | GET | Returns recent conversations as `[{id, title, updatedSdt}]`. |
| `{baseEndpoint}/messages/{conversationId}` | GET | Returns all messages for a conversation as `[{role, content, agentName, agentRef, toolCalls, actionCalls, timestamp}]`. |
| `{baseEndpoint}/info?organisationId=X` | GET | Returns usage info as `{countForMonth, limitForMonth, ...}`. |
| `{baseEndpoint}/scope?organisationId=X&scope=Y` | GET | Returns the most recent conversation for the given scope, or `{}` if none. |

## CSS variables

All colours and key dimensions are configurable via CSS variables defined on the `.component` root element. Override them via `config.css("--jui-chatpanel-color-primary: #0066cc;")` or via a parent CSS rule.

### Core colours

| Variable | Default | Description |
|----------|---------|-------------|
| `--jui-chatpanel-color-primary` | `#173327` | Buttons, titles, usage filled, bubble accent. |
| `--jui-chatpanel-color-bg` | `#fff` | Panel, cards, menus. |
| `--jui-chatpanel-color-bg-muted` | `#fbfaf7` | Picker trigger, welcome input, user bubble bg. |
| `--jui-chatpanel-color-border` | `#d8d2c6` | All borders, usage empty segments. |
| `--jui-chatpanel-color-text` | `#1d2c24` | Primary text. |
| `--jui-chatpanel-color-text-muted` | `#5b665e` | Usage label, subtitle. |
| `--jui-chatpanel-color-text-hint` | `#889188` | Input hints, thinking indicator. |
| `--jui-chatpanel-color-notice-bg` | `#fef3c7` | Notice background. |
| `--jui-chatpanel-color-notice-text` | `#92400e` | Notice text. |
| `--jui-chatpanel-color-error-bg` | `#fdf2f2` | Error background. |
| `--jui-chatpanel-color-error-text` | `#7f1d1d` | Error text. |
| `--jui-chatpanel-color-error-border` | `#efc1c1` | Error border. |

### User message bubble

| Variable | Default | Description |
|----------|---------|-------------|
| `--jui-chatpanel-bubble-user-width` | `100%` | Bubble width. |
| `--jui-chatpanel-bubble-user-align` | `stretch` | `align-self` positioning. |
| `--jui-chatpanel-bubble-user-radius` | `4px 8px 8px 4px` | Border radius. |
| `--jui-chatpanel-bubble-user-bg` | `var(--jui-chatpanel-color-bg-muted)` | Background. |
| `--jui-chatpanel-bubble-user-color` | `var(--jui-chatpanel-color-text)` | Text colour. |
| `--jui-chatpanel-bubble-user-padding` | `10px 14px` | Padding. |
| `--jui-chatpanel-bubble-user-margin` | `2px 0 10px 0` | Margin. |
| `--jui-chatpanel-bubble-user-border-left` | `3px solid primary` | Left border. |
| `--jui-chatpanel-bubble-user-border-right` | `none` | Right border. |
| `--jui-chatpanel-bubble-user-border-top` | `none` | Top border. |
| `--jui-chatpanel-bubble-user-border-bottom` | `none` | Bottom border. |

### Layout and content

| Variable | Default | Description |
|----------|---------|-------------|
| `--jui-chatpanel-content-offset` | `14px` | Shared left padding for agent header, assistant bubble, and tool activity. |
| `--jui-chatpanel-icon-left` | `-5px` | Absolute left position of the agent icon. |
| `--jui-chatpanel-messages-gap` | `10px` | Gap between messages in the conversation flex container. |

### Agent header

| Variable | Default | Description |
|----------|---------|-------------|
| `--jui-chatpanel-agent-color` | `var(--jui-chatpanel-color-text)` | Agent name colour. |
| `--jui-chatpanel-agent-font-size` | `0.9rem` | Agent name font size. |
| `--jui-chatpanel-agent-icon-color` | `var(--jui-chatpanel-color-primary)` | Agent icon colour. |
| `--jui-chatpanel-agent-icon-font-size` | `1em` | Agent icon font size. |

### Tool/action activity

| Variable | Default | Description |
|----------|---------|-------------|
| `--jui-chatpanel-tool-bg` | `#f5f5f5` | Tool group background. |
| `--jui-chatpanel-tool-color` | `#6b7280` | Tool group text colour. |
| `--jui-chatpanel-tool-font-size` | `0.75rem` | Summary font size. |
| `--jui-chatpanel-tool-item-font-size` | `0.688rem` | Individual item font size. |

# Design

## Component architecture

`ChatPanel` extends `Component<ChatPanel.Config>` and uses the `ILocalCSS` pattern with `@CssResource(stylesheet = """...""")` for all styling. The CSS is scoped under `.component` to prevent name clashes. All colours and key dimensions are exposed as CSS variables for theming.

The component has three visual states managed by a `.show` CSS class:

1. **Welcome screen** — Initial state with a prompt card and input. Shown via `welcomeEl.classList.add(styles().show())`.
2. **Conversation** — Messages area with input bar. Shown when the user sends a message or loads a conversation.
3. **Header bar** — Always visible. Contains usage indicator, new chat button, and conversation picker dropdown.

## Streaming and SSE

The panel uses `SSEPostConnector` (not the browser `EventSource` API) to `POST` JSON and progressively read a `text/event-stream` response. This allows sending a request body (which `EventSource` does not support).

Events are parsed incrementally via `readyState 3` (LOADING) and dispatched by event name. The `handleEvent()` method routes each event type to the appropriate rendering method.

## Multi-segment streaming

The server sends `continue` events between response segments during multi-turn tool calling. On `continue`, the client:

1. Finalises the current text bubble (renders markdown as non-partial).
2. Removes the bubble if it was empty (no text was streamed — only tool activity occurred).
3. Creates a fresh assistant bubble for the next segment.

The tool activity group is **not** collapsed on `continue` — all tool and action events across all turns accumulate into a single group. The group only collapses when streaming finishes (`done` event / SSE connection closes), showing "N actions completed".

When streaming finishes, if the final segment has no text content, the empty thinking bubble is removed rather than left lingering.

## Tool and action display

Tool invocations and action events are accumulated into a single collapsible group per response. While streaming, the group is open showing "N actions running..." with individual items listed. When streaming finishes, the group collapses to "N actions completed" with a clickable chevron to expand.

Tools (server-side operations) display with a `FontAwesome.fan()` icon. Actions (client-side UI commands) display with a `FontAwesome.binoculars()` icon.

The `actions()` list on the host controls which tools have their `tool` SSE event suppressed — for client-side actions, only the `action` event renders (avoiding duplicates).

## Scoped conversations

When `IChatPanelHost.scope()` returns a non-null value (e.g. `"application:123"`):

- **On refresh**: The panel calls `GET /app/chat/scope?organisationId=X&scope=Y` to find the most recent conversation for that scope. If found, it loads and displays that conversation automatically.
- **On new conversation**: The `scope` field is included in the POST body, and the server tags the created conversation with this scope in the database.
- **On navigation**: `refresh()` calls `startNewChat()` first to clear any stale state from a previous scope, then loads the scoped conversation for the new context.

This allows page-specific chat experiences where navigating back to a page resumes the previous conversation.

## Thinking indicator

When a message is sent, a thinking indicator ("Thinking.", "Thinking..", "Thinking...") animates on the assistant bubble using `setInterval`. It is stopped when the first `text`, `tool`, or `action` event arrives. If tool events arrive during processing, the indicator restarts after each tool to show the LLM is still working.

## Conversation recovery

When loading a past conversation via `loadConversation()`, messages are rendered from the stored data. Agent headers are displayed before each assistant message group, tracking the agent name — if the agent changes mid-conversation (multi-agent support), a new header appears. The `agentName` is resolved server-side from the stored `agentRef` via the `ChatAgentRegistry`.

Recorded tool calls and action calls are rendered as a collapsed activity group before the assistant's text response. Messages with no text content (only tool/action activity) still render their activity group. All text segments from the multi-turn loop are accumulated and stored as a single content string, ensuring conversation recovery shows the complete response.
