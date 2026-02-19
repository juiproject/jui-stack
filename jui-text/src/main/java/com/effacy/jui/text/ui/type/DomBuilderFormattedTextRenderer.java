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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

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
import com.effacy.jui.text.type.FormattedBlock;
import com.effacy.jui.text.type.FormattedLine;
import com.effacy.jui.text.type.FormattedText;
import com.effacy.jui.text.type.FormattedBlock.BlockType;
import com.effacy.jui.text.type.FormattedLine.FormatType;
import com.effacy.jui.text.type.FormattedLine.TextSegment;

/**
 * Renders a {@link FormattedText} model into a JUI
 * {@link IDomInsertableContainer} using the builder-pattern DOM API. This is
 * the model-driven counterpart of {@link
 * com.effacy.jui.text.ui.type.markdown.DomBuilderMarkdownHandler} — it
 * walks the pre-built model rather than responding to streaming parser events.
 * <p>
 * Typical usage inside a fragment or component build method:
 * <pre>
 * new DomBuilderFormattedTextRenderer(parent)
 *     .topHeadingLevel(3)
 *     .semanticTags(true)
 *     .semanticLists(true)
 *     .render(formattedText);
 * </pre>
 * <p>
 * The caller should apply the CSS class {@code juiFragFText} to the parent
 * element for block-level spacing and indent styles.
 *
 * @see FormattedText
 * @see FormattedTextStyles
 */
public class DomBuilderFormattedTextRenderer {

    /**
     * The root container to build into.
     */
    private IDomInsertableContainer<?> root;

