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
package com.effacy.jui.core.client.component;

import com.effacy.jui.platform.css.client.CssDeclaration;

public interface IComponentCSS extends CssDeclaration {

    /**
     * Default component CSS source location for use with defining styles. This is
     * just a convenience.
     */
    public final static String COMPONENT_CSS = "com/effacy/jui/core/client/component/Component.css";

    /**
     * The top-level component CSS.
     */
    public String component();

    /**
     * Style that is applied when the component is disabled.
     */
    public String disabled();

    /**
     * Style that is applied when the component has gained focus.
     */
    public String focus();

    /**
     * This can be used when there is no generation of the CSS file and the styles
     * reference globally provided styles from a globally provisioned style sheet
     * (for example, when trying to use advanced CSS).
     * <p>
     * If using this approach then the style methods should return the styles as
     * they appear in the style sheet. One should also override the
     * {@link #component()} styles to scope the component in a unique manner. That
     * will avoid CSS conflicts.
     */
    public static class GlobalComponentCSS implements IComponentCSS {

        @Override
        public boolean ensureInjected() {
            return true;
        }

        @Override
        public String getCssText() {
            return "";
        }

        @Override
        public String component() {
            return "component";
        }

        @Override
        public String disabled() {
            return "disabled";
        }

        @Override
        public String focus() {
            return "focus";
        }

    }
}
