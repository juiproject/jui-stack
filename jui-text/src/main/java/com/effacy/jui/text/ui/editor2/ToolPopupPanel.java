package com.effacy.jui.text.ui.editor2;

import com.effacy.jui.core.client.component.SimpleComponent;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;

/**
 * Base class for lightweight floating popup panels anchored to a DOM element.
 * <p>
 * Provides the shared infrastructure used by popup panels such as
 * {@link LinkPanel} and {@link VariablePanel}: singleton tracking (at most one
 * popup open at a time), fixed positioning below an anchor element,
 * outside-click dismissal, and cleanup.
 * <p>
 * Extends {@link SimpleComponent} so that JUI's event dispatch system
 * ({@link com.effacy.jui.core.client.dom.EventLifecycle}) is available to DOM
 * builder event handlers within the panel content.
 * <p>
 * Subclasses implement {@link #buildContent(ElementBuilder)} to populate the
 * panel and optionally override {@link #onShown()} for post-show actions (e.g.
 * focusing an input or initialising dynamic content).
 */
public abstract class ToolPopupPanel extends SimpleComponent {

    /**
     * Singleton tracking (at most one popup open at a time).
     */
    private static ToolPopupPanel currentPopup;

    /**
     * Hides the currently visible popup if any.
     */
    public static void hideCurrent() {
        if (currentPopup != null)
            currentPopup.hide();
    }

    /**
     * Instance state.
     */
    private elemental2.dom.EventListener dismissListener;

    /************************************************************************
     * Construction and rendering.
      ************************************************************************/
    protected ToolPopupPanel() {
        renderer(root -> buildContent(root), null);
    }

    /************************************************************************
     * Show / hide.
     ************************************************************************/

    /**
     * Shows this popup below the given anchor element. Any currently open
     * popup is hidden first.
     *
     * @param anchor
     *               the element to position below.
     */
    protected void show(Element anchor) {
        hideCurrent();
        currentPopup = this;
        render(DomGlobal.document.body, -1);
        attach();
        getRoot().addEventListener("mousedown", evt -> evt.stopPropagation());
        positionBelow(anchor);
        DomGlobal.setTimeout(args -> {
            installDismiss();
            onShown();
        }, 0);
    }

    /**
     * Subclasses populate the panel element here. Called once during rendering
     * before the panel is appended to the document.
     *
     * @param root
     *             the builder for the panel's root element.
     */
    protected abstract void buildContent(ElementBuilder root);

    /**
     * Called after the panel is shown and the dismiss listener is installed.
     * Subclasses can override to focus an input, initialise dynamic content,
     * or perform other post-show actions. Default implementation does nothing.
     */
    protected void onShown() {}

    /**
     * Hides and destroys this popup, removing any document-level listeners.
     */
    public void hide() {
        if (dismissListener != null) {
            DomGlobal.document.removeEventListener("mousedown", dismissListener);
            dismissListener = null;
        }
        Element root = getRoot();
        if (root != null) {
            if (root.parentNode != null)
                root.parentNode.removeChild(root);
        }
        if (currentPopup == this)
            currentPopup = null;
    }

    /************************************************************************
     * Positioning and dismiss.
     ************************************************************************/

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
    private static class JsRect {
        public double left, top, bottom;
    }

    private void positionBelow(Element anchor) {
        JsRect rect = Js.uncheckedCast(anchor.getBoundingClientRect());
        elemental2.dom.HTMLElement rootHtml = Js.uncheckedCast(getRoot());
        rootHtml.style.setProperty("left", rect.left + "px");
        rootHtml.style.setProperty("top", (rect.bottom + 4) + "px");
    }

    private void installDismiss() {
        Element root = getRoot();
        dismissListener = evt -> {
            Element target = Js.uncheckedCast(evt.target);
            Element el = target;
            while (el != null) {
                if (el == root)
                    return;
                el = el.parentElement;
            }
            hide();
        };
        DomGlobal.document.addEventListener("mousedown", dismissListener);
    }
}
