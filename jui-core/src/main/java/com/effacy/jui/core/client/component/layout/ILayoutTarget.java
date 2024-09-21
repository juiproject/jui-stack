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

import java.util.List;

import com.effacy.jui.core.client.component.IComponent;
import com.effacy.jui.core.client.observable.IObservable;

import elemental2.dom.Element;

/**
 * Represents something that can undergo layout by a {@link StyleLayout}.
 * 
 * @author Jeremy Buckley
 */
public interface ILayoutTarget extends IObservable {

    /**
     * Gets all the components in the target for layout.
     * 
     * @return The component to layout.
     */
    public List<IComponent> getItems();

    /**
     * Gets a given component for layout.
     * 
     * @param index
     *              the index of the component.
     * @return The component at that index (or {@code null} if there is none).
     */
    public IComponent getItemAt(int index);

    /**
     * Determines of the target has been rendered and is thus available for layout.
     * If this returns {@code true} then {@link #getLayoutTarget()} must return a
     * valid element to render components to.
     * 
     * @return {@code true} if it is.
     */
    public boolean isRendered();

    /**
     * Gets the element that the components should be rendered to for layout.
     * 
     * @return The target element.
     */
    public Element getLayoutTarget();

    /**
     * Called when the layout has started.
     */
    public void onLayoutExecuted();

    /**
     * Called when the layout has completed.
     * 
     * @param firstLayout
     *                    if that layout was the first layout performed on the
     *                    target.
     */
    public void onLayoutComplete(boolean firstLayout);

}
