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

import java.util.ArrayList;
import java.util.List;

import com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Container;
import com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Node;

/**
 * A collection of builder items. This will be built one after the other into
 * the parent.
 */
public class BuilderItemContainer<A> extends BuilderItem<A> {

    /**
     * The items held in the collection.
     */
    protected List<BuilderItem<A>> items = new ArrayList<> ();

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.dom.BuilderItem#buildImpl(com.effacy.jui.core.client.dom.renderer.DomTemplateDataRenderer.Container)
     */
    @Override
    public Node<A> buildImpl(Container<A> parent) {
        for (BuilderItem<A> item : items)
            item.build (parent);
        return parent;
    }

}
