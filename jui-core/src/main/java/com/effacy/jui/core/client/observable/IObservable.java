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
package com.effacy.jui.core.client.observable;

/**
 * Represents something that can be observed by way of listeners.
 * 
 * @author Jeremy Buckley
 */
public interface IObservable {

    /**
     * Adds a listener. Note that the order of listener execution is the same as
     * they were added.
     * <p>
     * If creating an anonymous class within the method call then make sure you
     * cast the anonymous class with its listener. There appears to be a GWT bug
     * that results in odd class access issues if this is not done.
     * 
     * @param listener
     *            the listener to add.
     */
    public <L extends IListener> L addListener(L listener);


    /**
     * Removes the listeners.
     * 
     * @param listeners
     *            the listeners to remove.
     */
    public void removeListener(IListener... listeners);


    /**
     * Removes all the listeners and observables.
     */
    public void removeAllListeners();


    /**
     * Convey events from this observable onto the passed observable (the latter
     * will treat such events is if they were fired directly against the
     * observable).
     * 
     * @param observable
     *            the observable to convey events to.
     * @param listenerTypes
     *            the types of listeners to convey (if none is specified then
     *            all is assumed).
     */
    @SuppressWarnings("unchecked")
    public void convey(IObservable observable, Class<? extends IListener>... listenerTypes);


    /**
     * Removes observables that were conveyed events.
     * 
     * @param observables
     *            the observable(s) to remove.
     */
    public void removeObservable(IObservable... observables);


    /**
     * Used to fire an event. This returns an implementation of the listener of
     * the given class type that will dispatch to all registered listeners of
     * the same type and method invocation.
     * 
     * @param listenerClass
     *            the listener class to locate.
     * @param listeners
     *            any additional listeners to include.
     * @return The listener (or {@code null} if not found).
     */
    public <L extends IListener> L fireEvent(Class<L> listenerClass, IListener... listeners);
}
