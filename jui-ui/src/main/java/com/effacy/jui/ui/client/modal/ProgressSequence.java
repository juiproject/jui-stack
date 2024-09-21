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
package com.effacy.jui.ui.client.modal;

import java.util.ArrayList;
import java.util.List;

import com.effacy.jui.core.client.component.Component;
import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.Label;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.Itr;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;

public class ProgressSequence  extends Component<ProgressSequence.Config> {
    
    /**
     * Configuration.
     */
    public static class Config extends Component.Config {

        /**
         * Style direction for the component.
         */
        public interface Style {

            /**
             * The CSS styles.
             * 
             * @return the styles.
             */
            public ILocalCSS styles();

            public String iconDone();

            public String iconActive();

            public String iconPending();

            /**
             * Convenience to create a styles instance from the given data.
             * 
             * @param styles
             *               the styles.
             * @return the style instance.
             */
            public static Style create(final ILocalCSS styles, String iconDone, String iconActive, String iconPending) {
                return new Style () {

                    @Override
                    public ILocalCSS styles() {
                        return styles;
                    }

                    public String iconDone() {
                        return iconDone;
                    }

                    public String iconActive() {
                        return iconActive;
                    }

                    public String iconPending() {
                        return iconPending;
                    }

                };
            }

            /**
             * Normal visual style (box-like with solid color body).
             */
            public static final Style STANDARD = create (StandardCSS.instance (), FontAwesome.check(), FontAwesome.circleDot(FontAwesome.Option.REGULAR), FontAwesome.circle (FontAwesome.Option.REGULAR));
        }

        /**
         * Various possible state of an item.
         */
        public enum State {

            /**
             * The step is pending.
             */
            PENDING,
            
            /**
             * The step is currently active.
             */
            ACTIVE,
            
            /**
             * The step has been completed.
             */
            DONE;
        }

        /**
         * Represents the configuration data for an item.
         */
        class Item {

            private String label;

            private State state;

            public Item(String label, State state) {
                this.label = label;
                this.state = (state == null) ? State.PENDING : state;
            }
        }

        /**
         * See {@link #style(Style)}.
         */
        private Style style = Style.STANDARD;

        private boolean compressed;

        /**
         * See {@link #add(String, State)}.
         */
        private List<Item> items = new ArrayList<>();

        /**
         * Assigns a style to use.
         * 
         * @param style
         *              the style.
         * @return this configuration instance.
         */
        public Config style(Style style) {
            if (style != null)
                this.style = style;
            return this;
        }

        public Config compressed() {
            return compressed (true);
        }

        public Config compressed(boolean compressed) {
            this.compressed = compressed;
            return this;
        }

        /**
         * Adds a item to the indicator with a state.
         * 
         * @param label
         *              the label of the item.
         * @param state
         *              the state of the item.
         * @return this configuration instance.
         */
        public Config add(String label, State state) {
            items.add (new Item(label, state));
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public ProgressSequence build(LayoutData... data) {
            return (ProgressSequence) super.build (new ProgressSequence (this), data);
        }

    }

    /**
     * Construct with configuration.
     * 
     * @param config
     *               the configuration.
     */
    public ProgressSequence(ProgressSequence.Config config) {
        super (config);
    }

    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$ (el).$ (root -> {
            if (config ().compressed)
                root.style (styles().compressed());
            Div.$ (root).style (styles ().bookend ());
            Itr.forEach (config().items, (ctx,item) -> {
                Div.$ (root).style (styles ().item ()).$ (itm -> {
                    if (!ctx.first () && !ctx.last ())
                        itm.style (styles ().center ());
                    Label.$ (itm).text (item.label);
                    Div.$ (itm).style (styles ().bottom ()).$ (bottom -> {
                        if (ctx.first ())
                            Div.$ (bottom).style (styles ().expand ());
                        else
                            Div.$ (bottom).style (styles ().line ()); 
                        if (item.state == Config.State.PENDING)
                            Em.$ (bottom).style (config ().style.iconPending ());
                        else if (item.state == Config.State.DONE)
                            Em.$ (bottom).style (config ().style.iconDone ());
                        else
                            Em.$ (bottom).style (config ().style.iconActive ());
                        if (ctx.last ())
                            Div.$ (bottom).style (styles ().expand ());
                        else
                            Div.$ (bottom).style (styles ().line ());
                    });
                });
            });
            Div.$ (root).style (styles ().bookend ());
        }).build ();
    }

    /********************************************************************
     * CSS
     ********************************************************************/

    /**
     * Styles (made available to selection).
     */
    protected ILocalCSS styles() {
        return config ().style.styles ();
    }

    public static interface ILocalCSS extends IComponentCSS {

        public String item();

        public String expand();

        public String bottom();

        public String line();

        public String bookend();

        public String center();

        public String compressed();
    }

    /**
     * Component CSS (standard pattern).
     */
    @CssResource({  
        IComponentCSS.COMPONENT_CSS,
        "com/effacy/jui/ui/client/modal/ProgressSequence.css"
    })
    public static abstract class StandardCSS implements ILocalCSS {

        private static StandardCSS STYLES; 

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (StandardCSS) GWT.create (StandardCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES; 
        }
    }
}

