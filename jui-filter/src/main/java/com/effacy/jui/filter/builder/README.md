# Filter builder


## Overview

We provide a mechanism for building general filter queries that consist of comparisons (terms) composed by a combination of NOT, AND and OR operators. This is also compatible with the [filter query parser](../parser/README.md) which can be used to either draft a filter in a human-friendly(-ish) manner or as a way of serialising a filter query.

The following illustrates the concepts in action:

```java
// Define your fields enum
enum Fields {
    NAME, AGE, STATUS;
}

// Create a simple expression
Expression<Fields> filter = FieldsQueryBuilder.and(
    FieldsQueryBuilder.field1(Operator.GT, 18),     // age > 18
    FieldsQueryBuilder.field2(Operator.EQ, "John")  // name = "John"
);

// Parse from string
String query = "age > 18 AND name = 'John'";
Expression<Fields> parsedFilter = FilterQueryParser.parse(query)
    .build(FieldsQueryBuilder.stringBuilder());
```

## Builders and buildables

There are two classes of importance: `IExpressionBuilder` and `IExpressionBuildable`. The former allows one to build an expression (of some type) through the application of `and(...)`, `or(...)` and `not(...)` applied to expressions and to produce fundamental comparison expressions with `term(...)`. The latter allows a class to declare its ability to work with a `IExpressionBuilder` to build expressions.

This separation allows one to do such things as parse an expression in a standard manner (i.e. `FilterQueryParser`) that creates an `IExpressionBuildable` which can then be applied to generate an expression through a `IExpressionBuilder` that can be applied in a specific circumstance (i.e. to build a data query).

### The builder

The interface `IExpressionBuilder<T,FIELD>` defines a contract for a concrete expression builder that builds expressions of type `T` using field specifications of type `FIELD` (this is often a string but could be an enum to enforce fields that exist and are able to be processed). Fundamental expressions (comparisons) can be created via the `term(...)` method:

```java
public T term(FIELD field, Operator operator, Object value);
```

Which limits the possible fields to instances of `FIELD` with comparison given by `Operator` against an arbirary value.

An example of a builder is `StringExpressionBuilder` where the expression is a string (the type `T`) and the field type is also a string (the type `FIELD`). This produces a human-readable version of the expression that can be parsed by `FilterQueryParser`. In particular:

```java
String input = ...;
// Normalise the input to str.
String str = FilterQueryParser.parse(input).build(new StringExpressionBuilder());
// Parse and re-generate str to out.
String out = FilterQueryParser.parse(str).build(new StringExpressionBuilder());
// The normalised form str and out are the same.
assertEquals(str, out);
```

In practice the builder interface does not constrain the product of fundamental expression sufficiently. The convention is then to create a separate builder that delegates to an instance of `ExpressionBuilder`:

```java
public static class FieldsQueryBuilder {
        
    // Enum to prescribe the fields that are supported.
    enum Fields {
        FIELD1, FIELD2, FIELD3;
    }

    private static ExpressionBuilder<Fields> INSTANCE = new ExpressionBuilder<FieldsQueryBuilder.Fields>();

    // AND, OR and NOT production methods.

    @SafeVarargs
    public static Expression<Fields> and(Expression<Fields>... expressions) {
        return INSTANCE.and(List.of(expressions));
    }

    public static Expression<Fields> and(List<Expression<Fields>> expressions) {
        return INSTANCE.and(expressions);
    }

    @SafeVarargs
    public static Expression<Fields> or(Expression<Fields>... expressions) {
        return INSTANCE.or(List.of(expressions));
    }

    public static Expression<Fields> or(List<Expression<Fields>> expressions) {
        return INSTANCE.or(expressions);
    }

    public static Expression<Fields> not(Expression<Fields> expression) {
        return INSTANCE.not(expression);
    }

    // Fundamental (comparison) expressions.

    public static Expression<Fields> field1(Operator op, int value) {
        return INSTANCE.term(Fields.FIELD1, op, value);
    }

    public static Expression<Fields> field2(Operator op, String value) {
        return INSTANCE.term(Fields.FIELD2, op, value);
    }
    
    public static Expression<Fields> field3(Operator op, Status value) {
        return INSTANCE.term(Fields.FIELD3, op, value);
    }
    
    public static Expression<Fields> field3(Operator op, Status[] value) {
        return INSTANCE.term(Fields.FIELD3, op, value);
    }
}
```

which can be used to produce expressions:

```java
Expression<Fields> exp = FieldsQueryBuilder.and (
    FieldsQueryBuilder.or (
        FieldsQueryBuilder.field1(Operator.GT, 22),
        FieldsQueryBuilder.field2(Operator.EQ, "some value")
    ),
    // For some enum Status
    FieldsQueryBuilder.field3(Operator.IN, new Status[] {Status.ACTIVE, Status.INACTIVE})
);
```

