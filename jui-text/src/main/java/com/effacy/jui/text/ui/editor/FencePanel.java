package com.effacy.jui.text.ui.editor;

import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.Button;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.Textarea;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.StringSupport;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;
import elemental2.dom.HTMLTextAreaElement;
import jsinterop.base.Js;

/**
 * A floating panel for editing the raw source of a generic fenced block
 * ({@code BlockType.FENCE}). Shows a source textarea and, when a renderer is registered for
 * the fence's info string, a live preview produced by that renderer.
 * <p>
 * Extends {@link ToolPopupPanel} for positioning, dismiss and singleton tracking.
 */
public class FencePanel extends ToolPopupPanel {

    /** Callback for fence panel actions. */
    public interface IFencePanelCallback {

        /** The user applied the (possibly changed) source. */
        void onApply(String content);

        /** The user removed the fence block. */
        void onRemove();
    }

    /**
     * Shows the fence editor below {@code anchor}.
     *
     * @param anchor
     *                the element to anchor the panel to.
     * @param info
     *                the fence info string.
     * @param content
     *                the current source (or {@code null} for a new fence).
     * @param renderer
     *                the renderer for live preview (may be {@code null}).
     * @param callback
     *                the apply / remove callback.
     */
    public static void show(Element anchor, String info, String content, IFenceRenderer renderer, IFencePanelCallback callback) {
        new FencePanel(info, content, renderer, callback).show(anchor);
    }

    private final String info;
    private final String currentContent;
    private final IFenceRenderer renderer;
    private final IFencePanelCallback callback;

    private HTMLTextAreaElement textarea;
    private Element previewEl;
    private String lastPreview;

    private FencePanel(String info, String content, IFenceRenderer renderer, IFencePanelCallback callback) {
        this.info = info;
        this.currentContent = content;
        this.renderer = renderer;
        this.callback = callback;
    }

    @Override
    protected void position(Element anchor) {
        // A fence editor (with a live preview) is larger than an inline popup, so centre it
        // in the viewport rather than anchoring below the block.
        positionCentered();
    }

    @Override
    protected void buildContent(ElementBuilder root) {
        root.style(styles().fpPanel());

        Textarea.$(root, 8, 0)
            .style(styles().fpTextarea())
            .attr("placeholder", (renderer != null) ? renderer.placeholder(info) : "Enter source…")
            .use(n -> {
                textarea = Js.uncheckedCast(n);
                if (!StringSupport.empty(currentContent))
                    textarea.value = currentContent;
            })
            .on(e -> {
                if ("Escape".equals(e.getKey())) {
                    e.stopEvent();
                    hide();
                }
            }, UIEventType.ONKEYDOWN);

        // Live preview (only when a renderer can produce one).
        if (renderer != null) {
            Div.$(root).style(styles().fpPreview()).$(preview -> {
                Div.$(preview).style(styles().fpPreviewBody()).use(n -> previewEl = (Element) n);
                Div.$(preview).style(styles().fpMask());
                Div.$(preview).style(styles().fpPreviewBtnWrap()).$(btnWrap -> {
                    Button.$(btnWrap).style(styles().fpBtn()).text("Preview")
                        .on(e -> {
                            e.stopEvent();
                            preview();
                        }, UIEventType.ONMOUSEDOWN);
                });
            });
        }

        Div.$(root).style(styles().fpButtons()).$(buttons -> {
            Button.$(buttons).style(styles().fpBtn()).text("Apply")
                .on(e -> {
                    e.stopEvent();
                    String content = textarea.value;
                    hide();
                    callback.onApply(content);
                }, UIEventType.ONMOUSEDOWN);
            if (!StringSupport.empty(currentContent)) {
                Button.$(buttons).style(styles().fpBtn(), styles().fpBtnDanger()).text("Remove")
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
            lastPreview = textarea.value;
            if ((renderer != null) && !StringSupport.empty(lastPreview))
                preview();
        }
    }

    private void markStale() {
        Element panel = getRoot();
        if (panel == null)
            return;
        if (!textarea.value.equals(lastPreview))
            panel.classList.add(styles().fpStale());
        else
            panel.classList.remove(styles().fpStale());
    }

    private void preview() {
        if ((renderer == null) || (previewEl == null))
            return;
        lastPreview = textarea.value;
        Element panel = getRoot();
        if (panel != null)
            panel.classList.remove(styles().fpStale());
        previewEl.innerHTML = "";
        renderer.render(previewEl, info, lastPreview);
    }

    /************************************************************************
     * CSS.
     ************************************************************************/

    protected IFencePanelCSS styles() {
        return FencePanelCSS.instance();
    }

    public static interface IFencePanelCSS extends IComponentCSS {

        String fpPanel();

        String fpStale();

        String fpTextarea();

        String fpPreview();

        String fpPreviewBody();

        String fpMask();

        String fpPreviewBtnWrap();

        String fpButtons();

        String fpBtn();

        String fpBtnDanger();
    }

    @CssResource(value = {
        IComponentCSS.COMPONENT_CSS
    }, stylesheet = """
        .fpPanel {
            position: fixed;
            background: #fff;
            border: 1px solid #e5e7eb;
            border-radius: 6px;
            box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12), 0 1px 4px rgba(0, 0, 0, 0.06);
            padding: 8px;
            z-index: var(--jui-editor-popover-z, 1000100);
            display: flex;
            flex-direction: column;
            gap: 6px;
            min-width: 520px;
            font-size: 0.875em;
        }
        .fpTextarea {
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
        .fpTextarea:focus {
            border-color: #3b82f6;
        }
        .fpPreview {
            position: relative;
            border: 1px solid #e5e7eb;
            border-radius: 4px;
            padding: 8px;
            min-height: 60px;
            text-align: center;
            overflow: auto;
            max-height: 320px;
        }
        .fpPreviewBody svg, .fpPreviewBody img {
            max-width: 100%;
        }
        .fpMask {
            display: none;
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: rgba(255, 255, 255, 0.7);
        }
        .fpStale .fpMask {
            display: block;
        }
        .fpPreviewBtnWrap {
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            display: none;
        }
        .fpStale .fpPreviewBtnWrap {
            display: block;
        }
        .fpButtons {
            display: flex;
            gap: 6px;
            justify-content: flex-end;
        }
        .fpBtn {
            border: none;
            background: #3b82f6;
            color: #fff;
            padding: 4px 10px;
            border-radius: 4px;
            cursor: pointer;
            font-size: inherit;
            white-space: nowrap;
        }
        .fpBtn:hover {
            background: #2563eb;
        }
        .fpBtnDanger {
            background: #ef4444;
        }
        .fpBtnDanger:hover {
            background: #dc2626;
        }
    """)
    public static abstract class FencePanelCSS implements IFencePanelCSS {

        private static FencePanelCSS STYLES;

        public static IFencePanelCSS instance() {
            if (STYLES == null) {
                STYLES = (FencePanelCSS) GWT.create(FencePanelCSS.class);
                STYLES.ensureInjected();
            }
            return STYLES;
        }
    }
}
