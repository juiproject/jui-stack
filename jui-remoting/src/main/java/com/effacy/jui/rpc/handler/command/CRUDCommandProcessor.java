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

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.effacy.jui.rpc.handler.ComparisonSupport;
import com.effacy.jui.rpc.handler.client.command.C;
import com.effacy.jui.rpc.handler.client.command.Construct;
import com.effacy.jui.rpc.handler.client.command.ICommand.IAction;
import com.effacy.jui.rpc.handler.client.command.V;
import com.effacy.jui.rpc.handler.command.CRUDCommandProcessor.IModification;
import com.effacy.jui.rpc.handler.exception.AccessRightsProcessorException;
import com.effacy.jui.rpc.handler.exception.NoProcessorException;
import com.effacy.jui.rpc.handler.exception.NotFoundProcessorException;
import com.effacy.jui.rpc.handler.exception.ProcessorException;
import com.effacy.jui.rpc.handler.exception.ProcessorException.ErrorType;
import com.effacy.jui.validation.model.IValidator;
import com.effacy.jui.validation.model.IValidator.Message;
import com.effacy.jui.validation.model.ValidationException;

/**
 * Support class for implementing {@link ICommandProcessor}'s. Note that a
 * resolver may return a {@code null} entity upon construction.
 * 
 * @author Jeremy Buckley
 */
public abstract class CRUDCommandProcessor<CMD extends C, ETY, CTX extends ICommandProcessorContext<CTX>, M extends IModification> extends CommandProcessor<CMD, ETY, CTX> {

    /**
     * If to execute actions after any update has been performed. This is sometimes
     * useful when an action depends on updates being applied.
     * <p>
     * Note that historically actions are applied before the update so this is
     * preserved as the default.
     */
    protected boolean executeActionsAfterUpdate = false;

    /**
     * Construct with prescribed source and target classes.
     * 
     * @param source
     *               the source class.
     * @param target
     *               the target class.
     */
    protected CRUDCommandProcessor(Class<CMD> command, Class<ETY> target) {
        super (command, target);
    }

    /************************************************************************
     * General callbacks.
     ************************************************************************/

    /**
     * Called immediately after the entity has been constructed, modified and saved.
     * This is called prior to
     * {@link #apply(Object, C, ICommandProcessorContext, Modification)} which
     * assumes an already saved entity.
     * 
     * @param entity
     *                     the entity that was created.
     * @param command
     *                     the command associated with the construction of the
     *                     entity.
     * @param modification
     *                     the modification information about the entity (what was
     *                     changed).
     */
    protected void onCreated(ETY entity, CMD command, M modification) {
        // Nothing.
    }

    /**
     * On completion of any persistence activities related to the entity (persisting
     * or deleting).
     * 
     * @param entity
     *                     the entity that was persisted.
     * @param command
     *                     the command associated with the construction of the
     *                     entity.
     * @param modification
     *                     the modification information about the entity (what was
     *                     changed).
     */
    protected void onCompletion(ETY entity, CMD command, M modification) {
        // Nothing.
    }

    /************************************************************************
     * Processor entity lifecycle callbacks.
     ************************************************************************/

    /**
     * Invoked when a lookup has successfully return an entity.
     * 
     * @param entity
     *                    the entity.
     * @param ctx
     *                    the context.
     * @param constructed
     *                    {@code true} the entity has been created (rather than
     *                    looked up).
     */
    protected void postLookup(ETY entity, CTX ctx, boolean constructed) {
        // Nothing.
    }

    /**
     * Hook prior to processing the command.
     * 
     * @param command
     *                the command.
     * @param context
     *                the operating context.
     */
    protected void preProcess(CMD command, CTX context) throws NoProcessorException, ProcessorException {
        // Nothing.
    }

    /**
     * Invoked after all actions and modifications have been performed but prior to
     * invoking {@link #onCompletion(Object, C, IModification)}.
     * 
     * @param entity
     *                     the entity.
     * @param modification
     *                     the modification that was used.
     * @param deleted
     *                     {@code true} if the entity has been deleted.
     */
    protected void preComplete(ETY entity, M modification, boolean deleted) {
        // Nothing.
    }

    /************************************************************************
     * Entity processing.
     ************************************************************************/

