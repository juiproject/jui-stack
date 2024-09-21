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
package com.effacy.jui.core.client.store;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.gwtproject.timer.client.Timer;

import com.effacy.jui.core.client.store.IStoreReplacable.IReplacementComparator;
import com.effacy.jui.platform.util.client.Logger;

/**
 * Sometimes a store will load with data for presentation and some of that data
 * is in a state of transition (for example, a record that is part-way through a
 * background process). In such cases the user may need to wait until the record
 * is in a stable state to act upon it.
 * <p>
 * Rather than have the user regularly (and manually) refresh the presentation
 * one can automatically poll for changes and when changes occur update the
 * presentation. This performs this role for store-backed data.
 * <p>
 * The mechanism should act upon a store and be invoked when the store updates
 * (extrenally).
 */
public class AutoRefresher<T> {

    /**
     * Used to debug the autorefresher.
     */
    public static boolean DEBUG = false;

    /**
     * Refresh processor for performing a refresh of items.
     */
    @FunctionalInterface
    public interface IRefreshProcessor<T> {

        /**
         * Given a list of items needing to be refreshed, obtain the refreshes of those
         * items. Any items that have since changed (as determined with the comparator)
         * will be replaced in the store.
         * <p>
         * Note that if an error occurs while refreshing then simply return a
         * {@code null} or empty list. The refresh process will continue.
         * 
         * @param items
         *                 the items needing refreshing.
         * @param callback
         *                 invoked when the refreshed items have been obtained.
         */
        public void refresh(List<T> items, Consumer<List<T>> callback);
    }

    /**
     * The store whose contents are being managed.
     */
    private IStoreReplacable<T> store;

    /**
     * The number of milliseconds before initiating a refresh call.
     */
    private int schedule;

    /**
     * Determines if the refresher is active (or stopped).
     */
    private boolean active;

    /**
     * Timer used to delay between refreshes.
     */
    private Timer timer;

    /**
     * To retrieve records for refresh.
     */
    private IRefreshProcessor<T> processor;

    /**
     * To compare records for replacement.
     */
    private IReplacementComparator<T> comparator;

    /**
     * To test record for needing refreshing.
     */
    private Predicate<T> test;

    /**
     * Construct a refresher.
     * 
     * @param store
     *                 the backing store.
     * @param schedule
     *                 the ms between initaiting a refresh and when one occurs.
     */
    public AutoRefresher(IStoreReplacable<T> store, int schedule) {
        this.store = store;
        this.schedule = schedule;

        // Add a listener so that when the store changes we kick-off a refresh.
        this.store.addListener (IStoreChangedListener.create (s -> {
            if (active) {
                if (DEBUG)
                    Logger.trace ("autorefresh", "Store changed, requesting refresh");
                refresh ();
            }
        }));
    }

    /**
     * Construct a refresher.
     * 
     * @param store
     *                   the backing store.
     * @param schedule
     *                   the ms between initaiting a refresh and when one occurs.
     * @param processor
     *                   see {@link #processor(IRefreshProcessor)}.
     * @param comparator
     *                   see {@link #comparator(IReplacementComparator)}.
     * @param test
     *                   see {@link #test(Predicate)}.
     */
    public AutoRefresher(IStoreReplacable<T> store, int schedule, IRefreshProcessor<T> processor, IReplacementComparator<T> comparator, Predicate<T> test) {
        this.store = store;
        this.schedule = schedule;
        this.processor = processor;
        this.comparator = comparator;
        this.test = test;
    }

    /**
     * Assigns a processor used to retrieve replacement items.
     * 
     * @param processor
     *                  the processor.
     * @return this refresher instance.
     */
    public AutoRefresher<T> processor(IRefreshProcessor<T> processor) {
        this.processor = processor;
        return this;
    }

    /**
     * Assigns a comparator (see
     * {@link IStoreReplacable#replace(List, IReplacementComparator)}). Used to
     * determine if a record in the store should be replaced by a given record.
     * 
     * @param comparator
     *                   the comparator (if {@code null} then a simple equality
     *                   check is performed).
     * @return this refresher instance.
     */
    public AutoRefresher<T> comparator(IReplacementComparator<T> comparator) {
        this.comparator = comparator;
        return this;
    }

