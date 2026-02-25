package com.effacy.jui.text.type.edit;

import java.util.List;
import java.util.Objects;

import com.effacy.jui.text.type.FormattedBlock;
import com.effacy.jui.text.type.FormattedBlock.BlockType;
import com.effacy.jui.text.type.FormattedLine;
import com.effacy.jui.text.type.FormattedLine.FormatType;
import com.effacy.jui.text.type.edit.step.ChangeFormatStep;
import com.effacy.jui.text.type.edit.step.DeleteBlockStep;
import com.effacy.jui.text.type.edit.step.DeleteTextStep;
import com.effacy.jui.text.type.edit.step.InsertBlockStep;
import com.effacy.jui.text.type.edit.step.InsertTextStep;
import com.effacy.jui.text.type.edit.step.JoinBlocksStep;
import com.effacy.jui.text.type.edit.step.MoveBlockStep;
import com.effacy.jui.text.type.edit.step.ReplaceBlockStep;
import com.effacy.jui.text.type.edit.step.SetBlockIndentStep;
import com.effacy.jui.text.type.edit.step.SetBlockMetaStep;
import com.effacy.jui.text.type.edit.step.SetBlockTypeStep;
import com.effacy.jui.text.type.edit.step.SplitBlockStep;
import com.effacy.jui.text.type.FormattedText;

/**
 * Factory methods that create {@link Transaction}s for common editing
 * operations.
 * <p>
 * Each method inspects the current {@link EditorState} (document + selection),
 * builds a transaction with the appropriate steps and explicit selection, and
 * returns it. Returns {@code null} when the operation cannot be performed.
 * <p>
 * The caller is responsible for applying the transaction via
 * {@link EditorState#apply(Transaction)} and pushing the inverse to
 * {@link History}.
 */
public final class Commands {

    private Commands() {}

    /**
     * Inserts text at the cursor position. If there is a range selection the
     * selected content is deleted first (supports both single-block and
     * multi-block selections).
     *
     * @param state
     *              the current editor state.
     * @param text
     *              the text to insert.
     * @return the transaction, or {@code null} if the text is empty.
     */
    public static Transaction insertText(EditorState state, String text) {
        if ((text == null) || text.isEmpty())
            return null;
        Selection sel = state.selection();
        Transaction tr = Transaction.create();

        int block = sel.anchorBlock();
        int offset = sel.anchorOffset();

        // Delete selection first if non-cursor.
        if (!sel.isCursor()) {
            block = sel.fromBlock();
            offset = sel.fromOffset();
            if (sel.fromBlock() == sel.toBlock()) {
                int len = sel.toOffset() - sel.fromOffset();
                tr.step(new DeleteTextStep(block, offset, len));
            } else {
                addDeleteRangeSteps(tr, state);
            }
        }

        tr.step(new InsertTextStep(block, offset, text));
        tr.setSelection(Selection.cursor(block, offset + text.length()));
        return tr;
    }

    /**
     * Deletes the character before the cursor (Backspace). With a range
     * selection, deletes the selection instead.
     *
     * @param state
     *              the current editor state.
     * @return the transaction, or {@code null} if at the start of the first
     *         block with no selection.
     */
    public static Transaction deleteCharBefore(EditorState state) {
        Selection sel = state.selection();

        // Range selection: delete selection.
        if (!sel.isCursor())
            return deleteSelection(state);

        int block = sel.anchorBlock();
        int offset = sel.anchorOffset();

        // Within a block: delete one character before cursor.
        if (offset > 0) {
            // Line break between lines cannot be removed by DeleteTextStep;
            // must join the two lines via ReplaceBlockStep instead.
            FormattedBlock blk = state.doc().getBlocks().get(block);
            if (charAt(blk, offset - 1) == '\n')
                return joinLinesAt(state, block, offset - 1);
            Transaction tr = Transaction.create();
            tr.step(new DeleteTextStep(block, offset - 1, 1));
            tr.setSelection(Selection.cursor(block, offset - 1));
            return tr;
        }

        // At start of block: join with previous.
        return joinWithPrevious(state);
    }

    /**
     * Deletes the character after the cursor (Delete key). With a range
     * selection, deletes the selection instead.
     *
     * @param state
     *              the current editor state.
     * @return the transaction, or {@code null} if at the end of the last block
     *         with no selection.
     */
    public static Transaction deleteCharAfter(EditorState state) {
        Selection sel = state.selection();

        // Range selection: delete selection.
        if (!sel.isCursor())
            return deleteSelection(state);

        int block = sel.anchorBlock();
        int offset = sel.anchorOffset();
        int contentSize = Positions.contentSize(state.doc().getBlocks().get(block));

        // Within a block: delete one character after cursor.
        if (offset < contentSize) {
            // Line break between lines cannot be removed by DeleteTextStep;
            // must join the two lines via ReplaceBlockStep instead.
            FormattedBlock blk = state.doc().getBlocks().get(block);
            if (charAt(blk, offset) == '\n')
                return joinLinesAt(state, block, offset);
            Transaction tr = Transaction.create();
            tr.step(new DeleteTextStep(block, offset, 1));
            tr.setSelection(Selection.cursor(block, offset));
            return tr;
        }

        // At end of block: join with next.
        return joinWithNext(state);
    }

    /**
     * Deletes the word before the cursor (Ctrl+Backspace). With a range
     * selection, deletes the selection instead.
     *
     * @param state
     *              the current editor state.
     * @return the transaction, or {@code null} if at the start of the first
     *         block with no selection.
     */
    public static Transaction deleteWordBefore(EditorState state) {
        Selection sel = state.selection();

        // Range selection: delete selection.
        if (!sel.isCursor())
            return deleteSelection(state);

        int block = sel.anchorBlock();
        int offset = sel.anchorOffset();

        // At start of block: join with previous.
        if (offset == 0)
            return joinWithPrevious(state);

        // Find word start and delete to there.
        int wordStart = findWordStart(state.doc().getBlocks().get(block), offset);
        int len = offset - wordStart;

        Transaction tr = Transaction.create();
        tr.step(new DeleteTextStep(block, wordStart, len));
        tr.setSelection(Selection.cursor(block, wordStart));
        return tr;
    }

    /**
     * Deletes the word after the cursor (Ctrl+Delete). With a range selection,
     * deletes the selection instead.
     *
     * @param state
     *              the current editor state.
     * @return the transaction, or {@code null} if at the end of the last block
     *         with no selection.
     */
    public static Transaction deleteWordAfter(EditorState state) {
        Selection sel = state.selection();

        // Range selection: delete selection.
        if (!sel.isCursor())
            return deleteSelection(state);

        int block = sel.anchorBlock();
        int offset = sel.anchorOffset();
        int contentSize = Positions.contentSize(state.doc().getBlocks().get(block));

        // At end of block: join with next.
        if (offset >= contentSize)
            return joinWithNext(state);

        // Find word end and delete to there.
        int wordEnd = findWordEnd(state.doc().getBlocks().get(block), offset);
        int len = wordEnd - offset;

        Transaction tr = Transaction.create();
        tr.step(new DeleteTextStep(block, offset, len));
        tr.setSelection(Selection.cursor(block, offset));
        return tr;
    }

