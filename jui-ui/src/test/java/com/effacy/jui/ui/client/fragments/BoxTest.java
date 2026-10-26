package com.effacy.jui.ui.client.fragments;

import org.junit.jupiter.api.Test;

import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.test.AbstractJUITest;
import com.effacy.jui.core.client.test.DomTestNodeFactory.FakeElement;

public class BoxTest extends AbstractJUITest {

    @Test
    public void layout_and_children_are_on_a_single_root() {
        build(Box.$ ().row ().gap (Length.px (4)).insert (Expander.$ ()))
            .child ("div.juiStack", c1 -> {
                c1.exists ().htmlEquals ("<div class=\"juiStack\" style=\"flexDirection: row; gap: 4px;\"><div style=\"flexGrow: 1;\"></div></div>");
            });
    }

    @Test
    public void adornments_reach_the_root() {
        build(Box.$ ().style ("custom").use (n -> ((FakeElement) n).attributes ().put ("data-x", "1")).insert (Expander.$ ()))
            .child ("div.juiStack", c1 -> {
                c1.exists ().hasClass ("custom").attributeEquals ("data-x", "1");
                c1.child ("div", c2 -> {
                    c2.exists ().htmlEquals ("<div style=\"flexGrow: 1;\"></div>");
                });
            })
            .select ("div.juiStack div.juiStack", c1 -> {
                c1.count (0);
            });
    }
}
