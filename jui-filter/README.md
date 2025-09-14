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