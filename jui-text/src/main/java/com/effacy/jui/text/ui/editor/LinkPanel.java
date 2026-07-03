package com.effacy.jui.text.ui.editor;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.Button;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.core.client.dom.builder.Input;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.text.ui.editor.LinkPanel.AnchorItem;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;
import elemental2.dom.HTMLInputElement;
import jsinterop.base.Js;

/**
 * A lightweight floating panel for entering or editing a link URL.
 * <p>
 * Optionally accepts a {@link Function} that provides {@link AnchorItem}
 * suggestions based on the user's input. When suggestions are available, items
 * are displayed in a selectable list below the input. When no suggestions
 * match, a hint row instructs the user to type a complete URL.
 * <p>
 * Extends {@link ToolPopupPanel} for positioning, dismiss, and singleton
 * tracking. At most one popup panel is visible at a time.
 */
public class LinkPanel extends ToolPopupPanel {

    /************************************************************************
     * Types.
     ************************************************************************/

    /**
     * A selectable link suggestion. An item with a {@code null} url is
     * <em>informational</em>: it is rendered as a non-selectable note (e.g. "no
     * matches for …") rather than a pickable suggestion.
     */
    public record AnchorItem(String label, String url) {

        /** Convenience for an informational (non-selectable) note row. */
        public static AnchorItem note(String label) {
            return new AnchorItem(label, null);
        }
    }

    /**
     * An asynchronous provider of link suggestions: given the typed text, supply the
     * matching items to the consumer (e.g. from a remote search). Responses arriving
     * for a superseded query are discarded by the panel.
     */
    @FunctionalInterface
    public interface IAnchorSource {

        /**
         * Queries for suggestions matching the typed text.
         *
         * @param text
         *                the typed text (may be empty — suppliers may return defaults).
         * @param results
         *                accepts the matching items when available.
         */
        void query(String text, Consumer<List<AnchorItem>> results);
    }

    /**
     * Callback for link panel actions.
     */
    public interface ILinkPanelCallback {

        /**
         * Called when the user applies a URL.
         */
        void onApply(String url);

        /**
         * As {@link #onApply(String)} with the display label of a picked suggestion
         * ({@code null} for a manually entered URL). Defaults to delegating to
         * {@link #onApply(String)}.
         */
        default void onApply(String url, String label) {
            onApply(url);
        }

        /**
         * Called when the user removes the existing link.
         */
        void onRemove();
    }

    /************************************************************************
     * Show.
     ************************************************************************/

    /**
     * Shows the link panel below {@code anchor} without suggestions.
     */
    public static void show(Element anchor, String currentUrl, ILinkPanelCallback callback) {
        show(anchor, currentUrl, (IAnchorSource) null, callback);
    }

    /**
     * Shows the link panel below {@code anchor}.
     *
     * @param anchor
     *               the element to anchor the panel to.
     * @param currentUrl
     *               the current link URL (or {@code null} if no link exists).
     * @param options
     *               optional function returning suggestions for the typed text
     *               (or {@code null} for no suggestions).
     * @param callback
     *               the callback for apply/remove actions.
     */
    public static void show(Element anchor, String currentUrl, Function<String, List<AnchorItem>> options, ILinkPanelCallback callback) {
        show(anchor, currentUrl, (options == null) ? (IAnchorSource) null
            : (text, results) -> results.accept(options.apply(text)), callback);
    }

    /**
     * Shows the link panel below {@code anchor} with an asynchronous suggestion source
     * (e.g. a remote search).
     *
     * @param anchor
     *               the element to anchor the panel to.
     * @param currentUrl
     *               the current link URL (or {@code null} if no link exists).
     * @param source
     *               optional asynchronous supplier of suggestions for the typed text
     *               (or {@code null} for no suggestions).
     * @param callback
     *               the callback for apply/remove actions.
     */
    public static void show(Element anchor, String currentUrl, IAnchorSource source, ILinkPanelCallback callback) {
        show(anchor, currentUrl, source, 0, callback);
    }

    /**
     * As {@link #show(Element, String, IAnchorSource, ILinkPanelCallback)} but with a
     * fixed panel width.
     *
     * @param width
     *               the panel width in pixels ({@code <= 0} falls back to
     *               {@link #DEFAULT_WIDTH}, and failing that the panel is content-sized).
     */
    public static void show(Element anchor, String currentUrl, IAnchorSource source, int width, ILinkPanelCallback callback) {
        LinkPanel panel = new LinkPanel(currentUrl, source, callback);
        panel.width = (width > 0) ? width : DEFAULT_WIDTH;
        panel.show(anchor);
    }