    /**
     * Default entity lookup if the lookup is not being passed through on the
     * command (which means that lookup is via a custom mechanism).
     * 
     * @param command
     *                the underlying command.
     * @param context
     *                the operating context.
     * @return the entity (or {@code null}).
     */
    protected ETY lookup(CMD command, CTX context) {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.rpc.handler.command.ICommandProcessor#resolve(com.effacy.jui.rpc.handler.client.ICommand,
     *      com.effacy.jui.rpc.handler.command.ICommandProcessorContext)
     */
    @Override
    public final ETY resolve(CMD command, CTX context) throws NoProcessorException, ProcessorException {
        preProcess (command, context);

        ETY entity = null;
        try {
            M persistence = null;
            if (command.lookup () != null) {
                entity = lookup (command.lookup (), context);
                if (entity == null)
                    throw new NoProcessorException (command.getClass (), command.lookup ().getClass ());
                postLookup (entity, context, false);

                // Determine if the action is a delete.
                if (command.isDelete ()) {
                    persistence = modification (entity, command, context, ModificationMode.DELETED);
                    delete (entity, context, persistence);
                    preComplete (entity, persistence, true);
                    onCompletion (entity, command, persistence);
                    return null;
                }

                // Create the persistence context and apply any modifications.
                persistence = modification (entity, command, context, ModificationMode.UPDATED);
            } else {
                if (command.get_construct () != null) {
                    entity = construct (command.get_construct (), context);
                    if (entity != null) {
                        postLookup (entity, context,  true);
                        persistence = modification (entity, command, context, ModificationMode.CREATED);
                    }
                } else {
                    entity = lookup (command, context);
                    if (entity != null)
                        postLookup (entity, context, false);
                }
                if (entity == null)
                    return entity;
                if (persistence == null)
                    persistence = modification (entity, command, context, ModificationMode.UPDATED);
            }

            // Perform modification and persistence as required.
            boolean callOnComplete = false;

            if (executeActionsAfterUpdate) {
                modify (entity, command, context, persistence);
                persistence.validate ();
            }
            if (!command.get_actions ().isEmpty ()) {
                if (actions (entity, command.get_actions (), context, persistence)) {
                    persistence.clear ();
                    preComplete (entity, persistence, false);
                    onCompletion (entity, command, persistence);
                    return null;
                }
            }
            if (!executeActionsAfterUpdate) {
                modify (entity, command, context, persistence);
                persistence.validate ();
            }
            if (persistence.isCreated ()) {
                // We save the entity here ready for any additional changes from
                // apply.
                entity = persist (entity);
                onCreated (entity, command, persistence);
                persistence.validate ();
                persistence.clear ();
                apply (entity, command, context, persistence);
                persistence.validate ();
                callOnComplete = true;
            } else {
                apply (entity, command, context, persistence);
                persistence.validate ();
            }
            preComplete (entity, persistence, false);
            if (persistence.isModified ()) {
                entity = persist (entity);
                onCompletion (entity, command, persistence);
            } else if (callOnComplete) {
                onCompletion (entity, command, persistence);
            }
        } catch (ProcessorException | NoProcessorException e) {
            throw e;
        } catch (Throwable e) {
            throwFor (e);
        }
        return entity;
    }

    /**
     * Look up an entity based on the prescribed lookup.
     * <p>
     * By default this scans through methods to find one that implements an
     * appropriate lookup.
     * 
     * @param lookup
     *                the lookup.
     * @param context
     *                the context to perform the lookup.
     * @return the associated entity.
     */
    @SuppressWarnings("unchecked")
    protected ETY lookup(Object lookup, CTX context) throws ProcessorException {
        for (Method method : methodsFor (getClass ())) {
            if (!target ().isAssignableFrom (method.getReturnType ()))
                continue;
            if (method.getParameterTypes ().length > 2)
                continue;
            if ((method.getParameterTypes ().length == 2) && !method.getParameterTypes ()[1].equals (context.getClass ()))
                continue;
            if (!method.getParameterTypes ()[0].isAssignableFrom (lookup.getClass ()))
                continue;
            try {
                method.setAccessible (true);
                if (method.getParameterTypes ().length == 2)
                    return (ETY) method.invoke (this, lookup, context);
                return (ETY) method.invoke (this, lookup);
            } catch (IllegalArgumentException e) {
                throw new NotFoundProcessorException ();
            } catch (IllegalAccessException e) {
                throw new NotFoundProcessorException ();
            } catch (InvocationTargetException e) {
                if (e.getCause () instanceof ProcessorException)
                    throw (ProcessorException) e.getCause ();
                throwFor (e.getCause ());
            } catch (Throwable e) {
                if (e instanceof ProcessorException)
                    throw (ProcessorException) e;
                throwFor (e);
            }
        }
        return null;
    }

