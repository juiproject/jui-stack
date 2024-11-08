package com.effacy.jui.ui.client.fragments;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import com.effacy.jui.core.client.Invoker;
import com.effacy.jui.core.client.dom.builder.A;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.I;
import com.effacy.jui.core.client.dom.builder.Li;
import com.effacy.jui.core.client.dom.builder.P;
import com.effacy.jui.core.client.dom.builder.Strong;
import com.effacy.jui.core.client.dom.builder.Text;
import com.effacy.jui.core.client.dom.builder.Ul;
import com.effacy.jui.platform.util.client.StringSupport;

/**
 * Used to build out a richly formatted content for {@link Notice} (though is
 * separated out for other purposes as applicable).
 */
public class NoticeBuilder {

    /**
     * Formatting to apply (for specific blocks of text).
     */
    public enum Formatting {
        BOLD, ITALIC;
    }

    /**
     * Construct a builder consisting of a single block (paragraph) containing a
     * single run of plain text.
     * 
     * @param contents
     *                 the contents (plain text).
     * @return the notice builder.
     */
    public static NoticeBuilder of(String contents) {
        return new NoticeBuilder().block(blk -> blk.add(contents));
    }

    /**
     * A block is a contigunous body of formatted text where each segment of text
     * that has uniform formatting is represented as a {@link Run}.
     */
    public static class Block {
        
        /**
         * The runs that constitute the contents of the block.
         */
        private List<Run> runs = new ArrayList<>();
        
        /**
         * If is a list item.
         */
        private boolean list;

        /**
         * See {@link #css(String)}.
         */
        private String css;

        /**
         * Convenience to call {@link #list(boolean)} passing {@code true}.
         */
        public Block list() {
            return list(true);
        }

        /**
         * Marks the block as a list item.
         * 
         * @param list
         *            {@code true} if this block is a list item.
         * @return this block instance.
         */
        public Block list(boolean list) {
            this.list = list;
            return this;
        }

        /**
         * Apply additional CSS to the block (the P tag).
         * 
         * @param css
         *            the CSS to apply.
         * @return this block instance.
         */
        public Block css(String css) {
            this.css = css;
            return this;
        }

        /**
         * Adds formatted text to the block (a run).
         * 
         * @param content
         *                   the content for the run.
         * @param href
         *                   (optional) invokers to activate when the text is clicked (a
         *                   link).
         * @param formatting
         *                   any formatting to apply.
         * @return this block instance.
         */
        public Block add(String content, Invoker href, Formatting... formatting) {
            runs.add (new Run(content, href, formatting));
            return this;
        }

        /**
         * See {@link #add(String, Invoker, Formatting...)} but with no link invoker.
         */
        public Block add(String content, Formatting... formatting) {
            runs.add (new Run(content, null, formatting));
            return this;
        }

        /**
         * Convenience to add bold text.
         * 
         * @param content
         *                the content to be bloded.
         * @return this block instance.
         */
        public Block bold(String content) {
            runs.add (new Run(content, null, Formatting.BOLD));
            return this;
        }

        /**
         * Convenience to add italic text.
         * 
         * @param content
         *                the content to be italic.
         * @return this block instance.
         */
        public Block italic(String content) {
            runs.add (new Run(content, null, Formatting.ITALIC));
            return this;
        }

        /**
         * Builds the block into which the run's will be added.
         * 
         * @param parent
         *               the parent to build into.
         */
        void build(ElementBuilder parent) {
            parent = list ? Li.$(parent) : P.$ (parent);
            if (!StringSupport.empty(css))
                parent.css (css);
            for (Run run : runs)
                run.build(parent);
        }
    }

    /**
     * Represents a run of text to which a consistent set of formatting is applied.
     */
    static class Run {
        
        /**
         * Formatting to apply.
         */
        private Set<Formatting> formatting;

        /**
         * Turns the run into an anchor and this is invoked when clicked on.
         */
        private Invoker href;

        /**
         * The text content for the run.
         */
        private String content;

        /**
         * Construct a run.
         * 
         * @param content
         *                   the content of the run (required).
         * @param href
         *                   (optional) an invoker that is invoked when the text is
         *                   clicked on (this is effected by wrapping in an A tag and
         *                   attaching an onclick handler).
         * @param formatting
         *                   the additional formatting to apply (if any).
         */
        Run(String content, Invoker href, Formatting... formatting) {
            this.content = (content == null) ? "" : content;
            this.href = href;
            if (formatting.length > 0) {
                this.formatting = new HashSet<>();
                for (Formatting format : formatting) {
                    if (format != null)
                        this.formatting.add (format);
                }
            }
        }

        /**
         * Builds the run into the parent.
         * <p>
         * Formatting is applied using standard TAGs and these contain all the content
         * of the run.
         * 
         * @param parent
         *               the parent to build into.
         */
        void build(ElementBuilder parent) {
            if (href != null)
                parent = A.$ (parent).onclick(e -> href.invoke());
            if (formatting != null) {
                if (formatting.contains(Formatting.ITALIC))
                    parent = I.$ (parent);
                if (formatting.contains(Formatting.BOLD))
                    parent = Strong.$ (parent);
            }
            Text.$ (parent, content);
        }
    }

    /**
     * The blocks in the notice.
     */
    private List<Block> blocks = new ArrayList<>();

    /**
     * Adds a block.
     * @return the added block for configuration.
     */
    public Block block() {
        Block block = new Block();
        blocks.add (block);
        return block;
    }

    /**
     * Adds a block and allows for configuration of the build.
     * 
     * @param builder
     *                to configure the block.
     * @return this notice builder.
     */
    public NoticeBuilder block(Consumer<Block> builder) {
        Block block = block();
        if(builder != null)
            builder.accept(block);
        return this;
    }

    /**
     * Builds the content into a parent. Each block will be captured in a P tag and
     * formatting by the corresponding formatting tag (i.e. I or STRONG).
     * 
     * @param parent
     *               the builder to build into.
     */
    public void build(ElementBuilder parent) {
        ElementBuilder subcontainer = null;
        for (Block block : blocks) {
            if (block.list) {
                if (subcontainer == null)
                    subcontainer = Ul.$(parent);
            } else
                subcontainer = null;
            if (subcontainer != null)
                block.build(subcontainer);
            else
                block.build(parent);
        }
    }
}
