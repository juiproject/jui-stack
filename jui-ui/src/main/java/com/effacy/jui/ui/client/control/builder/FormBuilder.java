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
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import com.effacy.jui.core.client.component.IComponent;
import com.effacy.jui.core.client.control.IControl;
import com.effacy.jui.core.client.control.IControl.Value;
import com.effacy.jui.core.client.dom.builder.ContainerBuilder;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.IDomInsertable;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.ui.client.control.builder.GroupBuilder.IGroupBuilderCSS;
import com.effacy.jui.ui.client.control.builder.GroupBuilder.IGroupBuilderConfig;
import com.effacy.jui.ui.client.control.builder.GroupBuilder.RowBuilder.RowCell;
import com.effacy.jui.ui.client.control.builder.IGroupBuilder.IRowBuilder.IControlCell;

public class FormBuilder<SRC,DST> implements IModificationContext, IGroupBuilder<SRC,DST>, IFormBuilder<SRC,DST> {

    /**
     * Configuration for the builder.
     */
    public interface IFormBuilderConfig {

        /**
         * See {@link IGroupBuilderConfig#styles()}.
         */
        public IGroupBuilderCSS styles();

        /**
         * See {@link IGroupBuilderConfig#questionIcon()}.
         */
        public String questionIcon();

        /**
         * See {@link IGroupBuilderConfig#setterApplyWhenNotDirty()}.
         */
        public boolean setterApplyWhenNotDirty();

        /**
         * See {@link IGroupBuilderConfig#depth()}.
         * <p>
         * This is the base depth for each page (being a group).
         */
        public int initialDepth();
    }

    /**
     * Configuration.
     */
    private IFormBuilderConfig config;

    /**
     * All the "pages" (these are groups).
     */
    private List<Page> groups = new ArrayList<>();

    /**
     * The current group (for delegation to).
     */
    private Page group;

    /**
     * The modification context.
     */
    private IModificationContext modificationContext = new ModificationContext ();

    /**
     * Invoked when built (see {@link #onBuild(Consumer)}).
     */
    private Consumer<IModificationContext> onBuild;

    /**
     * Invoked when source is applied (see {@link #onEdit(Consumer)}).
     */
    private Consumer<SRC> onSource;

    /**
     * Construct a form with initial configuration.
     * 
     * @param config
     *               the configuration.
     */
    public FormBuilder(IFormBuilderConfig config) {
        this.config = config;
    }

    @Override
    public IModificationContext modificationContext() {
        return modificationContext;
    }

    /************************************************************************
     * Activation.
     ************************************************************************/

    /**
     * The number of pages in the form.
     * 
     * @return the number.
     */
    public int size() {
        return groups.size();
    }

    /**
     * Obtains the group at the given index.
     * 
     * @param idx
     *            the index.
     * @return the associated group.
     */
    public GroupBuilder<SRC,DST> group(int idx) {
        return groups.get (idx);
    }

    @Override
    public GroupBuilder<SRC,DST> activate(int idx) {
        if ((idx < 0) || (idx >= size ()))
            return null;
        for (int i = 0; i < size (); i++) {
            GroupBuilder<SRC,DST> grp = groups.get(i);
            if (i == idx)
                grp._show ();
            else
                grp._hide ();
        }
        return group (idx);
    }

    /************************************************************************
     * Source.
     ************************************************************************/

