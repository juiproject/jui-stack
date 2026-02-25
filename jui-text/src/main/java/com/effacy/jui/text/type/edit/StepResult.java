package com.effacy.jui.text.type.edit;

/**
 * The result of applying a {@link Step} to a document.
 * <p>
 * Contains the inverse step that can undo the applied change and a
 * {@link StepMap} describing how positions shifted.
 *
 * @param inverse
 *                the step that, when applied, undoes the original step's
 *                changes.
 * @param map
 *                the position map describing how positions shifted (never
 *                null).
 */
public record StepResult(Step inverse, StepMap map) {
}
