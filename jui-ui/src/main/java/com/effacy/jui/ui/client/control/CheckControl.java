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
package com.effacy.jui.ui.client.control;

import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.control.Control;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.UIEvent.KeyCode;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.Input;
import com.effacy.jui.core.client.dom.builder.Label;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.platform.util.client.TimerSupport;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;
import elemental2.dom.HTMLInputElement;
import jsinterop.base.Js;

/**
 * A check-box (like) control that has a checkable interface with a label.
 * <p>
 * The checkable interface can be rendered either as a standard checkbox or as a
 * slider.
 *
 * @author Jeremy Buckley
 */
public class CheckControl extends Control<Boolean, CheckControl.Config> {

    /**
     * The default style to employ when one is not assign explicitly.
     */
    public static Config.Style DEFAULT_STYLE = Config.Style.STANDARD;

    /**
     * Configuration for building a {@link CheckControl}.
     */
    public static class Config extends Control.Config<Boolean, CheckControl.Config> {

        /********************************************************************
         * Styles for the tab set.
         ********************************************************************/

        /**
         * Style for the tab set (defines presentation configuration including CSS).
         */
        public interface Style {

            /**
             * The CSS styles.
             */
            public ILocalCSS styles();

            /**
             * Convenience to create a style.
             * 
             * @param styles
             *               the CSS styles.
             * @return the associated style.
             */
            public static Style create(final ILocalCSS styles) {
                return new Style () {

                    @Override
                    public ILocalCSS styles() {
                        return styles;
                    }

                };
            }

            public static final Style STANDARD = create(StandardLocalCSS.instance ());

        }

        /**
         * The styles to apply to the tab set.
         */
        private Style style = (DEFAULT_STYLE != null) ? DEFAULT_STYLE : Style.STANDARD;

        /**
         * See {@link #slider(boolean)}.
         */
        private boolean slider;

        /**
         * See {@link #label(String)}.
         */
        private String label;

        /**
         * See {@link #labelBold(boolean)}.
         */
        private boolean labelBold;

        /**
         * See {@link #description(String)}.
         */
        private String description;

        /**
         * See {@link #left(boolean)}.
         */
        private boolean left;

        /**
         * See {@link #expand(boolean)}.
         */
        private boolean expand;

        /**
         * Construct with a default style.
         */
        public Config() {
            super ();
        }

        /**
         * Construct with a style.
         * 
         * @param style
         *              the style.
         */
        public Config(Style style) {
            super ();
            if (style != null)
                this.style = style;
        }

        /**
         * Convenience to pass {@code true} to {@link #radio(boolean)}.
         * 
         * @return this configuration instance.
         */
        public Config slider() {
            return slider (true);
        }

        /**
         * The options should be radio buttons not check boxes.
         * 
         * @param slider
         *               {@code true} if should be radio buttons.
         * @return this configuration instance.
         */
        public Config slider(boolean slider) {
            this.slider = slider;
            return this;
        }

        /**
         * Provides a label for the check.
         * 
         * @param label
         *              the label for the check
         * @return this configuration instance.
         */
        public Config label(String label) {
            this.label = label;
            return this;
        }

        /**
         * Convenience for passing {@code true} through to {@link #labelBold(boolean)}.
         */
        public Config labelBold() {
            return labelBold (true);
        }

        /**
         * Bold faces the label (so it is more promanent).
         * 
         * @param bold
         *             {@code true} to bold the label.
         * @return this configuration instance.
         */
        public Config labelBold(boolean labelBold) {
            this.labelBold = labelBold;
            return this;
        }

