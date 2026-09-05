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
package com.effacy.jui.text.ui.type;

import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.platform.css.client.CssResource;
import com.google.gwt.core.client.GWT;

import elemental2.dom.DOMRect;
import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.EventListener;
import elemental2.dom.HTMLElement;
import elemental2.dom.KeyboardEvent;
import elemental2.dom.MouseEvent;
import elemental2.dom.Node;
import elemental2.dom.WheelEvent;
import jsinterop.base.Js;

/**
 * Opens a piece of rendered content — a diagram, in practice — enlarged over the page,
 * with zoom and pan.
 * <p>
 * A diagram in a document is sized to the column it sits in, which is the right size for
 * reading around and frequently the wrong size for reading. This is the way in to the
 * second: {@link #enable(Element)} marks a rendered element as openable (it takes a
 * {@code zoom-in} cursor and a click), and {@link #open(Element)} puts a <b>copy</b> of it
 * on a full-page surface scaled to fit, from which it can be zoomed and dragged about.
 * <p>
 * A copy, not the element itself: the document underneath is left exactly as it was, so
 * nothing has to be restored on the way out and an asynchronous renderer that is still
 * working on the original is undisturbed. It also means the enlarged view is a snapshot —
 * which is what a reader wants, the content being read-only in the first place.
 * <p>
 * <b>Read-only surfaces only.</b> In an editor a click on a diagram opens its source, and
 * two things cannot own the same gesture. See
 * {@link com.effacy.jui.text.ui.editor.IFenceRenderer#zoomable()}, which is how a fence
 * declares itself worth enlarging, and {@code DomBuilderFormattedTextRenderer}, which is
 * the only caller.
 * <p>
 * One overlay exists at a time and it is built on first use, so the cost of the feature to
 * a page with no diagrams in it is nothing.
 */
public final class ZoomOverlay {

    private ZoomOverlay() {
        // Static.
    }

    /** Zoom bounds. The lower is a whole large diagram; the upper is a label on it. */
    private static final double MIN_SCALE = 0.1;

    private static final double MAX_SCALE = 8.0;

    /** The factor one press of a zoom button applies. */
    private static final double STEP = 1.25;

    /** Movement (px) beyond which a press on the backdrop is a drag rather than a click. */
    private static final double DRAG_SLOP = 3;

    private static HTMLElement overlayEl;

    private static HTMLElement stageEl;

    /** Carries the transform; holds the copy of the content. */
    private static HTMLElement innerEl;

    private static HTMLElement pctEl;

    private static double scale = 1;

    /** The scale at which the content last fitted the stage — where <i>Fit</i> returns to. */
    private static double fitScale = 1;

    private static double panX;

    private static double panY;

    private static boolean dragging;

    /** The pointer moved while down, so the release is the end of a drag and not a click. */
    private static boolean dragged;

    private static double dragFromX;

    private static double dragFromY;

    private static EventListener keyListener;

    private static EventListener moveListener;

    private static EventListener upListener;

    /************************************************************************
     * Opening.
     ************************************************************************/

    /**
     * Marks a rendered element as openable: it takes the zoom cursor, and a click on it
     * opens a copy enlarged.
     * <p>
     * Safe to call on an element whose rendering has not finished — the copy is taken at
     * click time, so whatever the renderer has produced by then is what is shown.
     *
     * @param el
     *           the rendered element ({@code null} is ignored).
     */
    public static void enable(Element el) {
        if (el == null)
            return;
        el.classList.add(styles().zoomable());
        el.addEventListener("click", evt -> {
            evt.preventDefault();
            evt.stopPropagation();
            open(el);
        });
    }

    /**
     * Opens a copy of {@code source} enlarged, scaled to fit the page.
     *
     * @param source
     *               the element to show ({@code null} is ignored).
     */
    public static void open(Element source) {
        if (source == null)
            return;
        build();
        // The size the original occupies on the page. Taken from the original rather than
        // measured off the copy because a rendered diagram's own width is typically a
        // percentage of whatever contained it — put on a shrink-to-fit stage that resolves
        // to nothing useful, whereas the page has already answered the question.
        DOMRect rect = source.getBoundingClientRect();
        innerEl.innerHTML = "";
        innerEl.appendChild(source.cloneNode(true));
        innerEl.style.setProperty("width", rect.width + "px");
        overlayEl.style.setProperty("display", "flex");
        panX = 0;
        panY = 0;
        fitTo(rect.width, rect.height);
        if (keyListener == null) {
            keyListener = evt -> {
                KeyboardEvent ke = Js.uncheckedCast(evt);
                if ("Escape".equals(ke.key))
                    close();
            };
            DomGlobal.document.addEventListener("keydown", keyListener);
        }
    }

    /** Closes the overlay and drops the copy it was showing. */
    public static void close() {
        if (overlayEl == null)
            return;
        endDrag();
        overlayEl.style.setProperty("display", "none");
        innerEl.innerHTML = "";
        if (keyListener != null) {
            DomGlobal.document.removeEventListener("keydown", keyListener);
            keyListener = null;
        }
    }

