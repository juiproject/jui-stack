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
package com.effacy.jui.core.client.dom.renderer;

import com.effacy.jui.core.client.dom.builder.ContainerBuilder;
import com.effacy.jui.core.client.dom.builder.IDomInsertable;
import com.effacy.jui.core.client.dom.builder.RenderableBuilder;

import elemental2.dom.Element;

/**
 * Something that can be rendered.
 */
public interface IRenderable extends IDomInsertable {

    /**
     * Renders into the given element a the given index (if possible).
     * 
     * @param target
     *               the element this component should be rendered into.
     * @param index
     *               the index within the container <b>before</b> which this
     *               component will be inserted (defaults to appending to the end of
     *               the container if value is -1).
     */
    public void render(Element target, int index);

    /**
     * Default implementation is to render.
     */
    @Override
    default void insertInto(ContainerBuilder<?> parent) {
        new RenderableBuilder (this).insertInto (parent);
    }
    
}
