package com.effacy.jui.filter.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.effacy.jui.filter.builder.ExpressionBuilder.Expression;
import com.effacy.jui.filter.builder.ExpressionBuilderTest.FieldsQueryBuilder.Fields;
import com.effacy.jui.filter.builder.IExpressionBuilder.Operator;
import com.effacy.jui.filter.parser.FilterQueryParser;
import com.effacy.jui.filter.parser.FilterQueryParser.ParsedExpression;

public class ExpressionBuilderTest {

    @Test
    public void simple() {
        ExpressionBuilder<String> seb = new ExpressionBuilder<>();

        Expression<String> exp = seb.term("status", Operator.EQ, "elephant");
        exp = exp.and(seb.term("quantity", Operator.LT, 33.2));

        assertEquals("(status = \"elephant\" AND quantity < 33.2)", exp.build(new StringExpressionBuilder()));
    }

    @Test
    public void fields_01() {
        Expression<Fields> exp = FieldsQueryBuilder.field1(Operator.GT, 22)
            .and(FieldsQueryBuilder.field2(Operator.EQ, "hubba"), FieldsQueryBuilder.field3(Operator.IN, new Status[] {Status.ACTIVE, Status.INACTIVE}));
    
        assertEquals("(FIELD1 > 22 AND FIELD2 = \"hubba\" AND FIELD3 IN [ACTIVE,INACTIVE])", exp.build(StringExpressionBuilder.<Fields> create(f -> f.name())));
    }

    @Test
    public void fields_02() {
        Expression<Fields> exp = FieldsQueryBuilder.and (
            FieldsQueryBuilder.field1(Operator.GT, 22),
            FieldsQueryBuilder.field2(Operator.EQ, "hubba"),
            FieldsQueryBuilder.field3(Operator.IN, new Status[] {Status.ACTIVE, Status.INACTIVE})
        );
    
        assertEquals("(FIELD1 > 22 AND FIELD2 = \"hubba\" AND FIELD3 IN [ACTIVE,INACTIVE])", exp.build(StringExpressionBuilder.<Fields> create(f -> f.name()))); 
    }

