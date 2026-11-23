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
package com.effacy.jui.ui.client.fragments;

import com.effacy.jui.core.client.dom.ActivationHandler;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.IDomInsertable;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.ui.client.fragments.Menu.MenuFragment;
import com.effacy.jui.ui.client.icon.FontAwesome;

import elemental2.dom.Element;
import elemental2.dom.Event;
import elemental2.dom.HTMLLabelElement;
import jsinterop.base.Js;

public class MenuActivator {

    public static MenuActivatorFragment $() {
        return new MenuActivatorFragment ();
    }

    public static MenuActivatorFragment $(IDomInsertableContainer<?> parent) {
        MenuActivatorFragment frg = $ ();
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    /************************************************************************
     * Implementation
     ************************************************************************/

    public static class MenuActivatorFragment extends AMenuActivatorFragment<MenuActivatorFragment> {}

    public static class AMenuActivatorFragment<T extends AMenuActivatorFragment<T>> extends BaseFragmentWithChildren<T> {

        /**
         * See {@link #clickToActivate()}.
         */
        private boolean clickToActivate;

        /**
         * See {@link #aboveThreshold(int)}.
         */
        private int aboveThreshold = 100;

        /**
         * See {@link #aboveForced(boolean)}.
         */
        private boolean aboveForced = false;

        /**
         * Internal. Holds the activator.
         */
        private ActivationHandler handler;

        /**
         * The default is that the menu opens on mouse-over. However, it can be
         * configured to open on a click.
         * <p>
         * When click-activated any click within the activator that no menu item
         * handles toggles the menu. This includes clicks on native content placed in
         * the menu (such as links and inputs), which keep their default action.
         * A click on a label whose control is in the menu toggles the menu once (the
         * browser passes the click on to the control and only that click toggles).
         * 
         * @return this fragment instance.
         */
        @SuppressWarnings("unchecked")
        public T clickToActivate() {
            this.clickToActivate = true;
            return (T)this;
        }

        /**
         * Convenience to call {@link #aboveForced(boolean)} passing {@code true}.
         * 
         * @return this fragment instance.
         */
        public T aboveForced() {
            return aboveForced(true);
        }

        /**
         * Forces the menu to appear above the activator.
         * <p>
         * This will override any value assigned to {@link #aboveThreshold(int)}.
         * 
         * @param aboveForced
         *                    {@code true} to force display above.
         * @return this fragment instance.
         */
        @SuppressWarnings("unchecked")
        public T aboveForced(boolean aboveForced) {
            this.aboveForced = aboveForced;
            return (T)this;
        }

        /**
         * When the menu activator is with 100px of the bottom of the enclosing scroller
         * then menu will be displayed above the activator. This allows to to adjust
         * that threshold, but also allows you to disable it by setting a value that is
         * zero (or less).
         * <p>
         * Note that when displaying above the menu is offset above the bottom to clear
         * the height of the activator. This is configured in CSS with the variable
         * {@code --juiMenuActivator-aboveOffset}.
         * 
         * @param aboveThreshold
         *                       the activation threshold in px to situate the menu
         *                       above the activator.
         * @return this fragment instance.
         */
        @SuppressWarnings("unchecked")
        public T aboveThreshold(int aboveThreshold) {
            this.aboveThreshold = aboveThreshold;
            return (T)this;
        }

        @Override
        protected void buildInto(ElementBuilder root) {
            root.style ("juiMenuActivator");
            if (aboveForced)
                root.style ("above");
            if (clickToActivate) {
                root.apply (n -> {
                    if (!aboveForced && (aboveThreshold > 0))
                        handler = new ActivationHandler ((Element) n, (Element) n, "open", "above", aboveThreshold);
                    else
                        handler = new ActivationHandler ((Element) n, (Element) n, "open");
                });
            }
            // Clicks within that no item handles toggle a click-activated menu and are
            // kept from what contains the activator (such as a clickable table row).
            // They are not claimed (which would prevent their default action) so links
            // and inputs placed in the menu keep working.
            root.apply (n -> ((Element) n).addEventListener ("click", e -> {
                if (clickToActivate && !passedToLabelControl ((Element) n, e))
                    handler.toggle ();
                e.stopPropagation ();
            }));
            Em.$ (root).style ("activator", FontAwesome.ellipsisV ());
            Div.$ (root).$ (container -> {
                if (!clickToActivate)
                    container.style ("hoverable");
                super.buildInto (container);
            });
        }

        /**
         * Determines if the click is on a label whose activation the browser passes on
         * (as a second click) to the label's control within the activator. Only that
         * second click should toggle the menu (otherwise the menu toggles twice and
         * stays open).
         * 
         * @param activator
         *                  the activator element.
         * @param e
         *                  the click event.
         * @return {@code true} if the click will be passed on.
         */
        protected boolean passedToLabelControl(Element activator, Event e) {
            if (!(e.target instanceof Element))
                return false;
            Element target = (Element) e.target;
            Element label = target.closest ("label");
            if ((label == null) || !activator.contains (label))
                return false;
            Element control = Js.<HTMLLabelElement>uncheckedCast (label).control;
            if ((control == null) || !activator.contains (control))
                return false;
            // The browser does not pass on clicks on interactive content within the
            // label (which includes the control itself), nor clicks to a disabled
            // control.
            Element interactive = target.closest ("a,button,input,select,textarea");
            if ((interactive != null) && label.contains (interactive))
                return false;
            return !Js.isTruthy (Js.asPropertyMap (control).get ("disabled"));
        }

        @Override
        protected void onInsertChild(IDomInsertable child) {
            if (child instanceof MenuFragment) {
                // A hover menu has no handler (it closes when the pointer leaves).
                ((MenuFragment)child).menuItemClicked = (() -> {
                    if (handler != null)
                        handler.close ();
                });
            }
        }
        
    }
}
