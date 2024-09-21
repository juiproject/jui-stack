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
import com.effacy.jui.rpc.handler.exception.ProcessorException;

/**
 * A processor is a mechanism that takes an operating context and a query
 * request to generate a response. Generally this is for the purpose of
 * implementing a remotely executed query with response.
 *
 * @author Jeremy Buckley
 */
public interface IQueryProcessor<C> {

    /**
     * Processes a query with a given context to generate a response.
     * 
     * @param context
     *            the operating context (implementation dependent).
     * @param query
     *            the query to perform.
     * @return the response to the query.
     * @throws ProcessorException
     *             on error.
     */
    public Object process(C context, Object query) throws NoProcessorException, ProcessorException;


    /**
     * Determines if this processor is able to handle the passed query.
     * 
     * @param query
     *            the query to test for.
     * @return {@code true} if this processor and process the query.
     */
    public boolean matches(Object query);
}
