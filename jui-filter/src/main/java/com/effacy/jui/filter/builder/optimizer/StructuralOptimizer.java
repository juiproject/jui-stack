package com.effacy.jui.filter.builder.optimizer;

import java.util.ArrayList;
import java.util.List;

import com.effacy.jui.filter.builder.ExpressionBuilder;

/**
 * Optimizer that performs structural simplifications on expressions:
 * <ul>
 * <li>Flattens nested AND/OR expressions: (A AND (B AND C)) → (A AND B AND C)</li>
 * <li>Eliminates double negations: NOT(NOT A) → A</li>
 * <li>Handles empty expression lists</li>
 * <li>Simplifies single-element lists: AND([A]) → A</li>
 * </ul>
 */
public class StructuralOptimizer<F> implements IExpressionOptimizer<F> {
    
    private final ExpressionBuilder<F> builder;
    
    public StructuralOptimizer(ExpressionBuilder<F> builder) {
        this.builder = builder;
    }

    @Override
    public ExpressionBuilder.Expression<F> optimize(ExpressionBuilder.Expression<F> expression) {
        if (expression == null) {
            return null;
        }
        
        if (expression instanceof ExpressionBuilder<F>.ANDExpression) {
            return optimizeAnd((ExpressionBuilder<F>.ANDExpression) expression);
        } else if (expression instanceof ExpressionBuilder<F>.ORExpression) {
            return optimizeOr((ExpressionBuilder<F>.ORExpression) expression);
        } else if (expression instanceof ExpressionBuilder<F>.NOTExpression) {
            return optimizeNot((ExpressionBuilder<F>.NOTExpression) expression);
        } else {
            return expression;
        }
    }
    
    private ExpressionBuilder.Expression<F> optimizeAnd(ExpressionBuilder<F>.ANDExpression andExpr) {
        List<ExpressionBuilder.Expression<F>> flattenedExpressions = new ArrayList<>();
        collectAndTerms(andExpr, flattenedExpressions);
        return createOptimalExpression(flattenedExpressions, true);
    }
    
    private void collectAndTerms(ExpressionBuilder.Expression<F> expr, List<ExpressionBuilder.Expression<F>> result) {
        if (expr instanceof ExpressionBuilder<F>.ANDExpression) {
            // For AND expressions, recursively collect terms from children
            expr.traverse((depth, child) -> {
                if (depth == 1) { // Direct children only
                    collectAndTerms(child, result);
                }
            });
        } else {
            // For non-AND expressions, optimize and add to result
            ExpressionBuilder.Expression<F> optimized = optimize(expr);
            if (optimized != null) {
                result.add(optimized);
            }
        }
    }
    
    private ExpressionBuilder.Expression<F> optimizeOr(ExpressionBuilder<F>.ORExpression orExpr) {
        List<ExpressionBuilder.Expression<F>> flattenedExpressions = new ArrayList<>();
        collectOrTerms(orExpr, flattenedExpressions);
        return createOptimalExpression(flattenedExpressions, false);
    }
    
    private void collectOrTerms(ExpressionBuilder.Expression<F> expr, List<ExpressionBuilder.Expression<F>> result) {
        if (expr instanceof ExpressionBuilder<F>.ORExpression) {
            // For OR expressions, recursively collect terms from children
            expr.traverse((depth, child) -> {
                if (depth == 1) { // Direct children only
                    collectOrTerms(child, result);
                }
            });
        } else {
            // For non-OR expressions, optimize and add to result
            ExpressionBuilder.Expression<F> optimized = optimize(expr);
            if (optimized != null) {
                result.add(optimized);
            }
        }
    }
    
    private ExpressionBuilder.Expression<F> optimizeNot(ExpressionBuilder<F>.NOTExpression notExpr) {
        // Get the inner expression by accessing it through reflection or traversal
        final ExpressionBuilder.Expression<F>[] innerExpr = new ExpressionBuilder.Expression[1];
        notExpr.traverse((depth, expr) -> {
            if (depth == 1) { // Direct child of NOT
                innerExpr[0] = expr;
            }
        });
        
        if (innerExpr[0] instanceof ExpressionBuilder<F>.NOTExpression) {
            // Double negation: NOT(NOT A) → A
            final ExpressionBuilder.Expression<F>[] doubleInner = new ExpressionBuilder.Expression[1];
            innerExpr[0].traverse((depth, expr) -> {
                if (depth == 1) { // Direct child of inner NOT
                    doubleInner[0] = expr;
                }
            });
            return optimize(doubleInner[0]);
        }
        
        // Optimize the inner expression
        ExpressionBuilder.Expression<F> optimizedInner = optimize(innerExpr[0]);
        if (optimizedInner == innerExpr[0]) {
            return notExpr; // No change
        }
        return builder.not(optimizedInner);
    }
    
    private ExpressionBuilder.Expression<F> createOptimalExpression(
            List<ExpressionBuilder.Expression<F>> expressions, boolean isAnd) {
        
        if (expressions.isEmpty()) {
            return null; // Empty list - let caller handle
        }
        
        if (expressions.size() == 1) {
            return expressions.get(0); // Single element - return it directly
        }
        
        // Multiple elements - create appropriate expression
        return isAnd ? builder.and(expressions) : builder.or(expressions);
    }
}