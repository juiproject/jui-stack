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
package com.effacy.jui.core.client.navigation;

/**
 * Represents and reference and matching mechanism for navigation.
 * <p>
 * The reference (see {@link #getReference()} is what is populated into the
 * navigation path when the item is activated. The matching (see
 * {@link #matchReference(String)}) is the means to match an item to a path
 * segment in a full navigation path sequence.
 *
 * @author Jeremy Buckley
 */
public interface INavigationReference {

    /**
     * The path reference for the item.
     * 
     * @return the reference.
     */
    public String getReference();


    /**
     * Matches the passed path segment to this item to determine if it
     * represented that segment. The default is to test against
     * {@link #getReference()}.
     * 
     * @param segment
     *            the segment to test against.
     * @return {@code true} if there is a match.
     */
    default public boolean matchReference(String segment) {
        return getReference ().equals (segment);
    }

}
