/*******************************************************************************
 * Copyright 2026 Jeremy Buckley
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * <a href= "http://www.apache.org/licenses/LICENSE-2.0">Apache License v2</a>
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.effacy.jui.core.client.dom.builder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.effacy.jui.core.client.test.AbstractJUITest;
import com.effacy.jui.core.client.test.DomTestNodeFactory.FakeInput;

/**
 * Tests the application of the boolean {@code disabled} and {@code checked}
 * attributes by {@link ElementBuilder}: these are set as properties on any
 * element (not only an input) and any value other than {@code "false"} means
 * set.
 */
public class ElementBuilderBooleanAttributeTest extends AbstractJUITest {

    @Test
    public void disabled_on_button() {
        build(Button.$().disabled(true))
            .child("button", c -> c.exists().propertyEquals("disabled", true));
    }

    @Test
    public void checked_on_button() {
        build(Button.$().checked(true))
            .child("button", c -> c.exists().propertyEquals("checked", true));
    }

    @Test
    public void disabled_false_on_button() {
        build(Button.$().disabled(false))
            .child("button", c -> c.exists().propertyEquals("disabled", null));
    }

    @Test
    public void checked_false_on_button() {
        build(Button.$().checked(false))
            .child("button", c -> c.exists().propertyEquals("checked", null));
    }

    @Test
    public void any_present_value_on_button() {
        for (String value : new String[] { "disabled", "checked", "true", "TRUE", "" }) {
            build(Button.$().attr("disabled", value).attr("checked", value))
                .child("button", c -> c.exists()
                    .propertyEquals("disabled", true)
                    .propertyEquals("checked", true));
        }
    }

    @Test
    public void false_value_on_button() {
        for (String value : new String[] { "false", "FALSE", " false " }) {
            build(Button.$().attr("disabled", value).attr("checked", value))
                .child("button", c -> c.exists()
                    .propertyEquals("disabled", null)
                    .propertyEquals("checked", null));
        }
    }

    @Test
    public void disabled_and_checked_on_input() {
        FakeInput input = (FakeInput) build(Input.$("checkbox").disabled(true).checked(true)).element().children().get(0);
        Assertions.assertTrue(input.disabled);
        Assertions.assertTrue(input.checked);
    }

    @Test
    public void booleanAttribute() {
        Assertions.assertTrue(ElementBuilder.booleanAttribute(""));
        Assertions.assertTrue(ElementBuilder.booleanAttribute("disabled"));
        Assertions.assertTrue(ElementBuilder.booleanAttribute("checked"));
        Assertions.assertTrue(ElementBuilder.booleanAttribute("true"));
        Assertions.assertFalse(ElementBuilder.booleanAttribute(null));
        Assertions.assertFalse(ElementBuilder.booleanAttribute("false"));
        Assertions.assertFalse(ElementBuilder.booleanAttribute("False"));
    }
}
