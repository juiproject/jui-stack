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
package com.effacy.jui.core.client.component;

import java.util.function.Consumer;

import com.effacy.jui.core.client.observable.IListener;

/**
 * Emitted when {@link IComponent#activate()} is called.
 *
 * @author Jeremy Buckley
 */
public interface IActivateListener extends IListener {

    public void onActivate(IComponent cpt);

    public static IActivateListener create(Consumer<IComponent> activate) {
        return new IActivateListener () {

            @Override
            public void onActivate(IComponent value) {
                if (activate != null)
                    activate.accept (value);
            }

        };
    }
}
