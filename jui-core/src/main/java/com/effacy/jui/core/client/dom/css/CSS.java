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
package com.effacy.jui.core.client.dom.css;

import com.effacy.jui.core.client.dom.jquery.JQuery;
import com.effacy.jui.core.client.dom.jquery.JQueryElement;
import com.effacy.jui.core.client.dom.renderer.template.ProviderBuilder;
import com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder;

import elemental2.dom.Element;

public class CSS {

    public interface ICSSProperty {
        /**
         * The CSS property value as a string.
         */
        public String value();
    }

    public static Element apply(Element el, CSSPropertyApplier... properties) {
        apply (JQuery.$ (el), properties);
        return el;
    }

    /**
     * Applies the passed CSS properties to the given element.
     * 
     * @param el
     *                   the element to apply to.
     * @param properties
     *                   the properties appliers to execute.
     * @return the passed element.
     */
    public static JQueryElement apply(JQueryElement el, CSSPropertyApplier... properties) {
        for (CSSPropertyApplier property : properties) {
            if (property == null)
                continue;
            property.apply (el);
        }
        return el;
    }

    /**
     * Various standard CSS properties.
     * <p>
     * Note that not all properties will be represented here. Overtime this will
     * expand but if a property is not represented one will need to treat it as a
     * property-value pair.
     */

    public static final LengthProperty FONT_SIZE = new LengthProperty ("fontSize");

    public static final DecimalProperty FONT_WEIGHT = new DecimalProperty ("fontWeight");

    public static final LengthProperty WIDTH = new LengthProperty ("width");

    public static final LengthProperty MIN_WIDTH = new LengthProperty ("minWidth");

    public static final LengthProperty MAX_WIDTH = new LengthProperty ("maxWidth");

    public static final LengthProperty HEIGHT = new LengthProperty ("height");

    public static final LengthProperty MIN_HEIGHT = new LengthProperty ("minHeight");

    public static final LengthProperty MAX_HEIGHT = new LengthProperty ("maxHeight");

    public static final LengthProperty GAP = new LengthProperty ("gap");

    public static final LengthProperty TOP = new LengthProperty ("top");

    public static final LengthProperty BOTTOM = new LengthProperty ("bottom");

    public static final LengthProperty LEFT = new LengthProperty ("left");

    public static final LengthProperty RIGHT = new LengthProperty ("right");

    public static final InsetsProperty PADDING = new InsetsProperty ("padding");

    public static final LengthProperty PADDING_LEFT = new LengthProperty ("paddingLeft");

    public static final LengthProperty PADDING_RIGHT = new LengthProperty ("paddingRight");

    public static final LengthProperty PADDING_TOP = new LengthProperty ("paddingTop");

    public static final LengthProperty PADDING_BOTTOM = new LengthProperty ("paddingBottom");

    public static final InsetsProperty MARGIN = new InsetsProperty ("margin");

    public static final LengthProperty MARGIN_LEFT = new LengthProperty ("marginLeft");

    public static final LengthProperty MARGIN_RIGHT = new LengthProperty ("marginRight");

    public static final LengthProperty MARGIN_TOP = new LengthProperty ("marginTop");

    public static final LengthProperty MARGIN_BOTTOM = new LengthProperty ("marginBottom");

    public static final PositionProperty POSITION = new PositionProperty ("position");

    public static final TextAlignProperty TEXT_ALIGN = new TextAlignProperty ("textAlign");

    public static final LengthProperty INSETS = new LengthProperty ("insets");

    public static final CursorProperty CURSOR = new CursorProperty ("cursor");

    public static final ColorProperty COLOR = new ColorProperty ("color");

    public static final ColorProperty BACKGROUND_COLOR = new ColorProperty ("backgroundColor");

    /************************************************************************
     * Property implementation classes
     ************************************************************************/

    public interface CSSPropertyApplier {

        /**
         * Applies the CSS embodied by this applier to the given element.
         * 
         * @param el
         *           the element.
         * @return the passed element.
         */
        default public Element apply(Element el) {
            apply (JQuery.$ (el));
            return el;
        }

        public <E extends TemplateBuilder.Element<?>> E apply(E el);

        /**
         * Applies the CSS embodied by this applier to the given element specification.
         * 
         * @param el
         *           the element specification.
         * @return the passed element specification.
         */
        public JQueryElement apply(JQueryElement el);

    }

    public static abstract class CSSProperty<V extends ICSSProperty> {

        public Element apply(Element el, V value) {
            apply (JQuery.$ (el), value);
            return el;
        }

        /**
         * Applies the CSS to a {@link TemplateBuilder} element.
         */
        public <E extends TemplateBuilder.Element<?>> E apply(E el, V value) {
            if (value == null)
                el.css (text (), (String) null);
            else
                el.css (text (), ProviderBuilder.string (value.value ()));
            return el;
        }

        public JQueryElement apply(JQueryElement el, V value) {
            if (value == null)
                el.css (text (), (String) null);
            else
                el.css (text (), value.value ());
            return el;
        }


        public abstract String text();

        public CSSPropertyApplier with(final V value) {
            return new CSSPropertyApplier () {

                public <E extends TemplateBuilder.Element<?>> E apply(E el) {
                    return CSSProperty.this.apply (el, value);
                }

                public JQueryElement apply(JQueryElement el) {
                    return CSSProperty.this.apply (el, value);
                }
                
            };
        }

    }

    public static class ColorProperty extends CSSProperty<Color> {

        private String property;

        public ColorProperty(String property) {
            this.property = property;
        }

        @Override
        public String text() {
            return property;
        }

    }

    public static class LengthProperty extends CSSProperty<Length> {

        private String property;

        public LengthProperty(String property) {
            this.property = property;
        }

        @Override
        public String text() {
            return property;
        }

    }

    public static class DecimalProperty extends CSSProperty<Decimal> {

        private String property;

        public DecimalProperty(String property) {
            this.property = property;
        }

        @Override
        public String text() {
            return property;
        }

    }

    public static class InsetsProperty extends CSSProperty<Insets> {

        private String property;

        public InsetsProperty(String property) {
            this.property = property;
        }

        @Override
        public String text() {
            return property;
        }

    }

    public static class PositionProperty extends CSSProperty<Position> {

        private String property;

        public PositionProperty(String property) {
            this.property = property;
        }

        @Override
        public String text() {
            return property;
        }

    }

    public static class TextAlignProperty extends CSSProperty<TextAlign> {

        private String property;

        public TextAlignProperty(String property) {
            this.property = property;
        }

        @Override
        public String text() {
            return property;
        }

    }

    public static class CursorProperty extends CSSProperty<Cursor> {

        private String property;

        public CursorProperty(String property) {
            this.property = property;
        }

        @Override
        public String text() {
            return property;
        }

    }
}
