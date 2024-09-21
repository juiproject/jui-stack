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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.effacy.jui.core.client.IDisposable;
import com.effacy.jui.core.client.component.Component;
import com.effacy.jui.core.client.component.IComponent;
import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.dom.DomSupport;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.UIEvent;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.jquery.JQuery;
import com.effacy.jui.core.client.dom.jquery.JQueryElement;
import com.effacy.jui.core.client.store.IOffsetStore;
import com.effacy.jui.core.client.store.IPaginatedStore;
import com.effacy.jui.core.client.store.IStore;
import com.effacy.jui.core.client.store.IStore.Status;
import com.effacy.jui.core.client.util.Tribool;
import com.effacy.jui.core.client.store.IStoreChangedListener;
import com.effacy.jui.core.client.store.IStoreLoadingListener;
import com.effacy.jui.core.client.store.IStoreSelection;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.platform.util.client.TimerSupport;
import com.effacy.jui.rpc.handler.client.query.IRecord;
import com.effacy.jui.ui.client.Theme;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;
import jsinterop.base.Js;

/**
 * A gallery of items sourced from a store.
 * <p>
 * A gallery consists of panels or cards that present content in a non-tabular
 * format.
 *
 * @author Jeremy Buckley
 */
public class Gallery<R> extends Component<Gallery.Config> implements IGallery<R> {

    /**
     * Configuration for a gallery.
     */
    public static class Config extends Component.Config {

        /**
         * Style for the gallery layout.
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
             *               the styles to use.
             * @return the style.
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
             * Standard style for filling out in a grid fashion.
             */
            public static final Style GRID = create (GridLocalCSS.instance ());

            /**
             * Standard style for filling out in a row-like fashion.
             */
            public static final Style ROW = create (RowLocalCSS.instance ());

        }

        /**
         * See {@link #getStyle()}.
         */
        private Style style = Style.GRID;

        /**
         * See {@link #scrollable(boolean)}.
         */
        private boolean scrollable = true;

        /**
         * See {@link #renderOnRefresh(boolean)}.
         */
        private boolean renderOnRefresh = false;

        /**
         * See {@link #emptyUnfiltered(Consumer)}.
         */
        private Consumer<ElementBuilder> emptyUnfiltered;

        /**
         * See {@link #emptyFiltered(Consumer)}.
         */
        private Consumer<ElementBuilder> emptyFiltered;

        /**
         * See {@link #emptyError(Consumer)}.
         */
        private BiConsumer<ElementBuilder,String> emptyError;

        /**
         * Construct with the default style.
         */
        public Config() {
            super ();
        }

        /**
         * Construct with a specific style.
         * 
         * @param style
         *              the style.
         */
        public Config(Style style) {
            if (style != null)
                this.style = style;
        }

        /**
         * Assigns a style to use (see also {@link #Gallery(Style))}).
         * 
         * @param style
         *              the style.
         * @return this configuration instance.
         */
        public Config style(Style style) {
            if (style != null)
                this.style = style;
            return this;
        }

        /**
         * The style to apply to the gallery.
         * 
         * @return the style.
         */
        public Style getStyle() {
            return style;
        }

        /**
         * Determines if the gallery should be scrollable or not.
         * 
         * @param scrollable
         *                   {@code false} if not (default is {@code true}).
         * @return this configuration instance.
         */
        public Config scrollable(boolean scrollable) {
            this.scrollable = scrollable;
            return this;
        }

        /**
         * Determines if gallery items should always be re-rendered when the gallery is
         * refreshed (generally this will be quite expensive).
         * 
         * @param renderOnRefresh
         *                        {@code true} if it should.
         * @return this configuration instance.
         */
        public Config renderOnRefresh(boolean renderOnRefresh) {
            this.renderOnRefresh = renderOnRefresh;
            return this;
        }

        /**
         * Assign a rendering for the case where there are no results at all
         * (unfiltered).
         * 
         * @param render
         *               to render the message.
         * @return this configuration instance.
         */
        public Config emptyUnfiltered(Consumer<ElementBuilder> render) {
            this.emptyUnfiltered = render;
            return this;
        }

        /**
         * Assign a rendering for the case where there are no results for a given active
         * filtering.
         * 
         * @param render
         *               to render the message.
         * @return this configuration instance.
         */
        public Config emptyFiltered(Consumer<ElementBuilder> render) {
            this.emptyFiltered = render;
            return this;
        }

