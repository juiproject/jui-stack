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
package com.effacy.jui.core.client.component.layout;


import elemental2.dom.Element;
import elemental2.dom.Event;

/**
 * This captures the result of a layout of a component. A layout will, in
 * general, wrap each component in HTML that is specific to that layout and
 * provide a target to render the component into (this is exclusive of any HTML
 * that supports the entire layout as one). This captures the top-most level and
 * target as well as allows browser events that occur within the component
 * layout HTML to be handled by the layout.
 * 
 * @author Jeremy Buckley
 */
public interface IComponentLayout {

    /**
     * Obtains the root element of the layout. This is the top element in the
     * layout of the component.
     * 
     * @return The element.
     */
    public Element getLayoutEl();


    /**
     * Gets the root element of the component.
     * 
     * @return The element.
     */
    public Element getComponentEl();


    /**
     * To receive browser events to the rendered that rendered the code that was
     * a target for the event.
     * 
     * @param event
     *            the event.
     */
    public void onBrowserEvent(Event event);
}
