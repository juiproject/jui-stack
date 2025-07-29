package com.effacy.jui.filter.builder;

/**
 * Used to map one field value to another.
 */
@FunctionalInterface
public interface FieldMapper<F1,F2> {

    /**
     * Maps the given value of type F1 to a value of type F2.
     * 
     * @param value
     *              the value to map.
     * @return the mapped value.
     */
    public F2 map(F1 value);
}
