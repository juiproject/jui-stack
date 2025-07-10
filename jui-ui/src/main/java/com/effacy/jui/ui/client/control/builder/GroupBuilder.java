/*******************************************************************************Add commentMore actions
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
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import com.effacy.jui.core.client.component.IComponent;
import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.IEnableDisableListener;
import com.effacy.jui.core.client.component.IShowHideListener;
import com.effacy.jui.core.client.control.IControl;
import com.effacy.jui.core.client.control.IControl.Value;
import com.effacy.jui.core.client.control.IInvalidListener;
import com.effacy.jui.core.client.dom.DomSupport;
import com.effacy.jui.core.client.dom.builder.ContainerBuilder;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.H3;
import com.effacy.jui.core.client.dom.builder.IDomInsertable;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.core.client.dom.builder.Input;
import com.effacy.jui.core.client.dom.builder.Label;
import com.effacy.jui.core.client.dom.builder.Li;
import com.effacy.jui.core.client.dom.builder.Markup;
import com.effacy.jui.core.client.dom.builder.P;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.builder.Text;
import com.effacy.jui.core.client.dom.builder.Ul;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.dom.jquery.JQuery;
import com.effacy.jui.core.client.util.UID;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.platform.util.client.TimerSupport;
import com.effacy.jui.ui.client.control.builder.IGroupBuilder.IRowBuilder.IControlCell;

import elemental2.dom.Element;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;

/**
 * Concrete implementation of {@link IGroupBuilder}.
 * <p>
 * Note that this implements {@link IDomInsertable} (as well as
 * {@link IDomInsertableContainer}) which means it can partake in any standard
 * DOM building arrangement.
 */
public class GroupBuilder<SRC,DST> implements IGroupBuilder<SRC,DST> {

    /**
     * CSS styles expected from the builder.
     */
    public interface IGroupBuilderCSS extends IComponentCSS {

        public String group();

        public String conditional();

        public String compact();

        public String collapse();

        public String separator();

        public String horizontal();
        
        public String depth0(); 
        
        public String depth1();
        
        public String depth2();
        
        public String depth3();
        
        public String depth4();

        public String header();

        public String body();

        public String row();

        public String cell();

        public String required();

        public String help();

        public String nolabel();

        public String error();

        public String guidance();

        public String guidance_top();

        public String hidden();

        public String footer();

    }

    /**
     * Configuration for the group builder.
     */
    public interface IGroupBuilderConfig {

        /**
         * The CSS styles to use.
         */
        public IGroupBuilderCSS styles();

        /**
         * The icon CSS to use for a question.
         * 
         * @return the icon.
         */
        public String questionIcon();

        /**
         * Setters normally only apply when a control is dirty, this will force them to
         * apply to all controls.
         * 
         * @return {@code true} to apply even when not dirty.
         */
        public boolean setterApplyWhenNotDirty();

        /**
         * The (initial) depth.
         * 
         * @return the depth.
         */
        public int depth();
    }

    /**
     * The depth of the group in the hierarchy of groups. Set on the constructor.
     */
    private int depth;

    /**
     * See {@link #by(String)}.
     */
    private String reference;

    /**
     * See {@link #indent(Length)}.
     */
    private Length indent;

    /**
     * See {@link #gap(Length)}.
     */
    private Length gap;

    /**
     * See {@link #adorn}.
     */
    private BiConsumer<ElementBuilder,ElementBuilder> adorn;

    /**
     * See {@link #conditional(Consumer)}.
     */
    private GroupConditionalBuilder conditional;

    /**
     * See {@link #header(Consumer)}.
     */
    private HeaderBuilder header;

    /**
     * See {@link #footer(Consumer)}.
     */
    private FooterBuilder footer;

    /**
     * See {@link #separator()};
     */
    private boolean separator;

    /**
     * See {@link #horizontal()};
     */
    private boolean horizontal;

    /**
    * Internal state (when open).
    */
    private boolean _open = true;

    /**
     * Internal state (when hidden).
     */
    private boolean _hidden = false;
    
    /**
     * Collection of items contained in the group.
     */
    private List<IDomInsertable> items = new ArrayList<> ();

    /**
     * The root element for the group.
     */
    private Element groupEl;

    /**
     * Configuration for the group (and nested groups).
     */
    private IGroupBuilderConfig config;

    /**
     * The parent group (where nested).
     */
    private GroupBuilder<SRC,DST> parent;

    /**
     * The modification context.
     */
    private IModificationContext modificationContext;

