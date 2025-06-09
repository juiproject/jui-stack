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
package com.effacy.jui.ui.client.control.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.effacy.jui.core.client.IDirtable;
import com.effacy.jui.core.client.IEditable;
import com.effacy.jui.core.client.IOpenAware;
import com.effacy.jui.core.client.IResetable;
import com.effacy.jui.core.client.IResolver;
import com.effacy.jui.core.client.component.Component;
import com.effacy.jui.core.client.component.IComponent;
import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.layout.ActionBarLayout;
import com.effacy.jui.core.client.control.IControl;
import com.effacy.jui.core.client.control.IControl.Value;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.IDomInsertable;
import com.effacy.jui.core.client.dom.builder.Li;
import com.effacy.jui.core.client.dom.builder.P;
import com.effacy.jui.core.client.dom.builder.Ul;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.dom.jquery.JQuery;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.TimerSupport;
import com.effacy.jui.rpc.client.ErrorMessage;
import com.effacy.jui.ui.client.control.builder.GroupBuilder.IGroupBuilderCSS;
import com.effacy.jui.ui.client.control.builder.IGroupBuilder.IRowBuilder.IControlCell;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.effacy.jui.ui.client.panel.Panel;
import com.effacy.jui.ui.client.panel.PanelCreator;
import com.effacy.jui.validation.model.IErrorMessage;
import com.effacy.jui.validation.model.IInvalidatable;
import com.effacy.jui.validation.model.IValidatable;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;

public class ControlForm<SRC,DST> extends Component<ControlForm.Config> implements IModificationContext, IGroupBuilder<SRC,DST>, IFormBuilder<SRC,DST>, IResetable, IDirtable, IValidatable, IInvalidatable, IEditable<SRC>, IOpenAware {

    /**
     * Configuration for the control form.
     */
    public static class Config extends Component.Config {

        /**
         * Styles for the control form.
         */
        public interface Style {

            /**
             * The CSS styles.
             */
            public ILocalCSS styles();

            /**
             * The CSS style to use for the question mark.
             */
            public String questionIcon();

            /**
             * Convenience to create a style.
             * 
             * @param styles
             *               the CSS styles.
             * @return the associated style.
             */
            public static Style create(ILocalCSS styles, String questionIcon) {
                return new Style () {

                    @Override
                    public ILocalCSS styles() {
                        return styles;
                    }

                    @Override
                    public String questionIcon() {
                        return questionIcon;
                    }

                };
            }

            public static final Style STANDARD = create (StandardLocalCSS.instance (), FontAwesome.circleQuestion(FontAwesome.Option.BOLD));

            public static final Style COMPACT = create (CompactLocalCSS.instance (), FontAwesome.circleQuestion(FontAwesome.Option.BOLD));

        }

        /**
         * See {@link #style(Style)}.
         */
        protected Style style = Style.STANDARD;

        /**
         * See {@link #startingDepth(int)}.
         */
        protected int startingDepth;

        /**
         * See {@link #padding(Insets)}.
         */
        protected Insets padding;

        /**
         * See {@link #focusOnReset()}.
         */
        protected boolean focusOnReset;

        /**
         * See {@link #errorMessage(String)}.
         */
        protected String errorMessage = "There was a problem submitting the form, please see messages below.";

        /**
         * See {@link #maxWidth(Length)}.
         */
        protected Length maxWidth;

        /**
         * See {@link #modificationHandler(Consumer)}.
         */
        protected Consumer<ControlForm<?,?>> modificationHandler;

        /**
         * See {@link #setterApplyWhenNotDirty(boolean)}.
         */
        protected boolean setterApplyWhenNotDirty = false;

        protected Length barGap;

        /**
         * See {@link #errorRenderer(BiConsumer)}.
         */
        protected BiConsumer<ElementBuilder,List<? extends IErrorMessage>> errorRenderer;

        public Config() {
            testId ("control_form");
        }

        /**
         * Assigns the style to use.
         * 
         * @param style
         *              the style (ignored if {@code null}).
         * @return this configuration instance.
         */
        public Config style(Style style) {
            if (style != null)
                this.style = style;
            return this;
        }

        /**
         * Assigns a starting depth for the top-level group. This will be used to
         * determine group and row spacing.
         * 
         * @param startingDepth
         *                      the depth (between 0 and 4 inclusive).
         * @return this configuration instance.
         */
        public Config startingDepth(int startingDepth) {
            this.startingDepth = Math.max (0, Math.min (startingDepth, 4));
            return this;
        }

