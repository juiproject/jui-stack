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
package com.effacy.jui.ui.client.gallery.item;

import com.effacy.jui.core.client.dom.IDomSelector;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.IUIEventHandler;
import com.effacy.jui.core.client.dom.renderer.CachedDataRenderer;
import com.effacy.jui.core.client.dom.renderer.IDataRenderer;
import com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Container;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.ui.client.gallery.IGalleryItem;

import elemental2.dom.Element;

/**
 * Variant of a {@link IGalleryItem} that uses a renderer to generate the
 * gallery content.
 *
 * @author Jeremy Buckley
 */
public abstract class DataRendererGalleryItem<R, D> extends GalleryItem<R> {

    /**
     * The name of the cache key to the renderer. This is nominally derived but can
     * be set outright with {@link #cacheKey(String, boolean)}.
     */
    private String cacheKey;

    /**
     * The renderer which is passed during construction or is build by overriding
     * {@link #buildContainer(Container)} (or by passing a builder during
     * construction). This will be caching so that multiple instances of the item
     * will not incur multiple renderer templates.
     */
    protected IDataRenderer<D> renderer;

    /************************************************************************
     * Construction.
     ************************************************************************/

    /**
     * Builds a gallery item where the renderer is obtained by a call to
     * {@link #createRenderer()}.
     */
    protected DataRendererGalleryItem() {
        this (null);
    }

    /**
     * Construct with a cache key.
     * 
     * @param cacheKey
     *                 the cache key.
     */
    protected DataRendererGalleryItem(String cacheKey) {
        this.cacheKey = cacheKey;
    }

    /************************************************************************
     * Overrides.
     ************************************************************************/

    /**
     * Invoked by {@link #onRender(Element)} to build a node for insertion into the
     * root element. If the returned provider also implements
     * {@link IUIEventHandler} then it will be registered as a handler.
     * <p>
     * This return's {@code null} by default which will result in
     * {@link #onRender(Element)} moving to resolve a renderer.
     * 
     * @param config
     *               the component configuration.
     * @return the node (via a provider) to insert.
     */
    protected INodeProvider buildNode(D config) {
        return null;
    }

    /**
     * Obtains a renderer. If the renderer has not been set directly the one is
     * obtained from {@link #createRenderer()} then cached against the class.
     * 
     * @return the renderer.
     */
    protected final IDataRenderer<D> getRenderer() {
        if (renderer == null) {
            renderer = createRenderer ();
            if (cacheKey != null)
                renderer = new CachedDataRenderer<D> (cacheKey, () -> createRenderer ());
            else
                renderer = createRenderer ();
        }
        return renderer;
    }

    /**
     * Creates a new renderer and caches that renderer against the class name.
     * <p>
     * In general one should override this to generate a new renderer. One may use
     * the default renderer returned and configure that.
     * <p>
     * The renderer is also cached against the class.
     * 
     * @return the renderer.
     */
    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.ui.client.gallery.item.DataRendererGalleryItem#createRenderer()
     */
    protected IDataRenderer<D> createRenderer() {
        return null;
    }

    /**
     * Given the base record converts the format needed for the builder.
     * <p>
     * This needs to be overridden.
     * 
     * @param record
     *               the record.
     * @return the converted record.
     */
    protected abstract D convert(R record);

    /************************************************************************
     * Internal methods and overrides.
     ************************************************************************/

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#onRender(elemental2.dom.Element)
     */
    @Override
    protected IDomSelector onRender(Element el) {
        if (getRecord () == null)
            Logger.log ("NULL RECORD!!!");
        Object response = null;
        INodeProvider node = buildNode (convert (getRecord ()));
        if (node != null) {
            response = node;
            getRoot ().appendChild (node.node ());
        } else {
            response = getRenderer ().render (getRoot (), convert (getRecord ()));
        }
        if (response instanceof IUIEventHandler)
            registerEventHandler ((IUIEventHandler) response, "gallery");
        if (response instanceof IDomSelector)
            return (IDomSelector) response;
        return null;
    }

}
