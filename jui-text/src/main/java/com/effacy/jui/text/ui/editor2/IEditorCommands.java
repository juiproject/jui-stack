package com.effacy.jui.text.ui.editor2;

import com.effacy.jui.text.type.FormattedBlock.BlockType;
import com.effacy.jui.text.type.FormattedLine.FormatType;

import elemental2.dom.Element;

/**
 * Command callback interface through which a toolbar (or other external
 * controller) drives the editor.
 * <p>
 * The editor creates an internal implementation of this interface and passes it
 * to the toolbar via {@link IEditorToolbar#bind(IEditorCommands)}. Each method
 * synchronises the DOM selection and dispatches the appropriate transaction.
 */
public interface IEditorCommands {

    /**
     * Toggles the given inline format at the current selection.
     */
    void toggleFormat(FormatType type);

    /**
     * Sets the block type of the current selection's block(s).
     */
    void setBlockType(BlockType type);

    /**
     * Toggles the block type (e.g. list on/off) for the current selection.
     */
    void toggleBlockType(BlockType type);

    /**
     * Inserts a table with the given dimensions after the current block.
     */
    void insertTable(int rows, int cols);

    /**
     * Opens the link editing panel for the current selection. The
     * {@code anchor} element is used to position the panel popup.
     *
     * @param anchor
     *               the DOM element to anchor the link panel below (typically
     *               the toolbar's link button).
     */
    void insertLink(Element anchor);

    /**
     * Opens the variable insertion panel at the current cursor position. The
     * {@code anchor} element is used to position the panel popup.
     *
     * @param anchor
     *               the DOM element to anchor the variable panel below
     *               (typically the toolbar's variable button).
     */
    void insertVariable(Element anchor);

    /**
     * Inserts text at the current cursor position, replacing any active
     * selection.
     *
     * @param text
     *             the text to insert.
     */
    void insertText(String text);
}
