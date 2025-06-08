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

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.gwtproject.safehtml.shared.SafeHtmlBuilder;

import com.effacy.jui.core.client.Debug;
import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.control.Control;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.UIEvent;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.ContainerBuilder;
import com.effacy.jui.core.client.dom.builder.Cpt;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.Input;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.store.IFilteredStore;
import com.effacy.jui.core.client.store.IPaginatedStore;
import com.effacy.jui.core.client.store.ISearchStore;
import com.effacy.jui.core.client.store.IStore;
import com.effacy.jui.core.client.store.ListStore;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.platform.util.client.TimerSupport;
import com.effacy.jui.ui.client.control.ISearchMenu.ISearchMenuConfig;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;
import elemental2.dom.HTMLInputElement;

public class TextSearchControl<S> extends Control<String, TextSearchControl.Config<S>> {

    /************************************************************************
     * Configuration and construction
     ************************************************************************/

     /**
      * The default style to employ when one is not assign explicitly.
      */
     public static Config.Style DEFAULT_STYLE = Config.Style.STANDARD;

    /**
     * Configuration for building a {@link TextControl}.
     */
    public static class Config<S> extends Control.Config<String, Config<S>> implements ISearchMenuConfig<S> {

        /********************************************************************
         * Styles for the tab set.
         ********************************************************************/

        /**
         * Style for the tab set (defines presentation configuration including CSS).
         */
        public interface Style {

            /**
             * The CSS styles.
             */
            public ILocalCSS styles();

            /**
             * Convenience to create a style.
             * 
             * @param styles
             *                     the CSS styles.
             * @param selectorIcon
             *                     the CSS class to use for the selector icon.
             * @return the associated style.
             */
            public static Style create(final ILocalCSS styles) {
                return new Style () {

                    @Override
                    public ILocalCSS styles() {
                        return styles;
                    }

                };
            }

            /**
             * Standard style.
             */
            public static final Style STANDARD = Style.create (StandardLocalCSS.instance ());

        }

        /**
         * The styles to apply to the tab set.
         */
        private Style style = (DEFAULT_STYLE != null) ? DEFAULT_STYLE : Style.STANDARD;

        /**
         * See {@link #max(int)}.
         */
        private int max = 0;

        /**
         * See {@link #password(boolean)}.
         */
        private boolean password = false;

        /**
         * See {@link #placeholder(String)}.
         */
        private String placeholder;

        /**
         * See {@link #iconLeft(String)}.
         */
        private String iconLeft;

        /**
         * See {@link #iconRight(String)}.
         */
        private String iconRight;

        /**
         * See {@link #clearAction(boolean)}.
         */
        private boolean clearAction;

        /**
         * See {@link #keyPressHandler(Function)}.
         */
        private BiFunction<String, String, Boolean> keyPressHandler;

        /**
         * See {@link #selectorMaxHeight(Length)}.
         */
        private Length selectorMaxHeight = Length.em (14);

        /**
         * See {@link #selectorHeight(Length)}.
         */
        private Length selectorHeight;

        /**
         * See {@link #selectorWidth(Length)}.
         */
        private Length selectorWidth;

        /**
         * See {@link #selectorLeft(boolean)}.
         */
        private boolean selectorLeft;

        /**
         * See {@link #selectorTop(boolean)}.
         */
        private boolean selectorTop;

        /**
         * See {@link #store(IStore)}.
         */
        private IStore<S> store = new ListStore<S> ();

        /**
         * See {@link #storeBatchSize(int)}.
         */
        private int storeBatchSize = 20;

        /**
         * See {@link #storeClear(boolean)}.
         */
        private boolean storeClear;

        /**
         * See {@link #overflowSafe(boolean)}.
         */
        private boolean overflowSafe;

        /**
         * See {@link #labelMapper(Function)}.
         */
        private Function<S, String> labelMapper = (s) -> (s == null) ? "" : s.toString();

        /**
         * See {@link #optionRenderer(BiConsumer)}.
         */
        @SuppressWarnings("rawtypes")
        private BiConsumer<ContainerBuilder,S> optionRenderer;

        /**
         * See {@link #selectorShowOnResults(boolean)}.
         */
        private boolean selectorShowOnResults = true;

        /**
         * See {@link #selectionHandler(boolean,Consumer)}.
         */
        private Consumer<S> selectionHandler;

        /**
         * See {@link #clearOnSelection(boolean,Consumer)}.
         */
        private boolean clearOnSelection;