    /**
     * Deletes the selected range. Supports both single-block and multi-block
     * selections.
     *
     * @param state
     *              the current editor state.
     * @return the transaction, or {@code null} if the selection is a cursor.
     */
    public static Transaction deleteSelection(EditorState state) {
        Selection sel = state.selection();
        if (sel.isCursor())
            return null;

        int fromBlock = sel.fromBlock();
        int fromOffset = sel.fromOffset();

        // Single-block: fast path.
        if (sel.fromBlock() == sel.toBlock()) {
            int len = sel.toOffset() - fromOffset;
            Transaction tr = Transaction.create();
            tr.step(new DeleteTextStep(fromBlock, fromOffset, len));
            tr.setSelection(Selection.cursor(fromBlock, fromOffset));
            return tr;
        }

        // Multi-block: replace endpoints, delete intermediates, join.
        Transaction tr = Transaction.create();
        addDeleteRangeSteps(tr, state);
        tr.setSelection(Selection.cursor(fromBlock, fromOffset));
        return tr;
    }

    /**
     * Inserts a line break within the current block (Shift+Enter). The cursor
     * stays in the same block; a new {@link FormattedLine} is created at the
     * cursor offset. With a range selection, deletes the selection first then
     * inserts the line break at the resulting cursor.
     *
     * @param state
     *              the current editor state.
     * @return the transaction.
     */
    public static Transaction insertLineBreak(EditorState state) {
        Selection sel = state.selection();
        List<FormattedBlock> blocks = state.doc().getBlocks();
        Transaction tr = Transaction.create();

        int block, offset;
        FormattedBlock result;

        if (sel.isCursor()) {
            block = sel.anchorBlock();
            offset = sel.anchorOffset();
            result = blocks.get(block).clone();
        } else if (sel.fromBlock() == sel.toBlock()) {
            // Single-block selection: clone and remove range.
            block = sel.fromBlock();
            offset = sel.fromOffset();
            result = blocks.get(block).clone();
            result.remove(offset, sel.toOffset() - offset);
        } else {
            // Multi-block selection: build combined block from left + right.
            block = sel.fromBlock();
            offset = sel.fromOffset();
            int toBlock = sel.toBlock();
            int toOffset = sel.toOffset();

            FormattedBlock leftPart = blocks.get(block).clone();
            leftPart.split(offset); // leftPart = [0..offset), discard right

            FormattedBlock lastClone = blocks.get(toBlock).clone();
            FormattedBlock rightPart = lastClone.split(toOffset);

            // Merge touching lines (same as deleteSelection) before splitting.
            mergeBlockContent(leftPart, rightPart);
            result = leftPart;

            // Delete extra blocks.
            for (int i = toBlock; i > block; i--)
                tr.step(new DeleteBlockStep(i));
        }

        // Insert a line break at offset within the result block.
        splitLineAt(result, offset);

        tr.step(new ReplaceBlockStep(block, result));
        tr.setSelection(Selection.cursor(block, offset + 1));
        return tr;
    }

    /**
     * Splits the block at the cursor position (Enter key). With a range
     * selection, deletes the selection first then splits at the resulting
     * cursor.
     *
     * @param state
     *              the current editor state.
     * @return the transaction.
     */
    public static Transaction splitBlock(EditorState state) {
        Selection sel = state.selection();
        Transaction tr = Transaction.create();

        int block;
        int offset;

        if (!sel.isCursor()) {
            block = sel.fromBlock();
            offset = sel.fromOffset();
            if (sel.fromBlock() == sel.toBlock()) {
                int len = sel.toOffset() - sel.fromOffset();
                tr.step(new DeleteTextStep(block, offset, len));
            } else {
                addDeleteRangeSteps(tr, state);
            }
        } else {
            block = sel.anchorBlock();
            offset = sel.anchorOffset();
        }

        tr.step(new SplitBlockStep(block, offset));
        tr.setSelection(Selection.cursor(block + 1, 0));
        return tr;
    }

    /**
     * Joins the current block with the previous block (Backspace at block
     * start).
     *
     * @param state
     *              the current editor state.
     * @return the transaction, or {@code null} if at the first block or the
     *         blocks have different types.
     */
    public static Transaction joinWithPrevious(EditorState state) {
        Selection sel = state.selection();
        int block = sel.anchorBlock();
        if (block <= 0)
            return null;
        if (state.doc().getBlocks().get(block - 1).getType() != state.doc().getBlocks().get(block).getType())
            return null;
        int prevContentSize = Positions.contentSize(state.doc().getBlocks().get(block - 1));
        Transaction tr = Transaction.create();
        tr.step(new JoinBlocksStep(block - 1));
        tr.setSelection(Selection.cursor(block - 1, prevContentSize));
        return tr;
    }

    /**
     * Joins the current block with the previous block, allowing different
     * block types. Same-type joins use the efficient {@link JoinBlocksStep};
     * cross-type joins clone the previous block, merge the current block's
     * content via {@link #mergeBlockContent}, and replace + delete.
     *
     * @param state
     *              the current editor state.
     * @return the transaction, or {@code null} if at the first block.
     */
    public static Transaction forceJoinWithPrevious(EditorState state) {
        Selection sel = state.selection();
        int block = sel.anchorBlock();
        if (block <= 0)
            return null;
        List<FormattedBlock> blocks = state.doc().getBlocks();
        FormattedBlock prevBlock = blocks.get(block - 1);
        FormattedBlock curBlock = blocks.get(block);
        int prevContentSize = Positions.contentSize(prevBlock);
        if (prevBlock.getType() == curBlock.getType()) {
            Transaction tr = Transaction.create();
            tr.step(new JoinBlocksStep(block - 1));
            tr.setSelection(Selection.cursor(block - 1, prevContentSize));
            return tr;
        }
        FormattedBlock merged = prevBlock.clone();
        mergeBlockContent(merged, curBlock);
        Transaction tr = Transaction.create();
        tr.step(new ReplaceBlockStep(block - 1, merged));
        tr.step(new DeleteBlockStep(block));
        tr.setSelection(Selection.cursor(block - 1, prevContentSize));
        return tr;
    }

