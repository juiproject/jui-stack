package com.effacy.jui.text.ui.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.effacy.jui.platform.util.client.ScriptInjector;
import com.effacy.jui.platform.util.client.StringSupport;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.promise.Promise;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.JsPropertyMap;

/**
 * A {@link IFenceRenderer} plugin that renders ```` ```mermaid ```` fenced blocks as
 * diagrams using the <a href="https://mermaid.js.org/">Mermaid</a> library, loaded on
 * demand from a CDN.
 * <p>
 * Install once at start-up:
 * <pre>
 *   Mermaid.install();
 * </pre>
 * which registers it with {@link Fences}. Thereafter any FENCE block whose info string is
 * {@code mermaid} is rendered (in the editor and the source-editor preview) as a diagram,
 * and round-trips to ```` ```mermaid ... ``` ```` Markdown.
 * <p>
 * <b>Note.</b> The Mermaid JS API is version-sensitive; this targets the v10/v11 Promise
 * API ({@code mermaid.render(id, text) -> {svg}}). Pin {@link #CDN_URL} as needed.
 */
public class Mermaid implements IFenceRenderer {

    /** CDN URL for the Mermaid library (pin a version). */
    public static String CDN_URL = "https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.min.js";

    private static boolean INSTALLED = false;

    /** Registers the Mermaid renderer with {@link Fences} (idempotent). */
    public static void install() {
        if (INSTALLED)
            return;
        INSTALLED = true;
        Fences.register(new Mermaid());
    }

    /************************************************************************
     * Pluggable editor.
     ************************************************************************/

    /**
     * A custom editor for Mermaid diagrams, supplied via {@link Mermaid#editor(IMermaidEditor)}.
     * When set, it replaces the built-in source editor for ```` ```mermaid ```` blocks.
     */
    @FunctionalInterface
    public interface IMermaidEditor {

        /**
         * Opens the editor for the given Mermaid source.
         *
         * @param anchor
         *                the fence block element (e.g. for positioning).
         * @param content
         *                the current Mermaid source.
         * @param apply
         *                accepts the new source to persist (saves the block).
         * @param remove
         *                removes the fence block.
         */
        void open(Element anchor, String content, Consumer<String> apply, Runnable remove);
    }

    private static IMermaidEditor EDITOR;

    /**
     * Supplies a custom Mermaid editor, replacing the built-in source editor. Pass
     * {@code null} to revert to the built-in editor.
     *
     * @param editor
     *               the custom editor, or {@code null}.
     */
    public static void editor(IMermaidEditor editor) {
        EDITOR = editor;
    }

    @Override
    public boolean edit(Element anchor, String info, String content, Consumer<String> apply, Runnable remove) {
        if (EDITOR == null)
            return false;
        EDITOR.open(anchor, content, apply, remove);
        return true;
    }

    @Override
    public boolean accepts(String info) {
        return "mermaid".equals(info);
    }

    @Override
    public String label(String info) {
        return "Mermaid";
    }

    @Override
    public String placeholder(String info) {
        return "Enter Mermaid diagram source…";
    }

    @Override
    public void render(Element target, String info, String content) {
        if (StringSupport.empty(content))
            return;
        ensureLoaded(() -> doRender(target, content));
    }

    /************************************************************************
     * Library loading.
     ************************************************************************/

    private static boolean LOADED = false;
    private static List<Runnable> PENDING = null;
    private static int SEQ = 0;

    private static void ensureLoaded(Runnable then) {
        if (LOADED) {
            then.run();
            return;
        }
        if (PENDING == null) {
            // First request: start the load.
            PENDING = new ArrayList<>();
            PENDING.add(then);
            ScriptInjector.injectFromUrl(CDN_URL, (Optional<Exception> outcome) -> {
                if (!outcome.isPresent()) {
                    try {
                        JsPropertyMap<Object> config = JsPropertyMap.of();
                        config.set("startOnLoad", false);
                        config.set("securityLevel", "loose");
                        MermaidApi.initialize(config);
                    } catch (Throwable e) {
                        // Initialization is best-effort; render() will surface failures.
                    }
                    LOADED = true;
                }
                List<Runnable> queued = PENDING;
                PENDING = null;
                if (queued != null)
                    queued.forEach(Runnable::run);
            });
        } else {
            PENDING.add(then);
        }
    }

    private void doRender(Element target, String content) {
        if (!LOADED) {
            renderError(target, content);
            return;
        }
        String id = "mmd-" + (SEQ++);
        try {
            MermaidApi.render(id, content).then(result -> {
                target.innerHTML = result.svg;
                return null;
            }, error -> {
                renderError(target, content);
                return null;
            });
        } catch (Throwable e) {
            renderError(target, content);
        }
    }

    /** Fallback: show the source legibly when the diagram cannot be rendered. */
    private void renderError(Element target, String content) {
        Element pre = DomGlobal.document.createElement("pre");
        Element code = DomGlobal.document.createElement("code");
        code.textContent = content;
        pre.appendChild(code);
        target.innerHTML = "";
        target.appendChild(pre);
    }

    /************************************************************************
     * Mermaid JS interop (v10/v11).
     ************************************************************************/

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "mermaid")
    private static class MermaidApi {

        public static native void initialize(Object config);

        public static native Promise<RenderResult> render(String id, String text);
    }

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
    private static class RenderResult {

        public String svg;
    }
}
