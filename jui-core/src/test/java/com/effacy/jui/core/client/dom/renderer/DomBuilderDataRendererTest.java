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
package com.effacy.jui.core.client.dom.renderer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.effacy.jui.core.client.dom.IUIEventHandler;
import com.effacy.jui.core.client.dom.builder.DomBuilder;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.test.AbstractJUITest;
import com.effacy.jui.core.client.test.DomTest;
import com.effacy.jui.core.client.test.GWTTestBridge;
import com.effacy.jui.core.client.test.DomTestNodeFactory.FakeElement;

/**
 * Tests {@link DomBuilderDataRenderer} through both of its construction paths:
 * with a generator, and as a subclass (with no generator) that overrides
 * {@link DomBuilderDataRenderer#generate(ElementBuilder, Object)}.
 */
public class DomBuilderDataRendererTest extends AbstractJUITest {

    @Test
    public void generator() {
        DomBuilderDataRenderer<String> renderer = new DomBuilderDataRenderer<> (data -> DomBuilder.span (s -> s.text (data)).build ());
        FakeElement el = render (renderer, "Hello");
        DomTest.on (el).child ("span", c -> c.exists ().textEquals ("Hello"));
    }

    @Test
    public void subclass_generate() {
        DomBuilderDataRenderer<String> renderer = new DomBuilderDataRenderer<> () {

            @Override
            protected void generate(ElementBuilder el, String data) {
                Span.$ (el).text (data);
            }
        };
        FakeElement el = render (renderer, "World");
        DomTest.on (el).child ("span", c -> c.exists ().textEquals ("World"));
    }

    /**
     * Renders the data into a fresh element.
     */
    protected FakeElement render(DomBuilderDataRenderer<String> renderer, String data) {
        GWTTestBridge.init ();
        FakeElement el = root ("div");
        IUIEventHandler handler = renderer.render (el, data);
        Assertions.assertNotNull (handler);
        return el;
    }
}
