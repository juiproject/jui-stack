# Overview

This sub-project defines a mechanism for building and working with filters in Java (both client-side with JUI and server-side).

A filter is an expression that can be used to restrict a collection of records based on their fields or properties. These expressions cover boolean logic (`AND`, `OR`, `NOT`, `TRUE`, `FALSE`) and comparisons (`=`, `!=`, `<`, `>`, `CONTAINS`, `IN`, `BETWEEN`, etc.).  

The key idea is to provide a **normalized form** for these filters. That structure can be serialized to and from text, sent over the wire, stored as “saved filters,” and converted into backend-specific queries such as `dslquery`.  

Some of the motivators for doing this include:

- **Consistency** in having a single representation across client and server.
- **Portability** where filters can be serialized and reconstructed anywhere.
- **Safety** in validation that ensures operators are applied to compatible types.
- **Extensibility** where new expression families can be supported with additional converters.

## In practice

The normalised expresion form is given by the `Expression` class paramterised over a `Field` (generally an enumeration of the available properties than can be subject to comparison) built with the aid of and `ExpressionBuilder`.

A simple example of this is (a very stripped down version, normally one would have helper methods that delegate to the builder which simplfy use):

```java
public class AssetQueryFilter {

    public enum Fields implements Field {

        KEYWORDS(new StringType(Operator.EQ, Operator.CONTAINS, Operator.ENDS_WITH, Operator.STARTS_WITH)),
        STATUS(new EnumType<Statue>(Statue.class, Operator.EQ, Operator.NEQ, Operator.IN, Operator.NOT_IN));

        private Type type;
        private Fields(Type type) { this.type = type; }
        public Type type() { return type; }
    }

    private static ExpressionBuilder<Fields> BUILDER = new ExpressionBuilder<>(Fields.class);
}
```
Which can be used to generate an normalised expression:

```java
Expression<Fields> exp = AssetQueryFilter.BUILDER.and (
    AssetQueryFilter.BUILDER.term(Fields.KEYWORDS, Operator.CONTAINS, "apples"),
    AssetQueryFilter.BUILDER.term(Fields.STATUS, Operator.IN, new Status [] { Status.ACTIVE, Status.ARCHIVE })
);
```

Now this expression implements `IExpressionBuildable` which allows it to be passed to any `IExpressionBuilder` (`ExpressionBuilder` is one such builder) to generate another expression. For example `StringExpressionBuilder` can build out a serialised version an expression:

This can then be converted to a string (`keywords CONTAINS "apples" and status IN [ ACTIVE, ARCHIVE ]`) by:

```java
String serialised = exp.build(StringExpressionBuilder.<Fields> remap(f -> {
    // Here we map the field value to a string version.
    return f.name();
}));
```

Which will generate the string `keywords CONTAINS "apples" and status IN [ ACTIVE, ARCHIVE ]` (in practice there is a helper method `serialise(...)` on `ExpressionBuilder` to do just this).

We can also go from such a string represention by using the `FilterParser`:

```java
ParsedExpression pexp = FilterQueryParser.parse(str);
```

Which is an `IExpressionBuildable` over the general field `String`. This can be converted to normalised form by:

```java
Expression<Fields> exp = pexp.build(mapped(str -> {
    try {
        // Here we map the string version of the field value back to
        // the field value.
        return Fields.valueOf(str);
    } catch (Exception e) {
        throw new ExpressionBuildException("unable to map field " + str);
    }
}));
```

## Grammar

The grammar (used by the parser) adheres to the following (simplifed) EBNF:

```txt
expr        := orExpr
orExpr      := andExpr (OR andExpr)*
andExpr     := unaryExpr (AND unaryExpr)*
unaryExpr   := NOT unaryExpr | primary
primary     := comparison | TRUE | FALSE | '(' expr ')'

comparison  := operand compOp operand
             | field IN '[' valueList ']'
             | field BETWEEN value AND value
             | field IS NULL
             | field IS NOT NULL
             | field textOp string

operand     := field | value
field       := IDENT ('.' IDENT)*
value       := NUMBER | BOOLEAN | DATE | ENUM | STRING

compOp      := '=' | '!=' | '<' | '<=' | '>' | '>='
textOp      := CONTAINS | STARTS_WITH | ENDS_WITH
valueList   := value (',' value)*

```