    /**
     * Changes the type of the block at the cursor.
     *
     * @param state
     *              the current editor state.
     * @param type
     *              the new block type.
     * @return the transaction, or {@code null} if the block already has the
     *         given type.
     */
    public static Transaction setBlockType(EditorState state, BlockType type) {
        Selection sel = state.selection();
        int from = sel.isCursor() ? sel.anchorBlock() : sel.fromBlock();
        int to = sel.isCursor() ? sel.anchorBlock() : sel.toBlock();

        List<FormattedBlock> blocks = state.doc().getBlocks();
        boolean anyChanged = false;
        for (int i = from; i <= to; i++) {
            if (blocks.get(i).getType() != type) {
                anyChanged = true;
                break;
            }
        }
        if (!anyChanged)
            return null;

        Transaction tr = Transaction.create();
        for (int i = from; i <= to; i++) {
            if (blocks.get(i).getType() != type)
                tr.step(new SetBlockTypeStep(i, type));
        }
        tr.setSelection(state.selection());
        return tr;
    }

    /**
     * Increases the indent level of the block at the cursor.
     *
     * @param state
     *              the current editor state.
     * @return the transaction, or {@code null} if already at maximum indent
     *         (5).
     */
    public static Transaction indent(EditorState state) {
        Selection sel = state.selection();
        int from = sel.isCursor() ? sel.anchorBlock() : sel.fromBlock();
        int to = sel.isCursor() ? sel.anchorBlock() : sel.toBlock();

        List<FormattedBlock> blocks = state.doc().getBlocks();
        boolean anyChanged = false;
        for (int i = from; i <= to; i++) {
            if (blocks.get(i).getIndent() < 5) {
                anyChanged = true;
                break;
            }
        }
        if (!anyChanged)
            return null;

        Transaction tr = Transaction.create();
        for (int i = from; i <= to; i++) {
            int current = blocks.get(i).getIndent();
            if (current < 5)
                tr.step(new SetBlockIndentStep(i, current + 1));
        }
        tr.setSelection(state.selection());
        return tr;
    }

    /**
     * Decreases the indent level of the block at the cursor.
     *
     * @param state
     *              the current editor state.
     * @return the transaction, or {@code null} if already at minimum indent
     *         (0).
     */
    public static Transaction outdent(EditorState state) {
        Selection sel = state.selection();
        int from = sel.isCursor() ? sel.anchorBlock() : sel.fromBlock();
        int to = sel.isCursor() ? sel.anchorBlock() : sel.toBlock();

        List<FormattedBlock> blocks = state.doc().getBlocks();
        boolean anyChanged = false;
        for (int i = from; i <= to; i++) {
            if (blocks.get(i).getIndent() > 0) {
                anyChanged = true;
                break;
            }
        }
        if (!anyChanged)
            return null;

        Transaction tr = Transaction.create();
        for (int i = from; i <= to; i++) {
            int current = blocks.get(i).getIndent();
            if (current > 0)
                tr.step(new SetBlockIndentStep(i, current - 1));
        }
        tr.setSelection(state.selection());
        return tr;
    }

    /**
     * Moves a block and its indent-children (blocks immediately following with
     * a strictly greater indent level) up by one position. The moved group
     * swaps with the block (and its children) above it.
     * <p>
     * Example: blocks at indent levels [0, 1, 1, 0, 1] with cursor on block 3
     * (indent 0) moves blocks 3–4 above blocks 0–2.
     *
     * @param state
     *              the current editor state.
     * @param blockIndex
     *                   the index of the block to move.
     * @return the transaction, or {@code null} if the block is already at the
     *         top.
     */
    public static Transaction moveBlockUp(EditorState state, int blockIndex) {
        List<FormattedBlock> blocks = state.doc().getBlocks();
        if (blockIndex <= 0)
            return null;

        int count = 1 + countIndentChildren(blocks, blockIndex);
        int myIndent = blocks.get(blockIndex).getIndent();

        // Walk backward to find the group leader of the sibling above.
        // Skip children (indent > myIndent), stop at same indent (sibling).
        int aboveStart = blockIndex - 1;
        while ((aboveStart > 0) && (blocks.get(aboveStart).getIndent() > myIndent))
            aboveStart--;

        // If the block we landed on has a different indent, we can't move up
        // (we'd be crossing a parent boundary).
        if (blocks.get(aboveStart).getIndent() != myIndent)
            return null;

        Transaction tr = Transaction.create();
        tr.step(new MoveBlockStep(blockIndex, aboveStart, count));
        // Selection follows the moved block.
        tr.setSelection(Selection.cursor(aboveStart, state.selection().anchorOffset()));
        return tr;
    }

    /**
     * Moves a block and its indent-children down by one position. The moved
     * group swaps with the block (and its children) below it.
     *
     * @param state
     *              the current editor state.
     * @param blockIndex
     *                   the index of the block to move.
     * @return the transaction, or {@code null} if the block (with children) is
     *         already at the bottom.
     */
    public static Transaction moveBlockDown(EditorState state, int blockIndex) {
        List<FormattedBlock> blocks = state.doc().getBlocks();
        int count = 1 + countIndentChildren(blocks, blockIndex);
        int afterGroup = blockIndex + count;
        if (afterGroup >= blocks.size())
            return null;

        // Count the group below (the block after our group and its children).
        int belowCount = 1 + countIndentChildren(blocks, afterGroup);
        int target = blockIndex + belowCount;

        Transaction tr = Transaction.create();
        tr.step(new MoveBlockStep(blockIndex, target, count));
        tr.setSelection(Selection.cursor(target, state.selection().anchorOffset()));
        return tr;
    }

    /**
     * Counts the number of indent-children following a block. These are
     * consecutive blocks immediately after {@code blockIndex} that have a
     * strictly greater indent level.
     */
    static int countIndentChildren(List<FormattedBlock> blocks, int blockIndex) {
        int baseIndent = blocks.get(blockIndex).getIndent();
        int children = 0;
        for (int i = blockIndex + 1; i < blocks.size(); i++) {
            if (blocks.get(i).getIndent() <= baseIndent)
                break;
            children++;
        }
        return children;
    }

    /**
     * Joins the current block with the next block (Delete at block end).
     *
     * @param state
     *              the current editor state.
     * @return the transaction, or {@code null} if at the last block or the
     *         blocks have different types.
     */
    public static Transaction joinWithNext(EditorState state) {
        Selection sel = state.selection();
        int block = sel.anchorBlock();
        if (block >= state.doc().getBlocks().size() - 1)
            return null;
        if (state.doc().getBlocks().get(block).getType() != state.doc().getBlocks().get(block + 1).getType())
            return null;
        int currentContentSize = Positions.contentSize(state.doc().getBlocks().get(block));
        Transaction tr = Transaction.create();
        tr.step(new JoinBlocksStep(block));
        tr.setSelection(Selection.cursor(block, currentContentSize));
        return tr;
    }

