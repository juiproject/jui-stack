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

import java.util.List;

import com.effacy.jui.ui.client.editor.Editor.IEditor;
import com.effacy.jui.ui.client.editor.model.ContentBlock;
import com.effacy.jui.ui.client.editor.model.ContentBlock.BlockType;
import com.effacy.jui.ui.client.editor.model.ContentBlock.FormatType;
import com.effacy.jui.ui.client.editor.tools.FormattedTextTool;
import com.effacy.jui.ui.client.editor.tools.ITool;

/**
 * ParagraphBlock
 *
 * @author Jeremy Buckley
 */
public class ParagraphBlock extends FormattedTextBlock  {

    /**
     * The container of formatted text.
     */
    private FormattedText container;

    /**
     * Construct using the passed content block.
     * 
     * @param editor
     *                the owning editor.
     * @param content
     *                the content to render (can be {@code null}).
     */
    public ParagraphBlock(IEditor editor, ContentBlock content) {
        super (BlockType.PARAGRAPH, editor, content);
    }

    public ParagraphBlock(Block transform) {
        this (transform.editor (), transform.content ());
    }

    @Override
    public ParagraphBlock clone() {
        return new ParagraphBlock (editor, content ().clone ());
    }
    
    @Override
    public String label() {
        return "Paragraph";
    }

    @Override
    protected Block createSplitBlock(ContentBlock content) {
        return new ParagraphBlock (editor (), content);
    }

    @Override
    protected List<ITool> toolsForSelection() {
        List<ITool> tools = super.toolsForSelection ();
        tools.add (FormattedTextTool.tool (FormatType.BOLD));
        tools.add (FormattedTextTool.tool (FormatType.ITALIC));
        tools.add (FormattedTextTool.tool (FormatType.UNDERLINE));
        tools.add (FormattedTextTool.tool (FormatType.STRIKE));
        tools.add (FormattedTextTool.tool (FormatType.SUPERSCRIPT));
        tools.add (FormattedTextTool.tool (FormatType.SUBSCRIPT));
        tools.add (FormattedTextTool.tool (FormatType.CODE));
        return tools;
    }


    
}
