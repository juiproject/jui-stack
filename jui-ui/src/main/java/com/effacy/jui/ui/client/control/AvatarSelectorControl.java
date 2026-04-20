package com.effacy.jui.ui.client.control;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.control.Control;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.UIEvent;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.Input;
import com.effacy.jui.core.client.dom.builder.P;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.jquery.JQuery;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.google.gwt.core.client.GWT;

import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.FileReader;
import elemental2.dom.HTMLCanvasElement;
import elemental2.dom.HTMLImageElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.MouseEvent;
import jsinterop.base.Js;

/**
 * Control for selecting an avatar image. Supports two modes:
 * <ol>
 * <li>Selecting a stock avatar from one or more configurable
 * {@link IAvatarProvider} sources.</li>
 * <li>Uploading or drag-and-dropping a custom image which is center-cropped and
 * scaled to a target size.</li>
 * </ol>
 * <p>
 * The value is a {@code String} — either a provider URL or a
 * {@code data:image/...} base64 data URI.
 *
 * @see IAvatarProvider
 * @see AvatarProviders
 */
public class AvatarSelectorControl extends Control<String, AvatarSelectorControl.Config> {

    /************************************************************************
     * Avatar provider.
     ************************************************************************/

    /**
     * Provider of avatar image URLs for the stock avatar selection panel.
     * <p>
     * Implementations generate a set of URLs for a given count and image size,
     * optionally using contextual user data (e.g. email for Gravatar).
     *
     * @see AvatarProviders
     */
    public interface IAvatarProvider {

        /**
         * Contextual data about the user, supplied by the control.
         */
        public record Context(String email, String firstName, String lastName) {}

        /**
         * Generates a list of avatar URLs.
         * <p>
         * The number of URLs returned is determined by the provider. May
         * return an empty list if the provider cannot generate without
         * required context (e.g. no email for Gravatar).
         *
         * @param imageSize
         *                  the target image size in pixels (square).
         * @param context
         *                contextual user data (may be {@code null}).
         * @return the list of URLs (never {@code null}, may be empty).
         */
        List<String> generate(int imageSize, Context context);
    }

    /************************************************************************
     * Configuration.
     ************************************************************************/

    public static class Config extends Control.Config<String, Config> {

        /**
         * See {@link #provider(IAvatarProvider)}.
         */
        private List<IAvatarProvider> providers = new ArrayList<>();

        /**
         * See {@link #imageSize(int)}.
         */
        private int imageSize = 256;

        /**
         * Adds a stock avatar provider. Multiple providers may be added and
         * each will be displayed as a separate section in the selection panel.
         * <p>
         * If no providers are added, a default
         * {@link AvatarProviders#diceBear(String) DiceBear} provider with the
         * "avataaars" style is used.
         *
         * @param provider
         *                 the avatar provider.
         * @return this configuration.
         * @see AvatarProviders
         */
        public Config provider(IAvatarProvider provider) {
            if (provider != null)
                this.providers.add(provider);
            return this;
        }

        /**
         * The target image size in pixels (square). Uploaded images are
         * center-cropped and scaled to this size.
         *
         * @param size
         *             the size in pixels.
         * @return this configuration.
         */
        public Config imageSize(int size) {
            if (size > 0)
                this.imageSize = size;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public AvatarSelectorControl build(LayoutData... data) {
            if (providers.isEmpty())
                providers.add(AvatarProviders.diceBear("avataaars"));
            return build(new AvatarSelectorControl(this), data);
        }
    }

    /************************************************************************
     * Construction.
     ************************************************************************/

    public AvatarSelectorControl(Config config) {
        super(config);
    }

    public AvatarSelectorControl(Consumer<Config> config) {
        super(new Config());
        if (config != null)
            config.accept(config());
    }

    /************************************************************************
     * Value management.
     ************************************************************************/

    private String value;

    @Override
    protected String valueFromSource() {
        return value;
    }

    @Override
    protected void valueToSource(String value) {
        this.value = value;
        refreshPreview();
    }

    /************************************************************************
     * User context (for providers that need user data).
     ************************************************************************/

    private IAvatarProvider.Context userContext;

