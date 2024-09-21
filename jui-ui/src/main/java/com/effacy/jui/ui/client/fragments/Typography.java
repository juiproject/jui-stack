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
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.core.client.dom.builder.P;

public class Typography {

    public enum Style {
        BODY1, BODY2, SUBTITLE1, SUBTITLE2;
    }

    public static TypographyFragment $() {
        return new TypographyFragment ();
    }

    public static TypographyFragment $(IDomInsertableContainer<?> parent) {
        TypographyFragment frg = $ ();
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    public static class TypographyFragment extends ATypographyFragment<TypographyFragment> {}

    public static abstract class ATypographyFragment<T extends ATypographyFragment<T>> extends BaseFragmentWithChildren<T> {

        private Style style;

        @SuppressWarnings("unchecked")
        public T style(Style style) {
            this.style = style;
            return (T) this;
        }

        @Override
        protected void buildInto(ElementBuilder root) {
            if (Style.BODY1 == style)
                root.style ("juiBody1");
            else if (Style.BODY2 == style)
                root.style ("juiBody2");
            else if (Style.SUBTITLE1 == style)
                root.style ("juiSubtitle1");
            else if (Style.SUBTITLE2 == style)
                root.style ("juiSubtitle2");
            else
                root.style ("juiTypography");
            super.buildInto(root);
        }

        @Override
        protected ElementBuilder createRoot(ContainerBuilder<?> parent) {
            if (style != null)
                return P.$ (parent);
            return Div.$ (parent);
        }

    }
}
