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

import java.util.function.Consumer;
import java.util.function.Function;

import org.gwtproject.safehtml.shared.SafeHtmlBuilder;

import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.IFocusBlurListener;
import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.control.Control;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.Textarea;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.platform.util.client.TimerSupport;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLTextAreaElement;

/**
 * A control akin to an HTML <code>TEXTAREA</code> for the entering of a body of
 * (unformatted) text.
 * 
 * @author Jeremy Buckley
 */
public class TextAreaControl extends Control<String, TextAreaControl.Config> {

    /************************************************************************
     * Configuration and construction
     ************************************************************************/

    /**
     * Configuration for building a {@link TextAreaControl}.
     */
    public static class Config extends Control.Config<String, Config> {

        /********************************************************************
         * Variants — token-driven presentation overlays.
         ********************************************************************/

        /**
         * Variant for the component. Each variant reaches into the
         * {@code --cpt-textareactl-*} token layer at the root, overriding
         * specific tokens that the underlying CSS already consumes.
         * Pure-token overlays unless they swap the entire ILocalCSS
         * sheet (which only {@link #STANDARD} does).
         */
        @FunctionalInterface
        public interface Variant {

            /** Applies variant configuration. */
            void configure(Config cfg);

            /**
             * Standard visual style — bordered text area with the
             * shared control surface. Default; switches to the
             * standard CSS sheet (resetting any prior variant-applied
             * token overlays is the caller's responsibility).
             */
            public static final Variant STANDARD = config -> {
                config.styles (StandardLocalCSS.instance ());
            };

            /**
             * Transparent visual style — no border / background until
             * hover; italic placeholder; compact padding. Suited for
             * inline-edit prose fields.
             */
            public static final Variant TRANSPARENT = config -> {
                config.css("""
                    --cpt-textareactl-bg: transparent;
                    --cpt-textareactl-border: transparent;
                    --cpt-textareactl-border-hover: var(--jui-comp-control-border);
                    --cpt-textareactl-padding: 0.55em 0.25em;
                    --cpt-textareactl-placeholder-style: italic;
                """);
            };

            /**
             * Inline-edit visual style — very light outline at rest,
             * slightly darker background on hover, and a dim pencil
             * affordance in the top-right hinting that the value is
             * editable in place. Suited for click-to-edit prose
             * fields embedded directly in a read-oriented surface.
             */
            public static final Variant INLINE = config -> {
                config.css("""
                    --cpt-textareactl-bg: transparent;
                    --cpt-textareactl-bg-hover: var(--jui-color-neutral10);
                    --cpt-textareactl-border: var(--jui-color-neutral10);
                    --cpt-textareactl-border-hover: var(--jui-color-neutral20);
                    --cpt-textareactl-padding: 0.3em 0.4em;
                    --cpt-textareactl-icon: var(--jui-color-neutral40);
                """);
                config.iconRight (FontAwesome.pencil ());
            };
        }

        /********************************************************************
         * Backward-compat Style — bridged onto Variant.
         ********************************************************************/

        /**
         * Style for the tab set (defines presentation configuration
         * including CSS).
         * <p>
         * Retained for backward compatibility — implementations that
         * supply a custom {@link ILocalCSS} continue to work; the
         * default {@link #configure} bridges onto {@link Variant} by
         * applying the supplied styles. Prefer {@link Variant} for
         * new code.
         */
        @Deprecated
        public interface Style extends Variant {}

        /**
         * The styles bundle in effect. Variants typically leave this
         * as {@link StandardLocalCSS} and overlay token CSS at the
         * root; a custom {@link Style} can swap to a different sheet.
         */
        private ILocalCSS styles = StandardLocalCSS.instance ();

        /**
         * See {@link #height(Length)}.
         */
        protected Length height;

        /**
         * See {@link #counter(boolean)}.
         */
        private boolean counter;

        /**
         * See {@link #max(int)}.
         */
        private int max = 0;

        /**
         * See {@link #rows(int)}.
         */
        private int rows = 0;

        /**
         * See {@link #cols(int)}.
         */
        private int cols = 0;

        /**
         * See {@link #resizable(boolean)}.
         */
        private boolean resizable = false;

        /**
         * See {@link #nowrap(boolean)}.
         */
        private boolean nowrap = false;

        /**
         * See {@link #placeholder(String)}.
         */
        private String placeholder;

