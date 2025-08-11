package com.effacy.jui.filter.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.effacy.jui.filter.builder.optimizer.IExpressionOptimizer;
import com.effacy.jui.filter.parser.FilterQueryParser;
import com.effacy.jui.filter.parser.FilterQueryParser.FilterQueryParserException;
import com.effacy.jui.filter.parser.FilterQueryParser.ParsedExpression;
import com.effacy.jui.platform.util.client.Carrier;

/**
 * A base class for expression builders that produce expressions that are
 * buildable (see {@link IExpressionBuildable}).
 */
public class ExpressionBuilder<F> implements IExpressionBuilder<ExpressionBuilder.Expression<F>,F> {

    /**
     * Used to visit the nodes in the expression.
     */
    @FunctionalInterface
    public interface IExpressionVisitor<G> {

        /**
         * Visits a node of the given expression and of the given depth.
         * 
         * @param depth
         *                   the depth (from 0).
         * @param expression
         *                   the expression.
         */
        public void visit(int depth, Expression<G> expression);
    }

    @FunctionalInterface
    public interface IExpressionValidator<G> {
    
        public boolean validate(List<String> errors, G field, Operator op, Object value);
    }

    /**
     * Maps a string version of the field to the field (for deserialisation).
     */
    private FieldMapper<String,F> stringToFieldMapper;

    /**
     * Maps a field to its string representation (for serialisation).
     */
    private FieldMapper<F,String> fieldToStringMapper;

    /**
     * Construct without mapper information. This means
     * {@link #serialise(Expression)} and {@link #deserialise(String)} will not
     * work.
     */
    public ExpressionBuilder() {
        // Nothing.
    }

    /**
     * Construct for a field type that is an enum.
     * <p>
     * The type is used to construct mappers for serialisation and deserialisation.
     * 
     * @param klass
     *              the enum class for the field.
     */
    public ExpressionBuilder(Class<F> klass) {
        stringToFieldMapper = str -> {
            for (F v : klass.getEnumConstants()) {
                if (str.equalsIgnoreCase(((Enum<?>) v).name()))
                    return v;
            }
            throw new ExpressionBuildException("unable to map field " + str);
        };
        fieldToStringMapper = v -> ((Enum<?>) v).name();
    }

    /**
     * Construct with mappers to use for {@link #serialise(Expression)} and
     * {@link #deserialise(String)}. The mappers should be inverses of each other.
     * <p>
     * If the field type is an enum thn consider using
     * {@link #ExpressionBuilder(Class)}.
     * 
     * @param stringToFieldMapper
     *                            maps a string to a field.
     * @param fieldToStringMapper
     *                            maps a field to a string.
     */
    public ExpressionBuilder(FieldMapper<String,F> stringToFieldMapper, FieldMapper<F,String> fieldToStringMapper) {
        this.stringToFieldMapper = stringToFieldMapper;
        this.fieldToStringMapper = fieldToStringMapper;
    }

    /**
     * This will serialise the passed expression (produced by this builder) to a
     * string.
     * 
     * @param exp
     *            the expression to serialise.
     * @return the serialised expression.
     */
    public String serialise(Expression<F> exp) {
        if (fieldToStringMapper == null)
            throw new ExpressionBuildException("no field-to-string mapper defined");
        return exp.build(StringExpressionBuilder.<F> remap(fieldToStringMapper));
    }

    /**
     * Deserialised the passed string to an expression supported by this builder.
     * <p>
     * The resulting expression will also be validated.
     * 
     * @param str
     *               the string to parse.
     * @param mapper
     *               to map string values to
     * @return
     * @throws FilterQueryParserException
     *                                    if there was a problem parsing or
     *                                    processing the expression.
     */
    public Expression<F> deserialise(String str) throws FilterQueryParserException {
        if (stringToFieldMapper == null)
            throw new ExpressionBuildException("no string-to-field mapper defined");
        if (str == null)
            return null;
        ParsedExpression pexp = FilterQueryParser.parse(str);
        try {
            return pexp.build(mapped(v -> stringToFieldMapper.map(v))).validate();
        } catch (ExpressionBuildException e) {
            throw new FilterQueryParserException(e.getMessage());
        }
    }

    /**
     * The base expression class that is the product of the builder.
     */
    public abstract static class Expression<G> implements IExpressionBuildable<G> {

        /**
         * The parent builder.
         */
        private ExpressionBuilder<G> parent;

        /**
         * Constructs with reference to the parent builder to delegate operations to.
         * <p>
         * This is not a inner class so that we can better manage the generics.
         * 
         * @param parent
         *               the parent builder.
         */
        protected Expression(ExpressionBuilder<G> parent) {
            this.parent = parent;
        }
    
