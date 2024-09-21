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
package com.effacy.jui.ui.client.table.renderer;

import java.util.function.BiConsumer;

import com.effacy.jui.core.client.dom.UIEvent;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.builder.NodeBuilder.NodeContext;
import com.effacy.jui.ui.client.table.ITableCellRenderer;

import elemental2.dom.Element;

/**
 * Builds a table cell renderer from a {@link ElementBuilder} (including the
 * support for event handling).
 */
public class BuilderTableCellRenderer<D> extends TableCellRenderer<D> {

    /**
     * Used to render the content.
     */
    private BiConsumer<ElementBuilder, D> renderer;

    /**
     * Construct with a renderer.
     * <p>
     * The renderer is a lambda expression that is passed an element builder to
     * build the DOM into and the data for the record. Note that the builder will be
     * built by the cell renderer so you do not need to do that.
     * 
     * @param renderer
     *                 to render the cell content as DOM.
     */
    public BuilderTableCellRenderer(BiConsumer<ElementBuilder, D> renderer) {
        this.renderer = renderer;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.ui.client.table.ITableCellRenderer#render(elemental2.dom.Element,
     *      java.lang.Object)
     */
    @Override
    public ITableCellHandler<D> render(Element el, D data) {
        ElementBuilder builder = Wrap.$ (el);
        renderer.accept (builder, data);
        NodeContext ctx = builder.build ();
        if (!ctx.doesHandleEvent ())
            return null;
        return new ITableCellHandler<D> () {

            @Override
            public boolean handleUIEvent(UIEvent e, D data) {
                return ctx.handleEvent (e);
            }
            
        };
    }

    public static <D> ITableCellRenderer<D> create(BiConsumer<ElementBuilder, D> renderer) {
        return new BuilderTableCellRenderer<D> (renderer);
    }

}