        /**
         * Padding to apply.
         * 
         * @param padding
         *                the padding.
         * @return this configuration instance.
         */
        public Config padding(Insets padding) {
            this.padding = padding;
            return this;
        }

        /**
         * Convenience to pass {@code true} to {@link #focusOnReset(boolean)}.
         * 
         * @return this configuration.
         */
        public Config focusOnReset() {
            return focusOnReset (true);
        }

        /**
         * Brings focus to the first control when the form is reset.
         * 
         * @param focusOnReset
         *                     {@code true} to focus on reset.
         * @return this configuration.
         */
        public Config focusOnReset(boolean focusOnReset) {
            this.focusOnReset = focusOnReset;
            return this;
        }

        /**
         * Assigns the error message to display.
         * 
         * @param errorMessage
         *                     the error message.
         * @return this configuration.
         */
        public Config errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        /**
         * Assigns a maximum width.
         * 
         * @param maxWidth
         *                 the maximum width.
         * @return this configuration.
         */
        public Config maxWidth(Length maxWidth) {
            this.maxWidth = maxWidth;
            return this;
        }

        /**
         * Assigns a modification listener to listen to changes in the form controls.
         * 
         * @param modification
         *                     the listener.
         * @return this configuration.
         */
        public Config modificationHandler(Consumer<ControlForm<?,?>> modificationHandler) {
            this.modificationHandler = modificationHandler;
            return this;
        }

        /**
         * The default behaviour when performing a set. Normally controls only partake
         * in setting when they are dirty, this can change that so all control partake
         * regardeless. This can be overridden on a per-control basis.
         * 
         * @param setterApplyWhenNotDirty
         *                                {@code true} if non-dirty control invoke the
         *                                declared setter.
         * @return this configuration.
         */
        public Config setterApplyWhenNotDirty(boolean setterApplyWhenNotDirty) {
            this.setterApplyWhenNotDirty = setterApplyWhenNotDirty;
            return this;
        }

        /**
         * When employing a bar a gap is created between the form body and the bar. This
         * can be set here.
         * 
         * @param barGap
         *               the gap.
         * @return this configuration instance.
         */
        public Config barGap(Length barGap) {
            this.barGap = barGap;
            return this;
        }

        /**
         * Provide your own error message renderer.
         * 
         * @param errorRenderer
         *                      the renderer.
         * @return this configuration.
         */
        public Config errorRenderer(BiConsumer<ElementBuilder,List<? extends IErrorMessage>> errorRenderer) {
            this.errorRenderer = errorRenderer;
            return this;
        }

    }

    /**
     * The top-level group to build out.
     */
    private FormBuilder<SRC,DST> form;

    /**
     * See {@link #resetHandler(Consumer)}.
     */
    private Consumer<IModificationContext> resetHandler;

    /**
     * Construct an instance with the given configuration.
     * 
     * @param config
     *               the configuration.
     */
    public ControlForm(Config config) {
        super (config);
        form = new FormBuilder<SRC,DST> (new FormBuilder.IFormBuilderConfig () {

            @Override
            public String questionIcon() {
                return config.style.questionIcon ();
            }

            @Override
            public boolean setterApplyWhenNotDirty() {
                return config.setterApplyWhenNotDirty;
            }

            @Override
            public ILocalCSS styles() {
                return ControlForm.this.styles ();
            }

            @Override
            public int initialDepth() {
                return config.startingDepth;
            }

        });
    }
    
    @Override
    public IModificationContext modificationContext() {
        return form.modificationContext ();
    }

    /**
     * Used by {@link #onDirtyStateChanged()} to determine if
     * {@link #onDirtyStateChanged(boolean)} should be called.
     */
    private boolean lastDirty = false;

    @Override
    protected void onControlModified(IControl<?> control) {
        super.onControlModified (control);
        if (config ().modificationHandler != null)
            config ().modificationHandler.accept (this);
        if (lastDirty != dirty()) {
            lastDirty = !lastDirty;
            onDirtyStateChanged (lastDirty);
        }
    }

    /**
     * Invoked when there is a change of dirty state. This is always called on open.
     * 
     * @param dirty
     *              {@code true} if dirty.
     */
    protected void onDirtyStateChanged(boolean dirty) {
        // Nothing.
    }

    /**
     * Sets the dirty detection mechanism and calls
     * {@link #onDirtyStateChanged(boolean)}.
     */
    @Override
    public void onOpen() {
        lastDirty = dirty();
        onDirtyStateChanged (lastDirty);
    }