    /**
     * Joins the current block with the next block, allowing different block
     * types. Same-type joins use the efficient {@link JoinBlocksStep};
     * cross-type joins clone the current block, merge the next block's
     * content via {@link #mergeBlockContent}, and replace + delete.
     *
     * @param state
     *              the current editor state.
     * @return the transaction, or {@code null} if at the last block.
     */
    public static Transaction forceJoinWithNext(EditorState state) {
        Selection sel = state.selection();
        int block = sel.anchorBlock();
        List<FormattedBlock> blocks = state.doc().getBlocks();
        if (block >= blocks.size() - 1)
            return null;
        FormattedBlock curBlock = blocks.get(block);
        FormattedBlock nextBlock = blocks.get(block + 1);
        int currentContentSize = Positions.contentSize(curBlock);
        if (curBlock.getType() == nextBlock.getType()) {
            Transaction tr = Transaction.create();
            tr.step(new JoinBlocksStep(block));
            tr.setSelection(Selection.cursor(block, currentContentSize));
            return tr;
        }
        FormattedBlock merged = curBlock.clone();
        mergeBlockContent(merged, nextBlock);
        Transaction tr = Transaction.create();
        tr.step(new ReplaceBlockStep(block, merged));
        tr.step(new DeleteBlockStep(block + 1));
        tr.setSelection(Selection.cursor(block, currentContentSize));
        return tr;
    }

    /**
     * Adds a format type to the selected range. Supports multi-block
     * selections by adding one step per affected block.
     *
     * @param state
     *              the current editor state.
     * @param type
     *              the format type to add.
     * @return the transaction, or {@code null} if the selection is a cursor.
     */
    public static Transaction applyFormat(EditorState state, FormatType type) {
        Selection sel = state.selection();
        if (sel.isCursor())
            return null;
        List<FormattedBlock> blocks = state.doc().getBlocks();
        int fromBlock = sel.fromBlock();
        int toBlock = sel.toBlock();
        Transaction tr = Transaction.create();
        for (int i = fromBlock; i <= toBlock; i++) {
            int start = (i == fromBlock) ? sel.fromOffset() : 0;
            int end = (i == toBlock) ? sel.toOffset() : Positions.contentSize(blocks.get(i));
            int len = end - start;
            if (len > 0)
                tr.step(new ChangeFormatStep(i, start, len, type, true));
        }
        tr.setSelection(state.selection());
        return tr;
    }

    /**
     * Removes a format type from the selected range. Supports multi-block
     * selections by adding one step per affected block.
     *
     * @param state
     *              the current editor state.
     * @param type
     *              the format type to remove.
     * @return the transaction, or {@code null} if the selection is a cursor.
     */
    public static Transaction removeFormat(EditorState state, FormatType type) {
        Selection sel = state.selection();
        if (sel.isCursor())
            return null;
        List<FormattedBlock> blocks = state.doc().getBlocks();
        int fromBlock = sel.fromBlock();
        int toBlock = sel.toBlock();
        Transaction tr = Transaction.create();
        for (int i = fromBlock; i <= toBlock; i++) {
            int start = (i == fromBlock) ? sel.fromOffset() : 0;
            int end = (i == toBlock) ? sel.toOffset() : Positions.contentSize(blocks.get(i));
            int len = end - start;
            if (len > 0)
                tr.step(new ChangeFormatStep(i, start, len, type, false));
        }
        tr.setSelection(state.selection());
        return tr;
    }

    /**
     * Toggles a format type on the selected range. If the entire range already
     * has the format it is removed; otherwise it is added. Supports multi-block
     * selections by adding one step per affected block.
     *
     * @param state
     *              the current editor state.
     * @param type
     *              the format type to toggle.
     * @return the transaction, or {@code null} if the selection is a cursor.
     */
    public static Transaction toggleFormat(EditorState state, FormatType type) {
        Selection sel = state.selection();
        if (sel.isCursor())
            return null;

        List<FormattedBlock> blocks = state.doc().getBlocks();
        int fromBlock = sel.fromBlock();
        int fromOffset = sel.fromOffset();
        int toBlock = sel.toBlock();
        int toOffset = sel.toOffset();

        // Check if the entire range has the format.
        boolean active = true;
        for (int i = fromBlock; i <= toBlock; i++) {
            int start = (i == fromBlock) ? fromOffset : 0;
            int end = (i == toBlock) ? toOffset : Positions.contentSize(blocks.get(i));
            int len = end - start;
            if ((len > 0) && !blocks.get(i).hasFormat(start, len, type)) {
                active = false;
                break;
            }
        }

        boolean add = !active;
        Transaction tr = Transaction.create();
        for (int i = fromBlock; i <= toBlock; i++) {
            int start = (i == fromBlock) ? fromOffset : 0;
            int end = (i == toBlock) ? toOffset : Positions.contentSize(blocks.get(i));
            int len = end - start;
            if (len > 0)
                tr.step(new ChangeFormatStep(i, start, len, type, add));
        }
        tr.setSelection(state.selection());
        return tr;
    }

    /**
     * Appends the steps needed to delete a multi-block selection range to the
     * given transaction. The strategy is: replace the last block with its
     * right portion (from toOffset onward), delete intermediate blocks in
     * reverse order, replace the first block with its left portion (up to
     * fromOffset), then join if the endpoint blocks have the same type.
     * <p>
     * After these steps the cursor should be at
     * {@code (fromBlock, fromOffset)}.
     */
    private static void addDeleteRangeSteps(Transaction tr, EditorState state) {
        Selection sel = state.selection();
        List<FormattedBlock> blocks = state.doc().getBlocks();
        int fromBlock = sel.fromBlock();
        int fromOffset = sel.fromOffset();
        int toBlock = sel.toBlock();
        int toOffset = sel.toOffset();

        // 1. Replace last block with its right portion.
        FormattedBlock lastClone = blocks.get(toBlock).clone();
        FormattedBlock lastRight = lastClone.split(toOffset);
        tr.step(new ReplaceBlockStep(toBlock, lastRight));

        // 2. Delete intermediate blocks (reverse order keeps indices stable).
        for (int i = toBlock - 1; i > fromBlock; i--)
            tr.step(new DeleteBlockStep(i));

        // 3. Replace first block with its left portion.
        FormattedBlock firstLeft = blocks.get(fromBlock).clone();
        firstLeft.split(fromOffset);
        tr.step(new ReplaceBlockStep(fromBlock, firstLeft));

        // 4. Join if same type.
        if (blocks.get(fromBlock).getType() == blocks.get(toBlock).getType())
            tr.step(new JoinBlocksStep(fromBlock));
    }

    /************************************************************************
     * Block structural commands.
     ************************************************************************/

    /**
     * Inserts a new empty block after the given block index.
     *
     * @param state
     *              the current editor state.
     * @param blockIndex
     *                   the index after which to insert.
     * @param blockType
     *                   the type of the new block.
     * @return the transaction, or {@code null} if the arguments are invalid.
     */
    public static Transaction insertBlockAfter(EditorState state, int blockIndex, BlockType blockType) {
        if (blockType == null)
            return null;
        List<FormattedBlock> blocks = state.doc().getBlocks();
        if ((blockIndex < 0) || (blockIndex >= blocks.size()))
            return null;
        FormattedBlock newBlock = new FormattedBlock(blockType);
        newBlock.line("");
        Transaction tr = Transaction.create();
        tr.step(new InsertBlockStep(blockIndex + 1, newBlock));
        tr.setSelection(Selection.cursor(blockIndex + 1, 0));
        return tr;
    }

