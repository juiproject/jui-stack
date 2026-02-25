package com.effacy.jui.text.type.edit.step;

import com.effacy.jui.text.type.FormattedBlock;
import com.effacy.jui.text.type.FormattedText;
import com.effacy.jui.text.type.edit.Step;
import com.effacy.jui.text.type.edit.StepMap;
import com.effacy.jui.text.type.edit.StepResult;

/**
 * Sets or removes a single metadata entry on a block.
 * <p>
 * When {@code value} is non-null the entry is set; when {@code null} it is
 * removed. The inverse captures the previous value (which may be {@code null}
 * if the key did not exist).
 */
public class SetBlockMetaStep implements Step {

    private final int blockIndex;
    private final String key;
    private final String value;

    /**
     * @param blockIndex
     *                   index of the block to modify.
     * @param key
     *                   the metadata key.
     * @param value
     *                   the new value, or {@code null} to remove the key.
     */
    public SetBlockMetaStep(int blockIndex, String key, String value) {
        this.blockIndex = blockIndex;
        this.key = key;
        this.value = value;
    }

    @Override
    public StepResult apply(FormattedText doc) {
        FormattedBlock block = doc.getBlocks().get(blockIndex);
        String oldValue = block.getMeta().get(key);
        if (value == null)
            block.getMeta().remove(key);
        else
            block.getMeta().put(key, value);
        return new StepResult(new SetBlockMetaStep(blockIndex, key, oldValue), StepMap.EMPTY);
    }
}
