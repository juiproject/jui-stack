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

import java.util.List;

import com.effacy.jui.core.client.dom.builder.Br;
import com.effacy.jui.core.client.dom.builder.ContainerBuilder;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.Fragment;
import com.effacy.jui.core.client.dom.builder.H1;
import com.effacy.jui.core.client.dom.builder.H2;
import com.effacy.jui.core.client.dom.builder.H3;
import com.effacy.jui.core.client.dom.builder.H4;
import com.effacy.jui.core.client.dom.builder.H5;
import com.effacy.jui.core.client.dom.builder.H6;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.core.client.dom.builder.P;
import com.effacy.jui.platform.util.client.Itr;
import com.effacy.jui.text.type.FormattedBlock.BlockType;
import com.effacy.jui.text.type.FormattedLine;
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

    private boolean skipStyle;

    private int topHeadingLevel = 1;

    public FText(FormattedText text, boolean embed) {
        this.text = text;
        this.embed = embed;
    }

    /**
     * Skips applying the default fragment style.
     * 
     * @return this fragment.
     */
    public FText skipStyle() {
        return skipStyle (true);
    }

    /**
     * Skips applying the default fragment style.
     * 
     * @param skipStyle
     *                  {@code true} to skip the style.
     * @return this fragment.
     */
    public FText skipStyle(boolean skipStyle) {
        this.skipStyle = skipStyle;
        return this;
    }

    /**
     * Assigns the top heading level.
     * <p>
     * This is the heading level that the first heading will be rendered as.
     * Subsequent headings are adjusted accordingly.
     * 
     * @param level
     *              the level.
     * @return this fragment.
     */
    public FText topHeadingLevel(int level) {
        this.topHeadingLevel = Math.max(1, level);
        return this;
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
        if (!skipStyle)
            root.style ("juiFragFText");
        _build (root);
    }

    private void _build(ContainerBuilder<?> parent) {
        text.getBlocks ().forEach (blk -> {
            if (blk.typeIs (BlockType.PARA, BlockType.NLIST, BlockType.OLIST)) {
                P.$ (parent).$ (p -> {
                    if (blk.typeIs (BlockType.NLIST))
                        p.style ("list_bullet");
                    if (blk.typeIs (BlockType.OLIST))
                        p.style ("list_number");
                    if (blk.getIndent() > 0)
                        p.style ("indent" + blk.getIndent ());
                    insert(p, blk.getLines ());
                });
            } else if (blk.typeIs (BlockType.H1)) {
                h(parent, topHeadingLevel).$(p -> insert(p, blk.getLines()));
            } else if (blk.typeIs (BlockType.H2)) {
                h(parent, topHeadingLevel + 1).$(p -> insert(p, blk.getLines()));
            } else if (blk.typeIs (BlockType.H3)) {
                h(parent, topHeadingLevel + 2).$(p -> insert(p, blk.getLines()));
            }
        });
    }

    protected ContainerBuilder<?> h(ContainerBuilder<?> p, int level) {
        if (level <= 1)
            return H1.$ (p);
        if (level == 2)
            return H2.$ (p);
        if (level == 3)
            return H3.$ (p);
        if (level == 4)
            return H4.$ (p);
        if (level == 5)
            return H5.$ (p);
        if (level == 6)
            return H6.$ (p);
        return H6.$ (p);
    }

    protected void insert(ContainerBuilder<?> p, List<FormattedLine> lines) {
        Itr.forEach (lines, (c,line) -> {
            if (!c.first ())
                Br.$ (p);
            FLine.$ (p, line);
        });
    }
}
