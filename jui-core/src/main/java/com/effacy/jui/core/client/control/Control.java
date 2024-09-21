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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.effacy.jui.core.client.Debug;
import com.effacy.jui.core.client.component.Component;
import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.dom.IUIEventHandler;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.dom.jquery.JQuery;
import com.effacy.jui.core.client.dom.renderer.IDataRenderer;
import com.effacy.jui.core.client.util.UID;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.validation.model.IErrorMessage;
import com.effacy.jui.validation.model.IErrorMessageAcceptor;
import com.effacy.jui.validation.model.IInvalidator;
import com.effacy.jui.validation.model.IValidator;
import com.effacy.jui.validation.model.IValueValidator;
import com.effacy.jui.validation.model.acceptor.CompositeAcceptor;
import com.effacy.jui.validation.model.acceptor.PathAcceptor;
import com.effacy.jui.validation.model.validator.CompositeValidator;

import elemental2.dom.Element;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.HTMLSelectElement;
import elemental2.dom.HTMLTextAreaElement;

public abstract class Control<V, C extends Control.Config<V,C>> extends Component<C> implements IControl<V> {

    /**
     * To action changes in a controls value.
     */
    @FunctionalInterface
    public interface IControlModifiedHandler<V> {

        /**
         * Invoked on a change in the control.
         * 
         * @param ctl
         *                   the control instance.
         * @param value
         *                   the current value.
         * @param priorValue
         *                   the prior value (prior to the modification).
         */
        public void modified(IControl<V> ctl, V value, V priorValue);
    }

    /**
     * To listen to invalidation changes.
     */
    @FunctionalInterface
    public interface IInvalidationHandler {
    
        /**
         * Invoked when there is a change of invalidation state.
         * 
         * @param invalid
         *                 {@code true} if the control is invalid.
         * @param messages
         *                 list of error messages (only defined if the control is
         *                 invalid).
         */
        public void invalidation(boolean invalid, List<String> messages);
    }

    /**
     * Common configuration for controls.
     * <p>
     * Note that the first generic is for the value type of the control and the
     * second is for the configuration type. The latter is used to type the return
     * type of builder methods so that they can be chained when employed for
     * sub-classes.
     */
    public static class Config<V, C extends Config<V,C>> extends Component.Config {

        /**
         * See {@link #modifiedHandler(IControlModifiedHandler)}.
         */
        protected IControlModifiedHandler<V> modifiedHandler;

        /**
         * See {@link #invalidationHandler(IInvalidationHandler)}.
         */
        protected IInvalidationHandler invalidationHandler;

        /**
         * See {@link #name(String)}.
         */
        protected String name = UID.createUID ();

        /**
         * See {@link #suppressDirty(boolean)}.
         */
        protected boolean suppressDirty;

        /**
         * See {@link #testId(String)}.
         */
        protected String testId;

        /**
         * See {@link #width(Length)}.
         */
        protected Length width;

        /**
         * See {@link #padding(Insets)}.
         */
        protected Insets padding;

        /**
         * See {@link #readOnly(boolean)}.
         */
        protected boolean readOnly;

        /**
         * See {@link #validator(IValidator)} and
         * {@link #validator(String, IValueValidator)}.
         */
        protected CompositeValidator<V> validators;

        /**
         * See {@link #acceptor(IErrorMessageAcceptor)}.
         */
        protected CompositeAcceptor acceptors = new CompositeAcceptor ();

        /**
         * See {@link #validateOnModified(Supplier)}.
         */
        protected Supplier<Boolean> validateOnModified;

        /**
         * Associated the control with a name.
         * 
         * @param name
         *             the name.
         * @return this configuration instance.
         */
        @SuppressWarnings("unchecked")
        public C name(String name) {
            this.name = name;
            if ((this.testId == null) && (name != null))
                this.testId = name.toLowerCase().replace(' ', '_');
            return (C) this;
        }

        /**
         * Getter for {@link #name(String)}.
         */
        public String getName() {
            return name;
        }

        /**
         * Assigns a handler to be called when the value has changed.
         * <p>
         * The handler is passed the current value and the last value prior to the
         * modification event.
         * 
         * @param modifiedHandler
         *                        the handler.
         * @return this configuration instance.
         */
        @SuppressWarnings("unchecked")
        public C modifiedHandler(IControlModifiedHandler<V> modifiedHandler) {
            this.modifiedHandler = modifiedHandler;
            return (C) this;
        }

