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

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import com.effacy.jui.core.client.Debug;
import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.IFocusBlurListener;
import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.control.Control;
import com.effacy.jui.core.client.dom.DomSupport;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.ContainerBuilder;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.DomBuilder;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Container;
import com.effacy.jui.core.client.store.IFilteredStore;
import com.effacy.jui.core.client.store.IPaginatedStore;
import com.effacy.jui.core.client.store.ISearchStore;
import com.effacy.jui.core.client.store.IStore;
import com.effacy.jui.core.client.store.IStoreLoadingListener;
import com.effacy.jui.core.client.store.ListStore;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.platform.util.client.TimerSupport;
import com.effacy.jui.ui.client.control.ISelectorMenu.ISelectorMenuConfig;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;
import elemental2.dom.HTMLElement;

/**
 * A control for displaying a drop-list of items to choose from and allows the
 * user to select one of these items. Support is provided for large selection
 * sets via user search (keyword) and store-backed pagination.
 * <p>
 * The value type of the control can be almost anything but does need to
 * translate to something renderable (for selection). This can be achieved one
 * of two ways: providing a means to convert to a human-readable string (see
 * {@link Config#labelMapper(Function)}) or to override and provide a be-spoke
 * renderer (see {@link #buildRenderer(Container)}).
 *
 * @author Jeremy Buckley
 */
public class SelectionControl<V> extends Control<V, SelectionControl.Config<V>> {

    /************************************************************************
     * Configuration
     ************************************************************************/

    /**
     * The default style to employ when one is not assign explicitly.
     */
    public static Config.Style DEFAULT_STYLE = Config.Style.STANDARD;

    /**
     * Configuration for building a {@link SelectionControl}.
     */
    public static class Config<V> extends Control.Config<V, Config<V>> implements ISelectorMenuConfig<V> {

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
             * The CSS class to use for the selector icon.
             */
            public String selectorIcon();

            /**
             * Convenience to create a style.
             * 
             * @param styles
             *                     the CSS styles.
             * @param selectorIcon
             *                     the CSS class to use for the selector icon.
             * @return the associated style.
             */
            public static Style create(final ILocalCSS styles, final String selectorIcon) {
                return new Style () {

                    @Override
                    public ILocalCSS styles() {
                        return styles;
                    }

                    /**
                     * {@inheritDoc}
                     *
                     * @see com.effacy.jui.ui.client.control.TextControl.Config.Style#selectorIcon()
                     */
                    @Override
                    public String selectorIcon() {
                        return selectorIcon;
                    }

                };
            }

            /**
             * Standard style.
             */
            public static final Style STANDARD = Style.create (StandardLocalCSS.instance (), FontAwesome.angleDown ());

        }

        /**
         * The styles to apply to the tab set.
         */
        protected Style style = (DEFAULT_STYLE != null) ? DEFAULT_STYLE : Style.STANDARD;

        /**
         * See {@link #placeholder(String)}.
         */
        private String placeholder;

        /**
         * See {@link #allowEmpty(boolean)}.
         */
        private boolean allowEmpty;

        /**
         * See {@link #allowSearch(boolean)}.
         */
        private boolean allowSearch = true;

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
        private IStore<V> store = new ListStore<V> ();

        /**
         * See {@link #store(IStore, BiConsumer)}.
         */
        private BiConsumer<IStore<V>,V> preload;

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
        private Function<V, String> labelMapper;

        /**
         * See {@link #optionRenderer(BiConsumer)}.
         */
        @SuppressWarnings("rawtypes")
        private BiConsumer<ContainerBuilder,V> optionRenderer;

        /**
         * See {@link #selectionRenderer(BiConsumer)}.
         */
        @SuppressWarnings("rawtypes")
        private BiConsumer<ContainerBuilder,V> selectionRenderer;

        /**
         * See {@link #addHandler(Consumer<Consumer<V>>)}
         */
        private Consumer<Consumer<V>> addHandler;

        /**
         * See {@link #addLabel(String)}.
         */
        private String addLabel;

        /**
         * See {@link #comparator(BiFunction)}.
         */
        private BiFunction<V,V,Boolean> comparator;

        /**
         * See {@link #searchBufferCountThreshold(int)}.
         */
        private int searchBufferCountThreshold = 6;

        /**
         * See {@link #searchBufferTimeThreshold(int)}.
         */
        private int searchBufferTimeThreshold = 300;

