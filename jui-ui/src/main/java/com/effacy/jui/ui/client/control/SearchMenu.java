package com.effacy.jui.ui.client.control;

import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.SimpleComponent;
import com.effacy.jui.core.client.control.DelayedValueHandler;
import com.effacy.jui.core.client.dom.DomSupport;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.UIEvent;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.ContainerBuilder;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.ExistingElementBuilder;
import com.effacy.jui.core.client.dom.builder.Li;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.builder.Text;
import com.effacy.jui.core.client.dom.builder.Ul;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.jquery.JQuery;
import com.effacy.jui.core.client.store.FilteredStore;
import com.effacy.jui.core.client.store.IFilteredStore;
import com.effacy.jui.core.client.store.IPaginatedStore;
import com.effacy.jui.core.client.store.ISearchStore;
import com.effacy.jui.core.client.store.IStore;
import com.effacy.jui.core.client.store.IStore.Status;
import com.effacy.jui.core.client.store.IStoreChangedListener;
import com.effacy.jui.core.client.store.IStoreLoadingListener;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;
import elemental2.dom.ScrollIntoViewOptions;

public class SearchMenu<V> extends SimpleComponent implements ISearchMenu<V> {

    /**
     * Configuration for the menu.
     */
    private ISearchMenuConfig<V> config;

    /**
     * CSS styles.
     */
    private ISearchMenuCSS styles;

    /**
     * The element that contains items to select from.
     */
    protected Element itemsEl;

    /**
     * A filtered version of the store.
     */
    private IStore<V> store;

    /**
     * If the store is loading as a result of scrolling.
     */
    private boolean scrolling = false;

    /**
     * Used by {@link #onSearchKeyPress(UIEvent)} to buffer the keypresses prior to
     * initiating a search.
     */
    private DelayedValueHandler<String> searchDelay;

    public SearchMenu(ISearchMenuConfig<V> config) {
        this (config, null);
    }

    public SearchMenu(ISearchMenuConfig<V> config, ISearchMenuCSS styles) {
        this.config = config;
        this.styles = (styles == null) ? StandardSearchMenuCSS.instance() : styles;

        // Attach a listener to the store to refresh against.
        this.store = config.getStore ();
        if (!(config.getStore () instanceof IFilteredStore) && !(config.getStore () instanceof IPaginatedStore) && !(config.getStore() instanceof ISearchStore)) {
            // For a vanilla store one can wrap it.
            this.store = new FilteredStore<V> (config.getStore ());
        }
        this.store.addListener (IStoreChangedListener.create (s -> {
            if (SelectionControl.DebugMode.STORE.set ())
                Logger.trace ("[selector-menu]", "{store-changed} size=" + store.size() + " status=" + store.getStatus().name());
                onResults (store.size() > 0);
                _refresh();
        }));
        this.store.addListener (IStoreLoadingListener.create (before -> {
            if (config.isUseMaskOnLoad())
                DomSupport.mask (itemsEl);
        }, after -> {
            // if (SelectionControl.DebugMode.STORE.set ())
            //     Logger.trace ("[selector-menu]", "{store-afterload} size=" + store.size() + " status=" + store.getStatus().name ());
            if (config.isUseMaskOnLoad())
                DomSupport.unmask (itemsEl);
            if (this.store instanceof IPaginatedStore) {
                if (IStore.Status.ERROR == this.store.getStatus()) {
                    // This is an error state.
                    // TODO: Mark this up visually.
                    scrolling = false;
                    Logger.error ("An error occurred retrieving content");
                }
            }
        }));
    }

    /************************************************************************
     * Events
     ************************************************************************/

    /**
     * Invoked when there are results.
     * 
     * @param hasResults {@code true} if there are results.
     */
    protected void onResults(boolean hasResults) {
        // Nothing.
    }

    /************************************************************************
     * Behaviours
     ************************************************************************/

    @Override
    public void search(String value) {
        if (searchDelay == null) {
            searchDelay = new DelayedValueHandler<String> (keywords -> {
                if (store instanceof ISearchStore) {
                    if (StringSupport.empty (keywords))
                        ((ISearchStore<V>) this.store).clearFilter ();
                    else
                        ((ISearchStore<V>) this.store).filter (keywords);
                } else if (store instanceof IFilteredStore) {
                    if (StringSupport.empty (keywords))
                        ((IFilteredStore<V>) this.store).clearFilter ();
                    else
                        ((IFilteredStore<V>) this.store).filter (v -> filterMatch (keywords, v));
                }
            });
            searchDelay.maxCount (config.getSearchBufferCountThreshold ());
            searchDelay.threshold (config.getSearchBufferTimeThreshold ());
        }
        searchDelay.modified (value);
    }

    @Override
    public void up() {
        _move(false);
    }

    @Override
    public void down() {
        _move(true);
    }

    @Override
    public void select() {
        Element[] els = JQuery.$ (itemsEl).find ("li").get ();
        int i = 0;
        for (; i < els.length; i++) {
            if (els[i].classList.contains (styles ().active ())) {
                // We have found the active item.
                _select (Integer.parseInt (els[i].getAttribute ("item")));
            }
        }
    }

