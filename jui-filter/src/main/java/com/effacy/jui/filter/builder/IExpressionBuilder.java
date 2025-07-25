package com.effacy.jui.filter.builder;

import java.util.List;

import com.effacy.jui.filter.parser.FilterQueryParser;

/**
 * A mechanism that is able to generate an expression programtically as a
 * product of expressions through compositions of AND, OR and NOT as well as
 * primitive comparisons.
 * <p>
 * This can be used directly or indirectly (via an
 * {@link IExpressionBuildable}). See {@link FilterQueryParser} for an example.
 */
public interface IExpressionBuilder<T,FIELD> {

    /**
     * The various operators that are recognised buy the builder.
     */
    public enum Operator {
        EQ,NEQ,GT,GTE,LT,LTE,IN,NOT_IN,CONTAINS,STARTS_WITH,ENDS_WITH;
    }

    /**
     * Used to capture a literal value.
     */
    public static record Literal(String value) {}

    /**
     * See {@link #and(List)}.
     */
    default public T and(@SuppressWarnings("unchecked") T...expressions) {
        return and(List.of(expressions));
    }

    /**
     * Given a list of expressions, generates an expression that evaluates to the
     * AND of the expressions.
     * 
     * @param expressions
     *                    the expression to AND.
     * @return the resultant AND expression.
     */
    public T and(List<T> expressions);

    /**
     * See {@link #or(List)}.
     */
    default public T or(@SuppressWarnings("unchecked") T...expressions) {
        return or(List.of(expressions));
    }

    /**
     * Given a list of expressions, generates an expression that evaluates to the
     * OR of the expressions.
     * 
     * @param expressions
     *                    the expression to OR.
     * @return the resultant OR expression.
     */
    public T or(List<T> expressions);

    /**
     * Given an expression, creates an expression that evaluates to the NOT of that
     * expression.
     * 
     * @param expression
     *                   the expression to NOT.
     * @return the resultant NOT expression.
     */
    public T not(T expression);

    /**
     * Registers a comparison term with the field, comparison operator and value.
     * <p>
     * Values should be mapped
     * 
     * @param field
     *                 the field being compared.
     * @param operator
     *                 the comparison operator (mode of comparison).
     * @param value
     *                 the value being compared to.
     * @return the representative expression.
     * @throws ExpressionBuildException
     *                              if there is an incompatibility between the
     *                              field, operator and value.
     */
    public T term(FIELD field, Operator operator, Object value) throws ExpressionBuildException;
}
