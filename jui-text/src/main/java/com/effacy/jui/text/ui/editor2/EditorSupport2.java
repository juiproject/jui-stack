package com.effacy.jui.text.ui.editor2;

import elemental2.dom.Node;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * JS bridge for editor2 selection and input handling.
 * <p>
 * Implementation is in {@code <module-script-base>/jui_text_editor2.js}.
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class EditorSupport2 {

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
}
