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
package com.effacy.jui.rpc.handler.client.query;

/**
 * This is a type of {@link IRecord} that permits comparison (field level)
 * between two records.
 * <p>
 * This is different from the {@link Object#equals(Object)} comparator for
 * records, which is assumed to determine if the records represent the same
 * entity (so it is possible for the records to be <em>equal</em > but for the
 * fields not to be the same, in the sense that the records represent the same
 * entity, but different versions).
 * <p>
 * The most typical implementation of this is to compare versions and assume the
 * fields match.
 * 
 * @author Jeremy Buckley
 */
public interface IComparableRecord<D> extends IRecord<D> {

    /**
     * Compares this record with the passed data and determines if they are
     * absolutely identical.
     * 
     * @param record
     *            the record to compare against.
     * @return If they are the same.
     */
    public boolean same(Object record);
}
