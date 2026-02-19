package com.effacy.jui.filter.builder;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
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

        /**
         * Returns a validator that runs this validator first, then the given
         * validator if this one passes.
         */
        default ITypeValidator and(ITypeValidator next) {
            return (op, value, array, type) -> {
                Optional<String> result = this.validate(op, value, array, type);
                if (result.isPresent())
                    return result;
                return next.validate(op, value, array, type);
            };
        }
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
     * Basic {@link Integer}, {@link Long}, {@link Double} or {@link Float} type.
     */
    public static class BooleanType extends Type {
        public BooleanType(Operator...operators) {
            super(null, operators);
        }
        public BooleanType(ITypeValidator validator, Operator...operators) {
            super(validator, operators);
        }
        protected Optional<String> _validate(Operator op, Object value, boolean array, Class<?> type) {
            if ((value == null) || Boolean.class.equals(type))
                return Optional.empty();
            return Optional.of("expected a boolean");
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

    /**
     * Enumeration type backed by a dynamic set of string values rather than a Java
     * enum class. Values are resolved case-insensitively and stored as strings.
     * <p>
     * This supports models where the valid values are not known at compile time
     * (e.g. defined in YAML or loaded from a database).
     */
    public static class DynamicEnumType extends Type {

        /**
         * Map from lower-cased value to canonical form (preserves declaration order).
         */
        private Map<String, String> values;

        /**
         * Name of the enumeration (for error messages).
         */
        private String name;

        public DynamicEnumType(String name, Collection<String> values, Operator...operators) {
            super(null, operators);
            this.name = name;
            this.values = new LinkedHashMap<>();
            for (String v : values)
                this.values.put(v.toLowerCase(), v);
        }

        public DynamicEnumType(String name, Collection<String> values, ITypeValidator validator, Operator...operators) {
            super(validator, operators);
            this.name = name;
            this.values = new LinkedHashMap<>();
            for (String v : values)
                this.values.put(v.toLowerCase(), v);
        }

        /**
         * The name of the enumeration (for error messages).
         */
        public String name() {
            return name;
        }

        /**
         * The valid values in their canonical form (declaration order).
         */
        public Collection<String> values() {
            return values.values();
        }

        /**
         * Resolves a literal to its canonical value (case-insensitive).
         *
         * @param literal
         *                the literal to resolve.
         * @return the canonical value.
         * @throws ExpressionBuildException
         *                                  if the literal does not match any valid
         *                                  value.
         */
        public String valueOf(Literal literal) throws ExpressionBuildException {
            if (literal == null)
                return null;
            String canonical = values.get(literal.value().toLowerCase());
            if (canonical == null)
                throw new ExpressionBuildException("'" + literal.value() + "' is not a valid value for " + name);
            return canonical;
        }

        @Override
        public Object transform(Object value) throws ExpressionBuildException {
            if (value == null)
                return null;

            // Already a string — validate against the allowed set.
            if (value instanceof String) {
                String canonical = values.get(((String) value).toLowerCase());
                if (canonical == null)
                    throw new ExpressionBuildException("'" + value + "' is not a valid value for " + name);
                return canonical;
            }

            // Literal from parser.
            if (value instanceof Literal)
                return valueOf((Literal) value);

            // Not an array — invalid.
            if (!(value instanceof Object[]))
                throw new ExpressionBuildException();

            // Array handling.
            Object[] valueAsArray = (Object[]) value;
            if (valueAsArray.length == 0)
                return value;

            // Try to resolve each element.
            String[] result = new String[valueAsArray.length];
            for (int i = 0; i < valueAsArray.length; i++) {
                if (valueAsArray[i] == null) {
                    result[i] = null;
                } else if (valueAsArray[i] instanceof String) {
                    String canonical = values.get(((String) valueAsArray[i]).toLowerCase());
                    if (canonical == null)
                        throw new ExpressionBuildException("'" + valueAsArray[i] + "' is not a valid value for " + name);
                    result[i] = canonical;
                } else if (valueAsArray[i] instanceof Literal) {
                    result[i] = valueOf((Literal) valueAsArray[i]);
                } else {
                    throw new ExpressionBuildException();
                }
            }
            return result;
        }

        @Override
        protected Optional<String> _validate(Operator op, Object value, boolean array, Class<?> type) {
            if (type == null)
                return Optional.empty();
            if (!String.class.equals(type))
                return Optional.of("expected enum " + name);
            // Check that string values are members of the allowed set.
            if (array) {
                for (Object element : (Object[]) value) {
                    if ((element instanceof String) && !values.containsKey(((String) element).toLowerCase()))
                        return Optional.of("'" + element + "' is not a valid value for " + name);
                }
            } else if ((value instanceof String) && !values.containsKey(((String) value).toLowerCase()))
                return Optional.of("'" + value + "' is not a valid value for " + name);
            return Optional.empty();
        }
    }

    /**
     * Date type that converts string values to {@link Date}.
     * <p>
     * Supports pluggable parsers for accepting various date formats. The default
     * parser accepts ISO 8601 date strings ({@code yyyy-MM-dd}).
     * <p>
     * Parsed dates have the time component set to midnight (00:00:00).
     */
    public static class DateType extends Type {

        /**
         * Parses a string value into a {@link Date}.
         */
        @FunctionalInterface
        public interface Parser {
            Date parse(String value) throws ExpressionBuildException;

            /**
             * Returns a parser that tries this parser first, falling back to the
             * given parser if this one fails.
             */
            default Parser or(Parser fallback) {
                return str -> {
                    try {
                        return this.parse(str);
                    } catch (ExpressionBuildException e) {
                        return fallback.parse(str);
                    }
                };
            }
        }

        /**
         * ISO 8601 date parser ({@code yyyy-MM-dd}). Requires zero-padded values.
         * <p>
         * GWT-compatible: uses only {@link Date} deprecated constructors.
         */
        @SuppressWarnings("deprecation")
        public static Parser ISO = str -> {
            if ((str == null) || (str.length() != 10) || (str.charAt(4) != '-') || (str.charAt(7) != '-'))
                throw new ExpressionBuildException("invalid date: " + str);
            try {
                int year = Integer.parseInt(str.substring(0, 4));
                int month = Integer.parseInt(str.substring(5, 7));
                int day = Integer.parseInt(str.substring(8, 10));
                if ((month < 1) || (month > 12) || (day < 1) || (day > 31))
                    throw new ExpressionBuildException("invalid date: " + str);
                return new Date(year - 1900, month - 1, day);
            } catch (NumberFormatException e) {
                throw new ExpressionBuildException("invalid date: " + str);
            }
        };

        private Parser parser;

        public DateType(Operator...operators) {
            this(ISO, operators);
        }

        public DateType(Parser parser, Operator...operators) {
            super(null, operators);
            this.parser = parser;
        }

        public DateType(Parser parser, ITypeValidator validator, Operator...operators) {
            super(validator, operators);
            this.parser = parser;
        }

        public Parser parser() {
            return parser;
        }

        @Override
        public Object transform(Object value) throws ExpressionBuildException {
            if (value == null)
                return null;
            if (value instanceof Date)
                return value;
            if (value instanceof String)
                return parser.parse((String) value);
            if (value instanceof Literal)
                return parser.parse(((Literal) value).value());
            if (!(value instanceof Object[]))
                throw new ExpressionBuildException("expected a date");

            // Array handling.
            Object[] valueAsArray = (Object[]) value;
            if (valueAsArray.length == 0)
                return value;
            Date[] result = new Date[valueAsArray.length];
            for (int i = 0; i < valueAsArray.length; i++) {
                if (valueAsArray[i] == null)
                    result[i] = null;
                else if (valueAsArray[i] instanceof Date)
                    result[i] = (Date) valueAsArray[i];
                else if (valueAsArray[i] instanceof String)
                    result[i] = parser.parse((String) valueAsArray[i]);
                else if (valueAsArray[i] instanceof Literal)
                    result[i] = parser.parse(((Literal) valueAsArray[i]).value());
                else
                    throw new ExpressionBuildException("expected a date");
            }
            return result;
        }

        @Override
        protected Optional<String> _validate(Operator op, Object value, boolean array, Class<?> type) {
            if (type == null)
                return Optional.empty();
            if (Date.class.equals(type))
                return Optional.empty();
            if (!String.class.equals(type))
                return Optional.of("expected a date");
            // Attempt to parse string values to validate format.
            if (array) {
                for (Object element : (Object[]) value) {
                    if (element instanceof String) {
                        try {
                            parser.parse((String) element);
                        } catch (ExpressionBuildException e) {
                            return Optional.of(e.getMessage());
                        }
                    }
                }
            } else if (value instanceof String) {
                try {
                    parser.parse((String) value);
                } catch (ExpressionBuildException e) {
                    return Optional.of(e.getMessage());
                }
            }
            return Optional.empty();
        }
    }

    /**
     * Date/time type that converts string values to {@link Date}.
     * <p>
     * Supports pluggable parsers for accepting various date/time formats. The
     * default parser accepts ISO 8601 date ({@code yyyy-MM-dd}) and date-time
     * ({@code yyyy-MM-dd'T'HH:mm:ss}) strings.
     */
    public static class DateTimeType extends Type {

        /**
         * Parses a string value into a {@link Date}.
         */
        @FunctionalInterface
        public interface Parser {
            Date parse(String value) throws ExpressionBuildException;

            /**
             * Returns a parser that tries this parser first, falling back to the
             * given parser if this one fails.
             */
            default Parser or(Parser fallback) {
                return str -> {
                    try {
                        return this.parse(str);
                    } catch (ExpressionBuildException e) {
                        return fallback.parse(str);
                    }
                };
            }
        }

        /**
         * ISO 8601 parser accepting both date ({@code yyyy-MM-dd}) and date-time
         * ({@code yyyy-MM-dd'T'HH:mm:ss}) formats. Date-only values are treated as
         * midnight.
         * <p>
         * GWT-compatible: uses only {@link Date} deprecated constructors.
         */
        @SuppressWarnings("deprecation")
        public static Parser ISO = str -> {
            if (str == null)
                throw new ExpressionBuildException("invalid date/time: null");
            try {
                // Try date-time: yyyy-MM-ddTHH:mm:ss
                int tIdx = str.indexOf('T');
                if ((tIdx == 10) && (str.length() == 19)
                        && (str.charAt(4) == '-') && (str.charAt(7) == '-')
                        && (str.charAt(13) == ':') && (str.charAt(16) == ':')) {
                    int year = Integer.parseInt(str.substring(0, 4));
                    int month = Integer.parseInt(str.substring(5, 7));
                    int day = Integer.parseInt(str.substring(8, 10));
                    int hour = Integer.parseInt(str.substring(11, 13));
                    int minute = Integer.parseInt(str.substring(14, 16));
                    int second = Integer.parseInt(str.substring(17, 19));
                    if ((month < 1) || (month > 12) || (day < 1) || (day > 31)
                            || (hour < 0) || (hour > 23) || (minute < 0) || (minute > 59)
                            || (second < 0) || (second > 59))
                        throw new ExpressionBuildException("invalid date/time: " + str);
                    return new Date(year - 1900, month - 1, day, hour, minute, second);
                }
                // Fall back to date-only: yyyy-MM-dd
                if ((str.length() == 10) && (str.charAt(4) == '-') && (str.charAt(7) == '-')) {
                    int year = Integer.parseInt(str.substring(0, 4));
                    int month = Integer.parseInt(str.substring(5, 7));
                    int day = Integer.parseInt(str.substring(8, 10));
                    if ((month < 1) || (month > 12) || (day < 1) || (day > 31))
                        throw new ExpressionBuildException("invalid date/time: " + str);
                    return new Date(year - 1900, month - 1, day);
                }
                throw new ExpressionBuildException("invalid date/time: " + str);
            } catch (NumberFormatException e) {
                throw new ExpressionBuildException("invalid date/time: " + str);
            }
        };

        private Parser parser;

        public DateTimeType(Operator...operators) {
            this(ISO, operators);
        }

        public DateTimeType(Parser parser, Operator...operators) {
            super(null, operators);
            this.parser = parser;
        }

        public DateTimeType(Parser parser, ITypeValidator validator, Operator...operators) {
            super(validator, operators);
            this.parser = parser;
        }

        public Parser parser() {
            return parser;
        }

        @Override
        public Object transform(Object value) throws ExpressionBuildException {
            if (value == null)
                return null;
            if (value instanceof Date)
                return value;
            if (value instanceof String)
                return parser.parse((String) value);
            if (value instanceof Literal)
                return parser.parse(((Literal) value).value());
            if (!(value instanceof Object[]))
                throw new ExpressionBuildException("expected a date/time");

            // Array handling.
            Object[] valueAsArray = (Object[]) value;
            if (valueAsArray.length == 0)
                return value;
            Date[] result = new Date[valueAsArray.length];
            for (int i = 0; i < valueAsArray.length; i++) {
                if (valueAsArray[i] == null)
                    result[i] = null;
                else if (valueAsArray[i] instanceof Date)
                    result[i] = (Date) valueAsArray[i];
                else if (valueAsArray[i] instanceof String)
                    result[i] = parser.parse((String) valueAsArray[i]);
                else if (valueAsArray[i] instanceof Literal)
                    result[i] = parser.parse(((Literal) valueAsArray[i]).value());
                else
                    throw new ExpressionBuildException("expected a date/time");
            }
            return result;
        }

        @Override
        protected Optional<String> _validate(Operator op, Object value, boolean array, Class<?> type) {
            if (type == null)
                return Optional.empty();
            if (Date.class.equals(type))
                return Optional.empty();
            if (!String.class.equals(type))
                return Optional.of("expected a date/time");
            // Attempt to parse string values to validate format.
            if (array) {
                for (Object element : (Object[]) value) {
                    if (element instanceof String) {
                        try {
                            parser.parse((String) element);
                        } catch (ExpressionBuildException e) {
                            return Optional.of(e.getMessage());
                        }
                    }
                }
            } else if (value instanceof String) {
                try {
                    parser.parse((String) value);
                } catch (ExpressionBuildException e) {
                    return Optional.of(e.getMessage());
                }
            }
            return Optional.empty();
        }
    }

}
