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

import com.effacy.jui.core.client.IDisposable;
import com.effacy.jui.core.client.dom.renderer.IRenderable;
import com.effacy.jui.core.client.observable.IObservable;

import elemental2.dom.Element;

/**
 * Declares the contract expected of a component, being an encapsulate UI
 * element.
 *
 * @author Jeremy Buckley
 */
public interface IComponent extends IObservable, IDisposable, IRenderable {

    /**
     * An application wide and immutable reference to the component.
     * 
     * @return the UUID for the component.
     */
    public long getUUID();

    /**
     * Assigns the specified metadata attribute.
     * 
     * @param attribute
     *                  the attribute name.
     * @param value
     *                  the attribute value.
     */
    public void putMetaAttribute(String name, String value);

    /**
     * Determines if the passed attribute had the passed value.
     * 
     * @param name
     *              the name of the attribute to test.
     * @param match
     *              the value to test against.
     */
    default boolean matchMetaAttribute(String name, String match) {
        String value = getMetaAttribute (name);
        if (value == null)
            return (match == null);
        if (match == null)
            return false;
        return match.equals (value);
    }

    /**
     * Obtains the specified metadata attribute.
     * 
     * @param name
     *                  the attribute name.
     * @return the associated value ({@code null} if not set)
     */
    public String getMetaAttribute(String name);

    /**
     * Gets the ID of the component (see {@link #setId(String)}).
     * 
     * @return the component ID.
     */
    public String getId();

    /**
     * Sets the ID of the component.
     * <p>
     * This is applied as the <code>id</code> attribute of the root DOM element for
     * the component.
     * 
     * @param id
     *           the component ID.
     */
    public void setId(String id);

    /**
     * Gets the root element of the component.
     * <p>
     * This is only well-defined after the component has been rendered (see also
     * {@link #isRendered()}).
     * 
     * @return the root element.
     */
    public Element getRoot();

    /**
     * Determines if the component is hidden.
     * 
     * @return {@code true} if it is.
     */
    public boolean isHidden();

    /**
     * Determines if the component has been disabled.
     * 
     * @return {@code true} if it is currently disabled.
     */
    public boolean isDisabled();

    /**
     * Pushes the enable state.
     */
    public void pushEnableState();

    /**
     * Pops the last enable state.
     */
    public void popEnableState();

    /**
     * Disables the component.
     */
    public void disable();

    /**
     * Enables the component.
     */
    public void enable();

    /**
     * Hides the component.
     */
    public void hide();

    /**
     * Shows the component in-situ (assumes the component has been rendered and
     * attached).
     */
    public void show();

    /**
     * Determine if the passed component is attached or contained to this component
     * (for example, via an attachment point or region).
     * <p>
     * Note that this is potentially quite an expensive operation so use sparingly.
     * 
     * @param component
     *                  the component to check.
     * @return {@code true} if it is attached.
     */
    public boolean contains(IComponent component);

    /**
     * Applies the visitor for each child managed by the component. This processes
     * all attachment and region points.
     * 
     * @param visitor
     *                the visitor.
     */
    public void forEach(Consumer<IComponent> visitor);

    /**
     * Brings focus onto the component.
     */
    public void focus();

    /**
     * Blurs the component.
     */
    public void blur();

    /**
     * Determines if the control is in focus.
     * 
     * @return {@code true} if it is.
     */
    public boolean isInFocus();

    /**
     * This is called to force the component to re-determine its layout and sizing.
     * Generally it is called internally when aspects of the component change (such
     * as its size or content) but may be called externally when there has been an
     * outside influence (such as the result of a layout).
     */
    public void reconfigure();

    /**
     * Used to configure the component on render (a hook).
     * 
     * @param configurer
     *                   to invoke when rendered.
     */
    public <T extends IComponent> T configureOnRender(BiConsumer<IComponent, Element> configurer);

    /**
     * Used to configure the component on render (a hook).
     * 
     * @param configurer
     *                   to invoke when rendered.
     */
    default public <T extends IComponent> T css(Consumer<Element> configurer) {
        return configureOnRender ((cpt, el) -> configurer.accept (el));
    }

    /**
     * Gets layout data for the component. See {@link #setLayoutData(Object)}.
     * 
     * @return the layout data.
     */
    public Object getLayoutData();

    /**
     * Sets layout data on the component.
     * <p>
     * This is used by the parent component whenever it employs a layout to arrange
     * its children. The layout calls on this data to guide it in how it should
     * treat the component with respect to the configurable aspects of the layout.
     * 
     * @param layoutData
     *                   the layout data.
     */
    public void setLayoutData(Object layoutData);

    /**
     * Activate the component. This has no specific meaning other than is invokes an
     * activate event.
     */
    public void activate();

    /**
     * Determines if the passed element is contained in the components hierarchy.
     * 
     * @param el
     *           the element to test.
     * @return {@code true} if the element is contained in the component.
     */
    public boolean contains(Element el);

    /**
     * Masks the component.
     */
    public void mask();

    /**
     * Unmasks the component.
     */
    public void unmask();

    /**
     * Determines if the component has been rendered.
     * 
     * @return {@code true} if it has.
     */
    public boolean isRendered();

    /**
     * Register this passed parent as the parent of this component.
     * <p>
     * This will also orphan from any existing parent.
     * 
     * @param parent
     *               the parent.
     */
    public void parent(IParent parent);

    /**
     * Orphans this component from its parent.
     */
    public void orphan();

    /**
     * Obtains the parent (if registered).
     * 
     * @return the parent.
     */
    public IParent getParent();

    /**
     * Convenience to apply the passed command applier to this component and then
     * return the component.
     * <p>
     * This is quite useful for inline component creation and configration.
     * 
     * @param <C>     the component type.
     * @param applier
     *                the applier to apply to this component.
     * @return this component instance.
     */
    @SuppressWarnings("unchecked")
    default public <C extends IComponent> C apply(Consumer<C> applier) {
        if (applier != null)
            applier.accept ((C) this);
        return (C) this;
    }

    /**
     * Anything that can act as a parent to a component.
     */
    public interface IParent {

        /**
         * Orphans the passed child (assumed to be a child of this parent).
         * <p>
         * The only expectation is that the parent removes the child component from its
         * scope.
         * 
         * @param child
         *              the child to orphan.
         */
        public void orhpan(IComponent child);

        /**
         * Determines if the parent is attached (to the DOM).
         * 
         * @return {@code true} if it is.
         */
        public boolean isAttached();

        /**
         * Returns a test ID relative to the parent.
         * 
         * @return the test ID.
         */
        public String getTestId();
    }
}
