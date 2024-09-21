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
package com.effacy.jui.core.client.dom;

import java.util.List;

import elemental2.dom.Node;

public interface IDomSelector {
    
    /**
     * Finds the first node matching the given reference.
     * 
     * @param reference
     *                  the reference for lookup.
     * @return the first node (or {@code null}).
     */
    public <E extends Node> E first(String reference);

    /**
     * Get all the nodes matching the given reference.
     * 
     * @param reference
     *                  the reference.
     * @return the nodes (empty list if there are none).
     */
    public <E extends Node> List<E> all(String reference);
}
