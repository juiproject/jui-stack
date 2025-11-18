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
package com.effacy.jui.ui.client.navigation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.effacy.jui.core.client.Debug;
import com.effacy.jui.core.client.IActivateAware;
import com.effacy.jui.core.client.component.Component;
import com.effacy.jui.core.client.component.ComponentCreator;
import com.effacy.jui.core.client.component.IComponent;
import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.SimpleComponent;
import com.effacy.jui.core.client.component.layout.CardFitLayout;
import com.effacy.jui.core.client.component.layout.CardFitLayout.Config.Effect;
import com.effacy.jui.core.client.component.layout.ILayout.ActivateOutcome;
import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.builder.A;
import com.effacy.jui.core.client.dom.builder.Cpt;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.ExistingElementBuilder;
import com.effacy.jui.core.client.dom.builder.H2;
import com.effacy.jui.core.client.dom.builder.H3;
import com.effacy.jui.core.client.dom.builder.Header;
import com.effacy.jui.core.client.dom.builder.I;
import com.effacy.jui.core.client.dom.builder.NodeBuilder.NodeContext;
import com.effacy.jui.core.client.dom.builder.P;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.builder.Text;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.jquery.JQuery;
import com.effacy.jui.core.client.navigation.INavigationAware;
import com.effacy.jui.core.client.navigation.INavigationHandler;
import com.effacy.jui.core.client.navigation.INavigationHandler.NavigationContext;
import com.effacy.jui.core.client.navigation.INavigationHandlerProvider;
import com.effacy.jui.core.client.navigation.INavigationHandlerWithProvider;
import com.effacy.jui.core.client.navigation.INavigationResidualAware;
import com.effacy.jui.core.client.navigation.NavigationHandlerRouter;
import com.effacy.jui.core.client.navigation.NavigationHandlerRouter.RegistrationItem;
import com.effacy.jui.core.client.navigation.NavigationSupport;
import com.effacy.jui.core.client.util.TriConsumer;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.ListSupport;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.platform.util.client.Promise;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.effacy.jui.ui.client.navigation.CardNavigator.Config.CardConfiguration;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;

/**
 * This provides a means to navigate among sections that are represented
 * visually as cards.
 * <p>
 * Cards are configured with a label, optional icon and optional description and
 * are associated with a component. When the card is clicked on a navigation
 * event is generated that navigates to that card.
 * <p>
 * To display the top-level one simply navigates to an empty path. This is
 * interpreted as the top-level and displays the cards for navigation.
 * <p>
 * Components may implement {@link INavigationHandler} or
 * {@link INavigationHandlerProvider} in which case they will be injected into
 * the navigation hierarchy. Components may also implement
 * {@link INavigationAware} and {@link INavigationResidualAware} and will
 * recieve appropriate events.
 * <p>
 * Labels are generated from the label assigned to the card. However, if the
 * navigation context (see {@link NavigationContext}) has the navigation
 * property {@link #ATTR_CARDLABEL} then that will be used as the label of the
 * card being navigated to. This can be useful when using segmented cards and
 * the segmented card is dynamic in some sense (i.e. represents a specific
 * entity).
 * <p>
 * As noted there is the notion of a segmented card. This is a card that
 * represents a descendent node in the navigation hierarchy. For example,
 * suppose one has a card for editing users, with label "Users" and navigation
 * reference "users". Now suppose you want to edit a specific user in a separate
 * page. You could declare a segmented card "users/user". The card will not
 * render in the list of navigable cards but you can navigate to it by
 * navigating to "users/user". In this case the navigation sequence displays
 * will include the parent card "users" (there must be a parent card for this to
 * work). In this example you may want to specify the user to edit, maybe like
 * "users/user/23". In this case the component that is associated with the
 * segmented card "users/user" should implement
 * {@link INavigationResidualAware}. In this case it could extract the ID from
 * the residual and load the entity. Now you may want to display the name of the
 * user for this segmented card. If this is known ahead of time then you can use
 * the {@link #ATTR_CARDLABEL} metadata property to replace the default label
 * with the users name.
 */
public class CardNavigator extends Component<CardNavigator.Config> implements INavigationHandlerWithProvider {

    /**
     * See {@link Config#navigationHandler(IOnNavigationHandler)}.
     */
    @FunctionalInterface
    public interface IOnNavigationHandler {

