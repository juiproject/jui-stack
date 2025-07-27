package com.effacy.jui.filter.builder;

import java.util.Optional;

import com.effacy.jui.filter.builder.IExpressionBuilder.Literal;
import com.effacy.jui.filter.builder.IExpressionBuilder.Operator;

/**
 * Represents a type for fields for use in building expressions which permit
 * various validation and transformation behaviours to be applied.
 * <p>
 * The field exposes one method for gaining access to a type handler. This can
 * be used to validate operator and value combinations where the field is being
 * used.
 */
public interface Field {

    /**
     * The type handler for the field.
     * <p>
     * This is responsible for implementing restructions and the like.
     * 
     * @return the type handler.
     */
    public Type type();

    /************************************************************************
     * Standard types.
     ************************************************************************/

    @FunctionalInterface
    public interface ITypeValidator {
        public Optional<String> validate(Operator op, Object value, boolean array, Class<?> type);
    } 

     /**
      * Base type handler.
      */
    public static class Type {

        /**
         * Allowable operators.
         */
        protected Operator[] operators;

        /**
         * Custom validator.
         */
        protected ITypeValidator validator;

        /**
         * Construct with initial configuration.
         * 
         * @param validator
         *                  (optional) custom valiator.
         * @param operators
         *                  (optional) list of operators that are permitted.
         */
        protected Type(ITypeValidator validator, Operator...operators) {
            this.validator = validator;
            this.operators = operators;
        }

        /**
         * Used to transform from the given value to a value that is supported by the
         * field. At a minimum this should support values that arise through parsing
         * (see {@link FilterQueryParser}).
         * <p>
         * It is expected to properly handle arrays.
         * 
         * @param value
         *              the value to convert.
         * @return the converted value.
         * @see MappedExpressionBuilder
         */
        public Object transform(Object value) throws ExpressionBuildException {
            return value;
        }

        /**
         * Determines if the passed operator is allowed.
         * 
         * @param op
         *           the operator to test for.
         * @return {@code true} if it is allowed for this field.
         */
        public boolean allowed(Operator op) {
            if ((operators == null) || (operators.length == 0))
                return true;
            if (op == null)
                return false;
            for (Operator operator : operators) {
                if (operator == op)
                    return true;
            }
            return false;
        }

        /**
         * Validates the operator and value pair for this field.
         * <p>
         * This will first validate the operator against the list of supplied operators
         * (if that list has not been set, no validation occurs). It will then validate
         * against any supplied validator. If there is no supplied validator then it
         * will defer to the default for the type (as implemented by
         * {@link #_validate(Operator, Object)}).
         * 
         * @param op
         *              the operator.
         * @param value
         *              the value.
         * @return an optional that contains an error message if invalid.
         */
        public Optional<String> validate(Operator op, Object value) {
            // Check the operator.
            if (op == null)
                return Optional.of("no operator!");
            if (!allowed(op))
                return Optional.of("invalid operator");
            // Check that the value type is not an array when using non-array operators.
            boolean array = (value != null) && value.getClass().isArray();
            if (array && !op.is(Operator.IN, Operator.NOT_IN))
                return Optional.of("operator not compatible with array");
            Class<?> type = null;
            if (value != null) {
                if (array) {
                    Object[] valueAsArray = (Object[]) value;
                    for (int i = 0; i < valueAsArray.length; i++) {
                        if (valueAsArray[i] != null) {
                            type = valueAsArray[0].getClass();
                            break;
                        }
                    }
                } else
                    type = value.getClass();
            }
            if (validator != null) {
                Optional<String> outcome = validator.validate(op, value, array, type);
                if (outcome.isPresent())
                    return outcome;
                return Optional.empty();
            }
            return _validate(op, value, array, type);
        }

        /**
         * Default type validation. Invoked when no custom validator has been supplied.
         * 
         * @param op
         *              the operator.
         * @param value
         *              the value.
         * @param array
         *              if the value is an array.
         * @param type
         *              the class type for the value (will be {@code null} if the value
         *              is {@code null} or the value is an empty array or an array of
         *              {@code null} values).
         * @return an optional that contains an error message if invalid.
         */
        protected Optional<String> _validate(Operator op, Object value, boolean array, Class<?> type) {
            return Optional.empty();
        }
    }
    
