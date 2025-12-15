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

import java.util.function.Consumer;

import com.effacy.jui.core.client.component.Component;
import com.effacy.jui.core.client.component.ComponentCreator;
import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.SimpleComponent;
import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.component.layout.VertLayout;
import com.effacy.jui.core.client.component.layout.VertLayout.VertLayoutData.Separator;
import com.effacy.jui.core.client.dom.DomSupport;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.IUIEventHandler;
import com.effacy.jui.core.client.dom.SelectorUIEventHandler;
import com.effacy.jui.core.client.dom.UIEvent;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.DomBuilder;
import com.effacy.jui.core.client.dom.builder.Input;
import com.effacy.jui.core.client.dom.builder.P;
import com.effacy.jui.core.client.dom.builder.Text;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.dom.renderer.IDataRenderer;
import com.effacy.jui.core.client.dom.renderer.IRenderer;
import com.effacy.jui.core.client.dom.renderer.template.ITemplateBuilder;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.ui.client.button.Button;
import com.effacy.jui.ui.client.button.ButtonCreator;
import com.effacy.jui.ui.client.panel.Panel;
import com.effacy.jui.ui.client.panel.PanelCreator;
import com.google.gwt.core.client.GWT;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.HTMLInputElement;

/**
 * A simple collection of components that progressively build up in complexity.
 *
 * @author Jeremy Buckley
 */
public class Components extends Panel {

    /**
     * See the instantiation of button 3.
     */
    private int counter3 = 0;

    /**
     * See the instantiation of button 6.
     */
    private int counter6 = 0;

    /**
     * Construct an instance of the components panel with the demonstration
     * components in a vertical layout (down the page).
     */
    public Components() {
        super (PanelCreator.config ().scrollable ().layout (VertLayout.$ ().separator (Separator.LINE).spacing (Length.em (1)).build ()));

        // Purely in-line button component.
        add (ComponentCreator.build (outer -> {
            outer.button (btn -> {
                btn.text ("My inline button");
                btn.on (e -> DomGlobal.window.alert ("The inline button has been clicked"), UIEventType.ONCLICK);
            });
        }));

        // Purely in-line button componen that uses a reusable renderer.
        add (ComponentCreator.build (div -> {
            div.p ().text ("The button below is based on a reusable renderer");
            div.render (new ButtonAtom ());
        }));

        // A simple button component.
        add (new MyButton1 ("My first button"));

        // A simple button component with configuration.
        add(new MyButton2.Config()
                .label("My second button")
                .handler(btn -> DomGlobal.window.alert("The second button has been clicked"))
                .build());
        
        // A simple button component with configuration and runtime update of its label.
        add (new MyButton3.Config ()
                .label ("Number of times this button has been pressed: 0")
                .handler (btn -> btn.updateLabel ("Number of times this button has been pressed: " + (++counter3)))
                .build ());

        // A simple button with (local) styles.
        add (new MyButton4.Config ()
                .label ("My styled button")
                .handler (btn -> DomGlobal.window.alert ("The styled button has been clicked"))
                .build ());

        // A simple button with (global) styles.
        add (new MyButton5.Config ()
                .label ("My globally styled button")
                .handler (btn -> DomGlobal.window.alert ("The globally styled button has been clicked"))
                .build ());
        add (new MyButton6.Config ()
                .label ("Number of times this new button has been pressed: 0")
                .handler (btn -> btn.updateLabel ("Number of times this new button has been pressed: " + (++counter6)))
                .build ());
        add (new MyButton7.Config ()
                .label ("Button using a UI event handler")
                .build ());
        add (new MyButton8.Config ()
                .label ("Button using a template renderer 1")
                .build ());
        add (new MyButton8.Config ()
                .label ("Button using a template renderer 2")
                .build ());
        add (new MyButton9 ("Compact button", e -> DomGlobal.window.alert ("The compact button has been clicked")));

        add (new TemperatureCalculator ());
    }

    public static class ButtonAtom implements IRenderer {

        @Override
        public IUIEventHandler render(Element el) {
            return DomBuilder.el (el, root -> {
                root.button (btn-> {
                    btn.text ("Template button");
                    btn.on (e-> onClick (), UIEventType.ONCLICK);
                });
            }).build();
        }

