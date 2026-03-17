package com.effacy.jui.text.type.edit;

/**
 * Describes how document positions shift after applying a {@link Step}.
 * <p>
 * A step map is a sequence of ranges, each described by a triple
 * {@code (position, oldSize, newSize)}. Positions before the first range are
 * unchanged. Between ranges, positions are shifted by the accumulated delta.
 * Positions within a range are mapped to the start or end of the replacement
 * based on bias.
 * <p>
 * This follows the ProseMirror step-map design.
 * <p>
 * Example: replacing 3 characters at position 5 with 1 character produces
 * the map {@code (5, 3, 1)}. A position at 4 is unchanged; a position at 6
 * maps to 5 (bias left) or 6 (bias right); a position at 10 maps to 8
 * (shifted by delta -2).
 */
public class StepMap {

    /**
     * Identity map — no position changes.
     */
    public static final StepMap EMPTY = new StepMap(new int[0]);

    // Flat array of triples: [pos0, oldSize0, newSize0, pos1, oldSize1, newSize1, ...]
    private final int[] ranges;

    /**
     * Creates a step map from a flat array of
     * {@code (position, oldSize, newSize)} triples.
     *
     * @param ranges
     *               flat array of triples. Length must be a multiple of 3.
     */
    public StepMap(int[] ranges) {
        this.ranges = ranges;
    }

    /**
     * Convenience factory for a single range.
     */
    public static StepMap of(int pos, int oldSize, int newSize) {
        if ((oldSize == 0) && (newSize == 0))
            return EMPTY;
        return new StepMap(new int[] { pos, oldSize, newSize });
    }

    /**
     * Maps a position through this step map.
     *
     * @param pos
     *             the position to map.
     * @param bias
     *             mapping bias: {@code -1} to prefer the left side of a
     *             replaced range, {@code 1} to prefer the right side.
     * @return the mapped position.
     */
    public int map(int pos, int bias) {
        int delta = 0;
        for (int i = 0; i < ranges.length; i += 3) {
            int rangePos = ranges[i];
            int oldSize = ranges[i + 1];
            int newSize = ranges[i + 2];
            int rangeEnd = rangePos + oldSize;

            // Before this range in the old document.
            if (pos < rangePos)
                return pos + delta;

            // Strictly past this range.
            if (pos > rangeEnd) {
                delta += newSize - oldSize;
                continue;
            }

            // At exactly rangeEnd.
            if (pos == rangeEnd) {
                if (oldSize > 0) {
                    // First position after the old range — accumulate delta.
                    delta += newSize - oldSize;
                    continue;
                }
                // Insertion (oldSize=0): pos is at the insertion point — use bias.
                if (bias <= 0)
                    return rangePos + delta;
                return rangePos + newSize + delta;
            }

            // Within the replaced range [rangePos, rangeEnd).
            if (bias <= 0)
                return rangePos + delta;
            return rangePos + newSize + delta;
        }
        // Past all ranges — apply total delta.
        return pos + delta;
    }
}
