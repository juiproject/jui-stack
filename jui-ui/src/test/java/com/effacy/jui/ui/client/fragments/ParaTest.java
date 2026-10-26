package com.effacy.jui.ui.client.fragments;

import org.junit.jupiter.api.Test;

import com.effacy.jui.core.client.test.AbstractJUITest;
import com.effacy.jui.core.client.test.DomTestNodeFactory.FakeElement;

public class ParaTest extends AbstractJUITest {

    @Test
    public void renders_paragraphs_and_line_breaks() {
        build(Para.$ ("one\ntwo\n\nthree"))
            .select ("p", c1 -> {
                c1.count (2);
            })
            .select ("br", c1 -> {
                c1.count (1);
            });
    }

    @Test
    public void adornments_are_applied_to_each_paragraph() {
        build(Para.$ ("one\n\ntwo").style ("custom").use (n -> ((FakeElement) n).attributes ().put ("data-x", "1")))
            .select ("p", c1 -> {
                c1.count (2);
            })
            .select ("p.custom[data-x]", c1 -> {
                c1.count (2);
            });
    }

    @Test
    public void css_is_applied_to_each_paragraph() {
        build(Para.$ ("one\n\ntwo").css ("color: red"))
            .htmlEquals ("""
                <div>
                    <p style="color: red;">one</p>
                    <p style="color: red;">two</p>
                </div>
                """);
    }

    @Test
    public void nothing_rendered_when_conditional_is_false() {
        build(Para.$ ("one").iff (false))
            .select ("p", c1 -> {
                c1.count (0);
            });
    }
}