        /**
         * Assigns a handler to be called when the control changes validation state.
         * 
         * @param invalidationHandler
         *                            the handler.
         * @return this configuration instance.
         */
        @SuppressWarnings("unchecked")
        public C invalidationHandler(IInvalidationHandler invalidationHandler) {
            this.invalidationHandler = invalidationHandler;
            return (C) this;
        }

        /**
         * Indicates that the control should never return {@code true} from a call to
         * {@link IControl#isDirty()}.
         * 
         * @param suppressDirty
         *                      {@code true} if to suppress dirty checks.
         * @return this configuration instance.
         */
        @SuppressWarnings("unchecked")
        public C suppressDirty(boolean suppressDirty) {
            this.suppressDirty = suppressDirty;
            return (C) this;
        }

        /**
         * Getter for {@link #suppressDirty(boolean)}.
         */
        public boolean isSuppressDirty() {
            return suppressDirty;
        }

        /**
         * Assigns a test ID to apply to the control elements in the control. This is
         * implementation specific and may take the form of a prefix. Its intended to be
         * used by test automation to locate and activate specific controls.
         * 
         * @param testId
         *               the test ID (prefix).
         * @return this configuration instance.
         */
        @SuppressWarnings("unchecked")
        public C testId(String testId) {
            this.testId = testId;
            return (C) this;
        }

        /**
         * Getter for {@link #testId(String)}.
         */
        public String getTestId() {
            return testId;
        }

        /**
         * Assigns a strict width to the control. This is applied to the component root
         * element (which means some layouts could override this).
         * 
         * @param width
         *              the width ({@code null} for auto).
         * @return this configuration instance.
         */
        @SuppressWarnings("unchecked")
        public C width(Length width) {
            this.width = width;
            return (C) this;
        }

        /**
         * Getter for {@link #width(Length)}.
         */
        public Length getWidth() {
            return width;
        }

        /**
         * Provides padding around the control. This is applied to the component root
         * element.
         * 
         * @param padding
         *                the padding.
         * @return this configuration instance.
         */
        @SuppressWarnings("unchecked")
        public C padding(Insets padding) {
            this.padding = padding;
            return (C) this;
        }

        /**
         * Getter for {@link #padding(Insets)}.
         */
        public Insets getPadding() {
            return padding;
        }

        /**
         * Marks the control as being read only. This is a convenience rather than
         * waiting to call {@link IControl#readOnly(boolean)}.
         * 
         * @param readOnly
         *                 the readOnly.
         * @return this configuration instance.
         */
        @SuppressWarnings("unchecked")
        public C readOnly() {
            this.readOnly = true;
            return (C) this;
        }

        /**
         * Getter for {@link #readOnly(boolean)}.
         */
        public boolean isReadOnly() {
            return readOnly;
        }

        /**
         * Adds a validator to the control.
         * 
         * @param validator
         *                  the validator.
         * @return this configuration instance.
         */
        @SafeVarargs
        @SuppressWarnings("unchecked")
        public final C validator(IValidator<V>... validators) {
            if (validators != null) {
                for (IValidator<V> validator: validators) {
                    if (validator == null)
                        continue;
                    if (this.validators == null)
                        this.validators = new CompositeValidator<V> ();
                    this.validators.add ((IValidator<V>) validator);
                }
            }
            return (C) this;
        }

        /**
         * Adds a invalidation message acceptor (see {@link IErrorMessageAcceptor}).
         * 
         * @param acceptor
         *                 the acceptor to add.
         * @return this configuration instance.
         */
        @SuppressWarnings("unchecked")
        public C acceptor(IErrorMessageAcceptor acceptor) {
            acceptors.add (acceptor);
            return (C) this;
        }

        /**
         * Adds a invalidation message acceptor (see {@link PathAcceptor}).
         * 
         * @param paths
         *              the paths to match.
         * @return this configuration instance.
         */
        @SuppressWarnings("unchecked")
        public C acceptor(String... paths) {
            for (String path : paths) {
                if (!StringSupport.empty (path))
                    acceptor (new PathAcceptor (path));
            }
            return (C) this;
        }

        /**
         * Provide a means to determine if validation should be performed on
         * modification.
         * <p>
         * Validation will clear when valid.
         * 
         * @param validateOnModified
         *                           the mechanism.
         * @return this configuration instance.
         */
        @SuppressWarnings("unchecked")
        public C validateOnModified(Supplier<Boolean> validateOnModified) {
            this.validateOnModified = validateOnModified;
            return (C) this;
        }

