package com.effacy.jui.text.ui.editor2;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.Button;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.text.type.FormattedBlock.BlockType;
import com.effacy.jui.text.type.FormattedLine.FormatType;
import com.effacy.jui.ui.client.icon.FontAwesome;

import elemental2.dom.Element;

/**
 * Standard toolbar tools and factory methods for creating custom tools.
 * <p>
 * All constants are stateless {@link ITool} instances — safe as
 * {@code static final}. Per-toolbar mutable state is created by
 * {@link ITool#render(ITool.Context)} and returned as an
 * {@link ITool.Handle}.
 * <p>
 * Each factory method has two overloads: one accepting a {@code String}
 * label for simple text content, and one accepting a
 * {@code Consumer<ElementBuilder>} for custom button content (e.g.
 * FontAwesome icons):
 * <pre>
 * // Text label
 * Tools.format(FormatType.BLD, "B", "Bold (Ctrl+B)")
 *
 * // Custom icon
 * Tools.format(FormatType.BLD, r -&gt; Em.$(r).style(FontAwesome.bold()), "Bold (Ctrl+B)")
 * </pre>
 */
public class Tools {

    /************************************************************************
     * Format toggles.
     ************************************************************************/

    public static final ITool BOLD = format(FormatType.BLD, r -> Em.$(r).style(FontAwesome.bold()), "Bold (Ctrl+B)");
    public static final ITool ITALIC = format(FormatType.ITL, r -> Em.$(r).style(FontAwesome.italic()), "Italic (Ctrl+I)");
    public static final ITool UNDERLINE = format(FormatType.UL, r -> Em.$(r).style(FontAwesome.underline()), "Underline (Ctrl+U)");
    public static final ITool STRIKETHROUGH = format(FormatType.STR, r -> Em.$(r).style(FontAwesome.strikethrough()), "Strikethrough");
    public static final ITool SUBSCRIPT = format(FormatType.SUB, r -> Em.$(r).style(FontAwesome.subscript()), "Subscript");
    public static final ITool SUPERSCRIPT = format(FormatType.SUP, r -> Em.$(r).style(FontAwesome.superscript()), "Superscript");
    public static final ITool CODE = format(FormatType.CODE, r -> Em.$(r).style(FontAwesome.code()), "Code");
    public static final ITool HIGHLIGHT = format(FormatType.HL, r -> Em.$(r).style(FontAwesome.highlighter()), "Highlight");

    /************************************************************************
     * Block type setters.
     ************************************************************************/

    public static final ITool H1 = block(BlockType.H1, "H1", "Heading 1");
    public static final ITool H2 = block(BlockType.H2, "H2", "Heading 2");
    public static final ITool H3 = block(BlockType.H3, "H3", "Heading 3");
    public static final ITool PARAGRAPH = block(BlockType.PARA, r -> Em.$(r).style(FontAwesome.paragraph()), "Paragraph");

    /************************************************************************
     * Block type toggles.
     ************************************************************************/

    public static final ITool BULLET_LIST = toggleBlock(BlockType.NLIST, r -> Em.$(r).style(FontAwesome.listDots()), "Bullet List");
    public static final ITool NUMBERED_LIST = toggleBlock(BlockType.OLIST, r -> Em.$(r).style(FontAwesome.listNumeric()), "Numbered List");

    /************************************************************************
     * Action tools.
     ************************************************************************/

    public static final ITool TABLE = action(r -> Em.$(r).style(FontAwesome.table()), "Insert Table", cmd -> cmd.insertTable(2, 3));
    public static final ITool LINK = anchoredAction(r -> Em.$(r).style(FontAwesome.link()), "Link", (cmd, el) -> cmd.insertLink(el));
    public static final ITool VARIABLE = anchoredAction("{}", "Variable", (cmd, el) -> cmd.insertVariable(el));

    /************************************************************************
     * Visual separator.
     ************************************************************************/

    public static final ITool SEPARATOR = separator();

    /************************************************************************
     * Default tool set.
     ************************************************************************/

