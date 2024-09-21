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
package com.effacy.jui.ui.client.button;

import com.effacy.jui.core.client.Invoker;
import com.effacy.jui.platform.util.client.Logger;

@FunctionalInterface
public interface IButtonHandler {

    @FunctionalInterface
    public interface IButtonActionCallback {

        /**
         * Called to indicate that the button action has been completed.
         */
        public void complete();
    }

    /**
     * Handle the action on the button in an asynchronous manner. The calls
     * {@link #handleAction()} by default and upon return completes the call-back.
     * For actions that are long running, this method can be sub-classed and the
     * sub-class can either pass through the completion call-back or handle it
     * itself.
     * 
     * @param cb
     *           called when the action has been handled.
     */
    public void handleAction(IButtonActionCallback cb);

    /**
     * Creates a self-completing handler. Avoids having to complete the callback.
     * 
     * @param handler
     *                simple handler.
     * @return the button handler.
     */
    public static IButtonHandler convert(Invoker handler) {
        return cb -> {
            if (handler != null) {
                try {
                    handler.invoke ();
                } catch (Throwable e) {
                    Logger.reportUncaughtException (e);
                }
            }
            cb.complete ();
        };
    }
}
