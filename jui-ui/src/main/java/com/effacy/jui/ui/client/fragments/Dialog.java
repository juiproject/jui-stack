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

import java.util.ArrayList;
import java.util.List;

import com.effacy.jui.core.client.Invoker;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.H4;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.ui.client.fragments.Paper.APaperFragment;
import com.effacy.jui.ui.client.icon.FontAwesome;

/**
 * Fragment that presents a simple dialog with title, close action, body content
 * and footer for actions.
 */
public class Dialog {

    /**
     * Construct a dialog fragment.
     * 
     * @return the fragment.
     */
    public static DialogFragment $() {
        return new DialogFragment ();
    }

    /**
     * Construct a dialog fragment and insert into the parent.
     * 
     * @param parent
     *               the parent to insert into.
     * @return the fragment.
     */
    public static DialogFragment $(IDomInsertableContainer<?> parent) {
        DialogFragment frg = $ ();
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    /************************************************************************
     * Implementation
     ************************************************************************/

     /**
     * Represents an action.
     */
    public static record Action(String label, Btn.Variant variant, Btn.Nature nature, String icon, boolean left, Invoker handler) {

        /**
         * Create a right-located action.
         * 
         * @param label
         *                the label.
         * @param handler
         *                the click handler.
         * @return the action.
         */
        public static Action right(String label, Invoker handler) {
            return new Action(label, Btn.Variant.STANDARD, Btn.Nature.NORMAL, null, false, handler);
        }

        /**
         * Create a right-located action.
         * 
         * @param label
         *                the label.
         * @param variant
         *                the button variant.
         * @param handler
         *                the click handler.
         * @return the action.
         */
        public static Action right(String label, Btn.Variant variant, Invoker handler) {
            return new Action(label, (variant == null) ? Btn.Variant.STANDARD : variant, Btn.Nature.NORMAL, null, false, handler);
        }

        /**
         * Create a right-located action.
         * 
         * @param label
         *                the label.
         * @param variant
         *                the button variant.
         * @param nature
         *                the button nature.
         * @param handler
         *                the click handler.
         * @return the action.
         */
        public static Action right(String label, Btn.Variant variant, Btn.Nature nature, Invoker handler) {
            return new Action(label, (variant == null) ? Btn.Variant.STANDARD : variant, (nature == null) ? Btn.Nature.NORMAL : nature, null, false, handler);
        }

        /**
         * Create a right-located action.
         * 
         * @param label
         *                the label.
         * @param variant
         *                the button variant.
         * @param nature
         *                the button nature.
         * @param icon
         *                an icon to show.
         * @param handler
         *                the click handler.
         * @return the action.
         */
        public static Action right(String label, Btn.Variant variant, Btn.Nature nature, String icon, Invoker handler) {
            return new Action(label, (variant == null) ? Btn.Variant.STANDARD : variant, (nature == null) ? Btn.Nature.NORMAL : nature, icon, false, handler);
        }
        
        /**
         * Create a left-located action.
         * 
         * @param label
         *                the label.
         * @param handler
         *                the click handler.
         * @return the action.
         */
        public static Action left(String label, Invoker handler) {
            return new Action(label, Btn.Variant.STANDARD, Btn.Nature.NORMAL, null, true, handler);
        }

        /**
         * Create a left-located action.
         * 
         * @param label
         *                the label.
         * @param variant
         *                the button variant.
         * @param handler
         *                the click handler.
         * @return the action.
         */
        public static Action left(String label, Btn.Variant variant, Invoker handler) {
            return new Action(label, (variant == null) ? Btn.Variant.STANDARD : variant, Btn.Nature.NORMAL, null, true, handler);
        }

        /**
         * Create a left-located action.
         * 
         * @param label
         *                the label.
         * @param variant
         *                the button variant.
         * @param nature
         *                the button nature.
         * @param handler
         *                the click handler.
         * @return the action.
         */
        public static Action left(String label, Btn.Variant variant, Btn.Nature nature, Invoker handler) {
            return new Action(label, (variant == null) ? Btn.Variant.STANDARD : variant, (nature == null) ? Btn.Nature.NORMAL : nature, null, true, handler);
        }

        /**
         * Create a left-located action.
         * 
         * @param label
         *                the label.
         * @param variant
         *                the button variant.
         * @param nature
         *                the button nature.
         * @param icon
         *                an icon to show.
         * @param handler
         *                the click handler.
         * @return the action.
         */
        public static Action left(String label, Btn.Variant variant, Btn.Nature nature, String icon, Invoker handler) {
            return new Action(label, (variant == null) ? Btn.Variant.STANDARD : variant, (nature == null) ? Btn.Nature.NORMAL : nature, icon, true, handler);
        }
    }

    /**
     * Enumerates variants of the dialog.
     */
    public interface Variant {

        public static final Variant PLAIN = Variant.create("plain", true);

        public static final Variant PLAIN_NO_SHADOW = Variant.create("plain", false);

        /**
         * A CSS class to apply in addition.
         */
        public String style();

        /**
         * If a drop-shadow should be included.
         */
        public boolean dropshadow();

        /**
         * Convenience to create an instance of a variant.
         */
        public static Variant create(String style, boolean dropshadow) {
            return new Variant() {
                public String style() { return style; }
                public boolean dropshadow() { return dropshadow; }
            };
        }
    }

    /**
     * Concrete fragment for the helper class.
     */
    public static class DialogFragment extends ADialogFragment<DialogFragment> {}

    /**
     * Extendable fragment.
     */
    public static class ADialogFragment<T extends ADialogFragment<T>> extends APaperFragment<T> {

        /**
         * See {@link #variant(Variant)}.
         */
        protected Variant variant = Variant.PLAIN;

        /**
         * See {@link #title(String)}.
         */
        protected String title;

        /**
         * See {@link #onclose(Invoker)}.
         */
        protected Invoker onclose;

        /**
         * See {@link #action(Action)}.
         */
        protected List<Action> actions = new ArrayList<>();

        /**
         * Assigns a variant to the card.
         * 
         * @param variant
         *                the variant.
         * @return this fragment.
         */
        @SuppressWarnings("unchecked")
        public T variant(Variant variant) {
            if (variant != null)
                this.variant = variant;
            return (T) this;
        }

        /**
         * Title for the dialog.
         * 
         * @param title
         *              the title.
         * @return this fragment.
         */
        @SuppressWarnings("unchecked")
        public T title(String title) {
            this.title = title;
            return (T) this;
        }

        /**
         * Adds an on-close handler.
         * 
         * @param onclose
         *                the handler.
         * @return this fragment.
         */
        @SuppressWarnings("unchecked")
        public T onclose(Invoker onclose) {
            this.onclose = onclose;
            return (T) this;
        }

        /**
         * Adds an action.
         * 
         * @param action
         *               the action.
         * @return this fragment.
         */
        @SuppressWarnings("unchecked")
        public T action(Action action) {
            actions.add(action);
            return (T) this;
        }

        @Override
        protected void buildInto(ElementBuilder root) {
            root.style ("juiDialog", variant.style());
            if (variant.dropshadow())
                root.style("dropshadow");
            Div.$(root).style("header").$(header -> {
                if (!StringSupport.empty(title))
                    H4.$(header).text (title);
                    Expander.$(header);
                    if (onclose != null) {
                        Em.$(header).style(FontAwesome.times())
                            .onclick(e -> onclose.invoke());
                    }
            });
            Div.$(root).style("contents").$(contents -> {
                super.buildInto(contents);
            });
            if (!actions.isEmpty()) {
                Div.$(root).style("footer").$(footer -> {
                    actions.forEach(action -> {
                        if (action.left()) {
                            Btn.$(footer, action.label())
                                .icon(action.icon())
                                .variant(action.variant())
                                .nature(action.nature())
                                .onclick(action.handler());
                        }
                    });
                    Expander.$(footer);
                    actions.forEach(action -> {
                        if (!action.left()) {
                            Btn.$(footer, action.label())
                                .icon(action.icon())
                                .variant(action.variant())
                                .nature(action.nature())
                                .onclick(action.handler());
                        }
                    });
                });
            }
        }
    }

}
