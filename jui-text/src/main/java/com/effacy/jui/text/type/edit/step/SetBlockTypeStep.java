package com.effacy.jui.text.type.edit.step;

import com.effacy.jui.text.type.FormattedBlock;
import com.effacy.jui.text.type.FormattedBlock.BlockType;
import com.effacy.jui.text.type.edit.Step;
import com.effacy.jui.text.type.edit.StepMap;
import com.effacy.jui.text.type.edit.StepResult;
import com.effacy.jui.text.type.FormattedText;

/**
 * Changes a block's type.
 * <p>
 * Lightweight step that only modifies the type field. The inverse captures the
 * original type.
 */
public class SetBlockTypeStep implements Step {

    private final int blockIndex;
    private final BlockType newType;

    /**
     * @param blockIndex
     *                   index of the block to modify.
     * @param newType
     *                   the new block type.
     */
    public SetBlockTypeStep(int blockIndex, BlockType newType) {
        this.blockIndex = blockIndex;
        this.newType = newType;
    }

    @Override
    public StepResult apply(FormattedText doc) {
        FormattedBlock block = doc.getBlocks().get(blockIndex);
        BlockType oldType = block.getType();
        block.setType(newType);
        return new StepResult(new SetBlockTypeStep(blockIndex, oldType), StepMap.EMPTY);
    }
}
