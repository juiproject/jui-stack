package com.effacy.jui.text.ui.chat;

import java.util.List;

import org.gwtproject.json.client.JSONArray;
import org.gwtproject.json.client.JSONObject;
import org.gwtproject.json.client.JSONParser;
import org.gwtproject.json.client.JSONString;

import com.effacy.jui.core.client.component.Component;
import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.P;
import com.effacy.jui.core.client.dom.builder.Text;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.jquery.JQuery;
import com.effacy.jui.core.client.dom.sse.SSEPostConnector;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.text.type.builder.markdown.MarkdownParser;
import com.effacy.jui.text.ui.chat.IChatPanelHost.ContextEntry;
import com.effacy.jui.text.ui.type.FormattedTextStyles;
import com.effacy.jui.text.ui.type.builder.Elemental2Builder;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.google.gwt.core.client.GWT;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLTextAreaElement;
import elemental2.dom.XMLHttpRequest;

/**
 * Self-contained, embeddable chat panel with header bar (usage indicator, new
 * chat, conversation picker), welcome screen, messages area, and input bar.
 * <p>
 * Embed into any page layout. The host provides context and receives
 * notifications via {@link IChatPanelHost}.
 * <p>
 * Use {@link #postMessage(String)} to programmatically send a message from the
 * host page.
 */
public class ChatPanel extends Component<ChatPanel.Config> {

    /************************************************************************
     * Configuration
     ************************************************************************/

    public static class Config extends Component.Config {

        /**
         * Used to provide a default configuration.
         */
        @FunctionalInterface
        public interface Variant {

            public static final Variant BUBBLE = config -> {
                config.css("""
                    --jui-chatpanel-bubble-user-width: 80%;
                    --jui-chatpanel-bubble-user-align: flex-end;
                    --jui-chatpanel-bubble-user-radius: 16px 4px 16px 16px;
                    --jui-chatpanel-bubble-user-padding: 12px 16px;
                    --jui-chatpanel-bubble-user-border-left: none;
                    --jui-chatpanel-bubble-user-margin: 20px 0 10px 0;
                    --jui-chatpanel-messages-gap: 5px;
                """);
            };

            /**
             * Configure config.
             */
            void configure(Config config);
        }

        private IChatPanelHost host;
        private boolean showAgentName = true;
        private String baseEndpoint = "/app/chat";

        public Config() {
            super();
        }

        public Config(IChatPanelHost host) {
            super();
            this.host = host;
        }

        /**
         * Applies a variant.
         * 
         * @param variant
         *                the variant to apply (can be null).
         * @return this for chaining.
         */
        public Config variant(Variant variant) {
            if (variant != null)
                variant.configure(this);
            return this;
        }

        public Config host(IChatPanelHost host) {
            this.host = host;
            return this;
        }

        public IChatPanelHost getHost() {
            return host;
        }

        /**
         * Whether to display the agent name above assistant responses.
         *
         * @param showAgentName
         *                      {@code true} to show (default), {@code false} to hide.
         * @return this for chaining.
         */
        public Config showAgentName(boolean showAgentName) {
            this.showAgentName = showAgentName;
            return this;
        }

        public boolean isShowAgentName() {
            return showAgentName;
        }

        /**
         * The base endpoint path for chat API calls.
         *
         * @param baseEndpoint
         *                     the base path (default {@code "/app"}).
         * @return this for chaining.
         */
        public Config baseEndpoint(String baseEndpoint) {
            if (baseEndpoint != null)
                this.baseEndpoint = baseEndpoint;
            return this;
        }

        public String getBaseEndpoint() {
            return baseEndpoint;
        }

        @Override
        @SuppressWarnings("unchecked")
        public ChatPanel build(LayoutData... data) {
            return (ChatPanel) build(new ChatPanel(this), data);
        }
    }

    /************************************************************************
     * Construction
     ************************************************************************/

    private String conversationId;
    private boolean streaming;
    private boolean hasMessages;
    private String currentConversationTitle;
    private boolean conversationPickerOpen;

    // Header elements
    private Element usageEl;
    private Element conversationTriggerEl;
    private Element conversationMenuEl;
    private Element conversationListEl;

    // Welcome elements
    private Element welcomeEl;
    private HTMLTextAreaElement welcomeInputEl;

    // Conversation elements
    private Element conversationEl;
    private Element messagesEl;
    private HTMLTextAreaElement inputEl;

    // Streaming state
    private Element currentStreamEl;
    private StringBuilder streamBuffer;
    private String currentAgentName;
    private String currentAgentIcon;
    private double thinkingTimer = -1;
    private int thinkingDots;
    private String thinkingBubbleStyle;

    // Tool activity group (accumulated per response turn)
    private HTMLElement toolGroupEl;
    private HTMLElement toolGroupListEl;
    private HTMLElement toolGroupSummaryEl;
    private int toolGroupCount;

    public ChatPanel(Config config) {
        super(config);
    }

    private IChatPanelHost host() {
        return config().getHost();
    }

