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

import java.util.HashMap;
import java.util.Map;

import com.effacy.jui.core.client.dom.EventLifecycle.OneventFn;

import elemental2.dom.Element;
import elemental2.dom.Element.OnblurFn;
import elemental2.dom.Element.OnchangeFn;
import elemental2.dom.Element.OnclickFn;
import elemental2.dom.Element.OncontextmenuFn;
import elemental2.dom.Element.OndblclickFn;
import elemental2.dom.Element.OnfocusFn;
import elemental2.dom.Element.OnfocusinFn;
import elemental2.dom.Element.OnfocusoutFn;
import elemental2.dom.Element.OninputFn;
import elemental2.dom.Element.OnkeydownFn;
import elemental2.dom.Element.OnkeypressFn;
import elemental2.dom.Element.OnkeyupFn;
import elemental2.dom.Element.OnloadFn;
import elemental2.dom.Element.OnmousedownFn;
import elemental2.dom.Element.OnmousemoveFn;
import elemental2.dom.Element.OnmouseoutFn;
import elemental2.dom.Element.OnmouseoverFn;
import elemental2.dom.Element.OnmouseupFn;
import elemental2.dom.Element.OnmousewheelFn;
import elemental2.dom.Element.OnpasteFn;
import elemental2.dom.Element.OnscrollFn;
import elemental2.dom.Element.OntouchcancelFn;
import elemental2.dom.Element.OntouchendFn;
import elemental2.dom.Element.OntouchmoveFn;
import elemental2.dom.Element.OntouchstartFn;
import elemental2.dom.Event;

/**
 * Enumeration of various UI related event types combining those with bit
 * representations (see {@link Event}) and those that do not (see
 * {@link BrowserEvent}, though there are overlaps).
 *
 * @author Jeremy Buckley
 */
public enum UIEventType {

    /**
     * General unknown event.
     */
    UNKNOWN() {
        public void _attach(Element el) { }
        public void _detach(Element el) { }
    },

    /**
     * DOM ONBLUR event type.
     */
    ONBLUR(BrowserEvent.BLUR) {
        private OnblurFn handler = (e) -> { EventLifecycle.dispatch (e); return null; };
        public void _attach(Element el) { EventLifecycle.init (); el.onblur = handler; }
        public void _detach(Element el) { el.onblur = null; }
    },

    /**
     * DOM CANPLAYTHROUGH event type.
     */
    CANPLAYTHROUGH(BrowserEvent.CANPLAYTHROUGH) {
        private OneventFn handler = (e) -> { EventLifecycle.dispatch (e); return null; };
        public void _attach(Element el) { EventLifecycle.on (el, "oncanplaythrough", handler); }
        public void _detach(Element el) { EventLifecycle.on (el, "oncanplaythrough", null); }
    },

    /**
     * DOM ONCHANGE event type.
     */
    ONCHANGE(BrowserEvent.CHANGE) {
        private OnchangeFn handler = (e) -> { EventLifecycle.dispatch (e); return null; };
        public void _attach(Element el) { EventLifecycle.init (); el.onchange = handler; }
        public void _detach(Element el) { el.onchange = null; }
    },

    /**
     * DOM ONCLICK event type.
     */
    ONCLICK(BrowserEvent.CLICK) {
        private OnclickFn handler = (e) -> { EventLifecycle.dispatch (e); return null; };
        public void _attach(Element el) { EventLifecycle.init (); el.onclick = handler; }
        public void _detach(Element el) { el.onclick = null; }
    },

    /**
     * DOM ONCONTEXTMENU event type.
     */
    ONCONTEXTMENU(BrowserEvent.CONTEXTMENU) {
        private OncontextmenuFn handler = (e) -> { EventLifecycle.dispatch (e); return null; };
        public void _attach(Element el) { EventLifecycle.init (); el.oncontextmenu = handler; }
        public void _detach(Element el) { el.oncontextmenu = null; }
    },

