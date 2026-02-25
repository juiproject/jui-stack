package com.effacy.jui.text.type.edit.step;

import com.effacy.jui.text.type.FormattedBlock;
import com.effacy.jui.text.type.FormattedLine.FormatType;
import com.effacy.jui.text.type.edit.Step;
import com.effacy.jui.text.type.edit.StepMap;
import com.effacy.jui.text.type.edit.StepResult;
import com.effacy.jui.text.type.FormattedText;

/**
 * Adds or removes a {@link FormatType} on a character range within a block.
 * <p>
 * Formatting changes do not alter document positions so the StepMap is always
 * {@link StepMap#EMPTY}. The inverse is a {@link ReplaceBlockStep} that
 * restores the original block (preserving all formatting through undo).
 */
public class ChangeFormatStep implements Step {

    private final int blockIndex;
    private final int offset;
    private final int length;
    private final FormatType type;
    private final boolean add;

    /**
     * @param blockIndex
     *                   index of the block to modify.
     * @param offset
     *                   character offset within the block's content.
     * @param length
     *                   number of characters to format.
     * @param type
     *                   the format type to add or remove.
     * @param add
     *                   {@code true} to add the type, {@code false} to remove.
     */
    public ChangeFormatStep(int blockIndex, int offset, int length, FormatType type, boolean add) {
        this.blockIndex = blockIndex;
        this.offset = offset;
        this.length = length;
        this.type = type;
        this.add = add;
    }

    @Override
    public StepResult apply(FormattedText doc) {
        FormattedBlock block = doc.getBlocks().get(blockIndex);
        FormattedBlock originalClone = block.clone();

        if (add)
            block.addFormat(offset, length, type);
        else
            block.removeFormat(offset, length, type);

        return new StepResult(
            new ReplaceBlockStep(blockIndex, originalClone),
            StepMap.EMPTY
        );
    }
}
