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
package com.effacy.jui.text.ui.type.markdown;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import com.effacy.jui.text.type.FormattedBlock.BlockType;
import com.effacy.jui.text.type.FormattedLine.FormatType;
import com.effacy.jui.text.type.markdown.IMarkdownEventHandler;
import com.effacy.jui.text.ui.type.FormattedTextStyles;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.HTMLAnchorElement;
import elemental2.dom.HTMLElement;

/**
 * An {@link IMarkdownEventHandler} that builds directly into an Elemental2 DOM
 * element. This is useful for streaming scenarios where content is parsed
 * incrementally and rendered directly into the DOM without an intermediate
 * model.
 * <p>
 * Usage:
 * <pre>
 * Elemental2MarkdownHandler handler = new Elemental2MarkdownHandler(rootEl)
 *     .topHeadingLevel(3);
 * MarkdownParser.parse(handler, true, streamedContent);
 * </pre>
 * <p>
 * The caller should add the CSS class {@code juiFragFText} to the root element
 * for block-level spacing and indent styles.
 *
 * @see IMarkdownEventHandler
 */
public class Elemental2MarkdownHandler implements IMarkdownEventHandler {

    /**
     * The root element to build into.
     */
    private Element root;

    /**
     * Stack of elements for nested block context.
     */
    private Deque<Element> stack = new ArrayDeque<>();

    /**
     * The heading level that markdown H1 maps to (default 1).
     */
    private int topHeadingLevel = 1;

    /**
     * Whether we are on the first line within the current block.
     */
    private boolean firstLineInBlock;

    /**
     * Number of header rows in the current table (from meta).
     */
    private int tableHeaders;

    /**
     * Per-column alignment for the current table (from meta).
     */
    private String[] tableAlign;

    /**
     * Current row index within the current table (0-based).
     */
    private int tableRowIndex;

    /**
     * Current cell index within the current row (0-based).
     */
    private int tableCellIndex;

    /**
     * When {@code true}, use semantic HTML tags ({@code <strong>}, {@code <em>},
     * etc.) for inline formatting instead of {@code <span>} with CSS classes.
     * Format types without a semantic equivalent (e.g. highlight) fall back to
     * spans.
     */
    private boolean semanticTags;

    /**
     * When {@code true}, render lists using proper {@code <ul>}/{@code <li>}
     * elements with structural nesting based on indent level, rather than
     * styled {@code <p>} elements with CSS pseudo-element bullets.
     */
    private boolean semanticLists;

    /**
     * Stack of {@code <ul>} elements representing the current list nesting
     * depth. Only used when {@link #semanticLists} is enabled.
     */
    private Deque<Element> ulStack = new ArrayDeque<>();

    /**
     * Current list nesting depth (0-based). {@code -1} when not inside a list.
     */
    private int listDepth = -1;

    /**
     * The most recently created {@code <li>} element. Used as the parent for
     * nested {@code <ul>} elements when indent increases.
     */
    private Element lastLi;

    /**
     * A {@code <li>} element that has been created but not yet attached to the
     * DOM. Set in {@link #startBlock} for NLIST and resolved in
     * {@link #startLine} once the indent is known.
     */
    private Element pendingLi;

    /**
     * The indent level for the pending list item (from meta).
     */
    private int pendingIndent;

    /**
     * Construct with the root element to build into.
     *
     * @param root
     *             the target element.
     */
    public Elemental2MarkdownHandler(Element root) {
        this.root = root;
    }

    /**
     * Assigns the top heading level. Markdown H1 maps to
     * {@code <h{level}>}, H2 to {@code <h{level+1}>}, etc., capped at H6.
     *
     * @param level
     *              the level (1–6).
     * @return this handler for chaining.
     */
    public Elemental2MarkdownHandler topHeadingLevel(int level) {
        this.topHeadingLevel = Math.max(1, Math.min(6, level));
        return this;
    }

