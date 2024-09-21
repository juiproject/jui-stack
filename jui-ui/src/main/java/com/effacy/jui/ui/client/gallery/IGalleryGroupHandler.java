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
 * IGalleryGroupHandler
 *
 * @author Jeremy Buckley
 */
public interface IGalleryGroupHandler<R> {

    /**
     * Determines if there is a new group to be created.
     * 
     * @param previous
     *                 the previous record (will be {@code null} if the current is
     *                 the first).
     * @param current
     *                 the current record.
     * @return {@code true} if a new group is to be created.
     */
    public boolean newGroup(R previous, R current);

    /**
     * Renders a group description.
     * 
     * @param groupEl
     *                the group element to render into.
     * @param record
     *                the record to generate the group from.
     */
    public void renderGroup(Element groupEl, R record);
}
