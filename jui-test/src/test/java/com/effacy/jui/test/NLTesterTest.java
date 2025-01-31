package com.effacy.jui.test;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;

import com.effacy.jui.test.NLTester.Processor;

public class NLTesterTest {

    @org.junit.jupiter.api.Test
    public void internal_matcher() {
        Processor<Object> p;
        Optional<Map<String,String>> r;

        p = new Processor<Object>("The sum of {n1} and {n2} may be {n3}", (a,b) -> {});
        r = p.matchAgainstTemplate("The sum of 22 and 24 may be 46");
        Assertions.assertTrue(r.isPresent());
        Assertions.assertEquals(3, r.get().size());
        Assertions.assertEquals("22", r.get().get("n1"));
        Assertions.assertEquals("24", r.get().get("n2"));
        Assertions.assertEquals("46", r.get().get("n3"));

        p = new Processor<Object>("The sum of {n1} and {n2} may be {n3}", (a,b) -> {});
        r = p.matchAgainstTemplate("the sum of 22 and 24 mAy bE 46");
        Assertions.assertTrue(r.isPresent());
        Assertions.assertEquals(3, r.get().size());
        Assertions.assertEquals("22", r.get().get("n1"));
        Assertions.assertEquals("24", r.get().get("n2"));
        Assertions.assertEquals("46", r.get().get("n3"));

        p = new Processor<Object>("The sum of {n1} and {n2} may be {n3}", (a,b) -> {});
        r = p.matchAgainstTemplate("The sum of 22 and 24 is 46");
        Assertions.assertFalse(r.isPresent());

        p = new Processor<Object>("Assume that {name} is logged in, then their id is {id}", (a,b) -> {});
        r = p.matchAgainstTemplate("Assume that John Williams is logged in, then their id is 23");
        Assertions.assertTrue(r.isPresent());
        Assertions.assertEquals(2, r.get().size());
        Assertions.assertEquals("John Williams", r.get().get("name"));
        Assertions.assertEquals("23", r.get().get("id"));
    }

    @org.junit.jupiter.api.Test
    public void internal_tests() {
        var tester = new NLTester<Item>()
            .register("The first value is {value}", (v,p) -> {
                v.is(w -> w.value1(), p.get("value"));
            })
            .register("The second value is {value}", (v,p) -> {
                v.is(w -> w.value2(), p.get("value"));
            })
            .register("The last value is {value}", (v,p) -> {
                v.is(w -> w.value3(), p.get("value"));
            });

            tester.test(Test.$ (new Item("hubba", "bubba", "wibble")), """
              The first value is hubba
              The second value is bubba
              The last value is wibble
            """);

            try {
                tester.test(Test.$ (new Item("hubba", "bubba", "wibble")), """
                The first value is hubba
                The second value is bubbaa
                The last value is wibble
                """);
                Assertions.fail("Expected to find an assertion error");
            } catch (AssertionError e) {
                Assertions.assertTrue(e.getMessage().startsWith("At line 2"));    
            }

            try {
                tester.test(Test.$ (new Item("hubba", "bubba", "wibble")), """
                The first value is hubba
                The second value is bubba
                The last value is wibbles
                """);
                Assertions.fail("Expected to find an assertion error");
            } catch (AssertionError e) {
                Assertions.assertTrue(e.getMessage().startsWith("At line 3"));
            }
    }

    @org.junit.jupiter.api.Test
    public void internal_tests_notWellFormed() {
        var tester = new NLTester<Item>()
            .register("The first value is {value}", (v,p) -> {
                v.is(w -> w.value1(), p.get("value"));
            })
            .register("The second value is {value}", (v,p) -> {
                v.is(w -> w.value2(), p.get("value"));
            })
            .register("The last value is {value}", (v,p) -> {
                v.is(w -> w.value3(), p.get("value"));
            });

            try {
                tester.test(Test.$ (new Item("hubba", "bubba", "wibble")), """
                The first value is hubba
                The second value is bubba
                The third value is wibble
                """);
                Assertions.fail("Expected to find an assertion error");
            } catch (AssertionError e) {
                Assertions.assertEquals("There was no registered action for the test expressed on line 3", e.getMessage());
            }
    }

    public record Item(String value1, String value2, String value3) {}
}
