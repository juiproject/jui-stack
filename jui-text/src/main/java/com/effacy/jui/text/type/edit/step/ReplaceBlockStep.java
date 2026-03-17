package com.effacy.jui.text.type.edit.step;

import com.effacy.jui.text.type.FormattedBlock;
import com.effacy.jui.text.type.FormattedText;
import com.effacy.jui.text.type.edit.Positions;
import com.effacy.jui.text.type.edit.Step;
import com.effacy.jui.text.type.edit.StepMap;
import com.effacy.jui.text.type.edit.StepResult;

/**
 * Replaces a block's entire content with a new block.
 * <p>
 * This is the general-purpose step for any intra-block modification (text
 * editing, formatting changes, etc.). The inverse captures a clone of the
 * original block.
 */
public class ReplaceBlockStep implements Step {

    private final int blockIndex;
    private final FormattedBlock replacement;

    /**
     * @param blockIndex
     *                    index of the block to replace.
     * @param replacement
     *                    the new block content.
     */
    public ReplaceBlockStep(int blockIndex, FormattedBlock replacement) {
        this.blockIndex = blockIndex;
        this.replacement = replacement;
    }

    @Override
    public StepResult apply(FormattedText doc) {
        FormattedBlock original = doc.getBlocks().get(blockIndex);
        int contentPos = Positions.blockStart(doc, blockIndex) + 1;
        int oldContentSize = Positions.contentSize(original);
        FormattedBlock originalClone = original.clone();
        doc.getBlocks().set(blockIndex, replacement);
        int newContentSize = Positions.contentSize(replacement);
        return new StepResult(
            new ReplaceBlockStep(blockIndex, originalClone),
            StepMap.of(contentPos, oldContentSize, newContentSize)
        );
    }
}