    /**
     * Sets the user context for avatar providers that require user data (e.g.
     * Gravatar needs an email address).
     *
     * @param email
     *              the user's email address (may be {@code null}).
     * @param firstName
     *                  the user's first name (may be {@code null}).
     * @param lastName
     *                 the user's last name (may be {@code null}).
     */
    public AvatarSelectorControl userContext(String email, String firstName, String lastName) {
        this.userContext = new IAvatarProvider.Context(email, firstName, lastName);
        if (expanded)
            refreshStockSection();
        return this;
    }

    /************************************************************************
     * State.
     ************************************************************************/

    private boolean expanded;

    private Element previewEl;

    private Element panelEl;

    private Element stockSectionEl;

    private Element uploadSectionEl;

    private HTMLInputElement fileInputEl;

    private Element dropZoneEl;

    private Element changeLinkEl;
    private Element removeLinkEl;
    private Element cancelLinkEl;

    /**
     * Crop mode state.
     */
    private boolean cropping;
    private Element cropSectionEl;
    private Element cropCanvasWrapEl;
    private HTMLCanvasElement cropCanvas;
    private HTMLImageElement cropImage;
    private double displayScale;
    private double cropX, cropY, cropSize;
    private String dragMode;
    private double dragStartX, dragStartY;
    private double dragStartCropX, dragStartCropY, dragStartCropSize;
    private elemental2.dom.EventListener cropMoveListener;
    private elemental2.dom.EventListener cropUpListener;

    /************************************************************************
     * Render and DOM.
     ************************************************************************/

    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$(el).$(root -> {
            // Preview area: shows current avatar with change button.
            Div.$(root).style(styles().preview()).$(preview -> {
                Div.$(preview).style(styles().avatarImage()).by("previewImg");
                Div.$(preview).style(styles().actions()).$(actions -> {
                    Span.$(actions).text("Change").by("changeLink").style(styles().changeLink())
                        .onclick((e, n) -> toggleExpanded());
                    Span.$(actions).text("Remove").by("removeLink").style(styles().removeLink())
                        .onclick((e, n) -> {
                            value = null;
                            expanded = false;
                            refreshPreview();
                            refreshPanel();
                            modified();
                        });
                    Span.$(actions).text("Cancel").by("cancelLink").style(styles().changeLink())
                        .css("display: none;")
                        .onclick((e, n) -> {
                            expanded = false;
                            refreshPreview();
                            refreshPanel();
                        });
                });
            });

            // Expandable selection panel.
            Div.$(root).style(styles().panel()).by("panel").$(panel -> {

                // Stock avatars placeholder (populated dynamically by providers).
                Div.$(panel).by("stockSection");

                // Upload section with drop zone.
                Div.$(panel).by("uploadSection").$(section -> {
                    Span.$(section).text("Or upload your own").style(styles().sectionLabel());
                    Div.$(section).style(styles().dropZone()).by("dropZone").$(drop -> {
                        Em.$(drop).style(FontAwesome.cloudArrowUp()).css("font-size: 2em; color: #aaa;");
                        P.$(drop).text("Drop a PNG or JPEG image here to upload").css("color: #888; margin: 0.5em 0 0 0;");
                        Input.$(drop, "file")
                            .attr("accept", "image/png, image/jpeg")
                            .css("position: absolute; opacity: 0; width: 0; height: 0; overflow: hidden;")
                            .by("fileInput")
                            .on(e -> handleFileInput(), UIEventType.ONCHANGE);
                    })
                    .onclick((e, n) -> {
                        if ((fileInputEl != null) && (e.getTarget() != (Object) fileInputEl))
                            fileInputEl.click();
                    })
                    .on((e, n) -> {
                        e.preventDefault();
                        if (dropZoneEl != null)
                            dropZoneEl.classList.add(styles().dragover());
                    }, UIEventType.DRAGOVER)
                    .on((e, n) -> {
                        if (dropZoneEl != null)
                            dropZoneEl.classList.remove(styles().dragover());
                    }, UIEventType.DRAGLEAVE)
                    .on((e, n) -> {
                        e.preventDefault();
                        if (dropZoneEl != null)
                            dropZoneEl.classList.remove(styles().dragover());
                        handleDrop(e);
                    }, UIEventType.DROP);
                });

                // Crop section (hidden by default).
                Div.$(panel).by("cropSection").css("display: none;").$(crop -> {
                    P.$(crop).text("Drag to move, drag corners to resize").style(styles().cropInstructions());
                    Div.$(crop).by("cropCanvasWrap").style(styles().cropCanvasWrap());
                    Div.$(crop).style(styles().cropActions()).$(actions -> {
                        Span.$(actions).text("Cancel").style(styles().changeLink())
                            .onclick((e, n) -> exitCropMode(false));
                        Span.$(actions).text("Apply").style(styles().cropApplyLink())
                            .onclick((e, n) -> exitCropMode(true));
                    });
                });
            });
        }).build(dom -> {
            previewEl = dom.first("previewImg");
            panelEl = dom.first("panel");
            stockSectionEl = dom.first("stockSection");
            uploadSectionEl = dom.first("uploadSection");
            fileInputEl = Js.cast(dom.first("fileInput"));
            dropZoneEl = dom.first("dropZone");
            cropSectionEl = dom.first("cropSection");
            cropCanvasWrapEl = dom.first("cropCanvasWrap");
            changeLinkEl = dom.first("changeLink");
            removeLinkEl = dom.first("removeLink");
            cancelLinkEl = dom.first("cancelLink");
            refreshPreview();
            refreshPanel();
        });
    }

