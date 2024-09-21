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
package com.effacy.jui.core.client.control;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.effacy.jui.core.client.observable.IListener;

public interface IInvalidListener extends IListener {

    public void onInvalidated(IControl<?> ctl, List<String> messages);


    public void onClearInvalidated(IControl<?> ctl);

    public static IInvalidListener create(BiConsumer<IControl<?>,List<String>> invalid, Consumer<IControl<?>> clear) {
        return new IInvalidListener() {

            @Override
            public void onInvalidated(IControl<?> ctl, List<String> messages) {
                if (invalid != null)
                    invalid.accept(ctl, messages);
            }

            @Override
            public void onClearInvalidated(IControl<?> ctl) {
                if (clear != null)
                    clear.accept (ctl);
            }
            
        };
    }
}
