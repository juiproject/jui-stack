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
package com.effacy.jui.core.client.component.layout;

import com.effacy.jui.platform.util.client.StringSupport;

/**
 * Support class for layout data supplied to layouts.
 * 
 * @author Jeremy Buckley
 */
public class LayoutData {

    /**
     * Additional class to apply to the field.
     */
    private String extraStyle = "";

    protected LayoutData() {
        // Nothing.
    }


    /**
     * Copy constructor.
     * 
     * @param copy
     *            the data to copy.
     */
    public LayoutData(LayoutData copy) {
        extraStyle = copy.extraStyle;
    }


    /**
     * Application of an extra style class to each component being rendered.
     * 
     * @return The style class.
     */
    public String getExtraStyle() {
        return extraStyle;
    }


    /**
     * Sets an extra style class to apply to each component.
     * 
     * @param extraStyle
     *            the style class.
     */
    public void setExtraStyle(String extraStyle) {
        this.extraStyle = StringSupport.trim (extraStyle);
    }

}
