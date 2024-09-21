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
package com.effacy.jui.ui.client.gallery;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.effacy.jui.core.client.dom.builder.ContainerBuilder;
import com.effacy.jui.core.client.store.IStore;
import com.effacy.jui.platform.util.client.With;

public class GalleryCreator {

    public static <R> Gallery<R> $(ContainerBuilder<?> el, IStore<R> store, Supplier<IGalleryItem<R>> itemFactory) {
        return $(el, null, store, itemFactory);
    }

    public static <R> Gallery<R> $(ContainerBuilder<?> el, Consumer<Gallery.Config> cfg, IStore<R> store, Supplier<IGalleryItem<R>> itemFactory) {
        return With.$ (build(cfg, store, itemFactory), cpt -> el.render (cpt));
    }
    
    public static <R> Gallery<R> build(IStore<R> store, Supplier<IGalleryItem<R>> itemFactory) {
        return build (null, store, itemFactory);
    }

    public static <R> Gallery<R> build(Consumer<Gallery.Config> cfg, IStore<R> store, Supplier<IGalleryItem<R>> itemFactory) {
        Gallery.Config config = new Gallery.Config ();
        if (cfg != null)
            cfg.accept (config);
        return new Gallery<R> (config, store, itemFactory);
    }

}
