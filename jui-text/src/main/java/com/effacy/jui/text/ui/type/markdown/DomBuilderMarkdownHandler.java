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

import com.effacy.jui.core.client.dom.builder.A;
import com.effacy.jui.core.client.dom.builder.Br;
import com.effacy.jui.core.client.dom.builder.Custom;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.H1;
import com.effacy.jui.core.client.dom.builder.H2;
import com.effacy.jui.core.client.dom.builder.H3;
import com.effacy.jui.core.client.dom.builder.H4;
import com.effacy.jui.core.client.dom.builder.H5;
import com.effacy.jui.core.client.dom.builder.H6;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.core.client.dom.builder.P;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.builder.Table;
import com.effacy.jui.core.client.dom.builder.Td;
import com.effacy.jui.core.client.dom.builder.Text;
import com.effacy.jui.core.client.dom.builder.Th;
import com.effacy.jui.core.client.dom.builder.Tr;
import com.effacy.jui.text.type.FormattedBlock.BlockType;
import com.effacy.jui.text.type.FormattedLine.FormatType;
import com.effacy.jui.text.type.markdown.IMarkdownEventHandler;
import com.effacy.jui.text.ui.type.FormattedTextStyles;

/**
 * An {@link IMarkdownEventHandler} that builds into a JUI
 * {@link IDomInsertableContainer}. This is the builder-pattern counterpart of
 * {@link Elemental2MarkdownHandler} — it produces the same DOM structure but
 * via declarative JUI builders rather than direct Elemental2 manipulation.
 * <p>
 * Typical usage inside a fragment or component build method:
 * <pre>
 * DomBuilderMarkdownHandler handler = new DomBuilderMarkdownHandler(parent)
 *     .topHeadingLevel(3);
 * MarkdownParser.parse(handler, markdownContent);
 * </pre>
 * <p>
 * The caller should apply the CSS class {@code juiFragFText} to the parent
 * element for block-level spacing and indent styles.
 *
 * @see IMarkdownEventHandler
 * @see Elemental2MarkdownHandler
 * @see FText
 * @see FLine
 */
public class DomBuilderMarkdownHandler implements IMarkdownEventHandler {

    /**
     * The root container to build into.
     */
    private IDomInsertableContainer<?> root;

