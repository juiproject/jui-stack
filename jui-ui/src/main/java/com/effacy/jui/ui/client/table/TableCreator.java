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
package com.effacy.jui.ui.client.table;

import java.util.function.Consumer;

import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.core.client.store.IStore;

public class TableCreator {

    public static <R> Table<R> $(IDomInsertableContainer<?> parent, Consumer<Table.Config<R>> configurer, IStore<R> store) {
        Table<R> table = build (configurer, store);
        parent.insert (table);
        return table;
    }

    public static <R> Table<R> build(Consumer<Table.Config<R>> configurer, IStore<R> store) {
        Table.Config<R> config = new Table.Config<R> ();
        if (configurer != null)
            configurer.accept (config);
        return new Table<R> (config, store);
    }
}
