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
 * Standard implementation of {@link IComponentLayout}.
 * 
 * @author Jeremy Buckley
 */
public class ComponentLayout implements IComponentLayout {

    /**
     * The top element of the component layout.
     */
    private Element layoutEl;

    /**
     * The top element of the component itself.
     */
    private Element componentEl;

    /**
     * Construct with a root element that is also the target.
     * 
     * @param componentEl
     *                    the top element of the component.
     */
    public ComponentLayout(Element componentEl) {
        this.layoutEl = componentEl;
        this.componentEl = componentEl;
    }

    /**
     * Construct with separate root and target elements.
     * 
     * @param layoutEl
     *                    the top element of the layout of the component rendering.
     * @param componentEl
     *                    the top element of the component.
     */
    public ComponentLayout(Element layoutEl, Element componentEl) {
        this.layoutEl = layoutEl;
        this.componentEl = componentEl;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.core.client.container.layout.IComponentLayout#getLayoutEl()
     */
    @Override
    public Element getLayoutEl() {
        return layoutEl;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.core.client.container.layout.IComponentLayout#getComponentEl()
     */
    @Override
    public Element getComponentEl() {
        return componentEl;
    }

    /**
     * To receive browser events to the rendered that rendered the code that was a
     * target for the event.
     * 
     * @param event
     *              the event.
     */
    public void onBrowserEvent(Event event) {
        // Nothing.
    }
}
