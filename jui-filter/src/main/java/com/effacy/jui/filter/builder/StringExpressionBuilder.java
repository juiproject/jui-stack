package com.effacy.jui.filter.builder;

import java.lang.reflect.Array;
import java.util.List;

import com.effacy.jui.filter.parser.FilterQueryParser;

/**
 * A type of {@link IExpressionBuilder} that builds a string representation of
 * an expression.
 * <p>
 * This representation is parsable by {@link FilterQueryParser}.
 * <p>
 * Note that literals are expected to be mapped to enums.
 */
public class StringExpressionBuilder implements IExpressionBuilder<String,String> {

    /**
     * Creates a builder over the default field-type of {@link String}.
     * 
     * @return the builder.
     */
    public static StringExpressionBuilder create() {
        return new StringExpressionBuilder();
    }

    /**
     * Creates a builder over a general field-type with a mapper to map the field
     * type to a string.
     * 
     * @param <F>
     *               the field-type of the buildable.
     * @param mapper
     *               to map instances of the field type to a string (if {@code null}
     *               then {@link Object#toString()} will be used, or
     *               {@link Enum#name()} if is an enum).
     * @return the builder.
     */
    public static <F> IExpressionBuilder<String,F> create(FieldMapper<F,String> mapper) {
        if (mapper == null) {
            mapper = (v -> {
                if (v == null)
                    return null;
                if (v instanceof Enum)
                    return ((Enum<?>) v).name();
                return v.toString();
            });
        }
        return new MappedExpresionBuilder<>(new StringExpressionBuilder(), mapper);
    }

    /**
     * See {@link #operatorAsSymbol(boolean).
     */
    private boolean operatorAsSymbol;

    /**
     * By default, operators are represented in parsable form (i.e. = or <) rather
     * than symbolic form (i.e. EQ or LT). Use this to change that behaviour.
     * 
     * @param operatorAsSymbol
     *                         {@code true} to print in symbolic form.
     * @return this builder instance.
     */
    public StringExpressionBuilder operatorAsSymbol(boolean operatorAsSymbol) {
        this.operatorAsSymbol = operatorAsSymbol;
        return this;
    }

    @Override
    public String and(List<String> expressions) {
        return composer(" AND ", expressions);
    }

    @Override
    public String or(List<String> expressions) {
        return composer(" OR ", expressions);
    }

    @Override
    public String not(String expression) {
        return "(NOT " + expression + ")";
    }

    @Override
    public String term(String field, Operator operator, Object value) {
        return field + " " + operator(operator) + " " + value(value);
    }

    /**
     * Given a value, generates a string form for the value that can be parsed back
     * to the value.
     * 
     * @param value
     *              the value.
     * @return
     *         the string form.
     */
    protected String value(Object value) {
        if (value == null)
            return "null";
        if (value instanceof String)
            return "\"" + value + "\"";
        if (value instanceof Enum)
            return((Enum<?>) value).name();
        if (value instanceof Literal)
            return ((Literal) value).value();
        if (value.getClass().isArray()) {
            StringBuffer sb = new StringBuffer();
            sb.append('[');
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                if (i > 0)
                    sb.append(',');
                sb.append(value(Array.get(value, i)));
            }
            sb.append(']');
            return sb.toString();
        }
        return value.toString();
    }

    /**
     * Given an operator, generate its string form. This depends on
     * {@link #operatorAsSymbol}.
     * 
     * @param operator
     *                 the operator.
     * @return the string form.
     */
    protected String operator(Operator operator) {
        if (operatorAsSymbol)
            return operator.name();
        if (Operator.EQ == operator)
            return "=";
        if (Operator.NEQ == operator)
            return "!=";
        if (Operator.GT == operator)
            return ">";
        if (Operator.GTE == operator)
            return ">=";
        if (Operator.LT == operator)
            return "<";
        if (Operator.LTE == operator)
            return "<=";
        if (Operator.GT == operator)
            return ">";
        if (Operator.CONTAINS == operator)
            return "CONTAINS";
        if (Operator.STARTS_WITH == operator)
            return "STARTS WITH";
        if (Operator.ENDS_WITH == operator)
            return "ENDS WITH";
        if (Operator.IN == operator)
            return "IN";
        if (Operator.NOT_IN == operator)
            return "NOT IN";
        return operator.name();
    }

    /**
     * Generates a suitable expression for an AND or OR operator.
     * 
     * @param separater
     *                    the string form of the operator (i.e. " AND " or " OR ").
     * @param expressions
     *                    the expressions being operated on.
     * @return this composite.
     */
    protected String composer(String separater, List<String> expressions) {
        if (expressions.isEmpty())
            return "";
        if (expressions.size() == 1)
            return expressions.get(0);
        StringBuffer result = new StringBuffer();
        result.append('(');
        result.append(expressions.get(0));
        for (int i = 1; i < expressions.size(); i++) {
            result.append(separater);
            result.append(expressions.get(i));
        }
        result.append(')');
        return result.toString();
    }

}
