package com.effacy.jui.filter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.effacy.jui.filter.builder.ExpressionBuildException;
import com.effacy.jui.filter.builder.IExpressionBuilder;

/**
 * Collection of support methods to deal with values that arise in an
 * expression. This makes use of reflection so is not safe for use client-side.
 */
public final class ValueSupport {

    public static Long asLong(Object value) throws ExpressionBuildException {
        if (value == null)
            return null;
        if (value instanceof Long)
            return (Long) value;
        if (value instanceof Number)
            return ((Number) value).longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (Exception e) {
            throw new ExpressionBuildException();
        }
    }

    public static Long[] asLongArray(Object value) throws ExpressionBuildException {
        if (value == null)
            return null;
        if (!value.getClass().isArray())
            return null;
        Object[] array = (Object[]) value;
        Long[] result = (Long[]) Array.newInstance(Long.class, array.length);
        for (int i = 0; i < array.length; i++)
            result[i] = asLong(array[i]);
        return result;
    }

    public static Integer asInteger(Object value) throws ExpressionBuildException {
        if (value == null)
            return null;
        if (value instanceof Integer)
            return (Integer) value;
        if (value instanceof Number)
            return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            throw new ExpressionBuildException();
        }
    }

    public static Integer[] asIntegerArray(Object value) throws ExpressionBuildException {
        if (value == null)
            return null;
        if (!value.getClass().isArray())
            return null;
        Object[] array = (Object[]) value;
        Integer[] result = (Integer[]) Array.newInstance(Integer.class, array.length);
        for (int i = 0; i < array.length; i++)
            result[i] = asInteger(array[i]);
        return result;
    }

    public static Double asDouble(Object value) throws ExpressionBuildException {
        if (value == null)
            return null;
        if (value instanceof Double)
            return (Double) value;
        if (value instanceof Number)
            return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            throw new ExpressionBuildException();
        }
    }

    public static Double[] asDoubleArray(Object value) throws ExpressionBuildException {
        if (value == null)
            return null;
        if (!value.getClass().isArray())
            return null;
        Object[] array = (Object[]) value;
        Double[] result = (Double[]) Array.newInstance(Double.class, array.length);
        for (int i = 0; i < array.length; i++)
            result[i] = asDouble(array[i]);
        return result;
    }

    public static Boolean asBoolean(Object value) throws ExpressionBuildException {
        if (value instanceof Boolean)
            return (Boolean) value;
        return null;
    }

    public static String asString(Object value) throws ExpressionBuildException {
        if (value == null)
            return null;
        if (value instanceof String)
            return (String) value;
        return value.toString();
    }

    public static String[] asStringArray(Object value) throws ExpressionBuildException {
        if (value == null)
            return null;
        if (!value.getClass().isArray())
            return null;
        Object[] array = (Object[]) value;
        String[] result = (String[]) Array.newInstance(String.class, array.length);
        for (int i = 0; i < array.length; i++)
            result[i] = asString(array[i]);
        return result;
    }

    public static <T extends Enum<T>> T asEnum(Class<T> klass, Object value) throws ExpressionBuildException {
        if (value == null)
            return null;
        if (klass.isInstance(value))
            return klass.cast(value);
        String svalue = null;
        if (value instanceof String)
            svalue = (String) value;
        else if (value instanceof IExpressionBuilder.Literal)
            svalue = ((IExpressionBuilder.Literal) value).value();
        try {
            return Enum.valueOf(klass, svalue);
        } catch (Exception e) {
            throw new ExpressionBuildException();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> T[] asEnumArray(Class<T> klass, Object value) throws ExpressionBuildException {
        if (value == null)
            return null;
        if (!value.getClass().isArray())
            return null;
        Object[] array = (Object[]) value;
        T[] result = (T[]) Array.newInstance(klass, array.length);
        for (int i = 0; i < array.length; i++)
            result[i] = asEnum(klass, array[i]);
        return result;
    }

    /**
     * See {@link #asValue(Object, Function)} but for an array. Any unmapped values
     * (where {@link #asValue(Object, Function)} returns a {@code null}) are ignored
     * and not included in the result.
     * <p>
     * Note that the result is not an array (not supported for generics) but rather
     * a list (which supports generics).
     * 
     * @param <T>
     *               the type being mapped to.
     * @param value
     *               the value being mapped.
     * @param mapper
     *               to map the value (if not supplied then the object is assumed to
     *               be of the expected type).
     * @return the mapped list of values (or {@code null}).
     */
    public static <T> List<T> asValueArray(Object value, Function<Object,T> mapper) throws ExpressionBuildException {
        if (value == null)
            return null;
        if (!value.getClass().isArray())
            return null;
        Object[] array = (Object[]) value;
        List<T> result = new ArrayList<>();
        for (int i = 0; i < array.length; i++) {
            T val = asValue(array[i], mapper);
            if (val != null)
                result.add(val);
        }
        return result;
    }

    /**
     * As with {@link #asValueArray(Object, Function)} but if not an array then
     * coerces into an array of a single entry.
     * <p>
     * Note that the result is not an array (not supported for generics) but rather
     * a list (which supports generics).
     */
    public static <T> List<T> asValueArrayCoerce(Object value, Function<Object,T> mapper) throws ExpressionBuildException {
        List<T> values = asValueArray(value, mapper);
        if (values != null)
            return values;
        T val = asValue(values, mapper);
        if (val == null)
            return null;
        List<T> result = new ArrayList<>();
        result.add(val);
        return result;
    }

    /**
     * Takes a general object (of an expected but un-specified type) and maps to an
     * instance of the parameter type.
     * 
     * @param <T>
     *               the type being mapped to.
     * @param value
     *               the value being mapped.
     * @param mapper
     *               to map the value (if not supplied then the object is assumed to
     *               be of the expected type).
     * @return the mapped value (or {@code null}).
     */
    @SuppressWarnings("unchecked")
    public static <T> T asValue(Object value, Function<Object,T> mapper) throws ExpressionBuildException {
        if (value == null)
            return null;
        try {
            if (mapper == null)
                return (T) value;
            return mapper.apply(value);
        } catch (Throwable e) {
            throw new ExpressionBuildException(e);
        }
    }

}
