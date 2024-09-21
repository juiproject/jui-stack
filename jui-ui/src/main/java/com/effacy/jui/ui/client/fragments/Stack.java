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

import java.util.function.Consumer;

import com.effacy.jui.core.client.dom.builder.ContainerBuilder;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.core.client.dom.css.Length;

public class Stack {

    public static StackFragment $() {
        return $(null, null);
    }

    public static StackFragment $(IDomInsertableContainer<?> parent) {
        return $(parent, null);
    }

    public static StackFragment $(IDomInsertableContainer<?> parent, Consumer<StackFragment> builder) {
        StackFragment stack = new StackFragment();
        if (builder != null)
            builder.accept(stack);
        if (parent != null)
            parent.insert(stack);
        return stack;
    }

    public enum Align {
        START, CENTER, END, JUSTIFY;
    }

    public enum Justify {
        START, CENTER, END;
    }

    public static class StackFragment extends BaseFragmentWithChildren<StackFragment> {

        private boolean row;

        private Length gap;

        private Align align;

        private Justify justify;

        private boolean wrap;

        private boolean stretch;

        private boolean hideIfEmpty;

        /**
         * Direction is horizontal.
         * 
         * @return this fragment.
         */
        public StackFragment horizontal() {
            return horizontal(true);
        }

        /**
         * Direction is vertical.
         * 
         * @return this fragment.
         */
        public StackFragment vertical() {
            return horizontal(false);
        }

        public StackFragment horizontal(boolean row) {
            this.row = row;
            return this;
        }

        public StackFragment gap(Length gap) {
            this.gap = gap;
            return this;
        }

        public StackFragment align(Align align) {
            this.align = align;
            return this;
        }

        public StackFragment justify(Justify justify) {
            this.justify = justify;
            return this;
        }

        /**
         * Hide the stack if it has no contents.
         * 
         * @return this fragment.
         */
        public StackFragment hideIfEmpty() {
            return hideIfEmpty (true);
        }

        /**
         * Hide the stack if it has no contents.
         * 
         * @param hideIfEmpty
         *                    {@code true} if to hide if empty.
         * @return this fragment.
         */
        public StackFragment hideIfEmpty(boolean hideIfEmpty) {
            this.hideIfEmpty = hideIfEmpty;
            return this;
        }

        /**
         * Allow the contents to wrap.
         * 
         * @return this fragment.
         */
        public StackFragment wrap() {
            return wrap (true);
        }

        /**
         * Allow the contents to wrap.
         * 
         * @param wrap
         *             {@code true} to wrap contents.
         * @return this fragment.
         */
        public StackFragment wrap(boolean wrap) {
            this.wrap = wrap;
            return this;
        }

        @Override
        public void build(ContainerBuilder<?> parent) {
            if (hideIfEmpty && children.isEmpty ())
                return;
            super.build (parent);
        }

        @Override
        protected void buildInto(ElementBuilder root) {
            root.style ("juiStack");
            if (row)
                root.style ("horizontal");
            if (gap != null)
                root.css ("gap", gap.value());
            if (wrap)
                root.css ("flexWrap", "wrap");
            if (Align.START == align)
                root.css ("alignItems", "start");
            else if (Align.CENTER == align)
                root.css ("alignItems", "center");
            else if (Align.END == align)
                root.css ("alignItems", "end");
            else if (Align.JUSTIFY == align)
                root.style ("stretch");
            if (Justify.START == justify)
                root.css ("justifyItems", "start");
            else if (Justify.CENTER == justify)
                root.css ("justifyItems", "center");
            else if (Justify.END == justify)
                root.css ("justifyItems", "end");
            super.buildInto (root);
        }
    }

}
