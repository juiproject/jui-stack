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
package com.effacy.jui.ui.client.editor.model;

import com.effacy.jui.ui.client.editor.model.ContentBlock.Line;

/**
 * Represents a delta between two content blocks.
 */
public class ContentBlockDelta {

    /**
     * For a string diff checking see https://github.com/google/diff-match-patch.
     */
    public static class LineDelta {

        /**
         * See {@link #action()}.
         */
        private DeltaAction action;

        /**
         * See {@link #index()}.
         */
        private int index;

        /**
         * See {@link #line()}.
         */
        private Line line;

        /**
         * The action being performed.
         * 
         * @return the action.
         */
        public DeltaAction action() {
            return action;
        }

        /**
         * The index of the relevant line within the encompassing block.
         * <p>
         * For a modification or a removal the index is a 0-based reference directly to
         * the block. For an insertion it is a 0-based reference to an insertion point
         * between block (where 0 is the start and either a negative number or one that
         * is equal to the number of blocks or greater is at the end).
         * <p>
         * Given that insertions and removals modify indices the order of these delta's
         * is important as the indexes are contingent on these. By convention
         * modifications are dealt with first, followed by removals then by insertions.
         * Removals are processed in descending order (so there is not positional
         * contingency on prior removals) and additions are processed in ascending order
         * so contingency is relevant to location but assures no positional repetition.
         * 
         * @return the block reference.
         */
        public int index() {
            return index;
        }

        /**
         * The updated (or added) line.
         * 
         * @return the line (only valid on an add).
         */
        public Line line() {
            return line;
        }
    }
}