        /**
         * Invoked to handle an internal navigation request (arising from the
         * breadcrumb).
         * <p>
         * This allows for re-direction of a navigation request based on additional
         * data.
         * 
         * @param current
         *                the current navigation path (of the active card that is).
         * @param target
         *                the target navigation path (to the desired card).
         * @return {@code true} if it stop the navigation event.
         */
        public boolean handle(String current, String target);
    }

    /**
     * Navigation context metadata property that contains the label to use for the
     * selected card. This will override any assigned against the card
     * configuration.
     */
    public static final String ATTR_CARDLABEL = "CARD_LABEL";

    /**
     * Navigation context metadata property that contains the icon to use for the
     * selected card. This will override any assigned against the card
     * configuration.
     */
    public static final String ATTR_CARDLABEL_ICON = "CARD_LABEL_ICON";

    /**
     * A unique object to represent the top-level navigation item (where the cards
     * are displayed).
     */
    private static final CardConfiguration TOP = new CardConfiguration();

    /**
     * Configuration for the panel.
     */
    public static class Config extends Component.Config {

        /**
         * Card configuration (this is a navigation item so delegates events through to
         * the component).
         */
        public static class CardConfiguration extends RegistrationItem implements Comparable<CardConfiguration> {

            /**
             * See constructor.
             */
            private String[] reference;
    
            /**
             * See {@link #label(Supplier)}.
             */
            private Supplier<String> label;

            /**
             * See {@link #icon(Supplier)}.
             */
            private Supplier<String> icon;

            /**
             * See {@link #description(String)}.
             */
            private String description;

            /**
             * See {@link #notice(String)}.
             */
            private String notice;

            /**
             * See {@link #attr(String, Object)}.
             */
            private Map<String,Object> metadata = new HashMap<>();

            /**
             * Empty card.
             */
            CardConfiguration() {
                super(null);
            }

            /**
             * Construct with reference and component.
             * 
             * @param reference
             *                  the reference (appears in the path).
             * @param component
             *                  the component (to be activated).
             */
            public CardConfiguration(String reference, IComponent component) {
                super(component);
                this.reference = reference.split("/");
            }

            /**
             * Assigns metadata to the card configuration.
             * 
             * @param name
             *              the metadata field.
             * @param value
             *              the value.
             * @return this configuration instance.
             */
            public CardConfiguration attr(String name, Object value) {
                metadata.put(name, value);
                return this;
            }

            /**
             * Obtains a metadata value.
             * 
             * @param name
             *             the metadata field.
             * @return the associated value (may be {@code null})
             */
            public Object attr(String name) {
                return metadata.get(name);
            }

            /**
             * Determines if the card is segmented (i.e. has a path leading to it).
             */
            public boolean segmented() {
                return (reference.length > 1);
            }

            /**
             * Assigns a display label.
             * 
             * @param label
             *              the label.
             * @return this configuration.
             */
            public CardConfiguration label(String label) {
                this.label = () -> label;
                return this;
            }

            /**
             * Assigns a display label.
             * 
             * @param label
             *              the label.
             * @return this configuration.
             */
            public CardConfiguration label(Supplier<String> label) {
                this.label = label;
                return this;
            }

            /**
             * Assigns an icon CSS class to render.
             * 
             * @param icon
             *              the icon CSS class.
             * @return this configuration.
             */
            public CardConfiguration icon(String icon) {
                return icon(() -> icon);
            }

            /**
             * Assigns an icon CSS class to render.
             * 
             * @param icon
             *              the icon CSS class.
             * @return this configuration.
             */
            public CardConfiguration icon(Supplier<String> icon) {
                this.icon = icon;
                return this;
            }

            /**
             * Assigns an notice (text) to display along with the navigation (i.e. "coming
             * soon").
             * 
             * @param notice
             *               the notice text.
             * @return this configuration.
             */
            public CardConfiguration notice(String notice) {
                this.notice = notice;
                return this;
            }

            /**
             * Assigns a description to render along with the label.
             * 
             * @param description
             *              the supporting description.
             * @return this configuration.
             */
            public CardConfiguration description(String description) {
                this.description = description;
                return this;
            }

            /**
             * The references.
             * 
             * @return the references.
             */
            public String[] reference() {
                return reference;
            }