    /************************************************************************
     * Zoom and pan.
     ************************************************************************/

    /**
     * Scales content of the given size to sit within the stage, and takes that as the
     * <i>Fit</i> scale.
     * <p>
     * Upscaling is allowed rather than capped at 1: a diagram too small to read is exactly
     * the case this feature exists for, and refusing to enlarge it would be refusing the
     * point.
     */
    private static void fitTo(double width, double height) {
        // The allowance covers the surface's own padding either side plus a margin off
        // the edges of the stage, so a fitted diagram sits clear of them rather than
        // flush against them.
        double availableWidth = stageEl.clientWidth - 96;
        double availableHeight = stageEl.clientHeight - 96;
        if ((width <= 0) || (height <= 0) || (availableWidth <= 0) || (availableHeight <= 0))
            fitScale = 1;
        else
            fitScale = clamp(Math.min(availableWidth / width, availableHeight / height));
        scale = fitScale;
        apply();
    }

    /** Zooms by {@code factor} about the middle of the stage. */
    private static void zoom(double factor) {
        DOMRect rect = stageEl.getBoundingClientRect();
        zoomAt(factor, rect.left + (stageEl.clientWidth / 2.0), rect.top + (stageEl.clientHeight / 2.0));
    }

    /**
     * Zooms by {@code factor} keeping the content under ({@code clientX}, {@code clientY})
     * where it is — which is what makes a wheel zoom feel like magnification of the thing
     * being pointed at rather than of the picture as a whole.
     */
    private static void zoomAt(double factor, double clientX, double clientY) {
        double next = clamp(scale * factor);
        if (next == scale)
            return;
        // The transform origin is the middle of the stage, so work in offsets from it.
        DOMRect rect = stageEl.getBoundingClientRect();
        double dx = clientX - (rect.left + (stageEl.clientWidth / 2.0));
        double dy = clientY - (rect.top + (stageEl.clientHeight / 2.0));
        // Hold the content coordinate under the pointer: pan' = d - (d - pan) * next/scale.
        panX = dx - ((dx - panX) * (next / scale));
        panY = dy - ((dy - panY) * (next / scale));
        scale = next;
        apply();
    }

    private static double clamp(double value) {
        return Math.max(MIN_SCALE, Math.min(MAX_SCALE, value));
    }

    private static void apply() {
        innerEl.style.setProperty("transform",
            "translate(" + panX + "px, " + panY + "px) scale(" + scale + ")");
        pctEl.textContent = Math.round(scale * 100) + "%";
    }

    /************************************************************************
     * Dragging.
     ************************************************************************/

    private static void beginDrag(MouseEvent evt) {
        dragging = true;
        dragged = false;
        dragFromX = evt.clientX - panX;
        dragFromY = evt.clientY - panY;
        if (moveListener == null) {
            moveListener = e -> {
                if (!dragging)
                    return;
                MouseEvent me = Js.uncheckedCast(e);
                double x = me.clientX - dragFromX;
                double y = me.clientY - dragFromY;
                if ((Math.abs(x - panX) > DRAG_SLOP) || (Math.abs(y - panY) > DRAG_SLOP))
                    dragged = true;
                panX = x;
                panY = y;
                apply();
            };
            DomGlobal.document.addEventListener("mousemove", moveListener);
        }
        if (upListener == null) {
            upListener = e -> dragging = false;
            DomGlobal.document.addEventListener("mouseup", upListener);
        }
    }

    private static void endDrag() {
        dragging = false;
        if (moveListener != null) {
            DomGlobal.document.removeEventListener("mousemove", moveListener);
            moveListener = null;
        }
        if (upListener != null) {
            DomGlobal.document.removeEventListener("mouseup", upListener);
            upListener = null;
        }
    }

    /************************************************************************
     * Construction.
     ************************************************************************/

    /** Builds the overlay on first use and appends it to {@code body}. */
    private static void build() {
        if (overlayEl != null)
            return;
        overlayEl = div(styles().zoomOverlay());

        HTMLElement bar = div(styles().zoomBar());
        bar.appendChild(button("−", "Zoom out", () -> zoom(1 / STEP)));
        pctEl = div(styles().zoomPct());
        pctEl.textContent = "100%";
        bar.appendChild(pctEl);
        bar.appendChild(button("+", "Zoom in", () -> zoom(STEP)));
        bar.appendChild(button("Fit", "Fit to the page", () -> {
            panX = 0;
            panY = 0;
            scale = fitScale;
            apply();
        }));
        bar.appendChild(button("✕", "Close", ZoomOverlay::close));
        overlayEl.appendChild(bar);

        stageEl = div(styles().zoomStage());
        innerEl = div(styles().zoomInner());
        stageEl.appendChild(innerEl);
        overlayEl.appendChild(stageEl);

        stageEl.addEventListener("mousedown", evt -> {
            evt.preventDefault();
            MouseEvent me = Js.uncheckedCast(evt);
            beginDrag(me);
        });
        // Clicking the backdrop dismisses — the ordinary way out of a lightbox. Two things
        // are excluded from that, and both are about not fighting the pan: a press that
        // moved (it was a drag, not a click), and a press that landed on the content
        // itself (grabbing the diagram to move it is not a request to close it).
        stageEl.addEventListener("click", evt -> {
            if (dragged)
                return;
            Node target = Js.uncheckedCast(evt.target);
            if ((target != null) && innerEl.contains(target))
                return;
            close();
        });
        stageEl.addEventListener("wheel", evt -> {
            WheelEvent we = Js.uncheckedCast(evt);
            // The page behind must not scroll; this surface owns the gesture.
            we.preventDefault();
            zoomAt((we.deltaY < 0) ? STEP : (1 / STEP), we.clientX, we.clientY);
        });

        DomGlobal.document.body.appendChild(overlayEl);
    }

