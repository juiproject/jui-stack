package com.effacy.jui.text.ui.editor;

import java.util.List;

import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.dom.DomSupport;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.text.type.FormattedBlock;
import com.effacy.jui.text.type.FormattedBlock.BlockType;
import com.effacy.jui.text.type.edit.Selection;
import com.effacy.jui.text.type.edit.Transaction;
import com.effacy.jui.text.type.edit.step.DeleteBlockStep;
import com.effacy.jui.text.type.edit.step.ReplaceBlockStep;
import com.google.gwt.core.client.GWT;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;

/**
 * Handles {@link BlockType#FENCE} blocks — generic fenced blocks rendered by a pluggable
 * {@link IFenceRenderer} keyed by the fence's info string (see {@link Fences}).
 * <p>
 * The block is atomic ({@code contenteditable="false"}): the registered renderer produces
 * its visual form (e.g. a mermaid diagram), and clicking it opens a {@link FencePanel} to
 * edit the raw source. When no renderer is registered for the info string, the body is
 * shown as a code block (so unknown fences are still legible and round-trip).
 */
public class FenceBlockHandler implements IBlockHandler {

    @Override
    public boolean accepts(BlockType type) {
        return type == BlockType.FENCE;
    }

    /**
     * A fence's rendering is whatever its registered {@link IFenceRenderer} makes of the
     * body — for Mermaid a parse and lay-out producing SVG, asynchronously — so repeating
     * it for an edit elsewhere in the document is both wasted work and, because the result
     * lands after the fact, a visible flicker. The info string and the body are the whole
     * of the input: the renderer is chosen by the one and draws the other.
     */
    @Override
    public String renderKey(FormattedBlock block) {
        String info = StringSupport.safe(block.meta("info"));
        IFenceRenderer renderer = Fences.rendererFor(info);
        // A renderer drawing on anything beyond its arguments — a remote query, ambient
        // scope — has no key: the same source does not mean the same rendering, and one
        // kept from a previous pass would be frozen at what it first showed.
        if ((renderer != null) && !renderer.cacheable())
            return null;
        // Length-prefixed rather than delimited: a body may hold any character, so there
        // is no separator to trust — but a length says where the info string ends.
        return "fence:" + info.length() + ":" + info + StringSupport.safe(block.flatten());
    }

    @Override
    public Element render(FormattedBlock block, int blockIndex, IEditorContext ctx) {
        String info = block.meta("info");
        String content = block.flatten();

        Element wrapper = DomGlobal.document.createElement("div");
        wrapper.classList.add(styles().fence());
        wrapper.setAttribute("contenteditable", "false");
        wrapper.setAttribute("data-block-index", String.valueOf(blockIndex));

        Element body = DomGlobal.document.createElement("div");
        body.classList.add(styles().fenceBody());
        wrapper.appendChild(body);

        IFenceRenderer renderer = Fences.rendererFor(info);
        if (StringSupport.empty(content) && ((renderer == null) || !renderer.rendersEmpty())) {
            Element empty = DomGlobal.document.createElement("div");
            empty.classList.add(styles().fenceEmpty());
            String label = (renderer != null) ? renderer.label(info) : (StringSupport.empty(info) ? "fenced" : info);
            empty.innerHTML = "<p>Click to add " + escape(label) + " content</p>";
            body.appendChild(empty);
        } else if (renderer != null) {
            renderer.render(body, info, content);
        } else {
            renderFallback(body, info, content);
        }

        // Info chip in the corner (so the kind is always visible).
        if (!StringSupport.empty(info)) {
            Element chip = DomGlobal.document.createElement("span");
            chip.classList.add(styles().fenceChip());
            DomSupport.innerText(chip, (renderer != null) ? renderer.label(info) : info);
            wrapper.appendChild(chip);
        }

        // The index is read back off the element rather than captured here: this element
        // outlives the render that built it (see renderKey), and the editor re-stamps it
        // as the block moves.
        wrapper.addEventListener("click", evt -> {
            evt.preventDefault();
            evt.stopPropagation();
            openEditor(wrapper, IBlockHandler.blockIndexOf(wrapper), ctx);
        });

        return wrapper;
    }

    @Override
    public void focusBlock(int blockIndex, IEditorContext ctx) {
        // Auto-open the editor when a new fence block is inserted.
        Element editorEl = ctx.editorEl();
        Element blockEl = editorEl.querySelector("[data-block-index=\"" + blockIndex + "\"]");
        if (blockEl != null)
            DomGlobal.setTimeout(args -> openEditor(blockEl, blockIndex, ctx), 50);
    }