    /************************************************************************
     * Interactions.
     ************************************************************************/

    /**
     * Toggles the expanded state of the selection panel.
     */
    protected void toggleExpanded() {
        if (isReadOnly() || isDisabled())
            return;
        expanded = !expanded;
        refreshPanel();
    }

    /**
     * Handles file selection from the file input.
     */
    protected void handleFileInput() {
        if ((fileInputEl == null) || (fileInputEl.files.length == 0))
            return;
        processFile(fileInputEl.files.item(0));
        fileInputEl.value = "";
    }

    /**
     * Handles a file drop event.
     */
    protected void handleDrop(UIEvent e) {
        if (e.getDataTransfer() == null)
            return;
        if (e.getDataTransfer().files.length == 0)
            return;
        processFile(e.getDataTransfer().files.item(0));
    }

    /**
     * Processes an image file: reads it and enters crop mode so the user can
     * manually select the crop region.
     */
    protected void processFile(elemental2.dom.File file) {
        if (file == null)
            return;
        if (!file.type.startsWith("image/"))
            return;

        FileReader reader = new FileReader();
        reader.onload = evt -> {
            String dataUrl = reader.result.asString();
            HTMLImageElement img = Js.cast(DomGlobal.document.createElement("img"));
            img.onload = imgEvt -> {
                enterCropMode(img);
                return null;
            };
            img.src = dataUrl;
            return null;
        };
        reader.readAsDataURL(file);
    }

    /************************************************************************
     * Display refresh.
     ************************************************************************/

    /**
     * Refreshes the preview area to show the current avatar.
     */
    protected void refreshPreview() {
        if (previewEl == null)
            return;
        previewEl.innerHTML = "";
        if (value != null) {
            Element img = DomGlobal.document.createElement("img");
            String src = value;
            if (!src.startsWith("data:"))
                src += (src.contains("?") ? "&" : "?") + "_=" + System.currentTimeMillis();
            img.setAttribute("src", src);
            img.addEventListener("error", e -> {
                previewEl.innerHTML = "";
                Element icon = DomGlobal.document.createElement("em");
                icon.className = FontAwesome.bug();
                previewEl.appendChild(icon);
            });
            previewEl.appendChild(img);
        } else {
            Element icon = DomGlobal.document.createElement("em");
            icon.className = FontAwesome.user();
            previewEl.appendChild(icon);
        }
    }

    /**
     * Refreshes the panel visibility.
     */
    protected void refreshPanel() {
        if (panelEl == null)
            return;
        JQuery.$(panelEl).toggle(expanded);
        if (expanded) {
            refreshStockSection();
            JQuery.$(changeLinkEl).hide();
            JQuery.$(removeLinkEl).hide();
            JQuery.$(cancelLinkEl).show();
        } else {
            JQuery.$(changeLinkEl).show();
            JQuery.$(removeLinkEl).show();
            JQuery.$(cancelLinkEl).hide();
        }
    }

