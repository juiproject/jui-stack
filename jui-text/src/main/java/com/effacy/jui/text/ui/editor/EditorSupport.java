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

import java.util.HashSet;
import java.util.Set;

import com.effacy.jui.core.client.dom.DomSupport;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.Node;
import elemental2.dom.Range;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * Implementation is found in {@code <module-script-base>/jui_text_editor.js}.
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class EditorSupport {

    /**
     * Determines if the passed range is bounded by the given element.
     * 
     * @param el
     *           the element to test whether it bounds (contains) the range.
     * @param r
     *           the range to test.
     * @return {@code true} if the range is bounded.
     */
    @JsOverlay
    public static boolean bounded(Node el, Range r) {
        if ((el == null) || (r == null))
            return false;
        if ((r.startContainer == null) || (r.endContainer == null))
            return false;
        if (r.startContainer == r.endContainer)
            return DomSupport.isChildOf (r.startContainer, el);
        return DomSupport.isChildOf (r.startContainer, el) && DomSupport.isChildOf (r.endContainer, el);
    }

    /**
     * See {@link #insertionPoint(Range)} where the range employed is that of the
     * document selection.
     */
    @JsOverlay
    public static boolean insertionPoint() {
        return insertionPoint (null);
    }

    /**
     * Determines if the passed range selection is an insertion point.
     * <p>
     * This is determined by the existence of the range and that the start and end
     * loci are the same. As a matter of safety if there is a start loci but no end
     * then that too is considered an insertion point.
     * 
     * @param r
     *          the range to test (if this is {@code null} the the first range from
     *          the documents selection is used).
     * @return {@code true} if it is.
     */
    @JsOverlay
    public static boolean insertionPoint(Range r) {
        if (r == null) {
            if (DomGlobal.document.getSelection ().rangeCount == 0)
                return false;
            r = DomGlobal.document.getSelection ().getRangeAt (0);
            if (r == null)
                return false;
        }
        if ((r.startContainer != null) && (r.endContainer == null))
            return true;
        return (r.startContainer == r.endContainer) && (r.startOffset == r.endOffset);
    }

    /**
     * Counts the number of characters contained in the node.
     * <p>
     * Characters are the individual characters contained in each text node that is
     * descedent under the scoping node. In addition line breaks are each counted as
     * one character (the reason for this is that we need to locate positions
     * between consecutive adjacent breaks). This is consistent with the character
     * found for raw content.
     * 
     * @param el
     *           the element to count the characters for.
     * @return the count.
     */
    public static native int numberOfCharacters(Node el);

    /**
     * Obtains the position of the cursor using inter-character positioning (where
     * character positioning is based on the definition used for
     * {@link #numberOfCharacters(Node)}).
     * 
     * @param el
     *           the referencing element (that bounds the sequence of characters).
     * @return the position in the sequence of characters under the referencing
     *         element.
     */
    public static native int positionOfCursor(Node el);

    public static native int positionAtStart(Node el, Range r);

    public static native int positionAtEnd(Node el, Range r);

    /**
     * Positions the cursor at the start of the character sequence in the
     * referencing node.
     * 
     * @param el
     *           the referencing node.
     */
    public static native void positionCursorAtStart(Node el);

    /**
     * Positions the cursor at the end of the character sequence in the
     * referencing node.
     * 
     * @param el
     *           the referencing node.
     */
    public static native void positionCursorAtEnd(Node el);

    public static native void positionCursorAt(Node el, int position);

    public static native boolean isCursorAtEnd(Node el);

    public static native boolean isCursorAtStart(Node el);

    /**
     * Converts the contents of the scoping node into a sequence of lines where each
     * line is demarcated by a line break and the line content contains the
     * indexable text contents (i.e. no formatting).
     * 
     * @param scope
     *              the scoping node.
     * @param stop
     *              (optional) the stop node; text upto (but not include) the node.
     * @return the lines.
     */
    public static native String[] lines(Node scope, Node stop);

    /**
     * After any content change this performs a simple cleaning of the content.
     * <p>
     * This ensures that the content does not end in a single BR and that any
     * leading space is non-breaking.
     * 
     * @param el
     *           the container node.
     */
    public static native void clean(Node el);

    /**
     * Given two nodes finds the common parent of each.
     * 
     * @param el1
     *            first node.
     * @param el2
     *            second node.
     * @return the common parent.
     */
    public static native Node parent(Node el1, Node el2);

    /**
     * Applies a style.
     * 
     * @param scope
     *              the scope of the content region (this is a defined region so is
     *              expected to adhere to the requirements of such).
     * @param range
     *              the range to apply the style to.
     * @param style
     *              the style to apply (as a CSS class).
     * @return a revised version of the range (after formatting).
     */
    public static native Range apply(Node scope, Range range, String style);

    /**
     * Clears a style. This will remove the style for any span within the range. If
     * any of the endpoints of the range lie in a span that includes the style then
     * the span is split and the portion that contains the style is adjusted.
     * <p>
     * See {@link #apply(Node, Range, String)} for details of parameters.
     */
    public static native Range clear(Node scope, Range range, String style);

    /**
     * Determines all the formatting styles that apply to each locii in the range.
     * 
     * @param scope
     *              the scope of the content region (this is a defined region so is
     *              expected to adhere to the requirements of such).
     * @param range
     *              the range to apply calculates the styles from.
     * @return an array of all styles that individually apply each locii in the
     *         range.
     */
    public static native String[] styles(Node scope, Range range);

    /**
     * Returns a set version of {@link #styles(Node, Range)}.
     */
    @JsOverlay
    public static  Set<String> stylesAsSet(Node scope, Range range) {
        Set<String> styles = new HashSet<>();
        for (String style : styles (scope, range))
            styles.add (style);
        return styles;
    }

    /**
     * Callback for sequences of text andy any applicable formatting. See
     * {@link EditorSupport#traverse(Node, TraverseText, TraverseNewLine)}.
     */
    @JsFunction
    public interface TraverseText {
        void result(String content, String[] styles);
    }

    /**
     * Callback for newlines. See
     * {@link EditorSupport#traverse(Node, TraverseText, TraverseNewLine)}.
     */
    @JsFunction
    public interface TraverseNewLine {
        void result();
    }

    /**
     * Traverses the character content under the scope along with any applicable
     * text formatting.
     * 
     * @param scope
     *              the referencing node.
     * @param cb
     *              callback for text events.
     * @param nl
     *              callback for new line events.
     */
    public static native void traverse(Node scope, TraverseText cb, TraverseNewLine nl);

    public static native String latex(Node target, String text, boolean displayMode);

    /**
     * Generates a URL to retrieve an image of the given PlantUML text.
     * 
     * @param baseurl
     *                the base URL for the PlantUML image service.
     * @param text
     *                the UML or DITAA source to be encoded into the URL.
     * @return the URL to be included as a SRC attribute to an image tag.
     */
    public static native String diagram(String baseurl, String text);

    /**
     * Given content from the clipboard, generate a node that contains pastable
     * content.
     * 
     * @param clipboard
     *                  the clipboard contents.
     * @return a node that contains the contents (must be removed once used).
     */
    public static native Element paste(String clipboard);
}