        protected void onClick() {
            DomGlobal.window.alert ("The button was pressed");
        }
        
    }

    /*******************************************************************************
     * A very simple component.
     *******************************************************************************/

    /**
     * First simple button to demonstrate the ability to generate a responsive DOM
     * structure (i.e. with an event handler).
     */
    public static class MyButton1 extends SimpleComponent {

        /**
         * The label text of the button.
         */
        private String label;

        /**
         * Construct a button with its label.
         * 
         * @param label
         *              the label for the button.
         */
        public MyButton1(String label) {
            this.label = label;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.gwt.ui.client.component.RendererComponent#buildNode(com.effacy.jui.core.client.component.Component.Config)
         */
        @Override
        protected INodeProvider buildNode(Component.Config config) {
            return DomBuilder.div (outer -> {
                outer.button (btn -> {
                    btn.text (label);
                    btn.on (e -> onClick (), UIEventType.ONCLICK);
                });
            }).build ();
        }

        /**
         * Invoked when the button has been clicked.
         */
        protected void onClick() {
            DomGlobal.window.alert ("The first button has been clicked");
        }

    }

    /*******************************************************************************
     * A more complex component that follows the configruation pattern.
     *******************************************************************************/

    /**
     * Second simple button to demonstrate the use of configuration.
     */
    public static class MyButton2 extends Component<MyButton2.Config> {

        /**
         * Button configruation.
         */
        public static class Config extends Component.Config {

            /**
             * See {@link #label(String)}.
             */
            private String label;

            /**
             * See {@link #handler(Consumer)}.
             */
            private Consumer<MyButton2> handler;

            /**
             * Assigns a label for the button.
             * 
             * @param label
             *              the label.
             * @return this configuration instance.
             */
            public Config label(String label) {
                this.label = label;
                return this;
            }

            /**
             * Assigns a handler for button clicks.
             * 
             * @param handler
             *                the handler for button clicks.
             * @return this configuration instance.
             */
            public Config handler(Consumer<MyButton2> handler) {
                this.handler = handler;
                return this;
            }

            /**
             * {@inheritDoc}
             *
             * @see com.effacy.jui.core.client.component.Component.Config#build(com.effacy.jui.core.client.component.layout.LayoutData[])
             */
            @Override
            @SuppressWarnings("unchecked")
            public MyButton2 build(LayoutData... data) {
                return new MyButton2 (this);
            }

        }

        /**
         * Construct a button with its label.
         * 
         * @param label
         *              the label for the button.
         */
        public MyButton2(MyButton2.Config config) {
            super (config);
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.gwt.ui.client.component.RendererComponent#buildNode(com.effacy.jui.core.client.component.Component.Config)
         */
        @Override
        protected INodeProvider buildNode(MyButton2.Config config) {
            return DomBuilder.div (outer -> {
                outer.button (btn -> {
                    btn.text (config.label);
                    btn.on (e -> onClick (), UIEventType.ONCLICK);
                });
            }).build ();
        }

        /**
         * Invoked when the button has been clicked.
         */
        protected void onClick() {
            if (config ().handler != null)
                config ().handler.accept (this);
        }

    }

    /*******************************************************************************
     * A more complete component that includes the ability to change dynamically.
     *******************************************************************************/

    /**
     * Second simple button to demonstrate the use of configuration.
     */
    public static class MyButton3 extends Component<MyButton3.Config> {

        /**
         * Button configuration.
         */
        public static class Config extends Component.Config {

            /**
             * See {@link #label(String)}.
             */
            private String label;

            /**
             * See {@link #handler(Consumer)}.
             */
            private Consumer<MyButton3> handler;

            /**
             * Assigns a label for the button.
             * 
             * @param label
             *              the label.
             * @return this configuration instance.
             */
            public Config label(String label) {
                this.label = label;
                return this;
            }

            /**
             * Assigns a handler for button clicks.
             * 
             * @param handler
             *                the handler for button clicks.
             * @return this configuration instance.
             */
            public Config handler(Consumer<MyButton3> handler) {
                this.handler = handler;
                return this;
            }