    /**
     * Construct with configuration.
     * 
     * @param config
     *               the configuration.
     */
    public GroupBuilder(IGroupBuilderConfig config) {
        this (config, null);
    }

    /**
     * Construct with configuration and an externally provided modification context.
     * 
     * @param config
     *                            the configuration.
     * @param modificationContext
     *                            the modification context.
     */
    public GroupBuilder(IGroupBuilderConfig config, IModificationContext modificationContext) {
        this.depth = config.depth ();
        this.config = config;
        this.modificationContext = modificationContext;
        if (this.modificationContext == null)
            this.modificationContext = new ModificationContext ();
    }

    /**
     * Construct a nested group in the parent.
     * 
     * @param parent
     *               the parent group.
     */
    private GroupBuilder(GroupBuilder<SRC,DST> parent) {
        this.depth = parent.depth + 1;
        this.config = parent.config;
        this.parent = parent;
        this.modificationContext = parent.modificationContext;
    }

    @Override
    public IModificationContext modificationContext() {
        return modificationContext;
    }

    /**
     * Obtains the root level builder (ascend through the parents).
     */
    protected GroupBuilder<SRC,DST> root() {
        if (parent == null)
            return this;
        return parent.root();
    }

    /**
     * Brings the first control into focus.
     */
    public void focusFirst() {
        forEachCell (cell -> {
            if ((cell.control () != null) && !cell.control ().isDisabled () && !cell.control ().isHidden () && !cell.control ().isReadOnly () && !cell.control ().isSuspended ()) {
                TimerSupport.timer (() -> cell.control ().focus (), 100);
                return false;
            }
            return true;
        });
    }

    /**
     * Determines if the group is open and showing.
     * 
     * @return {@code true} if so.
     */
    public boolean isOpen() {
        return _open;
    }

    public String getConditionalDescriminator() {
        if (conditional == null)
            return null;
        return conditional.groupDescriminator;
    }

    public String getConditionalGroup() {
        if (conditional == null)
            return null;
        return conditional.group;
    }

    public GroupConditionalBuilder getConditional() {
        return conditional;
    }

    /**
     * Retrieves the group reference (as set by {@link #by(String)}).
     * 
     * @return the group reference.
     */
    public String getReference() {
        return reference;
    }

    @Override
    public IGroupBuilder<SRC,DST> adorn(BiConsumer<ElementBuilder,ElementBuilder> adorn) {
        this.adorn = adorn;
        return this;
    }

    @Override
    public IGroupBuilder<SRC,DST> by(String reference) {
        this.reference = reference;
        return this;
    }
    
    @Override
    public IGroupBuilder<SRC,DST> indent(Length offset) {
        this.indent = offset;
        return this;
    }

    @Override
    public IGroupBuilder<SRC,DST> gap(Length offset) {
        this.gap = offset;
        return this;
    }

    /**
     * Convenience to log (debug) the state of controls.
     */
    public void logControlState() {
        forEachCell(cell -> {
            if (cell.control() != null) {
                if (cell.control().invalidator().isInvalid()) {
                    Logger.warn("[invalid] " + cell.control().getClass().getSimpleName() + " [uid=" + cell.control().getName() + "] [label=" + cell.toString() + "]");
                } else {
                    Logger.info(cell.control().getClass().getSimpleName() + " [uid=" + cell.control().getName() + "] [label=" + cell.toString() + "]");
                }
            }
            return true;
        });
    }

    void forEachGroup(Function<GroupBuilder<SRC,DST>, Boolean> visitor) {
        _forEachGroup (visitor);
    }