    /************************************************************************
     * Navigation behaviours.
     ************************************************************************/

    /**
     * The number of pages.
     * 
     * @return the number.
     */
    public int size() {
        return form.size ();
    }

    /**
     * Activates the given page and applies focus to the first element (where
     * requested).
     * 
     * @param idx
     *                   the index of the page.
     * @param focusFirst
     *                   {@code true} if should bring the first control into focus.
     */
    public void activate(int idx, boolean focusFirst) {
        GroupBuilder<SRC,DST> grp = form.activate (idx);
        if ((grp != null) && focusFirst)
            grp.focusFirst ();
        _clearInvalid ();
    }

    @Override
    public IGroupBuilder<SRC, DST> activate(int idx) {
        return form.activate (idx);
    }

    /**
     * Used by {@link #navigator()}.
     */
    private INavigator navigator;

    /**
     * Obtains a navigator.
     * <p>
     * If none has been assigned the default linear navigator will be constructed
     * and returned.
     * 
     * @return the navigator.
     */
    public INavigator navigator() {
        if (navigator == null) {
            navigator = new INavigator() {

                private int step = 0;

                @Override
                public int step() {
                    return step;
                }

                @Override
                public int range() {
                    return form.size ();
                }

                @Override
                public void reset() {
                    step = 0;
                    _clearInvalid();
                    GroupBuilder<SRC,DST> grp = form.activate (0);
                    if (config ().focusOnReset)
                        grp.focusFirst ();
                }

                @Override
                public Directions previous(boolean clear) {
                    if (clear) {
                        // Easiest way is to roll through each control and reset directly.
                        form.group(step).forEachCell(cell ->{
                            if (cell.control () != null)
                                cell.control ().reset ();
                            return true;
                        });
                    }
                    if (step <= 0)
                        return directions ();
                    step--;
                    _clearInvalid();
                    form.activate (step);
                    return directions ();
                }

                @Override
                public Directions next() {
                    if (step >= range() - 1)
                        return directions ();
                    _clearInvalid();
                    // Use the scoped validation as that will accumulate errors on the form.
                    if (validate (step)) {
                        step++;
                        GroupBuilder<SRC,DST> grp = form.activate (step);
                        if (config ().focusOnReset)
                            grp.focusFirst ();
                    }
                    return directions ();
                }
                
            };
        }
        return navigator;
    }

    /************************************************************************
     * Top-level behaviours.
     ************************************************************************/

    /**
     * Creates a (right aligned) panel with a bar layout for the placement of
     * buttons.
     * 
     * @param builder
     *                to build out the bar.
     * @return this form.
     */
    public IGroupBuilder<SRC,DST> bar(Consumer<Panel> builder) {
        return bar (true, builder);
    }

    /**
     * Creates a panel with a bar layout for the placement of
     * buttons.
     * 
     * @param alignRight
     *                   if should be right aligned (otherwise will be created with
     *                   two zones, see {@link ActionBarLayout}).
     * @param builder
     *                   to build out the bar.
     * @return this form.
     */
    public IGroupBuilder<SRC,DST> bar(boolean alignRight, Consumer<Panel> builder) {
        if (config().barGap == null)
            config().barGap = Length.em (0.5);
        Panel panel = alignRight ? PanelCreator.buttonBarRightAligned (cfg -> {
            cfg.adorn(el -> CSS.MARGIN_TOP.apply(el, config().barGap));
        }, builder) :  PanelCreator.buttonBar (cfg -> {
            cfg.adorn(el -> CSS.MARGIN_TOP.apply(el, config().barGap));
        }, builder);
        form.insert (panel);
        return this;
    }

    @Override
    public IPage<SRC, DST> page() {
        return form.page ();
    }

    @Override
    public IPage<SRC, DST> page(int depth) {
        return form.page (depth);
    }

    /************************************************************************
     * Behaviour as a {@link IGroupBuilder}. All these delegate to the
     * top-level group.
     ************************************************************************/

    @Override
    public IGroupBuilder<SRC,DST> adorn(BiConsumer<ElementBuilder,ElementBuilder> adorn) {
        return form.adorn (adorn);
    }

    @Override
    public IGroupBuilder<SRC,DST> by(String reference) {
        return form.by (reference);
    }

    @Override
    public IGroupBuilder<SRC,DST> indent(Length offset) {
        return form.indent (offset);
    }
    
    @Override
    public IGroupBuilder<SRC,DST> gap(Length offset) {
        return form.gap (offset);
    }

