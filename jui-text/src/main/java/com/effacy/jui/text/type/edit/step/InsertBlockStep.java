package com.effacy.jui.text.type.edit.step;

import com.effacy.jui.text.type.FormattedBlock;
import com.effacy.jui.text.type.FormattedText;
import com.effacy.jui.text.type.edit.Positions;
import com.effacy.jui.text.type.edit.Step;
import com.effacy.jui.text.type.edit.StepMap;
import com.effacy.jui.text.type.edit.StepResult;

/**
 * Inserts a new block at the given index.
 * <p>
 * Existing blocks at and after the index are shifted right. The inverse is a
 * {@link DeleteBlockStep}.
 */
public class InsertBlockStep implements Step {

    private final int index;
    private final FormattedBlock block;

    /**
     * @param index
     *              insertion index (0 = before first block, size = after last).
     * @param block
     *              the block to insert.
     */
    public InsertBlockStep(int index, FormattedBlock block) {
        this.index = index;
        this.block = block;
    }

    @Override
    public StepResult apply(FormattedText doc) {
        int pos = Positions.blockStart(doc, index);
        doc.getBlocks().add(index, block);
        int ns = Positions.nodeSize(block);
        return new StepResult(new DeleteBlockStep(index), StepMap.of(pos, 0, ns));
    }
}
