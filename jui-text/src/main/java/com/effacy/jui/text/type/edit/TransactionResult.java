package com.effacy.jui.text.type.edit;

/**
 * The result of applying a {@link Transaction} to a document.
 * <p>
 * Contains the inverse transaction (for undo) and the position mapping that
 * describes how document positions shifted.
 *
 * @param inverse
 *                the inverse transaction. Applying it undoes the original
 *                transaction's changes.
 * @param mapping
 *                the position mapping describing how positions shifted during
 *                the transaction.
 */
public record TransactionResult(Transaction inverse, Mapping mapping) {
}
