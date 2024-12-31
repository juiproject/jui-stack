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

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.effacy.jui.core.client.component.IComponent;
import com.effacy.jui.core.client.control.IControl;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.IDomInsertable;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.ui.client.control.builder.IGroupBuilder.IRowBuilder.ICell;
import com.effacy.jui.ui.client.icon.FontAwesome;

public interface IGroupBuilder<SRC,DST> extends IDomInsertableContainer<IGroupBuilder<SRC,DST>>, IDomInsertable {

    /**
     * The modification context.
     * <p>
     * This will only be viable once the group has been built.
     * 
     * @return the context.
     */
    public IModificationContext modificationContext();

    /**
     * Can be used to adorn the group when it is being built.
     * <p>
     * Generally this is used to fine-adjust things like spacing or applying styles.
     * 
     * @param adorn
     *              invoked when built (first argument is the group element and
     *              second the group body).
     * @return this builder.
     */
    public IGroupBuilder<SRC,DST> adorn(BiConsumer<ElementBuilder,ElementBuilder> adorn);

    /**
     * Convenuence to apply styles directly to the group element (see
     * {@link #adorn(BiConsumer)}).
     * 
     * @param styles
     *            the styles to apply.
     * @return this builder.
     */
    default IGroupBuilder<SRC,DST> style(String... styles) {
        return adorn((g1,g2) -> {
            g1.style(styles);
        });
    }

    /**
     * Convenuence to apply CSS directly to the group element (see
     * {@link #adorn(BiConsumer)}).
     * 
     * @param css
     *            the CSS to apply.
     * @return this builder.
     */
    default IGroupBuilder<SRC,DST> css(String css) {
        return adorn((g1,g2) -> {
            g1.css(css);
        });
    } 

    /**
     * Assigns a reference to the group.
     * 
     * @param reference
     *                  the reference.
     * @return this builder.
     */
    public IGroupBuilder<SRC,DST> by(String reference);

    /**
     * Indents the group by the given amount.
     * 
     * @param offset
     *               the offset to indent by.
     * @return this builder.
     */
    public IGroupBuilder<SRC,DST> indent(Length offset);

    /**
     * Changes the gap between this group and the preceeding group.
     * 
     * @param offset
     *               the offset adjustment to make to the gap.
     * @return this builder.
     */
    public IGroupBuilder<SRC,DST> gap(Length offset);

    /**
     * Insert a separator above the group.
     * 
     * @return this builder.
     */
    public IGroupBuilder<SRC,DST> separator();

    /**
     * Orient the group horizontally.
     * 
     * @return this builder.
     */
    public IGroupBuilder<SRC,DST> horizontal();

    /**
     * Configures the conditional behaviour of the group.
     * 
     * @param conditional
     *                    to configure the behaviour.
     * @return this builder.
     */
    public IGroupBuilder<SRC,DST> conditional(Consumer<IGroupConditionalBuilder> conditional);

    /**
     * Declare and configure a header.
     * 
     * @param header
     *               to configure the header.
     * @return this builder.
     */
    public IGroupBuilder<SRC,DST> header(Consumer<IHeaderBuilder> header);

    /**
     * Declare and configure a footer.
     * 
     * @param footer
     *               to configure the footer.
     * @return this builder.
     */
    public IGroupBuilder<SRC,DST> footer(Consumer<IFooterBuilder> footer);

    /**
     * Add in and configure a nested group.
     * 
     * @param group
     *              to configure the nested group.
     * @return this builder.
     */
    public IGroupBuilder<SRC,DST> group(Consumer<IGroupBuilder<SRC,DST>> group);

    /**
     * This is a convenience method to nest at two levels rather than one level.
     * See {@link #group(Consumer)}.
     */
    default public IGroupBuilder<SRC,DST> group2(Consumer<IGroupBuilder<SRC,DST>> group) {
        return group (grp -> grp.group (group));
    }

    /**
     * This is a convenience method to nest at three levels rather than one level.
     * See {@link #group(Consumer)}.
     */
    default public IGroupBuilder<SRC,DST> group3(Consumer<IGroupBuilder<SRC,DST>> group) {
        return group2 (grp -> grp.group (group));
    }

