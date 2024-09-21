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

import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.builder.DomBuilder;
import com.effacy.jui.core.client.dom.builder.ExistingElementBuilder;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.core.client.dom.builder.NodeBuilder.NodeContext;
import com.effacy.jui.core.client.state.IStateVariable;

import elemental2.dom.Element;

public class StateComponentCreator {

    /**
     * A simple mechanism for creating a component and rendering it into a DOM
     * builder.
     * 
     * @param el
     *                the builder to render the component into.
     * @param state
     *                      the state variable for the component.
     * @param builder
     *                to build the DOM.
     */
    public static <S extends IStateVariable<S>> StateComponent<S> $(IDomInsertableContainer<?> el, S state, BiConsumer<S,ExistingElementBuilder> builder) {
        StateComponent<S> cpt = build (state, builder);
        el.insert (cpt);
        return cpt;
    }

    /**
     * A simple mechanism for building components (but not one that is very
     * practical, more for experimentation).
     * 
     * @param el
     *                the builder to render the component into.
     * @param state
     *                      the state variable for the component.
     * @param builder
     *                to build the DOM.
     * @param extractor
     *                  to extract from the built DOM.
     * @return the component instance.
     */
    public static <S extends IStateVariable<S>> StateComponent<S> $(IDomInsertableContainer<?> el, S state, BiConsumer<S,ExistingElementBuilder> builder, BiConsumer<S,NodeContext> extractor) {
        StateComponent<S> cpt = build (state, builder, extractor);
        el.insert (cpt);
        return cpt;
    }

    /**
     * A simple mechanism for building components (but not one that is very
     * practical, more for experimentation).
     * 
     * @param el
     *                the builder to render the component into.
     * @param state
     *                      the state variable for the component.
     * @param builder
     *                      to build the DOM.
     * @param onAfterRender
     *                      invoked after the component has been rendered.
     * @return the component instance.
     */
    public static <S extends IStateVariable<S>> StateComponent<S> $(IDomInsertableContainer<?> el, S state, BiConsumer<S,ExistingElementBuilder> builder, BiConsumer<S,NodeContext> extractor, Consumer<StateComponent<S>> afterRender) {
        StateComponent<S> cpt = build (state, builder, extractor, afterRender);
        el.insert (cpt);
        return cpt;
    }

    /**
     * A simple mechanism for building components (but not one that is very
     * practical, more for experimentation).
     * 
     * @param state
     *                      the state variable for the component.
     * @param builder
     *                to build the DOM.
     * @return the component instance.
     */
    public static <S extends IStateVariable<S>> StateComponent<S> build(S state, BiConsumer<S,ExistingElementBuilder> builder) {
        return build (state, builder, null, null);
    }
    
    /**
     * A simple mechanism for building components (but not one that is very
     * practical, more for experimentation).
     * 
     * @param state
     *                      the state variable for the component.
     * @param builder
     *                  to build the DOM.
     * @param extractor
     *                  to extract from the built DOM.
     * @return the component instance.
     */
    public static <S extends IStateVariable<S>> StateComponent<S> build(S state, BiConsumer<S,ExistingElementBuilder> builder, BiConsumer<S,NodeContext> extractor) {
        return build (state, builder, extractor, null);
    }

    /**
     * A simple mechanism for building components (but not one that is very
     * practical, more for experimentation).
     * 
     * @param state
     *                      the state variable for the component.
     * @param builder
     *                      to build the DOM.
     * @param onAfterRender
     *                      invoked after the component has been rendered.
     * @return the component instance.
     */
    public static <S extends IStateVariable<S>> StateComponent<S> build(S state, BiConsumer<S,ExistingElementBuilder> builder, BiConsumer<S,NodeContext> extractor, Consumer<StateComponent<S>> afterRender) {
        return new StateComponent<S> (state) {

            @Override
            protected INodeProvider buildNode(Element el, Component.Config data) {
                return DomBuilder.el (el, root -> {
                    if (builder != null)
                        builder.accept (state (), root);
                }).build (dom -> {
                    if (extractor != null)
                        extractor.accept (state (), dom);
                });
            }

            @Override
            protected void onAfterRender() {
                super.onAfterRender ();
                if (afterRender != null)
                    afterRender.accept (this);
            }

        };
    }
}