    @Override
    public FormBuilder<SRC,DST> onEdit(Consumer<SRC> onSource) {
        this.onSource = onSource;
        return this;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void clear() {
        forEachCell (cell -> {
            try {
                ((RowCell) cell)._clear ();
            } catch (Throwable e) {
                Logger.reportUncaughtException (e, FormBuilder.this);
            }
            return true;
        });
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void edit(SRC source) {
        if (onSource != null)
            onSource.accept (source);
        forEachCell (cell -> {
            try {
                ((RowCell) cell)._edit (source);
            } catch (Throwable e) {
                Logger.reportUncaughtException (e, FormBuilder.this);
            }
            return true;
        });
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public DST retrieve(DST destination) {
        forEachCell (cell -> {
            try {
                ((RowCell) cell)._retrieve (destination);
            } catch (Throwable e) {
                Logger.reportUncaughtException (e, FormBuilder.this);
            }
            return true;
        });
        return destination;
    }

    /************************************************************************
     * Create and managing pages.
     ************************************************************************/

    @Override
    public FormBuilder<SRC,DST> onBuild(Consumer<IModificationContext> onBuild) {
        this.onBuild = onBuild;
        return this;
    }

    /**
     * Creates a new page (essentially a new root group). The newly created group
     * will have a default reference applied of the form "pageN" where N is the page
     * index (from 1). This can be changed by providing an alternative reference
     * directly (see {@link IGroupBuilder#by(String)})).
     */
    @Override
    public IPage<SRC,DST> page() {
        return page (config.initialDepth ());
    }

    /**
     * See {@link #page()}.
     */
    @Override
    public IPage<SRC,DST> page(int depth) {
        group = new Page (new IGroupBuilderConfig() {

            @Override
            public IGroupBuilderCSS styles() {
                return config.styles ();
            }

            @Override
            public String questionIcon() {
                return config.questionIcon ();
            }

            @Override
            public boolean setterApplyWhenNotDirty() {
                return config.setterApplyWhenNotDirty ();
            }

            @Override
            public int depth() {
                return Math.min (Math.max (depth, 0), 4);
            }
            
        }, modificationContext);
        groups.add (group);
        group.by ("page" + groups.size ());
        return group;
    }

    /************************************************************************
     * Behaviour as a {@link IGroupBuilder}. All these delegate to the
     * top-level group.
     ************************************************************************/

     /**
     * Obtains the current group, creating one if not present.
     * 
     * @return the current group.
     */
    protected GroupBuilder<SRC,DST> currentGroup() {
        if (group == null)
            page ();
        return group;
    }

    @Override
    public IGroupBuilder<SRC,DST> adorn(BiConsumer<ElementBuilder,ElementBuilder> adorn) {
        return currentGroup ().adorn (adorn);
    }

    @Override
    public IGroupBuilder<SRC,DST> by(String reference) {
        return currentGroup ().by (reference);
    }

    @Override
    public IGroupBuilder<SRC,DST> indent(Length offset) {
        return currentGroup ().indent (offset);
    }
    
    @Override
    public IGroupBuilder<SRC,DST> gap(Length offset) {
        return currentGroup ().gap (offset);
    }

    @Override
    public IGroupBuilder<SRC, DST> separator() {
        return currentGroup ().separator ();
    }

    @Override
    public IGroupBuilder<SRC, DST> horizontal() {
        return currentGroup ().horizontal ();
    }

    @Override
    public IGroupBuilder<SRC, DST> conditional(Consumer<IGroupConditionalBuilder> conditional) {
        return currentGroup ().conditional (conditional);
    }

    @Override
    public IGroupBuilder<SRC, DST> footer(Consumer<IFooterBuilder> header) {
        return currentGroup ().footer (header);
    }

    @Override
    public IGroupBuilder<SRC, DST> group(Consumer<IGroupBuilder<SRC, DST>> group) {
        return currentGroup ().group (group);
    }

    @Override
    public IGroupBuilder<SRC, DST> header(Consumer<IHeaderBuilder> header) {
        return currentGroup ().header (header);
    }

    @Override
    public IGroupBuilder<SRC, DST> insert(IDomInsertable... insertable) {
        return currentGroup ().insert (insertable);
    }

    @Override
    public IGroupBuilder<SRC, DST> row(Consumer<IRowBuilder<SRC, DST>> row) {
        return currentGroup ().row (row);
    }

    @Override
    public IGroupBuilder<SRC, DST> component(IComponent cpt, Consumer<IRowBuilder.ICell> handler) {
        return currentGroup ().component (cpt, handler);
    }

    @Override
    public <V, CTL extends IControl<V>> IGroupBuilder<SRC, DST> control(String by, String label, CTL ctl, Consumer<IControlCell<V, CTL, SRC, DST>> handler) {
        return currentGroup ().control (by, label, ctl, handler);
    }

    /************************************************************************
     * Behaviour as an {@link IModificationContext}.
     ************************************************************************/

    @Override
    public <W> IControl<W> control(String reference) {
        return modificationContext.control (reference);
    }

    @Override
    public <W> W value(String reference) {
        return modificationContext.value (reference);
    }

    @Override
    public <W> W value(String reference, W defaultValue) {
        return modificationContext.value(reference, defaultValue);
    }

    @Override
    public <W> void set(String reference, W value) {
        modificationContext.set (reference, value);
    }

    @Override
    public <W> void set(String reference, Value<W> value) {
        modificationContext.set (reference, value);
    }

    @Override
    public void disable(String... references) {
        modificationContext.disable (references);
    }

    @Override
    public void enable(String... references) {
        modificationContext.enable (references);
    }

    @Override
    public boolean groupOpen(String reference) {
        return modificationContext.groupOpen (reference);
    }

    @Override
    public void hide(String... references) {
        modificationContext.hide (references);
    }

    @Override
    public void show(String... references) {
        modificationContext.show (references);
    }

    /************************************************************************
     * Rendering.
     ************************************************************************/

    void forEachGroup(Function<GroupBuilder<SRC,DST>, Boolean> visitor) {
        groups.forEach(grp -> {
            grp.forEachGroup (visitor);
        });
    }

    @SuppressWarnings("rawtypes")
    void forEachCell( Function<IControlCell, Boolean> visitor) {
        groups.forEach (grp -> {
            grp.forEachCell (visitor);
        });
    }

    @Override
    public void insertInto(ContainerBuilder<?> parent) {
        if (group == null)
            return;

        // Insert each of the groups into the parent. The form builder needs no
        // adornment around this as the separate groups are intended to be show only one
        // at a time (as if only one group were added).
        if (groups.size () == 1) {
            group.insertInto (parent);
        } else {
            groups.forEach (grp -> {
                grp.insertInto (parent);
            });
        }

        // If there is a on-build handler we just hook into the DOM build lifecycle by
        // adding a post-build execution.
        if (onBuild != null)
            parent.apply (n -> onBuild.accept (modificationContext));
    }

    /************************************************************************
     * Page class.
     ************************************************************************/

    public class Page extends GroupBuilder<SRC,DST> implements IPage<SRC,DST> {

        private Consumer<IPage<SRC, DST>> activationHandler;

        
        public Page(IGroupBuilderConfig config, IModificationContext modificationContext) {
            super(config, modificationContext);
        }

        @Override
        public IPage<SRC, DST> onActivate(Consumer<IPage<SRC, DST>> activationHandler) {
            this.activationHandler = activationHandler;
            return this;
        }

        @Override
        void _show() { 
            super._show();
            if (activationHandler != null)
                activationHandler.accept (this);
        }
        
    }
    
}