    /**
     * DOM ONDBLCLICK event type.
     */
    ONDBLCLICK(BrowserEvent.DBLCLICK) {
        private OndblclickFn handler = (e) -> { EventLifecycle.dispatch (e); return null; };
        public void _attach(Element el) { EventLifecycle.init (); el.ondblclick = handler; }
        public void _detach(Element el) { el.ondblclick = null; }
    },

    /**
     * DOM DRAG event type.
     */
    DRAG(BrowserEvent.DRAG) {
        private OneventFn handler = (e) -> { EventLifecycle.dispatch (e); return null; };
        public void _attach(Element el) { EventLifecycle.on (el, "ondrag", handler); }
        public void _detach(Element el) { EventLifecycle.on (el, "ondrag", null); }
    },

    /**
     * DOM DRAGEND event type.
     */
    DRAGEND(BrowserEvent.DRAGEND) {
        private OneventFn handler = (e) -> { EventLifecycle.dispatch (e); return null; };
        public void _attach(Element el) { EventLifecycle.on (el, "ondragend", handler); }
        public void _detach(Element el) { EventLifecycle.on (el, "ondragend", null); }
    },

    /**
     * DOM DRAGENTER event type.
     */
    DRAGENTER(BrowserEvent.DRAGENTER) {
        private OneventFn handler = (e) -> { EventLifecycle.dispatch (e); return null; };
        public void _attach(Element el) { EventLifecycle.on (el, "ondragenter", handler); }
        public void _detach(Element el) { EventLifecycle.on (el, "ondragenter", null); }
    },

    /**
     * DOM DRAGLEAVE event type.
     */
    DRAGLEAVE(BrowserEvent.DRAGLEAVE) {
        private OneventFn handler = (e) -> { EventLifecycle.dispatch (e); return null; };
        public void _attach(Element el) { EventLifecycle.on (el, "ondragleave", handler); }
        public void _detach(Element el) { EventLifecycle.on (el, "ondragleave", null); }
    },

    /**
     * DOM DRAGOVER event type.
     */
    DRAGOVER(BrowserEvent.DRAGOVER) {
        private OneventFn handler = (e) -> { EventLifecycle.dispatch (e); return null; };
        public void _attach(Element el) { EventLifecycle.on (el, "ondragover", handler); }
        public void _detach(Element el) { EventLifecycle.on (el, "ondragover", null); }
    },

    /**
     * DOM DRAGSTART event type.
     */
    DRAGSTART(BrowserEvent.DRAGSTART) {
        private OneventFn handler = (e) -> { EventLifecycle.dispatch (e); return null; };
        public void _attach(Element el) { EventLifecycle.on (el, "ondragstart", handler); }
        public void _detach(Element el) { EventLifecycle.on (el, "ondragstart", null); }
    },

    /**
     * DOM DROP event type.
     */
    DROP(BrowserEvent.DROP) {
        private OneventFn handler = (e) -> { EventLifecycle.dispatch (e); return null; };
        public void _attach(Element el) { EventLifecycle.on (el, "ondrop", handler); }
        public void _detach(Element el) { EventLifecycle.on (el, "ondrop", null); }
    },

    /**
     * DOM ENDED event type.
     */
    ENDED(BrowserEvent.ENDED) {
        private OneventFn handler = (e) -> { EventLifecycle.dispatch (e); return null; };
        public void _attach(Element el) { EventLifecycle.on (el, "onended", handler); }
        public void _detach(Element el) { EventLifecycle.on (el, "onended", null); }
    },

    /**
     * DOM ONERROR event type.
     */
    ONERROR(BrowserEvent.ERROR) {
        private OneventFn handler = (e) -> { EventLifecycle.dispatch (e); return null; };
        public void _attach(Element el) { EventLifecycle.on (el, "onerror", handler); }
        public void _detach(Element el) { EventLifecycle.on (el, "onerror", null); }
    },

