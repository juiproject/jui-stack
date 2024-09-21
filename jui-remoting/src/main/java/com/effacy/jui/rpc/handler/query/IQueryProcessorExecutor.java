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
 * Executor for queries.
 * 
 * @author Jeremy Buckley
 */
public interface IQueryProcessorExecutor<C> {

    /**
     * Executes a query.
     * 
     * @param context
     *            the context to execute within.
     * @param query
     *            the query to perform.
     * @return The result from the query.
     * @throws NoProcessorException
     *             If there is no processor available for execution.
     */
    public Object process(C context, Object query) throws NoProcessorException, Exception;
}
