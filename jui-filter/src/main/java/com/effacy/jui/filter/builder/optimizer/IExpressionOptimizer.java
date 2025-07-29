package com.effacy.jui.filter.builder.optimizer;

import com.effacy.jui.filter.builder.ExpressionBuilder;

/**
 * Interface for optimizing expressions to their minimal equivalent forms.
 * <p>
 * Optimizers can apply various transformations such as:
 * <ul>
 * <li>Boolean logic simplifications (identity, absorption, idempotence)</li>
 * <li>Structural optimizations (flattening, double negation elimination)</li>
 * <li>Comparison optimizations (redundant term elimination, range merging)</li>
 * </ul>
 * 
 * @param <F> the field type used in expressions
 */
public interface IExpressionOptimizer<F> {

    /**
     * Optimizes the given expression to its minimal equivalent form.
     * 
     * @param expression the expression to optimize
     * @return the optimized expression, or the original if no optimizations apply
     */
    ExpressionBuilder.Expression<F> optimize(ExpressionBuilder.Expression<F> expression);
}