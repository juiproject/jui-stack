package com.effacy.jui.text.type;

/**
 * Default generator for block identifiers.
 * <p>
 * This implementation is GWT-friendly: it avoids {@code UUID} and instead uses
 * a per-generator seed plus an incrementing counter.
 */
public class DefaultBlockIdGenerator implements IBlockIdGenerator {

    private static int generatorSequence;

    private final String seed;
    private int counter;

    public DefaultBlockIdGenerator() {
        this.seed = "blk_" + Long.toString(System.currentTimeMillis(), 36) + "_"
            + Integer.toString(generatorSequence++, 36);
    }

    @Override
    public String nextId() {
        return seed + "_" + Integer.toString(counter++, 36);
    }
}