    /**
     * DOM ONFOCUS event type.
     */
    ONFOCUS(BrowserEvent.FOCUS) {
        private OnfocusFn handler = (e) -> { EventLifecycle.dispatch (e); return null; };
        public void _attach(Element el) { EventLifecycle.init (); el.onfocus = handler; }
        public void _detach(Element el) { el.onfocus = null; }
    },

    /**
     * DOM FOCUSIN event type.
     */
    FOCUSIN(BrowserEvent.FOCUSIN) {
        private OnfocusinFn handler = (e) -> { EventLifecycle.dispatch (e); return null; };
        public void _attach(Element el) { EventLifecycle.init (); el.onfocusin = handler; }
        public void _detach(Element el) { el.onfocusin = null; }
    },

    /**
     * DOM FOCUSOUT event type.
     */
    FOCUSOUT(BrowserEvent.FOCUSOUT) {
        private OnfocusoutFn handler = (e) -> { EventLifecycle.dispatch (e); return null; };
        public void _attach(Element el) { EventLifecycle.init (); el.onfocusout = handler; }
        public void _detach(Element el) { el.onfocusout = null; }
    },

    /**
     * DOM ONINPUT event type.
     */
    ONINPUT(BrowserEvent.INPUT) {
        private OninputFn handler = (e) -> { EventLifecycle.dispatch (e); return null; };
        public void _attach(Element el) { EventLifecycle.init (); el.oninput = handler; }
        public void _detach(Element el) { el.oninput = null; }
    },

    /**
     * DOM GESTURECHANGE event type.
     */
    GESTURECHANGE(BrowserEvent.GESTURECHANGE) {
        private OneventFn handler = (e) -> { EventLifecycle.dispatch (e); return null; };
        public void _attach(Element el) { EventLifecycle.on (el, "ongesturechange", handler); }
        public void _detach(Element el) { EventLifecycle.on (el, "ongesturechange", null); }
    },

    /**
     * DOM GESTUREEND event type.
     */
    GESTUREEND(BrowserEvent.GESTUREEND) {
        private OneventFn handler = (e) -> { EventLifecycle.dispatch (e); return null; };
        public void _attach(Element el) { EventLifecycle.on (el, "ongestureend", handler); }
        public void _detach(Element el) { EventLifecycle.on (el, "ongestureend", null); }
    },

    /**
     * DOM GESTURESTART event type.
     */
    GESTURESTART(BrowserEvent.GESTURESTART) {
        private OneventFn handler = (e) -> { EventLifecycle.dispatch (e); return null; };
        public void _attach(Element el) { EventLifecycle.on (el, "ongesturestart", handler); }
        public void _detach(Element el) { EventLifecycle.on (el, "ongesturestart", null); }
    },

    /**
     * DOM ONKEYDOWN event type.
     */
    ONKEYDOWN(BrowserEvent.KEYDOWN) {
        private OnkeydownFn handler = (e) -> { EventLifecycle.dispatch (e); return null; };
        public void _attach(Element el) { EventLifecycle.init (); el.onkeydown = handler; }
        public void _detach(Element el) { el.onkeydown = null; }
    },

    /**
     * DOM ONKEYDOWN event type.
     */
    ONKEYPRESS(BrowserEvent.KEYPRESS) {
        private OnkeypressFn handler = (e) -> { EventLifecycle.dispatch (e); return null; };
        public void _attach(Element el) { EventLifecycle.init (); el.onkeypress = handler; }
        public void _detach(Element el) { el.onkeypress = null; }
    },

    /**
     * DOM ONKEYUP event type.
     */
    ONKEYUP(BrowserEvent.KEYUP) {
        private OnkeyupFn handler = (e) -> { EventLifecycle.dispatch (e); return null; };
        public void _attach(Element el) { EventLifecycle.init (); el.onkeyup = handler; }
        public void _detach(Element el) { el.onkeyup = null; }
    },

    /**
     * DOM ONLOAD event type.
     */
    ONLOAD(BrowserEvent.LOAD) {
        private OnloadFn handler = (e) -> { EventLifecycle.dispatch (e); };
        public void _attach(Element el) { EventLifecycle.init (); el.onload = handler; }
        public void _detach(Element el) { el.onload = null; }
    },