    /**
     * Called by {@link #forEachGroup(Function)} to implemention the conditional side
     * of the visit (in a recursive context).
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private boolean _forEachGroup(Function<GroupBuilder<SRC,DST>, Boolean> visitor) {
        if (!visitor.apply (this))
            return false;
        for (IDomInsertable item : items) {
            if (item instanceof GroupBuilder) {
                if (!((GroupBuilder) item)._forEachGroup (visitor))
                    return false;
            }
        }
        return true;
    }

    @SuppressWarnings("rawtypes")
    void forEachCell(Function<IControlCell, Boolean> visitor) {
        _forEachCell (visitor);
    }

    /**
     * Called by {@link #forEachCell(Function)} to implemention the conditional side
     * of the visit (in a recursive context).
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private boolean _forEachCell(Function<IControlCell, Boolean> visitor) {
        for (IDomInsertable item : items) {
            if (item instanceof GroupBuilder) {
                if (!((GroupBuilder) item)._forEachCell (visitor))
                    return false;
            } else if (item instanceof GroupBuilder.RowBuilder) {
                if (!((GroupBuilder.RowBuilder) item).forEachCell (visitor))
                    return false;
            }
        }
        return true;
    }

    @Override
    public IGroupBuilder<SRC,DST> separator() {
        this.separator = true;
        return this;
    }

    @Override
    public IGroupBuilder<SRC,DST> horizontal() {
        this.horizontal = true;
        return this;
    }

    @Override
    public IGroupBuilder<SRC,DST> conditional(Consumer<IGroupConditionalBuilder> conditional) {
        if (this.conditional == null)
            this.conditional = new GroupConditionalBuilder ();
        if (conditional != null)
            conditional.accept (this.conditional);
        return this;
    }

    @Override
    public IGroupBuilder<SRC,DST> header(Consumer<IHeaderBuilder> header) {
        if (this.header == null)
            this.header = new HeaderBuilder ();
        if (header != null)
            header.accept (this.header);
        return this;
    }

    @Override
    public IGroupBuilder<SRC,DST> footer(Consumer<IFooterBuilder> header) {
        if (this.footer == null)
            this.footer = new FooterBuilder ();
        if (header != null)
            header.accept (this.footer);
        return this;
    }

    @Override
    public IGroupBuilder<SRC,DST> insert(IDomInsertable... insertable) {
        for (IDomInsertable item : insertable) {
            if (item != null)
                items.add (item);
        }
        return this;
    }

    @Override
    public IGroupBuilder<SRC,DST> group(Consumer<IGroupBuilder<SRC,DST>> group) {
        GroupBuilder<SRC,DST> contributor = new GroupBuilder<>(this);
        items.add (contributor);
        if (group != null)
            group.accept (contributor);
        return this;
    }

    @Override
    public IGroupBuilder<SRC,DST> row(Consumer<IRowBuilder<SRC,DST>> row) {
        RowBuilder contributor = new RowBuilder();
        items.add (contributor);
        if (row != null)
            row.accept (contributor);
        return this;
    }

    @Override
    public <V,CTL extends IControl<V>> IGroupBuilder<SRC,DST> control(String by, String label, CTL ctl, Consumer<IRowBuilder.IControlCell<V,CTL,SRC,DST>> handler) {
        row (row -> row.control (by, label, ctl, handler));
        return this;
    }

    @Override
    public IGroupBuilder<SRC,DST> component(IComponent cpt, Consumer<IRowBuilder.ICell> handler) {
        row (row -> row.component (cpt, handler));
        return this;
    }

    /**
     * Implementation of {@link IHeaderBuilder} to declare build out the header.
     * <p>
     * This implements {@link IDomInsertable} so takes owneship for its own
     * rendering.
     */
    public class HeaderBuilder implements IDomInsertable, IHeaderBuilder {

        /**
         * See {@link #title(String, String)}.
         */
        protected String title;

        /**
         * See {@link #title(String, String)}.
         */
        protected String titleCss;

        /**
         * See {@link #icon(String)}.
         */
        protected String icon;

        /**
         * See {@link #instruction(String,String)}.
         */
        protected String instruction;

        /**
         * See {@link #instruction(String,String)}.
         */
        protected String instructionCss;

        /**
         * See {@link #renderer(Consumer)}.
         */
        protected Consumer<ElementBuilder> renderer;

        @Override
        public IHeaderBuilder title(String title, String css) {
            this.title = title;
            this.titleCss = css;
            return this;
        }

        @Override
        public IHeaderBuilder icon(String icon) {
            this.icon = icon;
            return this;
        }

        @Override
        public IHeaderBuilder instruction(String instruction, String css) {
            this.instruction = instruction;
            this.instructionCss = css;
            return this;
        }

        @Override
        public IHeaderBuilder renderer(Consumer<ElementBuilder> renderer) {
            this.renderer = renderer;
            return this;
        }

        @Override
        public void insertInto(ContainerBuilder<?> parent) {
            if (renderer != null) {
                Div.$ (parent).style (config.styles ().header ()).$ (header -> {
                    renderer.accept (header);
                });
                return;
            }
            if (StringSupport.empty (icon) && StringSupport.empty (title) && StringSupport.empty (instruction))
                return;
            Div.$ (parent).style (config.styles ().header ()).$ (header -> {
                if (!StringSupport.empty (title)) {
                    H3.$ (header).$ (h3 -> {
                        if (titleCss != null)
                            h3.css(titleCss);
                        if (!StringSupport.empty (icon))
                            Em.$ (h3).style (icon);
                        if (!StringSupport.empty (title)) 
                            Text.$ (h3, title);
                    });
                }
                if (!StringSupport.empty (instruction)) {
                    P.$ (header).$ (p -> {
                        if (instructionCss != null)
                            p.css(instructionCss);
                        Text.$(p, instruction);
                    });
                }
            });
        }
    }

