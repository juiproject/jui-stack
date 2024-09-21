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
import java.util.function.Consumer;

import com.effacy.jui.core.client.IDisposable;
import com.effacy.jui.core.client.dom.EventLifecycle.IEventPreviewHandler;
import com.effacy.jui.platform.util.client.Logger;

import elemental2.dom.Element;

/**
 * A utility class that enables a panel to be activated by clicking on an
 * element and the panel to hide when clicking outside the panel.
 * <p>
 * There are two common arrangements. There first is where there is an activator
 * element and a separate panel element. When the activator is clicked on a
 * style is applied to the panel to show it. When a click occurs in the panel
 * the panel remains open while a click outside the panel closes it. Since these
 * latter clicks are handled with a preview a click on the activator will close
 * the panel (via the preview) but the event will continue to propagate and
 * trigger a re-opening of the panel. So in this case a click on the activator
 * needs to cancel the event to stop it propagating. A constructor has been
 * provided to configure this model.
 * <p>
 * There is one problem with the first and that is that showing the panel will
 * often use a display setting which cannot be used to perform CSS transition
 * effects. If these are desired then opacity is a good option (and opacity can
 * be transitioned). However a panel with opacity that is absolutely positioned
 * will occlude the contents below it from receiving click events. This can be
 * problematic. One approach then it to include the panel in a parent that has
 * hidden overflow and then apply the activation style to that. The activation
 * style then sets overflow to visible and the opacity of the panel to 1. If the
 * wrapper element is only contains the panel then the above strategy will work,
 * if not then the whole wrapper will be an exclusion zone which can be
 * problematic (for example the wrapper contains the activator so styles applied
 * to the wrapper can change the state of the activator). If that is the case
 * then one needs to configure things separately through direct styles,
 * exclusions and cancels.
 *
 * @author Jeremy Buckley
 */
public class ActivationHandler implements IDisposable {

    /**
     * Mechanism to bind an element and style together to apply and remove the
     * styles.
     */
    class ClassApplication {

        /**
         * See constructor.
         */
        private Element el;

        /**
         * See constructor.
         */
        private String style;

        /**
         * Construct with element and style to apply / remove.
         * 
         * @param el
         *              the element to apply / remove the style.
         * @param style
         *              the style to apply / remove.
         */
        public ClassApplication(Element el, String style) {
            this.el = el;
            this.style = style;
        }

        /**
         * Apply the embodied style to the element.
         */
        public void apply() {
            el.classList.add (style);
        }

        /**
         * Remove the embodied style from the element.
         */
        public void clear() {
            el.classList.remove (style);
        }
    }

    /**
     * Collection of class applications to be applied on open and cleared on close.
     */
    protected List<ClassApplication> styles = new ArrayList<> ();

    /**
     * Any additional elements that result in a cancellation of the click event.
     */
    protected List<Element> cancels = new ArrayList<> ();

    /**
     * Any additional elements that define exclusion zones.
     */
    protected List<Element> exclusions = new ArrayList<> ();

    /**
     * Flags if the panel is active.
     */
    protected IEventPreviewHandler panelPreview;

    /**
     * Listeners to open and close events.
     */
    private List<Consumer<Boolean>> listeners = new ArrayList<>();

    /**
     * Construct a raw handler with the expectation that it will be configured using
     * {@link #exclude(Element...)} and {@link #cancel(Element...)}.
     */
    public ActivationHandler() {
        // Nothing.
    }

    /**
     * Construct handler against DOM elements.
     * <p>
     * The DOM elements are standard elements commonly involved in an activation.
     * The activator element is configured to cancel events (which prevents the
     * activator being re-activated after being closed), the panel element is the
     * panel that is shown and is added as an exclusion while the panel open class
     * is applied to the panel to show it.
     * <p>
     * There are more complex arrangements and these can be handled by adding
     * cancels, exclusions and styles directly.
     * 
     * @param activatorEl
     *                       the activation element that, when clicked on, activates
     *                       the panel (this is excluded from the event preview to
     *                       close the panel).
     * @param panelControlEl
     *                       the panel element that to which the open class is
     *                       applied to (this controls the opening and closing of
     *                       the panel).
     * @param panelOpenClass
     *                       the class applied to the panel element to show it.
     */
    public ActivationHandler(Element activatorEl, Element panelControlEl, String panelOpenClass) {
        style (panelControlEl, panelOpenClass);
        exclude (panelControlEl);
        cancel (activatorEl);
    }

