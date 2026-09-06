package com.effacy.jui.text.ui.type;

import java.util.HashMap;
import java.util.Map;

import com.effacy.jui.platform.css.client.CssDeclaration;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.text.type.FormattedBlock.BlockType;
import com.effacy.jui.text.type.FormattedLine;
import com.google.gwt.core.client.GWT;

public class FormattedTextStyles {

    /**
     * Maps {@link BlockType} to CSS classes to apply to the block element. Block
     * types not in the map receive no styling. Entries with multiple classes (e.g.
     * {@code NLIST}) will have all classes applied.
     */
    public static Map<BlockType, String[]> BLOCK_STYLES = new HashMap<>();
    static {
        BLOCK_STYLES.put (BlockType.PARA, new String[] { "block" });
        BLOCK_STYLES.put (BlockType.NLIST, new String[] { "block", "list_bullet" });
        BLOCK_STYLES.put (BlockType.OLIST, new String[] { "block", "list_number" });
        BLOCK_STYLES.put (BlockType.CODE, new String[] { "block", "code_block" });
        BLOCK_STYLES.put (BlockType.FENCE, new String[] { "block", "code_block" });
        BLOCK_STYLES.put (BlockType.QUOTE, new String[] { "block", "quote" });
        BLOCK_STYLES.put (BlockType.H1, new String[] { "block" });
        BLOCK_STYLES.put (BlockType.H2, new String[] { "block" });
        BLOCK_STYLES.put (BlockType.H3, new String[] { "block" });
        BLOCK_STYLES.put (BlockType.H4, new String[] { "block" });
        BLOCK_STYLES.put (BlockType.H5, new String[] { "block" });
        BLOCK_STYLES.put (BlockType.TCELL, new String[] { "block" });
    }

    /**
     * Maps {@link FormattedLine.FormatType} to CSS class suffixes. The full class
     * name is {@code "fmt_" + suffix} (e.g. {@code "fmt_bold"}).
     */
    public static Map<FormattedLine.FormatType,String> LINE_STYLES = new HashMap<>();
    static {
        LINE_STYLES.put (FormattedLine.FormatType.BLD, "bold");
        LINE_STYLES.put (FormattedLine.FormatType.CODE, "code");
        LINE_STYLES.put (FormattedLine.FormatType.CMT, "comment");
        LINE_STYLES.put (FormattedLine.FormatType.DEL, "delete");
        LINE_STYLES.put (FormattedLine.FormatType.INS, "insert");
        LINE_STYLES.put (FormattedLine.FormatType.HL, "highlight");
        LINE_STYLES.put (FormattedLine.FormatType.ITL, "italic");
        LINE_STYLES.put (FormattedLine.FormatType.STR, "strike");
        LINE_STYLES.put (FormattedLine.FormatType.SUB, "subscript");
        LINE_STYLES.put (FormattedLine.FormatType.SUP, "superscript");
        LINE_STYLES.put (FormattedLine.FormatType.UL, "underline");
    }

    /**
     * Maps {@link FormattedLine.FormatType} to semantic HTML tag names. Format
     * types not in the map have no semantic equivalent and fall back to
     * {@code <span>} with a CSS class from {@link #LINE_STYLES}.
     */
    public static Map<FormattedLine.FormatType,String> SEMANTIC_TAGS = new HashMap<>();
    static {
        SEMANTIC_TAGS.put (FormattedLine.FormatType.BLD, "strong");
        SEMANTIC_TAGS.put (FormattedLine.FormatType.ITL, "em");
        SEMANTIC_TAGS.put (FormattedLine.FormatType.STR, "s");
        SEMANTIC_TAGS.put (FormattedLine.FormatType.CODE, "code");
        SEMANTIC_TAGS.put (FormattedLine.FormatType.SUB, "sub");
        SEMANTIC_TAGS.put (FormattedLine.FormatType.SUP, "sup");
        SEMANTIC_TAGS.put (FormattedLine.FormatType.UL, "u");
    }