    /**
     * Implementation of {@link IFooterBuilder} to declare build out the footer.
     * <p>
     * This implements {@link IDomInsertable} so takes owneship for its own
     * rendering.
     */
    public class FooterBuilder implements IDomInsertable, IFooterBuilder {

        /**
         * See {@link #guidance(String)}.
         */
        protected String guidance;

        /**
         * See {@link #renderer(Consumer)}.
         */
        protected Consumer<ElementBuilder> renderer;

        /**
         * Used to support updates to the giudance.
         */
        protected Element guidanceEl;

        @Override
        public IFooterBuilder guidance(String guidance) {
            this.guidance = guidance;
            if (guidanceEl != null)
                guidanceEl.textContent = guidance;
            return this;
        }

        @Override
        public IFooterBuilder renderer(Consumer<ElementBuilder> renderer) {
            this.renderer = renderer;
            return this;
        }

        @Override
        public void insertInto(ContainerBuilder<?> parent) {
            if (renderer != null) {
                Div.$ (parent).style (config.styles ().footer ()).$ (header -> {
                    renderer.accept (header);
                });
                return;
            }
            if (StringSupport.empty (guidance))
                return;
            Div.$ (parent).style (config.styles ().footer ()).$ (header -> {
                if (!StringSupport.empty (guidance))
                    P.$ (header).$ (
                        Markup.$(guidance)
                    ).use (n -> guidanceEl = (Element) n);
            });
        }
    }
    
    public class GroupConditionalBuilder implements IGroupConditionalBuilder {
        
        private boolean active;

        private boolean compact;

        private ConditionalBehaviour behaviour;

        private String group;

        private String groupDescriminator;

        private BiConsumer<IModificationContext,Boolean> handler;

        public GroupConditionalBuilder active(boolean active) {
            this.active = active;
            return this;
        }

        public GroupConditionalBuilder compact(boolean compact) {
            this.compact = compact;
            return this;
        }
        
        public GroupConditionalBuilder behaviour(ConditionalBehaviour behaviour) {
            if (behaviour != null)
                this.behaviour = behaviour;
            return this;
        }
        
        public GroupConditionalBuilder group(String group, String groupDescriminator) {
            this.group = group;
            this.groupDescriminator = groupDescriminator;
            return this;
        }

        public GroupConditionalBuilder handler(BiConsumer<IModificationContext,Boolean> handler) {
            this.handler = handler;
            return this;
        }
    }

    /**
     * Implementation of {@link IRowBuilder} to declare build out a single row of
     * cells.
     * <p>
     * This implements {@link IDomInsertable} so takes owneship for its own
     * rendering. It will also implement the cells as configured with
     * {@link RowCell} (which does not implement {@link IDomInsertable}).
     */
    public class RowBuilder implements IDomInsertable, IRowBuilder<SRC,DST> {

        /**
         * See {@link #adorn(Consumer)}.
         */
        protected Consumer<ElementBuilder> adorner;

        /**
         * The declared cells.
         */
        protected List<RowCell<?,?>> cells = new ArrayList<>();

        /**
         * The root element.
         */
        private Element el;

        @SuppressWarnings("unchecked")
        boolean forEachCell(Function<RowBuilder.RowCell<?, IControl<?>>, Boolean> visitor) {
            for (RowCell<?,?> cell : cells) {
                if (!visitor.apply ((RowBuilder.RowCell<?, IControl<?>>) cell))
                    return false;
            }
            return true;
        }