    /**
     * DOM LOADEDMETADATE event type.
     */
    LOADEDMETADATA(BrowserEvent.LOADEDMETADATA) {
        private OneventFn handler = (e) -> { EventLifecycle.dispatch (e); return null; };
        public void _attach(Element el) { EventLifecycle.on (el, "onloadmetadata", handler); }
        public void _detach(Element el) { EventLifecycle.on (el, "onloadmetadata", null); }
    },

    /**
     * DOM ONMOUSEDOWN event type.
     */
    ONMOUSEDOWN(BrowserEvent.MOUSEDOWN) {
        private OnmousedownFn handler = (e) -> { EventLifecycle.dispatch (e); return null; };
        public void _attach(Element el) { EventLifecycle.init (); el.onmousedown = handler; }
        public void _detach(Element el) { el.onmousedown = null; }
    },

    /**
     * DOM ONMOUSEMOVE event type.
     */
    ONMOUSEMOVE(BrowserEvent.MOUSEMOVE) {
        private OnmousemoveFn handler = (e) -> { EventLifecycle.dispatch (e); return null; };
        public void _attach(Element el) { EventLifecycle.init (); el.onmousemove = handler; }
        public void _detach(Element el) { el.onmousemove = null; }
    },

    /**
     * DOM ONMOUSEOVER event type.
     * <p>
     * See also {@link #ONMOUSEENTER}.
     */
    ONMOUSEOVER(BrowserEvent.MOUSEOVER) {
        private OnmouseoverFn handler = (e) -> { EventLifecycle.dispatch (e); return null; };
        public void _attach(Element el) { EventLifecycle.init (); el.onmouseover = handler; }
        public void _detach(Element el) { el.onmouseover = null; }
    },

    /**
     * DOM ONMOUSEOUT event type.
     * <p>
     * See also {@link #ONMOUSELEAVE}.
     */
    ONMOUSEOUT(BrowserEvent.MOUSEOUT) {
        private OnmouseoutFn handler = (e) -> { EventLifecycle.dispatch (e); return null; };
        public void _attach(Element el) { EventLifecycle.init (); el.onmouseout = handler; }
        public void _detach(Element el) { el.onmouseout = null; }
    },

    /**
     * DOM ONMOUSEENTER event type.
     * <p>
     * This is when the mouse enters the element as a whole. For entering a parent
     * by leaving a child use {@link #ONMOUSEOVER}.
     */
    ONMOUSEENTER(BrowserEvent.MOUSEENTER) {
        private OneventFn handler = (e) -> { EventLifecycle.dispatch (e); return null; };
        public void _attach(Element el) { EventLifecycle.on (el, "onmouseenter", handler); }
        public void _detach(Element el) { EventLifecycle.on (el, "onmouseenter", null); }
    },

    /**
     * DOM ONMOUSEENTER event type.
     * <p>
     * This is when the mouse leaves the element as a whole. For leaving a parent
     * by entering a child use {@link #ONMOUSEOUT}.
     */
    ONMOUSELEAVE(BrowserEvent.MOUSELEAVE) {
        private OneventFn handler = (e) -> { EventLifecycle.dispatch (e); return null; };
        public void _attach(Element el) { EventLifecycle.on (el, "onmouseleave", handler); }
        public void _detach(Element el) { EventLifecycle.on (el, "onmouseleave", null); }
    },

    /**
     * DOM ONMOUSEUP event type.
     */
    ONMOUSEUP(BrowserEvent.MOUSEUP) {
        private OnmouseupFn handler = (e) -> { EventLifecycle.dispatch (e); return null; };
        public void _attach(Element el) { EventLifecycle.init (); el.onmouseup = handler; }
        public void _detach(Element el) { el.onmouseup = null; }
    },