    /**
     * Add in and configure a row to the group.
     * 
     * @param row
     *            to configure the row.
     * @return this builder.
     */
    public IGroupBuilder<SRC,DST> row(Consumer<IRowBuilder<SRC,DST>> row);

    /**
     * Creates a row and a control in that row.
     * <p>
     * See {@link IRowBuilder#control(String, IControl)}.
     */
    default public <V,CTL extends IControl<V>> IGroupBuilder<SRC,DST> control(String label, CTL ctl) {
        return control (null, label, ctl, null);
    }

    /**
     * Creates a row and a control in that row.
     * <p>
     * See {@link IRowBuilder#control(String, String, IControl)}.
     */
    default public <V,CTL extends IControl<V>> IGroupBuilder<SRC,DST> control(String by, String label, CTL ctl) {
        return control (by, label, ctl, null);
    }

    /**
     * Creates a row and a control in that row.
     * <p>
     * See {@link IRowBuilder#control(String, IControl, Consumer)}.
     */
    default public <V,CTL extends IControl<V>> IGroupBuilder<SRC,DST> control(String label, CTL ctl, Consumer<IRowBuilder.IControlCell<V,CTL,SRC,DST>> handler) {
        return control (null, label, ctl, handler);
    }

    /**
     * Creates a row and a control in that row.
     * <p>
     * See {@link IRowBuilder#control(String, String, IControl, Consumer)}.
     */
    public <V,CTL extends IControl<V>> IGroupBuilder<SRC,DST> control(String by, String label, CTL ctl, Consumer<IRowBuilder.IControlCell<V,CTL,SRC,DST>> handler);

    /**
     * Creates a row and a component in that row.
     * <p>
     * See {@link IRowBuilder#component(IComponent, int)}.
     */
    default public IGroupBuilder<SRC,DST> component(IComponent cpt) {
        return component (cpt, null);
    }

    /**
     * Creates a row and a component in that row.
     * <p>
     * See {@link IRowBuilder#component(IComponent, int, int)}.
     */
    public IGroupBuilder<SRC,DST> component(IComponent cpt, Consumer<ICell> handler);

    /**
     * Used to build out a header.
     */
    public interface IHeaderBuilder {

        /**
         * Declare a title.
         * 
         * @param title
         *              the title.
         * @return this header builder.
         */
        public IHeaderBuilder title(String title);

        /**
         * Declare am icon (displayed to the left of the title).
         * 
         * @param icon
         *             the icon CSS class (see {@link FontAwesome} for example).
         * @return this header builder.
         */
        public IHeaderBuilder icon(String icon);

        /**
         * Declare instructions to display (these appear under the title).
         * 
         * @param instruction
         *                    the instructions.
         * @return this header builder.
         */
        public IHeaderBuilder instruction(String instruction);

        /**
         * To render the header (does not use the other data).
         * 
         * @param renderer
         *                 the renderer.
         * @return this header builder.
         */
        public IHeaderBuilder renderer(Consumer<ElementBuilder> renderer);
    }

    /**
     * Used to build out a footer.
     */
    public interface IFooterBuilder {

        /**
         * Provides guidance text at the foot of the group.
         * 
         * @param guidance
         *                 the text to render.
         * @return this builder.
         */
        public IFooterBuilder guidance(String guidance);

        /**
         * To render the footer (does not use the other data).
         * 
         * @param renderer
         *                 the renderer.
         * @return this builder.
         */
        public IFooterBuilder renderer(Consumer<ElementBuilder> renderer);
    }

    public interface IGroupConditionalBuilder {

        public enum ConditionalBehaviour {
            /**
             * Hides the contents.
             */
            HIDE,
            
            /**
             * Employs an slider effect.
             */
            SLIDE,
            
            /**
             * Keeps it open.
             */
            OPEN;
        }

        default public IGroupConditionalBuilder active() {
            return active(true);
        }
        public IGroupConditionalBuilder active(boolean active);

        default public IGroupConditionalBuilder compact() {
            return compact(true);
        }
        public IGroupConditionalBuilder compact(boolean compact);

        public IGroupConditionalBuilder behaviour(ConditionalBehaviour behaviour);