        /**
         * See {@link #iconRight(String)}.
         */
        private String iconRight;

        /**
         * See {@link #expandOnFocus(Length)}.
         */
        private Length expandOnFocus;

        /**
         * See {@link #expandOnFocus(Length,boolean)}.
         */
        private boolean expandOnFocusRetain;

        /**
         * See {@link #expandOnContent(boolean)}.
         */
        private boolean expandOnContent;

        /**
         * See {@link #keyPressHandler(Function)}.
         */
        private Function<String, Boolean> keyPressHandler;

        /**
         * See {@link #pasteProcessor(Function)}.
         */
        private Function<String, String> pasteProcessor;

        /**
         * Construct with a default style.
         */
        public Config() {
            super ();
        }

        /**
         * Construct with a specific variant.
         *
         * @param variant
         *                the variant.
         */
        public Config(Variant variant) {
            super ();
            variant (variant);
        }

        /**
         * Construct with a style.
         *
         * @param style
         *              the style.
         */
        @Deprecated
        public Config(Style style) {
            super ();
            style (style);
        }

        /**
         * Assigns a presentation variant.
         *
         * @param variant
         *                the variant (default is {@link Variant#STANDARD}).
         * @return this configuration instance.
         */
        public Config variant(Variant variant) {
            if (variant != null)
                variant.configure (this);
            return this;
        }

        /**
         * Assigns a set of presentation variants in order.
         *
         * @param variants
         *                 the variants.
         * @return this configuration instance.
         */
        public Config variant(Variant... variants) {
            if (variants != null) {
                for (Variant variant : variants)
                    variant (variant);
            }
            return this;
        }

        /**
         * Assigns a presentation style.
         *
         * @param style
         *              the style (default is {@link Style#STANDARD}).
         * @return this configuration.
         */
        @Deprecated
        public Config style(Variant style) {
            if (style != null)
                style.configure (this);
            return this;
        }

        /**
         * Assigns the styles bundle to use for the control. Variants
         * call this when swapping the underlying sheet (typically only
         * {@link Variant#STANDARD} and {@link Style#create} bridges).
         *
         * @param styles
         *               the styles.
         * @return this configuration instance.
         */
        public Config styles(ILocalCSS styles) {
            if (styles != null)
                this.styles = styles;
            return this;
        }

        /**
         * Getter for {@link #styles(ILocalCSS)}.
         */
        public ILocalCSS getStyles() {
            if (styles == null)
                styles = StandardLocalCSS.instance ();
            return styles;
        }

        /**
         * Places an icon in the top-right corner of the text area
         * (inside the border). Used by {@link Variant#INLINE} to hint
         * at click-to-edit affordance.
         *
         * @param iconRight
         *                  icon CSS (i.e. {@link FontAwesome}).
         * @return this configuration instance.
         */
        public Config iconRight(String iconRight) {
            this.iconRight = iconRight;
            return this;
        }

        /**
         * Assigns a height to the control text area.
         * 
         * @param height
         *            the height to apply.
         * @return this configuration instance.
         */
        public Config height(Length height) {
            this.height = height;
            return this;
        }

        /**
         * Convenience to call {@link #counter(boolean)} with {@code true}.
         */
        public Config counter() {
            return counter(true);
        }

        /**
         * Determines if a character count indicator should be displayed.
         * <p>
         * If it is and there is a {@link #max(int)} then the maximum will also be
         * displayed.
         * 
         * @param counter
         *                {@code true} if to display a character counter.
         * @return this configuration instance.
         */
        public Config counter(boolean counter) {
            this.counter = counter;
            return this;
        }

        /**
         * Assigns the maximum number of characters the field can permit.
         * 
         * @param max
         *            the maximum (if 0 or less then no restriction is applied).
         * @return this configuration instance.
         */
        public Config max(int max) {
            this.max = max;
            return this;
        }

        /**
         * The number of rows to enforce.
         * 
         * @param rows
         *             the number.
         * @return this configuration instance.
         */
        public Config rows(int rows) {
            this.rows = rows;
            return this;
        }

        /**
         * The number of columns to enforce.
         * 
         * @param cols
         *             the number.
         * @return this configuration instance.
         */
        public Config cols(int cols) {
            this.cols = cols;
            return this;
        }

        /**
         * Convenience to call {@link #resizable(boolean)} with {@code true}.
         * 
         * @return this configuration instance.
         */
        public Config resizable() {
            return resizable (true);
        }

