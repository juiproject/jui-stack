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
package com.effacy.jui.core.client.dom.builder;

import java.util.function.Consumer;

public class Table {

    /**
     * Creates an instance of this element and applies the passed builders to it.
     * 
     * @param builder
     *                the builders to apply.
     * @return the newly created element.
     */
    @SafeVarargs
    public static ElementBuilder $(Consumer<ElementBuilder>... builder) {
        return DomBuilder.table (builder);
    }

    /**
     * Creates an instance of this element, applies the passed builders to it and
     * inserts it into the passed parent.
     * 
     * @param parent
     *                the parent to insert the element into.
     * @return the newly created element.
     */
    public static ElementBuilder $(IDomInsertableContainer<?> parent) {
        ElementBuilder el = $ ();
        parent.insert (el);
        return el;
    }
}