            /**
             * {@inheritDoc}
             *
             * @see com.effacy.jui.core.client.component.Component.Config#build(com.effacy.jui.core.client.component.layout.LayoutData[])
             */
            @Override
            @SuppressWarnings("unchecked")
            public MyButton3 build(LayoutData... data) {
                return new MyButton3 (this);
            }

        }

        /**
         * Reference to the element whose text is the label.
         */
        protected Element labelEl;

        /**
         * Construct a button with its label.
         * 
         * @param label
         *              the label for the button.
         */
        public MyButton3(MyButton3.Config config) {
            super (config);
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.gwt.ui.client.component.RendererComponent#buildNode(com.effacy.jui.core.client.component.Component.Config)
         */
        @Override
        protected INodeProvider buildNode(MyButton3.Config config) {
            return DomBuilder.div (outer -> {
                outer.button (btn -> {
                    btn.by ("label");
                    btn.text (config.label);
                    btn.on (e -> onClick (), UIEventType.ONCLICK);
                });
            }).build (tree -> {
                labelEl = tree.first ("label");
            });
        }

        /**
         * Invoked when the button has been clicked.
         */
        protected void onClick() {
            if (config ().handler != null)
                config ().handler.accept (this);
        }

        /**
         * Enables the updating of the label post render.
         * 
         * @param label
         *              the new label.
         */
        public void updateLabel(String label) {
            // If called pre-render we just update the configuration (so the label is picked
            // up during rendering).
            config ().label = label;

            // Post-render the label element will be available, so assign the new label
            // directly to that element using the safe inner text.
            if (labelEl != null) {
                //DomSupport.innerText (labelEl, label);
                Wrap.$ (labelEl).clear ().$ (el -> {
                    el.text (label);
                }).build ();
            }
        }

    }

    /*******************************************************************************
     * A complete component that incorporates styles.
     *******************************************************************************/

    /**
     * Second simple button to demonstrate the use of configuration.
     */
    public static class MyButton4 extends Component<MyButton4.Config> {

        /**
         * Button configruation.
         */
        public static class Config extends Component.Config {

            /**
             * See {@link #label(String)}.
             */
            private String label;

            /**
             * See {@link #handler(Consumer)}.
             */
            private Consumer<MyButton4> handler;

            /**
             * Assigns a label for the button.
             * 
             * @param label
             *              the label.
             * @return this configuration instance.
             */
            public Config label(String label) {
                this.label = label;
                return this;
            }

            /**
             * Assigns a handler for button clicks.
             * 
             * @param handler
             *                the handler for button clicks.
             * @return this configuration instance.
             */
            public Config handler(Consumer<MyButton4> handler) {
                this.handler = handler;
                return this;
            }

            /**
             * {@inheritDoc}
             *
             * @see com.effacy.jui.core.client.component.Component.Config#build(com.effacy.jui.core.client.component.layout.LayoutData[])
             */
            @Override
            @SuppressWarnings("unchecked")
            public MyButton4 build(LayoutData... data) {
                return new MyButton4 (this);
            }

        }

        /**
         * Reference to the element whose text is the label.
         */
        protected Element labelEl;