    /**
     * Basic {@link String} type.
     */
    public static class StringType extends Type {
        public StringType(Operator...operators) {
            super(null, operators);
        }
        public StringType(ITypeValidator validator, Operator...operators) {
            super(validator, operators);
        }
        protected Optional<String> _validate(Operator op, Object value, boolean array, Class<?> type) {
            if ((value == null) || String.class.equals(type))
                return Optional.empty();
            return Optional.of("expected a string");
        }
    }

    /**
     * Basic {@link Integer} or {@link Long} type.
     */
    public static class IntegralType extends Type {
        public IntegralType(Operator...operators) {
            super(null, operators);
        }
        public IntegralType(ITypeValidator validator, Operator...operators) {
            super(validator, operators);
        }
        protected Optional<String> _validate(Operator op, Object value, boolean array, Class<?> type) {
            if ((value == null) || Integer.class.equals(type) || Long.class.equals(type))
                return Optional.empty();
            return Optional.of("expected an integer");
        }
    }

    /**
     * Basic {@link Integer}, {@link Long}, {@link Double} or {@link Float} type.
     */
    public static class DecimalType extends Type {
        public DecimalType(Operator...operators) {
            super(null, operators);
        }
        public DecimalType(ITypeValidator validator, Operator...operators) {
            super(validator, operators);
        }
        protected Optional<String> _validate(Operator op, Object value, boolean array, Class<?> type) {
            if ((value == null) || Integer.class.equals(type) || Long.class.equals(type) || Double.class.equals(type) || Float.class.equals(type))
                return Optional.empty();
            return Optional.of("expected an integer");
        }
    }

    /**
     * Basic {@link Enum} type.
     */
    public static class EnumType<T extends Enum<T>> extends Type {
        private Class<T> type;
        public EnumType(Class<T> type, Operator...operators) {
            super(null, operators);
            this.type = type;
        }
        public EnumType(Class<T> type, ITypeValidator validator, Operator...operators) {
            super(validator, operators);
            this.type = type;
        }
        public Class<T> type() {
            return type;
        }
        public T[] values() {
            return type.getEnumConstants();
        }
        public T valueOf(Literal literal) throws ExpressionBuildException {
            if (literal == null)
                return null;
            String str = literal.value();
            for (T v : values())
            if (str.equalsIgnoreCase(v.name()))
                return v;
            throw new ExpressionBuildException();
        }
        public Object transform(Object value) throws ExpressionBuildException {
            if (value == null)
                return null;

            // Check if the value is of the required type (an enum is this should be OK).
            if (type.equals(value.getClass()))
                return value;

            // Check if the value is a literal, then map it.
            if (value instanceof Literal)
                return valueOf((Literal) value);

            // Check if is not an array.
            if (!(value instanceof Object[]))
                throw new ExpressionBuildException();

            // Is an array, so treat as such.
            Object[] valueAsArray = (Object[]) value;
            if (valueAsArray.length == 0)
                return value;

            //Check if the types are what is excpected.
            boolean enumType = true;
            for (Object v : valueAsArray) {
                if (!type.equals(v.getClass())) {
                    enumType = false;
                    break;
                }
            }
            if (enumType)
                return value;

            // Last chance is that the values are literals.
            Object[] mappedValue = new Object[valueAsArray.length];
            for (int i = 0; i < valueAsArray.length; i++) {
                if (valueAsArray[i] instanceof Literal) {
                    mappedValue[i] = valueOf((Literal) valueAsArray[i]);
                } else
                    throw new ExpressionBuildException();
            }
            return mappedValue;
        }
        protected Optional<String> _validate(Operator op, Object value, boolean array, Class<?> type) {
            // A null type means a null value, an empty array or an array of nulls, which
            // are all technically valid.
            if (type == null)
                return Optional.empty();
            if (this.type.equals(type))
                return Optional.empty();
            return Optional.of("expected enum " + this.type.getSimpleName());
        }
    }

}
