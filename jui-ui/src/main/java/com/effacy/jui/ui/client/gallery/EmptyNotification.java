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
package com.effacy.jui.ui.client.gallery;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.effacy.jui.core.client.Invoker;
import com.effacy.jui.core.client.dom.UIEvent;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder;
import com.effacy.jui.core.client.util.UID;
import com.effacy.jui.platform.css.client.CssDeclaration;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.ui.client.button.IButtonHandler.IButtonActionCallback;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;

/**
 * Used by {@link Gallery} to display content when the gallery store is empty
 * (which can occur if filtering produces no results or there are not results to
 * start with).
 *
 * @author Jeremy Buckley
 */
public class EmptyNotification {

    /**
     * The contents of the ID for the action.
     */
    public static String ID_ACTION = "empty-notify-action";

    /**
     * The attribute that contains the action reference ID.
     */
    public static String ATTRIBUTE_ACTION = "item";

    /**
     * Captures an action that the user can perform.
     */
    public class Action {
        /**
         * Internal unique reference ID.
         */
        private String uid = UID.createUID ();

        /**
         * Display label for the action.
         */
        private String label;

        /**
         * Action handler.
         */
        private Invoker handler;

        /**
         * Construct an action.
         * 
         * @param label
         *                the display label.
         * @param handler
         *                the action handler.
         */
        public Action(String label, Invoker handler) {
            this.label = label;
            this.handler = handler;
        }
    }

    /**
     * A single panel for displaying contents. Allows for multiple panels.
     */
    public class Panel {

        /**
         * See {@link #title(String)}.
         */
        private String title;

        /**
         * See {@link #paragraph(String)}.
         */
        private List<String> paragraphs = new ArrayList<> ();

        /**
         * See {@link #action(String, IButtonActionCallback)}.
         */
        private List<Action> actions = new ArrayList<> ();

        /**
         * See {@link #actionsRightAligned(boolean)}.
         */
        private boolean actionsRightAligned = false;

        /**
         * Assigns a title to the panel.
         * 
         * @param title
         *              the title.
         * @return this panel instance.
         */
        public Panel title(String title) {
            this.title = title;
            return this;
        }

        /**
         * Adds a paragraph to the panel.
         * 
         * @param paragraph
         *                  the paragraph.
         * @return this panel instance.
         */
        public Panel paragraph(String paragraph) {
            this.paragraphs.add (paragraph);
            return this;
        }

        /**
         * Adds an action the user can perform to the panel.
         * 
         * @param label
         *                the display label for the action.
         * @param handler
         *                the action handler.
         * @return this panel instance.
         */
        public Panel action(String label, Invoker handler) {
            Action action = new Action (label, handler);
            actions.add (action);
            return this;
        }

        /**
         * Align the actions to the right.
         * 
         * @param actionsRightAligned
         *                            {@code true} to right align (otherwise will be
         *                            left aligned).
         * @return this panel instance.
         */
        public Panel actionsRightAligned(boolean actionsRightAligned) {
            this.actionsRightAligned = actionsRightAligned;
            return this;
        }

    }

    /**
     * See {@link #panel(Consumer<Pabel>)}.
     */
    private List<Panel> panels = new ArrayList<> ();
    
    public EmptyNotification() {}

    public EmptyNotification (Consumer<Panel> config) {
        panel (config);
    }
    
    /**
     * Adds a panel to the notification.
     * 
     * @param config
     *               (optional) to configure the panel.
     * @return this notification instance.
     */
    public EmptyNotification panel(Consumer<Panel> config) {
        Panel panel = panel ();
        if (config != null)
            config.accept (panel);
        return this;
    }

    /**
     * Adds a panel to the notification.
     * 
     * @return the panel.
     */
    public Panel panel() {
        Panel panel = new Panel ();
        panels.add (panel);
        return panel;
    }

    /**
     * Finds an action by its internal ID.
     * 
     * @param id
     *           the internal ID of an action.
     * @return the action (if found, otherwise {@code null}).
     */
    Action findAction(String id) {
        for (Panel panel : panels) {
            for (Action action : panel.actions) {
                if (action.uid.equals (id))
                    return action;
            }
        }
        return null;
    }

    /********************************************************************
     * Handlers
     ********************************************************************/

    /**
     * Attempts to locate an action by its (internally generated) ID.
     * 
     * @param actionId
     *                 the action reference ID.
     * @param cb
     *                 (optional) invocation callback (this will be passed to the
     *                 action handler, if no handler is declared it is completed).
     * @return {@code true} if the action was found.
     */
    public boolean handleAction(String actionId) {
        if (StringSupport.empty (actionId))
            return false;
        Action action = findAction (actionId);
        if (action == null)
            return false;
        if (action.handler != null)
            action.handler.invoke ();
        return true;
    }

    /**
     * Attempts to locate an action by its (internally generated) ID.
     * 
     * @param actionEvent
     *                    the action event to extract the action data from.
     * @param cb
     *                    (optional) invocation callback (this will be passed to the
     *                    action handler, if no handler is declared it is
     *                    completed).
     * @return {@code true} if the action was found.
     */
    public boolean handleAction(UIEvent actionEvent) {
        Element actionEl = actionEvent.getTarget ("a", 3);
        if (actionEl == null)
            return false;
        String actionId = actionEl.getAttribute (ATTRIBUTE_ACTION);
        return handleAction (actionId);
    }