    @Override
    protected INodeProvider buildNode(Element el, Config data) {
        return Wrap.$(el).$(root -> {
            Div.$(root).style(styles().panel()).$(panel -> {

                // Header bar
                Div.$(panel).style(styles().header()).$(hdr -> {
                    Div.$(hdr).by("usage").style(styles().usage()).text("Loading...");
                    Div.$(hdr).style(styles().headerActions()).$(actions -> {
                        Div.$(actions).style(styles().newChatBtn()).text("New chat").onclick(e -> startNewChat());
                        Div.$(actions).style(styles().pickerWrap()).$(picker -> {
                            Div.$(picker).by("conversationTrigger").style(styles().pickerTrigger())
                                .onclick(e -> toggleConversationPicker())
                                .$(Text.$("Prior chats"), Text.$("\u25be"));
                            Div.$(picker).by("conversationMenu").style(styles().pickerMenu())
                                .onclick(e -> {
                                    Element target = (e != null) ? e.getTarget("[data-conversation-id]", 8) : null;
                                    if (target == null)
                                        return;
                                    Object attr = target.getAttribute("data-conversation-id");
                                    String id = (attr != null) ? String.valueOf(attr) : null;
                                    if ((id != null) && !id.isBlank())
                                        selectConversation(id);
                                });
                        });
                    });
                });

                // Welcome screen
                Div.$(panel).by("welcome").style(styles().welcome()).$(welcome -> {
                    Div.$(welcome).style(styles().welcomeCard()).$(card -> {
                        P.$(card).style(styles().welcomeTitle()).text("Start a conversation");
                        P.$(card).style(styles().welcomeSubtitle()).text("Ask a question to get started.");
                        Div.$(card).style(styles().inputBox()).$(bar -> {
                            bar.textarea(2, 0, ta -> ta.by("welcomeInput")
                                .attr("placeholder", "Ask a question...")
                                .style(styles().textarea()))
                                .on(e -> handleWelcomeKey(e), UIEventType.ONKEYDOWN);
                            Div.$(bar).style(styles().inputFooter()).$(footer -> {
                                Div.$(footer).style(styles().inputHint()).text("Enter to send");
                                Div.$(footer).style(styles().sendBtn()).text("Send").onclick(e -> sendFromWelcome());
                            });
                        });
                    });
                });

                // Conversation area (messages + input)
                Div.$(panel).by("conversation").style(styles().conversation()).$(conv -> {
                    Div.$(conv).style(styles().messagesScroll()).$(scroll -> {
                        Div.$(scroll).by("messages").style(styles().messages());
                    });
                    Div.$(conv).style(styles().inputBarWrap()).$(barWrap -> {
                        Div.$(barWrap).style(styles().inputBox()).$(bar -> {
                            bar.textarea(2, 0, ta -> ta.by("input")
                                .attr("placeholder", "Ask a follow-up...")
                                .style(styles().textarea()))
                                .on(e -> handleComposerKey(e), UIEventType.ONKEYDOWN);
                            Div.$(bar).style(styles().inputFooter()).$(footer -> {
                                Div.$(footer).style(styles().inputHint()).text("Enter to send");
                                Div.$(footer).style(styles().sendBtn()).text("Send").onclick(e -> sendFromInput());
                            });
                        });
                    });
                });
            });
        }).build(tree -> {
            usageEl = tree.first("usage");
            conversationTriggerEl = tree.first("conversationTrigger");
            conversationMenuEl = tree.first("conversationMenu");
            conversationListEl = conversationMenuEl;
            welcomeEl = tree.first("welcome");
            welcomeInputEl = tree.first("welcomeInput");
            conversationEl = tree.first("conversation");
            messagesEl = tree.first("messages");
            inputEl = tree.first("input");
            welcomeEl.classList.add(styles().show());
            refreshConversationTrigger();
            refresh();
        });
    }

    /************************************************************************
     * Public API
     ************************************************************************/

    /**
     * Programmatically send a message as if the user typed it.
     */
    public void postMessage(String message) {
        if (streaming || StringSupport.empty(message))
            return;
        if (!hasMessages)
            showConversation();
        doSend(message.trim());
    }

    /**
     * Clears all messages and resets conversation state.
     */
    public void clear() {
        if (messagesEl != null)
            messagesEl.innerHTML = "";
        conversationId = null;
        streaming = false;
        hasMessages = false;
        currentStreamEl = null;
        streamBuffer = null;
        stopThinkingIndicator();
    }

    /**
     * Loads and displays an existing conversation by ID.
     */
    public void loadConversation(String id) {
        streaming = false;
        conversationId = id;
        currentStreamEl = null;
        currentAgentName = null;
        currentAgentIcon = null;
        streamBuffer = null;
        stopThinkingIndicator();
        if (messagesEl != null)
            messagesEl.innerHTML = "";
        appendNoticeMessage("Loading conversation...");

        XMLHttpRequest xhr = new XMLHttpRequest();
        xhr.open("GET", endpoint("/messages/") + id);
        xhr.onload = e -> {
            if (xhr.status != 200) {
                if (messagesEl != null)
                    messagesEl.innerHTML = "";
                appendSystemMessage("Failed to load conversation.");
                return;
            }
            JSONArray arr = JSONParser.parseStrict(xhr.responseText).isArray();
            if (arr == null) {
                if (messagesEl != null)
                    messagesEl.innerHTML = "";
                appendSystemMessage("Failed to load conversation.");
                return;
            }
            if (messagesEl != null)
                messagesEl.innerHTML = "";
            String lastAgent = null;
            for (int i = 0; i < arr.size(); i++) {
                JSONObject msg = arr.get(i).isObject();
                if (msg == null)
                    continue;
                String role = jsonString(msg, "role");
                if (role == null)
                    continue;
                role = role.toLowerCase();
                String content = jsonString(msg, "content");
                if ("assistant".equals(role)) {
                    String agentName = jsonString(msg, "agentName");
                    if (config().isShowAgentName() && !StringSupport.empty(agentName)) {
                        if (!agentName.equals(lastAgent)) {
                            appendAgentHeader(agentName);
                            lastAgent = agentName;
                        }
                    }
                    // Render recorded tool/action activity before the response.
                    renderRecordedActivity(msg);
                    if (!StringSupport.empty(content))
                        appendMessage(role, content);
                } else {
                    lastAgent = null;
                    if (!StringSupport.empty(content))
                        appendMessage(role, content);
                }
            }
            scrollToBottom();
            if (inputEl != null)
                inputEl.focus();
        };
        xhr.onerror = e -> {
            if (messagesEl != null)
                messagesEl.innerHTML = "";
            appendSystemMessage("Failed to load conversation.");
            return null;
        };
        xhr.send();
    }

    /**
     * Whether the panel is currently streaming a response.
     */
    public boolean isStreaming() {
        return streaming;
    }

    /**
     * Loads usage and conversation list. Call this when the panel becomes
     * visible or when the organisation context changes.
     */
    public void refresh() {
        // Reset to welcome state so stale data from a previous scope is cleared.
        startNewChat();
        loadUsage();
        loadConversations();
        loadScopedConversation();
    }

    /**
     * If a scope is configured, loads the most recent conversation for that scope.
     * If none exists, the welcome screen remains.
     */
    private void loadScopedConversation() {
        String scope = host().scope();
        String orgId = contextId("organisation");
        if (scope == null || orgId == null)
            return;
        XMLHttpRequest xhr = new XMLHttpRequest();
        xhr.open("GET", endpoint("/scope?organisationId=") + orgId + "&scope=" + scope);
        xhr.onload = e -> {
            if (xhr.status != 200)
                return;
            String response = xhr.responseText;
            if (response == null || response.isBlank() || "{}".equals(response.trim()))
                return;
            JSONObject obj = JSONParser.parseStrict(response).isObject();
            if (obj == null)
                return;
            String cid = jsonString(obj, "id");
            if (cid != null && !cid.isBlank()) {
                showConversation();
                loadConversation(cid);
            }
        };
        xhr.send();
    }

    /************************************************************************
     * Welcome / conversation toggle
     ************************************************************************/

