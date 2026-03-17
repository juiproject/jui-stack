package com.effacy.jui.text.ui.editor;

import java.util.Map;
import java.util.Set;

import com.effacy.jui.text.type.FormattedLine;
import com.effacy.jui.text.type.FormattedLine.FormatType;
import com.effacy.jui.text.type.edit.EditorState;
import com.effacy.jui.text.type.edit.Transaction;

/**
 * Provides block handlers with access to the editor's services and state.
 * <p>
 * An instance is created once per {@link Editor} and passed to every
 * {@link IBlockHandler} method. Handlers must not hold the instance beyond the
 * lifecycle of the editor itself.
 */
public interface IEditorContext {

    /**
     * The root {@code contenteditable} div element of the editor.
     */
    elemental2.dom.Element editorEl();

    /**
     * The current editor state (document and selection).
     */
    EditorState state();

    /**
     * Applies a transaction to the state, pushes its inverse to the undo
     * history, and triggers a full re-render.
     *
     * @param tr
     *           the transaction to apply (ignored if {@code null}).
     */
    void applyTransaction(Transaction tr);

    /**
     * Applies a transaction to the state and pushes its inverse to history
     * but does <em>not</em> re-render. Use this when the handler updates the
     * DOM directly (e.g. cell formatting or blur-sync) and a full re-render
     * would lose the current editing position.
     *
     * @param tr
     *           the transaction to apply (ignored if {@code null}).
     */
    void applyTransactionSilent(Transaction tr);

    /**
     * Reads the current DOM selection and updates {@link #state()}'s
     * selection. Skips when inside a table cell (selection handled separately).
     */
    void syncSelectionFromDom();

    /**
     * Maps each {@link FormatType} to its CSS class name as emitted on
     * {@code <span>} elements during rendering.
     */
    Map<FormatType, String> formatClasses();

    /**
     * The editor's CSS accessor, providing class name strings for every
     * element the editor and its handlers render.
     */
    Editor.ILocalCSS styles();

    /**
     * The ordered-list marker formatter used to generate the visible counter
     * string (e.g. "1", "a", "iii") for each OLIST block.
     */
    Editor.IListIndexFormatter listIndexFormatter();

    /**
     * Renders a single {@link FormattedLine}'s content (plain text, spans,
     * links) as child nodes of {@code parent}.
     *
     * @param parent
     *               the DOM element to append segments into.
     * @param line
     *               the formatted line to render.
     */
    void renderLine(elemental2.dom.Element parent, FormattedLine line);

    /**
     * Notifies the editor that the current context is a table cell, and
     * requests a toolbar update for the given set of active format types.
     * Block-type buttons are always cleared (cells have no block type).
     * Pass an empty set when no formats are active at the cursor.
     *
     * @param activeFormats
     *                      the set of format types active at the current
     *                      cell selection; must not be {@code null}.
     */
    void notifyCellSelection(Set<FormatType> activeFormats);
}
