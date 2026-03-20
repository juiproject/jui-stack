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
