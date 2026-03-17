package com.effacy.jui.text.type.edit.step;

import com.effacy.jui.text.type.FormattedBlock;
import com.effacy.jui.text.type.FormattedText;
import com.effacy.jui.text.type.edit.Step;
import com.effacy.jui.text.type.edit.StepMap;
import com.effacy.jui.text.type.edit.StepResult;

/**
 * Changes a block's indentation level.
 * <p>
 * Lightweight step that only modifies the indent field. The inverse captures
 * the original indent.
 */
public class SetBlockIndentStep implements Step {

    private final int blockIndex;
    private final int newIndent;

    /**
     * @param blockIndex
     *                   index of the block to modify.
     * @param newIndent
     *                   the new indent level (0-5).
     */
    public SetBlockIndentStep(int blockIndex, int newIndent) {
        this.blockIndex = blockIndex;
        this.newIndent = newIndent;
    }

    @Override
    public StepResult apply(FormattedText doc) {
        FormattedBlock block = doc.getBlocks().get(blockIndex);
        int oldIndent = block.getIndent();
        block.setIndent(newIndent);
        return new StepResult(new SetBlockIndentStep(blockIndex, oldIndent), StepMap.EMPTY);
    }
}