    /**
     * Rebuilds the stock avatar section from the configured providers.
     */
    protected void refreshStockSection() {
        if (stockSectionEl == null)
            return;
        stockSectionEl.innerHTML = "";
        Element grid = DomGlobal.document.createElement("div");
        grid.className = styles().stockGrid();
        for (IAvatarProvider provider : config().providers) {
            List<String> urls = provider.generate(config().imageSize, userContext);
            if ((urls == null) || urls.isEmpty())
                continue;
            for (String url : urls) {
                Element img = DomGlobal.document.createElement("img");
                img.setAttribute("src", url);
                img.className = styles().stockItem();
                img.addEventListener("click", e -> {
                    value = url;
                    expanded = false;
                    refreshPreview();
                    refreshPanel();
                    modified();
                });
                grid.appendChild(img);
            }
        }
        stockSectionEl.appendChild(grid);
    }

    /************************************************************************
     * Crop mode.
     ************************************************************************/

    private static final int CROP_MAX_DISPLAY = 300;
    private static final int CROP_MIN_SIZE = 32;
    private static final int CROP_HANDLE_SIZE = 8;
    private static final int CROP_HANDLE_HIT = 12;

    /**
     * Enters crop mode for the given image. Displays the image on a canvas
     * with a draggable/resizable square crop overlay.
     */
    protected void enterCropMode(HTMLImageElement img) {
        cropImage = img;

        // Compute display dimensions to fit within CROP_MAX_DISPLAY.
        double scale;
        int displayW, displayH;
        if ((img.naturalWidth >= img.naturalHeight) && (img.naturalWidth > CROP_MAX_DISPLAY)) {
            scale = (double) CROP_MAX_DISPLAY / img.naturalWidth;
            displayW = CROP_MAX_DISPLAY;
            displayH = (int) (img.naturalHeight * scale);
        } else if (img.naturalHeight > CROP_MAX_DISPLAY) {
            scale = (double) CROP_MAX_DISPLAY / img.naturalHeight;
            displayH = CROP_MAX_DISPLAY;
            displayW = (int) (img.naturalWidth * scale);
        } else {
            scale = 1.0;
            displayW = img.naturalWidth;
            displayH = img.naturalHeight;
        }
        displayScale = scale;

        // Create canvas.
        cropCanvas = Js.cast(DomGlobal.document.createElement("canvas"));
        cropCanvas.width = displayW;
        cropCanvas.height = displayH;

        // Initialize crop rectangle: largest centered square.
        double minDim = Math.min(displayW, displayH);
        cropSize = minDim;
        cropX = (displayW - cropSize) / 2.0;
        cropY = (displayH - cropSize) / 2.0;

        // Append canvas and draw.
        if (cropCanvasWrapEl != null) {
            cropCanvasWrapEl.innerHTML = "";
            cropCanvasWrapEl.appendChild(cropCanvas);
        }
        drawCropOverlay();

        // Attach mousedown on canvas.
        cropCanvas.addEventListener("mousedown", e -> {
            e.preventDefault();
            e.stopPropagation();
            onCropMouseDown(Js.uncheckedCast(e));
        });

        // Show crop section, hide stock and upload sections.
        JQuery.$(stockSectionEl).hide();
        JQuery.$(uploadSectionEl).hide();
        JQuery.$(cropSectionEl).show();

        cropping = true;
    }