One could be even more constrained on the fundamental expressions as needed.

In order to allow the builder to be used by `FilterQueryParser` we do need to expose an `IExpressionBuilder` but one that accepts fields of string-type. `SimpleExpressionBuilder` provides a convenience method `mapped(...)` that is passed a mapper function that can translate a field represented as a string to one that respects the `FIELD` type.

```java
public static IExpressionBuilder<Expression<Fields>,String> stringBuilder() {
    return INSTANCE.mapped(v -> Fields.valueOf(v));
}
```

So one can obtain an expression from a string:

```java
String query = "field1 > 10 AND field2 = 'test'";
Expression<Fields> expression = FilterQueryParser.parse(query).build(FieldsQueryBuilder.stringBuilder());
```

## Error Handling

When working with the filter builder, be aware of these common error scenarios:

### Invalid field names
```java
try {
    String query = "invalidField = 'test'";
    Expression<Fields> expression = FilterQueryParser.parse(query)
        .build(FieldsQueryBuilder.stringBuilder());
} catch (IllegalArgumentException e) {
    // Handle invalid field name - enum valueOf() will throw this
    System.err.println("Invalid field name: " + e.getMessage());
}
```

### Parse errors
```java
try {
    String malformedQuery = "field1 > > 10"; // Invalid syntax
    FilterQueryParser.parse(malformedQuery);
} catch (ParseException e) {
    // Handle parsing errors
    System.err.println("Query parsing failed: " + e.getMessage());
}
```

### Type Mismatches
```java
// Ensure value types match field expectations
// Wrong: field1 expects int, but string provided
// FieldsQueryBuilder.field1(Operator.EQ, "not a number"); // Runtime error

// Correct: match expected types
FieldsQueryBuilder.field1(Operator.EQ, 42); // int value for int field
```

## Architecture Diagram

```
┌─────────────────────┐    ┌──────────────────────┐
│   FilterQueryParser │────│  IExpressionBuildable│
│                     │    │                      │
│ Parses string       │    │ Can be applied to    │
│ queries into        │    │ any builder          │
│ buildable objects   │    │                      │
└─────────────────────┘    └──────────────────────┘
           │                           │
           │                           ▼
           │               ┌──────────────────────┐
           │               │  IExpressionBuilder  │
           │               │         <T,FIELD>    │
           │               │                      │
           │               │ • term()             │
           │               │ • and()              │
           │               │ • or()               │
           │               │ • not()              │
           │               └──────────────────────┘
           │                           │
           ▼                           ▼
┌─────────────────────┐    ┌──────────────────────┐
│ StringExpression    │    │ Custom Builder       │
│ Builder             │    │ (e.g. SQL, MongoDB) │
│                     │    │                      │
│ Produces human-     │    │ Produces specific    │
│ readable strings    │    │ query format         │
└─────────────────────┘    └──────────────────────┘
```

The architecture separates concerns:
- **Parser**: Converts string queries to buildable expressions
- **Buildable**: Abstract representation that can work with any builder
- **Builder**: Converts expressions to specific formats (strings, SQL, etc.)

## Troubleshooting

### Problem: "No enum constant" error when parsing
**Cause**: Field name in query string doesn't match enum values
**Solution**: 
```java
// Ensure enum values match query field names exactly
enum Fields {
    FIELD1, FIELD2, FIELD3; // Use same case as in queries
}
```

### Problem: Expression not building correctly
**Cause**: Missing parentheses or operator precedence issues
**Solution**:
```java
// Use explicit grouping
Expression<Fields> expr = FieldsQueryBuilder.and(
    FieldsQueryBuilder.or(
        FieldsQueryBuilder.field1(Operator.GT, 10),
        FieldsQueryBuilder.field1(Operator.LT, 5)
    ),
    FieldsQueryBuilder.field2(Operator.EQ, "test")
);
```

### Problem: Null values in expressions
**Cause**: Passing null values to term() methods
**Solution**:
```java
// Check for null before building
Object value = getValue();
if (value != null) {
    return FieldsQueryBuilder.field1(Operator.EQ, value);
} else {
    return FieldsQueryBuilder.field1(Operator.IS_NULL, null);
}
```

### Problem: Complex queries failing to parse
**Cause**: Overly complex nested expressions or unsupported operators
**Solution**: Break down complex queries into simpler components and test incrementally

### Common Gotchas
- Field names are case-sensitive
- String values in queries must be quoted
- Array values use IN/NOT_IN operators
- Date/time values need proper formatting