    /**
     * Enables semantic HTML tags for inline formatting. When enabled, formats
     * with a semantic equivalent (bold → {@code <strong>}, italic → {@code <em>},
     * etc.) use the proper tag. Formats without a semantic tag (e.g. highlight)
     * fall back to {@code <span>} with a CSS class.
     *
     * @param semanticTags
     *                     {@code true} to use semantic tags.
     * @return this handler for chaining.
     */
    public Elemental2MarkdownHandler semanticTags(boolean semanticTags) {
        this.semanticTags = semanticTags;
        return this;
    }

    /**
     * Enables semantic list rendering using {@code <ul>}/{@code <li>} elements
     * with structural nesting based on indent level. When disabled (default),
     * lists are rendered as styled {@code <p>} elements with CSS pseudo-element
     * bullets.
     *
     * @param semanticLists
     *                      {@code true} to use semantic list elements.
     * @return this handler for chaining.
     */
    public Elemental2MarkdownHandler semanticLists(boolean semanticLists) {
        this.semanticLists = semanticLists;
        return this;
    }

    @Override
    public void startBlock(BlockType type) {
        // Close any active list context when a non-list block starts.
        if (semanticLists && (type != BlockType.NLIST))
            closeListContext();

        Element el;
        switch (type) {
            case PARA:
                el = createElement("p");
                break;
            case NLIST:
                if (semanticLists) {
                    // Create a detached <li> — it will be attached to the
                    // correct <ul> in resolveListItem() once the indent is
                    // known (from meta).
                    el = createElement("li");
                    pendingLi = el;
                    pendingIndent = 0;
                    stack.push(el);
                    firstLineInBlock = true;
                    return;
                }
                el = createElement("p");
                break;
            case H1:
                el = createHeading(topHeadingLevel);
                break;
            case H2:
                el = createHeading(topHeadingLevel + 1);
                break;
            case H3:
                el = createHeading(topHeadingLevel + 2);
                break;
            case TABLE:
                el = createElement("table");
                tableHeaders = 0;
                tableAlign = null;
                tableRowIndex = 0;
                break;
            case TROW:
                el = createElement("tr");
                tableCellIndex = 0;
                break;
            case TCELL: {
                boolean isHeader = (tableRowIndex < tableHeaders);
                el = createElement(isHeader ? "th" : "td");
                if ((tableAlign != null) && (tableCellIndex < tableAlign.length)) {
                    String align = tableAlign[tableCellIndex];
                    if ("C".equals(align))
                        ((HTMLElement) el).style.set("text-align", "center");
                    else if ("R".equals(align))
                        ((HTMLElement) el).style.set("text-align", "right");
                }
                break;
            }
            default:
                el = createElement("div");
                break;
        }
        String[] styles = FormattedTextStyles.BLOCK_STYLES.get(type);
        if (styles != null) {
            for (String s : styles)
                el.classList.add(s);
        }
        currentTarget().appendChild(el);
        stack.push(el);
        firstLineInBlock = true;
    }

    @Override
    public void endBlock(BlockType type) {
        if (pendingLi != null)
            resolveListItem();
        if (type == TROW)
            tableRowIndex++;
        if (type == TCELL)
            tableCellIndex++;
        stack.pop();
    }

    @Override
    public void meta(String name, String value) {
        if ("headers".equals(name)) {
            try {
                tableHeaders = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                // Ignore.
            }
        } else if ("align".equals(name)) {
            tableAlign = value.split(",");
        } else if ("indent".equals(name)) {
            try {
                int indent = Integer.parseInt(value);
                if (pendingLi != null) {
                    pendingIndent = indent;
                } else if (indent > 0) {
                    stack.peek().classList.add("indent" + indent);
                }
            } catch (NumberFormatException e) {
                // Ignore.
            }
        }
    }

