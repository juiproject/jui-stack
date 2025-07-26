package com.effacy.jui.filter.builder.optimizer;

import java.util.ArrayList;
import java.util.List;

import com.effacy.jui.filter.builder.ExpressionBuilder;

/**
 * Optimizer that applies boolean logic simplifications:
 * <ul>
 * <li>Identity elimination: A AND true → A, A OR false → A</li>
 * <li>Absorption: A OR (A AND B) → A, A AND (A OR B) → A</li>
 * <li>Idempotence: A AND A → A, A OR A → A</li>
 * <li>Contradiction elimination: A AND (NOT A) → false, A OR (NOT A) → true</li>
 * <li>Dominance: A OR true → true, A AND false → false</li>
 * </ul>
 */
public class BooleanLogicOptimizer<F> implements IExpressionOptimizer<F> {
    
    private final ExpressionBuilder<F> builder = new ExpressionBuilder<>();

    @Override
    public ExpressionBuilder.Expression<F> optimize(ExpressionBuilder.Expression<F> expression) {
        if (expression == null)
            return null;
        if (expression instanceof ExpressionBuilder<F>.ANDExpression)
            return optimizeAnd((ExpressionBuilder<F>.ANDExpression) expression);
        if (expression instanceof ExpressionBuilder<F>.ORExpression)
            return optimizeOr((ExpressionBuilder<F>.ORExpression) expression);
        return expression;
    }
    
    private ExpressionBuilder.Expression<F> optimizeAnd(ExpressionBuilder<F>.ANDExpression andExpr) {
        List<ExpressionBuilder.Expression<F>> terms = collectDirectChildren(andExpr);
        List<ExpressionBuilder.Expression<F>> finalTerms = new ArrayList<>();
        List<ExpressionBuilder.Expression<F>> negatedTerms = new ArrayList<>();
        
        // Check for all-boolean-expression optimization
        if (areAllBooleanExpressions(terms))
            return evaluateBooleanAnd(terms);
        
        // First pass: collect optimized terms and track negations
        for (ExpressionBuilder.Expression<F> term : terms) {
            ExpressionBuilder.Expression<F> optimized = optimize(term);
            if (optimized == null)
                continue;
            
            if (optimized instanceof ExpressionBuilder<F>.NOTExpression) {
                ExpressionBuilder.Expression<F> innerTerm = getInnerExpression(optimized);
                if (!containsEqual(negatedTerms, innerTerm)) {
                    negatedTerms.add(innerTerm);
                }
            }
            
            // Check for contradictions: A AND NOT A → false
            if (containsEqual(negatedTerms, optimized)) {
                return builder.bool(false); // Contradiction - entire AND is false
            }
            
            if (optimized instanceof ExpressionBuilder<F>.NOTExpression) {
                ExpressionBuilder.Expression<F> innerTerm = getInnerExpression(optimized);
                if (containsEqual(finalTerms, innerTerm)) {
                    return builder.bool(false); // Contradiction - entire AND is false
                }
            }
            
            // Identity elimination: A AND true → A (skip true)
            if (optimized instanceof ExpressionBuilder<F>.BoolExpression) {
                ExpressionBuilder<F>.BoolExpression boolExpr = (ExpressionBuilder<F>.BoolExpression) optimized;
                if (boolExpr.getValue()) {
                    // Skip true in AND - it's the identity element
                    continue;
                } 
                // false in AND makes entire expression false
                return builder.bool(false);
            }
            
            // Idempotence: remove duplicates
            if (!containsEqual(finalTerms, optimized)) {
                finalTerms.add(optimized);
            }
        }
        
        // Apply absorption: A AND (A OR B) → A
        finalTerms = applyAbsorptionToAnd(finalTerms);
        
        if (finalTerms.isEmpty())
            return null; // Empty AND
        if (finalTerms.size() == 1)
            return finalTerms.get(0); // Single term
        return builder.and(finalTerms);
    }
    
    private ExpressionBuilder.Expression<F> optimizeOr(ExpressionBuilder<F>.ORExpression orExpr) {
        List<ExpressionBuilder.Expression<F>> terms = collectDirectChildren(orExpr);
        List<ExpressionBuilder.Expression<F>> finalTerms = new ArrayList<>();
        List<ExpressionBuilder.Expression<F>> negatedTerms = new ArrayList<>();
        
        // Check for all-boolean-expression optimization
        if (areAllBooleanExpressions(terms))
            return evaluateBooleanOr(terms);
        
        // First pass: collect optimized terms and track negations
        for (ExpressionBuilder.Expression<F> term : terms) {
            ExpressionBuilder.Expression<F> optimized = optimize(term);
            if (optimized == null)
                continue;
            
            if (optimized instanceof ExpressionBuilder<F>.NOTExpression) {
                ExpressionBuilder.Expression<F> innerTerm = getInnerExpression(optimized);
                if (!containsEqual(negatedTerms, innerTerm)) {
                    negatedTerms.add(innerTerm);
                }
            }
            
            // Check for tautologies: A OR NOT A → true
            if (containsEqual(negatedTerms, optimized)) {
                return builder.bool(true); // Tautology - entire OR is true
            }
            
            if (optimized instanceof ExpressionBuilder<F>.NOTExpression) {
                ExpressionBuilder.Expression<F> innerTerm = getInnerExpression(optimized);
                if (containsEqual(finalTerms, innerTerm)) {
                    return builder.bool(true); // Tautology - entire OR is true
                }
            }
            
            // Identity elimination: A OR false → A (skip false)
            if (optimized instanceof ExpressionBuilder<F>.BoolExpression) {
                ExpressionBuilder<F>.BoolExpression boolExpr = (ExpressionBuilder<F>.BoolExpression) optimized;
                if (!boolExpr.getValue()) {
                    // Skip false in OR - it's the identity element
                    continue;
                } else {
                    // true in OR makes entire expression true
                    return builder.bool(true);
                }
            }
            
            // Idempotence: remove duplicates
            if (!containsEqual(finalTerms, optimized)) {
                finalTerms.add(optimized);
            }
        }
        
        // Apply absorption: A OR (A AND B) → A
        finalTerms = applyAbsorptionToOr(finalTerms);
        
        if (finalTerms.isEmpty())
            return null; // Empty OR
        if (finalTerms.size() == 1)
            return finalTerms.get(0); // Single term
        return builder.or(finalTerms);
    }
    
