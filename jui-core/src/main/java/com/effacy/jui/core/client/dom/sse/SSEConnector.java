package com.effacy.jui.core.client.dom.sse;

import com.effacy.jui.platform.util.client.Logger;

import elemental2.dom.EventSource;

/**
 * Connector that opens a GET-based {@code text/event-stream} connection using
 * the browser {@link EventSource} API. This complements
 * {@link SSEPostConnector} which POSTs JSON and progressively reads an SSE
 * response via {@code XMLHttpRequest}.
 * <p>
 * Usage:
 *
 * <pre>
 * new SSEConnector()
 *     .onMessage(data -> { ... })
 *     .onOpen(() -> { ... })
 *     .onError(() -> { ... })
 *     .connect("/app/stream");
 * </pre>
 */
public class SSEConnector {

    @FunctionalInterface
    public interface ISSEMessageHandler {

        /**
         * Called when a message event is received.
         *
         * @param data
         *             the event data payload.
         */
        void onMessage(String data);
    }

    @FunctionalInterface
    public interface ISSEOpenHandler {

        /**
         * Called when the connection is established.
         */
        void onOpen();
    }

    @FunctionalInterface
    public interface ISSEErrorHandler {

        /**
         * Called when an error occurs on the connection.
         */
        void onError();
    }

    private EventSource eventSource;

    private ISSEMessageHandler messageHandler;

    private ISSEOpenHandler openHandler;

    private ISSEErrorHandler errorHandler;

    private boolean debug;

    /**
     * Assigns the handler for message events.
     */
    public SSEConnector onMessage(ISSEMessageHandler handler) {
        this.messageHandler = handler;
        return this;
    }

    /**
     * Assigns the handler for connection open.
     */
    public SSEConnector onOpen(ISSEOpenHandler handler) {
        this.openHandler = handler;
        return this;
    }

    /**
     * Assigns the handler for errors.
     */
    public SSEConnector onError(ISSEErrorHandler handler) {
        this.errorHandler = handler;
        return this;
    }

    /**
     * Enables debug logging of SSE events.
     */
    public SSEConnector debug(boolean debug) {
        this.debug = debug;
        return this;
    }

    /**
     * Opens the SSE connection to the given URL.
     *
     * @param url
     *            the endpoint URL.
     */
    public void connect(String url) {
        eventSource = new EventSource(url);

        eventSource.onmessage = (evt -> {
            fireMessage(evt.data);
        });

        eventSource.onopen = (evt -> {
            fireOpen();
        });

        eventSource.onerror = (evt -> {
            fireError();
        });
    }

    private void fireMessage(String data) {
        if (debug)
            Logger.trace("SSE-message", data);
        if (messageHandler != null) {
            try {
                messageHandler.onMessage(data);
            } catch (Throwable t) {
                Logger.error("Uncaught exception on SSE message", t);
            }
        }
    }

    private void fireOpen() {
        if (debug)
            Logger.trace("SSE-open", "connected");
        if (openHandler != null) {
            try {
                openHandler.onOpen();
            } catch (Throwable t) {
                Logger.error("Uncaught exception on SSE open", t);
            }
        }
    }

    private void fireError() {
        if (debug)
            Logger.trace("SSE-error", "error");
        if (errorHandler != null) {
            try {
                errorHandler.onError();
            } catch (Throwable t) {
                Logger.error("Uncaught exception on SSE error", t);
            }
        }
    }
}
