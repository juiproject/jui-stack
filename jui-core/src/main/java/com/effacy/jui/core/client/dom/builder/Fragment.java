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
import java.util.function.Consumer;
import java.util.function.Supplier;

import elemental2.dom.Element;
import elemental2.dom.Node;
import jsinterop.base.Js;

public class Fragment<T extends Fragment<T>> implements IDomInsertable {

    @FunctionalInterface
    public interface IFragmentAdornment {

        /**
         * Applies the adornment to the fragment target (generally the root) element.
         * 
         * @param target
         *             the target element.
         */
        public void adorn(ElementBuilder target);
    }

    /**
     * For building the fragment.
     */
    private Consumer<ContainerBuilder<?>> builder;

    /**
     * The adornments to be applied to the fragment.
     */
    private List<IFragmentAdornment> adornments;

    /**
     * See {@link #use(Consumer)}.
     */
    protected Consumer<Node> use;

    /**
     * See {@link #iff(boolean)}.
     */
    protected Supplier<Boolean> conditional;

    /**
     * Can be used to conditionally display the fragment.
     * 
     * @param conditional
     *                    the evaluated condition.
     * @return this fragment.
     */
    @SuppressWarnings("unchecked")
    public T iff(boolean conditional) {
        this.conditional = () -> conditional;
        return (T) this;
    }

    /**
     * Can be used to conditionally display the fragment.
     * 
     * @param conditional
     *                    the evaluated condition.
     * @return this fragment.
     */
    @SuppressWarnings("unchecked")
    public T iff(Supplier<Boolean> conditional) {
        this.conditional = conditional;
        return (T) this;
    }

    /**
     * Applies the given styles to the fragment root.
     * 
     * @param styles
     *               the styles to apply.
     * @return this fragment.
     */
    public T style(String...styles) {
        return adorn (FragmentAdornments.style (styles));
    }

    /**
     * Applies formatted CSS (i.e. "font-size: 12px") to the fragment root.
     * 
     * @param css
     *            the formatted css to apply.
     * @return this fragment.
     */
    public T css(String css) {
        return adorn (FragmentAdornments.css (css));
    }

    /**
     * Applies one or more adornments.
     * <p>
     * This is addative so can be called multiple times.
     * 
     * @param adornments
     *                   the adornment(s) to apply.
     * @return this fragment.
     */
    @SuppressWarnings("unchecked")
    public T adorn(IFragmentAdornment... adornments) {
        if (this.adornments == null)
            this.adornments = new ArrayList<> ();
        for (IFragmentAdornment adornment : adornments) {
            if (adornment == null)
                continue;
            this.adornments.add (adornment);
        }
        return (T)this;
    }
    
    /**
     * Applies a {@link ElementBuilder#use(Consumer)} to the root element (if
     * created).
     * 
     * @param use
     *            the use to apply.
     * @return this fragment.
     */
    @SuppressWarnings("unchecked")
    public T use(Consumer<Node> use) {
        this.use = use;
        return (T) this;
    }

    /**
     * Default constructor.
     */
    protected Fragment() {
        // Nothing.
    }

    /**
     * Construct with a builder.
     * 
     * @param builder
     *                the builder (see {@link #builder(Consumer)}).
     */
    protected Fragment(Consumer<ContainerBuilder<?>> builder) {
        builder(builder);
    }

    /**
     * Assigns a builder.
     * <p>
     * The builder will be invoked with the parent into which it will build its
     * contents. More than one element can be inserted into the parent and
     * adornments need to be handled manually.
     * <p>
     * If adornments (including any assigned {@link #use(Consumer)}) need to be
     * appllied then they may be done manually by a call to
     * {@link #adorn(ElementBuilder)}. For example:
     * <tt>
     * builder(parent -> {
     *   adorn(Div.$(parent)).$ (
     *     ...
     *   );
     * });
     * </tt>
     * 
     * @param builder
     *                the builder.
     */
    protected void builder(Consumer<ContainerBuilder<?>> builder) {
        this.builder = builder;
    }

    /**
     * Obtains a deferred consolidation of adornments so that they can be applied
     * specifcally (i.e. to an alternative target).
     * <p>
     * This is how adornment must be applied when using the constructor based method
     * or supplied builder method (since we cannot know exactly how the contents are
     * added to the parent, which could be multiple).
     * 
     * @return the deferred adornments.
     */
    protected IFragmentAdornment adornments() {
        return FragmentAdornments.deferred(() -> {
            return FragmentAdornments.collection(adornments);
        });
    }

    /**
     * Builds into the passed parent.
     * 
     * @param parent the parent to build into.
     */
    public void build(ContainerBuilder<?> parent) {
        if ((conditional != null) && !conditional.get())
            return;
        if (builder != null) {
            builder.accept (parent);
        } else {
            ElementBuilder el = createRoot (parent);
            if (el != null) {
                el.$ (root -> {
                    buildInto (root);
                    adorn (root);
                });
            }
        }
    }

    /**
     * Called by {@link #build(ContainerBuilder)} (when no builder has been
     * assigned) to create the root element of the fragment.
     * <p>
     * This is passed to {@link #buildInto(ElementBuilder)} and then to which
     * adornments are applied.
     * <p>
     * This may return {@code null} in which case the fragment will generate no
     * content.
     * 
     * @param parent
     *               the parent to add the root element to.
     * @return the root element as a builder.
     */
    protected ElementBuilder createRoot(ContainerBuilder<?> parent) {
        return Div.$ (parent);
    }

    /**
     * Builds into the passed root element. This is a DIV that is created by
     * {@link #build(ContainerBuilder)} when no builder has been assigned.
     * Adornments are applied automatically.
     * 
     * @param root
     *             the root element to build into.
     */
    protected void buildInto(ElementBuilder root) {
        // Nothing.
    }

    /**
     * To apply any registered adornments (as well as any other preparations, such
     * as the use).
     * 
     * @param target
     *               the target to apply the adornments to.
     * @return the passed target.
     */
    protected <E extends ElementBuilder> E adorn(E target) {
        if (target == null)
            return target;
        if (use != null)
            target.use (use);
        if (adornments != null)
            adornments.forEach (adornment -> adornment.adorn (target));
        return target;
    }
    
    /************************************************************************
     * For IDomInsertable.
     ************************************************************************/

    /**
     * This is a hook to apply a style to the parent node. Generally this is not how
     * a fragment should operate (it should be completely self-contained), however
     * there are rare times where we want to work around this limitation.
     */
    protected String parentStyleHook;

    @Override
    public final void insertInto(ContainerBuilder<?> parent) {
        new FragmentBuilder ().insertInto (parent);
    }
    
    /**
     * Builder for a fragment. This defers the build until the builder is actually
     * built. This allows the fragment to be configured after it is added to the
     * parent builder.
     */
    public class FragmentBuilder extends DeferredContainerBuilder<FragmentBuilder> {

        @Override
        protected void build(Node parent, BuildContext ctx) {
            if (parentStyleHook != null)
                ((Element) Js.cast(parent)).classList.add (parentStyleHook);
            Fragment.this.build (this);
        }
        
    }
    
}
