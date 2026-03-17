package com.effacy.jui.text.type.edit;

import com.effacy.jui.text.type.FormattedText;

/**
 * Represents a selection within the document.
 * <p>
 * A selection is defined by an anchor (where selection started) and a head
 * (where it ends). When both are equal, the selection is a cursor (insertion
 * point). Positions are expressed as block index + character offset within the
 * block, with methods to convert to/from flat document-wide positions.
 *
 * @param anchorBlock
 *                     block index where the selection started.
 * @param anchorOffset
 *                     character offset where the selection started.
 * @param headBlock
 *                     block index where the selection ends.
 * @param headOffset
 *                     character offset where the selection ends.
 */
public record Selection(int anchorBlock, int anchorOffset, int headBlock, int headOffset) {

    /**
     * Creates a cursor (insertion point) at the given position.
     *
     * @param blockIndex
     *                   the block index.
     * @param offset
     *                   character offset within the block.
     * @return the selection.
     */
    public static Selection cursor(int blockIndex, int offset) {
        return new Selection(blockIndex, offset, blockIndex, offset);
    }

    /**
     * Creates a range selection.
     *
     * @param anchorBlock
     *                     block index where the selection started.
     * @param anchorOffset
     *                     character offset where the selection started.
     * @param headBlock
     *                     block index where the selection ends.
     * @param headOffset
     *                     character offset where the selection ends.
     * @return the selection.
     */
    public static Selection range(int anchorBlock, int anchorOffset, int headBlock, int headOffset) {
        return new Selection(anchorBlock, anchorOffset, headBlock, headOffset);
    }

    /**
     * Determines if this selection is a cursor (no range).
     */
    public boolean isCursor() {
        return (anchorBlock == headBlock) && (anchorOffset == headOffset);
    }

    /**
     * Returns the block index of the start of the selection (the lesser of anchor
     * and head).
     */
    public int fromBlock() {
        if (anchorBlock < headBlock)
            return anchorBlock;
        if (anchorBlock > headBlock)
            return headBlock;
        return anchorBlock;
    }

    /**
     * Returns the character offset of the start of the selection.
     */
    public int fromOffset() {
        if (anchorBlock < headBlock)
            return anchorOffset;
        if (anchorBlock > headBlock)
            return headOffset;
        return Math.min(anchorOffset, headOffset);
    }

    /**
     * Returns the block index of the end of the selection.
     */
    public int toBlock() {
        if (anchorBlock > headBlock)
            return anchorBlock;
        if (anchorBlock < headBlock)
            return headBlock;
        return anchorBlock;
    }

    /**
     * Returns the character offset of the end of the selection.
     */
    public int toOffset() {
        if (anchorBlock > headBlock)
            return anchorOffset;
        if (anchorBlock < headBlock)
            return headOffset;
        return Math.max(anchorOffset, headOffset);
    }

    /**
     * Creates a new selection with block indices adjusted by a delta.
     *
     * @param blockDelta
     *                   the amount to shift block indices.
     * @return the adjusted selection.
     */
    public Selection adjustBlocks(int blockDelta) {
        return new Selection(
            anchorBlock + blockDelta, anchorOffset,
            headBlock + blockDelta, headOffset
        );
    }

    /**
     * Returns the anchor as a flat document position.
     *
     * @param doc
     *            the document.
     * @return the flat position.
     */
    public int anchorFlat(FormattedText doc) {
        return Positions.toFlat(doc, anchorBlock, anchorOffset);
    }

    /**
     * Returns the head as a flat document position.
     *
     * @param doc
     *            the document.
     * @return the flat position.
     */
    public int headFlat(FormattedText doc) {
        return Positions.toFlat(doc, headBlock, headOffset);
    }

    /**
     * Creates a selection from flat document positions.
     *
     * @param doc
     *               the document (used to resolve flat positions).
     * @param anchor
     *               anchor flat position.
     * @param head
     *               head flat position.
     * @return the selection.
     */
    public static Selection fromFlat(FormattedText doc, int anchor, int head) {
        ResolvedPosition a = Positions.resolve(doc, anchor);
        ResolvedPosition h = Positions.resolve(doc, head);
        int aOffset = Math.max(0, a.blockOffset());
        int hOffset = Math.max(0, h.blockOffset());
        return new Selection(a.blockIndex(), aOffset, h.blockIndex(), hOffset);
    }

    /**
     * Maps this selection through a position mapping, converting to flat
     * positions in the old document, mapping through the changes, and resolving
     * back using the new (mutated) document.
     *
     * @param mapping
     *                the position mapping from a transaction.
     * @param oldDoc
     *                the document before the transaction was applied (used to
     *                compute flat positions). If the document is mutated in
     *                place, pass a clone taken before apply.
     * @param newDoc
     *                the document after the transaction was applied (used to
     *                resolve mapped positions).
     * @return the mapped selection.
     */
    public Selection map(Mapping mapping, FormattedText oldDoc, FormattedText newDoc) {
        int anchorFlat = Positions.toFlat(oldDoc, anchorBlock, anchorOffset);
        int headFlat = Positions.toFlat(oldDoc, headBlock, headOffset);
        int newAnchor = mapping.map(anchorFlat, 1);
        int newHead = mapping.map(headFlat, 1);
        return Selection.fromFlat(newDoc, newAnchor, newHead);
    }

    @Override
    public String toString() {
        if (isCursor())
            return "Cursor[block=" + anchorBlock + ", offset=" + anchorOffset + "]";
        return "Range[anchor=(" + anchorBlock + "," + anchorOffset + "), head=(" + headBlock + "," + headOffset + ")]";
    }
}