    /**
     * Inserts a new empty block before the given block index.
     *
     * @param state
     *              the current editor state.
     * @param blockIndex
     *                   the index before which to insert.
     * @param blockType
     *                   the type of the new block.
     * @return the transaction, or {@code null} if the arguments are invalid.
     */
    public static Transaction insertBlockBefore(EditorState state, int blockIndex, BlockType blockType) {
        if (blockType == null)
            return null;
        List<FormattedBlock> blocks = state.doc().getBlocks();
        if ((blockIndex < 0) || (blockIndex >= blocks.size()))
            return null;
        FormattedBlock newBlock = new FormattedBlock(blockType);
        newBlock.line("");
        Transaction tr = Transaction.create();
        tr.step(new InsertBlockStep(blockIndex, newBlock));
        tr.setSelection(Selection.cursor(blockIndex, 0));
        return tr;
    }

    /**
     * Deletes the block at the given index. Cannot delete the last remaining
     * block.
     *
     * @param state
     *              the current editor state.
     * @param blockIndex
     *                   the index of the block to delete.
     * @return the transaction, or {@code null} if the index is invalid or only
     *         one block remains.
     */
    public static Transaction deleteBlock(EditorState state, int blockIndex) {
        List<FormattedBlock> blocks = state.doc().getBlocks();
        if ((blockIndex < 0) || (blockIndex >= blocks.size()))
            return null;
        if (blocks.size() <= 1)
            return null;
        Transaction tr = Transaction.create();
        tr.step(new DeleteBlockStep(blockIndex));
        int newBlock = (blockIndex > 0) ? blockIndex - 1 : 0;
        tr.setSelection(Selection.cursor(newBlock, 0));
        return tr;
    }

    /**
     * Replaces the block at the given index with a new block.
     *
     * @param state
     *              the current editor state.
     * @param blockIndex
     *                   the index of the block to replace.
     * @param replacement
     *                    the replacement block.
     * @return the transaction, or {@code null} if the arguments are invalid.
     */
    public static Transaction replaceBlock(EditorState state, int blockIndex, FormattedBlock replacement) {
        if (replacement == null)
            return null;
        List<FormattedBlock> blocks = state.doc().getBlocks();
        if ((blockIndex < 0) || (blockIndex >= blocks.size()))
            return null;
        Transaction tr = Transaction.create();
        tr.step(new ReplaceBlockStep(blockIndex, replacement));
        tr.setSelection(Selection.cursor(blockIndex, 0));
        return tr;
    }

    /************************************************************************
     * Link commands.
     ************************************************************************/

    /**
     * Applies a link (FormatType.A with URL metadata) to the selected range.
     * Supports multi-block selections.
     *
     * @param state
     *              the current editor state.
     * @param url
     *              the link URL.
     * @return the transaction, or {@code null} if the selection is a cursor or
     *         the URL is empty.
     */
    public static Transaction applyLink(EditorState state, String url) {
        if ((url == null) || url.isEmpty())
            return null;
        Selection sel = state.selection();
        if (sel.isCursor())
            return null;
        List<FormattedBlock> blocks = state.doc().getBlocks();
        int fromBlock = sel.fromBlock();
        int toBlock = sel.toBlock();
        Transaction tr = Transaction.create();
        for (int i = fromBlock; i <= toBlock; i++) {
            int start = (i == fromBlock) ? sel.fromOffset() : 0;
            int end = (i == toBlock) ? sel.toOffset() : Positions.contentSize(blocks.get(i));
            int len = end - start;
            if (len > 0) {
                FormattedBlock clone = blocks.get(i).clone();
                clone.addFormat(start, len, FormatType.A);
                setLinkMetaOnRange(clone, start, len, url);
                tr.step(new ReplaceBlockStep(i, clone));
            }
        }
        tr.setSelection(state.selection());
        return tr;
    }

    /**
     * Removes a link (FormatType.A and link metadata) from the selected range.
     * Supports multi-block selections.
     *
     * @param state
     *              the current editor state.
     * @return the transaction, or {@code null} if the selection is a cursor.
     */
    public static Transaction removeLink(EditorState state) {
        Selection sel = state.selection();
        if (sel.isCursor())
            return null;
        List<FormattedBlock> blocks = state.doc().getBlocks();
        int fromBlock = sel.fromBlock();
        int toBlock = sel.toBlock();
        Transaction tr = Transaction.create();
        for (int i = fromBlock; i <= toBlock; i++) {
            int start = (i == fromBlock) ? sel.fromOffset() : 0;
            int end = (i == toBlock) ? sel.toOffset() : Positions.contentSize(blocks.get(i));
            int len = end - start;
            if (len > 0) {
                FormattedBlock clone = blocks.get(i).clone();
                clearLinkMetaOnRange(clone, start, len);
                clone.removeFormat(start, len, FormatType.A);
                tr.step(new ReplaceBlockStep(i, clone));
            }
        }
        tr.setSelection(state.selection());
        return tr;
    }

    /**
     * Updates (or applies) a link on the selected range. If a link already
     * exists it is replaced with the new URL; if none exists the link is added.
     * Supports multi-block selections.
     *
     * @param state
     *              the current editor state.
     * @param newUrl
     *              the new link URL.
     * @return the transaction, or {@code null} if the selection is a cursor or
     *         the URL is empty.
     */
    public static Transaction updateLink(EditorState state, String newUrl) {
        if ((newUrl == null) || newUrl.isEmpty())
            return null;
        Selection sel = state.selection();
        if (sel.isCursor())
            return null;
        List<FormattedBlock> blocks = state.doc().getBlocks();
        int fromBlock = sel.fromBlock();
        int toBlock = sel.toBlock();
        Transaction tr = Transaction.create();
        for (int i = fromBlock; i <= toBlock; i++) {
            int start = (i == fromBlock) ? sel.fromOffset() : 0;
            int end = (i == toBlock) ? sel.toOffset() : Positions.contentSize(blocks.get(i));
            int len = end - start;
            if (len > 0) {
                FormattedBlock clone = blocks.get(i).clone();
                clearLinkMetaOnRange(clone, start, len);
                clone.removeFormat(start, len, FormatType.A);
                clone.addFormat(start, len, FormatType.A);
                setLinkMetaOnRange(clone, start, len, newUrl);
                tr.step(new ReplaceBlockStep(i, clone));
            }
        }
        tr.setSelection(state.selection());
        return tr;
    }

