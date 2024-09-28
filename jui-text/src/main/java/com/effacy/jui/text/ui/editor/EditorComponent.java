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

import com.effacy.jui.core.client.component.SimpleComponent;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.text.type.FormattedBlock.BlockType;
import com.effacy.jui.text.type.FormattedLine.FormatType;
import com.effacy.jui.text.type.FormattedText;
import com.effacy.jui.text.ui.editor.tools.BlockSelectionTool;
import com.effacy.jui.text.ui.editor.tools.FormattedTextTool;
import com.effacy.jui.text.ui.editor.tools.ToolBar;

import elemental2.dom.Element;

public class EditorComponent extends SimpleComponent {

    private Editor editor;

    private boolean inline;

    public EditorComponent(boolean inline) {
        this.inline = inline;
        editor = new Editor ();
        registerEventHandler (editor);

        // Seeding content.
        FormattedText content = new FormattedText()
            .block (BlockType.H1, blk -> blk.line (line -> line.append ("Markets and economics")))
            .block (BlockType.NLIST, blk -> blk.line ("This is a line item"))
            .block (BlockType.NLIST, blk -> blk.line ("This is a second line item"))
            .block (BlockType.PARA, blk -> blk
                .line (line -> line
                .append ("Markets assume basic economic\n value as underlying decision making but this conflicts with social relations and morals whose values are ")
                .append ("not", FormatType.BLD)
                .append (" substitutable."))
                .indent (1))
            .block (BlockType.EQN, blk -> blk
                .split (EquationBlock.DEMO_2)
                .indent (1))
            .block (BlockType.PARA, blk -> blk
                .line (line -> line.append ("Two challenges to free market assumptions: coercion / fairness (market assumption relies on the ideal of consent however one questions whether free choice is truely voluntary in the face of inequity - on the supply side needing the money and on the demand side not having the money) and corruption / degradation (some measures of value not substitutable with economic value so commodification degrades that value - commonly civic and honorific).")))
            .block (BlockType.DIA, blk -> blk
                .split (DiagramBlock.DEMO_2)
                .meta (DiagramBlock.META_CAPTION, "Demonstration diagram"))
            .block (BlockType.PARA, blk -> blk
                .line (line -> line.append ("Two challenges to free market assumptions: coercion / fairness (market assumption relies on the ideal of consent however one questions whether free choice is truely voluntary in the face of inequity - on the supply side needing the money and on the demand side not having the money) and corruption / degradation (some measures of value not substitutable with economic value so commodification degrades that value - commonly civic and honorific).")))
            .block (BlockType.PARA, blk -> blk
                .line (line -> line.append ("Two challenges to free market assumptions: coercion / fairness (market assumption relies on the ideal of consent however one questions whether free choice is truely voluntary in the face of inequity - on the supply side needing the money and on the demand side not having the money) and corruption / degradation (some measures of value not substitutable with economic value so commodification degrades that value - commonly civic and honorific).")))
            .block (BlockType.PARA, blk -> blk
                .line (line -> line.append ("Two challenges to free market assumptions: coercion / fairness (market assumption relies on the ideal of consent however one questions whether free choice is truely voluntary in the face of inequity - on the supply side needing the money and on the demand side not having the money) and corruption / degradation (some measures of value not substitutable with economic value so commodification degrades that value - commonly civic and honorific).")))
            ;
        editor.update (content);
    }

	@Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$ (el).$ (root -> {
            if (!inline) {
                root.style ("editor_cpt");
                Div.$(root).$ (toolbar -> {
                    toolbar.style ("editor_cpt_toolbar");
                    editor.toolbar (ToolBar.$ (toolbar, tb -> {
                        tb.add (new BlockSelectionTool ());
                        tb.add (FormattedTextTool.tool (FormatType.BLD));
                        tb.add (FormattedTextTool.tool (FormatType.ITL));
                        tb.add (FormattedTextTool.tool (FormatType.UL));
                        tb.add (FormattedTextTool.tool (FormatType.STR));
                        tb.add (FormattedTextTool.tool (FormatType.SUP));
                        tb.add (FormattedTextTool.tool (FormatType.SUB));
                        tb.add (FormattedTextTool.tool (FormatType.CODE));
                        tb.add (FormattedTextTool.tool (FormatType.HL));
                    }));
                });
            }
            Div.$(root).$(outer -> {
                outer.by ("editor");
            });
        }).build (extractor -> {
            editor.bind (extractor.first ("editor"));
        });
    }
    

}
