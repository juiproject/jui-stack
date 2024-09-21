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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.effacy.jui.rpc.handler.exception.NoProcessorException;

/**
 * A standard implementation of {@link IQueryProcessorRegistry}. This will walk
 * though each processor (in the order they were added) then through each
 * registry delegate (in the order they were added) to find the relevant
 * processor. For processors added directly to this registry each will be
 * queries (see {@link IQueryProcessor#matches(Object)}).
 * 
 * @author Jeremy Buckley
 * @param <C>
 *            the context type.
 */
public class QueryProcessorRegistry<C> implements IQueryProcessorRegistry<C> {

    /**
     * The processors in the registry.
     */
    private List<IQueryProcessor<C>> processors = new ArrayList<IQueryProcessor<C>> ();

    /**
     * Delegates.
     */
    private List<IQueryProcessorRegistry<C>> delegates = new ArrayList<IQueryProcessorRegistry<C>> ();

    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.rpc.handler.query.IQueryProcessorRegistry#find(java.lang.Object)
     */
    @Override
    public IQueryProcessor<C> find(Object query) throws NoProcessorException {
        if (query == null)
            throw new NoProcessorException (Void.class);

        // Try the registered processors.
        for (IQueryProcessor<C> processor : processors) {
            if (processor.matches (query))
                return processor;
        }

        // Try the delegates.
        for (IQueryProcessorRegistry<C> delegate : delegates) {
            try {
                return delegate.find (query);
            } catch (NoProcessorException e) {
                // Onto next.
            }
        }

        // Nothing found.
        throw new NoProcessorException (query.getClass ());
    }


    /**
     * Adds a processor.
     * 
     * @param processor
     *            the processor to add.
     */
    public void add(IQueryProcessor<C> processor) {
        processors.add (processor);
    }


    /**
     * Adds a collection of processors.
     * 
     * @param processors
     *            the processors to add.
     */
    public void add(Collection<IQueryProcessor<C>> processors) {
        if (processors != null)
            this.processors.addAll (processors);
    }


    /**
     * Sets the processors.
     * 
     * @param processors
     *            the processors.
     */
    public void setProcessors(List<IQueryProcessor<C>> processors) {
        if (processors != null)
            this.processors = processors;
    }


    /**
     * Adds a delegate.
     * 
     * @param delegate
     *            the delegate to add.
     */
    public void add(IQueryProcessorRegistry<C> delegate) {
        delegates.add (delegate);
    }


    /**
     * Sets the delegates.
     * 
     * @param delegates
     *            the delegates.
     */
    public void setDelegates(List<IQueryProcessorRegistry<C>> delegates) {
        if (delegates != null)
            this.delegates = delegates;
    }
}