        /**
         * Determines if the text area for entry can be resized by the user.
         * 
         * @param resizable
         *                  {@code true} if so (default is {@code false}).
         * @return this configuration instance.
         */
        public Config resizable(boolean resizable) {
            this.resizable = resizable;
            return this;
        }

        /**
         * Convenience to call {@link #nowrap(boolean)} with {@code true}.
         * 
         * @return this configuration instance.
         */
        public Config nowrap() {
            return nowrap (true);
        }

        /**
         * Determines if the text area for entry should not wrap lines.
         * 
         * @param nowrap
         *                  {@code true} if not to wrap (default is {@code false}).
         * @return this configuration instance.
         */
        public Config nowrap(boolean nowrap) {
            this.nowrap = nowrap;
            return this;
        }

        /**
         * See {@link #expandOnFocus(Length, boolean)} but with no height retention.
         */
        public Config expandOnFocus(Length expandOnFocus) {
            this.expandOnFocus = expandOnFocus;
            return this;
        }

        /**
         * Assign a height to expand to when focus is gained (and when lost it reverts
         * to the prior height).
         * <p>
         * Optionally retains the height if not empty.
         * 
         * @param expandOnFocus
         *                         the height to expand to when focus is gained.
         * @param retainIfNotEmpty
         *                         {@code true} to retain the height if not empty.
         * @return this configuration instance.
         */
        public Config expandOnFocus(Length expandOnFocus, boolean retainIfNotEmpty) {
            this.expandOnFocus = expandOnFocus;
            this.expandOnFocusRetain = retainIfNotEmpty;
            return this;
        }

        /**
         * Convenience to call {@link #expandOnContent(boolean)} passing {@code true}.
         */
        public Config expandOnContent() {
            return expandOnContent(true);
        }

        /**
         * Expands the height of the text area to match the content (to reveal all the
         * content rather than scrolling).
         * 
         * @param expandOnContent
         *                        {@code true} to expand when the content changes.
         * @return this configuration instance.
         */
        public Config expandOnContent(boolean expandOnContent) {
            this.expandOnContent = expandOnContent;
            return this;
        }

        /**
         * Registers a key press handler. This (if present) will receive the various key
         * presses and return whether to accept that key press or not. It is a way of
         * filtering out key presses (or simply to respond to specific keys, like the
         * enter key).
         * 
         * @param keyPressHandler
         *                        the handler.
         * @return this configuration instance.
         */
        public Config keyPressHandler(Function<String, Boolean> keyPressHandler) {
            this.keyPressHandler = keyPressHandler;
            return this;
        }

        /**
         * Registers a paste processor. This converts the incoming content to be placed
         * into the text area.
         * 
         * @param pasteProcessor
         *                       the handler.
         * @return this configuration instance.
         */
        public Config pasteProcessor(Function<String, String> pasteProcessor) {
            this.pasteProcessor = pasteProcessor;
            return this;
        }

        /**
         * Registers the default paste processor which performs some basic cleanup.
         * 
         * @return this configuration instance.
         */
        public Config defaultPasteProcessor() {
            this.pasteProcessor = v -> {
                return v
                    .replaceAll("[\u2018\u2019\u02BC]", "'") // smart single quotes
                    .replaceAll("[\u201C\u201D]", "\"")      // smart double quotes
                    .replaceAll("\u2014", "--")              // em dash
                    .replaceAll("\u2013", "-")               // en dash
                    .replaceAll("\u2026", "...")             // ellipsis
                    .replaceAll("\u00A0", " ")               // non-breaking space
                    .replaceAll("[\u200B\u200C\u200D]", "");
            };
            return this;
        }

        /**
         * Assigns placeholder text to display when the field is empty.
         * 
         * @param placeholder
         *                    placeholder content to display.
         * @return this configuration instance.
         */
        public Config placeholder(String placeholder) {
            this.placeholder = placeholder;
            return this;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.component.Component.Config#build(com.effacy.jui.core.client.component.layout.LayoutData[])
         */
        @Override
        @SuppressWarnings("unchecked")
        public TextAreaControl build(LayoutData... data) {
            return build (new TextAreaControl (this), data);
        }

    }