    /**
     * DOM ONMOUSEWHEEL event type.
     */
    ONMOUSEWHEEL(BrowserEvent.MOUSEWHEEL) {
        private OnmousewheelFn handler = (e) -> { EventLifecycle.dispatch (e); return null; };
        public void _attach(Element el) { EventLifecycle.init (); el.onmousewheel = handler; }
        public void _detach(Element el) { el.onmousewheel = null; }
    },

    /**
     * DOM ONPASTE event type.
     */
    ONPASTE(BrowserEvent.PASTE) {
        private OnpasteFn handler = (e) -> { EventLifecycle.dispatch (e); return null; };
        public void _attach(Element el) { EventLifecycle.init (); el.onpaste = handler; }
        public void _detach(Element el) { el.onpaste = null; }
    },

    /**
     * DOM PROGRESS event type.
     */
    PROGRESS(BrowserEvent.PROGRESS) {
        private OneventFn handler = (e) -> { EventLifecycle.dispatch (e); return null; };
        public void _attach(Element el) { EventLifecycle.on (el, "onprogress", handler); }
        public void _detach(Element el) { EventLifecycle.on (el, "onprogress", null); }
    },

    /**
     * DOM ONSCROLL event type.
     */
    ONSCROLL(BrowserEvent.SCROLL) {
        private OnscrollFn handler = (e) -> { EventLifecycle.dispatch (e); return null; };
        public void _attach(Element el) { EventLifecycle.init (); el.onscroll = handler; }
        public void _detach(Element el) { el.onscroll = null; }
    },

    /**
     * DOM TOUCHCANCEL event type.
     */
    TOUCHCANCEL(BrowserEvent.TOUCHCANCEL) {
        private OntouchcancelFn handler = (e) -> { EventLifecycle.dispatch (e); return null; };
        public void _attach(Element el) { EventLifecycle.init (); el.ontouchcancel = handler; }
        public void _detach(Element el) { el.ontouchcancel = null; }
    },

    /**
     * DOM TOUCHEND event type.
     */
    TOUCHEND(BrowserEvent.TOUCHEND) {
        private OntouchendFn handler = (e) -> { EventLifecycle.dispatch (e); return null; };
        public void _attach(Element el) { EventLifecycle.init (); el.ontouchend = handler; }
        public void _detach(Element el) { el.ontouchend = null; }
    },

    /**
     * DOM TOUCHMOVE event type.
     */
    TOUCHMOVE(BrowserEvent.TOUCHMOVE) {
        private OntouchmoveFn handler = (e) -> { EventLifecycle.dispatch (e); return null; };
        public void _attach(Element el) { EventLifecycle.init (); el.ontouchmove = handler; }
        public void _detach(Element el) { el.ontouchmove = null; }
    },

    /**
     * DOM TOUCHSTART event type.
     */
    TOUCHSTART(BrowserEvent.TOUCHSTART) {
        private OntouchstartFn handler = (e) -> { EventLifecycle.dispatch (e); return null; };
        public void _attach(Element el) { EventLifecycle.init (); el.ontouchstart = handler; }
        public void _detach(Element el) { el.ontouchstart = null; }
    };

    /**
     * The associated named event.
     */
    private String label = null;

    /**
     * Map of known browser events to the underlying UI event.
     */
    //private static Map<Integer, UIEventType> CODES;

    /**
     * Map of known browser events to the underlying UI event.
     */
    private static Map<String, UIEventType> LABELS;

    /**
     * Constructs an unknown event type.
     */
    private UIEventType() {
        // Nothing.
    }

    /**
     * Construct with a browser event code.
     * 
     * @param label
     *             the event label.
     */
    private UIEventType(String label) {
        this.label = label;
    }

    /**
     * Obtains the browser event label (these are for named events rather than ones
     * with a code).
     * 
     * @return the browser event label or {@code null} if this does not have one.
     */
    public String label() {
        return label;
    }

    /**
     * Sinks this event on the given element. This will _attach the GWT event
     * dispatcher to the element against the given event handler.
     * 
     * @param el
     *           the element to sink the event on (this can be {@code null} in which
     *           case nothing will happen).
     */
    public Element attach(Element el) {
        if (el == null)
            return null;

        _attach (el);

        return el;
    }