        /**
         * See {@link #useMaskOnLoad(boolean)}.
         */
        private boolean useMaskOnLoad = false;

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
        public Config<V> style(Style style) {
            if (style != null)
                this.style = style;
            return this;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.control.Control.Config#name(java.lang.String)
         */
        @Override
        public Config<V> name(String name) {
            return (Config<V>) super.name (name);
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.control.Control.Config#modifiedHandler(java.util.function.BiConsumer)
         */
        @Override
        public Config<V> modifiedHandler(IControlModifiedHandler<V> modifiedHandler) {
            return (Config<V>) super.modifiedHandler (modifiedHandler);
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.control.Control.Config#suppressDirty(boolean)
         */
        @Override
        public Config<V> suppressDirty(boolean suppressDirty) {
            return (Config<V>) super.suppressDirty (suppressDirty);
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.control.Control.Config#testId(java.lang.String)
         */
        @Override
        public Config<V> testId(String testId) {
            return (Config<V>) super.testId (testId);
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.control.Control.Config#width(com.effacy.jui.core.client.dom.css.Length)
         */
        @Override
        public Config<V> width(Length width) {
            return (Config<V>) super.width (width);
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.control.Control.Config#padding(com.effacy.jui.core.client.dom.css.Insets)
         */
        @Override
        public Config<V> padding(Insets padding) {
            return (Config<V>) super.padding (padding);
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.control.Control.Config#readOnly()
         */
        @Override
        public Config<V> readOnly() {
            return (Config<V>) super.readOnly ();
        }

        /**
         * Assigns placeholder text to display when the field is empty.
         * 
         * @param placeholder
         *                    placeholder content to display.
         * @return this configuration instance.
         */
        public Config<V> placeholder(String placeholder) {
            this.placeholder = placeholder;
            return this;
        }

        /**
         * Allows the selection to be empty ({@code null}).
         * 
         * @param allowEmpty
         *                   {@code true} to allow empty selections.
         * @return this configuration instance.
         */
        public Config<V> allowEmpty(boolean allowEmpty) {
            this.allowEmpty = allowEmpty;
            return this;
        }

        /**
         * See {@link #allowEmpty(boolean)}. Convenience to pass {@code true}.
         * 
         * @return this configuration instance.
         */
        public Config<V> allowEmpty() {
            return allowEmpty (true);
        }

        /**
         * Getter for {@link #allowEmpty(boolean)}.
         */
        public boolean isAllowEmpty() {
            return allowEmpty;
        }

        /**
         * Allows the selection to be searched ({@code null}).
         * 
         * @param allowEmpty
         *                   {@code true} to allow searching (default is {@code true}).
         * @return this configuration instance.
         */
        public Config<V> allowSearch(boolean allowSearch) {
            this.allowSearch = allowSearch;
            return this;
        }

        /**
         * Getter for {@link #allowSearch(boolean)}.
         */
        public boolean isAllowSearch() {
            return allowSearch;
        }

        /**
         * Assigns the maximum height of the selector.
         * 
         * @param selectorMaxHeight
         *                       the height of the selector.
         * @return this configuration instance.
         */
        public Config<V> selectorMaxHeight(Length selectorMaxHeight) {
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
        public Config<V> selectorHeight(Length selectorHeight) {
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
        public Config<V> selectorWidth(Length selectorWidth) {
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
        public Config<V> selectorTop(boolean selectorTop) {
            this.selectorTop = selectorTop;
            return this;
        }

        /**
         * See {@link #selectorTop(boolean)}. Convenience to pass {@code true}.
         * 
         * @return this configuration instance.
         */
        public Config<V> selectorTop() {
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
        public Config<V> selectorLeft(boolean selectorLeft) {
            this.selectorLeft = selectorLeft;
            return this;
        }

        /**
         * See {@link #selectorLeft(boolean)}. Convenience to pass {@code true}.
         * 
         * @return this configuration instance.
         */
        public Config<V> selectorLeft() {
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
        public Config<V> store(IStore<V> store) {
            if (store != null)
                this.store = store;
            return this;
        }

        /**
         * See {@link #store(IStore))} but includes a preload action to be invoked prior
         * to loading from the store.
         * <p>
         * The preload can be used to confgure the store based on the currently selected
         * value. An example would be to ammend the underlying store query to exclude
         * the selected item (or any other item). Note that for this example one should
         * invoke {@link #storeClear()} to ensure a reload is performed each time it is
         * opened.
         * <p>
         * Note that the preload is invoked on the store's
         * {@link IStoreLoadingListener#onStoreBeforeLoad(IStore)} event (so only makes
         * sense for store that generate this event, such as paginated stores).
         * 
         * @param store
         *                the store to use.
         * @param preload
         *                to be invoked prior to loading from the store.
         * @return this configuration instance.
         */
        @SuppressWarnings("unchecked")
        public <S extends IStore<V>> Config<V> store(S store, BiConsumer<S,V> preload) {
            if (store != null) {
                this.store = store;
                if (preload != null)
                    this.preload = (BiConsumer<IStore<V>,V>) preload;
            }
            return this;
        }

        /**
         * See {@link #store(IStore)}.
         */
        public IStore<V> getStore() {
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
        public Config<V> storeBatchSize(int storeBatchSize) {
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
        public Config<V> storeClear(boolean storeClear) {
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
        public Config<V> overflowSafe() {
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
        public Config<V> labelMapper(Function<V, String> labelMapper) {
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
        public Config<V> optionRenderer(BiConsumer<ContainerBuilder,V> optionRenderer) {
            this.optionRenderer = optionRenderer;
            return this;
        }

        /**
         * Provides a renderer for the selected option.
         * 
         * @param optionRenderer
         *                       the renderer.
         * @return this configuration instance.
         */
        @SuppressWarnings("rawtypes")
        public Config<V> selectionRenderer(BiConsumer<ContainerBuilder,V> selectionRenderer) {
            this.selectionRenderer = selectionRenderer;
            return this;
        }

        /**
         * Registers an <i>add</i> handler that has the selector display an add action.
         * <p>
         * The handler will be invoked with a callback. If the add action creates
         * something, then that should be returned via the callback.
         * 
         * @param addHandler
         *                   the handler.
         * @return this configuration instance.
         */
        public Config<V> addHandler(Consumer<Consumer<V>> addHandler) {
            this.addHandler = addHandler;
            return this;
        }

        /**
         * Getter for {@link #addHandler(Consumer<Consumer<V>>)}.
         * 
         * @return the label.
         */
        public Consumer<Consumer<V>> getAddHandler() {
            return addHandler;
        }

        /**
         * Provides for an alternative label to the default for when the add action is
         * configured (see {@link #addHandler(Consumer)}).
         * 
         * @param addLabel
         *                 the alternative label.
         * @return this configuration instance.
         */
        public Config<V> addLabel(String addLabel) {
            this.addLabel = addLabel;
            return this;
        }

        /**
         * Getter for {@link #addLabel(String)}.
         * 
         * @return the label.
         */
        public String getAddLabel() {
            return addLabel;
        }

        /**
         * Assigns a comparator to use to compare values.
         * 
         * @param comparator
         *                   the comparator.
         * @return this configuration instance.
         */
        public Config<V> comparator(BiFunction<V,V,Boolean> comparator) {
            this.comparator = comparator;
            return this;
        }

        /**
         * Assigns the count threshold for the search box. After this many changes a
         * search event is fired automatically.
         * <p>
         * Set to 0 to not to perform any keypress buffering.
         * 
         * @param searchBufferCountThreshold
         *                                   the threshold (default is 6).
         * @return this configuration instance.
         */
        public Config<V> searchBufferCountThreshold(int searchBufferCountThreshold) {
            this.searchBufferCountThreshold = searchBufferCountThreshold;
            return this;
        }

        /**
         * The maximum time (in millisecond) between firing a search when there is an
         * unactioned search query.
         * 
         * @param searchBufferTimeThreshold
         *                                  the threshold in ms (default is 300).
         * @return this configuration instance.
         */
        public Config<V> searchBufferTimeThreshold(int searchBufferTimeThreshold) {
            this.searchBufferTimeThreshold = searchBufferTimeThreshold;
            return this;
        }

        /**
         * The buffering threshold for update count.
         * 
         * @return the threshold (default is 6 updates).
         */
        public int getSearchBufferCountThreshold() {
            return searchBufferCountThreshold;
        }

        /**
         * The buffering threshold for time from first change.
         * 
         * @return the threshold in ms (default is 300).
         */
        public int getSearchBufferTimeThreshold() {
            return searchBufferTimeThreshold;
        }

        /**
         * See {@link #useMaskOnLoad(boolean)}. Convenience to pass {@code true}.
         */
        public Config<V> useMaskOnLoad() {
            return useMaskOnLoad(true);
        }

        /**
         * Sets whether or not masking on the menu should be used when data is being
         * loaded.
         * 
         * @param useMaskOnLoad
         *                      {@code true} if it should (default is not to).
         * @return this configuration instance.
         */
        public Config<V> useMaskOnLoad(boolean useMaskOnLoad) {
            this.useMaskOnLoad = useMaskOnLoad;
            return this;
        }

        /**
         * Determines of the menu should be masked during a load of data from the store.
         * 
         * @return {@code true} if it should.
         */
        public boolean isUseMaskOnLoad() {
            return useMaskOnLoad;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.component.Component.Config#build(com.effacy.jui.core.client.component.layout.LayoutData[])
         */
        @Override
        @SuppressWarnings("unchecked")
        public SelectionControl<V> build(LayoutData... data) {
            return build (new SelectionControl<V> (this), data);
        }

    }

    /**
     * Encapsulates all information available to render a given datum (selection
     * item).
     */
    public static class Datum<V> {

        /**
         * See {@link #config()}.
         */
        private Config<V> config;

        /**
         * See {@link #value()}.
         */
        private V value;

        /**
         * Construct with encapsulated data.
         */
        public Datum(Config<V> config, V value) {
            this.config = config;
            this.value = value;
        }

        /**
         * The configuration passed to the component.
         * 
         * @return the configuration.
         */
        public Config<V> config() {
            return config;
        }

        /**
         * The value of the datum (the selection item).
         * 
         * @return the value.
         */
        public V value() {
            return value;
        }
    }

    /************************************************************************
     * Members
     ************************************************************************/

    /**
     * The selector for selection of items.
     */
    private ISelectorMenu<V> selector;

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
            return ((SelectionControl.DEBUG & flag) > 0);
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
    public SelectionControl(SelectionControl.Config<V> config) {
        super (config);

        // Configure an instance of the selector.
        selector = createSelector ();
        selector.configureOnRender((cpt, el) -> {
            if (config ().selectorHeight != null)
                CSS.HEIGHT.apply (el, config ().selectorHeight);
            if (config ().selectorMaxHeight != null)
                CSS.MAX_HEIGHT.apply (el, config ().selectorMaxHeight);
            if (config ().selectorWidth != null)
                CSS.WIDTH.apply (selectorLocatorEl, config ().selectorWidth);
        });
        selector.addListener (IFocusBlurListener.create (null, cpt -> {
            // A bit of a delay here ensures any following events a properly handled.
            TimerSupport.timer (() -> {
                if (!isInFocus ())
                    hideSelector ();
            }, 20);
        }));

        // Configure any preload handler.
        if (config.preload != null) {
            config.store.addListener (IStoreLoadingListener.create (str -> {
                config.preload.accept (config.store, value ());
            }, null));
        }
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
    protected void buildRenderer(ContainerBuilder root, V value) {
        if (config ().labelMapper != null)
            Span.$ (root).text (config ().labelMapper.apply (value));
        else
            Span.$ (root).text ((value == null) ? "[NULL]" : value.toString ());
    }

    /**
     * Allows for the rendering of the selected value to be different from that of
     * the selection value (as appears in the selection list).
     * <p>
     * The default implementation delegates to {@link #buildRenderer(Container)}.
     * 
     * @param root
     *             the root container.
     */
    protected void buildSelectedRenderer(ContainerBuilder<?> root, V value) {
        if (config ().selectionRenderer != null)
            config ().selectionRenderer.accept (root, value);
        else
            buildRenderer (root, value);
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
    protected boolean filterMatch(String keywords, V value) {
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
     * Creates the selector component for the control.
     * <p>
     * The default is an instance of {@link SelectorMenu}.
     * 
     * @return the selector.
     */
    protected ISelectorMenu<V> createSelector() {
        return new SelectorMenu<V> (config ()) {

            @SuppressWarnings("rawtypes")
            @Override
            protected void buildItem(ContainerBuilder el, V value) {
                if (SelectionControl.this.config().optionRenderer != null)
                    SelectionControl.this.config().optionRenderer.accept(el, value);
                else
                    SelectionControl.this.buildRenderer (el, value);
            }

            @Override
            protected boolean filterMatch(String keywords, V value) {
                return SelectionControl.this.filterMatch (keywords, value);
            }

            @Override
            protected void onSelect(V value) {
                SelectionControl.this.select (value);
            }

            @Override
            protected void onAdd(V value) {
                if (value != null)
                    SelectionControl.this.select (value);
            }

        };
    }

    /************************************************************************
     * Presentation.
     ************************************************************************/

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#onBlur()
     */
    @Override
    protected void onBlur() {
        super.onBlur ();

        TimerSupport.timer (() -> {
            if (!selector.isInFocus ())
                hideSelector ();
        }, 200);
    }

    /**
     * The value for presentation.
     */
    private V value;

    /**
     * The content area (displays the value).
     */
    protected Element contentEl;

    /**
     * The selector div that holds the selector component.
     */
    protected Element selectorLocatorEl;

    /**
     * Internal action to select a value. This displays the item then hides the
     * selector while propagating a modified to the component controller.
     * 
     * @param value
     *              the value that was selected.
     */
    public void select(V value) {
        valueToSource (value);
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
     * Determines if the selector is open.
     * 
     * @return {@code true} if it is.
     */
    protected boolean _isSelectorOpen() {
        return getRoot ().classList.contains (styles ().open ());
    }

    /**
     * Shows (and resets) the selector.
     */
    public void showSelector() {
        getRoot ().classList.add (styles ().open ());
        if (value == null) {
            selector.reset (null);
        } else {
            List<V> values = new ArrayList<>();
            values.add (value);
            selector.reset (values);
        }
    }

    /**
     * Styles (made available to selection).
     */
    protected ILocalCSS styles() {
        return config ().style.styles ();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.control.Control#valueFromSource()
     */
    @Override
    public V valueFromSource() {
        return value;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.control.Control#valueToSource(java.lang.Object)
     */
    @Override
    public void valueToSource(V value) {
        this.value = value;

        if (value == null) {
            DomSupport.removeAllChildren (contentEl);
            getRoot ().classList.add (styles ().empty ());
            HTMLElement el = DomSupport.createSpan ();
            contentEl.appendChild (el);
            DomSupport.innerText (el, StringSupport.empty (config ().placeholder) ? "- Select -" : config ().placeholder);
        } else {
            getRoot ().classList.remove (styles ().empty ());
            DomSupport.removeAllChildren (contentEl);
            Wrap.buildInto (contentEl, root -> buildSelectedRenderer (root, value));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#buildNode(com.effacy.jui.core.client.component.Component.Config)
     */
    @Override
    protected INodeProvider buildNode(Config<V> data) {
        return DomBuilder.div (inner -> {
            inner.style (styles ().inner ());
            if (Debug.isTestMode())
                inner.attr ("test-ref", "input");
            inner.by ("control").id ("control");
            inner.on (e -> {
                // This just captures the event and prevents it from activating a focus (which
                // we want after the click to properly handle the hiding of the selector if it
                // is open).
                if (e.isEvent(UIEventType.ONMOUSEDOWN)) {
                    e.stopEvent ();
                    return;
                }
                if (DomSupport.isChildOf (e.getTarget (), selectorLocatorEl))
                    return;
                if (_isSelectorOpen()) {
                    hideSelector ();
                    focus();
                } else {
                    showSelector ();
                    // We only ask for focus is the selector is not searchable (so that the search
                    // box can gain focus).
                    if (!config ().isAllowSearch ())
                        focus ();
                }
            }, 1, UIEventType.ONMOUSEDOWN, UIEventType.ONCLICK, UIEventType.ONKEYPRESS);
            inner.attr ("tabindex", "0");
            Div.$ (inner).$ (selector -> {
                if (Debug.isTestMode())
                    selector.attr ("test-ref", "selector");
                selector.id ("selector").by ("selector");
                selector.apply (attach (SelectionControl.this.selector));
                if (data.selectorTop)
                    selector.style (styles ().selector_top ());
                if (data.selectorLeft)
                    selector.style (styles ().selector_left ());
            });
            Em.$ (inner).style (styles ().read_only (), FontAwesome.lock ());
            Span.$ (inner).id ("content").by ("content");
            if (data.isAllowEmpty ()) {
                Em.$ (inner).$ (icon -> {
                    icon.on (e -> {
                        e.stopEvent ();
                        select (null);
                    }, 0, UIEventType.ONCLICK);
                    icon.id ("remove");
                    icon.style (FontAwesome.times ());
                    if (Debug.isTestMode())
                        icon.setAttribute ("test-ref", "remove");
                });
            }
            Em.$ (inner).style (config ().style.selectorIcon (), styles ().open ());
        }).build (tree -> {
            manageFocusEl (tree.first ("control"));
            contentEl = tree.first ("content");
            selectorLocatorEl = tree.first ("selector");
        });
    }

    /********************************************************************
     * CSS with standard styles.
     ********************************************************************/

    public static interface ILocalCSS extends IControlCSS {

        /**
         * Inner wrap around the control (for the border).
         */
        public String inner();

        public String open();

        public String empty();
        
        public String selector_top();

        public String selector_left();

    }

    /**
     * Component CSS (horizontal).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        "com/effacy/jui/ui/client/control/Control.css",
        "com/effacy/jui/ui/client/control/SelectionControl.css",
        "com/effacy/jui/ui/client/control/SelectionControl_Override.css"
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
