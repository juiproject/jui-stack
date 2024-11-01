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
         * Construct with the CSS of an icon.
         * 
         * @param icon
         *             the icon CSS.
         */
        public AvatarFragment(String href) {
            this.href = href;
        }
        
        /**
         * The icon (CSS) to use when no avatar HREF is available.
         * <p>
         * The default is {@link FontAwesome#user()}.
         * 
         * @param icon
         *             the icon CSS to apply.
         * @return this fragment instance.
         */
        public AvatarFragment icon(String icon) {
            if (icon != null)
                this.icon = icon;
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
                    Img.$ (outer, href);
                } if (initials != null) {
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