        /**
         * Assign a rendering for the case where there is an error on the store.
         * <p>
         * The render expression is passed a root node to render into and the message
         * associated with the error (from {@link IStore#getStatusMessage()}).
         * 
         * @param render
         *               to render the message.
         * @return this configuration instance.
         */
        public Config emptyError(BiConsumer<ElementBuilder,String> render) {
            this.emptyError = render;
            return this;
        }

        /**
         * Getter for {@link #emptyUnfiltered(Consumer)}.
         */
        Consumer<ElementBuilder> getEmptyUnfiltered() {
            if (this.emptyUnfiltered == null) {
                emptyUnfiltered ((v) -> {
                    EmptyNotification.buildPanel (v, new EmptyNotification (panel -> {
                        panel.title ("Sorry, no results found");
                        panel.paragraph ("We were not able to find anything to display.");
                    }));
                });
            }
            return this.emptyUnfiltered;
        }

        /**
         * Getter for {@link #emptyFiltered(Consumer)}.
         */
        Consumer<ElementBuilder> getEmptyFiltered() {
            if (this.emptyFiltered != null)
                return this.emptyFiltered;
            return getEmptyUnfiltered ();
        }

        /**
         * Getter for {@link #emptyEmpty(Consumer)}.
         */
        BiConsumer<ElementBuilder,String> getEmptyError() {
            if (this.emptyError == null) {
                emptyError ((v,msg) -> {
                    EmptyNotification.buildPanel (v, new EmptyNotification (panel -> {
                        panel.title ("Sorry, there was a problem");
                        panel.paragraph (msg);
                    }));
                });
            }
            return this.emptyError;
        }
    }

    /**
     * The underlying store.
     */
    private IStore<R> store;

    /**
     * Used to generate gallery items.
     */
    private Supplier<IGalleryItem<R>> itemFactory;

    /************************************************************************
     * Debugging
     ************************************************************************/

    /**
     * Various debug modes.
     */
    public enum DebugMode {

        /**
         * Log loading via scroll.
         */
        SCROLL(1<<1);

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
            return ((Gallery.DEBUG & flag) > 0);
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

    /**
     * Construct a gallery instance. Default configuration is used.
     * 
     * @param store
     *                    the backing store.
     * @param itemFactory
     *                    a supplier of {@link IGalleryItem} instances.
     */
    public Gallery(IStore<R> store, Supplier<IGalleryItem<R>> itemFactory) {
        this (new Gallery.Config (), store, itemFactory);
    }

    /**
     * Construct a gallery.
     * 
     * @param config
     *                    configuration for the gallery.
     * @param store
     *                    the backing store.
     * @param itemFactory
     *                    a supplier of {@link IGalleryItem} instances.
     */
    public Gallery(Config config, IStore<R> store, Supplier<IGalleryItem<R>> itemFactory) {
        super (config);
        this.store = store;
        this.store.addListener (IStoreChangedListener.create (s -> {
            _renderRecords ()
                .isTrue (() -> onScrolledToBottomOfPage ())
                .isFalse (() -> {
                    // If false then we defer and re-check (to be safe).
                    TimerSupport.defer (() -> _endOfPage ().isTrue (() -> onScrolledToBottomOfPage ()));
                })
                .isUndetermined (() -> {
                    // We just wait a little in this case.
                    TimerSupport.timer (() -> _endOfPage ().isTrue (() -> onScrolledToBottomOfPage ()), 100);
                });
        }));
        this.store.addListener (IStoreLoadingListener.create (s -> {
            if (Status.UNLOADED == s.getStatus ())
                hideEmpty ();
            showLoading (Status.UNLOADED != s.getStatus ());
        }, s -> {
            hideLoading ();
            if (IStore.Status.ERROR == store.getStatus())
                showEmpty (EmptyState.ERROR, store.getStatusMessage ());
            else if (store.empty ())
                showEmpty ((Status.FILTERED == store.getStatus ()) ? EmptyState.FILTERED : EmptyState.UNFILTER, null);
            else
                hideEmpty ();
        }));
        this.itemFactory = itemFactory;

        // This is used to check if the page needs further loading.
        setMonitorWindowResize (true);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.ui.client.gallery.IGallery#getStore()
     */
    @Override
    public IStore<R> getStore() {
        return store;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#onWindowResize(int, int)
     */
    @Override
    protected void onWindowResize(int width, int height) {
        if (isRendered () && _endOfPage ().isTrue ())
            onScrolledToBottomOfPage ();
    }

    /**
     * Invoked when the gallery region has scrolled to the bottom. Called by
     * {@link #onWindowResize(int, int)} and {@link #onGalleryScroll(UIEvent)}.
     */
    protected void onScrolledToBottomOfPage() {
        // Check that the store has some contents and is not loading.
        if (store.getStatus ().is (Status.UNLOADED, Status.LOADING))
            return;
        if (store instanceof IPaginatedStore) {
            if (!store.fullyLoaded ())
                ((IPaginatedStore<R>) store).loadNext (true);
        } else if (store instanceof IOffsetStore) {
            if (!store.fullyLoaded ())
                ((IOffsetStore<R>) store).loadNext ();
        }
    }

    /********************************************************************
     * Presentation.
     ********************************************************************/

    /**
     * Creates an instance of the gallery item.
     * 
     * @return the item.
     */
    protected IGalleryItem<R> createGalleryItem() {
        if (itemFactory != null)
            return itemFactory.get ();
        return null;
    }

    /**
     * Gallery area.
     */
    protected Element galleryEl;

    /**
     * The target for the gallery content.
     */
    protected Element targetEl;

    /**
     * The empty area.
     */
    protected JQueryElement emptyEl;

    /**
     * The empty area (filtered).
     */
    protected JQueryElement emptyFilteredEl;

    /**
     * The empty area (filtered).
     */
    protected JQueryElement emptyUnfilteredEl;

    /**
     * The empty area (error).
     */
    protected JQueryElement emptyErrorEl;

    /**
     * Set when {@link #showLoading()} is invoked prior to rendering so the state
     * can be applied when rendered.
     */
    private boolean preRenderShowLoading = true;

    /**
     * Shows the gallery loading mask and indicator.
     * 
     * @param quiet
     *              {@code true} if to suppress the loading indicator.
     */
    public void showLoading(boolean quiet) {
        if (!isRendered ()) {
            preRenderShowLoading = true;
            return;
        }
        if (quiet)
            getRoot ().classList.add (styles ().quiet ());
        getRoot ().classList.add (styles ().mask ());
    }

    /**
     * Hide the gallery loading mask and indicator.
     */
    public void hideLoading() {
        if (!isRendered ()) {
            preRenderShowLoading = false;
            return;
        }
        getRoot ().classList.remove (styles ().quiet ());
        getRoot ().classList.remove (styles ().mask ());
    }

    /********************************************************************************
     * Handling the display of the empty or error state.
     ********************************************************************************/

    /**
     * Various empty states.
     */
    enum EmptyState {
        NONE, UNFILTER, FILTERED, ERROR;
    }

    /**
     * Set when {@link #showEmpty()} is invoked prior to rendering so the state can
     * be applied when rendered.
     */
    private EmptyState preRenderShowEmpty = EmptyState.NONE;

    /**
     * See {@link #showError(String)}.
     */
    private String preRenderShowEmptyMessage;

    /**
     * Shows the empty area.
     */
    public void showEmpty(EmptyState state, String message) {
        if (state == null)
            return;
        if (!isRendered ()) {
            preRenderShowEmpty = state;
            preRenderShowEmptyMessage = message;
            return;
        }
        if (EmptyState.NONE == state) {
            getRoot ().classList.remove (styles ().empty ());
        } else {
            getRoot ().classList.add (styles ().empty ());
            if (EmptyState.FILTERED == state) {
                emptyFilteredEl.show ();
                emptyUnfilteredEl.hide ();
                emptyErrorEl.hide ();
            } else if (EmptyState.UNFILTER == state) {
                emptyFilteredEl.hide ();
                emptyUnfilteredEl.show ();
                emptyErrorEl.hide ();
            } else {
                emptyFilteredEl.hide ();
                emptyUnfilteredEl.hide ();
                emptyErrorEl.show ();
                buildInto (Js.cast (emptyErrorEl.get(0)), el -> {
                    buildEmptyErrorPanel (el, message);
                });
            }
        }
    }

    /**
     * Shows the empty area.
     */
    public void hideEmpty() {
        showEmpty (EmptyState.NONE, null);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#onAfterRender()
     */
    @Override
    protected void onAfterRender() {
        super.onAfterRender ();

        if (config ().scrollable)
            getRoot ().classList.add (styles ().scrollable ());
        if (preRenderShowLoading)
            showLoading (false);
        if (preRenderShowEmpty != EmptyState.NONE)
            showEmpty (preRenderShowEmpty, preRenderShowEmptyMessage);

        _renderRecords ();
    }

    /**
     * Determines if the gallery content area is at the end (i.e. scrolled to the
     * bottom). This can be used to activate a page load.
     * <p>
     * It is possible that the end-of-page cannot yet be determined (this may happen
     * when the gallery, or its owning page, has just been shown and so the browser
     * has not performed a layout). In that case a {@link Tribool#UNDETERMINED} is
     * returned. Often the solution is to wait a bit and try again.
     * 
     * @return {@code true} if is at the end.
     */
    public Tribool _endOfPage() {
        if (galleryEl == null) {
            if (DebugMode.SCROLL.set ())
                Logger.trace ("gallery", "{gallery-endofpage} FALSE");
            return Tribool.FALSE;
        }
        if (galleryEl.clientHeight == 0) {
            // If there is no client height then we may have just been shown and the layout
            // has not been determined.
            if (DebugMode.SCROLL.set ())
                Logger.trace ("gallery", "{gallery-endofpage} UNDETERMINED [clientHeight=0]");
            return Tribool.UNDETERMINED;
        }
        if (galleryEl.scrollTop + galleryEl.clientHeight >= (galleryEl.scrollHeight - 10)) {
            if (DebugMode.SCROLL.set ()) {
                Logger.trace ("gallery", "{gallery-endofpage} TRUE [scrollTop=" + galleryEl.scrollTop + " clientHeight=" +
                    galleryEl.clientHeight + " (" + (galleryEl.scrollTop +
                    galleryEl.clientHeight) + ") scrollHeight=" + galleryEl.scrollHeight + " (" +
                    (galleryEl.scrollHeight - 10) + ")]");
            }
            return Tribool.TRUE;
        }
        if (DebugMode.SCROLL.set ()) {
            Logger.trace ("gallery", "{gallery-endofpage} UNDETERMINED [scrollTop=" + galleryEl.scrollTop + " clientHeight=" +
                galleryEl.clientHeight + " (" + (galleryEl.scrollTop +
                galleryEl.clientHeight) + ") scrollHeight=" + galleryEl.scrollHeight + " (" +
                (galleryEl.scrollHeight - 10) + ")]");
        }
        return Tribool.FALSE;
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
            Div.$ (root).$ (gallery -> {
                gallery.by ("gallery");
                gallery.attr ("test-ref", "gallery");
                gallery.style (styles ().gallery ());
                gallery.on (e -> onGalleryScroll (e), UIEventType.ONSCROLL);
                Div.$ (gallery).by ("target");
            });
            Div.$ (root).$ (empty -> {
                empty.by ("empty");
                empty.style (styles ().empty ());
                Div.$ (empty).$ (panels -> {
                    panels.id ("unfiltered").by ("unfiltered");
                    buildEmptyUnfilteredPanel (panels);
                });
                Div.$ (empty).$ (panels -> {
                    panels.id ("filtered").by ("filtered");
                    buildEmptyFilteredPanel (panels);
                });
                Div.$ (empty).$ (panels -> {
                    panels.id ("error").by ("error");
                    // Note that contents are written in at the time to accommodate the error
                    // message.
                });
            });
            Div.$ (root).$ (mask -> {
                mask.addClassName (styles ().mask ());
                Div.$ (mask).$ (inner -> {
                    inner.style (Theme.styles ().loader ());
                });
            });
        }).build (tree -> {
            galleryEl = tree.first ("gallery");
            targetEl = tree.first ("target");
            emptyEl = JQuery.$ ((Element) tree.first ("empty"));
            emptyFilteredEl = JQuery.$ ((Element) tree.first ("filtered"));
            emptyUnfilteredEl = JQuery.$ ((Element) tree.first ("unfiltered"));
            emptyErrorEl = JQuery.$ ((Element) tree.first ("error"));
        });
    }

    /**
     * Builds out the display for the case where there are no results (unfiltered).
     * 
     * @param container
     *                  the container to build into.
     */
    protected void buildEmptyUnfilteredPanel(ElementBuilder container) {
        config ().getEmptyUnfiltered ().accept (container);
    }

    /**
     * Builds out the display for the case where there are no results (filtered).
     * 
     * @param container
     *                  the container to build into.
     */
    protected void buildEmptyFilteredPanel(ElementBuilder container) {
        config ().getEmptyFiltered ().accept (container);
    }

    /**
     * Builds out the display for the case where there are no results (due to an
     * error).
     * 
     * @param container
     *                  the container to build into.
     * @param message
     *                  any associated error message.
     */
    protected void buildEmptyErrorPanel(ElementBuilder container, String message) {
        config ().getEmptyError ().accept (container, message);
    }

    /**
     * Handles when the gallery region scrolls.
     */
    protected void onGalleryScroll(UIEvent e) {
        if (DebugMode.SCROLL.set())
            Logger.trace ("gallery", "{gallery-scroll}}");
        _endOfPage ()
            .isTrue (() -> onScrolledToBottomOfPage ())
            .isUndetermined (() -> {
                // We just wait a little in this case.
                TimerSupport.timer (() -> _endOfPage ().isTrue (() -> onScrolledToBottomOfPage ()), 100);
            });
    }

    protected IGalleryGroupHandler<R> _createGroupHandler() {
        return null;
    }

    private List<GalleryItemWrapper> currentRecords = new ArrayList<> ();

    /**
     * Obtains the index of the specified record in the gallery (as displayed). If
     * the record implements {@link IRecord} then equality check is based on the ID.
     * Note that sameness is not used as a comparison as that is used only to
     * determine if the record should be re-rendered.
     * 
     * @param record
     *               the record to get the index of.
     * @return The index of the record (or {@code -1} if it is not present).
     */
    protected int _indexOf(Object record) {
        for (int i = 0, len = currentRecords.size (); i < len; i++) {
            Object compare = currentRecords.get (i).getRecord ();
            if (compare.equals (record))
                return i;
        }
        return -1;
    }

    /**
     * Renders all the records in the passed store and returns whether the gallery
     * is displaying the end of the gallery page (which means a possible advancement
     * of the store).
     * 
     * @param <R>
     * @param store
     *              the store to render.
     * @return {@code true} if displaying the end of the gallery region.
     */
    @SuppressWarnings("unchecked")
    public Tribool _renderRecords() {
        int itemsPerRow = 0;
        List<R> records = store.asList ();

        // Can't do anything if not rendered yet.
        if (!isRendered ())
            return Tribool.FALSE;

        // Normalise.
        if (itemsPerRow < 1)
            itemsPerRow = 1;
        IGalleryGroupHandler<R> groupHandler = _createGroupHandler ();

        // Get the target for rendering.

        // Make sure we have something to render to.
        if (galleryEl == null)
            return Tribool.FALSE;

        // Remove any empty and error styles.
        galleryEl.classList.remove (styles ().empty ());

        // Check for and render the empty case if relevant.
        if (records.isEmpty ()) {
            for (GalleryItemWrapper item : currentRecords) {
                // fireEvent (IGalleryPresenterListener.class).onRemoveItem (item.item);
                item.dispose ();
            }
            currentRecords.clear ();
            DomSupport.removeAllChildren (targetEl);
            return Tribool.FALSE;
        }

        // Go through all the elements and strip groups.
        // if ((itemsPerRow == 1) && (groupHandler != null)) {
        // GQuery items = GQuery.$ (targetEl).find ("." + styles ().wrapGroup ());
        // items.remove ();
        // }

        // Walk through the new list and identify the position already held
        // in the current list. The idea being that we retain the wrapper
        // elements for records that already exist, deleting those who have
        // been removed and inserting new ones when required. This keeps as
        // many DOM elements as possible in place improving performance and
        // helps prevent page jumping (for example, if adding records to the
        // end of the gallery and the page is scrolled to the bottom of the
        // gallery then re-writing all of the elements would result in the
        // page jumping to the top of the gallery each refresh). The model
        // below works well for insertions and removals but we simplify
        // things for re-ordering (where the jumping issue is generally a
        // non-issue). In this case we impose the condition that current
        // positions must be increasing in order and anything out of this
        // order is simply re-inserted. This could be "cleaned" by a
        // reorder of DOM elements post determination of current position
        // but this has been deferred to a later date.
        int[] currentPositions = new int[records.size ()];
        int i = 0;
        int idxMax = -1;
        for (R record : records) {
            currentPositions[i] = _indexOf (record);
            if (currentPositions[i] >= 0) {
                if (idxMax < currentPositions[i])
                    idxMax = currentPositions[i];
                else
                    currentPositions[i] = -1;
            }
            i++;
        }

        // Create a list of items that will be added.
        List<Element> addList = new ArrayList<Element> ();

        // Render the records in one pass. Do this by
        // removing those records that are no longer to be displayed or
        // being re-rendered and inserting re-rendered or new records.
        int idx = 0;
        int adjustment = 0;
        List<GalleryItemWrapper> revisedRecords = new ArrayList<> ();

        for (R record : records) {
            // Get the current position of the record we are inserting.
            int currentPosition = currentPositions[idx];

            GalleryItemWrapper item;
            boolean newRecord = false;
            if (currentPosition == -1) {
                // New element we are inserting.
                item = new GalleryItemWrapper ();
                addList.add (item.getElement ());
                DomSupport.insertChild (targetEl, item.getElement (), idx);
                adjustment--;
            } else {
                // Remove everything up to the element.
                int distaceAway = currentPosition - adjustment - idx;
                while (distaceAway > 0) {
                    targetEl.removeChild (targetEl.childNodes.getAt (idx));
                    distaceAway--;
                    adjustment++;
                }
                item = currentRecords.get (currentPosition);
            }
            newRecord = item.setRecord (record);
            revisedRecords.add (item);

            // Render the record (only if we are refreshing old records or
            // it is a new record).
            if (config ().renderOnRefresh || newRecord) {
                boolean fireCreateEvent = !item.rendered;
                item.render ();
                // if (fireCreateEvent)
                // fireEvent (IGalleryPresenterListener.class).onCreateItem (item.item);
            }

            // Update the selection state.
            boolean selected = false;
            if (store instanceof IStoreSelection)
                selected = ((IStoreSelection<Object>) store).isSelected (record);

            item.updateSelection (selected);
            idx++;
        }

        // Clean up any extra elements.
        int size = targetEl.childElementCount;
        while (idx++ < size)
            targetEl.removeChild (targetEl.lastChild);

        // Assign positional classes to elements (accounting for additions
        // and removals).
        for (int j = 0, len = revisedRecords.size (); j < len; j++) {
            GalleryItemWrapper item = revisedRecords.get (j);
            Element itemEl = item.getElement ();
            itemEl.id = "sl-gallery-item-" + j;

            ListPosition rowPos = _getRowPosition (j, len);
            ListPosition colPos = _getColPosition (j, itemsPerRow);
            item.updatePosition (rowPos, colPos);
        }

        // Set the current records (discarding old ones).
        for (GalleryItemWrapper item : currentRecords) {
            if (!revisedRecords.contains (item)) {
                // fireEvent (IGalleryPresenterListener.class).onRemoveItem (item.item);
                item.dispose ();
            }
        }
        currentRecords.clear ();
        currentRecords.addAll (revisedRecords);

        // Insert the groups holders.
        if ((itemsPerRow == 1) && (groupHandler != null)) {
            R previous = null;
            for (GalleryItemWrapper item : revisedRecords) {
                R current = (R) item.getRecord ();
                if (groupHandler.newGroup (previous, current)) {
                    Element el = item.getElement ();
                    if (el.parentElement != null) {
                        Element group = DomSupport.createDiv ();
                        // group.addClassName (styles ().wrapGroup ());
                        // if (previous == null)
                        // group.addClassName (styles ().wrapGroupFirst ());
                        // else
                        // group.addClassName (styles ().wrapGroupMiddle ());
                        // el.getParentElement ().insertBefore (group, el);
                        // groupHandler.renderGroup (group, current);
                    }
                }
                previous = current;
            }
        }

        return _endOfPage ();
    }

    /**
     * Converts the current item index to a list position relative to the current
     * row.
     * 
     * @param idx
     *             the index of the item.
     * @param size
     *             the total number of items.
     * @return The list position in the row.
     */
    protected ListPosition _getRowPosition(int idx, int size) {
        if (size == 1)
            return ListPosition.ONLY;
        if (idx == 0)
            return ListPosition.FIRST;
        if (idx >= size - 1)
            return ListPosition.LAST;
        return ListPosition.MIDDLE;
    }

    /**
     * Converts the current item index to a list position relative to the current
     * column.
     * 
     * @param idx
     *                    the index of the item.
     * @param itemsPerRow
     *                    the number of items to disply on a given row.
     * @return The list position in the column.
     */
    protected ListPosition _getColPosition(int idx, int itemsPerRow) {
        if (itemsPerRow <= 1)
            return ListPosition.ONLY;
        if (idx % itemsPerRow == 0)
            return ListPosition.FIRST;
        if ((idx + 1) % itemsPerRow == 0)
            return ListPosition.LAST;
        return ListPosition.MIDDLE;
    }

    /**
     * Possible positions an item could be in.
     */
    public enum ListPosition {
        /**
         * If this is the only element in the list.
         */
        ONLY,

        /**
         * If this is the first (but not only) element in the list.
         */
        FIRST,

        /**
         * If this is a middle (and not only) element in the list.
         */
        MIDDLE,

        /**
         * If this is the last (but not only) element in the list.
         */
        LAST;
    }

    /**
     * Embodies a single item in a gallery view. This is responsible for binding the
     * gallery renderer (which could also be a component), the underlying data
     * record and presentation of the surrounding wrapper.
     */
    class GalleryItemWrapper implements IDisposable {

        /**
         * The top-level element of the item.
         */
        private Element el;

        /**
         * The root element for rendering the item into.
         */
        private Element rootEl;

        /**
         * The item to present the record.
         */
        private IGalleryItem<R> item;

        /**
         * The record under management.
         */
        private R record;

        /**
         * Flag to indicate that the wrapper has been rendered.
         */
        private boolean rendered;

        GalleryItemWrapper() {
            this.item = createGalleryItem ();
            item.setGallery (Gallery.this);
        }

        /**
         * Updates the display state for being (or not being) selected.
         * 
         * @param selected
         *                 {@code true} if selected, otherwise {@code false} if not
         *                 selected.
         */
        public void updateSelection(boolean selected) {

        }

        public void updatePosition(ListPosition rowPosition, ListPosition colPosition) {

        }

        /**
         * Obtains the underlying record.
         * 
         * @return the record.
         */
        R getRecord() {
            return record;
        }

        /**
         * Assigns the underlying record the item renders.
         * 
         * @param record
         *               the record.
         * @return {@code true} if there was a change in record (i.e. what was there
         *         already was different from what was passed).
         */
        boolean setRecord(R record) {
            if (this.record == record)
                return false;
            if ((this.record != null) && (record != null) && this.record.equals (record))
                return false;
            this.record = record;
            if (item != null)
                item.setRecord (record);
            return true;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.IDisposable#dispose()
         */
        public void dispose() {
            item.dispose ();
        }

        /**
         * Obtains the root element of the item wrapper.
         * 
         * @return the element.
         */
        Element getElement() {
            build ();
            return el;
        }

        /**
         * Renders the item into the element.
         */
        void render() {
            build ();
            if (item != null) {
                item.render (rootEl);
                if (item instanceof IComponent)
                    Gallery.this.adopt ((IComponent) item);
            } else {
                DomSupport.innerText (rootEl, "EMPTY");
            }
            rendered = true;
        }

        /**
         * Builds the root element.
         */
        private void build() {
            if (el != null)
                return;
            rootEl = el = DomSupport.createDiv ();
        }

    }

    /********************************************************************
     * CSS with standard styles.
     ********************************************************************/

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#styles()
     */
    public ILocalCSS styles() {
        return config ().getStyle ().styles ();
    }

    public static interface ILocalCSS extends IComponentCSS {

        public String gallery();

        public String empty();

        /**
         * Defines and activates (when on the root element) a mask.
         */
        public String mask();

        /**
         * Used with {@link #mask()} to provide a quiet version (does not show the
         * loader).
         */
        public String quiet();

        public String scrollable();

    }

    /**
     * Component CSS (horizontal).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        "com/effacy/jui/ui/client/gallery/Gallery.css",
        "com/effacy/jui/ui/client/gallery/Gallery_Grid.css",
        "com/effacy/jui/ui/client/gallery/Gallery_Grid_Override.css"
    })
    public static abstract class GridLocalCSS implements ILocalCSS {

        private static GridLocalCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (GridLocalCSS) GWT.create (GridLocalCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }

    /**
     * Component CSS (horizontal).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        "com/effacy/jui/ui/client/gallery/Gallery.css",
        "com/effacy/jui/ui/client/gallery/Gallery_Row.css",
        "com/effacy/jui/ui/client/gallery/Gallery_Row_Override.css"
    })
    public static abstract class RowLocalCSS implements ILocalCSS {

        private static RowLocalCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (RowLocalCSS) GWT.create (RowLocalCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }

}
