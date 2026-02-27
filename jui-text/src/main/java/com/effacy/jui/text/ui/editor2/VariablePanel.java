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
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;

/**
 * A lightweight floating panel for selecting a variable to insert.
 * <p>
 * Accepts a {@link Function} that provides {@link VariableItem} suggestions
 * based on the user's input. Items are displayed in a selectable list below
 * the search input. When no suggestions match, a hint row is shown.
 * <p>
 * At most one {@code VariablePanel} is visible at a time; showing a new panel
 * automatically hides the previously open one. The panel dismisses itself when
 * the user clicks outside it.
 */
public class VariablePanel {

    /************************************************************************
     * Types.
     ************************************************************************/

    /**
     * A selectable variable suggestion.
     */
    public record VariableItem(String label, String name) {}

    /**
     * Callback for variable panel actions.
     */
    public interface IVariablePanelCallback {

        /**
         * Called when the user selects a variable.
         *
         * @param name
         *              the variable identifier.
         * @param label
         *              the display label.
         */
        void onSelect(String name, String label);
    }

    /************************************************************************
     * Static single-instance tracking (at most one panel open at a time).
     ************************************************************************/

    private static VariablePanel currentPanel;

    /**
     * Hides the currently visible panel if any.
     */
    public static void hideCurrent() {
        if (currentPanel != null)
            currentPanel.hide();
    }

    /************************************************************************
     * Show / hide.
     ************************************************************************/

    /**
     * Shows the variable panel below {@code anchor}.
     *
     * @param anchor
     *               the element to anchor the panel to.
     * @param options
     *               function returning variable suggestions for the typed
     *               text.
     * @param callback
     *               the callback for selection actions.
     */
    public static void show(Element anchor, Function<String, List<VariableItem>> options, IVariablePanelCallback callback) {
        hideCurrent();
        currentPanel = new VariablePanel();
        currentPanel.build(anchor, options, callback);
    }

    /************************************************************************
     * Instance state.
     ************************************************************************/

    private Element panelEl;
    private elemental2.dom.EventListener dismissListener;

    /**
     * Hides and destroys the panel, removing any document-level listeners.
     */
    public void hide() {
        if (dismissListener != null) {
            DomGlobal.document.removeEventListener("mousedown", dismissListener);
            dismissListener = null;
        }
        if (panelEl != null) {
            if (panelEl.parentNode != null)
                panelEl.parentNode.removeChild(panelEl);
            panelEl = null;
        }
        if (currentPanel == this)
            currentPanel = null;
    }

    /************************************************************************
     * Internal construction.
     ************************************************************************/

    private void build(Element anchor, Function<String, List<VariableItem>> options, IVariablePanelCallback callback) {
        panelEl = DomGlobal.document.createElement("div");
        panelEl.classList.add(styles().varPanel());

        // Search input.
        HTMLInputElement input = Js.uncheckedCast(DomGlobal.document.createElement("input"));
        input.type = "text";
        input.placeholder = "Search variables...";
        input.classList.add(styles().varInput());
        panelEl.appendChild(input);

        // Suggestion list container.
        Element suggestionList = DomGlobal.document.createElement("div");
        suggestionList.classList.add(styles().varList());
        panelEl.appendChild(suggestionList);

        // Wire up input event for filtering.
        input.addEventListener("input", evt -> {
            updateSuggestions(suggestionList, input.value.trim(), options, callback);
        });

        // Keyboard handling.
        input.addEventListener("keydown", evt -> {
            KeyboardEvent ke = Js.uncheckedCast(evt);
            if ("Escape".equals(ke.key)) {
                evt.preventDefault();
                hide();
            }
        });

        // Show initial suggestions.
        updateSuggestions(suggestionList, "", options, callback);

        // Prevent clicks inside the panel from dismissing it.
        panelEl.addEventListener("mousedown", evt -> {
            evt.stopPropagation();
        });

        DomGlobal.document.body.appendChild(panelEl);
        positionBelow(anchor);

        // Defer dismiss listener and input focus so the originating mousedown
        // event finishes bubbling before the outside-click listener is active.
        DomGlobal.setTimeout(args -> {
            installDismiss();
            input.focus();
        }, 0);
    }

