/*******************************************************************************
 * Copyright 2024 Jeremy Buckley
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
package com.effacy.jui.core.client.component;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.effacy.jui.core.client.GWTTest;
import com.effacy.jui.core.client.IDisposable;
import com.effacy.jui.core.client.dom.IUIEventHandler;
import com.effacy.jui.core.client.dom.UIEvent;

public class ComponentTest extends GWTTest {

    /**
     * Tests the disposal of event handlers when the component is disposed of.
     */
    @Test
    public void testEventRegistration_disposal() {
        Component<Component.Config> cpt = new Component<Component.Config> ();
        Assertions.assertTrue (cpt.uiHandlers == null);

        MockUIEventHandler handler = new MockUIEventHandler ();
        cpt.registerEventHandler (handler);
        Assertions.assertEquals (1, cpt.uiHandlers.size ());

        Assertions.assertFalse (handler.disposed);
        cpt.dispose ();
        Assertions.assertEquals (0, cpt.uiHandlers.size ());
        Assertions.assertTrue (handler.disposed);
    }

    /**
     * Tests the removal of handlers.
     */
    @Test
    public void testEventRegistration_removal() {
        Component<Component.Config> cpt = new Component<Component.Config> ();
        Assertions.assertTrue (cpt.uiHandlers == null);

        // Add handler 1 with no key.
        MockUIEventHandler handler1 = new MockUIEventHandler ();
        cpt.registerEventHandler (handler1);
        Assertions.assertEquals (1, cpt.uiHandlers.size ());
        Assertions.assertFalse (handler1.disposed);

        // Add handler 2 with no key.
        MockUIEventHandler handler2 = new MockUIEventHandler ();
        cpt.registerEventHandler (handler2);
        Assertions.assertEquals (2, cpt.uiHandlers.size ());
        Assertions.assertFalse (handler2.disposed);

        // Remove handler 2.
        cpt.removeEventHandler (handler2);
        Assertions.assertEquals (1, cpt.uiHandlers.size ());
        Assertions.assertFalse (handler1.disposed);
        Assertions.assertTrue (handler2.disposed);

        // Remove handler 1.
        cpt.removeEventHandler (handler1);
        Assertions.assertEquals (0, cpt.uiHandlers.size ());
        Assertions.assertTrue (handler1.disposed);
        Assertions.assertTrue (handler2.disposed);
    }

    /**
     * Tests the registration of handlers with a key.
     */
    @Test
    public void testEventRegistration_replacement1() {
        Component<Component.Config> cpt = new Component<Component.Config> ();
        Assertions.assertTrue (cpt.uiHandlers == null);

        // Add handler 1 with key.
        MockUIEventHandler handler1 = new MockUIEventHandler ();
        cpt.registerEventHandler (handler1, "hubba");
        Assertions.assertEquals (1, cpt.uiHandlers.size ());
        Assertions.assertFalse (handler1.disposed);

        // Add handler 2 with the same key. First handler should be removed and disposed
        // of.
        MockUIEventHandler handler2 = new MockUIEventHandler ();
        cpt.registerEventHandler (handler2, "hubba");
        Assertions.assertEquals (1, cpt.uiHandlers.size ());
        Assertions.assertTrue (handler1.disposed);
        Assertions.assertFalse (handler2.disposed);

        // Test the equality mechanism for the wrapper.
        Assertions.assertFalse (cpt.uiHandlers.get (0).equals (handler1));
        Assertions.assertTrue (cpt.uiHandlers.get (0).equals (handler2));
        
        // Remove the first handler (no effect).
        cpt.removeEventHandler (handler1);
        Assertions.assertEquals (1, cpt.uiHandlers.size ());
        Assertions.assertTrue (handler1.disposed);
        Assertions.assertFalse (handler2.disposed);
        
        // Remove the last handler.
        cpt.removeEventHandler (handler2);
        Assertions.assertEquals (0, cpt.uiHandlers.size ());
        Assertions.assertTrue (handler1.disposed);
        Assertions.assertTrue (handler2.disposed);
    }

    /**
     * Tests the registration of handlers with a key.
     */
    @Test
    public void testEventRegistration_replacement2() {
        Component<Component.Config> cpt = new Component<Component.Config> ();
        Assertions.assertTrue (cpt.uiHandlers == null);

        // Add handler 1 with key.
        MockUIEventHandler handler1 = new MockUIEventHandler ();
        cpt.registerEventHandler (handler1, "hubba1");
        Assertions.assertEquals (1, cpt.uiHandlers.size ());
        Assertions.assertFalse (handler1.disposed);

        // Add handler 2 with different key.
        MockUIEventHandler handler2 = new MockUIEventHandler ();
        cpt.registerEventHandler (handler2, "hubba2");
        Assertions.assertEquals (2, cpt.uiHandlers.size ());
        Assertions.assertFalse (handler1.disposed);
        Assertions.assertFalse (handler2.disposed);

        // Add handler 3 with same key as 1.
        MockUIEventHandler handler3 = new MockUIEventHandler ();
        cpt.registerEventHandler (handler3, "hubba1");
        Assertions.assertEquals (2, cpt.uiHandlers.size ());
        Assertions.assertTrue (handler1.disposed);
        Assertions.assertFalse (handler2.disposed);
        Assertions.assertFalse (handler3.disposed);
    }

    /**
     * Mock UI event handler.
     */
    class MockUIEventHandler implements IUIEventHandler, IDisposable {

        boolean disposed;

        @Override
        public boolean handleEvent(UIEvent event) {
            return false;
        }

        @Override
        public void dispose() {
            disposed = true;
        }

        public void reset() {
            disposed = false;
        }
    }
}