        /**
         * Construct a button with its label.
         * 
         * @param label
         *              the label for the button.
         */
        public MyButton4(MyButton4.Config config) {
            super (config);
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.gwt.ui.client.component.RendererComponent#buildNode(com.effacy.jui.core.client.component.Component.Config)
         */
        @Override
        protected INodeProvider buildNode(MyButton4.Config config) {
            return DomBuilder.div (outer -> {
                outer.addClassName (styles ().outer ());
                outer.button (btn -> {
                    btn.by ("label");
                    btn.text (config.label);
                    btn.on (e -> onClick (), UIEventType.ONCLICK);
                });
            }).build (tree -> {
                labelEl = tree.first ("label");
            });
        }

        /**
         * Invoked when the button has been clicked.
         */
        protected void onClick() {
            if (config ().handler != null)
                config ().handler.accept (this);
        }

        /**
         * Enables the updating of the label post render.
         * 
         * @param label
         *              the new label.
         */
        public void updateLabel(String label) {
            // If called pre-render we just update the configuration (so the label is picked
            // up during rendering).
            config ().label = label;

            // Post-render the label element will be available, so assign the new label
            // directly to that element using the safe inner text.
            if (labelEl != null)
                DomSupport.innerText (labelEl, label);
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.gwt.ui.client.component.RendererComponent#styles()
         */
        @Override
        protected ILocalCSS styles() {
            return LocalCSS.instance ();
        }

        public static interface ILocalCSS extends IComponentCSS {

            public String outer();

        }

        /**
         * Component CSS (standard pattern).
         */
        @CssResource({ IComponentCSS.COMPONENT_CSS, "com/effacy/jui/playground/ui/samples/MyButton4.css" })
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

    /*******************************************************************************
     * A complete component that incorporates styles.
     *******************************************************************************/

    /**
     * Second simple button to demonstrate the use of configuration.
     */
    public static class MyButton5 extends Component<MyButton5.Config> {

        /**
         * Button configruation.
         */
        public static class Config extends Component.Config {

            /**
             * See {@link #label(String)}.
             */
            private String label;

            /**
             * See {@link #handler(Consumer)}.
             */
            private Consumer<MyButton5> handler;

            /**
             * Assigns a label for the button.
             * 
             * @param label
             *              the label.
             * @return this configuration instance.
             */
            public Config label(String label) {
                this.label = label;
                return this;
            }

            /**
             * Assigns a handler for button clicks.
             * 
             * @param handler
             *                the handler for button clicks.
             * @return this configuration instance.
             */
            public Config handler(Consumer<MyButton5> handler) {
                this.handler = handler;
                return this;
            }

            /**
             * {@inheritDoc}
             *
             * @see com.effacy.jui.core.client.component.Component.Config#build(com.effacy.jui.core.client.component.layout.LayoutData[])
             */
            @Override
            @SuppressWarnings("unchecked")
            public MyButton5 build(LayoutData... data) {
                return new MyButton5 (this);
            }

        }

        /**
         * Reference to the element whose text is the label.
         */
        protected Element labelEl;

        /**
         * Construct a button with its label.
         * 
         * @param label
         *              the label for the button.
         */
        public MyButton5(MyButton5.Config config) {
            super (config);
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.gwt.ui.client.component.RendererComponent#buildNode(com.effacy.jui.core.client.component.Component.Config)
         */
        @Override
        protected INodeProvider buildNode(MyButton5.Config config) {
            return DomBuilder.div (outer -> {
                // This could easily be: outer.addClassName ("outer");
                outer.addClassName (styles ().outer ());
                outer.button (btn -> {
                    btn.by ("label");
                    btn.text (config.label);
                    btn.on (e -> onClick (), UIEventType.ONCLICK);
                });
            }).build (tree -> {
                labelEl = tree.first ("label");
            });
        }

        /**
         * Invoked when the button has been clicked.
         */
        protected void onClick() {
            if (config ().handler != null)
                config ().handler.accept (this);
        }

        /**
         * Enables the updating of the label post render.
         * 
         * @param label
         *              the new label.
         */
        public void updateLabel(String label) {
            // If called pre-render we just update the configuration (so the label is picked
            // up during rendering).
            config ().label = label;

            // Post-render the label element will be available, so assign the new label
            // directly to that element using the safe inner text.
            if (labelEl != null) 
                DomSupport.innerText (labelEl, label);
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.gwt.ui.client.component.RendererComponent#styles()
         */
        @Override
        protected LocalCSS styles() {
            return LocalCSS.INSTANCE;
        }

        /**
         * An alternative to using the generated CSS but rather relying on a global CSS.
         * In this case the style names are passed through directly.
         */
        public static class LocalCSS extends IComponentCSS.GlobalComponentCSS {

            public static final LocalCSS INSTANCE = new LocalCSS ();

            /**
             * {@inheritDoc}
             *
             * @see com.effacy.jui.core.client.component.IComponentCSS.GlobalComponentCSS#component()
             */
            @Override
            public String component() {
                // Rename the component CSS style to reflect the button.
                return "button";
            }

            /**
             * This is included for consistency, but it is strictly not necessary and the
             * CSS styles can be referenced by string only. However this does provide some
             * name enforcement.
             */
            public String outer() {
                return "outer";
            }

        }

    }

    /*******************************************************************************
     * A variation of the counter button that includes a button component.
     *******************************************************************************/

    /**
     * An example of a component including another component.
     */
    public static class MyButton6 extends Component<MyButton6.Config> {

        /**
         * Button configruation.
         */
        public static class Config extends Component.Config {

            /**
             * See {@link #label(String)}.
             */
            private String label;

            /**
             * See {@link #handler(Consumer)}.
             */
            private Consumer<MyButton6> handler;

            /**
             * Assigns a label for the button.
             * 
             * @param label
             *              the label.
             * @return this configuration instance.
             */
            public Config label(String label) {
                this.label = label;
                return this;
            }

            /**
             * Assigns a handler for button clicks.
             * 
             * @param handler
             *                the handler for button clicks.
             * @return this configuration instance.
             */
            public Config handler(Consumer<MyButton6> handler) {
                this.handler = handler;
                return this;
            }

            /**
             * {@inheritDoc}
             *
             * @see com.effacy.jui.core.client.component.Component.Config#build(com.effacy.jui.core.client.component.layout.LayoutData[])
             */
            @Override
            @SuppressWarnings("unchecked")
            public MyButton6 build(LayoutData... data) {
                return new MyButton6 (this);
            }

        }

        /**
         * Button component that is being bundled.
         */
        private Button button;

        /**
         * Construct a button with its label.
         * 
         * @param label
         *              the label for the button.
         */
        public MyButton6(MyButton6.Config config) {
            super (config);
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.gwt.ui.client.component.RendererComponent#buildNode(com.effacy.jui.core.client.component.Component.Config)
         */
        @Override
        protected INodeProvider buildNode(MyButton6.Config config) {
            return DomBuilder.div (outer -> {
                P.$ (outer).text ("This is a button component in another component:");
                Div.$ (outer).css (CSS.PADDING_LEFT, Length.em (1)).$ (inner -> {
                    button = ButtonCreator.$ (inner, cfg -> {
                        cfg.label (config ().label);
                        cfg.testId ("btn99");
                        cfg.handler (cb -> {
                            onClick ();
                            cb.complete ();
                        });
                    });
                });
            }).build ();
        }

        /**
         * Invoked when the button has been clicked.
         */
        protected void onClick() {
            if (config ().handler != null)
                config ().handler.accept (this);
        }

        /**
         * Enables the updating of the label post render.
         * 
         * @param label
         *              the new label.
         */
        public void updateLabel(String label) {
            button.updateLabel (label);
        }

    }

    /*******************************************************************************
     * An example that uses a registered UI event handler.
     *******************************************************************************/

    /**
     * Button that demonstrates the registration of events.
     */
    public static class MyButton7 extends Component<MyButton7.Config> {

        /**
         * Button configruation.
         */
        public static class Config extends Component.Config {

            /**
             * See {@link #label(String)}.
             */
            private String label;

            /**
             * Assigns a label for the button.
             * 
             * @param label
             *              the label.
             * @return this configuration instance.
             */
            public Config label(String label) {
                this.label = label;
                return this;
            }

            /**
             * {@inheritDoc}
             *
             * @see com.effacy.jui.core.client.component.Component.Config#build(com.effacy.jui.core.client.component.layout.LayoutData[])
             */
            @Override
            @SuppressWarnings("unchecked")
            public MyButton7 build(LayoutData... data) {
                return new MyButton7 (this);
            }

        }

        /**
         * Construct a button with its label.
         * 
         * @param label
         *              the label for the button.
         */
        public MyButton7(MyButton7.Config config) {
            super (config);

            // Register the event handler that will extract the button node and associate it
            // with an on-click handler.
            registerEventHandler (new SelectorUIEventHandler ().bind ("button", e -> {
                DomGlobal.window.alert ("Button type 7 has been clicked.");
            }, UIEventType.ONCLICK));
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.gwt.ui.client.component.RendererComponent#buildNode(com.effacy.jui.core.client.component.Component.Config)
         */
        @Override
        protected INodeProvider buildNode(MyButton7.Config config) {
            return DomBuilder.div (outer -> {
                outer.button (btn -> {
                    btn.by ("button");
                    btn.text (config.label);
                });
            }).build ();
        }

    }

    /*******************************************************************************
     * An example that uses a registered UI event handler.
     *******************************************************************************/

    /**
     * Button that demonstrates the use of a cached template renderer and associated
     * event handler.
     */
    public static class MyButton8 extends Component<MyButton8.Config> {

        /**
         * Button configruation.
         */
        public static class Config extends Component.Config {

            /**
             * See {@link #label(String)}.
             */
            private String label;

            /**
             * Assigns a label for the button.
             * 
             * @param label
             *              the label.
             * @return this configuration instance.
             */
            public Config label(String label) {
                this.label = label;
                return this;
            }

            /**
             * {@inheritDoc}
             *
             * @see com.effacy.jui.core.client.component.Component.Config#build(com.effacy.jui.core.client.component.layout.LayoutData[])
             */
            @Override
            @SuppressWarnings("unchecked")
            public MyButton8 build(LayoutData... data) {
                return new MyButton8 (this);
            }

        }

        /**
         * Construct a button with its label.
         * 
         * @param label
         *              the label for the button.
         */
        public MyButton8(MyButton8.Config config) {
            super (config);
        }

        /**
         * Invoked when the button has been clicked.
         */
        protected void onClick() {
            DomGlobal.window.alert ("Button \"" + config ().label + "\" clicked");
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.component.Component#buildRenderer()
         */
        @Override
        protected IDataRenderer<MyButton8.Config> buildRenderer() {
            return ITemplateBuilder.renderer ("MyButton8", root -> {
                Logger.log ("Instance of MyButton8 renderer created");
                root.div ().button () //
                    .on (e -> ((MyButton8) e.getSource ()).onClick (), UIEventType.ONCLICK) //
                    .by ("button") //
                    .text (d -> d.label);
            });
        }

    }

    /************************************************************************
     * A more interactive example.
     ************************************************************************/

    /**
     * An example that is a little different from the buttons above.
     * <p>
     * This was translated over from a VueJS example
     * {@link https://vuejs.org/examples/#temperature-converter}.
     * <p>
     * The key differences between a pure JS version and Java version are the strict
     * typing (in terms of the values) and the use of programmatic DOM building (no
     * HTML templates).
     */
    public static class TemperatureCalculator extends Component<Component.Config> {

        protected HTMLInputElement celciusEl;

        protected HTMLInputElement fahrenheitEl;

        protected void setCelcius() {
            fahrenheitEl.value = Double.toString (Double.parseDouble (celciusEl.value) * (9.0 / 5.0) + 32.0);
        }

        protected void setFahrenheit() {
            celciusEl.value = Double.toString ((Double.parseDouble (fahrenheitEl.value) - 32.0) * (5.0 / 9.0));
        }

        @Override
        protected INodeProvider buildNode(Component.Config data) {
            return DomBuilder.div (outer -> {
                P.$ (outer).text ("Convert between Celcius and Fahrenheit:");
                Div.$ (outer).$ (
                    Input.$ ("number").by ("celcius").onchange (e -> setCelcius ()),
                    Text.$ (" Celsius = "),
                    Input.$ ( "number").by ("fahrenheit").onchange (e -> setFahrenheit ()),
                    Text.$ (" Fahrenheit = ")
                );
            }).build (tree -> {
                celciusEl = tree.first ("celcius");
                fahrenheitEl = tree.first ("fahrenheit");
            });
        }

    }

    public static class MyButton9 extends SimpleComponent {

        public MyButton9(String title, Consumer<UIEvent> onClick) {
            renderer (root -> {
                root.style ("my-button");
                root.button (btn -> {
                    btn.text (title);
                    btn.on (onClick, UIEventType.ONCLICK);
                });
            });
        }
    }

}
