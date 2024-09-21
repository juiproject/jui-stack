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

import elemental2.dom.Element;

/**
 * Represents a gallery item. This combines record and element (wrapper)
 * together.
 */
public interface IGalleryItem<M> {

    /**
     * The owning gallery.
     * 
     * @return the gallery.
     */
    public IGallery<M> getGallery();

    /**
     * Setter for {@link #getGallery()}.
     */
    public void setGallery(IGallery<M> gallery);

    /**
     * Gets the record.
     * 
     * @return The record.
     */
    public M getRecord();

    /**
     * Setter for {@link #getRecord()}.
     */
    public void setRecord(M record);

    /**
     * Marks the item as having been selected.
     */
    default public void select() {
    }

    /**
     * Un-marks selection of the element.
     */
    default public void unselect() {
    }

    /**
     * Requests that the record be rendered into the wrapper.
     */
    public void render(Element target);

    /**
     * Enables the item (called when the gallery is enabled).
     */
    default public void enable() {
    }

    /**
     * Disables the item (called when the gallery is disabled).
     */
    default public void disable() {
    }

    /**
     * Reload the item (where reloading is possible).
     */
    default public void reload() {
    }

    /**
     * Invoked when the item is disposed of.
     */
    default public void dispose() {
    }
}
