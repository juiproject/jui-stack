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
package com.effacy.jui.core.client.component;

import java.util.ArrayList;
import java.util.List;

import com.effacy.jui.core.client.dom.DomSupport;
import com.effacy.jui.core.client.dom.IUIEventHandler;
import com.effacy.jui.core.client.dom.UIEvent;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.util.BrowserInfo;
import com.effacy.jui.platform.util.client.TimerSupport;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import jsinterop.base.Js;

/**
 * Used to manage blur and focus beyond single elements and convey that
 * behaviour onto more complex structures (i.e. components).
 *
 * @author Jeremy Buckley
 */
public abstract class BlurAndFocusBehaviour implements IUIEventHandler {

    /**
     * See {@link #generateFocusEl()}.
     */
    protected Element pseudoFocusEl;

    /**
     * Collection of element that focus and blur events have been attached to and
     * who control the focal state of the component.
     */
    protected List<Element> focalElements = new ArrayList<> ();

    /**
     * See {@link #setFocusDeferred(boolean)}.
     */
    protected boolean focusDeferred = true;

    /**
     * Indicates that deferred blurring should be employed (this handles cases where
     * a component may have more that one element that can take focus and prevents
     * blurring of the component when there is simply a transfer of focus).
     */
    protected boolean blurDeferred = false;

    /**
     * Used to manage non-reentrance to {@link #_focus()} and {@link #focus()}.
     */
    protected boolean focusScheduled;

    /**
     * Used to prevent re-entrance to the focus flow.
     */
    protected boolean focusRunning;

    /**
     * Used to manage non-reentrance to {@link #_focus()} and {@link #focus()}.
     */
    protected boolean blurScheduled;

    /**
     * Used to prevent re-entrance to the focus flow.
     */
    protected boolean blurRunning;

    /**
     * Flag indicating if focus is being held.
     */
    protected boolean hasFocus;

    /*******************************************************************************
     * Primary methods for focus and blur management.
     *******************************************************************************/

    /**
     * Indicates that deferred blurring should be employed (this handles cases where
     * a component may have more that one element that can take focus and prevents
     * blurring of the component when there is simply a transfer of focus).
     * 
     * @param blurDeferred
     *                     {@code true} to employ deferred blurring (default is
     *                     {@code false}).
     * @return this instance.
     */
    public BlurAndFocusBehaviour blurDeferred(boolean blurDeferred) {
        this.blurDeferred = blurDeferred;
        return this;
    }

    /**
     * When calling {@link #focus()} the default behaviour is to defer a subsequent
     * call to {@link #renderFocus()} to allow events to complete. In some cases
     * they may not be desirable. To stop this behaviour and apply focus immediately
     * call this method passing {@code false} so that deferral will not occur.
     * 
     * @param focusDeferred
     *                      {@code true} if deferral of focus should be applied
     *                      (which is the default).
     * @return this instance.
     */
    public BlurAndFocusBehaviour focusDeferred(boolean focusDeferred) {
        this.focusDeferred = focusDeferred;
        return this;
    }

    /**
     * Brings into focus.
     * <p>
     * This ultimately invokes {@link #_focus()} but will be deferred (see
     * {@link TimerSupport#defer(Runnable)}) if {@link #setFocusDeferred(boolean)}
     * is set.
     * <p>
     * Note that no {@link Component#onFocus()} events are fired purposefully and if
     * re-entrant safe (i.e. it keeps track or re-entrance so it only runs once).
     * 
     * @see IComponent#focus()
     */
    public void focus() {
        focus (focusDeferred, false);
    }

    /**
     * Takes out of focus.
     * <p>
     * This implements the blur mechanism ultimately calling {@link #renderBlur()}
     * to perform the relevant UI updates. It also manages the the change in
     * internal focus state.
     * <p>
     * As with {@link #_focus()} this implements re-entrance protection.
     * 
     * @see IComponent#blur()
     */
    public void blur() {
        blur (blurDeferred, false);
    }

    /**
     * Determines if focus is being held.
     * 
     * @return {@code true} if it is.
     */
    public boolean isHasFocus() {
        return hasFocus;
    }

    /**
     * When the underlying component is enabled we need to enable the focus.
     */
    public void enable() {
        if (getFocusEl () != null)
            DomSupport.enable (getFocusEl());
            //DomSupport.setProperty (getFocusEl (), "disabled", false);
    }

    /**
     * When the underlying component is disabled we need to disable the focus.
     */
    public void disable() {
        if (getFocusEl () != null)
            DomSupport.disable (getFocusEl());
            //DomSupport.setProperty (getFocusEl (), "disabled", true);
    }

    /**
     * Adds an element for focus events.
     * <p>
     * This will serve both to receive focus actions and to respond to focus events.
     * 
     * @param el
     *           the focus element.
     */
    public void manageFocusEl(Element el) {
        if (el == null)
            return;
        if (focalElements.contains (el))
            return;
        focalElements.add (el);
        UIEventType.ONFOCUS.attach (el);
        UIEventType.ONBLUR.attach (el);
    }