    private void startNewChat() {
        if (streaming)
            return;
        conversationId = null;
        currentConversationTitle = null;
        hasMessages = false;
        setConversationPickerOpen(false);
        refreshConversationTrigger();
        if (messagesEl != null)
            messagesEl.innerHTML = "";
        stopThinkingIndicator();
        streaming = false;
        currentStreamEl = null;
        streamBuffer = null;
        if (welcomeEl != null)
            welcomeEl.classList.add(styles().show());
        if (conversationEl != null)
            conversationEl.classList.remove(styles().show());
    }

    private void showConversation() {
        hasMessages = true;
        if (welcomeEl != null)
            welcomeEl.classList.remove(styles().show());
        if (conversationEl != null)
            conversationEl.classList.add(styles().show());
    }

    private void sendFromWelcome() {
        if (streaming)
            return;
        String message = welcomeInputEl.value;
        if (StringSupport.empty(message))
            return;
        welcomeInputEl.value = "";
        showConversation();
        doSend(message.trim());
    }

    private void handleWelcomeKey(com.effacy.jui.core.client.dom.UIEvent e) {
        if ((e == null) || !"Enter".equals(e.getKeyCode()) || e.isShiftKey())
            return;
        e.preventDefault();
        sendFromWelcome();
    }

    /************************************************************************
     * Input bar
     ************************************************************************/

    private void sendFromInput() {
        if (streaming || (inputEl == null))
            return;
        String message = inputEl.value;
        if (StringSupport.empty(message))
            return;
        inputEl.value = "";
        doSend(message.trim());
    }

    private void handleComposerKey(com.effacy.jui.core.client.dom.UIEvent e) {
        if ((e == null) || !"Enter".equals(e.getKeyCode()) || e.isShiftKey())
            return;
        e.preventDefault();
        sendFromInput();
    }

    /************************************************************************
     * Sending
     ************************************************************************/

    private void doSend(String message) {
        List<ContextEntry> ctx = host().context();
        if (ctx == null || ctx.isEmpty())
            return;

        String cid = (conversationId != null) ? conversationId : host().conversationId();

        appendMessage("user", message);
        currentStreamEl = appendMessage("assistant", null);
        currentAgentName = null;
        currentAgentIcon = null;
        streamBuffer = new StringBuilder();
        startThinkingIndicator();
        streaming = true;
        if (inputEl != null)
            inputEl.disabled = true;

        JSONObject body = new JSONObject();
        body.put("message", new JSONString(message));
        if (cid != null)
            body.put("conversationId", new JSONString(cid));

        String agentRef = host().agentRef();
        if (agentRef != null)
            body.put("agentRef", new JSONString(agentRef));

        String scope = host().scope();
        if (scope != null)
            body.put("scope", new JSONString(scope));

        // Context entries.
        JSONArray ctxArr = new JSONArray();
        for (int i = 0; i < ctx.size(); i++) {
            JSONObject entry = new JSONObject();
            entry.put("type", new JSONString(ctx.get(i).type()));
            entry.put("id", new JSONString(ctx.get(i).id()));
            ctxArr.set(i, entry);
        }
        body.put("context", ctxArr);

        List<String> actions = host().actions();
        if (actions != null && !actions.isEmpty()) {
            JSONArray actionsArr = new JSONArray();
            for (int i = 0; i < actions.size(); i++)
                actionsArr.set(i, new JSONString(actions.get(i)));
            body.put("actions", actionsArr);
        }

        new SSEPostConnector()
            .onEvent((eventName, data) -> handleEvent(eventName, data))
            .onError((status, msg) -> {
                appendSystemMessage("Connection error: " + msg);
                finishStreaming();
            })
            .onComplete(() -> finishStreaming())
            .send(endpoint(""), body.toString());
    }

    /************************************************************************
     * SSE event handling
     ************************************************************************/

    private void handleEvent(String eventName, String data) {
        if ("meta".equals(eventName)) {
            JSONObject obj = JSONParser.parseStrict(data).isObject();
            if (obj == null)
                return;
            currentAgentName = jsonString(obj, "agentName");
            currentAgentIcon = jsonString(obj, "agentIcon");
            if (config().isShowAgentName() && !StringSupport.empty(currentAgentName))
                appendAgentHeader(currentAgentName);
        } else if ("text".equals(eventName)) {
            JSONObject obj = JSONParser.parseStrict(data).isObject();
            String content = (obj != null) ? jsonString(obj, "content") : null;
            if (content != null)
                appendToStream(content);
        } else if ("done".equals(eventName)) {
            JSONObject obj = JSONParser.parseStrict(data).isObject();
            if (obj != null) {
                String cid = jsonString(obj, "conversationId");
                if (cid != null) {
                    conversationId = cid;
                    host().onConversationEstablished(cid);
                }
            }
            loadConversations();
            loadUsage();
        } else if ("continue".equals(eventName)) {
            // Finalise the current text bubble (if any) and prepare for
            // the next segment. Do NOT collapse the tool group — tool and
            // action events from subsequent turns should continue
            // accumulating into the same group.
            stopThinkingIndicator();
            if ((currentStreamEl != null) && (streamBuffer != null) && (streamBuffer.length() > 0)) {
                // Render accumulated text as final markdown.
                currentStreamEl.innerHTML = "";
                MarkdownParser.parse(p -> p.partial(false), configureHandler(currentStreamEl), streamBuffer.toString());
            } else if (currentStreamEl != null) {
                // No text was streamed — remove the empty/thinking bubble.
                Element wrapper = findWrapperOf(currentStreamEl);
                if (wrapper != null)
                    wrapper.parentElement.removeChild(wrapper);
            }
            // Start a fresh assistant bubble for the next segment.
            currentStreamEl = appendMessage("assistant", null);
            streamBuffer = new StringBuilder();
            startThinkingIndicator();
        } else if ("tool".equals(eventName)) {
            JSONObject obj = JSONParser.parseStrict(data).isObject();
            if (obj != null) {
                String toolName = jsonString(obj, "toolName");
                String toolLabel = jsonString(obj, "label");
                // Skip display for client-side actions — the subsequent
                // "action" event will display them with the correct icon.
                List<String> actionNames = host().actions();
                if ((actionNames == null) || !actionNames.contains(toolName))
                    appendToolActivity(toolName, toolLabel, false);
            }
        } else if ("action".equals(eventName)) {
            JSONObject obj = JSONParser.parseStrict(data).isObject();
            if (obj != null) {
                String action = jsonString(obj, "action");
                String payload = (obj.get("payload") != null) ? obj.get("payload").toString() : null;
                if (action != null) {
                    String label = jsonString(obj, "label");
                    appendToolActivity(action, label, true);
                    host().onChatAction(action, payload);
                }
            }
        } else if ("notice".equals(eventName)) {
            JSONObject obj = JSONParser.parseStrict(data).isObject();
            stopThinkingIndicator();
            appendNoticeMessage((obj != null) ? jsonString(obj, "message") : data);
        } else if ("error".equals(eventName)) {
            JSONObject obj = JSONParser.parseStrict(data).isObject();
            appendSystemMessage("Error: " + ((obj != null) ? jsonString(obj, "message") : data));
        }
    }

