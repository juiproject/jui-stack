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
package com.effacy.jui.ui.client;

import com.effacy.jui.core.client.dom.css.Color;
import com.effacy.jui.core.client.dom.jquery.JQuery;
import com.effacy.jui.core.client.dom.jquery.JQueryElement;
import com.effacy.jui.platform.css.client.CssDeclaration;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.TimerSupport;
import com.google.gwt.core.client.GWT;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;

/**
 * Standard color and geomerty theme.
 *
 * @author Jeremy Buckley
 */
public final class Theme {

    /************************************************************************
     * Standard colors
     ************************************************************************/

    public static Color colorPrimary05() {
        return Color.variable ("--jui-color-primary05");
    }

    public static Color colorPrimary10() {
        return Color.variable ("--jui-color-primary10");
    }

    public static Color colorPrimary20() {
        return Color.variable ("--jui-color-primary20");
    }

    public static Color colorPrimary30() {
        return Color.variable ("--jui-color-primary30");
    }

    public static Color colorPrimary40() {
        return Color.variable ("--jui-color-primary40");
    }

    public static Color colorPrimary50() {
        return Color.variable ("--jui-color-primary50");
    }

    public static Color colorPrimary60() {
        return Color.variable ("--jui-color-primary60");
    }

    public static Color colorPrimary70() {
        return Color.variable ("--jui-color-primary70");
    }

    public static Color colorPrimary80() {
        return Color.variable ("--jui-color-primary80");
    }

    public static Color colorPrimary90() {
        return Color.variable ("--jui-color-primary90");
    }

    public static Color colorSecondary05() {
        return Color.variable ("--jui-color-secondary05");
    }

    public static Color colorSecondary10() {
        return Color.variable ("--jui-color-secondary10");
    }

    public static Color colorSecondary20() {
        return Color.variable ("--jui-color-secondary20");
    }

    public static Color colorSecondary30() {
        return Color.variable ("--jui-color-secondary30");
    }

    public static Color colorSecondary40() {
        return Color.variable ("--jui-color-secondary40");
    }

    public static Color colorSecondary50() {
        return Color.variable ("--jui-color-secondary50");
    }

    public static Color colorSecondary60() {
        return Color.variable ("--jui-color-secondary60");
    }

    public static Color colorSecondary70() {
        return Color.variable ("--jui-color-secondary70");
    }

    public static Color colorSecondary80() {
        return Color.variable ("--jui-color-secondary80");
    }

    public static Color colorSecondary90() {
        return Color.variable ("--jui-color-secondary90");
    }

    public static Color colorTertiary05() {
        return Color.variable ("--jui-color-tertiary05");
    }

    public static Color colorTertiary10() {
        return Color.variable ("--jui-color-tertiary10");
    }

    public static Color colorTertiary20() {
        return Color.variable ("--jui-color-tertiary20");
    }

    public static Color colorTertiary30() {
        return Color.variable ("--jui-color-tertiary30");
    }

    public static Color colorTertiary40() {
        return Color.variable ("--jui-color-tertiary40");
    }

    public static Color colorTertiary50() {
        return Color.variable ("--jui-color-tertiary50");
    }

    public static Color colorTertiary60() {
        return Color.variable ("--jui-color-tertiary60");
    }

    public static Color colorTertiary70() {
        return Color.variable ("--jui-color-tertiary70");
    }

    public static Color colorTertiary80() {
        return Color.variable ("--jui-color-tertiary80");
    }

    public static Color colorTertiary90() {
        return Color.variable ("--jui-color-tertiary90");
    }

    public static Color colorNeutral05() {
        return Color.variable ("--jui-color-neutral05");
    }

    public static Color colorNeutral10() {
        return Color.variable ("--jui-color-neutral10");
    }

    public static Color colorNeutral20() {
        return Color.variable ("--jui-color-neutral20");
    }

    public static Color colorNeutral30() {
        return Color.variable ("--jui-color-neutral30");
    }

    public static Color colorNeutral40() {
        return Color.variable ("--jui-color-neutral40");
    }

    public static Color colorNeutral50() {
        return Color.variable ("--jui-color-neutral50");
    }

    public static Color colorNeutral60() {
        return Color.variable ("--jui-color-neutral60");
    }

    public static Color colorNeutral70() {
        return Color.variable ("--jui-color-neutral70");
    }

