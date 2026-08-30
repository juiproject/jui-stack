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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.effacy.jui.core.client.dom.builder.A;
import com.effacy.jui.core.client.dom.builder.Br;
import com.effacy.jui.core.client.dom.builder.Custom;
import com.effacy.jui.core.client.dom.builder.Div;
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
import com.effacy.jui.text.ui.editor.Fences;
import com.effacy.jui.text.ui.editor.IFenceRenderer;

import elemental2.dom.Element;
import jsinterop.base.Js;

/**
 * Renders a {@link FormattedText} model into a JUI
 * {@link IDomInsertableContainer} using the builder-pattern DOM API. This is
 * the model-driven counterpart of {@link
 * com.effacy.jui.text.ui.type.builder.DomBuilderBuilder} — it
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
 * The parent element must be a formatted-text content root: apply a {@link ContentStyle} to
 * it — {@code ContentStyle.document().apply(root)} — which scopes it with the {@code richtext}
 * class (so the block, heading, list, quote, code and inline-format styles resolve) <em>and</em>
 * selects the presentation density (compact vs document spacing). This is the same style the
 * editor accepts, so both surfaces present identically. (The {@code FText} fragment does this
 * for you.) For a bare scope with no density overrides, use {@code ContentStyle.compact().apply(root)}.
 *
 * @see FormattedText
 * @see FormattedTextStyles
 * @see ContentStyle
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
     * Slug ids assigned to headings so far in this render, with a running count for
     * de-duplication (a repeated heading text gets {@code slug-1}, {@code slug-2}, …).
     * These make headings targetable by in-page {@code #anchor} links (see
     * {@link LinkHandlers#slug(String)}). Reset on each {@link #render(FormattedText)}.
     */
    private Map<String, Integer> headingSlugs = new HashMap<>();

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
        headingSlugs.clear();
        resetListCounters();
        prevWasOlist = false;
        prevOlistIndent = 0;
        for (FormattedBlock block : text.getBlocks())
            renderBlock(block);
        closeListContext();
    }

    /************************************************************************
     * Ordered-list numbering.
     *
     * In semantic mode the browser numbers real <ol> items. Outside it — which
     * is the default, and what FText uses — an ordered item is a paragraph
     * carrying .list_number, and its marker is drawn by CSS from the
     * data-list-index attribute set here. Without it an ordered list renders as
     * indented text with no numbers at all.
     *
     * The counter semantics mirror the editor's StandardBlockHandler exactly,
     * because the same document has to number the same way whether it is being
     * edited or read: a run of ordered items keeps one sequence, anything else
     * between them starts a new one, and descending a level resets the deeper
     * counters while leaving the shallower ones alone.
     ************************************************************************/

    private final int[] listCounters = new int[6];

    private boolean prevWasOlist;

    private int prevOlistIndent;

    private void resetListCounters() {
        for (int i = 0; i < listCounters.length; i++)
            listCounters[i] = 0;
    }

    /**
     * Advances the counters for an ordered item and returns its marker.
     */
    private String nextListIndex(int indent) {
        int ind = Math.max(0, Math.min(indent, listCounters.length - 1));
        if (!prevWasOlist)
            resetListCounters();
        else if (ind > prevOlistIndent) {
            for (int i = prevOlistIndent + 1; i < listCounters.length; i++)
                listCounters[i] = 0;
        }
        listCounters[ind]++;
        prevOlistIndent = ind;
        prevWasOlist = true;
        return ListIndex.format(ind, listCounters[ind]);
    }

    /************************************************************************
     * Block rendering
     ************************************************************************/

    private void renderBlock(FormattedBlock block) {
        BlockType type = block.getType();

        // Anything that is not an ordered item ends the run, so the next ordered
        // list starts from one again. Matches the editor.
        if (type != BlockType.OLIST)
            prevWasOlist = false;

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
                applyHeadingId(el, block);
                renderLines(block, el);
                break;
            case H2:
                el = h(root, topHeadingLevel + 1);
                applyBlockStyles(el, type);
                applyHeadingId(el, block);
                renderLines(block, el);
                break;
            case H3:
                el = h(root, topHeadingLevel + 2);
                applyBlockStyles(el, type);
                applyHeadingId(el, block);
                renderLines(block, el);
                break;
            case H4:
                el = h(root, topHeadingLevel + 3);
                applyBlockStyles(el, type);
                applyHeadingId(el, block);
                renderLines(block, el);
                break;
            case H5:
                el = h(root, topHeadingLevel + 4);
                applyBlockStyles(el, type);
                applyHeadingId(el, block);
                renderLines(block, el);
                break;
            case NLIST:
            case OLIST:
                if (semanticLists) {
                    renderSemanticListItem(block);
                } else {
                    el = P.$(root);
                    applyBlockStyles(el, type);
                    applyIndent(el, block.getIndent());
                    // The marker CSS reads this; without it an ordered list is
                    // indented text with nothing in front of it.
                    if (type == BlockType.OLIST)
                        el.attr("data-list-index", nextListIndex(block.getIndent()));
                    renderLines(block, el);
                }
                break;
            case QUOTE:
                el = Custom.$(root, "blockquote");
                applyBlockStyles(el, type);
                renderLines(block, el);
                break;
            case FENCE: {
                // Registry-driven rendering (matching the editor's FenceBlockHandler): a
                // registered renderer produces the rich representation; otherwise fall back
                // to showing the fenced source as a code block.
                String info = block.meta("info");
                String content = block.flatten();
                IFenceRenderer renderer = Fences.rendererFor(info);
                boolean hasContent = (content != null) && !content.isEmpty();
                if ((renderer != null) && (hasContent || renderer.rendersEmpty())) {
                    el = Div.$(root);
                    applyBlockStyles(el, type);
                    el.use(n -> {
                        Element target = Js.uncheckedCast(n);
                        renderer.render(target, info, content);
                    });
                } else {
                    el = Custom.$(root, "pre");
                    applyBlockStyles(el, type);
                    Custom.$(el, "code").text(content);
                }
                break;
            }
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
        boolean ordered = (block.getType() == BlockType.OLIST);
        String listTag = ordered ? "ol" : "ul";

        if (ulStack.isEmpty()) {
            ElementBuilder ul = Custom.$(root, listTag);
            ulStack.push(ul);
            listDepth = 0;
            listOrdered = ordered;
        }

        // Increase nesting: create nested <ol>/<ul> inside the last <li>.
        while (listDepth < indent) {
            ElementBuilder ul = Custom.$(listTag);
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

        // Variable — render as a chip (styled by the shared richtext stylesheet), matching the
        // editor so the two surfaces present variables identically.
        if (segment.variable()) {
            Span.$(target).style("variable").text(text);
            return;
        }

        // Image — render as <img> element.
        if (segment.image()) {
            String src = segment.hasMeta() ? segment.meta().get(FormattedLine.META_IMAGE) : null;
            String width = segment.hasMeta() ? segment.meta().get(FormattedLine.META_WIDTH) : null;
            String height = segment.hasMeta() ? segment.meta().get(FormattedLine.META_HEIGHT) : null;
            String align = segment.hasMeta() ? segment.meta().get(FormattedLine.META_ALIGN) : null;
            String margin = segment.hasMeta() ? segment.meta().get(FormattedLine.META_MARGIN) : null;
            // Alt is meta; the segment text is the image's sentinel character (never
            // rendered).
            String alt = segment.hasMeta() ? segment.meta().get(FormattedLine.META_ALT) : null;
            ElementBuilder img = Custom.$("img");
            target.insert(img);
            if ((src != null) && !src.isEmpty())
                img.attr("src", src);
            if ((alt != null) && !alt.isEmpty())
                img.attr("alt", alt);
            if ((width != null) && !width.isEmpty())
                img.attr("width", width);
            if ((height != null) && !height.isEmpty())
                img.attr("height", height);
            // Margin applies to all sides; a block alignment (below) then overrides the
            // horizontal margin on the auto side(s).
            if ((margin != null) && !margin.isEmpty())
                img.css("margin", margin + "px");
            // Block alignment: the image sits on its own line, aligned via auto margins.
            if ((align != null) && !align.isEmpty()) {
                img.css("display", "block");
                if ("center".equals(align)) {
                    img.css("margin-left", "auto");
                    img.css("margin-right", "auto");
                } else if ("right".equals(align)) {
                    img.css("margin-left", "auto");
                } else {
                    img.css("margin-right", "auto");
                }
            }
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

    /**
     * Gives a heading a slug id so it is targetable by an in-page {@code #anchor} link (e.g.
     * {@code [x](#operating-scenarios)} finds an "Operating scenarios" heading). Repeated
     * heading text is de-duplicated ({@code slug}, {@code slug-1}, …), matching the common
     * markdown-anchor convention.
     */
    private void applyHeadingId(ElementBuilder el, FormattedBlock block) {
        String base = LinkHandlers.slug(block.flatten());
        if (base.isEmpty())
            return;
        Integer seen = headingSlugs.get(base);
        String id = (seen == null) ? base : (base + "-" + seen);
        headingSlugs.put(base, (seen == null) ? 1 : (seen + 1));
        el.attr("id", id);
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
        listOrdered = false;
        lastLi = null;
    }
}