    @Test
    public void fields_03() throws Exception {
        ParsedExpression pexp = FilterQueryParser.parse("""
            FIELD1 > 22 AND FIELD2 = "hubba" AND FIELD3 IN [ACTIVE,INACTIVE]
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
    
        assertEquals("((FIELD1 > 22 AND FIELD2 = \"hubba\") AND FIELD3 IN [ACTIVE,INACTIVE])", exp.build(StringExpressionBuilder.<Fields> create(f -> f.name()))); 
    }

    @Test
    public void fields_04() throws Exception {
        Expression<Fields> exp = FieldsQueryBuilder.and (
            FieldsQueryBuilder.or (
                FieldsQueryBuilder.field1(Operator.GT, 22),
                FieldsQueryBuilder.field2(Operator.EQ, "hubba")
            ),
            FieldsQueryBuilder.field3(Operator.IN, new Status[] {Status.ACTIVE, Status.INACTIVE})
        );
    
        assertEquals("((FIELD1 > 22 OR FIELD2 = \"hubba\") AND FIELD3 IN [ACTIVE,INACTIVE])", exp.build(StringExpressionBuilder.<Fields> create(f -> f.name()))); 
    }

    @Test
    public void fields_05() throws Exception {
        Expression<Fields> exp1 = FieldsQueryBuilder.and (
            FieldsQueryBuilder.or (
                FieldsQueryBuilder.field1(Operator.GT, 22),
                FieldsQueryBuilder.field2(Operator.EQ, "hubba")
            ),
            FieldsQueryBuilder.field3(Operator.IN, new Status[] {Status.ACTIVE, Status.INACTIVE})
        );

        ParsedExpression pexp = FilterQueryParser.parse("""
            ((FIELD1 > 22 OR FIELD2 = \"hubba\") AND FIELD3 IN [ACTIVE,INACTIVE])
        """);
        Expression<Fields> exp2 = pexp.build(FieldsQueryBuilder.stringBuilder());
    
    }

    @Test
    public void equals_01() throws Exception {
        Expression<Fields> exp1 = FieldsQueryBuilder.and (
            FieldsQueryBuilder.or (
                FieldsQueryBuilder.field1(Operator.GT, 22),
                FieldsQueryBuilder.field2(Operator.EQ, "hubba")
            ),
            FieldsQueryBuilder.field3(Operator.EQ, Status.ACTIVE)
        );

        Expression<Fields> exp2 = FieldsQueryBuilder.and (
            FieldsQueryBuilder.or (
                FieldsQueryBuilder.field1(Operator.GT, 22),
                FieldsQueryBuilder.field2(Operator.EQ, "hubba")
            ),
            FieldsQueryBuilder.field3(Operator.EQ, Status.ACTIVE)
        );

        assertEquals(exp1, exp2);
    }

    @Test
    public void equals_02() throws Exception {
        Expression<Fields> exp1 = FieldsQueryBuilder.and (
            FieldsQueryBuilder.or (
                FieldsQueryBuilder.field1(Operator.GT, 22),
                FieldsQueryBuilder.field2(Operator.EQ, "hubba")
            ),
            FieldsQueryBuilder.field3(Operator.IN, new Status[] {Status.ACTIVE, Status.INACTIVE})
        );

        Expression<Fields> exp2 = FieldsQueryBuilder.and (
            FieldsQueryBuilder.or (
                FieldsQueryBuilder.field1(Operator.GT, 22),
                FieldsQueryBuilder.field2(Operator.EQ, "hubba")
            ),
            FieldsQueryBuilder.field3(Operator.IN, new Status[] {Status.ACTIVE, Status.INACTIVE})
        );

        assertEquals(exp1, exp2);
    }

    @Test
    public void equals_03() throws Exception {
        Expression<Fields> exp1 = FieldsQueryBuilder.and (
            FieldsQueryBuilder.or (
                FieldsQueryBuilder.field1(Operator.GT, 22),
                FieldsQueryBuilder.field2(Operator.EQ, "hubba")
            ),
            FieldsQueryBuilder.field3(Operator.IN, new Status[] {Status.ACTIVE, Status.INACTIVE})
        );

        // Here the status has the same contents but in a different order.
        Expression<Fields> exp2 = FieldsQueryBuilder.and (
            FieldsQueryBuilder.or (
                FieldsQueryBuilder.field1(Operator.GT, 22),
                FieldsQueryBuilder.field2(Operator.EQ, "hubba")
            ),
            FieldsQueryBuilder.field3(Operator.IN, new Status[] {Status.INACTIVE, Status.ACTIVE})
        );

        assertEquals(exp1, exp2);
    }

    @Test
    public void equals_04() throws Exception {
        Expression<Fields> exp1 = FieldsQueryBuilder.and (
            FieldsQueryBuilder.or (
                FieldsQueryBuilder.field1(Operator.GT, 22),
                FieldsQueryBuilder.field2(Operator.EQ, "hubba")
            ),
            FieldsQueryBuilder.field3(Operator.EQ, Status.ACTIVE)
        );

        Expression<Fields> exp2 = FieldsQueryBuilder.and (
            FieldsQueryBuilder.or (
                FieldsQueryBuilder.field1(Operator.GT, 22),
                FieldsQueryBuilder.field2(Operator.EQ, "wibble")
            ),
            FieldsQueryBuilder.field3(Operator.EQ, Status.ACTIVE)
        );

        assertNotEquals(exp1, exp2);
    }

    enum Status {
        ACTIVE, INACTIVE, OTHER;
    }

    public static class FieldsQueryBuilder {
        
        enum Fields {
            FIELD1, FIELD2, FIELD3;
        }

        private static ExpressionBuilder<Fields> INSTANCE = new ExpressionBuilder<FieldsQueryBuilder.Fields>();

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
