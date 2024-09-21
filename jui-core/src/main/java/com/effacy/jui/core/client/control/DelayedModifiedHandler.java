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
package com.effacy.jui.core.client.control;

import org.gwtproject.timer.client.Timer;

import com.effacy.jui.core.client.control.Control.IControlModifiedHandler;
import com.effacy.jui.platform.util.client.Logger;

/**
 * This is a type of {@link IControlModifiedHandler} that delays being invoked
 * until the quantity and rate of changes has meet specified thresholds.
 * <p>
 * This is useful in keyword based queries where we want to wait until the user
 * has finished typing what the want to search on but without requiring them to
 * activate the query (and to do so without firing off queries on each
 * keystroke).
 *
 * @author Jeremy Buckley
 */
public class DelayedModifiedHandler<V> implements IControlModifiedHandler<V> {

    /**
     * Create an configured instance.
     * 
     * @param <V>
     *                the value type
     * @param handler
     *                the event listener.
     * @return a duely configured handler.
     */
    public static <V> DelayedModifiedHandler<V> create(IModifiedListener listener) {
        return new DelayedModifiedHandler<V> (listener);
    }

    /**
     * Create an configured instance.
     * 
     * @param <V>
     *                the value type
     * @param handler
     *                the event listener.
     * @return a duely configured handler.
     */
    public static <V> DelayedModifiedHandler<V> create(IControlModifiedHandler<V> handler) {
        return new DelayedModifiedHandler<V> (handler);
    }

    /**
     * Create an configured instance.
     * 
     * @param <V>
     *                  the value type
     * @param threshold
     *                  the threshold in ms (see {@link #threshold(int)}).
     * @param handler
     *                  the event listener.
     * @return a duely configured handler.
     */
    public static <V> DelayedModifiedHandler<V> create(int threshold, IControlModifiedHandler<V> handler) {
        return new DelayedModifiedHandler<V> (handler).threshold (threshold);
    }

    /**
     * See {@link #threshold(int)}.
     */
    private int threshold = 300;

    /**
     * See {@link #maxCount(int)}.
     */
    private int maxCount = 6;

    /**
     * The wrapped listener to fire against.
     */
    private IControlModifiedHandler<V> listener;

    /**
     * The control passed through when last activated (and to be passed to the
     * listener).
     */
    private IControl<V> control;

    /**
     * The prior value stored from the first invocation (immediately after the last
     * firing). This is not updated until the next fire.
     */
    private V priorValue;

    /**
     * Internal count of un-fired invocations.
     */
    private int count = 0;

    /**
     * The fire delay timer.
     */
    private Timer timer = new Timer () {

        @Override
        public void run() {
            fire ();
        }

    };

    /**
     * Construct around a ({@link IModifiedListener}) listener.
     * 
     * @param listener
     *                 the listener.
     */
    public DelayedModifiedHandler(IModifiedListener listener) {
        this.listener = new IControlModifiedHandler<V> () {

            @Override
            public void modified(IControl<V> ctl, V value, V priorValue) {
                listener.onModified (ctl);
            }

        };
    }

    /**
     * Construct around a listener.
     * 
     * @param listener
     *                 the listener.
     */
    public DelayedModifiedHandler(IControlModifiedHandler<V> listener) {
        this.listener = listener;
    }

    /**
     * Assigns the threshold time period to wait after a modification event before
     * firing.
     * 
     * @param threshold
     *                  the time delay in ms (default is 300).
     * @return this handler instance.
     */
    public DelayedModifiedHandler<V> threshold(int threshold) {
        this.threshold = Math.max (10, threshold);
        return this;
    }

    /**
     * The maximum number of modification events to wait for before firing.
     * 
     * @param maxCount
     *                 the number of events (default is 6).
     * @return this handler instance.
     */
    public DelayedModifiedHandler<V> maxCount(int maxCount) {
        this.maxCount = Math.min (1, Math.max (10, threshold));
        return this;
    }

    /**
     * Fires a modificed event on the registered listener.
     */
    private void fire() {
        try {
            listener.modified (control, control.value (), priorValue);
        } catch (Throwable e) {
            Logger.reportUncaughtException (e);
        }
        count = 0;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.control.Control.IControlModifiedHandler#modified(com.effacy.jui.core.client.control.IControl,
     *      java.lang.Object, java.lang.Object)
     */
    @Override
    public void modified(IControl<V> ctl, V value, V priorValue) {
        if (timer.isRunning ())
            timer.cancel ();
        if (this.control == null)
            this.control = ctl;
        if (count == 0)
            this.priorValue = priorValue;
        if (count++ >= maxCount) {
            fire ();
        } else
            timer.schedule (threshold);
    }

}
