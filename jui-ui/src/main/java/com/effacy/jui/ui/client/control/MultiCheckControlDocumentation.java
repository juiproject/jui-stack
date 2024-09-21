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
package com.effacy.jui.ui.client.control;

import com.effacy.jui.ui.client.explorer.ComponentDocumentation;

public class MultiCheckControlDocumentation {
    
    public static ComponentDocumentation documentation() {
        return new ComponentDocumentation.Config ()
            .title ("Multi-check")
            .className (MultiCheckControl.class.getCanonicalName())
            .description ("A single control that supports multiple options that can be selected from.")
            .example (MultiCheckControlCreator.<String> create ().label ("Select from").left ()
                .option ("yes", "Yes")
                .option ("no", "No")
                .option ("na", "N/A")
                .build())
        .build ();
    }

}
