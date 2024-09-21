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
package com.effacy.jui.ui.client.fragments;

import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;

public class Paper {

    public static PaperFragment $() {
        return new PaperFragment ();
    }

    public static PaperFragment $(IDomInsertableContainer<?> parent) {
        PaperFragment frg = $ ();
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    public static class PaperFragment extends APaperFragment<PaperFragment> {}

    public static abstract class APaperFragment<T extends APaperFragment<T>> extends BaseFragmentWithChildren<T> {

        @Override
        protected void buildInto(ElementBuilder root) {
            root.style ("juiPaper");
            super.buildInto(root);
        }

    }
}
