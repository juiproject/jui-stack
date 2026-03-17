package com.effacy.jui.text.type.edit;

import com.effacy.jui.text.type.FormattedText;

/**
 * A single atomic mutation to a {@link FormattedText} document.
 * <p>
 * Steps are the building blocks of transactions. Each step mutates the document
 * in place and returns a {@link StepResult} containing the inverse step (for
 * undo).
 */
public interface Step {

    /**
     * Apply this step to the document, mutating it in place.
     *
     * @param doc
     *            the document to mutate.
     * @return the result containing the inverse step.
     */
    StepResult apply(FormattedText doc);
}
