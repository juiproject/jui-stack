# Overview

This parse is not for the CSS (that is handled by `CssProcessor` in the parent package) but for the custom declarations (`@def` and `@eval`) that mimic (in a limited sense) functionality for value substitution present in GWT's CSS (and GSS) processing.

# Parser

## Grammar

As described in [Modification](#modification) the grammar is declared in `ExpressionParser.jjt`. This process the following:

1. `@def` lines which serve as value replacements in the CSS declarations. An example is `@def dark-gray #ccc;` which replace all instance of `dark-gray` with `#ccc`. The format is `@def REFERENCE REPLACEMENT`.
2. `@eval` lines are similar to `@def` execpt what is replaced is the outcome of executing an expression. Currently this is only a function call to a static method with no arguments. The format is `@def REFERENCE PACKAGE.CLASSNAME.FUNCTION();`.

These have been included for backward compatibility of original CSS that made use of these features present in the CSS and GSS processing provided by GWT. The is definitely scope to build on this.

## Substitution

The processing of substitutions occurs in `CssProcessor` and operates as follows:

1. The CSS is scanned line-by-line and the lines to be processed by this grammar are extracted (i.e if they beging with `@def`, etc). A revised CSS is generated with these lines removed.
2. The extracted lines from (1) are parsed and substitution values extracted as reference-to-expression pairs. The expression is a Java expression that can be inserted directly into the rebind class to be executed to return the substitution value.
3. The CSS is parsed and scanned. Each CSS declaration member value is scanned for the presence of a substiution reference (note that all these are coersed to lower case first). All found references are replaced under the mapping `ref -> __REF__`.

The exported CSS will have these references in place with the expectation that code will be injected into thr rebind class to perform the substitution on the resultant string. This is on the basis that the substitution value is given as a Java expression so needs to be evaluated. For example:

```java
String expression = ...;
cssParser.export ().replace ("__RADIUS__", expression);
```

*A possible future enhancement would be to differentiate expressions from literals so that the literals can be inserted directly (i.e. without needing to do a replacement post export). Another would be to evaluate the expression based on the exitent source code rather than injecting it into the rebind class (though this is a little more difficult).*

# Modification

## Grammar changes

The parser makes use of [JavaCC](https://javacc.github.io/javacc/) which is used to generate the code to perform the parsing. The grammar is defined in `ExpressionParser.jjt` found under the source tree `/src/main/jjtree/`. This us used as input to the JavaCC processor to generate the relevant classed under this package. To re-generate these classes one uses Maven by running (from the project base directory):

```bash
mvn -Pjavacc generate-sources
```

Some key points to observe are:

1. The command will attempt to update any existing classes but this is not always perfect. You can delete build classes completely to ensure that they are created afresh.
2. The classes that of the form `AstXXX` are actually modified post creation. These modifications pertain to how the AST tree is employed in practice. For our purposes we pass a builder through the tree to populate out the builder with the data needed to perform substiutions in the CSS. So **do not** delete these classes (unless you intend to re-write the post-creation modifications). *This is OK when you have removed the associated class reference in the JJT template.*
3. The class `BaseNode` is not generated at all and is a completely custom class that serves as the base class for all the generated `AstXXX` classes. This extends `SimpleNode` (which is a generated class) and is declared in the JJT template as the base class for the `AstXXX` classes.

## Testing

Unit testing is performed by `ExpressionParserTest` in the test source tree `/src/test/java` (under the same package).