    @Override
    public void startLine() {
        if (pendingLi != null)
            resolveListItem();
        if (!firstLineInBlock) {
            Element br = createElement("br");
            currentTarget().appendChild(br);
        }
        firstLineInBlock = false;
    }

    @Override
    public void endLine() {
        // Nothing to do — content was appended directly.
    }

    @Override
    public void text(String text) {
        currentTarget().appendChild(DomGlobal.document.createTextNode(text));
    }

    @Override
    public void formatted(String text, FormatType format) {
        // Try semantic tag first when enabled.
        if (semanticTags) {
            String tag = FormattedTextStyles.SEMANTIC_TAGS.get(format);
            if (tag != null) {
                Element el = createElement(tag);
                if ((text != null) && !text.isEmpty())
                    el.appendChild(DomGlobal.document.createTextNode(text));
                currentTarget().appendChild(el);
                return;
            }
        }
        // Fall back to <span> with CSS class.
        String css = FormattedTextStyles.LINE_STYLES.get(format);
        if (css == null) {
            text(text);
            return;
        }
        Element span = createElement("span");
        span.classList.add("fmt_" + css);
        if ((text != null) && !text.isEmpty())
            span.appendChild(DomGlobal.document.createTextNode(text));
        currentTarget().appendChild(span);
    }

    @Override
    public void link(String label, String url) {
        HTMLAnchorElement a = (HTMLAnchorElement) createElement("a");
        if ((url != null) && !url.isEmpty()) {
            a.href = url;
            if (url.startsWith("http"))
                a.target = "_blank";
        }
        if ((label != null) && !label.isEmpty())
            a.appendChild(DomGlobal.document.createTextNode(label));
        currentTarget().appendChild(a);
    }

    @Override
    public void variable(String name, Map<String, String> meta) {
        currentTarget().appendChild(DomGlobal.document.createTextNode(name));
    }

    /************************************************************************
     * Internal helpers
     ************************************************************************/

    private static final BlockType TROW = BlockType.TROW;
    private static final BlockType TCELL = BlockType.TCELL;

    /**
     * Returns the current target element for appending content.
     */
    private Element currentTarget() {
        if (stack.isEmpty())
            return root;
        return stack.peek();
    }

    /**
     * Creates a heading element for the given level (capped at 6).
     */
    private Element createHeading(int level) {
        int capped = Math.min(6, Math.max(1, level));
        return createElement("h" + capped);
    }

    /**
     * Creates an element by tag name.
     */
    private Element createElement(String tag) {
        return (Element) DomGlobal.document.createElement(tag);
    }

    /************************************************************************
     * Semantic list helpers
     ************************************************************************/

    /**
     * Attaches the pending {@code <li>} to the correct {@code <ul>} based on
     * the indent level. Creates or removes {@code <ul>} nesting as needed.
     */
    private void resolveListItem() {
        if (ulStack.isEmpty()) {
            // First item — create the root <ul> and append to parent context.
            Element ul = createElement("ul");
            Element li = stack.pop();
            currentTarget().appendChild(ul);
            stack.push(li);
            ulStack.push(ul);
            listDepth = 0;
        }

        // Increase nesting: create nested <ul> inside the last <li>.
        while (listDepth < pendingIndent) {
            Element ul = createElement("ul");
            if (lastLi != null)
                lastLi.appendChild(ul);
            else
                ulStack.peek().appendChild(ul);
            ulStack.push(ul);
            listDepth++;
        }

        // Decrease nesting: pop back to the target depth.
        while (listDepth > pendingIndent) {
            ulStack.pop();
            listDepth--;
        }

        // Attach <li> to the current <ul>.
        ulStack.peek().appendChild(pendingLi);
        lastLi = pendingLi;
        pendingLi = null;
    }

    /**
     * Closes the active list context, resetting all list nesting state.
     */
    private void closeListContext() {
        ulStack.clear();
        listDepth = -1;
        lastLi = null;
        pendingLi = null;
    }
}
