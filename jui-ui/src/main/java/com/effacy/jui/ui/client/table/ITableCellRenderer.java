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
package com.effacy.jui.ui.client.table;

import java.util.function.BiConsumer;
import java.util.function.Function;

import com.effacy.jui.core.client.dom.IUIEventHandler;
import com.effacy.jui.core.client.dom.UIEvent;
import com.effacy.jui.core.client.dom.renderer.IDataRenderer;

import elemental2.dom.Element;

/**
 * This renderer is used by {@link Table} to render the contents of cells.
 *
 * @author Jeremy Buckley
 */
@FunctionalInterface
public interface ITableCellRenderer<D> {

    /**
     * This is returned when a cell is rendered and used by the table to direct UI
     * events to for handling.
     */
    public interface ITableCellHandler<D> {

        /**
         * Invoked when an event is targeted against the cell. Passed is the event and
         * the underlying data (generally this is a record but via conversion can be a
         * specific property on a record).
         * 
         * @param e
         *             the UI event.
         * @param data
         *             the data pertinent to the record (or property).
         * @return {@code true} if the event was handled (more specifcally the event
         *         properly targeted to the cell).
         */
        public boolean handleUIEvent(UIEvent e, D data);
    }

    /**
     * Renders the data into the cell.
     * 
     * @param el
     *             the target element of the cell to render into.
     * @param data
     *             the data to render.
     * @return (optional) a handler for handling UI events.
     */
    public ITableCellHandler<D> render(Element el, D data);

    /**
     * Creates a simple custom renderer that only renders content.
     * 
     * @param renderer
     *                 the renderer.
     * @return the cell renderer.
     */
    public static <D> ITableCellRenderer<D> custom(BiConsumer<Element, D> renderer) {
        return new ITableCellRenderer<D> () {

            @Override
            public ITableCellHandler<D> render(Element el, D data) {
                if (renderer != null)
                    renderer.accept (el, data);
                return null;
            }

        };
    }


    /**
     * Creates a cell renderer from a data renderer.
     * <p>
     * If the data renderer returns a handle then that handler will be used.
     * 
     * @param renderer
     *                 the data renderer to use.
     * @return the wrapping cell renderer.
     */
    public static <D> ITableCellRenderer<D> create(IDataRenderer<D> renderer) {
        return new ITableCellRenderer<D> () {

            @Override
            public ITableCellHandler<D> render(Element el, D data) {
                if (renderer != null) {
                    IUIEventHandler handler = renderer.render (el, data);
                    if (handler == null)
                        return null;
                    return new ITableCellHandler<D> () {

                        @Override
                        public boolean handleUIEvent(UIEvent e, D data) {
                            return handler.handleEvent (e);
                        }

                    };
                }
                return null;
            }

        };
    }

    /**
     * Convenience to perform a data transformation. Generally this is used to
     * transform through from a data record to specific property value that is being
     * rendered.
     * 
     * @param <R>
     *                  the record type being mapped from.
     * @param <D>
     *                  the data type being mapped to.
     * @param converter
     *                  a conversion from R to D.
     * @param renderer
     *                  the renderer that will render the transformed data.
     * @return the transformed renderer.
     */
    public static <R, D> ITableCellRenderer<R> convert(Function<R, D> converter, ITableCellRenderer<D> renderer) {
        return new ITableCellRenderer<R> () {

            @Override
            public ITableCellHandler<R> render(Element el, R data) {
                ITableCellHandler<D> handler = renderer.render (el, converter.apply (data));
                return new ITableCellHandler<R> () {

                    @Override
                    public boolean handleUIEvent(UIEvent e, R data) {
                        if (handler == null)
                            return false;
                        return handler.handleUIEvent (e, converter.apply (data));
                    }

                };
            }

        };
    }
}
