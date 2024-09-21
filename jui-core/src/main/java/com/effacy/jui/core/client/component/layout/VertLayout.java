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
package com.effacy.jui.core.client.component.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.effacy.jui.core.client.component.IComponent;
import com.effacy.jui.core.client.component.IShowHideListener;
import com.effacy.jui.core.client.component.layout.ActionBarLayout.HAlignment;
import com.effacy.jui.core.client.component.layout.VertLayout.VertLayoutData.Separator;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.dom.jquery.JQuery;
import com.effacy.jui.core.client.dom.jquery.JQueryElement;
import com.effacy.jui.platform.css.client.CssDeclaration;
import com.effacy.jui.platform.css.client.CssResource;
import com.google.gwt.core.client.GWT;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import jsinterop.base.Js;

/**
 * This layout stacks items vertically to their natural height. It imposes
 * optional spacing between elements and wraps elements in DIV's that include
 * specific styles indicating the absolute positioning of the elements in the
 * stack.
 * 
 * @author Jeremy Buckley
 */
public class VertLayout extends Layout {

    /**
     * Convenience to construct layout configuration.
     * 
     * @return the configuration.
     */
    public static VertLayout.Config $() {
        return new VertLayout.Config ();
    }

    /**
     * Convenience to create layout data.
     * 
     * @return the layout data.
     */
    public static VertLayoutData data() {
        return new VertLayoutData ();
    }

    /**
     * Configuration for the layout.
     */
    public static class Config {

        /**
         * See {@link #spacingIncludesFirstAndLast(boolean)}.
         */
        private boolean spacingIncludesFirstAndLast;

        /**
         * See {@link #getBaseline()}.
         */
        private VertLayoutData baseline = new VertLayoutData ();

        /**
         * Default configuration (fit both width and height).
         */
        public Config() {
            // Nothing.
        }

        /**
         * The default is to have spacing only apply to the inter-component gaps. This
         * extends that to include the first and last (which is good for the case where
         * the panel has a border).
         * 
         * @param spacingIncludesFirstAndLast
         *                                    {@code true} if to fully extend spacing.
         * @return this configuration instance.
         */
        public Config spacingIncludesFirstAndLast(boolean spacingIncludesFirstAndLast) {
            this.spacingIncludesFirstAndLast = spacingIncludesFirstAndLast;
            return this;
        }

        /**
         * Getter for {@link #spacingIncludesFirstAndLast(boolean)}.
         */
        public boolean isSpacingIncludesFirstAndLast() {
            return spacingIncludesFirstAndLast;
        }

        /**
         * Sets the spacing above to apply when there is a preceding item.
         * 
         * @param spacingAbove
         *                     the spacing above.
         * @return this configuration instance.
         */
        public Config spacingAbove(Length spacingAbove) {
            baseline.spacingAbove (spacingAbove);
            return this;
        }

        /**
         * Sets the spacing below to apply when there is a following item.
         * 
         * @param spacingBelow
         *                     the spacing above.
         * @return this configuration instance.
         */
        public Config spacingBelow(Length spacingBelow) {
            baseline.spacingBelow (spacingBelow);
            return this;
        }

        /**
         * Sets the spacing above and below to apply.
         * 
         * @param spacing
         *                the spacing above and below.
         * @return this configuration instance.
         */
        public Config spacing(Length spacing) {
            baseline.spacing (spacing);
            return this;
        }

        /**
         * Sets padding to the left and right of the component. This allows the
         * separator line (if there is one) to extend to the bound of the container
         * without having the child components to the same.
         * 
         * @param paddingSide
         *                    the padding.
         * @return this configuration instance
         */
        public Config paddingSide(Length paddingSide) {
            baseline.paddingSide (paddingSide);
            return this;
        }

        /**
         * Sets the horizontal alignment.
         * 
         * @param align
         *              the alignment.
         * @return this configuration instance.
         */
        public Config align(HAlignment align) {
            baseline.align (align);
            return this;
        }

        /**
         * Assigns a separator.
         * 
         * @param separator
         *                  the separator.
         * @return this configuration instance.
         */
        public Config separator(Separator separator) {
            baseline.separator (separator);
            return this;
        }

        /**
         * The baseline configuration.
         * 
         * @return the baselin.
         */
        public VertLayoutData getBaseline() {
            return baseline;
        }

        /**
         * Construct an instance of the layout using this configuration.
         * 
         * @return the instance
         */
        public VertLayout build() {
            return new VertLayout (this);
        }
    }

    /**
     * Layout factor for the layout.
     */
    public static final ILayoutFactory FACTORY = new ILayoutFactory () {

        @Override
        public ILayout create() {
            return new VertLayout.Config ().build ();
        }

    };

