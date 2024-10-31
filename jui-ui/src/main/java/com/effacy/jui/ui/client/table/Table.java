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
package com.effacy.jui.ui.client.table;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import com.effacy.jui.core.client.component.Component;
import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.dom.DomSupport;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.IUIEventHandler;
import com.effacy.jui.core.client.dom.UIEvent;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.DomBuilder;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.builder.Tbody;
import com.effacy.jui.core.client.dom.builder.Th;
import com.effacy.jui.core.client.dom.builder.Thead;
import com.effacy.jui.core.client.dom.builder.Tr;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Color;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.dom.jquery.JQuery;
import com.effacy.jui.core.client.dom.jquery.JQueryElement;
import com.effacy.jui.core.client.store.IOffsetStore;
import com.effacy.jui.core.client.store.IPaginatedStore;
import com.effacy.jui.core.client.store.IStore;
import com.effacy.jui.core.client.store.IStore.Status;
import com.effacy.jui.core.client.store.IStoreChangedListener;
import com.effacy.jui.core.client.store.IStoreLoadingListener;
import com.effacy.jui.core.client.store.IStoreSelection;
import com.effacy.jui.core.client.util.Tribool;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.Itr;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.platform.util.client.TimerSupport;
import com.effacy.jui.rpc.handler.client.query.IRecord;
import com.effacy.jui.ui.client.Theme;
import com.effacy.jui.ui.client.gallery.EmptyNotification;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.effacy.jui.ui.client.table.ITableCellRenderer.ITableCellHandler;
import com.effacy.jui.ui.client.table.Table.Config.SortDirection;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.Node;
import jsinterop.base.Js;

/**
 * Standard implementation of {@link ITable} that provides a tabular view of
 * record data.
 *
 * @author Jeremy Buckley
 */
public class Table<R> extends Component<Table.Config<R>> implements ITable<R> {
    /**
     * Configuration for a gallery.
     */
    public static class Config<R> extends Component.Config {

        /**
         * Style for the gallery layout. 
         */
        public interface Style {

            /**
             * The CSS styles.
             */
            public ILocalCSS styles();

            /**
             * Header icon to use for the ascending direction.
             */
            public String iconAscending();

            /**
             * Header icon to use for the descending direction.
             */
            public String iconDescending();

            /**
             * Header icon to use to indicate that a column is sortable (but not sorting).
             * This is optional.
             */
            public String iconSortable();

            /**
             * Convenience to create a style instance.
             * 
             * @param styles
             *                       the style.
             * @param iconAscending
             *                       icon for the header ascending indicator.
             * @param iconDescending
             *                       icon for the header ascending indicator.
             * @param iconSortable
             *                       icon for the header "sortable" indicator.
             * @return the style instance.
             */
            public static Style create(ILocalCSS styles, String iconAscending, String iconDescending, String iconSortable) {
                return new Style () {

                    @Override
                    public ILocalCSS styles() {
                        return styles;
                    }

                    @Override
                    public String iconAscending() {
                        return iconAscending;
                    }

                    @Override
                    public String iconDescending() {
                        return iconDescending;
                    }

                    @Override
                    public String iconSortable() {
                        return iconSortable;
                    }

                };
            }
            
            /**
             * Standard style.
             */
            public static final Style STANDARD = Style.create (StandardLocalCSS.instance(), FontAwesome.arrowDown(), FontAwesome.arrowUp(), FontAwesome.arrowsUpDown());

        }

        /**
         * See {@link #getStyle()}.
         */
        private Style style = Style.STANDARD;

        /**
         * See {@link #scrollable(boolean)}.
         */
        private boolean scrollable = true;

        /**
         * See {@link #renderOnRefresh(boolean)}.
         */
        private boolean renderOnRefresh = false;

        /**
         * See {@link #cellPadding(Insets)}.
         */
        private Insets cellPadding;

        /**
         * See {@link #color(Color)}.
         */
        private Color color;

