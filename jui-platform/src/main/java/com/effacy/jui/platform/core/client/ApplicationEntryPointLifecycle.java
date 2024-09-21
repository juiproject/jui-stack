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
package com.effacy.jui.platform.core.client;

import java.util.ArrayList;
import java.util.List;

import org.gwtproject.timer.client.Timer;

import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.platform.util.client.ScriptInjector;

/**
 * Allows for blocking of the loading of an application (via
 * {@link ApplicationEntryPoint#onApplicationLoad()}).
 * <p>
 * One may obtain block future such loads by calling {@link #delay()}. This
 * returns a {@link ApplicationEntryPointLifecycle.Callback} that can be used to
 * remove the block when the blocking operation has completed.
 * <p>
 * This is typically used during script loading to prevent the application from
 * loading until necessary scripts are loaded. Due to the nature of the
 * operation it is only effective when called from a module initialiser (entry
 * point), of course that is really the only case where it is needed. An example
 * is {@link ScriptInjector} which uses this internally.
 */
public class ApplicationEntryPointLifecycle {

    /**
     * Set to {@code true} to enable debugging logging.
     */
    public static boolean DEBUG = false;

    /**
     * Callback that is returned by {@link ApplicationEntryPointLifecycle#delay()}.
     * This should be invoked when the delay is not longer needed.
     */
    public interface Callback {
    
        /**
         * On completion of the delaying (application module blocking) operation.
         */
        public void complete();
    }

    /**
     * Delays loading of the application until the passed callback is marked as
     * complete (by calling {@link Callback#complete()}).
     * 
     * @return the callback to cancel the block.
     */
    public static Callback delay() {
        if (DEBUG)
            Logger.trace ("[ApplicationEntryPointLifecycle]", "delay obtained");
        Callback handler = new Callback() {
            public void complete() {
                if (DEBUG)
                    Logger.trace ("[ApplicationEntryPointLifecycle]", "delay released");
                HANDLERS.remove(this);
            }
        };
        HANDLERS.add (handler);
        return handler;
    }

    /**
     * Called by {@link ApplicationEntryPoint#onModuleLoad()} to effect a delayed
     * loading of the application. When all blocks have been removed
     * {@link ApplicationEntryPoint#onApplicationLoad()} will be called.
     * 
     * @param entryPoint
     *                   the invoking entry point.
     * @param timeout
     *                   a timeout for blocking operations to completed within
     *                   (before load will proceed regardless).
     */
    static void start(ApplicationEntryPoint entryPoint, long timeout) {
        if (DEBUG)
            Logger.trace ("[ApplicationEntryPointLifecycle]", "start load with timout " + timeout + "ms");
        if (HANDLERS.isEmpty()) {
            if (DEBUG)
                Logger.trace ("[ApplicationEntryPointLifecycle]", "loading application (0ms)");
            entryPoint.onApplicationLoad();
            return;
        }
        long startTime = System.currentTimeMillis();
        new Timer() {

            @Override
            public void run() {
                if ((System.currentTimeMillis() - startTime) > timeout) {
                    Logger.warn ("ApplicationEntryPointLifecycle timout exceeded, proceeding....");
                    entryPoint.onApplicationLoad();
                    return;
                }
                if (HANDLERS.isEmpty()) {
                    if (DEBUG)
                        Logger.trace ("[ApplicationEntryPointLifecycle]", "loading application (" + (System.currentTimeMillis() - startTime) + "ms)");
                    entryPoint.onApplicationLoad();
                    return;
                }
                if (DEBUG)
                    Logger.trace ("[ApplicationEntryPointLifecycle]", "waiting for another 10ms");
                schedule(10);
            }

        }.schedule(10);
        
    }

    /**
     * List of registered blocking handler callbacks.
     */
    private static List<Callback> HANDLERS = new ArrayList<>();

}
