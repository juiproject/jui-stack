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
package com.effacy.jui.core.client;

import java.util.Collection;

import com.effacy.jui.core.client.component.IDisposeListener;
import com.effacy.jui.core.client.observable.IListener;
import com.effacy.jui.core.client.observable.ListenerOracle;
import com.effacy.jui.core.client.store.IStoreAfterLoadListener;
import com.effacy.jui.core.client.store.IStoreBeforeLoadListener;
import com.effacy.jui.core.client.store.IStoreChangedListener;

/**
 * A variation of the {@link ListenerOracle} for unit testing. This is supported
 * by the {@link GWTTestBridge}.
 */
public class ListenerOracleMock extends ListenerOracle {

    @Override
    public <L extends IListener> L find(Class<L> klass, Collection<IListener> listeners, String debugString) {
        L listener = super.find (klass, listeners, debugString);
        if (listener != null)
            return listener;
        if (klass == IDisposeListener.class)
            return (L) IDisposeListener.create (cpt -> {});
        if (klass == IStoreChangedListener.class)
            return (L) IStoreChangedListener.create (str -> {});
        if (klass == IStoreAfterLoadListener.class)
            return (L) IStoreAfterLoadListener.create (str -> {});
        if (klass == IStoreBeforeLoadListener.class)
            return (L) IStoreBeforeLoadListener.create (str -> {});
        return null;
    }
}