        /**
         * See {@link #and(List)}.
         */
        @SafeVarargs
        public final Expression<G> and(Expression<G>...expressions) {
            if (expressions.length == 0)
                return this;
            return and(List.of(expressions));
        }

        /**
         * Construct an AND expression from this and the passed expressions.
         * 
         * @param expressions
         *                    the expressions to AND with.
         * @return the AND expression.
         */
        public Expression<G> and(List<Expression<G>> expressions) {
            if ((expressions == null) || expressions.isEmpty())
                return this;
            List<Expression<G>> exp = new ArrayList<>();
            exp.add(this);
            exp.addAll(expressions);
            return parent.and(exp);
        }

        /**
         * See {@link #or(List)}.
         */
        @SafeVarargs
        public final Expression<G> or(Expression<G>...expressions) {
            if (expressions.length == 0)
                return this;
            return or(List.of(expressions));
        }

        /**
         * Construct an OR expression from this and the passed expressions.
         * 
         * @param expressions
         *                    the expressions to OR with.
         * @return the OR expression.
         */
        public Expression<G> or(List<Expression<G>> expressions) {
            if ((expressions == null) || expressions.isEmpty())
                return this;
            List<Expression<G>> exp = new ArrayList<>();
            exp.add(this);
            exp.addAll(expressions);
            return parent.or(exp);
        }

        /**
         * Wrap this in a negation.
         * 
         * @return the negated expression.
         */
        public Expression<G> not() {
            return parent.not(this);
        }

        /**
         * Traverses the expression structure.
         * 
         * @param visitor
         *                the visitor to process expression nodes.
         */
        public void traverse(IExpressionVisitor<G> visitor) {
            traverse(0, visitor);
        }

        /**
         * Implements {@link #traverse(IExpressionVisitor)} with the ability to carry
         * depth data.
         * 
         * @param depth
         *                the depth in the structure.
         * @param visitor
         *                the visitor to process expression nodes.
         */
        protected void traverse(int depth, IExpressionVisitor<G> visitor) {
            visitor.visit(depth, this);
        }

        /**
         * See {@link #validate(IExpressionValidator)} but uses the default validator
         * which expects the field type to implement {@link Field}.
         * 
         * @return this expression.
         * @throws ExpressionBuildException
         *                                  if validation fails.
         */
        public Expression<G> validate() throws ExpressionBuildException {
            return validate(null);
        }

        /**
         * Validates the expression using the given validator. If the validator is
         * {@code null} then the default is used (which expects the field type to
         * implement {@link Field}).
         * <p>
         * If invalid then {@link ExpressionBuildException} will be thrown containing
         * the error message.
         * 
         * @param validator
         *                  (optional) the validator to use.
         * @return this expression.
         * @throws ExpressionBuildException
         *                                  if validation fails.
         */
        public Expression<G> validate(IExpressionValidator<G> validator) throws ExpressionBuildException {
            List<String> errors = new ArrayList<>();
            if (!validate(errors, validator)) {
                StringBuffer sb = new StringBuffer();
                for (int i = 0, len = errors.size(); i < len; i++) {
                    if (i > 0) {
                        if (i < (len - 1))
                            sb.append(", ");
                        else
                            sb.append(" and ");
                    }
                    sb.append(errors.get(i));
                }
                throw new ExpressionBuildException(sb.toString());
            }
            return this;
        }

        /**
         * Validates the expression.
         * 
         * @param errors
         *                  to accumulate errors.
         * @param validator
         *                  the terms validator to use.
         * @return {@code true} if valid.
         */
        protected boolean validate(List<String> errors, IExpressionValidator<G> validator) {
            return true;
        }

        /**
         * Calculates the complexity of this expression based on node types and nesting depth.
         * <p>
         * Uses weighted scoring:
         *  - Comparison terms: 1 point
         *  - NOT operations: 2 points  
         *  - AND operations: 2 points
         *  - OR operations: 3 points
         *  - Deep nesting penalty: +1 point per level beyond depth 3
         * 
         * @return the complexity score
         */
        public int complexity() {
            Carrier<Integer> complexity = Carrier.of(0);
            traverse((depth, expr) -> {
                // Base complexity by expression type
                if (expr instanceof ExpressionBuilder<?>.ComparisonExpression) {
                    complexity.set(complexity.get() + 1);
                } else if (expr instanceof ExpressionBuilder<?>.NOTExpression) {
                    complexity.set(complexity.get() + 2);
                } else if (expr instanceof ExpressionBuilder<?>.ANDExpression) {
                    complexity.set(complexity.get() + 2);
                } else if (expr instanceof ExpressionBuilder<?>.ORExpression) {
                    complexity.set(complexity.get() + 3);
                } else if (expr instanceof ExpressionBuilder<?>.BoolExpression) {
                    complexity.set(complexity.get() + 1);
                }
                
                // Penalty for deep nesting (beyond depth 3)
                if (depth > 3) {
                    complexity.set(complexity.get() + (depth - 3));
                }
            });
            return complexity.get();
        }