    /**
     * Look up an entity based on the prescribed lookup.
     * <p>
     * By default this scans through methods to find one that implements an
     * appropriate lookup.
     * 
     * @param lookup
     *                the lookup.
     * @param context
     *                the context to perform the lookup.
     * @return the associated entity.
     */
    @SuppressWarnings("unchecked")
    protected ETY construct(Construct construct, CTX context) throws ProcessorException {
        for (Method method : methodsFor (getClass ())) {
            if (!target ().isAssignableFrom (method.getReturnType ()))
                continue;
            if (method.getParameterTypes ().length > 2)
                continue;
            if ((method.getParameterTypes ().length == 2) && !method.getParameterTypes ()[1].equals (context.getClass ()))
                continue;
            if (!method.getParameterTypes ()[0].equals (construct.getClass ()))
                continue;
            try {
                method.setAccessible (true);
                if (method.getParameterTypes ().length == 2)
                    return (ETY) method.invoke (this, construct, context);
                return (ETY) method.invoke (this, construct);
            } catch (IllegalArgumentException e) {
                throw new NotFoundProcessorException ();
            } catch (IllegalAccessException e) {
                throw new NotFoundProcessorException ();
            } catch (InvocationTargetException e) {
                if (e.getCause () instanceof ProcessorException)
                    throw (ProcessorException) e.getCause ();
                throwFor (e.getCause ());
            } catch (Throwable e) {
                if (e instanceof ProcessorException)
                    throw (ProcessorException) e;
                throwFor (e);
            }
        }
        return null;
    }

    /**
     * Processes a collection of actions.
     * <p>
     * Groups the actions by class and invoked
     * {@link #action(Object, Class, List, IModification)}.
     * 
     * @param entity
     *                     the entity the actions pertain to.
     * @param allActions
     *                     the actions to process.
     * @param context
     *                     the context.
     * @param modification
     *                     the modification context.
     * @return {@code true} if any of the actions resulted in deletion of the
     *         entity.
     */
    protected boolean actions(ETY entity, List<IAction> allActions, CTX context, M modification) throws ProcessorException {
        // Collate actions into type.
        Map<Class<? extends IAction>, List<IAction>> groups = new HashMap<Class<? extends IAction>, List<IAction>> ();
        for (IAction action : allActions) {
            if (action == null)
                continue;
            List<IAction> actions = groups.get (action.getClass ());
            if (actions == null) {
                actions = new ArrayList<IAction> ();
                groups.put (action.getClass (), actions);
            }
            actions.add (action);
        }

        // Order the classes.
        List<Class<? extends IAction>> actionClasses = new ArrayList<> (groups.keySet ());
        Collections.sort (actionClasses, new Comparator<Class<? extends IAction>> () {

            @Override
            public int compare(Class<? extends IAction> o1, Class<? extends IAction> o2) {
                if (o1 == o2)
                    return 0;
                int p1 = priority (o1);
                int p2 = priority (o2);
                if (p1 < p2)
                    return 1;
                if (p1 > p2)
                    return -1;
                return o1.getSimpleName ().compareTo (o2.getSimpleName ());
            }

        });

        // Process the actions in priority order.
        for (Class<? extends IAction> klass : actionClasses)
            if (action (entity, klass, groups.get (klass), context, modification))
                return true;
        return false;
    }

    /**
     * Process a collection of actions of the same type.
     * 
     * @param entity
     *                     the entity.
     * @param actions
     *                     the action being performed.
     * @param context
     *                     the context.
     * @param modification
     *                     the modification context.
     * @return {@code true} if the action deletes the entity.
     */
    protected boolean action(ETY entity, Class<? extends IAction> actionClass, List<IAction> actions, CTX context, M modification) throws ProcessorException {
        if ((actions == null) || actions.isEmpty ())
            return false;

        // Pull out a list of all the methods on the class.
        List<Method> methods = new ArrayList<Method> ();
        Class<?> klass = getClass ();
        while ((klass != null) && !klass.getName ().endsWith ("AbstractCRUDResolver")) {
            for (Method method : klass.getDeclaredMethods ())
                methods.add (method);
            klass = klass.getSuperclass ();
        }

        // Find a method that is able to process the actions. A method must have
        // as its second parameter one that is assignable from the action class
        // or be a collection that is assignable. The first matching case will
        // be used.
        LOOP: for (Method method : methods) {
            // Basic parameter checks.
            if ((method.getParameterTypes ().length < 2) || (method.getParameterTypes ().length > 4))
                continue;
            if (!method.getParameterTypes ()[0].isAssignableFrom (entity.getClass ()))
                continue;

            // Validate the parameters.
            Object[] params = new Object[method.getParameterTypes ().length];
            params[0] = entity;
            Class<?> param1Type = method.getParameterTypes ()[1];
            if (param1Type.isArray ()) {
                if (!param1Type.getComponentType ().equals (actionClass))
                    continue;
            } else if (!param1Type.equals (actionClass))
                continue;

            // Fill out the remaining params.
            for (int i = 2; i < params.length; i++) {
                if (method.getParameterTypes ()[i].equals (context.getClass ()))
                    params[i] = context;
                else if ((modification != null) && method.getParameterTypes ()[i].equals (modification.getClass ()))
                    params[i] = modification;
                else
                    continue LOOP;
            }

            try {
                method.setAccessible (true);
                if (param1Type.isArray ()) {
                    Object[] actionArray = (Object[]) Array.newInstance (actionClass, actions.size ());
                    int idx = 0;
                    for (IAction action : actions)
                        actionArray[idx++] = action;
                    params[1] = actionArray;
                    method.invoke (this, params);
                } else {
                    for (IAction action : actions) {
                        params[1] = action;
                        method.invoke (this, params);
                    }
                }

                // Check for an action only annotation.
                for (Annotation annotation : method.getAnnotations ()) {
                    if (annotation.annotationType ().equals (IAction.Terminal.class)) {
                        return true;
                    }
                }
            } catch (IllegalArgumentException e) {
                throw new NotFoundProcessorException ();
            } catch (IllegalAccessException e) {
                throw new NotFoundProcessorException ();
            } catch (InvocationTargetException e) {
                if (e.getCause () instanceof ProcessorException)
                    throw (ProcessorException) e.getCause ();
                throwFor (e.getCause ());
            } catch (Throwable e) {
                if (e instanceof ProcessorException)
                    throw (ProcessorException) e;
                throwFor (e);
            }
        }

        return false;
    }