    /************************************************************************
     * Instance state.
     ************************************************************************/

    /**
     * The default fixed width (px) for the panel. When {@code 0} (and no width is
     * passed to {@link #show(Element, String, IAnchorSource, int, ILinkPanelCallback)})
     * the panel is content-sized. Assign once at start-up to fix the width
     * application-wide.
     */
    public static int DEFAULT_WIDTH = 0;

    private String currentUrl;
    private IAnchorSource options;
    private ILinkPanelCallback callback;
    private HTMLInputElement input;
    private Element suggestionListEl;
    private List<AnchorItem> currentItems;
    /** The fixed panel width in pixels ({@code <= 0} = content-sized). */
    private int width = DEFAULT_WIDTH;
    /** Discards suggestion responses that arrive for a superseded query. */
    private int querySeq;

    private LinkPanel(String currentUrl, IAnchorSource options, ILinkPanelCallback callback) {
        this.currentUrl = currentUrl;
        this.options = options;
        this.callback = callback;
    }

    /************************************************************************
     * ToolPopupPanel.
     ************************************************************************/

    @Override
    protected void buildContent(ElementBuilder root) {
        root.style(styles().linkPanel())
            .style(options != null, styles().linkPanelVertical());
        if (width > 0)
            root.css("width", width + "px");
        if (options != null) {
            // Vertical layout: input row with optional Remove button, then suggestion list.
            Div.$(root).style(styles().inputRow()).$(inputRow -> {
                buildInputField(inputRow);
                if ((currentUrl != null) && !currentUrl.isEmpty())
                    buildRemoveButton(inputRow);
            });
            Div.$(root).style(styles().suggestionList())
                .use(n -> suggestionListEl = (Element) n)
                .on(e -> {
                    Element target = e.getTarget();
                    while ((target != null) && (target != suggestionListEl)) {
                        String idx = target.getAttribute("data-idx");
                        if ((idx != null) && !idx.isEmpty()) {
                            int i = Integer.parseInt(idx);
                            if ((currentItems != null) && (i >= 0) && (i < currentItems.size())
                                    && (currentItems.get(i).url() != null)) {
                                e.stopEvent();
                                hide();
                                callback.onApply(currentItems.get(i).url(), currentItems.get(i).label());
                            }
                            return;
                        }
                        target = target.parentElement;
                    }
                }, UIEventType.ONMOUSEDOWN);
        } else {
            // Horizontal layout: input + Apply + Remove inline.
            buildInputField(root);
            Button.$(root).style(styles().linkBtn()).text("Apply")
                .on(e -> {
                    e.stopEvent();
                    String url = input.value.trim();
                    if (!url.isEmpty()) {
                        hide();
                        callback.onApply(url);
                    }
                }, UIEventType.ONMOUSEDOWN);
            if ((currentUrl != null) && !currentUrl.isEmpty())
                buildRemoveButton(root);
        }
    }

    @Override
    protected void onShown() {
        if (input != null)
            input.focus();
        if (options != null) {
            input.addEventListener("input", evt -> updateSuggestions(input.value.trim()));
            updateSuggestions(input.value.trim());
        }
    }

    /************************************************************************
     * Helpers.
     ************************************************************************/

    private void buildInputField(IDomInsertableContainer<?> parent) {
        Input.$(parent, "text")
            .style(styles().linkInput())
            .attr("placeholder", "Enter URL...")
            .use(n -> {
                input = Js.uncheckedCast(n);
                if ((currentUrl != null) && !currentUrl.isEmpty())
                    input.value = currentUrl;
            })
            .on(e -> {
                if ("Enter".equals(e.getKey())) {
                    e.stopEvent();
                    String url = input.value.trim();
                    if (!url.isEmpty()) {
                        hide();
                        callback.onApply(url);
                    }
                }
                if ("Escape".equals(e.getKey())) {
                    e.stopEvent();
                    hide();
                }
            }, UIEventType.ONKEYDOWN);
    }

    private void buildRemoveButton(IDomInsertableContainer<?> parent) {
        Button.$(parent).style(styles().linkBtn(), styles().linkBtnDanger()).text("Remove")
            .on(e -> {
                e.stopEvent();
                hide();
                callback.onRemove();
            }, UIEventType.ONMOUSEDOWN);
    }

