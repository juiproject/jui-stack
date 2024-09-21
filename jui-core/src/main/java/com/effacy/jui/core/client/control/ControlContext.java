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
package com.effacy.jui.core.client.control;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.effacy.jui.core.client.IDirtable;
import com.effacy.jui.core.client.observable.IListener;
import com.effacy.jui.core.client.observable.Observable;
import com.effacy.jui.core.client.util.Nullable;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.validation.model.IErrorMessage;
import com.effacy.jui.validation.model.IInvalidator;
import com.effacy.jui.validation.model.IValidatable;

/**
 * Manages a group of controls (generally within a component).
 *
 * @author Jeremy Buckley
 */
public class ControlContext extends Observable implements Iterable<IControl<?>>, IValidatable, IDirtable {

    /************************************************************************
     * Supporting classes
     ************************************************************************/

    /**
     * For visiting controls in the context.
     */
    @FunctionalInterface
    public interface IVisitor {

        /**
         * Visit a specific control.
         * 
         * @param depth
         *                the depth in the child hierarchy.
         * @param label
         *                the control label.
         * @param handler
         *                the control.
         */
        public void visit(int depth, ControlHandler<?, ?> handler);

    }

    /**
     * Version of {@link IVisitor} that also captures child context interactions.
     */
    public interface IEnhancedVisitor extends IVisitor {

        /**
         * Invoked when entering into a new context.
         * 
         * @param depth
         *                the current depth.
         * @param context
         *                the context being entered into.
         */
        public void enter(int depth, ControlContext context);
    }