    private List<ExpressionBuilder.Expression<F>> collectDirectChildren(ExpressionBuilder.Expression<F> expr) {
        List<ExpressionBuilder.Expression<F>> children = new ArrayList<>();
        expr.traverse((depth, child) -> {
            if (depth == 1) { // Direct children only
                children.add(child);
            }
        });
        return children;
    }
    
    @SuppressWarnings("unchecked")
    private ExpressionBuilder.Expression<F> getInnerExpression(ExpressionBuilder.Expression<F> notExpr) {
        if (!(notExpr instanceof ExpressionBuilder<F>.NOTExpression)) {
            return notExpr;
        }
        
        final ExpressionBuilder.Expression<F>[] inner = new ExpressionBuilder.Expression[1];
        notExpr.traverse((depth, expr) -> {
            if (depth == 1) { // Direct child of NOT
                inner[0] = expr;
            }
        });
        return inner[0];
    }
    
    /**
     * Helper method to check if a list contains an expression that equals the given expression.
     * Uses the equals() method instead of relying on hashCode().
     */
    private boolean containsEqual(List<ExpressionBuilder.Expression<F>> list, ExpressionBuilder.Expression<F> expr) {
        for (ExpressionBuilder.Expression<F> item : list) {
            if (item.equals(expr))
                return true;
        }
        return false;
    }
    
    private List<ExpressionBuilder.Expression<F>> applyAbsorptionToAnd(List<ExpressionBuilder.Expression<F>> terms) {
        List<ExpressionBuilder.Expression<F>> result = new ArrayList<>();
        
        for (ExpressionBuilder.Expression<F> term : terms) {
            boolean shouldSkip = false;
            
            // Check if this term should be absorbed: A AND (A OR B) → A
            // If this is an OR expression that contains a simpler term also in the AND, skip it
            if (term instanceof ExpressionBuilder<F>.ORExpression) {
                List<ExpressionBuilder.Expression<F>> orTerms = collectDirectChildren(term);
                for (ExpressionBuilder.Expression<F> orTerm : orTerms) {
                    if (containsEqual(terms, orTerm) && !orTerm.equals(term)) {
                        // This OR expression contains a term that's also standalone in the AND
                        // So this OR expression should be absorbed (skipped)
                        shouldSkip = true;
                        break;
                    }
                }
            }
            
            if (!shouldSkip) {
                result.add(term);
            }
        }
        
        return result;
    }
    
    private List<ExpressionBuilder.Expression<F>> applyAbsorptionToOr(List<ExpressionBuilder.Expression<F>> terms) {
        List<ExpressionBuilder.Expression<F>> result = new ArrayList<>();
        
        for (ExpressionBuilder.Expression<F> term : terms) {
            boolean shouldSkip = false;
            
            // Check if this term should be absorbed: A OR (A AND B) → A
            // If this is an AND expression that contains a simpler term also in the OR, skip it
            if (term instanceof ExpressionBuilder<F>.ANDExpression) {
                List<ExpressionBuilder.Expression<F>> andTerms = collectDirectChildren(term);
                for (ExpressionBuilder.Expression<F> andTerm : andTerms) {
                    if (containsEqual(terms, andTerm) && !andTerm.equals(term)) {
                        // This AND expression contains a term that's also standalone in the OR
                        // So this AND expression should be absorbed (skipped)
                        shouldSkip = true;
                        break;
                    }
                }
            }
            
            if (!shouldSkip)
                result.add(term);
        }
        
        return result;
    }
    
    /**
     * Checks if all expressions in the list are boolean expressions (true/false literals).
     */
    private boolean areAllBooleanExpressions(List<ExpressionBuilder.Expression<F>> terms) {
        for (ExpressionBuilder.Expression<F> term : terms) {
            if (!(term instanceof ExpressionBuilder<F>.BoolExpression))
                return false;
        }
        return true;
    }
    
    /**
     * Evaluates a pure boolean AND expression where all terms are boolean literals.
     */
    private ExpressionBuilder.Expression<F> evaluateBooleanAnd(List<ExpressionBuilder.Expression<F>> terms) {
        // AND is true only if all terms are true
        for (ExpressionBuilder.Expression<F> term : terms) {
            ExpressionBuilder<F>.BoolExpression boolTerm = (ExpressionBuilder<F>.BoolExpression) term;
            if (!boolTerm.getValue()) {
                return builder.bool(false); // Any false makes the whole AND false
            }
        }
        return builder.bool(true); // All terms are true
    }
    
    /**
     * Evaluates a pure boolean OR expression where all terms are boolean literals.
     */
    private ExpressionBuilder.Expression<F> evaluateBooleanOr(List<ExpressionBuilder.Expression<F>> terms) {
        // OR is false only if all terms are false
        for (ExpressionBuilder.Expression<F> term : terms) {
            ExpressionBuilder<F>.BoolExpression boolTerm = (ExpressionBuilder<F>.BoolExpression) term;
            if (boolTerm.getValue()) {
                return builder.bool(true); // Any true makes the whole OR true
            }
        }
        return builder.bool(false); // All terms are false
    }
}