        /**
         * For used when the group is conditional (i.e. a radio group). Here we define
         * the group that the conditional block belongs to and the discriminator used to
         * identify the block. This is returned by a call to
         * {@link IModificationContext#value(String)} passing the group (reference).
         * 
         * @param group
         *                           the name of the group the conditional belongs to.
         * @param groupDescriminator
         *                           to use to discriminate this specific conditional
         *                           within the group.
         * @return this builder.
         */
        public IGroupConditionalBuilder group(String group, String groupDescriminator);

        public IGroupConditionalBuilder handler(BiConsumer<IModificationContext,Boolean> handler);
    }

    public interface IRowBuilder<SRC,DST> {

        /**
         * Convenuence to apply CSS directly to the root element (see
         * {@link #adorn(Consumer)}).
         * 
         * @param css
         *            the CSS to apply.
         * @return this builder.
         */
        default IRowBuilder<SRC,DST> css(String css) {
            return adorn((el) -> {
                el.css(css);
            });
        }

        /**
         * Convenuence to apply styles directly to the root element (see
         * {@link #adorn(Consumer)}).
         * 
         * @param styles
         *            the styles to apply.
         * @return this builder.
         */
        default IRowBuilder<SRC,DST> style(String... styles) {
            return adorn((el) -> {
                el.style(styles);
            });
        }

        /**
         * To adorn the row root element.
         * 
         * @param adorner
         *           actions to apply to the root element.
         * @return this builder.
         */
        public IRowBuilder<SRC,DST> adorn(Consumer<ElementBuilder> adorner);
        
        /**
         * See {@link #control(String, String, IControl, Consumer)} but with no
         * <code>by</code> or <code>handler</code>.
         */
        default public <V,CTL extends IControl<V>> IRowBuilder<SRC,DST> control(String label, CTL ctl) {
            return control (null, label, ctl, null);
        }

        /**
         * See {@link #control(String, String, IControl, Consumer)} but with no
         * <code>handler</code>.
         */
        default public <V,CTL extends IControl<V>> IRowBuilder<SRC,DST> control(String by, String label, CTL ctl) {
            return control (by, label, ctl, null);
        }

        /**
         * See {@link #control(String, String, IControl, Consumer)} but with no
         * <code>by</code>.
         */
        default public <V,CTL extends IControl<V>> IRowBuilder<SRC,DST> control(String label, CTL ctl, Consumer<IControlCell<V,CTL,SRC,DST>> handler) {
            return control (null, label, ctl, handler);
        }

        /**
         * Adds a control.
         * 
         * @param <V>
         *                the value type for the control.
         * @param <CTL>
         *                the control type.
         * @param by
         *                (optional) a reference to the control.
         * @param label
         *                the label to adorn the control with.
         * @param ctl
         *                the control.
         * @param handler
         *                (optional) to handle changes on the control.
         * @return this row builder.
         */
        public <V,CTL extends IControl<V>> IRowBuilder<SRC,DST> control(String by, String label, CTL ctl, Consumer<IControlCell<V,CTL,SRC,DST>> handler);

        /**
         * Inserts DOM content via a builder. This will build directly into the cell
         * container.
         * 
         * @param builder
         *                the builder to build content.
         * @return this builder.
         */
        public IRowBuilder<SRC,DST> insert(Consumer<ElementBuilder> builder);

        /**
         * Adds a component to the row.
         * 
         * @param cpt
         *                the component to add.
         * @return this builder.
         */
        default public IRowBuilder<SRC,DST> component(IComponent cpt) {
            return component(cpt, null);
        }

        /**
         * Adds a component to the row.
         * 
         * @param cpt
         *                the component to add.
         * @param handler
         *                to further configure the cell.
         * @return this builder.
         */
        public IRowBuilder<SRC,DST> component(IComponent cpt, Consumer<ICell> handler);

        /**
         * Inserts an expander to push subsequent content to the right.
         * 
         * @return this builder.
         */
        public IRowBuilder<SRC,DST> expander();

        /**
         * User to further configure a cell.
         */
        public interface ICell {

            /**
             * Grow this cell to the relative proportional share (the default is 0).
             * 
             * @param share
             *              the proportional share (as taken across all shares).
             * @return this cell instance.
             */
            public ICell grow(int share);

            /**
             * Apply a positional offset to the element.
             * 
             * @param vertical
             *               the vertical offset.
             * @param horizontal
             *               the horizontal offset.
             * @return this cell instance.
             */
            public ICell offset(int vertical, int horizontal);