    /**
     * Data used to modify an items position in the {@link VertLayout}.
     * 
     * @author Jeremy Buckley
     */
    public static class VertLayoutData extends LayoutData {

        /**
         * Differing modes of visual separation of vertical components.
         */
        public enum Separator {
            /**
             * No separator.
             */
            NONE,

            /**
             * A single line separating each adjacent item.
             */
            LINE;
        }

        /**
         * See {@link #spacingAbove(Length)}.
         */
        private Length spacingAbove = null;

        /**
         * See {@link #spacingBelow(Length)}.
         */
        private Length spacingBelow = null;

        /**
         * See {@link #paddingSide(Length)}.
         */
        private Length paddingSide = null;

        /**
         * See {@link #align(HAlignment)}.
         */
        private HAlignment align = null;

        /**
         * See {@link #separator(Separator)}.
         */
        private Separator separator = null;

        /**
         * Empty constructor.
         */
        public VertLayoutData() {
            // Nothing.
        }

        /**
         * Copy constructor.
         * 
         * @param copy
         *             the data to copy.
         */
        public VertLayoutData(VertLayoutData copy) {
            if (copy == null)
                return;
            spacingAbove = copy.spacingAbove;
            spacingBelow = copy.spacingBelow;
            paddingSide = copy.paddingSide;
            align = copy.align;
            separator = copy.separator;
        }

        /**
         * Getter for {@link #spacingAbove(Length)}.
         */
        public Length getSpacingAbove() {
            return spacingAbove;
        }

        /**
         * Sets the spacing above to apply when there is a preceding item.
         * 
         * @param spacingAbove
         *                     the spacing above.
         * @return this configuration instance.
         */
        public VertLayoutData spacingAbove(Length spacingAbove) {
            this.spacingAbove = spacingAbove;
            return this;
        }

        /**
         * Getter for {@link #spacingBelow(Length)}.
         */
        public Length getSpacingBelow() {
            return spacingBelow;
        }

        /**
         * Sets the spacing below to apply when there is a following item.
         * 
         * @param spacingBelow
         *                     the spacing above.
         * @return this configuration instance.
         */
        public VertLayoutData spacingBelow(Length spacingBelow) {
            this.spacingBelow = spacingBelow;
            return this;
        }

        /**
         * Sets the spacing above and below to apply.
         * 
         * @param spacing
         *                the spacing above and below.
         * @return this configuration instance.
         */
        public VertLayoutData spacing(Length spacing) {
            spacingAbove (spacing);
            spacingBelow (spacing);
            return this;
        }

        /**
         * Getter for {@link #paddingSide(Length)}.
         */
        public Length getPaddingSide() {
            return paddingSide;
        }

        /**
         * Sets padding to the left and right of the component. This allows the
         * separator line (if there is one) to extend to the bound of the container
         * without having the child components to the same.
         * 
         * @param paddingSide
         *                    the padding.
         * @return this configuration instance
         */
        public VertLayoutData paddingSide(Length paddingSide) {
            this.paddingSide = paddingSide;
            return this;
        }

        /**
         * Getter for {@link #align(HAlignment)}.
         */
        public HAlignment getAlign() {
            return align;
        }

        /**
         * Sets the horizontal alignment.
         * 
         * @param align
         *              the alignment.
         * @return this configuration instance.
         */
        public VertLayoutData align(HAlignment align) {
            this.align = (align == null) ? HAlignment.CENTER : align;
            return this;
        }

        /**
         * Getter for {@link #separator(Separator)}.
         */
        public Separator getSeparator() {
            return separator;
        }

        /**
         * Assigns a separator.
         * 
         * @param separator
         *                  the separator.
         * @return this configuration instance.
         */
        public VertLayoutData separator(Separator separator) {
            this.separator = separator;
            return this;
        }

    }

    /************************************************************************
     * Configuration.
     ************************************************************************/

    /**
     * The styles.
     */
    private ILocalCSS styles = LocalCSS.instance ();

    /**
     * The layout configuration.
     */
    private VertLayout.Config config;

    /**
     * Construct with configuration.
     * 
     * @param config
     *               the configuration.
     */
    public VertLayout(VertLayout.Config config) {
        this.config = config;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.layout.Layout#renderComponent(com.effacy.jui.core.client.component.IComponent,
     *      int, elemental2.dom.Element, int)
     */
    @Override
    protected void renderComponent(IComponent component, int index, Element target, int size) {
        renderComponentLayout (component, index, target, size);
    }

    /**
     * Maps of components to their wrappers.
     */
    protected Map<IComponent, Wrapper> wrappers = new HashMap<> ();

    class Wrapper {

        Element wrapperEl;

        boolean hidden;

        VertLayoutData layoutData;

