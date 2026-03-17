package com.effacy.jui.text.ui.editor;

import elemental2.dom.Node;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * JS bridge for editor2 selection and input handling.
 * <p>
 * Implementation is in {@code <module-script-base>/jui_text_editor2.js}.
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class EditorSupport {

    /**
     * Reads the current DOM selection mapped to block coordinates.
     *
     * @param editorEl
     *                 the contenteditable editor element.
     * @return array {@code [anchorBlock, anchorOffset, headBlock, headOffset]}
     *         or {@code null} if no valid selection.
     */
    public static native int[] readSelection(Node editorEl);

    /**
     * Sets the DOM selection to a cursor at the given block/offset.
     *
     * @param editorEl
     *                   the contenteditable editor element.
     * @param blockIndex
     *                   the block index (data-block-index attribute).
     * @param offset
     *                   the character offset within the block.
     */
    public static native void setCursor(Node editorEl, int blockIndex, int offset);

    /**
     * Sets the DOM selection to a range spanning two block/offset pairs.
     *
     * @param editorEl
     *                     the contenteditable editor element.
     * @param anchorBlock
     *                     anchor block index.
     * @param anchorOffset
     *                     anchor character offset.
     * @param headBlock
     *                     head (focus) block index.
     * @param headOffset
     *                     head character offset.
     */
    public static native void setSelection(Node editorEl, int anchorBlock, int anchorOffset, int headBlock, int headOffset);

    /**
     * Counts the total number of characters in a block element.
     *
     * @param el
     *           the block element.
     * @return the character count.
     */
    public static native int charCount(Node el);

    /**
     * Returns the {@code inputType} property from an {@code InputEvent}.
     *
     * @param event
     *              the native event object.
     * @return the input type string, or {@code null}.
     */
    public static native String getInputType(Object event);

    /**
     * Returns the {@code data} property from an {@code InputEvent}.
     *
     * @param event
     *              the native event object.
     * @return the input data string, or {@code null}.
     */
    public static native String getInputData(Object event);

    /**
     * Returns the plain-text content from a {@code ClipboardEvent}.
     *
     * @param event
     *              the native clipboard event object.
     * @return the plain text, or {@code null}.
     */
    public static native String getClipboardText(Object event);

    /**
     * Moves the cursor to the start of the content of a contenteditable element.
     *
     * @param element
     *                the contenteditable element.
     */
    public static native void moveCursorToStart(Object element);

    /**
     * Moves the cursor to the end of the content of a contenteditable element.
     *
     * @param element
     *                the contenteditable element.
     */
    public static native void moveCursorToEnd(Object element);

    /**
     * Returns the character offset of the cursor within a contenteditable cell
     * element, or {@code -1} if the cursor is not inside the element or the
     * selection is not a cursor (i.e. is a range).
     *
     * @param cellElement
     *                    the cell element to test against.
     * @return cursor offset, or {@code -1}.
     */
    public static native int cursorOffsetInCell(Object cellElement);

    /**
     * Returns the {@code <td>} or {@code <th>} element (with
     * {@code contenteditable="true"}) containing the current selection anchor,
     * searching upward within {@code editorEl}. Returns {@code null} if the
     * selection is not inside a contenteditable cell.
     *
     * @param editorEl
     *                 the contenteditable editor element.
     * @return the cell element, or {@code null}.
     */
    public static native Object cellFromSelection(Node editorEl);

    /**
     * Returns the {@code [anchorOffset, headOffset]} character offsets of the
     * current selection within a contenteditable cell element, or {@code null}
     * if the selection is not inside the element.
     *
     * @param cellElement
     *                    the cell element to test against.
     * @return {@code [anchorOffset, headOffset]}, or {@code null}.
     */
    public static native int[] selectionInCell(Object cellElement);

    /**
     * Sets the DOM selection to a range within a cell element at the given
     * character offsets.
     *
     * @param cellElement
     *                    the cell element.
     * @param from
     *             start character offset.
     * @param to
     *           end character offset.
     */
    public static native void setSelectionInCell(Object cellElement, int from, int to);

    /**
     * Renders a LaTeX expression into a DOM element using KaTeX.
     *
     * @param target
     *               the element to render into.
     * @param text
     *             the LaTeX source.
     * @param displayMode
     *                    {@code true} for display mode (block), {@code false} for
     *                    inline.
     * @return {@code null} on success, or an error message string.
     */
    public static native String latex(Node target, String text, boolean displayMode);

    /**
     * Encodes PlantUML source and returns the full image URL.
     *
     * @param baseurl
     *                the PlantUML server base URL.
     * @param text
     *             the PlantUML source text.
     * @return the full image URL.
     */
    public static native String diagram(String baseurl, String text);
}