    /************************************************************************
     * Message rendering
     ************************************************************************/

    private void appendAgentHeader(String agentName) {
        Element insertBefore = findStreamElWrapper();
        Element row = DomGlobal.document.createElement("div");
        row.classList.add(styles().agentHeader());
        HTMLElement icon = (HTMLElement) DomGlobal.document.createElement("i");
        icon.setAttribute("class", FontAwesome.locationCrosshairs());
        icon.classList.add(styles().agentIcon());
        row.appendChild(icon);
        HTMLElement name = (HTMLElement) DomGlobal.document.createElement("span");
        name.textContent = agentName;
        row.appendChild(name);
        if (insertBefore != null)
            messagesEl.insertBefore(row, insertBefore);
        else
            messagesEl.appendChild(row);
        scrollToBottom();
    }

    /**
     * Renders recorded tool and action calls from a recovered conversation message.
     * These are displayed as a collapsed activity group (same as live tool activity).
     */
    private void renderRecordedActivity(JSONObject msg) {
        JSONArray tools = (msg.get("toolCalls") != null) ? msg.get("toolCalls").isArray() : null;
        JSONArray actions = (msg.get("actionCalls") != null) ? msg.get("actionCalls").isArray() : null;
        int total = ((tools != null) ? tools.size() : 0) + ((actions != null) ? actions.size() : 0);
        if (total == 0)
            return;

        // Reset the tool group for this message.
        resetToolGroup();

        List<String> actionNames = host().actions();
        if (tools != null) {
            for (int j = 0; j < tools.size(); j++) {
                JSONObject tc = tools.get(j).isObject();
                if (tc == null) continue;
                String toolName = jsonString(tc, "name");
                // Skip client-side actions (same filter as live streaming).
                if ((actionNames != null) && actionNames.contains(toolName))
                    continue;
                appendToolActivity(toolName, jsonString(tc, "label"), false);
            }
        }
        if (actions != null) {
            for (int j = 0; j < actions.size(); j++) {
                JSONObject ac = actions.get(j).isObject();
                if (ac == null) continue;
                appendToolActivity(jsonString(ac, "name"), jsonString(ac, "label"), true);
            }
        }

        // Collapse the group since this is historical.
        collapseToolGroup();
    }

    private void resetToolGroup() {
        toolGroupEl = null;
        toolGroupListEl = null;
        toolGroupSummaryEl = null;
        toolGroupCount = 0;
    }

    private Element appendMessage(String role, String text) {
        boolean isUser = "user".equals(role);
        Element[] ref = { null };
        Wrap.appendInto(messagesEl, root -> {
            Div.$(root).by("bubble")
                .style(isUser ? styles().bubbleUser() : styles().bubbleAssistant())
                .text((isUser && !StringSupport.empty(text)) ? text : "");
        }, dom -> ref[0] = dom.first("bubble"));
        if (!isUser)
            ref[0].classList.add(FormattedTextStyles.styles().standard());
        if (!isUser && !StringSupport.empty(text))
            MarkdownParser.parse(p -> p.partial(false), configureHandler(ref[0]), text);
        scrollToBottom();
        return ref[0];
    }

    private void appendToStream(String text) {
        if (streamBuffer == null)
            return;
        if (currentStreamEl == null)
            currentStreamEl = appendMessage("assistant", null);
        stopThinkingIndicator();
        streamBuffer.append(text);
        currentStreamEl.innerHTML = "";
        MarkdownParser.parse(p -> p.partial(true), configureHandler(currentStreamEl), streamBuffer.toString());
        scrollToBottom();
    }

    private void appendNoticeMessage(String message) {
        Wrap.appendInto(messagesEl, root -> {
            Div.$(root).style(styles().notice()).text(message);
        });
        scrollToBottom();
    }

    private void appendToolActivity(String toolName, String displayLabel, boolean isAction) {
        stopThinkingIndicator();

        // Create the group container on first tool call in this turn.
        if (toolGroupEl == null) {
            toolGroupEl = (HTMLElement) DomGlobal.document.createElement("div");
            toolGroupEl.classList.add(styles().toolRow());

            HTMLElement details = (HTMLElement) DomGlobal.document.createElement("details");
            details.classList.add(styles().toolDetails());
            details.setAttribute("open", "");
            details.addEventListener("toggle", e -> {
                // Update chevron when user manually opens/closes.
                Element chevron = toolGroupSummaryEl != null ? toolGroupSummaryEl.querySelector("i") : null;
                if (chevron != null)
                    chevron.setAttribute("class", details.hasAttribute("open") ? FontAwesome.angleDown() + " " + styles().toolChevron() : FontAwesome.angleRight() + " " + styles().toolChevron());
            });

            toolGroupSummaryEl = (HTMLElement) DomGlobal.document.createElement("summary");
            toolGroupSummaryEl.classList.add(styles().toolSummary());
            details.appendChild(toolGroupSummaryEl);

            toolGroupListEl = (HTMLElement) DomGlobal.document.createElement("div");
            toolGroupListEl.classList.add(styles().toolList());
            details.appendChild(toolGroupListEl);

            toolGroupEl.appendChild(details);
            toolGroupCount = 0;

            // Insert before the current response bubble.
            Element insertBefore = findStreamElWrapper();
            if (insertBefore != null)
                messagesEl.insertBefore(toolGroupEl, insertBefore);
            else
                messagesEl.appendChild(toolGroupEl);
        }

        // Update summary with chevron.
        toolGroupCount++;
        updateToolGroupSummary(true);

        // Add this tool/action to the list with appropriate icon.
        HTMLElement item = (HTMLElement) DomGlobal.document.createElement("div");
        item.classList.add(styles().toolItem());
        HTMLElement icon = (HTMLElement) DomGlobal.document.createElement("i");
        icon.setAttribute("class", isAction ? FontAwesome.binoculars() : FontAwesome.fan());
        icon.classList.add(styles().toolItemIcon());
        item.appendChild(icon);
        HTMLElement labelEl = (HTMLElement) DomGlobal.document.createElement("span");
        if (!StringSupport.empty(displayLabel))
            labelEl.textContent = displayLabel;
        else if (toolName != null)
            labelEl.textContent = toolName;
        else
            labelEl.textContent = isAction ? "action" : "tool";
        item.appendChild(labelEl);
        toolGroupListEl.appendChild(item);

        // Restart thinking indicator since the LLM is still processing.
        startThinkingIndicator();
        scrollToBottom();
    }

