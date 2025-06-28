package com.effacy.jui.filter.parser;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.effacy.jui.filter.builder.IExpressionBuilder;

public class BaseNode extends SimpleNode {

    public enum ValueType {
        STRING, LITERAL, INTEGER, DECIMAL, BOOLEAN;
    }

    protected String image;

    protected ValueType valueType;

	public BaseNode(int i) {
		super(i);
	}

	public BaseNode(FilterQueryParser p, int i) {
		super(p, i);
	}

    public void setImage(String image) {
        this.image = image;
    }

    public void setValueType(ValueType valueType) {
        this.valueType = valueType;
    }

    protected void forEach(Consumer<Node> visitor) {
        for (Node node : children)
            visitor.accept (node);
    }

    @SuppressWarnings("unchecked")
    protected <T> T find(Class<T> klass) {
        for (Node child : children) {
            if (klass.isAssignableFrom(child.getClass()))
                return (T) child;
        }
        return null;
    }

    public <T> T build(IExpressionBuilder<T> builder) {
        List<T> expressions = new ArrayList<>();
        if (children != null) {
            for (int i = 0; i < children.length; i++)
                expressions.add(((BaseNode) children[i]).build(builder));
        }
        if (this instanceof AstRoot)
            return expressions.get(0);
        if (this instanceof AstAndExpression)
            return builder.and(expressions);
        if (this instanceof AstOrExpression)
            return builder.or(expressions);
        if (this instanceof AstNotExpression)
            return builder.not(expressions.get(0));
        if (this instanceof AstEquals)
            return builder.term(term(), IExpressionBuilder.Operator.EQ, value());
        if (this instanceof AstNotEquals)
            return builder.term(term(), IExpressionBuilder.Operator.NEQ, value());
        if (this instanceof AstGreaterThan)
            return builder.term(term(), IExpressionBuilder.Operator.GT, value());
        if (this instanceof AstGreaterThanOrEqual)
            return builder.term(term(), IExpressionBuilder.Operator.GTE, value());
        if (this instanceof AstLessThan)
            return builder.term(term(), IExpressionBuilder.Operator.LT, value());
        if (this instanceof AstLessThanOrEqual)
            return builder.term(term(), IExpressionBuilder.Operator.LTE, value());
        if (this instanceof AstIn)
            return builder.term(term(), IExpressionBuilder.Operator.IN, value());
        if (this instanceof AstNotIn)
            return builder.term(term(), IExpressionBuilder.Operator.NOT_IN, value());
        if (this instanceof AstContains)
            return builder.term(term(), IExpressionBuilder.Operator.CONTAINS, value());
        if (this instanceof AstStartsWith)
            return builder.term(term(), IExpressionBuilder.Operator.STARTS_WITH, value());
        if (this instanceof AstEndsWith)
            return builder.term(term(), IExpressionBuilder.Operator.ENDS_WITH, value());

        return null;
    }

    protected String term() {
        if ((children == null) || (children.length == 0))
            return null;
        return ((BaseNode) children[0]).image;
    }

    protected Object value() {
        if ((children == null) || (children.length == 1))
            return null;
        if (children[1] instanceof AstValue)
            return value((BaseNode) children[1]);
        if (children[1] instanceof AstList) {
            Node [] items = ((BaseNode) children[1]).children;
            if (items == null)
                return new Object[0];
            Object[] v = new Object[items.length];
            for (int i = 0; i < items.length; i++)
                v[i] = value((BaseNode) items[i]);
            return v;
        }
        return null;
    }

    protected Object value(BaseNode node) {
        ValueType valueType = node.valueType;
        if (valueType == null)
            return null;
        if (ValueType.BOOLEAN == valueType)
            return "true".equals(node.image);
        if (ValueType.INTEGER == valueType)
            return Integer.parseInt(node.image);
        if (ValueType.DECIMAL == valueType)
            return Double.parseDouble(node.image);
        return node.image;
    }

    protected void print(int depth) {
        if (this instanceof AstField)
            System.out.println(" ".repeat(Math.max(0, depth)) + getClass().getSimpleName() + "[" + this.image + "]");
        else if (this instanceof AstValue)
            System.out.println(" ".repeat(Math.max(0, depth)) + getClass().getSimpleName() + "[" + this.image + "]{" + this.valueType.name() + "}");
        else
            System.out.println(" ".repeat(Math.max(0, depth)) + getClass().getSimpleName());
        if (children != null) {
            for (int i = 0; i < children.length; i++)
                ((BaseNode) children[i]).print(depth + 1);
        }
    }
}

