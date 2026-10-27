package com.effacy.jui.ui.client.fragments;

import java.lang.reflect.Field;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.effacy.jui.core.client.Debug;
import com.effacy.jui.core.client.test.AbstractJUITest;
import com.effacy.jui.core.client.test.DomTestNodeFactory.FakeElement;
import com.effacy.jui.core.client.util.Tribool;

public class MenuItemTest extends AbstractJUITest {

    /**
     * The {@code test-id} attribute is only rendered in test mode (which is
     * normally determined from a meta tag, not available in the fake DOM), so we
     * set that directly.
     */
    @BeforeAll
    public static void enableTestMode() throws Exception {
        testMode (Tribool.TRUE);
    }

    @AfterAll
    public static void resetTestMode() throws Exception {
        testMode (Tribool.UNDETERMINED);
    }

    private static void testMode(Tribool mode) throws Exception {
        Field field = Debug.class.getDeclaredField ("TEST_MODE");
        field.setAccessible (true);
        field.set (null, mode);
    }

    // Note that the fake DOM cannot register event handlers, so the items here are
    // not given an on-click handler (the test ID is applied to the same item
    // element that becomes clickable when one is assigned).

    @Test
    public void test_id_is_rendered_on_the_item_element() {
        build(MenuItem.$ ().label ("Edit").testId ("edit-item"))
            .child ("div.juiMenuItem", c1 -> {
                c1.exists ().attributeEquals ("test-id", "edit-item");
                c1.child ("span", c2 -> {
                    c2.exists ().textEquals ("Edit");
                });
            });
    }

    @Test
    public void no_test_id_attribute_when_not_assigned() {
        build(MenuItem.$ ().label ("Edit"))
            .child ("div.juiMenuItem", c1 -> {
                c1.exists ();
            })
            .select ("[test-id]", c1 -> {
                c1.count (0);
            });
    }

    @Test
    public void adornments_applied_through_base_fragment_survive() {
        build(MenuItem.$ ().label ("Edit").style ("custom").use (n -> ((FakeElement) n).attributes ().put ("data-x", "1")))
            .child ("div.juiMenuItem", c1 -> {
                c1.exists ().hasClass ("custom").attributeEquals ("data-x", "1");
            });
    }
}