            /**
             * Obtains a suitable label to display.
             *
             * @return the label.
             */
            public String label() {
                return (label == null) ? reference[reference.length - 1] : label.get();
            }

            /**
             * See {@link #icon(String)}.
             *
             * @return the icon supplier.
             */
            public Supplier<String> icon() {
                return icon;
            }

            /**
             * See {@link #description(String)}.
             *
             * @return the description.
             */
            public String description() {
                return description;
            }

            /**
             * See {@link #notice(String)}.
             *
             * @return the notice.
             */
            public String notice() {
                return notice;
            }

            /**
             * Determines if this is a prefix of the passed card (a match is deemed a prefix).
             * 
             * @param card
             *             the card to test.
             * @return {@code true} if it is a prefix.
             */
            public boolean prefixOf(CardConfiguration card) {
                if (card == null)
                    return false;
                if (reference.length > card.reference.length)
                    return false;
                for (int i = 0; i < reference.length; i++) {
                    if (!reference[i].equals(card.reference[i]))
                        return false;
                }
                return true;
            }

            @Override
            public int compareTo(CardConfiguration o) {
                if (o == null)
                    return -1;
                return _compareTo(o, 0);
            }

            /**
             * Used by {@link #compareTo(CardConfiguration)} and performs a recursive
             * comparison.
             */
            protected int _compareTo(CardConfiguration o, int depth) {
                if (this.reference.length <= depth) {
                    if (o.reference.length <= depth)
                        return 0;
                    return 1;
                }
                if (o.reference.length <= depth)
                    return -1;
                if (this.reference[depth].equals(o.reference[depth]))
                    return _compareTo(o, depth + 1);
                return this.reference[depth].compareTo(o.reference[depth]);
            }


        }

        /**
         * Style direction for the component.
         */
        public interface Style {

            /**
             * The CSS styles.
             * 
             * @return the styles.
             */
            public ILocalCSS styles();

            /**
             * To include the active page in the breadcrumb.
             */
            public boolean includeActiveInCrumb();

            /**
             * Convenience to create a styles instance from the given data.
             * 
             * @param styles
             *               the styles.
             * @return the style instance.
             */
            public static Style create(ILocalCSS styles, boolean includeActiveInCrumb) {
                return new Style () {

                    @Override
                    public ILocalCSS styles() {
                        return styles;
                    }

                    @Override
                    public boolean includeActiveInCrumb() {
                        return includeActiveInCrumb;
                    }
                };
            }

            /**
             * Normal visual style.
             * <p>
             * This has a breadcrumb at the top starting with the title but stopping short
             * of the current page. The current page appears below the breadcrumb and in a
             * larger font with a clearly differentiated back action.
             */
            public static final Style STANDARD = create (StandardCSS.instance (), false);

            /**
             * Extended visual style.
             * <p>
             * This is the same as {@link #STANDARD} except that the breadcrumb terminates
             * with the current page (i.e. the trail is full). The current page still
             * appears under the trail as per {@link #STANDARD}.
             */
            public static final Style EXTENDED = create (ExtendedCSS.instance (), true);

            /**
             * Compact visual style.
             * <p>
             * Here there is only a breadcrumb with the current page appearing at the end.
             * This is differentiated from the trail by being in a larger font.
             */
            public static final Style COMPACT = create (CompactCSS.instance (), true);
        }

        /**
         * See {@link #style(Style)}.
         */
        private Style style = Style.STANDARD;

        /**
         * See {@link #title(String)}.
         */
        private String title;

        /**
         * See {@link #title(String, String)}.
         */
        private String titleIcon;

        /**
         * See {@link #titleOnlyInBreadcrumb(boolean)}.
         */
        private boolean titleOnlyInBreadcrumb;

        /**
         * See {@link #effect(Effect)}.
         */
        private Effect effect;

        /**
         * See {@link #card(String, IComponent, Consumer)}.
         */
        private List<CardConfiguration> cards = new ArrayList<>();

        /**
         * See {@link #navigationHandler(Function)}.
         */
        private IOnNavigationHandler navigationHandler;

        /**
         * Assigns a presentation style.
         * 
         * @param style
         *              the style (default is {@link Style#NORMAL}).
         * @return this configuration instance.
         */
        public Config style(Style style) {
            if (style != null)
                this.style = style;
            return this;
        }