        /**
         * See {@link #activateOnFocus(boolean)}.
         */
        private boolean activateOnFocus;

        /**
         * Construct with a default style.
         */
        public Config() {
            super ();
        }

        /**
         * Construct with a style.
         * 
         * @param style
         *              the style.
         */
        public Config(Style style) {
            style (style);
        }

        /**
         * Assigns a different style.
         * 
         * @param style
         *              the style.
         * @return this configuration.
         */
        public Config<S> style(Style style) {
            if (style != null)
                this.style = style;
            return this;
        }

        /**
         * Assigns the maximum number of characters the field can permit.
         * 
         * @param max
         *            the maximum (if 0 or less then no restriction is applied).
         * @return this configuration instance.
         */
        public Config<S> max(int max) {
            this.max = max;
            return this;
        }

        /**
         * See {@link #password(boolean)}. Convenience to pass {@code true}.
         * 
         * @return this configuration instance.
         */
        public Config<S> password() {
            return password (true);
        }

        /**
         * Marks the text input as occluded (such as when entering a password).
         * 
         * @param password
         *                 {@code true} if should be occluded.
         * @return this configuration instance.
         */
        public Config<S> password(boolean password) {
            this.password = password;
            return this;
        }

        /**
         * Assigns placeholder text to display when the field is empty.
         * 
         * @param placeholder
         *                    placeholder content to display.
         * @return this configuration instance.
         */
        public Config<S> placeholder(String placeholder) {
            this.placeholder = placeholder;
            return this;
        }

        /**
         * Places an icon on the left side of the field (inside the border).
         * 
         * @param iconLeft
         *                 icon CSS (i.e. {@link FontAwesome}).
         * @return this configuration instance.
         */
        public Config<S> iconLeft(String iconLeft) {
            this.iconLeft = iconLeft;
            return this;
        }

        /**
         * Places an icon on the right side of the field (inside the border).
         * 
         * @param iconRight
         *                  icon CSS (i.e. {@link FontAwesome}).
         * @return this configuration instance.
         */
        public Config<S> iconRight(String iconRight) {
            this.iconRight = iconRight;
            return this;
        }

        /**
         * See {@link #password(boolean)}. Convenience to pass {@code true}.
         * 
         * @return this configuration instance.
         */
        public Config<S> clearAction() {
            return clearAction (true);
        }

        /**
         * Whether or not a clear action should be displayed when the control has
         * contents.
         * 
         * @param clearAction
         *                    {@code true} if a clear action should be included.
         * @return this configuration instance.
         */
        public Config<S> clearAction(boolean clearAction) {
            this.clearAction = clearAction;
            return this;
        }

        /**
         * Registers a key press handler. This (if present) will receive the various key
         * presses and return whether to accept that key press or not. It is a way of
         * filtering out key presses (or simply to respond to specific keys, like the
         * enter key).
         * <p>
         * The function is passed the keycode and the key from the key event. If
         * accepted it must return {@code true}.
         * 
         * @param keyPressHandler
         *                        the handler.
         * @return this configuration instance.
         */
        public Config<S> keyPressHandler(BiFunction<String, String, Boolean> keyPressHandler) {
            this.keyPressHandler = keyPressHandler;
            return this;
        }

        /**
         * Assigns the maximum height of the selector.
         * 
         * @param selectorMaxHeight
         *                       the height of the selector.
         * @return this configuration instance.
         */
        public Config<S> selectorMaxHeight(Length selectorMaxHeight) {
            this.selectorMaxHeight = selectorMaxHeight;
            if (selectorMaxHeight != null)
                this.selectorHeight = null;
            return this;
        }

        /**
         * Assigns the height of the selector.
         * 
         * @param selectorHeight
         *                       the height of the selector.
         * @return this configuration instance.
         */
        public Config<S> selectorHeight(Length selectorHeight) {
            this.selectorHeight = selectorHeight;
            if (selectorHeight != null)
                this.selectorMaxHeight = null;
            return this;
        }

        /**
         * Assigns the width of the selector.
         * 
         * @param selectorWidth
         *                      the width of the selector (if {@code null} then will be
         *                      inherited from the control).
         * @return this configuration instance.
         */
        public Config<S> selectorWidth(Length selectorWidth) {
            this.selectorWidth = selectorWidth;
            return this;
        }

        /**
         * Determines if the selector should display above.
         * 
         * @param selectorTop
         *                    {@code true} to display above.
         * @return this configuration instance.
         */
        public Config<S> selectorTop(boolean selectorTop) {
            this.selectorTop = selectorTop;
            return this;
        }