        @Override
        public void insertInto(ContainerBuilder<?> parent) {
            // Generate the DOM for a row of cells.
            if (!cells.isEmpty ()) {
                Div.$ (parent).style (config.styles ().row ()).$ (row -> {
                    if (adorner != null)
                        adorner.accept(row);
                    row.use(n -> el = (Element) n);
                    // Create a cell for each of the items in the row.
                    cells.forEach (cell -> {
                        Div.$ (row).style (config.styles ().cell ()).$ (c -> {
                            // Grap the element. This can be used by the cell handler.
                            c.use (n -> {
                                cell.el = (Element) n;
                            });
                            if (cell.control != null) {
                                // Attach event handlers that change the cell (and row) state.
                                c.use (n -> {
                                    // Show / hide listener. This will hide the cell when the control is hidden
                                    // as well as the row if all cells are hidden.
                                    cell.control.addListener (IShowHideListener.create((cpt,show) -> {
                                        DomSupport.addRemoveClass ((Element) n, config.styles ().hidden (), !show);
                                        Element r = n.parentElement;
                                        boolean showing = false;
                                        for (Element cld : r.getElementsByClassName(config.styles().cell()).asList()) {
                                            if (!cld.classList.contains(config.styles().hidden()))
                                                showing = true;
                                        }
                                        DomSupport.addRemoveClass (r, config.styles ().hidden (), !showing);
                                    }));
                                    // Enable / disable listener. This will disable the cell when the controls is
                                    // disabled. 
                                    cell.control.addListener (IEnableDisableListener.create ((cpt, enable) -> {
                                        DomSupport.addRemoveClass ((Element) n, config.styles ().disabled (), !enable);
                                    }));
                                    // Inavlidation listener to present error messages.
                                    cell.control.addListener(IInvalidListener.create ((ctl, list) -> {
                                        DomSupport.addRemoveClass ((Element) n, config.styles ().error (), true);
                                        Element errorEl = JQuery.$ (n).find ("UL." + config.styles().error()).get(0);
                                        Wrap.$(errorEl).$ (errors -> {
                                            list.forEach (error -> Li.$ (errors).text (error));
                                        }).build ();
                                    }, ctl -> {
                                        DomSupport.addRemoveClass ((Element) n, config.styles ().error (), false);
                                        JQuery.$ (n).find ("ul." + config.styles().error()).find ("li").remove ();
                                    }));
                                    // Initial cell state.
                                    if (cell.disabled)
                                        ((HTMLElement) n).classList.add (config.styles ().disabled ());
                                    if (cell.hidden)
                                        ((HTMLElement) n).classList.add (config.styles ().hidden ());
                                });
                            }
                            if (cell.grow > 0)
                                c.css ("flex-grow", "" + cell.grow);
                            if (cell.rightAlign)
                                c.css ("align-items", "end");
                            // The label (if any).
                            Label.$ (c).$ (l -> {
                                if (!StringSupport.empty(cell.label))
                                    Span.$(l)
                                        .text(cell.label)
                                        .use(n -> cell.elLabel = (Element) n);
                                if (cell.required)
                                    l.style (config.styles ().required ());
                                if (!StringSupport.empty (cell.help)) {
                                    Div.$ (l).style (config.styles ().help ()).$ (help -> {
                                        Em.$ (help).style (config.questionIcon ());
                                        Div.$ (help).text (cell.help);
                                    });
                                }
                            });
                            // Disply any assigned guidance (top).
                            if (!StringSupport.empty(cell.guidance) && cell.guidanceTop) {
                                Div.$ (c).style (config.styles ().guidance_top ()).css(cell.guidanceCss).$ (
                                    Markup.$(cell.guidance)
                                );
                            }
                            // Container for the contents (i.e. component / control).
                            Div.$ (c).$ (inner -> {
                                if (cell.offsetv != 0)
                                    inner.css (CSS.BOTTOM, Length.px (cell.offsetv));
                                if (cell.offseth != 0)
                                    inner.css (CSS.LEFT, Length.px (cell.offseth));
                                if (cell.builder != null) {
                                    cell.builder.accept (inner);
                                } else if (cell.component != null) {
                                    inner.insert (cell.component);
                                } else if (cell.control != null) {
                                    if (reference != null)
                                        cell.control.putMetaAttribute ("group", reference);
                                    GroupBuilder<SRC,DST> root = root();
                                    if (root.reference != null)
                                        cell.control.putMetaAttribute ("group_root", root.reference);
                                    inner.insert (cell.control);
                                }
                            });
                            // Placeholder for error messages.
                            Ul.$ (c).style (config.styles ().error (), null).$ (error -> {
                                if (!StringSupport.empty(cell.errorCss))
                                    error.css(cell.errorCss);
                            });
                            // Disply any assigned guidance (bottom).
                            if (!StringSupport.empty(cell.guidance) && !cell.guidanceTop) {
                                Div.$ (c).style (config.styles ().guidance ()).css(cell.guidanceCss).$ (
                                    Markup.$(cell.guidance)
                                );
                            }
                        });
                    });
                    // Hide the label elements if there are no labels.
                    if (!cells.stream ().anyMatch (cell -> !StringSupport.empty (cell.label)))
                        row.style (config.styles ().nolabel ());
                    // Hide the row if there are no cells showing.
                    if (!cells.stream().anyMatch(cell -> !cell.hidden))
                        row.style (config.styles ().hidden ());
                });
            }
        }

