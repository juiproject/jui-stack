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
package com.effacy.jui.ui.client;

import java.util.HashMap;
import java.util.Map;

import com.effacy.jui.core.client.component.Component;
import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.dom.DomSupport;
import com.effacy.jui.core.client.dom.renderer.template.ITemplateBuilder;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.TimerSupport;
import com.google.gwt.core.client.GWT;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;

/**
 * Provides a notification message that displays at the base of the screen.
 * <p>
 * To use call the static method {@link Notifier#create()} which returns an
 * instance of the notifier. The content can then be configured and the
 * notification shown with {@link Notifier#show()} or
 * {@link Notifier#show(int)}. The latter provides a duration for the
 * notification to show upon which it is removed. The former displays
 * permanently until {@link Notifier#remove()} is called.
 * <p>
 * Note that the notification instance can be re-used as needed, or even
 * refreshed (which requires re-showing). That allows the notification to
 * display progress rather just a brief notice of an event or action.
 *
 * @author Jeremy Buckley
 */
public class Notifier {

    /**
     * The default time to show a message.
     */
    public static int DEFAULT_SHOW_PERIOD = 2000;

    /**
     * See {@link #text(String)}.
     */
    private String text;

    /**
     * Internal timer.
     */
    private TimerSupport.ITimer timer;

    /**
     * Assigns some text to display.
     * 
     * @param text
     *             the text to display.
     * @return this notifier instance.
     */
    public Notifier text(String text) {
        this.text = text;
        return this;
    }

    /**
     * Shows the notifier.
     * <p>
     * The notification will not be hidden and remains shown until a call to
     * {@link #remove()} is made.
     */
    public void show() {
        show (0);
    }

    /**
     * Shows the notifier for the given length of time (after which it will be
     * removed).
     * 
     * @param millis
     *               the time (in ms).
     */
    public void show(int millis) {
        instance ().render (this);

        // Start the timer when there is a time period being applied.
        if (timer != null)
            timer.cancel ();
        if (millis > 0) {
            if (timer == null)
                timer = TimerSupport.timer (() -> remove ());
        }
        timer.schedule (millis);
    }

    /**
     * Removes this notifier.
     */
    public void remove() {
        if (timer != null) {
            timer.cancel ();
            timer = null;
        }
        instance ().remove (this);
    }

    /**
     * Creates a notifier.
     * 
     * @return the notifier to configure and show.
     */
    public static Notifier create() {
        return new Notifier ();
    }

    /**
     * Clears all the notifiers that are currently showing.
     */
    public static void clear() {
        instance ().clear ();
    }

    /**
     * See {@link #notify(String, int)} but uses {@link #DEFAULT_SHOW_PERIOD} as the display period.
     * 
     * @param message
     *                the message to disdplay.
     */
    public static void notify(String message) {
        notify (message, DEFAULT_SHOW_PERIOD);
    }

    /**
     * Convenience to display a message for the given period of time.
     * 
     * @param message
     *                the message to disdplay.
     * @param millis
     *                the time (in ms) to display for.
     */
    public static void notify(String message, int millis) {
        Notifier.create ().text (message).show (millis);
    }

    /************************************************************************
     * Implementation
     ************************************************************************/

    private static NotifierImpl INSTANCE;

    private static NotifierImpl instance() {
        if (INSTANCE == null) {
            INSTANCE = new NotifierImpl ();
            Element el = DomSupport.createDiv ();
            el.id = CONTAINER_NAME;
            DomGlobal.document.body.appendChild (el);
            INSTANCE.bind (CONTAINER_NAME);
        }
        return INSTANCE;
    }

    /**
     * ID for the top-level notifier DIV.
     */
    private static String CONTAINER_NAME = "notifier-container";

    /**
     * Actual notifier component that displays notifications. Notifications are
     * stacked on top of each other until they are removed. The removal process
     * involves CSS animations to fade and collapse the notification prior to the
     * removal of the notification DOM itself. This component provides the stacking
     * so embodied multiple, dynamic notifications. As such this is only one
     * instance of this and is attached to a top-level DOM element.
     */
    static class NotifierImpl extends Component<Component.Config> {

        /**
         * Collection of notifiers that are currently displayed.
         */
        private Map<Notifier, Element> notifiers = new HashMap<> ();

        /**
         * Constructor.
         */
        protected NotifierImpl() {
            super (new Component.Config ());
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.component.RendererComponent#styles()
         */
        @Override
        protected INotifierCSS styles() {
            return NotifierCSS.instance ();
        }

        /**
         * Clears all the currently showing notifiers.
         */
        public void clear() {
            notifiers.forEach ((n, e) -> n.remove ());
        }

        /**
         * Renders out the notifier.
         * 
         * @param notifier
         *                 the notifier.
         */
        public void render(Notifier notifier) {
            if (notifier == null)
                return;
            Element el = notifiers.get (notifier);
            if (el == null) {
                // Create a root element for the notifier and add to the
                // notifiers list.
                el = DomSupport.createDiv ();
                el.classList.add (styles ().notifier ());
                getRoot ().appendChild (el);
                notifiers.put (notifier, el);
            }

            // Render the contents of the notifier to its element.
            ITemplateBuilder.<Notifier>renderer ("Notifier_Item", root -> {
                root.p ().text (d -> d.text);
            }).render (el, notifier);
        }

        /**
         * Removes a notifier.
         * 
         * @param notifier
         *                 the notifier.
         */
        public void remove(Notifier notifier) {
            if (notifier == null)
                return;
            final Element el = notifiers.get (notifier);
            if (el == null)
                return;
            notifiers.remove (notifier);
            el.classList.add (styles ().hide ());

            // Delay removal of the element to allow animations to take effect.
            TimerSupport.timer (() -> el.remove (), 1000);
        }

    }

    public static interface INotifierCSS extends IComponentCSS {

            /**
             * An individual notifier.
             */
            public String notifier();

            /**
             * Applies the hide animation (prior to removal of the notifier).
             */
            public String hide();
        }

    /**
     * Component CSS (standard pattern).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        "com/effacy/jui/ui/client/Notifier.css",
        "com/effacy/jui/ui/client/Notifier_Override.css"
    })
    public static abstract class NotifierCSS implements INotifierCSS {

        private static NotifierCSS STYLES;

        public static INotifierCSS instance() {
            if (STYLES == null) {
                STYLES = (NotifierCSS) GWT.create (NotifierCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }
}