    /**
     * The heading level that markdown H1 maps to (default 1).
     */
    private int topHeadingLevel = 1;

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
     * Stack of {@code <ul>} builders representing the current list nesting
     * depth. Only used when {@link #semanticLists} is enabled.
     */
    private Deque<ElementBuilder> ulStack = new ArrayDeque<>();

    /**
     * Current list nesting depth (0-based). {@code -1} when not inside a list.
     */
    private int listDepth = -1;

    /**
     * The most recently created {@code <li>} builder. Used as the parent for
     * nested {@code <ul>} elements when indent increases.
     */
    private ElementBuilder lastLi;

    /**
     * Construct with the root container to build into.
     *
     * @param root
     *             the target container.
     */
    public DomBuilderFormattedTextRenderer(IDomInsertableContainer<?> root) {
        this.root = root;
    }

    /**
     * Assigns the top heading level. Markdown H1 maps to
     * {@code <h{level}>}, H2 to {@code <h{level+1}>}, etc., capped at H6.
     *
     * @param level
     *              the level (1–6).
     * @return this renderer for chaining.
     */
    public DomBuilderFormattedTextRenderer topHeadingLevel(int level) {
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
     * @return this renderer for chaining.
     */
    public DomBuilderFormattedTextRenderer semanticTags(boolean semanticTags) {
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
     * @return this renderer for chaining.
     */
    public DomBuilderFormattedTextRenderer semanticLists(boolean semanticLists) {
        this.semanticLists = semanticLists;
        return this;
    }

    /**
     * Renders the given {@link FormattedText} into the root container.
     *
     * @param text
     *             the formatted text to render.
     */
    public void render(FormattedText text) {
        if ((text == null) || text.empty())
            return;
        for (FormattedBlock block : text.getBlocks())
            renderBlock(block);
        closeListContext();
    }

    /************************************************************************
     * Block rendering
     ************************************************************************/

    private void renderBlock(FormattedBlock block) {
        BlockType type = block.getType();

        // Close any active list context when a non-list block starts.
        if (semanticLists && (type != BlockType.NLIST))
            closeListContext();

        ElementBuilder el;
        switch (type) {
            case PARA:
                el = P.$(root);
                applyBlockStyles(el, type);
                applyIndent(el, block.getIndent());
                renderLines(block, el);
                break;
            case H1:
                el = h(root, topHeadingLevel);
                applyBlockStyles(el, type);
                renderLines(block, el);
                break;
            case H2:
                el = h(root, topHeadingLevel + 1);
                applyBlockStyles(el, type);
                renderLines(block, el);
                break;
            case H3:
                el = h(root, topHeadingLevel + 2);
                applyBlockStyles(el, type);
                renderLines(block, el);
                break;
            case NLIST:
                if (semanticLists) {
                    renderSemanticListItem(block);
                } else {
                    el = P.$(root);
                    applyBlockStyles(el, type);
                    applyIndent(el, block.getIndent());
                    renderLines(block, el);
                }
                break;
            case TABLE:
                renderTable(block);
                break;
            default:
                el = Span.$(root);
                renderLines(block, el);
                break;
        }
    }

    /************************************************************************
     * Table rendering
     ************************************************************************/

    private void renderTable(FormattedBlock tableBlock) {
        ElementBuilder table = Table.$(root);

        int headers = 0;
        String[] align = null;
        String headersStr = tableBlock.meta("headers");
        if (headersStr != null) {
            try {
                headers = Integer.parseInt(headersStr);
            } catch (NumberFormatException e) {
                // Ignore.
            }
        }
        String alignStr = tableBlock.meta("align");
        if (alignStr != null)
            align = alignStr.split(",");

        int rowIndex = 0;
        for (FormattedBlock row : tableBlock.getBlocks()) {
            if (row.getType() != BlockType.TROW)
                continue;
            ElementBuilder tr = Tr.$(table);

            int cellIndex = 0;
            for (FormattedBlock cell : row.getBlocks()) {
                if (cell.getType() != BlockType.TCELL)
                    continue;
                boolean isHeader = (rowIndex < headers);
                ElementBuilder td = isHeader ? Th.$(tr) : Td.$(tr);
                applyBlockStyles(td, BlockType.TCELL);
                if ((align != null) && (cellIndex < align.length)) {
                    if ("C".equals(align[cellIndex]))
                        td.css("text-align", "center");
                    else if ("R".equals(align[cellIndex]))
                        td.css("text-align", "right");
                }
                renderLines(cell, td);
                cellIndex++;
            }
            rowIndex++;
        }
    }

    /************************************************************************
     * Semantic list rendering
     ************************************************************************/

    private void renderSemanticListItem(FormattedBlock block) {
        int indent = block.getIndent();

        if (ulStack.isEmpty()) {
            ElementBuilder ul = Custom.$(root, "ul");
            ulStack.push(ul);
            listDepth = 0;
        }

        // Increase nesting: create nested <ul> inside the last <li>.
        while (listDepth < indent) {
            ElementBuilder ul = Custom.$("ul");
            if (lastLi != null)
                lastLi.insert(ul);
            else
                ulStack.peek().insert(ul);
            ulStack.push(ul);
            listDepth++;
        }

        // Decrease nesting: pop back to the target depth.
        while (listDepth > indent) {
            ulStack.pop();
            listDepth--;
        }

        ElementBuilder li = Custom.$(ulStack.peek(), "li");
        lastLi = li;
        renderLines(block, li);
    }

    /************************************************************************
     * Line and segment rendering
     ************************************************************************/

    private void renderLines(FormattedBlock block, IDomInsertableContainer<?> target) {
        List<FormattedLine> lines = block.getLines();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0)
                Br.$(target);
            renderLine(lines.get(i), target);
        }
    }

    private void renderLine(FormattedLine line, IDomInsertableContainer<?> target) {
        for (TextSegment segment : line.sequence())
            renderSegment(segment, target);
    }

    private void renderSegment(TextSegment segment, IDomInsertableContainer<?> target) {
        String text = segment.text();

        // Variable — render as plain text.
        if (segment.variable()) {
            Text.$(target, text);
            return;
        }

        FormatType[] formats = segment.formatting();
        String link = segment.link();

        // Collect effective formats (excluding the link marker).
        List<FormatType> effectiveFormats = new ArrayList<>();
        for (FormatType fmt : formats) {
            if (fmt != FormatType.A)
                effectiveFormats.add(fmt);
        }

        // Plain text — no formatting, no link.
        if (effectiveFormats.isEmpty() && (link == null)) {
            if ((text != null) && !text.isEmpty())
                Text.$(target, text);
            return;
        }

        // Link wrapping.
        if ((link != null) && !link.isEmpty()) {
            ElementBuilder a;
            if (link.startsWith("http")) {
                a = A.$(target, link);
                a.attr("target", "_blank");
            } else {
                a = A.$(target, link);
            }
            if (effectiveFormats.isEmpty()) {
                if ((text != null) && !text.isEmpty())
                    a.text(text);
            } else {
                renderFormattedContent(text, effectiveFormats, a);
            }
            return;
        }

        // Formatted content without link.
        renderFormattedContent(text, effectiveFormats, target);
    }

    /**
     * Renders formatted text into the target, using semantic tags or CSS
     * classes depending on configuration.
     */
    private void renderFormattedContent(String text, List<FormatType> formats, IDomInsertableContainer<?> target) {
        if (semanticTags) {
            // Separate into formats with semantic tags and those without.
            List<String> tags = new ArrayList<>();
            List<String> cssClasses = new ArrayList<>();
            for (FormatType fmt : formats) {
                String tag = FormattedTextStyles.SEMANTIC_TAGS.get(fmt);
                if (tag != null) {
                    tags.add(tag);
                } else {
                    String css = FormattedTextStyles.LINE_STYLES.get(fmt);
                    if (css != null)
                        cssClasses.add("fmt_" + css);
                }
            }

            // Build nested semantic tags.
            IDomInsertableContainer<?> current = target;
            for (String tag : tags) {
                ElementBuilder el = Custom.$(current, tag);
                current = el;
            }

            // If CSS-only formats remain, wrap in a span.
            if (!cssClasses.isEmpty()) {
                ElementBuilder span = Span.$(current);
                for (String cls : cssClasses)
                    span.style(cls);
                current = span;
            }

            if ((text != null) && !text.isEmpty())
                Text.$(current, text);
        } else {
            // CSS-only mode — single span with all format classes.
            ElementBuilder span = Span.$(target);
            for (FormatType fmt : formats) {
                String css = FormattedTextStyles.LINE_STYLES.get(fmt);
                if (css != null)
                    span.style("fmt_" + css);
            }
            if ((text != null) && !text.isEmpty())
                span.text(text);
        }
    }

    /************************************************************************
     * Internal helpers
     ************************************************************************/

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

    private void applyBlockStyles(ElementBuilder el, BlockType type) {
        String[] styles = FormattedTextStyles.BLOCK_STYLES.get(type);
        if (styles != null)
            el.style(styles);
    }

    private void applyIndent(ElementBuilder el, int indent) {
        if (indent > 0)
            el.style("indent" + indent);
    }

    /************************************************************************
     * Semantic list helpers
     ************************************************************************/

    /**
     * Closes the active list context, resetting all list nesting state.
     */
    private void closeListContext() {
        ulStack.clear();
        listDepth = -1;
        lastLi = null;
    }
}
