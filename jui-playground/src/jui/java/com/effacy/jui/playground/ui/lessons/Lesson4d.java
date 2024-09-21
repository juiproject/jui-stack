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
package com.effacy.jui.playground.ui.lessons;

import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.component.layout.VertLayout;
import com.effacy.jui.core.client.control.Control;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.dom.jquery.JQuery;
import com.effacy.jui.core.client.dom.jquery.JQueryElement;
import com.effacy.jui.platform.util.client.ComparisonSupport;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.ui.client.InfoBlockCreator;
import com.effacy.jui.ui.client.panel.Panel;

import elemental2.dom.Element;

public class Lesson4d extends Panel {

    public Lesson4d() {
        super (new Panel.Config ().scrollable ().layout (VertLayout.$ ().spacing (Length.em (1)).build ()).padding (Insets.em (2)));

        // This display a title and descriptive text on the page.
        add (InfoBlockCreator.<Void>$ (cfg -> cfg.padding (Insets.em (0, 1.75, 0, 0.75)), builder -> {
            builder.header ("Part D: Custom controls", header -> {
                header.subtitle ("This lesson explores how custom controls can be constructed.");
            });
        })).update (null);

        RatingControl ctl = add (new RatingControl.Config().range(5).build());
        ctl.setValue(3);
        ctl.handleControlModified (c -> Logger.info ("Value: " + c.value ()));

        RatingControlExercise1 ctl2 = add (new RatingControlExercise1 (new RatingControl.Config().range(5)));
        ctl2.setValue(3);
        ctl2.handleControlModified (c -> Logger.info ("Value: " + c.value ()));

        RatingControlExercise2 ctl3 = add (new RatingControlExercise2 (new RatingControl.Config().range(5)));
        ctl3.setValue(3);
        ctl3.handleControlModified (c -> Logger.info ("Value: " + c.value ()));

        add (new RatingControlExercise3(new RatingControl.Config().range(5)));
    }

    public static class RatingControl extends Control<Integer,RatingControl.Config> {

        /************************************************************************
         * Construction.
         ************************************************************************/

        public static class Config extends Control.Config<Integer,Config> {

            /**
             * See {@link #range(int)}.
             */
            private int range = 1;


            /**
             * Specifies the range (from 1 to this number).
             * @param range
             *              the range (minmum value being 1).
             * @return this configuration instance.
             */
            public Config range(int range) {
                this.range = Math.max (1, range);
                return this;
            }

            @Override
            @SuppressWarnings("unchecked")
            public RatingControl build(LayoutData... data) {
                return build (new RatingControl (this), data);
            }

        }

        /************************************************************************
         * Construction.
         ************************************************************************/
        
        /**
         * Construct with configuration.
         * 
         * @param config
         *               the configuration.
         */
        public RatingControl(Config config) {
            super(config);
        }

        /************************************************************************
         * Value management.
         ************************************************************************/

        @Override
        protected Integer valueFromSource() {
            JQueryElement el = JQuery.$ (getRoot()).find ("div.selected");
            if (el.length() > 0) {
                String item = el.get(0).getAttribute("item");
                return Integer.parseInt (item);
            }
            return null;
        }

        @Override
        protected void valueToSource(Integer value) {
            // Clear any selected items.
            JQuery.$ (getRoot()).find ("div").removeClass ("selected");
            // Find the item we are interested in and select it.
            if (value != null)
                JQuery.$ (getRoot()).find("div[item=" + value + "]").addClass("selected");
        }

        /************************************************************************
         * Render and DOM.
         ************************************************************************/

        @Override
        protected INodeProvider buildNode(Element el) {
            return Wrap.$ (el).$ (root -> {
                root.style ("lesson4 rating");
                for (int i = 1; i <= config().range; i++) {
                    Div.$ (root)
                        .attr ("item", "" + i)
                        .text ("" + i)
                        .onclick((e,n) -> {
                            JQuery.$ (n.parentElement).find ("div").removeClass ("selected");
                            JQuery.$ (n).addClass("selected");
                            modified ();
                        });
                }
            }).build ();
        }
    }

    public static class RatingControlExercise1 extends RatingControl{

        public RatingControlExercise1(RatingControl.Config config) {
            super(config);
        }

        /************************************************************************
         * Value management.
         ************************************************************************/

        @Override
        protected Integer valueFromSource() {
            JQueryElement el = JQuery.$ (boxesEl).find ("div.selected");
            if (el.length() > 0) {
                String item = el.get(0).getAttribute("item");
                return Integer.parseInt (item);
            }
            return null;
        }
     
        @Override
        protected void valueToSource(Integer value) {
            JQuery.$ (boxesEl).find ("div").removeClass ("selected");
            if (value != null)
                JQuery.$ (boxesEl).find("div[item=" + value + "]").addClass("selected");
        }

        /************************************************************************
         * Render and DOM.
         ************************************************************************/

        private Element boxesEl;

        @Override
        protected INodeProvider buildNode(Element el) {
            return Wrap.$ (el).$ (root -> {
                root.style ("lesson4 rating");
                root.by ("boxes");
                for (int i = 1; i <= config().range; i++) {
                    Div.$ (root)
                        .attr ("item", "" + i)
                        .text ("" + i)
                        .onclick((e,n) -> {
                            JQuery.$ (n.parentElement).find ("div").removeClass ("selected");
                            JQuery.$ (n).addClass("selected");
                            modified ();
                        });
                }
            }).build (dom -> {
                boxesEl = dom.first ("boxes");
            });
        }
    }

    public static class RatingControlExercise2 extends RatingControl {

        public RatingControlExercise2(RatingControl.Config config) {
            super(config);
        }

        /************************************************************************
         * Value management.
         ************************************************************************/

        private Integer value;

        @Override
        protected Integer valueFromSource() {
            return value;
        }

        @Override
        protected void valueToSource(Integer value) {
            if (ComparisonSupport.equal(value, this.value))
                return;
            this.value = value;
            rerender();
        }

        /************************************************************************
         * Render and DOM.
         ************************************************************************/

        @Override
        protected INodeProvider buildNode(Element el) {
            return Wrap.$ (el).$ (root -> {
                root.style ("lesson4 rating");
                for (int i = 1; i <= config().range; i++) {
                    int idx = i;
                    Div.$ (root).$ (box -> {
                        box.text ("" + idx);
                        if ((value != null) && value.equals(idx))
                            box.style ("selected");
                        box.onclick((e,n) -> {
                            value = idx;
                            modified ();
                            rerender ();
                        });
                    });
                }
            }).build ();
        }
    }

    public static class RatingControlExercise3 extends RatingControl {

        public RatingControlExercise3(RatingControl.Config config) {
            super(config);
        }

        /************************************************************************
         * Render and DOM.
         ************************************************************************/

        @Override
        protected INodeProvider buildNode(Element el) {
            return Wrap.$ (el).$ (root -> {
                root.style ("lesson4 rating");
                for (int i = 1; i <= config().range; i++) {
                    Div.$ (root)
                        .attr ("item", "" + i)
                        .text ("" + i)
                        .onclick((e,n) -> {
                            if (JQuery.$ (n).hasClass("selected")) {
                                JQuery.$ (n).removeClass("selected");
                            } else {
                                JQuery.$ (n.parentElement).find ("div").removeClass ("selected");
                                JQuery.$ (n).addClass("selected");
                            }
                            modified ();
                        });
                }
            }).build ();
        }
    }
}
