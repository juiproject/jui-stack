/*******************************************************************************
 * Copyright 2024 Jeremy Buckley
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
package com.effacy.jui.ui.client.editor;

import java.util.Set;

import com.effacy.jui.ui.client.editor.model.ContentBlock.FormatType;

import elemental2.dom.Range;

public interface IFormattedText {

    /**
     * Applies the passed formatting to the given range.
     * 
     * @param range
     *               the range to apply to.
     * @param format
     *               the format to apply.
     * @return any revised range after formatting has been applied.
     */
    public Range apply(Range range, FormatType format);

    /**
     * Clears the passed formatting from the given range.
     * 
     * @param range
     *               the range to clear from.
     * @param format
     *               the format to clear.
     * @return any revised range after formatting has been cleared.
     */
    public Range remove(Range range, FormatType format);

    /**
     * Given a range, determines the formats that apply to that range.
     * 
     * @param range
     *              the range.
     * @return the applicable formats.
     */
    public Set<FormatType> formats(Range range);

    /**
     * Used to filter which formatting tool types are supported. The default is all.
     * 
     * @param type
     *             the type to support.
     * @return {@code true} if it is supported.
     */
    default public boolean applies(FormatType type) {
        return true;
    }
}
