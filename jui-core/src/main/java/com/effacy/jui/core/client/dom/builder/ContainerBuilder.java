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
import java.util.function.Consumer;

import org.gwtproject.safehtml.shared.SafeHtml;

import com.effacy.jui.core.client.dom.renderer.IDataRenderer;
import com.effacy.jui.core.client.dom.renderer.IRenderable;
import com.effacy.jui.core.client.dom.renderer.IRenderer;
import com.effacy.jui.platform.util.client.With;

import elemental2.dom.Node;

public abstract class ContainerBuilder<T extends ContainerBuilder<T>> extends NodeBuilder<T> implements IDomInsertableContainer<T> {

    /**
     * Calls {@link #insert(IDomInsertable...)} and returns the passed renderable.
     * 
     * @param renderable
     *                   the renderable to add.
     * @return this node builder.
     */
    @SuppressWarnings("unchecked")
    public T render(IRenderable renderable) {
        insert (renderable);
        return (T) this;
    }

    /**
     * Calls {@link #insert(IDomInsertable...)} and returns the passed renderer.
     * 
     * @param renderer
     *                 the renderer.
     * @return this builder.
     */
    @SuppressWarnings("unchecked")
    public T render(IRenderer renderer) {
        insert (renderer);
        return (T) this;
    } 

    /**
     * Similar to {@link #render(IRenderer)} but combines with rendering data (and
     * wraps in a {@link DataRendererBuilder}).
     * 
     * @param <D>      the data type for the renderer.
     * @param renderer
     *                 the renderer.
     * @param data
     *                 the data to apply to the renderer.
     * @return this builder.
     */
    @SuppressWarnings("unchecked")
    public <D> T render(IDataRenderer<D> renderer, D data) {
        if (renderer == null)
            return (T) this;
        insert (new DataRendererBuilder<D> (renderer, data));
        return (T) this;
    }

    /**
     * Inserts a {@link Text} node with constant contents.
     * <p>
     * This does not return the {@link Text} instance but treats as a property on
     * the contianer element (allowing it to be chained).
     * 
     * @param contents
     *                 the contents.
     * @return this builder instance.
     */
    public T text(String contents) {
        return text (contents, false);
    }

    /**
     * Inserts a {@link Text} node with constant contents.
     * <p>
     * This does not return the {@link Text} instance but treats as a property on
     * the contianer element (allowing it to be chained).
     * 
     * @param contents
     *                 the contents.
     * @return this builder instance.
     */
    @SuppressWarnings("unchecked")
    public T text(String contents, boolean split) {
        TextBuilder builder = new TextBuilder (contents).split (split);
        insert (builder);
        return (T) this;
    }

    /**
     * Inserts a {@link Text} node with provided contents (as safe html).
     * <p>
     * This does not return the {@link Text} instance but treats as a property on
     * the contianer element (allowing it to be chained).
     * 
     * @param contents
     *                 the contents provider.
     * @return this builder instance.
     */
    @SuppressWarnings("unchecked")
    public T safeHtml(SafeHtml contents) {
        TextBuilder text = new TextBuilder (() -> contents.asString ());
        insert (text);
        return (T) this;
    }

    /**
     * Inserts a non-breaking space (for convenience).
     * 
     * @return this builder instance.
     */
    public T nbsp() {
        return text("\u00A0");
    }

    /**
     * Inserts a custom DOM element.
     * 
     * @param tagName
     *                the name of the tag.
     * @return the element.
     */
    public ElementBuilder custom(String tagName) {
        return With.$ (new ElementBuilder (tagName), v -> insert (v));
    }

    /************************************************************************
     * Element creators.
     ************************************************************************/

    /**
     * Inserts a custom DOM element.
     * 
     * @param tagName
     *                   the name of the tag.
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder custom(String tagName, Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.custom (tagName, configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder a(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.a (configurer), v -> insert (v));
    }
    
    /**
     * Inserts a standard anchor DOM element with the given HREF attribute.
     * 
     * @param href
     *             the href (i.e. url).
     * @return the element.
     */
    public ElementBuilder a(String href) {
        return With.$ (DomBuilder.a (href), v -> insert (v));
    }

