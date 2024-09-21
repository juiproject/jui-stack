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
package com.effacy.jui.platform.css.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used on a class implementing {@link CssDeclaration} to guide how the class
 * should be generated (including which CSS resources to use).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface CssResource {

    /**
     * Reference to the the sources that should be used.
     * 
     * @return the source for the template.
     */
    public String [] value() default {};

    /**
     * The various means of combining CSS files (see {@link #values()}).
     */
    public enum Combine {

        /**
         * Appends the contents in the same order as the resources are declared.
         */
        APPEND,
        
        /**
         * Replaces selectors with declarations that appear in later CSS files. Note
         * that the selectors must match completely.
         */
        REPLACE,
        
        /**
         * Merges delcaration for matching selectors.
         */
        MERGE;
    }

    /**
     * Describes how the CSS should be combined if there is more than one.
     * 
     * @return the combination model.
     */
    public Combine combine() default Combine.APPEND;

    /**
     * Strict mode requires that every styles represented in the declaration has a
     * corresponding style in the CSS file.
     * <p>
     * This has no impact on the styles in the CSS file, there can be declared
     * styles that do not have a corresponding entry and these will be passed
     * through unaffected.
     * 
     * @return {@code true} if strict (default is {@code true}).
     */
    public boolean strict() default true;

    /**
     * Generates a map of selectors and declarations that can be inspected
     * programatically.
     * 
     * @return {@code true} if to do so (default is not to).
     */
    public boolean generateCssDecarations() default false;

    /**
     * Can be used to map a style name in the CSS resource to a method name (without
     * further obfuscation).
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.METHOD })
    public @interface UseStyle {

        /**
         * The name to use.
         * 
         * @return the name.
         */
        public String value();
    }

    /**
     * Used to declare a font with associated resources that need to be injected.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE })
    @Repeatable(Fonts.class)
    public @interface Font {

        /**
         * The name of the font.
         * 
         * @return the name.
         */
        public String name();

        /**
         * The font style.
         * 
         * @return the style.
         */
        public String style() default "normal";

        /**
         * The font weight.
         * 
         * @return the weight.
         */
        public String weight() default "400";

        /**
         * If not to inline.
         * 
         * @return {@code true} to not inline (default is {@code false}).
         */
        public boolean noinline() default false;

        /**
         * The sources to inline or reference.
         * 
         * @return the sources.
         */
        public String [] sources() default {};

        /**
         * If the sources reference artefacts already in the module base.
         * 
         * @return {@code true} if so.
         */
        public boolean useModuleBase() default false;

        /**
         * Allows for optional sources. If a source could not be found then it is simply
         * excluded. If no sources are found then the font is not included.
         * <p>
         * The motivation for this is to allow user-supplied font files which may be
         * subject to license restructions.
         * 
         * @return {@code true} if so.
         */
        public boolean optional() default false;
    }

    /**
     * Used to bundle multiple font declarations.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE })
    public @interface Fonts {

        /**
         * The fonts being declared.
         * 
         * @return the fonts.
         */
        public Font [] value();
    }
}
