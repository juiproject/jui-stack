package com.effacy.jui.filter.builder.optimizer;

import java.util.Arrays;
import java.util.List;

import com.effacy.jui.filter.builder.ExpressionBuilder;

/**
 * Composite optimizer that applies multiple optimizers in sequence.
 * <p>
 * This allows for complex optimization strategies by chaining different 
 * optimizers together. Optimizers are applied in the order they are provided,
 * with each optimizer operating on the result of the previous one.
 * <p>
 * The composite optimizer will continue applying optimizers until no further
 * changes are made, ensuring that all possible optimizations are applied.
 */
public class CompositeOptimizer<F> implements IExpressionOptimizer<F> {
    
    private final List<IExpressionOptimizer<F>> optimizers;
    private final int maxIterations;
    
    /**
     * Creates a composite optimizer with the given optimizers and default max iterations (10).
     * 
     * @param optimizers the optimizers to apply in sequence
     */
    @SafeVarargs
    public CompositeOptimizer(IExpressionOptimizer<F>... optimizers) {
        this(10, optimizers);
    }
    
    /**
     * Creates a composite optimizer with the given optimizers and max iterations.
     * 
     * @param maxIterations maximum number of optimization passes to prevent infinite loops
     * @param optimizers the optimizers to apply in sequence
     */
    @SafeVarargs
    public CompositeOptimizer(int maxIterations, IExpressionOptimizer<F>... optimizers) {
        this.optimizers = Arrays.asList(optimizers);
        this.maxIterations = maxIterations;
    }
    
    /**
     * Creates a composite optimizer with the given list of optimizers.
     * 
     * @param optimizers the optimizers to apply in sequence
     */
    public CompositeOptimizer(List<IExpressionOptimizer<F>> optimizers) {
        this(optimizers, 10);
    }
    
    /**
     * Creates a composite optimizer with the given list of optimizers and max iterations.
     * 
     * @param optimizers the optimizers to apply in sequence
     * @param maxIterations maximum number of optimization passes
     */
    public CompositeOptimizer(List<IExpressionOptimizer<F>> optimizers, int maxIterations) {
        this.optimizers = optimizers;
        this.maxIterations = maxIterations;
    }

    @Override
    public ExpressionBuilder.Expression<F> optimize(ExpressionBuilder.Expression<F> expression) {
        if (expression == null || optimizers.isEmpty()) {
            return expression;
        }
        
        ExpressionBuilder.Expression<F> current = expression;
        ExpressionBuilder.Expression<F> previous;
        int iterations = 0;
        
        do {
            previous = current;
            
            // Apply each optimizer in sequence
            for (IExpressionOptimizer<F> optimizer : optimizers) {
                current = optimizer.optimize(current);
                if (current == null) {
                    return null; // Short-circuit if any optimizer returns null
                }
            }
            
            iterations++;
        } while (!expressionsEqual(current, previous) && iterations < maxIterations);
        
        return current;
    }
    
    /**
     * Checks if two expressions are structurally equal.
     * Uses the equals method if available, otherwise falls back to reference equality.
     */
    private boolean expressionsEqual(ExpressionBuilder.Expression<F> expr1, ExpressionBuilder.Expression<F> expr2) {
        if (expr1 == expr2)
            return true;
        if ((expr1 == null) || (expr2 == null))
            return false;
        return expr1.equals(expr2);
    }
    
    /**
     * Creates a standard composite optimizer with commonly used optimizers.
     * 
     * @param builder the expression builder to use for creating new expressions
     * @return a composite optimizer with structural, boolean logic, and comparison optimizers
     */
    public static <F> CompositeOptimizer<F> standard() {
        return new CompositeOptimizer<>(
            new StructuralOptimizer<>(),
            new NotPushingOptimizer<>(),
            new BooleanLogicOptimizer<>(),
            new ComparisonOptimizer<>()
        );
    }
    
    /**
     * Creates an aggressive composite optimizer that applies optimizers multiple times.
     * 
     * @param builder the expression builder to use for creating new expressions
     * @return a composite optimizer with more aggressive optimization settings
     */
    public static <F> CompositeOptimizer<F> aggressive() {
        return new CompositeOptimizer<>(
            20, // More iterations
            new StructuralOptimizer<>(),
            new NotPushingOptimizer<>(),
            new BooleanLogicOptimizer<>(),
            new ComparisonOptimizer<>(),
            new StructuralOptimizer<>(), // Apply structural again after other optimizations
            new NotPushingOptimizer<>(), // Apply NOT pushing again
            new BooleanLogicOptimizer<>()  // And boolean logic again
        );
    }
}