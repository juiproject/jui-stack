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
package com.effacy.jui.rpc.handler.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.effacy.jui.rpc.handler.client.command.ICommand;
import com.effacy.jui.rpc.handler.exception.NoProcessorException;

/**
 * Implementation of {@link ICommandProcessorRegistry}.
 * 
 * @author Jeremy Buckley
 */
public class CommandProcessorRegistry<C extends ICommandProcessorContext<C>> implements ICommandProcessorRegistry<C> {

    /**
     * Map of class pairs (source and target) to associated resolver.
     */
    private Map<Class<?>, ICommandProcessor<?, ?, C>> pairToResolver = new HashMap<Class<?>, ICommandProcessor<?, ?, C>> ();

    /**
     * Adds a single resolver.
     * 
     * @param resolver
     *            the resolver to add.
     */
    public void add(ICommandProcessor<?, ?, C> resolver) {
        if (resolver != null)
            pairToResolver.put (resolver.command (), resolver);
    }


    /**
     * Sets a list of resolvers.
     * 
     * @param resolvers
     *            the resolvers.
     */
    public void setResolvers(List<ICommandProcessor<?, ?, C>> resolvers) {
        if (resolvers == null)
            return;
        for (ICommandProcessor<?, ?, C> resolver : resolvers)
            add (resolver);
    }


    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.rpc.handler.command.ICommandProcessorRegistry#find(java.lang.Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public <A extends ICommand, B> ICommandProcessor<A, B, C> find(Class<A> command) throws NoProcessorException {
        if (command == null)
            throw new NoProcessorException (Void.class);
        Class<?> lookup = command;
        while (lookup != null) {
            ICommandProcessor<?, ?, C> resolver = pairToResolver.get (lookup);
            if (resolver != null)
                return (ICommandProcessor<A, B, C>) resolver;
            lookup = lookup.getSuperclass ();
            if (lookup.equals (Object.class))
                lookup = null;
        }
        throw new NoProcessorException (command);
    }

}
