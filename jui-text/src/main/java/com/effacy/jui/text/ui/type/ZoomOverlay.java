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
import com.effacy.jui.core.client.component.SimpleComponent;
import com.effacy.jui.core.client.dom.DomSupport;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.Custom;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
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
 * <b>Shape.</b> A static facade over a body-level singleton {@link Overlay} component,
 * following {@code Notifier}: the entry points are static because the callers (a content
 * renderer, a click on a diagram) are not components and have nothing to own an instance,
 * while the overlay itself is a component and gets the renderer, the managed events and
 * the lifecycle that go with being one. It is built on first use, so a page with no
 * diagrams on it pays nothing.
 */
public final class ZoomOverlay {

    private ZoomOverlay() {
        // Static facade.
    }

    /** Zoom bounds. The lower is a whole large diagram; the upper is a label on it. */
    private static final double MIN_SCALE = 0.1;

    private static final double MAX_SCALE = 8.0;

    /** The factor one press of a zoom button applies. */
    private static final double STEP = 1.25;

    /** Movement (px) beyond which a press on the backdrop is a drag rather than a click. */
    private static final double DRAG_SLOP = 3;

    /** The allowance fitting leaves around the content: the surface's padding plus a margin. */
    private static final double FIT_MARGIN = 96;

    /************************************************************************
     * Facade.
     ************************************************************************/

    /**
     * Marks a rendered element as openable: it takes the zoom cursor, and a click on it
     * opens a copy enlarged.
     * <p>
     * Safe to call on an element whose rendering has not finished — the copy is taken at
     * click time, so whatever the renderer has produced by then is what is shown.
     * <p>
     * The listener is a raw one because the element is not ours: it belongs to whatever
     * rendered the content, which is a renderer rather than a component and offers no
     * managed hook to attach to. It is scoped to that element and goes when it does.
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
        instance().present(source);
    }

    /** Closes the overlay and drops the copy it was showing. */
    public static void close() {
        if (INSTANCE != null)
            INSTANCE.dismiss();
    }

    /************************************************************************
     * The singleton.
     ************************************************************************/

    /** The id of the body-level element the overlay is bound to. */
    private static final String CONTAINER_ID = "jui-zoom-overlay";

    private static Overlay INSTANCE;

    /**
     * The overlay, built and bound to a body-level element on first use.
     *
     * @return the singleton.
     */
    private static Overlay instance() {
        if (INSTANCE == null) {
            INSTANCE = new Overlay();
            Element el = DomSupport.createDiv();
            el.id = CONTAINER_ID;
            DomGlobal.document.body.appendChild(el);
            INSTANCE.bind(CONTAINER_ID);
        }
        return INSTANCE;
    }

    /************************************************************************
     * The overlay component.
     ************************************************************************/

    /**
     * The full-page surface: a control bar and a stage carrying the copied content under a
     * transform.
     */
    static class Overlay extends SimpleComponent {

        private HTMLElement stageEl;

        /** Carries the transform; holds the copy of the content. */
        private HTMLElement innerEl;

        private Element pctEl;

        private double scale = 1;

        /** The scale at which the content last fitted the stage — where <i>Fit</i> returns to. */
        private double fitScale = 1;

        private double panX;

        private double panY;

        private boolean dragging;

        /** The pointer moved while down, so the release is the end of a drag and not a click. */
        private boolean dragged;

        private double dragFromX;

        private double dragFromY;

        /**
         * Document-level listeners, held for removal. The drag pair lives only for the
         * duration of a drag and the key listener only while the overlay is open — a
         * gesture-scoped registration, which is the acceptable use of a raw document
         * listener.
         */
        private EventListener keyListener;

        private EventListener moveListener;

        private EventListener upListener;

