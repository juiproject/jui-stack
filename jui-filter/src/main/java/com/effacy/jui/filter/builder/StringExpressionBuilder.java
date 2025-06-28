package com.effacy.jui.filter.builder;

import java.lang.reflect.Array;
import java.util.List;

public class StringExpressionBuilder implements IExpressionBuilder<String> {

    private boolean operatorAsSymbol = false;

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

    protected String value(Object value) {
        if (value == null)
            return "null";
        if (value instanceof String)
            return "\"" + value + "\"";
        if (value instanceof Enum)
            return((Enum<?>) value).name();
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
