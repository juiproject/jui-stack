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
package com.effacy.jui.ui.client.fragments.control;

import com.effacy.jui.core.client.control.IControl;
import com.effacy.jui.core.client.dom.DomSupport;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.FragmentWithChildren;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.core.client.dom.builder.IFragmentCSS;
import com.effacy.jui.core.client.dom.builder.Label;
import com.effacy.jui.core.client.dom.builder.Li;
import com.effacy.jui.core.client.dom.builder.Ul;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.Carrier;
import com.effacy.jui.platform.util.client.StringSupport;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;

/**
 * Used to capture invalidation events from controls contained within it and
 * display the associated error messages.
 */
public class ControlField {

    public static ControlFieldFragment $() {
        return new ControlFieldFragment ();
    }

    public static ControlFieldFragment $(IDomInsertableContainer<?> parent) {
        ControlFieldFragment frg = $ ();
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    /**
     * Concrete class for the control messages fragment.
     */
    public static class ControlFieldFragment extends FragmentWithChildren<ControlFieldFragment> {

        /**
         * See {@link #styles(ILocalCSS)}.
         */
        private ILocalCSS styles = STANDARD;

        /**
         * See {@link #label(String)}.
         */
        private String label;

        /**
         * See {@link #required(boolean)}.
         */
        private boolean required;

        public ControlFieldFragment label(String label) {
            this.label = label;
            return this;
        }

        public ControlFieldFragment required() {
            return required(true);
        }

        public ControlFieldFragment required(boolean required) {
            this.required = required;
            return this;
        }

        /**
         * Assigns alternative styles to the fragment.
         * 
         * @param styles
         *               the styles.
         * @return this fragment (for chaining).
         */
        public ControlFieldFragment styles(ILocalCSS styles) {
            if (styles != null)
                this.styles = styles;
            return this;
        }


        @Override
        protected void buildInto(ElementBuilder root) {
            Carrier<Element> messagesEl = Carrier.of();
            if (!StringSupport.empty(label)) {
                Label.$(root).$(l -> {
                    if(required)
                        l.style(styles().required());
                    l.text(label);
                });
            }
            Div.$(root).$(main -> {
                children.forEach(main::insert);
                main.handleLodgements(ctx -> {
                    ctx.forEach(l -> {
                        if (l instanceof IControl) {
                            IControl<?> control = (IControl<?>) l;
                            control.handleInvalidation(msg -> {
                                Wrap.$(messagesEl.get()).$ (errors -> {
                                    msg.forEach (error -> Li.$ (errors).text (error));
                                }).build ();
                                messagesEl.get().parentElement.classList.add(styles().error());
                            }, () -> {
                                DomSupport.removeAllChildren (messagesEl.get());
                                messagesEl.get().parentElement.classList.remove(styles().error());
                            });
                        }
                    });
                });
            });
            Ul.$(root).style(styles().messages()).$(messages -> {
                messages.use(n -> messagesEl.set((Element) n));
            });
        }
        
        /**
         * Styles (made available to selection).
         */
        @Override
        protected ILocalCSS styles() {
            return styles;
        }
    }

    /********************************************************************
     * CSS with standard styles.
     ********************************************************************/

    public static interface ILocalCSS extends IFragmentCSS {
        String error();
        String required();
        String messages();
    }

    /**
     * Standard styles.
     */
    public static final ILocalCSS STANDARD = StandardLocalCSS.instance();
    @CssResource({
        "com/effacy/jui/ui/client/fragments/control/ControlField.css",
        "com/effacy/jui/ui/client/fragments/control/ControlField_Override.css"
    })
    public static abstract class StandardLocalCSS implements ILocalCSS {

        private static StandardLocalCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (StandardLocalCSS) GWT.create (StandardLocalCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }

}
