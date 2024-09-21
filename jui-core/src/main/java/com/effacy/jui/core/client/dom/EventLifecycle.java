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
package com.effacy.jui.core.client.dom;

import java.util.ArrayList;
import java.util.List;

import com.effacy.jui.platform.util.client.Logger;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.Event;
import elemental2.dom.EventListener;
import elemental2.dom.EventTarget.AddEventListenerOptionsUnionType;
import jsinterop.annotations.JsFunction;

/**
 * Tapping into (and managing) events and event lifecycles.
 *
 * @author Jeremy Buckley
 */
public class EventLifecycle {

    /*******************************************************************************
     * Event loop management.
     *******************************************************************************/

    public interface IEventListener {

        public void processBrowserEvent(Event e);

    }

    /*******************************************************************************
     * Event pre-viewing.
     *******************************************************************************/

    /**
     * All registered previewers.
     */
    private static List<EventPreviewHandler> PREVIEW_HANDLERS = new ArrayList<>();

    /**
     * Registers a previewer of browser events. These will receive notification of a
     * class of browser events (see {@link UIEventType}) prior to dispatch to the
     * component.
     * 
     * @param preview
     *                the previewer.
     * @return a handler that allows for removal.
     */
    public static IEventPreviewHandler registerPreview(IEventPreview preview) {
        return new EventPreviewHandler (preview);
    }

    /**
     * Internal. To process a preview of events.
     * 
     * @param e
     *          the event to preview.
     * @return the outcome (the first non-{@link IEventPreview.Outcome#CONTINUE}).
     */
    private static void processPreview(Event e) {
        _processPreview (e);
    }

    /**
     * Called by {@link #processPreview(Event)} to wrap the call in an
     * <code>$entry</code>.
     * <p>
     * Until we have completely decoupled GWT we need to wrap calls to processing
     * events using GWT's {@code $entry} (see
     * {@link com.google.gwt.core.client.impl.Impl#entry(com.google.gwt.core.client.JavaScriptObject)})
     * for proper handling of the scheduler (the deferred commands). This acts a bit
     * like an aspect.
     * 
     * @param e
     *          the event to process.
     */
    protected static native void _processPreview(Event e) /*-{
        $entry(@com.effacy.jui.core.client.dom.EventLifecycle::__processPreview(*))(e);
    }-*/;

    /**
     * Called by {@link #_processPreview(Event)} to implement the intent behind
     * {@link #processPreview(Event)}.
     */
    private static void __processPreview(Event e) {
        IEventPreview.Outcome outcome = null;
        for (IEventPreview preview : PREVIEW_HANDLERS) {
            outcome = preview.previewEvent (e);
            if ((outcome != null) && (outcome != IEventPreview.Outcome.CONTINUE))
                break;
        }
        if ((outcome != null) && (outcome != IEventPreview.Outcome.CONTINUE))
            e.stopPropagation ();
    }

    /**
     * Envelopes a mechanism that can preview browser events.
     */
    @FunctionalInterface
    public interface IEventPreview {

        /**
         * Enumerates the various outcomes of previewing an event.
         */
        public enum Outcome {

            /**
             * Continue processing the event.
             */
            CONTINUE,
            
            /**
             * Cancel processing the event.
             */
            CANCEL;
        }

        /**
         * Previews an event with the ability to return an outcome that dictates further
         * processing.
         * 
         * @param e
         *          the event to preview.
         * @return how to continue processing (a {@code null} value will be equivalant
         *         to {@link Outcome#CONTINUE}).
         */
        public Outcome previewEvent(Event e);
    }

    /**
     * Handler to manage the registration of a previewer.
     */
    public interface IEventPreviewHandler {

        /**
         * Removes the handler so that it will no longer receive preview events.
         */
        public void remove();
    }

    /**
     * Internal. Implementation of {@link IEventPreviewHandler} that includes a
     * delegate to an underlying preview.
     */
    private static class EventPreviewHandler implements IEventPreviewHandler, IEventPreview {

        /**
         * The delegate.
         */
        private IEventPreview preview;

        /**
         * Creates with a delegate. If the delegate is non-{@code null} then the
         * instance will be added to the list of preview handlers
         * ({@link EventLifecycle#PREVIEW_HANDLERS}).
         * 
         * @param preview
         *                the preview to delegate to.
         */
        EventPreviewHandler(IEventPreview preview) {
            this.preview = preview;
            if (this.preview != null)
                PREVIEW_HANDLERS.add (this);
        }

