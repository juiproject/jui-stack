package com.effacy.jui.filter.builder;

/**
 * When there was a problem building an expression.
 * <p>
 * Generally this should not occur and it means a field type was not properly
 * handled.
 */
public class ExpressionBuildException extends Error {
    
    public ExpressionBuildException() {
        super();
    }
    
    public ExpressionBuildException(String message) {
        super(message);
    }
    
    public ExpressionBuildException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ExpressionBuildException(Throwable cause) {
        super(cause);
    }
}