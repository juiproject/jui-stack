package com.effacy.jui.filter.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

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
         * Tests the expression against the given predicate.
         * @param test
         * @return
         */
        public boolean test(Predicate<Expression<G>> test) {
            Carrier<Boolean> found = Carrier.of(false);
            traverse((depth,e) -> {
                if (test.test(e))
                    found.set(true);
            });
            return found.get();
        }
    }

    /**
     * Creates a new expression that is the AND of the passed expressions.
     */
    public Expression<F> and(List<Expression<F>> expressions) {
        return new ANDExpression(expressions);
    }

    /**
     * Creates a new expression that is the OR of the passed expressions.
     */
    public Expression<F> or(List<Expression<F>> expressions) {
        return new ORExpression(expressions);
    }

    /**
     * Creates a new expression that is the NOT of the passed expression.
     */
    public Expression<F> not(Expression<F> expression) {
        return new NOTExpression(expression);
    }

    /**
     * Creates a new expression represents the comparison of the given field with
     * the given value under the specified operator.
     */
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

}