    /**
     * Stack of containers for nested block context.
     */
    private Deque<IDomInsertableContainer<?>> stack = new ArrayDeque<>();

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
     * Stack of list wrapper ({@code <ul>} or {@code <ol>}) builders
     * representing the current list nesting depth. Only used when
     * {@link #semanticLists} is enabled.
     */
    private Deque<ElementBuilder> ulStack = new ArrayDeque<>();

    /**
     * Current list nesting depth (0-based). {@code -1} when not inside a list.
     */
    private int listDepth = -1;

    /**
     * Whether the current list context is ordered ({@code <ol>}) or unordered
     * ({@code <ul>}). Only meaningful when {@link #ulStack} is non-empty.
     */
    private boolean listOrdered;

    /**
     * The most recently created {@code <li>} builder. Used as the parent for
     * nested list elements when indent increases.
     */
    private ElementBuilder lastLi;

    /**
     * A {@code <li>} builder that has been created but not yet inserted into a
     * list wrapper. Set in {@link #startBlock} for NLIST/OLIST and resolved in
     * {@link #startLine} once the indent is known.
     */
    private ElementBuilder pendingLi;

    /**
     * The indent level for the pending list item (from meta).
     */
    private int pendingIndent;

    /**
     * Whether the pending list item is ordered.
     */
    private boolean pendingOrdered;

    /**
     * Construct with the root container to build into.
     *
     * @param root
     *             the target container.
     */
    public DomBuilderMarkdownHandler(IDomInsertableContainer<?> root) {
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
    public DomBuilderMarkdownHandler topHeadingLevel(int level) {
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
    public DomBuilderMarkdownHandler semanticTags(boolean semanticTags) {
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
    public DomBuilderMarkdownHandler semanticLists(boolean semanticLists) {
        this.semanticLists = semanticLists;
        return this;
    }

    @Override
    public void startBlock(BlockType type) {
        // Close any active list context when a non-list block starts, or
        // when the list type changes (unordered → ordered or vice versa).
        if (semanticLists) {
            boolean isList = (type == BlockType.NLIST) || (type == BlockType.OLIST);
            if (!isList) {
                closeListContext();
            } else if (!ulStack.isEmpty() && (listOrdered != (type == BlockType.OLIST))) {
                closeListContext();
            }
        }

        IDomInsertableContainer<?> target = currentTarget();
        ElementBuilder el;
        switch (type) {
            case PARA:
                el = P.$(target);
                break;
            case NLIST:
            case OLIST:
                if (semanticLists) {
                    // Create a detached <li> — it will be inserted into the
                    // correct <ol>/<ul> in resolveListItem() once the indent
                    // is known (from meta).
                    ElementBuilder li = Custom.$("li");
                    pendingLi = li;
                    pendingIndent = 0;
                    pendingOrdered = (type == BlockType.OLIST);
                    stack.push(li);
                    firstLineInBlock = true;
                    return;
                }
                el = P.$(target);
                break;
            case H1:
                el = h(target, topHeadingLevel);
                break;
            case H2:
                el = h(target, topHeadingLevel + 1);
                break;
            case H3:
                el = h(target, topHeadingLevel + 2);
                break;
            case TABLE:
                el = Table.$(target);
                tableHeaders = 0;
                tableAlign = null;
                tableRowIndex = 0;
                break;
            case TROW:
                el = Tr.$(target);
                tableCellIndex = 0;
                break;
            case TCELL: {
                boolean isHeader = (tableRowIndex < tableHeaders);
                el = isHeader ? Th.$(target) : Td.$(target);
                if ((tableAlign != null) && (tableCellIndex < tableAlign.length)) {
                    String align = tableAlign[tableCellIndex];
                    if ("C".equals(align))
                        el.css("text-align", "center");
                    else if ("R".equals(align))
                        el.css("text-align", "right");
                }
                break;
            }
            default:
                el = Span.$(target);
                break;
        }
        String[] styles = FormattedTextStyles.BLOCK_STYLES.get(type);
        if (styles != null)
            el.style(styles);
        stack.push(el);
        firstLineInBlock = true;
    }

    @Override
    public void endBlock(BlockType type) {
        if (pendingLi != null)
            resolveListItem();
        if (type == BlockType.TROW)
            tableRowIndex++;
        if (type == BlockType.TCELL)
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
                } else if ((indent > 0) && (stack.peek() instanceof ElementBuilder)) {
                    ((ElementBuilder) stack.peek()).style("indent" + indent);
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
        if (!firstLineInBlock)
            Br.$(currentTarget());
        firstLineInBlock = false;
    }

    @Override
    public void endLine() {
        // Nothing to do — content was appended directly.
    }

    @Override
    public void text(String text) {
        Text.$(currentTarget(), text);
    }

    @Override
    public void formatted(String text, FormatType format) {
        // Try semantic tag first when enabled.
        if (semanticTags) {
            String tag = FormattedTextStyles.SEMANTIC_TAGS.get(format);
            if (tag != null) {
                ElementBuilder el = Custom.$(currentTarget(), tag);
                if ((text != null) && !text.isEmpty())
                    el.text(text);
                return;
            }
        }
        // Fall back to <span> with CSS class.
        String css = FormattedTextStyles.LINE_STYLES.get(format);
        if (css == null) {
            text(text);
            return;
        }
        ElementBuilder span = Span.$(currentTarget());
        span.style("fmt_" + css);
        if ((text != null) && !text.isEmpty())
            span.text(text);
    }

    @Override
    public void link(String label, String url) {
        IDomInsertableContainer<?> target = currentTarget();
        if ((url == null) || url.isEmpty()) {
            ElementBuilder a = A.$(target);
            if ((label != null) && !label.isEmpty())
                a.text(label);
        } else if (url.startsWith("http")) {
            ElementBuilder a = A.$(target, url);
            a.attr("target", "_blank");
            if ((label != null) && !label.isEmpty())
                a.text(label);
        } else {
            ElementBuilder a = A.$(target, url);
            if ((label != null) && !label.isEmpty())
                a.text(label);
        }
    }

    @Override
    public void variable(String name, Map<String, String> meta) {
        Text.$(currentTarget(), name);
    }

    /************************************************************************
     * Internal helpers
     ************************************************************************/

    /**
     * Returns the current target container for appending content.
     */
    private IDomInsertableContainer<?> currentTarget() {
        if (stack.isEmpty())
            return root;
        return stack.peek();
    }

    /**
     * Creates a heading element for the given level (capped at 6) and inserts
     * it into the given parent.
     */
    private ElementBuilder h(IDomInsertableContainer<?> parent, int level) {
        if (level <= 1)
            return H1.$(parent);
        if (level == 2)
            return H2.$(parent);
        if (level == 3)
            return H3.$(parent);
        if (level == 4)
            return H4.$(parent);
        if (level == 5)
            return H5.$(parent);
        return H6.$(parent);
    }

    /************************************************************************
     * Semantic list helpers
     ************************************************************************/

    /**
     * Attaches the pending {@code <li>} to the correct list wrapper based on
     * the indent level. Creates or removes nesting as needed.
     */
    private void resolveListItem() {
        String listTag = pendingOrdered ? "ol" : "ul";
        if (ulStack.isEmpty()) {
            // First item — create the root <ol>/<ul> and insert into parent.
            ElementBuilder ul = Custom.$(listTag);
            IDomInsertableContainer<?> li = stack.pop();
            currentTarget().insert(ul);
            stack.push(li);
            ulStack.push(ul);
            listDepth = 0;
            listOrdered = pendingOrdered;
        }

        // Increase nesting: create nested <ol>/<ul> inside the last <li>.
        while (listDepth < pendingIndent) {
            ElementBuilder ul = Custom.$(listTag);
            if (lastLi != null)
                lastLi.insert(ul);
            else
                ulStack.peek().insert(ul);
            ulStack.push(ul);
            listDepth++;
        }

        // Decrease nesting: pop back to the target depth.
        while (listDepth > pendingIndent) {
            ulStack.pop();
            listDepth--;
        }

        // Attach <li> to the current <ul>.
        ulStack.peek().insert(pendingLi);
        lastLi = pendingLi;
        pendingLi = null;
    }

    /**
     * Closes the active list context, resetting all list nesting state.
     */
    private void closeListContext() {
        ulStack.clear();
        listDepth = -1;
        listOrdered = false;
        lastLi = null;
        pendingLi = null;
    }
}
