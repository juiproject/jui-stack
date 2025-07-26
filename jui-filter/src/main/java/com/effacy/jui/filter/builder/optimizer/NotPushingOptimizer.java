package com.effacy.jui.filter.builder.optimizer;

import com.effacy.jui.filter.builder.ExpressionBuilder;
import com.effacy.jui.filter.builder.IExpressionBuilder.Operator;

/**
 * Optimizer that pushes NOT operations into comparison operators to create more efficient representations:
 * <ul>
 * <li>NOT(field = value) → field != value</li>
 * <li>NOT(field != value) → field = value</li>
 * <li>NOT(field > value) → field <= value</li>
 * <li>NOT(field >= value) → field < value</li>
 * <li>NOT(field < value) → field >= value</li>
 * <li>NOT(field <= value) → field > value</li>
 * <li>NOT(field IN [values]) → field NOT IN [values]</li>
 * <li>NOT(field NOT IN [values]) → field IN [values]</li>
 * </ul>
 * 
 * This optimization improves readability and may allow other optimizers to be more effective
 * by eliminating NOT expressions that wrap simple comparisons.
 */
public class NotPushingOptimizer<F> implements IExpressionOptimizer<F> {
    
    private final ExpressionBuilder<F> builder;
    
    public NotPushingOptimizer(ExpressionBuilder<F> builder) {
        this.builder = builder;
    }

    @Override
    public ExpressionBuilder.Expression<F> optimize(ExpressionBuilder.Expression<F> expression) {
        if (expression == null) {
            return null;
        }
        
        if (expression instanceof ExpressionBuilder<F>.NOTExpression) {
            return optimizeNot((ExpressionBuilder<F>.NOTExpression) expression);
        } else if (expression instanceof ExpressionBuilder<F>.ANDExpression) {
            return optimizeAnd((ExpressionBuilder<F>.ANDExpression) expression);
        } else if (expression instanceof ExpressionBuilder<F>.ORExpression) {
            return optimizeOr((ExpressionBuilder<F>.ORExpression) expression);
        } else {
            return expression;
        }
    }
    
    private ExpressionBuilder.Expression<F> optimizeNot(ExpressionBuilder<F>.NOTExpression notExpr) {
        // Get the inner expression
        ExpressionBuilder.Expression<F> inner = getInnerExpression(notExpr);
        
        if (inner instanceof ExpressionBuilder<F>.ComparisonExpression) {
            ExpressionBuilder<F>.ComparisonExpression compExpr = (ExpressionBuilder<F>.ComparisonExpression) inner;
            
            F field = compExpr.field();
            Operator operator = compExpr.operator();
            Object value = compExpr.value();
            
            // Apply negation transformation
            Operator negatedOperator = negateOperator(operator);
            if (negatedOperator != null) {
                return builder.term(field, negatedOperator, value);
            }
        }
        
        // If we can't push the NOT down, recursively optimize the inner expression
        ExpressionBuilder.Expression<F> optimizedInner = optimize(inner);
        if (optimizedInner == inner) {
            return notExpr; // No change
        }
        return builder.not(optimizedInner);
    }
    
    private ExpressionBuilder.Expression<F> optimizeAnd(ExpressionBuilder<F>.ANDExpression andExpr) {
        return optimizeLogicalExpression(andExpr, true);
    }
    
    private ExpressionBuilder.Expression<F> optimizeOr(ExpressionBuilder<F>.ORExpression orExpr) {
        return optimizeLogicalExpression(orExpr, false);
    }
    
    private ExpressionBuilder.Expression<F> optimizeLogicalExpression(ExpressionBuilder.Expression<F> expr, boolean isAnd) {
        java.util.List<ExpressionBuilder.Expression<F>> children = collectDirectChildren(expr);
        java.util.List<ExpressionBuilder.Expression<F>> optimizedChildren = new java.util.ArrayList<>();
        
        boolean changed = false;
        for (ExpressionBuilder.Expression<F> child : children) {
            ExpressionBuilder.Expression<F> optimized = optimize(child);
            optimizedChildren.add(optimized);
            if (optimized != child) {
                changed = true;
            }
        }
        
        if (!changed) {
            return expr; // No changes
        }
        
        if (optimizedChildren.size() == 1) {
            return optimizedChildren.get(0);
        }
        
        return isAnd ? builder.and(optimizedChildren) : builder.or(optimizedChildren);
    }
    
    private java.util.List<ExpressionBuilder.Expression<F>> collectDirectChildren(ExpressionBuilder.Expression<F> expr) {
        java.util.List<ExpressionBuilder.Expression<F>> children = new java.util.ArrayList<>();
        expr.traverse((depth, child) -> {
            if (depth == 1) { // Direct children only
                children.add(child);
            }
        });
        return children;
    }
    
    private ExpressionBuilder.Expression<F> getInnerExpression(ExpressionBuilder<F>.NOTExpression notExpr) {
        final ExpressionBuilder.Expression<F>[] inner = new ExpressionBuilder.Expression[1];
        notExpr.traverse((depth, expr) -> {
            if (depth == 1) { // Direct child of NOT
                inner[0] = expr;
            }
        });
        return inner[0];
    }
    
    /**
     * Returns the negated version of an operator, or null if negation is not supported.
     */
    private Operator negateOperator(Operator operator) {
        switch (operator) {
            case EQ:
                return Operator.NEQ;
            case NEQ:
                return Operator.EQ;
            case GT:
                return Operator.LTE;
            case GTE:
                return Operator.LT;
            case LT:
                return Operator.GTE;
            case LTE:
                return Operator.GT;
            case IN:
                return Operator.NOT_IN;
            case NOT_IN:
                return Operator.IN;
            // String operations cannot be reliably negated due to implementation-dependent semantics
            case CONTAINS:
            case STARTS_WITH:
            case ENDS_WITH:
            default:
                return null; // Cannot negate this operator
        }
    }
}