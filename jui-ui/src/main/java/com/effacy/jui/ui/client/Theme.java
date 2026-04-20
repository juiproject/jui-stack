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

    /**
     * Stable marker attribute applied to the document body once the JUI theme has
     * been initialised. External application CSS can use this rather than relying
     * on the generated theme class name.
     */
    public static final String THEME_ATTRIBUTE = "data-jui-theme";

    /**
     * Attribute used to activate an alternate palette on the document body.
     */
    public static final String PALETTE_ATTRIBUTE = "data-palette";

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

    public static Color colorInk05() {
        return Color.variable ("--jui-color-ink05");
    }

    public static Color colorInk10() {
        return Color.variable ("--jui-color-ink10");
    }

    public static Color colorInk20() {
        return Color.variable ("--jui-color-ink20");
    }

    public static Color colorInk30() {
        return Color.variable ("--jui-color-ink30");
    }

    public static Color colorInk40() {
        return Color.variable ("--jui-color-ink40");
    }

    public static Color colorInk50() {
        return Color.variable ("--jui-color-ink50");
    }

    public static Color colorInk60() {
        return Color.variable ("--jui-color-ink60");
    }

    public static Color colorInk70() {
        return Color.variable ("--jui-color-ink70");
    }

    public static Color colorInk80() {
        return Color.variable ("--jui-color-ink80");
    }

    public static Color colorInk90() {
        return Color.variable ("--jui-color-ink90");
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

    public static Color colorInfo05() {
        return Color.variable ("--jui-color-info05");
    }

    public static Color colorInfo10() {
        return Color.variable ("--jui-color-info10");
    }

    public static Color colorInfo20() {
        return Color.variable ("--jui-color-info20");
    }

    public static Color colorInfo30() {
        return Color.variable ("--jui-color-info30");
    }

    public static Color colorInfo40() {
        return Color.variable ("--jui-color-info40");
    }

    public static Color colorInfo50() {
        return Color.variable ("--jui-color-info50");
    }

    public static Color colorInfo60() {
        return Color.variable ("--jui-color-info60");
    }

    public static Color colorInfo70() {
        return Color.variable ("--jui-color-info70");
    }

    public static Color colorInfo80() {
        return Color.variable ("--jui-color-info80");
    }

    public static Color colorInfo90() {
        return Color.variable ("--jui-color-info90");
    }

    public static Color colorAuxWhite() {
        return Color.variable ("--jui-color-aux-white");
    }

    public static Color colorAuxBlack() {
        return Color.variable ("--jui-color-aux-black");
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

    /**
     * A built-in alternate colour palette. Activating a palette redefines the
     * {@code --jui-color-*} reference tokens (and, where needed, selected role
     * tokens) through the {@code [data-palette]} attribute on the document body.
     * Components do not need to change — they consume role and component-family
     * tokens, which resolve against the active palette.
     * <p>
     * This enum is only a convenience for palettes that ship with JUI. JUI
     * applications are free to define their own palette names in application CSS
     * and activate them through {@link #palette(String)}.
     */
    public enum Palette {

        /** The built-in palette (teal primary). */
        DEFAULT (null),

        /** Editorial — muted, earthen, surface-layered. */
        EDITORIAL ("editorial");

        private final String attribute;

        Palette(String attribute) {
            this.attribute = attribute;
        }

        /**
         * The value used for the {@code data-palette} attribute, or
         * {@code null} for the default palette (no attribute).
         */
        public String attribute() {
            return attribute;
        }
    }

    /**
     * Activates the given palette. Setting {@link Palette#DEFAULT} or
     * {@code null} returns to the built-in palette.
     */
    public static void palette(Palette palette) {
        palette ((palette == null) ? null : palette.attribute ());
    }

    /**
     * Activates a palette by name. This is the preferred entry point for
     * application-defined palettes that live in external CSS such as
     * {@code common.css}, {@code theme.css}, or {@code jui.css}.
     * <p>
     * Applications can define selectors such as:
     *
     * <pre>
     * body[data-jui-theme][data-palette="myapp"] {
     *     --jui-palette-primary-hue: 250;
     * }
     * </pre>
     *
     * then activate that palette with:
     *
     * <pre>
     * Theme.palette ("myapp");
     * </pre>
     *
     * Passing {@code null} or an empty string clears the active palette and
     * returns to the default theme values.
     *
     * @param palette
     *                the palette name, or {@code null} / empty to clear.
     */
    public static void palette(String palette) {
        palette = (palette == null) ? null : palette.trim ();
        if ((palette == null) || palette.isEmpty ()) {
            DomGlobal.document.body.removeAttribute (PALETTE_ATTRIBUTE);
        } else {
            DomGlobal.document.body.setAttribute (PALETTE_ATTRIBUTE, palette);
        }
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
        "com/effacy/jui/ui/client/Theme.Reference.css",
        "com/effacy/jui/ui/client/Theme.Reference.Editorial.css",
        "com/effacy/jui/ui/client/Theme.Role.css",
        "com/effacy/jui/ui/client/Theme.Scale.css",
        "com/effacy/jui/ui/client/Theme.Component.css",
        "com/effacy/jui/ui/client/Theme.Legacy.css",
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
            DomGlobal.document.body.setAttribute (THEME_ATTRIBUTE, "true");
            return true;
        }

    }
}
