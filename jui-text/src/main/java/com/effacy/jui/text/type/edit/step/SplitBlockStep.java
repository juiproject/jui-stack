package com.effacy.jui.text.type.edit.step;

import com.effacy.jui.text.type.FormattedBlock;
import com.effacy.jui.text.type.FormattedText;
import com.effacy.jui.text.type.edit.Positions;
import com.effacy.jui.text.type.edit.Step;
import com.effacy.jui.text.type.edit.StepMap;
import com.effacy.jui.text.type.edit.StepResult;

/**
 * Splits a block at a character offset into two consecutive blocks.
 * <p>
 * The left portion remains at the original index; the right portion is
 * inserted at {@code blockIndex + 1}. In position space this inserts two
 * boundary tokens (block-close + block-open) at the split point.
 * <p>
 * The inverse is a {@link JoinBlocksStep} that merges the two blocks back.
 */
public class SplitBlockStep implements Step {

    private final int blockIndex;
    private final int offset;

    /**
     * @param blockIndex
     *                   index of the block to split.
     * @param offset
     *                   character offset within the block's content where the
     *                   split occurs. Content before this offset stays in the
     *                   original block; content from this offset onward moves
     *                   to a new block.
     */
    public SplitBlockStep(int blockIndex, int offset) {
        this.blockIndex = blockIndex;
        this.offset = offset;
    }

    @Override
    public StepResult apply(FormattedText doc) {
        FormattedBlock block = doc.getBlocks().get(blockIndex);
        int contentPos = Positions.blockStart(doc, blockIndex) + 1;

        FormattedBlock right = block.split(offset);
        doc.getBlocks().add(blockIndex + 1, right);

        return new StepResult(
            new JoinBlocksStep(blockIndex),
            StepMap.of(contentPos + offset, 0, 2)
        );
    }
}
