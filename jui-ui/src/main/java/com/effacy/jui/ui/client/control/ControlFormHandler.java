package com.effacy.jui.ui.client.control;

import java.util.ArrayList;
import java.util.List;

import com.effacy.jui.core.client.control.IControl;
import com.effacy.jui.core.client.control.IControl.Value;
import com.effacy.jui.core.client.control.IInvalidListener;
import com.effacy.jui.core.client.control.IModifiedListener;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.validation.model.IErrorMessage;

/**
 * Convenience to manage a collection of control for ad-hoc forms.
 */
public class ControlFormHandler<SRC,DST> {

    /**
     * Invoked when a registered control's value is modified.
     */
    @FunctionalInterface
    public interface IModifiedHandler {

        /**
         * @param control
         *                the control whose value changed.
         * @param value
         *                the (new) current value of the control.
         */
        public void onModified(IControl<?> control);
    }

    /**
     * Invoked when a registered control is invalidated (i.e. fails validation) or
     * has its invalidation cleared (i.e. becomes valid again).
     */
    @FunctionalInterface
    public interface IInvalidationHandler {

        /**
         * Invoked when a registered control is invalidated (i.e. fails validation).
         * 
         * @param control
         *                  the control that was invalidated.
         * @param reference
         *                  the reference of the control (as registered with this
         *                  handler).
         * @param clear
         *                  {@code true} if the invalidation is being cleared (i.e. the
         *                  control is now valid), otherwise {@code false} if the
         *                  control has just become invalid.
         */
        public void onInvalidation(IControl<?> control, String reference, boolean clear);
    }

    /**
     * Use to obtain a value from a source to assign to a control.
     */
    @FunctionalInterface
    public interface IGetter<W,SRC> {

        /**
         * Gets the value from the passed source (to apply to the related control).
         * 
         * @param ctl
         *               the control.
         * @param source
         *               the source to get from.
         * @return the value retrieved from the source to apply to the control.
         */
        public W get(IControl<W> ctl, SRC source);
    }

    /**
     * Used to set a value (from a control) to a destination.
     */
    @FunctionalInterface
    public interface ISetter<W,DST> {

        public void set(ISetterContext context, W value, DST destination);

        /**
         * Provides a context to support the setting of values on a destination.
         */
        public interface ISetterContext {

            /**
             * Determines if the underlying control was dirty.
             * 
             * @return {@code true} if it was.
             */
            public boolean dirty();
        }
    }

    class ControlHandler<W> {

        IControl<W> control;

        IGetter<W, SRC> getter;

        ISetter<W, DST> setter;

        Boolean disabled;

        String reference;

        IModifiedListener modifiedListener = IModifiedListener.create (c -> {
            for (IModifiedHandler modifiedHandler : modifiedHandlers)
                modifiedHandler.onModified(control);
        });

        IInvalidListener invalidListener = IInvalidListener.create((ctl,msg) -> {
            for (IInvalidationHandler invalidationHandler : invalidationHandlers)
                invalidationHandler.onInvalidation(control, reference, false);
        }, ctl -> {
            for (IInvalidationHandler invalidationHandler : invalidationHandlers)
                invalidationHandler.onInvalidation(control, reference, true);
        });

        ControlHandler(IControl<W> control, String reference, IGetter<W, SRC> getter, ISetter<W, DST> setter) {
            this.control = control;
            this.reference = reference;
            this.getter = getter;
            this.setter = setter;
            control.addListener (modifiedListener);
            control.addListener(invalidListener);
        }

        /**
         * Detaches from the control.
         */
        void detach() {
            control.removeListener (modifiedListener);
            control.removeListener(invalidListener);
            control = null;
            getter = null;
            setter = null;
        }

        boolean dirty() {
            return (control != null) && control.dirty();
        }

        boolean validate() {
            if (control == null)
                return true;
            if (control.isHidden() || control.isReadOnly() || control.isDisabled ())
                return true;
            return control.validate();
        }

        void wait(boolean waiting) {
            if (control != null)
                control.waiting(waiting);
        }

        void clearInvalid() {
            if (control != null)
                control.invalidator().clear();
        }

        void clear() {
            if (control != null) {
                control.invalidator().clear();
                control.setValue (Value.of (null));
            }
        }

        void reset() {
            if (control != null)
                control.reset();
        }

        void edit(SRC source) {
            if ((control != null) && (getter != null)) {
                control.waiting(false);
                control.setValue (Value.of (getter.get (control, source)).force());
            }
        }