            /**
             * Displays the guidance text.
             * 
             * @param guidance
             *                 the guidance text to display.
             * @return this cell instance.
             */
            public ICell guidance(String guidance);
        }

        /**
         * Use to further configure a control based cell.
         */
        public interface IControlCell<V,CTL extends IControl<V>,SRC,DST> {

            /**
             * Obtains the control itself.
             * 
             * @return the control.
             */
            public CTL control();

            /**
             * Obtains the component in the cell. Normally this will be a control (and so
             * returns the same as {@link #control()}) but sometimes the cell will contain a
             * component.
             * 
             * @return the component (which could just be the control).
             */
            public IComponent component();

            /**
             * Provides a name to reference the control with.
             * 
             * @param reference
             *                  the reference name to used.
             * @return this handler.
             */
            public IControlCell<V,CTL,SRC,DST> by(String reference);

            /**
             * Mark as required.
             * 
             * @return this handler.
             */
            public IControlCell<V,CTL,SRC,DST> required();

            /**
             * Assign help text for display.
             * 
             * @param help
             *             the help text.
             * @return this handler.
             */
            public IControlCell<V,CTL,SRC,DST> help(String help);

            /**
             * Assign a modification handler (invoked when the control is modified).
             * 
             * @param handler
             *                the handler to invoke.
             * @return this handler.
             */
            public IControlCell<V,CTL,SRC,DST> modify(IModification<V,CTL> handler);
            
            /**
             * Assigns the initial enablement state.
             * 
             * @return this handler.
             */
            public IControlCell<V,CTL,SRC,DST> disable();

            /**
             * Assigns the initial hidden state.
             * 
             * @return this handler.
             */
            public IControlCell<V,CTL,SRC,DST> hide();

            /**
             * Grow this cell to the relative proportional share (the default is 0).
             * 
             * @param share
             *              the proportional share (as taken across all shares).
             * @return this handler instance.
             */
            public IControlCell<V,CTL,SRC,DST> grow(int share);

            /**
             * Assign a cell alignment to the right (makes sense only for grown cells where
             * there is room for the control ).
             * 
             * @param align
             *              {@code true} to right align.
             * @return this handler instance.
             */
            public IControlCell<V,CTL,SRC,DST> rightAlign(boolean align);

            /**
             * Convenience to invoke {@link #rightAlign(boolean)} with {@code true}.
             * 
             * @return this handler instance.
             */
            default public IControlCell<V,CTL,SRC,DST> rightAlign() {
                return rightAlign(true);
            }

            /**
             * Apply a positional offset to the element.
             * 
             * @param vertical
             *               the vertical offset.
             * @param horizontal
             *               the horizontal offset.
             * @return this handler instance.
             */
            public IControlCell<V,CTL,SRC,DST> offset(int vertical, int horizontal);

            /**
             * Displays the guidance text.
             * 
             * @param guidance
             *                 the guidance text to display.
             * @return this cell instance.
             */
            public IControlCell<V,CTL,SRC,DST> guidance(String guidance);

            /**
             * User to clear a control (set to empty).
             * 
             * @param clearer
             *               invoked to clear the control.
             * @return this handler.
             */
            public IControlCell<V,CTL,SRC,DST> clear(IClearer<V> clearer);

            /**
             * Used to get a value from an external source and apply to the control for
             * editing.
             * 
             * @param getter
             *               the getter to invoke.
             * @return this handler.
             */
            public IControlCell<V,CTL,SRC,DST> edit(IGetter<V,SRC> getter);

            /**
             * Used to get a value from an external source and apply to the control for
             * editing.
             * 
             * @param getter
             *               the getter to invoke.
             * @return this handler.
             */
            default public IControlCell<V,CTL,SRC,DST> edit(IAbridgedGetter<V,SRC> getter) {
                return edit ((ctl, v) -> getter.get(v));
            }

            /**
             * An alternative name for {@link #edit(IGetter)}.
             */
            default public IControlCell<V,CTL,SRC,DST> from(IGetter<V,SRC> getter) {
                return edit (getter);
            }

            /**
             * An alternative name for {@link #edit(IGetter)}.
             */
            default public IControlCell<V,CTL,SRC,DST> from(IAbridgedGetter<V,SRC> getter) {
                return edit (getter);
            }

