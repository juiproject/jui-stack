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
package com.effacy.jui.ui.client.fragments;

import com.effacy.jui.core.client.dom.builder.Html;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.ui.client.fragments.Dialog.Action;
import com.effacy.jui.ui.client.fragments.Dialog.DialogFragment;

public class DialogDocumentation {

    public static DialogFragment example1(IDomInsertableContainer<?> root) {
        return Dialog.$(root)
            .css("width: 400px;")
            .title("A simple dialog")
            .onclose(() -> Logger.info("CLOSE"))
            .variant(Dialog.Variant.PLAIN)
            .action(Action.left("clear", Btn.Variant.TEXT, Btn.Nature.GREY, () -> Logger.info("CLEAR")))
            .action(Action.right("Apply", Btn.Variant.STANDARD_EXPANDED, () -> Logger.info("APPLY"))).$ (
                Html.$("""
                    <div style='padding: 0.5em 1.5em; color: #666;'>
                        <p>This is a simple dialog with some simple contents, not much else.</p>
                    </div>     
                """)
            );
    }
}
