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
package com.effacy.jui.core.client.dom.renderer.template;

import com.effacy.jui.core.client.dom.IUIEventHandler;
import com.effacy.jui.core.client.dom.renderer.CachedDataRenderer;
import com.effacy.jui.core.client.dom.renderer.IDataRenderer;
import com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Container;

import elemental2.dom.Element;

/**
 * Something that is able to build a template (into a {@link Container}).
 * <p>
 * Also provides a number of support methods to construct renderers (including
 * caching).
 *
 * @author Jeremy Buckley
 */
@FunctionalInterface
public interface ITemplateBuilder<D> {

    /**
     * Builds into the passed container.
     * 
     * @param container
     *                  the container to build into.
     */
    public void build(Container<D> container);

    /**
     * Creates a renderer from a {@link ITemplateBuilder} that builds a template
     * structure used to undergird the renderer.
     * 
     * @param <A>
     *                the data type the template will process against.
     * @param builder
     *                passed a container this will build the template into that
     *                container.
     * @return the associated renderer.
     */
    public static <A> IDataRenderer<A> renderer(ITemplateBuilder<A> builder) {
        // Create a deferred renderer that will build the renderer only when first
        // requested to do so.
        return new IDataRenderer<A> () {

            private IDataRenderer<A> renderer;

            @Override
            public IUIEventHandler render(Element el, A data) {
                if (renderer == null) {
                    Container<A> container = new Container<A> ();
                    if (builder != null)
                        builder.build (container);
                    renderer = container.renderer ();
                }
                return renderer.render (el, data);
            }

        };
    }

    /**
     * See {@link #renderer(ITemplateBuilder)} but creates a cached version under
     * the given cache key.
     * <p>
     * In this case the builder will only be invoked once for the given cache key
     * (so using this multiple times with the same cache key will only result in the
     * template being build once).
     * 
     * @param <A>
     *                 the data type the template will process against.
     * @param cacheKey
     *                 the key to use to cache under (if {@code null} then no
     *                 caching will be performed).
     * @param builder
     *                 passed a container this will build the template into that
     *                 container.
     * @return the associated renderer.
     */
    public static <A> IDataRenderer<A> renderer(String cacheKey, ITemplateBuilder<A> builder) {
        if (cacheKey == null)
            return renderer (builder);
        return new CachedDataRenderer<A> (cacheKey) {

            /**
             * {@inheritDoc}
             *
             * @see com.effacy.jui.core.client.dom.renderer.CachedDataRenderer#buildRenderer()
             */
            @Override
            protected IDataRenderer<A> buildRenderer() {
                return ITemplateBuilder.renderer (builder);
            }

        };
    }
}
