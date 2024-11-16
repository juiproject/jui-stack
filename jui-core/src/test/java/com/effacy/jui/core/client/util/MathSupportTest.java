package com.effacy.jui.core.client.util;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MathSupportTest {

    @Test
    public void summary_stats_01() {
        List<Number> values = Arrays.asList(5, 7);
        MathSupport.SummaryStatistics stats = MathSupport.summaryStatistics(values);
        Assertions.assertEquals(2, stats.n());
        Assertions.assertEquals(6.0, stats.median());
        Assertions.assertEquals(5.5, stats.q1());
        Assertions.assertEquals(6.5, stats.q3());
        Assertions.assertEquals(6.0, stats.average());
    }

    @Test
    public void summary_stats_02() {
        List<Number> values = Arrays.asList(1, 1, 10);
        MathSupport.SummaryStatistics stats = MathSupport.summaryStatistics(values);
        Assertions.assertEquals(3, stats.n());
        Assertions.assertEquals(1.0, stats.median());
        Assertions.assertEquals(1, stats.q1());
        Assertions.assertEquals(5.5, stats.q3());
        Assertions.assertEquals(4.0, stats.average());
    }

    @Test
    public void bin_01() {
        List<Number> values = Arrays.asList(1, 1, 10);
        MathSupport.BinStatistics stats = MathSupport.bin(values, 1.0, 1.0, 20);

        Assertions.assertEquals(3, stats.n());

        MathSupport.Bin[] bins = stats.data();
        Assertions.assertEquals(20, bins.length);
        
        Assertions.assertEquals(1.0, bins[0].left());
        Assertions.assertEquals(2.0, bins[0].right());
        Assertions.assertEquals(2.0, bins[1].left());
        Assertions.assertEquals(3.0, bins[1].right());

        Assertions.assertEquals(2, bins[0].n());
        Assertions.assertEquals(0, bins[1].n());
        Assertions.assertEquals(0, bins[2].n());
        Assertions.assertEquals(0, bins[3].n());
        Assertions.assertEquals(0, bins[4].n());
        Assertions.assertEquals(0, bins[5].n());
        Assertions.assertEquals(0, bins[6].n());
        Assertions.assertEquals(0, bins[7].n());
        Assertions.assertEquals(0, bins[8].n());
        Assertions.assertEquals(1, bins[9].n());
        Assertions.assertEquals(0, bins[10].n());
    }
}
