package com.effacy.jui.filter.builder;

import java.util.List;

public interface ExpressionBuilder<T> {

    public enum Operator {
        EQ,NEQ,GT,GTE,LT,LTE,IN,NOT_IN,IS,CONTAINS,STARTS_WITH,ENDS_WITH;
    }

    default public T and(T...expressions) {
        return and(List.of(expressions));
    }

    public T and(List<T> expressions);

    default public T or(T...expressions) {
        return or(List.of(expressions));
    }

    public T or(List<T> expressions);

    public T not(T expression);

    public T term(String field, Operator operator, Object value);
}