    /*******************************************************************************
     * Methods to be (or could be) overridden.
     *******************************************************************************/

    /**
     * Called by {@link #_focus()} to apply focus to the UI elements.
     * <p>
     * The default behaviour is to apply focus to the element returned by
     * {@link #getFocusEl()}. In this instance {@link #safeFocus(Element, int)} is
     * employed to apply focus progressively against the element until focus is
     * formally gained (and if not then the component is blurred).
     */
    protected void renderFocus() {
        safeFocus (getFocusEl (), 100);
    }

    /**
     * Called by {@link #blur()} to apply focus to the UI elements.
     * <p>
     * The default behaviour is to apply blur to the element returned by
     * {@link #getFocusEl()}.
     * <p>
     * Note that it is possible that UI change events will be fired as a result of
     * the application.
     */
    protected void renderBlur() {
        Element focusEl = getFocusEl ();
        if (focusEl != null)
            focusEl.blur ();
    }

    /**
     * Called by {@link #safeBlur(Element, int)} if focus cannot be achieved and to
     * deliberately enforce a blur state.
     * <p>
     * The default is to call {@link #blur()} but it may be desirable to go through
     * a different pathway.
     */
    protected void requestBlur() {
        blur ();
    }

    /**
     * Fires a blur event.
     * <p>
     * This is a convenience only.
     */
    protected void onBlur() {
        // Nothing.
    }

    /**
     * Fires a focus event.
     * <p>
     * This is a convenience only.
     */
    protected void onFocus() {
        // Nothing.
    }

    /*******************************************************************************
     * Focus and blur behviours.
     *******************************************************************************/

    /**
     * See {@link #focus()}.
     * <p>
     * Allows for the specification of it being deferred and whether to generate an
     * event.
     * 
     * @param deferred
     *                  {@code true} if to be deferred.
     * @param fireEvent
     *                  {@code true} to fire an event.
     */
    protected void focus(boolean deferred, boolean fireEvent) {
        cancelDeferredBlur ();
        if (focusScheduled || hasFocus)
            return;
        focusScheduled = true;
        if (!deferred) {
            _focus ();
            if (fireEvent)
                onFocus ();
        } else {
            // Defer in this context only defers to the end of the UI event loop. It can
            // still be cancelled by setting focusScheduled to fals.
            TimerSupport.defer (() -> {
                if (focusScheduled) {
                    _focus ();
                    if (fireEvent)
                        onFocus ();
                }
            });
        }
    }

    /**
     * Called only by {@link #focus()} to implement focus (the former implements
     * deferral). It will change the internal focus state and implements the actual
     * rending by invoking {@link #renderFocus()}.
     */
    private void _focus() {
        // Re-entrance check.
        if (focusRunning)
            return;
        try {
            focusRunning = true;
            hasFocus = true;
            renderFocus ();
        } catch (Throwable e) {
            // Just in case the element is hidden or not
            // visible.
        } finally {
            focusScheduled = false;
            focusRunning = false;
        }
    }

    /**
     * Applies focus to the given element in a robust manner (sometimes an element
     * may be directed to gain focus but may not be in a position to do so, such as
     * not being visible or in the process of a CSS transition). That means that it
     * first tries to apply focus then checks if focus has been gained (see
     * {@link #hasFocus(Element)}. If not then it delays to then end of the browser
     * UI loop and tries again. Failing that a strict delay (see the {@code delay}
     * parameter) is applied and a final attempt performed. If after all this focus
     * has still not been gained then the owner component is blurred which ensures
     * that we don't have a "dangling" focus (i.e. a component that is marked as
     * having focus but contains no element that does have focus thus cannot blur
     * naturally).
     * 
     * @param focusEl
     *                the element to apply focus to.
     * @param delay
     *                the final delay to apply if focus still not gained after
     *                initial attempts.
     */
    protected void safeFocus(Element focusEl, int delay) {
        if (focusEl != null) {
            focusEl.focus ();
            // Some weird stuff can go on that may prevent the element from
            // gaining focus. So we check that focus has actually been gained
            // and if not start deferring.
            if (!hasFocus (focusEl)) {
                TimerSupport.defer (() -> {
                    focusEl.focus ();
                    // Try once more but with a longer delay.
                    if (!hasFocus (focusEl)) {
                        TimerSupport.timer (() -> {
                            focusEl.focus ();
                            // Still no luck then blur the owner so we don't end
                            // up with focus without actually having focus.
                            if (!hasFocus (focusEl))
                                requestBlur ();
                        }).schedule (delay);
                    }
                });
            }
        }
    }

    /**
     * Cancels any focus that is in flight.
     */
    protected void cancelDeferredFocus() {
        focusScheduled = false;
    }

