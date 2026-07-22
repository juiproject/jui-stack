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
package com.effacy.jui.text.ui.type;

import java.util.LinkedHashMap;
import java.util.Map;

import com.effacy.jui.core.client.dom.builder.ElementBuilder;

import elemental2.dom.HTMLElement;

/**
 * The presentation of a block of formatted text — an open bag of
 * {@code --jui-richtext-*} token overrides applied inline on the content root
 * (so they win over the shared {@link FormattedTextStyles} defaults, for that
 * subtree only).
 * <p>
 * It is not bound to any one surface: the same style can be handed to the
 * editor or {@link #apply(ElementBuilder) applied} to a read-only presentation
 * (an {@link ElementBuilder} root, e.g. via {@code FText} or a
 * {@link DomBuilderFormattedTextRenderer}). Both surfaces read the same tokens
 * from the shared stylesheet, so a style means the same thing everywhere.
 * <p>
 * It is not a closed set: build your own with {@code new ContentStyle()} and the
 * fluent setters (or {@link #token(String, String)} for any content token, e.g.
 * {@code --jui-richtext-h2-size}), or start from a provided standard
 * ({@link #compact()} / {@link #document()}) and tweak it.
 * <pre>
 *   ContentStyle.document()                                    // a standard configuration
 *   ContentStyle.document().blockSpacing("8px")                // … tweaked
 *   new ContentStyle().listIndent("2em").lineHeight("1.7")     // your own
 * </pre>
 * The compact look is simply "no overrides" (the stylesheet defaults); the
 * document look is a standard bundle of overrides — no special class or flag,
 * just token values.
 */
public class ContentStyle {

    private final LinkedHashMap<String, String> tokens = new LinkedHashMap<> ();

    /**
     * An empty style — the stylesheet's own (compact) defaults. Add overrides
     * with the fluent setters to build your own.
     */
    public ContentStyle() {
        // Empty.
    }

    /**
     * The standard <b>compact</b> configuration: the stylesheet defaults (tight
     * spacing, base-level lists flush). Equivalent to {@code new ContentStyle()};
     * provided for symmetry and intent.
     */
    public static ContentStyle compact() {
        return new ContentStyle ();
    }

    /**
     * The standard <b>document</b> configuration: roomier spacing and base-level
     * lists indented. A convenience bundle of overrides — copy and tweak, or
     * build your own.
     */
    public static ContentStyle document() {
        return new ContentStyle ()
            .listIndent ("1.5em")
            .blockSpacing ("5px")
            .paragraphSpacing ("0.6em")
            .lineHeight ("1.6");
    }

    /**
     * Overrides an arbitrary content token (e.g.
     * {@code token("--jui-richtext-h2-size", "1.7em")}).
     *
     * @param name
     *              the custom property name (including the leading {@code --}).
     * @param value
     *              the value.
     * @return this instance.
     */
    public ContentStyle token(String name, String value) {
        if ((name != null) && (value != null))
            tokens.put (name, value);
        return this;
    }

    /** The base-level list indent (e.g. {@code "1.5em"}; {@code "0"} keeps lists flush). */
    public ContentStyle listIndent(String value) {
        return token ("--jui-richtext-list-indent", value);
    }

    /**
     * The vertical padding on each list item (e.g. {@code "3px"}) — the gap
     * between list items. Kept independent of {@link #blockSpacing(String)} so
     * lists stay tight even when the prose spacing is roomy.
     */
    public ContentStyle listSpacing(String value) {
        return token ("--jui-richtext-list-spacing", value);
    }

    /** Vertical spacing between blocks (e.g. {@code "5px"}). */
    public ContentStyle blockSpacing(String value) {
        return token ("--jui-richtext-block-spacing", value);
    }

    /** Space below a paragraph (e.g. {@code "0.6em"}). */
    public ContentStyle paragraphSpacing(String value) {
        return token ("--jui-richtext-para-spacing", value);
    }

    /** Content line-height (e.g. {@code "1.6"}). */
    public ContentStyle lineHeight(String value) {
        return token ("--jui-richtext-line-height", value);
    }

    /**
     * Sets a builder root up as a formatted-text content root (the read-only /
     * presentation path): applies the {@code richtext} scope class from
     * {@link FormattedTextStyles} (which also injects the sheet) and layers this
     * style's token overrides on top. One call is all a content root needs.
     *
     * @param root
     *             the content root builder.
     * @return the supplied root (for chaining).
     */
    public ElementBuilder apply(ElementBuilder root) {
        if (root != null) {
            root.style (FormattedTextStyles.styles ().richtext ());
            for (Map.Entry<String, String> token : tokens.entrySet ())
                root.css (token.getKey (), token.getValue ());
        }
        return root;
    }

    /**
     * Sets a live DOM element up as a formatted-text content root (the editor
     * path, where the root is an element rather than a builder): applies the
     * {@code richtext} scope class from {@link FormattedTextStyles} (which also
     * injects the sheet) and layers this style's token overrides on top.
     *
     * @param root
     *             the content root element.
     */
    public void apply(HTMLElement root) {
        if (root != null) {
            root.classList.add (FormattedTextStyles.styles ().richtext ());
            for (Map.Entry<String, String> token : tokens.entrySet ())
                root.style.setProperty (token.getKey (), token.getValue ());
        }
    }
}
