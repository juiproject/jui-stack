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

import org.gwtproject.core.client.Scheduler;
import org.gwtproject.timer.client.Timer;


/**
 * Useful set of tools for delayed actions.
 *
 * @author Jeremy Buckley
 */
public class TimerSupport {

    /**
     * Creates a timer based on the passed runnable.
     * 
     * @param runner
     *               the runnable to run.
     * @return the timer.
     */
    public static ITimer timer(final Runnable runner) {
        Timer timer = new Timer () {

            @Override
            public void run() {
                runner.run ();
            }

        };
        return new ITimer () {

            @Override
            public void schedule(int millis) {
                timer.schedule (millis);
            }

            @Override
            public void repeat(int millis) {
                timer.scheduleRepeating (millis);
            }

            @Override
            public void cancel() {
                timer.cancel ();
            }

            @Override
            public void run() {
                timer.run ();
            }

        };
    }

    /**
     * As per {@link #timer(Runnable)} but immediately schedules to run after the
     * given delay adjusted by the passed start time (from the current time).
     * <p>
     * This can be used to enforce a minimum delay.
     * 
     * @param runner
     *                    the runnable to run.
     * @param delayMillis
     *                    the delay to run in ms.
     * @param start
     *                    the starting time (using
     *                    {@link System#currentTimeMillis()}).
     */
    public static void timer(Runnable runner, int delayMillis, long start) {
        delayMillis = delayMillis - (int) (System.currentTimeMillis () - start);
        if (delayMillis < 0)
            runner.run ();
        else
            timer (runner, delayMillis);
    }

    /**
     * As per {@link #timer(Runnable)} but immediately schedules to run after the
     * given delay.
     * 
     * @param runner
     *                    the runnable to run.
     * @param delayMillis
     *                    the delay to run in ms.
     * @return the timer.
     */
    public static ITimer timer(Runnable runner, int delayMillis) {
        ITimer timer = timer (runner);
        if(delayMillis < 0)
            delayMillis = 0;
        timer.schedule (delayMillis);
        return timer;
    }

    /**
     * As per {@link #timer(Runnable)} but immediately schedules to run after the
     * given delay.
     * 
     * @param runner
     *                    the runnable to run.
     * @param delayMillis
     *                    the delay to run in ms.
     * @return the timer.
     */
    public static ITimer repeat(Runnable runner, int delayMillis) {
        ITimer timer = timer (runner);
        timer.repeat (delayMillis);
        return timer;
    }

    /**
     * Runs the passed runner when the browser event loop has completed. See
     * {@link Scheduler#scheduleDeferred(org.gwtproject.core.client.Scheduler.ScheduledCommand)}.
     * 
     * @param runner
     *               the runnable to run.
     * @return the timer.
     */
    public static void defer(Runnable runner) {
        Scheduler.get ().scheduleDeferred (() -> runner.run ());
    }

    /**
     * Timer abstraction.
     */
    public interface ITimer {

        /**
         * Start the timer with the given delay.
         * 
         * @param millis
         *               the delay in milliseconds.
         */
        public void schedule(int millis);

        /**
         * Start and repeat the timer with the given delay.
         * 
         * @param millis
         *               the delay in milliseconds.
         */
        public void repeat(int millis);

        /**
         * Cancel any active scheduling of the timer.
         */
        public void cancel();

        /**
         * Force a run of the timer. This won't have an impact on any existing schedule.
         */
        public void run();
    }
}
