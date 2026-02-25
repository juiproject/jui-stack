package com.effacy.jui.text.type.edit.step;

import com.effacy.jui.text.type.FormattedBlock;
import com.effacy.jui.text.type.FormattedText;
import com.effacy.jui.text.type.edit.Positions;
import com.effacy.jui.text.type.edit.Step;
import com.effacy.jui.text.type.edit.StepMap;
import com.effacy.jui.text.type.edit.StepResult;

/**
 * Merges two adjacent blocks by appending the content of
 * {@code block[index+1]} into {@code block[index]} and removing the second
 * block.
 * <p>
 * In position space this removes two boundary tokens (block-close +
 * block-open) at the join point. The inverse is a {@link SplitBlockStep}
 * that splits back at the original boundary.
 * <p>
 * Both blocks must have the same type (required by
 * {@link FormattedBlock#merge(FormattedBlock)}).
 */
public class JoinBlocksStep implements Step {

    private final int blockIndex;

    /**
     * @param blockIndex
     *                   index of the first block. The block at
     *                   {@code blockIndex + 1} will be merged into it and
     *                   removed.
     */
    public JoinBlocksStep(int blockIndex) {
        this.blockIndex = blockIndex;
    }

    @Override
    public StepResult apply(FormattedText doc) {
        FormattedBlock left = doc.getBlocks().get(blockIndex);
        FormattedBlock right = doc.getBlocks().get(blockIndex + 1);
        int leftContentSize = Positions.contentSize(left);
        int contentPos = Positions.blockStart(doc, blockIndex) + 1;

        left.merge(right);
        doc.getBlocks().remove(blockIndex + 1);

        return new StepResult(
            new SplitBlockStep(blockIndex, leftContentSize),
            StepMap.of(contentPos + leftContentSize, 2, 0)
        );
    }
}
