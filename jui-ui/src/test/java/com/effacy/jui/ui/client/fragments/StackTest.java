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

    @Test
    public void use_runs_once() {
        int[] count = { 0 };
        build(Stack.$ ().use (n -> count[0]++));
        org.junit.jupiter.api.Assertions.assertEquals (1, count[0]);
    }

    @Test
    public void nothing_rendered_when_conditional_is_false() {
        build(Stack.$ ().iff (false).insert (Expander.$ ()))
            .select ("div > div", c1 -> {
                c1.count (0);
            });
    }

    @Test
    public void rendered_when_conditional_is_true() {
        build(Stack.$ ().iff (true).insert (Expander.$ ()))
            .child ("div.juiStack", c1 -> {
                c1.exists ();
            });
    }

    @Test
    public void hide_if_empty_still_applies_with_conditional_true() {
        build(Stack.$ ().iff (true).hideIfEmpty ())
            .select ("div > div", c1 -> {
                c1.count (0);
            });
        build(Stack.$ ().iff (true).hideIfEmpty ().insert (Expander.$ ()))
            .child ("div.juiStack", c1 -> {
                c1.exists ();
            });
    }
}
