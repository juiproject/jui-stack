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

import com.effacy.jui.core.client.dom.builder.ContainerBuilder;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.core.client.dom.css.Length;

public class Box {
    
    public static BoxFragment $() {
        return new BoxFragment ();
    }

    public static BoxFragment $(IDomInsertableContainer<?> parent) {
        BoxFragment frg = $ ();
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    public static class BoxFragment extends BaseFragmentWithChildren<BoxFragment> {

        private boolean row;

        private Length gap;

        public BoxFragment row() {
            return row (true);
        }
        
        public BoxFragment col() {
            return row (false);
        }

        public BoxFragment row(boolean row) {
            this.row = row;
            return this;
        }

        public BoxFragment gap(Length gap) {
            this.gap = gap;
            return this;
        }

        @Override
        public void build(ContainerBuilder<?> parent) {
            parent.div (stack -> {
                stack.style("juiStack");
                if (row)
                    stack.css ("flexDirection", "row");
                if (gap != null)
                    stack.css ("gap", gap.value ());
                super.build (stack);
            });
        }
    }
    
}
