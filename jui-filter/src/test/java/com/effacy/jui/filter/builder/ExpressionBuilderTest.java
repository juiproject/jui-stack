package com.effacy.jui.filter.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.effacy.jui.filter.builder.ExpressionBuilder.Expression;
import com.effacy.jui.filter.builder.ExpressionBuilderTest.FieldExpressionBuilder.Fields;
import com.effacy.jui.filter.builder.IExpressionBuilder.Operator;
import com.effacy.jui.filter.parser.FilterQueryParser;
import com.effacy.jui.filter.parser.FilterQueryParser.ParsedExpression;

public class ExpressionBuilderTest {

    @Test
    public void simple() {
        SimpleExpressionBuilder seb = new SimpleExpressionBuilder();

        Expression<String> exp = seb.term("status", Operator.EQ, "elephant");
        exp = exp.and(seb.term("quantity", Operator.LT, 33.2));

        assertEquals("(status = \"elephant\" AND quantity < 33.2)", exp.build(new StringExpressionBuilder()));
    }

    @Test
    public void fields_01() {
        Expression<Fields> exp = FieldExpressionBuilder.field1(Operator.GT, 22)
            .and(FieldExpressionBuilder.field2(Operator.EQ, "hubba"), FieldExpressionBuilder.field3(Operator.IN, new Status[] {Status.ACTIVE, Status.INACTIVE}));
    
        assertEquals("(FIELD1 > 22 AND FIELD2 = \"hubba\" AND FIELD3 IN [ACTIVE,INACTIVE])", exp.build(StringExpressionBuilder.<Fields> create(f -> f.name())));
    }

    @Test
    public void fields_02() {
        Expression<Fields> exp = FieldExpressionBuilder.and (
            FieldExpressionBuilder.field1(Operator.GT, 22),
            FieldExpressionBuilder.field2(Operator.EQ, "hubba"),
            FieldExpressionBuilder.field3(Operator.IN, new Status[] {Status.ACTIVE, Status.INACTIVE})
        );
    
        assertEquals("(FIELD1 > 22 AND FIELD2 = \"hubba\" AND FIELD3 IN [ACTIVE,INACTIVE])", exp.build(StringExpressionBuilder.<Fields> create(f -> f.name()))); 
    }

    @Test
    public void fields_03() throws Exception {
        ParsedExpression pexp = FilterQueryParser.parse("""
            FIELD1 > 22 AND FIELD2 = "hubba" AND FIELD3 IN [ACTIVE,INACTIVE]
        """);
        Expression<Fields> exp = pexp.build(FieldExpressionBuilder.stringBuilder());
    
        assertEquals("((FIELD1 > 22 AND FIELD2 = \"hubba\") AND FIELD3 IN [ACTIVE,INACTIVE])", exp.build(StringExpressionBuilder.<Fields> create(f -> f.name()))); 
    }

    @Test
    public void fields_04() {
        Expression<Fields> exp = FieldExpressionBuilder.and (
            FieldExpressionBuilder.or (
                FieldExpressionBuilder.field1(Operator.GT, 22),
                FieldExpressionBuilder.field2(Operator.EQ, "hubba")
            ),
            FieldExpressionBuilder.field3(Operator.IN, new Status[] {Status.ACTIVE, Status.INACTIVE})
        );
    
        assertEquals("((FIELD1 > 22 OR FIELD2 = \"hubba\") AND FIELD3 IN [ACTIVE,INACTIVE])", exp.build(StringExpressionBuilder.<Fields> create(f -> f.name()))); 
    }

    enum Status {
        ACTIVE, INACTIVE, OTHER;
    }

    public static class FieldExpressionBuilder {
        
        enum Fields {
            FIELD1, FIELD2, FIELD3;
        }

        private static ExpressionBuilder<Fields> INSTANCE = new ExpressionBuilder<FieldExpressionBuilder.Fields>();

        public static IExpressionBuilder<Expression<Fields>,String> stringBuilder() {
            return INSTANCE.mapped(v -> Fields.valueOf(v));
        }

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
}
