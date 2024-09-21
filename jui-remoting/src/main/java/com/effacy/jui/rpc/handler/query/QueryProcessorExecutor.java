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
 * Base implementation of {@link IQueryProcessorExecutor} that includes support
 * for a registry (see {@link IQueryProcessorRegistry}).
 * 
 * @author Jeremy Buckley
 * @param <C>
 *            context type.
 */
public class QueryProcessorExecutor<C> implements IQueryProcessorExecutor<C> {

    /**
     * The registry.
     */
    private IQueryProcessorRegistry<C> registry;

    /**
     * Construct instance (no registry).
     */
    public QueryProcessorExecutor() {
        // Nothing.
    }


    /**
     * Construct instance.
     * 
     * @param registry
     *            the registry to use.
     */
    public QueryProcessorExecutor(IQueryProcessorRegistry<C> registry) {
        if (registry != null)
            this.registry = registry;
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.rpc.handler.query.IQueryProcessorExecutor#process(java.lang.Object,
     *      java.lang.Object)
     */
    @Override
    public Object process(C context, Object query) throws NoProcessorException, Exception {
        return registry.find (query).process (context, query);
    }


    /**
     * Sets the registry for the query executor.
     * 
     * @param registry
     *            the registry.
     */
    public void setRegistry(IQueryProcessorRegistry<C> registry) {
        this.registry = registry;
    }

}
