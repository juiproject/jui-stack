package com.effacy.jui.text.type.edit;

import java.util.List;

import com.effacy.jui.text.type.FormattedBlock;
import com.effacy.jui.text.type.FormattedLine;
import com.effacy.jui.text.type.FormattedText;

/**
 * Utilities for computing flat document positions.
 * <p>
 * The position scheme follows ProseMirror conventions: each block has an
 * opening and closing boundary token (1 position each), characters consume 1
 * position each, and line breaks between lines within a block consume 1
 * position.
 * <p>
 * Example for a document with PARA("Hello\nWorld") and H1("Title"):
 * <pre>
 * [open] H e l l o \n W o r l d [close] [open] T i t l e [close]
 *   0    1 2 3 4 5  6  7 8 9 10 11  12     13  14 15 16 17 18  19
 * </pre>
 */
public final class Positions {

    private Positions() {}

    /**
     * Computes the content size of a block (excluding open/close tokens).
     * <p>
     * For text blocks this is the sum of line lengths plus line breaks. For
     * container blocks (TABLE, TROW) it is the sum of child node sizes. For
     * atomic blocks (EQN, DIA) it is 0.
     */
    public static int contentSize(FormattedBlock block) {
        switch (block.getType().constraint()) {
            case LINES:
            case LINES_OR_BLOCKS: {
                List<FormattedLine> lines = block.getLines();
                if ((lines != null) && !lines.isEmpty()) {
                    int size = 0;
                    for (int i = 0; i < lines.size(); i++) {
                        if (i > 0)
                            size++;
                        size += lines.get(i).length();
                    }
                    return size;
                }
                // LINES_OR_BLOCKS may have child blocks instead.
                List<FormattedBlock> blocks = block.getBlocks();
                if ((blocks != null) && !blocks.isEmpty()) {
                    int size = 0;
                    for (FormattedBlock child : blocks)
                        size += nodeSize(child);
                    return size;
                }
                return 0;
            }
            case BLOCKS: {
                List<FormattedBlock> blocks = block.getBlocks();
                if ((blocks == null) || blocks.isEmpty())
                    return 0;
                int size = 0;
                for (FormattedBlock child : blocks)
                    size += nodeSize(child);
                return size;
            }
            case CONTENT:
            case CONTENT_AND_LINES:
                // Atomic — cursor cannot enter.
                return 0;
            default:
                return 0;
        }
    }

    /**
     * The total size of a block in position space: 2 (open + close) plus the
     * content size.
     */
    public static int nodeSize(FormattedBlock block) {
        return 2 + contentSize(block);
    }

    /**
     * The flat position where a top-level block starts (its opening boundary).
     *
     * @param doc
     *            the document.
     * @param blockIndex
     *                   index of the block.
     * @return the flat position.
     */
    public static int blockStart(FormattedText doc, int blockIndex) {
        int pos = 0;
        List<FormattedBlock> blocks = doc.getBlocks();
        for (int i = 0; i < blockIndex; i++)
            pos += nodeSize(blocks.get(i));
        return pos;
    }

    /**
     * The total document length in position space.
     */
    public static int length(FormattedText doc) {
        int len = 0;
        for (FormattedBlock block : doc.getBlocks())
            len += nodeSize(block);
        return len;
    }

    /**
     * Converts block-relative coordinates to a flat position.
     * <p>
     * The {@code charOffset} is the character offset from the block's content
     * start (across all lines, with line breaks counted), matching the semantics
     * used by {@link Selection}.
     *
     * @param doc
     *                   the document.
     * @param blockIndex
     *                   top-level block index.
     * @param charOffset
     *                   character offset within the block's content.
     * @return the flat position.
     */
    public static int toFlat(FormattedText doc, int blockIndex, int charOffset) {
        return blockStart(doc, blockIndex) + 1 + charOffset;
    }

    /**
     * Resolves a flat position to its structural coordinates.
     *
     * @param doc
     *                 the document.
     * @param position
     *                 the flat position (0 to {@link #length(FormattedText)}).
     * @return the resolved position.
     * @throws IllegalArgumentException
     *                                  if position is out of range.
     */
    public static ResolvedPosition resolve(FormattedText doc, int position) {
        List<FormattedBlock> blocks = doc.getBlocks();
        int pos = 0;
        for (int bi = 0; bi < blocks.size(); bi++) {
            FormattedBlock block = blocks.get(bi);
            int ns = nodeSize(block);

            // Before this block?
            if (position < pos)
                break;

            // At block open boundary.
            if (position == pos)
                return new ResolvedPosition(position, bi, -1, -1, -1);

            // At block close boundary.
            if (position == pos + ns - 1)
                return new ResolvedPosition(position, bi, -1, -1, contentSize(block));

            // Inside this block's content.
            if (position < pos + ns - 1) {
                int contentPos = position - pos - 1; // offset into content
                return resolveInBlock(position, bi, block, contentPos);
            }

            pos += ns;
        }
        // Position at or beyond document end — clamp to last block close.
        if (!blocks.isEmpty()) {
            int lastIdx = blocks.size() - 1;
            return new ResolvedPosition(position, lastIdx, -1, -1, contentSize(blocks.get(lastIdx)));
        }
        return new ResolvedPosition(position, 0, -1, -1, 0);
    }

    /**
     * Resolves a content-relative offset within a block to line/char
     * coordinates.
     */
    private static ResolvedPosition resolveInBlock(int flatPos, int blockIndex, FormattedBlock block, int contentOffset) {
        List<FormattedLine> lines = block.getLines();
        if ((lines != null) && !lines.isEmpty()) {
            int offset = 0;
            for (int li = 0; li < lines.size(); li++) {
                int lineLen = lines.get(li).length();
                if (contentOffset <= offset + lineLen)
                    return new ResolvedPosition(flatPos, blockIndex, li, contentOffset - offset, contentOffset);
                offset += lineLen + 1; // +1 for line break
            }
            // Past end of lines — clamp to end of last line.
            int lastLine = lines.size() - 1;
            return new ResolvedPosition(flatPos, blockIndex, lastLine, lines.get(lastLine).length(), contentOffset);
        }
        // Container block (TABLE, TROW) — resolve into child blocks.
        List<FormattedBlock> children = block.getBlocks();
        if ((children != null) && !children.isEmpty()) {
            int childPos = 0;
            for (int ci = 0; ci < children.size(); ci++) {
                int childNs = nodeSize(children.get(ci));
                if (contentOffset < childPos + childNs) {
                    // Recurse into child — but for top-level resolution, we return
                    // the block-level coordinates. Deep resolution into tables is
                    // deferred to Phase 6.
                    return new ResolvedPosition(flatPos, blockIndex, -1, -1, contentOffset);
                }
                childPos += childNs;
            }
        }
        return new ResolvedPosition(flatPos, blockIndex, -1, -1, contentOffset);
    }
}
