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
package com.effacy.jui.text.ui.fragment;

import com.effacy.jui.core.client.dom.builder.Br;
import com.effacy.jui.core.client.dom.builder.ContainerBuilder;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.Fragment;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.core.client.dom.builder.P;
import com.effacy.jui.platform.util.client.Itr;
import com.effacy.jui.text.type.FormattedBlock.BlockType;
import com.effacy.jui.text.type.FormattedText;

/**
 * Renders {@link FormattedText}.
 */
public class FText extends Fragment<FText> {

    public static FText $(FormattedText text) {
        return $ (null, text);
    }

    public static FText $(FormattedText text, boolean embed) {
        return $ (null, text, embed);
    }

    public static FText $(IDomInsertableContainer<?> parent, FormattedText text) {
        return $ (parent, text, false);
    }

    public static FText $(IDomInsertableContainer<?> parent, FormattedText text, boolean embed) {
        FText frg = new FText (text, embed);
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    private FormattedText text;

    private boolean embed;

    public FText(FormattedText text, boolean embed) {
        this.text = text;
        this.embed = embed;
    }

    @Override
    public void build(ContainerBuilder<?> parent) {
        if (embed)
            _build (parent);
        else
            super.build(parent);
    }

    @Override
    protected void buildInto(ElementBuilder root) {
        root.style ("juiFragFText");
        _build (root);
    }

    private void _build(ContainerBuilder<?> parent) {
        text.getBlocks ().forEach (blk -> {
            if (blk.typeIs (BlockType.PARA, BlockType.NLIST)) {
                P.$ (parent).$ (p -> {
                    if (blk.getIndent() > 0) {
                        p.style ("indent" + blk.getIndent ());
                        if (blk.typeIs (BlockType.NLIST))
                            p.style ("list_bullet");
                    }
                    Itr.forEach (blk.getLines (), (c,line) -> {
                        if (!c.first ())
                            Br.$ (p);
                        FLine.$ (p, line);
                    });
                });
            }
        });
    }
}
