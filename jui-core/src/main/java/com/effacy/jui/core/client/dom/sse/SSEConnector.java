package com.effacy.jui.core.client.dom.sse;

import java.util.function.Consumer;

import com.effacy.jui.platform.util.client.Logger;

import elemental2.dom.Event;
import elemental2.dom.EventSource;

public class SSEConnector {

    private EventSource eventSource;

    private Consumer<String> onmessage;

    private Consumer<Event> onopen;

    private Consumer<Event> onerror;

    private boolean debug;

    public SSEConnector onmessage(Consumer<String> onmessage) {
        this.onmessage = onmessage;
        return this;
    }

    public SSEConnector onopen(Consumer<Event> onopen) {
        this.onopen = onopen;
        return this;
    }

    public SSEConnector onerror(Consumer<Event> onerror) {
        this.onerror = onerror;
        return this;
    }

    public SSEConnector debug(boolean debug) {
        this.debug = debug;
        return this;
    }

    public void connect(String url) {
        eventSource = new EventSource(url);

        eventSource.onmessage = (evt -> {
            if (debug)
                Logger.trace("SSE-onmessage", evt.data);
            if (onmessage != null) {
                try {
                    onmessage.accept(evt.data);
                } catch (Throwable e) {
                    Logger.error("Uncaught exception on SSE onmessage", e);
                }
            }
        });

        eventSource.onopen = (evt -> {
            if (debug)
                Logger.trace("SSE-done", evt.toString());
            if (onopen != null) {
                try {
                    onopen.accept(evt);
                } catch (Throwable e) {
                    Logger.error("Uncaught exception on SSE open", e);
                }
            }
        });

        eventSource.onerror = (evt -> {
            if (debug)
                Logger.trace("SSE-error", evt.toString());
            if (onerror != null) {
                try {
                    onerror.accept(evt);
                } catch (Throwable e) {
                    Logger.error("Uncaught exception on SSE error", e);
                }
            }
        });
    }
}
