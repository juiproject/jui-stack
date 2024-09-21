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
package com.effacy.jui.codeserver.gwt;

import java.util.regex.Pattern;

public class Constants {
    /**
     * The URL prefix for all source maps and Java source code.
     */
    public static final String SOURCEMAP_PATH = "/sourcemaps/";

    /**
     * The suffix that Super Dev Mode uses in source map URL's.
     */
    public static final String SOURCEMAP_URL_SUFFIX = "_sourcemap.json";

    public static final String SOURCEROOT_TEMPLATE_VARIABLE = "$sourceroot_goes_here$";

    public static final Pattern STRONG_NAME = Pattern.compile("[\\dA-F]{32}");

    public static final Pattern CACHE_JS_FILE = Pattern.compile ("(" + STRONG_NAME + ").cache.js$");
  
    public static final Pattern SOURCEMAP_FILENAME_PATTERN = Pattern.compile("^(" + STRONG_NAME + ")" + Pattern.quote(SOURCEMAP_URL_SUFFIX) + "$");

    public static final Pattern SOURCEMAP_MODULE_PATTERN = Pattern.compile("^" + SOURCEMAP_PATH + "([^/]+)/");

    public static final String SYMBOLMAP_PATH = "/symbolmaps/";

    public static final String SYMBOLMAP_URL_SUFFIX = ".symbolMap";

    public static final Pattern SYMBOLMAP_FILENAME_PATTERN = Pattern.compile("^(" + STRONG_NAME + ")" + Pattern.quote(SYMBOLMAP_URL_SUFFIX) + "$");

    public static final Pattern SYMBOLMAP_MODULE_PATTERN = Pattern.compile("^" + SYMBOLMAP_PATH + "([^/]+)/");

    /**
     * The template for the sourcemap location to give the compiler.
     * It contains one template variable, __HASH__ for the strong name.
     */
    public static String sourceMapLocationTemplate(String moduleName) {
        return SOURCEMAP_PATH + moduleName + "/__HASH__" + SOURCEMAP_URL_SUFFIX;
    }
}