    /**
     * Construct with configuration.
     * 
     * @param config
     *               the configuration.
     */
    public TextAreaControl(TextAreaControl.Config config) {
        super (config);

        if (config.expandOnFocus != null) {
            addListener(IFocusBlurListener.create(focus -> {
                height(config.expandOnFocus);
            }, blr -> {
                // This may be null which removes the height.
                if (!config.expandOnFocusRetain || StringSupport.empty(value()))
                    height(config.height);
            }));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.control.Control#prepareValueForAssignment(java.lang.Object)
     */
    @Override
    protected String prepareValueForAssignment(String value) {
        // This avoids the issue of the equivalence of null and the empty
        // string: convert null values to empty strings.
        return StringSupport.safe (super.prepareValueForAssignment (value));
    }

    /**
     * The input element.
     */
    protected HTMLTextAreaElement inputEl;

    /**
     * For the character counter.
     */
    protected HTMLElement counterEl;

    /**
     * See {@link #footer(Consumer)}.
     */
    protected Consumer<ElementBuilder> footer;

    /**
     * Assigns a builder for building out content below the textarea.
     * 
     * @param footer
     *               the builder.
     * @return this control.
     */
    public TextAreaControl footer(Consumer<ElementBuilder> footer) {
        this.footer = footer;
        return this;
    }

    /**
     * This is used to assign a new height at run time. Often used to increase the
     * size of the text area when activated.
     * <p>
     * If called prior to rendering then will have the effect of setting a height on
     * the configuration.
     * 
     * @param height
     *               the new height (if {@code null} then the natural height is
     *               imposed).
     * @return this control.
     */
    public TextAreaControl height(Length height) {
        if (!isRendered()) {
            config().height = height;
        } else {
            if (height == null)
                height = Length.px (0);
            CSS.MIN_HEIGHT.apply(inputEl, height);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.control.Control#valueFromSource()
     */
    @Override
    public String valueFromSource() {
        String value = StringSupport.safe (inputEl.value);
        return value;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.control.Control#valueToSource(java.lang.Object)
     */
    @Override
    public void valueToSource(String value) {
        inputEl.value = StringSupport.safe (value);
        if ((config().expandOnFocus != null) && config().expandOnFocusRetain) {
            if (StringSupport.empty(value))
                height(config().height);
            else
                height(config().expandOnFocus);
        }
        _updateCounter();
        if (config().expandOnContent)
            TimerSupport.defer(() -> _resize());
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#buildNode(com.effacy.jui.core.client.component.Component.Config)
     */
    @Override
    protected INodeProvider buildNode(Element el, Config data) {
        return Wrap.$(el).$(root -> {
            Div.$(root).$(inner -> {
                inner.style (styles ().inner ());
                if (!StringSupport.empty (data.iconRight))
                    inner.style (styles ().icon_right ());
                Textarea.$ (inner).$ (ta -> {
                    ta.by ("input");
                    ta.on (e -> {
                        if (!filterKeyPress (e.getKeyCode (), inputEl.value))
                            e.stopEvent ();
                    }, UIEventType.ONKEYPRESS);
                    if (config().expandOnContent)
                        ta.on (e -> _resize(), UIEventType.ONINPUT);
                    ta.on (e -> modified (), UIEventType.ONKEYUP);
                    ta.on (e -> {
                        TimerSupport.defer(() -> {
                            if (config().pasteProcessor != null)
                                inputEl.value = config().pasteProcessor.apply(inputEl.value);
                            modified ();
                        });
                    }, UIEventType.ONPASTE);
                    ta.attr ("name", StringSupport.empty (data.getName ()) ? "" + getUUID () : data.getName ());
                    if (data.rows > 0)
                        ta.attr ("rows", "" + data.rows);
                    if (data.cols > 0)
                        ta.attr ("cols", "" + data.cols);
                    if (data.max > 0)
                        ta.attr ("maxlength", "" + data.max);
                    if (!StringSupport.empty (data.placeholder))
                        ta.attr ("placeholder", new SafeHtmlBuilder ().appendEscaped (data.placeholder).toSafeHtml ().asString ());
                    if (data.height != null)
                        ta.css (CSS.HEIGHT, data.height);
                    if (data.nowrap)
                        ta.attr ("wrap", "off");
                    ta.testId (buildTestId ("input")).testRef ("input");
                });
                if (!StringSupport.empty (data.iconRight))
                    Em.$ (inner).style (styles ().right (), data.iconRight);
                if (footer != null)
                    footer.accept(inner);
            });
            if (config().counter) {
                Div.$(root).style(styles().counter()).by("counter").text ((config().max <= 0) ? "0 characters" : "0 / " + config().max);
            }
        }).build (tree -> {
            inputEl = (HTMLTextAreaElement) manageFocusEl (tree.first ("input"));
            counterEl = (HTMLElement) manageFocusEl (tree.first ("counter"));
        });
    }

    /**
     * Re-sizes the text area to fit its content, using the {@code rows}
     * attribute as the lower bound.
     * <p>
     * The rows attribute already drives a natural height on the textarea; this
     * method only applies an explicit {@code height} when the content's
     * scrollHeight exceeds that natural height. When content shrinks back
     * within the natural height the explicit height is removed so the rows
     * attribute resumes driving the size.
     * <p>
     * If the element is not yet laid out (clientHeight reads 0 — e.g. inside a
     * not-yet-visible ancestor, or the deferred call fires before layout
     * completes) this method bails out. Previous versions fell back to a
     * hardcoded 16px floor here, which produced a textarea rendered as a
     * single line instead of the configured number of rows. A later input or
     * window-resize event will retry with a valid measurement; the rows
     * attribute keeps sizing correct in the meantime.
     */
    protected void _resize() {
        inputEl.style.removeProperty("height");
        int naturalHeight = inputEl.clientHeight;
        if (naturalHeight <= 0)
            return;
        if (inputEl.scrollHeight > naturalHeight)
            CSS.HEIGHT.apply(inputEl, Length.px(inputEl.scrollHeight));
    }

    @Override
    protected void onModified() {
        super.onModified();
        _updateCounter();
    }

    /**
     * Updates the counter indicator.
     */
    protected void _updateCounter() {
        if (counterEl != null) {
            Wrap.buildInto(counterEl, el -> {
                int length = (inputEl.value == null) ? 0 : inputEl.value.length();
                if (config().max <= 0) {
                    if (length == 1)
                        el.text("1 character");
                    else
                        el.text(length + " characters");
                } else {
                    if (length < config().max) {
                        counterEl.classList.remove(styles().limit());
                        el.text(length + " / " + config().max);
                    } else {
                        counterEl.classList.add(styles().limit());
                        el.text("limit reached " + length + " / " + config().max);
                    }
                }
            });
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.control.Control#onAfterRender()
     */
    @Override
    protected void onAfterRender() {
        super.onAfterRender ();

        if (config ().resizable)
            getRoot ().classList.add (styles ().resizable ());
        if (config ().expandOnContent)
            setMonitorWindowResize (true);
    }

    @Override
    protected void onWindowResize(int width, int height) {
        if (config ().expandOnContent)
            _resize ();
    }

    /**
     * Processes a key press and determines if the change should go ahead.
     * 
     * @param keyCode
     *                the key code from the press.
     * @param value
     *                the current value on the element.
     * @return {@code true} if the key passes.
     */
    protected boolean filterKeyPress(String keyCode, String value) {
        if (config ().keyPressHandler != null) {
            Boolean response = config ().keyPressHandler.apply (keyCode);
            if ((response == null) || response)
                return true;
            return false;
        }
        return true;
    }

    /**
     * Styles (made available to selection).
     */
    protected ILocalCSS styles() {
        return config ().getStyles ();
    }

    /********************************************************************
     * CSS with standard styles.
     ********************************************************************/

    public static interface ILocalCSS extends IControlCSS {

        /**
         * Inner wrap around the control (for the border).
         */
        public String inner();

        /**
         * Top-right corner icon (e.g. INLINE variant's pencil).
         */
        public String right();

        /**
         * Applied to {@link #inner()} when {@link Config#iconRight} is
         * set — reserves right padding on the textarea so content
         * doesn't ride under the absolutely-positioned icon.
         */
        public String icon_right();

        /**
         * Allows for the text area to resize.
         */
        public String resizable();

        /**
         * Formats the character counter.
         */
        public String counter();

        /**
         * When the character counter has reached its limit.
         */
        public String limit();

    }

    /**
     * Component CSS (horizontal).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        "com/effacy/jui/ui/client/control/Control.css",
        "com/effacy/jui/ui/client/control/TextAreaControl.css",
        "com/effacy/jui/ui/client/control/TextAreaControl_Override.css"
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