    private void updateSuggestions(String text) {
        final int seq = ++querySeq;
        options.query(text, items -> {
            // A newer query has superseded this response (async sources).
            if (seq != querySeq)
                return;
            currentItems = items;
            Wrap.buildInto(suggestionListEl, root -> {
                if ((currentItems != null) && !currentItems.isEmpty()) {
                    for (int i = 0; i < currentItems.size(); i++) {
                        AnchorItem item = currentItems.get(i);
                        if (item.url() == null) {
                            // Informational note — not selectable.
                            Div.$(root).style(styles().hintItem()).text(item.label());
                            continue;
                        }
                        Div.$(root).style(styles().suggestionItem())
                            .attr("data-idx", String.valueOf(i))
                            .text(item.label());
                    }
                } else {
                    Div.$(root).style(styles().hintItem()).$(hint -> {
                        Span.$(hint).style(styles().hintIcon()).text("\u2295");
                        Div.$(hint).$(info -> {
                            Div.$(info).style(styles().hintLabel())
                                .text(text.isEmpty() ? "..." : text);
                            Div.$(info).style(styles().hintDesc())
                                .text("Type a complete URL to link");
                        });
                    });
                }
            });
        });
    }

    /************************************************************************
     * CSS.
     ************************************************************************/

    protected ILinkPanelCSS styles() {
        return LinkPanelCSS.instance();
    }

    public static interface ILinkPanelCSS extends IComponentCSS {

        String linkPanel();

        String linkPanelVertical();

        String linkInput();

        String linkBtn();

        String linkBtnDanger();

        String inputRow();

        String suggestionList();

        String suggestionItem();

        String hintItem();

        String hintIcon();

        String hintLabel();

        String hintDesc();
    }

    @CssResource(value = {
        IComponentCSS.COMPONENT_CSS
    }, stylesheet = """
        .linkPanel {
            position: fixed;
            background: #fff;
            border: 1px solid #e5e7eb;
            border-radius: 6px;
            box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12), 0 1px 4px rgba(0, 0, 0, 0.06);
            padding: 8px;
            z-index: 10000;
            display: flex;
            gap: 6px;
            align-items: center;
            font-size: 0.875em;
        }
        .linkPanelVertical {
            flex-direction: column;
            align-items: stretch;
            min-width: 280px;
        }
        .linkInput {
            border: 1px solid #d1d5db;
            border-radius: 4px;
            padding: 4px 8px;
            outline: none;
            min-width: 220px;
            font-size: inherit;
        }
        .linkInput:focus {
            border-color: #3b82f6;
        }
        .linkBtn {
            border: none;
            background: #3b82f6;
            color: #fff;
            padding: 4px 10px;
            border-radius: 4px;
            cursor: pointer;
            font-size: inherit;
            white-space: nowrap;
        }
        .linkBtn:hover {
            background: #2563eb;
        }
        .linkBtnDanger {
            background: #ef4444;
        }
        .linkBtnDanger:hover {
            background: #dc2626;
        }
        .inputRow {
            display: flex;
            gap: 6px;
            align-items: center;
        }
        .inputRow .linkInput {
            flex: 1;
            min-width: 0;
        }
        .suggestionList {
            display: flex;
            flex-direction: column;
        }
        .suggestionItem {
            padding: 6px 8px;
            cursor: pointer;
            border-radius: 4px;
        }
        .suggestionItem:hover {
            background: #eff6ff;
            color: #1d4ed8;
        }
        .hintItem {
            padding: 6px 8px;
            display: flex;
            gap: 8px;
            align-items: center;
            color: #9ca3af;
        }
        .hintIcon {
            font-size: 1.1em;
        }
        .hintLabel {
            font-weight: 500;
            color: #374151;
        }
        .hintDesc {
            font-size: 0.85em;
            color: #9ca3af;
        }
    """)
    public static abstract class LinkPanelCSS implements ILinkPanelCSS {

        private static LinkPanelCSS STYLES;

        public static ILinkPanelCSS instance() {
            if (STYLES == null) {
                STYLES = (LinkPanelCSS) GWT.create(LinkPanelCSS.class);
                STYLES.ensureInjected();
            }
            return STYLES;
        }
    }
}
