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
 * A very simple layout that does not adorn any of the components being placed.
 */
public class MinimalLayout extends Layout {

    /**
     * Convenience to construct layout configuration.
     * 
     * @return the configuration.
     */
    public static MinimalLayout.Config config() {
        return new MinimalLayout.Config ();
    }

    /**
     * Configuration for the layout.
     */
    public static class Config {

        /**
         * Construct an instance of the layout using this configuration.
         * 
         * @return the instance
         */
        public MinimalLayout build() {
            return new MinimalLayout (this);
        }
    }

    /**
     * The layout configuration.
     */
    private MinimalLayout.Config config;

    /**
     * Construct with configuration.
     * 
     * @param config
     *               the configuration.
     */
    public MinimalLayout(MinimalLayout.Config config) {
        this.config = config;
    }
    
    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.layout.Layout#renderComponent(com.effacy.jui.core.client.component.IComponent,
     *      int, elemental2.dom.Element, int)
     */
    @Override
    protected void renderComponent(IComponent component, int index, Element target, int size) {
        component.render (target, index);
    }
}
