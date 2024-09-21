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
package com.effacy.jui.core.client.navigation;

import com.effacy.jui.core.client.component.IComponent;
import com.effacy.jui.platform.util.client.Logger;

/**
 * This is intended to be implemented by children of a {@link INavigationAware}
 * component that should recieve activation and de-activation events.
 */
public interface INavigationAwareChild {

    default public void onNavigateDeactivated() {
        if (NavigationHandler.DebugMode.ACTIVATE.set ())
            Logger.log ("{nav-activation-child:deactivate} [" + toString() + "]");
        if (this instanceof IComponent) {
            ((IComponent) this).forEach (child -> {
                if (child instanceof INavigationAwareChild)
                    ((INavigationAwareChild) child).onNavigateDeactivated ();
            });
        }
    }

    default public void onNavigateActivated() {
            Logger.log ("{nav-activation-child:activate} [" + toString() + "]");
        if (this instanceof IComponent) {
            ((IComponent) this).forEach (child -> {
                if (child instanceof INavigationAwareChild)
                    ((INavigationAwareChild) child).onNavigateActivated ();
            });
        }
    }
    
}
