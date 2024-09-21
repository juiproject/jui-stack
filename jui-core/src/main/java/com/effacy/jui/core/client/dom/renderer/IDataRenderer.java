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

import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.IUIEventHandler;
import com.effacy.jui.core.client.dom.UIEvent;
import com.effacy.jui.core.client.dom.builder.DomBuilder;
import com.effacy.jui.core.client.observable.Observable;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.Node;

/**
 * Provides a mechanism for rendering some content, configured by some data,
 * into a DOM element.
 *
 * @author Jeremy Buckley
 */
public interface IDataRenderer<D> {

    /**
     * Render content into the given element.
     * 
     * @param el
     *             the element to render into.
     * @param data
     *             the data that supports (provides guidance to and content for)
     *             rendering.
     */
    public IUIEventHandler render(Element el, D data);

    /**
     * Handle the passed event.
     * 
     * @param e
     *               the event to handler.
     * @param source
     *               the source of the event.
     * @param type
     *               an event type reference (mapped to the event handler).
     * @return {@code true} if the event was handled.
     */
    public default boolean event(UIEvent e, Observable source, String type) {
        return false;
    }

    /**
     * A convenience to convert this renderer to a renderer over another data type
     * (with type conversion).
     * 
     * @param <B>
     *                  the new data type.
     * @param converter
     *                  a converter to convert from the new data type to <D>
     * @return the revised renderer.
     */
    default public <B> IDataRenderer<B> convert(Function<B, D> converter) {
        return new IDataRenderer<B> () {

            /**
             * {@inheritDoc}
             *
             * @see com.effacy.jui.core.client.dom.renderer.IDataRenderer#render(elemental2.dom.Element,
             *      java.lang.Object)
             */
            @Override
            public IUIEventHandler render(Element el, B data) {
                return IDataRenderer.this.render (el, converter.apply (data));
            }

            /**
             * {@inheritDoc}
             *
             * @see com.effacy.jui.core.client.dom.renderer.IDataRenderer#event(com.effacy.jui.core.client.dom.UIEvent,
             *      com.effacy.jui.core.client.observable.Observable, java.lang.String)
             */
            @Override
            public boolean event(UIEvent e, Observable source, String type) {
                return IDataRenderer.super.event (e, source, type);
            }

        };
    }

    /**
     * Convenience to create a data renderer from a conversion from data to node
     * (i.e. directly building the node structure).
     * <p>
     * This may create the node structure using methods on {@link DomGlobal} or from
     * {@link DomBuilder}.
     * 
     * @param <D>
     *                  the data type.
     * @param generator
     *                  the node generator (using the passed data).
     * @return the associated renderer.
     */
    public static <D> IDataRenderer<D> node(Function<D, Node> generator) {
        return new NodeDataRenderer<> (generator);
    }

    /**
     * Creates a {@link IDataRenderer} from a builder that maps configuration data
     * to an {@link INodeProvider}.
     * 
     * @param <D>
     *                the data type.
     * @param builder
     *                the builder.
     * @return the data renderer.
     */
    public static <D> IDataRenderer<D> provider(Function<D, INodeProvider> builder) {
        return new NodeProviderDataRenderer<D>(builder);
    }

    /**
     * Converts a {@link IRenderer} to a {@link IDataRenderer}.
     * 
     * @param <D>
     *                 the data type.
     * @param renderer the renderer to convert (this is {@code null} safe).
     * @return the converted renderer.
     */
    public static <D> IDataRenderer<D> convert(IRenderer renderer) {
        return new IDataRenderer<D>() {

            @Override
            public IUIEventHandler render(Element el, D data) {
                if (renderer == null)
                    return null;
                return renderer.render (el);
            }
        };
    }

    /**
     * Costructs an empty renderer (that constructs no DOM and returns no event
     * handler).
     * 
     * @param <D>
     *            the data type.
     * @return the renderer.
     */
    public static <D> IDataRenderer<D> empty() {
        return new IDataRenderer<D> () {

            @Override
            public IUIEventHandler render(Element el, D data) {
                return null;
            }

        };
    }

}
