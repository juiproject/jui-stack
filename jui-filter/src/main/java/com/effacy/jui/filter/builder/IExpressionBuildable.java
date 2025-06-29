package com.effacy.jui.filter.builder;

/**
 * Indicates that something can build expressions using a
 * {@link IExpressionBuilder}.
 */
public interface IExpressionBuildable<F> {

    /**
     * Given a builder, build into that builder the expression that is represented
     * by this instance.
     * 
     * @param <T>
     *                the expression type.
     * @param <F>
     *                the field type.
     * @param builder
     *                the builder that builds an instance of the expression type.
     * @return the built expression.
     */
    public <T> T build(IExpressionBuilder<T,F> builder);
}