    private void updateToolGroupSummary(boolean open) {
        if (toolGroupSummaryEl == null)
            return;
        toolGroupSummaryEl.innerHTML = "";
        HTMLElement chevron = (HTMLElement) DomGlobal.document.createElement("i");
        chevron.setAttribute("class", open ? FontAwesome.angleDown() : FontAwesome.angleRight());
        chevron.classList.add(styles().toolChevron());
        toolGroupSummaryEl.appendChild(chevron);
        HTMLElement text = (HTMLElement) DomGlobal.document.createElement("span");
        String suffix = open ? " running..." : " completed";
        text.textContent = toolGroupCount + (toolGroupCount == 1 ? " action" : " actions") + suffix;
        toolGroupSummaryEl.appendChild(text);
    }

    /**
     * Collapses the tool activity group and updates the summary to past tense,
     * then resets for the next turn.
     */
    private void collapseToolGroup() {
        if ((toolGroupEl != null) && (toolGroupSummaryEl != null)) {
            updateToolGroupSummary(false);
            Element details = toolGroupEl.querySelector("details");
            if (details != null)
                details.removeAttribute("open");
        }
        toolGroupEl = null;
        toolGroupListEl = null;
        toolGroupSummaryEl = null;
        toolGroupCount = 0;
    }

    /**
     * Finds the direct child of messagesEl that contains currentStreamEl.
     */
    private Element findStreamElWrapper() {
        return findWrapperOf(currentStreamEl);
    }

    /**
     * Finds the direct child of messagesEl that contains the given element.
     */
    private Element findWrapperOf(Element el) {
        if ((el == null) || (messagesEl == null))
            return null;
        while ((el != null) && (el.parentElement != messagesEl))
            el = el.parentElement;
        return el;
    }

    private void appendSystemMessage(String message) {
        stopThinkingIndicator();
        Element bubble = appendMessage("assistant", null);
        bubble.classList.remove(FormattedTextStyles.styles().standard());
        bubble.classList.add(styles().error());
        bubble.textContent = message;
    }

    /************************************************************************
     * Thinking indicator
     ************************************************************************/

    private void startThinkingIndicator() {
        if (currentStreamEl == null)
            return;
        thinkingDots = 0;
        thinkingBubbleStyle = currentStreamEl.getAttribute("class");
        currentStreamEl.setAttribute("class", styles().thinking());
        currentStreamEl.textContent = "Thinking.";
        thinkingTimer = DomGlobal.setInterval(ignore -> {
            if (currentStreamEl == null)
                return;
            thinkingDots = (thinkingDots + 1) % 3;
            switch (thinkingDots) {
                case 0 -> currentStreamEl.textContent = "Thinking.";
                case 1 -> currentStreamEl.textContent = "Thinking..";
                default -> currentStreamEl.textContent = "Thinking...";
            }
        }, 500);
    }

    private void stopThinkingIndicator() {
        if (thinkingTimer >= 0) {
            DomGlobal.clearInterval(thinkingTimer);
            thinkingTimer = -1;
        }
        if ((thinkingBubbleStyle != null) && (currentStreamEl != null)) {
            currentStreamEl.setAttribute("class", thinkingBubbleStyle);
            thinkingBubbleStyle = null;
        }
    }

    private void finishStreaming() {
        stopThinkingIndicator();
        if ((currentStreamEl != null) && (streamBuffer != null) && (streamBuffer.length() > 0)) {
            // Render final markdown.
            currentStreamEl.innerHTML = "";
            MarkdownParser.parse(p -> p.partial(false), configureHandler(currentStreamEl), streamBuffer.toString());
        } else if (currentStreamEl != null) {
            // No text was streamed in this final segment — remove the
            // empty/thinking bubble so it doesn't linger.
            Element wrapper = findWrapperOf(currentStreamEl);
            if (wrapper != null)
                wrapper.parentElement.removeChild(wrapper);
        }
        streaming = false;
        currentStreamEl = null;
        streamBuffer = null;
        collapseToolGroup();
        if (inputEl != null) {
            inputEl.disabled = false;
            inputEl.focus();
        }
    }

    /************************************************************************
     * Usage indicator
     ************************************************************************/

    private void loadUsage() {
        String orgId = contextId("organisation");
        if ((orgId == null) || (usageEl == null))
            return;
        XMLHttpRequest xhr = new XMLHttpRequest();
        xhr.open("GET", endpoint("/info?organisationId=") + orgId);
        xhr.onload = e -> {
            if (xhr.status != 200) {
                usageEl.textContent = "";
                return;
            }
            JSONObject obj = JSONParser.parseStrict(xhr.responseText).isObject();
            if (obj == null)
                return;
            long count = Math.round(jsonNumber(obj, "countForMonth"));
            long limit = Math.round(jsonNumber(obj, "limitForMonth"));
            if (limit <= 0) {
                usageEl.textContent = "";
                return;
            }
            usageEl.innerHTML = "";

            Element bar = DomGlobal.document.createElement("span");
            bar.classList.add(styles().usageBar());
            int segments = 8;
            int filled = filledSegments(count, limit, segments);
            for (int i = 0; i < segments; i++) {
                Element segment = DomGlobal.document.createElement("span");
                segment.classList.add(i < filled ? styles().usageSegmentFilled() : styles().usageSegmentEmpty());
                bar.appendChild(segment);
            }
            usageEl.appendChild(bar);

            Element percent = DomGlobal.document.createElement("span");
            percent.classList.add(styles().usagePercent());
            percent.textContent = formatPercentage(count, limit);
            usageEl.appendChild(percent);
        };
        xhr.send();
    }

    /************************************************************************
     * Conversation list and picker
     ************************************************************************/