    @Override
    public void reset() {
        if (searchDelay != null)
            searchDelay.reset();
        if (store instanceof IPaginatedStore) {
            ((IPaginatedStore<V>) store).clear();
        } else if (store instanceof ISearchStore) {
            ((ISearchStore<V>) this.store).clearFilter();
        } else if (store instanceof IFilteredStore) {
            ((IFilteredStore<V>) this.store).clearFilter ();
        }
    }

    /************************************************************************
     * Events
     ************************************************************************/

    /**
     * Moves the selector up or down.
     * 
     * @param down
     *             {@code true} to move down.
     */
    protected void _move(boolean down) {
        Element[] els = JQuery.$ (itemsEl).find ("li").get ();
        int i = 0;
        for (; i < els.length; i++) {
            if (els[i].classList.contains (styles ().active ())) {
                els[i].classList.remove (styles ().active ());
                if (down)
                    i++;
                else
                    i--;
                if (i < 0)
                    i = els.length - 1;
                else if (i > els.length - 1)
                    i = 0;
                els[i].classList.add (styles ().active ());
                _scrollIntoView (els[i]);
                return;
            }
        }
    }

    /**
     * Process scrolling of the items area.
     */
    protected void onItemsScroll(UIEvent e) {
        // Do nothing if no more or is loading.
        if (!(store instanceof IPaginatedStore))
            return;
        if (store.getStatus () == Status.LOADING)
            return;
        if (store.size () == store.getTotalAvailable ())
            return;

        Element el = itemsEl.parentElement;
        if (el.scrollTop + el.clientHeight >= (el.scrollHeight - 10)) {
            IPaginatedStore<V> pstore = store.cast ();
            if (SelectionControl.DebugMode.STORE.set ())
                Logger.trace ("[selector-menu]", "{store-scroll} batchsize=" + config.getStoreBatchSize ());
            scrolling = true;
            pstore.load (0, pstore.getPageSize () + config.getStoreBatchSize ());
        }
    }

    /**
     * Process a hover over a selection item. This will activate the item (with
     * CSS). See {@link #_activate(int, boolean)}.
     */
    protected void onItemHover(UIEvent e) {
        Element targetEl = e.getTarget ("li", 4);
        if (targetEl == null)
            return;
        if (targetEl.classList.contains (styles ().active ()))
            return;
        try {
            _activate (Integer.parseInt (targetEl.getAttribute ("item")), false);
        } catch (Throwable ex) {
            // Not to worry.
        }
    }

    /**
     * Activates the given item by index into the (filtered) store.
     * <p>
     * This works by assignment of CSS.
     * 
     * @param idx
     *                       the index into the (filtered) store.
     * @param scrollIntoView
     *                       {@code true} if to scroll the list of items to view.
     * @return {@code true} if an activation was successfully performed (i.e. the
     *         index was in range).
     */
    protected boolean _activate(int idx, boolean scrollIntoView) {
        if (idx < 0)
            return false;
        Element[] els = JQuery.$ (itemsEl).find ("li").get ();
        if (idx >= els.length)
            return false;
        if (els[idx].classList.contains (styles ().active ()))
            return true;
        for (int i = 0; i < els.length; i++) {
            if (i != idx) {
                els[i].classList.remove (styles ().active ());
            } else {
                els[i].classList.add (styles ().active ());
                if (scrollIntoView)
                    _scrollIntoView (els[i]);
            }
        }
        return true;
    }

    /**
     * Implements scrolling of the given element into view.
     * 
     * @param el
     *           the element (being a selectable item).
     */
    protected void _scrollIntoView(Element el) {
        ScrollIntoViewOptions options = ScrollIntoViewOptions.create ();
        options.setBlock ("nearest");
        el.scrollIntoView (options);
    }

    /**
     * Action a click on a selection item. See {@link #_select(int)}.
     */
    protected void onItemClick(UIEvent e) {
        Element targetEl = e.getTarget ("li", 4);
        if (targetEl == null)
            return;
        // Ignore if loading.
        if (targetEl.parentElement.classList.contains(styles().list_spinner()))
            return;
        try {
            _select (Integer.parseInt (targetEl.getAttribute ("item")));
        } catch (Throwable ex) {
            Logger.reportUncaughtException (ex, this);
        }
    }