        public Wrapper(Element wrapperEl, VertLayoutData layoutData) {
            this.wrapperEl = wrapperEl;
            this.layoutData = layoutData;
        }

        public void show() {
            hidden = false;
            JQuery.$ (wrapperEl).show ();
        }

        public void hide() {
            hidden = true;
            JQuery.$ (wrapperEl).hide ();
        }

        public boolean isHidden() {
            return hidden;
        }

        public void clearSeparator() {
            wrapperEl.classList.remove (styles.separatorLine ());
        }

        public void applySeparator() {
            wrapperEl.classList.add (styles.separatorLine ());
        }

        public void applySeparatorIf() {
            if (Separator.LINE == layoutData.getSeparator ())
                applySeparator ();
        }

        public void only() {
            wrapperEl.classList.add (styles.only ());
            wrapperEl.classList.remove (styles.first ());
            wrapperEl.classList.remove (styles.middle ());
            wrapperEl.classList.remove (styles.last ());
        }

        public void first() {
            wrapperEl.classList.remove (styles.only ());
            wrapperEl.classList.add (styles.first ());
            wrapperEl.classList.remove (styles.middle ());
            wrapperEl.classList.remove (styles.last ());
        }

        public void middle() {
            wrapperEl.classList.remove (styles.only ());
            wrapperEl.classList.remove (styles.first ());
            wrapperEl.classList.add (styles.middle ());
            wrapperEl.classList.remove (styles.last ());
        }

        public void last() {
            wrapperEl.classList.remove (styles.only ());
            wrapperEl.classList.remove (styles.first ());
            wrapperEl.classList.remove (styles.middle ());
            wrapperEl.classList.add (styles.last ());
        }

        public void spacingAbove(boolean apply) {
            if (!apply) {
                CSS.PADDING_TOP.with (Length.px (0)).apply (JQuery.$ (wrapperEl));
            } else {
                Length spacing = layoutData.getSpacingAbove ();
                if (spacing == null)
                    spacing = Length.px (0);
                CSS.PADDING_TOP.with (spacing).apply (JQuery.$ (wrapperEl));
            }
        }

        public void spacingBelow(boolean apply) {
            if (!apply) {
                CSS.PADDING_BOTTOM.with (Length.px (0)).apply (JQuery.$ (wrapperEl));
            } else {
                Length spacing = layoutData.getSpacingBelow ();
                if (spacing == null)
                    spacing = Length.px (0);
                CSS.PADDING_BOTTOM.with (spacing).apply (JQuery.$ (wrapperEl));
            }
        }
    }

    /**
     * See {@link #renderComponentLayout(IComponent, int, Elem, int)} but takes an
     * {@link Element}.
     */
    protected IComponentLayout renderComponentLayout(IComponent c, int index, Element target, int size) {
        try {
            // Obtain layout data for the component. This is a copy so is unique
            // to this method invocation.
            VertLayoutData layoutData = getLayoutData (c);

            // Create and insert the wrapper.
            final Wrapper wrapper = new Wrapper (DomGlobal.document.createElement ("DIV"), layoutData);
            wrappers.put (c, wrapper);
            wrapper.wrapperEl.classList.add (styles.wrap ());
            int numberOfChildren = target.childElementCount;
            if (index < 0)
                index = numberOfChildren;
            if (index >= numberOfChildren)
                target.appendChild (wrapper.wrapperEl);
            else
                target.insertBefore (wrapper.wrapperEl, target.childNodes.getAt (index));

            // Apply any side padding.
            if (layoutData.getPaddingSide () != null) {
                JQueryElement wel = JQuery.$ (wrapper.wrapperEl);
                CSS.PADDING_LEFT.with (layoutData.getPaddingSide ()).apply (JQuery.$ (wel));
                CSS.PADDING_RIGHT.with (layoutData.getPaddingSide ()).apply (JQuery.$ (wel));
            }

            // Apply any alignment.
            if (HAlignment.LEFT == layoutData.getAlign ())
                wrapper.wrapperEl.classList.add (styles.alignLeft ());
            else if (HAlignment.RIGHT == layoutData.getAlign ())
                wrapper.wrapperEl.classList.add (styles.alignRight ());
            else
                wrapper.wrapperEl.classList.add (styles.alignMiddle ());

            // Render the component into the target.
            Element targetEl = wrapper.wrapperEl;
            if (c.getRoot () != null)
                targetEl.appendChild (Js.cast (c.getRoot ()));
            else
                c.render (Js.cast (targetEl), 0);

            // Listener to show and hide events and show / hide the wrapper.
            c.addListener (new IShowHideListener () {

                @Override
                public void onShow(IComponent component) {
                    wrapper.show ();
                    applyPositionalStyles ();
                }

                @Override
                public void onHide(IComponent component) {
                    wrapper.hide ();
                    applyPositionalStyles ();
                }

            });

            // Hide if the component is currently marked as hidden.
            if (c.isHidden ())
                wrapper.hide ();

            // Apply the positional styles.
            applyPositionalStyles ();

            // Return a layout result.
            return new ComponentLayout (Js.cast (wrapper.wrapperEl), Js.cast (c.getRoot ()));
        } catch (Throwable e) {
            if (GWT.getUncaughtExceptionHandler () != null)
                GWT.getUncaughtExceptionHandler ().onUncaughtException (e);
            throw e;
        }
    }