    private void loadConversations() {
        String orgId = contextId("organisation");
        if ((orgId == null) || (conversationListEl == null))
            return;
        XMLHttpRequest xhr = new XMLHttpRequest();
        xhr.open("GET", endpoint("/conversations?organisationId=") + orgId);
        xhr.onload = e -> {
            conversationListEl.innerHTML = "";
            currentConversationTitle = null;
            if (xhr.status != 200) {
                refreshConversationTrigger();
                return;
            }
            JSONArray arr = JSONParser.parseStrict(xhr.responseText).isArray();
            if (arr == null) {
                refreshConversationTrigger();
                return;
            }
            if (arr.size() == 0) {
                Wrap.appendInto(conversationListEl, root -> {
                    Div.$(root).style(styles().pickerEmpty()).text("No prior chats");
                });
                refreshConversationTrigger();
                return;
            }
            for (int i = 0; i < arr.size(); i++) {
                JSONObject item = arr.get(i).isObject();
                if (item == null)
                    continue;
                String id = jsonString(item, "id");
                String title = jsonString(item, "title");
                if (id == null)
                    continue;
                String label = (title != null && !title.isBlank()) ? title : "Untitled conversation";
                if (id.equals(conversationId))
                    currentConversationTitle = label;
                boolean active = id.equals(conversationId);
                Wrap.appendInto(conversationListEl, root -> {
                    Div.$(root)
                        .attr("data-conversation-id", id)
                        .style(active ? styles().pickerItemActive() : styles().pickerItem())
                        .text(label);
                });
            }
            refreshConversationTrigger();
        };
        xhr.send();
    }

    private void selectConversation(String id) {
        if (!hasMessages)
            showConversation();
        conversationId = id;
        setConversationPickerOpen(false);
        loadConversation(id);
        loadConversations();
    }

    private void toggleConversationPicker() {
        setConversationPickerOpen(!conversationPickerOpen);
    }

    private void setConversationPickerOpen(boolean open) {
        conversationPickerOpen = open;
        if (conversationMenuEl != null) {
            if (open)
                JQuery.$(conversationMenuEl).show();
            else
                JQuery.$(conversationMenuEl).hide();
        }
    }

    private void refreshConversationTrigger() {
        if (conversationTriggerEl == null)
            return;
        String label = StringSupport.empty(currentConversationTitle) ? "Prior chats" : currentConversationTitle;
        conversationTriggerEl.innerHTML = "";
        Element text = DomGlobal.document.createElement("span");
        text.classList.add(styles().pickerTriggerLabel());
        text.textContent = label;
        conversationTriggerEl.appendChild(text);
        Element icon = DomGlobal.document.createElement("span");
        icon.classList.add(styles().pickerTriggerIcon());
        icon.textContent = conversationPickerOpen ? "\u25b4" : "\u25be";
        conversationTriggerEl.appendChild(icon);
    }

    /************************************************************************
     * Utilities
     ************************************************************************/

    /**
     * Finds the ID for the given context type, or {@code null} if not present.
     */
    private String contextId(String type) {
        List<ContextEntry> ctx = host().context();
        if (ctx == null)
            return null;
        for (ContextEntry entry : ctx) {
            if (type.equals(entry.type()))
                return entry.id();
        }
        return null;
    }

    /**
     * Builds a query string of context entries (e.g.
     * {@code "organisation=5&application=123"}).
     */
    private String contextQueryParams() {
        List<ContextEntry> ctx = host().context();
        if (ctx == null || ctx.isEmpty())
            return "";
        StringBuilder sb = new StringBuilder();
        for (ContextEntry entry : ctx) {
            if (sb.length() > 0)
                sb.append("&");
            sb.append(entry.type()).append("=").append(entry.id());
        }
        return sb.toString();
    }

    private Elemental2Builder configureHandler(Element el) {
        return new Elemental2Builder(el).semanticTags(true).semanticLists(true);
    }

    private void scrollToBottom() {
        DomGlobal.setTimeout(ignore -> {
            if ((messagesEl != null) && (messagesEl.parentElement != null)) {
                HTMLElement scrollContainer = (HTMLElement) messagesEl.parentElement;
                scrollContainer.scrollTop = scrollContainer.scrollHeight;
            }
        }, 10);
    }

    private String formatPercentage(long count, long limit) {
        if (limit <= 0)
            return "0%";
        double percentage = (count * 100d) / limit;
        if (Math.abs(percentage - Math.round(percentage)) < 0.05d)
            return Math.round(percentage) + "%";
        return (Math.round(percentage * 10d) / 10d) + "%";
    }

    private int filledSegments(long count, long limit, int segments) {
        if ((limit <= 0) || (segments <= 0))
            return 0;
        double ratio = Math.max(0d, Math.min(1d, count / (double) limit));
        return (int) Math.round(ratio * segments);
    }

    /**
     * Resolves a chat endpoint path using the configured base endpoint.
     */
    private String endpoint(String path) {
        return config().getBaseEndpoint() + path;
    }

    private String jsonString(JSONObject obj, String key) {
        if ((obj == null) || (obj.get(key) == null) || (obj.get(key).isString() == null))
            return null;
        return obj.get(key).isString().stringValue();
    }

    private double jsonNumber(JSONObject obj, String key) {
        if ((obj == null) || (obj.get(key) == null) || (obj.get(key).isNumber() == null))
            return 0;
        return obj.get(key).isNumber().doubleValue();
    }

    /************************************************************************
     * CSS
     ************************************************************************/

    @Override
    protected ILocalCSS styles() {
        return LocalCSS.instance();
    }

    public static interface ILocalCSS extends IComponentCSS {

        /* Layout */
        String panel();
        String show();

        /* Header */
        String header();
        String usage();
        String headerActions();
        String newChatBtn();
        String pickerWrap();
        String pickerTrigger();
        String pickerTriggerLabel();
        String pickerTriggerIcon();
        String pickerMenu();
        String pickerEmpty();
        String pickerItem();
        String pickerItemActive();

        /* Welcome */
        String welcome();
        String welcomeCard();
        String welcomeTitle();
        String welcomeSubtitle();

        /* Conversation */
        String conversation();
        String messagesScroll();
        String messages();
        String inputBarWrap();
        String inputBox();
        String textarea();
        String inputFooter();
        String inputHint();
        String sendBtn();

        /* Agent header */
        String agentHeader();
        String agentIcon();

        /* Bubbles */
        String bubbleUser();
        String bubbleAssistant();
        String notice();
        String error();
        String thinking();

        /* Tool activity */
        String toolRow();
        String toolDetails();
        String toolSummary();
        String toolChevron();
        String toolList();
        String toolItem();
        String toolItemIcon();

        /* Usage indicator */
        String usageBar();
        String usageSegmentFilled();
        String usageSegmentEmpty();
        String usagePercent();
    }

