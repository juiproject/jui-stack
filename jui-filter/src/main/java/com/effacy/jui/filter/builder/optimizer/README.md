# Expression Optimizer Package

This package provides optimization capabilities for expressions built using the `ExpressionBuilder`. Optimizers transform expressions into minimal equivalent forms by applying various boolean logic, structural, and comparison-specific optimizations.

## Core Components

### IExpressionOptimizer Interface
Defines the contract for all optimizers with a single `optimize()` method that accepts an expression and returns its optimized form.

### Expression.optimize() Method
Added to the base Expression class, this method accepts an optimizer parameter and applies it to the expression:
```java
Expression<F> optimized = expression.optimize(optimizer);
```

## Optimizer Implementations

### StructuralOptimizer
Performs structural simplifications on expressions:
- **Flattening**: `(A AND (B AND C)) → (A AND B AND C)`
- **Double negation elimination**: `NOT(NOT A) → A`
- **Single-element simplification**: `AND([A]) → A`
- **Empty list handling**: Properly handles empty expression lists

### BooleanLogicOptimizer
Applies boolean algebra rules:
- **Identity elimination**: `A AND true → A`, `A OR false → A`
- **Absorption**: `A OR (A AND B) → A`, `A AND (A OR B) → A`
- **Idempotence**: `A AND A → A`, `A OR A → A`
- **Contradiction detection**: `A AND (NOT A) → false`
- **Tautology detection**: `A OR (NOT A) → true`

### ComparisonOptimizer
Optimizes comparison expressions:
- **Contradiction detection**: `field = 5 AND field = 10 → false`
- **Redundancy elimination**: `field > 5 AND field > 3 → field > 5`
- **Impossible conditions**: `field > 10 AND field < 5 → false`
- **Range optimization**: Simplifies overlapping range conditions
- **IN/NOT_IN optimizations**: Set-based redundancy and contradiction detection
- **Subset elimination**: `field IN [A,B] AND field IN [A,B,C] → field IN [A,B]`

### NotPushingOptimizer
Pushes NOT operations into comparison operators for better readability and optimization:
- **Equality negation**: `NOT(field = value) → field != value`
- **Inequality negation**: `NOT(field != value) → field = value`
- **Comparison negation**: `NOT(field > value) → field <= value`
- **Range negation**: `NOT(field >= value) → field < value`
- **Set negation**: `NOT(field IN [values]) → field NOT IN [values]`
- **Safe handling**: String operations (CONTAINS, STARTS_WITH, ENDS_WITH) remain unchanged

### CompositeOptimizer
Chains multiple optimizers together and applies them iteratively until no further optimizations are possible:
- **Configurable iteration limit** to prevent infinite loops
- **Convenience factory methods** for common optimization strategies
- **Automatic convergence detection** stops when no changes occur

## Usage Examples

### Basic Usage
```java
ExpressionBuilder<MyField> builder = new ExpressionBuilder<>(MyField.class);
Expression<MyField> expr = builder.term(MyField.NAME, Operator.EQ, "John")
    .and(builder.term(MyField.AGE, Operator.GT, 25));

// Apply a single optimizer
IExpressionOptimizer<MyField> optimizer = new StructuralOptimizer<>(builder);
Expression<MyField> optimized = expr.optimize(optimizer);
```

### Composite Optimization
```java
// Standard optimization (applies all optimizers once)
IExpressionOptimizer<MyField> standard = CompositeOptimizer.standard(builder);
Expression<MyField> optimized = expr.optimize(standard);

// Aggressive optimization (multiple passes)
IExpressionOptimizer<MyField> aggressive = CompositeOptimizer.aggressive(builder);
Expression<MyField> fullyOptimized = expr.optimize(aggressive);
```

### Custom Composite
```java
IExpressionOptimizer<MyField> custom = new CompositeOptimizer<>(
    new StructuralOptimizer<>(builder),
    new NotPushingOptimizer<>(builder),
    new BooleanLogicOptimizer<>(builder)
);
Expression<MyField> optimized = expr.optimize(custom);
```

## Optimization Examples

### Before and After
```java
// Structural optimization
// Original: (A AND (B AND C)) AND A
// Optimized: A AND B AND C

// Boolean logic optimization  
// Original: A OR (A AND B)
// Optimized: A

// Comparison optimization
// Original: field > 10 AND field > 5 AND field < 3
// Optimized: false (contradiction)

// Double negation elimination
// Original: NOT(NOT(field = "value"))
// Optimized: field = "value"

// NOT pushing optimization
// Original: NOT(field = 42)
// Optimized: field != 42

// IN/NOT_IN optimization
// Original: field IN [A,B] AND field IN [A,B,C]  
// Optimized: field IN [A,B]

// Combined optimization
// Original: NOT(field = 42) AND NOT(field = 42)
// Optimized: field != 42 (NOT pushing + idempotence)
```

## Implementation Notes

- All optimizers leverage the existing `traverse()` method for tree walking
- Optimizations maintain expression semantics - optimized expressions are logically equivalent to originals
- The `complexity()` method can be used to measure optimization effectiveness
- Optimizers handle null expressions gracefully
- Type safety is maintained throughout the optimization process

## Best Practices

1. Use `CompositeOptimizer.standard()` for most optimization needs
2. Apply `CompositeOptimizer.aggressive()` for complex expressions where maximum optimization is desired
3. Measure complexity before and after optimization to verify improvements
4. Consider the cost/benefit of optimization for simple expressions