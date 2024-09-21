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
package com.effacy.jui.rpc.handler.query;

import com.effacy.jui.rpc.handler.exception.NoProcessorException;

/**
 * Represents a registry of processors that can be identified by the query they
 * support.
 * 
 * @author Jeremy Buckley
 * @param <C>
 *            the context type.
 */
public interface IQueryProcessorRegistry<C> {

    /**
     * Finds a suitable processor given the passed query.
     * 
     * @param query
     *            the query to use to locate the relevant processor.
     * @return the most relevant processor.
     * @throws NoProcessorException
     *             if no processor could be found.
     */
    public IQueryProcessor<C> find(Object query) throws NoProcessorException;
}
