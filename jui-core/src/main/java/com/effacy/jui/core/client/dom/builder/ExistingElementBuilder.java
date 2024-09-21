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
package com.effacy.jui.core.client.dom.builder;

import com.effacy.jui.core.client.dom.DomSupport;

import elemental2.dom.Element;
import elemental2.dom.Node;

/**
 * Used to wrap an existing element.
 */
public class ExistingElementBuilder extends ElementBuilder {

    /**
     * The existing element.
     */
    private Element existing;

    /**
     * Construct with the element to wrap.
     * 
     * @param existing
     *                 the element to wrap.
     */
    public ExistingElementBuilder(Element existing) {
        super (null);
        this.existing = existing;
    }

    @Override
    protected Node _nodeImpl(Node parent, BuildContext ctx) {
        return super._nodeImpl (existing, ctx);
    }

    /**
     * Clears all the nodes in the existing element.
     * 
     * @return this builder instance.
     */
    public ExistingElementBuilder clear() {
        DomSupport.removeAllChildren (existing);
        return this;
    }

}
