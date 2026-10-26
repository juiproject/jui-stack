package com.effacy.jui.ui.client.fragments;

import org.junit.jupiter.api.Test;

import com.effacy.jui.core.client.test.AbstractJUITest;
import com.effacy.jui.core.client.test.DomTestNodeFactory.FakeElement;

public class ExpanderTest extends AbstractJUITest {

    @Test
    public void renders_a_growing_div() {
        build(Expander.$ ())
            .child ("div", c1 -> {
                c1.exists ().htmlEquals ("<div style=\"flexGrow: 1;\"></div>");
            });
    }

    @Test
    public void adornments_reach_the_root() {
        build(Expander.$ ().style ("custom").css ("color: red").use (n -> ((FakeElement) n).attributes ().put ("data-x", "1")))
            .child ("div.custom", c1 -> {
                c1.exists ().attributeEquals ("data-x", "1").htmlEquals ("<div class=\"custom\" data-x=\"1\" style=\"flexGrow: 1; color: red;\"></div>");
            });
    }

    @Test
    public void not_rendered_when_conditional_is_false() {
        build(Expander.$ ().iff (false))
            .select ("div > div", c1 -> {
                c1.count (0);
            });
    }
}
