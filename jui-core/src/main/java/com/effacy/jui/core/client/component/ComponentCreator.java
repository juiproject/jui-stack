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
package com.effacy.jui.core.client.component;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.builder.DomBuilder;
import com.effacy.jui.core.client.dom.builder.ExistingElementBuilder;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.core.client.dom.builder.NodeBuilder.NodeContext;
import com.effacy.jui.platform.util.client.Carrier;

import elemental2.dom.Element;

/**
 * Tools for working with inline components.
 * <p>
 * Inline components are a convenient mechanism for creating component without
 * having to explicitly create a dedicated class.
 *
 * @author Jeremy Buckley
 */
public class ComponentCreator {

    /**
     * A simple mechanism for creating a component and rendering it into a DOM
     * builder.
     * 
     * @param el
     *                the builder to render the component into.
     * @param builder
     *                to build the DOM.
     */
    public static Component<Component.Config> $(IDomInsertableContainer<?> el, Consumer<ExistingElementBuilder> builder) {
        Component<Component.Config> cpt = build (builder);
        el.insert (cpt);
        return cpt;
    }

    /**
     * A simple mechanism for creating a component and rendering it into a DOM
     * builder.
     * 
     * @param el
     *                the builder to render the component into.
     * @param builder
     *                to build the DOM.
     */
    public static Component<Component.Config> $(IDomInsertableContainer<?> el, BiConsumer<Supplier<IComponent>, ExistingElementBuilder> builder) {
        Component<Component.Config> cpt = build (builder);
        el.insert (cpt);
        return cpt;
    }

    /**
     * A simple mechanism for creating a component and rendering it into a DOM
     * builder.
     * 
     * @param el
     *                the builder to render the component into.
     * @param builder
     *                to build the DOM.
     * @param extractor
     *                  to extract from the built DOM.
     */
    public static Component<Component.Config> $(IDomInsertableContainer<?> el, Consumer<ExistingElementBuilder> builder, Consumer<NodeContext> extractor) {
        Component<Component.Config> cpt = build (builder, extractor);
        el.insert (cpt);
        return cpt;
    }



    /**
     * A simple mechanism for creating a component and rendering it into a DOM
     * builder.
     * 
     * @param el
     *                the builder to render the component into.
     * @param builder
     *                to build the DOM.
     * @param extractor
     *                  to extract from the built DOM.
     * @param afterRender
     *                      invoked after the component has been rendered.
     */
    public static Component<Component.Config> $(IDomInsertableContainer<?> el, Consumer<ExistingElementBuilder> builder, Consumer<NodeContext> extractor, Consumer<IComponent> afterRender) {
        Component<Component.Config> cpt = build (builder, extractor, afterRender);
        el.insert (cpt);
        return cpt;
    }


    /**
     * A simple mechanism for building components (but not one that is very
     * practical, more for experimentation).
     * 
     * @param builder
     *                to build the DOM.
     * @return the component instance.
     * 
     * @deprecated use {@link #build(Consumer)}.
     */
    public static Component<Component.Config> $(Consumer<ExistingElementBuilder> builder) {
        return build (builder);
    }
    
    /**
     * A simple mechanism for building components (but not one that is very
     * practical, more for experimentation).
     * 
     * @param builder
     *                  to build the DOM.
     * @param extractor
     *                  to extract from the built DOM.
     * @return the component instance.
     * 
     * @deprecated use {@link #build(Consumer, Consumer)}.
     */
    public static Component<Component.Config> $(Consumer<ExistingElementBuilder> builder, Consumer<NodeContext> extractor) {
        return build (builder, extractor);
    }

    /**
     * A simple mechanism for building components (but not one that is very
     * practical, more for experimentation).
     * 
     * @param builder
     *                      to build the DOM.
     * @param afterRender
     *                      invoked after the component has been rendered.
     * @return the component instance.
     * 
     * @deprecated use {@link #build(Consumer, Consumer, Consumer).
     */
    public static Component<Component.Config> $(Consumer<ExistingElementBuilder> builder, Consumer<NodeContext> extractor, Consumer<IComponent> afterRender) {
        return build (builder, extractor, afterRender);
    }

    /**
     * A simple mechanism for building components (but not one that is very
     * practical, more for experimentation).
     * 
     * @param builder
     *                to build the DOM.
     * @return the component instance.
     */
    public static Component<Component.Config> build(Consumer<ExistingElementBuilder> builder) {
        return build (builder, null, null);
    }
    
    /**
     * A simple mechanism for building components (but not one that is very
     * practical, more for experimentation).
     * 
     * @param builder
     *                  to build the DOM.
     * @param extractor
     *                  to extract from the built DOM.
     * @return the component instance.
     */
    public static Component<Component.Config> build(Consumer<ExistingElementBuilder> builder, Consumer<NodeContext> extractor) {
        return build (builder, extractor, null);
    }

    /**
     * A simple mechanism for building components (but not one that is very
     * practical, more for experimentation).
     * 
     * @param builder
     *                      to build the DOM.
     * @param extractor
     *                  to extract from the built DOM.
     * @param afterRender
     *                      invoked after the component has been rendered.
     * @return the component instance.
     */
    public static Component<Component.Config> build(Consumer<ExistingElementBuilder> builder, Consumer<NodeContext> extractor, Consumer<IComponent> afterRender) {
        return new SimpleComponent () {

            @Override
            protected INodeProvider buildNode(Element el, Component.Config data) {
                return DomBuilder.el (el, builder).build (extractor);
            }

            @Override
            protected void onAfterRender() {
                super.onAfterRender ();
                if (afterRender != null)
                    afterRender.accept (this);
            }

        };
    }

    /**
     * A simple mechanism for building components.
     * <p>
     * Passed is a builder expression that takes two arguments, the first is a
     * supplied to the finally created component while the second is a DOM builder
     * element to build DOM into.
     * 
     * @param builder
     *                to build the DOM.
     * @return the component instance.
     */
    public static Component<Component.Config> build(BiConsumer<Supplier<IComponent>, ExistingElementBuilder> builder) {
        return build (builder, null);
    }

    /**
     * A simple mechanism for building components.
     * <p>
     * Passed is a builder expression that takes two arguments, the first is a
     * supplied to the finally created component while the second is a DOM builder
     * element to build DOM into.
     * 
     * @param builder
     *                  to build the DOM.
     * @param extractor
     *                  to extract from the built DOM.
     * @return the component instance.
     */
    public static Component<Component.Config> build(BiConsumer<Supplier<IComponent>, ExistingElementBuilder> builder, Consumer<NodeContext> extractor) {
        Carrier<IComponent> supplier = Carrier.of ();
        SimpleComponent cpt = new SimpleComponent () {

            @Override
            protected INodeProvider buildNode(Element el, Component.Config data) {
                return DomBuilder.el (el, root -> {
                    builder.accept(() -> supplier.get (), root);
                }).build (extractor);
            }

        };
        supplier.set (cpt);
        return cpt;
    }

}
