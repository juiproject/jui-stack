package com.effacy.jui.text.type.edit.step;

import com.effacy.jui.text.type.FormattedBlock;
import com.effacy.jui.text.type.FormattedText;
import com.effacy.jui.text.type.edit.Positions;
import com.effacy.jui.text.type.edit.Step;
import com.effacy.jui.text.type.edit.StepMap;
import com.effacy.jui.text.type.edit.StepResult;

/**
 * Deletes the block at the given index.
 * <p>
 * The deleted block is captured so the inverse ({@link InsertBlockStep}) can
 * restore it.
 */
public class DeleteBlockStep implements Step {

    private final int index;

    /**
     * @param index
     *              index of the block to delete.
     */
    public DeleteBlockStep(int index) {
        this.index = index;
    }

    @Override
    public StepResult apply(FormattedText doc) {
        int pos = Positions.blockStart(doc, index);
        int ns = Positions.nodeSize(doc.getBlocks().get(index));
        FormattedBlock removed = doc.getBlocks().remove(index);
        return new StepResult(new InsertBlockStep(index, removed), StepMap.of(pos, ns, 0));
    }
}
