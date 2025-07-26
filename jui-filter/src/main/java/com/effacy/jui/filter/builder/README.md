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

## Standard examples

### Querydsl expressions

Here we can create a [Querydsl](https://querydsl.com/) `BooleanExpression` from a structured query expression over a specific field of query terms.

To begin with, one must create a suitable field and builder over that field:

```java
public class PersonQueryFilter {

    public enum Fields implements Field {

        KEYWORDS(new StringType()),
        STATUS(new EnumType<PersonStatus>(PersonStatus.class)),
        DEPARTMENT(new IntegralType());

        private Type type;
        private Fields(Type type) { this.type = type; }
        public Type type() { return type; }
    }

    private static ExpressionBuilder<Fields> INSTANCE = new ExpressionBuilder<>(Fields.class);

    public static String serialise(Expression<Fields> exp) throws ExpressionBuildException {
        return INSTANCE.serialise(exp);
    }

    public static Expression<Fields> deserialise(String str) throws FilterQueryParserException {
        return INSTANCE.deserialise(str);
    }

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

    public static Expression<Fields> keywords(String value) {
        return INSTANCE.term(Fields.KEYWORDS, Operator.CONTAINS, value);
    }
    
    public static Expression<Fields> statusIn(PersonStatus... value) {
        return INSTANCE.term(Fields.STATUS, Operator.IN, value);
    }
    
    public static Expression<Fields> statusNotIn(PersonStatus... value) {
        return INSTANCE.term(Fields.STATUS, Operator.NOT_IN, value);
    }
    
    public static Expression<Fields> department(long value) {
        return INSTANCE.term(Fields.DEPARTMENT, Operator.EQ, value);
    }
}
```

Here we have `Fields` enumerating the various filter terms. The builder itself is a collection of static methods that interacts with a global `ExpressionBuilder<Fields>` instance. The reason for this is that term creation via `ExpressionBuilder` is very general, and most often we want to guide what can be sensibly created in terms of constraints for specific fields.

An example expresion is:

```java
var exp = PersonQueryFilter.and(
    PersonQueryFilter.keywords("jane"),
    PersonQueryFilter.or(
        PersonQueryFilter.department(7L),
        PersonQueryFilter.status(PersonStatus.INACTIVE)
    )
);
```

Equivalently we could have used a serialised form:

```java
var exp = PersonQueryFilter.deserialise("""
    KEYWORDS contains \"jane\" AND (
        (DEPARTMENT eq 7) OR (STATUS IN [ INACTIVE ])
    )
""")
```

The serialised form could come from the client. There it could have been created as above and serialised:

```java
var exp = PersonQueryFilter.and(
    ...
);
String filter = PersonQueryFilter.serialise(exp);
// send filter
```

The expression can then make its way to DSL and used to create a `BooleanExpression`:

```java
QPersonEntity personE = QPersonEntity.personEntity;
...
try {
    BooleanExpression exp = query.getFilter().build(new QueryDslExpressionBuilder<>((ctx,field,op,val) -> {
        return switch(field) {
            case KEYWORDS ->
                // Apply search keywords to name and email
                ctx.fromString(personE.name, op, val, v -> StringUtils.trimToEmpty(v))
                .or(ctx.fromString(personE.email, op, val, v -> StringUtils.trimToEmpty(v)));
            case STATUS ->
                ctx.fromEnum(PersonStatus.class, personE.status, op, val);
            case DEPARTMENT ->
                ctx.fromLong(assetE.department.id, op, val);
            default -> Expressions.TRUE;
        };
    }));
} catch (ExpressionBuildException e) {
    // Handle error
}
```

Where `QueryDslExpressionBuilder` is declared as (or similar to):

```java
public class QueryDslExpressionBuilder<FIELD> implements IExpressionBuilder<BooleanExpression,FIELD> {

    /**
     * Provides conversion tools for values and applies them to paths.
     */
    public static class Context {

        public BooleanExpression fromLong(NumberPath<Long> path, Operator op, Object val) throws ExpressionBuildException {
            Long value = ValueSupport.asLong(val);
            return switch (op) {
                case EQ -> path.eq(value);
                case NEQ -> path.ne(value);
                case GT -> path.gt(value);
                case GTE -> path.goe(value);
                case LT -> path.lt(value);
                case LTE -> path.loe(value);
                case IN -> path.eq(value);
                case NOT_IN -> path.ne(value);
                default -> Expressions.FALSE;
            };
        }

        public BooleanExpression fromString(StringPath path, Operator op, Object val) throws ExpressionBuildException {
            return fromString(path, op, val, null);
        }

        public BooleanExpression fromString(StringPath path, Operator op, Object val, Function<String,String> tx) throws ExpressionBuildException {
            String value = ValueSupport.asString(val);
            if (tx != null)
                value = tx.apply(value);
            return switch (op) {
                case EQ -> path.eq(value);
                case NEQ -> path.ne(value);
                case GT -> path.gt(value);
                case GTE -> path.goe(value);
                case LT -> path.lt(value);
                case LTE -> path.loe(value);
                case IN -> path.eq(value);
                case NOT_IN -> path.ne(value);
                case CONTAINS -> path.contains(value);
                case STARTS_WITH -> path.startsWith(value);
                case ENDS_WITH -> path.endsWith(value);
                default -> Expressions.FALSE;
            };
        }

        public <T extends Enum<T>> BooleanExpression fromEnum(Class<T> klass, EnumPath<T> path, Operator op, Object val) throws ExpressionBuildException {
            if (val == null)
                return Expressions.FALSE;
            T[] avalue = ValueSupport.asEnumArray(klass, val);
            T value = null;
            if (avalue != null) {
                if (avalue.length == 0)
                    return Expressions.FALSE;
                if (avalue.length > 1) {
                    return switch (op) {
                        case IN -> path.in(avalue);
                        case NOT_IN -> path.notIn(avalue);
                        default -> Expressions.FALSE;
                    };
                }
                value = avalue[0];
            }
            if (value == null)
                value = ValueSupport.asEnum(klass, val);
            return switch (op) {
                case EQ -> path.eq(value);
                case NEQ -> path.ne(value);
                case GT -> path.gt(value);
                case GTE -> path.goe(value);
                case LT -> path.lt(value);
                case LTE -> path.loe(value);
                case IN -> path.eq(value);
                case NOT_IN -> path.ne(value);
                default -> Expressions.FALSE;
            };
        }

    }

    public interface Composer<FIELD> {
        public BooleanExpression term(Context ctx, FIELD field, Operator operator, Object value);
    }

    private Composer<FIELD> composer;

    private Context context;

    public QueryDslExpressionBuilder(Composer<FIELD> composer) {
        this.composer = composer;
        this.context = new Context();
    }

    @Override
    public BooleanExpression and(List<BooleanExpression> expressions) {
        if ((expressions == null) || expressions.isEmpty())
            return Expressions.FALSE;
        BooleanExpression base = null;
        for (BooleanExpression e : expressions) {
            if (e == null)
                continue;
            if (base == null)
                base = e;
            else
                base = base.and(e);
        }
        return base;
    }

    @Override
    public BooleanExpression or(List<BooleanExpression> expressions) {
        if ((expressions == null) || expressions.isEmpty())
            return Expressions.FALSE;
        BooleanExpression base = null;
        for (BooleanExpression e : expressions) {
            if (e == null)
                continue;
            if (base == null)
                base = e;
            else
                base = base.or(e);
        }
        return base;
    }

    @Override
    public BooleanExpression not(BooleanExpression expression) {
        if (expression == null)
            return Expressions.TRUE;
        return expression.not();
    }

    @Override
    public BooleanExpression term(FIELD field, Operator operator, Object value) throws ExpressionBuildException {
        return composer.term(context, field, operator, value);
    }
}
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
