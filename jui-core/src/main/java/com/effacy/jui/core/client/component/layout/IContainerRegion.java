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

/**
 * A container region is a region of a container that is able to hold
 * components.
 * 
 * @author Jeremy Buckley
 */
public interface IContainerRegion extends ILayoutTarget, Iterable<IComponent> {

    /**
     * Sets the layout. This will remove the current layout (if set) and
     * establish the new layout. The layout will be invoked after being set (for
     * the case where the container is already rendered).
     * 
     * @param layout
     *            the layout to set.
     */
    public void setLayout(ILayout layout);


    /**
     * Gets the declared layout.
     * 
     * @return The layout.
     */
    public ILayout getLayout();


    /**
     * Returns the index of the item.
     * 
     * @param item
     *            the item to get the index of.
     * @return The index of the item (or -1 if not found).
     */
    public int indexOf(IComponent component);


    /**
     * Adds the passed component.
     * 
     * @param <C>
     *            the component type.
     * @param component
     *            the component.
     * @return The passed component returned back.
     */
    public <D extends IComponent> D add(D component);


    /**
     * Adds the passed component.
     * 
     * @param <C>
     *            the component type.
     * @param component
     *            the component.
     * @return The passed component returned back.
     */
    public <D extends IComponent> D add(D component, LayoutData layoutData);


    /**
     * Inserts a component into the container.
     * 
     * @param component
     *            the component to insert.
     * @param index
     *            the index into the list of components (this will be adjusted
     *            modulo the size of the list, so -2 means second from the end).
     * @param layoutData
     *            layout data to apply to the component.
     */
    public boolean insert(IComponent component, int index, LayoutData layoutData);


    /**
     * Disposes of all the children in the regions.
     */
    public void disposeAll();


    /**
     * Perform a layout. Should not be called directly, but by a layout
     * implementation. Call {@link #layout()} instead.
     * 
     * @param force
     *            if the layout should be forced.
     * @return If the layout was performed.
     */
    public boolean layout(boolean force);

}