    /**
     * Assigns a test predicate to determine if a record in the store needs to be
     * placed on the refresh list.
     * 
     * @param test
     *             the test predicate.
     * @return this refresher instance.
     */
    public AutoRefresher<T> test(Predicate<T> test) {
        this.test = test;
        return this;
    }

    /**
     * See {@link #activate(boolean)}; convenience to pass {@code true}.
     */
    public AutoRefresher<T> activate() {
        return activate (true);
    }
    
    /**
     * See {@link #activate(boolean)}; convenience to pass {@code false}.
     */
    public AutoRefresher<T> deactivate() {
        return activate (false);
    }

    /**
     * Activates or de-activates the refresher.
     * <p>
     * If de-activating the any refresh in progress will be stopped.
     * 
     * @param active
     *               {@code true} to activate.
     * @return this refresher instance.
     */
    public AutoRefresher<T> activate(boolean active) {
        if (DEBUG) {
            if (active)
                Logger.trace ("autorefresh", "Activated");
            else
                Logger.trace ("autorefresh", "De-activated");
        }
        this.active = active;
        if (active)
            this.active = true;
        else
            stop ();
        return this;
    }

    
    /**
     * Stops the refresher timer (won't halt if one is in middle of processing).
     */
    public void stop() {
        if (timer != null) {
            timer.cancel ();
            timer = null;
        }
    }

    /**
     * Initiates a refresh.
     * <p>
     * This will only work if the refresher is active. Further, it will continue to
     * refresh (while active) until nothing more needs refreshing.
     * <p>
     * In general this does not need to be called directly as it will be invoked
     * when a store change event is detected.
     */
    public void refresh() {
        // Refresher needs to be active.
        if (!active)
            return;

        if (DEBUG) {
            if (timer != null)
                Logger.trace ("autorefresh", "Refresh called but already running");
            else
                Logger.trace ("autorefresh", "Refresh called, starting refresh cycle");
        }

        // Check if a refresh is in progress.
        if (timer != null)
            return;
        
        // Determines the items needing refreshing and start a refresh.
        List<T> items = new ArrayList<>();
        for (T item : store) {
            if (test.test (item))
                items.add (item); 
        }

        if (DEBUG) {
            if (items.isEmpty())
                Logger.trace ("autorefresh", "Refresh detected no items to refresh, stopping");
            else
                Logger.trace ("autorefresh", "Refresh detected " + items.size() + " item(s) to refresh, starting");
        }
        if (!items.isEmpty())
            _refresh (items, schedule);
    }

    /**
     * Called by {@link #refresh()}.
     */
    protected void _refresh(List<T> items, int delay) {
        if (delay <= 0) {
            _refresh (items);
            return;
        }
        stop ();
        timer = new Timer() {
            public void run () {
                if (active)
                    _refresh (items);
            }
        };
        if (DEBUG)
            Logger.trace ("autorefresh", "Scheduled for " + delay + "ms");
        timer.schedule (delay);
    }

    /**
     * Called by {@link #_refresh(List, int)}.
     */
    protected void _refresh(List<T> items) {
        if (processor == null)
            return;
        if (DEBUG)
            Logger.trace ("autorefresh", "Invoking refresh query");
        processor.refresh (items, updates -> {
            if (DEBUG) {
                if ((updates == null) || updates.isEmpty())
                    Logger.trace ("autorefresh", "Query returned no updates");
                else
                    Logger.trace ("autorefresh", "Query returned " + updates.size() + " update(s)");
            }
            if (updates != null)
                store.replace (updates, comparator);
            stop ();
            if (DEBUG) {
                if (active)
                    Logger.trace ("autorefresh", "Query initiating next refresh");
                else
                    Logger.trace ("autorefresh", "No longer active, stopping");
            }
            if (active)
                refresh ();
        });
    }
}
