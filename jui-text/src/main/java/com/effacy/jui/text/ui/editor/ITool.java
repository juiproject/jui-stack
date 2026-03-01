package com.effacy.jui.text.ui.editor;

import java.util.Set;

import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.text.type.FormattedBlock.BlockType;
import com.effacy.jui.text.type.FormattedLine.FormatType;

/**
 * Contract for a single toolbar tool. Instances are stateless descriptors
 * (safe as {@code static final} constants). All per-toolbar mutable state
 * lives in the {@link Handle} returned by {@link #render(Context)}.
 * <p>
 * Standard tools are available as constants on {@link Tools}. Custom tools
 * can be created by implementing this interface directly or by using the
 * factory methods on {@link Tools}.
 */
public interface ITool {

    /**
     * Renders this tool into the toolbar.
     *
     * @param ctx
     *            the rendering context (parent container, commands, styles).
     * @return a handle for state updates, or {@code null} for stateless tools
     *         (e.g. separators).
     */
    Handle render(Context ctx);

    /**
     * Rendering context provided by the toolbar during tool creation.
     */
    interface Context {

        /**
         * The DOM container to add elements to.
         */
        IDomInsertableContainer<?> parent();

        /**
         * The editor commands. May be {@code null} during initial render —
         * action handlers must call this lazily (at click time), not eagerly
         * (at render time).
         */
        IEditorCommands commands();

        /**
         * CSS classes for toolbar buttons.
         */
        EditorToolbar.ILocalCSS styles();
    }

    /**
     * Per-toolbar handle for a rendered tool. Holds the mutable state (button
     * element reference) needed for active-state toggling.
     */
    interface Handle {

        /**
         * Updates active state for a normal block/format context.
         *
         * @param activeBlockType
         *                        the block type of the current selection's
         *                        block.
         * @param activeFormats
         *                        the inline formats active at the current
         *                        selection.
         */
        void updateState(BlockType activeBlockType, Set<FormatType> activeFormats);

        /**
         * Updates active state for a cell-editing context. Block type is not
         * meaningful in cells — block-type tools should deactivate.
         *
         * @param activeFormats
         *                      the inline formats active at the cell selection.
         */
        void updateCellState(Set<FormatType> activeFormats);
    }
}
