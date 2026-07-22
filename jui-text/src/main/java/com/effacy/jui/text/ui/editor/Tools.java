package com.effacy.jui.text.ui.editor;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

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
    public static final ITool H4 = block(BlockType.H4, "H4", "Heading 4");
    public static final ITool H5 = block(BlockType.H5, "H5", "Heading 5");
    public static final ITool PARAGRAPH = block(BlockType.PARA, r -> Em.$(r).style(FontAwesome.paragraph()), "Paragraph");

    /************************************************************************
     * Block type toggles.
     ************************************************************************/

    public static final ITool BULLET_LIST = toggleBlock(BlockType.NLIST, r -> Em.$(r).style(FontAwesome.listDots()), "Bullet List");
    public static final ITool NUMBERED_LIST = toggleBlock(BlockType.OLIST, r -> Em.$(r).style(FontAwesome.listNumeric()), "Numbered List");
    public static final ITool QUOTE = toggleBlock(BlockType.QUOTE, r -> Em.$(r).style(FontAwesome.quoteLeft()), "Quote");

    /************************************************************************
     * Action tools.
     ************************************************************************/

    public static final ITool TABLE = action(r -> Em.$(r).style(FontAwesome.table()), "Insert Table", cmd -> cmd.insertTable(2, 3));
    public static final ITool EQUATION = action(r -> Em.$(r).style(FontAwesome.squareRootVariable()), "Insert Equation", cmd -> cmd.insertEquation());
    public static final ITool DIAGRAM = action(r -> Em.$(r).style(FontAwesome.diagramProject()), "Insert Diagram", cmd -> cmd.insertDiagram());

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
            H1, H2, H3, H4, H5, PARAGRAPH,
            SEPARATOR,
            BULLET_LIST, NUMBERED_LIST,
            SEPARATOR,
            TABLE, EQUATION, DIAGRAM
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
     * Creates a tool that inserts a generic fenced block ({@code BlockType.FENCE}) of the
     * given info string (e.g. {@code mermaid}), with a text label.
     *
     * @see #fence(String, Consumer, String)
     */
    public static ITool fence(String info, String label, String tooltip) {
        return fence(info, btn -> btn.text(label), tooltip);
    }

    /**
     * Creates a tool that inserts a generic fenced block ({@code BlockType.FENCE}) of the
     * given info string, with custom button content. The fence is rendered (and edited) by
     * the {@link IFenceRenderer} registered for {@code info} (see {@link Fences}).
     *
     * @param info
     *              the fence info string (e.g. {@code mermaid}).
     * @param content
     *              populates the button's inner content.
     * @param tooltip
     *              the button tooltip.
     */
    public static ITool fence(String info, Consumer<ElementBuilder> content, String tooltip) {
        return action(content, tooltip, cmd -> cmd.insertFence(info));
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
     * Creates a link tool with a text label.
     *
     * @see #link(Consumer, String, Function)
     */
    public static ITool link(String label, String tooltip, Function<String, List<LinkPanel.AnchorItem>> options) {
        return link(btn -> btn.text(label), tooltip, options);
    }

    /**
     * Creates a link tool with custom button content and an <em>asynchronous</em>
     * suggestion source (e.g. a remote search). Named distinctly from
     * {@link #link(Consumer, String, Function)} — an overload would make existing
     * implicitly-typed lambda call sites ambiguous.
     *
     * @see #link(Consumer, String, Function)
     */
    public static ITool linkAsync(Consumer<ElementBuilder> content, String tooltip, LinkPanel.IAnchorSource source) {
        return linkAsync(content, tooltip, 0, source);
    }

    /**
     * As {@link #linkAsync(Consumer, String, LinkPanel.IAnchorSource)} but with a fixed
     * width for the link panel.
     *
     * @param width
     *              the panel width in pixels ({@code <= 0} falls back to
     *              {@link LinkPanel#DEFAULT_WIDTH}, and failing that content-sized).
     */
    public static ITool linkAsync(Consumer<ElementBuilder> content, String tooltip, int width, LinkPanel.IAnchorSource source) {
        return ctx -> {
            Element[] btn = new Element[1];
            Button.$(ctx.parent()).style(ctx.styles().tbtn()).$(content).attr("title", tooltip)
                .use(n -> btn[0] = (Element) n)
                .on(e -> {
                    e.stopEvent();
                    if (ctx.commands() == null)
                        return;
                    ctx.commands().syncSelection();
                    String currentUrl = ctx.commands().currentLink();
                    String currentLabel = ctx.commands().currentLinkLabel();
                    LinkPanel.show(btn[0], currentUrl, currentLabel, source, width, new LinkPanel.ILinkPanelCallback() {

                        @Override
                        public void onApply(String url) {
                            ctx.commands().applyLink(url);
                        }

                        @Override
                        public void onApply(String url, String label) {
                            ctx.commands().applyLink(url, label);
                        }

                        @Override
                        public void onRemove() {
                            ctx.commands().removeLink();
                        }
                    });
                }, UIEventType.ONMOUSEDOWN);
            return null;
        };
    }

    /**
     * Creates a link tool with custom button content. The button freezes
     * the selection, then opens a {@link LinkPanel} for applying, editing,
     * or removing a link. The tool owns the popup and data source; the
     * editor provides only data-level commands.
     *
     * @param content
     *              populates the button's inner content.
     * @param tooltip
     *              the button tooltip.
     * @param options
     *              function returning link suggestions for the typed text.
     */
    public static ITool link(Consumer<ElementBuilder> content, String tooltip, Function<String, List<LinkPanel.AnchorItem>> options) {
        return ctx -> {
            Element[] btn = new Element[1];
            Button.$(ctx.parent()).style(ctx.styles().tbtn()).$(content).attr("title", tooltip)
                .use(n -> btn[0] = (Element) n)
                .on(e -> {
                    e.stopEvent();
                    if (ctx.commands() == null)
                        return;
                    ctx.commands().syncSelection();
                    String currentUrl = ctx.commands().currentLink();
                    LinkPanel.show(btn[0], currentUrl, options, new LinkPanel.ILinkPanelCallback() {

                        @Override
                        public void onApply(String url) {
                            ctx.commands().applyLink(url);
                        }

                        @Override
                        public void onApply(String url, String label) {
                            ctx.commands().applyLink(url, label);
                        }

                        @Override
                        public void onRemove() {
                            ctx.commands().removeLink();
                        }
                    });
                }, UIEventType.ONMOUSEDOWN);
            return null;
        };
    }

    /**
     * Creates an image tool with a text label.
     *
     * @see #image(Consumer, String, Function, int)
     */
    public static ITool image(String label, String tooltip, Function<String, List<ImagePanel.ImageItem>> options, int columns) {
        return image(btn -> btn.text(label), tooltip, options, columns);
    }

    /**
     * Creates an image tool with custom button content. The button freezes
     * the selection, then opens an {@link ImagePanel} for selecting or
     * entering an image URL.
     *
     * @param content
     *              populates the button's inner content.
     * @param tooltip
     *              the button tooltip.
     * @param options
     *              function returning image items for the typed text.
     * @param columns
     *              number of grid columns (1 for list layout).
     */
    public static ITool image(Consumer<ElementBuilder> content, String tooltip, Function<String, List<ImagePanel.ImageItem>> options, int columns) {
        return ctx -> {
            Element[] btn = new Element[1];
            Button.$(ctx.parent()).style(ctx.styles().tbtn()).$(content).attr("title", tooltip)
                .use(n -> btn[0] = (Element) n)
                .on(e -> {
                    e.stopEvent();
                    if (ctx.commands() == null)
                        return;
                    ctx.commands().syncSelection();
                    String currentSrc = ctx.commands().currentImage();
                    ImagePanel.show(btn[0], currentSrc, options, columns, new ImagePanel.IImagePanelCallback() {

                        @Override
                        public void onApply(String src) {
                            ctx.commands().applyImage(src);
                        }

                        @Override
                        public void onRemove() {
                            ctx.commands().removeImage();
                        }
                    });
                }, UIEventType.ONMOUSEDOWN);
            return null;
        };
    }

    /**
     * Creates a variable tool with a text label.
     *
     * @see #variable(Consumer, String, Function)
     */
    public static ITool variable(String label, String tooltip, Function<String, List<VariablePanel.VariableItem>> options) {
        return variable(btn -> btn.text(label), tooltip, options);
    }

    /**
     * Creates a variable tool with custom button content. The button
     * freezes the selection, then opens a {@link VariablePanel} for
     * selecting a variable to insert. The tool owns the popup and data
     * source.
     *
     * @param content
     *              populates the button's inner content.
     * @param tooltip
     *              the button tooltip.
     * @param options
     *              function returning variable suggestions for the typed
     *              text.
     */
    public static ITool variable(Consumer<ElementBuilder> content, String tooltip, Function<String, List<VariablePanel.VariableItem>> options) {
        return ctx -> {
            Element[] btn = new Element[1];
            Button.$(ctx.parent()).style(ctx.styles().tbtn()).$(content).attr("title", tooltip)
                .use(n -> btn[0] = (Element) n)
                .on(e -> {
                    e.stopEvent();
                    if (ctx.commands() == null)
                        return;
                    ctx.commands().syncSelection();
                    VariablePanel.show(btn[0], options, new VariablePanel.IVariablePanelCallback() {

                        @Override
                        public void onSelect(String name, String label) {
                            ctx.commands().applyVariable(name, label);
                        }
                    });
                }, UIEventType.ONMOUSEDOWN);
            return null;
        };
    }

    /**
     * Handler invoked when the comment tool is activated (see
     * {@link #comment(Consumer, String, ICommentHandler)}). The handler owns
     * the comment experience (composer popup, comment creation, etc.); the
     * editor provides only data-level commands. On completion the handler
     * applies the anchor with
     * {@link IEditorCommands#applyComment(String)} (or removes it with
     * {@link IEditorCommands#removeComment()}).
     */
    @FunctionalInterface
    public interface ICommentHandler {

        /**
         * Invoked with the selection frozen.
         *
         * @param commands
         *                 the editor commands (for applying or removing the
         *                 anchor).
         * @param anchor
         *                 the tool button element (for popup anchoring).
         * @param reference
         *                 the comment reference under the cursor, or
         *                 {@code null} when the selection is not in an
         *                 existing comment.
         */
        void open(IEditorCommands commands, Element anchor, String reference);
    }

    /**
     * Creates a comment tool with a text label.
     *
     * @see #comment(Consumer, String, ICommentHandler)
     */
    public static ITool comment(String label, String tooltip, ICommentHandler handler) {
        return comment(btn -> btn.text(label), tooltip, handler);
    }

    /**
     * Creates a comment tool with custom button content. The button freezes
     * the selection then delegates to the handler, passing the comment
     * reference under the cursor (if any). The handler owns the popup and
     * comment lifecycle; it applies the anchor via
     * {@link IEditorCommands#applyComment(String)} when the comment is
     * established. The handle tracks whether the cursor is inside a comment
     * anchor ({@link FormatType#CMT}).
     *
     * @param content
     *              populates the button's inner content.
     * @param tooltip
     *              the button tooltip.
     * @param handler
     *              the comment handler.
     */
    public static ITool comment(Consumer<ElementBuilder> content, String tooltip, ICommentHandler handler) {
        return ctx -> {
            Element[] btn = new Element[1];
            Button.$(ctx.parent()).style(ctx.styles().tbtn()).$(content).attr("title", tooltip)
                .use(n -> btn[0] = (Element) n)
                .on(e -> {
                    e.stopEvent();
                    if (ctx.commands() == null)
                        return;
                    ctx.commands().syncSelection();
                    handler.open(ctx.commands(), btn[0], ctx.commands().currentComment());
                }, UIEventType.ONMOUSEDOWN);
            return new ITool.Handle() {

                @Override
                public void updateState(BlockType activeBlockType, Set<FormatType> activeFormats) {
                    if (activeFormats.contains(FormatType.CMT))
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
