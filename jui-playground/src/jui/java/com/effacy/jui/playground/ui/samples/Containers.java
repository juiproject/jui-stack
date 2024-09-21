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
package com.effacy.jui.playground.ui.samples;

import com.effacy.jui.core.client.component.Component;
import com.effacy.jui.core.client.component.IComponent;
import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.layout.CardFitLayout;
import com.effacy.jui.core.client.component.layout.VertLayout;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.builder.DomBuilder;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.playground.ui.shared.TextComponent;
import com.effacy.jui.ui.client.button.Button;
import com.effacy.jui.ui.client.panel.Panel;
import com.effacy.jui.ui.client.panel.PanelCreator;
import com.google.gwt.core.client.GWT;

/**
 * A simple collection of components that progressively build up in complexity.
 *
 * @author Jeremy Buckley
 */
public class Containers extends Panel {

    /**
     * Construct an instance of the components panel with the demonstration
     * components in a vertical layout (down the page).
     */
    public Containers() {
        super (PanelCreator.config ().layout (VertLayout.$ ().spacing (Length.em (0.5)).build ()));

        add (new MyContainer1 ());
    }

    /*******************************************************************************
     * A very simple component.
     *******************************************************************************/

    /**
     * First simple button to demonstrate the ability to generate a responsible DOM
     * structure.
     */
    public static class MyContainer1 extends Component<Component.Config> {

        private IComponent text1 = new TextComponent ("The first paragraph.");

        private IComponent text2 = new TextComponent ("The second paragraph.");

        /**
         * Construct an instance of the container.
         */
        public MyContainer1() {
            super (new Component.Config ());

            findRegionPoint ("RIGHT").add (new Button.Config ().label ("Swap left panel").handler (cb -> {
                RegionPoint rp = findRegionPoint ("LEFT");
                if (((CardFitLayout) rp.getLayout ()).getActiveItem () == text1)
                    rp.getLayout ().activate (text2);
                else
                    rp.getLayout ().activate (text1);
                cb.complete ();
            }).build ());

            findRegionPoint ("LEFT").add (text1);
            findRegionPoint ("LEFT").add (text2);
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.gwt.ui.client.component.RendererComponent#buildNode(com.effacy.jui.core.client.component.Component.Config)
         */
        @Override
        protected INodeProvider buildNode(com.effacy.jui.core.client.component.Component.Config config) {
            return DomBuilder.div (outer -> {
                outer.addClassName (styles ().outer ());
                outer.div () //
                        .addClassName (styles ().left ()) //
                        .apply (region ("LEFT", null));
                outer.div () //
                        .addClassName (styles ().right ()) //
                        .apply (region ("RIGHT", VertLayout.$ ().build ()));
            }).build ();
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.component.Component#styles()
         */
        @Override
        protected ILocalCSS styles() {
            return LocalCSS.instance ();
        }

        public static interface ILocalCSS extends IComponentCSS {

            public String outer();

            public String left();

            public String right();

        }

        /**
         * Component CSS (standard pattern).
         */
        @CssResource({ IComponentCSS.COMPONENT_CSS, "com/effacy/jui/playground/ui/samples/MyContainer1.css" })
        public static abstract class LocalCSS implements ILocalCSS {

            private static LocalCSS STYLES;

            public static ILocalCSS instance() {
                if (STYLES == null) {
                    STYLES = (LocalCSS) GWT.create (LocalCSS.class);
                    STYLES.ensureInjected ();
                }
                return STYLES;
            }
        }

    }

}
