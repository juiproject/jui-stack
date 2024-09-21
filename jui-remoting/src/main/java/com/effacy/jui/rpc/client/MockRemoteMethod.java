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
package com.effacy.jui.rpc.client;

import java.util.List;

import org.gwtproject.core.client.Scheduler;
import org.gwtproject.core.client.Scheduler.ScheduledCommand;
import org.gwtproject.timer.client.Timer;


/**
 * Mock remote method call which
 * 
 * @author Steve Baker
 * @author Jeremy Buckley
 */
public abstract class MockRemoteMethod<T> extends RemoteMethod<T> {

    /**
     * Delay in milliseconds.
     */
    protected int debugDelay = 0;


    /**
     * Default constructor.
     */
    protected MockRemoteMethod() {
        // Nothing.
    }


    /**
     * Construct with a delay.
     * 
     * @param debugDelay
     *            the delay to use.
     */
    protected MockRemoteMethod(int debugDelay) {
        this.debugDelay = Math.max (0, debugDelay);
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.remote.client.IRemoteMethod#invoke(com.effacy.jui.remote.client.IRemoteMethodCallback,
     *      com.google.gwt.core.client.JsArray, java.lang.Object)
     */
    @Override
    public void invoke(final IRemoteMethodCallback<T> callback, final List<Object> args) {
        int debugDelay = getDebugDelay ();
        if (debugDelay > 0) {
            Timer t = new Timer () {

                /**
                 * {@inheritDoc}
                 * 
                 * @see org.gwtproject.timer.client.Timer#run()
                 */
                @Override
                public void run() {
                    try {
                        dispatchResponse (callback, process (args));
                    } catch (Throwable e) {
                        if (callback != null) {
                            callback.onTransportError (e.getLocalizedMessage ());
                        }
                    }
                }
            };
            t.schedule (debugDelay);
        } else {
            Scheduler.get ().scheduleDeferred (new ScheduledCommand () {

                public void execute() {
                    try {
                        dispatchResponse (callback, process (args));
                    } catch (Throwable e) {
                        if (callback != null) {
                            callback.onTransportError (e.getLocalizedMessage ());
                        }
                    }
                }
            });
        }
    }


    /**
     * Gets the debug delay time.
     * 
     * @return The debug delay.
     */
    protected int getDebugDelay() {
        return this.debugDelay;
    }


    /**
     * Perform the actual processing of the method.
     * 
     * @param args
     *            the arguments to pass to the method.
     * @return The response as would be received from the server.
     */
    protected abstract RemoteResponse<T> process(Object... args);

}
