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

import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.SimpleComponent;
import com.effacy.jui.core.client.control.DelayedValueHandler;
import com.effacy.jui.core.client.dom.DomSupport;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.UIEvent;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.A;
import com.effacy.jui.core.client.dom.builder.ContainerBuilder;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.ExistingElementBuilder;
import com.effacy.jui.core.client.dom.builder.Input;
import com.effacy.jui.core.client.dom.builder.Li;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.builder.Text;
import com.effacy.jui.core.client.dom.builder.Ul;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.jquery.JQuery;
import com.effacy.jui.core.client.dom.jquery.JQueryElement;
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
import elemental2.dom.HTMLInputElement;
import elemental2.dom.ScrollIntoViewOptions;

/**
 * A standard selector used by {@link SelectionControl}.
 * <p>
 * This provides for a store-based presentation of items that can be searched
 * on, navigated among and selected from.
 *
 * @author Jeremy Buckley
 */
public class SelectorMenu<V> extends SimpleComponent implements ISelectorMenu<V> {

    /**
     * A filtered version of the store.
     */
    private IStore<V> store;

    /**
     * If the store is loading as a result of scrolling.
     */
    private boolean scrolling = false;

    /**
     * The value to use to lookup the active item post loading from the store. This
     * is set on {@link #_reset(Object)} when the store needs an initial load. Once
     * loaded the value is used to select an item (if it happens to be in the
     * store). This is a bit of a nice-to-have and ensure that the selected item
     * will be highlighted by default after the loading of the store, so long as the
     * item is present in that first load.
     */
    private List<V> selection = new ArrayList<>();

    /**
     * CSS styles.
     */
    private ISelectorMenuCSS styles;

    /**
     * Configuration for the selector menu.
     */
    private ISelectorMenuConfig<V> config;

    /**
     * Construct with configuration.
     * 
     * @param config
     *               the configuration to use.
     */
    public SelectorMenu(ISelectorMenuConfig<V> config) {
        this (config, StandardSelectorMenuCSS.instance ());
    }