        @Override
        public IRowHandler handler() {
            return new IRowHandler() {

                @Override
                public void show() {
                    if (el != null)
                        JQuery.$(el).show();
                }

                @Override
                public void hide() {
                    if (el != null)
                        JQuery.$(el).hide();
                }

            };
        }
        
        @Override
        public IRowBuilder<SRC,DST> adorn(Consumer<ElementBuilder> adorner) {
            this.adorner = adorner;
            return this;
        }

        @Override
        public <V,CTL extends IControl<V>> IRowBuilder<SRC,DST> control(String by, String label, CTL ctl, Consumer<IControlCell<V,CTL,SRC,DST>> handler) {
            RowCell<V,CTL> cell = new RowCell<>(label, ctl);
            if (handler != null)
                handler.accept (cell);
            if (!StringSupport.empty (by))
                cell.by (by);
            cells.add (cell);
            return this;
        }

        @Override
        public IRowBuilder<SRC,DST> insert(Consumer<ElementBuilder> builder, Consumer<ICell> handler) {
            if (builder == null)
                return this;
            RowCell<Void,IControl<Void>> cell = new RowCell<>(builder);
            if (handler != null)
                handler.accept (cell);
            cells.add (cell);
            return this;
        }

        @Override
        public RowBuilder component(IComponent cpt, Consumer<ICell> handler) {
            if (cpt == null)
                return this;
            RowCell<Void,IControl<Void>> cell = new RowCell<>(cpt);
            if (handler != null)
                handler.accept (cell);
            cells.add (cell);
            return this;
        }

        @Override
        public IRowBuilder<SRC,DST> expander() {
            cells.add (new RowCell<>());
            return this;
        }

        /**
         * Implementation of {@link IControlCell} and {@link ICell} as managed by a row.
         */
        class RowCell<V,CTL extends IControl<V>> implements IControlCell<V,CTL,SRC,DST>, ICell {

            protected String reference;

            protected boolean required;

            protected String help;

            protected IClearer<V> clearer;

            protected IGetter<V, SRC> getter;

            protected ISetter<V, DST> setter;

            protected boolean setterApplyWhenNotDirty = config.setterApplyWhenNotDirty ();

            protected IModification<V,CTL> modification;

            protected boolean disabled;

            protected boolean hidden;

            protected int grow = 0;

            protected boolean rightAlign = false;

            protected int offsetv = 0;

            protected int offseth = 0;

            protected String label;

            protected String guidance;

            protected String guidanceCss;

            protected boolean guidanceTop;

            protected String errorCss;

            protected Consumer<ElementBuilder> builder;

            protected IComponent component;

            protected CTL control;

            protected Element el;

            protected Element elLabel;

            RowCell() {
                // Expander.
                grow = 1;
            }

            RowCell(Consumer<ElementBuilder> builder) {
                this.builder = builder;
            }

            RowCell(IComponent component) {
                this.component = component;
            }

            RowCell(String label, CTL control) {
                this.label = label;
                this.control = control;
            }

            public ICellHandler handler() {
                return new ICellHandler() {

                    @Override
                    public void show() {
                        if (el != null)
                            JQuery.$(el).show();
                    }

                    @Override
                    public void hide() {
                        if (el != null)
                            JQuery.$(el).hide();
                    }

                    @Override
                    public void updateLabel(String label) {
                        if (elLabel != null)
                            elLabel.textContent = label;
                    }

                };
            }

            @Override
            public CTL control() {
                return control;
            }

            @Override
            public IComponent component() {
                if (control != null)
                    return control;
                return component;
            }

            @Override
            public RowCell<V,CTL> by(String reference) {
                this.reference = reference;
                return this;
            }

            @Override
            public RowCell<V,CTL> required() {
                this.required = true;
                return this;
            }

            @Override
            public RowCell<V,CTL> help(String help) {
                this.help = help;
                return this;
            }

            @Override
            public IControlCell<V,CTL,SRC,DST> clear(IClearer<V> clearer) {
                this.clearer = clearer;
                return this;
            }

