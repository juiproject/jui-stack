package com.effacy.jui.text.type;

/**
 * Generates block identifiers for {@link FormattedBlock} instances.
 */
public interface IBlockIdGenerator {

    /**
     * Generates the next block identifier.
     * 
     * @return the identifier.
     */
    String nextId();
}
