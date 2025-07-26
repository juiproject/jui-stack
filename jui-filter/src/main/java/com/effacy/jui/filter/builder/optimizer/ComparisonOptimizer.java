package com.effacy.jui.filter.builder.optimizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.effacy.jui.filter.builder.ExpressionBuilder;
import com.effacy.jui.filter.builder.IExpressionBuilder.Operator;

/**
 * Optimizer that applies comparison-specific optimizations:
 * <ul>
 * <li>Contradiction detection: field = 5 AND field = 10 → false</li>
 * <li>Redundancy elimination: field > 5 AND field > 3 → field > 5</li>
 * <li>Range simplification: field >= 5 AND field <= 10 → optimized form</li>
 * <li>Impossible conditions: field > 10 AND field < 5 → false</li>
 * </ul>
 */
public class ComparisonOptimizer<F> implements IExpressionOptimizer<F> {
    
    private final ExpressionBuilder<F> builder;
    
    public ComparisonOptimizer(ExpressionBuilder<F> builder) {
        this.builder = builder;
    }

    @Override
    public ExpressionBuilder.Expression<F> optimize(ExpressionBuilder.Expression<F> expression) {
        if (expression == null) {
            return null;
        }
        
        if (expression instanceof ExpressionBuilder<F>.ANDExpression) {
            return optimizeAndComparisons((ExpressionBuilder<F>.ANDExpression) expression);
        } else if (expression instanceof ExpressionBuilder<F>.ORExpression) {
            return optimizeOrComparisons((ExpressionBuilder<F>.ORExpression) expression);
        } else if (expression instanceof ExpressionBuilder<F>.NOTExpression) {
            return optimizeNotComparison((ExpressionBuilder<F>.NOTExpression) expression);
        } else {
            return expression;
        }
    }
    
    private ExpressionBuilder.Expression<F> optimizeAndComparisons(ExpressionBuilder<F>.ANDExpression andExpr) {
        List<ExpressionBuilder.Expression<F>> terms = collectDirectChildren(andExpr);
        Map<F, List<ComparisonInfo<F>>> comparisonsByField = groupComparisonsByField(terms);
        List<ExpressionBuilder.Expression<F>> optimizedTerms = new ArrayList<>();
        
        // Process comparisons for each field
        for (Map.Entry<F, List<ComparisonInfo<F>>> entry : comparisonsByField.entrySet()) {
            F field = entry.getKey();
            List<ComparisonInfo<F>> comparisons = entry.getValue();
            
            if (comparisons.size() == 1) {
                // Single comparison for this field - keep as is
                optimizedTerms.add(comparisons.get(0).expression);
            } else {
                // Multiple comparisons for same field - optimize
                ExpressionBuilder.Expression<F> optimized = optimizeFieldComparisons(field, comparisons);
                if (optimized == null) {
                    return null; // Contradiction found - entire AND is false
                }
                optimizedTerms.add(optimized);
            }
        }
        
        // Add non-comparison terms
        for (ExpressionBuilder.Expression<F> term : terms) {
            if (!(term instanceof ExpressionBuilder<F>.ComparisonExpression)) {
                ExpressionBuilder.Expression<F> optimized = optimize(term);
                if (optimized != null) {
                    optimizedTerms.add(optimized);
                }
            }
        }
        
        if (optimizedTerms.isEmpty()) {
            return null;
        } else if (optimizedTerms.size() == 1) {
            return optimizedTerms.get(0);
        } else {
            return builder.and(optimizedTerms);
        }
    }
    
    private ExpressionBuilder.Expression<F> optimizeOrComparisons(ExpressionBuilder<F>.ORExpression orExpr) {
        // For OR expressions, we mainly look for tautologies
        List<ExpressionBuilder.Expression<F>> terms = collectDirectChildren(orExpr);
        List<ExpressionBuilder.Expression<F>> optimizedTerms = new ArrayList<>();
        
        for (ExpressionBuilder.Expression<F> term : terms) {
            ExpressionBuilder.Expression<F> optimized = optimize(term);
            if (optimized != null) {
                optimizedTerms.add(optimized);
            }
        }
        
        if (optimizedTerms.isEmpty()) {
            return null;
        } else if (optimizedTerms.size() == 1) {
            return optimizedTerms.get(0);
        } else {
            return builder.or(optimizedTerms);
        }
    }
    
