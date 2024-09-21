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

import com.effacy.jui.core.client.component.SimpleComponent;
import com.effacy.jui.ui.client.gallery.IGallery;
import com.effacy.jui.ui.client.gallery.IGalleryItem;

import elemental2.dom.Element;

/**
 * Support class for implementing component based {@link IGalleryItem}'s.
 *
 * @author Jeremy Buckley
 */
public abstract class GalleryItem<R> extends SimpleComponent implements IGalleryItem<R> {

    /**
     * See {@link #getRecord()}.
     */
    protected R record;

    /**
     * See {@link #getGallery()}.
     */
    protected IGallery<R> gallery;

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.ui.client.gallery.IGalleryItem#getGallery()
     */
    @Override
    public IGallery<R> getGallery() {
        return gallery;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.ui.client.gallery.IGalleryItem#setGallery(com.effacy.jui.ui.client.gallery.IGallery)
     */
    @Override
    public void setGallery(IGallery<R> gallery) {
        this.gallery = gallery;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.ui.client.gallery.IGalleryItem#getRecord()
     */
    @Override
    public R getRecord() {
        return record;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.ui.client.gallery.IGalleryItem#setRecord(java.lang.Object)
     */
    @Override
    public void setRecord(R record) {
        this.record = record;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.ui.client.gallery.IGalleryItem#render(elemental2.dom.Element)
     */
    @Override
    public void render(Element target) {
        // Delegate this through to the component.
        super.render (target, 0);
    }

}
