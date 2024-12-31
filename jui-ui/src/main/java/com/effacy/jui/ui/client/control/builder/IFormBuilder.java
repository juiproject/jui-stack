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

import java.util.function.Consumer;

import com.effacy.jui.core.client.IEditable;
import com.effacy.jui.core.client.dom.builder.IDomInsertable;

public interface IFormBuilder<SRC,DST> extends IDomInsertable {

    public interface IPage<SRC,DST> extends IGroupBuilder<SRC,DST> {

        public IPage<SRC,DST> onActivate(Consumer<IPage<SRC,DST>> handler);
    }

    /**
     * Creates a new page (top-level group).
     * <p>
     * Uses the default initial depth (from configuration).
     * 
     * @return the page.
     */
    public IPage<SRC,DST> page();

    /**
     * Creates a new page (top-level group).
     * 
     * @param depth
     *              the initial depth.
     * @return the page.
     */
    public IPage<SRC,DST> page(int depth);

    /**
     * Activeate the given page.
     * 
     * @param idx
     *            the index of the builder.
     * @return the associate builder for the page.
     */
    public IGroupBuilder<SRC,DST> activate(int idx);

    /**
     * See {@link #page()} but allows one to pass a lambda-expression to build out
     * the page.
     * 
     * @param builder
     *                to build out the page.
     */
    default public IPage<SRC,DST> page(Consumer<IGroupBuilder<SRC,DST>> builder) {
        IPage<SRC,DST>  group = page ();
        if (builder != null)
            builder.accept (group);
        return group;
    }

    /**
     * See {@link #page()} but allows one to pass a lambda-expression to build out
     * the page.
     * 
     * @param depth
     *                the initial depth.
     * @param builder
     *                to build out the page.
     */
    default public IPage<SRC,DST> page(int depth, Consumer<IGroupBuilder<SRC,DST>> builder) {
        IPage<SRC,DST> group = page (depth);
        if (builder != null)
            builder.accept (group);
        return group;
    }

    /**
     * A handler that is invoked once the form elements have been built into the
     * DOM. The modification context will be available and you can then safely
     * perform any
     * modification.
     * 
     * @param onBuild
     *                the handler to invoke (is passed the modification context).
     * @return this builder instance.
     */
    public IFormBuilder<SRC,DST> onBuild(Consumer<IModificationContext> onBuild);

    /**
     * A handler that is invoked when an edit is applied.
     * 
     * @param handler
     *                 the handler to invoke.
     * @return this builder instance.
     */
    public IFormBuilder<SRC,DST> onEdit(Consumer<SRC> handler);

    /**
     * Clears each of the child components.
     * <p>
     * The default behaviour is to set each control to {@code null}, however this
     * can be overridden by supplying a means to clear explicityly (see
     * {@link IRowBuilder#clear(IClearere)}).
     */
    public void clear();

    /**
     * Applies the passed source to the form and all its child controls.
     * <p>
     * This has the same behavior expectations as {@link IEditable#edit(SRC)}.
     * 
     * @param source
     *               the source to apply.
     * @return the passed source.
     */
    public void edit(SRC source);
    
    /**
     * Configure the passed object by passing through to each cell.
     * 
     * @param destination
     *                    the object to confure.
     * @return the passed desitination.
     */
    public DST retrieve(DST destination);
}
