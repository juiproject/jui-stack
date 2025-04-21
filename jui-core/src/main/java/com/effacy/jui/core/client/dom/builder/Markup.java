package com.effacy.jui.core.client.dom.builder;

/**
 * Helper for {@link MarkupBuilder}.
 */
public class Markup {

    /**
     * Creates an instance of {@link MarkupBuilder} with the given content (if the
     * passed text contains no markup then a {@link TextBuilder} is returned).
     * 
     * @param text
     *             the text to apply.
     * @return the newly created element builder.
     */
    public static NodeBuilder<?> $(String text) {
        if (!text.contains("*"))
            return Text.$(text);
        return new MarkupBuilder (text);
    }
    
    /**
     * Creates an instance of this {@link MarkupBuilder} (or {@link TextBuilder})
     * and inserts it into the passed parent.
     * 
     * @param parent
     *               the parent to insert the element into.
     * @param text
     *               the text to apply.
     * @return the newly created element builder.
     */
    public static NodeBuilder<?> $(IDomInsertableContainer<?> parent, String text) {
        NodeBuilder<?> builder = $ (text);
        parent.insert (builder);
        return builder;
    }
}