            @Override
            public RowCell<V,CTL> edit(IGetter<V, SRC> getter) {
                this.getter = getter;
                return this;
            }

            @Override
            public RowCell<V,CTL> retrieve(ISetter<V, DST> setter) {
                this.setter = setter;
                this.setterApplyWhenNotDirty = config.setterApplyWhenNotDirty ();
                return this;
            }

            @Override
            public RowCell<V,CTL> retrieve(ISetter<V, DST> setter, boolean applyWhenNotDirty) {
                this.setter = setter;
                this.setterApplyWhenNotDirty = applyWhenNotDirty;
                return this;
            }

            @Override
            public RowCell<V,CTL> modify(IModification<V,CTL> handler) {
                this.modification = handler;
                return this;
            }

            @Override
            public RowCell<V,CTL> disable() {
                this.disabled = true;
                return this;
            }

            @Override
            public RowCell<V,CTL> hide() {
                this.hidden = true;
                return this;
            }

            @Override
            public RowCell<V,CTL> grow(int share) {
                this.grow = share;
                return this;
            }

            @Override
            public RowCell<V,CTL> rightAlign(boolean align) {
                this.rightAlign = align;
                return this;
            }

            @Override
            public RowCell<V,CTL> offset(int vertical, int horizontal) {
                this.offsetv = vertical;
                this.offseth = horizontal;
                return this;
            }

            @Override
            public RowCell<V,CTL> guidance(String guidance) {
                return guidance(guidance, false, null);
            }

            @Override
            public RowCell<V,CTL> guidance(String guidance, boolean top) {
                return guidance(guidance, top, null);
            }

            @Override
            public RowCell<V,CTL> guidance(String guidance, String css) {
                return guidance(guidance, false, css);
            }

            @Override
            public RowCell<V,CTL> guidance(String guidance, boolean top, String css) {
                this.guidance = guidance;
                this.guidanceCss = css;
                this.guidanceTop = top;
                return this;
            }

            @Override
            public IControlCell<V,CTL,SRC,DST> errorCss(String css) {
                this.errorCss = css;
                return this;
            }

            /**
             * Cleats the control in the cell.
             */
            void _clear() {
                if (control != null) {
                    if (clearer != null)
                        clearer.clear(control);
                    else
                        control.setValue (Value.of (null));
                }
            }

            /**
             * Obtains a value from the source to pass through to the control to edit.
             * <p>
             * The value assignment is forced as many forms are re-used. This way we have
             * consistent behaviour on edit regardless of the state of the value of the
             * control.
             */
            void _edit(SRC source) {
                if ((control != null) && (getter != null))
                    control.setValue (Value.of (getter.get (control, source)).force());
            }

            /**
             * Retreive a value from the underlying control and assigns that value to the
             * destination.
             */
            void _retrieve(DST destination) {
                if ((this.control == null) || (setter == null))
                    return;
                if (this.control.isHidden() || this.control.isReadOnly() || this.control.isDisabled ())
                    return;
                if (!setterApplyWhenNotDirty && !this.control.dirty())
                    return;
                setter.set (new ISetter.ISetterContext () {

                    @Override
                    public boolean dirty() {
                        return control.dirty ();
                    }

                    @Override
                    public <W> IControl<W> control(String reference) {
                        return modificationContext.control (reference);
                    }

                }, this.control.value (), destination);
            }
            
            @Override
            public String toString() {
                return label;
            }
        }
        
    }


    /************************************************************************
     * Rendering
     ************************************************************************/

     /**
      * For conditional groups this is the input selector for the group. Used for
      * programmatic activation.
      */
    private HTMLInputElement inputEl;