    private ExpressionBuilder.Expression<F> optimizeNotComparison(ExpressionBuilder<F>.NOTExpression notExpr) {
        ExpressionBuilder.Expression<F> inner = getInnerExpression(notExpr);
        ExpressionBuilder.Expression<F> optimizedInner = optimize(inner);
        
        if (optimizedInner == inner) {
            return notExpr; // No change
        }
        return builder.not(optimizedInner);
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
    
    private ExpressionBuilder.Expression<F> getInnerExpression(ExpressionBuilder.Expression<F> notExpr) {
        final ExpressionBuilder.Expression<F>[] inner = new ExpressionBuilder.Expression[1];
        notExpr.traverse((depth, expr) -> {
            if (depth == 1) { // Direct child of NOT
                inner[0] = expr;
            }
        });
        return inner[0];
    }
    
    private Map<F, List<ComparisonInfo<F>>> groupComparisonsByField(List<ExpressionBuilder.Expression<F>> terms) {
        Map<F, List<ComparisonInfo<F>>> result = new HashMap<>();
        
        for (ExpressionBuilder.Expression<F> term : terms) {
            if (term instanceof ExpressionBuilder<F>.ComparisonExpression) {
                ExpressionBuilder<F>.ComparisonExpression compExpr = (ExpressionBuilder<F>.ComparisonExpression) term;
                F field = compExpr.field();
                Operator operator = compExpr.operator();
                Object value = compExpr.value();
                
                ComparisonInfo<F> info = new ComparisonInfo<>(compExpr, field, operator, value);
                result.computeIfAbsent(field, k -> new ArrayList<>()).add(info);
            }
        }
        
        return result;
    }
    
    private ExpressionBuilder.Expression<F> optimizeFieldComparisons(F field, List<ComparisonInfo<F>> comparisons) {
        // Check for direct contradictions first (same field, same operator, different values)
        for (int i = 0; i < comparisons.size(); i++) {
            ComparisonInfo<F> comp1 = comparisons.get(i);
            for (int j = i + 1; j < comparisons.size(); j++) {
                ComparisonInfo<F> comp2 = comparisons.get(j);
                
                // Check for equality contradictions: field = A AND field = B (where A != B)
                if (comp1.operator == Operator.EQ && comp2.operator == Operator.EQ) {
                    if (!Objects.equals(comp1.value, comp2.value)) {
                        return null; // Contradiction
                    }
                }
                
                // Check for impossible range conditions
                if (isImpossibleCondition(comp1, comp2)) {
                    return null; // Contradiction
                }
            }
        }
        
        // If no contradictions, look for redundancies
        List<ComparisonInfo<F>> nonRedundant = removeRedundantComparisons(comparisons);
        
        if (nonRedundant.size() == 1) {
            return nonRedundant.get(0).expression;
        } else {
            // Create AND of remaining comparisons
            List<ExpressionBuilder.Expression<F>> expressions = new ArrayList<>();
            for (ComparisonInfo<F> comp : nonRedundant) {
                expressions.add(comp.expression);
            }
            return builder.and(expressions);
        }
    }
    
    private boolean isImpossibleCondition(ComparisonInfo<F> comp1, ComparisonInfo<F> comp2) {
        // Check for impossible numeric conditions
        if (comp1.value instanceof Number && comp2.value instanceof Number) {
            double val1 = ((Number) comp1.value).doubleValue();
            double val2 = ((Number) comp2.value).doubleValue();
            
            // field > 10 AND field < 5
            if (comp1.operator == Operator.GT && comp2.operator == Operator.LT && val1 >= val2) {
                return true;
            }
            if (comp1.operator == Operator.LT && comp2.operator == Operator.GT && val2 >= val1) {
                return true;
            }
            
            // field >= 10 AND field < 10
            if (comp1.operator == Operator.GTE && comp2.operator == Operator.LT && val1 >= val2) {
                return true;
            }
            if (comp1.operator == Operator.LT && comp2.operator == Operator.GTE && val2 >= val1) {
                return true;
            }
            
            // field > 10 AND field <= 10
            if (comp1.operator == Operator.GT && comp2.operator == Operator.LTE && val1 >= val2) {
                return true;
            }
            if (comp1.operator == Operator.LTE && comp2.operator == Operator.GT && val2 >= val1) {
                return true;
            }
        }
        
        // Check for impossible IN/NOT_IN conditions
        if ((comp1.operator == Operator.IN || comp1.operator == Operator.NOT_IN) &&
            (comp2.operator == Operator.IN || comp2.operator == Operator.NOT_IN)) {
            
            Set<Object> comp1Set = valueToSet(comp1.value);
            Set<Object> comp2Set = valueToSet(comp2.value);
            
            // field IN [1,2,3] AND field IN [4,5,6] → false (disjoint sets)
            if (comp1.operator == Operator.IN && comp2.operator == Operator.IN) {
                return areDisjoint(comp1Set, comp2Set);
            }
            
            // field NOT_IN [1,2,3] AND field IN [1,2] → false (subset contradiction)
            if (comp1.operator == Operator.NOT_IN && comp2.operator == Operator.IN) {
                return isSubset(comp2Set, comp1Set);
            }
            
            // field IN [1,2] AND field NOT_IN [1,2,3] → false (subset contradiction)
            if (comp1.operator == Operator.IN && comp2.operator == Operator.NOT_IN) {
                return isSubset(comp1Set, comp2Set);
            }
        }
        
        return false;
    }
    
    private List<ComparisonInfo<F>> removeRedundantComparisons(List<ComparisonInfo<F>> comparisons) {
        List<ComparisonInfo<F>> result = new ArrayList<>();
        
        for (ComparisonInfo<F> comp : comparisons) {
            boolean isRedundant = false;
            
            // Check if this comparison is made redundant by any other
            for (ComparisonInfo<F> other : comparisons) {
                if (comp != other && isRedundant(comp, other)) {
                    isRedundant = true;
                    break;
                }
            }
            
            if (!isRedundant) {
                result.add(comp);
            }
        }
        
        return result;
    }
    
    private boolean isRedundant(ComparisonInfo<F> comp, ComparisonInfo<F> other) {
        // Check for numeric redundancies
        if (comp.value instanceof Number && other.value instanceof Number) {
            double val1 = ((Number) comp.value).doubleValue();
            double val2 = ((Number) other.value).doubleValue();
            
            // field > 3 is redundant if we have field > 5
            if (comp.operator == Operator.GT && other.operator == Operator.GT && val1 <= val2) {
                return true;
            }
            
            // field < 10 is redundant if we have field < 5
            if (comp.operator == Operator.LT && other.operator == Operator.LT && val1 >= val2) {
                return true;
            }
            
            // Similar logic for GTE, LTE
            if (comp.operator == Operator.GTE && other.operator == Operator.GTE && val1 <= val2) {
                return true;
            }
            
            if (comp.operator == Operator.LTE && other.operator == Operator.LTE && val1 >= val2) {
                return true;
            }
            
            // Mixed operator redundancy checks
            // field >= 10 is redundant if we have field > 10 (for same value)
            if (comp.operator == Operator.GTE && other.operator == Operator.GT && val1 == val2) {
                return true;
            }
            
            // field <= 10 is redundant if we have field < 10 (for same value) 
            if (comp.operator == Operator.LTE && other.operator == Operator.LT && val1 == val2) {
                return true;
            }
            
            // field >= 10 is redundant if we have field > 9 (stricter condition covers it)
            if (comp.operator == Operator.GTE && other.operator == Operator.GT && val1 <= val2) {
                return true;
            }
            
            // field <= 10 is redundant if we have field < 11 (stricter condition covers it)
            if (comp.operator == Operator.LTE && other.operator == Operator.LT && val1 >= val2) {
                return true;
            }
        }
        
        // Handle IN and NOT_IN operators
        if ((comp.operator == Operator.IN || comp.operator == Operator.NOT_IN) &&
            (other.operator == Operator.IN || other.operator == Operator.NOT_IN)) {
            
            Set<Object> compSet = valueToSet(comp.value);
            Set<Object> otherSet = valueToSet(other.value);
            
            // field IN [1,2,3] is redundant if we have field IN [1,2] (larger set is redundant)
            if (comp.operator == Operator.IN && other.operator == Operator.IN) {
                return isSubset(otherSet, compSet);
            }
            
            // field NOT_IN [1,2] is redundant if we have field NOT_IN [1,2,3] (smaller set is redundant)  
            if (comp.operator == Operator.NOT_IN && other.operator == Operator.NOT_IN) {
                return isSubset(compSet, otherSet);
            }
        }
        
        return false;
    }
    
    /**
     * Converts a value to a Set for set operations.
     * If the value is an array, converts to Set; otherwise creates a singleton Set.
     */
    private Set<Object> valueToSet(Object value) {
        if (value == null) {
            return new HashSet<>();
        }
        if (value.getClass().isArray()) {
            return new HashSet<>(Arrays.asList((Object[]) value));
        }
        Set<Object> set = new HashSet<>();
        set.add(value);
        return set;
    }
    
    /**
     * Checks if two sets are disjoint (have no common elements).
     */
    private boolean areDisjoint(Set<Object> set1, Set<Object> set2) {
        Set<Object> smaller = set1.size() <= set2.size() ? set1 : set2;
        Set<Object> larger = set1.size() > set2.size() ? set1 : set2;
        
        for (Object item : smaller) {
            if (larger.contains(item)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Checks if set1 is a subset of set2.
     */
    private boolean isSubset(Set<Object> set1, Set<Object> set2) {
        return set2.containsAll(set1);
    }
    
    /**
     * Returns the intersection of two sets.
     */
    private Set<Object> intersection(Set<Object> set1, Set<Object> set2) {
        Set<Object> result = new HashSet<>(set1);
        result.retainAll(set2);
        return result;
    }
    
    /**
     * Converts a Set back to an appropriate value for creating expressions.
     */
    private Object setToValue(Set<Object> set) {
        if (set.isEmpty()) {
            return new Object[0];
        }
        if (set.size() == 1) {
            return set.iterator().next();
        }
        return set.toArray();
    }
    
    private static class ComparisonInfo<F> {
        final ExpressionBuilder.Expression<F> expression;
        final F field;
        final Operator operator;
        final Object value;
        
        ComparisonInfo(ExpressionBuilder.Expression<F> expression, F field, Operator operator, Object value) {
            this.expression = expression;
            this.field = field;
            this.operator = operator;
            this.value = value;
        }
    }
}