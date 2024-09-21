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

import com.effacy.jui.core.client.component.IComponent;

import elemental2.dom.Element;

/**
 * A layout wrapper renderer a component plus some additional adornments to that
 * component.
 * 
 * @author Jeremy Buckley
 */
public interface ILayoutRenderer {

    /**
     * Renders a single component into a given target location at a given index.
     * If the component has already been rendered, then it should be moved to
     * the new location, otherwise it should be rendered directly.
     * 
     * @param component
     *            the component to render.
     * @param index
     *            the index of the component as the child of the container.
     * @param target
     *            the target element to render the component into.
     * @param size
     *            the total number of items.
     * @return The inserted element.
     */
    public IComponentLayout renderComponent(IComponent component, int index, Element target, int size);

}