    /**
     * Extracts a priority for the given class.
     * 
     * @param klass
     *              the class.
     * @return the priority.
     */
    protected int priority(Class<? extends IAction> klass) {
        if (klass == null)
            return -1;
        IAction.Priority priority = klass.getAnnotation (IAction.Priority.class);
        if (priority == null)
            return 0;
        return priority.value ();
    }

    /**
     * Obtains the methods declared on the passed class and its sub-classes.
     * 
     * @param klass
     *              the class.
     * @return the methods.
     */
    protected List<Method> methodsFor(Class<?> klass) {
        List<Method> methods = new ArrayList<Method> ();
        while ((klass != null) && !klass.getPackage ().getName ().startsWith ("java")) {
            for (Method method : klass.getDeclaredMethods ())
                methods.add (method);
            klass = klass.getSuperclass ();
        }
        return methods;
    }

    /**
     * Delete the given entity. The default implementation throws an
     * {@link AccessRightsProcessorException} on the assumption that delete is not
     * supported (from a use perspective this can be treated as the user not having
     * rights to delete).
     * 
     * @param entity
     *                     the entity to delete.
     * @param context
     *                     the processor context.
     * @param modification
     *                     the modification context for recording changes against.
     * @throws ProcessorException
     *                            On error.
     */
    protected void delete(ETY entity, CTX context, M modification) throws ProcessorException {
        throw new AccessRightsProcessorException ();
    }

    /**
     * Performs a modification of the passed entity (but performs no save). If the
     * entity is modified then the modification must be marked as modified otherwise
     * the entity will not be saved (unless it is a newly constructed entity).
     * <p>
     * Since modification could occur in a chained manner (i.e. by way of
     * sub-classing and overriding) persistence is handled separately for the entity
     * in question. It may be that secondary entities are required and if they are
     * created outside of the context of the resolver framework (i.e. constructed
     * directly) then they should be saved directly as well by this method (unless
     * the entity has an appropriate cascade policy).
     * 
     * @param entity
     *                     the entity to be modified.
     * @param command
     *                     the associated command.
     * @param context
     *                     the processor context.
     * @param modification
     *                     the modification context for recording changes against.
     * @throws ProcessorException
     *                            on error.
     */
    protected abstract void modify(ETY entity, CMD command, CTX context, M modification) throws ProcessorException;

    /**
     * This is similar to
     * {@link #modify(Object, C, ICommandProcessorContext, Modification)} except
     * that changes are expected to be self saving. In this case the passed entity
     * is assumed to have been saved (i.e. post-construction).
     * 
     * @param entity
     *                     the entity to be modified.
     * @param command
     *                     the associated command.
     * @param context
     *                     the processor context.
     * @param modification
     *                     the modification context for recording changes against.
     * @throws ProcessorException
     *                            on error.
     */
    protected void apply(ETY entity, CMD command, CTX context, M modification) throws ProcessorException {
        // Nothing.
    }

    /**
     * Performs a low-level persistence of the entity.
     * 
     * @param entity
     *               the entity to persist.
     * @return The persisted entity.
     * @throws ProcessorException
     *                            On error.
     */
    protected abstract ETY persist(ETY entity) throws ProcessorException;

    /**
     * Constructs a modification.
     * 
     * @param entity
     *                  the entity being modified.
     * @param command
     *                  the command performing the modification.
     * @param context
     *                  the context in which this is operating.
     * @param operation
     *                  the modification operation being performed.
     * @return The modification instance.
     * @throws ProcessorException
     *                            On error.
     */
    protected abstract M modification(ETY entity, CMD command, CTX context, ModificationMode operation) throws ProcessorException;

    /**
     * Mode of persistence for use by {@link Modification}.
     */
    public enum ModificationMode {
        CREATED, UPDATED, DELETED;
    }

    public interface IModification {

        /**
         * Determines if the persistence was a created.
         * 
         * @return {@code true} if it was a creation of a new entity.
         */
        public boolean isCreated();