    /**
     * Returns all standard tools with separators between groups, suitable
     * for use as the default tool set when none is configured.
     */
    public static ITool[] all() {
        return new ITool[] {
            BOLD, ITALIC, UNDERLINE, STRIKETHROUGH, SUBSCRIPT, SUPERSCRIPT, CODE, HIGHLIGHT,
            SEPARATOR,
            H1, H2, H3, PARAGRAPH,
            SEPARATOR,
            BULLET_LIST, NUMBERED_LIST,
            SEPARATOR,
            TABLE, SEPARATOR, LINK, SEPARATOR, VARIABLE
        };
    }

    /************************************************************************
     * Factory methods.
     ************************************************************************/

    /**
     * Creates a format toggle tool with a text label.
     *
     * @see #format(FormatType, Consumer, String)
     */
    public static ITool format(FormatType type, String label, String tooltip) {
        return format(type, btn -> btn.text(label), tooltip);
    }

    /**
     * Creates a format toggle tool with custom button content. The button
     * calls {@link IEditorCommands#toggleFormat(FormatType)} on mousedown,
     * and the handle tracks whether the format is active.
     *
     * @param type
     *              the format type to toggle.
     * @param content
     *              populates the button's inner content (e.g.
     *              {@code r -> Em.$(r).style(FontAwesome.bold())}).
     * @param tooltip
     *              the button tooltip.
     */
    public static ITool format(FormatType type, Consumer<ElementBuilder> content, String tooltip) {
        return ctx -> {
            Element[] btn = new Element[1];
            Button.$(ctx.parent()).style(ctx.styles().tbtn()).$(content).attr("title", tooltip)
                .use(n -> btn[0] = (Element) n)
                .on(e -> {
                    e.stopEvent();
                    if (ctx.commands() != null)
                        ctx.commands().toggleFormat(type);
                }, UIEventType.ONMOUSEDOWN);
            return new ITool.Handle() {

                @Override
                public void updateState(BlockType activeBlockType, Set<FormatType> activeFormats) {
                    if (activeFormats.contains(type))
                        btn[0].classList.add(ctx.styles().tbtnActive());
                    else
                        btn[0].classList.remove(ctx.styles().tbtnActive());
                }

                @Override
                public void updateCellState(Set<FormatType> activeFormats) {
                    updateState(null, activeFormats);
                }
            };
        };
    }

    /**
     * Creates a block type setter tool with a text label.
     *
     * @see #block(BlockType, Consumer, String)
     */
    public static ITool block(BlockType type, String label, String tooltip) {
        return block(type, btn -> btn.text(label), tooltip);
    }

    /**
     * Creates a block type setter tool with custom button content. The
     * button calls {@link IEditorCommands#setBlockType(BlockType)} on
     * mousedown, and the handle tracks whether the block type is active. In
     * cell context the handle deactivates (cells have no block type).
     *
     * @param type
     *              the block type to set.
     * @param content
     *              populates the button's inner content.
     * @param tooltip
     *              the button tooltip.
     */
    public static ITool block(BlockType type, Consumer<ElementBuilder> content, String tooltip) {
        return ctx -> {
            Element[] btn = new Element[1];
            Button.$(ctx.parent()).style(ctx.styles().tbtn()).$(content).attr("title", tooltip)
                .use(n -> btn[0] = (Element) n)
                .on(e -> {
                    e.stopEvent();
                    if (ctx.commands() != null)
                        ctx.commands().setBlockType(type);
                }, UIEventType.ONMOUSEDOWN);
            return new ITool.Handle() {

                @Override
                public void updateState(BlockType activeBlockType, Set<FormatType> activeFormats) {
                    if (type == activeBlockType)
                        btn[0].classList.add(ctx.styles().tbtnActive());
                    else
                        btn[0].classList.remove(ctx.styles().tbtnActive());
                }

                @Override
                public void updateCellState(Set<FormatType> activeFormats) {
                    btn[0].classList.remove(ctx.styles().tbtnActive());
                }
            };
        };
    }

