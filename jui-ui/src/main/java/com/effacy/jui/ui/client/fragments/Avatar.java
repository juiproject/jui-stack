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
package com.effacy.jui.ui.client.fragments;

import com.effacy.jui.core.client.Invoker;
import com.effacy.jui.core.client.dom.builder.ContainerBuilder;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.I;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.core.client.dom.builder.Img;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.ui.client.icon.FontAwesome;

import elemental2.dom.Element;

/**
 * Displays an avatar rendered as an image (if there is an image), as a custom
 * icon (via CSS) or as a standard icon with an optional pair of letters serving
 * as initials.
 * <p>
 * Optionally the avatar can be made clickable (see
 * {@link AvatarFragment#onclick(Invoker)}).
 * <p>
 * This also has basic provision for a failed image load (this can happen when
 * the avatar is hosted externally). In this case a "bug" icon is displayed
 * along with an error message when the user hovers over the avatar.
 */
public class Avatar {

    /**
     * Creates an unbound fragment.
     * 
     * @param href
     *             the url of the image (if this is {@code null} then a default
     *             icon will be displayed).
     * @return the fragment.
     */
    public static AvatarFragment $(String href) {
        return new AvatarFragment (href);
    }

    /**
     * Insert an avatar into the given parent.
     * 
     * @param parent
     *               the parent node.
     * @param href
     *               the url of the image (if this is {@code null} then a default
     *               icon will be displayed).
     * @return the fragment.
     */
    public static AvatarFragment $(IDomInsertableContainer<?> parent, String href) {
        AvatarFragment frg = $ (href);
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    /**
     * The border style.
     */
    public enum BorderStyle {
        NONE, SOLID, DASHED;
    }

    /**
     * Fragment implementation.
     */
    public static class AvatarFragment extends BaseFragment<AvatarFragment> {

        /**
         * See constructor.
         */
        protected String href;

        /**
         * See {@link #icon(String)}.
         */
        protected String icon;

        /**
         * See {@link #initials(String)}.
         */
        protected String initials;

        /**
         * See {@link #size(Length)}.
         */
        protected Length size;

        /**
         * See {@link #onclick(Invoker)}.
         */
        protected Invoker onclick;

        /**
         * See {@link #border(BorderStyle)}.
         */
        protected BorderStyle border = BorderStyle.SOLID;

        /**
         * See {@link #loaderror(String)}.
         */
        protected String loaderror = "Your avatar failed to load. This usually fixes itself after a little while.";

        /**
         * Construct with the CSS of an icon.
         * 
         * @param icon
         *             the icon CSS.
         */
        public AvatarFragment(String href) {
            this.href = href;
        }
        
        /**
         * Specifies an alternative error message in the case that an image load fails.
         * <p>
         * A {@code null} value disables the hover message.
         * 
         * @param loaderror
         *                  the error message.
         * @return this fragment instance.
         */
        public AvatarFragment loaderror(String loaderror) {
            this.loaderror = loaderror;
            return this;
        }

        /**
         * To display initials when there is no image.
         * <p>
         * The passed value will be converted to upper case and if more than two
         * characters in length will be transformed to be the first letter of the first
         * and last words.
         * 
         * @param initials
         *                 the initials to display.
         * @return this fragment instance.
         */
        public AvatarFragment initials(String initials) {
            if (initials == null) {
                this.initials = null;
                return this;
            }
            this.initials = StringSupport.trim(initials);
            if (StringSupport.empty(this.initials)) {
                this.initials = null;
                return this;
            }
            if (this.initials.length() > 2) {
                int i = this.initials.lastIndexOf(" ");
                if (i < 0)
                    this.initials = this.initials.substring(0,2);
                else
                    this.initials = this.initials.substring(0,1) + this.initials.substring(i + 1, i + 2);
            }
            return this;
        }

        public AvatarFragment icon(String icon) {
            if (icon != null)
                this.icon = icon;
            return this;
        }

        /**
         * The size of the avatar.
         * <p>
         * The mechanism is to apply a font size to the root element then the CSS uses
         * that as the relative measure.
         * 
         * @param size
         *             the size to apply.
         * @return this fragment instance.
         */
        public AvatarFragment size(Length size) {
            this.size = size;
            return this;
        }

        /**
         * The border style to apply.
         * 
         * @param border
         *               the style.
         * @return this fragment instance.
         */
        public AvatarFragment border(BorderStyle border) {
            if (border != null)
                this.border = border;
            return this;
        }

        /**
         * Adds an on-click handler to the avatar.
         * 
         * @param onclick
         *                the handler.
         * @return this icon instance.
         */
        public AvatarFragment onclick(Invoker onclick) {
            this.onclick = onclick;
            return this;
        }

        @Override
        protected ElementBuilder createRoot(ContainerBuilder<?> parent) {
            if (icon == null)
                icon  = FontAwesome.user ();
            return Span.$ (parent).$ (outer -> {
                outer.style ("juiAvatar", "border_" + border.name().toLowerCase());
                if (!StringSupport.empty (href)) {
                    if (loaderror != null)
                        Div.$(outer).style("hover").text(loaderror);
                    Img.$ (outer, href)
                        .use(n -> {
                            ((Element) n).onerror= (Element.OnerrorFn) (e -> {
                                n.parentElement.classList.add("failed");
                                return null;
                            });
                        });
                    Span.$(outer).style("failed").$(
                        Em.$().style(FontAwesome.bug())
                    );
                } else if (initials != null) {
                    Span.$ (outer).$ (ico -> {
                        I.$ (ico).text(initials);
                        Em.$ (ico).style (icon);
                    });
                } else {
                    Span.$ (outer).$ (ico -> {
                        Em.$ (ico).style (icon);
                    });
                }
                if (size != null)
                    outer.css (CSS.FONT_SIZE, size);
                if (onclick != null) {
                    outer.style ("clickable");
                    outer.onclick (e -> onclick.invoke());
                }
            });
        }

    }
}