        /**
         * Determines if the entity was modified.
         * 
         * @return {@code true} if it was modified.
         */
        public boolean isModified();

        /**
         * Checks to see if there are any errors (from validation) and throws an
         * exception if there are.
         * 
         * @throws ProcessorException
         *                            the exception thrown if there are errors.
         */
        public void validate() throws ProcessorException;

        /**
         * Clear all changes.
         */
        public void clear();
    }

    /**
     * A persistence context carries information about modification and creation for
     * the purposes of persisting the entity and generation of an audit trail.
     */
    public static class Modification implements IModification {

        /**
         * If changes have been applied (regardless of whether or not these have been
         * named).
         */
        private boolean modified;

        /**
         * The persistence mode for the modification.
         */
        private ModificationMode mode;

        /**
         * Collections of errors accumulated over the changes.
         */
        private List<ProcessorException.Error> errors = new ArrayList<> ();

        /**
         * Construct with a created state. If this is created then it will automatically
         * be marked as modified.
         * 
         * @param mode
         *                   the modification mode.
         * @param accessRole
         *                   the applicable access role.
         */
        public Modification(ModificationMode mode) {
            this.mode = mode;
        }

        /**
         * Mark as modified.
         */
        public void modified() {
            this.modified = true;
        }

        /**
         * Determines if the persistence was a update (as opposed to a create).
         * 
         * @return {@code true} if it was an update of an existing entity.
         */
        public boolean isUpdated() {
            return ModificationMode.UPDATED.equals (mode);
        }

        /**
         * Determines if the persistence was a created.
         * 
         * @return {@code true} if it was a creation of a new entity.
         */
        public boolean isCreated() {
            return ModificationMode.CREATED.equals (mode);
        }

        /**
         * Determines if the persistence a removal (deletion).
         * 
         * @return {@code true} if the entity has been deleted.
         */
        public boolean isDeleted() {
            return ModificationMode.DELETED.equals (mode);
        }

        /**
         * Determines if the entity was modified.
         * 
         * @return {@code true} if it was modified.
         */
        public boolean isModified() {
            return modified;
        }