        /**
         * Assigns a display title for the card navigation. This appears as the top-level label.
         * 
         * @param title
         *              the title.
         * @return this configuration instance.
         */
        public Config title(String title) {
            this.title = title;
            return this;
        }

        /**
         * Assigns a display title for the card navigation. This appears as the
         * top-level label.
         * 
         * @param title
         *                  the title.
         * @param titleIcon
         *                  the CSS for the back icon to use.
         * @return this configuration instance.
         */
        public Config title(String title, String titleIcon) {
            this.title = title;
            this.titleIcon = titleIcon;
            return this;
        }

        /**
         * The title normally displays at the top of the main page and then in the
         * breadcrumb trail. This restricts the use of the title only in the latter
         * (this is genarally used when a custom navigator is supplied).
         * 
         * @param titleOnlyInBreadcrumb
         *                              {@code true} to limit its use to the breadcrumb
         *                              trail.
         * @return this configuration instance.
         */
        public Config titleOnlyInBreadcrumb(boolean titleOnlyInBreadcrumb) {
            this.titleOnlyInBreadcrumb = titleOnlyInBreadcrumb;
            return this;
        }

        /**
         * Determines if a transiation effect should be employed.
         * 
         * @param effect
         *               the transition effect to apply.
         * @return this configuration instance.
         */
        public Config effect(Effect effect) {
            this.effect = effect;
            return this;
        }

        /**
         * Declare a card. This needs to have a reference (for the navigation path) and
         * a component (which is displayed). Also passed is a lambda-expression that can
         * further configure the card (i.e. providing a label).
         * <p>
         * For a segmented (deep child) card express the reference as a path (i.e.
         * "users/user").
         * 
         * @param reference
         *                  the navigation reference.
         * @param component
         *                  the component to display.
         * @param config
         *                  to configure the card.
         * @return this configuration.
         */
        public Config card(String reference, IComponent component, Consumer<CardConfiguration> config) {
            CardConfiguration cfg = new CardConfiguration(reference, component);
            if (config != null)
                config.accept(cfg);
            cards.add(cfg);
            return this;
        }

        /**
         * Hook into the navigation system when navigation is invoked from the
         * breadcrumb trail. Allows for alternative navigation to be applied.
         * <p>
         * Note that this is not triggered by a programmatic navigation request, only
         * from the UI.
         * 
         * @param navigationHandler
         *                          the handler (returns {@code true} if navigation
         *                          should be
         *                          stopped).
         * @return this configuration.
         */
        public Config navigationHandler(IOnNavigationHandler navigationHandler) {
            this.navigationHandler = navigationHandler;
            return this;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.component.Component.Config#build(com.effacy.jui.core.client.component.layout.LayoutData[])
         */
        @Override
        @SuppressWarnings("unchecked")
        public CardNavigator build(LayoutData... data) {
            return (CardNavigator) build (new CardNavigator (this), data);
        }

        /**
         * See {@link #style(Style)}.
         *
         * @return the style.
         */
        public Style style() {
            return style;
        }

        /**
         * See {@link #title(String)}.
         *
         * @return the title.
         */
        public String title() {
            return title;
        }

        /**
         * See {@link #titleOnlyInBreadcrumb(boolean)}.
         *
         * @return the flag value.
         */
        public boolean titleOnlyInBreadcrumb() {
            return titleOnlyInBreadcrumb;
        }

        /**
         * See {@link #effect(Effect)}.
         *
         * @return the effect.
         */
        public Effect effect() {
            return effect;
        }

        /**
         * See {@link #card(String, IComponent, Consumer)}.
         *
         * @return the cards.
         */
        public List<CardConfiguration> cards() {
            return cards;
        }

        /**
         * See {@link #navigationHandler(Function)}.
         *
         * @return the navigation handler.
         */
        public IOnNavigationHandler navigationHandler() {
            return navigationHandler;
        }

        /**
         * Used by {@link #card(List)} to maintain a separately sorted list of cards for
         * resolving depth.
         */
        private List<CardConfiguration> sorted = new ArrayList<>();

        /**
         * Locates the card that matches the given reference sequence (where there is
         * more than one reference then the card is necessarily segmented).
         *
         * @param ref
         *            the reference sequence to match against.
         * @return the matching card.
         */
        public CardConfiguration card(List<String> ref) {
            if (sorted.isEmpty()) {
                sorted.addAll(cards);
                Collections.sort(sorted);
            }

            // Loop over the cards to determine a match. The first, and longest, match
            // counts.
            LOOP: for (CardConfiguration card : sorted) {
                for (int i = 0; i < card.reference.length; i++) {
                    if (ref.size() <= i)
                        continue LOOP;
                    if (!ref.get(i).equals(card.reference[i]))
                        continue LOOP;
                }
                return card;
            }
            return null;
        }

        /**
         * Obtains the sequence of cards leading to the given one.
         */
        public List<CardConfiguration> path(CardConfiguration card) {
            if (sorted.isEmpty()) {
                sorted.addAll(cards);
                Collections.sort(sorted);
            }
            List<CardConfiguration> path = new ArrayList<>();
            sorted.forEach(c -> {
                if (c.prefixOf(card))
                    path.add (c);
            });
            Collections.reverse(path);
            return path;
        }

    }