        /**
         * See {@link #selectorTop(boolean)}. Convenience to pass {@code true}.
         * 
         * @return this configuration instance.
         */
        public Config<S> selectorTop() {
            return selectorTop (true);
        }

        /**
         * Determines if the selector should display with overhang to the left (right
         * aligned).
         * 
         * @param selectorLeft
         *                     {@code true} to display to the left.
         * @return this configuration instance.
         */
        public Config<S> selectorLeft(boolean selectorLeft) {
            this.selectorLeft = selectorLeft;
            return this;
        }

        /**
         * See {@link #selectorLeft(boolean)}. Convenience to pass {@code true}.
         * 
         * @return this configuration instance.
         */
        public Config<S> selectorLeft() {
            return selectorLeft (true);
        }

        /**
         * Assigns a store to use as the backing store for the control.
         * <p>
         * If the store implements {@link IFilteredStore} then (assuming the default
         * selector is used) the contents will be searchable using a
         * {@link Object#toString()} conversion of the object type (or one can override
         * {@link SelectionControl#filterMatch(String, Object)}).
         * <p>
         * If the store implements {@link ISearchStore} the (assuming the default
         * selector is used) the contents will be searchable against the store itself
         * (i.e. search keywords will be passed through to
         * {@link ISearchStore#filter(String)}).
         * 
         * @param store
         *              the store to use.
         * @return this configuration instance.
         */
        public Config<S> store(IStore<S> store) {
            if (store != null)
                this.store = store;
            return this;
        }

        /**
         * See {@link #store(IStore)}.
         */
        public IStore<S> getStore() {
            return store;
        }

        /**
         * For {@link IPaginatedStore} stores this is batch size to load pages of data
         * in (increments the page size by this amount).
         * 
         * @param storeBatchSize
         *                       the batch size to use when loading (default is 20).
         * @return this configuration instance.
         */
        public Config<S> storeBatchSize(int storeBatchSize) {
            this.storeBatchSize = Math.max (1, storeBatchSize);
            return this;
        }

        /**
         * See {@link #storeBatchSize(int)}.
         */
        public int getStoreBatchSize() {
            return storeBatchSize;
        }

        /**
         * For {@link IPaginatedStore} stores this clears the store on each opening of
         * the selector (forcing a reload).
         * 
         * @param storeClear
         *                   {@code true} if to perform a clear on each opening of the
         *                   selector.
         * @return this configuration instance.
         */
        public Config<S> storeClear(boolean storeClear) {
            this.storeClear = storeClear;
            return this;
        }

        /**
         * See {@link #storeClear(boolean)}.
         */
        public boolean isStoreClear() {
            return storeClear;
        }

        /**
         * This should be set when there is a risk of the control selector invoking
         * overflow (i.e. when used in a modal, which could result in obstruction of the
         * selector or induce scrolling of the modal contents).
         * <p>
         * If set this selector will be positioned using fixed semantics. However this
         * will result the selector being positioned fixed relative to the window so
         * will not move if the background scrolls.
         * 
         * @return this configuration instance.
         */
        public Config<S> overflowSafe() {
            this.overflowSafe = true;
            return this;
        }

        /**
         * See {@link #overflowSafe()}.
         */
        public boolean isOverflowSafe() {
            return this.overflowSafe;
        }

        /**
         * When the default store rendering is used this provides a convenience
         * mechanism to extract a label for an item.
         * 
         * @param labelMapper
         *                    the label mapper to use.
         * @return this configuration instance.
         */
        public Config<S> labelMapper(Function<S, String> labelMapper) {
            if (labelMapper != null)
                this.labelMapper = labelMapper;
            return this;
        }

        /**
         * Provides a renderer for options that appear in the selection selector.
         * 
         * @param optionRenderer
         *                       the renderer.
         * @return this configuration instance.
         */
        @SuppressWarnings("rawtypes")
        public Config<S> optionRenderer(BiConsumer<ContainerBuilder,S> optionRenderer) {
            this.optionRenderer = optionRenderer;
            return this;
        }

        /**
         * Determines if the selector should only display when there are matching
         * results (the default) or when values are in the text field.
         * 
         * @param selectorShowOnResults
         *                              {@code false} to show selector when typing in
         *                              the text field.
         * @return this configuration instance.
         */
        public Config<S> selectorShowOnResults(boolean selectorShowOnResults) {
            this.selectorShowOnResults = selectorShowOnResults;
            return this;
        }

