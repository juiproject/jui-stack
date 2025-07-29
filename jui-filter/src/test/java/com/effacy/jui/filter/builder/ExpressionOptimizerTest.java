package com.effacy.jui.filter.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.effacy.jui.filter.builder.ExpressionBuilder.Expression;
import com.effacy.jui.filter.builder.ExpressionOptimizerTest.FieldsQueryBuilder.Fields;
import com.effacy.jui.filter.builder.IExpressionBuilder.Operator;
import com.effacy.jui.filter.builder.optimizer.BooleanLogicOptimizer;
import com.effacy.jui.filter.builder.optimizer.ComparisonOptimizer;
import com.effacy.jui.filter.builder.optimizer.CompositeOptimizer;
import com.effacy.jui.filter.builder.optimizer.IExpressionOptimizer;
import com.effacy.jui.filter.builder.optimizer.NotPushingOptimizer;
import com.effacy.jui.filter.builder.optimizer.StructuralOptimizer;
import com.effacy.jui.filter.parser.FilterQueryParser;
import com.effacy.jui.filter.parser.FilterQueryParser.ParsedExpression;

public class ExpressionOptimizerTest {

    /************************************************************************
     * Structural Optimizations
     ************************************************************************/

    @Test
    public void optimization_structural_01() throws Exception {
        // Test flattening of nested AND expressions
        ParsedExpression pexp = FilterQueryParser.parse("""
            FIELD1 > 22 AND (FIELD2 = "hubba" AND FIELD3 = ACTIVE)
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new StructuralOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Structural flattening: A AND (B AND C) → A AND B AND C
        assertEquals("(FIELD1 > 22 AND FIELD2 = \"hubba\" AND FIELD3 = ACTIVE)", 
                     optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_structural_02() throws Exception {
        // Test double negation elimination
        ParsedExpression pexp = FilterQueryParser.parse("""
            NOT(NOT(FIELD1 > 22))
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new StructuralOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Double negation elimination: NOT(NOT A) → A
        assertEquals("FIELD1 > 22", 
                     optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    /************************************************************************
     * Boolean Logic Optimizations
     ************************************************************************/

    @Test
    public void optimization_boolean_logic_01() throws Exception {
        // Test idempotence optimization
        ParsedExpression pexp = FilterQueryParser.parse("""
            FIELD1 > 22 AND FIELD1 > 22
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new BooleanLogicOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Idempotence: A AND A → A
        assertEquals("FIELD1 > 22", 
                     optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_boolean_logic_02() throws Exception {
        // Test absorption optimization
        ParsedExpression pexp = FilterQueryParser.parse("""
            FIELD1 > 22 OR (FIELD1 > 22 AND FIELD2 = "hubba")
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new BooleanLogicOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Absorption: A OR (A AND B) → A
        assertEquals("FIELD1 > 22", 
                     optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    /************************************************************************
     * Comparison Optimizations
     ************************************************************************/

    @Test
    public void optimization_comparison_01() throws Exception {
        // Test redundancy elimination in comparisons
        ParsedExpression pexp = FilterQueryParser.parse("""
            FIELD1 > 5 AND FIELD1 > 3
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new ComparisonOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Redundancy elimination: field > 5 AND field > 3 → field > 5 (more restrictive condition)
        assertEquals("FIELD1 > 5", 
                     optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_comparison_02() throws Exception {
        // Test contradiction detection in comparisons
        ParsedExpression pexp = FilterQueryParser.parse("""
            FIELD2 = "hello" AND FIELD2 = "world"
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new ComparisonOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Contradiction detection: field = A AND field = B → false
        assertFalse(optimized);
    }

    @Test
    public void optimization_comparison_03() throws Exception {
        // Test redundancy elimination with less-than operator
        ParsedExpression pexp = FilterQueryParser.parse("""
            FIELD1 < 10 AND FIELD1 < 15
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new ComparisonOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Redundancy elimination: field < 10 AND field < 15 → field < 10 (more restrictive condition)
        assertEquals("FIELD1 < 10", 
                     optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_comparison_04() throws Exception {
        // Test redundancy elimination with greater-than-or-equal operator
        ParsedExpression pexp = FilterQueryParser.parse("""
            FIELD1 >= 20 AND FIELD1 >= 15
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new ComparisonOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Redundancy elimination: field >= 20 AND field >= 15 → field >= 20 (more restrictive condition)
        assertEquals("FIELD1 >= 20", 
                     optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_comparison_05() throws Exception {
        // Test redundancy elimination with less-than-or-equal operator
        ParsedExpression pexp = FilterQueryParser.parse("""
            FIELD1 <= 10 AND FIELD1 <= 5
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new ComparisonOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Redundancy elimination: field <= 10 AND field <= 5 → field <= 5 (more restrictive condition)
        assertEquals("FIELD1 <= 5", 
                     optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_comparison_06() throws Exception {
        // Test contradiction detection with impossible range
        ParsedExpression pexp = FilterQueryParser.parse("""
            FIELD1 > 20 AND FIELD1 < 10
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new ComparisonOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Contradiction detection: field > 20 AND field < 10 → false (impossible condition)
        assertFalse(optimized);
    }

    @Test
    public void optimization_comparison_07() throws Exception {
        // Test contradiction detection with boundary conditions
        ParsedExpression pexp = FilterQueryParser.parse("""
            FIELD1 >= 10 AND FIELD1 < 10
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new ComparisonOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Contradiction detection: field >= 10 AND field < 10 → false (impossible condition)
        assertFalse(optimized);
    }

    @Test
    public void optimization_comparison_08() throws Exception {
        // Test mixed operator redundancy elimination
        ParsedExpression pexp = FilterQueryParser.parse("""
            FIELD1 > 10 AND FIELD1 >= 10
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new ComparisonOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Redundancy elimination: field > 10 AND field >= 10 → field > 10 (more restrictive condition)
        assertEquals("FIELD1 > 10", 
                     optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_comparison_09() throws Exception {
        // Test boundary contradiction with <= and >
        ParsedExpression pexp = FilterQueryParser.parse("""
            FIELD1 <= 10 AND FIELD1 > 10
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new ComparisonOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Contradiction detection: field <= 10 AND field > 10 → false (impossible condition)
        assertFalse(optimized);
    }

    @Test
    public void optimization_comparison_10() throws Exception {
        // Test valid range conditions that don't contradict
        ParsedExpression pexp = FilterQueryParser.parse("""
            FIELD1 >= 5 AND FIELD1 <= 15
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new ComparisonOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // No contradiction: field >= 5 AND field <= 15 represents valid range [5,15]
        assertEquals("(FIELD1 >= 5 AND FIELD1 <= 15)", 
                     optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_comparison_11() throws Exception {
        // Test mixed operator redundancy elimination (reverse case)
        ParsedExpression pexp = FilterQueryParser.parse("""
            FIELD1 >= 10 AND FIELD1 > 10
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new ComparisonOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Redundancy elimination: field >= 10 AND field > 10 → field > 10 (more restrictive condition)
        assertEquals("FIELD1 > 10", 
                     optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_comparison_12() throws Exception {
        // Test mixed operator redundancy elimination with <= and <
        ParsedExpression pexp = FilterQueryParser.parse("""
            FIELD1 <= 5 AND FIELD1 < 5
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new ComparisonOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Redundancy elimination: field <= 5 AND field < 5 → field < 5 (more restrictive condition)
        assertEquals("FIELD1 < 5", 
                     optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    /************************************************************************
     * String Comparison Tests (Should NOT be optimized)
     ************************************************************************/

    @Test
    public void optimization_string_no_redundancy_01() throws Exception {
        // Test that string comparisons with > operator are not optimized
        ParsedExpression pexp = FilterQueryParser.parse("""
            FIELD2 > "apple" AND FIELD2 > "banana"
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new ComparisonOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // String comparisons should not be optimized (implementation-dependent semantics)
        assertEquals("(FIELD2 > \"apple\" AND FIELD2 > \"banana\")", 
                     optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_string_no_redundancy_02() throws Exception {
        // Test that string comparisons with < operator are not optimized
        ParsedExpression pexp = FilterQueryParser.parse("""
            FIELD2 < "zebra" AND FIELD2 < "apple"
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new ComparisonOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // String comparisons should not be optimized (implementation-dependent semantics)
        assertEquals("(FIELD2 < \"zebra\" AND FIELD2 < \"apple\")", 
                     optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_string_no_redundancy_03() throws Exception {
        // Test that string comparisons with >= and <= operators are not optimized
        ParsedExpression pexp = FilterQueryParser.parse("""
            FIELD2 >= "hello" AND FIELD2 <= "world"
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new ComparisonOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // String range comparisons should not be optimized (implementation-dependent semantics)
        assertEquals("(FIELD2 >= \"hello\" AND FIELD2 <= \"world\")", 
                     optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_string_no_contradiction() throws Exception {
        // Test that apparent string contradictions are not detected as impossible
        ParsedExpression pexp = FilterQueryParser.parse("""
            FIELD2 > "zebra" AND FIELD2 < "apple"
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new ComparisonOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // String contradictions should not be detected (comparison semantics are implementation-dependent)
        assertEquals("(FIELD2 > \"zebra\" AND FIELD2 < \"apple\")", 
                     optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_string_mixed_operators() throws Exception {
        // Test that string comparisons with mixed operators are not optimized
        ParsedExpression pexp = FilterQueryParser.parse("""
            FIELD2 > "hello" AND FIELD2 >= "hello"
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new ComparisonOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // String comparisons with mixed operators should not be optimized
        assertEquals("(FIELD2 > \"hello\" AND FIELD2 >= \"hello\")", 
                     optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_string_equality_idempotence() throws Exception {
        // Test that string equality idempotence still works (this should be safe)
        ParsedExpression pexp = FilterQueryParser.parse("""
            FIELD2 = "test" AND FIELD2 = "test"
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new BooleanLogicOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // String equality idempotence should work: field = "test" AND field = "test" → field = "test"
        assertEquals("FIELD2 = \"test\"", 
                     optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_string_equality_contradiction() throws Exception {
        // Test that string equality contradictions are still detected
        ParsedExpression pexp = FilterQueryParser.parse("""
            FIELD2 = "hello" AND FIELD2 = "world"
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new ComparisonOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // String equality contradictions should still be detected: field = "A" AND field = "B" → false
        assertFalse(optimized);
        
        // Verify the string representation is "false"
        String stringOutput = optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name()));
        assertEquals("false", stringOutput);
    }

    /************************************************************************
     * Enum Comparison Tests (Should NOT be optimized)
     ************************************************************************/

    @Test
    public void optimization_enum_no_redundancy_01() throws Exception {
        // Test that enum comparisons with > operator are not optimized
        ParsedExpression pexp = FilterQueryParser.parse("""
            FIELD3 > ACTIVE AND FIELD3 > INACTIVE
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new ComparisonOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Enum comparisons should not be optimized (implementation-dependent ordering)
        assertEquals("(FIELD3 > ACTIVE AND FIELD3 > INACTIVE)", 
                     optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_enum_no_redundancy_02() throws Exception {
        // Test that enum comparisons with < operator are not optimized
        ParsedExpression pexp = FilterQueryParser.parse("""
            FIELD3 < OTHER AND FIELD3 < ACTIVE
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new ComparisonOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Enum comparisons should not be optimized (implementation-dependent ordering)
        assertEquals("(FIELD3 < OTHER AND FIELD3 < ACTIVE)", 
                     optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_enum_no_redundancy_03() throws Exception {
        // Test that enum comparisons with >= and <= operators are not optimized
        ParsedExpression pexp = FilterQueryParser.parse("""
            FIELD3 >= ACTIVE AND FIELD3 <= OTHER
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new ComparisonOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Enum range comparisons should not be optimized (implementation-dependent ordering)
        assertEquals("(FIELD3 >= ACTIVE AND FIELD3 <= OTHER)", 
                     optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_enum_no_contradiction() throws Exception {
        // Test that apparent enum contradictions are not detected as impossible
        ParsedExpression pexp = FilterQueryParser.parse("""
            FIELD3 > OTHER AND FIELD3 < ACTIVE
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new ComparisonOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Enum contradictions should not be detected (ordering semantics are implementation-dependent)
        assertEquals("(FIELD3 > OTHER AND FIELD3 < ACTIVE)", 
                     optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_enum_mixed_operators() throws Exception {
        // Test that enum comparisons with mixed operators are not optimized
        ParsedExpression pexp = FilterQueryParser.parse("""
            FIELD3 > ACTIVE AND FIELD3 >= ACTIVE
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new ComparisonOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Enum comparisons with mixed operators should not be optimized
        assertEquals("(FIELD3 > ACTIVE AND FIELD3 >= ACTIVE)", 
                     optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_enum_equality_idempotence() throws Exception {
        // Test that enum equality idempotence still works (this should be safe)
        ParsedExpression pexp = FilterQueryParser.parse("""
            FIELD3 = ACTIVE AND FIELD3 = ACTIVE
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new BooleanLogicOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Enum equality idempotence should work: field = ACTIVE AND field = ACTIVE → field = ACTIVE
        assertEquals("FIELD3 = ACTIVE", 
                     optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_enum_equality_contradiction() throws Exception {
        // Test that enum equality contradictions are still detected
        ParsedExpression pexp = FilterQueryParser.parse("""
            FIELD3 = ACTIVE AND FIELD3 = INACTIVE
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new ComparisonOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Enum equality contradictions should still be detected: field = A AND field = B → false
        assertFalse(optimized);
    }

    /************************************************************************
     * IN/NOT_IN Operator Optimizations  
     ************************************************************************/

    @Test
    public void optimization_in_subset_redundancy_01() throws Exception {
        // Test redundancy elimination: field IN [1,2] is redundant if we have field IN [1,2,3]
        Expression<Fields> exp = FieldsQueryBuilder.and(
            FieldsQueryBuilder.field3(Operator.IN, new Status[] {Status.ACTIVE, Status.INACTIVE}),
            FieldsQueryBuilder.field3(Operator.IN, new Status[] {Status.ACTIVE, Status.INACTIVE, Status.OTHER})
        );
        
        IExpressionOptimizer<Fields> optimizer = new ComparisonOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Redundancy elimination: field IN [1,2] AND field IN [1,2,3] → field IN [1,2] (more restrictive)
        assertEquals("FIELD3 IN [ACTIVE,INACTIVE]", 
                     optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_in_subset_redundancy_02() throws Exception {
        // Test redundancy elimination with reversed order
        Expression<Fields> exp = FieldsQueryBuilder.and(
            FieldsQueryBuilder.field3(Operator.IN, new Status[] {Status.ACTIVE, Status.INACTIVE, Status.OTHER}),
            FieldsQueryBuilder.field3(Operator.IN, new Status[] {Status.ACTIVE, Status.INACTIVE})
        );
        
        IExpressionOptimizer<Fields> optimizer = new ComparisonOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Redundancy elimination: field IN [1,2,3] AND field IN [1,2] → field IN [1,2] (more restrictive)
        assertEquals("FIELD3 IN [ACTIVE,INACTIVE]", 
                     optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_not_in_subset_redundancy_01() throws Exception {
        // Test redundancy elimination: field NOT_IN [1,2,3] is redundant if we have field NOT_IN [1,2]
        Expression<Fields> exp = FieldsQueryBuilder.and(
            FieldsQueryBuilder.INSTANCE.term(Fields.FIELD3, Operator.NOT_IN, new Status[] {Status.ACTIVE, Status.INACTIVE, Status.OTHER}),
            FieldsQueryBuilder.INSTANCE.term(Fields.FIELD3, Operator.NOT_IN, new Status[] {Status.ACTIVE, Status.INACTIVE})
        );
        
        IExpressionOptimizer<Fields> optimizer = new ComparisonOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Redundancy elimination: field NOT_IN [1,2,3] AND field NOT_IN [1,2] → field NOT_IN [1,2,3] (more restrictive)
        assertEquals("FIELD3 NOT IN [ACTIVE,INACTIVE,OTHER]", 
                     optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_in_disjoint_contradiction() throws Exception {
        // Test contradiction detection: field IN [1,2] AND field IN [3,4] → false (disjoint sets)
        Expression<Fields> exp = FieldsQueryBuilder.and(
            FieldsQueryBuilder.field3(Operator.IN, new Status[] {Status.ACTIVE, Status.INACTIVE}),
            FieldsQueryBuilder.field3(Operator.IN, new Status[] {Status.OTHER})
        );
        
        IExpressionOptimizer<Fields> optimizer = new ComparisonOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Contradiction detection: field IN [disjoint sets] → false (null)
        assertFalse(optimized);
    }

    @Test
    public void optimization_in_not_in_contradiction_01() throws Exception {
        // Test contradiction detection: field IN [1,2] AND field NOT_IN [1,2,3] → false
        Expression<Fields> exp = FieldsQueryBuilder.and(
            FieldsQueryBuilder.field3(Operator.IN, new Status[] {Status.ACTIVE, Status.INACTIVE}),
            FieldsQueryBuilder.INSTANCE.term(Fields.FIELD3, Operator.NOT_IN, new Status[] {Status.ACTIVE, Status.INACTIVE, Status.OTHER})
        );
        
        IExpressionOptimizer<Fields> optimizer = new ComparisonOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Contradiction detection: field IN [subset] AND field NOT_IN [superset] → false (null)
        assertFalse(optimized);
    }

    @Test
    public void optimization_in_not_in_contradiction_02() throws Exception {
        // Test contradiction detection: field NOT_IN [1,2,3] AND field IN [1,2] → false
        Expression<Fields> exp = FieldsQueryBuilder.and(
            FieldsQueryBuilder.INSTANCE.term(Fields.FIELD3, Operator.NOT_IN, new Status[] {Status.ACTIVE, Status.INACTIVE, Status.OTHER}),
            FieldsQueryBuilder.field3(Operator.IN, new Status[] {Status.ACTIVE, Status.INACTIVE})
        );
        
        IExpressionOptimizer<Fields> optimizer = new ComparisonOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Contradiction detection: field NOT_IN [superset] AND field IN [subset] → false (null)
        assertFalse(optimized);
    }

    @Test
    public void optimization_in_partial_overlap_no_contradiction() throws Exception {
        // Test that partial overlap doesn't create contradiction: field IN [1,2,3] AND field IN [2,3,4] → valid
        Expression<Fields> exp = FieldsQueryBuilder.and(
            FieldsQueryBuilder.field3(Operator.IN, new Status[] {Status.ACTIVE, Status.INACTIVE, Status.OTHER}),
            FieldsQueryBuilder.field3(Operator.IN, new Status[] {Status.INACTIVE, Status.OTHER})
        );
        
        IExpressionOptimizer<Fields> optimizer = new ComparisonOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Partial overlap with subset: field IN [1,2,3] AND field IN [2,3] → field IN [2,3] (intersection)
        assertEquals("FIELD3 IN [INACTIVE,OTHER]", 
                     optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_not_in_partial_overlap_no_contradiction() throws Exception {
        // Test that NOT_IN operators with partial overlap remain unchanged (complex intersection)
        Expression<Fields> exp = FieldsQueryBuilder.and(
            FieldsQueryBuilder.INSTANCE.term(Fields.FIELD3, Operator.NOT_IN, new Status[] {Status.ACTIVE, Status.INACTIVE}),
            FieldsQueryBuilder.INSTANCE.term(Fields.FIELD3, Operator.NOT_IN, new Status[] {Status.INACTIVE, Status.OTHER})
        );
        
        IExpressionOptimizer<Fields> optimizer = new ComparisonOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // NOT_IN with partial overlap: complex case, should remain unchanged for now
        String result = optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name()));
        assertTrue(result.equals("(FIELD3 NOT IN [ACTIVE,INACTIVE] AND FIELD3 NOT IN [INACTIVE,OTHER])") ||
                   result.equals("(FIELD3 NOT IN [INACTIVE,OTHER] AND FIELD3 NOT IN [ACTIVE,INACTIVE])"),
                   "Expected unchanged expression with both terms, got: " + result);
    }

    @Test
    public void optimization_in_idempotence() throws Exception {
        // Test idempotence: field IN [1,2,3] AND field IN [1,2,3] → field IN [1,2,3]
        Expression<Fields> exp = FieldsQueryBuilder.and(
            FieldsQueryBuilder.field3(Operator.IN, new Status[] {Status.ACTIVE, Status.INACTIVE, Status.OTHER}),
            FieldsQueryBuilder.field3(Operator.IN, new Status[] {Status.ACTIVE, Status.INACTIVE, Status.OTHER})
        );
        
        IExpressionOptimizer<Fields> optimizer = new BooleanLogicOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Idempotence: field IN [same set] AND field IN [same set] → field IN [set]
        assertEquals("FIELD3 IN [ACTIVE,INACTIVE,OTHER]", 
                     optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_not_in_idempotence() throws Exception {
        // Test idempotence: field NOT_IN [1,2,3] AND field NOT_IN [1,2,3] → field NOT_IN [1,2,3]
        Expression<Fields> exp = FieldsQueryBuilder.and(
            FieldsQueryBuilder.INSTANCE.term(Fields.FIELD3, Operator.NOT_IN, new Status[] {Status.ACTIVE, Status.INACTIVE}),
            FieldsQueryBuilder.INSTANCE.term(Fields.FIELD3, Operator.NOT_IN, new Status[] {Status.ACTIVE, Status.INACTIVE})
        );
        
        IExpressionOptimizer<Fields> optimizer = new BooleanLogicOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Idempotence: field NOT_IN [same set] AND field NOT_IN [same set] → field NOT_IN [set]
        assertEquals("FIELD3 NOT IN [ACTIVE,INACTIVE]", 
                     optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    /************************************************************************
     * Tautology Tests
     ************************************************************************/

    @Test
    public void optimization_tautology_simple() throws Exception {
        // Test basic tautology: A OR NOT A → true
        ParsedExpression pexp = FilterQueryParser.parse("""
            FIELD1 = 5 OR NOT FIELD1 = 5
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new BooleanLogicOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Basic tautology: field = value OR NOT field = value → true
        assertTrueBool(optimized);
        
        // Verify the string representation is "true"
        String stringOutput = optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name()));
        assertEquals("true", stringOutput);
    }

    @Test
    public void optimization_tautology_complex() throws Exception {
        // Test complex tautology: (A AND B) OR NOT (A AND B) → true
        ParsedExpression pexp = FilterQueryParser.parse("""
            (FIELD1 = 5 AND FIELD2 = "test") OR NOT (FIELD1 = 5 AND FIELD2 = "test")
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new BooleanLogicOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Complex tautology: expression OR NOT expression → true
        assertTrueBool(optimized);
        
        // Verify the string representation is "true"
        String stringOutput = optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name()));
        assertEquals("true", stringOutput);
    }

    @Test
    public void optimization_tautology_with_other_terms() throws Exception {
        // Test tautology with other terms: (A OR NOT A) AND B → B
        ParsedExpression pexp = FilterQueryParser.parse("""
            (FIELD1 = 5 OR NOT FIELD1 = 5) AND FIELD2 = "test"
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new BooleanLogicOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Tautology elimination: true AND B → B
        String stringOutput = optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name()));
        assertEquals("FIELD2 = \"test\"", stringOutput);
    }

    /************************************************************************
     * Boolean Expression Optimizations
     ************************************************************************/

    @Test
    public void optimization_boolean_true_and_false() throws Exception {
        // Test true AND false → false
        ParsedExpression pexp = FilterQueryParser.parse("""
            true AND false
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new BooleanLogicOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Boolean evaluation: true AND false → false
        assertFalse(optimized);
        assertEquals("false", optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_boolean_true_or_false() throws Exception {
        // Test true OR false → true
        ParsedExpression pexp = FilterQueryParser.parse("""
            true OR false
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new BooleanLogicOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Boolean evaluation: true OR false → true
        assertTrueBool(optimized);
        assertEquals("true", optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_boolean_false_and_false() throws Exception {
        // Test false AND false → false
        ParsedExpression pexp = FilterQueryParser.parse("""
            false AND false
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new BooleanLogicOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Boolean evaluation: false AND false → false
        assertFalse(optimized);
        assertEquals("false", optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_boolean_true_and_true() throws Exception {
        // Test true AND true → true
        ParsedExpression pexp = FilterQueryParser.parse("""
            true AND true
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new BooleanLogicOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Boolean evaluation: true AND true → true
        assertTrueBool(optimized);
        assertEquals("true", optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_boolean_false_or_false() throws Exception {
        // Test false OR false → false
        ParsedExpression pexp = FilterQueryParser.parse("""
            false OR false
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new BooleanLogicOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Boolean evaluation: false OR false → false
        assertFalse(optimized);
        assertEquals("false", optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_boolean_true_or_true() throws Exception {
        // Test true OR true → true
        ParsedExpression pexp = FilterQueryParser.parse("""
            true OR true
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new BooleanLogicOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Boolean evaluation: true OR true → true
        assertTrueBool(optimized);
        assertEquals("true", optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_boolean_mixed_with_field() throws Exception {
        // Test mixing boolean with field expressions: true AND (FIELD1 = 5) → (FIELD1 = 5)
        ParsedExpression pexp = FilterQueryParser.parse("""
            true AND FIELD1 = 5
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new BooleanLogicOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Identity elimination: true AND A → A
        assertEquals("FIELD1 = 5", optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_boolean_false_dominates_and() throws Exception {
        // Test false dominates AND: false AND (FIELD1 = 5) → false
        ParsedExpression pexp = FilterQueryParser.parse("""
            false AND FIELD1 = 5
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new BooleanLogicOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Dominance: false AND A → false
        assertFalse(optimized);
        assertEquals("false", optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_boolean_true_dominates_or() throws Exception {
        // Test true dominates OR: true OR (FIELD1 = 5) → true
        ParsedExpression pexp = FilterQueryParser.parse("""
            true OR FIELD1 = 5
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new BooleanLogicOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Dominance: true OR A → true
        assertTrueBool(optimized);
        assertEquals("true", optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_boolean_false_identity_or() throws Exception {
        // Test false identity OR: false OR (FIELD1 = 5) → (FIELD1 = 5)
        ParsedExpression pexp = FilterQueryParser.parse("""
            false OR FIELD1 = 5
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new BooleanLogicOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Identity elimination: false OR A → A
        assertEquals("FIELD1 = 5", optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    /************************************************************************
     * NOT Pushing Optimizations
     ************************************************************************/

    @Test
    public void optimization_not_pushing_equality() throws Exception {
        // Test NOT(field = value) → field != value
        ParsedExpression pexp = FilterQueryParser.parse("""
            NOT(FIELD1 = 42)
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new NotPushingOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // NOT pushing: NOT(field = value) → field != value
        assertEquals("FIELD1 != 42", 
                     optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_not_pushing_inequality() throws Exception {
        // Test NOT(field != value) → field = value
        Expression<Fields> exp = FieldsQueryBuilder.not(
            FieldsQueryBuilder.INSTANCE.term(Fields.FIELD1, Operator.NEQ, 42)
        );
        
        IExpressionOptimizer<Fields> optimizer = new NotPushingOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // NOT pushing: NOT(field != value) → field = value
        assertEquals("FIELD1 = 42", 
                     optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_not_pushing_greater_than() throws Exception {
        // Test NOT(field > value) → field <= value
        ParsedExpression pexp = FilterQueryParser.parse("""
            NOT(FIELD1 > 42)
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new NotPushingOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // NOT pushing: NOT(field > value) → field <= value
        assertEquals("FIELD1 <= 42", 
                     optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_not_pushing_greater_than_equal() throws Exception {
        // Test NOT(field >= value) → field < value
        ParsedExpression pexp = FilterQueryParser.parse("""
            NOT(FIELD1 >= 42)
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new NotPushingOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // NOT pushing: NOT(field >= value) → field < value
        assertEquals("FIELD1 < 42", 
                     optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_not_pushing_less_than() throws Exception {
        // Test NOT(field < value) → field >= value
        ParsedExpression pexp = FilterQueryParser.parse("""
            NOT(FIELD1 < 42)
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new NotPushingOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // NOT pushing: NOT(field < value) → field >= value
        assertEquals("FIELD1 >= 42", 
                     optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_not_pushing_less_than_equal() throws Exception {
        // Test NOT(field <= value) → field > value
        ParsedExpression pexp = FilterQueryParser.parse("""
            NOT(FIELD1 <= 42)
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new NotPushingOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // NOT pushing: NOT(field <= value) → field > value
        assertEquals("FIELD1 > 42", 
                     optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_not_pushing_in() throws Exception {
        // Test NOT(field IN [values]) → field NOT IN [values]
        ParsedExpression pexp = FilterQueryParser.parse("""
            NOT(FIELD3 IN [ACTIVE,INACTIVE])
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new NotPushingOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // NOT pushing: NOT(field IN [values]) → field NOT IN [values]
        assertEquals("FIELD3 NOT IN [ACTIVE,INACTIVE]", 
                     optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_not_pushing_not_in() throws Exception {
        // Test NOT(field NOT IN [values]) → field IN [values]
        Expression<Fields> exp = FieldsQueryBuilder.not(
            FieldsQueryBuilder.INSTANCE.term(Fields.FIELD3, Operator.NOT_IN, new Status[] {Status.ACTIVE, Status.INACTIVE})
        );
        
        IExpressionOptimizer<Fields> optimizer = new NotPushingOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // NOT pushing: NOT(field NOT IN [values]) → field IN [values]
        assertEquals("FIELD3 IN [ACTIVE,INACTIVE]", 
                     optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_not_pushing_string_operations_unchanged() throws Exception {
        // Test that string operations like CONTAINS cannot be negated and remain unchanged
        Expression<Fields> exp = FieldsQueryBuilder.not(
            FieldsQueryBuilder.INSTANCE.term(Fields.FIELD2, Operator.CONTAINS, "test")
        );
        
        IExpressionOptimizer<Fields> optimizer = new NotPushingOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // String operations cannot be negated - should remain unchanged
        assertEquals("(NOT FIELD2 CONTAINS \"test\")", 
                     optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_not_pushing_nested_expressions() throws Exception {
        // Test that NOT pushing works within complex expressions
        ParsedExpression pexp = FilterQueryParser.parse("""
            FIELD1 > 10 AND NOT(FIELD2 = "test") AND NOT(FIELD3 IN [ACTIVE])
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = new NotPushingOptimizer<>();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // NOT pushing should work on both negated comparisons within the AND
        String result = optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name()));
        assertTrue(result.contains("FIELD2 != \"test\""), "Should push NOT into equality: " + result);
        assertTrue(result.contains("FIELD3 NOT IN [ACTIVE]"), "Should push NOT into IN: " + result);
        assertTrue(result.contains("FIELD1 > 10"), "Should preserve non-negated terms: " + result);
    }

    @Test
    public void optimization_not_pushing_composite() throws Exception {
        // Test NOT pushing with composite optimization
        ParsedExpression pexp = FilterQueryParser.parse("""
            NOT(FIELD1 = 42) AND NOT(FIELD1 = 42)
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = CompositeOptimizer.standard();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Should push NOT and then apply idempotence: NOT(field = 42) AND NOT(field = 42) → field != 42
        assertEquals("FIELD1 != 42", 
                     optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    /************************************************************************
     * Composite Optimizations
     ************************************************************************/

    @Test
    public void optimization_composite_01() throws Exception {
        // Test composite optimization with structural and boolean logic
        ParsedExpression pexp = FilterQueryParser.parse("""
            FIELD1 > 22 AND (FIELD1 > 22 AND (FIELD2 = "hubba" AND FIELD2 = "hubba"))
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = CompositeOptimizer.standard();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Should flatten structure and remove duplicates (order may vary)
        String result = optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name()));
        // Accept either order since AND is commutative
        assertTrue(result.equals("(FIELD1 > 22 AND FIELD2 = \"hubba\")") ||
                   result.equals("(FIELD2 = \"hubba\" AND FIELD1 > 22)"),
                   "Expected flattened expression with both terms, got: " + result);
    }

    @Test
    public void optimization_composite_02() throws Exception {
        // Test complex optimization scenario
        ParsedExpression pexp = FilterQueryParser.parse("""
            (FIELD1 > 10 AND FIELD1 > 5) OR (FIELD1 > 10 AND (FIELD2 = "test" AND FIELD2 = "test"))
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = CompositeOptimizer.aggressive();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // Should simplify redundant comparisons, remove duplicates, and apply absorption
        // (FIELD1 > 10 AND FIELD1 > 5) → FIELD1 > 10
        // (FIELD2 = "test" AND FIELD2 = "test") → FIELD2 = "test"  
        // FIELD1 > 10 OR (FIELD1 > 10 AND FIELD2 = "test") → FIELD1 > 10 (absorption)
        assertEquals("FIELD1 > 10", 
                     optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name())));
    }

    @Test
    public void optimization_no_change() throws Exception {
        // Test that already optimal expressions remain unchanged
        ParsedExpression pexp = FilterQueryParser.parse("""
            FIELD1 > 22 AND FIELD2 = "hubba"
        """);
        Expression<Fields> exp = pexp.build(FieldsQueryBuilder.stringBuilder());
        
        IExpressionOptimizer<Fields> optimizer = CompositeOptimizer.standard();
        Expression<Fields> optimized = exp.optimize(optimizer);
        
        // No optimization rules apply: expression remains unchanged (order may vary)
        String result = optimized.build(StringExpressionBuilder.<Fields>remap(f -> f.name()));
        // Accept either order since AND is commutative
        assertTrue(result.equals("(FIELD1 > 22 AND FIELD2 = \"hubba\")") ||
                   result.equals("(FIELD2 = \"hubba\" AND FIELD1 > 22)"),
                   "Expected unchanged expression with both terms, got: " + result);
    }

    /************************************************************************
     * Helper methods
     ************************************************************************/
    
    /**
     * Asserts that the expression is a false boolean literal.
     */
    private void assertFalse(ExpressionBuilder.Expression<Fields> expression) {
        assertNotNull(expression);
        assertTrue(expression instanceof ExpressionBuilder<?>.BoolExpression);
        assertEquals(false, ((ExpressionBuilder<Fields>.BoolExpression) expression).getValue());
    }

    /**
     * Asserts that the expression is a true boolean literal.
     */
    private void assertTrueBool(ExpressionBuilder.Expression<Fields> expression) {
        assertNotNull(expression);
        org.junit.jupiter.api.Assertions.assertTrue(expression instanceof ExpressionBuilder<?>.BoolExpression);
        assertEquals(true, ((ExpressionBuilder<Fields>.BoolExpression) expression).getValue());
    }

    /************************************************************************
     * Test Infrastructure
     ************************************************************************/

    enum Status {
        ACTIVE, INACTIVE, OTHER;
    }

    public static class FieldsQueryBuilder {
        
        enum Fields {
            FIELD1, FIELD2, FIELD3;
        }

        public static ExpressionBuilder<Fields> INSTANCE = new ExpressionBuilder<FieldsQueryBuilder.Fields>(FieldsQueryBuilder.Fields.class);

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