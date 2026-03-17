package com.effacy.jui.text.type.edit;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Manages undo/redo history for an {@link EditorState}.
 * <p>
 * Records inverse transactions from each applied change. Undo applies the
 * inverse; the result of applying the inverse becomes the redo transaction.
 * <p>
 * Usage:
 * <pre>
 * History history = new History();
 *
 * // Apply a transaction and record it.
 * Transaction inverse = state.apply(tr);
 * history.push(inverse);
 *
 * // Undo.
 * if (history.canUndo())
 *     history.undo(state);
 *
 * // Redo.
 * if (history.canRedo())
 *     history.redo(state);
 * </pre>
 */
public class History {

    private final Deque<Transaction> undoStack = new ArrayDeque<>();
    private final Deque<Transaction> redoStack = new ArrayDeque<>();
    private final int maxDepth;

    public History() {
        this(100);
    }

    /**
     * Creates a history with a maximum undo depth.
     *
     * @param maxDepth
     *                 maximum number of undo levels.
     */
    public History(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    /**
     * Record an inverse transaction from a forward application. Clears the redo
     * stack (new changes invalidate redo history).
     *
     * @param inverse
     *                the inverse transaction returned by
     *                {@link EditorState#apply(Transaction)}.
     */
    public void push(Transaction inverse) {
        undoStack.push(inverse);
        if (undoStack.size() > maxDepth)
            ((ArrayDeque<Transaction>) undoStack).removeLast();
        redoStack.clear();
    }

    /**
     * Undo the last change by applying the inverse transaction.
     * <p>
     * The result of applying the inverse (which is the inverse-of-inverse, i.e.
     * the redo transaction) is pushed to the redo stack.
     *
     * @param state
     *              the current editor state.
     * @return {@code true} if an undo was performed.
     */
    public boolean undo(EditorState state) {
        if (undoStack.isEmpty())
            return false;
        Transaction inverse = undoStack.pop();
        Transaction redo = state.apply(inverse);
        redoStack.push(redo);
        return true;
    }

    /**
     * Redo the last undone change.
     * <p>
     * Applies the redo transaction and pushes its inverse back to the undo stack.
     *
     * @param state
     *              the current editor state.
     * @return {@code true} if a redo was performed.
     */
    public boolean redo(EditorState state) {
        if (redoStack.isEmpty())
            return false;
        Transaction redo = redoStack.pop();
        Transaction inverse = state.apply(redo);
        undoStack.push(inverse);
        return true;
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /**
     * Clears all history.
     */
    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }
}
