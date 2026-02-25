package com.effacy.jui.text.type.edit.step;

import com.effacy.jui.text.type.FormattedBlock;
import com.effacy.jui.text.type.FormattedText;
import com.effacy.jui.text.type.edit.Positions;
import com.effacy.jui.text.type.edit.Step;
import com.effacy.jui.text.type.edit.StepMap;
import com.effacy.jui.text.type.edit.StepResult;

/**
 * Deletes a range of text within a block.
 * <p>
 * The inverse is a {@link ReplaceBlockStep} that restores the original block
 * (preserving formatting through undo). The forward StepMap is precise: only
 * positions at or after the deletion point are shifted.
 */
public class DeleteTextStep implements Step {

    private final int blockIndex;
    private final int offset;
    private final int length;

    /**
     * @param blockIndex
     *                   index of the block to delete from.
     * @param offset
     *                   character offset where deletion starts (across all
     *                   lines, with line breaks counted).
     * @param length
     *                   number of characters to delete.
     */
    public DeleteTextStep(int blockIndex, int offset, int length) {
        this.blockIndex = blockIndex;
        this.offset = offset;
        this.length = length;
    }

    @Override
    public StepResult apply(FormattedText doc) {
        FormattedBlock block = doc.getBlocks().get(blockIndex);
        FormattedBlock originalClone = block.clone();
        int contentPos = Positions.blockStart(doc, blockIndex) + 1;

        block.remove(offset, length);

        return new StepResult(
            new ReplaceBlockStep(blockIndex, originalClone),
            StepMap.of(contentPos + offset, length, 0)
        );
    }
}
