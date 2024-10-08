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
import com.effacy.jui.platform.util.client.Itr;
import com.effacy.jui.text.type.FormattedBlock;
import com.effacy.jui.text.type.FormattedBlock.BlockType;
import com.effacy.jui.text.type.FormattedLine.FormatType;
import com.effacy.jui.text.ui.editor.Editor.IEditor;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;

public class HeadingBlock extends FormattedContentBlock {

    private int level;

    static BlockType type(int level) {
        if (level == 1)
            return BlockType.H1;
        if (level == 2)
            return BlockType.H2;
        return BlockType.H3;
    }

    public HeadingBlock(IEditor editor, int level, FormattedBlock content) {
        super (type (level), editor, content);
        this.level = Math.min (3, Math.max (1, level));
    }

    public HeadingBlock(int level, Block transform) {
        this (transform.editor (), level, transform.content ());
    }

    @Override
    public Block clone() {
        return new HeadingBlock (editor, level, content ().clone ());
    }

    @Override
    public String label() {
        return "Heading " + level;
    }

    @Override
    public boolean applies(FormatType type) {
        return false;
    }

    @Override
    protected void populate(FormattedBlock content) {
        EditorSupport.traverse (containerEl, (str,styles) -> {
            content.lastLine ().append (str);
        }, () -> {
            content.line ();
        });
    }

    @Override
    protected Element createContentArea() {
        if (level == 3)
            return DomSupport.createElement("H3");
        if (level == 2)
            return DomSupport.createElement("H2");
        return DomSupport.createElement("H1");
    }

    @Override
    protected void refresh(FormattedBlock content) {
        DomSupport.removeAllChildren (containerEl);
        if ((content == null) || content.getLines ().isEmpty ()) {
            containerEl.classList.add (styles().empty());
            containerEl.setAttribute ("placeholder", "Please enter some content!");
        } else {
            Itr.forEach(content.getLines(), (c,v) -> {
                if (!c.first ())
                    containerEl.append (DomGlobal.document.createElement ("br"));
                v.traverse ((text,styles) -> {
                    containerEl.append (DomGlobal.document.createTextNode (text));
                });
            });

            // Ensure the content is clean.
            EditorSupport.clean (containerEl);
        }
    }

    @Override
    protected Block createSplitBlock(FormattedBlock content) {
        return new HeadingBlock (editor (), level, content);
    }
}