            /**
             * An alternative name for See {@link #retrieve(ISetter)}.
             */
            default public IControlCell<V,CTL,SRC,DST> to(ISetter<V,DST> setter) {
                return retrieve (setter);
            }

            /**
             * An alternative name for See {@link #retrieve(ISetter,boolean)}.
             */
            default public IControlCell<V,CTL,SRC,DST> to(ISetter<V,DST> setter, boolean applyWhenNotDirty) {
                return retrieve (setter, applyWhenNotDirty);
            }

            /**
             * Used to map the control value onto something post edit (i.e. to apply a
             * change).
             * <p>
             * Whether it is invoked when the control is not dirty is dependent on the
             * default setting of the surrounding form.
             */
            public IControlCell<V,CTL,SRC,DST> retrieve(ISetter<V,DST> setter);

            /**
             * Assigns a setter to be invoked when applying the control value to an external
             * object.
             * 
             * @param setter
             *                          the setter to invoke.
             * @param applyWhenNotDirty
             *                          {@code true} if to invoke the setter even when the
             *                          control is not dirty (default is {@code false}).
             * @return this handler.
             */
            public IControlCell<V,CTL,SRC,DST> retrieve(ISetter<V,DST> setter, boolean applyWhenNotDirty);

        }
    }

    @FunctionalInterface
    public interface IModification<V,CTL extends IControl<V>> {
        
        /**
         * Invoked when the passed control has been modified.
         * 
         * @param ctx
         *                the form modification context for changing state.
         * @param control
         *                the control that was modified.
         * @param value
         *                the new value of the control.
         */
        public void modified(IModificationContext ctx, CTL control);

    }

    /**
     * Use to clear control.
     */
    @FunctionalInterface
    public interface IClearer<V> {

        /**
         * Clears the given control.
         * 
         * @param ctl
         *               the control.
         */
        public void clear(IControl<V> ctl);
    }

    /**
     * Use to obtain a value from a source to assign to a control.
     */
    @FunctionalInterface
    public interface IGetter<V,SRC> {

        /**
         * Gets the value from the passed source (to apply to the related control).
         * 
         * @param ctl
         *               the control.
         * @param source
         *               the source to get from.
         * @return the value retrieved from the source to apply to the control.
         */
        public V get(IControl<V> ctl, SRC source);
    }

    /**
     * Use to obtain a value from a source to assign to a control.
     */
    @FunctionalInterface
    public interface IAbridgedGetter<V,SRC> {

        /**
         * Gets the value from the passed source (to apply to the related control).
         * 
         * @param source
         *               the source to get from.
         * @return the value retrieved from the source to apply to the control.
         */
        public V get(SRC source);
    }

    /**
     * Used to set a value (from a control) to a destination.
     */
    @FunctionalInterface
    public interface ISetter<V,DST> {

        public void set(ISetterContext context, V value, DST destination);

        /**
         * Provides a context to support the setting of values on a destination.
         */
        public interface ISetterContext {

            /**
             * Determines if the underlying control was dirty.
             * 
             * @return {@code true} if it was.
             */
            public boolean dirty();

            /**
             * Obtains a control with the given reference.
             * 
             * @param <W>       the value type.
             * @param reference
             *                  the reference to the control.
             * @return the control (will be {@code null} if not present).
             */
            public <W> IControl<W> control(String reference);

            /**
             * See {@link #value(String)} but with a default if the associated value is
             * {@code null}.
             * 
             * @param <W>          the value type.
             * @param reference
             *                     the reference to the control.
             * @param defaultValue
             *                     the default value to use when the underlying value is
             *                     {@code null} (or the control does not exist).
             * @return the value.
             */
            default public <W> W value(String reference, W defaultValue) {
                W value = value (reference);
                return (value == null) ? defaultValue : value;
            }

            /**
             * Obtains a control value from the control with the given reference. It is
             * assumed the control exists.
             * 
             * @param <W>       the value type.
             * @param reference
             *                  the reference to the control.
             * @return the control value (will be {@code null} if not present).
             */
            default public <W> W value(String reference) {
                IControl<W> ctl = control(reference);
                return (ctl == null) ? null : ctl.value ();
            }
        }
    }
}
