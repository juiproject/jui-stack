package com.effacy.jui.filter.builder;

public interface Field {
    
    public interface Type<T> {

        public T cast(Object v) throws ExpressionBuildException;
        
    }
    
    public static class StringType implements Type<String> {
        public String cast(Object v) throws ExpressionBuildException {
            if (v == null)
                return null;
            if (v instanceof String)
                return (String) v;
            return v.toString();
        }
    }

    public static class LongType implements Type<Long> {
        public Long cast(Object v) throws ExpressionBuildException {
            if (v == null)
                return null;
            if (v instanceof Number)
                return ((Number) v).longValue();
            throw new ExpressionBuildException();
        }
    }

    public static class IntegerType implements Type<Integer> {
        public Integer cast(Object v) throws ExpressionBuildException {
            if (v == null)
                return null;
            if (v instanceof Number)
                return ((Number) v).intValue();
            throw new ExpressionBuildException();
        }
    }

    public static class DoubleType implements Type<Double> {
        public Double cast(Object v) throws ExpressionBuildException {
            if (v == null)
                return null;
            if (v instanceof Number)
                return ((Number) v).doubleValue();
            throw new ExpressionBuildException();
        }
    }

    public static class EnumType<T extends Enum<T>> implements Type<T> {
        private Class<T> type;
        public EnumType(Class<T> type) {
            this.type = type;
        }
        public T[] values() {
            return type.getEnumConstants();
        }
        public T cast(Object v) throws ExpressionBuildException {
            if (v == null)
                return null;
            for(T val : type.getEnumConstants()) {
                if (v == val)
                    return val;
            }
            for(T val : type.getEnumConstants()) {
                if (v.toString().toLowerCase().equals(val.name().toLowerCase()))
                    return val;
            }
            throw new ExpressionBuildException();
        }
    }

    public Type<?> type();

}