    /**
     * Sets the {@code link} metadata on all {@link FormatType#A} format regions
     * in the block that overlap {@code [start, start+len)}.
     * <p>
     * Defensively copies each format's meta map before mutating it, since
     * {@link FormattedLine#clone()} shares meta maps by reference.
     */
    private static void setLinkMetaOnRange(FormattedBlock block, int start, int len, String url) {
        int end = start + len;
        int lineStart = 0;
        for (FormattedLine line : block.getLines()) {
            for (FormattedLine.Format fmt : line.getFormatting()) {
                int absStart = lineStart + fmt.getIndex();
                int absEnd = absStart + fmt.getLength();
                if ((absEnd > start) && (absStart < end) && fmt.getFormats().contains(FormatType.A)) {
                    fmt.setMeta(new java.util.HashMap<>(fmt.getMeta()));
                    fmt.getMeta().put(FormattedLine.META_LINK, url);
                }
            }
            lineStart += line.length() + 1;
        }
    }

    /**
     * Clears the {@code link} metadata from all format regions in the block
     * that overlap {@code [start, start+len)}.
     * <p>
     * Defensively copies each format's meta map before mutating it, since
     * {@link FormattedLine#clone()} shares meta maps by reference.
     */
    private static void clearLinkMetaOnRange(FormattedBlock block, int start, int len) {
        int end = start + len;
        int lineStart = 0;
        for (FormattedLine line : block.getLines()) {
            for (FormattedLine.Format fmt : line.getFormatting()) {
                int absStart = lineStart + fmt.getIndex();
                int absEnd = absStart + fmt.getLength();
                if ((absEnd > start) && (absStart < end)) {
                    fmt.setMeta(new java.util.HashMap<>(fmt.getMeta()));
                    fmt.getMeta().remove(FormattedLine.META_LINK);
                }
            }
            lineStart += line.length() + 1;
        }
    }

    /************************************************************************
     * Block metadata command.
     ************************************************************************/

    /**
     * Sets or removes a metadata entry on the block at the cursor.
     *
     * @param state
     *              the current editor state.
     * @param key
     *              the metadata key.
     * @param value
     *              the value to set, or {@code null} to remove the key.
     * @return the transaction, or {@code null} if the key is empty or the
     *         value is unchanged.
     */
    public static Transaction setBlockMeta(EditorState state, String key, String value) {
        if ((key == null) || key.isEmpty())
            return null;
        int block = state.selection().anchorBlock();
        String current = state.doc().getBlocks().get(block).getMeta().get(key);
        if (Objects.equals(current, value))
            return null;
        Transaction tr = Transaction.create();
        tr.step(new SetBlockMetaStep(block, key, value));
        tr.setSelection(state.selection());
        return tr;
    }

    /************************************************************************
     * Clear formatting.
     ************************************************************************/

    /**
     * Removes all inline formatting from the selected range. Supports
     * multi-block selections. Each affected block is cloned, stripped, and
     * replaced via {@link ReplaceBlockStep} so undo restores the original.
     *
     * @param state
     *              the current editor state.
     * @return the transaction, or {@code null} if the selection is a cursor.
     */
    public static Transaction clearFormatting(EditorState state) {
        Selection sel = state.selection();
        if (sel.isCursor())
            return null;
        List<FormattedBlock> blocks = state.doc().getBlocks();
        int fromBlock = sel.fromBlock();
        int toBlock = sel.toBlock();
        Transaction tr = Transaction.create();
        for (int i = fromBlock; i <= toBlock; i++) {
            int start = (i == fromBlock) ? sel.fromOffset() : 0;
            int end = (i == toBlock) ? sel.toOffset() : Positions.contentSize(blocks.get(i));
            int len = end - start;
            if (len > 0) {
                FormattedBlock clone = blocks.get(i).clone();
                stripFormattingInRange(clone, start, len);
                tr.step(new ReplaceBlockStep(i, clone));
            }
        }
        tr.setSelection(state.selection());
        return tr;
    }

    /************************************************************************
     * Block utilities.
     ************************************************************************/

    /**
     * Duplicates the block at the given index, inserting the clone immediately
     * after it.
     *
     * @param state
     *              the current editor state.
     * @param blockIndex
     *                   the index of the block to duplicate.
     * @return the transaction, or {@code null} if the index is invalid.
     */
    public static Transaction duplicateBlock(EditorState state, int blockIndex) {
        List<FormattedBlock> blocks = state.doc().getBlocks();
        if ((blockIndex < 0) || (blockIndex >= blocks.size()))
            return null;
        FormattedBlock clone = blocks.get(blockIndex).clone();
        Transaction tr = Transaction.create();
        tr.step(new InsertBlockStep(blockIndex + 1, clone));
        tr.setSelection(Selection.cursor(blockIndex + 1, 0));
        return tr;
    }

    /**
     * Toggles the block type. If the cursor block already has {@code type} it
     * is changed to {@code PARA}; otherwise it is changed to {@code type}. For
     * range selections: if all blocks in the range already have {@code type},
     * all are reverted to {@code PARA}; otherwise all are set to {@code type}.
     *
     * @param state
     *              the current editor state.
     * @param type
     *              the block type to toggle.
     * @return the transaction, or {@code null} if nothing would change.
     */
    public static Transaction toggleBlockType(EditorState state, BlockType type) {
        Selection sel = state.selection();
        int from = sel.isCursor() ? sel.anchorBlock() : sel.fromBlock();
        int to = sel.isCursor() ? sel.anchorBlock() : sel.toBlock();
        List<FormattedBlock> blocks = state.doc().getBlocks();

        boolean allMatch = true;
        for (int i = from; i <= to; i++) {
            if (blocks.get(i).getType() != type) {
                allMatch = false;
                break;
            }
        }
        BlockType target = allMatch ? BlockType.PARA : type;

        boolean anyChanged = false;
        for (int i = from; i <= to; i++) {
            if (blocks.get(i).getType() != target) {
                anyChanged = true;
                break;
            }
        }
        if (!anyChanged)
            return null;

        Transaction tr = Transaction.create();
        for (int i = from; i <= to; i++) {
            if (blocks.get(i).getType() != target)
                tr.step(new SetBlockTypeStep(i, target));
        }
        tr.setSelection(state.selection());
        return tr;
    }

    /************************************************************************
     * Select all.
     ************************************************************************/

    /**
     * Selects the entire document. Returns a step-less transaction that only
     * changes the selection.
     *
     * @param state
     *              the current editor state.
     * @return the transaction, or {@code null} if the document is empty or the
     *         selection already covers everything.
     */
    public static Transaction selectAll(EditorState state) {
        List<FormattedBlock> blocks = state.doc().getBlocks();
        if (blocks.isEmpty())
            return null;
        int lastBlock = blocks.size() - 1;
        int lastOffset = Positions.contentSize(blocks.get(lastBlock));
        Selection current = state.selection();
        if (!current.isCursor()
            && (current.fromBlock() == 0) && (current.fromOffset() == 0)
            && (current.toBlock() == lastBlock) && (current.toOffset() == lastOffset))
            return null;
        Transaction tr = Transaction.create();
        tr.setSelection(Selection.range(0, 0, lastBlock, lastOffset));
        return tr;
    }