    /**
     * Region containing the components being managed.
     */
    private static final String REGION_BODY = "body";

    /************************************************************************
     * Member variables.
     ************************************************************************/

    /**
     * The header element that contains the breadcrumbs.
     */
    private Element headerEl;

    /**
     * An alternative navigator (than the default use of cards).
     */
    private IComponent navigatorCpt;

    /************************************************************************
     * Construction
     ************************************************************************/

    /**
     * Construct with defaults.
     */
    public CardNavigator() {
        this (new Config ());
    }

    /**
     * Construct with configuration.
     * 
     * @param config
     *               the configuration.
     */
    public CardNavigator(Config config) {
        super (config);
    }

    /**
     * Assigns a custom navigator (rather than using the default card layout).
     * <p>
     * A mechanism needs to be provided to allow the passed component to invoke
     * navigation events on the card navigator (see {@link #navigate(String)}). This
     * can be achieved easily with an inline-component (simply by calling the method
     * directly, see also {@link #renderer(Consumer, Consumer)}) otherwise you can
     * use a callback that invokes the navigation mechanism (or similar).
     * 
     * @param navigator
     *                  the navigator.
     */
    protected void navigator(IComponent navigator) {
        this.navigatorCpt = navigator;
    }

    /**
     * See {@link #renderer(Consumer, Consumer)} but without an <code>onbuild</code>
     * parameter.
     */
    protected void renderer(Consumer<ExistingElementBuilder> builder) {
        renderer (builder, null);
    }

    /**
     * This is made to look a bit like the same method on {@link SimpleComponent}
     * however it creates a component using the passed builder (and onbuild) then
     * sets that component as the {@link #navigator(IComponent)}. This is an
     * important distinction as it is not providing an alternative to build node
     * (but does mean you can more easily switch between this and
     * {@link SimpleComponent}).
     * <p>
     * This is not a very "correct" approach as it exposes the internals of the
     * resultant component violating the principle of encapsulation (note that DOM
     * builder declared events will be properly handled but one needs to keep in
     * mind that events arising from this component are directed to the component
     * and not the parent). However, this component is generated uniquely to the
     * instance and so is entirely composite to the instance.
     * 
     * @param builder
     *                to build out the component root element.
     * @param onbuild
     *                (optional) will be invoked post-build to allow for element
     *                extraction.
     */
    protected void renderer(Consumer<ExistingElementBuilder> builder, Consumer<NodeContext> onbuild) {
        navigator (ComponentCreator.build (builder, onbuild));
    }

    /************************************************************************
     * Navigation
     ************************************************************************/

