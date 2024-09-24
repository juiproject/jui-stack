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

import java.util.function.Consumer;

import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.platform.util.client.TimerSupport;
import com.effacy.jui.platform.util.client.TimerSupport.ITimer;

/**
 * See {@link DelayedModifiedHandler}.
 */
public class DelayedValueHandler<V> {

    /**
     * See {@link #threshold(int)}.
     */
    protected int threshold = 300;

    /**
     * See {@link #maxCount(int)}.
     */
    protected int maxCount = 6;

    /**
     * Internal count of un-fired invocations.
     */
    private int count = 0;

    /**
     * The last assigned value.
     */
    private V value;

    /**
     * The fire delay timer.
     */
    protected ITimer timer = TimerSupport.timer(() -> fire ());

    /**
     * Receives the value.
     */
    private Consumer<V> receiver;

    /**
     * Construct with what will receive the values.
     * 
     * @param receiver
     *                 the receiver of the values.
     */
    public DelayedValueHandler(Consumer<V> receiver) {
        this.receiver = receiver;
    }

    /**
     * Assigns the threshold time period to wait after a modification event before
     * firing.
     * <p>
     * Cannot be less than 10ms.
     * 
     * @param threshold
     *                  the time delay in ms (default is 300).
     * @return this handler instance.
     */
    public DelayedValueHandler<V> threshold(int threshold) {
        this.threshold = Math.max (10, threshold);
        return this;
    }

    /**
     * The maximum number of modification events to wait for before firing.
     * <p>
     * Cannot be less than 1 or greater than 10.
     * 
     * @param maxCount
     *                 the number of events (default is 6).
     * @return this handler instance.
     */
    public DelayedValueHandler<V> maxCount(int maxCount) {
        this.maxCount = Math.max (1, Math.min (10, maxCount));
        return this;
    }

    /**
     * Fires a receiver event.
     */
    protected void fire() {
        try {
            receiver.accept (value);
        } catch (Throwable e) {
            Logger.reportUncaughtException (e);
        }
        count = 0;
    }

    /**
     * Modifies the value.
     * 
     * @param value
     *              the value.
     */
    public void modified(V value) {
        timer.cancel ();
        this.value = value;
        if (count++ >= maxCount)
            fire ();
        else
            timer.schedule (threshold);
    }

    /**
     * Resets the handler clearing the current count, timer and sets the value to
     * {@code null}.
     */
    public void reset() {
        count = 0;
        timer.cancel();
        value = null;
    }

}