        void retrieve(DST destination) {
            if ((this.control == null) || (setter == null))
                return;
            if (this.control.isHidden() || this.control.isReadOnly() || this.control.isDisabled ())
                return;
            if (!setterApplyWhenNotDirty && !this.control.dirty())
                return;
            setter.set (new ISetter.ISetterContext () {
                public boolean dirty() {
                    return control.dirty ();
                }
            }, this.control.value (), destination);
        }

        void accept(List<? extends IErrorMessage> errors) {
            if (control != null)
                control.invalidator().accept(errors);
        }

        void disable(boolean disabled) {
            if (control != null) {
                if (disabled) {
                    this.disabled = control.isDisabled();
                    control.disable();
                } else if (this.disabled == null) {
                    control.enable();
                } else {
                    if (!this.disabled)
                        control.enable();
                    this.disabled = null;
                }
            }
        }

    }

    private boolean setterApplyWhenNotDirty;

    private List<ControlHandler<?>> controls = new ArrayList<>();

    private List<IModifiedHandler> modifiedHandlers = new ArrayList<>();

    private List<IInvalidationHandler> invalidationHandlers = new ArrayList<>();

    /**
     * Clears all the registered controls and detaches from them.
     */
    public void detach() {
        controls.forEach(ControlHandler::detach);
        controls.clear();
    }

    public ControlFormHandler<SRC,DST> setterApplyWhenNotDirty() {
        return setterApplyWhenNotDirty(true);
    }

    public ControlFormHandler<SRC,DST> setterApplyWhenNotDirty(boolean setterApplyWhenNotDirty) {
        this.setterApplyWhenNotDirty = setterApplyWhenNotDirty;
        return this;
    }

    public <W,T extends IControl<W>> T register(T control, IGetter<W, SRC> getter, ISetter<W, DST> setter) {
        return register(control, control.getName(), getter, setter);
    }

    public <W,T extends IControl<W>> T register(T control, String reference, IGetter<W, SRC> getter, ISetter<W, DST> setter) {
        if (control == null)
            return null;
        ControlHandler<W> handler = new ControlHandler<W> (control, reference, getter, setter);
        controls.add (handler);
        return control;
    }

    /**
     * Registers a handler that fires whenever <em>any</em> registered control is
     * modified. Useful for cross-cutting reactions such as refreshing a form-wide
     * summary or enabling a save button as soon as any field becomes dirty.
     *
     * @param handler
     *                the handler to invoke on any modification.
     * @return this instance.
     */
    public ControlFormHandler<SRC,DST> handleModified(IModifiedHandler handler) {
        if (handler != null)
            modifiedHandlers.add (handler);
        return this;
    }

    /**
     * Registers a handler that fires whenever <em>any</em> registered control is
     * invalidated (i.e. fails validation) or has its invalidation cleared (i.e.
     * becomes valid again). Useful for cross-cutting reactions such as displaying a
     * form-wide error summary.
     * 
     * @param handler
     *                the handler to invoke on any invalidation or invalidation
     *                clear.
     * @return this instance.
     */
    public ControlFormHandler<SRC,DST> handleInvalidation(IInvalidationHandler handler) {
        if (handler != null)
            invalidationHandlers.add (handler);
        return this;
    }

    public void wait(boolean waiting) {
        controls.forEach(control -> control.wait(waiting));
    }

    public SRC edit(SRC source) {
        controls.forEach(control -> control.edit(source));
        return source;
    }

    public DST retrieve(DST destination) {
        controls.forEach(control -> control.retrieve(destination));
        return destination;
    }

    public void reset() {
        controls.forEach(control -> control.reset());
    }

    public void clearInvalid() {
        controls.forEach(control -> control.clearInvalid());
    }

    public void clear() {
        controls.forEach(control -> control.clear());
    }

    public void disable(boolean disabled) {
        controls.forEach(control -> control.disable(disabled));
    }

    /**
     * Validates all registered controls and returns {@code true} if they are all
     * valid, or {@code false} if any control is invalid.
     * 
     * @return {@code true} if all controls are valid, {@code false} otherwise.
     */
    public boolean validate() {
        boolean value = true;
        for (ControlHandler<?> control : controls) {
            if (!control.validate())
                value = false;
        }
        return value;
    }

    public boolean dirty() {
        for (ControlHandler<?> control : controls) {
            if (control.dirty())
                return true;
        }
        return false;
    }

    public List<IErrorMessage> invalidate(List<? extends IErrorMessage> errors) {
        if (errors == null)
            errors = new ArrayList<> ();
        List<IErrorMessage> remainingErrors = new ArrayList<> (errors);
        controls.forEach(control -> control.accept(remainingErrors));
        return remainingErrors;
    }

}