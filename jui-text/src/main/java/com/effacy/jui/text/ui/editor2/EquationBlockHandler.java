package com.effacy.jui.text.ui.editor2;

import java.util.ArrayList;
import java.util.List;

import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.dom.css.CSSInjector;
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
 * Handles {@link BlockType#EQN} blocks.
 * <p>
 * Renders a non-editable equation block using KaTeX (loaded from CDN). The
 * user clicks the block to open an {@link EquationPanel} for editing the
 * LaTeX source.
 */
public class EquationBlockHandler implements IBlockHandler {

    /************************************************************************
     * KaTeX loading.
     ************************************************************************/

    /**
     * The KaTeX version (used to retrieve the relevant JS and CSS).
     */
    public static String VERSION = "0.16.9";

    /**
     * Callbacks queued while KaTeX is loading. {@code null} once loaded.
     */
    private static List<Runnable> LOADING = new ArrayList<>();
    static {
        ScriptInjector.injectFromUrl("https://cdn.jsdelivr.net/npm/katex@" + VERSION + "/dist/katex.min.js", outcome -> {
            if (LOADING != null) {
                LOADING.forEach(r -> r.run());
                LOADING = null;
            }
        });
        CSSInjector.injectFromUrl("https://cdn.jsdelivr.net/npm/katex@" + VERSION + "/dist/katex.min.css");
    }

    /************************************************************************
     * IBlockHandler.
     ************************************************************************/

    @Override
    public boolean accepts(BlockType type) {
        return type == BlockType.EQN;
    }

    @Override
    public Element render(FormattedBlock block, int blockIndex, IEditorContext ctx) {
        String source = block.getContent();

        // Outer wrapper: contenteditable="false" so the editor treats it as atomic.
        Element wrapper = DomGlobal.document.createElement("div");
        wrapper.classList.add(styles().eqnWrapper());
        wrapper.setAttribute("contenteditable", "false");
        wrapper.setAttribute("data-block-index", String.valueOf(blockIndex));

        // KaTeX rendering target.
        Element renderEl = DomGlobal.document.createElement("div");
        renderEl.classList.add(styles().eqnRender());
        wrapper.appendChild(renderEl);

        // Empty state.
        Element emptyEl = DomGlobal.document.createElement("div");
        emptyEl.classList.add(styles().eqnEmpty());
        emptyEl.innerHTML = "<em class=\"fa-solid fa-square-root-variable\"></em><p>No equation to show!</p>";
        wrapper.appendChild(emptyEl);

        // Error state.
        Element errorEl = DomGlobal.document.createElement("div");
        errorEl.classList.add(styles().eqnError());
        errorEl.innerHTML = "<em class=\"fa-solid fa-bug\"></em><p>Error with the equation!</p>";
        wrapper.appendChild(errorEl);

        // Render KaTeX or show empty/error.
        if (StringSupport.empty(source)) {
            wrapper.classList.add(styles().eqnWrapperEmpty());
        } else {
            renderLatex(wrapper, renderEl, source);
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
        // Auto-open the editor when a new equation block is inserted.
        Element editorEl = ctx.editorEl();
        Element blockEl = editorEl.querySelector("[data-block-index=\"" + blockIndex + "\"]");
        if (blockEl != null) {
            DomGlobal.setTimeout(args -> openEditor(blockEl, blockIndex, ctx), 50);
        }
    }

    /************************************************************************
     * Rendering and editing.
     ************************************************************************/

    private void renderLatex(Element wrapper, Element renderEl, String source) {
        if (LOADING != null) {
            LOADING.add(() -> {
                String err = EditorSupport2.latex(renderEl, source, true);
                if (!StringSupport.empty(err))
                    wrapper.classList.add(styles().eqnWrapperError());
            });
        } else {
            String err = EditorSupport2.latex(renderEl, source, true);
            if (!StringSupport.empty(err))
                wrapper.classList.add(styles().eqnWrapperError());
        }
    }

    private void openEditor(Element anchor, int blockIndex, IEditorContext ctx) {
        List<FormattedBlock> blocks = ctx.state().doc().getBlocks();
        if ((blockIndex < 0) || (blockIndex >= blocks.size()))
            return;
        FormattedBlock block = blocks.get(blockIndex);
        String currentSource = block.getContent();
        EquationPanel.show(anchor, currentSource, new EquationPanel.IEquationPanelCallback() {

            @Override
            public void onApply(String source) {
                FormattedBlock updated = new FormattedBlock(BlockType.EQN);
                if (!StringSupport.empty(source))
                    updated.setContent(source);
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

    protected IEquationCSS styles() {
        return EquationCSS.instance();
    }

    public static interface IEquationCSS extends IComponentCSS {

        String eqnWrapper();

        String eqnWrapperEmpty();

        String eqnWrapperError();

        String eqnRender();

        String eqnEmpty();

        String eqnError();
    }

    @CssResource(value = {
        IComponentCSS.COMPONENT_CSS
    }, stylesheet = """
        .eqnWrapper {
            cursor: pointer;
            padding: 0.75em;
            border-radius: 4px;
            margin: 0.25em 0;
            transition: background 0.15s;
        }
        .eqnWrapper:hover {
            background: #f9fafb;
        }
        .eqnRender {
            text-align: center;
        }
        .eqnEmpty {
            display: none;
            text-align: center;
            padding: 1em 0;
            color: #9ca3af;
        }
        .eqnEmpty em {
            font-size: 2em;
        }
        .eqnEmpty p {
            margin: 0.25em 0 0 0;
            font-weight: 600;
        }
        .eqnWrapperEmpty .eqnEmpty {
            display: block;
        }
        .eqnWrapperEmpty .eqnRender {
            display: none;
        }
        .eqnError {
            display: none;
            text-align: center;
            padding: 1em 0;
            color: #ef4444;
        }
        .eqnError em {
            font-size: 2em;
        }
        .eqnError p {
            margin: 0.25em 0 0 0;
            font-weight: 600;
        }
        .eqnWrapperError .eqnError {
            display: block;
        }
        .eqnWrapperError .eqnRender {
            display: none;
        }
        .eqnWrapperError .eqnEmpty {
            display: none;
        }
    """)
    public static abstract class EquationCSS implements IEquationCSS {

        private static EquationCSS STYLES;

        public static IEquationCSS instance() {
            if (STYLES == null) {
                STYLES = (EquationCSS) GWT.create(EquationCSS.class);
                STYLES.ensureInjected();
            }
            return STYLES;
        }
    }
}
