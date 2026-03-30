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
package com.effacy.jui.core.client.dom.builder;

import java.util.function.Supplier;

import com.effacy.jui.core.client.MarkupParser;

import elemental2.dom.DomGlobal;
import elemental2.dom.Node;


/**
 * A very simple mechanism for displaying marked up content.
 * <p>
 * For more complex formatting consider using {@code FormattedText} and
 * {@code MarkdownParser}.
 */
public class MarkupBuilder extends NodeBuilder<MarkupBuilder> {

    /**
     * Supplies the contents for the node.
     */
    private Supplier<String> content;

    /**
     * Construct with the text contents.
     * 
     * @param content
     *                the contents.
     */
    public MarkupBuilder(String content) {
        this (() -> content);
    }

    /**
     * Construct with the text contents.
     * 
     * @param content
     *                the contents (via a supplier).
     */
    public MarkupBuilder(Supplier<String> content) {
        this.content = content;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.uticorel.client.dom.DomBuilder.NodeBuilder#_nodeImpl(Node, BuildContext)
     */
    @Override
    protected Node _nodeImpl(Node parent, BuildContext ctx) {
        if (content == null)
            return DomGlobal.document.createTextNode ("");
        String text = content.get();
        if (text == null)
            return DomGlobal.document.createTextNode ("");
        if (!text.contains("*")) {
            appendTextWithNewlines(parent, text);
            return DomGlobal.document.createTextNode ("");
        }
        for (MarkupParser.Block blk : MarkupParser.parse(text)) {
            if (blk.isPlain()) {
                appendTextWithNewlines(parent, blk.text());
            } else {
                Node p = parent;
                if (blk.bold()) {
                    p = DomGlobal.document.createElement("strong");
                    parent.appendChild(p);
                }
                if (blk.italic()) {
                    Node i = DomGlobal.document.createElement("i");
                    p.appendChild(i);
                    p = i;
                }
                appendTextWithNewlines(p, blk.text());
            }
        }
        return DomGlobal.document.createTextNode ("");
    }

    /**
     * Appends text to the given parent node, replacing newline characters with
     * {@code <br>} elements.
     */
    private void appendTextWithNewlines(Node parent, String text) {
        String[] lines = text.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            if (i > 0)
                parent.appendChild(DomGlobal.document.createElement("br"));
            if (!lines[i].isEmpty())
                parent.appendChild(DomGlobal.document.createTextNode(lines[i]));
        }
    }

}
