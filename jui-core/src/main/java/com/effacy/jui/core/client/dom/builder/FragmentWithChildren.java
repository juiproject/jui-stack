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
package com.effacy.jui.core.client.dom.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * A type of {@link Fragment} to which children can be inserted (in accordance
 * with {@link IDomInsertableContainer}).
 * 
 * @param <T> the type of fragment (used to type returns).
 */
public class FragmentWithChildren<T extends FragmentWithChildren<T>> extends Fragment<T> implements IDomInsertableContainer<T> {
    
    /**
     * The children to render into the fragment.
     */
    protected List<IDomInsertable> children = new ArrayList<> ();

    /**
     * For building the fragment.
     */
    private BiConsumer<ContainerBuilder<?>, List<IDomInsertable>> builder;

    /**
     * Default constructor.
     */
    protected FragmentWithChildren() {
        super();
    }
    
    /**
     * Construct with a builder.
     * 
     * @param builder
     *                the builder (see {@link #builder(BiConsumer)}).
     */
    protected FragmentWithChildren(BiConsumer<ContainerBuilder<?>, List<IDomInsertable>> builder) {
        builder (builder);
    }

    /**
     * Assigns a builder.
     * <p>
     * The builder will be invoked with the parent into which it will build its
     * contents along with a list of children than must be inserted in the
     * appropriate location (or locations). Note that more than one element can be
     * inserted into the parent a adornments need to be handled manually.
     * 
     * @param builder
     *                the builder.
     */
    protected void builder(BiConsumer<ContainerBuilder<?>, List<IDomInsertable>> builder) {
        this.builder = builder;
    }

    /**
     * See {@link Fragment#build(ContainerBuilder)}.
     */
    @Override
    public void build(ContainerBuilder<?> parent) {
        if (builder != null) {
            builder.accept (parent, children);
        } else {
            ElementBuilder el = createRoot (parent);
            if (use != null)
                el.use (use);
            if (el != null) {
                el.$ (root -> {
                    buildInto (root);
                    adorn (root);
                });
            }
        }
    }

    /**
     * See {@link Fragment#buildInto(ElementBuilder)}.
     */
    @Override
    protected void buildInto(ElementBuilder root) {
        children.forEach (child -> child.insertInto (root));
    }

    /**
     * See {@link IDomInsertableContainer#insert(IDomInsertable...)}.
     */
    @Override
    @SuppressWarnings("unchecked")
    public T insert(IDomInsertable... children) {
        for (IDomInsertable child : children) {
            this.children.add (child);
            onInsertChild (child);
        }
        return (T) this;
    }

    /**
     * Convenience callback when a child is inserted.
     * 
     * @param child
     *              the child.
     */
    protected void onInsertChild (IDomInsertable child) {
        // Nothing.
    }
}