    /**
     * Inserts a standard anchor DOM element with the given HREF attribute and
     * target attribute.
     * 
     * @param href
     *               the href (i.e. url).
     * @param target
     *               the target attribute.
     * @return the element.
     */
    public ElementBuilder a(String href, String target) {
        return With.$ (DomBuilder.a (href, target), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder article(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.article (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder aside(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.aside (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder blockquote(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.blockquote (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder br(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.br (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder button(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.button (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder cite(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.cite (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder code(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.code (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder caption(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.caption (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder col(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.col (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder colgroup(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.colgroup (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder dd(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.dd (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder details(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.details (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder div(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.div (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder dl(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.dl (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder dt(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.dt (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder em(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.em (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder footer(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.footer (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder h1(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.h1 (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder h2(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.h2 (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder h3(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.h3 (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder h4(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.h4 (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder h5(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.h5 (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder h6(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.h6 (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder header(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.header (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder hr(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.hr (configurer), v -> insert (v));
    }


    /**
     * Inserts a standard label DOM element containing the passed text (a
     * convenience method that avoids having to separately insert a text node into
     * the label node).
     * 
     * @return the element.
     */
    public ElementBuilder label(String text) {
        ElementBuilder el = With.$ (new ElementBuilder ("label"), v -> insert (v));
        el.text (text);
        return el;
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder label(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.label (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder li(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.li (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder mark(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.mark (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder nav(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.nav (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder ol(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.ol (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder span(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.span (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder p(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.p (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder u(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.u (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder i(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.i (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder img(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.img (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param src
     *            the source URL for the image.
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder img(String src, Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.img (src, configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder strong(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.strong (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder sub(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.sub (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder summary(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.summary (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder sup(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.sup (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder ul(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.ul (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder table(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.table (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder thead(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.thead (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder tbody(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.tbody (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder tfoot(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.tfoot (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder tr(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.tr (configurer), v -> insert (v));
    }


    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder th(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.th (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder td(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.td (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM input element.
     * 
     * @param type
     *             the input type.
     * @return the element.
     */
    public ElementBuilder input(String type) {
        return With.$ (DomBuilder.input (type), v -> insert (v));
    }

    /**
     * Inserts a standard DOM input element.
     * 
     * @param type
     *                   the input type.
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder input(String type, Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.input (type, configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder textarea(Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.textarea (configurer), v -> insert (v));
    }

    /**
     * Inserts a standard DOM textarea element.
     * 
     * @param rows
     *             the number of rows (will only be applied if greater than 0).
     * @param cols
     *             the number of cols (will only be applied if greater than 0).
     * @return the element.
     */
    public ElementBuilder textarea(int rows, int cols) {
        return With.$ (DomBuilder.textarea (rows, cols), v -> insert (v));
    }

    /**
     * Inserts a standard DOM textarea element.
     * 
     * @param rows
     *                   the number of rows (will only be applied if greater than
     *                   0).
     * @param cols
     *                   the number of cols (will only be applied if greater than
     *                   0).
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public final ElementBuilder textarea(int rows, int cols, Consumer<ElementBuilder>... configurer) {
        return With.$ (DomBuilder.textarea (rows, cols, configurer), v -> insert (v));
    }

    /************************************************************************
     * Child node and node management.
     ************************************************************************/

    /**
     * The children in the container.
     */
    @SuppressWarnings("rawtypes")
    private List<NodeBuilder> children; 

    /**
     * Convenience to iterate over a list of items.
     * 
     * @param <V>
     *                the item type.
     * @param items
     *                the items to iterate over
     * @param builder
     *                a builder that is passed this container and the item being
     *                visited
     * @return this container.
     */
    @SuppressWarnings("unchecked")
    public <V> T forEach(List<V> items, BiConsumer<ContainerBuilder<?>, V> builder) {
        if (items != null) {
            items.forEach (item -> {
                builder.accept (this, item);
            });
        }
        return (T)this;
    }

    /**
     * Inserts the given children into this container. The children are of type
     * {@link IDomInsertable} which means they know how to insert themselves. For a
     * {@link NodeBuilder} this adds itself to the children of the container, other
     * variants may actually build additional structures.
     * <p>
     * See {@link IDomInsertableContainer#insert(IDomInsertable...)}.
     * 
     * @param <N>
     *                 the type of node.
     * @param children
     *                 the children to add (any of these that are {@code null} will
     *                 be ignored).
     * @return the first added child (as a convenience).
     */
    @Override
    @SuppressWarnings("unchecked")
    public final T insert(IDomInsertable... children) {
        for (IDomInsertable child : children) {
            if (child != null)
                child.insertInto (this);
        }
        return (T) this;
    }

    /**
     * Adds a node into this builder.
     * <p>
     * This is only accessible to the node builder hierarchy.
     * 
     * @param node the node to add.
     */
    @SuppressWarnings("rawtypes")
    void insertNode(NodeBuilder node) {
        if (children == null)
            children = new ArrayList<> ();
        children.add (node);
    }

    /**
     * Obtains the last child that was inserted.
     * 
     * @return the child.
     */
    @SuppressWarnings("rawtypes")
    public NodeBuilder lastChild() {
        if (children.isEmpty())
            return null;
        return children.get(children.size() - 1);
    }

    /**
     * Obtains the last child that was inserted, so long as it was an element
     * builder.
     * 
     * @return the child.
     */
    @SuppressWarnings("rawtypes")
    public ElementBuilder lastChildAsElementBuilder() {
        NodeBuilder nb = lastChild();
        if (nb == null)
            return null;
        if (nb instanceof ElementBuilder)
            return (ElementBuilder) nb;
        return null;
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected Node _nodeImpl(Node parent, BuildContext ctx) {
        if (!condition)
            return null;
        if (children != null) {
            for (NodeBuilder child : children) {
                if (child == null)
                    continue;
                Node node = child._node (parent, ctx);
                if (node != null)
                    parent.appendChild (node);
            }
        }
        return null;
    }

}
