package com.effacy.jui.text.ui.editor;

import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.dom.DomSupport;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.Button;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.P;
import com.effacy.jui.core.client.dom.builder.Textarea;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;
import elemental2.dom.HTMLTextAreaElement;
import jsinterop.base.Js;

/**
 * A floating panel for entering or editing LaTeX equation source with a live
 * KaTeX preview.
 * <p>
 * Extends {@link ToolPopupPanel} for positioning, dismiss, and singleton
 * tracking. At most one popup panel is visible at a time.
 */
public class EquationPanel extends ToolPopupPanel {

    /************************************************************************
     * Types.
     ************************************************************************/

    /**
     * Callback for equation panel actions.
     */
    public interface IEquationPanelCallback {

        /**
         * Called when the user applies the equation source.
         */
        void onApply(String source);

        /**
         * Called when the user removes the equation block.
         */
        void onRemove();
    }

    /************************************************************************
     * Show.
     ************************************************************************/

    /**
     * Shows the equation panel below {@code anchor}.
     *
     * @param anchor
     *                      the element to anchor the panel to.
     * @param currentSource
     *                      the current LaTeX source (or {@code null} for a new
     *                      equation).
     * @param callback
     *                      the callback for apply/remove actions.
     */
    public static void show(Element anchor, String currentSource, IEquationPanelCallback callback) {
        EquationPanel panel = new EquationPanel(currentSource, callback);
        panel.show(anchor);
    }

    /************************************************************************
     * Instance state.
     ************************************************************************/

    private String currentSource;
    private IEquationPanelCallback callback;
    private HTMLTextAreaElement textarea;
    private Element previewEl;
    private Element errorEl;

    private EquationPanel(String currentSource, IEquationPanelCallback callback) {
        this.currentSource = currentSource;
        this.callback = callback;
    }

    /************************************************************************
     * ToolPopupPanel.
     ************************************************************************/

    @Override
    protected void buildContent(ElementBuilder root) {
        root.style(styles().eqnPanel());

        // Textarea for LaTeX source.
        Textarea.$(root, 6, 0)
            .style(styles().eqnTextarea())
            .attr("placeholder", "Enter LaTeX...")
            .use(n -> {
                textarea = Js.uncheckedCast(n);
                if ((currentSource != null) && !currentSource.isEmpty())
                    textarea.value = currentSource;
            })
            .on(e -> {
                if ("Escape".equals(e.getKey())) {
                    e.stopEvent();
                    hide();
                }
            }, UIEventType.ONKEYDOWN);

        // Preview area.
        Div.$(root).style(styles().eqnPreview()).$(preview -> {
            Div.$(preview).use(n -> previewEl = (Element) n);
            Div.$(preview).style(styles().eqnEmpty()).$(empty -> {
                Em.$(empty).style(FontAwesome.squareRootVariable());
                P.$(empty).text("No equation to show!");
            });
        });

        // Error area.
        Div.$(root).style(styles().eqnError())
            .use(n -> errorEl = (Element) n);

        // Buttons.
        Div.$(root).style(styles().eqnButtons()).$(buttons -> {
            Button.$(buttons).style(styles().eqnBtn()).text("Apply")
                .on(e -> {
                    e.stopEvent();
                    String source = textarea.value.trim();
                    hide();
                    callback.onApply(source);
                }, UIEventType.ONMOUSEDOWN);
            if ((currentSource != null) && !currentSource.isEmpty()) {
                Button.$(buttons).style(styles().eqnBtn(), styles().eqnBtnDanger()).text("Remove")
                    .on(e -> {
                        e.stopEvent();
                        hide();
                        callback.onRemove();
                    }, UIEventType.ONMOUSEDOWN);
            }
        });
    }

    @Override
    protected void onShown() {
        if (textarea != null) {
            textarea.focus();
            textarea.addEventListener("input", evt -> refreshPreview(textarea.value.trim()));
            refreshPreview(textarea.value.trim());
        }
    }

    /************************************************************************
     * Preview.
     ************************************************************************/

    private void refreshPreview(String source) {
        Element panel = getRoot();
        if (panel == null)
            return;
        panel.classList.remove(styles().eqnPanelEmpty(), styles().eqnPanelError());
        errorEl.textContent = "";
        if (StringSupport.empty(source)) {
            panel.classList.add(styles().eqnPanelEmpty());
        } else {
            String err = EditorSupport.latex(previewEl, source, true);
            if (!StringSupport.empty(err)) {
                panel.classList.add(styles().eqnPanelError());
                if (err.startsWith("KaTeX parse error:"))
                    err = err.substring(19);
                DomSupport.innerText(errorEl, err);
            }
        }
    }

    /************************************************************************
     * CSS.
     ************************************************************************/

    protected IEquationPanelCSS styles() {
        return EquationPanelCSS.instance();
    }

    public static interface IEquationPanelCSS extends IComponentCSS {

        String eqnPanel();

        String eqnPanelEmpty();

        String eqnPanelError();

        String eqnTextarea();

        String eqnPreview();

        String eqnEmpty();

        String eqnError();

        String eqnButtons();

        String eqnBtn();

        String eqnBtnDanger();
    }

    @CssResource(value = {
        IComponentCSS.COMPONENT_CSS
    }, stylesheet = """
        .eqnPanel {
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
            min-width: 400px;
            font-size: 0.875em;
        }
        .eqnTextarea {
            width: 100%;
            box-sizing: border-box;
            border: 1px solid #d1d5db;
            border-radius: 4px;
            padding: 6px 8px;
            outline: none;
            font-family: 'Courier New', Courier, monospace;
            font-size: inherit;
            resize: vertical;
        }
        .eqnTextarea:focus {
            border-color: #3b82f6;
        }
        .eqnPreview {
            border: 1px solid #e5e7eb;
            border-radius: 4px;
            padding: 8px;
            min-height: 40px;
            text-align: center;
            overflow: auto;
            max-height: 200px;
        }
        .eqnEmpty {
            display: none;
            color: #9ca3af;
            text-align: center;
            padding: 0.5em 0;
        }
        .eqnEmpty em {
            font-size: 1.5em;
        }
        .eqnEmpty p {
            margin: 0.25em 0 0 0;
            font-size: 0.9em;
        }
        .eqnPanelEmpty .eqnEmpty {
            display: block;
        }
        .eqnPanelEmpty .eqnPreview > div:first-child {
            display: none;
        }
        .eqnError {
            display: none;
            color: #ef4444;
            font-size: 0.85em;
            padding: 2px 0;
        }
        .eqnPanelError .eqnError {
            display: block;
        }
        .eqnPanelError .eqnPreview > div:first-child {
            display: none;
        }
        .eqnPanelError .eqnEmpty {
            display: none;
        }
        .eqnButtons {
            display: flex;
            gap: 6px;
            justify-content: flex-end;
        }
        .eqnBtn {
            border: none;
            background: #3b82f6;
            color: #fff;
            padding: 4px 10px;
            border-radius: 4px;
            cursor: pointer;
            font-size: inherit;
            white-space: nowrap;
        }
        .eqnBtn:hover {
            background: #2563eb;
        }
        .eqnBtnDanger {
            background: #ef4444;
        }
        .eqnBtnDanger:hover {
            background: #dc2626;
        }
    """)
    public static abstract class EquationPanelCSS implements IEquationPanelCSS {

        private static EquationPanelCSS STYLES;

        public static IEquationPanelCSS instance() {
            if (STYLES == null) {
                STYLES = (EquationPanelCSS) GWT.create(EquationPanelCSS.class);
                STYLES.ensureInjected();
            }
            return STYLES;
        }
    }
}
