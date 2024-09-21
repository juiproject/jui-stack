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
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.store.ListStore;

public class SelectionControlDocumentation {
    public static ComponentDocumentation documentation() {
        return new ComponentDocumentation.Config ()
            .title ("Selection")
            .className (SelectionControl.class.getCanonicalName())
            .description ("A drop-selection control for selecting a single option from among a number of candidates. Includes the ability to search and naviagte using keys.")
            .example (SelectionControlCreator.<String> create (cfg -> {
                cfg.selectorLeft (false);
                cfg.allowEmpty (true);
                cfg.selectorHeight (Length.em (13));
                cfg.store (new ListStore<String> () //
                    .add ("Option 1") //
                    .add ("Option 2") //
                    .add ("Option 3") //
                    .add ("Option 4") //
                    .add ("Option 5") //
                    .add ("Option 6") //
                    .add ("Option 7") //
                    .add ("Option 8") //
                );
            }))
        .build ();
    }
}