        /**
         * An additional description that appears under the label.
         * 
         * @param description
         *                    the description.
         * @return this configuration instance.
         */
        public Config description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Convenience for passing {@code true} through to {@link #expand(boolean)}.
         */
        public Config left() {
            return left (true);
        }

        /**
         * Reverse the checkbox and label (i.e. label on the left rather than on the
         * right).
         * 
         * @param left
         *             {@code true} to place the label on the left.
         * @return this configuration instance.
         */
        public Config left(boolean left) {
            this.left = left;
            return this;
        }

        /**
         * Convenience for passing {@code true} through to {@link #expand(boolean)}.
         */
        public Config expand() {
            return expand (true);
        }

        /**
         * To allow for the gap between the checkbox and the label to expand so the
         * label and checkbox appear at the ends of the containment area for the
         * control.
         * <p>
         * When placing in a control group ensure that the containment area grows.
         * 
         * @param expand
         *               {@code true} to place the label on the left.
         * @return this configuration instance.
         */
        public Config expand(boolean expand) {
            this.expand = expand;
            return this;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.component.Component.Config#build(com.effacy.jui.core.client.component.layout.LayoutData[])
         */
        @Override
        @SuppressWarnings("unchecked")
        public CheckControl build(LayoutData... data) {
            return build (new CheckControl (this), data);
        }

    }

    /**
     * Construct with configuration.
     * 
     * @param config
     *               the configuration.
     */
    public CheckControl(CheckControl.Config config) {
        super (config);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.control.Control#prepareValueForAssignment(java.lang.Object)
     */
    @Override
    protected Boolean prepareValueForAssignment(Boolean value) {
        // This avoids the issue of the equivalence of null and the empty
        // string: convert null values to empty strings.
        return (value == null) ? false : super.prepareValueForAssignment (value);
    }

    /************************************************************************
     * Rendering and styles.
     ************************************************************************/

    /**
     * The input element.
     */
    protected HTMLInputElement inputEl;

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.control.Control#valueFromSource()
     */
    @Override
    public Boolean valueFromSource() {
        boolean value = inputEl.checked;
        if (value)
            getRoot ().classList.add (styles ().active ());
        else
            getRoot ().classList.remove (styles ().active ());
        return value;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.control.Control#valueToSource(java.lang.Object)
     */
    @Override
    public void valueToSource(Boolean value) {
        if (value) {
            inputEl.checked = true;
            getRoot ().classList.add (styles ().active ());
        } else {
            inputEl.checked = false;
            getRoot ().classList.remove (styles ().active ());
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#buildNode(com.effacy.jui.core.client.component.Component.Config)
     */
    @Override
    protected INodeProvider buildNode(Element el, Config data) {
        return Wrap.$ (el).$ (root -> {
            Div.$ (root).$ (inner -> {
                inner.style (styles ().inner ());
                Div.$ (inner).$ (item -> {
                    item.style (styles ().item ());
                    if (data.slider)
                        item.style (styles ().toggle ());
                    if (data.left)
                        item.style (styles ().reverse ());
                    if (data.expand)
                        item.style (styles ().expand ());
                    if (data.labelBold)
                        item.style (styles ().bold ());
                    Label.$ (item).$ (toggle -> {
                        toggle.style (styles ().toggle ());
                        toggle.attr ("for", "check_ctl_" + getUUID ());
                        Input.$ (toggle, "checkbox")
                                .id ("check_ctl_" + getUUID ())
                                .attr ("name", data.getName ())
                                .by ("input")
                                .on (e -> {
                                    modified ();
                                    TimerSupport.defer (() -> e.getTarget ().focus ());
                                }, UIEventType.ONCHANGE)
                                .on (e -> {
                                    if (!KeyCode.ENTER.is (e.getKeyCode ()))
                                        return;
                                    HTMLInputElement inp = Js.cast (e.getTarget ());
                                    inp.checked = !inp.checked;
                                    modified ();
                                    e.stopEvent ();
                                }, UIEventType.ONKEYPRESS)
                                .testId(buildTestId ("input")).testRef("input");
                    });
                    if (!StringSupport.empty(data.label)) {
                        Span.$ (item).style (styles ().spacer ());
                        Label.$ (item)
                            .attr ("for", "check_ctl_" + getUUID ()).$ (label -> {
                                Span.$ (label).text (data.label);
                                if (!StringSupport.empty(data.description))
                                    Span.$ (label).style(styles().description()).text (data.description);
                            });
                    }
                });
            });
        }).build (tree -> {
            inputEl = (HTMLInputElement) manageFocusEl (tree.first ("input"));
        });
    }

    /********************************************************************
     * CSS with standard styles.
     ********************************************************************/

    /**
     * Styles (made available to selection).
     */
    protected ILocalCSS styles() {
        return config ().style.styles ();
    }

    public static interface ILocalCSS extends IControlCSS {

        /**
         * Inner wrap around the control (for the border).
         */
        public String inner();

        public String item();

        public String reverse();

        public String expand();

        public String bold();

        public String spacer();

        public String toggle();

        public String active();

        public String description();
    }

    /**
     * Component CSS (horizontal).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        "com/effacy/jui/ui/client/control/CheckControl.css",
        "com/effacy/jui/ui/client/control/CheckControl_Override.css"
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