    @CssResource(value = IComponentCSS.COMPONENT_CSS, stylesheet = """
        .component {
            /* Core colours */
            --jui-chatpanel-color-primary: #173327;
            --jui-chatpanel-color-bg: #fff;
            --jui-chatpanel-color-bg-muted: #fbfaf7;
            --jui-chatpanel-color-border: #d8d2c6;
            --jui-chatpanel-color-text: #1d2c24;
            --jui-chatpanel-color-text-muted: #5b665e;
            --jui-chatpanel-color-text-hint: #889188;
            --jui-chatpanel-color-notice-bg: #fef3c7;
            --jui-chatpanel-color-notice-text: #92400e;
            --jui-chatpanel-color-error-bg: #fdf2f2;
            --jui-chatpanel-color-error-text: #7f1d1d;
            --jui-chatpanel-color-error-border: #efc1c1;

            /* User message bubble */
            --jui-chatpanel-bubble-user-width: 100%;
            --jui-chatpanel-bubble-user-align: stretch;
            --jui-chatpanel-bubble-user-radius: 4px 8px 8px 4px;
            --jui-chatpanel-bubble-user-bg: var(--jui-chatpanel-color-bg-muted);
            --jui-chatpanel-bubble-user-color: var(--jui-chatpanel-color-text);
            --jui-chatpanel-bubble-user-padding: 10px 14px;
            --jui-chatpanel-bubble-user-margin: 2px 0 10px 0;
            --jui-chatpanel-bubble-user-border-left: 3px solid var(--jui-chatpanel-color-primary);
            --jui-chatpanel-bubble-user-border-right: none;
            --jui-chatpanel-bubble-user-border-top: none;
            --jui-chatpanel-bubble-user-border-bottom: none;

            /* Tool/action activity */
            --jui-chatpanel-tool-bg: #f5f5f5;
            --jui-chatpanel-tool-color: #6b7280;
            --jui-chatpanel-tool-font-size: 0.85rem;
            --jui-chatpanel-tool-item-font-size: 0.8rem;

            /* Content offset (shared by agent header, assistant bubble, tool activity) */
            --jui-chatpanel-content-offset: 14px;
            --jui-chatpanel-icon-left: -5px;
            --jui-chatpanel-messages-gap: 10px;

            /* Agent header */
            --jui-chatpanel-agent-color: var(--jui-chatpanel-color-text);
            --jui-chatpanel-agent-font-size: 0.9rem;
            --jui-chatpanel-agent-icon-color: var(--jui-chatpanel-color-primary);
            --jui-chatpanel-agent-icon-font-size: 1em;

            overflow: auto;
            height: 100%;
        }
        .component .show {
            display: flex !important;
        }
        .component .panel {
            display: flex;
            flex-direction: column;
            height: 100%;
            overflow: hidden;
            overflow: auto;
            background: var(--jui-chatpanel-color-bg);
        }

        /* Header */
        .component .header {
            padding: 10px 14px;
            border-bottom: 1px solid var(--jui-chatpanel-color-border);
            background: var(--jui-chatpanel-color-bg);
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 10px;
            flex-shrink: 0;
        }
        .component .usage {
            font-size: 0.75rem;
            color: var(--jui-chatpanel-color-text-muted);
            min-width: 0;
            overflow: hidden;
        }
        .component .headerActions {
            display: flex;
            align-items: center;
            gap: 8px;
            min-width: 0;
        }
        .component .newChatBtn {
            padding: 6px 10px;
            background: var(--jui-chatpanel-color-primary);
            color: var(--jui-chatpanel-color-bg);
            border-radius: 8px;
            cursor: pointer;
            font-size: 0.75rem;
            font-weight: 600;
            white-space: nowrap;
        }
        .component .pickerWrap {
            position: relative;
        }
        .component .pickerTrigger {
            max-width: 180px;
            padding: 6px 10px;
            border: 1px solid var(--jui-chatpanel-color-border);
            border-radius: 8px;
            background: var(--jui-chatpanel-color-bg-muted);
            color: var(--jui-chatpanel-color-text);
            cursor: pointer;
            font-size: 0.75rem;
            display: flex;
            align-items: center;
            gap: 6px;
            white-space: nowrap;
            overflow: hidden;
        }
        .component .pickerTriggerLabel {
            flex: 1;
            min-width: 0;
            overflow: hidden;
            text-overflow: ellipsis;
        }
        .component .pickerTriggerIcon {
            flex-shrink: 0;
        }
        .component .pickerMenu {
            display: none;
            position: absolute;
            right: 0;
            top: calc(100% + 6px);
            width: min(320px, 60vw);
            max-height: 280px;
            overflow-y: auto;
            background: var(--jui-chatpanel-color-bg);
            border: 1px solid var(--jui-chatpanel-color-border);
            border-radius: 12px;
            box-shadow: 0 12px 28px rgba(0,0,0,0.12);
            padding: 6px;
            z-index: 20;
        }
        .component .pickerEmpty {
            padding: 8px 10px;
            color: var(--jui-chatpanel-color-text-hint);
            font-size: 0.688rem;
        }
        .component .pickerItem {
            padding: 8px 10px;
            border-radius: 8px;
            background: transparent;
            color: var(--jui-chatpanel-color-text);
            cursor: pointer;
            font-size: 0.75rem;
            line-height: 1.4;
        }
        .component .pickerItemActive {
            padding: 8px 10px;
            border-radius: 8px;
            background: var(--jui-chatpanel-color-bg-muted);
            color: var(--jui-chatpanel-color-text);
            cursor: pointer;
            font-size: 0.75rem;
            line-height: 1.4;
        }

        /* Welcome */
        .component .welcome {
            display: none;
            flex: 1;
            align-items: center;
            justify-content: center;
            padding: 24px;
            overflow-y: auto;
        }
        .component .welcomeCard {
            width: 100%;
            max-width: 800px;
            background: var(--jui-chatpanel-color-bg);
            border: 1px solid var(--jui-chatpanel-color-border);
            border-radius: 16px;
            padding: 24px;
            box-shadow: 0 12px 36px rgba(0,0,0,0.06);
        }
        .component .welcomeTitle {
            margin: 0 0 8px 0;
            font-size: 1.125rem;
            font-weight: 600;
            color: var(--jui-chatpanel-color-primary);
        }
        .component .welcomeSubtitle {
            margin: 0 0 16px 0;
            color: var(--jui-chatpanel-color-text-muted);
            font-size: 0.813rem;
            line-height: 1.5;
        }

        /* Conversation */
        .component .conversation {
            display: none;
            flex: 1;
            flex-direction: column;
            overflow: hidden;
        }
        .component .messagesScroll {
            flex: 1;
            overflow-y: auto;
        }
        .component .messages {
            padding: 16px;
            display: flex;
            flex-direction: column;
            gap: var(--jui-chatpanel-messages-gap);
        }
        .component .inputBarWrap {
            padding: 0 12px 12px 12px;
            flex-shrink: 0;
        }

        /* Shared input box */
        .component .inputBox {
            display: flex;
            flex-direction: column;
            gap: 6px;
            background: var(--jui-chatpanel-color-bg);
            border: 1px solid var(--jui-chatpanel-color-border);
            border-radius: 14px;
            padding: 10px 12px;
            box-shadow: 0 4px 16px rgba(0,0,0,0.04);
        }
        .component .welcome .inputBox {
            border-radius: 12px;
            padding: 10px;
            background: var(--jui-chatpanel-color-bg-muted);
            box-shadow: none;
        }
        .component .textarea {
            width: 100%;
            min-height: 44px;
            resize: vertical;
            border: 0;
            background: transparent;
            padding: 2px;
            font-size: 0.813rem;
            line-height: 1.5;
            color: var(--jui-chatpanel-color-text);
            outline: none;
        }
        .component .welcome .textarea {
            min-height: 52px;
            padding: 4px 2px;
        }
        .component .inputFooter {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 10px;
        }
        .component .inputHint {
            font-size: 0.688rem;
            color: var(--jui-chatpanel-color-text-hint);
        }
        .component .sendBtn {
            padding: 6px 12px;
            background: var(--jui-chatpanel-color-primary);
            color: var(--jui-chatpanel-color-bg);
            border-radius: 8px;
            cursor: pointer;
            font-size: 0.75rem;
            font-weight: 600;
        }

        /* Agent header */
        .component .agentHeader {
            position: relative;
            padding: 4px 0 2px var(--jui-chatpanel-content-offset);
            font-size: var(--jui-chatpanel-agent-font-size);
            font-weight: 600;
            color: var(--jui-chatpanel-agent-color);
        }
        .component .agentIcon {
            position: absolute;
            left: var(--jui-chatpanel-icon-left);
            top: 5px;
            font-size: var(--jui-chatpanel-agent-icon-font-size);
            color: var(--jui-chatpanel-agent-icon-color);
            top: 1em;
            margin-top: -0.4em;
        }

        /* Message bubbles */
        .component .bubbleUser {
            width: var(--jui-chatpanel-bubble-user-width);
            box-sizing: border-box;
            word-wrap: break-word;
            align-self: var(--jui-chatpanel-bubble-user-align);
            margin: var(--jui-chatpanel-bubble-user-margin);
            padding: var(--jui-chatpanel-bubble-user-padding);
            border-radius: var(--jui-chatpanel-bubble-user-radius);
            border-left: var(--jui-chatpanel-bubble-user-border-left);
            border-right: var(--jui-chatpanel-bubble-user-border-right);
            border-top: var(--jui-chatpanel-bubble-user-border-top);
            border-bottom: var(--jui-chatpanel-bubble-user-border-bottom);
            background: var(--jui-chatpanel-bubble-user-bg);
            color: var(--jui-chatpanel-bubble-user-color);
            font-size: 0.813rem;
            line-height: 1.55;
        }
        .component .bubbleAssistant {
            width: 100%;
            box-sizing: border-box;
            word-wrap: break-word;
            align-self: stretch;
            margin: 0;
            padding: 0 0 0 var(--jui-chatpanel-content-offset);
            background: transparent;
            color: var(--jui-chatpanel-color-text);
            font-size: 0.813rem;
            line-height: 1.65;
        }
        .component .bubbleAssistant p.block {
            margin: 0;
        }
        .component .notice {
            align-self: center;
            padding: 8px 14px;
            border-radius: 8px;
            background: var(--jui-chatpanel-color-notice-bg);
            color: var(--jui-chatpanel-color-notice-text);
            font-size: 0.75rem;
        }
        .component .error {
            background: var(--jui-chatpanel-color-error-bg);
            color: var(--jui-chatpanel-color-error-text);
            border: 1px solid var(--jui-chatpanel-color-error-border);
        }
        .component .thinking {
            align-self: flex-start;
            font-size: 0.813rem;
            line-height: 1.5;
            color: var(--jui-chatpanel-color-text-hint);
            font-style: italic;
        }

        /* Tool activity */
        .component .toolRow {
            align-self: stretch;
            padding: 6px 14px;
            margin: 2px 0 2px var(--jui-chatpanel-content-offset);
            font-size: var(--jui-chatpanel-tool-font-size);
            color: var(--jui-chatpanel-tool-color);
            background: var(--jui-chatpanel-tool-bg);
            border-radius: 8px;
        }
        .component .toolDetails {
            padding: 0;
        }
        .component .toolSummary {
            cursor: pointer;
            list-style: none;
            font-weight: 500;
            padding: 2px 0;
            display: flex;
            align-items: center;
            gap: 6px;
        }
        .component .toolSummary::-webkit-details-marker {
            display: none;
        }
        .component .toolChevron {
            font-size: 0.625rem;
            width: 12px;
            text-align: center;
            flex-shrink: 0;
        }
        .component .toolList {
            padding: 4px 0 2px 18px;
        }
        .component .toolItem {
            padding: 2px 0;
            font-size: var(--jui-chatpanel-tool-item-font-size);
            color: var(--jui-chatpanel-tool-color);
            display: flex;
            align-items: center;
            gap: 6px;
        }
        .component .toolItemIcon {
            font-size: 0.625rem;
            width: 14px;
            text-align: center;
            flex-shrink: 0;
        }

        /* Usage indicator */
        .component .usageBar {
            display: inline-flex;
            align-items: center;
            gap: 1px;
            margin: 0 6px 0 0;
            vertical-align: middle;
        }
        .component .usageSegmentFilled {
            display: inline-block;
            width: 6px;
            height: 8px;
            border-radius: 1px;
            background: var(--jui-chatpanel-color-primary);
        }
        .component .usageSegmentEmpty {
            display: inline-block;
            width: 6px;
            height: 8px;
            border-radius: 1px;
            background: var(--jui-chatpanel-color-border);
        }
        .component .usagePercent {
            font-weight: 600;
            color: var(--jui-chatpanel-color-text);
        }
    """)
    public static abstract class LocalCSS implements ILocalCSS {

        private static LocalCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (LocalCSS) GWT.create(LocalCSS.class);
                STYLES.ensureInjected();
            }
            return STYLES;
        }
    }
}

