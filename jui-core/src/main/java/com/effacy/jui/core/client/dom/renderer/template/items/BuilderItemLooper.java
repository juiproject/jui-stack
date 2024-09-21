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
package com.effacy.jui.core.client.dom.renderer.template.items;

import java.util.List;

import com.effacy.jui.core.client.dom.renderer.template.Provider;
import com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Container;
import com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Node;

/**
 * A {@link BuilderItem} that supports looping. This wraps an internal
 * {@link BuilderItem} typed for the type of the loop. This builder and a
 * provider that generates a list of the underlying items to loop over are
 * provided during construction.
 */
public class BuilderItemLooper<W, V> extends BuilderItem<V> {

    /**
     * See constructor.
     */
    private BuilderItem<W> builder;

    /**
     * See constructor.
     */
    private Provider<List<W>, V> looper;

    /**
     * Construct with a builder for the items provided by the looper.
     * 
     * @param builder
     *                the builder that takes loop items.
     * @param looper
     *                a provider that provides a list if items to loop over.
     */
    public <B extends BuilderItem<W>> BuilderItemLooper(B builder, Provider<List<W>, V> looper) {
        this.builder = builder;
        this.looper = looper;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.dom.renderer.template.items.BuilderItem#buildImpl(com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Container)
     */
    @Override
    protected Node<V> buildImpl(Container<V> parent) {
        builder.build (parent.container ().loop (looper));
        return parent;
    }

}