    /**
     * Draws the crop overlay on the canvas: the image with a dark overlay
     * and a clear crop rectangle with corner handles.
     */
    protected void drawCropOverlay() {
        if ((cropCanvas == null) || (cropImage == null))
            return;
        CanvasRenderingContext2D ctx = Js.cast(cropCanvas.getContext("2d"));
        int w = cropCanvas.width;
        int h = cropCanvas.height;

        // Draw scaled image.
        ctx.clearRect(0, 0, w, h);
        ctx.drawImage(cropImage, 0, 0, w, h);

        // Dark overlay over entire image.
        Js.asPropertyMap(ctx).set("fillStyle", "rgba(0,0,0,0.5)");
        ctx.fillRect(0, 0, w, h);

        // Clear the crop region (redraw image portion).
        ctx.save();
        ctx.beginPath();
        ctx.rect(cropX, cropY, cropSize, cropSize);
        ctx.clip();
        ctx.drawImage(cropImage, 0, 0, w, h);
        ctx.restore();

        // Crop border.
        Js.asPropertyMap(ctx).set("strokeStyle", "rgba(255,255,255,0.9)");
        ctx.lineWidth = 2;
        ctx.strokeRect(cropX, cropY, cropSize, cropSize);

        // Corner handles.
        Js.asPropertyMap(ctx).set("fillStyle", "rgba(255,255,255,0.9)");
        double hs = CROP_HANDLE_SIZE;
        double ho = hs / 2.0;
        ctx.fillRect(cropX - ho, cropY - ho, hs, hs);                         // NW
        ctx.fillRect(cropX + cropSize - ho, cropY - ho, hs, hs);              // NE
        ctx.fillRect(cropX - ho, cropY + cropSize - ho, hs, hs);              // SW
        ctx.fillRect(cropX + cropSize - ho, cropY + cropSize - ho, hs, hs);   // SE
    }

    /**
     * Handles mousedown on the crop canvas. Determines the drag mode and
     * attaches document-level listeners.
     */
    protected void onCropMouseDown(MouseEvent e) {
        if (cropCanvas == null)
            return;
        elemental2.dom.DOMRect rect = cropCanvas.getBoundingClientRect();
        double mx = e.clientX - rect.left;
        double my = e.clientY - rect.top;

        // Determine drag mode: check corners first, then inside.
        dragMode = hitTestCorner(mx, my);
        if (dragMode == null) {
            if ((mx >= cropX) && (mx <= cropX + cropSize) && (my >= cropY) && (my <= cropY + cropSize))
                dragMode = "move";
        }
        if (dragMode == null)
            return;

        dragStartX = e.clientX;
        dragStartY = e.clientY;
        dragStartCropX = cropX;
        dragStartCropY = cropY;
        dragStartCropSize = cropSize;

        // Attach document-level listeners.
        cropMoveListener = evt -> onCropMouseMove(Js.uncheckedCast(evt));
        cropUpListener = evt -> onCropMouseUp();
        DomGlobal.document.addEventListener("mousemove", cropMoveListener);
        DomGlobal.document.addEventListener("mouseup", cropUpListener);

        // Suppress text selection during drag.
        DomGlobal.document.body.style.setProperty("user-select", "none");
    }

    /**
     * Returns the corner handle name if the point is near a corner, or
     * {@code null} otherwise.
     */
    private String hitTestCorner(double mx, double my) {
        double hit = CROP_HANDLE_HIT;
        if ((Math.abs(mx - cropX) < hit) && (Math.abs(my - cropY) < hit))
            return "nw";
        if ((Math.abs(mx - (cropX + cropSize)) < hit) && (Math.abs(my - cropY) < hit))
            return "ne";
        if ((Math.abs(mx - cropX) < hit) && (Math.abs(my - (cropY + cropSize)) < hit))
            return "sw";
        if ((Math.abs(mx - (cropX + cropSize)) < hit) && (Math.abs(my - (cropY + cropSize)) < hit))
            return "se";
        return null;
    }

