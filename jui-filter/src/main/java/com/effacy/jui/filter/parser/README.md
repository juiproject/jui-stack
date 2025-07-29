# Filter Parser

## Overview

This package holds code to parse a simple query filter expression (e.g. `status = ACTIVE and (count < 20 OR count > 30)`). Nearly all the code is generated using JavaCC from the `FilterQueryParser.jjt` file in the `src/main/jjtree` source branch. The only class that exists independently of the generated code is `BaseNode`.

The generated code adheres to the following:

1. AST node classes are prefixed with `Ast`.
2. AST node classes extends `BaseNode` (which extends the generated class `SimpleNode`).
    a. `BaseNode` declares an enum `ValueType` which prescribes the different values that are supported and ensures that the node properly captures the value type (these are not typed and represented as strings). 
3. The parser class is `FilterQueryParser` declared in `FilterQueryParser.jjt`.
    a. The parser declares a static `parse(...)` method that returns a `ParsedExpression` (or generates `FilterQueryParserException`).
    b. The `ParseExpression` implements `IExpressionBuildable<String>` so the parsed expression can be used to build other expressions.
    c. The `ParseExpression` suports a `print()` method that can be used for debugging (it prints to the console a representation of the parsed AST).

## Making changes

You should **only** make direct changes to `BaseNode` or the source grammar `FilterQueryParser.jjt`; changes to generated files risk being overwritten.

If you make changes to the source grammar `FilterQueryParser.jjt` then you can regenerated the code by executing:

```bash
mvn -P javacc compile
```

on the module project.

## Testing

There is a unit test `FilterQueryParserTest` for testing the parser itself. Ensure this passes after making any grammar changes (and modify accordingly).

For parsers, making changes to the grammar can wreck havoc pretty quickly so the tests are essential.

## Grammar

Here is a compact EBNF representation of the grammar:

```bnf
expression   ::= orExpr ;
orExpr       ::= andExpr (("OR" | "or") andExpr)* ;
andExpr      ::= notExpr (("AND" | "and") notExpr)* ;
notExpr      ::= ("NOT" | "not" | "!" | "~") notExpr
               | primary ;
primary      ::= comparison
               | "(" expression ")"
               | "TRUE" | "true"
               | "FALSE" | "false" ;
comparison   ::= field compOp value
               | field listOp list ;
field        ::= IDENTIFIER ;
compOp       ::= "=" | "==" | "IS" | "is"
               | "!=" | "~=" | "<>"
               | ">" | ">=" | "<" | "<="
               | "CONTAINS" | "contains"
               | "STARTS WITH" | "starts with"
               | "ENDS WITH" | "ends with" ;
listOp       ::= "IN" | "in" | "NOT IN" | "not in" ;
value        ::= STRING | IDENTIFIER | INTEGER | DECIMAL
               | "TRUE" | "true" | "FALSE" | "false"
               | "NULL" | "null" ;
list         ::= "[" value ("," value)* "]" ;
IDENTIFIER   ::= letter (letter | digit | "_")* ;
STRING       ::= '"' (any character except '"')* '"' ;
INTEGER      ::= ["-"] digit+ ;
DECIMAL      ::= ["-"] digit+ "." digit+ ;
```