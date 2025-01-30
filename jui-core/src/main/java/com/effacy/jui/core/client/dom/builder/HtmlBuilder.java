package com.effacy.jui.core.client.dom.builder;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.gwtproject.regexp.shared.MatchResult;
import org.gwtproject.regexp.shared.RegExp;

import com.effacy.jui.core.client.dom.builder.HtmlBuilder.HtmlTemplate.Built;
import com.effacy.jui.core.client.dom.jquery.JQuery;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.Node;

/**
 * Used to build out HTML from a string representation of HTML.
 * <p>
 * The content must have a single root tag and be well-formed. It should also be
 * used with care as content is generated directly via
 * {@link Element#innerHTML} which means that unsafe content can be injected if
 * it is not properly prepared.
 */
public class HtmlBuilder extends ContainerBuilder<HtmlBuilder> {

    /**
     * The template used for this builder.
     */
    private HtmlTemplate html;

    /**
     * Properties to apply.
     */
    private Map<String,String> properties;

    /**
     * Construct with the template contents.
     * 
     * @param content
     *                the contents.
     */
    public HtmlBuilder(String content, Map<String,String> properties) {
        if (content == null)
            return;

        // Retain properties.
        this.properties = properties;
        
        // Retrieve from cache.
        String hash = hash(content);
        html = lookup(hash);
        if (html != null)
            return;

        // Not in cache so we create. First we trim.
        content = content.replaceAll("\\r?\\n", "");

        // Sanity check for a single element.
        MatchResult tagmatcher = RegExp.compile("^<([a-zA-Z0-9]+)[^>]*>(.*?)</\\1>$", "gm").exec(content.trim());
        html = new HtmlTemplate((tagmatcher == null) ? null : content);

        // Cache the result.
        cache(hash, html);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.uticorel.client.dom.DomBuilder.NodeBuilder#_nodeImpl(Node,BuildContext)
     */
    @Override
    protected Node _nodeImpl(Node parent, BuildContext ctx) {
        if (html == null)
            return parent;
        Built built = html.generateNode(properties);
        parent.appendChild (built.node());
        Node body = (built.content() != null) ? built.content() : parent;
        super._nodeImpl(body, ctx);
        return built.node();
    }

    /**
     * <em>For future use when it comes to mapping html content to associated
     * templates, when templating is employed.</em>
     * 
     * @param content
     *                the (HTML) content to hash.
     * @return the associated hash for lookup.
     */
    private String hash(String content) {
        long hash = 0;
        for (char c : content.toCharArray())
            hash = 31 * hash + c;
        return content.length() + ":" + hash;
    }

    /************************************************************************
     * Templating (and caching).
     * <p>
     * This is provided at this stage as a framework to (eventually) build
     * out a comprehensive templating mechanism.
     * <p>
     * The framework, as it stands, focusses on the caching mechanism which
     * uses the string-based template representation as a generator for a
     * key.
     ************************************************************************/

    /**
     * Cache of templates.
     */
    private static Map<String,HtmlTemplate> CACHE = new HashMap<>();

    /**
     * Lookup a template given its hash (see {@link #hash(String)}).
     * 
     * @param hash
     *             the hash code.
     * @return the associated template or {@code null}.
     */
    private static HtmlTemplate lookup(String hash) {
        return CACHE.get(hash);
    }

    /**
     * Insert the template of the given hash (see {@link #hash(String)}).
     * 
     * @param hash
     *                 the hash code.
     * @param template
     *                 the associated template.
     */
    private static void cache(String hash, HtmlTemplate template) {
        CACHE.put(hash, template);
    }
    
    /**
     * Represents a parsed template for building content.
     */
    class HtmlTemplate {

        /**
         * Return type for a template build. Consists of the node that was created and
         * an optional node into which to place child content.
         */
        public record Built(Node node, Node content) {}

        /**
         * The HTML content.
         */
        private String html;

        /**
         * If content is included.
         */
        private boolean includesContent;

        /**
         * A (fairly) unique key for mapping to the content element.
         */
        private static String UID = "sajgjdsfhg387538gsdi762";

        /**
         * Construct with HTML content (to parse).
         * 
         * @param html
         *             the content.
         */
        public HtmlTemplate(String html) {
            this.html = html;
            this.includesContent = html.contains("$$");
            if (this.includesContent)
                this.html = html.replace("$$", "<span id='" + UID + "'></span>");
        }

        /**
         * Generates a node from configuration data.
         * 
         * @param properties
         *                   the configuration data.
         * @return the associated node built from the template.
         */
        protected Built generateNode(Map<String,String> properties) {
            String processedHtml = html;
            if (properties != null) {
                for (Entry<String,String> pair : properties.entrySet())
                    processedHtml = processedHtml.replace("${" + pair.getKey() + "}", pair.getValue());
            }
            Element node = DomGlobal.document.createElement("div");
            node.innerHTML = processedHtml;
            Element insert = node.firstElementChild;
            node.remove();
            if (!includesContent) 
                return new Built(insert, null);
            Element contentEl = JQuery.$(insert).find("#" + UID).get(0);
            Element contentParentEl = contentEl.parentElement;
            contentEl.remove();
            return new Built(insert, contentParentEl);
        }

    }
}
