package com.effacy.jui.text.type.edit.step;

import com.effacy.jui.text.type.FormattedBlock;
import com.effacy.jui.text.type.FormattedText;
import com.effacy.jui.text.type.edit.Positions;
import com.effacy.jui.text.type.edit.Step;
import com.effacy.jui.text.type.edit.StepMap;
import com.effacy.jui.text.type.edit.StepResult;

/**
 * Inserts plain text at a character offset within a block.
 * <p>
 * The inverse is a {@link ReplaceBlockStep} that restores the original block
 * (preserving formatting). The forward StepMap is precise: only positions at or
 * after the insertion point are shifted.
 */
public class InsertTextStep implements Step {

    private final int blockIndex;
    private final int offset;
    private final String text;

    /**
     * @param blockIndex
     *                   index of the block to insert into.
     * @param offset
     *                   character offset within the block's content (across all
     *                   lines, with line breaks counted).
     * @param text
     *                   the plain text to insert.
     */
    public InsertTextStep(int blockIndex, int offset, String text) {
        this.blockIndex = blockIndex;
        this.offset = offset;
        this.text = text;
    }

    @Override
    public StepResult apply(FormattedText doc) {
        FormattedBlock block = doc.getBlocks().get(blockIndex);
        FormattedBlock originalClone = block.clone();
        int contentPos = Positions.blockStart(doc, blockIndex) + 1;

        block.insert(offset, text);

        return new StepResult(
            new ReplaceBlockStep(blockIndex, originalClone),
            StepMap.of(contentPos + offset, 0, text.length())
        );
    }
}
