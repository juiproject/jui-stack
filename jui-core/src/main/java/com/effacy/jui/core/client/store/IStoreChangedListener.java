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

import java.util.function.Consumer;

import com.effacy.jui.core.client.observable.IListener;

public interface IStoreChangedListener extends IListener {

    public void onStoreChanged(IStore<?> store);

    /**
     * Convenience to create a listener.
     */
    public static <V> IStoreChangedListener create(Consumer<IStore<V>> listener) {
        return new IStoreChangedListener () {

            @Override
            @SuppressWarnings("unchecked")
            public void onStoreChanged(IStore<?> store) {
                listener.accept ((IStore<V>) store);
            }

        };
    }
}
