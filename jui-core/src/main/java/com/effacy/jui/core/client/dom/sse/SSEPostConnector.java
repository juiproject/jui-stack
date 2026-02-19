package com.effacy.jui.core.client.dom.sse;

import com.effacy.jui.platform.util.client.Logger;

import elemental2.dom.XMLHttpRequest;

/**
 * Connector that POSTs JSON and progressively reads a {@code text/event-stream}
 * response using {@link XMLHttpRequest}. This complements {@link SSEConnector}
 * which is GET-only (it wraps the browser {@code EventSource} API).
 * <p>
 * Usage:
 *
 * <pre>
 * new SSEPostConnector()
 *     .onEvent((name, data) -> { ... })
 *     .onError((status, msg) -> { ... })
 *     .onComplete(() -> { ... })
 *     .send("/app/chat", jsonBody);
 * </pre>
 * <p>
 * The connector reads the response incrementally via
 * {@code onreadystatechange} (readyState 3 = LOADING) and parses SSE-formatted
 * text into typed events. Each SSE record is expected to be:
 *
 * <pre>
 * event: eventName
 * data: payload
 *
 * </pre>
 *
 * where a blank line terminates the record and triggers dispatch.
 */
public class SSEPostConnector {

    @FunctionalInterface
    public interface ISSEEventHandler {

        /**
         * Called when a complete SSE event has been received.
         *
         * @param eventName
         *                  the event type (e.g. "text", "permission", "done").
         * @param data
         *                  the event data payload (typically JSON).
         */
        void onEvent(String eventName, String data);
    }

    @FunctionalInterface
    public interface ISSEErrorHandler {

        /**
         * Called on transport or HTTP errors.
         *
         * @param status
         *                the HTTP status code (0 for network/timeout errors).
         * @param message
         *                a description of the error.
         */
        void onError(int status, String message);
    }

    @FunctionalInterface
    public interface ISSECompleteHandler {

        /**
         * Called when the response has been fully received.
         */
        void onComplete();
    }

    private ISSEEventHandler eventHandler;

    private ISSEErrorHandler errorHandler;

    private ISSECompleteHandler completeHandler;

    private boolean debug;

    /**
     * Assigns the handler for SSE events.
     */
    public SSEPostConnector onEvent(ISSEEventHandler handler) {
        this.eventHandler = handler;
        return this;
    }

    /**
     * Assigns the handler for errors.
     */
    public SSEPostConnector onError(ISSEErrorHandler handler) {
        this.errorHandler = handler;
        return this;
    }

    /**
     * Assigns the handler for completion.
     */
    public SSEPostConnector onComplete(ISSECompleteHandler handler) {
        this.completeHandler = handler;
        return this;
    }

    /**
     * Enables debug logging of SSE events.
     */
    public SSEPostConnector debug(boolean debug) {
        this.debug = debug;
        return this;
    }

    /**
     * Sends a POST request and streams the SSE response.
     *
     * @param url
     *                 the endpoint URL.
     * @param jsonBody
     *                 the JSON request body.
     */
    public void send(String url, String jsonBody) {
        XMLHttpRequest xhr = new XMLHttpRequest();

        // Parser state maintained across readystatechange calls.
        int[] processedLength = { 0 };
        String[] partialLine = { "" };
        String[] currentEvent = { "message" };
        StringBuilder[] currentData = { new StringBuilder() };
        boolean[] hasData = { false };

        xhr.onreadystatechange = e -> {
            // readyState 3 (LOADING): partial data available.
            // readyState 4 (DONE): request complete.
            if ((xhr.readyState != 3) && (xhr.readyState != 4))
                return null;

            // Process any new response text.
            String fullText = xhr.responseText;
            if ((fullText != null) && (fullText.length() > processedLength[0])) {
                String newText = fullText.substring(processedLength[0]);
                processedLength[0] = fullText.length();
                parseSSE(newText, partialLine, currentEvent, currentData, hasData);
            }

            // On completion, handle status.
            if (xhr.readyState == 4) {
                if ((xhr.status == 200) || (xhr.status == 0)) {
                    if (completeHandler != null) {
                        try {
                            completeHandler.onComplete();
                        } catch (Throwable t) {
                            Logger.error("Uncaught exception on SSEPost complete", t);
                        }
                    }
                } else if (xhr.status == 403) {
                    fireError(403, "Session expired");
                } else {
                    fireError(xhr.status, "Request failed with status " + xhr.status);
                }
            }

            return null;
        };

        xhr.onerror = e -> {
            fireError(0, "Network error");
            return null;
        };

        xhr.ontimeout = e -> {
            fireError(0, "Request timed out");
            //return null;
        };

        xhr.open("POST", url, true);
        xhr.setRequestHeader("Content-Type", "application/json; charset=UTF-8");
        xhr.setRequestHeader("Accept", "text/event-stream");
        xhr.send(jsonBody);
    }

    /**
     * Incrementally parses SSE-formatted text. Maintains state across calls via the
     * mutable array parameters.
     */
    private void parseSSE(String newText, String[] partialLine, String[] currentEvent, StringBuilder[] currentData, boolean[] hasData) {
        // Prepend any leftover partial line from the previous call.
        String text = partialLine[0] + newText;
        partialLine[0] = "";

        int pos = 0;
        while (pos < text.length()) {
            int nl = text.indexOf('\n', pos);
            if (nl < 0) {
                // No newline found — the rest is a partial line, buffer it.
                partialLine[0] = text.substring(pos);
                break;
            }

            String line = text.substring(pos, nl);
            // Strip trailing \r if present (handles \r\n line endings).
            if (line.endsWith("\r"))
                line = line.substring(0, line.length() - 1);
            pos = nl + 1;

            if (line.isEmpty()) {
                // Blank line: dispatch the accumulated event.
                if (hasData[0])
                    fireEvent(currentEvent[0], currentData[0].toString());
                currentEvent[0] = "message";
                currentData[0] = new StringBuilder();
                hasData[0] = false;
            } else if (line.startsWith("event:")) {
                currentEvent[0] = line.substring(6).trim();
            } else if (line.startsWith("data:")) {
                if (hasData[0])
                    currentData[0].append('\n');
                currentData[0].append(line.substring(5).trim());
                hasData[0] = true;
            }
            // Ignore "id:", "retry:", and comment lines starting with ":".
        }
    }

    private void fireEvent(String eventName, String data) {
        if (debug)
            Logger.trace("SSEPost-event[" + eventName + "]", data);
        if (eventHandler != null) {
            try {
                eventHandler.onEvent(eventName, data);
            } catch (Throwable t) {
                Logger.error("Uncaught exception on SSEPost event", t);
            }
        }
    }

    private void fireError(int status, String message) {
        if (debug)
            Logger.trace("SSEPost-error", status + ": " + message);
        if (errorHandler != null) {
            try {
                errorHandler.onError(status, message);
            } catch (Throwable t) {
                Logger.error("Uncaught exception on SSEPost error", t);
            }
        }
    }
}
