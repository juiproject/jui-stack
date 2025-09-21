/*******************************************************************************
 * Copyright 2025 Jeremy Buckley
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

import com.effacy.jui.core.client.Invoker;
import com.effacy.jui.core.client.observable.IListener;

/**
 * Used to enable a component to signal to a controller (or owning) component
 * that it is desired to close (or make go-away in some sense). See
 * {@link ModalDialogCreator}.
 */
public interface ICloseListener extends IListener {

    /**
     * Invoke to request a close.
     */
    public void onCloseRequested();

    public static ICloseListener create(Invoker closed) {
        return new ICloseListener () {

            @Override
            public void onCloseRequested() {
                if (closed != null)
                    closed.invoke();
            }

        };
    }
}
