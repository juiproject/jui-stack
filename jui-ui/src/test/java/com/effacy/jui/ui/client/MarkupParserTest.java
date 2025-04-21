package com.effacy.jui.ui.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.effacy.jui.ui.client.MarkupParser.Block;

public class MarkupParserTest {

    @Test
    public void parse() {
        var results = MarkupParser.parse("hubba bubba");
        Assertions.assertEquals(1, results.size());
        assertBlock(results.get(0), false, false, "hubba bubba");

        results = MarkupParser.parse("*hubba* bubba");
        Assertions.assertEquals(2, results.size());
        assertBlock(results.get(0), true, false, "hubba");
        assertBlock(results.get(1), false, false, " bubba");

        results = MarkupParser.parse("*hubba* *bubba*");
        Assertions.assertEquals(3, results.size());
        assertBlock(results.get(0), true, false, "hubba");
        assertBlock(results.get(1), false, false, " ");
        assertBlock(results.get(2), true, false, "bubba");

        results = MarkupParser.parse("*hubba* **bubba**");
        Assertions.assertEquals(3, results.size());
        assertBlock(results.get(0), true, false, "hubba");
        assertBlock(results.get(1), false, false, " ");
        assertBlock(results.get(2), false, true, "bubba");

        results = MarkupParser.parse("hubba **bubba**");
        Assertions.assertEquals(2, results.size());
        assertBlock(results.get(0), false, false, "hubba ");
        assertBlock(results.get(1), false, true, "bubba");

        results = MarkupParser.parse("*a**b***");
        Assertions.assertEquals(2, results.size());
        assertBlock(results.get(0), true, false, "a");
        assertBlock(results.get(1), true, true, "b");

        results = MarkupParser.parse("*hubba **bubba***");
        Assertions.assertEquals(2, results.size());
        assertBlock(results.get(0), true, false, "hubba ");
        assertBlock(results.get(1), true, true, "bubba");

        results = MarkupParser.parse("hubba *bubba");
        Assertions.assertEquals(2, results.size());
        assertBlock(results.get(0), false, false, "hubba ");
        assertBlock(results.get(1), true, false, "bubba");

        results = MarkupParser.parse("hubba **bubba");
        Assertions.assertEquals(2, results.size());
        assertBlock(results.get(0), false, false, "hubba ");
        assertBlock(results.get(1), false, true, "bubba");

        results = MarkupParser.parse("***bubba***");
        Assertions.assertEquals(1, results.size());
        assertBlock(results.get(0), true, true, "bubba");

        results = MarkupParser.parse("***bubba**");
        Assertions.assertEquals(1, results.size());
        assertBlock(results.get(0), true, true, "bubba");

        results = MarkupParser.parse("***bubba*");
        Assertions.assertEquals(1, results.size());
        assertBlock(results.get(0), true, true, "bubba");

        results = MarkupParser.parse("***bubba");
        Assertions.assertEquals(1, results.size());
        assertBlock(results.get(0), true, true, "bubba");
    }

    public void assertBlock(Block block, boolean bold, boolean italic, String content) {
        Assertions.assertEquals(bold, block.bold());
        Assertions.assertEquals(italic, block.italic());
        Assertions.assertEquals(!bold && !italic, block.isPlain());
        if (content != null)
            Assertions.assertEquals(content, block.text());
    }
}
