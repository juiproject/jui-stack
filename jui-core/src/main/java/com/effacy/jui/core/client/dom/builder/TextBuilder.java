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
package com.effacy.jui.core.client.dom.builder;

import java.util.function.Supplier;

import com.effacy.jui.platform.util.client.StringSupport;

import elemental2.dom.DomGlobal;
import elemental2.dom.Node;
import elemental2.dom.Text;

/**
 * Builds out a text node.
 */
public class TextBuilder extends NodeBuilder<TextBuilder> {

    /**
     * Supplies the contents for the node.
     */
    private Supplier<String> content;

    /**
     * See {@link #split(boolean)}.
     */
    private boolean split;

    /**
     * Construct with the text contents.
     * 
     * @param content
     *                the contents.
     */
    public TextBuilder(String content) {
        this (() -> content);
    }

    /**
     * Construct with the text contents.
     * 
     * @param content
     *                the contents (via a supplier).
     */
    public TextBuilder(Supplier<String> content) {
        this.content = content;
    }

    /**
     * Convenience to call {@link #split(boolean)} passing {@code true}.
     * 
     * @return this builder instance.
     */
    public TextBuilder split() {
        return split (true);
    }

    /**
     * Determines if the content should be split across multiple lines (based on
     * encountering a `\r`).
     * 
     * @param split
     *              {@code true} if to split.
     * @return this builder instance.
     */
    public TextBuilder split(boolean split) {
        this.split = split;
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.uticorel.client.dom.DomBuilder.NodeBuilder#_nodeImpl(Node, BuildContext)
     */
    @Override
    protected Text _nodeImpl(Node parent, BuildContext ctx) {
        if (content == null)
            return DomGlobal.document.createTextNode ("");
        if (!split)
            return DomGlobal.document.createTextNode (StringSupport.safe (content.get ()));
        String [] parts = StringSupport.split (content.get ());
        for (int i = 0; i < parts.length; i++) {
            if (i > 0)
                parent.appendChild (DomGlobal.document.createElement ("br"));
            if (StringSupport.empty (parts[0]))
                continue;
            Text node = DomGlobal.document.createTextNode (StringSupport.safe (parts[i]));
            if (i >= (parts.length - 1))
                return node;
            parent.appendChild (node);
        }
        return DomGlobal.document.createTextNode (""); 
    }

}
