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
        BLOCK_STYLES.put (BlockType.H1, new String[] { "block" });
        BLOCK_STYLES.put (BlockType.H2, new String[] { "block" });
        BLOCK_STYLES.put (BlockType.H3, new String[] { "block" });
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
     * Styles (made available to selection).
     */
    public static IFormattedTextCSS styles() {
        return LocalCSS.instance ();
    }

    public static interface IFormattedTextCSS extends CssDeclaration {

        /**
         * Standard CSS for formatted text.
         */
        public String standard();
    
    }

    /**
     * Component CSS (standard pattern).
     */
    @CssResource(stylesheet = """
.standard {
    position: relative;
    margin: 0;
}

.standard .fmt_bold {
    font-weight: 600;
}

.standard .fmt_italic {
    font-style: italic;
}

.standard .fmt_underline {
    text-decoration: underline;
}

.standard .fmt_strike {
    text-decoration: line-through;
}

.standard .fmt_strike.fmt_underline {
    text-decoration: underline line-through;
}

.standard .fmt_subscript {
    vertical-align: sub;
    font-size: 0.8em;
}

.standard .fmt_superscript {
    vertical-align: super;
    font-size: 0.8em;
}

.standard .fmt_highlight {
    background-color: #F5EB72;
}

.standard .fmt_code {
    font-family: "SFMono-Regular", Menlo, Consolas, "PT Mono", "Liberation Mono", Courier, monospace;
    line-height: normal;
    background: rgba(135,131,120,.15);
    color: #EB5757;
    border-radius: 4px;
    font-size: 85%;
    padding: 0.2em 0.4em;
}

.standard > .indent1 {
    margin-left: 1.5em;
}

.standard > .indent2 {
    margin-left: 3.25em;
}

.standard > .indent3 {
    margin-left: 4.75em;
}

.standard > .list_bullet {
    padding-left: 1.5em;
}

.standard > .list_bullet::before {
    position: absolute;
    left: 1em;
    content: '\\2022';
}

.standard > .list_number {
    padding-left: 1.5em;
}

.standard > .list_tick {
    padding-left: 1.5em;
}

.standard > .list_tick::before {
    position: absolute;
    left: 1em;
    content: '\\2713';
}

.standard h1 {
    font-size: 1.8em;
    font-weight: 500;
}

.standard h2 {
    font-size: 1.6em;
    font-weight: 500;
}

.standard h3 {
    font-size: 1.4em;
    font-weight: 500;
}

.standard h4 {
    font-size: 1.2em;
    font-weight: 500;
}

.standard h5 {
    font-size: 1.1em;
    font-weight: 500;
}
    """)
    public static abstract class LocalCSS implements IFormattedTextCSS {

        private static LocalCSS STYLES;

        public static IFormattedTextCSS instance() {
            if (STYLES == null) {
                STYLES = (LocalCSS) GWT.create (LocalCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }
}
