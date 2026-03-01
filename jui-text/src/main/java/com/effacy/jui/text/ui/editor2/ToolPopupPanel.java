package com.effacy.jui.text.ui.editor2;

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
 * Subclasses implement {@link #buildContent(Element)} to populate the panel
 * and optionally override {@link #onShown()} for post-show actions (e.g.
 * focusing an input).
 */
public abstract class ToolPopupPanel {

    /************************************************************************
     * Singleton tracking (at most one popup open at a time).
     ************************************************************************/

    private static ToolPopupPanel currentPopup;

    /**
     * Hides the currently visible popup if any.
     */
    public static void hideCurrent() {
        if (currentPopup != null)
            currentPopup.hide();
    }

    /************************************************************************
     * Instance state.
     ************************************************************************/

    private Element panelEl;
    private elemental2.dom.EventListener dismissListener;

    /**
     * Returns the panel's root element, or {@code null} if the panel is not
     * currently shown.
     */
    protected Element panelEl() {
        return panelEl;
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
        panelEl = DomGlobal.document.createElement("div");
        buildContent(panelEl);
        panelEl.addEventListener("mousedown", evt -> evt.stopPropagation());
        DomGlobal.document.body.appendChild(panelEl);
        positionBelow(anchor);
        DomGlobal.setTimeout(args -> {
            installDismiss();
            onShown();
        }, 0);
    }

    /**
     * Subclasses populate the panel element here. Called once during
     * {@link #show(Element)} before the panel is appended to the document.
     *
     * @param panel
     *              the panel's root element.
     */
    protected abstract void buildContent(Element panel);

    /**
     * Called after the panel is shown and the dismiss listener is installed.
     * Subclasses can override to focus an input or perform other post-show
     * actions. Default implementation does nothing.
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
        if (panelEl != null) {
            if (panelEl.parentNode != null)
                panelEl.parentNode.removeChild(panelEl);
            panelEl = null;
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
        elemental2.dom.HTMLElement panelHtml = Js.uncheckedCast(panelEl);
        panelHtml.style.setProperty("left", rect.left + "px");
        panelHtml.style.setProperty("top", (rect.bottom + 4) + "px");
    }

    private void installDismiss() {
        dismissListener = evt -> {
            Element target = Js.uncheckedCast(evt.target);
            Element el = target;
            while (el != null) {
                if (el == panelEl)
                    return;
                el = el.parentElement;
            }
            hide();
        };
        DomGlobal.document.addEventListener("mousedown", dismissListener);
    }
}