     /**
     * Underlying navigation handler.
     */
    private NavigationHandlerRouter navigationRouter = new NavigationHandlerRouter () {

        @Override
        protected void onNavigationForward(NavigationContext context, List<String> path, TriConsumer<NavigationContext, List<String>, Object> propagator) {
            // On forward set the active card to null (default) and attempt to resolve based
            // on the path. If resolved remove the references from the path (there could be
            // more than one).
            CardConfiguration activeCard = null;
            List<String> childPath = NavigationSupport.copy (path);
            if ((path != null) && !path.isEmpty()) {
                activeCard = config().card(path);
                if (activeCard == null)
                    return;
                childPath = childPath.subList(activeCard.reference.length, childPath.size());
            }

            // Use TOP for the top-level (when there is no child path).
            propagator.accept (context, childPath, (activeCard != null) ? activeCard : TOP);
        }

        @Override
        protected Promise<ActivateOutcome> onChildActivated(NavigationContext context, Object child) {
            // Handle the special case of the top element.
            if (TOP == child) {
                getRoot().classList.remove(styles().body());
                if (config().titleOnlyInBreadcrumb) {
                    JQuery.$ (headerEl).hide ();
                } else {
                    buildInto(headerEl, header -> {
                        H2.$ (header).text (config().title);
                    });
                    JQuery.$ (headerEl).show ();
                }
                if (navigatorCpt instanceof INavigationAware)
                    ((INavigationAware) navigatorCpt).onNavigateTo (context);
                if (navigatorCpt instanceof IActivateAware)
                    ((IActivateAware) navigatorCpt).onAnyActivation ();
                return Promise.create (ActivateOutcome.ACTIVATED);
            }

            // Generate the header.
            CardConfiguration card = (CardConfiguration) child;
            List<CardConfiguration> path = config().path(card);
            buildInto(headerEl, header -> {
                buildBreadcrumb (header, context, path);
            });
            JQuery.$ (headerEl).show();

            // Mark on the root the current state for testing.
            if (Debug.isTestMode ())
                getRoot().setAttribute("test-state", card.reference[0]);
            
            // Activate the child component in the body.
            getRoot().classList.add(styles().body());
            Promise<ActivateOutcome> promise = Promise.create ();
            IComponent cpt = card.component ();
            ((CardFitLayout) findRegionPoint (REGION_BODY).getLayout ()).activate (cpt).onFulfillment (v -> promise.fulfill (v));
            return promise;
        }

        @Override
        protected void onNavigationBackward(NavigationContext context, List<String> path, Consumer<List<String>> propagator) {
            // Normally the path will have the card reference path included. However, if the
            // component is a navigable itself this seems to get lost. Not sure why that is
            // the case so should look into the underlying cause. For now this will put the
            // prefix in if it is not found. This is not perfect as if the prefix is
            // reflected in the sub-navigation then that will appear as correct when it is
            // not. This is not a high-likely scenario and not a big stopper as references
            // can easily be changed.
            CardConfiguration card = (CardConfiguration) activeChild();
            if ((path != null) && (card != null) && (card.reference != null) && (card.reference.length > 0)) {
                if (path.isEmpty() || !card.reference[0].equals(path.get(0)))
                    for (int i = card.reference.length - 1; i >= 0; i--)
                    path.add(0, card.reference[i]);
            }
            super.onNavigationBackward(context, path, propagator);
            onAfterNavigate(path);
        }
    };
    
    /**
     * Convenience to report the current navigation path.
     * 
     * @param path
     *             the path.
     */
    protected void onAfterNavigate(List<String> path) {
        // Nothing.
    }