        /**
         * Assigns a selection handler that is invoked when an item is selected from the
         * search menu.
         * <p>
         * Note that if {@code clearOnSelection} is passed as {@code true} then
         * {@link #selectorShowOnResults(boolean)} will be set to {@code false}.
         * 
         * @param clearOnSelection
         *                         {@code true} if the contents of the text field should
         *                         be cleared when something has been selected.
         * @param selectionHandler
         *                         the handler.
         * @return this configuration instance.
         */
        public Config<S> selectionHandler(boolean clearOnSelection, Consumer<S> selectionHandler) {
            this.selectionHandler = selectionHandler;
            this.clearOnSelection = clearOnSelection;
            if (this.clearOnSelection)
                this.selectorShowOnResults = false;
            return this;
        }

        /**
         * Activates search on focus. This effective initiates a search based on an
         * empty string.
         * 
         * @param activateOnFocus
         *                        {@code true} to do so.
         * @return this configuration instance.
         */
        public Config<S> activateOnFocus(boolean activateOnFocus) {
            this.activateOnFocus = activateOnFocus;
            return this;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.component.Component.Config#build(com.effacy.jui.core.client.component.layout.LayoutData[])
         */
        @Override
        @SuppressWarnings("unchecked")
        public TextSearchControl<S> build(LayoutData... data) {
            return build (new TextSearchControl<S> (this), data);
        }

    }

    /************************************************************************
     * Members
     ************************************************************************/

    /**
     * The selector for selection of items.
     */
    private ISearchMenu<S> selector;

    /************************************************************************
     * Debugging
     ************************************************************************/

    /**
     * Various debug modes.
     */
    public enum DebugMode {
        /**
         * Log rendering related activities.
         */
        STORE(1<<1),
        
        /**
         * Log menu selection events.
         */
        MENU(1<<2);

        /**
         * Bit flag for the specific debug mode.
         */
        private int flag;

        /**
         * Construct with initial data.
         */
        private DebugMode(int flag) {
            this.flag = flag;
        }

        /**
         * Determines if the flag is set.
         * 
         * @return {@code true} if it is.
         */
        public boolean set() {
            return ((TextSearchControl.DEBUG & flag) > 0);
        }
    }

    /**
     * Flag to toggle debug mode.
     */
    private static int DEBUG = 0;

    /**
     * Assigns the passed modes for debugging.
     * 
     * @param modes
     *              the modes.
     */
    public static void debug(DebugMode...modes) {
        DEBUG = 0;
        for (DebugMode mode : modes) {
            if (mode == null)
                continue;
            DEBUG |= mode.flag;
        }
    }

    /************************************************************************
     * Construction
     ************************************************************************/

    /**
     * Construct with configuration.
     * 
     * @param config
     *               the configuration.
     */
    public TextSearchControl(TextSearchControl.Config<S> config) {
        super (config);

        // Configure an instance of the selector.
        selector = createSelector ();
        selector.configureOnRender((cpt, el) -> {
            if (config ().selectorHeight != null)
                CSS.HEIGHT.apply (el, config ().selectorHeight);
            if (config ().selectorMaxHeight != null)
                CSS.MAX_HEIGHT.apply (el, config ().selectorMaxHeight);
            if (config ().selectorWidth != null)
                CSS.WIDTH.apply (el, config ().selectorWidth);
        });
    }

    /**
     * Creates the selector component for the control.
     * <p>
     * The default is an instance of {@link SelectorMenu}.
     * 
     * @return the selector.
     */
    protected ISearchMenu<S> createSelector() {
        return new SearchMenu<S> (config ()) {

            @SuppressWarnings("rawtypes")
            @Override
            protected void buildItem(ContainerBuilder el, S value) {
                if (TextSearchControl.this.config().optionRenderer != null)
                    TextSearchControl.this.config().optionRenderer.accept(el, value);
                else
                    TextSearchControl.this.buildRenderer (el, value);
            }

            @Override
            protected boolean filterMatch(String keywords, S value) {
                return TextSearchControl.this.filterMatch (keywords, value);
            }

            @Override
            protected void onSelect(S value) {
                if (TextSearchControl.this.config().selectionHandler != null)
                    TextSearchControl.this.config().selectionHandler.accept(value);
                if (TextSearchControl.this.config().clearOnSelection) {
                    inputEl.value = "";
                    TextSearchControl.this.hideSelector();
                } else
                    TextSearchControl.this.select (value);
            }

            @Override
            protected void onResults(boolean hasResults) {
                if (TextSearchControl.this.config().selectorShowOnResults) {
                    if (hasResults)
                        showSelector();
                    else
                        hideSelector();
                }
            }

        };
    }