## Examples

### Querydsl

The following is a comprehensive builder for generating [Querydsl](http://querydsl.com/) expressions from an `IExpressionBuildable`. The builder needs to be constructed with a mechanism that generates primitive expressions from dsl paths.

As an example consider the following (fictitious) filter (partially implemented):

```java
public class ReviewReviewerDataQPQueryFilter {
    
    public enum Fields implements Field {

        KEYWORDS(new StringType(Operator.EQ, Operator.CONTAINS, Operator.ENDS_WITH, Operator.STARTS_WITH)),
        DEPARTMENT(new IntegralType(Operator.EQ, Operator.NEQ, Operator.IN, Operator.NOT_IN)),
        LOCATION(new IntegralType(Operator.EQ, Operator.NEQ, Operator.IN, Operator.NOT_IN)),
        ;

        private Type type;
        private Fields(Type type) { this.type = type; }
        public Type type() { return type; }
    }

    private static ExpressionBuilder<Fields> INSTANCE = new ExpressionBuilder<>(Fields.class);

    public static Expression<Fields> deserialise(String str) throws FilterQueryParserException {
        return INSTANCE.deserialise(str);
    }
}

```

From this we can generate filter expressions like:

```java
Expression<ReviewReviewerDataQPQueryFilter.Fields> filter
    = ReviewReviewerDataQPQueryFilter.deserialise(
        "keywords contains 'managers' and (department in [2,3] or location in [2,4])"
    );
```

We can then construct a dsl builder to convert this to a suitable dsl expression:

```java
QPersonEntity personE = QPersonEntity.personEntity;
...

BooleanExpression exp = filter.validate().build(new QueryDslExpressionBuilder<>((ctx, field, op, val) -> {
    return switch(field) {
        case KEYWORDS ->
            ctx.fromString(personE.name, op, val, v -> StringUtils.trimToEmpty(v));
        case DEPARTMENT ->
            ctx.fromLong(personE.departmentId, op, val);
        case LOCATION ->
            ctx.fromLong(personE.locationId, op, val);
        default -> Expressions.TRUE;
    };
}));
```

The builder used above is:

```java
import java.util.List;
import java.util.function.Function;

import com.effacy.jui.filter.ValueSupport;
import com.effacy.jui.filter.builder.ExpressionBuildException;
import com.effacy.jui.filter.builder.ExpressionBuilder;
import com.effacy.jui.filter.builder.IExpressionBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;

public class QueryDslExpressionBuilder<FIELD> implements IExpressionBuilder<BooleanExpression,FIELD> {

    public static final int DEFAULT_MAX_COMPLEXITY = 50;
    public static final int LOW_COMPLEXITY_LIMIT = 20;
    public static final int MEDIUM_COMPLEXITY_LIMIT = 50;
    public static final int HIGH_COMPLEXITY_LIMIT = 100;

    /**
     * Context used to expose utility methods.
     */
    public static class Context {

        /**
         * Apply a long valued constraint to the given path.
         * 
         * @param path
         *             the path to apply to.
         * @param op
         *             the constraint operator.
         * @param val
         *             the (long) value.
         * @return a suitable expression.
         */
        public BooleanExpression fromLong(NumberPath<Long> path, Operator op, Object val) throws ExpressionBuildException {
            if ((val != null) && (val instanceof Object[])) {
                Long[] value = ValueSupport.asLongArray(val);
                return switch (op) {
                    case IN -> path.in(value);
                    case NOT_IN -> path.notIn(value);
                    default -> Expressions.FALSE;
                };
            }
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

        /**
         * Apply an int valued constraint to the given path.
         * 
         * @param path
         *             the path to apply to.
         * @param op
         *             the constraint operator.
         * @param val
         *             the (int) value.
         * @return a suitable expression.
         */
        public BooleanExpression fromInt(NumberPath<Integer> path, Operator op, Object val) throws ExpressionBuildException {
            Integer value = ValueSupport.asInteger(val);
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
        
        /**
         * Apply a boolean (TRUE or FALSE) valued constraint to the given path.
         * 
         * @param path
         *             the path to apply to.
         * @param op
         *             the constraint operator.
         * @param val
         *             the (boolean) value.
         * @return a suitable expression.
         */
        public BooleanExpression fromBoolean(BooleanPath path, Operator op, Object val) throws ExpressionBuildException {
            Boolean value = ValueSupport.asBoolean(val);
            if (value == null)
                return Expressions.TRUE;
            return switch (op) {
                case EQ -> value ? path.isTrue() : path.isFalse();
                case NEQ -> value ? path.isFalse() : path.isTrue();
                default -> Expressions.FALSE;
            };
        }

        /**
         * See
         * {@link #fromString(StringPath, com.effacy.jui.filter.builder.IExpressionBuilder.Operator, Object, Function)}
         * where the mapping function is {@code null} (not used).
         */
        public BooleanExpression fromString(StringPath path, Operator op, Object val) throws ExpressionBuildException {
            return fromString(path, op, val, null);
        }

        /**
         * Apply a string valued constraint to the given path allowing for a mapping of
         * values.
         * 
         * @param path
         *             the path to apply to.
         * @param op
         *             the constraint operator.
         * @param val
         *             the (string) value.
         * @param tx
         *             an optional mapping of the value (the mapped value will be used
         *             as the constraint).
         * @return a suitable expression.
         */
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

        /**
         * Apply an enum valued constraint to the given path.
         * 
         * @param klass
         *              the enum class.
         * @param path
         *              the path to apply to.
         * @param op
         *              the constraint operator.
         * @param val
         *              the (enum) value.
         * @return a suitable expression.
         */
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

    /**
     * A composer (functional) interface for composing a dslquery expression from
     * term parts.
     */
    @FunctionalInterface
    public interface Composer<FIELD> {

        /**
         * Constructs a {@link BooleanExpression} from a field, operator and value given
         * a context of supporting transformations.
         * 
         * @param ctx
         *                 the context (for support).
         * @param field
         *                 the expression field (that being constrained).
         * @param operator
         *                 the operator (constraint being performed).
         * @param value
         *                 the value (doing the constraining).
         * @return the relevant expression.
         */
        public BooleanExpression term(Context ctx, FIELD field, Operator operator, Object value);
    }

    /**
     * Composer for building dsl expressions.
     */
    private Composer<FIELD> composer;

    /**
     * Context to pass to the composer.
     */
    private Context context;

    /**
     * The maximum allowable complexity.
     */
    private int maxComplexity;

    /**
     * Construct with a composer. Default complexity is used.
     * 
     * @param composer
     *                 the composer.
     */
    public QueryDslExpressionBuilder(Composer<FIELD> composer) {
        this(composer, DEFAULT_MAX_COMPLEXITY);
    }

    /**
     * Construct with a composer and a custom complexity limit.
     * 
     * @param composer
     *                      the composer.
     * @param maxComplexity
     *                      the maximum allowable complexity.
     */
    public QueryDslExpressionBuilder(Composer<FIELD> composer, int maxComplexity) {
        this.composer = composer;
        this.context = new Context();
        this.maxComplexity = maxComplexity;
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
            return Expressions.FALSE;
        return expression.not();
    }

    @Override
    public BooleanExpression bool(boolean value) {
        return value ? Expressions.TRUE : Expressions.FALSE;
    }

    @Override
    public BooleanExpression term(FIELD field, Operator operator, Object value) throws ExpressionBuildException {
        return composer.term(context, field, operator, value);
    }

    /**
     * Builds a BooleanExpression from the given expression, validating complexity
     * first.
     * 
     * @param expression
     *                   the expression to build
     * @return the built BooleanExpression
     * @throws ExpressionBuildException if the expression is too complex or build
     *                                  fails
     */
    public BooleanExpression buildWithComplexityValidation(ExpressionBuilder.Expression<FIELD> expression) throws ExpressionBuildException {
        validateComplexity(expression);
        return expression.build(this);
    }

    /**
     * Validates that the given expression does not exceed the configured complexity
     * limit.
     * 
     * @param expression
     *                   the expression to validate
     * @throws ExpressionBuildException if the expression is too complex
     */
    public void validateComplexity(ExpressionBuilder.Expression<FIELD> expression) throws ExpressionBuildException {
        int complexity = expression.complexity();
        if (complexity > maxComplexity) {
            throw new ExpressionBuildException(
                String.format("Expression complexity (%d) exceeds maximum allowed (%d)", complexity, maxComplexity)
            );
        }
    }
}
```