    @Override
    public IGroupBuilder<SRC, DST> separator() {
        return form.separator ();
    }

    @Override
    public IGroupBuilder<SRC, DST> horizontal() {
        return form.horizontal ();
    }

    @Override
    public IGroupBuilder<SRC, DST> conditional(Consumer<IGroupConditionalBuilder> conditional) {
        return form.conditional (conditional);
    }

    @Override
    public IGroupBuilder<SRC, DST> footer(Consumer<IFooterBuilder> header) {
        return form.footer (header);
    }

    @Override
    public IGroupBuilder<SRC, DST> group(Consumer<IGroupBuilder<SRC, DST>> group) {
        return form.group (group);
    }

    @Override
    public IGroupBuilder<SRC, DST> header(Consumer<IHeaderBuilder> header) {
        return form.header (header);
    }

    @Override
    public IGroupBuilder<SRC, DST> insert(IDomInsertable... insertable) {
        return form.insert (insertable);
    }

    @Override
    public IGroupBuilder<SRC, DST> row(Consumer<IRowBuilder<SRC, DST>> row) {
        return form.row (row);
    }

    @Override
    public IGroupBuilder<SRC, DST> component(IComponent cpt, Consumer<IRowBuilder.ICell> handler) {
        return form.component (cpt, handler);
    }

    @Override
    public <V, CTL extends IControl<V>> IGroupBuilder<SRC, DST> control(String by, String label, CTL ctl, Consumer<IControlCell<V, CTL, SRC, DST>> handler) {
        return form.control (by, label, ctl, handler);
    }

    /************************************************************************
     * Behaviour as an {@link IModificationContext}.
     ************************************************************************/

    @Override
    public <W> IControl<W> control(String reference) {
        return form.control (reference);
    }

    @Override
    public <W> W value(String reference) {
        return form.value (reference);
    }

    @Override
    public <W> W value(String reference, W defaultValue) {
        return form.value(reference, defaultValue);
    }

    @Override
    public <W> void set(String reference, W value) {
        form.set (reference, value);
    }

    @Override
    public <W> void set(String reference, Value<W> value) {
        form.set (reference, value);
    }

    @Override
    public void disable(String... references) {
        form.disable (references);
    }

    @Override
    public void enable(String... references) {
        form.enable (references);
    }

    @Override
    public boolean groupOpen(String reference) {
        return form.groupOpen (reference);
    }

    @Override
    public void hide(String... references) {
        form.hide (references);
    }

    @Override
    public void show(String... references) {
        form.show (references);
    }

    /**
     * A handler that is invoked once the form elements have been built into the
     * DOM. This is invoked before any of the component callbacks are executed, but
     * after the modification context is available. You can then safely perform any
     * modifications.
     * 
     * @param onBuild
     *                the handler to invoke (is passed the modification context).
     */
    public IFormBuilder<SRC,DST> onBuild(Consumer<IModificationContext> onBuild) {
        form.onBuild (onBuild);
        return this;
    }

    /************************************************************************
     * Behaviour as {@link IDirtable}
     ************************************************************************/

    @Override
    public boolean dirty() {
        return super.controls ().dirty ();
    }

    /************************************************************************
     * Behaviour as {@link IResetable}
     ************************************************************************/

    @Override
    public void reset() {
        // We don't reset while loading. Once loading has finished a reset is performed.
        if (loading)
            return;

        super.controls ().reset ();
        if ((form != null) && (resetHandler != null))
            resetHandler.accept (form);
        preRenderErrorMessages = null;
        
        if (isRendered()) {
            if (navigator != null) {
                navigator.reset ();
            } else {
                _clearInvalid ();
                activate (0, config ().focusOnReset);
            }
        }
    }

    /**
     * Assigns a handler that is invoked when the form is reset (see
     * {@link IResetable#reset()}).
     * 
     * @param resetHandler
     *                     the handler to invoke.
     */
    public ControlForm<SRC,DST> handleReset(Consumer<IModificationContext> resetHandler) {
        this.resetHandler = resetHandler;
        return this;
    }

    /************************************************************************
     * Behaviour as {@link IValidatable}
     ************************************************************************/

    @Override
    public boolean valid() {
        return super.controls ().valid ();
    }

    @Override
    public boolean validate(boolean clearIfValid) {
        if (controls ().validate (true)) {
            if (clearIfValid)
                clearInvalid();
            return true;
        }
        invalidate (null);
        return false;
    }