    /**
     * Builds a renderer for the underlying value as it should appear in the list of
     * values that can be selected from.
     * <p>
     * The default is to perform a {@link Object#toString()} operation to map to the
     * desired value unless a mapper is configured (see
     * {@link Config#labelMapper(Function)}).
     * 
     * @param root
     *             the root container.
     */
    @SuppressWarnings("rawtypes")
    protected void buildRenderer(ContainerBuilder root, S value) {
        if (config ().labelMapper != null)
            Span.$ (root).text (config ().labelMapper.apply (value));
        else
            Span.$ (root).text ((value == null) ? "[NULL]" : value.toString ());
    }

    /**
     * Performs a keyword filtering to check if a value matches or not
     * 
     * @param keywords
     *                 the keywords to filter on.
     * @param value
     *                 the value to test.
     * @return {@code true} if there is a match.
     */
    protected boolean filterMatch(String keywords, S value) {
        if (value == null)
            return false;
        if (keywords == null)
            return false;
        String testValue = null;
        if (config ().labelMapper != null)
            testValue = config ().labelMapper.apply (value);
        else
            testValue = value.toString ();
        return testValue.toLowerCase ().contains (keywords.toLowerCase ());
    }

    /**
     * Internal action to select a value. This displays the item then hides the
     * selector while propagating a modified to the component controller.
     * 
     * @param value
     *              the value that was selected.
     */
    public void select(S value) {
        valueToSource (config ().labelMapper.apply (value));
        hideSelector ();
        focus ();
        modified ();
    }

    /**
     * Hides the selector.
     */
    public void hideSelector() {
        getRoot ().classList.remove (styles ().open ());
    }

    /**
     * Shows (and resets) the selector.
     * 
     * @return {@code true} if the selector was shown ({@code false} if it was
     *         alreadying showing).
     */
    public boolean showSelector() {
        boolean showing = getRoot ().classList.contains(styles().open());
        if (showing)
            return false;
        getRoot ().classList.add (styles ().open ());
        return true;
    }

    /************************************************************************
     * Behaviour
     ************************************************************************/

    @Override
    protected void onBlur() {
        super.onBlur();

        if (config().clearOnSelection)
            inputEl.value = "";
            
        // A bit of a delay here ensures any following events a properly handled.
        TimerSupport.timer (() -> {
            if (!isInFocus ())
                hideSelector ();
        }, 20);
    }

    @Override
    protected void onFocus() {
        super.onFocus();

        if (config().activateOnFocus) {
            if (showSelector())
                selector.reset();
            selector.search(inputEl.value);
        }
    }