    /**
     * See {@link #blur()}.
     * <p>
     * Allows for the specification of it being deferred and whether to generate an
     * event.
     * 
     * @param deferred
     *                  {@code true} if to be deferred.
     * @param fireEvent
     *                  {@code true} to fire an event.
     */
    protected void blur(boolean deferred, boolean fireEvent) {
        cancelDeferredFocus ();
        if (blurScheduled || !hasFocus)
            return;
        blurScheduled = true;
        if (!deferred) {
            _blur ();
            if (fireEvent)
                onBlur ();
        } else {
            TimerSupport.defer (() -> {
                if (blurScheduled) {
                    _blur ();
                    if (fireEvent)
                        onBlur ();
                }
            });
        }
    }

    /**
     * Called only by {@link #focus()} to implement focus (the former implements
     * deferral). It will change the internal focus state and implements the actual
     * rending by invoking {@link #renderFocus()}.
     */
    private void _blur() {
        // Re-entrance check.
        if (blurRunning)
            return;
        try {
            blurRunning = true;
            hasFocus = false;
            renderBlur ();
        } catch (Throwable e) {
            // Just in case the element is hidden or not
            // visible.
        } finally {
            blurScheduled = false;
            blurRunning = false;
        }
    }

    /**
     * Blocks any invocation of a deferred blur.
     */
    protected void cancelDeferredBlur() {
        blurScheduled = false;
    }

    /*******************************************************************************
     * Focus elements and their management.
     *******************************************************************************/

    /**
     * Obtains the (first) element to receive focus.
     * <p>
     * This will return the {@link #generateFocusEl()} element (if used) otherwise
     * the first managed focus element (if neither of these then {@code null}). For
     * a custom focus element then override.
     * 
     * @return the element.
     */
    protected Element getFocusEl() {
        if (pseudoFocusEl != null)
            return pseudoFocusEl;
        if (!focalElements.isEmpty ())
            return focalElements.get (0);
        return null;
    }

    /**
     * Determines if the passed element is one of the managed focus elements.
     * 
     * @param el
     *           the element to test.
     * @return {@code true} if it is.
     */
    protected boolean isManagedFocusEl(Element el) {
        if (el == null)
            return false;

        return focalElements.contains (el);
    }

    /**
     * Determines if the passed element currently has focus. Does so by testing
     * against the document's active element.
     * 
     * @param el
     *           the element to test.
     * @return {@code true} if it does.
     */
    protected boolean hasFocus(Element el) {
        if (DomGlobal.document.activeElement == null)
            return false;
        if (el == null)
            return false;
        return (DomGlobal.document.activeElement == el);
    }

    /**
     * Generates a pseudo focus element for the component (where the component may
     * not have focus elements itself).
     * <p>
     * This is not current wired up but is being kept for possible future inclusion
     * (since there are quite a few settings being applied we want to retain that
     * knowledge).
     */
    protected Element generateFocusEl(Element rootEl) {
        if (rootEl == null)
            return null;
        if (pseudoFocusEl == null) {
            if (BrowserInfo.isWebKit) {
                pseudoFocusEl = DomSupport.createInput ();
                ((HTMLElement) pseudoFocusEl).style.set ("opacity", "0.0");
                ((HTMLElement) pseudoFocusEl).style.set ("zIndex", "-1");
                ((HTMLElement) pseudoFocusEl).style.set ("overflow", "hidden");
                ((HTMLElement) pseudoFocusEl).style.set ("position", "absolute");
                ((HTMLElement) pseudoFocusEl).style.set ("height", "0px");
                ((HTMLElement) pseudoFocusEl).style.set ("width", "0px");
                ((HTMLElement) pseudoFocusEl).style.set ("borderWith", "0px");
                ((HTMLElement) pseudoFocusEl).style.set ("left", "0px");
                ((HTMLElement) pseudoFocusEl).style.set ("right", "0px");
                ((HTMLInputElement) pseudoFocusEl).tabIndex = -1;
                rootEl.appendChild (pseudoFocusEl);
            } else
                pseudoFocusEl = rootEl;
        }
        UIEventType.ONFOCUS.attach (pseudoFocusEl);
        UIEventType.ONBLUR.attach (pseudoFocusEl);
        return pseudoFocusEl;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.dom.IUIEventHandler#handleEvent(com.effacy.jui.core.client.dom.UIEvent)
     */
    @Override
    public boolean handleEvent(UIEvent event) {
        // Handle a managed focus element gaining focus.
        if (event.isEvent (UIEventType.ONFOCUS) && isManagedFocusEl (Js.cast (event.getTarget ()))) {
            if (blurScheduled) {
                // If blur is scheduled then likely we are just in transition
                // between focus elements. So we just cancel the blur and leave
                // it in the current state.
                cancelDeferredBlur ();
            } else {
                focus (focusDeferred, true);
            }
            event.stopEvent ();
            return true;
        }

        // Handle a managed focus element loosing focus.
        if (event.isEvent (UIEventType.ONBLUR) && isManagedFocusEl (Js.cast (event.getTarget ()))) {
            // We will defer blur if asked or when there are more than one focus
            // element under management (to enable transition between elements
            // without changing component focus).
            blur ((blurDeferred || (focalElements.size () > 1)), true);
            event.stopEvent ();
            return true;
        }
        return false;
    }

}
