package com.effacy.jui.text.ui.editor;

import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.Button;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.Img;
import com.effacy.jui.core.client.dom.builder.Input;
import com.effacy.jui.core.client.dom.builder.P;
import com.effacy.jui.core.client.dom.builder.Textarea;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.HTMLTextAreaElement;
import jsinterop.base.Js;

/**
 * A floating panel for entering or editing PlantUML diagram source with a
 * button-triggered preview.
 * <p>
 * Extends {@link ToolPopupPanel} for positioning, dismiss, and singleton
 * tracking. At most one popup panel is visible at a time.
 */
public class DiagramPanel extends ToolPopupPanel {

    /************************************************************************
     * Types.
     ************************************************************************/

    /**
     * Callback for diagram panel actions.
     */
    public interface IDiagramPanelCallback {

        /**
         * Called when the user applies the diagram source and caption.
         */
        void onApply(String source, String caption);

        /**
         * Called when the user removes the diagram block.
         */
        void onRemove();
    }

    /************************************************************************
     * Show.
     ************************************************************************/

    /**
     * Shows the diagram panel below {@code anchor}.
     *
     * @param anchor
     *                       the element to anchor the panel to.
     * @param currentSource
     *                       the current PlantUML source (or {@code null} for a
     *                       new diagram).
     * @param currentCaption
     *                       the current caption (or {@code null}).
     * @param callback
     *                       the callback for apply/remove actions.
     */
    public static void show(Element anchor, String currentSource, String currentCaption, IDiagramPanelCallback callback) {
        DiagramPanel panel = new DiagramPanel(currentSource, currentCaption, callback);
        panel.show(anchor);
    }

    /************************************************************************
     * Instance state.
     ************************************************************************/

    private String currentSource;
    private IDiagramPanelCallback callback;
    private HTMLTextAreaElement textarea;
    private HTMLInputElement captionInput;
    private Element previewEl;
    private Element imageEl;
    private String lastPreviewSource;

    private String currentCaption;

    private DiagramPanel(String currentSource, String currentCaption, IDiagramPanelCallback callback) {
        this.currentSource = currentSource;
        this.currentCaption = currentCaption;
        this.callback = callback;
    }

    /************************************************************************
     * ToolPopupPanel.
     ************************************************************************/