        /**
         * See {@link #selectable(boolean)}.
         */
        private boolean selectable;

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
         * See {@linl #header(String)}.
         */
        private List<Header> headers = new ArrayList<> ();

        /**
         * The possible sorting directions.
         */
        public enum SortDirection {
            ASC, DESC;
        }

        /**
         * Configuration for a single column header.
         */
        public class Header {

            /**
             * The display label.
             */
            private String label;

            /**
             * See {@link #icon(String)}.
             */
            private String icon;

            /**
             * See {@link #sortable(boolean)}.
             */
            private boolean sortable;

            /**
             * See {@link #direction(SortDirection)}.
             */
            private SortDirection direction = SortDirection.DESC;

            /**
             * See {@link #sortHandler(Consumer)}.
             */
            private Consumer<SortDirection> sortHandler;

            /**
             * See {@link #sorted(boolean)}.
             */
            private boolean sorted;

            /**
             * See {@link #width(Length)}.
             */
            private Length width;

            /**
             * See {@link #renderer(Function, ITableCellRenderer)}.
             */
            private ITableCellRenderer<R> renderer;

            /**
             * Construct header.
             * 
             * @param label
             *              the display label.
             */
            public <D> Header(String label) {
                this.label = label;
            }

            /**
             * Assigns a renderer and mechanism to extract data for the renderer from the
             * record being rendered.
             * 
             * @param <D>
             * @param renderer
             *                 the renderer.
             * @return this header.
             */
            public Header renderer(ITableCellRenderer<R> renderer) {
                this.renderer = renderer;
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
             * An icon to include with the header.
             * 
             * @param icon
             *             CSS style for the icon.
             * @return this header instance.
             */
            public Header icon(String icon) {
                this.icon = icon;
                return this;
            }

            /**
             * Marks the header as being sortable with the given default direction (when
             * first becomes sorted) and handler for performing the sort when a sort action
             * is invoked.
             * 
             * @param direction
             *                    the default sort direction.
             * @param sortHandler
             *                    to be invoked when a sort action has occurred on this
             *                    header.
             * @return this header instance.
             */
            public Header sortable(SortDirection direction, Consumer<SortDirection> sortHandler) {
                this.sortable = true;
                this.direction = direction;
                this.sortHandler = sortHandler;
                return this;
            }

            /**
             * Indicates that the header is sorted by default.
             * 
             * @return this header instance.
             * @see #sorted(boolean)
             */
            public Header sorted() {
                return sorted (true);
            }

            /**
             * Determines if the header is sorted by default (only the first header
             * encounted that is sorted will be sorted).
             * 
             * @param sortable
             *                 {@code true} if it is.
             * @return this header instance.
             */
            public Header sorted(boolean sorted) {
                this.sorted = sorted;
                return this;
            }

            /**
             * The width of the column.
             * 
             * @param width
             *              the width.
             * @return this header instance.
             */
            public Header width(Length width) {
                this.width = width;
                return this;
            }

        }

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
         * The style to apply to the gallery.
         * 
         * @return the style.
         */
        public Style getStyle() {
            return style;
        }

        /**
         * Defines the default cell padding to apply. This overrides any that is present
         * on the selected style.
         * 
         * @param cellPadding
         *                    the padding to apply.
         * @return this configuration.
         */
        public Config<R> cellPadding(Insets cellPadding) {
            this.cellPadding = cellPadding;
            return this;
        }

        /**
         * Background color to apply. This overrides any that is present on the selected
         * style.
         * 
         * @param color
         *              the color to apply.
         * @return this configuration.
         */
        public Config<R> color(Color color) {
            this.color = color;
            return this;
        }

        /**
         * Adds a header to the table.
         * 
         * @param label
         *              the header label.
         * @return the header for further configuration.
         */
        public Header header(String label) {
            Header header = new Header (label);
            headers.add (header);
            return header;
        }

        /**
         * Adds a header to the table.
         * 
         * @param label
         *               the header label.
         * @param config
         *               (optional) to further configure the header.
         * @return this configuration instance.
         */
        public Config<R> header(String label, Consumer<Header> config) {
            Header header = header (label);
            if (config != null)
                config.accept (header);
            return this;
        }

        /**
         * Convenience to call {@link #scrollable(boolean)} passing {@code true}.
         */
        public Config<R> scrollable() {
            return scrollable (true);
        }

        /**
         * Determines if the gallery should be scrollable or not.
         * 
         * @param scrollable
         *                   {@code false} if not (default is {@code true}).
         * @return this configuration instance.
         */
        public Config<R> scrollable(boolean scrollable) {
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
        public Config<R> renderOnRefresh(boolean renderOnRefresh) {
            this.renderOnRefresh = renderOnRefresh;
            return this;
        }
        
        /**
         * See {@link #selectable(boolean)}. Convenience to pass through {@code true}.
         */
        public Config<R> selectable() {
            return selectable (true);
        }
        
        /**
         * Determines if the rows of the table should be selectable.
         * 
         * @param selectable
         *                   {@code true} if items are selectable.
         * @return this configuration instance.
         */
        public Config<R> selectable(boolean selectable) {
            this.selectable = selectable;
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
        public Config<R> emptyUnfiltered(Consumer<ElementBuilder> render) {
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
        public Config<R> emptyFiltered(Consumer<ElementBuilder> render) {
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
        public Config<R> emptyError(BiConsumer<ElementBuilder,String> render) {
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

    private boolean selectionRunning;

    /**
     * Construct a table.
     * 
     * @param store
     *              the backing store.
     */
    public Table(IStore<R> store) {
        this (new Table.Config<R> (), store);
    }

    /**
     * Construct a table.
     * 
     * @param config
     *               configuration for the gallery.
     * @param store
     *               the backing store.
     */
    @SuppressWarnings("unchecked")
    public Table(Config<R> config, IStore<R> store) {
        super (config);
        this.store = store;
        this.store.addListener (IStoreLoadingListener.create (s -> {
            if (Status.UNLOADED == store.getStatus ())
                hideEmpty ();
            showLoading (Status.UNLOADED != store.getStatus ());
        }, s -> {
            hideLoading ();
            if (IStore.Status.ERROR == store.getStatus())
                showEmpty (EmptyState.ERROR, store.getStatusMessage ());
            else if (store.empty ())
                showEmpty ((Status.FILTERED == store.getStatus ()) ? EmptyState.FILTERED : EmptyState.UNFILTER, null);
            else
                hideEmpty ();
        }));
        this.store.addListener (IStoreChangedListener.create (s -> {
            renderRecords (getStore ())
                .isTrue (() -> onScrolledToBottomOfPage ())
                .isFalse (() -> {
                    // If false then we defer and re-check (to be safe).
                    TimerSupport.defer (() -> endOfPage ().isTrue (() -> onScrolledToBottomOfPage ()));
                })
                .isUndetermined (() -> {
                    // We just wait a little in this case.
                    TimerSupport.timer (() -> endOfPage ().isTrue (() -> onScrolledToBottomOfPage ()), 100);
                });
        }));
        if (config.selectable && (this.store instanceof IStoreSelection)) {
            ((IStoreSelection<R>) this.store).handleOnSelectionChanged (s-> {
                if (selectionRunning)
                    return;
                List<R> selection = s.selection ();
                currentRecords.forEach (r -> {
                    if (selection.contains (r.record))
                        r.updateSelection (true);
                    else
                        r.updateSelection (false);
                });
            });
        }

        // This is used to check if the page needs further loading.
        setMonitorWindowResize (true);

        // Event handler for processing actions within a record. This delegates to the
        // record wrapper.
        registerEventHandler (new IUIEventHandler () {

            @Override
            public boolean handleEvent(UIEvent event) {
                if (!event.isEvent (UIEventType.ONCLICK))
                    return false;

                // Try to find the relevant row (quickly).
                Node el = event.getTarget ();
                Node cellEl = null;
                while (el != null) {
                    if (el.parentNode == contentEl)
                        break;
                    cellEl = el;
                    el = el.parentNode;
                }
                if ((el == null) || (cellEl == null))
                    return false;

                // Extract the row element (which is mapped to by an wrapper).
                for (RecordWrapper wrapper : currentRecords) {
                    if (wrapper.getElement () == el) {
                        if (wrapper.handleUIEvent (event, (Element) cellEl)) {
                            event.stopEvent ();
                            return true;
                        }
                        return false;
                    }
                }
                return false;
            }
        });
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
     * Resets the state of the table (namely the headers).
     */
    public void reset() {
        if (!isRendered ())
            return;
        Config<R>.Header headerToActivate = null;
        LOOP: for (Config<R>.Header header : config ().headers) {
            if (!header.sortable)
                continue;
            if (headerToActivate == null)
                headerToActivate = header;
            if (header.sorted) {
                headerToActivate = header;
                break LOOP;
            }
        }
        if (headerToActivate != null)
            sort (headerToActivate, null, true);
    }

    /**
     * Invoked when there is a change in sort direction or header.
     * <p>
     * The default behaviour is to invoked any sort handler on the header.
     * 
     * @param header
     *                  the header.
     * @param direction
     *                  the direction.
     */
    protected void onHeaderSort(Config<R>.Header header, SortDirection direction) {
        if (header.sortHandler != null)
            header.sortHandler.accept (direction);
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

        reset ();

        // If the store has content then we render that content.
        if (!store.empty ()) {
            hideLoading ();
            renderRecords (getStore ());
        }
    }

    /********************************************************************
     * Rendering.
     ********************************************************************/

    /**
     * The element that scrolls (is used for auto-pagination).
     */
    protected Element scrollerEl;

    /**
     * Content area.
     */
    protected Element contentEl;

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
     * The empty area (filtered).
     */
    protected JQueryElement headerEl;

    /**
     * Sort against the given header.
     * 
     * @param header
     *                  the header.
     * @param direction
     *                  the direction to sort.
     * @param quiet
     *                  if no notification should be performed.
     */
    public void sort(Config<R>.Header header, SortDirection direction, boolean quiet) {
        if (!header.sortable)
            return;

        // Extract the relevant header.
        for (Element el : headerEl.find ("th").get ()) {
            try {
                int idx = Integer.parseInt (el.getAttribute ("item"));
                Config<R>.Header headerIndexed = config ().headers.get (idx);
                if (header == headerIndexed) {
                    sort (el, header, direction, quiet);
                    return;
                }
            } catch (Throwable e) {
                // Not to worry.
            }
        }
    }

    /**
     * Applies a sort.
     * 
     * @param el
     *                  the element.
     * @param header
     *                  the header.
     * @param direction
     *                  the direction.
     * @param quiet
     *                  {@code true} if no notification should occur.
     */
    private void sort(Element el, Config<R>.Header header, SortDirection direction, boolean quiet) {
        // Clear the sort for all other headers.
        headerEl.find ("th").removeClass (styles ().ascending ()).removeClass (styles ().descending ());

        // Apply any defaults.
        if (direction == null)
            direction = header.direction;

        // Apply the sort to this header.
        if (SortDirection.ASC == direction) {
            el.classList.add (styles ().ascending ());
        } else {
            el.classList.add (styles ().descending ());
        }

        // Notify.
        if (!quiet)
            onHeaderSort (header, direction);
    }

    /**
     * Set when {@link #showLoading()} is invoked prior to rendering so the state
     * can be applied when rendered.
     */
    private boolean preRenderShowLoading = true;

    /**
     * Shows the table loading mask and indicator.
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
     * Hide the table loading mask and indicator.
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

    /********************************************************************************
     * Rendering of the main table (including headers) and management of associated
     * events.
     ********************************************************************************/

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#buildNode(elemental2.dom.Element,
     *      com.effacy.jui.core.client.component.Component.Config)
     */
    @Override
    protected INodeProvider buildNode(Element el, Config<R> data) {
        return DomBuilder.el (el, root-> {
            if (config().color != null)
                root.css (CSS.BACKGROUND_COLOR, config().color);
            Div.$ (root).$ (outer -> {
                outer.by ("scroller");
                outer.on (e -> onGalleryScroll (e), UIEventType.ONSCROLL);
                outer.style (styles ().table ());
                outer.table (table -> {
                    Thead.$ (table).$ (header -> {
                        Tr.$ (header).$ (tr -> {
                            tr.by ("header");
                            if (data.selectable) {
                                // Here we need a gap for the selector column.
                                Th.$ (tr).style (styles ().selector ());
                            }
                            Itr.forEach (data.headers, (c,h) -> {
                                Th.$ (tr).$ (th -> {
                                    th.attr ("item", "" + c.index ());
                                    th.css ("width", (h.width == null) ? null : h.width.value ());
                                    if (h.sortable) {
                                        th.style (styles ().sortable ());
                                        th.on (e -> handleHeaderClick (e), UIEventType.ONCLICK);
                                    }
                                    Div.$ (th).$ (inner -> {
                                        if (!StringSupport.empty (h.icon)) {
                                            Em.$ (inner).$ (icon -> {
                                                icon.style (h.icon, styles ().icon ());
                                            });
                                        }
                                        Span.$ (inner).$ (hdr -> {
                                            hdr.style (styles ().header ());
                                            hdr.text (h.label);
                                        });
                                        if (h.sortable) {
                                            Span.$ (inner).$ (sorter -> {
                                                sorter.style (styles ().sortable ());
                                                Em.$ (sorter).style (data.getStyle ().iconAscending (), styles ().ascending ());
                                                Em.$ (sorter).style (data.getStyle ().iconDescending (), styles ().descending ());
                                                if (data.getStyle().iconSortable() != null)
                                                    Em.$ (sorter).style (data.getStyle ().iconSortable(), styles().sortable());
                                            });
                                        }
                                    });
                                });
                            });
                        });
                    });
                    Tbody.$ (table).$ (gallery -> {
                        gallery.by ("body");
                    });
                    Div.$ (table).$ (empty -> {
                        empty.by ("empty");
                        empty.addClassName (styles ().empty ());
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
                });
            });
            Div.$ (root).$ (mask -> {
                mask.style (styles ().mask ());
                Div.$ (mask).$ (inner -> {
                    inner.style (Theme.styles ().loader ());
                });
            });
        }).build (tree -> {
            headerEl = JQuery.$ ((Element) tree.first ("header"));
            emptyUnfilteredEl = JQuery.$ ((Element) tree.first ("unfiltered"));
            emptyFilteredEl = JQuery.$ ((Element) tree.first ("filtered"));
            emptyErrorEl = JQuery.$ ((Element) tree.first ("error"));
            contentEl = tree.first ("body");
            scrollerEl = tree.first ("scroller");
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
     * Handles a click on any of the action in the empty results panels.
     */
    protected void handleHeaderClick(UIEvent e) {
        Element el = e.getTarget ("th", 5);
        if (el == null)
            return;

        int idx = Integer.parseInt (el.getAttribute ("item"));
        Config<R>.Header header = config ().headers.get (idx);
        if (el.classList.contains (styles ().descending ())) {
            sort (el, header, SortDirection.ASC, false);
        } else if (el.classList.contains (styles ().ascending ())) {
            sort (el, header, SortDirection.DESC, false);
        } else {
            sort (el, header, null, false);
        }
    }

    /**
     * Invoked when the scrolling area has scrolled. This will call
     * {@link #endOfPage()} to determine if the bottom of the scroll areas has been
     * reached and if so will load the next batch of records (via a call to
     * {@link #onScrolledToBottomOfPage()}).
     */
    protected void onGalleryScroll(UIEvent e) {
        endOfPage ()
            .isTrue (() -> onScrolledToBottomOfPage ())
            .isUndetermined (() -> {
                // We just wait a little in this case.
                TimerSupport.timer (() -> endOfPage ().isTrue (() -> onScrolledToBottomOfPage ()), 100);
            });
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
    public Tribool endOfPage() {
        if (scrollerEl == null)
            return Tribool.FALSE;
        if ((scrollerEl.clientHeight == 0) || (contentEl.clientHeight == 0)) {
            // If there is no client height then we may have just been shown and the layout
            // has not been determined.
            return Tribool.UNDETERMINED;
        }
        if (scrollerEl.scrollTop + scrollerEl.clientHeight >= (scrollerEl.scrollHeight - 10)) {
            // Logger.log ("endOfPage: scrollTop=" + contentEl.scrollTop + " clientHeight="
            //  +
            //  contentEl.clientHeight + " (" + (contentEl.scrollTop +
            //  contentEl.clientHeight) + ") scrollHeight=" + contentEl.scrollHeight + " (" +
            //  (contentEl.scrollHeight - 10) + ")");
            return Tribool.TRUE;
        }
        return Tribool.FALSE;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#onWindowResize(int, int)
     */
    @Override
    protected void onWindowResize(int width, int height) {
        if (isRendered () && endOfPage ().isTrue ())
            onScrolledToBottomOfPage ();
    }

    /**
     * Invoked when the scroll area has scrolled to the bottom. This invokes the
     * store to load the next batch of results.
     * <p>
     * This is called by {@link #onGalleryScroll(UIEvent)} and
     * {@link #onWindowResize(int, int)} (the latter to catch any change in windows
     * size that may expose the bottom of the scroll area).
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

    /********************************************************************************
     * Rendering of records in the table.
     ********************************************************************************/

    private List<RecordWrapper> currentRecords = new ArrayList<> ();

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
    protected int indexOf(Object record) {
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
    public Tribool renderRecords(IStore<R> store) {
        List<R> records = store.asList ();

        // Can't do anything if not rendered yet.
        if (!isRendered ())
            return Tribool.FALSE;

        // Make sure we have something to render to.
        if (contentEl == null)
            return Tribool.FALSE;

        // Remove any empty and error styles.
        contentEl.classList.remove (styles ().empty ());

        // Check for and render the empty case if relevant.
        if (records.isEmpty ()) {
            for (RecordWrapper item : currentRecords) {
                item.discard ();
            }
            currentRecords.clear ();
            DomSupport.removeAllChildren (contentEl);
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
            currentPositions[i] = indexOf (record);
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
        List<RecordWrapper> revisedRecords = new ArrayList<> ();

        for (R record : records) {
            // Get the current position of the record we are inserting.
            int currentPosition = currentPositions[idx];

            RecordWrapper item;
            boolean newRecord = false;
            if (currentPosition == -1) {
                // New element we are inserting.
                item = new RecordWrapper ();
                addList.add (item.getElement ());
                DomSupport.insertChild (contentEl, item.getElement (), idx);
                adjustment--;
            } else {
                // Remove everything up to the element.
                int distaceAway = currentPosition - adjustment - idx;
                while (distaceAway > 0) {
                    contentEl.removeChild (contentEl.childNodes.getAt (idx));
                    distaceAway--;
                    adjustment++;
                }
                item = currentRecords.get (currentPosition);
            }
            newRecord = item.setRecord (record);
            revisedRecords.add (item);

            // Render the record (only if we are refreshing old records or
            // it is a new record).
            if (config ().renderOnRefresh || newRecord)
                item.render ();

            // Update the selection state.
            boolean selected = false;
            if (store instanceof IStoreSelection)
                selected = ((IStoreSelection<Object>) store).isSelected (record);

            item.updateSelection (selected);
            idx++;
        }

        // Clean up any extra elements.
        int size = contentEl.childElementCount;
        while (idx++ < size)
            contentEl.removeChild (contentEl.lastChild);

        // Assign positional classes to elements (accounting for additions
        // and removals).
        for (int j = 0, len = revisedRecords.size (); j < len; j++) {
            RecordWrapper item = revisedRecords.get (j);
            Element itemEl = item.getElement ();
            itemEl.id = "sl-gallery-item-" + j;
            // item.updatePosition (rowPos, colPos);
        }

        // Set the current records (discarding old ones).
        for (RecordWrapper item : currentRecords) {
            if (!revisedRecords.contains (item)) {
                item.discard ();
            }
        }
        currentRecords.clear ();
        currentRecords.addAll (revisedRecords);

        return endOfPage ();
    }

    /**
     * Embodies a single item in a gallery view. This is responsible for binding the
     * gallery renderer (which could also be a component), the underlying data
     * record and presentation of the surrounding wrapper.
     */
    class RecordWrapper {

        /**
         * The top-level element of the item.
         */
        private Element el;

        /**
         * Any selector element for the row.
         */
        private HTMLInputElement selectorEl;

        /**
         * The individal header elements.
         */
        private Element[] headerEl;

        /**
         * Registered handles for the cell.
         */
        private ITableCellHandler<?>[] handlers;

        /**
         * The underlying record being rendered.
         */
        private R record;

        /**
         * Flag to determine if rendering has been performed.
         */
        private boolean rendered;

        /**
         * Updates the display state for being (or not being) selected.
         * 
         * @param selected
         *                 {@code true} if selected, otherwise {@code false} if not
         *                 selected.
         * @return {@code true} if the state was changed.
         */
        public boolean updateSelection(boolean selected) {
            if (selectorEl == null)
                return false;
            if (selectorEl.checked == selected)
                return false;
            selectorEl.checked = selected;
            return true;
        }

        /**
         * The underlying record.
         * 
         * @return the record.
         */
        public R getRecord() {
            return record;
        }

        /**
         * Assigns the record to render.
         * 
         * @param record
         *               the record.
         * @return {@code true} if changed (i.e. the prior record was different).
         */
        public boolean setRecord(R record) {
            if (this.record == record)
                return false;
            if ((this.record != null) && (record != null) && this.record.equals (record))
                return false;
            this.record = record;
            return true;
        }

        public void discard() {
        }

        public Element getElement() {
            build ();
            return el;
        }

        /**
         * Renders the item into the element.
         */
        @SuppressWarnings("unchecked")
        public void render() {
            build ();
            int i = 0;
            handlers = new ITableCellHandler<?>[headerEl.length];
            for (Config<R>.Header header : config ().headers) {
                if (header.renderer == null)
                    DomSupport.innerText (headerEl[i], "No renderer");
                else
                    handlers[i] = header.renderer.render (headerEl[i], record);
                i++;
            }
            if ((selectorEl != null) && ((store instanceof IStoreSelection) && ((IStoreSelection<R>) store).isSelected(record)))
                selectorEl.checked = true;
            rendered = true;
        }

        private void build() {
            if (el != null)
                return;
            el = DomSupport.createTR ();
            if (config().selectable) {
                Element selectorTd = DomSupport.createTD (el);
                selectorTd.classList.add (styles ().selector ());
                selectorEl = DomSupport.createInput (selectorTd, input -> {
                    input.setAttribute ("type", "checkbox");
                    UIEventType.ONCLICK.attach (input);
                });
            }
            headerEl = new Element[config ().headers.size ()];
            for (int i = 0; i < headerEl.length; i++) {
                headerEl[i] = DomSupport.createTD (el);
                if (config().cellPadding != null)
                    CSS.PADDING.apply (headerEl[i], config().cellPadding);
            }
        }

        /**
         * Invoked to handle a UI event on a cell.
         * 
         * @param event
         *                 the event.
         * @param headerEl
         *                 the header element that the event maps to (for efficiency).
         * @return {@code true} if the header handled the event.
         */
        @SuppressWarnings("unchecked")
        protected boolean handleUIEvent(UIEvent event, Element headerEl) {
            if (event.getTarget() == selectorEl) {
                if (store instanceof IStoreSelection) {
                    try {
                        // Setting this prevents the store processing the subsequent change event (since
                        // it orginates here).
                        selectionRunning = true;
                        if (selectorEl.checked)
                            ((IStoreSelection<R>) store).select (record);
                        else
                            ((IStoreSelection<R>) store).unselect (record);
                    } finally {
                        selectionRunning = false;
                    }
                }
                return false;
            }
            for (int i = 0; i < this.headerEl.length; i++) {
                if (this.headerEl[i] == headerEl) {
                    if (this.handlers[i] == null)
                        return false;
                    return ((ITableCellHandler<Object>) this.handlers[i]).handleUIEvent (event, record);
                }
            }
            return false;
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

        public String table();

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

        public String ascending();

        public String descending();

        public String sortable();

        public String header();

        public String selector();

        public String icon();

    }

    /**
     * Component CSS (horizontal).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        "com/effacy/jui/ui/client/table/Table.css",
        "com/effacy/jui/ui/client/table/Table_Standard.css",
        "com/effacy/jui/ui/client/table/Table_Standard_Override.css"
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
