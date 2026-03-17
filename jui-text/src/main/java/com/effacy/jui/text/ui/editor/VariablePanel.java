package com.effacy.jui.text.ui.editor;

import java.util.List;
import java.util.function.Function;

import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.Input;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.platform.css.client.CssResource;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;
import elemental2.dom.HTMLInputElement;
import jsinterop.base.Js;

/**
 * A lightweight floating panel for selecting a variable to insert.
 * <p>
 * Accepts a {@link Function} that provides {@link VariableItem} suggestions
 * based on the user's input. Items are displayed in a selectable list below
 * the search input. When no suggestions match, a hint row is shown.
 * <p>
 * Extends {@link ToolPopupPanel} for positioning, dismiss, and singleton
 * tracking. At most one popup panel is visible at a time.
 */
public class VariablePanel extends ToolPopupPanel {

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
     * Show.
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
        VariablePanel panel = new VariablePanel(options, callback);
        panel.show(anchor);
    }

    /************************************************************************
     * Instance state.
     ************************************************************************/

    private Function<String, List<VariableItem>> options;
    private IVariablePanelCallback callback;
    private HTMLInputElement input;
    private Element suggestionListEl;
    private List<VariableItem> currentItems;

    private VariablePanel(Function<String, List<VariableItem>> options, IVariablePanelCallback callback) {
        this.options = options;
        this.callback = callback;
    }

    /************************************************************************
     * ToolPopupPanel.
     ************************************************************************/

    @Override
    protected void buildContent(ElementBuilder root) {
        root.style(styles().varPanel());
        Input.$(root, "text")
            .style(styles().varInput())
            .attr("placeholder", "Search variables...")
            .use(n -> input = Js.uncheckedCast(n))
            .on(e -> updateSuggestions(input.value.trim()), UIEventType.ONINPUT)
            .on(e -> {
                if ("Escape".equals(e.getKey())) {
                    e.stopEvent();
                    hide();
                }
            }, UIEventType.ONKEYDOWN);
        Div.$(root).style(styles().varList())
            .use(n -> suggestionListEl = (Element) n)
            .on(e -> {
                Element target = e.getTarget();
                while ((target != null) && (target != suggestionListEl)) {
                    String idx = target.getAttribute("data-idx");
                    if ((idx != null) && !idx.isEmpty()) {
                        int i = Integer.parseInt(idx);
                        if ((currentItems != null) && (i >= 0) && (i < currentItems.size())) {
                            e.stopEvent();
                            hide();
                            VariableItem item = currentItems.get(i);
                            callback.onSelect(item.name(), item.label());
                        }
                        return;
                    }
                    target = target.parentElement;
                }
            }, UIEventType.ONMOUSEDOWN);
    }

    @Override
    protected void onShown() {
        if (input != null)
            input.focus();
        updateSuggestions("");
    }

    /************************************************************************
     * Suggestion list.
     ************************************************************************/

    private void updateSuggestions(String text) {
        currentItems = options.apply(text);
        Wrap.buildInto(suggestionListEl, root -> {
            if ((currentItems != null) && !currentItems.isEmpty()) {
                for (int i = 0; i < currentItems.size(); i++) {
                    VariableItem item = currentItems.get(i);
                    Div.$(root).style(styles().varItem())
                        .attr("data-idx", String.valueOf(i))
                        .$(row -> {
                            Div.$(row).style(styles().varItemLabel()).text(item.label());
                            Div.$(row).style(styles().varItemName()).text(item.name());
                        });
                }
            } else {
                Div.$(root).style(styles().varHint()).text("No matching variables");
            }
        });
    }

    /************************************************************************
     * CSS.
     ************************************************************************/

    protected IVariablePanelCSS styles() {
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
