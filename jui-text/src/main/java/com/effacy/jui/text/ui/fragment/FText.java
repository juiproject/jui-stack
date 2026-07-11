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

import com.effacy.jui.core.client.dom.builder.ContainerBuilder;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.Fragment;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.text.type.FormattedText;
import com.effacy.jui.text.ui.type.ContentStyle;
import com.effacy.jui.text.ui.type.DomBuilderFormattedTextRenderer;

/**
 * Renders {@link FormattedText} read-only, using the shared {@link DomBuilderFormattedTextRenderer}
 * (the single model→DOM renderer) scoped by the {@code richtext} content stylesheet — so a
 * fragment, a chat bubble and the editor all present formatted text identically.
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

    private ContentStyle contentStyle = ContentStyle.document ();

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
     * Assigns the content style (spacing / density) applied to the rendered text.
     * <p>
     * Defaults to {@link ContentStyle#document()} (roomier, document-like presentation).
     * Pass {@link ContentStyle#compact()} for a tight, field-sized rendering, or a custom
     * style. Has no effect when {@link #skipStyle()} is set (the caller then owns styling).
     *
     * @param contentStyle
     *                     the content style (a {@code null} is treated as
     *                     {@link ContentStyle#compact()}).
     * @return this fragment.
     */
    public FText contentStyle(ContentStyle contentStyle) {
        this.contentStyle = (contentStyle == null) ? ContentStyle.compact () : contentStyle;
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
        // apply() scopes the root with the richtext class AND layers the style's overrides.
        if (!skipStyle)
            (contentStyle != null ? contentStyle : ContentStyle.compact ()).apply (root);
        _build (root);
    }

    private void _build(ContainerBuilder<?> parent) {
        new DomBuilderFormattedTextRenderer (parent)
            .topHeadingLevel (topHeadingLevel)
            .render (text);
    }
}
