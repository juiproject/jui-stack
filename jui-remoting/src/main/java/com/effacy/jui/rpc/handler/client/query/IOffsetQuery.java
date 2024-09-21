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

public interface IOffsetQuery<T> {

    /**
     * Assigns the size of the page (the maximum number of results to return).
     * 
     * @param size
     *            the size.
     */
    public void setBatchSize(int size);


    /**
     * Obtains the assigned page size (see {@link #setBatchSize(int)}).
     * 
     * @return the page size.
     */
    public int getBatchSize();


    /**
     * Sets the zero indexed page to return results for.
     * 
     * @param page
     *            the page.
     */
    public void setPage(T page);


    public T getPage();
}
