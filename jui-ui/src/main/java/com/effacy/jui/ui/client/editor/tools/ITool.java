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
package com.effacy.jui.ui.client.editor.tools;

import com.effacy.jui.core.client.dom.UIEvent;
import com.effacy.jui.core.client.dom.builder.ContainerBuilder;
import com.effacy.jui.ui.client.editor.Block;
import com.effacy.jui.ui.client.editor.Editor.IEditor;

import elemental2.dom.Range;

public interface ITool {

    /**
     * Used to prepare a tool bar for operating in a given context (i.e. a range
     * selection).
     */
    public interface  IToolContext {

        /**
         * The editor.
         * 
         * @return the editor.
         */
        public IEditor editor();

        /**
         * The block that the tool will be operating on.
         * 
         * @return the block.
         */
        public Block block();

        /**
         * The current range.
         * 
         * @return the range.
         */
        public Range range();

        /**
         * Updates the range.
         * 
         * @param range the range.
         */
        public void updateRange(Range range);
        
    }

    public String uuid();

    /**
     * Invoked when another tool has been clicked (to ensure that the tool is
     * closed).
     */
    default void close() {
        // Nothing.
    }

    public boolean apply(IToolContext ctx, UIEvent event);

    public void deactivate();

    public void activate(IToolContext ctx);

    public void render(ContainerBuilder<?> parent, IToolBarCSS styles);
}
