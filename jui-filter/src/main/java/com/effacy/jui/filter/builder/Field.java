package com.effacy.jui.filter.builder;

import java.util.Optional;

import com.effacy.jui.filter.builder.IExpressionBuilder.Operator;

/**
 * Represents a types field for use in building expressions.
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
            if (value != null)
                type = !array ? value.getClass() : value.getClass().getComponentType();
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
         *              is {@code null}).
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
        protected Optional<String> _validate(Operator op, Object value, boolean array, Class<?> type) {
            if ((value == null) || this.type.equals(type))
                return Optional.empty();
            return Optional.of("expected enum " + this.type.getSimpleName());
        }
    }

}
