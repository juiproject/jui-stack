package com.effacy.jui.text.ui.editor;

import java.util.ArrayList;
import java.util.List;

/**
 * Registry of {@link IFenceRenderer}s keyed by fence info string. This is the pluggable
 * seam for fenced blocks: register a renderer for an info string (e.g. {@code mermaid}) and
 * the editor (via {@link FenceBlockHandler}) and read-only renderers will use it to render
 * and edit fences carrying that info.
 * <p>
 * The set of registered info strings also drives <em>parsing</em>: pass
 * {@link #isRegistered(String)} as the predicate to
 * {@code MarkdownParser.fence(...)} so that exactly those fences are parsed as
 * {@code BlockType.FENCE} (everything else stays a code block).
 *
 * @see IFenceRenderer
 */
public final class Fences {

    private static final List<IFenceRenderer> RENDERERS = new ArrayList<>();

    /**
     * Registers a fence renderer. Renderers are consulted in registration order; the first
     * whose {@link IFenceRenderer#accepts(String)} returns {@code true} wins.
     *
     * @param renderer
     *                 the renderer to register (ignored if {@code null}).
     */
    public static void register(IFenceRenderer renderer) {
        if (renderer != null)
            RENDERERS.add(renderer);
    }

    /**
     * Finds the renderer for the given info string.
     *
     * @param info
     *             the fence info string.
     * @return the renderer, or {@code null} if none is registered for it.
     */
    public static IFenceRenderer rendererFor(String info) {
        for (IFenceRenderer r : RENDERERS) {
            if (r.accepts(info))
                return r;
        }
        return null;
    }

    /**
     * Whether some renderer handles the given info string.
     *
     * @param info
     *             the fence info string.
     * @return {@code true} if a renderer is registered for it.
     */
    public static boolean isRegistered(String info) {
        return rendererFor(info) != null;
    }

    private Fences() {}
}