        /**
         * Clear all changes.
         */
        public void clear() {
            modified = false;
            errors.clear ();
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.rpc.handler.command.CRUDCommandProcessor.IModification#validate()
         */
        @Override
        public void validate() throws ProcessorException {
            if (!errors.isEmpty ())
                throw new ProcessorException (errors);
        }

        /**
         * Registers a processor error.
         * 
         * @param err
         *             the processor error to register.
         */
        public void error(ProcessorException.Error err) {
            if (err != null)
                errors.add (err);
        }

        /**
         * Extracts errors from the passed validation exception.
         * 
         * @param path
         *             the path to apply (if this is {@code null} then the path from the
         *             message is used).
         * @param e
         *             the errors.
         */
        public void error(String path, ValidationException e) {
            if (e != null) {
                for (IValidator.Message message : e)
                    error (path, message);
            }
        }

        /**
         * Registers an error for processing later.
         * 
         * @param path
         *                the path to apply.
         * @param message
         *                the message.
         */
        public void error(String path, IValidator.Message message) {
            if (message != null)
                errors.add (new ProcessorException.Error (ErrorType.VALIDATION).path ((path != null) ? path : message.getPath ()).message (message.getMessage ()).code (message.getCode ()));
        }

        /**
         * Registers an error for processing later.
         * 
         * @param path
         *                the path to apply.
         * @param message
         *                the message.
         */
        public void error(String path, String message) {
            if (message != null)
                errors.add (new ProcessorException.Error (ErrorType.VALIDATION).path (path).message (message));
        }

        /**
         * When an unexpected error occurs, translate it to a suitable message to be
         * recorded in the error messages for the modification.
         * 
         * @param e
         *          the exception.
         * @return the error message.
         */
        protected String translateUnexpectedError(Exception e) {
            return "uncaught exception " + e.getClass ().getSimpleName ();
        }

        /**
         * For use to handle post update.
         */
        @FunctionalInterface
        public interface PostUpdateHandler<T> {

            /**
             * Process post update.
             * 
             * @param value
             *              the value update.
             * @throws Exception
             *                   on error.
             */
            public void process(T value) throws Exception;
        }

        /**
         * Describes a setter for setting a value on an entity.
         */
        @FunctionalInterface
        public interface ValueSetter<T> {

            /**
             * Applies the given value.
             * 
             * @param value
             *              the value.
             * @throws Exception
             *                             on error.
             */
            public void apply(T value) throws Exception;
        }

        /**
         * Used to map value with the ability to throw an exception during the mapping
         * process.
         */
        public interface ValueMapper<A,B> {
        
            /**
             * Maps the value (of type A) to a new value (of type B).
             * 
             * @param value
             *              the value to map from.
             * @return the mapped value.
             * @throws Exception
             *                   if there was a problem performing the mapping.
             */
            public B map(A value) throws Exception;
        }

        /**
         * Used to record an update event.
         */
        @FunctionalInterface
        public interface IUpdateRecorder<T> {

            /**
             * Record an update event.
             * 
             * @param value
             *                         the revised value (can be {@code null}).
             * @param oldValue
             *                         the prior value (can be {@code null}).
             * @param oldValueProvided
             *                         if the old value was provided (since {@code null} is
             *                         a valid value).
             */
            public void record(T value, T oldValue, boolean oldValueProvided);
        }

        /**
         * An extended version of {@link V} that allows for exceptions to be thrown from
         * {@link #getValue()}.
         */
        public class EV<B> {

            private V<B> cmd;

            public EV(V<B> cmd) {
                this.cmd = cmd;
            }

            public B getValue() throws Exception {
                return cmd.getValue ();
            }

            public boolean isSet() {
                return cmd.isSet ();
            }

            public boolean isNull() {
                return cmd.isNull ();
            }
             
        }

        /**
         * Returned by {@link Modification#updater(ValueSetter)} (and related) to
         * configure an assignment.
         */
        public class Setter<T> {

            /**
             * Value validator.
             */
            private IValidator<T> validator;

            /**
             * For handling exceptions.
             */
            private BiConsumer<Exception, Consumer<Message>> exception;

            /**
             * The setter for setting the value.
             */
            private ValueSetter<T> setter;

            /**
             * The value where a direct value is applied.
             */
            private T value;

            /**
             * The original (old) value for detecting and reporting changes.
             */
            private Supplier<T> oldValueSupplier;

            /**
             * The original (old) value for detecting and reporting changes. Supplied by {@link #oldValueSupplier}.
             */
            private T oldValue;

            /**
             * If the old value has not been provided (so propagating of the old value is
             * not performed).
             */
            private boolean oldValueNotProvided = false;

            /**
             * The value where the value is being provided via a command field.
             */
            private EV<T> cmd;

            /**
             * See {@link #notNull()}.
             */
            private boolean notNull = false;

            /**
             * See {@link #path(String)}.
             */
            private String path;

            /**
             * See {@link #recorder(IUpdateRecorder...)}.
             */
            private List<IUpdateRecorder<T>> recorders = new ArrayList<> ();

            /**
             * See {@link #accessCheck(boolean)}.
             */
            private Function<T,Boolean> updateAllowed;

            /**
             * Coupled with {@link #updateAllowed} and is a message to passed while mapping
             * to a validation exception.
             */
            private String updateAllowedMessage;

            /**
             * Invoked after an update has occurred.
             */
            private PostUpdateHandler<T> postUpdate;

            /**
             * Construct with a setter only. An old value is not assumed so is not
             * propagated through.
             * 
             * @param setter
             *               the setter.
             */
            public Setter(ValueSetter<T> setter) {
                this.setter = setter;
                this.oldValueNotProvided = true;
            }

            /**
             * Register a handler that is invoked after an update has occurred.
             * 
             * @param postUpdate
             *                   the handler.
             * @return the setter.
             */
            public Setter<T> postUpdate(PostUpdateHandler<T> postUpdate) {
                this.postUpdate = postUpdate;
                return this;
            }

            /**
             * An exception handler (for exceptions arising from applying an update or
             * mapping a value that are not {@link ValidationException}'s or
             * {@link ProcessorException}'s).
             * <p>
             * This accepts an exception and generates from that exception one or more error
             * messages that are accumulated by the provided consumer of messages.
             * 
             * @param exception
             *                  the exception handler to convert to messages.
             * @return this setter instance.
             */
            public Setter<T> exception(BiConsumer<Exception, Consumer<Message>> exception) {
                this.exception = exception;
                return this;
            }

            /**
             * A validator to validate the value.
             * <p>
             * This is executed prior to performing the update.
             * 
             * @param validator
             *                  the validator.
             * @return this setter instance.
             */
            public Setter<T> validator(IValidator<T> validator) {
                this.validator = validator;
                return this;
            }

            /**
             * Provides the original value that is used for a comparison check and to
             * propagate to recorders.
             * 
             * @param oldValue
             *                 a supplier to supply the original value.
             * @return this setter instance.
             */
            public Setter<T> currentValue(Supplier<T> oldValue) {
                this.oldValueSupplier = oldValue;
                this.oldValueNotProvided = false;
                return this;
            }

            /**
             * Provides the original value that is used for a comparison check and to
             * propagate to recorders.
             * 
             * @param oldValue
             *                 the original value.
             * @return this setter instance.
             */
            public Setter<T> currentValue(T oldValue) {
                this.oldValue = oldValue;
                this.oldValueNotProvided = false;
                return this;
            }

            /**
             * Provides the value (if not provided then it is assumed that the value is
             * {@code null}). Note that this is ignored if a command value is set (see
             * {@link #cmd(V)}).
             * 
             * @param value
             *              the value to assign.
             * @return this setter instance.
             */
            public Setter<T> value(T value) {
                this.value = value;
                return this;
            }

            /**
             * See {@link #value(Object)} but allows for a different value type to be
             * provided by means of converting that value.
             * 
             * @param <Q>
             * @param value
             *                  the value to set (of the source value type).
             * @param converter
             *                  converts the source value to the target value.
             * @return this setter instance.
             */
            public <Q> Setter<T> value(Q value, Function<Q, T> converter) {
                this.value = converter.apply (value);
                return this;
            }

            /**
             * Assigns a command value to obtain the value being set. A check will also be
             * performed on the value to see if it has been set and if not no update is
             * performed.
             * 
             * @param cmd
             *            the command value.
             * @return this setter instance.
             * @see {@link #cmd(V, Function)}.
             */
            public Setter<T> cmd(V<T> cmd) {
                this.cmd = new EV<T> (cmd);
                return this;
            }

            /**
             * See {@link #cmd(V)} but allows for a command value of a different value type
             * to be used by provided a means to convert that value.
             * 
             * @param <Q>
             * @param cmd
             *                  the command value (for the source value).
             * @param converter
             *                  to convert the source to the target value.
             * @return this setter instance.
             */
            public <Q> Setter<T> cmd(V<Q> cmd, ValueMapper<Q, T> converter) {
                this.cmd = new EV<T> (null) {

                    @Override
                    public T getValue() throws Exception {
                        return converter.map (cmd.getValue ());
                    }

                    @Override
                    public boolean isSet() {
                        return cmd.isSet ();
                    }

                    @Override
                    public boolean isNull() {
                        return cmd.isNull ();
                    }

                };
                return this;
            }

            /**
             * Provided a check to only update if the value is not {@code null}.
             * 
             * @return this setter instance.
             */
            public Setter<T> notNull() {
                this.notNull = true;
                return this;
            }

            /**
             * Adds a recorder that is invoked when an update is performed. This is additive
             * so all recorders added will be invoked collectively.
             * <p>
             * This is {@code null}-value safe (i.e. one can pass a {@code null} recorder).
             * 
             * @param recorders
             *                  the recorders to add.
             * @return this setter instance.
             */
            public Setter<T> recorder(IUpdateRecorder<T> recorder) {
                if (recorder != null)
                    this.recorders.add (recorder);
                return this;
            }

            /**
             * This is really a convenience to allow one to externally impose a block on
             * update.
             * <p>
             * If the update attempts and is blocked then a {@link ValidationException} is
             * generated using the message passed. If that message is {@code null} then an
             * {@link AccessRightsProcessorException} is thrown. The latter case is more
             * significant in that an {@link ProcessorException.Error} of error type
             * {@link ProcessorException.ErrorType#ACCESS_RIGHTS} is associated (UI's
             * normally treat this as a top-level error condition). Note also that no direct
             * message is associated with this and the substitution
             * <code>{AccessRightsProcessorException}</code> is used (so if you are mapping
             * messages then this needs to be mapped).
             * 
             * @param updatedAllowed
             *                          {@code true} (the default) to allow updates.
             * @param validationMessage
             *                          (optional but see comments) validation message to
             *                          generate a {@link ValidationException} with.
             * @return this setter instance.
             */
            public Setter<T> accessCheck(boolean updateAllowed, String validationMessage) {
                this.updateAllowed = v -> updateAllowed;
                this.updateAllowedMessage = validationMessage;
                return this;
            }

            /**
             * Variant of {@link #accessCheck(boolean, String)} with no validation message
             * (so a {@link AccessRightsProcessorException} is thrown).
             */
            public Setter<T> accessCheck(boolean updateAllowed) {
                return accessCheck(updateAllowed, null);
            }

            /**
             * Variant of {@link #accessCheck(boolean, String)} but where the determination
             * is dependant on an update being made and the lambda-expression takes that
             * update value into consideration (this must return a {@code true} to
             * apply the update).
             */
            public Setter<T> accessCheck(Function<T,Boolean> updateAllowed, String validationMessage) {
                this.updateAllowed = updateAllowed;
                this.updateAllowedMessage = validationMessage;
                return this;
            }


            /**
             * Variant of {@link #accessCheck(Function, String)} with no validation message
             * (so a {@link AccessRightsProcessorException} is thrown).
             */
            public Setter<T> accessCheck(Function<T,Boolean> updateAllowed) {
                return accessCheck(updateAllowed, null);
            }

            /**
             * Assigns the error path for mapping validation errors.
             * 
             * @param error
             *              the error path.
             * @return this setter instance.
             */
            public Setter<T> path(String path) {
                this.path = path;
                return this;
            }

            /**
             * This is a convenience to call {@link #value(Object)} followed by
             * {@link #update()}.
             * 
             * @param value
             *              the value to update.
             * @return {@code true} if the update was successful.
             * @throws ProcessorException
             *                            if something fundamental went wrong (this
             *                            precludes validation errors which are accumulated
             *                            in the modification).
             */
            public boolean update(T value) throws ProcessorException {
                value (value);
                return update ();
            }

            /**
             * This is a convenience to call {@link #cmd(Object)} followed by
             * {@link #update()}.
             * 
             * @param cmd
             *            the command value to update from.
             * @return {@code true} if the update was successful.
             * @throws ProcessorException
             *                            if something fundamental went wrong (this
             *                            precludes validation errors which are accumulated
             *                            in the modification).
             */
            public boolean updateByCmd(V<T> cmd) throws ProcessorException {
                cmd (cmd);
                return update ();
            }
            
            /**
             * This is a convenience to call {@link #cmd(Object)} followed by
             * {@link #update()}.
             * 
             * @param cmd
             *                  the command value to update from.
             * @param converter
             *                  value converter from the command to the assignment value
             *                  type.
             * @return {@code true} if the update was successful.
             * @throws ProcessorException
             *                            if something fundamental went wrong (this
             *                            precludes validation errors which are accumulated
             *                            in the modification).
             */
            public <Q> boolean updateByCmd(V<Q> cmd, ValueMapper<Q,T> converter) throws ProcessorException {
                cmd (cmd, converter);
                return update ();
            }

            /**
             * Applies the update.
             * <p>
             * It is recommended not to call this directly but rather to use
             * {@link #update(Object)} or {@link #updateByCmd(V)}. The rationale being that
             * is forces you to execute an update (it is easy to forget to add a call to
             * {@link #update()} to the chain of assignments). However, this is a matter of
             * preference.
             * 
             * @return {@code true} if the update was successful.
             * @throws ProcessorException
             *                            if something fundamental went wrong (this
             *                            precludes validation errors which are accumulated
             *                            in the modification).
             */
            public boolean update() throws ProcessorException {
                try {
                    // Extract the value from the command if there is a command.
                    if (cmd != null) {
                        if (!cmd.isSet ())
                            return false;
                        if (notNull && cmd.isNull ())
                            return false;
                        value = cmd.getValue ();
                    }

                    // Resolve the old value.
                    if ((oldValueSupplier != null) && (oldValue == null))
                        oldValue = oldValueSupplier.get();
                     
                    // Check for sameness.
                    if (!oldValueNotProvided && ComparisonSupport.same (oldValue, value))
                        return false;

                    // Check for an update block.
                    if ((updateAllowed != null) && !updateAllowed.apply (value)) {
                        if (updateAllowedMessage == null)
                            throw new AccessRightsProcessorException (-1, path, null);
                        throw new ValidationException().add (new Message (updateAllowedMessage).path (path));
                    }

                    // Check for any pre-validator.
                    if (validator != null) {
                        if (!validator.validate (value, msg -> error (path, msg)))
                            return false;
                    }

                    // Apply the changes.
                    setter.apply (value);
                    Modification.this.modified = true;
                    onUpdate (value, oldValue, !oldValueNotProvided);
                    for (IUpdateRecorder<T> recorder : recorders) {
                        if (recorder != null)
                            recorder.record (value, oldValue, !oldValueNotProvided);
                    }

                    // Invoke any post-update.
                    if (postUpdate != null)
                        postUpdate.process (value);
                } catch (ProcessorException e) {
                    // Transferr over the processor errors so that we accumulate them.
                    e.forEach (err -> error (err));
                } catch (ValidationException e) {
                    // Extract out the errors from the validation and accumulate them.
                    error (path, e);
                    return false;
                } catch (Exception e) {
                    if (exception != null)
                        exception.accept (e, msg -> error (path, msg));
                    else
                        error (path, translateUnexpectedError (e));
                    return false;
                }

                // Having reached here we are successful.
                return true;
            }

            /**
             * Convenience to override as needed.
             * 
             * @param value
             *                         the value being set.
             * @param oldValue
             *                         the old value (if available).
             * @param oldValueProvided
             *                         {@code true} if the old value was available and has
             *                         been provided.
             */
            protected void onUpdate(T value, T oldValue, boolean oldValueProvided) {
                // Nothing.
            }

        }

        /**
         * Obtains a means to update a value, but with an old value to compare against.
         * 
         * @param <T>
         * @param setter
         *               the value setter.
         * @return the setter.
         */
        public <T> Setter<T> updater(ValueSetter<T> setter) {
            return new Setter<T> (setter);
        }

        /**
         * Obtains a means to update a value.
         * 
         * @param <T>
         * @param setter
         *                     the value setter.
         * @param currentValue
         *                     the current value (for reference).
         * @return the setter.
         */
        public <T> Setter<T> updater(ValueSetter<T> setter, T currentValue) {
            return new Setter<T> (setter).currentValue (currentValue);
        }

    }

}
