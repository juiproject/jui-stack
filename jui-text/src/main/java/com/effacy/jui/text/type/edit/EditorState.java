package com.effacy.jui.text.type.edit;

import com.effacy.jui.text.type.FormattedText;

/**
 * The current state of the editor: document and selection.
 * <p>
 * All document mutations go through {@link #apply(Transaction)}, which mutates
 * the document in place and returns the inverse transaction for undo. The
 * {@link History} class manages undo/redo by storing these inverse transactions.
 */
public class EditorState {

    private final FormattedText doc;
    private Selection selection;

    private EditorState(FormattedText doc, Selection selection) {
        this.doc = doc;
        this.selection = selection;
    }

    /**
     * Creates an editor state from a document with cursor at the start.
     *
     * @param doc
     *            the document.
     * @return the editor state.
     */
    public static EditorState create(FormattedText doc) {
        return new EditorState(doc, Selection.cursor(0, 0));
    }

    /**
     * Creates an editor state with a specific selection.
     *
     * @param doc
     *            the document.
     * @param selection
     *                  the initial selection.
     * @return the editor state.
     */
    public static EditorState create(FormattedText doc, Selection selection) {
        return new EditorState(doc, selection);
    }

    /**
     * The document.
     */
    public FormattedText doc() {
        return doc;
    }

    /**
     * The current selection.
     */
    public Selection selection() {
        return selection;
    }

    /**
     * Updates the selection.
     *
     * @param selection
     *                  the new selection.
     */
    public void setSelection(Selection selection) {
        this.selection = selection;
    }

    /**
     * Apply a transaction to the document. The document is mutated in place.
     * If the transaction carries an explicit selection it is used; otherwise
     * the current selection is mapped through the position changes.
     * <p>
     * The returned inverse transaction carries the pre-apply selection so that
     * undo restores the cursor to its original position.
     *
     * @param tr
     *           the transaction to apply.
     * @return the inverse transaction.
     */
    public Transaction apply(Transaction tr) {
        Selection beforeSelection = this.selection;

        // Compute flat positions before mutation (for fallback mapping).
        int anchorFlat = Positions.toFlat(doc, beforeSelection.anchorBlock(), beforeSelection.anchorOffset());
        int headFlat = Positions.toFlat(doc, beforeSelection.headBlock(), beforeSelection.headOffset());

        // Apply (mutates doc).
        TransactionResult result = tr.apply(doc);

        // Use explicit selection or map through changes.
        if (tr.selection() != null) {
            this.selection = tr.selection();
        } else {
            int newAnchor = result.mapping().map(anchorFlat, 1);
            int newHead = result.mapping().map(headFlat, 1);
            this.selection = Selection.fromFlat(doc, newAnchor, newHead);
        }

        // Inverse carries the pre-apply selection for undo.
        Transaction inverse = result.inverse();
        inverse.setSelection(beforeSelection);
        return inverse;
    }
}