    @Override
    public void insertInto(ContainerBuilder<?> parent) {

        // Populate the modification context with groups and controls from the cells.
        forEachGroup (grp -> {
            ((ModificationContext) modificationContext).process (grp);
            return true;
        });
        forEachCell (cell -> {
            ((ModificationContext) modificationContext).process (cell);
            return true;
        });

        // Build out the group.
        Div.$ (parent).$ (group -> {
            if (indent != null)
                group.css (CSS.MARGIN_LEFT, indent);
            if (gap != null)
                group.css (CSS.MARGIN_TOP, gap);
            if (horizontal)
                group.style (config.styles().horizontal ());
            group.style (config.styles().group ());
            group.apply (n -> groupEl = (Element) n);
            if (separator)
                group.style (config.styles ().separator ());
            if (depth == 0)
                group.style (config.styles().depth0 ());
            else if (depth == 1)
                group.style (config.styles().depth1 ());
            else if (depth == 2)
                group.style (config.styles().depth2 ());
            else if (depth == 3)
                group.style (config.styles().depth3 ());
            else
                group.style (config.styles().depth4 ());
            if (conditional != null) {
                group.style (config.styles ().conditional ());
                if (conditional.compact)
                    group.style (config.styles ().compact ());
                if (!conditional.active) {
                    group.style (config.styles ().collapse ());
                    _close ();
                }
                String icon = (header == null) ? null : header.icon;
                String title = ((header == null) || StringSupport.empty(header.title)) ? "NO TITLE PROVIDED FOR THIS GROUP" : header.title;
                String instruction = (header == null) ? null : header.instruction;
                boolean active = conditional.active;
                boolean grouped = !StringSupport.empty (conditional.group);
                String groupedName = conditional.group;
                String groupedValue = conditional.groupDescriminator;
                Div.$ (group).style (config.styles ().header ()).$ (hdr -> {
                    Div.$ (hdr).$ (bar -> {
                        String uid = UID.createUID ();
                        Div.$ (bar).$ (
                            Input.$ (grouped ? "radio" : "checkbox").$ (input -> {
                                input.id (uid);
                                input.checked (active);
                                input.use(n -> inputEl = (HTMLInputElement) n);
                                if (grouped) {
                                    input.name (groupedName);
                                    input.value (groupedValue);
                                }
                                input.onchange ((e,n) -> {
                                    if (((HTMLInputElement) n).checked)  {
                                        _open ();
                                    } else {
                                        _close ();
                                    }
                                });
                            })
                        );
                        Label.$ (bar).$ (label -> {
                            label.setAttribute ("for", uid);
                            if (!StringSupport.empty (icon))
                                Em.$ (label).style (icon);
                            Text.$ (label, title);
                        });
                    });
                    if (!StringSupport.empty (instruction)) {
                        P.$ (hdr).$ (
                            Markup.$(instruction)
                        );
                    }
                });
            } else if (header != null)
                group.insert (header);
            if (!items.isEmpty()) {
                Div.$ (group).style(config.styles ().body()).$ (body -> {
                    if (adorn != null)
                        adorn.accept (group, body);
                    items.forEach (item -> item.insertInto (body));
                });
            }
            if (footer != null)
                group.insert (footer);
        });
    }

    /**
     * External activation of a group.
     */
    void _activate() {
        if (inputEl != null)
            inputEl.click ();
    }

    void _enable() {
        forEachCell (cell -> {
            if (cell.component () != null)
                cell.component ().enable ();
            return true;
        });
    }

    void _disable() {
        forEachCell (cell -> {
            if (cell.component () != null)
                cell.component ().disable ();
            return true;
        });
    }

    /**
     * Simply shows the group.
     */
    void _show() {
        if (!_hidden)
            return;
        _hidden = false;
        if (groupEl != null)
            JQuery.$ (groupEl).show ();
        forEachCell (cell -> {
            if (cell.control () != null)
                cell.control ().suspend (false);
            return true;
        });
    }

    void _hide() {
        if (_hidden)
            return;
        _hidden = true;
        if (groupEl != null)
            JQuery.$ (groupEl).hide ();
        forEachCell (cell -> {
            if (cell.control () != null)
                cell.control ().suspend (true);
            return true;
        });
    }

    void _open() {
        if (_open)
            return;
        _open = true;
        if ((conditional != null) && !StringSupport.empty (conditional.group)) {
            // Find all the other groups of the same name and close them.
            root ().forEachGroup ((grp) -> {
                if ((this != grp) && (grp.conditional != null) && conditional.group.equals (grp.conditional.group))
                    grp._close ();
                return true;
            });
        }
        if (groupEl != null)
            groupEl.classList.remove (config.styles ().collapse ());
        forEachCell (cell -> {
            if (cell.control () != null)
                cell.control ().suspend (false);
            return true;
        });
        if ((conditional != null) && (conditional.handler != null))
            conditional.handler.accept (modificationContext, true);
    }

    void _close() {
        if (!_open)
            return;
        _open = false;
        if (groupEl != null)
            groupEl.classList.add (config.styles ().collapse ());
        forEachCell (cell -> {
            if (cell.control () != null) {
                cell.control ().reset ();
                cell.control ().suspend (true);
            }
            return true;
        });
        if ((conditional != null) && (conditional.handler != null))
            conditional.handler.accept (modificationContext, false);
    }

}