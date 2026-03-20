package com.effacy.jui.text.ui.editor;

import java.util.List;
import java.util.function.Function;

import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.Button;
import com.effacy.jui.core.client.dom.builder.Custom;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.core.client.dom.builder.Input;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.platform.css.client.CssResource;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;
import elemental2.dom.HTMLInputElement;
import jsinterop.base.Js;

/**
 * A lightweight floating panel for selecting or entering an image URL.
 * <p>
 * Supports two modes of image selection:
 * <ul>
 * <li>Direct URL entry via an input field (Enter to apply).</li>
 * <li>Selection from a searchable list of {@link ImageItem} options,
 * displayed as either a grid (columns &gt; 1) with thumbnails above labels,
 * or a list (columns = 1) with thumbnails to the left of labels.</li>
 * </ul>
 * <p>
 * Extends {@link ToolPopupPanel} for positioning, dismiss, and singleton
 * tracking. At most one popup panel is visible at a time.
 */
public class ImagePanel extends ToolPopupPanel {

    /************************************************************************
     * Types.
     ************************************************************************/

    /**
     * A selectable image option.
     *
     * @param label
     *              the display label.
     * @param url
     *              the image source URL (applied on selection).
     * @param thumbnail
     *              optional thumbnail preview URL (or {@code null} for
     *              label-only display).
     */
    public record ImageItem(String label, String url, String thumbnail) {}

    /**
     * Callback for image panel actions.
     */
    public interface IImagePanelCallback {

        /**
         * Called when the user applies an image URL.
         */
        void onApply(String src);

        /**
         * Called when the user removes the existing image.
         */
        void onRemove();
    }

    /************************************************************************
     * Show.
     ************************************************************************/

    /**
     * Shows the image panel below {@code anchor} without options.
     */
    public static void show(Element anchor, String currentSrc, IImagePanelCallback callback) {
        show(anchor, currentSrc, null, 1, callback);
    }

    /**
     * Shows the image panel below {@code anchor}.
     *
     * @param anchor
     *               the element to anchor the panel to.
     * @param currentSrc
     *               the current image src (or {@code null} if no image exists).
     * @param options
     *               optional function returning image items for the typed text
     *               (or {@code null} for no suggestions).
     * @param columns
     *               number of grid columns for the item list (1 for list
     *               layout, &gt; 1 for grid layout with thumbnails above
     *               labels).
     * @param callback
     *               the callback for apply/remove actions.
     */
    public static void show(Element anchor, String currentSrc, Function<String, List<ImageItem>> options, int columns, IImagePanelCallback callback) {
        ImagePanel panel = new ImagePanel(currentSrc, options, columns, callback);
        panel.show(anchor);
    }

    /************************************************************************
     * Instance state.
     ************************************************************************/

    private String currentSrc;
    private Function<String, List<ImageItem>> options;
    private int columns;
    private IImagePanelCallback callback;
    private HTMLInputElement input;
    private Element itemListEl;
    private List<ImageItem> currentItems;

    private ImagePanel(String currentSrc, Function<String, List<ImageItem>> options, int columns, IImagePanelCallback callback) {
        this.currentSrc = currentSrc;
        this.options = options;
        this.columns = Math.max(1, columns);
        this.callback = callback;
    }

    /************************************************************************
     * ToolPopupPanel.
     ************************************************************************/

    @Override
    protected void buildContent(ElementBuilder root) {
        root.style(styles().imgPanel());

        // Input row: URL input + optional Remove button.
        Div.$(root).style(styles().inputRow()).$(inputRow -> {
            buildInputField(inputRow);
            if ((currentSrc != null) && !currentSrc.isEmpty())
                buildRemoveButton(inputRow);
        });

        // Item list (when options are provided).
        if (options != null) {
            Div.$(root).style(styles().imgList())
                .style(columns > 1, styles().imgListGrid())
                .use(n -> {
                    itemListEl = (Element) n;
                    if (columns > 1)
                        Js.<elemental2.dom.HTMLElement>uncheckedCast(itemListEl)
                            .style.setProperty("grid-template-columns", "repeat(" + columns + ", 1fr)");
                })
                .on(e -> {
                    Element target = e.getTarget();
                    while ((target != null) && (target != itemListEl)) {
                        String idx = target.getAttribute("data-idx");
                        if ((idx != null) && !idx.isEmpty()) {
                            int i = Integer.parseInt(idx);
                            if ((currentItems != null) && (i >= 0) && (i < currentItems.size())) {
                                e.stopEvent();
                                hide();
                                callback.onApply(currentItems.get(i).url());
                            }
                            return;
                        }
                        target = target.parentElement;
                    }
                }, UIEventType.ONMOUSEDOWN);
        }
    }

    @Override
    protected void onShown() {
        if (input != null)
            input.focus();
        if (options != null) {
            input.addEventListener("input", evt -> updateItems(input.value.trim()));
            updateItems(input.value.trim());
        }
    }

    /************************************************************************
     * Helpers.
     ************************************************************************/

    private void buildInputField(IDomInsertableContainer<?> parent) {
        Input.$(parent, "text")
            .style(styles().imgInput())
            .attr("placeholder", "Enter image URL...")
            .use(n -> {
                input = Js.uncheckedCast(n);
                if ((currentSrc != null) && !currentSrc.isEmpty())
                    input.value = currentSrc;
            })
            .on(e -> {
                if ("Enter".equals(e.getKey())) {
                    e.stopEvent();
                    String src = input.value.trim();
                    if (!src.isEmpty()) {
                        hide();
                        callback.onApply(src);
                    }
                }
                if ("Escape".equals(e.getKey())) {
                    e.stopEvent();
                    hide();
                }
            }, UIEventType.ONKEYDOWN);
    }

