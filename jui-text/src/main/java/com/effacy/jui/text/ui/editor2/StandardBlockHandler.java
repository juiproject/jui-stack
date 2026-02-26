package com.effacy.jui.text.ui.editor2;

import java.util.List;

import com.effacy.jui.text.type.FormattedBlock;
import com.effacy.jui.text.type.FormattedBlock.BlockType;
import com.effacy.jui.text.type.FormattedLine;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;

/**
 * Handles the standard inline-editable block types: PARA, H1, H2, H3, NLIST,
 * and OLIST.
 * <p>
 * All keyboard and format events for these types are handled by the editor's
 * built-in logic (undo, text insertion, format commands) — this handler
 * returns {@code false} for every event method so the editor proceeds with its
 * default processing.
 */
public class StandardBlockHandler implements IBlockHandler {

    /**
     * Running counters for each indent level of an OLIST sequence. Reset at
     * the start of each render pass.
     */
    private int[] listCounters = new int[6];

    /**
     * Whether the previous block rendered was an OLIST block. Used to detect
     * when a new list sequence starts and the counters should be reset.
     */
    private boolean prevWasOlist = false;

    /**
     * Indent level of the last OLIST block rendered. Used to detect when the
     * list descends to a deeper level so shallower counters are preserved.
     */
    private int prevOlistIndent = 0;

    @Override
    public boolean accepts(BlockType type) {
        return type.is(BlockType.PARA, BlockType.H1, BlockType.H2, BlockType.H3,
                BlockType.NLIST, BlockType.OLIST);
    }

    @Override
    public void beginRender(IEditorContext ctx) {
        for (int j = 0; j < listCounters.length; j++)
            listCounters[j] = 0;
        prevWasOlist = false;
        prevOlistIndent = 0;
    }

    @Override
    public Element render(FormattedBlock block, int blockIndex, IEditorContext ctx) {
        // Update ordered-list counters before creating the element.
        if (block.getType() == BlockType.OLIST) {
            int ind = block.getIndent();
            if (!prevWasOlist) {
                for (int j = 0; j < listCounters.length; j++)
                    listCounters[j] = 0;
            } else if (ind > prevOlistIndent) {
                for (int j = prevOlistIndent + 1; j < listCounters.length; j++)
                    listCounters[j] = 0;
            }
            listCounters[ind]++;
            prevOlistIndent = ind;
            prevWasOlist = true;
        } else {
            prevWasOlist = false;
        }

        Element el = createBlockElement(block.getType(), ctx);
        el.setAttribute("data-block-index", String.valueOf(blockIndex));
        el.classList.add(ctx.styles().block());
        if (block.getIndent() > 0)
            el.classList.add("indent" + block.getIndent());

        List<FormattedLine> lines = block.getLines();
        boolean empty = lines.isEmpty()
                || ((lines.size() == 1) && (lines.get(0).length() == 0));

        if (empty) {
            // Empty blocks need a BR for contenteditable cursor placement.
            el.appendChild(DomGlobal.document.createElement("br"));
        } else {
            for (int i = 0; i < lines.size(); i++) {
                if (i > 0)
                    el.appendChild(DomGlobal.document.createElement("br"));
                ctx.renderLine(el, lines.get(i));
            }
            // Trailing BR needed when the last line is empty so the browser
            // can position the cursor on the new line.
            if (lines.get(lines.size() - 1).length() == 0) {
                Element br = DomGlobal.document.createElement("br");
                br.setAttribute("data-trailing", "true");
                el.appendChild(br);
            }
        }

        // Apply the list marker (data attribute consumed by CSS ::before).
        if (block.getType() == BlockType.OLIST)
            el.setAttribute("data-list-index",
                    ctx.listIndexFormatter().format(block.getIndent(), listCounters[block.getIndent()]));

        return el;
    }

    /**
     * Maps a block type to its HTML element tag name, applying list-style CSS
     * classes where required.
     */
    private Element createBlockElement(BlockType type, IEditorContext ctx) {
        if (type == BlockType.H1)
            return DomGlobal.document.createElement("h1");
        if (type == BlockType.H2)
            return DomGlobal.document.createElement("h2");
        if (type == BlockType.H3)
            return DomGlobal.document.createElement("h3");
        Element el = DomGlobal.document.createElement("p");
        if (type == BlockType.NLIST)
            el.classList.add(ctx.styles().listBullet());
        else if (type == BlockType.OLIST)
            el.classList.add(ctx.styles().listNumber());
        return el;
    }
}
