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
package com.effacy.jui.text.ui.editor;

import com.effacy.jui.core.client.dom.DomSupport;
import com.effacy.jui.core.client.dom.UIEvent;
import com.effacy.jui.platform.css.client.CssDeclaration;
import com.effacy.jui.text.type.FormattedBlock;
import com.effacy.jui.text.type.FormattedBlock.BlockType;
import com.effacy.jui.text.ui.editor.Editor.IEditor;

import elemental2.dom.Element;

public class NumberedListBlock extends ListBlock {

    private Element numberEl;

    /**
     * Construct using the passed content block.
     * 
     * @param editor
     *                the owning editor.
     * @param content
     *                the content to render (can be {@code null}).
     */
    public NumberedListBlock(IEditor editor, FormattedBlock content) {
        super (BlockType.NLIST, editor, content);
    }

    public NumberedListBlock(Block transform) {
        this (transform.editor (), transform.content ());
    }

    @Override
    public NumberedListBlock clone() {
        return new NumberedListBlock (editor, content ().clone ());
    }
    
    @Override
    public String label() {
        return "Numbered list";
    }

    @Override
    protected Element createContainer() {
        Element el = super.createContainer ();
        Element outerEl = DomSupport.createDiv ();
        outerEl.classList.add (styles().numberedlist ());
        numberEl = DomSupport.createSpan (outerEl);
        outerEl.append (el);
        return outerEl;
    }

    /**
     * Action a press on the backspace key.
     * 
     * @param event
     *              the underlying event.
     * @return {@code true} if the event was handled.
     */
    protected boolean onBackSpaceKey(UIEvent event) {
        if (EditorSupport.insertionPoint () && (EditorSupport.positionOfCursor (contentEl ()) == 0)) {
            editor.onReplace (this, new ParagraphBlock(editor(), content().transform (BlockType.PARA)), true);
            return true;
        }
        return false;
    }

    @Override
    protected Block createSplitBlock(FormattedBlock content) {
        return new NumberedListBlock (editor (), content);
    }

    @Override
    protected void refresh(FormattedBlock content) {
        super.refresh (content);
        renumber ();
    }

    @Override
    public boolean renumber() {
        if (!super.renumber ())
            return false;

        // Update the numbering for the block.
        numberEl.textContent = number () + ".";

        return true;
    }

    /**
     * Styles needed for diagrams.
     */
    public interface INumberedListBlockCSS extends CssDeclaration {

        public String numberedlist();
    }
    
}
