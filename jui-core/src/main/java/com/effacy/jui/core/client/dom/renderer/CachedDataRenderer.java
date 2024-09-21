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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.effacy.jui.core.client.dom.IUIEventHandler;
import com.effacy.jui.platform.util.client.Logger;

import elemental2.dom.Element;

/**
 * Caching version of a {@link IDataRenderer}). A cache key is supplied and the
 * underlying renderer will only be built once and cached under that key. The
 * cache is global so multiple instance of this class that used the same cache
 * key will share the same underlying renderer.
 * <p>
 * The general case for using a cache is where the renderer takes some time to
 * build (i.e. it is a template of some form) so regenerating the renderer each
 * time it is needed is not efficient.
 * <p>
 * For renderers that build DOM directly, there is no need to support them with
 * a cache.
 *
 * @author Jeremy Buckley
 */
public class CachedDataRenderer<D> implements IDataRenderer<D> {

    /**
     * Convenience to cache the passed renderer under the given cache key. If called
     * more than once then the subsequent calls are ignored.
     * 
     * @param <D>
     * @param cacheKey
     *                 the key to cache under.
     * @param renderer
     *                 the renderer being cached.
     * @return a cached version of the renderer (for subsequent use).
     */
    public static <D> IDataRenderer<D> cache(String cacheKey, IDataRenderer<D> renderer) {
        return new CachedDataRenderer<D> (cacheKey, () -> renderer);
    }

    /**
     * Convenience to cache the passed renderer under the given cache key.
     * <p>
     * This will retrieve the renderer from the supplier only once (for the given
     * key).
     * 
     * @param <D>
     * @param cacheKey
     *                 the key to cache under.
     * @param renderer
     *                 the renderer supplier (that will be cached).
     * @return a cached version of the renderer (for subsequent use).
     */
    public static <D> IDataRenderer<D> cache(String cacheKey, Supplier<IDataRenderer<D>> renderer) {
        return new CachedDataRenderer<D> (cacheKey, renderer);
    }

    /**
     * Used to globally set debug mode.
     */
    public static boolean DEBUG = false;

    /**
     * The cache of renderers.
     */
    private static final Map<String, IDataRenderer<?>> CACHE = new HashMap<> ();

    /**
     * The cache key to reference the renderer.
     */
    private String cacheKey;

    /**
     * An optional builder provided during construction and used by
     * {@link #buildRenderer()}.
     */
    private Supplier<IDataRenderer<D>> builder;

    /**
     * Construct with a cache key.
     * 
     * @param cacheKey
     *                 the key.
     */
    public CachedDataRenderer(String cacheKey) {
        this.cacheKey = cacheKey;
    }

    /**
     * Construct with a cache key.
     * 
     * @param cacheKey
     *                 the key.
     * @param builder
     *                 (optional) a builder to use to build the first renderer
     *                 instance.
     */
    public CachedDataRenderer(String cacheKey, Supplier<IDataRenderer<D>> builder) {
        this.cacheKey = cacheKey;
        this.builder = builder;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.dom.renderer.IDataRenderer#render(elemental2.dom.Element,
     *      java.lang.Object)
     */
    @Override
    public IUIEventHandler render(Element el, D data) {
        return renderer ().render (el, data);
    }

    /**
     * Obtains the renderer from the cache, creating one if necessary.
     * 
     * @return the renderer.
     */
    @SuppressWarnings("unchecked")
    public IDataRenderer<D> renderer() {
        IDataRenderer<D> renderer = (IDataRenderer<D>) CACHE.get (cacheKey);
        if (DEBUG)
            Logger.log ("CachedDataRenderer: " + cacheKey + " -> found=" + (renderer != null));
        if (renderer == null) {
            renderer = buildRenderer ();
            if (DEBUG)
                Logger.log ("CachedDataRenderer: " + cacheKey + " -> creating=" + (renderer != null));
            if (renderer == null)
                throw new RuntimeException ("No renderer (cache-key=" + cacheKey + ")");
            CACHE.put (cacheKey, renderer);
        }
        return renderer;
    }

    /**
     * Builds the first instance of the renderer. This will be cached.
     * <p>
     * This can be overridden or a builder can be provided during construction.
     * 
     * @return the renderer.
     */
    protected IDataRenderer<D> buildRenderer() {
        if (builder != null)
            return builder.get ();
        return null;
    }

}
