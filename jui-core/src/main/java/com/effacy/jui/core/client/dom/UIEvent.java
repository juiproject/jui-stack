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

import java.util.Collection;

import com.effacy.jui.core.client.observable.IObservable;

import elemental2.dom.DataTransfer;
import elemental2.dom.DragEvent;
import elemental2.dom.Element;
import elemental2.dom.Event;
import elemental2.dom.KeyboardEvent;
import elemental2.dom.Node;
import jsinterop.base.Js;

/**
 * Wrapper around a standard event with some utility methods.
 *
 * @author Jeremy Buckley
 */
public class UIEvent {

    /**
     * Various keycodes as capture by a UI event.
     */
    public enum KeyCode {
        ARROW_UP("ArrowUp"),
        ARROW_DOWN("ArrowDown"),
        ARROW_LEFT("ArrowLeft"),
        ARROW_RIGHT("ArrowRight"),
        BACKSPACE("Backspace"),
        ENTER("Enter"),
        TAB("Tab");

        /**
         * Private constructor.
         */
        private KeyCode(String code) {
            this.code = code;
        }

        /**
         * See {@link #code()}.
         */
        private String code;

        /**
         * The underlying key code.
         * 
         * @return the code
         * @see https://developer.mozilla.org/en-US/docs/Web/API/UI_Events/Keyboard_event_code_values
         */
        public String code() {
            return code;
        }

        /**
         * Given an event determine if there is a keycode and that it is this keycode.
         * 
         * @param e
         *          the IUI event.
         * @return {@code true} if the keycode matches.
         */
        public boolean is(UIEvent e) {
            String keycode = e.getKeyCode ();
            if (keycode == null)
                return false;
            return code.equals (keycode);
        }

        /**
         * Determines if the passed code matches this one.
         * 
         * @param code
         *             the code.
         * @return {@code true} if the keycode matches.
         */
        public boolean is(String code) {
            if (code == null)
                return false;
            return code.equals (this.code);
        }

        /**
         * Convenience to check if the event represents an arrow key.
         * 
         * @param e
         *          the event to check.
         * @return {@code true} if the event is an arrow key.
         */
        public static boolean isArrowKey(UIEvent e) {
            String keycode = e.getKeyCode ();
            if (keycode == null)
                return false;
            return KeyCode.ARROW_UP.code.equals (keycode) || KeyCode.ARROW_DOWN.code.equals (keycode) //
                    || KeyCode.ARROW_LEFT.code.equals (keycode) || KeyCode.ARROW_RIGHT.code.equals (keycode);
        }
    }

    /**
     * The source of the IU event.
     */
    protected IObservable source;

    /**
     * The wrapped event.
     */
    protected Event event;

    /**
     * The UI event type.
     */
    protected UIEventType type;

    /**
     * If the event is being stopped.
     */
    protected boolean stopped;

    /**
     * Construct the event.
     * 
     * @param source
     *               the source of the event (the object that create it).
     * @param event
     *               the event being wrapped.
     */
    public UIEvent(IObservable source, Event event) {
        this.source = source;
        this.event = event;
    }

    /**
     * The source of the event. This is the object that created it (and will depend
     * on the context).
     * 
     * @return the source.
     */
    public IObservable getSource() {
        return source;
    }

    /**
     * Gets the underlying DOM event.
     * 
     * @return The underling DOM event.
     */
    public Event getEvent() {
        return event;
    }

    /**
     * Gets the UI event type (which also captures named bitless events).
     * 
     * @return the event type.
     */
    public UIEventType getType() {
        if (type == null)
            type = UIEventType.resolve (getEventLabel ());
        return type;
    }

    /**
     * Gets the event label (type as a string).
     * 
     * @return the event label.
     */
    public String getEventLabel() {
        return (event == null) ? null : event.type;
    }

    /**
     * Gets the event key code.
     * 
     * @return the key code.
     */
    public String getKeyCode() {
        if (!(event instanceof KeyboardEvent))
            return null;
        return (event == null) ? null : ((KeyboardEvent) Js.cast (event)).code;
    }

    /**
     * Gets the event key.
     * 
     * @return the key.
     */
    public String getKey() {
        if (!(event instanceof KeyboardEvent))
            return null;
        return (event == null) ? null : ((KeyboardEvent) Js.cast (event)).key;
    }

    /**
     * Determines if the shift key is pressed.
     * 
     * @return {@code true} if it is.
     */
    public boolean isShiftKey() {
        if (!(event instanceof KeyboardEvent))
            return false;
        return (event == null) ? false : ((KeyboardEvent) Js.cast (event)).shiftKey;
    }