        /**
         * Automatically validates on modification. See
         * {@link #validateOnModified(Supplier)}.
         * 
         * @return this configuration instance.
         */
        @SuppressWarnings("unchecked")
        public C validateOnModified() {
            this.validateOnModified = () -> true;
            return (C) this;
        }

    }

    /**
     * Construct a control with configuration.
     * 
     * @param config
     *               the configuration.
     */
    protected Control(C config) {
        super (config);
        resetValue = prepareValueForAssignment (null);
        currentValue = resetValue;
        this.readOnly = config.readOnly;
    }

    /**
     * Construct around a renderer. If the renderer implements
     * {@link IUIEventHandler} it will be registered as a handler.
     * 
     * @param config
     *                 the component configuration.
     * @param renderer
     *                 the renderer to use.
     */
    public Control(C config, IDataRenderer<C> renderer) {
        super (config, renderer);
    }

    /************************************************************************
     * Value management.
     ************************************************************************/

    /**
     * The current value of the control.
     */
    private V currentValue = null;

    /**
     * The value to reset to when canceling an edit or reseting the control.
     */
    private V resetValue = null;

    /**
     * The last modified value.
     */
    private V lastModifiedValue = null;

    /**
     * Read-only state.
     */
    private boolean readOnly;

    /**
     * The control is suspended (for some reason).
     */
    private boolean suspended;

