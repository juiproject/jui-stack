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
package com.effacy.jui.platform.util.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Represents a standard promise that can be used to return an async value (i.e.
 * via a timer).
 *
 * @author Jeremy Buckley
 */
public class Promise<V> {

    /**
     * Creates a primed (unfulfilled) promise.
     * 
     * @return the promise ready for use.
     */
    public static <V> Promise<V> create() {
        return new Promise<V> ();
    }

    /**
     * Creates a completed (fulfilled) promise.
     * 
     * @param value
     *              the value of the promise.
     * @return the promise.
     */
    public static <V> Promise<V> create(V value) {
        return new Promise<V> ().fulfill (value);
    }

    /**
     * Listeners waiting for the promise to fulfill.
     */
    private List<Consumer<V>> listeners;

    /**
     * The fulfilled value of the promise.
     */
    private V value;

    /**
     * Flags the promise as having been fulfilled.
     */
    private boolean fulfilled = false;

    /**
     * Fulfill a promise.
     * <p>
     * All listeners will be notified and then removed. Any future listeners will be
     * notified immediately (upon being added).
     * 
     * @param value
     *              the value of the promise.
     * @return this promise.
     */
    public Promise<V> fulfill(V value) {
        this.value = value;
        this.fulfilled = true;
        if (listeners != null) {
            listeners.forEach (l -> {
                try {
                    l.accept (value);
                } catch (Throwable e) {
                    Logger.reportUncaughtException (e);
                }
            });
            listeners.clear ();
            listeners = null;
        }
        return this;
    }

    /**
     * Adds a listener to be fulfilled. This may be invoked immediately (if the
     * promise is already fulfilled) otherwise will wait.
     * 
     * @param listener
     *                 the listener to wait.
     * @return this promise instance.
     */
    public Promise<V> onFulfillment(Consumer<V> listener) {
        if (listener == null)
            return this;
        if (fulfilled) {
            try {
                listener.accept (value);
            } catch (Throwable e) {
                Logger.reportUncaughtException (e);
            }
        } else {
            if (listeners == null)
                listeners = new ArrayList<> ();
            listeners.add (listener);
        }
        return this;
    }
}