        @Override
        public Outcome previewEvent(Event e) {
            return this.preview.previewEvent (e);
        }

        @Override
        public void remove() {
                PREVIEW_HANDLERS.remove (this);
        }
        
    }

    /*******************************************************************************
     * Top-level event handlers.
     *******************************************************************************/

    public interface IEventRegistration {
        public void remove();
    }

    static class EventRegistration implements IEventRegistration {

        private EventListener listener;

        private List<EventRegistration> registration;

        public EventRegistration(EventListener listener, List<EventRegistration> registration) {
            this.listener = listener;
            this.registration = registration;
            if (registration != null)
                registration.add (this);
        }

        @Override
        public void remove() {
            if (registration != null) {
                registration.remove (this);
                listener = null;
                registration = null;
            }

        }

        public void handleEvent(Event evt) {
            if (listener != null)
                listener.handleEvent (evt);
        }

    }

    /**
     * See {@link #registerWindowResizeEvent(EventListener)}.
     */
    private static final List<EventRegistration> EVENTS_RESIZE = new ArrayList<> ();

    /**
     * Registers a listener for window resize events.
     * 
     * @param listener
     *                 the listener.
     */
    public static IEventRegistration registerWindowResizeEvent(EventListener listener) {
        if (listener == null)
            return new EventRegistration (null, null);
        if (EVENTS_RESIZE.isEmpty ()) {
            DomGlobal.window.addEventListener ("resize", e -> {
                EVENTS_RESIZE.forEach (l -> {
                    try {
                        l.handleEvent (e);
                    } catch (Throwable ex) {
                        Logger.reportUncaughtException (ex);
                    }
                });
            });
        }
        return new EventRegistration (listener, EVENTS_RESIZE);
    }

    /**
     * See {@link #registerWindowResizeEvent(EventListener)}.
     */
    private static final List<EventRegistration> EVENTS_SCROLL = new ArrayList<> ();

    /**
     * Registers a listener for window scroll events.
     * 
     * @param listener
     *                 the listener.
     */
    public static IEventRegistration registerDocumentScrollEvent(EventListener listener) {
        if (listener == null)
            return new EventRegistration (null, null);
        if (EVENTS_SCROLL.isEmpty ()) {
            DomGlobal.document.addEventListener ("scroll", e -> {
                EVENTS_SCROLL.forEach (l -> {
                    try {
                        l.handleEvent (e);
                    } catch (Throwable ex) {
                        Logger.reportUncaughtException (ex);
                    }
                });
            });
        }
        return new EventRegistration (listener, EVENTS_SCROLL);
    }

    /**
     * See {@link #registerWindowCloseEvent(EventListener)}.
     */
    private static final List<EventRegistration> EVENTS_WINDOW_CLOSE = new ArrayList<> ();

    /**
     * Registers a listener for window scroll events.
     * 
     * @param listener
     *                 the listener.
     */
    public static IEventRegistration registerWindowCloseEvent(EventListener listener) {
        if (listener == null)
            return new EventRegistration (null, null);
        if (EVENTS_WINDOW_CLOSE.isEmpty ()) {
            DomGlobal.window.addEventListener ("close", e -> {
                EVENTS_WINDOW_CLOSE.forEach (l -> {
                    try {
                        l.handleEvent (e);
                    } catch (Throwable ex) {
                        Logger.reportUncaughtException (ex);
                    }
                });
            });
        }
        return new EventRegistration (listener, EVENTS_WINDOW_CLOSE);
    }

    /**
     * Registers the passed event listener against the passed element which will
     * receive browser events.
     * 
     * @param el
     *                 the element to register.
     * @param listener
     *                 the listener to associate.
     */
    public static void register(Element el, IEventListener listener)  {
        _register (el, listener);
    }

    /**
     * Called by {@link #register(Element, IEventListener)}.
     */
    public static native void _register(Element el, IEventListener listener) /*-{
        el.__juiEventListener = listener;
    }-*/;

    /**
     * Degregisters any event listener associated with the passed element.
     * 
     * @param el
     *           the element.
     */
    public static void deregister(Element el) {
        _deregister (el);
    }

    /**
     * Called by {@link #deregister(Element)}.
     */
    public static native void _deregister(Element el) /*-{
        el.__juiEventListener = null;
    }-*/;