    private static HTMLElement div(String style) {
        HTMLElement el = Js.uncheckedCast(DomGlobal.document.createElement("div"));
        el.classList.add(style);
        return el;
    }

    private static HTMLElement button(String label, String title, Runnable action) {
        HTMLElement el = Js.uncheckedCast(DomGlobal.document.createElement("button"));
        el.classList.add(styles().zoomBtn());
        el.textContent = label;
        el.setAttribute("title", title);
        el.setAttribute("type", "button");
        el.addEventListener("click", evt -> {
            evt.stopPropagation();
            action.run();
        });
        return el;
    }

    /************************************************************************
     * CSS.
     *
     * Appended to body, outside any component's scoped DOM, so it carries its own
     * injected sheet — the same arrangement as the editor's floating affordances
     * (see EditorOverlayCSS). Colours are token-driven with literal fallbacks so
     * the surface follows a theme without needing one.
     *
     * Three conventions here are not decoration:
     *
     *  - Class names are namespaced (zoomOverlay, not overlay). These names are
     *    global — the sheet is not scoped by an enclosing .component — so a name
     *    like "overlay", "bar" or "btn" is an invitation to collide with whatever
     *    else is on the page. EditorOverlayCSS names its own imgOverlay/linkCard
     *    for the same reason.
     *
     *  - Colour fallbacks are hex-with-alpha (#0f172aeb) rather than rgba(), so no
     *    var() fallback in this sheet contains a comma.
     *
     *  - No comments inside a rule block, and no shorthand "inset" — longhand
     *    top/right/bottom/left instead.
     *
     * The last two are precautions rather than diagnoses: the backdrop did not
     * paint on the first cut of this and the cause was not identifiable from the
     * source, so the constructs that a CSS pipeline is most likely to mishandle
     * were removed rather than argued about.
     ************************************************************************/

    private static ILocalCSS styles() {
        return Styles.instance();
    }

    public static interface ILocalCSS extends IComponentCSS {

        String zoomable();

        String zoomOverlay();

        String zoomBar();

        String zoomBtn();

        String zoomPct();

        String zoomStage();

        String zoomInner();
    }

    @CssResource(value = {
        IComponentCSS.COMPONENT_CSS
    }, stylesheet = """
        .zoomable {
            cursor: zoom-in;
        }
        .zoomOverlay {
            position: fixed;
            top: 0;
            right: 0;
            bottom: 0;
            left: 0;
            z-index: 1200;
            display: none;
            flex-direction: column;
            background-color: var(--jui-zoom-backdrop, #0f172aeb);
        }
        .zoomBar {
            flex-shrink: 0;
            display: flex;
            align-items: center;
            justify-content: flex-end;
            gap: 6px;
            padding: 10px 14px;
        }
        .zoomBtn {
            border: none;
            cursor: pointer;
            min-width: 30px;
            height: 30px;
            padding: 0 10px;
            border-radius: 6px;
            font: inherit;
            font-size: 0.85rem;
            line-height: 30px;
            color: var(--jui-zoom-btn-color, #ffffff);
            background-color: var(--jui-zoom-btn-bg, #ffffff29);
        }
        .zoomBtn:hover {
            background-color: var(--jui-zoom-btn-hover-bg, #ffffff47);
        }
        .zoomPct {
            min-width: 52px;
            text-align: center;
            font-size: 0.8rem;
            color: var(--jui-zoom-btn-color, #ffffff);
            user-select: none;
        }
        .zoomStage {
            flex: 1;
            min-height: 0;
            overflow: hidden;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: grab;
        }
        .zoomStage:active {
            cursor: grabbing;
        }
        .zoomInner {
            transform-origin: center center;
            max-width: none;
            padding: 16px;
            border-radius: 8px;
            background-color: var(--jui-zoom-surface, #ffffff);
        }
        .zoomInner svg, .zoomInner img {
            max-width: none;
            max-height: none;
            display: block;
        }
    """)
    public static abstract class Styles implements ILocalCSS {

        private static Styles STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (Styles) GWT.create(Styles.class);
                STYLES.ensureInjected();
            }
            return STYLES;
        }
    }
}