    /**
     * Selects the specified item. If the item does not exist it will be treated as
     * a clear.
     * 
     * @param idx
     *            the item index (use any value less than zero for clear selection).
     */
    protected void _select(int idx) {
        if (idx < 0)
            onSelect (null);
        else
            onSelect (this.store.get (idx));
    }

    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$ (el).$ (root -> {
            Div.$ (root).style (styles ().list ())
                .on (e -> onItemsScroll (e), UIEventType.ONSCROLL)
                .on (e -> onItemClick (e), UIEventType.ONMOUSEDOWN).$ (
                Ul.$ ().id ("items").by ("items").on (e -> onItemHover (e), UIEventType.ONMOUSEENTER)
            );
            Div.$ (root).style (styles ().empty ()).$ (
                Text.$ ("No results found")
            );
        }).build (tree -> {
            itemsEl = tree.first ("items");
        });
    }

    protected void _refresh () {
        int index = 0;

        // Remove an spinner style which may have been added on the last build.
        itemsEl.classList.remove (styles ().list_spinner ());

        // Make sure there is a store.
        if (store == null)
            return;

        ExistingElementBuilder items = Wrap.$ (itemsEl);

        // Determine where we are starting from.
        int idx = 0;
        if (scrolling) {
            if ((itemsEl.lastElementChild != null) && itemsEl.lastElementChild.classList.contains(styles ().list_spinner()))
                itemsEl.lastElementChild.remove();
            if ((itemsEl.childElementCount == 0) || (store.size() < itemsEl.childElementCount)) {
                scrolling = false;
            } else {
                idx = itemsEl.childElementCount;
            }
        }
        if (idx == 0)
            items.clear ();

        // Logging.
        if (SelectionControl.DebugMode.MENU.set ())
            Logger.trace ("[selector-menu]", "{refresh} idx=" + idx + " size=" + store.size() + " existing=" + itemsEl.childElementCount + " scrolling=" + scrolling);

        // Build in children.
        while (idx < store.size()) {
            V value = store.get (idx);
            ElementBuilder li = items.li ();
            // Just sink events on this, the handler has been declared on the parent element
            // (and that will handle these events) so we don't need to worry about handler
            // registration.
            li.on (UIEventType.ONMOUSEENTER);
            li.attr ("item", "" + idx);
            li.testRef ("item-" + idx);
            idx++;
            buildItem (li, value);
        }

        // Various states.
        boolean empty = store.empty ();
        boolean emptyButLoading = empty && (store instanceof IPaginatedStore) && (store.getStatus().is (IStore.Status.UNLOADED, IStore.Status.LOADING));

        // Central spinner when the store has no content and is loading (first load).
        if (emptyButLoading) {
            items.style (styles ().list_spinner ());
            Li.$ (items).$ (loading -> {
                Em.$ (loading).style (FontAwesome.sync (FontAwesome.Option.SPIN));
            });
        }
        
        // Spinner on bottom row when store is loading a new page of results.
        if ((store instanceof IPaginatedStore) && !store.empty () && (store.size () < store.getTotalAvailable ())) {
            Li.$ (items).$ (loading -> {
                loading.style (styles ().list_spinner ());
                Em.$ (loading).style (FontAwesome.sync (FontAwesome.Option.SPIN));
            });
        }
        
        // Build contents.
        items.build ();

        // Apply empty style if empty.
        if (empty && !emptyButLoading) {
            getRoot ().classList.add (styles ().empty ());
        } else {
            getRoot ().classList.remove (styles ().empty ());
            if (!_activate (index, !scrolling))
                _activate (0, !scrolling);
        }

        // Clear scrolling.
        scrolling = false;
    }

    /**
     * Builds a renderer for the underlying value as it should appear in the list of
     * values that can be selected from.
     * <p>
     * The default is to perform a {@link Object#toString()} operation to map to the
     * desired value.
     * 
     * @param root
     *             the root container.
     */
    @SuppressWarnings("rawtypes")
    protected void buildItem(ContainerBuilder el, V value) {
        Span.$ (el).text ((value == null) ? "[NULL]" : value.toString ());
    }

    /**
     * Performs a keyword filtering to check if a value matches or not.
     * <p>
     * This is only used for {@link IFilteredStore} (which is also the default store
     * type).
     * <p>
     * The default implementation performs a {@link Object#toString()} then performs
     * a containment test (case insenstive).
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
        return value.toString ().toLowerCase ().contains (keywords.toLowerCase ());
    }

    /**
     * Invoked when a selection has been made.
     * 
     * @param value
     *              the value of the selection.
     */
    protected void onSelect(V value) {
        // Nothing.
    }

    /********************************************************************
     * CSS with standard styles.
     ********************************************************************/

    /**
     * Styles (made available to selection).
     */
    protected ISearchMenuCSS styles() {
        return styles;
    }

    /**
     * CSS for the selector menu.
     */
    public static interface ISearchMenuCSS extends IComponentCSS {

        public String search();

        public String add();

        public String search_hide();

        public String search_clear();

        public String list();

        public String active();

        public String clear();

        public String empty();

        public String list_spinner();

        public String fixed();

    }

    /**
     * Component CSS (horizontal).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        "com/effacy/jui/ui/client/control/SearchMenu.css",
        "com/effacy/jui/ui/client/control/SearchMenu_Override.css"
    })
    public static abstract class StandardSearchMenuCSS implements ISearchMenuCSS {

        private static StandardSearchMenuCSS STYLES;

        public static ISearchMenuCSS instance() {
            if (STYLES == null) {
                STYLES = (StandardSearchMenuCSS) GWT.create (StandardSearchMenuCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }
}

