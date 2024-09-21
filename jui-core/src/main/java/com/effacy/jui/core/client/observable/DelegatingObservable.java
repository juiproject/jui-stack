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
 * Observable that delegates to another observable.
 * 
 * @author Jeremy Buckley
 */
public abstract class DelegatingObservable<O extends IObservable> implements IObservable {

    /**
     * The delegate.
     */
    private O delegate;


    /**
     * Construct with delegate.
     * 
     * @param delegate
     *            the delegate.
     */
    protected DelegatingObservable(O delegate) {
        this.delegate = delegate;
        assert (delegate != null) : "Delegate cannot be assigned a null delegate";
    }


    /**
     * Obtains the delegate.
     * 
     * @return The delegate.
     */
    protected O delegate() {
        return delegate;
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.core.client.observable.IObservable#addListener(com.effacy.jui.core.client.observable.IListener)
     */
    @Override
    public <L extends IListener> L addListener(L listener) {
        return delegate.addListener (listener);
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.core.client.observable.IObservable#removeListener(com.effacy.jui.core.client.observable.IListener[])
     */
    @Override
    public void removeListener(IListener... listeners) {
        delegate.removeListener (listeners);
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.core.client.observable.IObservable#removeAllListeners()
     */
    @Override
    public void removeAllListeners() {
        delegate.removeAllListeners ();
    }


    /**
     * {@inheritDoc}
     * 
     * @see 
     *      com.effacy.gwt.common.client.event.IObservable#addObservable(com.effacy
     *      .gwt.common.client.event.IObservable, java.lang.Class<? extends
     *      com.effacy.gwt.common.client.event.IListener>[])
     */
    @SuppressWarnings("unchecked")
    @Override
    public void convey(IObservable observable, Class<? extends IListener>... listenerTypes) {
        delegate.convey (observable, listenerTypes);
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.core.client.observable.IObservable#removeObservable(com.effacy.jui.core.client.observable.IObservable[])
     */
    @Override
    public void removeObservable(IObservable... observables) {
        delegate.removeObservable (observables);
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.core.client.observable.IObservable#fireEvent(java.lang.Class,
     *      com.effacy.jui.core.client.observable.IListener[])
     */
    @Override
    public <L extends IListener> L fireEvent(Class<L> listenerClass, IListener... listeners) {
        return delegate.fireEvent (listenerClass, listeners);
    }

}
