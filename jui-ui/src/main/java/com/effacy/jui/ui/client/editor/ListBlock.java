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
package com.effacy.jui.ui.client.editor;

import com.effacy.jui.core.client.dom.UIEvent;
import com.effacy.jui.ui.client.editor.Editor.IEditor;
import com.effacy.jui.ui.client.editor.model.ContentBlock;
import com.effacy.jui.ui.client.editor.model.ContentBlock.BlockType;

public abstract class ListBlock extends FormattedTextBlock {

    protected int number;

    protected ListBlock(BlockType type, IEditor editor, ContentBlock content) {
        super (type, editor, content);
    }

    /**
     * This is invoked when the block prior has changed (in case this block has a
     * dependency).
     * 
     * @return {@code true} to cascade the change.
     */
    public boolean onPriorChanged() {
        return renumber ();
    }

    public boolean renumber() {
        int oldNumber = number;

        number = 1;
        Block prev = editor.prior (this);
        if (prev instanceof ListBlock)
            number = ((ListBlock) prev).number () + 1;

        return (number != oldNumber);
    }

    public int number() {
        return number;
    }

    @Override
    protected boolean onBackSpaceKey(UIEvent event) {
        if (content().length() == 0) {
            // Turn into a paragraph.
            editor.onReplace(this, new ParagraphBlock (this), true);
            return true;
        }
        return super.onBackSpaceKey(event);
    }

    @Override
    protected boolean onEnterKey(UIEvent event) {
        if (content().length() == 0) {
            // Turn into a paragraph.
            editor.onReplace(this, new ParagraphBlock (this), true);
            return true;
        }
        return super.onEnterKey(event);
    }
    
}
