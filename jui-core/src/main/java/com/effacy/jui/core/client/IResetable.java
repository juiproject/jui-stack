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

import com.effacy.jui.core.client.component.IComponent;
import com.effacy.jui.platform.util.client.Logger;

/**
 * Supports the ability to be reset.
 *
 * @author Jeremy Buckley
 */
public interface IResetable {

    /**
     * Perform a reset.
     */
    default public void reset() {
        if (this instanceof IComponent) {
            ((IComponent) this).forEach (cpt -> {
                if (cpt instanceof IResetable) {
                    try {
                        ((IResetable) cpt).reset ();
                    } catch (Throwable e) {
                        Logger.reportUncaughtException (e);
                    }
                }
            });
        }
    }
}
