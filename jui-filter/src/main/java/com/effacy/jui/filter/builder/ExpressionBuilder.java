package com.effacy.jui.filter.builder;

import java.util.ArrayList;
import java.util.List;

/**
 * A base class for expression builders that produce expressions that are
 * buildable (see {@link IExpressionBuildable}).
 */
public class ExpressionBuilder<F> implements IExpressionBuilder<ExpressionBuilder.Expression<F>,F> {

    @FunctionalInterface
    public interface IExpressionVisitor<G> {
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
    }

    public Expression<F> and(List<Expression<F>> expressions) {
        return new ANDExpression(expressions);
    }

    public Expression<F> or(List<Expression<F>> expressions) {
        return new ORExpression(expressions);
    }

    public Expression<F> not(Expression<F> expression) {
        return new NOTExpression(expression);
    }

    public Expression<F> term(F field, Operator operator, Object value) {
        return new ComparisonExpression(field, operator, value);
    }

    public <G> IExpressionBuilder<Expression<F>,G> mapped(FieldMapper<G,F> mapper) {
        return new IExpressionBuilder<Expression<F>,G> () {

            @Override
            public Expression<F> and(List<Expression<F>> expressions) {
                return ExpressionBuilder.this.and(expressions);
            }

            @Override
            public Expression<F> or(List<Expression<F>> expressions) {
                return ExpressionBuilder.this.or(expressions);
            }

            @Override
            public Expression<F> not(Expression<F> expression) {
                return ExpressionBuilder.this.not(expression);
            }

            @Override
            public Expression<F> term(G field, Operator operator, Object value) {
                return ExpressionBuilder.this.term(mapper.map(field), operator, value);
            }
        };
    }

    /************************************************************************
     * Internals
     ************************************************************************/

    public class ANDExpression extends Expression<F> {

        private List<Expression<F>> expressions;

        ANDExpression(List<Expression<F>> expressions) {
            super(ExpressionBuilder.this);
            this.expressions = expressions;
        }

        @Override
        public <T> T build(IExpressionBuilder<T,F> builder) {
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
            return builder.and(resolved);
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
    }

    public class ORExpression extends Expression<F> {

        private List<Expression<F>> expressions;

        ORExpression(List<Expression<F>> expressions) {
            super(ExpressionBuilder.this);
            this.expressions = expressions;
        }

        @Override
        public <T> T build(IExpressionBuilder<T,F> builder) {
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
            return builder.or(resolved);
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
    }

    public class NOTExpression extends Expression<F> {

        private Expression<F> expression;

        NOTExpression(Expression<F> expression) {
            super(ExpressionBuilder.this);
            this.expression = expression;
        }

        @Override
        public <T> T build(IExpressionBuilder<T,F> builder) {
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
        public <T> T build(IExpressionBuilder<T,F> builder) {
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
    }

}