    /**
     * See {@link #validate(boolean)} but only validates those controls that pass the given text.
     */
    public boolean validate(boolean clearIfValid, int idx) {
        String ref = form.group(idx).getReference();
        if (controls ().validate (true, ctl -> ctl.matchMetaAttribute("group_root", ref))) {
            if (clearIfValid)
                clearInvalid();
            return true;
        }
        invalidate (null);
        return false;
    }

    /**
     * See {@link #validate()} but only validates those controls that pass the given
     * test.
     */
    public boolean validate(int idx) {
        String ref = form.group(idx).getReference();
        if (controls ().validate (ctl -> ctl.matchMetaAttribute("group_root", ref)))
            return true;
        invalidate (null);
        return false;
    }

    /**
     * Scroll to the top of the form.
     * <p>
     * This will only work in cases where the form itself has been rendereding into
     * an element that scrolls on overflow. The implementation reaches out to the
     * parent elemnt of the components root and sets the <code>scrollTop</code> to
     * zero. A more formal approach would be to enusre that the parent component
     * has scollable content and exposes a suitable interface for scrolling.
     * Practically, however, this approach covers most cases so is good enough.
     */
    public void scrollTop() {
        if (isRendered())
            getRoot().parentElement.scrollTop = 0;
    }

    /**
     * Scroll to the bottom of the form.
     * <p>
     * See supporting notes on {@link #scrollTop()}.
     */
    public void scrollBottom() {
        if (isRendered())
            getRoot().parentElement.scrollTop = getRoot().parentElement.scrollHeight;
    }

    /************************************************************************
     * Behaviour as {@link IInvalidatable}
     ************************************************************************/

    /**
     * Stores any errors that have been applied pre-render. If this is present then
     * an error state is assumed (only {@code null} will assume no error state).
     */
    private List<? extends IErrorMessage> preRenderErrorMessages;

    /**
     * Element to render error messages into.
     */
    protected Element errorMessageEl;

    @Override
    public void invalidate(List<? extends IErrorMessage> errors) {
        preRenderErrorMessages = null;
        if (errors == null)
            errors = new ArrayList<> ();
        if (!isRendered()) {
            preRenderErrorMessages = new ArrayList<> (errors);
            return;
        }
        List<IErrorMessage> remainingErrors = new ArrayList<> (errors);
        controls ().accept (remainingErrors);
        _invalidate (remainingErrors);
    }

    /**
     * Called by {@link #invalidate(List)} with the expectation that the component
     * has been rendered and the passed errors are non-{@code null} (but could be
     * empty).
     */
    protected void _invalidate(List<? extends IErrorMessage> messages) {
        buildInto (errorMessageEl, msg -> {
            if (config ().errorRenderer != null) {
                // Use any custom renderer.
                config ().errorRenderer.accept (msg, messages);
            } else {
                // Use the default renderer.
                Div.$ (msg).style (styles ().errors_inner ()).$ (inner -> {
                    Div.$ (inner).style (styles ().errors_inner ()).$ (body -> {
                        Em.$ (body).style (FontAwesome.bell ());
                        P.$ (body).text (config ().errorMessage);
                    });
                    if ((messages != null) && !messages.isEmpty()) {
                        Ul.$ (inner).$ (list -> {
                            messages.forEach (message -> {
                                Li.$ (list).text (message.getMessage ());
                            });
                        });
                    }
                });
            }
        });
        JQuery.$ (errorMessageEl).show ();

        // Scroll to the top of the form.
        TimerSupport.defer(() -> {
            scrollTop();
        });
    }

    @Override
    public void clearInvalid() {
        if (isRendered ())
            _clearInvalid ();
    }

    /**
     * Called by {@link #clearInvalid()} on the assumption that the component has
     * been rendered.
     * <p>
     * We also clear the invalid state on the controls.
     */
    protected void _clearInvalid() {
        JQuery.$ (errorMessageEl).hide ();
        controls().clear();
    }

    /************************************************************************
     * Behaviour as {@link IEditable} (and related functionality).
     ************************************************************************/

    /**
     * Indicates if the form is loading (see {@link #source(IResolver)}).
     */
    private boolean loading;

    /**
     * Source from {@link #source(Object)}.
     */
    private SRC source;

    /**
     * See {@link #updating()}.
     */
    private boolean updating;

    /**
     * Indicates that the controls are being updated with data.
     * 
     * @return {@code true} if they are.
     */
    protected boolean updating() {
        return updating;
    }

