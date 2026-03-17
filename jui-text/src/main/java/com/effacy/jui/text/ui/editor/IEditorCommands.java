package com.effacy.jui.text.ui.editor;

import com.effacy.jui.text.type.FormattedBlock.BlockType;
import com.effacy.jui.text.type.FormattedLine.FormatType;

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
     * Inserts an equation block after the current block.
     */
    void insertEquation();

    /**
     * Inserts a diagram block after the current block.
     */
    void insertDiagram();

    /**
     * Inserts text at the current cursor position, replacing any active
     * selection.
     *
     * @param text
     *             the text to insert.
     */
    void insertText(String text);

    /**
     * Synchronises the editor's internal selection state with the current
     * DOM selection. Call this before querying selection-dependent state
     * (e.g. {@link #currentLink()}) or before opening a popup that will
     * modify the document at the captured selection.
     */
    void syncSelection();

    /**
     * Returns the link URL at the current cursor position, or {@code null}
     * if the cursor is not inside a link. Call {@link #syncSelection()}
     * first to ensure the selection is up to date.
     */
    String currentLink();

    /**
     * Applies a link URL to the current range selection. If the selection
     * is a cursor (collapsed) and not inside an existing link, this is a
     * no-op.
     *
     * @param url
     *            the URL to apply.
     */
    void applyLink(String url);

    /**
     * Removes the link from the current selection.
     */
    void removeLink();

    /**
     * Inserts a variable at the current cursor position.
     *
     * @param name
     *             the variable identifier.
     * @param label
     *             the display label for the variable.
     */
    void applyVariable(String name, String label);
}