        /**
         * Optimizes this expression using the given optimizer to produce a minimal 
         * equivalent form.
         * 
         * @param optimizer the optimizer to apply to this expression
         * @return the optimized expression, or this expression if no optimizations apply
         */
        public Expression<G> optimize(IExpressionOptimizer<G> optimizer) {
            return optimizer.optimize(this);
        }
    }

    /**
     * Creates a new expression that is the AND of the passed expressions.
     */
    @Override
    public Expression<F> and(List<Expression<F>> expressions) {
        return new ANDExpression(expressions);
    }

    /**
     * Creates a new expression that is the OR of the passed expressions.
     */
    @Override
    public Expression<F> or(List<Expression<F>> expressions) {
        return new ORExpression(expressions);
    }

    /**
     * Creates a new expression that is the NOT of the passed expression.
     */
    @Override
    public Expression<F> not(Expression<F> expression) {
        return new NOTExpression(expression);
    }

    /**
     * Cretes a bool-expression.
     */
    @Override
    public Expression<F> bool(boolean value) {
        return new BoolExpression(value);
    }

    /**
     * Creates a new expression represents the comparison of the given field with
     * the given value under the specified operator.
     */
    @Override
    public Expression<F> term(F field, Operator operator, Object value) {
        return new ComparisonExpression(field, operator, value);
    }

    /**
     * Creates a new expression buider over a different field set using the mapper
     * to translate from the source set to the target.
     */
    public <G> IExpressionBuilder<Expression<F>,G> mapped(FieldMapper<G,F> mapper) {
        return new MappedExpresionBuilder<Expression<F>, G, F> (this, mapper);
    }

    /************************************************************************
     * Internals
     ************************************************************************/

    abstract class NaryExpression extends Expression<F> {

        private List<Expression<F>> expressions;

        NaryExpression(List<Expression<F>> expressions) {
            super(ExpressionBuilder.this);
            this.expressions = expressions;
        }

        protected <T> List<T> resolve(IExpressionBuilder<T,F> builder) throws ExpressionBuildException {
            List<T> resolved = new ArrayList<>();
            if (expressions != null) {
                for (Expression<F> se : expressions) {
                    if (se == null)
                        continue;
                    T r = se.build(builder);
                    if (r != null)
                        resolved.add(r);
                }
            }
            return resolved;
        }

        @Override
        protected void traverse(int depth, IExpressionVisitor<F> visitor) {
            super.traverse(depth, visitor);
            if (expressions != null) {
                expressions.forEach(exp -> {
                    exp.traverse(depth + 1, visitor);
                });
            }
        }

        @Override
        protected boolean validate(List<String> errors, IExpressionValidator<F> validator) {
            boolean valid = true;
            for (Expression<F> exp : expressions) {
                if (!exp.validate(errors, validator))
                    valid = false;
            }
            return valid;
        }

        protected boolean _equals(ExpressionBuilder<?>.NaryExpression exp) {
            if ((expressions == null) || expressions.isEmpty())
                return (exp.expressions == null) || exp.expressions.isEmpty();
            if ((exp.expressions == null) || exp.expressions.isEmpty())
                return false;
            if (exp.expressions.size() != exp.expressions.size())
                return false;
            for (int i = 0; i < expressions.size(); i++) {
                if (!expressions.get(i).equals(exp.expressions.get(i)))
                    return false;
            }
            return true;
        }
    }

    public class ANDExpression extends NaryExpression {

        ANDExpression(List<Expression<F>> expressions) {
            super(expressions);
        }

        @Override
        public <T> T build(IExpressionBuilder<T,F> builder) throws ExpressionBuildException {
            return builder.and(resolve(builder));
        }

        @Override
        public boolean equals(Object exp) {
            if (exp == null)
                return false;
            if (this == exp)
                return true;
            if (!(exp instanceof ExpressionBuilder<?>.ANDExpression))
                return false;
            return super._equals((ExpressionBuilder<?>.NaryExpression) exp);
        }
    }

    public class ORExpression extends NaryExpression {

        ORExpression(List<Expression<F>> expressions) {
            super(expressions);
        }

        @Override
        public <T> T build(IExpressionBuilder<T,F> builder) throws ExpressionBuildException {
            return builder.or(resolve(builder));
        }