    /********************************************************************
     * CSS
     ********************************************************************/

    /**
     * The active formatted-text stylesheet. Defaults to {@link StandardFormattedTextCSS};
     * override via {@link #styles(IFormattedTextCSS)}.
     */
    private static IFormattedTextCSS PROVIDER;

    /**
     * The active formatted-text stylesheet. Apply {@link IFormattedTextCSS#richtext()} to the
     * container element that holds rendered content ({@link DomBuilderFormattedTextRenderer}
     * output, chat bubbles, etc.) to scope the content styles.
     *
     * @return the active stylesheet (never {@code null}).
     */
    public static IFormattedTextCSS styles() {
        if (PROVIDER == null)
            PROVIDER = StandardFormattedTextCSS.instance ();
        return PROVIDER;
    }

    /**
     * Supplies a custom formatted-text stylesheet, replacing {@link StandardFormattedTextCSS}.
     * Others provide their own by implementing {@link IFormattedTextCSS} with their own
     * {@code @CssResource} (or extending {@link StandardFormattedTextCSS}) and passing an
     * instance here. Pass {@code null} to revert to the standard styles.
     * <p>
     * The content classes the renderer applies ({@code fmt_*}, {@code code_block},
     * {@code quote}, {@code list_bullet}, {@code list_number}, {@code indent*},
     * {@code diff_added} / {@code diff_removed} and the heading elements) are the contract a
     * replacement must honour; only the presentation changes.
     *
     * @param provider
     *                 the stylesheet, or {@code null} to reset.
     */
    public static void styles(IFormattedTextCSS provider) {
        PROVIDER = provider;
    }

    public static interface IFormattedTextCSS extends CssDeclaration {

        /**
         * The scope class for a block of rendered formatted text. Content styles are defined
         * beneath it (headings, inline formats, code blocks, quotes, lists).
         */
        public String richtext();

    }

