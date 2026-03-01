package com.effacy.jui.text.ui.editor;

import java.util.Set;

import com.effacy.jui.core.client.component.IComponent;
import com.effacy.jui.text.type.FormattedBlock.BlockType;
import com.effacy.jui.text.type.FormattedLine.FormatType;

/**
 * Contract for an editor toolbar.
 * <p>
 * Implementations receive state updates from the containing control and
 * send commands back via {@link IEditorCommands}. The stock implementation
 * is {@link EditorToolbar}.
 * <p>
 * The toolbar is responsible only for its inner representation (buttons,
 * active states). Positional concerns (layout, floating behaviour) are
 * managed entirely by the containing control (e.g.
 * {@link FormattedTextEditor}).
 */
public interface IEditorToolbar extends IComponent {

    /**
     * Toolbar position relative to the editor area. Used by the containing
     * control to decide layout; the toolbar itself does not use this.
     */
    public enum Position {
        TOP, BOTTOM, FLOATING
    }

    /**
     * Binds this toolbar to the editor's command interface. Called once
     * during rendering.
     *
     * @param commands
     *                 the command callback for driving the editor.
     */
    void bind(IEditorCommands commands);

    /**
     * Updates the toolbar to reflect the current editor state.
     *
     * @param activeBlockType
     *                        the block type of the anchor block (e.g. H1,
     *                        PARA, NLIST).
     * @param activeFormats
     *                        the set of inline formats active at the current
     *                        cursor or across the current range selection.
     * @param rangeSelected
     *                        {@code true} if the selection is a range (not a
     *                        collapsed cursor). Useful for floating toolbars
     *                        that show/hide based on selection type.
     */
    void updateState(BlockType activeBlockType, Set<FormatType> activeFormats, boolean rangeSelected);

    /**
     * Updates the toolbar for a cell-editing context. Block-type tools should
     * be deactivated (cells have no block type). Only inline format state is
     * meaningful.
     *
     * @param activeFormats
     *                      the set of inline formats active at the cell
     *                      selection.
     */
    void updateCellState(Set<FormatType> activeFormats);
}
