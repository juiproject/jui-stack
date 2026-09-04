package com.effacy.jui.text.ui.editor;

import java.util.function.Consumer;

import elemental2.dom.Element;

/**
 * A pluggable renderer for a generic fenced block ({@code BlockType.FENCE}), keyed by the
 * fence's <em>info string</em> (the token after the opening ```` ``` ````, e.g.
 * {@code mermaid}). Register implementations with {@link Fences#register(IFenceRenderer)}.
 * <p>
 * A fence carries an opaque body (the raw fenced content) and an info string. The renderer
 * turns that body into a visual representation; the editor handles the rest (atomic block
 * placement, click-to-edit via the fence's source, and Markdown round-tripping). The same
 * renderer is also used to drive the live preview in the source editor.
 *
 * @see Fences
 * @see FenceBlockHandler
 */
public interface IFenceRenderer {

    /**
     * Whether this renderer handles the given info string (e.g. {@code "mermaid".equals(info)}).
     *
     * @param info
     *             the fence info string (may be {@code null} or empty).
     * @return {@code true} if this renderer owns the fence.
     */
    boolean accepts(String info);

    /**
     * Renders the fence body into the given (cleared) target element. Rendering may be
     * asynchronous (e.g. loading a library, then producing SVG) — the implementation should
     * populate {@code target} when ready.
     *
     * @param target
     *                the element to render into (already empty).
     * @param info
     *                the fence info string.
     * @param content
     *                the raw fenced body.
     */
    void render(Element target, String info, String content);

    /**
     * A short human label for the fence kind, used in the editor chrome. Defaults to the
     * info string.
     */
    default String label(String info) {
        return info;
    }

    /**
     * Whether this renderer renders a fence with an <em>empty</em> body. Defaults to
     * {@code false}, in which case an empty fence shows the editor's "click to add"
     * prompt instead. Return {@code true} for fence kinds whose body is optional
     * configuration rather than the content itself (the rendering being derived, e.g.
     * from live data).
     */
    default boolean rendersEmpty() {
        return false;
    }

    /**
     * Whether the rendering is <b>determined entirely by {@code info} and {@code content}</b>
     * — the same pair always producing the same result. Defaults to {@code true}, which is
     * what {@link #render} being a function of its arguments means.
     * <p>
     * The editor re-renders the whole document on every transaction. A cacheable fence is
     * spared that: the editor keeps the element it built and re-appends it, so the fence
     * renders when its own source changes and not on every keystroke elsewhere in the
     * document. For an asynchronous renderer that also removes a flicker, since a render
     * in flight now completes into the element that is still on screen.
     * <p>
     * <b>Return {@code false} if the rendering draws on anything else</b> — a remote
     * query, ambient scope set from outside, the clock, a random seed. Such a fence must
     * be rebuilt each time or it will freeze at whatever it first showed; and where the
     * body is not the input (see {@link #rendersEmpty()}) two fences in genuinely
     * different scopes would otherwise be treated as the same rendering.
     *
     * @return {@code true} if the rendering may be reused for an unchanged source.
     */
    default boolean cacheable() {
        return true;
    }

    /**
     * Placeholder text for the source editor. Defaults to a generic prompt.
     */
    default String placeholder(String info) {
        return "Enter " + label(info) + " source…";
    }

    /**
     * Optionally supplies a custom editor for this fence kind. The default returns
     * {@code false}, so the built-in source editor ({@link FencePanel}) is used.
     * <p>
     * To provide your own editor, open it here and return {@code true}: call
     * {@code apply.accept(newSource)} when the user saves, or {@code remove.run()} when they
     * delete the block. (Dismissal of the editor UI is the implementation's responsibility.)
     *
     * @param anchor
     *                the fence block element (e.g. for positioning).
     * @param info
     *                the fence info string.
     * @param content
     *                the current source.
     * @param apply
     *                accepts the new source to persist.
     * @param remove
     *                removes the fence block.
     * @return {@code true} if a custom editor was opened (the default editor is then skipped).
     */
    default boolean edit(Element anchor, String info, String content, Consumer<String> apply, Runnable remove) {
        return false;
    }
}
