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
package com.effacy.jui.ui.client.control;

import java.util.List;

import com.effacy.jui.core.client.component.IDisposeListener;
import com.effacy.jui.core.client.control.IControl;
import com.effacy.jui.core.client.control.IInvalidListener;
import com.effacy.jui.core.client.dom.DomSupport;
import com.effacy.jui.core.client.dom.jquery.JQuery;
import com.effacy.jui.platform.util.client.StringSupport;

import elemental2.dom.Element;
import elemental2.dom.HTMLLIElement;
import jsinterop.base.Js;

/**
 * A mechanism for brokering error states on controls to the various adorning
 * elements.
 * <p>
 * For example, you can declare an element to which a specified error CSS class
 * is applied as well a location to render error messages into.
 * <p>
 * This is generally used within a lambda expression embedded in a DOM building
 * structure.
 */
public class ControlErrorBroker {

    /**
     * Custom message renderer.
     */
    @FunctionalInterface
    public interface IMessageRenderer {

        /**
         * Renders messages into the given element. You can assume that the element has
         * had any previous content removed and that the list of messages is non-empty
         * and contains no empty strings.
         * 
         * @param el
         *                 the element to write into (this will have been set as the
         *                 messages target).
         * @param messages
         *                 the messages to render out.
         */
        public void render(Element el, List<String> messages);
    }

    private IControl<?> control;

    private Element errorStyleTargetEl;

    private String errorStyle;

    private Element messageTargetEl;

    private IMessageRenderer messagerRenderer;

    /**
     * Construct with default renderer (separate messages in an LI).
     */
    public ControlErrorBroker() {
        // Nothing.
    }

    /**
     * Construct with a custom message renderer.
     * 
     * @param messagerRenderer
     *                         the renderer to use.
     */
    public ControlErrorBroker(IMessageRenderer messagerRenderer) {
        this.messagerRenderer = messagerRenderer;
    }

    /**
     * Sets the control to listen to for invalidation events.
     * <p>
     * When the control is disposed of then the references in this instance are
     * cleared.
     * 
     * @param control
     *                the control to register against.
     * @return the passed control.
     */
    public <V,CTL extends IControl<V>> CTL setControl(CTL control) {
        this.control = control;
        this.control.addListener (IInvalidListener.create ((ctl,messages) -> {
            if (errorStyleTargetEl != null)
                errorStyleTargetEl.classList.add (errorStyle);
            if (this.messageTargetEl != null)
                renderErrors (this.messageTargetEl, messages);
        }, (ctl) -> {
            if (errorStyleTargetEl != null)
                errorStyleTargetEl.classList.remove (errorStyle);
            if (this.messageTargetEl != null)
                JQuery.$ (this.messageTargetEl).hide ();
        }));
        this.control.addListener(IDisposeListener.create ((cpt) -> {
            ControlErrorBroker.this.control = null;
            ControlErrorBroker.this.errorStyleTargetEl = null;
        }));
        return control;
    }

    /**
     * Assigns a target element and style to be applied to that element when the
     * control becomes invalid. When the control changes to being valid the style is
     * removed.
     * 
     * @param el
     *              the target for the style.
     * @param style
     *              the style to apply when the control becomes invalid.
     */
    public void setErrorStyleTarget(Element el, String style) {
        this.errorStyleTargetEl = el;
        this.errorStyle = style;
    }

    /**
     * Assigns a target to write error messages into.
     * 
     * @param el
     *           the target for error messages.
     */
    public void setMessageTarget(Element el) {
        this.messageTargetEl = el;
        if (this.messageTargetEl != null)
            JQuery.$ (this.messageTargetEl).hide ();
    }

    /**
     * Called when there is a need to render messages.
     */
    protected void renderErrors(Element el, List<String> messages) {
        DomSupport.removeAllChildren (el);
        if (messages != null)
            messages.removeIf (str -> StringSupport.empty (str));
        if ((messages == null) || messages.isEmpty ()) {
            JQuery.$ (el).hide ();
            return;
        }
        if (messagerRenderer != null) {
            messagerRenderer.render (el, messages);
        } else {
            messages.forEach (error -> {
                HTMLLIElement li = DomSupport.createLi ();
                DomSupport.innerText (li, error);
                el.appendChild (Js.cast (li));
            });
        }
        JQuery.$ (el).show ();
    }
}