    protected void buildBreadcrumb(ElementBuilder header, NavigationContext context, List<CardConfiguration> path) {
        CardConfiguration card = path.get(path.size() - 1);
        String cardLabel = context.getMetadata(CardNavigator.ATTR_CARDLABEL, card.label());
        boolean displayTitle = !StringSupport.empty(config().title);
        Div.$ (header).style(styles().crumb ()).$(crumb -> {
            if (displayTitle) {
                Span.$ (crumb).style (styles ().clickable ()).$ (
                    Em.$ ().style(!StringSupport.empty(config().titleIcon) ? config().titleIcon : FontAwesome.arrowLeft ())
                ).onclick (e -> {
                    if (config().navigationHandler != null) {
                        if (config().navigationHandler.handle(NavigationSupport.build(path.get(path.size()-1).reference), "/"))
                            return;
                    }
                    navigate (new NavigationContext (NavigationContext.Source.INTERNAL, false));
                }).text (config().title);
            }
            ListSupport.forEach (path, (ctx, c) -> {
                if (ctx.last()) {
                    if (config().style.includeActiveInCrumb()) {
                        if (displayTitle || !ctx.first())
                            Em.$ (crumb).style (FontAwesome.chevronRight ());
                        Span.$ (crumb).use (n -> breadcrumbLabelEl = (Element) n).$ (span -> {
                            Text.$ (span, cardLabel);
                            if (!StringSupport.empty(card.notice))
                                Span.$ (span).style (styles ().notice ()).text (card.notice);
                        });
                    }
                } else {
                    if (displayTitle || !ctx.first())
                        Em.$ (crumb).style (FontAwesome.chevronRight ());
                    Span.$ (crumb).style (styles ().clickable ()).onclick (e -> {
                        if (config().navigationHandler != null) {
                            if (config().navigationHandler.handle(NavigationSupport.build(path.get(path.size()-1).reference), NavigationSupport.build(c.reference)))
                                return;
                        }
                        navigate (buildNavigationContext(), c.reference);
                    }).text (c.label());
                }
            });
        });
        H2.$ (header).$ (h2 -> {
            A.$ (h2).$ (
                Em.$ ().style(FontAwesome.chevronLeft ())
            ).onclick(e -> {
                if (config().navigationHandler != null) {
                    if (config().navigationHandler.handle(NavigationSupport.build(path.get(path.size()-1).reference), (path.size() <= 1) ? "/" : NavigationSupport.build (path.get(path.size() - 2).reference)))
                        return;
                }
                if (path.size() <= 1)
                    navigate (buildNavigationContext());
                else
                    navigate (buildNavigationContext(), path.get(path.size() - 2).reference);
            });
            Span.$ (h2)
                .use (n -> headerLabelEl = (Element) n)
                .text (cardLabel);
            if (context.hasMetadata(ATTR_CARDLABEL_ICON)) {
                I.$(h2).style ((String) context.getMetadata(ATTR_CARDLABEL_ICON));
            } else {
                String icon = (card.icon == null) ? null : card.icon.get();
                if (icon != null)
                    I.$(h2).style (icon);
            }
        });
    }

    /**
     * Context meta-data.
     */
    public static final String BREADCRUMB = "breadcrumb";

