package com.effacy.jui.text.ui.chat;

import java.util.List;

/**
 * Contract between a {@link ChatPanel} and its host page.
 * <p>
 * The host provides context entries describing where the agent is operating and
 * receives notifications for client-side actions.
 */
public interface IChatPanelHost {

    /**
     * A typed context entry that describes a scope the chat agent is operating
     * in. Each entry has a type (e.g. "organisation", "application") and an ID.
     * The server uses these to resolve available tools and domain guidance.
     */
    public record ContextEntry(String type, String id) {

        /**
         * Convenience factory method.
         */
        public static ContextEntry of(String type, String id) {
            return new ContextEntry(type, id);
        }
    }

    /**
     * Context entries describing the scopes the chat agent is operating in.
     * <p>
     * Each entry has a type and an ID. For example, an application page would
     * return entries for both the organisation and the application:
     * <pre>
     * List.of(
     *     ContextEntry.of("organisation", "5"),
     *     ContextEntry.of("application", "123")
     * )
     * </pre>
     * The server uses these to resolve available tools, authentication, and
     * domain-specific guidance.
     *
     * @return context entries (must not be {@code null} — at minimum provide the
     *         organisation context).
     */
    public List<ContextEntry> context();

    /**
     * The agent reference to use for chat interactions. Return {@code null} to
     * use the default agent.
     *
     * @return the agent reference (e.g. "knowledgevibe"), or {@code null}.
     */
    public default String agentRef() {
        return null;
    }

    /**
     * Optional fixed conversation ID. Return {@code null} to let the panel manage
     * its own conversation lifecycle.
     *
     * @return the conversation ID, or {@code null}.
     */
    public default String conversationId() {
        return null;
    }

    /**
     * Called by the panel when streaming completes and a conversation ID is
     * assigned or confirmed by the server.
     *
     * @param conversationId
     *                       the conversation ID.
     */
    public default void onConversationEstablished(String conversationId) {}

    /**
     * Optional scope that binds conversations to a specific context (e.g.
     * "application:123"). When set, the chat panel will attempt to load the most
     * recent conversation for this scope on activation, and new conversations will
     * be tagged with this scope.
     *
     * @return the scope string, or {@code null} for unscoped conversations.
     */
    public default String scope() {
        return null;
    }

    /**
     * Client-side action names available on this page. These are UI commands that
     * the AI agent can invoke to control the host page (e.g. "refresh_view",
     * "post_message").
     * <p>
     * Each name must correspond to a registered server-side {@code IChatAction}.
     * The {@code tool} SSE event is suppressed for these names since the subsequent
     * {@code action} event handles the display.
     *
     * @return action names, or {@code null}.
     */
    public default List<String> actions() {
        return null;
    }

    /**
     * Called when the AI agent invokes a client-side action to control the host
     * page UI.
     *
     * @param action
     *                the action name (e.g. "refresh_view").
     * @param payload
     *                the action payload (JSON string).
     */
    public default void onChatAction(String action, String payload) {}
}