    /**
     * Handles mousemove during a crop drag or resize operation.
     */
    protected void onCropMouseMove(MouseEvent e) {
        double dx = e.clientX - dragStartX;
        double dy = e.clientY - dragStartY;
        int canvasW = cropCanvas.width;
        int canvasH = cropCanvas.height;

        if ("move".equals(dragMode)) {
            cropX = clamp(dragStartCropX + dx, 0, canvasW - cropSize);
            cropY = clamp(dragStartCropY + dy, 0, canvasH - cropSize);
        } else if ("se".equals(dragMode)) {
            double delta = Math.max(dx, dy);
            double newSize = clamp(dragStartCropSize + delta, CROP_MIN_SIZE, Math.min(canvasW - dragStartCropX, canvasH - dragStartCropY));
            cropSize = newSize;
        } else if ("nw".equals(dragMode)) {
            double delta = Math.max(dx, dy);
            double newSize = clamp(dragStartCropSize - delta, CROP_MIN_SIZE, Math.min(dragStartCropX + dragStartCropSize, dragStartCropY + dragStartCropSize));
            double shift = dragStartCropSize - newSize;
            cropX = dragStartCropX + shift;
            cropY = dragStartCropY + shift;
            cropSize = newSize;
        } else if ("ne".equals(dragMode)) {
            double delta = Math.max(dx, -dy);
            double newSize = clamp(dragStartCropSize + delta, CROP_MIN_SIZE, Math.min(canvasW - dragStartCropX, dragStartCropY + dragStartCropSize));
            double shift = dragStartCropSize - newSize;
            cropY = dragStartCropY + shift;
            cropSize = newSize;
        } else if ("sw".equals(dragMode)) {
            double delta = Math.max(-dx, dy);
            double newSize = clamp(dragStartCropSize + delta, CROP_MIN_SIZE, Math.min(dragStartCropX + dragStartCropSize, canvasH - dragStartCropY));
            double shift = dragStartCropSize - newSize;
            cropX = dragStartCropX + shift;
            cropSize = newSize;
        }
        drawCropOverlay();
    }

    /**
     * Handles mouseup — removes document listeners and clears drag state.
     */
    protected void onCropMouseUp() {
        if (cropMoveListener != null) {
            DomGlobal.document.removeEventListener("mousemove", cropMoveListener);
            cropMoveListener = null;
        }
        if (cropUpListener != null) {
            DomGlobal.document.removeEventListener("mouseup", cropUpListener);
            cropUpListener = null;
        }
        DomGlobal.document.body.style.removeProperty("user-select");
        dragMode = null;
    }

    /**
     * Exits crop mode. If {@code apply} is true, crops the image to the
     * selected region and sets the value.
     */
    protected void exitCropMode(boolean apply) {
        if (apply && (cropImage != null) && (cropCanvas != null)) {
            // Convert display coordinates to source coordinates.
            double sourceX = cropX / displayScale;
            double sourceY = cropY / displayScale;
            double sourceSize = cropSize / displayScale;

            // Create final canvas at target size.
            int size = config().imageSize;
            HTMLCanvasElement canvas = Js.cast(DomGlobal.document.createElement("canvas"));
            canvas.width = size;
            canvas.height = size;
            CanvasRenderingContext2D ctx = Js.cast(canvas.getContext("2d"));
            ctx.drawImage(cropImage, sourceX, sourceY, sourceSize, sourceSize, 0, 0, size, size);

            value = canvas.toDataURL("image/png");
            modified();
        }

        // Clean up crop state.
        onCropMouseUp();
        if (cropCanvasWrapEl != null)
            cropCanvasWrapEl.innerHTML = "";
        cropCanvas = null;
        cropImage = null;
        cropping = false;

        // Restore sections.
        JQuery.$(cropSectionEl).hide();
        JQuery.$(stockSectionEl).show();
        JQuery.$(uploadSectionEl).show();

        expanded = false;
        refreshPreview();
        refreshPanel();
    }

    /**
     * Clamps a value between min and max.
     */
    private static double clamp(double value, double min, double max) {
        if (value < min)
            return min;
        if (value > max)
            return max;
        return value;
    }

    /************************************************************************
     * CSS.
     ************************************************************************/

    @Override
    protected ILocalCSS styles() {
        return LocalCSS.instance();
    }

    public static interface ILocalCSS extends IControlCSS {

        String preview();

        String avatarImage();

        String actions();

        String changeLink();

        String removeLink();

        String panel();

        String sectionLabel();

        String stockGrid();

        String stockItem();

        String dropZone();

        String dragover();

        String cropCanvasWrap();

        String cropActions();

        String cropApplyLink();

        String cropInstructions();
    }

    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        IControlCSS.CONTROL_CSS,
        "com/effacy/jui/ui/client/control/AvatarSelectorControl.css",
        "com/effacy/jui/ui/client/control/AvatarSelectorControl_Override.css"
    })
    public static abstract class LocalCSS implements ILocalCSS {

        private static LocalCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (LocalCSS) GWT.create(LocalCSS.class);
                STYLES.ensureInjected();
            }
            return STYLES;
        }
    }
}