    /**
     * Construct with configuration from the owning {@link SelectionControl}.
     * 
     * @param config
     *               the configuration.
     */
    public SelectorMenu(ISelectorMenuConfig<V> config, ISelectorMenuCSS styles) {
        this.config = config;
        this.styles = styles;

        // Attach a listener to the store to refresh against.
        this.store = config.getStore ();
        if (!(config.getStore () instanceof IFilteredStore) && !(config.getStore () instanceof IPaginatedStore) && !(config.getStore() instanceof ISearchStore)) {
            // For a vanilla store one can wrap it.
            this.store = new FilteredStore<V> (config.getStore ());
        }
        this.store.addListener (IStoreChangedListener.create (s -> {
            if (SelectionControl.DebugMode.STORE.set ())
                Logger.trace ("[selector-menu]", "{store-changed} size=" + store.size() + " status=" + store.getStatus().name());
            _refresh ();
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

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.ui.client.control.SelectionControl.ISelectionControlSelector#reset(java.lang.Object)
     */
    @Override
    public void reset(List<V> value) {
        focus ();
        _reset (value);
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

    /**
     * Invoked when a de-selection has been made.
     * 
     * @param value
     *              the value of the de-selection.
     */
    protected void onDeselect(V value) {
        // Nothing.
    }

    /**
     * Invoked when an add handler generates a value.
     * 
     * @param value
     *              the value.
     */
    protected void onAdd(V value) {
        // Nothing.
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

    /************************************************************************
     * Presentation
     ************************************************************************/

    /**
     * The search section.
     */
    protected Element searchEl;

    /**
     * The search input element.
     */
    protected HTMLInputElement searchInputEl;

    /**
     * The element that contains items to select from.
     */
    protected Element itemsEl;

    /**
     * The clear selection block.
     */
    // protected JQueryElement clearSelectionEl;

    /**
     * Action a click on clear selection.
     */
    protected void onClearSelectionClick(UIEvent e) {
        _select (-1);
        e.stopEvent ();
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
            JQueryElement input = JQuery.$ (targetEl).find ("input");
            if (input.length () > 0) {
                boolean checked = input.is(":checked");
                input.prop("checked", !checked);
                if (checked)
                    _deselect (Integer.parseInt (targetEl.getAttribute ("item")));
                else
                    _select (Integer.parseInt (targetEl.getAttribute ("item")));
            } else
                _select (Integer.parseInt (targetEl.getAttribute ("item")));
        } catch (Throwable ex) {
            Logger.reportUncaughtException (ex, this);
        }
    }

    /**
     * Used by {@link #onSearchKeyPress(UIEvent)} to buffer the keypresses prior to
     * initiating a search.
     */
    private DelayedValueHandler<String> keywordDelay;

    /**
     * Action a key press in the search section. This will activate filtering on the
     * item store.
     * <p>
     * Note that the configuration has to allow search for this to activate. This
     * permits the search to be hidden and partake in the key up and down actions
     * and to support focus.
     */
    protected void onSearchKeyPress(UIEvent e) {
        if (!config.isAllowSearch ())
            return;
        if (UIEvent.KeyCode.isArrowKey (e))
            return;
        if (StringSupport.empty (searchInputEl.value))
            searchEl.classList.remove (styles ().search_clear ());
        else
            searchEl.classList.add (styles ().search_clear ());
        if (keywordDelay == null) {
            keywordDelay = new DelayedValueHandler<String> (keywords -> {
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
            keywordDelay.maxCount (config.getSearchBufferCountThreshold ());
            keywordDelay.threshold (config.getSearchBufferTimeThreshold ());
        }
        keywordDelay.modified (StringSupport.trim (searchInputEl.value));
    }

    /**
     * Action a specific key press on the search section.
     */
    protected void onSearchKeyCheck(UIEvent e) {
        if (UIEvent.KeyCode.ENTER.is (e)) {
            _select ();
            e.stopEvent ();
        } else if (UIEvent.KeyCode.ARROW_UP.is (e)) {
            _move (false);
            e.stopEvent ();
        } else if (UIEvent.KeyCode.ARROW_DOWN.is (e)) {
            _move (true);
            e.stopEvent ();
        }
    }

    /**
     * Action a click on clearing search keywords.
     */
    protected void onClearClick(UIEvent e) {
        e.stopEvent ();
        searchInputEl.value = "";
        searchInputEl.focus ();
        searchEl.classList.remove (styles ().search_clear ());
        if (store instanceof ISearchStore) {
            if (SelectionControl.DebugMode.STORE.set ())
                Logger.trace ("[selector-menu]", "{store-clearfilter}");
            ((ISearchStore<V>) this.store).clearFilter ();
        } else if (store instanceof IFilteredStore) {
            if (SelectionControl.DebugMode.STORE.set ())
                Logger.trace ("[selector-menu]", "{store-clearfilter}");
            ((IFilteredStore<V>) this.store).clearFilter ();
        }
    }

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
     * Obtains the index of the currently active item (or {@code -1}).
     * 
     * @return the index.
     */
    protected int _activeIndex() {
        for (Element el : JQuery.$ (itemsEl).find ("li").get ()) {
            if (el.classList.contains (styles ().active ())) {
                try {
                    return Integer.parseInt (el.getAttribute ("item"));
                } catch (Throwable ex) {
                    // Not to worry.
                }
            }
        }
        return -1;
    }

    /**
     * Selects the currently selected item.
     */
    protected void _select() {
        int idx = _activeIndex ();
        if (idx >= 0)
            _select (idx);
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

    protected void _deselect(int idx) {
        onDeselect (this.store.get (idx));
    }

    /**
     * Resets the presentation of the selector with the passed value as marked as
     * currently selected.
     * 
     * @param value
     *              the value to pre-select.
     */
    public void _reset(List<V> values) {
        this.selection.clear ();
        if (values != null)
            this.selection.addAll (values);
        searchInputEl.value = "";
        searchEl.classList.remove (styles ().search_clear ());
        if (store instanceof ISearchStore)
            ((ISearchStore<V>) this.store).clearFilter ();
        else if (store instanceof IFilteredStore)
            ((IFilteredStore<V>) this.store).clearFilter ();

        if (store instanceof IPaginatedStore) {
            IPaginatedStore<V> pstore = store.cast ();
            if (config.isStoreClear () || (pstore.getStatus () == Status.UNLOADED)) {
                if (SelectionControl.DebugMode.STORE.set ())
                    Logger.trace ("[selector-menu]", "{store-clear} batchsize=" + config.getStoreBatchSize ());
                pstore.clear ();
                pstore.load (0, config.getStoreBatchSize ());
                this.selection.addAll (selection);
            }
        }
        if (this.selection.isEmpty()) {
            _refresh ();
        } else {
            if (store.getStatus () != Status.LOADING)
                _refresh ();
        }
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
     * Refreshes the list of items from the store and activates the given index.
     * <p>
     * This invokes
     * {@link SelectorMenu#buildRenderer(ContainerBuilder, Object)}
     * when building the DOM template. This allows the implementation to modify the
     * presentation as needed.
     */
    public void _refresh() {
        int index = selection.isEmpty () ? 0 : Math.max (0, store.indexOf (selection.get(selection.size() - 1)));
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
            if (config.isSelectable ())
                Input.$ (li, "checkbox").checked (contains (value));
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
     * Used to determine if the passed item appears in the current selection.
     * 
     * @param item
     *             the item to check.
     * @return {@code true} if it is present.
     */
    protected boolean contains(V item) {
        if (config.getComparator() == null)
            return selection.contains (item);
        for (V sel : selection) {
            if (config.getComparator().apply(sel, item))
                return true;
        }
        return false;
    }

    /**
     * Styles (made available to selection).
     */
    protected ISelectorMenuCSS styles() {
        return styles;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#buildNode(elemental2.dom.Element,
     *      com.effacy.jui.core.client.component.Component.Config)
     */
    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$ (el).$ (root -> {
            Div.$ (root).$ (search -> {
                search.by ("search");
                search.style (styles ().search ());
                Div.$ (search).$ (
                    Input.$ ("text").id ("search").by ("search_input")
                        .on (e -> onSearchKeyPress (e), UIEventType.ONKEYUP)
                        .on (e -> onSearchKeyCheck (e), UIEventType.ONKEYDOWN)
                        .attr ("placeholder", "Search"),
                    Em.$ ().style (FontAwesome.search ()),
                    Em.$ ().by ("search_clear")
                        .on (e -> onClearClick (e), UIEventType.ONCLICK)
                        .style (styles ().search_clear (), FontAwesome.times ()).setAttribute ("tabindex", "0")
                );
            });
            Div.$ (root).style (styles ().list ())
                .on (e -> onItemsScroll (e), UIEventType.ONSCROLL)
                .on (e -> onItemClick (e), UIEventType.ONMOUSEDOWN).$ (
                Ul.$ ().id ("items").by ("items").on (e -> onItemHover (e), UIEventType.ONMOUSEENTER)
            );
            Div.$ (root).style (styles ().empty ()).$ (
                Text.$ ("No results found")
            );
            if (config.getAddHandler() != null) {
                Div.$ (root).style (styles ().add ()).$ (add -> {
                    A.$ (add).$ (
                        Em.$ ().style (FontAwesome.plus ()),
                        Span.$ ().text (StringSupport.empty (config.getAddLabel()) ? "Add new" : config.getAddLabel())
                    ).onclick (e -> {
                        config.getAddHandler().accept (v -> {
                            onAdd(v);
                        });
                    });
                });
            }
        }).build (tree -> {
            searchEl = tree.first ("search");
            searchInputEl = (HTMLInputElement) tree.first ("search_input");
            itemsEl = tree.first ("items");
            manageFocusEl (searchInputEl);
            manageFocusEl (tree.first ("search_clear"));
            manageFocusEl (tree.first ("clear_a"));
        });
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#onAfterRender()
     */
    @Override
    protected void onAfterRender() {
        super.onAfterRender ();
        if (config.isOverflowSafe())
            getRoot ().classList.add (styles ().fixed ());
        if (!config.isAllowSearch () || (!(store instanceof IFilteredStore) && !(store instanceof ISearchStore)))
            getRoot ().classList.add (styles ().search_hide ());
        _refresh ();
    }

    /********************************************************************
     * CSS with standard styles.
     ********************************************************************/

    /**
     * CSS for the selector menu.
     */
    public static interface ISelectorMenuCSS extends IComponentCSS {

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
        "com/effacy/jui/ui/client/control/SelectorMenu.css",
        "com/effacy/jui/ui/client/control/SelectorMenu_Override.css"
    })
    public static abstract class StandardSelectorMenuCSS implements ISelectorMenuCSS {

        private static StandardSelectorMenuCSS STYLES;

        public static ISelectorMenuCSS instance() {
            if (STYLES == null) {
                STYLES = (StandardSelectorMenuCSS) GWT.create (StandardSelectorMenuCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }

}
