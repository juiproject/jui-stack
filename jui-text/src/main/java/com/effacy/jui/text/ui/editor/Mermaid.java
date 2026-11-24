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
 * <b>Security.</b> Diagrams are rendered with Mermaid's {@code strict} security level
 * by default: markup in labels is stripped of script, and {@code click} directives cannot
 * run script or link to {@code javascript:} addresses. A diagram written by one person
 * therefore cannot run script in the browser of another who views it. An application that
 * trusts every author of its diagrams can opt into {@code loose} with
 * {@link #securityLevel(SecurityLevel)}.
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

    /**
     * The Mermaid security levels this plugin supports (Mermaid's {@code securityLevel}
     * configuration).
     */
    public enum SecurityLevel {

        /**
         * Markup in labels is stripped of script and {@code click} directives cannot run
         * script (the default).
         */
        STRICT("strict"),

        /**
         * {@code click} directives are honoured, including links to {@code javascript:}
         * addresses. This lets the author of a diagram run script in the browser of anyone
         * who views it, so only use it where every author is trusted.
         */
        LOOSE("loose");

        private String value;

        private SecurityLevel(String value) {
            this.value = value;
        }

        /**
         * The value Mermaid expects for {@code securityLevel}.
         */
        public String value() {
            return value;
        }
    }

    private static SecurityLevel SECURITY_LEVEL = SecurityLevel.STRICT;

    /**
     * Sets the security level diagrams are rendered with. The default is
     * {@link SecurityLevel#STRICT}; ask for {@link SecurityLevel#LOOSE} only where every
     * author of a diagram is trusted.
     * <p>
     * Takes effect for diagrams rendered after the call (diagrams already on screen keep
     * the rendering they have).
     *
     * @param level
     *              the security level ({@code null} restores the default).
     */
    public static void securityLevel(SecurityLevel level) {
        SECURITY_LEVEL = (level == null) ? SecurityLevel.STRICT : level;
        if (LOADED)
            initialize();
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

    /** A diagram, and a dense one as often as not — worth opening at a readable size. */
    @Override
    public boolean zoomable() {
        return true;
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
                    initialize();
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

    /**
     * Configures the loaded library with the current settings.
     */
    private static void initialize() {
        try {
            JsPropertyMap<Object> config = JsPropertyMap.of();
            config.set("startOnLoad", false);
            config.set("securityLevel", SECURITY_LEVEL.value());
            MermaidApi.initialize(config);
        } catch (Throwable e) {
            // Initialization is best-effort; render() will surface failures.
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
