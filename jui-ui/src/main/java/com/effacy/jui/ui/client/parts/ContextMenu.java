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
package com.effacy.jui.ui.client.parts;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.dom.ActivationHandler;
import com.effacy.jui.core.client.dom.IDomSelectable;
import com.effacy.jui.core.client.dom.IDomSelector;
import com.effacy.jui.core.client.dom.IUIEventHandler;
import com.effacy.jui.core.client.dom.UIEvent;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.renderer.template.Condition;
import com.effacy.jui.core.client.dom.renderer.template.Provider;
import com.effacy.jui.core.client.dom.renderer.template.ProviderBuilder;
import com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Container;
import com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Node;
import com.effacy.jui.core.client.dom.renderer.template.items.BuilderItem;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.ui.client.gallery.item.PanelGalleryItem.LocalCSS;

import elemental2.dom.Element;

/**
 * A {@link BuilderItem} that generates a context menu.
 * <p>
 * Builders that include this must supply CSS that implements
 * {@link IContextMenuCSS}, provide the mechanism to activate the context menu
 * (by adding the {@link IContextMenuCSS#open()} CSS class to the root of the
 * context menu) and bind a {@link UIEventType#ONCLICK} handler to the menu
 * items to process clicks (these are tagged with the ID {@link #ID_MENU_ITEM}).
 * <p>
 * As a convenience one may extend {@link ContextMenu.EventHandler} and register
 * that against the enveloping component. This will assume the existence of a
 * single context menu (though not require one to be rendered) with an activator
 * tagged with the ID {@link ContextMenu.EventHandler#BY_MENU_ACTIVATOR}. The
 * event handler will bind to the activator element as well as the root of the
 * context menu. These will be coupled with an instance of
 * {@link ActivationHandler} to provide the activation logic for the menu. It
 * will also bind to all the menu items and when clicked will invoke
 * {@link ContextMenu.EventHandler#onMenuItemClicked(String)}. This is expected
 * to be overridden to handle the click action. In the case that click handlers
 * have been assigned then it should perform a suitable lookup (factoring in the
 * handler prefix) of the handler in the template metadata and invoke passing
 * the relevant context information (the parameter type <code>V</code>).
 *
 * @author Jeremy Buckley
 * @param <D>
 *            the data type that will be used to configure the template output
 *            during rendering.
 * @param <V>
 *            the value type of the click handler (this is the type returned by
 *            {@link UIEvent#getSource()} when a click event occurs).
 */
public class ContextMenu<D, V> extends BuilderItem<D> {

    /**
     * The reference to apply to the element that should activate the menu (must be
     * applied externally).
     */
    public static final String BY_MENU_ACTIVATOR = "contextmenu-activator";

    /**
     * The reference to the context menu for selection.
     */
    protected static final String BY_MENU = "contextmenu";

    /**
     * CSS contract expected by the context menu.
     */
    public interface IContextMenuCSS extends IComponentCSS {

        public static final String CSS = "com/effacy/jui/ui/client/parts/ContextMenu.css";

        public String menu();

        public String open();
    }

    /**
     * Event handler for handling opening and closing of the menu. This is hooked in
     * as the actual DOM for this will reside outside of the menu itself (which is
     * just the selector part).
     */
    static class MenuActivatorEventHandler implements IUIEventHandler, IDomSelectable {

        /**
         * The element used to activate the context menu.
         */
        protected Element contextMenuActivatorEl;
        /**
         * The context menu.
         */
        protected Element contextMenuEl;

        /**
         * Manages the activation of the context menu.
         */
        protected ActivationHandler menuHandler;

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.dom.IDomSelectable#select(com.effacy.jui.core.client.dom.IDomSelector)
         */
        @Override
        public void select(IDomSelector selector) {
            contextMenuActivatorEl = selector.first (BY_MENU_ACTIVATOR);
            UIEventType.ONCLICK.attach (contextMenuActivatorEl);

            contextMenuEl = selector.first (BY_MENU);
            UIEventType.ONCLICK.attach (contextMenuEl);
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.dom.IUIEventHandler#handleEvent(com.effacy.jui.core.client.dom.UIEvent)
         */
        @Override
        public boolean handleEvent(UIEvent event) {
            // Check for a click on the activator.
            if (event.matches (contextMenuActivatorEl, UIEventType.ONCLICK)) {
                event.stopEvent ();
                if (menuHandler == null)
                    menuHandler = new ActivationHandler (contextMenuActivatorEl, contextMenuEl, LocalCSS.instance ().open ());
                menuHandler.toggle ();
                return true;
            }

            // Check for a click in the menu (then hide the activator);
            if (event.matches (contextMenuEl, UIEventType.ONCLICK)) {
                if ((menuHandler != null) && menuHandler.isOpen ())
                    menuHandler.close ();
                // We deliberately pass this through to be processed by the click handler.
                return false;
            }
            return false;
        }

    }