    /**
     * Invalidator for the control.
     */
    private IInvalidator invalidator = new IInvalidator () {

        /**
         * Invalidity state.
         */
        private boolean invalid;

        /**
         * If validation has been blocked.
         */
        private boolean blocked;

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.validation.model.IValidatable#valid()
         */
        @Override
        public boolean valid() {
            if (canBeValidated () && (config ().validators != null))
                return config ().validators.validate (value (), msg -> {});
            return true;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.validation.model.IValidatable#validate(boolean)
         */
        @Override
        public boolean validate(boolean clearIfValid) {
            if (clearIfValid)
                clear ();
            if (canBeValidated () && (config ().validators != null)) {
                List<String> messages = new ArrayList<> ();
                if (!config ().validators.validate (value (), msg -> messages.add (msg.getMessage ()))) {
                    invalidate (messages);
                    return false;
                }
            }
            return true;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.validation.model.IInvalidator#isInvalid()
         */
        @Override
        public boolean isInvalid() {
            return invalid;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.validation.model.IInvalidator#enable(boolean)
         */
        @Override
        public void enable(boolean enable) {
            this.blocked = !enable;
        }

        /**
         * Invalidation messages.
         */
        private List<String> messages;

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.validation.model.IInvalidator#invalidate(java.util.List)
         */
        @Override
        public void invalidate(List<String> messages) {
            if (blocked)
                return;
            invalid = true;
            if (isRendered ())
                markInvalid (invalid);
            this.messages = messages;
            fireEvent (IInvalidListener.class).onInvalidated (Control.this, messages);
            if (config ().invalidationHandler != null)
                config ().invalidationHandler.invalidation (true, messages);
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.validation.model.IInvalidator#accept(java.util.List)
         */
        @Override
        public boolean accept(List<? extends IErrorMessage> messages) {
            if (config ().acceptors.isEmpty ()) {
                // We try to use the name.
                if (!StringSupport.empty (config ().name))
                    config ().acceptor (new PathAcceptor (config ().name));
            }
            List<IErrorMessage> accepted = messages.stream ().filter (msg -> config ().acceptors.accept (msg)).collect (Collectors.toList ());
            if (!accepted.isEmpty ()) {
                invalidate (accepted.stream ().map (v -> v.getMessage ()).collect (Collectors.toList ()));
                messages.removeAll (accepted);
                return true;
            }
            return false;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.validation.model.IInvalidator#clear()
         */
        @Override
        public void clear() {
            // We only need to apply this when this control is currently
            // invalid.
            if (invalid) {
                invalid = false;
                markInvalid (false);
                fireEvent (IInvalidListener.class).onClearInvalidated (Control.this);
                if (config ().invalidationHandler != null)
                    config ().invalidationHandler.invalidation (false, null);
            }
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.validation.model.IInvalidator#messages()
         */
        @Override
        public List<String> messages() {
            if (!invalid || (messages == null))
                return new ArrayList<>();
            return messages;
        }
    };

    /**
     * Indicates that the control is in a state to be validated.
     * <p>
     * The default ensures that the control is enabled, not read only, not hidden
     * and not suspended.
     * 
     * @return {@code true} if so.
     */
    protected boolean canBeValidated() {
        return isEnabled () && !isReadOnly () && !isHidden () && !isSuspended();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.control.IControlValue#getValue()
     */
    @Override
    public V value() {
        syncValueFromSource ();
        return prepareValueForRetrieval (clone (currentValue));
    }

    /**
     * {@inheritDoc}
     * <p>
     * This delegates to {@link #empty(Object)} for specific behaviour (the sole
     * exception being where the current value is {@code null} which is
     * automatically determined as being empty).
     *
     * @see com.effacy.jui.core.client.control.IControl#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        if (currentValue == null)
            return true;
        return empty (currentValue);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.control.IControl#setValue(java.lang.Object)
     */
    @Override
    public final void setValue(Value<V> value) {
        if (value == null)
            value = Value.of (null);
        waiting (false);
        onBeforeSetValue ();
        V val = value.value ();
        if (value.isMerge ())
            val = merge (currentValue, val);
        assignCurrentValue (val, !value.isDirty ());
        syncValueToSource (!value.isQuiet (), value.isForce ());
        // When quiet we don't fire an event so the last modified value does not get
        // updated. This needs to be updated so the next time we fire an event we are
        // correctly determining a change in value.
        if (value.isQuiet())
            lastModifiedValue = val;
        onAfterSetValue ();
    }

    /**
     * Invoked by
     * {@link #setValue(com.effacy.jui.core.client.control.IControl.Value)} when the
     * value type is marked as merge.
     * <p>
     * The default is to pass through the value to merge as the updated value.
     * Controls that respect merge need to override this method to provide a
     * suitable implementation.
     * 
     * @param existing
     *                 the existing value.
     * @param merge
     *                 the value to merge.
     * @return the result of the merge.
     */
    protected V merge(V existing, V merge) {
        return merge;
    }

    /**
     * Central point for assigning the current value.
     * 
     * @param value
     *              the current value to assign.
     * @param reset
     *              if the reset value should also be set.
     */
    protected void assignCurrentValue(V value, boolean reset) {
        this.currentValue = prepareValueForAssignment (cloneForCurrentValue (value));
        if (reset)
            this.resetValue = cloneForResetValue (currentValue);
    }

    /**
     * Retrieves the current value (no cloning).
     * 
     * @return the current value.
     */
    protected V retrieveCurrentValue() {
        return this.currentValue;
    }

    /**
     * Retrieves the reset value (no cloning).
     * 
     * @return the reset value.
     */
    protected V retrieveResetValue() {
        return this.resetValue;
    }

    /************************************************************************
     * Behaviour overrides.
     * <p>
     * These methods define specific control behaviours in relation to the value
     * type. The defaults cater for the more common cases however exotic controls
     * may require more specifity.
     ************************************************************************/

    /**
     * Filters the value prior to returning from {@link #setValue(Object)}.
     * 
     * @param value
     *              the value to filter.
     * @return the filtered value.
     */
    protected V prepareValueForAssignment(V value) {
        return value;
    }

    /**
     * Filters the value prior to returning from {@link #getValue()}.
     * 
     * @param value
     *              the value to filter.
     * @return the filtered value.
     */
    protected V prepareValueForRetrieval(V value) {
        return value;
    }

    /**
     * Clone the value before passing to {@link #valueToSource(Object)}. The default
     * is to clone by {@link #clone(Object)} however if cloning is not required then
     * override this and return the passed value.
     * 
     * @param value
     *              the value to clone.
     * @return the cloned value.
     */
    protected V cloneForValueToSource(V value) {
        if (value == null)
            return null;
        return clone (value);
    }

    /**
     * Clones for assignment to the current value. Default is to clone via a call to
     * {@link #clone(Object)}.
     * 
     * @param value
     *              the value to clone.
     * @return the cloned value.
     */
    protected V cloneForCurrentValue(V value) {
        if (value == null)
            return null;
        return clone (value);
    }

    /**
     * Clones for assignment to the reset value. Default is to clone via a call to
     * {@link #clone(Object)}.
     * 
     * @param value
     *              the value to clone.
     * @return the cloned value.
     */
    protected V cloneForResetValue(V value) {
        if (value == null)
            return null;
        return clone (value);
    }

    /**
     * Clones the value.
     * 
     * @param value
     *              the value to clone.
     * @return the cloned value.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected V clone(V value) {
        if (value == null)
            return null;
        if (value instanceof List)
            return (V) new ArrayList ((List<?>) value);
        if (value instanceof Map)
            return (V) new HashMap ((Map<?, ?>) value);
        return value;
    }

    /**
     * Determines if the two values are the same or not. By default this checks for
     * value equality then object equality (i.e. with the
     * {@link Object#equals(Object)} method). In the case the objects are
     * collections then simply tests that the collections are of the same size and
     * the elements in v1 are contained in v2 (assumes they are not null).
     * 
     * @param v1
     *           the first value.
     * @param v2
     *           the second value.
     */
    protected boolean equals(V v1, V v2) {
        if (v1 == v2)
            return true;
        if ((v2 == null) || (v1 == null))
            return false;
        if (v1 instanceof Collection) {
            if (((Collection<?>) v1).size () != ((Collection<?>) v2).size ())
                return false;
            for (Object value : (Collection<?>) v1)
                if (!((Collection<?>) v2).contains (value))
                    return false;
            return true;
        }
        return v1.equals (v2);
    }

    /**
     * Determines if a non-{@code null} type value should be considered empty.
     * 
     * @param value
     *              the value to test (will not be {@code null}).
     * @return {@code true} if it should be considered empty.
     */
    protected boolean empty(V value) {
        if (value == null)
            return true;
        if (currentValue instanceof String)
            return ((String) currentValue).trim ().isEmpty ();
        if (currentValue instanceof List)
            return ((List<?>) currentValue).isEmpty ();
        return false;
    }

    /************************************************************************
     * State management.
     ************************************************************************/

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.control.IControlValue#isDirty()
     */
    @Override
    public boolean dirty() {
        if (config ().suppressDirty)
            return false;
        return !equals (resetValue, currentValue);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.IResetable#reset()
     */
    @Override
    public void reset() {
        onBeforeReset ();
        invalidator ().clear ();
        if (dirty ()) {
            assignCurrentValue (resetValue, false);
            syncValueToSource (true, false);
            onAfterReset ();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.control.IControl#getName()
     */
    @Override
    public String getName() {
        return config ().name;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.control.IControl#readOnly(boolean)
     */
    @Override
    public void readOnly(boolean readOnly) {
        if (readOnly != this.readOnly) {
            this.readOnly = readOnly;
            if (isRendered ())
                markReadOnly (readOnly);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.control.IControl#isReadOnly()
     */
    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.control.IControl#suspend(boolean)
     */
    @Override
    public void suspend(boolean suspended) {
        this.suspended = suspended;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.control.IControl#isSuspended()
     */
    @Override
    public boolean isSuspended() {
        return suspended;
    }
    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.control.IControl#waiting(boolean)
     */
    @Override
    public void waiting(boolean waiting) {
        markWaiting (waiting);
    }

    /************************************************************************
     * Events and callbacks.
     ************************************************************************/

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.control.IControl#invalidator()
     */
    @Override
    public IInvalidator invalidator() {
        return invalidator;
    }

    /**
     * Called when the control is being reset.
     */
    protected void onBeforeReset() {
        // Nothing.
    }

    /**
     * Called when the control has been reset and was dirty (so a change was made to
     * the value).
     */
    protected void onAfterReset() {
        // Nothing.
    }

    /**
     * Called when there is a synchronization of values (only when the field value
     * and the source value differ).
     * 
     * @param toSource
     *                    {@code true} if the value was assigned from the field to
     *                    the value source, otherwise the value of the field was
     *                    updated from the source.
     * @param sourceValue
     *                    the value that the source had prior to synchronization.
     * @param fieldValue
     *                    the value that this field had prior to synchronization.
     */
    protected void onSynchronize(boolean toSource, V sourceValue, V fieldValue) {
        // Nothing.
    }

    /**
     * Called when the form field is modified. Fires a modified event to all
     * listeners.
     */
    protected void onModified() {
        // Nothing.
    }

    /**
     * Invoked by the value set methods prior to the value actually being assigned
     * to the control.
     */
    protected void onBeforeSetValue() {
        // Nothing.
    }

    /**
     * Invoked by the value set methods after the value having been assigned to the
     * control.
     */
    protected void onAfterSetValue() {
        // Nothing.
    }

    /**
     * See {@link #fireModified(boolean)} but un-forced.
     */
    protected void fireModified() {
        fireModified (false);
    }

    /**
     * Calls {@link #onModified()} if the current value is different from the last
     * time this method was called.
     * 
     * @param force
     *              {@code true} to force the modified event even if the values are
     *              the same.
     */
    protected void fireModified(boolean force) {
        if (force || !equals (lastModifiedValue, currentValue)) {
            // Update the last modified but don't need to clone as the
            // currentValue is isolated.
            V lastModifiedValueTmp = lastModifiedValue;
            lastModifiedValue = currentValue;
            onModified ();
            fireEvent (IModifiedListener.class).onModified (this);
            if ((config ().validateOnModified != null) && config ().validateOnModified.get ())
                validate (true);
            if (config ().modifiedHandler != null)
                config ().modifiedHandler.modified (this, currentValue, lastModifiedValueTmp);
        }
    }

    /**
     * Call to synchronise the value from the underlying source of values to this
     * field. If the value from the control is different from the current value, a
     * modified event will be fired.
     * <p>
     * Sub-classes must call this when they detect that the source of the field
     * value has changed. This will allow the field to update its internal value
     * based on that of the source.
     */
    protected void syncValueFromSource() {
        if (isRendered ()) {
            V sourceValue = valueFromSource ();
            if (!equals (sourceValue, currentValue)) {
                V fieldValue = currentValue;
                assignCurrentValue (sourceValue, false);
                onSynchronize (false, sourceValue, fieldValue);
                fireModified ();
            }
        }
    }

    /**
     * Call to synchronise the fields current value to the control.
     * <p>
     * Note that if the control has not been modified then nothing will be done
     * (including the firing of any events). The exception to this is when
     * {@code force} is being used and then an event will always be fired if
     * {@link fireModified} is {@code true} even if the control has not been
     * rendered.
     * 
     * @param fireModified
     *                     {@code true} if the method should invoke a modification
     *                     event if the current value is different from the source
     *                     value (and last modification event).
     * @param force
     *                     if the synchronization should be forced (even if the
     *                     value from the source equals that currently set).
     */
    protected void syncValueToSource(boolean fireModified, boolean force) {
        if (isRendered ()) {
            V sourceValue = valueFromSource ();
            if (force || !equals (sourceValue, currentValue)) {
                valueToSource (cloneForValueToSource (currentValue));
                onSynchronize (true, sourceValue, currentValue);
                if (fireModified)
                    fireModified (force);
            }
        } else if (fireModified && force)
            fireModified (true);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.core.client.component.EventBoxComponent#onAfterRender()
     */
    @Override
    protected void onAfterRender() {
        super.onAfterRender ();
        syncValueToSource (false, true);
        if (isHidden ())
            hide ();
        if (invalidator.isInvalid ())
            markInvalid (true);
        if (readOnly)
            markReadOnly (true);
        if (preRenderWaiting)
            markWaiting (true);
        if (config ().width != null)
            CSS.WIDTH.apply (getRoot (), config ().width);
        if (config ().padding != null)
            config ().padding.padding (getRoot ());
    }

    /**
     * Convenience to invoke when a change needs to be propagated.
     */
    protected void modified() {
        syncValueFromSource ();
    }

    /**
     * Convenience to disable (enable) all input elements in the control.
     * 
     * @param disable
     *                {@code true} to disable (otherwise will enable);
     */
    protected void disableInputElements(boolean disable) {
        for (Element el : JQuery.$ (getRoot ()).find ("input").toArray())
            ((HTMLInputElement) el).disabled = disable;
        for (Element el : JQuery.$ (getRoot ()).find ("textarea").toArray())
            ((HTMLTextAreaElement) el).disabled = disable;
        for (Element el : JQuery.$ (getRoot ()).find ("select").toArray())
            ((HTMLSelectElement) el).disabled = disable;
    }

    /*******************************************************************************
     * Methods requiring implementation.
     *******************************************************************************/

    /**
     * Gets the value from the underlying source (which could be HTML or something
     * else). This will only ever be called after the field has been rendered.
     * <p>
     * This will be used as a basis of comparison and subsequent assignment.
     * Assignments are performed via {@link #assignCurrentValue(Object, boolean)}
     * which will create a clone of the value by calling {@link #clone(Object)}. By
     * default cloning is minimal so should you pass an object that will be modified
     * in the future by the source you should either return a copy here or implement
     * {@link #clone(Object)} to correctly copy the object (which is the preferred
     * approach).
     * 
     * @return the value extracted from the (UI) source.
     */
    protected abstract V valueFromSource();

    /**
     * Assigns the value to the underlying source (which could be HTML or something
     * else). This will only ever be called after the field has been rendered.
     * <p>
     * The value passed will not be cloned in anyway so if the source is likely to
     * modify it in the future then the implementation should perform a suitable
     * clone (i.e. implement {@link #clone(Object)} and call that).
     * 
     * @param value
     *              the value to assign.
     */
    protected abstract void valueToSource(V value);

    /*******************************************************************************
     * Management of visual state.
     * <p>
     * These may be overridden for finer control over visual state (rather than the
     * defaults which rely on control styles).
     *******************************************************************************/

    /**
     * Implements marking the control as being invalid (in error).
     * <p>
     * The default behaviour is to add (remove) the {@link IControlCSS#invalid()}
     * style.
     * 
     * @param invalid
     *                {@code true} to mark otherwise clear.
     */
    protected void markInvalid(boolean invalid) {
        if (invalid) {
            getRoot ().classList.add (styles ().invalid ());
            if (Debug.isTestMode ())
                getRoot ().setAttribute("test-state", "invalid");
        } else {
            getRoot ().classList.remove (styles ().invalid ());
            if (Debug.isTestMode ())
                getRoot ().setAttribute("test-state", "");
        }
    }

    /**
     * Implements marking the control as being read-only.
     * 
     * @param readOnly
     *                 {@code true} to mark otherwise clear.
     */
    protected void markReadOnly(boolean readOnly) {
        if (!isRendered ()) {
            config ().readOnly ();
            return;
        }
        if (readOnly) {
            getRoot ().classList.add (styles ().read_only ());
            if (Debug.isTestMode ())
                getRoot ().setAttribute("test-state", "read_only");
        } else {
            getRoot ().classList.remove (styles ().read_only ());
            if (Debug.isTestMode ())
                getRoot ().setAttribute("test-state", "");
        }
        disableInputElements (readOnly);
    }

    /**
     * Pre-render assignment of {@link #waiting(boolean)}.
     */
    private boolean preRenderWaiting;

    /**
     * Implements marking the control as being in a wait state (i.e. initial data is
     * pending).
     * 
     * @param waiting
     *                {@code true} to mark otherwise clear.
     */
    protected void markWaiting(boolean waiting) {
        if (!isRendered()) {
            preRenderWaiting = waiting;
            return;
        }
        if (waiting) {
            getRoot ().classList.add (styles ().waiting ());
            if (Debug.isTestMode ())
                getRoot ().setAttribute("test-state", "waiting");
        } else {
            getRoot ().classList.remove (styles ().waiting ());
            if (Debug.isTestMode ()) {
                if (readOnly)
                    getRoot ().setAttribute("test-state", "read_only");
                else if (isDisabled())
                    getRoot ().setAttribute("test-state", "disabled");
                else
                    getRoot ().setAttribute("test-state", "");
            }
        }
    }

    /*******************************************************************************
     * Component behaviour overrides.
     * <p>
     * Changes in behaviour from that of the default component.
     *******************************************************************************/

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#implEnable()
     */
    @Override
    protected void implEnable() {
        super.implEnable();

        // Enable all controls.
        disableInputElements (false);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#implDisable()
     */
    @Override
    protected void implDisable() {
        super.implDisable();

        // Enable all controls.
        disableInputElements (true);

        // Blur the control.
        blur ();
    }

    /************************************************************************
     * Expected styles.
     ************************************************************************/

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#styles()
     */
    @Override
    protected IControlCSS styles() {
        return (IControlCSS) super.styles ();
    }

    public static interface IControlCSS extends IComponentCSS {

        /**
         * Default component CSS source location for use with defining styles. This is
         * just a convenience.
         */
        public final static String CONTROL_CSS = "com/effacy/jui/core/client/control/Control.css";

        /**
         * Invalid state.
         */
        public String invalid();

        /**
         * For the read only state.
         */
        public String read_only();

        /**
         * For the waiting state.
         */
        public String waiting();

    }
}
