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

import java.util.function.Function;
import java.util.function.Supplier;

import com.effacy.jui.core.client.dom.IUIEventHandler;
import com.effacy.jui.core.client.dom.builder.ContainerBuilder;
import com.effacy.jui.core.client.dom.builder.DomBuilder;
import com.effacy.jui.core.client.dom.builder.IDomInsertable;
import com.effacy.jui.core.client.dom.builder.RendererBuilder;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.Node;

/**
 * A simple (non-data) renderer.
 *
 * @author Jeremy Buckley
 */
public interface IRenderer extends IDomInsertable {

    /**
     * Renders into the passed element.
     * 
     * @param el
     *           the element to render into.
     * @return (optional) a UI event handler for use.
     */
    public IUIEventHandler render(Element el);

    /**
     * Default implementation is to render.
     */
    @Override
    default void insertInto(ContainerBuilder<?> parent) {
        new RendererBuilder (this).insertInto (parent);
    }

    /**
     * Convenience to convert a data renderer to a renderer where data passed is
     * {@code null}.
     * 
     * @param renderer
     *                 the data renderer to convert.
     * @return a renderer that calls the data renderer with {@code null} data.
     */
    public static <D> IRenderer convert(final IDataRenderer<D> renderer) {
        return new IRenderer () {

            /**
             * {@inheritDoc}
             *
             * @see IRenderer#render(Element)
             */
            @Override
            public IUIEventHandler render(Element el) {
                return renderer.render (el, null);
            }

        };
    }

    /**
     * Convenience to create a renderer from node supplier (i.e. directly building
     * the node structure).
     * <p>
     * This may create the node structure using methods on {@link DomGlobal} or from
     * {@link DomBuilder}.
     * 
     * @param generator
     *                  the node generator.
     * @return the associated renderer.
     */
    public static IRenderer build(Supplier<Node> generator) {
        return new IRenderer () {

            @Override
            public IUIEventHandler render(Element el) {
                Node node = generator.get ();
                if (node != null)
                    el.appendChild (node);
                return null;
            }
        };
    }

    public static IRenderer build(Function<Element,IUIEventHandler> builder) {
        return new IRenderer () {

            @Override
            public IUIEventHandler render(Element el) {
                return builder.apply (el);
            }
        }; 
    }
}
