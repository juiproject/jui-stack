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
package com.effacy.jui.platform.util.client;

import com.google.gwt.core.client.GWT;

/**
 * Collection of utilities for working with resources.
 */
public final class Resources {

    /**
     * Takes a resource and locates in at the source of static
     * files as declared in the module base (i.e. those that appear under the
     * {@code public} directory).
     * 
     * @param resource the resource to locate.
     * @return the url to use
     */
    public static String staticModuleBase(String resource) {
        if (StringSupport.empty (resource))
            return GWT.getModuleBaseForStaticFiles();
        if (resource.startsWith ("/"))
            resource = resource.substring (1);
        return GWT.getModuleBaseForStaticFiles() + resource;
    }
}