    /**
     * Convenience to process an action event across collection of notifications
     * from which the event could have arisen.
     * 
     * @param actionEvent
     *                      the action event (see
     *                      {@link #handleAction(UIEvent, IButtonActionCallback)}).
     * @param cb
     *                      the callback (see
     *                      {@link #handleAction(UIEvent, IButtonActionCallback)}).
     * @param notifications
     *                      the notifications to search.
     * @return {@code true} if the action was found.
     */
    public static boolean handleAction(UIEvent actionEvent, IButtonActionCallback cb, EmptyNotification... notifications) {
        for (EmptyNotification notification : notifications) {
            if ((notification != null) && EmptyNotification.handleAction (actionEvent, cb))
                return true;
        }
        return false;
    }

    /********************************************************************
     * Rendering
     ********************************************************************/

    /**
     * See
     * {@link #buildPanel(com.effacy.gwt.core.client.dom.renderer.DOMDataRenderer.Element, Function, ILocalCSS)}
     * but uses the default styles.
     */
    public static <A> void buildPanel(TemplateBuilder.Element<A> parent, Function<A, EmptyNotification> data) {
        buildPanel (parent, data, StandardLocalCSS.instance ());
    }

    /**
     * Given a parent element this will render into that parent a collection of
     * DIV's for each panel in the {@link EmptyNotification} yielded by the data
     * function (which extracts from the parents data the notification that is being
     * rendered).
     * <p>
     * Once rendered one can mine for anchor tags with the id attribute
     * {@link #ID_ACTION}. The {@code item} attribute will then contain the
     * reference of the action (this is generated internally). The caller will need
     * to sink {@link UIEventType#ONCLICK} on those anchor elements and extract the
     * item from them. A call to
     * {@link #handleAction(String, IButtonActionCallback, EmptyNotification...)}
     * will map the action ID to the relevant action declared in the notification.
     * 
     * @param <A>
     *               the parent data type
     * @param parent
     *               the parent element to render the panels of the notification
     *               into.
     * @param data
     *               the data extractor mapping the parent data to the notification
     *               being rendered.
     * @param styles
     *               the styles source (cannot be {@code null}).
     */
    public static <A> void buildPanel(TemplateBuilder.Element<A> parent, Function<A, EmptyNotification> data, ILocalCSS styles) {
        parent.addClassName (styles.panels ());
        parent.container (data).div ().loop (c -> (c == null) ? new ArrayList<> () : c.panels, panel -> {
            panel.addClassName (styles.panel ());
            panel.h3 ().condition (v -> !StringSupport.empty (v.title)).text (v -> v.title);
            panel.p ().loop (v -> v.paragraphs, p -> p.text (u -> u));
            panel.div (actions -> {
                actions.condition (v -> !v.actions.isEmpty ());
                actions.addClassName (styles.actions ());
                actions.span (filler -> {
                    filler.condition (v -> v.actionsRightAligned);
                    filler.addClassName (styles.fill ());
                });
                actions.a ().loop (v -> v.actions, action -> {
                    action.text (u -> u.label);
                    action.id (EmptyNotification.ID_ACTION);
                    action.setAttribute (EmptyNotification.ATTRIBUTE_ACTION, u -> u.uid);
                });
            });
        });
    }

    /**
     * See
     * {@link #buildPanel(com.effacy.gwt.core.client.dom.renderer.DOMDataRenderer.Element, Function, ILocalCSS)}
     * but uses the default styles.
     */
    public static <A> void buildPanel(ElementBuilder parent, EmptyNotification data) {
        ILocalCSS styles = StandardLocalCSS.instance ();
        parent.addClassName (styles.panels ());
        for (Panel v : data.panels) {
            parent.div (panel -> {
                panel.addClassName (styles.panel ());
                if (!StringSupport.empty (v.title))
                    panel.h3 ().text (v.title);
                for (String p : v.paragraphs)
                    panel.p ().text (p);
                if (!v.actions.isEmpty ()) {
                    panel.div (actions -> {
                        actions.addClassName (styles.actions ());
                        if (v.actionsRightAligned)
                            actions.span ().addClassName (styles.fill ());
                        for (Action u : v.actions) {
                            actions.a (action -> {
                                action.text (u.label);
                                action.on (e -> {
                                    if (u.handler != null)
                                        u.handler.invoke ();
                                }, UIEventType.ONCLICK);
                                action.id (EmptyNotification.ID_ACTION);
                                action.setAttribute (EmptyNotification.ATTRIBUTE_ACTION, u.uid);
                            });
                        }
                    });
                }
            });
        }
    }

    /********************************************************************
     * CSS with standard styles.
     ********************************************************************/

    public static interface ILocalCSS extends CssDeclaration {

        /**
         * Envelops the collection of panels in notification.
         */
        public String panels();

        /**
         * Envelopes a single panel.
         */
        public String panel();

        /**
         * Envelopes a collection of actions.
         */
        public String actions();

        /**
         * Provides a filling to right-align actions.
         */
        public String fill();
    }

    /**
     * Component CSS (horizontal).
     */
    @CssResource({
        "com/effacy/jui/ui/client/gallery/EmptyNotification.css",
        "com/effacy/jui/ui/client/gallery/EmptyNotification_Override.css"
    })
    public static abstract class StandardLocalCSS implements ILocalCSS {

        private static StandardLocalCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (StandardLocalCSS) GWT.create (StandardLocalCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }
}