    private void buildRemoveButton(IDomInsertableContainer<?> parent) {
        Button.$(parent).style(styles().imgBtn(), styles().imgBtnDanger()).text("Remove")
            .on(e -> {
                e.stopEvent();
                hide();
                callback.onRemove();
            }, UIEventType.ONMOUSEDOWN);
    }

    private void updateItems(String text) {
        currentItems = options.apply(text);
        Wrap.buildInto(itemListEl, root -> {
            if ((currentItems != null) && !currentItems.isEmpty()) {
                for (int i = 0; i < currentItems.size(); i++) {
                    ImageItem item = currentItems.get(i);
                    Div.$(root).style(styles().imgItem())
                        .style(columns > 1, styles().imgItemGrid())
                        .attr("data-idx", String.valueOf(i))
                        .$(cell -> {
                            if ((item.thumbnail() != null) && !item.thumbnail().isEmpty()) {
                                Custom.$(cell, "img")
                                    .attr("src", item.thumbnail())
                                    .attr("alt", item.label())
                                    .style(styles().imgThumb())
                                    .style(columns > 1, styles().imgThumbLarge())
                                    .style(columns == 1, styles().imgThumbSmall());
                            }
                            Span.$(cell).style(styles().imgItemLabel()).text(item.label());
                        });
                }
            } else {
                Div.$(root).style(styles().hintItem()).$(hint -> {
                    Span.$(hint).style(styles().hintIcon()).text("\u2295");
                    Div.$(hint).$(info -> {
                        Div.$(info).style(styles().hintLabel())
                            .text(text.isEmpty() ? "..." : text);
                        Div.$(info).style(styles().hintDesc())
                            .text("Type a complete URL for the image");
                    });
                });
            }
        });
    }

    /************************************************************************
     * CSS.
     ************************************************************************/

    protected IImagePanelCSS styles() {
        return ImagePanelCSS.instance();
    }

    public static interface IImagePanelCSS extends IComponentCSS {

        String imgPanel();

        String imgInput();

        String imgBtn();

        String imgBtnDanger();

        String inputRow();

        String imgList();

        String imgListGrid();

        String imgItem();

        String imgItemGrid();

        String imgItemLabel();

        String imgThumb();

        String imgThumbSmall();

        String imgThumbLarge();

        String hintItem();

        String hintIcon();

        String hintLabel();

        String hintDesc();
    }

    @CssResource(value = {
        IComponentCSS.COMPONENT_CSS
    }, stylesheet = """
        .imgPanel {
            position: fixed;
            background: #fff;
            border: 1px solid #e5e7eb;
            border-radius: 6px;
            box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12), 0 1px 4px rgba(0, 0, 0, 0.06);
            padding: 8px;
            z-index: 10000;
            display: flex;
            flex-direction: column;
            gap: 6px;
            min-width: 280px;
            font-size: 0.875em;
        }
        .imgInput {
            border: 1px solid #d1d5db;
            border-radius: 4px;
            padding: 4px 8px;
            outline: none;
            min-width: 220px;
            font-size: inherit;
        }
        .imgInput:focus {
            border-color: #3b82f6;
        }
        .imgBtn {
            border: none;
            background: #3b82f6;
            color: #fff;
            padding: 4px 10px;
            border-radius: 4px;
            cursor: pointer;
            font-size: inherit;
            white-space: nowrap;
        }
        .imgBtn:hover {
            background: #2563eb;
        }
        .imgBtnDanger {
            background: #ef4444;
        }
        .imgBtnDanger:hover {
            background: #dc2626;
        }
        .inputRow {
            display: flex;
            gap: 6px;
            align-items: center;
        }
        .inputRow .imgInput {
            flex: 1;
            min-width: 0;
        }
        .imgList {
            display: flex;
            flex-direction: column;
            max-height: 280px;
            overflow-y: auto;
        }
        .imgListGrid {
            display: grid;
            gap: 6px;
        }
        .imgItem {
            display: flex;
            gap: 8px;
            align-items: center;
            padding: 6px 8px;
            cursor: pointer;
            border-radius: 4px;
        }
        .imgItem:hover {
            background: #eff6ff;
            color: #1d4ed8;
        }
        .imgItemGrid {
            flex-direction: column;
            align-items: center;
            text-align: center;
        }
        .imgItemLabel {
            font-size: 0.85em;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }
        .imgThumb {
            object-fit: cover;
            border-radius: 4px;
        }
        .imgThumbSmall {
            width: 32px;
            height: 32px;
            flex-shrink: 0;
        }
        .imgThumbLarge {
            width: 100%;
            aspect-ratio: 1;
        }
        .hintItem {
            padding: 6px 8px;
            display: flex;
            gap: 8px;
            align-items: center;
            color: #9ca3af;
        }
        .hintIcon {
            font-size: 1.1em;
        }
        .hintLabel {
            font-weight: 500;
            color: #374151;
        }
        .hintDesc {
            font-size: 0.85em;
            color: #9ca3af;
        }
    """)
    public static abstract class ImagePanelCSS implements IImagePanelCSS {

        private static ImagePanelCSS STYLES;

        public static IImagePanelCSS instance() {
            if (STYLES == null) {
                STYLES = (ImagePanelCSS) GWT.create(ImagePanelCSS.class);
                STYLES.ensureInjected();
            }
            return STYLES;
        }
    }
}
