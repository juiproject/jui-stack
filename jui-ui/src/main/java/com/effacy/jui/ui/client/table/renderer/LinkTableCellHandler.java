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

import java.util.function.Consumer;
import java.util.function.Function;

import com.effacy.jui.core.client.dom.DomSupport;
import com.effacy.jui.core.client.dom.UIEvent;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.ui.client.table.ITableCellRenderer;
import com.effacy.jui.ui.client.table.ITableCellRenderer.ITableCellHandler;

import elemental2.dom.Element;

/**
 * A standard handler for displaying a link that can be clicked on.
 *
 * @author Jeremy Buckley
 */
public class LinkTableCellHandler<R> implements ITableCellHandler<R> {

    /**
     * The element that holds the link that can be activated.
     */
    private Element anchorEl;

    /**
     * The handler to invoked when the link is clicked on.
     */
    private Consumer<R> linkHandler;

    /**
     * Construct a handler with the root element, configuration data and a link
     * handler. The DOM will be created into the root element and events sunk.
     * 
     * @param el
     *                    the root element to render into.
     * @param data
     *                    the data configuring the DOM.
     * @param linkHandler
     *                    the handler to invoked when the link has been clicked.
     */
    public LinkTableCellHandler(Element el, String data, Consumer<R> linkHandler) {
        this.linkHandler = linkHandler;
        anchorEl = DomSupport.createA (el);
        DomSupport.innerText (anchorEl, data);
        UIEventType.ONCLICK.attach (anchorEl);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.ui.client.table.ITableCellRenderer.ITableCellHandler#handleUIEvent(com.effacy.jui.core.client.dom.UIEvent,
     *      java.lang.Object)
     */
    @Override
    public boolean handleUIEvent(UIEvent e, R data) {
        if ((linkHandler == null) || !DomSupport.isChildOf (e.getTarget (), anchorEl))
            return false;
        linkHandler.accept (data);
        return true;
    }

    /**
     * Contruct a render for the link cell.
     * 
     * @param <R>
     * @param value
     *                    the value extractor from the record to the value to
     *                    display.
     * @param linkHandler
     *                    a handler to handle clicks.
     * @return the renderer.
     */
    public static <R> ITableCellRenderer<R> create(Function<R, String> value, Consumer<R> linkHandler) {
        return new ITableCellRenderer<R> () {

            @Override
            public ITableCellHandler<R> render(Element el, R data) {
                return new LinkTableCellHandler<R> (el, value.apply (data), linkHandler);
            }

        };
    }

}
