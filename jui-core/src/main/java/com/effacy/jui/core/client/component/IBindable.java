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

import elemental2.dom.Element;

/**
 * Something that can bind to an element in the DOM referenced by ID.
 */
public interface IBindable {
    
    /**
     * See {@link #bind(String, boolean)} but configured to clear the element of all
     * its children by default.
     */
    default public Element bind(String elementId) {
        return bind(elementId, true);
    }

    /**
     * Binds the component to the given element specified by it's ID.
     * 
     * @param elementId
     *                  the ID of the element.
     * @param clear
     *                  {@code true} if to clear the element of all its children.
     * @return the element being bound to.
     */
    public Element bind(String elementId, boolean clear);
}
