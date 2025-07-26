package com.effacy.jui.filter.builder;

import java.util.List;

/**
 * Used to allow an expression builder to be used with a builable of a different
 * field-type by providing a mapper for values of that type.
 * <p>
 * Consider the case where one has a builder over expresion-type T and
 * field-type F2 but we want to use this with a different field-type F1. So long
 * as we can translate instances of F1 to instances of F2 then we can used this
 * to construct such a builder.
 */
public class MappedExpresionBuilder<T,F1,F2> implements IExpressionBuilder<T,F1> {

    /**
     * The underlying builder being mapped to.
     */
    private IExpressionBuilder<T,F2> delegate;

    /**
     * Maps field-type instances.
     */
    private FieldMapper<F1,F2> mapper;

    /**
     * Construct with the underlying builder (with field-type F2) and a mapper of
     * field-type intances F1 to F2. The mapper then will take field-type F2 and
     * generate an expression of the same type.
     * 
     * @param delegate
     *                 the builder to delegate to.
     * @param mapper
     *                 to map instance of the target field-type F1 to instances of
     *                 type F2 that the delegate can understand.
     */
    public MappedExpresionBuilder(IExpressionBuilder<T,F2> delegate, FieldMapper<F1,F2> mapper) {
        this.delegate = delegate;
        this.mapper = mapper;
    }

    @Override
    public T and(List<T> expressions) {
        return delegate.and(expressions);
    }

    @Override
    public T or(List<T> expressions) {
        return delegate.or(expressions);
    }

    @Override
    public T not(T expression) {
        return delegate.not(expression);
    }

    @Override
    public T term(F1 field, Operator operator, Object value) throws ExpressionBuildException {
        F2 mapped = null;
        try {
            mapped = mapper.map(field);
        } catch (Throwable e) {
            if (field instanceof Enum)
                throw new ExpressionBuildException("unable to map field " + ((Enum<?>)field).name());
            throw new ExpressionBuildException("unable to map field " + field);
        }
        return delegate.term(mapped, operator, value);
         
    }

}
