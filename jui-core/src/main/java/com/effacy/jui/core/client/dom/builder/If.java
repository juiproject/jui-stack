package com.effacy.jui.core.client.dom.builder;

import java.util.function.Supplier;

/**
 * Convenience conditional for use in the argument-based dom builder
 * construction.
 */
public class If {

    /**
     * If the condition is {@code true} the return the value from the first
     * supplier, otherwise use the value from the second.
     * 
     * @param condition
     *                  the condition to switch against.
     * @param v1
     *                  the value supplier for an affirmative condition.
     * @param v2
     *                  the value supplier for a negative condition.
     * @return the resolved builder.
     */
    public static ElementBuilder $(boolean condition, Supplier<ElementBuilder> v1, Supplier<ElementBuilder> v2) {
        if (condition)
            return (v1 == null) ? null : v1.get();
        return (v2 == null) ? null : v2.get();
    }
}