    /**
     * Creates a block type toggle tool with a text label.
     *
     * @see #toggleBlock(BlockType, Consumer, String)
     */
    public static ITool toggleBlock(BlockType type, String label, String tooltip) {
        return toggleBlock(type, btn -> btn.text(label), tooltip);
    }

    /**
     * Creates a block type toggle tool with custom button content. The
     * button calls {@link IEditorCommands#toggleBlockType(BlockType)} on
     * mousedown. State tracking is the same as
     * {@link #block(BlockType, Consumer, String)}.
     *
     * @param type
     *              the block type to toggle.
     * @param content
     *              populates the button's inner content.
     * @param tooltip
     *              the button tooltip.
     */
    public static ITool toggleBlock(BlockType type, Consumer<ElementBuilder> content, String tooltip) {
        return ctx -> {
            Element[] btn = new Element[1];
            Button.$(ctx.parent()).style(ctx.styles().tbtn()).$(content).attr("title", tooltip)
                .use(n -> btn[0] = (Element) n)
                .on(e -> {
                    e.stopEvent();
                    if (ctx.commands() != null)
                        ctx.commands().toggleBlockType(type);
                }, UIEventType.ONMOUSEDOWN);
            return new ITool.Handle() {

                @Override
                public void updateState(BlockType activeBlockType, Set<FormatType> activeFormats) {
                    if (type == activeBlockType)
                        btn[0].classList.add(ctx.styles().tbtnActive());
                    else
                        btn[0].classList.remove(ctx.styles().tbtnActive());
                }

                @Override
                public void updateCellState(Set<FormatType> activeFormats) {
                    btn[0].classList.remove(ctx.styles().tbtnActive());
                }
            };
        };
    }

    /**
     * Creates a stateless action tool with a text label.
     *
     * @see #action(Consumer, String, Consumer)
     */
    public static ITool action(String label, String tooltip, Consumer<IEditorCommands> action) {
        return action(btn -> btn.text(label), tooltip, action);
    }

    /**
     * Creates a stateless action tool with custom button content. The
     * button invokes the given action on mousedown. No state tracking is
     * performed.
     *
     * @param content
     *              populates the button's inner content.
     * @param tooltip
     *              the button tooltip.
     * @param action
     *              the action to invoke with the editor commands.
     */
    public static ITool action(Consumer<ElementBuilder> content, String tooltip, Consumer<IEditorCommands> action) {
        return ctx -> {
            Button.$(ctx.parent()).style(ctx.styles().tbtn()).$(content).attr("title", tooltip)
                .on(e -> {
                    e.stopEvent();
                    if (ctx.commands() != null)
                        action.accept(ctx.commands());
                }, UIEventType.ONMOUSEDOWN);
            return null;
        };
    }

    /**
     * Creates a stateless anchored action tool with a text label.
     *
     * @see #anchoredAction(Consumer, String, BiConsumer)
     */
    public static ITool anchoredAction(String label, String tooltip, BiConsumer<IEditorCommands, Element> action) {
        return anchoredAction(btn -> btn.text(label), tooltip, action);
    }

    /**
     * Creates a stateless anchored action tool with custom button content.
     * The button passes the button element to the action (for popup
     * anchoring). No state tracking is performed.
     *
     * @param content
     *              populates the button's inner content.
     * @param tooltip
     *              the button tooltip.
     * @param action
     *              the action to invoke with the editor commands and the
     *              button element.
     */
    public static ITool anchoredAction(Consumer<ElementBuilder> content, String tooltip, BiConsumer<IEditorCommands, Element> action) {
        return ctx -> {
            Button.$(ctx.parent()).style(ctx.styles().tbtn()).$(content).attr("title", tooltip)
                .on((e, n) -> {
                    e.stopEvent();
                    if (ctx.commands() != null)
                        action.accept(ctx.commands(), (Element) n);
                }, UIEventType.ONMOUSEDOWN);
            return null;
        };
    }

    /**
     * Creates a visual separator (vertical line between tool groups). No
     * state tracking is performed.
     */
    public static ITool separator() {
        return ctx -> {
            Span.$(ctx.parent()).style(ctx.styles().tbtnSep());
            return null;
        };
    }

    private Tools() {}
}