    /************************************************************************
     * Rendering and editing.
     ************************************************************************/

    private void renderFallback(Element body, String info, String content) {
        Element pre = DomGlobal.document.createElement("pre");
        pre.classList.add(styles().fenceCode());
        Element code = DomGlobal.document.createElement("code");
        DomSupport.innerText(code, content);
        pre.appendChild(code);
        body.appendChild(pre);
    }

    private void openEditor(Element anchor, int blockIndex, IEditorContext ctx) {
        List<FormattedBlock> blocks = ctx.state().doc().getBlocks();
        if ((blockIndex < 0) || (blockIndex >= blocks.size()))
            return;
        FormattedBlock block = blocks.get(blockIndex);
        String info = block.meta("info");
        String content = block.flatten();
        IFenceRenderer renderer = Fences.rendererFor(info);

        java.util.function.Consumer<String> apply = newContent -> {
            FormattedBlock updated = new FormattedBlock(BlockType.FENCE);
            if (!StringSupport.empty(info))
                updated.meta("info", info);
            if (newContent != null) {
                String[] lines = newContent.split("\n", -1);
                int n = lines.length;
                // Drop a single trailing empty line introduced by a trailing newline.
                if ((n > 0) && lines[n - 1].isEmpty())
                    n--;
                for (int i = 0; i < n; i++)
                    updated.line(lines[i]);
            }
            Transaction tr = Transaction.create();
            tr.step(new ReplaceBlockStep(blockIndex, updated));
            tr.setSelection(Selection.cursor(blockIndex, 0));
            ctx.applyTransaction(tr);
        };
        Runnable remove = () -> {
            Transaction tr = Transaction.create();
            tr.step(new DeleteBlockStep(blockIndex));
            int newIdx = Math.max(0, blockIndex - 1);
            tr.setSelection(Selection.cursor(newIdx, 0));
            ctx.applyTransaction(tr);
        };

        // Let the renderer supply its own editor; otherwise fall back to the built-in panel.
        if ((renderer != null) && renderer.edit(anchor, info, content, apply, remove))
            return;
        FencePanel.show(anchor, info, content, renderer, new FencePanel.IFencePanelCallback() {

            @Override
            public void onApply(String newContent) {
                apply.accept(newContent);
            }

            @Override
            public void onRemove() {
                remove.run();
            }
        });
    }

    private static String escape(String s) {
        if (s == null)
            return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /************************************************************************
     * CSS.
     ************************************************************************/

    protected IFenceCSS styles() {
        return FenceCSS.instance();
    }

    public static interface IFenceCSS extends IComponentCSS {

        String fence();

        String fenceBody();

        String fenceChip();

        String fenceCode();

        String fenceEmpty();
    }

    @CssResource(value = {
        IComponentCSS.COMPONENT_CSS
    }, stylesheet = """
        .fence {
            position: relative;
            cursor: pointer;
            padding: 0.75em;
            border: 1px solid #e5e7eb;
            border-radius: 6px;
            margin: 0.4em 0;
            transition: background 0.15s, border-color 0.15s;
            text-align: center;
            background: #fff;
        }
        .fence:hover {
            background: #f9fafb;
            border-color: #d1d5db;
        }
        .fenceBody svg, .fenceBody img {
            max-width: 100%;
        }
        .fenceChip {
            position: absolute;
            top: 6px;
            right: 8px;
            font-size: 0.7em;
            font-weight: 600;
            color: #6b7280;
            background: #f3f4f6;
            border-radius: 4px;
            padding: 1px 6px;
            pointer-events: none;
        }
        .fenceCode {
            margin: 0;
            text-align: left;
            white-space: pre;
            overflow-x: auto;
            font-family: 'Courier New', Courier, monospace;
            font-size: 0.9em;
        }
        .fenceEmpty {
            color: #9ca3af;
            padding: 0.75em 0;
        }
        .fenceEmpty p {
            margin: 0;
            font-weight: 600;
        }
    """)
    public static abstract class FenceCSS implements IFenceCSS {

        private static FenceCSS STYLES;

        public static IFenceCSS instance() {
            if (STYLES == null) {
                STYLES = (FenceCSS) GWT.create(FenceCSS.class);
                STYLES.ensureInjected();
            }
            return STYLES;
        }
    }
}