    /**
     * Goes through and applies the positional styles to the wrappers based on which
     * ones are enabled or not.
     */
    protected void applyPositionalStyles() {
        List<Wrapper> activeWrappers = new ArrayList<> ();
        for (IComponent cpt : getItems ()) {
            Wrapper wrapper = wrappers.get (cpt);
            if ((wrapper != null) && !wrapper.isHidden ())
                activeWrappers.add (wrapper);
        }
        if (activeWrappers.isEmpty ())
            return;

        if (activeWrappers.size () == 1) {
            Wrapper wrapper = activeWrappers.get (0);
            wrapper.only ();
            wrapper.spacingAbove (config.isSpacingIncludesFirstAndLast ());
            wrapper.spacingBelow (config.isSpacingIncludesFirstAndLast ());
            return;
        }

        int size = activeWrappers.size ();
        for (int index = 0; index < size; index++) {
            Wrapper wrapper = activeWrappers.get (index);
            if (index == 0)
                wrapper.clearSeparator ();
            else
                wrapper.applySeparatorIf ();
            boolean spacingAbove = true;
            boolean spacingBelow = true;
            if (index == 0) {
                wrapper.first ();
                spacingAbove = config.isSpacingIncludesFirstAndLast ();
            } else if (index == (size - 1)) {
                wrapper.last ();
                spacingBelow = config.isSpacingIncludesFirstAndLast ();
            } else {
                wrapper.middle ();
            }
            wrapper.spacingAbove (spacingAbove);
            wrapper.spacingBelow (spacingBelow);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.core.client.container.layout.StyleLayout#getLayoutData(com.effacy.jui.core.client.component.IComponent)
     */
    @SuppressWarnings("unchecked")
    protected VertLayoutData getLayoutData(IComponent c) {
        VertLayoutData layoutData = null;
        LayoutData data = super.getLayoutData (c);
        if (data instanceof VertLayoutData)
            layoutData = new VertLayoutData ((VertLayoutData) data);
        else
            layoutData = new VertLayoutData ();

        if (layoutData.getSpacingAbove () == null)
            layoutData.spacingAbove ((config.getBaseline ().spacingAbove == null) ? Length.px (0) : config.getBaseline ().spacingAbove);
        if (layoutData.getSpacingBelow () == null)
            layoutData.spacingBelow ((config.getBaseline ().spacingBelow == null) ? Length.px (0) : config.getBaseline ().spacingBelow);
        if (layoutData.getAlign () == null)
            layoutData.align ((config.getBaseline ().align == null) ? HAlignment.CENTER : config.getBaseline ().align);
        if (layoutData.getSeparator () == null)
            layoutData.separator ((config.getBaseline ().separator == null) ? Separator.NONE : config.getBaseline ().separator);
        if (layoutData.getPaddingSide () == null)
            layoutData.paddingSide ((config.getBaseline ().paddingSide == null) ? Length.px (0) : config.getBaseline ().paddingSide);

        return layoutData;
    }

    /************************************************************************
     * Styles.
     ************************************************************************/

    /**
     * CSS styles for the layout.
     */
    public static interface ILocalCSS extends CssDeclaration {

        /**
         * Wrapper.
         */
        public String wrap();

        /**
         * Class to apply to the first element.
         * 
         * @return The style.
         */
        public String first();

        /**
         * Class to apply to any element that is neither first nor last.
         * 
         * @return The style.
         */
        public String middle();

        /**
         * Class to apply to the last element.
         * 
         * @return The style.
         */
        public String last();

        /**
         * Single element.
         */
        public String only();

        /**
         * Right left.
         */
        public String alignLeft();

        /**
         * Middle alignment.
         */
        public String alignMiddle();

        /**
         * Right alignment.
         */
        public String alignRight();

        /**
         * Separator line (solid).
         */
        public String separatorLine();
    }

        /**
     * Component CSS (standard).
     */
    @CssResource({
        "com/effacy/jui/core/client/component/layout/VertLayout.css", 
        "com/effacy/jui/core/client/component/layout/VertLayout_Override.css"
    })
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