    /**
     * Determines if the alt key is pressed.
     * 
     * @return {@code true} if it is.
     */
    public boolean isAltKey() {
        if (!(event instanceof KeyboardEvent))
            return false;
        return (event == null) ? false : ((KeyboardEvent) Js.cast (event)).altKey;
    }

    /**
     * Determines if the up arrow key is pressed.
     * 
     * @return {@code true} if it is.
     */
    public boolean isUpKey() {
        if (!(event instanceof KeyboardEvent))
            return false;
        return (event == null) ? false : "ArrowUp".equals (((KeyboardEvent) Js.cast (event)).key);
    }

    /**
     * Determines if the down arrow key is pressed.
     * 
     * @return {@code true} if it is.
     */
    public boolean isDownKey() {
        if (!(event instanceof KeyboardEvent))
            return false;
        return (event == null) ? false : "ArrowDown".equals (((KeyboardEvent) Js.cast (event)).key);
    }

    /**
     * Determines if the left arrow key is pressed.
     * 
     * @return {@code true} if it is.
     */
    public boolean isLeftKey() {
        if (!(event instanceof KeyboardEvent))
            return false;
        return (event == null) ? false : "ArrowLeft".equals (((KeyboardEvent) Js.cast (event)).key);
    }

    /**
     * Determines if the right arrow key is pressed.
     * 
     * @return {@code true} if it is.
     */
    public boolean isRightKey() {
        if (!(event instanceof KeyboardEvent))
            return false;
        return (event == null) ? false : "ArrowRight".equals (((KeyboardEvent) Js.cast (event)).key);
    }

    /**
     * Gets the event target element.
     * 
     * @return The element.
     */
    public Element getTarget() {
        if ((event == null) || (event.target == null))
            return null;
        return NodeSupport.asElement (event.target);
    }

    /**
     * Gets the parent that matches the passed selector.
     * 
     * @param selector
     *                    the selector to match against.
     * @param maxDistance
     *                    the maximum parents to search in.
     * @return the resulting match.
     */
    public Element getTarget(String selector, int maxDistance) {
        return DomSupport.parent (getTarget (), selector, maxDistance);
    }

    /**
     * Determines if this event is one of the specified types.
     * 
     * @param types
     *              the types to check.
     * @return {@code true} if there is a match on one of the passed events.
     */
    public boolean isEvent(UIEventType... types) {
        for (UIEventType type : types) {
            if ((type != null) && type.matches (event))
                return true;
        }
        return false;
    }

    /**
     * Determines if this event is one of the specified types.
     * 
     * @param types
     *              the types to check.
     * @return {@code true} if there is a match on one of the passed events.
     */
    public boolean isEvent(Collection<UIEventType> types) {
        for (UIEventType type : types) {
            if ((type != null) && type.matches (event))
                return true;
        }
        return false;
    }

    /**
     * Determines if this event is a match for the given node and type. The types
     * must match directly while the target of the event must be in the sub-tree
     * headed by the node.
     * 
     * @param node
     *              the node to match.
     * @param types
     *              event types to match.
     * @return {@code true} if there is a match.
     */
    public boolean matches(Node node, UIEventType... types) {
        if (!isEvent (types))
            return false;
        return DomSupport.isChildOf (getTarget (), node);
    }

    /**
     * Prevents the browser from taking its default action for the given event.
     */
    public void preventDefault() {
        if (event != null)
            event.preventDefault ();
    }

    /**
     * Cancels bubbling for the given event. This will stop the event from being
     * propagated to parent elements.
     */
    public void cancelBubble() {
        stopped = true;
        if (event != null)
            _cancelBubble (event);
    }

    /**
     * Called by {@link #cancelBubble()}.
     */
    private native void _cancelBubble(Event evt) /*-{
        evt.cancelBubble = true;
      }-*/;

    /**
     * Stops the event (preventDefault and cancelBubble).
     */
    public void stopEvent() {
        stopped = true;
        cancelBubble ();
        preventDefault ();
    }

    /**
     * Determines if the event has been stopped (by a call to {@link #stopEvent()}
     * or {@link #cancelBubble()}). Note that if the underlying event has been
     * cancelled directly then this will not return {@code true}.
     * 
     * @return {@code true} if it has been stopped.
     */
    public boolean isStopped() {
        return stopped;
    }

    /**
     * Obtain data transfer from the event.
     * 
     * @return the transfer.
     */
    public DataTransfer getDataTransfer() {
        return (event == null) ? null : ((DragEvent) Js.cast (event)).dataTransfer;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return (event == null) ? null : event.type;
    }
}