    protected IContextMenuCSS styles;

    /**
     * The positioning of the menu.
     */
    protected Insets position;

    /**
     * Menu items (see {@link #item(String)}.
     */
    protected List<MenuItem> items = new ArrayList<> ();

    /**
     * Construct with styles.
     * 
     * @param styles
     *               the styles.
     */
    public ContextMenu(IContextMenuCSS styles) {
        this.styles = styles;
    }

    /**
     * Construct with styles.
     * 
     * @param styles
     *                 the styles.
     * @param position
     *                 the relative position to the containing element.
     */
    public ContextMenu(IContextMenuCSS styles, Insets position) {
        this.styles = styles;
        this.position = position;
    }

    /**
     * Determines if there are no items configured against the item.
     * 
     * @return {@code true} if there are none.
     */
    public boolean isEmpty() {
        return items.isEmpty ();
    }

    /**
     * Adds a menu item to the menu.
     * 
     * @param label
     *              the label of the item.
     * @return the menu item for further configuration.
     */
    public MenuItem item(String label) {
        MenuItem item = new MenuItem (label);
        items.add (item);
        return item;
    }

    /**
     * Adds a menu item to the menu.
     * 
     * @param label
     *              the label of the item.
     * @param item
     *              (optional) a configurer for the item.
     * @return this content menu instance.
     */
    public ContextMenu<D, V> item(String label, Consumer<MenuItem> item) {
        MenuItem _item = item (label);
        if (item != null)
            item.accept (_item);
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.dom.renderer.template.items.BuilderItem#buildImpl(com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Container)
     */
    @Override
    protected Node<D> buildImpl(Container<D> parent) {
        return parent.div (menu -> {
            menu.onBuild (ctx -> {
                ctx.register (new MenuActivatorEventHandler ());
            });
            menu.id ("contextmenu").by (BY_MENU);
            menu.addClassName (styles.menu ());
            menu.ul (ul -> {
                if (position != null)
                    position.postion (ul);
                items.forEach (item -> item.build (ul));
            });
        });
    }

    /************************************************************************
     * Contents
     ************************************************************************/

    /**
     * A single menu item.
     */
    public class MenuItem extends BuilderItem<D> {

        /**
         * Item label (see constructors).
         */
        protected Provider<String, D> label;

        /**
         * See {@link #icon(String)}.
         */
        protected Provider<String, D> icon;

        /**
         * See {@link #disabled(Condition)}.
         */
        protected Condition<D> disabled;

        /**
         * See {@link #clickHandler(Consumer)}.
         */
        private Consumer<V> clickHandler;

        /**
         * Construct with a label for the item.
         * 
         * @param label
         *              the label.
         */
        public MenuItem(String label) {
            this (ProviderBuilder.string (label));
        }

        /**
         * Construct with a label for the item.
         * 
         * @param reference
         *                  the unique reference to the item.
         * @param label
         *                  the label.
         */
        public MenuItem(Provider<String, D> label) {
            this.label = label;
        }

        /**
         * An optional icon to display along with the label.
         * 
         * @param icon
         *             the icon CSS class.
         * @return this menu item.
         */
        public MenuItem icon(String icon) {
            return icon (ProviderBuilder.string (icon));
        }

        /**
         * See {@link #icon(String)} but as a provider.
         */
        public MenuItem icon(Provider<String, D> icon) {
            this.icon = icon;
            return this;
        }

        /**
         * Condition to disable the action.
         * 
         * @param disabled
         *                 the disabling condition.
         * @return this menu item.
         */
        public MenuItem disabled(Condition<D> disabled) {
            this.disabled = disabled;
            return this;
        }

        /**
         * Handler to invoke when the item is clicked.
         * 
         * @param clickHandler
         *                     the handler.
         * @return this menu item.
         */
        public MenuItem clickHandler(Consumer<V> clickHandler) {
            this.clickHandler = clickHandler;
            return this;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.dom.renderer.template.items.BuilderItem#buildImpl(com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Container)
         */
        @Override
        @SuppressWarnings("unchecked")
        protected Node<D> buildImpl(Container<D> parent) {
            return parent.li (li -> {
                li.by ("menu-item");
                if (clickHandler != null)
                    li.on (e -> clickHandler.accept ((V) e.getSource ()), 1, UIEventType.ONCLICK);
                li.addClassName (ProviderBuilder.string (styles.disabled (), disabled));
                if (icon != null) {
                    li.em (ico -> {
                        ico.condition (d -> !StringSupport.empty (icon.get (d)));
                        ico.addClassName (icon);
                    });
                }
                li.span (span -> {
                    span.text (label);
                });
            });
        }

    }
}