    protected abstract void _attach(Element el);

    public void detach(Element el) {
        _detach (el);
    }

    protected abstract void _detach(Element el);

    /**
     * Determines if this is one of the passed type.
     * 
     * @param types
     *              the types to test for.
     * @return {@code true} if it is.
     */
    public boolean is(UIEventType...types) {
        for (UIEventType type : types) {
            if (this == type)
                return true;
        }
        return false;
    }

    /**
     * Determines if this event type matches the passed event.
     * 
     * @param event
     *              the event to test.
     * @return {@code true} if there is a match.
     */
    public boolean matches(Event event) {
        if (event == null)
            return false;
        if (label == null)
            return false;
        return (label.equalsIgnoreCase (event.type));
    }

    /**
     * Given a standard browser event label return the associated
     * {@link UIEventType} if there is one.
     * 
     * @param label
     *              the browser event label.
     * @return the associated UI event (or {@code null} if there is no match).
     */
    public static synchronized UIEventType resolve(String label) {
        if (LABELS == null) {
            LABELS = new HashMap<String, UIEventType> ();
            for (UIEventType eventType : UIEventType.values ()) {
                String l = eventType.label ();
                if (l == null)
                    continue;
                l = l.toLowerCase ().trim ();
                LABELS.put (l, eventType);
                if (l.startsWith ("on"))
                    continue;
                LABELS.put ("on" + l, eventType);
            }
        }
        UIEventType result = (label == null) ? null : LABELS.get (label.toLowerCase ().trim ());
        return (result == null) ? UNKNOWN : result;
    }

    /**
     * Raw browser events.
     */
    public static final class BrowserEvent {

        public static final String BLUR = "blur";
        public static final String CANPLAYTHROUGH = "canplaythrough";
        public static final String CHANGE = "change";
        public static final String CLICK = "click";
        public static final String CONTEXTMENU = "contextmenu";
        public static final String DBLCLICK = "dblclick";
        public static final String DRAG = "drag";
        public static final String DRAGEND = "dragend";
        public static final String DRAGENTER = "dragenter";
        public static final String DRAGLEAVE = "dragleave";
        public static final String DRAGOVER = "dragover";
        public static final String DRAGSTART = "dragstart";
        public static final String DROP = "drop";
        public static final String ENDED = "ended";
        public static final String ERROR = "error";
        public static final String FOCUS = "focus";
        public static final String FOCUSIN = "focusin";
        public static final String FOCUSOUT = "focusout";
        public static final String GESTURECHANGE = "gesturechange";
        public static final String GESTUREEND = "gestureend";
        public static final String GESTURESTART = "gesturestart";
        public static final String INPUT = "input";
        public static final String KEYDOWN = "keydown";
        public static final String KEYPRESS = "keypress";
        public static final String KEYUP = "keyup";
        public static final String LOAD = "load";
        public static final String LOADEDMETADATA = "loadedmetadata";
        public static final String LOSECAPTURE = "losecapture";
        public static final String MOUSEDOWN = "mousedown";
        public static final String MOUSEMOVE = "mousemove";
        public static final String MOUSEOUT = "mouseout";
        public static final String MOUSEOVER = "mouseover";
        public static final String MOUSEENTER = "mouseenter";
        public static final String MOUSELEAVE = "mouseleave";
        public static final String MOUSEUP = "mouseup";
        public static final String MOUSEWHEEL = "mousewheel";
        public static final String PROGRESS = "progress";
        public static final String SCROLL = "scroll";
        public static final String TOUCHCANCEL = "touchcancel";
        public static final String TOUCHEND = "touchend";
        public static final String TOUCHMOVE = "touchmove";
        public static final String TOUCHSTART = "touchstart";
        public static final String PASTE = "paste";

        /**
         * Private constructor.
         */
        private BrowserEvent() {
            // Nothing.
        }
    }

}
