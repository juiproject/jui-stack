package com.effacy.jui.text.type.edit.step;

import java.util.ArrayList;
import java.util.List;

import com.effacy.jui.text.type.FormattedBlock;
import com.effacy.jui.text.type.FormattedText;
import com.effacy.jui.text.type.edit.Positions;
import com.effacy.jui.text.type.edit.Step;
import com.effacy.jui.text.type.edit.StepMap;
import com.effacy.jui.text.type.edit.StepResult;

/**
 * Moves one or more consecutive blocks to a new position.
 * <p>
 * The {@code toIndex} is the position in the final list where the first moved
 * block will end up. The inverse simply swaps from and to.
 */
public class MoveBlockStep implements Step {

    private final int fromIndex;
    private final int toIndex;
    private final int count;

    /**
     * @param fromIndex
     *                  index of the first block to move.
     * @param toIndex
     *                  target index in the final list where the first moved block
     *                  should appear.
     * @param count
     *                  number of consecutive blocks to move.
     */
    public MoveBlockStep(int fromIndex, int toIndex, int count) {
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
        this.count = count;
    }

    @Override
    public StepResult apply(FormattedText doc) {
        if (fromIndex == toIndex)
            return new StepResult(new MoveBlockStep(toIndex, fromIndex, count), StepMap.EMPTY);

        // Compute positions before mutation.
        int fromPos = Positions.blockStart(doc, fromIndex);
        int movedSize = 0;
        for (int i = 0; i < count; i++)
            movedSize += Positions.nodeSize(doc.getBlocks().get(fromIndex + i));

        // Insertion point in old-doc coordinates.
        int insertPosOld;
        if (toIndex > fromIndex)
            insertPosOld = Positions.blockStart(doc, toIndex + count);
        else
            insertPosOld = Positions.blockStart(doc, toIndex);

        List<FormattedBlock> blocks = doc.getBlocks();

        // Extract blocks to move.
        List<FormattedBlock> moved = new ArrayList<>(count);
        for (int i = 0; i < count; i++)
            moved.add(blocks.remove(fromIndex));

        // Insert at target position in the reduced list.
        int insertAt = Math.min(toIndex, blocks.size());
        blocks.addAll(insertAt, moved);

        // StepMap: two ranges ordered by old position (remove + insert).
        StepMap map;
        if (fromPos < insertPosOld)
            map = new StepMap(new int[] { fromPos, movedSize, 0, insertPosOld, 0, movedSize });
        else
            map = new StepMap(new int[] { insertPosOld, 0, movedSize, fromPos, movedSize, 0 });

        return new StepResult(new MoveBlockStep(insertAt, fromIndex, count), map);
    }
}