    /**
     * Adds a listener to the activation handler.
     * <p>
     * Whenever the activator opens or closes the listener will be invoked with a
     * {@code Boolean} value. Open will pass a {@code true} and close a
     * {@code false}.
     * 
     * @param listener
     *                 the listener to add.
     * @return this handler.
     */
    public ActivationHandler listen(Consumer<Boolean> listener) {
        if (listener != null)
            listeners.add (listener);
        return this;
    }

    /**
     * Provides an element and a style to apply to the element when the activation
     * is opened. When closed the style will be removed.
     * 
     * @param el
     *              the element.
     * @param style
     *              the style.
     * @return this handler.
     */
    public ActivationHandler style(Element el, String style) {
        if ((el == null) || (style == null))
            return this;
        styles.add (new ClassApplication (el, style));
        return this;
    }

    /**
     * Provides an element and a style to apply to the element when the activation
     * is opened. When closed the style will be removed.
     * 
     * @param el
     *                the element.
     * @param style
     *                the style.
     * @param exclude
     *                {@code true} if the element should be added to the exlusion
     *                list.
     * @return this handler.
     */
    public ActivationHandler style(Element el, String style, boolean exclude) {
        if ((el == null) || (style == null))
            return this;
        styles.add (new ClassApplication (el, style));
        if (exclude)
            exclude (el);
        return this;
    }

    /**
     * Specifies any elements that should not result in a close of the panel.
     * <p>
     * Generally this should include the panel itself.
     * 
     * @param elements
     *                 the elements.
     * @return this handler.
     */
    public ActivationHandler exclude(Element... elements) {
        for (Element el : elements) {
            if (el != null)
                exclusions.add (el);
        }
        return this;
    }

    /**
     * Specifies any elements that should result in a cancellation of preview click.
     * This only applies when there is a close of the activation (i.e. exclusion
     * zones will not result in a cancel).
     * <p>
     * Generally this should include the activator element to ensure that there is
     * no re-activation.
     * 
     * @param elements
     *                 the elements.
     * @return this handler.
     */
    public ActivationHandler cancel(Element... elements) {
        for (Element el : elements) {
            if (el != null)
                cancels.add (el);
        }
        return this;
    }

    /**
     * Toggles the handler. If the handler is closed then it will be opened,
     * otherwise is will be closed.
     * <p>
     * Generally one calls this when the activator is clicked on.
     * 
     * @return {@code true} if open.
     */
    public boolean toggle() {
        if (!isOpen ()) {
            open ();
            return true;
        }
        close ();
        return false;
    }

    /**
     * Opens the panel. Generally this is invoked when the activator is clicked on.
     */
    public void open() {
        if (panelPreview != null)
            return;
        for (ClassApplication style : styles)
            style.apply ();
        panelPreview = EventLifecycle.registerPreview (e -> {
                if (UIEventType.ONCLICK.matches(e)) {
                    Element el = (Element) e.target;
                    if (!isExcluded (el)) {
                        close ();
                        if (isCancel (el))
                            return EventLifecycle.IEventPreview.Outcome.CANCEL;
                    }
                }
                return EventLifecycle.IEventPreview.Outcome.CONTINUE;
        });
        onOpen ();
        listeners.forEach (listener -> {
            try {
                listener.accept (true);
            } catch (Throwable e) {
                Logger.reportUncaughtException (e, this);
            }
        });
    }

    /**
     * Determines if the passed element lies in an close exclusion zone.
     */
    protected boolean isExcluded(Element el) {
        for (Element elem : exclusions) {
            if (DomSupport.isChildOf (el, elem))
                return true;
        }
        return false;
    }

    /**
     * Determines if the passed element lies in an event cancel zone.
     */
    protected boolean isCancel(Element el) {
        for (Element elem : cancels) {
            if (DomSupport.isChildOf (el, elem))
                return true;
        }
        return false;
    }

    /**
     * Closes the panel.
     */
    public void close() {
        if (panelPreview == null)
            return;
        for (ClassApplication style : styles)
            style.clear ();
        panelPreview.remove ();
        panelPreview = null;
        onClose ();
        listeners.forEach (listener -> {
            try {
                listener.accept (false);
            } catch (Throwable e) {
                Logger.reportUncaughtException (e, this);
            }
        });
    }

    /**
     * Determines if the panel is open or not.
     * 
     * @return {@code true} if it is.
     */
    public boolean isOpen() {
        return (panelPreview != null);
    }

    /**
     * Invoked when opened.
     */
    protected void onOpen() {
        // Nothing.
    }

    /**
     * Invoked when closed.
     */
    protected void onClose() {
        // Nothing.
    }

    /**
     * {@inheritDoc}
     * 
     * @see IDisposable#dispose().
     */
    @Override
    public void dispose() {
        close ();
        cancels.clear ();
        exclusions.clear ();
        styles.clear ();
        listeners.clear ();
    }

}