    /**
     * Mark the form as loading (or finished loading).
     * 
     * @param loading
     *                the loading state.
     */
    protected void _loading(boolean loading) {
        if (this.loading == loading)
            return;
        if (isRendered ()) {
            _clearInvalid ();
            TimerSupport.defer(() -> {
                scrollTop();
            });
        }
        this.loading = loading;
        controls ().waiting (loading);
    }

    @Override
    public void editLoading() {
        _loading (true);
    }

    @Override
    public void editFailed(String message) {
        _loading (false);
        invalidate (Arrays.asList (new ErrorMessage (message)));
    }

    /**
     * Clears the form controls and form from what is being edited.
     * <p>
     * This will set the source to {@code null} and clear each of the controls to
     * empty. This is used in cases where the form is dual purpose (edit and
     * create).
     */
    public void clear() {
        this.source = null;
        if (!isRendered ())
            return;
        form.clear();
    }
    
    /**
     * Gets values from the passed source to populate out the controls in the form.
     * <p>
     * Only controls that have declared a getter (see
     * {@link IControlCell#get(com.effacy.jui.ui.client.control.builder.IGroupBuilder.IGetter)})
     * will be processed.
     * <p>
     * After all controls have been updated the form will be {@link #reset()}. This
     * will ensure that focus is properly applied.
     * 
     * @param source
     *               the source to obtain data from.
     * @return the passed source.
     */
    public void edit(SRC source) {
        this.source = source;
        if (!isRendered ())
            return;
        _loading (false);
        updating = true;
        try {
            onEdit (source);
            form.edit (source);
            reset ();
        } finally {
            updating = false;
        }
    }

    @Override
    public ControlForm<SRC,DST> onEdit(Consumer<SRC> applier) {
        form.onEdit (applier);
        return this;
    }

    /**
     * Invoked when source is being applied (prior to applying the source to the
     * form).
     * 
     * @param source
     *               the source.
     */
    protected void onEdit(SRC source) {
        // Nothing.
    }

    /**
     * The value passed to {@link #edit(Object)}.
     * 
     * @return the value.
     */
    public SRC source() {
        return source;
    }

    /**
     * Applies values on the form to the the passed destination.
     * <p>
     * Only controls that have declared a setter (see
     * {@link IControlCell#set(com.effacy.jui.ui.client.control.builder.IGroupBuilder.ISetter)})
     * will be processed.
     * 
     * @param source
     *               the source to obtain data from.
     * @return the passed source.
     */
    @Override
    public DST retrieve(DST destination) {
        form.retrieve (destination);
        return destination;
    }

    /************************************************************************
     * Rendering.
     ************************************************************************/

    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$ (el).$ (root -> {
            if (config ().padding != null)
                root.css (CSS.PADDING, config ().padding);
            if (config ().maxWidth != null)
                root.css (CSS.MAX_WIDTH, config ().maxWidth);
            Div.$ (root).style (styles ().errors ()).by ("errors");
            form.insertInto (root);
        }).build (dom -> {
            errorMessageEl = dom.first ("errors");
            JQuery.$ (errorMessageEl).hide ();
        });
    }

    @Override
    protected void onAfterRender() {
        form.activate (0);
        if (source != null)
            edit (source);
        if (this.preRenderErrorMessages != null)
            _invalidate (this.preRenderErrorMessages);
        super.onAfterRender();

        // Check the loading state. If loading then we set the controls into the waiting
        // state. If not then we perform a reset (which sets focus among other things).
        if (loading) {
            controls ().waiting (true);
        } else {
            TimerSupport.defer(() -> {
                reset();
            });
        }
    }
    
    @Override
    protected ILocalCSS styles() {
        return config ().style.styles();
    }

    /************************************************************************
     * Styles (and CSS).
     ************************************************************************/

    public interface ILocalCSS extends IGroupBuilderCSS {

        public String errors();

        public String errors_inner();

    }

    /**
     * Standard CSS.
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        "com/effacy/jui/ui/client/control/builder/ControlForm_Standard.css",
        "com/effacy/jui/ui/client/control/builder/ControlForm_Standard_Override.css"
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


    /**
     * Compact (for dialogs) CSS.
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        "com/effacy/jui/ui/client/control/builder/ControlForm_Standard.css",
        "com/effacy/jui/ui/client/control/builder/ControlForm_Standard_Override.css",
        "com/effacy/jui/ui/client/control/builder/ControlForm_Compact.css"
    })
    public static abstract class CompactLocalCSS implements ILocalCSS {

        private static CompactLocalCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) { 
                STYLES = (CompactLocalCSS) GWT.create (CompactLocalCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }
    
}