    /**
     * Internal. Dispatch an event to previewers and the most relevant event listener.
     * 
     * @param event
     *              the event to dispatch.
     */
    protected static void dispatch(Event event) {
        _dispatch (event);
    }

    /**
     * Called by {@link #dispatch(Event)} to wrap in an <code>$entry</code> (see
     * comments in {@link #_processPreview(Event)}).
     */
    protected static native void _dispatch(Event event) /*-{
        $entry(@com.effacy.jui.core.client.dom.EventLifecycle::__dispatch(*))(event);
    }-*/;

    /**
     * Called by {@link #_dispatch(Event)} to implement {@link #dispatch(Event)}.
     */
    protected static void __dispatch(Event event) {
        Element el = firstAncestorWithListener (event);
        if (el == null)
            return;
        eventListenerFor (el).processBrowserEvent (event);
    }

    /**
     * Given an event (more specifically its target), locate the closest (ascendent)
     * element that has an event listener attached to it.
     * 
     * @param event
     *              the event to extract the listener for.
     * @return the element that has the listener.
     */
    private static native Element firstAncestorWithListener(Event event) /*-{
        var el = event.currentTarget;
        while ((el != null) && (el.__juiEventListener == null))
            el = el.parentElement;
        return el;
    }-*/;

    /**
     * Obtains the registered event listener (where there is on) against the passed
     * element.
     * <p>
     * This will have been assigned by {@link #register(Element, IEventListener)}.
     * 
     * @param el
     *           the element to extract the listener from.
     * @return the listener (or {@code null} if there is none).
     */
    private static native IEventListener eventListenerFor(Element el) /*-{
        return el.__juiEventListener;
    }-*/;
    
    /**
     * A generalised event handler.
     */
    @JsFunction
    public interface OneventFn {
        Object onInvoke(Event p0);
    }

    /**
     * Internal. Attaches an event to the element.
     */
    protected static native void on(Element el, String handler, OneventFn fn) /*-{
        el[handler] = fn;
    }-*/;

    /*******************************************************************************
     * Initialisation.
     *******************************************************************************/

    private static boolean INITIALISED = false;

    protected static void init() {
        if (INITIALISED)
            return;
        INITIALISED = true;
        DomGlobal.window.addEventListener ("click", (e) -> processPreview (e), AddEventListenerOptionsUnionType.of (true));
        DomGlobal.window.addEventListener ("dblclick", (e) -> processPreview (e), AddEventListenerOptionsUnionType.of (true));
        DomGlobal.window.addEventListener ("mousedown", (e) -> processPreview (e), AddEventListenerOptionsUnionType.of (true));
        DomGlobal.window.addEventListener ("mouseup", (e) -> processPreview (e), AddEventListenerOptionsUnionType.of (true));
        DomGlobal.window.addEventListener ("mousemove", (e) -> processPreview (e), AddEventListenerOptionsUnionType.of (true));
        DomGlobal.window.addEventListener ("mouseover", (e) -> processPreview (e), AddEventListenerOptionsUnionType.of (true));
        DomGlobal.window.addEventListener ("mouseout", (e) -> processPreview (e), AddEventListenerOptionsUnionType.of (true));
        DomGlobal.window.addEventListener ("mousewheel", (e) -> processPreview (e), AddEventListenerOptionsUnionType.of (true));
        DomGlobal.window.addEventListener ("touchstart", (e) -> processPreview (e), AddEventListenerOptionsUnionType.of (true));
        DomGlobal.window.addEventListener ("touchend", (e) -> processPreview (e), AddEventListenerOptionsUnionType.of (true));
        DomGlobal.window.addEventListener ("touchmove", (e) -> processPreview (e), AddEventListenerOptionsUnionType.of (true));
        DomGlobal.window.addEventListener ("touchcancel", (e) -> processPreview (e), AddEventListenerOptionsUnionType.of (true));
        DomGlobal.window.addEventListener ("gesturestart", (e) -> processPreview (e), AddEventListenerOptionsUnionType.of (true));
        DomGlobal.window.addEventListener ("gestureend", (e) -> processPreview (e), AddEventListenerOptionsUnionType.of (true));
        DomGlobal.window.addEventListener ("gesturechange", (e) -> processPreview (e), AddEventListenerOptionsUnionType.of (true));
    }
}