    /************************************************************************
     * Suggestion list.
     ************************************************************************/

    private void updateSuggestions(Element container, String text, Function<String, List<VariableItem>> options, IVariablePanelCallback callback) {
        container.innerHTML = "";
        List<VariableItem> items = options.apply(text);
        if ((items != null) && !items.isEmpty()) {
            for (VariableItem item : items) {
                Element row = DomGlobal.document.createElement("div");
                row.classList.add(styles().varItem());

                Element label = DomGlobal.document.createElement("div");
                label.classList.add(styles().varItemLabel());
                label.textContent = item.label();
                row.appendChild(label);

                Element name = DomGlobal.document.createElement("div");
                name.classList.add(styles().varItemName());
                name.textContent = item.name();
                row.appendChild(name);

                row.addEventListener("mousedown", evt -> {
                    evt.preventDefault();
                    evt.stopPropagation();
                    hide();
                    callback.onSelect(item.name(), item.label());
                });
                container.appendChild(row);
            }
        } else {
            // No matches — show hint row.
            Element hint = DomGlobal.document.createElement("div");
            hint.classList.add(styles().varHint());
            hint.textContent = "No matching variables";
            container.appendChild(hint);
        }
    }

    /************************************************************************
     * Positioning and dismiss.
     ************************************************************************/

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
    private static class JsRect {
        public double left, top, bottom;
    }

    private void positionBelow(Element anchor) {
        JsRect rect = Js.uncheckedCast(anchor.getBoundingClientRect());
        elemental2.dom.HTMLElement panelHtml = Js.uncheckedCast(panelEl);
        panelHtml.style.setProperty("left", rect.left + "px");
        panelHtml.style.setProperty("top", (rect.bottom + 4) + "px");
    }

    private void installDismiss() {
        dismissListener = evt -> {
            Element target = Js.uncheckedCast(evt.target);
            Element el = target;
            while (el != null) {
                if (el == panelEl)
                    return;
                el = el.parentElement;
            }
            hide();
        };
        DomGlobal.document.addEventListener("mousedown", dismissListener);
    }

    /************************************************************************
     * CSS.
     ************************************************************************/

    private static IVariablePanelCSS styles() {
        return VariablePanelCSS.instance();
    }

    public static interface IVariablePanelCSS extends IComponentCSS {

        String varPanel();

        String varInput();

        String varList();

        String varItem();

        String varItemLabel();

        String varItemName();

        String varHint();
    }

    @CssResource(value = {
        IComponentCSS.COMPONENT_CSS
    }, stylesheet = """
        .varPanel {
            position: fixed;
            background: #fff;
            border: 1px solid #e5e7eb;
            border-radius: 6px;
            box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12), 0 1px 4px rgba(0, 0, 0, 0.06);
            padding: 8px;
            z-index: 10000;
            display: flex;
            flex-direction: column;
            gap: 6px;
            min-width: 280px;
            font-size: 0.875em;
        }
        .varInput {
            border: 1px solid #d1d5db;
            border-radius: 4px;
            padding: 4px 8px;
            outline: none;
            font-size: inherit;
        }
        .varInput:focus {
            border-color: #8b5cf6;
        }
        .varList {
            display: flex;
            flex-direction: column;
            max-height: 200px;
            overflow-y: auto;
        }
        .varItem {
            padding: 6px 8px;
            cursor: pointer;
            border-radius: 4px;
            display: flex;
            flex-direction: column;
            gap: 1px;
        }
        .varItem:hover {
            background: #f5f3ff;
            color: #5b21b6;
        }
        .varItemLabel {
            font-weight: 500;
        }
        .varItemName {
            font-size: 0.85em;
            color: #9ca3af;
        }
        .varItem:hover .varItemName {
            color: #7c3aed;
        }
        .varHint {
            padding: 6px 8px;
            color: #9ca3af;
            font-style: italic;
        }
    """)
    public static abstract class VariablePanelCSS implements IVariablePanelCSS {

        private static VariablePanelCSS STYLES;

        public static IVariablePanelCSS instance() {
            if (STYLES == null) {
                STYLES = (VariablePanelCSS) GWT.create(VariablePanelCSS.class);
                STYLES.ensureInjected();
            }
            return STYLES;
        }
    }
}