    /************************************************************************
     * Clipboard operations.
     ************************************************************************/

    /**
     * Extracts the formatted content of the current selection as a new
     * {@link FormattedText}. This is a read-only query — it does not modify
     * the document or produce a transaction.
     *
     * @param state
     *              the current editor state.
     * @return the extracted content, or {@code null} if the selection is a
     *         cursor.
     */
    public static FormattedText extractSelection(EditorState state) {
        Selection sel = state.selection();
        if (sel.isCursor())
            return null;
        List<FormattedBlock> blocks = state.doc().getBlocks();
        int fromBlock = sel.fromBlock();
        int fromOffset = sel.fromOffset();
        int toBlock = sel.toBlock();
        int toOffset = sel.toOffset();

        FormattedText result = new FormattedText();

        if (fromBlock == toBlock) {
            // Single-block: clone, trim right, trim left.
            FormattedBlock clone = blocks.get(fromBlock).clone();
            clone.split(toOffset); // discard right; clone is [0..toOffset)
            FormattedBlock extracted = clone.split(fromOffset); // extracted is [fromOffset..toOffset)
            result.getBlocks().add(extracted);
            return result;
        }

        // Multi-block: first, middles, last.
        FormattedBlock firstClone = blocks.get(fromBlock).clone();
        FormattedBlock firstRight = firstClone.split(fromOffset);
        result.getBlocks().add(firstRight);

        for (int i = fromBlock + 1; i < toBlock; i++)
            result.getBlocks().add(blocks.get(i).clone());

        FormattedBlock lastClone = blocks.get(toBlock).clone();
        lastClone.split(toOffset); // discard right; lastClone is [0..toOffset)
        result.getBlocks().add(lastClone);

        return result;
    }

    /**
     * Inserts clipboard content at the cursor. If there is a range selection
     * the selected content is replaced. Supports single-block and multi-block
     * paste content.
     * <p>
     * For selection replacement, the result blocks are built directly from the
     * original document state (rather than using separate delete + paste
     * steps) to avoid stale-state issues when steps execute sequentially.
     *
     * @param state
     *              the current editor state.
     * @param content
     *              the content to paste.
     * @return the transaction, or {@code null} if the content is null or
     *         empty.
     */
    public static Transaction paste(EditorState state, FormattedText content) {
        if ((content == null) || content.getBlocks().isEmpty())
            return null;
        List<FormattedBlock> pastedBlocks = content.getBlocks();
        List<FormattedBlock> blocks = state.doc().getBlocks();
        Selection sel = state.selection();
        Transaction tr = Transaction.create();
        int N = pastedBlocks.size();

        // Determine the left and right remnants around the insertion point.
        int fromBlock, fromOffset;
        FormattedBlock leftPart;
        FormattedBlock rightPart;

        if (sel.isCursor()) {
            fromBlock = sel.anchorBlock();
            fromOffset = sel.anchorOffset();
            leftPart = blocks.get(fromBlock).clone();
            rightPart = leftPart.split(fromOffset);
        } else {
            fromBlock = sel.fromBlock();
            fromOffset = sel.fromOffset();
            int toBlock = sel.toBlock();
            int toOffset = sel.toOffset();

            // Left remnant: fromBlock[0..fromOffset).
            leftPart = blocks.get(fromBlock).clone();
            leftPart.split(fromOffset); // discard right; leftPart is [0..fromOffset)

            // Right remnant: toBlock[toOffset..end).
            FormattedBlock lastSource = blocks.get(toBlock).clone();
            rightPart = lastSource.split(toOffset); // rightPart is [toOffset..end)

            // Delete blocks between fromBlock and toBlock (reverse order).
            for (int i = toBlock; i > fromBlock; i--)
                tr.step(new DeleteBlockStep(i));
        }

        // Merge first pasted block into leftPart.
        mergeBlockContent(leftPart, pastedBlocks.get(0).clone());

        if (N == 1) {
            int newOffset = Positions.contentSize(leftPart);
            mergeBlockContent(leftPart, rightPart);
            tr.step(new ReplaceBlockStep(fromBlock, leftPart));
            tr.setSelection(Selection.cursor(fromBlock, newOffset));
        } else {
            FormattedBlock lastPasted = pastedBlocks.get(N - 1).clone();
            int cursorOffset = Positions.contentSize(lastPasted);
            mergeBlockContent(lastPasted, rightPart);
            lastPasted.setType(rightPart.getType());

            tr.step(new ReplaceBlockStep(fromBlock, leftPart));
            for (int i = 1; i < N - 1; i++)
                tr.step(new InsertBlockStep(fromBlock + i, pastedBlocks.get(i).clone()));
            tr.step(new InsertBlockStep(fromBlock + N - 1, lastPasted));
            tr.setSelection(Selection.cursor(fromBlock + N - 1, cursorOffset));
        }
        return tr;
    }

    /**
     * Pastes plain text at the cursor. The text is split on newlines into
     * separate PARA blocks and delegated to {@link #paste(EditorState, FormattedText)}.
     *
     * @param state
     *              the current editor state.
     * @param text
     *              the plain text to paste.
     * @return the transaction, or {@code null} if the text is null or empty.
     */
    public static Transaction pasteText(EditorState state, String text) {
        if ((text == null) || text.isEmpty())
            return null;
        FormattedText content = new FormattedText();
        for (String line : text.split("\n", -1)) {
            if (!line.isEmpty())
                content.block(BlockType.PARA, b -> b.line(line));
        }
        if (content.getBlocks().isEmpty())
            return null;
        return paste(state, content);
    }

    /************************************************************************
     * Block content helpers (package-private for testability).
     ************************************************************************/

    /**
     * Appends the line content of {@code source} into {@code target}, merging
     * the first line of source into the last line of target and adding
     * remaining lines. This is equivalent to {@link FormattedBlock#merge(FormattedBlock)}
     * but without the block type check.
     *
     * @param target
     *               the block to append into (mutated in place).
     * @param source
     *               the block whose content is appended.
     */
    static void mergeBlockContent(FormattedBlock target, FormattedBlock source) {
        if ((source == null) || source.getLines().isEmpty())
            return;
        if (target.getLines().isEmpty()) {
            for (FormattedLine line : source.getLines())
                target.getLines().add(line.clone());
            return;
        }
        List<FormattedLine> sourceLines = source.getLines();
        target.lastLine().merge(sourceLines.get(0));
        for (int i = 1; i < sourceLines.size(); i++)
            target.getLines().add(sourceLines.get(i).clone());
    }

    /************************************************************************
     * Formatting helpers (package-private for testability).
     ************************************************************************/

