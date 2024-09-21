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
import java.util.Map;
import java.util.Map.Entry;

import com.effacy.jui.rpc.handler.client.command.ICommand;
import com.effacy.jui.rpc.handler.client.ref.UniqueRef;
import com.effacy.jui.rpc.handler.command.IResolverTracer.ResolutionType;
import com.effacy.jui.rpc.handler.exception.NoProcessorException;
import com.effacy.jui.rpc.handler.exception.ProcessorException;
import com.effacy.jui.rpc.handler.exception.ProcessorException.ErrorType;

import java.util.Set;
import java.util.Stack;

/**
 * Standard implementation of {@link ICommandProcessorContext}.
 *
 * @author Jeremy Buckley
 */
public class CommandProcessorContext<C extends ICommandProcessorContext<C>> implements ICommandProcessorContext<C>, IResolverRegistryInjectable<C> {

    /**
     * The underlying resolver registry.
     */
    private ICommandProcessorRegistry<C> registry;

    /**
     * Map of references to resolved object instances.
     */
    private Map<UniqueRef, ReferenceLookupItem> referenceMap = new HashMap<UniqueRef, ReferenceLookupItem> ();

    /**
     * Stack of commands having been invoked.
     */
    private Stack<ICommand> commandStack = new Stack<ICommand> ();

    /**
     * Any tracer that may be assigned.
     */
    private IResolverTracer tracer;

    public class ReferenceLookupItem {
        private ICommand command;
        private Object resultant;

        public ReferenceLookupItem(ICommand command, Object resultant) {
            this.command = command;
            this.resultant = resultant;
        }

        public UniqueRef reference() {
            return command.reference ();
        }

        public String label() {
            return command.label ();
        }

        public Object resultant() {
            return resultant;
        }
    }

    /**
     * Construct without a registry.
     */
    protected CommandProcessorContext() {
        // Nothing.
    }

    /**
     * Constructs the context.
     * 
     * @param registry
     *                 the resolver registry to use.
     */
    public CommandProcessorContext(ICommandProcessorRegistry<C> registry) {
        this.registry = registry;
    }

    /**
     * Assigns a tracer to the resolver to trace resolutions.
     * 
     * @param tracer
     *               the tracer.
     */
    public void setTracer(IResolverTracer tracer) {
        this.tracer = tracer;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.rpc.handler.command.IResolverRegistryInjectable#setRegistry(com.effacy.jui.rpc.handler.command.ICommandProcessorRegistry)
     */
    @Override
    public void setRegistry(ICommandProcessorRegistry<C> registry) {
        if (registry != null)
            this.registry = registry;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.rpc.handler.command.ICommandProcessorContext#resolve(com.effacy.jui.rpc.handler.client.command.shared.ICommand,
     *      java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <S extends ICommand, T> T resolve(S command, Class<T> target) throws NoProcessorException, ProcessorException {
        if (tracer != null)
            tracer.resolving (command);

        // Check for an explicit lookup by reference.
        if ((command.lookup () != null) && (command.lookup () instanceof UniqueRef)) {
            T result = lookup ((UniqueRef) command.lookup (), target);
            if (tracer != null)
                tracer.resolved (command, result, ResolutionType.LOOKUP);
            return result;
        }

        // Check for a previously cached version.
        T reference = lookupNullable (command.reference (), target);
        if (reference != null) {
            if (tracer != null)
                tracer.resolved (command, reference, ResolutionType.CACHE);
            return reference;
        }

        // Lookup a processor.
        ICommandProcessor<S, T, C> resolver = (ICommandProcessor<S, T, C>) registry.find (command.getClass ());
        if ((target != null) && !target.isAssignableFrom (resolver.target ()))
            throw new NoProcessorException (target.getClass ());
        commandStack.push (command);
        try {
            T result = resolver.resolve (command, (C) this);
            if (result != null)
                cache (command, result);
            if (tracer != null)
                tracer.resolved (command, result, ResolutionType.RESOLVER);
            return result;
        } catch (ProcessorException | NoProcessorException e) {
            throw e;
        } catch (Throwable e) {
            // This is not particularly good as we should have dealt with this
            // by now.
            ProcessorException error = new ProcessorException ();
            error.add (ErrorType.SYSTEM, err -> err.message (e.getMessage ()));
            if (tracer != null)
                tracer.failed (command, error);
            throw error;
        } finally {
            commandStack.pop ();
        }
    }

    /**
     * Given a command reference performs a lookup for the associated resolved
     * object. This requires that the command have been previously resolved.
     * 
     * @param reference
     *                  the reference to lookup.
     * @param target
     *                  the class type that the lookup resolves to.
     * @return the associated object.
     * @throws NoProcessorException
     *                              if there is no matching lookup.
     */
    protected <T> T lookup(UniqueRef reference, Class<T> target) throws NoProcessorException {
        if (target == null)
            throw new NoProcessorException (Void.class);
        T obj = lookupNullable (reference, target);
        if (obj == null)
            throw new NoProcessorException (target.getClass ());
        return obj;
    }

    /**
     * Identical to {@link #lookup(UniqueRef, Class)} except that it will
     * return a {@code null} if no reference has been found.
     * 
     * @param reference
     *                  the reference to lookup.
     * @param target
     *                  the class type that the lookup resolves to.
     * @return the associated object (or {@code null}).
     */
    @SuppressWarnings("unchecked")
    protected <T> T lookupNullable(UniqueRef reference, Class<T> target) {
        ReferenceLookupItem obj = referenceMap.get (reference);
        if ((obj == null) || (obj.resultant == null))
            return null;
        if (!target.isAssignableFrom (obj.resultant.getClass ()))
            return null;
        return (T) obj.resultant;
    }

    /**
     * Caches an object against a reference lookup. This can be retrieved by
     * {@link #lookup(UniqueRef, Class)}.
     * 
     * @param lookup
     *               the lookup key.
     * @param target
     *               the object to cache against the key.
     */
    protected void cache(ICommand command, Object value) {
        referenceMap.put (command.reference (), new ReferenceLookupItem (command, value));
    }

    /**
     * Collection of entries from the reference map.
     * 
     * @return the entries.
     */
    protected Set<Entry<UniqueRef, ReferenceLookupItem>> references() {
        return referenceMap.entrySet ();
    }

    /**
     * Obtains a copy of the reference map of resolved entities.
     * 
     * @return the resolution of resolved references.
     */
    public Map<UniqueRef, ReferenceLookupItem> getResolutions() {
        return new HashMap<UniqueRef, ReferenceLookupItem> (referenceMap);
    }

    /**
     * Gets the calling command (the command from which this command invocation has
     * been called from).
     * 
     * @return The parent command (or {@code null} if none).
     */
    public ICommand getCallingCommand() {
        if (commandStack.size () <= 1)
            return null;
        return commandStack.get (commandStack.size () - 2);
    }

}
