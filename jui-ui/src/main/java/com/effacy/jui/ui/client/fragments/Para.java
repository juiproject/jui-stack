/*******************************************************************************
 * Copyright 2025 Jeremy Buckley
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
package com.effacy.jui.ui.client.fragments;

import com.effacy.jui.core.client.dom.builder.Br;
import com.effacy.jui.core.client.dom.builder.ContainerBuilder;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.core.client.dom.builder.P;
import com.effacy.jui.core.client.dom.builder.Text;
import com.effacy.jui.platform.util.client.StringSupport;

/**
 * Fragment that renders text into paragraph ({@code <p>}) elements with
 * intelligent line break handling.
 * <p>
 * A single line feed inserts a {@code <br>} within the current paragraph. Two
 * or more consecutive line feeds create a new {@code <p>} element.
 * <p>
 * Since this fragment may produce multiple top-level {@code <p>} elements,
 * there is no single root and adornments ({@code .css()}, {@code .style()}) are
 * not supported.
 */
public class Para {

    /**
     * Create a new fragment with the given text.
     * 
     * @param text
     *             the text to render, may contain line feeds for paragraph and line
     *             breaks.
     * @return the new fragment.
     */
    public static ParaFragment $(String text) {
        return new ParaFragment(text);
    }

    /**
     * Create a new fragment with the given text and insert it into the given
     * parent.
     * 
     * @param parent
     *             the parent to insert into, may be null.
     * @param text
     *             the text to render, may contain line feeds for paragraph and line
     *             breaks.
     * @return the new fragment.
     */
    public static ParaFragment $(IDomInsertableContainer<?> parent, String text) {
        ParaFragment frg = $(text);
        if (parent != null)
            parent.insert(frg);
        return frg;
    }

    /**
     * Fragment implementation.
     */
    public static class ParaFragment extends BaseFragment<ParaFragment> {

        /**
         * See {@link #ParaFragment(String)}.
         */
        private String text;

        /**
         * Create a new fragment with the given text.
         * 
         * @param text
         *             the text to render, may contain line feeds for paragraph and line
         *             breaks.
         */
        public ParaFragment(String text) {
            this.text = text;
        }

        @Override
        public void build(ContainerBuilder<?> parent) {
            if ((conditional != null) && !conditional.get())
                return;
            if (StringSupport.empty(text))
                return;

            // Normalize line endings.
            String normalized = text.replace("\r\n", "\n").replace("\r", "\n");

            // Split on two or more newlines for paragraphs.
            String[] paragraphs = normalized.split("\n{2,}");
            for (String paragraph : paragraphs) {
                if (StringSupport.empty(paragraph))
                    continue;
                P.$(parent).$(p -> {
                    String[] lines = paragraph.split("\n");
                    for (int i = 0; i < lines.length; i++) {
                        if (i > 0)
                            Br.$(p);
                        Text.$(p, lines[i]);
                    }
                });
            }
        }
    }
}
