package com.effacy.jui.ui.client.fragments;

import org.junit.jupiter.api.Test;

import com.effacy.jui.core.client.test.AbstractJUITest;
import com.effacy.jui.core.client.test.DomTestNodeFactory.FakeElement;

/**
 * Regression test: {@link Stack} overrides {@code build} but delegates to the
 * base implementation, so its adornments must reach the root.
 */
public class StackTest extends AbstractJUITest {

    @Test
    public void adornments_reach_the_root() {
        build(Stack.$ ().style ("custom").css ("color: red").use (n -> ((FakeElement) n).attributes ().put ("data-x", "1")).insert (Expander.$ ()))
            .child ("div.juiStack", c1 -> {
                c1.exists ().hasClass ("custom").attributeEquals ("data-x", "1");
                c1.child ("div", c2 -> {
                    c2.exists ();
                });
            });
    }

    @Test
    public void adornments_not_applied_when_hidden_if_empty() {
        build(Stack.$ ().hideIfEmpty ().style ("custom"))
            .select ("div.juiStack", c1 -> {
                c1.count (0);
            });
    }
}
