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

import java.util.function.BiConsumer;

import com.effacy.jui.core.client.observable.IListener;

public interface IEnableDisableListener extends IListener {

    public void onEnable(IComponent cpt);


    public void onDisable(IComponent cpt);



    /**
     * Creates a listener from the given consumer. The second argument will be
     * {@code true} when being enabled, otherwise is false.
     * 
     * @param handler
     *                the consumer to invoke.
     * @return the listener.
     */
    public static IEnableDisableListener create(BiConsumer<IComponent,Boolean> handler) {
        return new IEnableDisableListener() {

            @Override
            public void onEnable(IComponent cpt) {
                if (handler != null)
                    handler.accept(cpt, true);
            }

            @Override
            public void onDisable(IComponent cpt) {
                if (handler != null)
                    handler.accept(cpt, false);
            }

        };
    }
}