    /**
     * The input element.
     */
    protected HTMLInputElement inputEl;

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.control.Control#prepareValueForAssignment(java.lang.Object)
     */
    @Override
    protected String prepareValueForAssignment(String value) {
        // This avoids the issue of the equivalence of null and the empty
        // string: convert null values to empty strings.
        return StringSupport.safe (super.prepareValueForAssignment (value));
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.control.Control#valueFromSource()
     */
    @Override
    public String valueFromSource() {
        String value = StringSupport.safe (inputEl.value);
        if (config ().clearAction) {
            if (StringSupport.empty (value))
                getRoot ().classList.remove (styles ().clear ());
            else
                getRoot ().classList.add (styles ().clear ());
        }
        return value;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.control.Control#valueToSource(java.lang.Object)
     */
    @Override
    public void valueToSource(String value) {
        if (config ().clearAction) {
            if (StringSupport.empty (value))
                getRoot ().classList.remove (styles ().clear ());
            else
                getRoot ().classList.add (styles ().clear ());
        }
        inputEl.value = StringSupport.safe (value);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#buildNode(com.effacy.jui.core.client.component.Component.Config)
     */
    @Override
    protected INodeProvider buildNode(Element el, Config<S> data) {
        return Wrap.$ (el).$ (
            Div.$ ().style (styles ().inner ()).$ (
                Em.$ ()
                    .style (styles ().read_only (), FontAwesome.lock ()),
                Em.$ ().iff (!StringSupport.empty (data.iconLeft))
                    .style (styles ().left (), data.iconLeft),
                Input.$ (data.password ? "password" : "text").$ (input -> {
                    input
                        .ref ("input")
                        .on (e -> {
                            // Search menu navigation and selection.
                            if (UIEvent.KeyCode.ENTER.is (e)) {
                                selector.select();
                                e.stopEvent();
                            } else if (UIEvent.KeyCode.ARROW_UP.is (e)) {
                                selector.up();
                                e.stopEvent();
                            } else if (UIEvent.KeyCode.ARROW_DOWN.is (e)) {
                                selector.down();
                                e.stopEvent();
                            }
                        }, UIEventType.ONKEYDOWN)
                        .on (e -> {
                            // Filtering key presses.
                            if (!filterKeyPress (e.getKeyCode (), e.getKey(), inputEl.value))
                                e.stopEvent ();
                        }, UIEventType.ONKEYPRESS)
                        .on (e -> {
                            // Filter out navigation keys.
                            if (UIEvent.KeyCode.ENTER.is (e) || UIEvent.KeyCode.ARROW_UP.is (e) || UIEvent.KeyCode.ARROW_DOWN.is (e) || UIEvent.KeyCode.ARROW_LEFT.is (e) || UIEvent.KeyCode.ARROW_RIGHT.is (e)) {
                                e.stopEvent();
                                return;
                            }
                            // Activate the selector.
                            if (!StringSupport.empty(inputEl.value)) {
                                if (!config().selectorShowOnResults) {
                                    if (showSelector())
                                        selector.reset();
                                }
                                selector.search(inputEl.value);
                            } else
                                hideSelector();
                        }, UIEventType.ONKEYUP, UIEventType.ONPASTE);
                    if (StringSupport.empty (data.getName ()))
                        input.attr ("name", "" + getUUID ());
                    else
                        input.attr ("name", data.getName ());
                    if (data.max > 0)
                        input.attr ("maxlength", "" + data.max);
                    if (!StringSupport.empty (data.placeholder))
                        input.attr ("placeholder", new SafeHtmlBuilder ().appendEscaped (data.placeholder));
                    input.testId (buildTestId ("input")).testRef ("input");
                }),
                Em.$ ().iff (data.clearAction)
                    .style (styles ().clear (), FontAwesome.times ())
                    .testId (buildTestId ("clear")).testRef ("clear")
                    .on (e -> {
                        inputEl.value = "";
                        modified ();
                    }, UIEventType.ONCLICK),
                Em.$ ().iff (!StringSupport.empty (data.iconRight))
                    .style (styles ().right (), data.iconRight)
            ),
            Div.$ ().$ (selector -> {
                if (Debug.isTestMode())
                    selector.attr ("test-ref", "selector");
                selector.id ("selector").by ("selector");
                if (data.selectorTop)
                    selector.style (styles ().selector_top ());
                if (data.selectorLeft)
                    selector.style (styles ().selector_left ());
                Cpt.$(selector, TextSearchControl.this.selector);
            })
        ).build (tree -> {
            // Register the input as the focus element (we only have one).
            inputEl = (HTMLInputElement) manageFocusEl (tree.first ("input"));
        });
    }

    /**
     * Processes a key press and determines if the change should go ahead.
     * 
     * @param keyCode
     *                the key code from the press.
     * @param key
     *                the key.
     * @param value
     *                the current value on the element.
     * @return {@code true} if the key passes.
     */
    protected boolean filterKeyPress(String keyCode, String key, String value) {
        if (config ().keyPressHandler != null) {
            Boolean response = config ().keyPressHandler.apply (keyCode, key);
            if ((response == null) || response)
                return true;
            return false;
        }
        return true;
    }

    /********************************************************************
     * CSS with standard styles.
     ********************************************************************/

    /**
     * Styles (made available to selection).
     */
    protected ILocalCSS styles() {
        return config ().style.styles ();
    }

    public static interface ILocalCSS extends IControlCSS {

        /**
         * Inner wrap around the control (for the border).
         */
        public String inner();

        /**
         * Left locality.
         */
        public String left();

        /**
         * Right locality.
         */
        public String right();

        /**
         * Clear action.
         */
        public String clear();

        /**
         * Open selector.
         */
        public String open();

        /**
         * Show the select above.
         */
        public String selector_top();

        /**
         * Show the select left aligned.
         */
        public String selector_left();

    }

    /**
     * Component CSS (horizontal).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        "com/effacy/jui/ui/client/control/Control.css",
        "com/effacy/jui/ui/client/control/TextSearchControl.css",
        "com/effacy/jui/ui/client/control/TextSearchControl_Override.css"
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