        Overlay() {
            renderer(root -> {
                Div.$(root).style("zoomBar").$(bar -> {
                    button(bar, "−", "Zoom out", () -> zoom(1 / STEP));
                    Div.$(bar).style("zoomPct").by("pct").text("100%");
                    button(bar, "+", "Zoom in", () -> zoom(STEP));
                    button(bar, "Fit", "Fit to the page", this::refit);
                    button(bar, "✕", "Close", this::dismiss);
                });
                Div.$(root).style("zoomStage").by("stage")
                    .on(e -> {
                        e.preventDefault();
                        MouseEvent me = Js.uncheckedCast(e.getEvent());
                        beginDrag(me);
                    }, UIEventType.ONMOUSEDOWN)
                    // Clicking the backdrop dismisses — the ordinary way out of a lightbox.
                    // Two things are excluded, and both are about not fighting the pan: a
                    // press that moved (it was a drag, not a click), and a press that landed
                    // on the content itself (grabbing the diagram to move it is not a
                    // request to close it).
                    .on(e -> {
                        if (dragged)
                            return;
                        Node target = e.getTarget();
                        if ((target != null) && innerEl.contains(target))
                            return;
                        dismiss();
                    }, UIEventType.ONCLICK)
                    .$(stage -> Div.$(stage).style("zoomInner").by("inner"));
            }, dom -> {
                stageEl = Js.uncheckedCast(dom.first("stage"));
                innerEl = Js.uncheckedCast(dom.first("inner"));
                pctEl = dom.first("pct");
                // Raw, and deliberately: UIEventType.ONMOUSEWHEEL is the legacy
                // "mousewheel" event, which Firefox never fired and which is deprecated
                // everywhere else. The standard event is "wheel", and it has no
                // UIEventType, so this is the one place the managed API cannot express
                // what is needed.
                stageEl.addEventListener("wheel", evt -> {
                    WheelEvent we = Js.uncheckedCast(evt);
                    // The page behind must not scroll; this surface owns the gesture.
                    we.preventDefault();
                    zoomAt((we.deltaY < 0) ? STEP : (1 / STEP), we.clientX, we.clientY);
                });
            });
        }

        private void button(ElementBuilder parent, String label, String title, Runnable action) {
            Custom.$(parent, "button").style("zoomBtn").attr("type", "button").attr("title", title)
                .text(label)
                .on(e -> {
                    e.stopEvent();
                    action.run();
                }, UIEventType.ONCLICK);
        }

        /************************************************************************
         * Opening and closing.
         ************************************************************************/

        void present(Element source) {
            // The size the original occupies on the page. Taken from the original rather
            // than measured off the copy because a rendered diagram's own width is
            // typically a percentage of whatever contained it — put on a shrink-to-fit
            // stage that resolves to nothing useful, whereas the page has already answered
            // the question.
            DOMRect rect = source.getBoundingClientRect();
            innerEl.innerHTML = "";
            innerEl.appendChild(source.cloneNode(true));
            innerEl.style.setProperty("width", rect.width + "px");
            // A class rather than show(): the root lays out as a flex column, and the
            // framework's show() restores a display the CSS is the authority on.
            getRoot().classList.add("open");
            panX = 0;
            panY = 0;
            fitTo(rect.width, rect.height);
            if (keyListener == null) {
                keyListener = evt -> {
                    KeyboardEvent ke = Js.uncheckedCast(evt);
                    if ("Escape".equals(ke.key))
                        dismiss();
                };
                DomGlobal.document.addEventListener("keydown", keyListener);
            }
        }