    /**
     * Visitor used for debugging purposes.
     */
    public static class DebugVisitor implements IEnhancedVisitor {

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.control.ControlContext.IVisitor#visit(int,
         *      com.effacy.jui.core.client.control.ControlContext.IControlHandler)
         */
        @Override
        public void visit(int depth, ControlHandler<?, ?> handler) {
            String prefix = "  ";
            while (depth-- > 0) {
                prefix = "  " + prefix;
            }
            Logger.log (prefix + handler.control ().getName () + " (" + handler.control ().getClass ().getSimpleName () + ")");

        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.control.ControlContext.IEnhancedVisitor#enter(int,
         *      com.effacy.jui.core.client.control.ControlContext)
         */
        @Override
        public void enter(int depth, ControlContext context) {
            String prefix = "- ";
            while (depth-- > 0) {
                prefix = "  " + prefix;
            }
            Logger.log (prefix + "[context]");
        }

    }

    /**
     * Represents a state of the controls in the control manager and allows one to
     * restore that state.
     */
    public interface IControlManagerState {

        /**
         * Restores the state.
         */
        public void restore();
    }

    /**
     * Listens to changes in the context.
     */
    public interface IControlContextListener extends IListener {

        /**
         * Invoked when a handler is added.
         * 
         * @param handler
         *                the handler being added.
         */
        public void onAdd(ControlHandler<?, ? extends IControl<?>> handler);

        /**
         * Invoked when a handler is removed.
         * 
         * @param handler
         *                the handler being removed.
         */
        public void onRemove(ControlHandler<?, ? extends IControl<?>> handler);

        /**
         * Invoked when a child context control is modified.
         * 
         * @param control
         *                the control.
         */
        public void onModified(IControl<?> control);
    }

    /**
     * Annotate a control to map a message.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface MapMessage {

        /**
         * The path to map (dot separated and last component is the context).
         * 
         * @return the path.
         */
        public String path();

        /**
         * The error code (must match).
         * 
         * @return the error code.
         */
        public int code() default -1;

        /**
         * Message override (optional).
         * 
         * @return the message.
         */
        public String message() default "";

        /**
         * List of renderer references.
         */
        @Documented
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.FIELD)
        public @interface Messages {

            /**
             * The renderer references.
             * 
             * @return the override references.
             */
            public MapMessage[] value() default {};

        }
    }

    /************************************************************************
     * Debugging
     ************************************************************************/

    /**
     * Reference count for the control.
     */
    private static long REFERENCE_COUNT = 0;

    /**
     * Reference for the control.
     */
    final protected long uuid = REFERENCE_COUNT++;

    /**
     * A unique reference to the control group.
     * 
     * @return the reference.
     */
    public long getUUID() {
        return uuid;
    }

    /************************************************************************
     * Members
     ************************************************************************/

    /**
     * All the registered controls (if there are any).
     */
    private Map<String, ControlHandler<?, ? extends IControl<?>>> controls = new HashMap<> ();

    /**
     * All the registered controls (including any children).
     */
    private Collection<ControlHandler<?, ? extends IControl<?>>> handlersIncludingChildren = new ArrayList<> ();

    /**
     * Child contexts.
     */
    private List<ControlContext> children;

    /**
     * Listener for child contexts.
     */
    private IControlContextListener childListener = new IControlContextListener () {

        @Override
        public void onAdd(ControlHandler<?, ? extends IControl<?>> handler) {
            // Add to our list of handlers and propagate up.
            handlersIncludingChildren.add (handler);
            ControlContext.this.fireEvent (IControlContextListener.class).onAdd (handler);
        }

        @Override
        public void onRemove(ControlHandler<?, ? extends IControl<?>> handler) {
            // Remove from our list of handlers and propagate up.
            handlersIncludingChildren.remove (handler);
            ControlContext.this.fireEvent (IControlContextListener.class).onRemove (handler);
        }

        @Override
        public void onModified(IControl<?> control) {
            // Child context control has been modified so alert (as if were of
            // the context). The handler will propagate up.
            ControlContext.this.handleModified (control);
        }

    };

    /**
     * The restore status when present.
     */
    public IControlManagerState restoreStatus;

    /************************************************************************
     * Handler and control query methods (include child contexts)
     ************************************************************************/

    /**
     * The total number of controls under management.
     * 
     * @return the number.
     */
    public int size() {
        return handlersIncludingChildren.size ();
    }

    /**
     * Determines if the passed controls is managed.
     * 
     * @param control
     *                the control to test.
     * @return {@code true} if it is registered.
     */
    public boolean contains(IControl<?> control) {
        if (control == null)
            return false;
        for (ControlHandler<?, ? extends IControl<?>> handler : handlersIncludingChildren) {
            if (handler.control == control)
                return true;
        }
        return false;
    }

    /**
     * Gets the control based on the passed name.
     * 
     * @param <V>
     *             the control value.
     * @param <C>
     *             the control.
     * @param name
     *             the name of the control.
     * @return the control (or {@code null} if not found).
     */
    @SuppressWarnings("unchecked")
    public <V, CPT extends IControl<V>> CPT get(String name) {
        if (name == null)
            return null;
        for (ControlHandler<?, ? extends IControl<?>> handler : handlersIncludingChildren) {
            if ((handler.control != null) && name.equals (handler.control.getName ()))
                return (CPT) handler.control;
        }
        return null;
    }

    /**
     * Gets the value of the named control.
     * 
     * @param <V>
     *             the value type.
     * @param name
     *             the name of the control.
     * @return the value as a nullable.
     */
    public <V> Nullable<V> value(String name) {
        IControl<V> ctl = get (name);
        return (ctl == null) ? Nullable.unset () : Nullable.of (ctl.value ());
    }

    /**
     * Traverse all the controls in the context (including children).
     * 
     * @param visitor
     *                the visitor to record controls against (this is {@code null}
     *                safe).
     */
    public void traverse(IVisitor visitor) {
        if (visitor == null)
            return;
        traverse (0, visitor);
    }

    /**
     * Implements traversal that includes a record of the depth.
     * 
     * @param depth
     *                the current depth.
     * @param visitor
     *                the visitor to record controls against.
     */
    protected void traverse(int depth, IVisitor visitor) {
        // First traverse the controls in this current context (not in the
        // children).
        for (ControlHandler<?, ?> handler : controls.values ())
            visitor.visit (depth, handler);

        // Now process each child.
        if (children != null) {
            boolean enhanced = (visitor instanceof IEnhancedVisitor);
            for (ControlContext child : children) {
                if (enhanced)
                    ((IEnhancedVisitor) visitor).enter (depth, child);
                child.traverse (depth + 1, visitor);
            }
        }
    }

    /**
     * Adds a child context to this context. The children will be included in any
     * search over controls.
     * 
     * @param context
     *                the context to add.
     */
    public void add(ControlContext context) {
        if (context == null)
            return;

        // Add the child and extract all the handlers.
        if (children == null)
            children = new ArrayList<ControlContext> ();
        if (!children.contains (context)) {
            children.add (context);
            for (ControlHandler<?, ? extends IControl<?>> handler : handlersIncludingChildren)
                handlersIncludingChildren.add (handler);
            context.addListener (childListener);
        }
    }

    /**
     * Remove the child context.
     * 
     * @param context
     *                the context to remove.
     */
    public void remove(ControlContext context) {
        if ((context == null) || !children.contains (context))
            return;
        children.remove (context);
        context.removeListener (childListener);
        for (ControlHandler<?, ? extends IControl<?>> handler : handlersIncludingChildren)
            handlersIncludingChildren.remove (handler);
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Iterable#iterator()
     */
    @Override
    @SuppressWarnings("unchecked")
    public Iterator<IControl<?>> iterator() {
        return (Iterator<IControl<?>>) handlersIncludingChildren.stream ().map (h -> h.control ()).iterator ();
    }

    /**
     * Obtains all the controls subject to the given selector.
     * 
     * @param filter
     *               the filter for the controls.
     * @return the matching controls.
     */
    public List<IControl<?>> controls(Predicate<IControl<?>> filter) {
        if (filter == null)
            return handlersIncludingChildren.stream ().map (h -> h.control ()).collect (Collectors.toList ());
        return handlersIncludingChildren.stream ().filter (h -> filter.test (h.control ())).map (h -> h.control ()).collect (Collectors.toList ());
    }

    /**
     * Finds the first matching control.
     * 
     * @param filter
     *               the filter for controls.
     * @return the first matching control (which may not exist).
     */
    public Optional<IControl<?>> control(Predicate<IControl<?>> filter) {
        for (ControlHandler<?, ? extends IControl<?>> handler : handlersIncludingChildren) {
            if (filter.test (handler.control ()))
                return Optional.of (handler.control ());
        }
        return Optional.empty ();
    }

    /**
     * Obtains all the controls subject to the given selector.
     * 
     * @param selector
     *                 the selector.
     * @return the matching controls.
     */
    protected Collection<ControlHandler<?, ? extends IControl<?>>> handlers(Predicate<IControl<?>> selector) {
        if (selector == null)
            return handlersIncludingChildren;
        return handlersIncludingChildren.stream ().filter (handler -> selector.test (handler.control ())).collect (Collectors.toList ());
    }

    protected Optional<ControlHandler<?, ? extends IControl<?>>> handler(long uuid) {
        return handlersIncludingChildren.stream ().filter (handler -> handler.uuid () == uuid).findFirst ();
    }

    /************************************************************************
     * Control state management
     ************************************************************************/

    /**
     * Obtains a management state for all the controls.
     * 
     * @return the state.
     */
    public IControlManagerState state() {
        return state (null);
    }

    /**
     * Obtains a state of enablement for the controls under the management that pass
     * the filter.
     * 
     * @param filter
     *               filter to determine which controls should be included.
     * @return the state.
     */
    public IControlManagerState state(Predicate<IControl<?>> filter) {
        final Map<Long, Boolean> enableState = new HashMap<> ();
        for (ControlHandler<?, ? extends IControl<?>> handler : handlers (filter))
            enableState.put (handler.uuid (), !handler.control ().isDisabled ());
        return new IControlManagerState () {

            @Override
            public void restore() {
                handlersIncludingChildren.stream ().filter (h -> enableState.containsKey (h.uuid ())).forEach (h -> {
                    if (enableState.get (h.uuid ()))
                        h.control ().enable ();
                    else
                        h.control ().disable ();
                });
            }

        };
    }

    /**
     * Enables all (or the specified) controls.
     */
    public void enable() {
        enable (null);
    }

    /**
     * Enables the filters controls.
     * 
     * @param filter
     *               filter for the controls to enable.
     */
    public void enable(Predicate<IControl<?>> filter) {
        if (filter == null)
            handlersIncludingChildren.forEach (h -> h.control ().enable ());
        else
            handlersIncludingChildren.stream ().filter (h -> filter.test (h.control ())).forEach (h -> h.control ().enable ());
    }

    /**
     * Disables all (or the specified) controls.
     */
    public void disable() {
        disable (null);
    }

    /**
     * Disables the filters controls.
     * 
     * @param filter
     *               filter for the controls to enable.
     */
    public void disable(Predicate<IControl<?>> filter) {
        if (filter == null)
            handlersIncludingChildren.forEach (h -> h.control ().disable ());
        else
            handlersIncludingChildren.stream ().filter (h -> filter.test (h.control ())).forEach (h -> h.control ().disable ());
    }

    /**
     * See {@link IControl#waiting(boolean)}. Applies to all managed controls.
     * 
     * @param waiting
     *                {@code true} if in the waiting state.
     */
    public void waiting(boolean waiting) {
        handlersIncludingChildren.forEach (h -> h.control ().waiting (waiting));
    }

    /************************************************************************
     * Control error management
     ************************************************************************/

    /**
     * Resets the controls.
     */
    public void reset() {
        handlersIncludingChildren.forEach (h -> h.control ().reset ());
    }

    /**
     * Determines if there are dirty controls (optionally filtered under the named
     * collection).
     * 
     * @return {@code true} if any of the controls are dirty.
     */
    public boolean dirty() {
        return dirty (null);
    }

    /**
     * Determines if there are dirty controls (optionally filtered under the named
     * collection).
     * 
     * @return {@code true} if any of the controls are dirty.
     */
    public boolean dirty(Predicate<IControl<?>> filter) {
        if (filter == null)
            return handlersIncludingChildren.stream ().anyMatch (h -> h.control ().dirty ());
        return handlersIncludingChildren.stream ().filter (h -> filter.test (h.control ())).anyMatch (h -> h.control ().dirty ());
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.validation.model.IValidatable#valid()
     */
    @Override
    public boolean valid() {
        return !handlersIncludingChildren.stream ().anyMatch (h -> !h.control ().valid ());
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.validation.model.IValidatable#validate()
     */
    @Override
    public boolean validate() {
        return validate((Predicate<IControl<?>>) null);
    }

    /**
     * Validates only those controls that pass the test.
     * 
     * @param test
     *             the test to filter controls by.
     * @return {@code true} if valid.
     */
    public boolean validate(Predicate<IControl<?>> test) {
        boolean valid = true;
        for (ControlHandler<?, ?> handler : handlersIncludingChildren) {
            if ((test != null) && !test.test (handler.control ()))
                continue;
            if (!handler.control ().validate ())
                valid = false;
        }
        return valid;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.validation.model.IValidatable#validate(boolean)
     */
    @Override
    public boolean validate(boolean clearIfValid) {
        return validate (clearIfValid, null);
    }

    /**
     * Validates only those controls that pass the test.
     * 
     * @param clearIfValid
     *                     {@code true} to clear invalidation on a control if it is
     *                     now valid.
     * @param test
     *                     the test to filter controls by.
     * @return {@code true} if valid.
     */
    public boolean validate(boolean clearIfValid, Predicate<IControl<?>> test) {
        boolean valid = true;
        for (ControlHandler<?, ?> handler : handlersIncludingChildren) {
            if ((test != null) && !test.test (handler.control ()))
                continue;
            if (!handler.control ().validate (clearIfValid))
                valid = false;
        }
        return valid;
    }

    /**
     * See {@link IInvalidator#accept(List)}. Applies to each control under
     * management.
     * <p>
     * Recall that messages are removed from the list as they are accepted.
     * 
     * @param messages
     *                 the messages.
     */
    public void accept(IErrorMessage... messages) {
        List<IErrorMessage> listOfMessages = new ArrayList<> ();
        for (IErrorMessage message : messages) {
            if (message != null)
                listOfMessages.add (message);
        }
        accept (listOfMessages);
    }

    /**
     * See {@link IInvalidator#accept(List)}. Applies to each control under
     * management.
     * <p>
     * Recall that messages are removed from the list as they are accepted.
     * 
     * @param messages
     *                 the messages.
     */
    public void accept(List<? extends IErrorMessage> messages) {
        handlersIncludingChildren.forEach (h -> h.control ().invalidator ().accept (messages));
    }

    /**
     * Clears (see {@link IInvalidator#clear()}) all the controls.
     */
    public void clear() {
        handlersIncludingChildren.forEach (ctl -> ctl.control ().invalidator ().clear ());
    }

    /**
     * Clears out all the controls.
     */
    public void dispose() {
        handlersIncludingChildren.forEach (h -> h.dispose ());
        controls.clear ();
    }

    /**
     * Converts a path and name context to a lookup key.
     * 
     * @param path
     *             the path.
     * @param name
     *             the name.
     * @return the lookup key.
     */
    protected String compose(String path, String name) {
        if (StringSupport.empty (path))
            return (name == null) ? "" : name;
        if (name == null)
            return path;
        return compose (new String[] { path }, name);
    }

    /**
     * Converts a path and name context to a lookup key.
     * 
     * @param path
     *             the path.
     * @param name
     *             the name.
     * @return the lookup key.
     */
    protected String compose(String[] path, String name) {
        if ((path == null) || (path.length == 0))
            return StringSupport.trim (name);
        StringBuffer sb = new StringBuffer ();
        if (path != null) {
            for (String cpt : path) {
                if (cpt != null) {
                    sb.append (cpt);
                    sb.append ('.');
                }
            }
        }
        sb.append (StringSupport.trim (name));
        return sb.toString ();
    }

    /**
     * Removes the given control. This does not act on any child contexts.
     * 
     * @param control
     *                the control to remove.
     */
    public void remove(IControl<?> control) {
        if (control == null)
            return;
        handlersIncludingChildren.stream ().filter (h -> (h.control () == control)).findFirst ().ifPresent (h -> {
            handlersIncludingChildren.remove (h);
            h.dispose ();
            fireEvent (IControlContextListener.class).onRemove (h);
        });
    }

    /**
     * Handlers when a control contained in the context (or one of its children) is
     * modified.
     * 
     * @param control
     *                the control that was modified.
     */
    final protected void handleModified(IControl<?> control) {
        ControlContext.this.fireEvent (IControlContextListener.class).onModified (control);
        try {
            onModified (control);
        } catch (Throwable e) {
            Logger.reportUncaughtException (e);
        }
    }

    /**
     * Invoked when one of the controls is modified.
     * 
     * @param control
     *                the modified control.
     */
    protected void onModified(IControl<?> control) {
        // Nothing.
    }

    /**
     * Registers a control. This makes it available to the context (and any contexts
     * that include this as a child).
     * 
     * @param control
     *                the control to register.
     * @return the handler for the control.
     */
    public <V, CTRL extends IControl<V>> ControlHandler<V, CTRL> register(CTRL control) {
        return register (new ControlHandler<V, CTRL> (control));
    }

    /**
     * Registers a control handler. Allows for external creation of a handler (for
     * example, by instantiation of {@link ControlHandler}.
     * 
     * @param handler
     *                the handler to register.
     * @return the passed handler.
     */
    public <V, CTRL extends IControl<V>> ControlHandler<V, CTRL> register(final ControlHandler<V, CTRL> handler) {
        handler.assignListener (new IModifiedListener () {

            @Override
            public void onModified(IControl<?> control) {
                ControlContext.this.handleModified (handler.control ());
            }
        });
        controls.put (handler.control ().getName (), handler);
        handlersIncludingChildren.add (handler);
        fireEvent (IControlContextListener.class).onAdd (handler);
        return handler;
    }

    /**
     * A standard implementation of {@link IControlHandler}.
     */
    public static class ControlHandler<V, CTRL extends IControl<V>> {

        private long uuid = REFERENCE_COUNT++;

        private CTRL control;

        protected IModifiedListener listener = null;

        public ControlHandler(CTRL control) {
            this.control = control;
        }

        public long uuid() {
            return uuid;
        }

        public void assignListener(IModifiedListener listener) {
            if (this.listener != null)
                control.removeListener (this.listener);
            if (listener != null)
                control.addListener (listener);
            this.listener = listener;
        }

        public CTRL control() {
            return control;
        }

        /**
         * Disposes of the control handler and clears the control.
         */
        public void dispose() {
            if (control != null)
                control.removeListener (listener);
            listener = null;
            control = null;
        }

        /**
         * Convenience to add a modified listener.
         * 
         * @param modified
         *                 the modfied handler.
         * @return this handler.
         */
        public ControlHandler<V, CTRL> onModified(Consumer<CTRL> modified) {
            if (modified == null)
                return this;
            control ().addListener (new IModifiedListener () {

                @Override
                @SuppressWarnings("unchecked")
                public void onModified(IControl<?> control) {
                    modified.accept ((CTRL) control);
                }

            });
            return this;
        }
    }

}
