/*******************************************************************************
 * Copyright 2026 Jeremy Buckley
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
package com.effacy.jui.ui.client.navigation;

import java.util.List;

/**
 * Draws a navigator's navigation somewhere the navigator does not own.
 * <p>
 * A navigator normally renders its own tab strip inside itself. Sometimes it
 * should appear elsewhere — in the page header above it, in a toolbar, in a
 * sidebar, behind an overflow menu on a narrow screen. This is how: the
 * navigator keeps the navigation (the set, activation, deep linking, handler
 * chaining) and delegates the <b>drawing</b> of it to whatever implements this.
 * <p>
 * <b>Nothing is shared but data.</b> The presenter builds its own DOM in its own
 * component, so its handlers dispatch to it, its styling is its own, and its
 * lifecycle is its own. That is the whole reason for the interface: the
 * alternative — handing an element across a component boundary — puts the DOM in
 * one component's tree and the event registration in another's, and JUI
 * dispatches to the <em>nearest ancestor</em> carrying a listener
 * ({@code EventLifecycle}), so the clicks would go to the host and be dropped.
 * <p>
 * <b>Three rules, and a presenter that breaks them looks fine and is not.</b>
 * <ul>
 * <li><b>Never mark yourself active on click.</b> Activation is asynchronous and
 * may be refused — a component still loading, an editor with unsaved changes.
 * Ask, by calling {@link IHandler#navigate(String)}, and wait to be told what
 * happened through {@link #activate(String)}.
 * <li><b>{@link #activate(String)} arrives for navigation you did not cause</b> —
 * a deep link, a breadcrumb, a call in code, a child handler propagating back
 * up. Wiring only the click path leaves the strip correct until the first deep
 * link and stale from then on.
 * <li><b>Be idempotent.</b> {@link #activate(String)} for what is already active
 * must do nothing and must not call back.
 * </ul>
 * <p>
 * The built-in strip is itself written against this interface, which is the only
 * real check that the interface is expressive enough — see
 * {@code TabNavigator.DefaultNavigationPresenter}.
 */
public interface INavigationPresenter {

    /**
     * One navigable item.
     *
     * @param reference
     *                  identifies it to the navigator; what
     *                  {@link IHandler#navigate(String)} and
     *                  {@link INavigationPresenter#activate(String)} carry.
     * @param label
     *                  the display label.
     * @param icon
     *                  an icon CSS class, or {@code null}.
     * @param indicator
     *                  a short marker rendered alongside, or {@code null}.
     * @param count
     *                  a count to display, or negative for none.
     * @param enabled
     *                  {@code false} where the item should be shown but not
     *                  selectable.
     * @param group
     *                  the index of the {@link Group} it belongs to.
     */
    public record Item(String reference, String label, String icon, String indicator, int count, boolean enabled, int group) {}

    /**
     * A group of items.
     * <p>
     * A presenter is free to ignore the grouping entirely — a chip strip in a
     * header usually should — but it must then still render every item.
     *
     * @param index
     *               the group's index within the navigator.
     * @param label
     *               the group heading, or {@code null}.
     * @param icon
     *               an icon CSS class for the heading, or {@code null}.
     * @param silent
     *               {@code true} where the group carries no heading of its own.
     * @param expand
     *               {@code true} where space should be taken before the group.
     * @param first
     *               {@code true} for the first non-silent group.
     * @param items
     *               the items, in order.
     */
    public record Group(int index, String label, String icon, boolean silent, boolean expand, boolean first, List<Item> items) {}

    /**
     * How a presenter asks for navigation. Supplied by {@link #bind(IHandler)}.
     */
    @FunctionalInterface
    public interface IHandler {

        /**
         * Requests navigation to the given item.
         * <p>
         * A request, not an instruction: it may be refused or deferred, and the
         * outcome arrives (if it arrives) as {@link #activate(String)}.
         *
         * @param reference
         *                  the item to navigate to.
         */
        public void navigate(String reference);
    }

    /**
     * Binds the presenter to a navigator. Called before the first
     * {@link #present(List)}.
     *
     * @param handler
     *                how to ask the navigator for navigation.
     */
    public void bind(IHandler handler);

    /**
     * Instructs the presenter to draw this set of items.
     * <p>
     * Called on first render and again whenever the set changes shape. It may
     * arrive <b>before the presenter has rendered</b> — the navigator does not
     * know or care — so hold it and draw on render rather than assuming a DOM.
     *
     * @param groups
     *               the items, grouped, in order.
     */
    public void present(List<Group> groups);

    /**
     * Instructs the presenter to show the given item as the active one, and every
     * other as not.
     *
     * @param reference
     *                  the active item, or {@code null} where none is.
     */
    public void activate(String reference);

    /**
     * One item has changed — its count, label, icon or enabled state.
     * <p>
     * A no-op by default, which is right for a presenter showing only the active
     * item and wrong for one drawing the whole set: ignore it there and counts and
     * disabled states will be whatever they were at the last {@link #present(List)}
     * for ever after.
     *
     * @param item
     *             the item in its new state.
     */
    default public void update(Item item) {
        // Nothing.
    }

    /**
     * The navigator is going away. Anything drawn should be cleared: the presenter
     * may well outlive the navigator, and a strip left behind navigates nowhere.
     */
    default public void unbind() {
        // Nothing.
    }
}
