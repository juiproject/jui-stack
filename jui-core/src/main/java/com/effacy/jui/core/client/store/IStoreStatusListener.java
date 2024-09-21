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
package com.effacy.jui.core.client.store;

import com.effacy.jui.core.client.observable.IListener;
import com.effacy.jui.core.client.store.IStore.Status;
import com.effacy.jui.core.client.util.TriConsumer;

/**
 * Invoked when a store changes its status.
 */
public interface IStoreStatusListener extends IListener {

    public void onStoreStatucChanged(IStore<?> store, Status status, Status prior);

    /**
     * Convenience to create a listener.
     */
    public static <V> IStoreStatusListener create(final TriConsumer<? super IStore<V>, Status, Status> listener) {
        return new IStoreStatusListener () {

            @Override
            @SuppressWarnings("unchecked")
            public void onStoreStatucChanged(IStore<?> store, Status status, Status prior) {
                listener.accept ((IStore<V>) store, status, prior);
            }

        };
    }
}