    /**
     * The default formatted-text stylesheet. Presentation is driven by
     * {@code --jui-richtext-*} custom properties (each with a sensible default), so a consumer
     * can retheme without replacing the sheet by setting those tokens on any ancestor element
     * (custom properties inherit) — for example via a control/editor variant's {@code css()}.
     */
    @CssResource(stylesheet = """
.richtext {
    position: relative;
    margin: 0;
    line-height: var(--jui-richtext-line-height, inherit);
}

/* --jui-richtext-list-indent is deliberately NOT defaulted here, nor as a var()
   fallback in the list calc() below. A zero length does not survive this
   stylesheet pipeline in either position: "0em" is normalised to a unitless "0",
   which is valid CSS everywhere except inside calc(), where adding a <number> to
   a <length> is a type error that invalidates the whole declaration — leaving
   list items with no left padding and their markers' "left" resolving to auto,
   dropping the bullet onto the first character.
   ContentStyle.apply() writes the token as an inline style instead. That is set
   at runtime, so the pipeline never sees it and the unit survives. Every content
   root goes through apply(), including the editor's (which defaults to
   compact()), so the token is always defined wherever these rules can match. */

/* Block (paragraph/heading/etc.) vertical rhythm — token-driven so a ContentStyle applies
   identically here and in the editor (whose root also carries .richtext). Default is compact
   (2px); the document style makes it roomier. The editor layers its own contenteditable-only
   block props (min-height, white-space) on top under its .component scope. */
.richtext > .block {
    padding: var(--jui-richtext-block-spacing, 2px) 0;
}

.richtext p {
    margin: 0 0 var(--jui-richtext-para-spacing, 0.2em) 0;
}

.richtext .fmt_bold {
    font-weight: var(--jui-richtext-bold-weight, 600);
}

.richtext .fmt_italic {
    font-style: italic;
}

.richtext .fmt_underline {
    text-decoration: underline;
}

.richtext .fmt_strike {
    text-decoration: line-through;
}

.richtext .fmt_strike.fmt_underline {
    text-decoration: underline line-through;
}

.richtext .fmt_subscript {
    vertical-align: sub;
    font-size: 0.8em;
}

.richtext .fmt_superscript {
    vertical-align: super;
    font-size: 0.8em;
}

.richtext .fmt_highlight {
    background-color: var(--jui-richtext-highlight-bg, #F5EB72);
}

.richtext .fmt_comment {
    background-color: var(--jui-richtext-comment-bg, rgba(245, 235, 114, 0.35));
    border-bottom: 2px solid var(--jui-richtext-comment-border, #E5CE49);
}

.richtext .fmt_code {
    font-family: var(--jui-richtext-mono-font, "SFMono-Regular", Menlo, Consolas, "PT Mono", "Liberation Mono", Courier, monospace);
    line-height: normal;
    background: var(--jui-richtext-code-bg, rgba(135,131,120,.15));
    color: var(--jui-richtext-code-color, inherit);
    border-radius: 4px;
    font-size: 85%;
    padding: 0.2em 0.4em;
}

.richtext .variable {
    background: var(--jui-richtext-variable-bg, #e0e7ff);
    color: var(--jui-richtext-variable-color, #3730a3);
    padding: 1px 6px;
    border-radius: 3px;
    font-size: 0.85em;
    font-weight: 500;
    display: inline;
    user-select: all;
    cursor: default;
}

/* Links. There was no rule here at all, so a link fell through to whatever the
   surrounding application does to an anchor — which in a body of prose is
   typically a saturated colour that breaks the line it sits in and makes the
   sentence read as a list of destinations.

   At rest a link is the text colour with a light dashed underline: the underline
   marks it without taking it out of the sentence, and dashed says "this is a
   pointer" where solid says "this is emphasis". On hover it firms up — a little
   darker, the underline solid — so the state change is felt rather than
   announced.

   Both states are tokens, and both defaults are derived from currentColor rather
   than fixed, so the treatment holds wherever the content is rendered: muted
   caption, dark card, anything. */
.richtext a {
    color: var(--jui-richtext-link-color, inherit);
    text-decoration: underline;
    text-decoration-style: dashed;
    text-decoration-thickness: 1px;
    text-decoration-color: var(--jui-richtext-link-underline, color-mix(in srgb, currentColor 40%, transparent));
    text-underline-offset: 0.18em;
    cursor: pointer;
}

.richtext a:hover {
    color: var(--jui-richtext-link-hover-color, color-mix(in srgb, currentColor 78%, #000));
    text-decoration-style: solid;
    text-decoration-color: var(--jui-richtext-link-hover-underline, currentColor);
}

.richtext img {
    max-width: 100%;
    height: auto;
    vertical-align: middle;
    border-radius: 4px;
}

/* Elements are qualified (pre./blockquote.) so these win over the editor's structural
   .block padding when the editor scopes its content with the richtext class. */
.richtext > pre.code_block {
    margin: 0.75em 0;
    padding: 0.85em 1em;
    overflow-x: auto;
    white-space: pre-wrap;
    background: var(--jui-richtext-codeblock-bg, rgba(135,131,120,.15));
    border-radius: var(--jui-richtext-codeblock-radius, 8px);
}

.richtext > pre.code_block > code {
    font-family: var(--jui-richtext-mono-font, "SFMono-Regular", Menlo, Consolas, "PT Mono", "Liberation Mono", Courier, monospace);
    color: inherit;
    background: transparent;
    font-size: 0.95em;
    line-height: 1.45;
    padding: 0;
}

.richtext > blockquote.quote {
    margin: 0.7em 0;
    padding: 0.55em 0.9em;
    border-left: 3px solid var(--jui-richtext-quote-border, rgba(135,131,120,.35));
    border-radius: var(--jui-richtext-quote-radius, 6px);
    background: var(--jui-richtext-quote-bg, rgba(135,131,120,.06));
    color: var(--jui-richtext-quote-color, #5b6168);
}

.richtext > .indent1 {
    margin-left: 1.5em;
}

.richtext > .indent2 {
    margin-left: 3em;
}

.richtext > .indent3 {
    margin-left: 4.5em;
}

.richtext > .indent4 {
    margin-left: 6em;
}

.richtext > .indent5 {
    margin-left: 7.5em;
}

/* The base (level-1) list indent is added to the marker padding (and the marker's own
   offset) so it composes additively with the .indentN nesting margins and keeps the
   bullet aligned to the text. Default 0 (compact / flush); a content style may set it. */
.richtext > .list_bullet {
    padding-left: calc(1.5em + var(--jui-richtext-list-indent));
}

.richtext > .list_bullet::before {
    position: absolute;
    left: calc(1em + var(--jui-richtext-list-indent));
    content: '\\2022';
}

.richtext > .list_number {
    padding-left: calc(1.5em + var(--jui-richtext-list-indent));
}

/* The number comes from the attribute the renderer sets, exactly as it does in the
   editor. Without this rule an ordered list renders with the item padding but no
   marker at all — indented text with nothing in front of it. Set slightly further
   left than the bullet because a number is wider, and more so once it reaches two
   digits or a roman numeral. */
.richtext > .list_number::before {
    position: absolute;
    left: calc(0.15em + var(--jui-richtext-list-indent));
    content: attr(data-list-index) '.';
}

.richtext > .list_tick {
    padding-left: calc(1.5em + var(--jui-richtext-list-indent));
}

.richtext > .list_tick::before {
    position: absolute;
    left: calc(1em + var(--jui-richtext-list-indent));
    content: '\\2713';
}

/* A list item is a paragraph (.block + .list_*), so it would otherwise take the prose block
   padding and paragraph margin — which the document style makes roomy, pushing items apart.
   Give list items their own tight vertical rhythm via a dedicated token (default 3px, prose
   margins zeroed) so the gap stays compact regardless of prose spacing. Placed after the
   .block rule so it wins for the item's top/bottom padding. Mirrors the editor's list rules.

   position:relative makes each item the containing block for its own marker. The markers
   below are absolutely positioned, and without this they resolve against .richtext (the only
   positioned ancestor) rather than against the item they belong to. That happens to look
   right only while an item's left edge coincides with the content root's — it does not for a
   nested item, whose .indentN margin moves the item while leaving its marker behind at the
   root's coordinate. */
.richtext > .list_bullet, .richtext > .list_number, .richtext > .list_tick {
    position: relative;
    padding-top: var(--jui-richtext-list-spacing, 3px);
    padding-bottom: var(--jui-richtext-list-spacing, 3px);
    margin-top: 0;
    margin-bottom: 0;
}

.richtext h1 {
    font-size: var(--jui-richtext-h1-size, 1.8em);
    font-weight: var(--jui-richtext-heading-weight, 500);
    line-height: 1.25;
    margin: 0.9em 0 0.3em 0;
}

.richtext h2 {
    font-size: var(--jui-richtext-h2-size, 1.6em);
    font-weight: var(--jui-richtext-heading-weight, 500);
    line-height: 1.25;
    margin: 0.8em 0 0.25em 0;
}

.richtext h3 {
    font-size: var(--jui-richtext-h3-size, 1.4em);
    font-weight: var(--jui-richtext-heading-weight, 500);
    line-height: 1.3;
    margin: 0.7em 0 0.2em 0;
}

.richtext h4 {
    font-size: var(--jui-richtext-h4-size, 1.2em);
    font-weight: var(--jui-richtext-heading-weight, 500);
    line-height: 1.3;
    margin: 0.7em 0 0.2em 0;
}

.richtext h5 {
    font-size: var(--jui-richtext-h5-size, 1.1em);
    font-weight: var(--jui-richtext-heading-weight, 500);
    line-height: 1.3;
    margin: 0.7em 0 0.2em 0;
}

/* Change marking. A diff document (see FormattedTextDiff) is the new document with the
   old one's departed blocks put back in place, each changed block carrying the diff
   meta-data that the renderer turns into these classes. Ordinary content carries none of
   it, so these rules never match anything that is not a comparison.

   The bar is an inset shadow rather than a border so that marking a block does not move
   it — the marked and unmarked blocks around it have to stay on the same left edge, or
   the document reads as ragged rather than as annotated. Removed content also carries a
   line through it, so added and removed are told apart by something other than the
   colour. */
.richtext .diff_added, .richtext .diff_removed, .richtext .diff_changed {
    padding-left: 0.5em;
    border-radius: var(--jui-richtext-diff-radius, 3px);
}

/* A list item's padding is its marker's offset, so it is restored here rather than
   overridden by the rule above (which would drop the bullet onto the first character). */
.richtext > .list_bullet.diff_added, .richtext > .list_bullet.diff_removed, .richtext > .list_bullet.diff_changed,
.richtext > .list_number.diff_added, .richtext > .list_number.diff_removed, .richtext > .list_number.diff_changed,
.richtext > .list_tick.diff_added, .richtext > .list_tick.diff_removed, .richtext > .list_tick.diff_changed {
    padding-left: calc(1.5em + var(--jui-richtext-list-indent));
}

.richtext .diff_added {
    background: var(--jui-richtext-diff-added-bg, rgba(46, 125, 50, 0.10));
    box-shadow: inset 3px 0 0 var(--jui-richtext-diff-added-bar, #2e7d32);
}

.richtext .diff_removed {
    background: var(--jui-richtext-diff-removed-bg, rgba(198, 40, 40, 0.09));
    box-shadow: inset 3px 0 0 var(--jui-richtext-diff-removed-bar, #c62828);
    text-decoration: line-through;
    text-decoration-color: var(--jui-richtext-diff-removed-strike, rgba(198, 40, 40, 0.5));
}

/* A block that was edited rather than added or removed: it holds both what went and
   what came in, marked inline, so the block itself takes a neutral wash — a colour of
   its own here would compete with the marks it contains. */
.richtext .diff_changed {
    background: var(--jui-richtext-diff-changed-bg, rgba(120, 130, 140, 0.09));
    box-shadow: inset 3px 0 0 var(--jui-richtext-diff-changed-bar, #78828c);
}

/* The inline marks (INS / DEL). Word-level marking is only legible if the two are
   distinguishable at a glance and inside a sentence, so removed text is struck through
   and inserted text underlined — the colours are then confirmation rather than the whole
   signal, which also keeps it readable printed and to a colour-blind reader. */
.richtext .fmt_delete {
    background: var(--jui-richtext-diff-removed-bg, rgba(198, 40, 40, 0.09));
    color: var(--jui-richtext-diff-removed-color, #8e2020);
    text-decoration: line-through;
    text-decoration-color: var(--jui-richtext-diff-removed-strike, rgba(198, 40, 40, 0.5));
}

.richtext .fmt_insert {
    background: var(--jui-richtext-diff-added-bg, rgba(46, 125, 50, 0.10));
    color: var(--jui-richtext-diff-added-color, #1f5c23);
    text-decoration: underline;
    text-decoration-color: var(--jui-richtext-diff-added-underline, rgba(46, 125, 50, 0.5));
}

/* Marked content inside a block that is itself wholly added or removed would otherwise
   be marked twice (the block's wash and the span's), which reads as a change within a
   change. The block has already said it. */
.richtext .diff_added .fmt_insert, .richtext .diff_removed .fmt_delete {
    background: transparent;
}

.richtext > :first-child { margin-top: 0; }
    """)
    public static abstract class StandardFormattedTextCSS implements IFormattedTextCSS {

        private static StandardFormattedTextCSS STYLES;

        public static IFormattedTextCSS instance() {
            if (STYLES == null) {
                STYLES = (StandardFormattedTextCSS) GWT.create (StandardFormattedTextCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }
}
