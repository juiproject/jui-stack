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
 * A flat document position resolved to its structural coordinates.
 * <p>
 * Produced by {@link Positions#resolve(com.effacy.jui.text.type.FormattedText, int)}.
 * Contains both the original flat position and the block/line/char coordinates
 * it maps to.
 * <p>
 * Boundary positions (at a block's open or close token) have
 * {@code lineIndex == -1}.
 *
 * @param position
 *                    the original flat position.
 * @param blockIndex
 *                    index of the top-level block containing this position.
 * @param lineIndex
 *                    index of the line within the block, or {@code -1} if
 *                    this position is at a block boundary (open/close token).
 * @param charInLine
 *                    character offset within the line, or {@code -1} if at a
 *                    block boundary.
 * @param blockOffset
 *                    character offset from the block's content start (across
 *                    all lines, with line breaks counting as 1). This matches
 *                    the offset semantics used by {@link Selection}. Returns
 *                    {@code -1} if at a block open boundary, or the block's
 *                    content length if at a block close boundary.
 */
public record ResolvedPosition(int position, int blockIndex, int lineIndex, int charInLine, int blockOffset) {

    /**
     * Whether this position is at a block boundary (open or close token) rather
     * than inside content.
     */
    public boolean isAtBoundary() {
        return lineIndex == -1;
    }

    @Override
    public String toString() {
        if (isAtBoundary())
            return "Pos[" + position + " block=" + blockIndex + " boundary offset=" + blockOffset + "]";
        return "Pos[" + position + " block=" + blockIndex + " line=" + lineIndex + " char=" + charInLine + " blockOffset=" + blockOffset + "]";
    }
}
