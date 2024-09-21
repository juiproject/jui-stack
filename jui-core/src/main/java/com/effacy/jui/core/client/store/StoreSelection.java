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

import com.effacy.jui.core.client.observable.Observable;

/**
 * Standard implememtation of {@link IStoreSelection}.
 *
 * @author Jeremy Buckley
 */
public class StoreSelection<V> extends Observable implements IStoreSelection<V> {

    /**
     * Selected items.
     */
    private ListStore<V> selection = new ListStore<> ();
    {
        selection.addListener (new IStoreChangedListener () {

            @Override
            public void onStoreChanged(IStore<?> store) {
                StoreSelection.this.fireEvent (IStoreSelectionListener.class).onSelectionChanged (StoreSelection.this);
            }
        });
    }

    @Override
    public IStoreMutable<V> selectionAsStore() {
        return selection;
    }

    @Override
    public void handleOnSelectionChanged(Consumer<IStoreSelection<V>> handler) {
        if (handler == null)
            return;
        selection.addListener (new IStoreChangedListener () {

            @Override
            public void onStoreChanged(IStore<?> store) {
                handler.accept (StoreSelection.this);
            }
        });
    }
    
}
