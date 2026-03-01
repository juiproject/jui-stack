package com.effacy.jui.text.ui.editor;

import java.util.ArrayList;
import java.util.List;

import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.dom.DomSupport;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.ScriptInjector;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.text.type.FormattedBlock;
import com.effacy.jui.text.type.FormattedBlock.BlockType;
import com.effacy.jui.text.type.edit.Transaction;
import com.effacy.jui.text.type.edit.step.DeleteBlockStep;
import com.effacy.jui.text.type.edit.step.ReplaceBlockStep;
import com.effacy.jui.text.type.edit.Selection;
import com.google.gwt.core.client.GWT;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;

/**
 * Handles {@link BlockType#DIA} blocks.
 * <p>
 * Renders a non-editable diagram block using PlantUML (encoded client-side
 * via {@code plantuml-encoder.min.js}). The user clicks the block to open a
 * {@link DiagramPanel} for editing the PlantUML source.
 */
public class DiagramBlockHandler implements IBlockHandler {

    /************************************************************************
     * PlantUML encoder loading.
     ************************************************************************/

    /**
     * The base URL for converting encoded PlantUML source to an image.
     */
    public static String BASE_URL = "//www.plantuml.com/plantuml/img/";

    /**
     * Meta-data field name for the caption.
     */
    public static String META_CAPTION = "caption";

    /**
     * Callbacks queued while the PlantUML encoder is loading. {@code null}
     * once loaded.
     */
    private static List<Runnable> LOADING = new ArrayList<>();
    static {
        ScriptInjector.injectFromModuleBase("plantuml-encoder.min.js", outcome -> {
            if (LOADING != null) {
                LOADING.forEach(r -> r.run());
                LOADING = null;
            }
        });
    }

    /************************************************************************
     * IBlockHandler.
     ************************************************************************/

    @Override
    public boolean accepts(BlockType type) {
        return type == BlockType.DIA;
    }

    @Override
    public Element render(FormattedBlock block, int blockIndex, IEditorContext ctx) {
        String source = block.getContent();
        String caption = block.meta(META_CAPTION);

        // Outer wrapper: contenteditable="false" so the editor treats it as atomic.
        Element wrapper = DomGlobal.document.createElement("div");
        wrapper.classList.add(styles().diaWrapper());
        wrapper.setAttribute("contenteditable", "false");
        wrapper.setAttribute("data-block-index", String.valueOf(blockIndex));

        // Image element for the rendered diagram.
        Element imageEl = DomGlobal.document.createElement("img");
        imageEl.classList.add(styles().diaImage());
        wrapper.appendChild(imageEl);

        // Empty state.
        Element emptyEl = DomGlobal.document.createElement("div");
        emptyEl.classList.add(styles().diaEmpty());
        emptyEl.innerHTML = "<em class=\"fa-solid fa-images\"></em><p>No diagram to show!</p>";
        wrapper.appendChild(emptyEl);

        // Caption.
        Element captionEl = DomGlobal.document.createElement("p");
        captionEl.classList.add(styles().diaCaption());
        wrapper.appendChild(captionEl);
        if (!StringSupport.empty(caption)) {
            DomSupport.innerText(captionEl, caption);
        } else {
            ((elemental2.dom.HTMLElement) captionEl).style.set("display", "none");
        }

        // Render diagram or show empty state.
        if (StringSupport.empty(source)) {
            wrapper.classList.add(styles().diaWrapperEmpty());
        } else {
            renderDiagram(imageEl, source);
        }

        // Click to edit.
        wrapper.addEventListener("click", evt -> {
            evt.preventDefault();
            evt.stopPropagation();
            openEditor(wrapper, blockIndex, ctx);
        });

        return wrapper;
    }

    @Override
    public void focusBlock(int blockIndex, IEditorContext ctx) {
        // Auto-open the editor when a new diagram block is inserted.
        Element editorEl = ctx.editorEl();
        Element blockEl = editorEl.querySelector("[data-block-index=\"" + blockIndex + "\"]");
        if (blockEl != null) {
            DomGlobal.setTimeout(args -> openEditor(blockEl, blockIndex, ctx), 50);
        }
    }

    /************************************************************************
     * Rendering and editing.
     ************************************************************************/

    private void renderDiagram(Element imageEl, String source) {
        if (LOADING != null) {
            LOADING.add(() -> {
                String url = EditorSupport.diagram(BASE_URL, source);
                imageEl.setAttribute("src", url);
            });
        } else {
            String url = EditorSupport.diagram(BASE_URL, source);
            imageEl.setAttribute("src", url);
        }
    }

    private void openEditor(Element anchor, int blockIndex, IEditorContext ctx) {
        List<FormattedBlock> blocks = ctx.state().doc().getBlocks();
        if ((blockIndex < 0) || (blockIndex >= blocks.size()))
            return;
        FormattedBlock block = blocks.get(blockIndex);
        String currentSource = block.getContent();
        String currentCaption = block.meta(META_CAPTION);
        DiagramPanel.show(anchor, currentSource, currentCaption, new DiagramPanel.IDiagramPanelCallback() {

            @Override
            public void onApply(String source, String caption) {
                FormattedBlock updated = new FormattedBlock(BlockType.DIA);
                if (!StringSupport.empty(source))
                    updated.setContent(source);
                if (!StringSupport.empty(caption))
                    updated.meta(META_CAPTION, caption);
                Transaction tr = Transaction.create();
                tr.step(new ReplaceBlockStep(blockIndex, updated));
                tr.setSelection(Selection.cursor(blockIndex, 0));
                ctx.applyTransaction(tr);
            }

            @Override
            public void onRemove() {
                Transaction tr = Transaction.create();
                tr.step(new DeleteBlockStep(blockIndex));
                int newIdx = Math.max(0, blockIndex - 1);
                tr.setSelection(Selection.cursor(newIdx, 0));
                ctx.applyTransaction(tr);
            }
        });
    }

    /************************************************************************
     * CSS.
     ************************************************************************/

    protected IDiagramCSS styles() {
        return DiagramCSS.instance();
    }

    public static interface IDiagramCSS extends IComponentCSS {

        String diaWrapper();

        String diaWrapperEmpty();

        String diaImage();

        String diaEmpty();

        String diaCaption();
    }

    @CssResource(value = {
        IComponentCSS.COMPONENT_CSS
    }, stylesheet = """
        .diaWrapper {
            cursor: pointer;
            padding: 0.75em;
            border-radius: 4px;
            margin: 0.25em 0;
            transition: background 0.15s;
            text-align: center;
        }
        .diaWrapper:hover {
            background: #f9fafb;
        }
        .diaImage {
            max-width: 100%;
        }
        .diaEmpty {
            display: none;
            text-align: center;
            padding: 1em 0;
            color: #9ca3af;
        }
        .diaEmpty em {
            font-size: 2em;
        }
        .diaEmpty p {
            margin: 0.25em 0 0 0;
            font-weight: 600;
        }
        .diaWrapperEmpty .diaEmpty {
            display: block;
        }
        .diaWrapperEmpty .diaImage {
            display: none;
        }
        .diaCaption {
            margin: 0.5em 0 0 0;
            font-style: italic;
            color: #6b7280;
            font-size: 0.9em;
        }
    """)
    public static abstract class DiagramCSS implements IDiagramCSS {

        private static DiagramCSS STYLES;

        public static IDiagramCSS instance() {
            if (STYLES == null) {
                STYLES = (DiagramCSS) GWT.create(DiagramCSS.class);
                STYLES.ensureInjected();
            }
            return STYLES;
        }
    }
}