        @Override
        public boolean equals(Object exp) {
            if (exp == null)
                return false;
            if (this == exp)
                return true;
            if (!(exp instanceof ExpressionBuilder<?>.ORExpression))
                return false;
            return super._equals((ExpressionBuilder<?>.NaryExpression) exp);
        }
    }

    public class NOTExpression extends Expression<F> {

        private Expression<F> expression;

        NOTExpression(Expression<F> expression) {
            super(ExpressionBuilder.this);
            this.expression = expression;
        }

        @Override
        public <T> T build(IExpressionBuilder<T,F> builder) throws ExpressionBuildException {
            if (expression == null)
                return null;
            T r = expression.build(builder);
            if (r == null)
                return null;
            return builder.not(r);
        }

        @Override
        protected void traverse(int depth, IExpressionVisitor<F> visitor) {
            super.traverse(depth, visitor);
            if (expression != null)
                expression.traverse(depth + 1, visitor);
        }
        
        @Override
        protected boolean validate(List<String> errors, IExpressionValidator<F> validator) {
            return expression.validate(errors, validator);
        }

        @Override
        public boolean equals(Object exp) {
            if (exp == null)
                return false;
            if (this == exp)
                return true;
            if (!(exp instanceof ExpressionBuilder<?>.NOTExpression))
                return false;
            ExpressionBuilder<?>.NOTExpression castExp = (ExpressionBuilder<?>.NOTExpression) exp;
            if (expression == null)
                return (castExp.expression == null);
            if (castExp.expression == null)
                return false;
            return expression.equals(castExp);
        }
    }

    public class ComparisonExpression extends Expression<F> {

        private F field;

        private Operator operator;

        private Object value;

        ComparisonExpression(F field, Operator operator, Object value) {
            super(ExpressionBuilder.this);
            this.field = field;
            this.operator = operator;
            // If the operator is an IN or NOT_IN then we need to ensure that the value is
            // an array (it is OK to transform this to an array).
            if ((operator == Operator.IN) || (operator == Operator.NOT_IN)) {
                if (value == null)
                    value = new Object[0];
                else if (!value.getClass().isArray())
                    value = new Object[] { value };
            }
            this.value = value;
        }

        @Override
        public <T> T build(IExpressionBuilder<T,F> builder) throws ExpressionBuildException {
            return builder.term(field, operator, value);
        }

        public F field() {
            return field;
        }

        public Operator operator() {
            return operator;
        }

        public Object value() {
            return value;
        }

        @Override
        protected boolean validate(List<String> errors, IExpressionValidator<F> validator) {
            if (validator != null)
                return validator.validate(errors, field, operator, value);
            if (field instanceof Field) {
                Optional<String> outcome = (((Field) field).type().validate(operator, value));
                if (outcome.isEmpty())
                    return true;
                String fieldName = (field instanceof Enum) ? ((Enum<?>) field).name() : field.toString();
                errors.add(outcome.get() + ": " + fieldName + " " + operator.name() + " " + value);
                return false;
            }
            return true;
        }

        @Override
        public boolean equals(Object exp) {
            if (exp == null)
                return false;
            if (this == exp)
                return true;
            if (!(exp instanceof ExpressionBuilder<?>.ComparisonExpression))
                return false;
            ExpressionBuilder<?>.ComparisonExpression castExp = (ExpressionBuilder<?>.ComparisonExpression) exp;
            if ((field == null) && (castExp.field != null))
                return false;
            if ((field != null) && (castExp.field == null))
                return false;
            if ((field != castExp.field) && !field.equals(castExp.field))
                return false;
            if (operator != castExp.operator)
                return false;
            if ((value == null) && (castExp.value != null))
                return false;
            if ((value != null) && (castExp.value == null))
                return false;
            if ((value != null) && (castExp.value != null) && value.getClass().isArray() && castExp.value.getClass().isArray()) {
                Object[] arr1 = (Object[]) value;
                Object[] arr2 = (Object[]) castExp.value;
                if (arr1.length != arr2.length)
                    return false;
                List<Object> list1 = new ArrayList<>(Arrays.asList(arr1));
                List<Object> list2 = new ArrayList<>(Arrays.asList(arr2));
                if (!list1.containsAll(list2) || !list2.containsAll(list1))
                    return false;
            } else if ((value != castExp.value) && (value != null && !value.equals(castExp.value)))
                return false;
            return true;
        }
    }

    /**
     * Expression that represents a boolean literal (true or false).
     */
    public class BoolExpression extends Expression<F> {

        private boolean value;

        public BoolExpression(boolean value) {
            super(ExpressionBuilder.this);
            this.value = value;
        }

        @Override
        public <T> T build(IExpressionBuilder<T,F> builder) throws ExpressionBuildException {
            return builder.bool(value);
        }

        public boolean getValue() {
            return value;
        }
    }


}
