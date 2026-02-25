package com.effacy.jui.text.type.edit;

import java.util.Collections;
import java.util.List;

/**
 * A composed sequence of {@link StepMap}s from a {@link Transaction}.
 * <p>
 * Maps a position through all step maps in order, producing the final position
 * after all steps have been applied.
 */
public class Mapping {

    /**
     * Empty mapping — no position changes.
     */
    public static final Mapping EMPTY = new Mapping(Collections.emptyList());

    private List<StepMap> maps;

    public Mapping(List<StepMap> maps) {
        this.maps = maps;
    }

    /**
     * Maps a position through all step maps sequentially.
     *
     * @param pos
     *             the position to map.
     * @param bias
     *             mapping bias: {@code -1} for left, {@code 1} for right.
     * @return the mapped position.
     */
    public int map(int pos, int bias) {
        for (StepMap map : maps)
            pos = map.map(pos, bias);
        return pos;
    }

    /**
     * The individual step maps.
     */
    public List<StepMap> maps() {
        return Collections.unmodifiableList(maps);
    }
}
