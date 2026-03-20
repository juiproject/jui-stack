/*******************************************************************************
 * Copyright 2026 Jeremy Buckley
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * <a href= "http://www.apache.org/licenses/LICENSE-2.0">Apache License v2</a>
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
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
