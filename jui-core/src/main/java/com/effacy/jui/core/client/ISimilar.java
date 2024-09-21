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
package com.effacy.jui.core.client;

/**
 * Implemented by classes that have a notion of similarity. This is a weaker
 * form of comparison that equality or sameness (same being object-level
 * equality) where similarity is based on an equivalence across some
 * characteristic.
 * <p>
 * A common case is a record which has an ID and a version. Relative to the ID
 * two records are the same even if their versions differ (they represent the
 * same underlying entity). An equality check would encompass the version along
 * with the ID (as those two dimensions should completely determine the record).
 *
 * @author Jeremy Buckley
 */
public interface ISimilar {
    /**
     * Determines if this is the same as the passed object.
     * 
     * @param obj
     *            the object to test.
     * @return {@code true} if they are the same.
     */
    public boolean same(Object obj);
}
