package com.effacy.jui.filter.builder;

import java.util.function.Function;

/**
 * Indicates that something can build expressions using a
 * {@link IExpressionBuilder}.
 */
public interface IExpressionBuildable<F> {

    /**
     * Given a builder, build into that builder the expression that is represented
     * by this instance.
     * <p>
     * Note that the field types need to be the same. If they differ then you can
     * wrap the passed builder in a {@link MappedExpressionBuilder} that performs
     * the revelvant field mapping.
     * 
     * @param <T>
     *                the expression type.
     * @param <F>
     *                the field type.
     * @param builder
     *                the builder that builds an instance of the expression type.
     * @return the built expression.
     */
    public <T> T build(IExpressionBuilder<T,F> builder) throws ExpressionBuildException;

    /**
     * See {@link #build(IExpressionBuilder)} but allows one to process the
     * resulting expression after being built.
     */
    default <T> T build(IExpressionBuilder<T,F> builder, Function<T,T> postbuild) throws ExpressionBuildException {
        T exp = build(builder);
        if (postbuild != null)
            return postbuild.apply(exp);
        return exp;
    }
}