        void dismiss() {
            endDrag();
            getRoot().classList.remove("open");
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
         * Upscaling is allowed rather than capped at 1: a diagram too small to read is
         * exactly the case this feature exists for, and refusing to enlarge it would be
         * refusing the point.
         */
        private void fitTo(double width, double height) {
            double availableWidth = stageEl.clientWidth - FIT_MARGIN;
            double availableHeight = stageEl.clientHeight - FIT_MARGIN;
            if ((width <= 0) || (height <= 0) || (availableWidth <= 0) || (availableHeight <= 0))
                fitScale = 1;
            else
                fitScale = clamp(Math.min(availableWidth / width, availableHeight / height));
            scale = fitScale;
            apply();
        }

        /** Returns to the fitted scale and centres. */
        private void refit() {
            panX = 0;
            panY = 0;
            scale = fitScale;
            apply();
        }

        /** Zooms by {@code factor} about the middle of the stage. */
        private void zoom(double factor) {
            DOMRect rect = stageEl.getBoundingClientRect();
            zoomAt(factor, rect.left + (stageEl.clientWidth / 2.0), rect.top + (stageEl.clientHeight / 2.0));
        }

        /**
         * Zooms by {@code factor} keeping the content under ({@code clientX},
         * {@code clientY}) where it is — which is what makes a wheel zoom feel like
         * magnification of the thing being pointed at rather than of the picture as a whole.
         */
        private void zoomAt(double factor, double clientX, double clientY) {
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

        private void apply() {
            innerEl.style.setProperty("transform",
                "translate(" + panX + "px, " + panY + "px) scale(" + scale + ")");
            pctEl.textContent = Math.round(scale * 100) + "%";
        }

        /************************************************************************
         * Dragging.
         ************************************************************************/

        private void beginDrag(MouseEvent evt) {
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

        private void endDrag() {
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

        /**
         * {@inheritDoc}
         * <p>
         * The singleton is never disposed in practice, but the document listeners it can be
         * holding are exactly the kind that outlive a component's own DOM — so they are
         * released here rather than left to an assumption about its lifetime.
         *
         * @see com.effacy.jui.core.client.component.Component#onDispose()
         */
        @Override
        protected void onDispose() {
            dismiss();
            super.onDispose();
        }

        /**
         * The overlay shares the facade's sheet: {@code .zoomable} is applied out in the
         * document by {@link ZoomOverlay#enable(Element)} while everything else is this
         * component's, and one sheet for the feature is easier to keep coherent than two.
         */
        @Override
        protected ILocalCSS styles() {
            return ZoomOverlay.styles();
        }
    }

    /************************************************************************
     * CSS.
     *
     * The overlay is a component, so this is legitimately an IComponentCSS and the
     * root is styled through .component — which is generated per sheet, so the
     * names beneath it cannot collide with anything else on the page.
     *
     * Two conventions worth keeping:
     *
     *  - Colour fallbacks are hex-with-alpha (#0f172aeb) rather than rgba(), so no
     *    var() fallback in this sheet contains a comma; and no shorthand "inset".
     *    Precautions rather than diagnoses — the backdrop did not paint on the
     *    first cut of this and the cause was never identified, so the constructs a
     *    CSS pipeline is most likely to mishandle were removed rather than argued
     *    about. Not worth re-litigating while it works.
     *
     *  - .zoomable is deliberately NOT scoped under .component: it is applied to the
     *    rendered diagram out in the document, which is not part of this component.
     ************************************************************************/

    private static ILocalCSS styles() {
        return Styles.instance();
    }

    public static interface ILocalCSS extends IComponentCSS {

        String zoomable();
    }

    @CssResource(value = {
        IComponentCSS.COMPONENT_CSS
    }, stylesheet = """
        .zoomable {
            cursor: zoom-in;
        }
        .component {
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
        .component.open {
            display: flex;
        }
        .component .zoomBar {
            flex-shrink: 0;
            display: flex;
            align-items: center;
            justify-content: flex-end;
            gap: 6px;
            padding: 10px 14px;
        }
        .component .zoomBtn {
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
        .component .zoomBtn:hover {
            background-color: var(--jui-zoom-btn-hover-bg, #ffffff47);
        }
        .component .zoomPct {
            min-width: 52px;
            text-align: center;
            font-size: 0.8rem;
            color: var(--jui-zoom-btn-color, #ffffff);
            user-select: none;
        }
        .component .zoomStage {
            flex: 1;
            min-height: 0;
            overflow: hidden;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: grab;
        }
        .component .zoomStage:active {
            cursor: grabbing;
        }
        .component .zoomInner {
            transform-origin: center center;
            max-width: none;
            padding: 16px;
            border-radius: 8px;
            background-color: var(--jui-zoom-surface, #ffffff);
        }
        .component .zoomInner svg, .component .zoomInner img {
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
