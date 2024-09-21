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
package com.effacy.jui.core.client.component.layout;

import com.effacy.jui.core.client.component.layout.VertLayout.VertLayoutData.Separator;
import com.effacy.jui.core.client.dom.css.Length;

public final class VertLayoutCreator {

    public static VertLayout create() {
        return new VertLayout.Config ()
            .build();
    }

    public static VertLayout create(Length spacing) {
        return new VertLayout.Config ()
            .spacing (spacing)
            .build ();
    }

    public static VertLayout create(Separator separator) {
        return new VertLayout.Config ()
            .separator (separator)
            .build();
    }
    
    public static VertLayout create(Length spacing, Separator separator) {
        return new VertLayout.Config ()
            .spacing (spacing)
            .separator (separator)
            .build();
    }
}
