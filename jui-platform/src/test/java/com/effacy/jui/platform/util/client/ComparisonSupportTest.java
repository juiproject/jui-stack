package com.effacy.jui.platform.util.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ComparisonSupportTest {

    @Test
    public void compare() {
        Assertions.assertTrue(ComparisonSupport.compare("aaa", "bbb") > 0);
        Assertions.assertTrue(ComparisonSupport.compare("aaa", "aaa") == 0);
        Assertions.assertTrue(ComparisonSupport.compare("bbb", "aaa") < 0);

        Assertions.assertTrue(ComparisonSupport.compare("", "bbb") > 0);
        Assertions.assertTrue(ComparisonSupport.compare("  ", "bbb") > 0);
        Assertions.assertTrue(ComparisonSupport.compare(null, "bbb") > 0);

        Assertions.assertTrue(ComparisonSupport.compare("bbb", "") < 0);
        Assertions.assertTrue(ComparisonSupport.compare("bbb", "  ") < 0);
        Assertions.assertTrue(ComparisonSupport.compare("bbb", null) < 0);

        Assertions.assertTrue(ComparisonSupport.compare("", "") == 0);
        Assertions.assertTrue(ComparisonSupport.compare("   ", "  ") == 0);
        Assertions.assertTrue(ComparisonSupport.compare(null, "") == 0);
        Assertions.assertTrue(ComparisonSupport.compare("", null) == 0);
        Assertions.assertTrue(ComparisonSupport.compare(null, null) == 0);

        List<String> list = new ArrayList<>();
        list.add ("bbb");
        list.add ("ddd");
        list.add ("");
        list.add ("aaa");
        Collections.sort(list, (a,b) -> ComparisonSupport.compare(b, a));
        Assertions.assertEquals("", list.get(0));
        Assertions.assertEquals("aaa", list.get(1));
        Assertions.assertEquals("bbb", list.get(2));
        Assertions.assertEquals("ddd", list.get(3));
    }
}
