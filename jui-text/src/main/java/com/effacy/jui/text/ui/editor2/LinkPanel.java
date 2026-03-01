package com.effacy.jui.text.ui.editor2;

import java.util.List;
import java.util.function.Function;

import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.platform.css.client.CssResource;
import com.google.gwt.core.client.GWT;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.KeyboardEvent;
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
     * A selectable link suggestion.
     */
    public record AnchorItem(String label, String url) {}

    /**
     * Callback for link panel actions.
     */
    public interface ILinkPanelCallback {

        /**
         * Called when the user applies a URL.
         */
        void onApply(String url);

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
        show(anchor, currentUrl, null, callback);
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
        LinkPanel panel = new LinkPanel(currentUrl, options, callback);
        panel.show(anchor);
    }

    /************************************************************************
     * Instance state.
     ************************************************************************/

    private String currentUrl;
    private Function<String, List<AnchorItem>> options;
    private ILinkPanelCallback callback;
    private HTMLInputElement input;

    private LinkPanel(String currentUrl, Function<String, List<AnchorItem>> options, ILinkPanelCallback callback) {
        this.currentUrl = currentUrl;
        this.options = options;
        this.callback = callback;
    }

    /************************************************************************
     * ToolPopupPanel.
     ************************************************************************/

    @Override
    protected void buildContent(Element panel) {
        panel.classList.add(styles().linkPanel());
        if (options != null)
            panel.classList.add(styles().linkPanelVertical());

        // URL input.
        input = Js.uncheckedCast(DomGlobal.document.createElement("input"));
        input.type = "text";
        input.placeholder = "Enter URL...";
        input.classList.add(styles().linkInput());
        if ((currentUrl != null) && !currentUrl.isEmpty())
            input.value = currentUrl;

        if (options != null) {
            // Vertical layout: input row with optional Remove button, then suggestion list.
            Element inputRow = DomGlobal.document.createElement("div");
            inputRow.classList.add(styles().inputRow());
            inputRow.appendChild(input);

            // Remove button (only when editing an existing link).
            if ((currentUrl != null) && !currentUrl.isEmpty())
                inputRow.appendChild(buildRemoveButton());

            panel.appendChild(inputRow);

            // Suggestion list container.
            Element suggestionList = DomGlobal.document.createElement("div");
            suggestionList.classList.add(styles().suggestionList());
            panel.appendChild(suggestionList);

            // Wire up input event for filtering.
            input.addEventListener("input", evt -> {
                updateSuggestions(suggestionList, input.value.trim());
            });

            // Show initial suggestions.
            updateSuggestions(suggestionList, input.value.trim());
        } else {
            // Original horizontal layout: input + Apply + Remove inline.
            panel.appendChild(input);

            // Apply button.
            Element applyBtn = DomGlobal.document.createElement("button");
            applyBtn.classList.add(styles().linkBtn());
            applyBtn.textContent = "Apply";
            applyBtn.addEventListener("mousedown", evt -> {
                evt.preventDefault();
                evt.stopPropagation();
                String url = input.value.trim();
                if (!url.isEmpty()) {
                    hide();
                    callback.onApply(url);
                }
            });
            panel.appendChild(applyBtn);

            // Remove button (only when editing an existing link).
            if ((currentUrl != null) && !currentUrl.isEmpty())
                panel.appendChild(buildRemoveButton());
        }

        // Keyboard handling on input.
        input.addEventListener("keydown", evt -> {
            KeyboardEvent ke = Js.uncheckedCast(evt);
            if ("Enter".equals(ke.key)) {
                evt.preventDefault();
                String url = input.value.trim();
                if (!url.isEmpty()) {
                    hide();
                    callback.onApply(url);
                }
            } else if ("Escape".equals(ke.key)) {
                evt.preventDefault();
                hide();
            }
        });
    }

    @Override
    protected void onShown() {
        if (input != null)
            input.focus();
    }

    /************************************************************************
     * Helpers.
     ************************************************************************/

    private Element buildRemoveButton() {
        Element removeBtn = DomGlobal.document.createElement("button");
        removeBtn.classList.add(styles().linkBtn());
        removeBtn.classList.add(styles().linkBtnDanger());
        removeBtn.textContent = "Remove";
        removeBtn.addEventListener("mousedown", evt -> {
            evt.preventDefault();
            evt.stopPropagation();
            hide();
            callback.onRemove();
        });
        return removeBtn;
    }

    private void updateSuggestions(Element container, String text) {
        container.innerHTML = "";
        List<AnchorItem> items = options.apply(text);
        if ((items != null) && !items.isEmpty()) {
            for (AnchorItem item : items) {
                Element row = DomGlobal.document.createElement("div");
                row.classList.add(styles().suggestionItem());
                row.textContent = item.label();
                row.addEventListener("mousedown", evt -> {
                    evt.preventDefault();
                    evt.stopPropagation();
                    hide();
                    callback.onApply(item.url());
                });
                container.appendChild(row);
            }
        } else {
            // No matches — show hint row.
            Element hint = DomGlobal.document.createElement("div");
            hint.classList.add(styles().hintItem());

            Element icon = DomGlobal.document.createElement("span");
            icon.classList.add(styles().hintIcon());
            icon.textContent = "\u2295";
            hint.appendChild(icon);

            Element info = DomGlobal.document.createElement("div");

            Element label = DomGlobal.document.createElement("div");
            label.classList.add(styles().hintLabel());
            label.textContent = text.isEmpty() ? "..." : text;
            info.appendChild(label);

            Element desc = DomGlobal.document.createElement("div");
            desc.classList.add(styles().hintDesc());
            desc.textContent = "Type a complete URL to link";
            info.appendChild(desc);

            hint.appendChild(info);
            container.appendChild(hint);
        }
    }

    /************************************************************************
     * CSS.
     ************************************************************************/

    private static ILinkPanelCSS styles() {
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