    public static Color colorNeutral80() {
        return Color.variable ("--jui-color-neutral80");
    }

    public static Color colorNeutral90() {
        return Color.variable ("--jui-color-neutral90");
    }

    public static Color colorError05() {
        return Color.variable ("--jui-color-error05");
    }

    public static Color colorError10() {
        return Color.variable ("--jui-color-error10");
    }

    public static Color colorError20() {
        return Color.variable ("--jui-color-error20");
    }

    public static Color colorError30() {
        return Color.variable ("--jui-color-error30");
    }

    public static Color colorError40() {
        return Color.variable ("--jui-color-error40");
    }

    public static Color colorError50() {
        return Color.variable ("--jui-color-error50");
    }

    public static Color colorError60() {
        return Color.variable ("--jui-color-error60");
    }

    public static Color colorError70() {
        return Color.variable ("--jui-color-error70");
    }

    public static Color colorError80() {
        return Color.variable ("--jui-color-error80");
    }

    public static Color colorError90() {
        return Color.variable ("--jui-color-error90");
    }

    public static Color colorAuxWhite() {
        return Color.variable ("--jui-color-aux-white");
    }

    public static Color colorAuxBlack() {
        return Color.variable ("--jui-color-aux-black");
    }

    public static Color colorAuxBlue() {
        return Color.variable ("--jui-color-aux-blue");
    }

    public static Color colorAuxFocus1() {
        return Color.variable ("--jui-color-aux-focus1");
    }

    public static Color colorAuxFocus2() {
        return Color.variable ("--jui-color-aux-focus2");
    }

    /************************************************************************
     * Setup
     ************************************************************************/

    /**
     * Initialises the theme (and global CSS).
     */
    public static void init() {
        ThemeCSS.init ();
    }

    /************************************************************************
     * Global styles.
     ************************************************************************/

    /**
     * The theme CSS.
     * 
     * @return the CSS styles.
     */
    public static IThemeCSS styles() {
        return ThemeCSS.instance ();
    }

    /**
     * See {@link #fade(JQueryElement)} but accepts a DOM element.
     */
    public static void fade(Element el) {
        fade (JQuery.$ (el));
    }

    /**
     * Applies a fade to the passed DOM element.
     * <p>
     * This adds a fade CSS class to the element that implements a brief fade in (a
     * transition of the opacity from 0 to 1) then a timer that removes the CSS
     * class some time later. As such, the fade can be applied multiple times to the
     * same element.
     * 
     * @param el
     *           the element to apply the fade to.
     */
    public static void fade(JQueryElement el) {
        el.addClass (ThemeCSS.instance ().fade ());
        TimerSupport.timer (() -> el.removeClass (ThemeCSS.instance ().fade ()), 500);
    }

    /********************************************************************
     * Global CSS for the theme.
     ********************************************************************/

    public interface IThemeCSS extends CssDeclaration {

        /**
         * Theme styles that contains the various color (and other) variable
         * declarations. This is applied to the body element of the page.
         * <p>
         * See {@link ThemeCSS#init()}.
         */
        public String theme();

        /**
         * This is a useful style to display a loading indicator. This should be applied
         * to a single DIV element and can be sized by apply a {@code font-size} to the
         * DIV. The DIV should also be position (i.e. using an auto margin for centering
         * and an appropriate top margin for offset). No positioning is included (other
         * than sufficient to ensure it displays).
         * <p>
         * The color of the
         * <p>
         * Access via {@link Theme#styles()}.
         */
        public String loader();

        /**
         * For a fade effect.
         */
        public String fade();
    }

    @CssResource(value = {
        "com/effacy/jui/ui/client/Theme.css",
        "com/effacy/jui/ui/client/Theme_Override.css"
    }, generateCssDecarations = true)
    public abstract static class ThemeCSS implements IThemeCSS {

        private static ThemeCSS STYLES;

        public static IThemeCSS instance() {
            if (STYLES == null) {
                STYLES = (ThemeCSS) GWT.create (ThemeCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }

        private static boolean INIT = false;

        /**
         * Initialises the CSS
         * 
         * @return {@code true} if initialised (i.e. the first call).
         */
        public static boolean init() {
            if (INIT)
                return false;
            INIT = true;
            IThemeCSS styles = instance ();
            DomGlobal.document.body.classList.add (styles.theme ());
            return true;
        }

    }
}
