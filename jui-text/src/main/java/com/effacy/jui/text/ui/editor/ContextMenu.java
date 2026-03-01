package com.effacy.jui.text.ui.editor;

import java.util.ArrayList;
import java.util.List;

import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.platform.css.client.CssResource;
import com.google.gwt.core.client.GWT;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;

/**
 * A lightweight, self-managing floating context menu backed purely by
 * elemental2 DOM.
 * <p>
 * Configure with {@link #item} / {@link #sep} calls, then show with
 * {@link #showRight} or {@link #showBelow}:
 * <pre>
 *   new ContextMenu()
 *       .item("Insert row above", () -> { ... })
 *       .item("Insert row below", () -> { ... })
 *       .sep()
 *       .item("Delete row", () -> { ... })
 *       .showRight(handleElement);
 * </pre>
 * <p>
 * At most one {@code ContextMenu} is visible at a time; showing a new menu
 * automatically hides the previously open one. The menu dismisses itself when
 * the user clicks outside it or activates an item.
 */
public class ContextMenu {

    /************************************************************************
     * Static single-instance tracking (at most one menu open at a time).
     ************************************************************************/

    /** The currently visible menu, or {@code null}. */
    private static ContextMenu currentMenu;

    /**
     * Hides the currently visible menu if any. Safe to call when no menu is
     * open.
     */
    public static void hideCurrent() {
        if (currentMenu != null)
            currentMenu.hide();
    }

    /************************************************************************
     * Instance state.
     ************************************************************************/

    /**
     * Deferred builders accumulated via {@link #item} / {@link #sep} calls.
     * Each runnable appends one element to {@link #menuEl} and is executed
     * inside {@link #show} after the menu element is created.
     */
    private List<Runnable> builders = new ArrayList<>();

    /** The live menu DOM element, or {@code null} when hidden. */
    private elemental2.dom.Element menuEl;

    /** Document-level listener that auto-dismisses on outside click. */
    private elemental2.dom.EventListener dismissListener;

    /************************************************************************
     * Builder API.
     ************************************************************************/

    /**
     * Appends a clickable item to the menu.
     *
     * @param label  the visible label.
     * @param action the action to run when the item is clicked.
     * @return {@code this} for chaining.
     */
    public ContextMenu item(String label, Runnable action) {
        builders.add(() -> appendItem(label, action));
        return this;
    }

    /**
     * Appends a horizontal separator to the menu.
     *
     * @return {@code this} for chaining.
     */
    public ContextMenu sep() {
        builders.add(this::appendSep);
        return this;
    }

    /************************************************************************
     * Show / hide.
     ************************************************************************/

    /**
     * Shows the menu to the right of {@code anchor}.
     *
     * @param anchor the element to anchor the menu to.
     */
    public void showRight(elemental2.dom.Element anchor) {
        show(anchor, false);
    }

    /**
     * Shows the menu below {@code anchor}.
     *
     * @param anchor the element to anchor the menu to.
     */
    public void showBelow(elemental2.dom.Element anchor) {
        show(anchor, true);
    }

    /**
     * Hides and destroys the menu, removing any document-level listeners.
     * Safe to call when the menu is already hidden.
     */
    public void hide() {
        if (dismissListener != null) {
            DomGlobal.document.removeEventListener("mousedown", dismissListener);
            dismissListener = null;
        }
        if (menuEl != null) {
            if (menuEl.parentNode != null)
                menuEl.parentNode.removeChild(menuEl);
            menuEl = null;
        }
        if (currentMenu == this)
            currentMenu = null;
    }

    /************************************************************************
     * Internal helpers.
     ************************************************************************/

    private void show(elemental2.dom.Element anchor, boolean below) {
        // Hide any other open menu first.
        hideCurrent();
        currentMenu = this;

        // Build the menu element.
        menuEl = DomGlobal.document.createElement("div");
        menuEl.classList.add(styles().contextMenu());
        builders.forEach(Runnable::run);

        DomGlobal.document.body.appendChild(menuEl);
        positionMenu(anchor, below);
        installDismiss();
    }

    private void appendItem(String label, Runnable action) {
        Element item = DomGlobal.document.createElement("div");
        item.classList.add(styles().item());
        item.textContent = label;
        item.addEventListener("mousedown", evt -> {
            evt.preventDefault();
            evt.stopPropagation();
            hide();
            action.run();
        });
        menuEl.appendChild(item);
    }

    private void appendSep() {
        Element sep = DomGlobal.document.createElement("div");
        sep.classList.add(styles().separator());
        menuEl.appendChild(sep);
    }

    /**
     * Native overlay for reading the viewport-relative bounding rectangle
     * returned by {@code Element.getBoundingClientRect()}.
     */
    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
    private static class JsRect {
        public double left, top, right, bottom;
    }

    private void positionMenu(elemental2.dom.Element anchor, boolean below) {
        JsRect rect = Js.uncheckedCast(anchor.getBoundingClientRect());
        elemental2.dom.HTMLElement menuHtml = Js.uncheckedCast(menuEl);
        double left, top;
        if (below) {
            left = rect.left;
            top  = rect.bottom + 4;
        } else {
            left = rect.right + 4;
            top  = rect.top;
        }
        menuHtml.style.setProperty("left", left + "px");
        menuHtml.style.setProperty("top",  top  + "px");
    }

    private void installDismiss() {
        dismissListener = evt -> {
            elemental2.dom.Element target = Js.uncheckedCast(evt.target);
            elemental2.dom.Element el = target;
            while (el != null) {
                if (el == menuEl)
                    return;
                el = el.parentElement;
            }
            hide();
        };
        DomGlobal.document.addEventListener("mousedown", dismissListener);
    }

    /************************************************************************
     * CSS.
     ************************************************************************/

    private IContextMenuCSS styles() {
        return ContextMenuCSS.instance();
    }

    public static interface IContextMenuCSS extends IComponentCSS {

        String contextMenu();

        String item();

        String separator();
    }

    @CssResource(value = {
        IComponentCSS.COMPONENT_CSS
    }, stylesheet = """
        .contextMenu {
            position: fixed;
            background: #fff;
            border: 1px solid #e5e7eb;
            border-radius: 6px;
            box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12), 0 1px 4px rgba(0, 0, 0, 0.06);
            padding: 4px 0;
            min-width: 180px;
            z-index: 10000;
            font-size: 0.875em;
            color: #374151;
        }
        .contextMenu .item {
            padding: 6px 14px;
            cursor: pointer;
            user-select: none;
            white-space: nowrap;
        }
        .contextMenu .item:hover {
            background: #eff6ff;
            color: #1d4ed8;
        }
        .contextMenu .separator {
            height: 1px;
            background: #e5e7eb;
            margin: 4px 0;
        }
    """)
    public static abstract class ContextMenuCSS implements IContextMenuCSS {

        private static ContextMenuCSS STYLES;

        public static IContextMenuCSS instance() {
            if (STYLES == null) {
                STYLES = (ContextMenuCSS) GWT.create(ContextMenuCSS.class);
                STYLES.ensureInjected();
            }
            return STYLES;
        }
    }
}