    /**
     * Strips all inline formatting from a range within a block, mutating the
     * block in place. Format entries fully inside the range are removed;
     * entries partially overlapping are trimmed; entries spanning the range
     * are split into left and right portions.
     *
     * @param block
     *              the block to mutate (should be a clone).
     * @param start
     *              block-level start offset.
     * @param len
     *              number of characters to strip.
     */
    static void stripFormattingInRange(FormattedBlock block, int start, int len) {
        int end = start + len;
        int lineStart = 0;
        for (FormattedLine line : block.getLines()) {
            int lineLen = line.length();
            int lineEnd = lineStart + lineLen;
            if ((lineEnd > start) && (lineStart < end)) {
                int rangeStart = Math.max(0, start - lineStart);
                int rangeEnd = Math.min(lineLen, end - lineStart);
                List<FormattedLine.Format> kept = new java.util.ArrayList<>();
                for (FormattedLine.Format fmt : line.getFormatting()) {
                    int fStart = fmt.getIndex();
                    int fEnd = fStart + fmt.getLength();
                    if ((fEnd <= rangeStart) || (fStart >= rangeEnd)) {
                        // No overlap: keep.
                        kept.add(fmt);
                    } else if ((fStart >= rangeStart) && (fEnd <= rangeEnd)) {
                        // Fully contained: remove.
                    } else if ((fStart < rangeStart) && (fEnd > rangeEnd)) {
                        // Spanning: split into left and right.
                        kept.add(copyFormat(fmt, fStart, rangeStart - fStart));
                        kept.add(copyFormat(fmt, rangeEnd, fEnd - rangeEnd));
                    } else if (fStart < rangeStart) {
                        // Left portion survives.
                        kept.add(copyFormat(fmt, fStart, rangeStart - fStart));
                    } else {
                        // Right portion survives.
                        kept.add(copyFormat(fmt, rangeEnd, fEnd - rangeEnd));
                    }
                }
                line.getFormatting().clear();
                line.getFormatting().addAll(kept);
            }
            lineStart = lineEnd + 1;
        }
    }

    /**
     * Creates a copy of a format entry with new index and length, preserving
     * the format types and metadata.
     */
    private static FormattedLine.Format copyFormat(FormattedLine.Format src, int newIndex, int newLength) {
        FormattedLine.Format copy = new FormattedLine.Format(newIndex, newLength, src.formats());
        if ((src.getMeta() != null) && !src.getMeta().isEmpty())
            copy.setMeta(new java.util.HashMap<>(src.getMeta()));
        return copy;
    }

    /************************************************************************
     * Line break helpers (package-private for testability).
     ************************************************************************/

    /**
     * Joins two adjacent lines at a line break position within a block,
     * effectively deleting the line break. The cursor is placed at the
     * join point.
     *
     * @param state
     *              the current editor state.
     * @param blockIdx
     *                 the block index.
     * @param breakOffset
     *                    the block-level character offset of the line break.
     * @return the transaction.
     */
    private static Transaction joinLinesAt(EditorState state, int blockIdx, int breakOffset) {
        FormattedBlock clone = state.doc().getBlocks().get(blockIdx).clone();
        List<FormattedLine> lines = clone.getLines();
        int pos = 0;
        for (int i = 0; i < lines.size() - 1; i++) {
            pos += lines.get(i).length();
            if (pos == breakOffset) {
                lines.get(i).merge(lines.get(i + 1));
                lines.remove(i + 1);
                break;
            }
            pos++; // line break
        }
        Transaction tr = Transaction.create();
        tr.step(new ReplaceBlockStep(blockIdx, clone));
        tr.setSelection(Selection.cursor(blockIdx, breakOffset));
        return tr;
    }

    /**
     * Splits the line at the given block-level character offset, inserting a
     * line break. The block is mutated in place: the line containing the
     * offset is split into two adjacent lines.
     *
     * @param block
     *              the block to mutate (should be a clone).
     * @param offset
     *               block-level character offset at which to insert the break.
     */
    static void splitLineAt(FormattedBlock block, int offset) {
        List<FormattedLine> lines = block.getLines();
        if (lines.isEmpty()) {
            lines.add(new FormattedLine());
            lines.add(new FormattedLine());
            return;
        }
        int remaining = offset;
        for (int i = 0; i < lines.size(); i++) {
            FormattedLine line = lines.get(i);
            if (remaining <= line.length()) {
                FormattedLine rightLine = line.split(remaining);
                lines.add(i + 1, rightLine);
                return;
            }
            remaining -= line.length() + 1;
        }
        // Offset at or beyond end: append empty line.
        lines.add(new FormattedLine());
    }

    /************************************************************************
     * Word boundary helpers (package-private for testability).
     ************************************************************************/

    /**
     * Returns the character at a block-level offset, walking across lines.
     * Returns {@code '\n'} for line break positions.
     */
    static char charAt(FormattedBlock block, int offset) {
        for (FormattedLine line : block.getLines()) {
            int ll = line.length();
            if (offset < ll)
                return line.getText().charAt(offset);
            if (offset == ll)
                return '\n';
            offset -= ll + 1;
        }
        throw new IndexOutOfBoundsException("offset beyond block content");
    }

    /**
     * Whether the character is a word character (letter, digit, or
     * underscore).
     */
    static boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c) || (c == '_');
    }

    /**
     * Scans backward from {@code offset} to find the start of the
     * current/previous word. At a word character the scan goes back through
     * word characters; at a non-word character it goes back through non-word
     * characters then through word characters.
     */
    static int findWordStart(FormattedBlock block, int offset) {
        if (offset <= 0)
            return 0;
        int contentSize = Positions.contentSize(block);
        if (offset > contentSize)
            offset = contentSize;
        int pos = offset;

        char c = charAt(block, pos - 1);
        if (isWordChar(c)) {
            while ((pos > 0) && isWordChar(charAt(block, pos - 1)))
                pos--;
        } else {
            while ((pos > 0) && !isWordChar(charAt(block, pos - 1)))
                pos--;
            while ((pos > 0) && isWordChar(charAt(block, pos - 1)))
                pos--;
        }
        return pos;
    }

    /**
     * Scans forward from {@code offset} to find the end of the current/next
     * word. Mirror of {@link #findWordStart(FormattedBlock, int)}.
     */
    static int findWordEnd(FormattedBlock block, int offset) {
        int contentSize = Positions.contentSize(block);
        if (offset >= contentSize)
            return contentSize;
        int pos = offset;

        char c = charAt(block, pos);
        if (isWordChar(c)) {
            while ((pos < contentSize) && isWordChar(charAt(block, pos)))
                pos++;
        } else {
            while ((pos < contentSize) && !isWordChar(charAt(block, pos)))
                pos++;
            while ((pos < contentSize) && isWordChar(charAt(block, pos)))
                pos++;
        }
        return pos;
    }
}
