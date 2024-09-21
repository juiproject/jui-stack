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

public class Text {

    /**
     * Creates an instance of this element with the given content.
     * 
     * @param text
     *             the text to apply.
     * @return the newly created element builder.
     */
    public static TextBuilder $(String text) {
        return new TextBuilder (text);
    }
    
    /**
     * Creates an instance of this element, applies the passed builders to it and
     * inserts it into the passed parent.
     * 
     * @param parent
     *                the parent to insert the element into.
     * @param text
     *             the text to apply.
     * @return the newly created element builder.
     */
    public static TextBuilder $(IDomInsertableContainer<?> parent, String text) {
        TextBuilder builder = $ (text);
        parent.insert (builder);
        return builder;
    }

    /************************************************************************
     * Commonly used characters.
     ************************************************************************/

    /**
     * Escape sequence for an nbsp.
     * 
     * @return the sequence.
     */
    public static String nbsp() {
        return "\u00A0";
    }

    /**
     * Escape sequence for an bull.
     * 
     * @return the sequence.
     */
    public static String bull() {
        return "\u2022";
    }
}
