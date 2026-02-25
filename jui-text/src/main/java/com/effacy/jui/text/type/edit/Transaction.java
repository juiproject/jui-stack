package com.effacy.jui.text.type.edit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.effacy.jui.text.type.FormattedText;

/**
 * A sequence of {@link Step}s that are applied atomically to a document.
 * <p>
 * Transactions are the sole mechanism for mutating an {@link EditorState}'s
 * document. After application, the transaction produces an inverse transaction
 * that can undo all changes (used by {@link History}).
 * <p>
 * Usage:
 * <pre>
 * Transaction tr = Transaction.create()
 *     .step(new InsertBlockStep(0, block))
 *     .step(new SetBlockIndentStep(0, 2));
 * TransactionResult result = tr.apply(doc);
 * Transaction inverse = result.inverse();
 * Mapping mapping = result.mapping();
 * </pre>
 */
public class Transaction {

    private final List<Step> steps = new ArrayList<>();
    private Selection selection;

    /**
     * Creates an empty transaction.
     */
    public static Transaction create() {
        return new Transaction();
    }

    /**
     * Sets the selection that should be applied after this transaction. When
     * set, {@link EditorState#apply(Transaction)} uses this selection instead
     * of mapping the previous selection through the position changes.
     *
     * @param selection
     *                  the explicit selection.
     * @return this transaction for chaining.
     */
    public Transaction setSelection(Selection selection) {
        this.selection = selection;
        return this;
    }

    /**
     * The explicit selection for this transaction, or {@code null} if the
     * selection should be derived by mapping through position changes.
     */
    public Selection selection() {
        return selection;
    }

    /**
     * Adds a step to this transaction.
     *
     * @param step
     *             the step to add.
     * @return this transaction for chaining.
     */
    public Transaction step(Step step) {
        if (step != null)
            steps.add(step);
        return this;
    }

    /**
     * The steps in this transaction.
     *
     * @return unmodifiable list of steps.
     */
    public List<Step> steps() {
        return Collections.unmodifiableList(steps);
    }

    /**
     * Whether this transaction has any steps.
     */
    public boolean isEmpty() {
        return steps.isEmpty();
    }

    /**
     * Apply all steps to the document in order. Each step mutates the document
     * in place. Returns a {@link TransactionResult} containing the inverse
     * transaction and the position {@link Mapping}.
     *
     * @param doc
     *            the document to mutate.
     * @return the result containing inverse and mapping.
     */
    public TransactionResult apply(FormattedText doc) {
        List<Step> inverseSteps = new ArrayList<>();
        List<StepMap> maps = new ArrayList<>();
        for (Step step : steps) {
            StepResult result = step.apply(doc);
            inverseSteps.add(result.inverse());
            maps.add(result.map());
        }
        Collections.reverse(inverseSteps);
        Transaction inverse = new Transaction();
        inverse.steps.addAll(inverseSteps);
        return new TransactionResult(inverse, new Mapping(maps));
    }
}
