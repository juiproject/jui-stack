/*******************************************************************************
 * Copyright 2024 Jeremy Buckley
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
package com.effacy.jui.core.client.dom.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.Node;

/**
 * Configures and builds an SVG element structure.
 */
public class Svg implements IDomInsertable {

    public static Svg $() {
        return $ (null, null);
    }

    public static Svg $(IDomInsertableContainer<?> parent) {
        return $ (parent, null);
    }

    public static Svg $(IDomInsertableContainer<?> parent, Consumer<Svg> builder) {
        Svg frg = new Svg ();
        if (builder != null)
            builder.accept (frg);
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    private int width;

    private int height;

    private String viewBox;

    private String id;

    private String fill = "none";

    private String fillRule = "evenodd";

    private List<Path> paths = new ArrayList<> ();

    /**
     * Captures the configuration for a single path element.
     */
    public class Path {
        protected List<String> styles;
        protected String fill;
        protected String d;
        protected Map<String,String> attributes;

        /**
         * Constructs a path element with required data.
         * 
         * @param fill
         *             the path fill.
         * @param d
         *             the points of the path.
         */
        protected Path(String fill, String d) {
            this.fill = fill;
            this.d = d;
        }

        public Path style(String...styles) {
            if (this.styles == null)
                this.styles = new ArrayList<>();
                for (String style : styles) {
                    if (style == null)
                        continue;
                    this.styles.add (style);
                }
            return this;
        }

        /**
         * Assigns the passed attribute with value.
         * 
         * @param name
         *              the attribute name.
         * @param value
         *              the value of the attribute.
         * @return this path instance.
         */
        public Path attr(String name, String value) {
            if (attributes == null)
                attributes = new HashMap<>();
            attributes.put (name, value);
            return this;
        }
    }

    public Svg width(int width) {
        this.width = width;
        return this;
    }

    public Svg height(int height) {
        this.height = height;
        return this;
    }

    public Svg id(String id) {
        this.id = id;
        return this;
    }

    public Svg viewBox(String viewBox) {
        this.viewBox = viewBox;
        return this;
    }

    public Svg fill(String fill) {
        this.fill = fill;
        return this;
    }

    public Svg fillRule(String fillRule) {
        this.fillRule = fillRule;
        return this;
    }
    
    /**
     * Adds a path element.
     * 
     * @param fill
     *                the fill value.
     * @param d
     *                the d value.
     * @return this SVG element.
     */
    public Svg path(String fill, String d) {
        paths.add (new Path (fill, d));
        return this;
    }

    /**
     * Adds a path element.
     * 
     * @param fill
     *                the fill value.
     * @param d
     *                the d value.
     * @param builder
     *                used to add additional configuration (i.e. attributes).
     * @return this SVG element.
     */
    public Svg path(String fill, String d, Consumer<Path> builder) {
        Path path = new Path (fill, d);
        paths.add (path);
        if (builder != null)
            builder.accept (path);
        return this;
    }


    @Override
    public void insertInto(ContainerBuilder<?> parent) {
        new SvgBuilder ().insertInto (parent);
    }
    

     /**
      * Builder for a fragment. This defers the build until the builder is actually
      * built. This allows the fragment to be configured after it is added to the
      * parent builder.
      */
    public class SvgBuilder extends DeferredContainerBuilder<SvgBuilder> {

        @Override
        protected void build(Node parent, BuildContext ctx) {
            Element svg = DomGlobal.document.createElementNS ("http://www.w3.org/2000/svg", "svg");
            if (viewBox != null)
                svg.setAttribute ("viewBox", viewBox);
            if (width > 0)
                svg.setAttribute ("width", width);
            if (height > 0)
                svg.setAttribute ("height", height);
            if (id != null)
                svg.setAttribute ("id", id);

            Element g = DomGlobal.document.createElementNS ("http://www.w3.org/2000/svg", "g");
            if (fill != null)
                g.setAttribute ("fill", fill);
            if (fillRule != null)
                g.setAttribute ("fill-rule", fillRule);
            paths.forEach (path -> {
                Element p = DomGlobal.document.createElementNS ("http://www.w3.org/2000/svg", "path");
                if (path.styles != null)
                    path.styles.forEach (style -> p.classList.add (style));
                p.setAttribute ("fill", path.fill);
                p.setAttribute ("d", path.d);
                if (path.attributes != null) {
                    path.attributes.forEach ((name, value) -> {
                        p.setAttribute (name, value);
                    });
                }
                g.appendChild (p);
            });
            svg.appendChild (g);
            parent.appendChild (svg);
        }
        
    }
}