    /**
     * Builds a navigation context when navigation has been invoked from the
     * breadcrumb trail.
     * <p>
     * The default adds the meta-data {@link #BREADCRUMB} set to {@code true} to
     * indicate that this has originated from the breadcrumb.
     * 
     * @return the navigation context.
     */
    protected NavigationContext buildNavigationContext() {
        return new NavigationContext (NavigationContext.Source.INTERNAL, false).metadata(BREADCRUMB, true);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.navigation.INavigationHandlerProvider#handler()
     */
    @Override
    public INavigationHandler handler() {
        return navigationRouter;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.navigation.INavigationHandlerProvider#navigate(NavigationContext, List)
     */
    @Override
    public void navigate(NavigationContext context, List<String> path) {
        if (context == null)
            context = new NavigationContext();
        
        // Empty case we just delegate through (which will activate top).
        if ((path == null) || path.isEmpty()) {
            navigationRouter.navigate (context, path);
            return;
        }

        // See note on activeCard. Since we are tracking the active card manually we
        // need to prepend its reference to this path so we properly construct the
        // desired path relative to the top (which is the empty path).
        List<String> pathCpts = new ArrayList<>(path);
        if (context.isRelative() && (navigationRouter.activeChild () != null)) {
            CardConfiguration card = (CardConfiguration) navigationRouter.activeChild ();
            if (card != TOP) {
                for (int i = card.reference.length - 1; i >=0; i--)
                    pathCpts.add(0, card.reference[i]);
            }
        }
        navigationRouter.navigate (context.relative(false), pathCpts);
    }

    /************************************************************************
     * Rendering
     ************************************************************************/

     /**
      * Holds the label to the last element in the breadcrumb trail.
      */
    protected Element breadcrumbLabelEl; 

    /**
     * Holds the label for the current page.
     */
    protected Element headerLabelEl; 

    /**
     * For the current navigation path update the label of the target page.
     * <p>
     * This is really a convenience to cater for cases where the label is dependent
     * on some content (i.e. a record being rendered) and the context changes (i.e.
     * the record is updated).
     * 
     * @param label
     *              the revised label.
     */
    public void updateCurrentLabel(String label) {
        if  (breadcrumbLabelEl != null)
            breadcrumbLabelEl.textContent = label;
        if  (headerLabelEl != null)
            headerLabelEl.textContent = label;
    }

    @Override
    protected void onAfterRender() {
        super.onAfterRender();

        // Hide the header if there is no title.
        if (config().titleOnlyInBreadcrumb)
            JQuery.$ (headerEl).hide ();

        // Add the cards into the card-layout region. We also register the card (since
        // it extends RegistrationItem). This means the component contained within can
        // participate in the navigation hierarchy.
        config ().cards.forEach (card -> {
            findRegionPoint (REGION_BODY).add (card.component ());
            navigationRouter.register (card);
        });
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#buildNode(elemental2.dom.Element,
     *      com.effacy.jui.core.client.component.Component.Config)
     */
    @Override
    protected INodeProvider buildNode(Element el, Config data) {
        return Wrap.$ (el).$ (root -> {
            Div.$ (root).style (styles ().wrap ()).$ (wrap -> {
                Header.$ (wrap).$ (header -> {
                    header.style (styles ().header ());
                    header.by ("header");
                    H2.$ (header).text (config().title);
                });
                Div.$ (wrap).$ (body -> {
                    body.style (styles ().body ());
                    body.use (region (REGION_BODY, new CardFitLayout.Config (true).effect (config ().effect).build ()));
                });
                List<CardConfiguration> cards = new ArrayList<> (config().cards);
                cards.removeIf(v -> v.segmented());
                buildNavigator (wrap, cards);
            });
        }).build (dom -> {
            headerEl = dom.first("header");
        });
    }

    /**
     * Called by {@link #buildNode(Element, Config)} to render out the navigator
     * (the default being the set of cards). Note that only top-level
     * (non-segmented) cards are passed.
     * 
     * @param target
     *               the target builder to build into.
     * @param cards
     *               the cards to render.
     */
    protected void buildNavigator(ElementBuilder target, List<CardConfiguration> cards) {
        Div.$ (target).$ (wrapper -> {
            wrapper.style (styles ().navigator ());
            if (navigatorCpt != null) {
                wrapper.style (styles ().custom ());
                Cpt.$ (wrapper, navigatorCpt);
            } else {
                Div.$ (wrapper).style(styles().wrap()).$ (inner -> {
                    cards.forEach(card -> {
                        buildCard(inner, card);
                    });
                });
            }
        });
    }

    /**
     * Called by {@link #buildNavigator(ElementBuilder, List)} to render a single card.
     * 
     * @param target
     *               the target to render the card into.
     * @param card
     *               the card to render.
     */
    protected void buildCard(ElementBuilder target, CardConfiguration card) {
        Div.$(target).onclick(e -> {
            // Go direct to the router so that we bypass prepending the current active card.
            navigationRouter.navigate (new NavigationContext (NavigationContext.Source.INTERNAL, false), card.reference);
        }).$ (
            H3.$().text (card.label ()),
            P.$ ().iff (!StringSupport.empty (card.description)).text(card.description)
        );
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#styles()
     */
    @Override
    protected ILocalCSS styles() {
        return config().style.styles();
    }

    public static interface ILocalCSS extends IComponentCSS {

        public String wrap();

        public String header();

        public String crumb();

        public String body();

        public String navigator();

        public String custom();

        public String clickable();

        public String notice();
    }

    /**
     * Component CSS (standard pattern).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        "com/effacy/jui/ui/client/navigation/CardNavigator.css",
        "com/effacy/jui/ui/client/navigation/CardNavigator_Override.css"
    })
    public static abstract class StandardCSS implements ILocalCSS {

        private static StandardCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (StandardCSS) GWT.create (StandardCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }

    /**
     * Component CSS (standard pattern).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        "com/effacy/jui/ui/client/navigation/CardNavigator.css",
        "com/effacy/jui/ui/client/navigation/CardNavigator_Override.css"
    })
    public static abstract class ExtendedCSS implements ILocalCSS {

        private static ExtendedCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (ExtendedCSS) GWT.create (ExtendedCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }

    /**
     * Component CSS (compact pattern).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        "com/effacy/jui/ui/client/navigation/CardNavigator.css",
        "com/effacy/jui/ui/client/navigation/CardNavigator_Compact.css",
        "com/effacy/jui/ui/client/navigation/CardNavigator_Compact_Override.css"
    })
    public static abstract class CompactCSS implements ILocalCSS {

        private static CompactCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (CompactCSS) GWT.create (CompactCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }

}