    @Override
    protected void buildContent(ElementBuilder root) {
        root.style(styles().diaPanel());

        // Textarea for PlantUML source.
        Textarea.$(root, 8, 0)
            .style(styles().diaTextarea())
            .attr("placeholder", "Enter PlantUML source...")
            .use(n -> {
                textarea = Js.uncheckedCast(n);
                if (!StringSupport.empty(currentSource))
                    textarea.value = currentSource;
            })
            .on(e -> {
                if ("Escape".equals(e.getKey())) {
                    e.stopEvent();
                    hide();
                }
            }, UIEventType.ONKEYDOWN);

        // Caption input.
        Input.$(root, "text")
            .style(styles().diaCaption())
            .attr("placeholder", "Caption (optional)")
            .use(n -> {
                captionInput = Js.uncheckedCast(n);
                if (!StringSupport.empty(currentCaption))
                    captionInput.value = currentCaption;
            })
            .on(e -> {
                if ("Escape".equals(e.getKey())) {
                    e.stopEvent();
                    hide();
                }
            }, UIEventType.ONKEYDOWN);

        // Preview area.
        Div.$(root).style(styles().diaPreview()).$(preview -> {
            Img.$(preview).use(n -> imageEl = (Element) n);
            Div.$(preview).style(styles().diaEmpty()).$(empty -> {
                Em.$(empty).style(FontAwesome.images());
                P.$(empty).text("No diagram to show!");
            });
            Div.$(preview).style(styles().diaMask());
            Div.$(preview).style(styles().diaPreviewBtn()).$(btnWrap -> {
                Button.$(btnWrap).style(styles().diaBtn()).text("Preview")
                    .on(e -> {
                        e.stopEvent();
                        preview();
                    }, UIEventType.ONMOUSEDOWN);
            });
        }).use(n -> previewEl = (Element) n);

        // Buttons.
        Div.$(root).style(styles().diaButtons()).$(buttons -> {
            Button.$(buttons).style(styles().diaBtn()).text("Apply")
                .on(e -> {
                    e.stopEvent();
                    String source = textarea.value.trim();
                    String caption = captionInput.value.trim();
                    hide();
                    callback.onApply(source, caption);
                }, UIEventType.ONMOUSEDOWN);
            if (!StringSupport.empty(currentSource)) {
                Button.$(buttons).style(styles().diaBtn(), styles().diaBtnDanger()).text("Remove")
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
            textarea.addEventListener("input", evt -> markStale());
            lastPreviewSource = textarea.value.trim();
            if (!StringSupport.empty(lastPreviewSource))
                preview();
        }
    }

    /************************************************************************
     * Preview.
     ************************************************************************/

    private void markStale() {
        String source = textarea.value.trim();
        if ((source != null) && !source.equals(lastPreviewSource)) {
            previewEl.classList.add(styles().diaPanelStale());
        } else {
            previewEl.classList.remove(styles().diaPanelStale());
        }
    }

    private void preview() {
        lastPreviewSource = textarea.value.trim();
        Element panel = getRoot();
        if (panel == null)
            return;
        panel.classList.remove(styles().diaPanelEmpty());
        previewEl.classList.remove(styles().diaPanelStale());
        if (StringSupport.empty(lastPreviewSource)) {
            panel.classList.add(styles().diaPanelEmpty());
        } else {
            String url = EditorSupport.diagram(DiagramBlockHandler.BASE_URL, lastPreviewSource);
            imageEl.setAttribute("src", url);
        }
    }

    /************************************************************************
     * CSS.
     ************************************************************************/

    protected IDiagramPanelCSS styles() {
        return DiagramPanelCSS.instance();
    }

    public static interface IDiagramPanelCSS extends IComponentCSS {

        String diaPanel();

        String diaPanelEmpty();

        String diaPanelStale();

        String diaTextarea();

        String diaCaption();

        String diaPreview();

        String diaEmpty();

        String diaMask();

        String diaPreviewBtn();

        String diaButtons();

        String diaBtn();

        String diaBtnDanger();
    }

    @CssResource(value = {
        IComponentCSS.COMPONENT_CSS
    }, stylesheet = """
        .diaPanel {
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
            min-width: 500px;
            font-size: 0.875em;
        }
        .diaTextarea {
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
        .diaTextarea:focus {
            border-color: #3b82f6;
        }
        .diaCaption {
            width: 100%;
            box-sizing: border-box;
            border: 1px solid #d1d5db;
            border-radius: 4px;
            padding: 6px 8px;
            outline: none;
            font-size: inherit;
        }
        .diaCaption:focus {
            border-color: #3b82f6;
        }
        .diaPreview {
            position: relative;
            border: 1px solid #e5e7eb;
            border-radius: 4px;
            padding: 8px;
            min-height: 60px;
            text-align: center;
            overflow: auto;
            max-height: 300px;
        }
        .diaPreview img {
            max-width: 100%;
        }
        .diaEmpty {
            display: none;
            color: #9ca3af;
            text-align: center;
            padding: 0.5em 0;
        }
        .diaEmpty em {
            font-size: 1.5em;
        }
        .diaEmpty p {
            margin: 0.25em 0 0 0;
            font-size: 0.9em;
        }
        .diaPanelEmpty .diaEmpty {
            display: block;
        }
        .diaPanelEmpty .diaPreview img {
            display: none;
        }
        .diaMask {
            display: none;
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: rgba(255, 255, 255, 0.7);
        }
        .diaPanelStale .diaMask {
            display: block;
        }
        .diaPreviewBtn {
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            display: none;
        }
        .diaPanelStale .diaPreviewBtn {
            display: block;
        }
        .diaButtons {
            display: flex;
            gap: 6px;
            justify-content: flex-end;
        }
        .diaBtn {
            border: none;
            background: #3b82f6;
            color: #fff;
            padding: 4px 10px;
            border-radius: 4px;
            cursor: pointer;
            font-size: inherit;
            white-space: nowrap;
        }
        .diaBtn:hover {
            background: #2563eb;
        }
        .diaBtnDanger {
            background: #ef4444;
        }
        .diaBtnDanger:hover {
            background: #dc2626;
        }
    """)
    public static abstract class DiagramPanelCSS implements IDiagramPanelCSS {

        private static DiagramPanelCSS STYLES;

        public static IDiagramPanelCSS instance() {
            if (STYLES == null) {
                STYLES = (DiagramPanelCSS) GWT.create(DiagramPanelCSS.class);
                STYLES.ensureInjected();
            }
            return STYLES;
        }
    }
}
