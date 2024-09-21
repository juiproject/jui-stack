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
package com.effacy.jui.ui.client.gallery.item;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Cursor;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.dom.renderer.IDataRenderer;
import com.effacy.jui.core.client.dom.renderer.template.ConditionBuilder;
import com.effacy.jui.core.client.dom.renderer.template.ITemplateBuilder;
import com.effacy.jui.core.client.dom.renderer.template.Provider;
import com.effacy.jui.core.client.dom.renderer.template.ProviderBuilder;
import com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder;
import com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Container;
import com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Node;
import com.effacy.jui.core.client.dom.renderer.template.items.BuilderItem;
import com.effacy.jui.core.client.dom.renderer.template.items.BuilderItems;
import com.effacy.jui.core.client.util.UID;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.With;
import com.effacy.jui.ui.client.gallery.IGalleryItem;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.effacy.jui.ui.client.parts.ContextMenu;
import com.effacy.jui.ui.client.parts.ContextMenu.IContextMenuCSS;
import com.google.gwt.core.client.GWT;

/**
 * A standard panel gallery item.
 * <p>
 * This can be used outright or to serve as a template for a custom item.
 *
 * @author Jeremy Buckley
 */
public abstract class PanelGalleryItem<R, D> extends DataRendererGalleryItem<R, D> {

    /**
     * Convenience to provide a gallery supplier for the item.
     * 
     * @param <R>
     *                the record type.
     * @param builder
     *                the builder for the item.
     * @return the supplier for use in a gallery.
     */
    public static <R, D> Supplier<IGalleryItem<R>> create(final Function<R, D> converter, final Consumer<PanelGalleryItem<R, D>.PanelGalleryItemCreator> builder) {
        String uid = UID.createUID ();
        return () -> new PanelGalleryItem<R, D> ("PanelGalleryItem_" + uid) {

            /**
             * {@inheritDoc}
             *
             * @see com.effacy.jui.ui.client.gallery.item.DataRendererGalleryItem#convert(java.lang.Object)
             */
            @Override
            protected D convert(R record) {
                return converter.apply (record);
            }

            /**
             * {@inheritDoc}
             *
             * @see com.effacy.jui.ui.client.gallery.item.PanelGalleryItem2#configure(com.effacy.jui.ui.client.gallery.item.PanelGalleryItem2.PanelGalleryItemCreator)
             */
            @Override
            protected void configure(PanelGalleryItemCreator renderer) {
                builder.accept (renderer);
            }

        };
    }

    /**
     * Convenience to provide a gallery supplier for the item.
     * 
     * @param <R>
     *                the record type.
     * @param builder
     *                the builder for the item.
     * @return the supplier for use in a gallery.
     */
    public static <R> Supplier<IGalleryItem<R>> create(final Consumer<PanelGalleryItem<R, R>.PanelGalleryItemCreator> builder) {
        String uid = UID.createUID ();
        return () -> new PanelGalleryItem<R, R> ("PanelGalleryItem_" + uid) {

            /**
             * {@inheritDoc}
             *
             * @see com.effacy.jui.ui.client.gallery.item.DataRendererGalleryItem#convert(java.lang.Object)
             */
            @Override
            protected R convert(R record) {
                return record;
            }

            /**
             * {@inheritDoc}
             *
             * @see com.effacy.jui.ui.client.gallery.item.PanelGalleryItem2#configure(com.effacy.jui.ui.client.gallery.item.PanelGalleryItem2.PanelGalleryItemCreator)
             */
            @Override
            protected void configure(PanelGalleryItemCreator renderer) {
                builder.accept (renderer);
            }

        };
    }

    /**
     * Construct an instance of the gallery item.
     * 
     * @param cacheKey
     *                 (optional) the cache key to use for this item.
     */
    protected PanelGalleryItem(String cacheKey) {
        super (cacheKey);
    }

    /************************************************************************
     * Internal methods
     ************************************************************************/

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.ui.client.gallery.item.DataRendererGalleryItem#createRenderer()
     */
    @Override
    protected IDataRenderer<D> createRenderer() {
        PanelGalleryItemCreator creator = new PanelGalleryItemCreator ();
        configure (creator);
        return ITemplateBuilder.<D>renderer (creator);
    }

    /**
     * Called by {@link #createRenderer()} to configure a
     * {@link GenevaGalleryItemRenderer}. One may override this or
     * {@link #createRenderer()}.
     * 
     * @param creator
     *                the creator to configure.
     */
    protected abstract void configure(PanelGalleryItemCreator creator);

    /************************************************************************
     * Renderer
     ************************************************************************/

    /**
     * The renderer used to render the gallery item.
     */
    public class PanelGalleryItemCreator extends BuilderItem<D> {

        @Override
        @SuppressWarnings("unchecked")
        public Node<D> buildImpl(Container<D> parent) {
            TemplateBuilder.Element<D> main = parent.div ();
            if (clickHandler != null) {
                main.id ("action");
                main.setAttribute ("item", "action-panel");
                // Register a panel click handler but set the order high so that it is always
                // the last to be invoked.
                main.on (e -> clickHandler.accept (((IGalleryItem<R>) e.getSource ())), 1000, UIEventType.ONCLICK);
                CSS.CURSOR.apply (main, Cursor.POINTER);
            }
            if (width != null)
                CSS.WIDTH.apply (main, width);
            if (height != null)
                CSS.HEIGHT.apply (main, height);
            if (header != null)
                header.build (main);
            sections.build (main);
            return main;
        }

        /**
         * See {@link #width(Length)}.
         */
        private Length width;

        /**
         * See {@link #height(Length)}.
         */
        private Length height;

        /**
         * See {@link #clickHandler(Consumer)}.
         */
        private Consumer<IGalleryItem<R>> clickHandler;

        /**
         * See {@link #header(Provider)}.
         */
        private Header header;

        /**
         * The title. This is given as a collection to allow for multiple title variants
         * (using conditions to separate them).
         */
        private BuilderItems<D, BuilderItem<D>> sections = new BuilderItems<D, BuilderItem<D>> ();

        /**
         * Title item which appears at the top of the gallery item.
         */
        public class Header extends BuilderItem<D> {

            /**
             * See {@link #clickHandler(Consumer)}.
             */
            private Consumer<IGalleryItem<R>> clickHandler;

            /**
             * Provider for the title.
             */
            private Provider<String, D> title;

            /**
             * See {@link #titleIcon(Provider)}.
             */
            private Provider<String, D> titleIcon;

            /**
             * See {@link #titleAvatar(Provider)}.
             */
            private Provider<String, D> titleAvatar;

            /**
             * See {@link #subtitle(Provider)}.
             */
            private Provider<String, D> subtitle;

            /**
             * See {@link #menu()}.
             */
            private ContextMenu<D, IGalleryItem<R>> menu;

            /**
             * Construct a title with a provider for the actual title.
             * 
             * @param title
             *              the title.
             */
            public Header(Provider<String, D> title) {
                this.title = title;
            }

            /**
             * Adds a click handler for the title. When set this will allows the title to be
             * clickable.
             * 
             * @param clickHandler
             *                     the click handler.
             * @return this renderer instance.
             */
            public Header clickHandler(Consumer<IGalleryItem<R>> clickHandler) {
                this.clickHandler = clickHandler;
                return this;
            }

            /**
             * Provides an icon (appears to the left of the title).
             * 
             * @param icon
             *             the icon.
             * @return this item.
             */
            public Header titleIcon(String icon) {
                if (icon != null)
                    this.titleIcon = ProviderBuilder.string (icon);
                return this;
            }

            /**
             * Provides an icon (appears to the left of the title).
             * 
             * @param icon
             *             the icon (via provider).
             * @return this instance.
             */
            public Header titleIcon(Provider<String, D> icon) {
                this.titleIcon = icon;
                return this;
            }

            /**
             * Provides an URL for an avatar (appears to the left of the title).
             * <p>
             * This takes precedence over {@link #titleIcon(Provider)} when the URL is not
             * empty (or {@code null}).
             * 
             * @param avatarUrl
             *                  the URL of the avatar (via provider).
             * @return this instance.
             */
            public Header titleAvatar(Provider<String, D> avatarUrl) {
                this.titleAvatar = avatarUrl;
                return this;
            }

            /**
             * Provide a sub-title.
             * 
             * @param subtitle
             *                 the sub-title.
             * @return this item.
             */
            public Header subtitle(String subtitle) {
                if (subtitle != null)
                    this.subtitle = ProviderBuilder.string (subtitle);
                return this;
            }

            /**
             * Provide a sub-title.
             * 
             * @param subtitle
             *                 the sub-title.
             * @return this instance.
             */
            public Header subtitle(Provider<String, D> subtitle) {
                this.subtitle = subtitle;
                return this;
            }

            /**
             * Obtains the context menu for configuration.
             * 
             * @return the menu.
             */
            public ContextMenu<D, IGalleryItem<R>> menu() {
                if (menu == null)
                    menu = new ContextMenu<D, IGalleryItem<R>> (styles (), Insets.ln (null, Length.em (-0.5), Length.em (-0.75), null));
                return menu;
            }

            /**
             * Configure the context menu.
             * 
             * @param menu
             *             the configurer.
             * @return this instance.
             */
            public Header menu(Consumer<ContextMenu<D, IGalleryItem<R>>> menu) {
                if (menu != null)
                    menu.accept (menu ());
                return this;
            }

            /**
             * {@inheritDoc}
             *
             * @see com.effacy.jui.core.client.dom.renderer.template.items.BuilderItem#buildImpl(com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Container)
             */
            @Override
            @SuppressWarnings("unchecked")
            protected Node<D> buildImpl(Container<D> parent) {
                return parent.div (header -> {
                    header.addClassName (styles ().header ());
                    if (titleIcon != null) {
                        header.em ().condition (ConditionBuilder.<D>notEmptyCondition (titleIcon).and (ConditionBuilder.<D>emptyCondition (titleAvatar))) //
                                .addClassName (titleIcon) //
                                .addClassName (ProviderBuilder.string (styles ().large (), ConditionBuilder.<D>notEmptyCondition (subtitle)));
                    }
                    if (titleAvatar != null) {
                        header.span (avatar -> {
                            avatar.condition (ConditionBuilder.<D>notEmptyCondition (titleAvatar));
                            avatar.addClassName (styles ().avatar ());
                            avatar.img (titleAvatar);
                        });
                    }
                    header.div (body -> {
                        if (clickHandler == null) {
                            body.h6 ().by ("header").text (this.title);
                        } else {
                            With.$ (body.a (), a -> {
                                a.on (e -> clickHandler.accept (((IGalleryItem<R>) e.getSource ())), UIEventType.ONCLICK);
                                a.text (this.title);
                                a.setAttribute ("item", "header-title");
                            });
                        }
                        body.p ().text (subtitle).condition (ConditionBuilder.<D>notEmptyCondition (subtitle));
                    });
                    if (menu != null) {
                        header.span (span -> {
                            span.addClassName (styles ().menu ());
                            span.a (act -> {
                                act.condition (d -> !menu.isEmpty ());
                                act.by (ContextMenu.BY_MENU_ACTIVATOR);
                                act.addClassName (FontAwesome.ellipsisV (FontAwesome.Option.BOLD));
                            });
                            menu.build (span);
                        });

                    }
                });
            }

        }

        public class Section<V> extends BuilderItem<V> {

            /**
             * {@inheritDoc}
             *
             * @see com.effacy.jui.core.client.dom.renderer.template.items.BuilderItem#buildImpl(com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Container)
             */
            @Override
            protected Node<V> buildImpl(Container<V> parent) {
                return parent.div (section -> {
                    section.text ("Hubba");
                });
            }

        }

        /**
         * Fixes a width for the panel.
         * 
         * @param width
         *              the width.
         * @return this renderer instance.
         */
        public PanelGalleryItemCreator width(Length width) {
            this.width = width;
            return this;
        }

        /**
         * Fixes a height for the panel.
         * 
         * @param height
         *               the height.
         * @return this renderer instance.
         */
        public PanelGalleryItemCreator height(Length height) {
            this.height = height;
            return this;
        }

        /**
         * Adds a click handler for the panel.
         * 
         * @param clickHandler
         *                     the click handler.
         * @return this renderer instance.
         */
        public PanelGalleryItemCreator clickHandler(Consumer<IGalleryItem<R>> clickHandler) {
            this.clickHandler = clickHandler;
            return this;
        }

        /**
         * Configures the header.
         * 
         * @param title
         *              the title of the header.
         * @return the header to further configure.
         */
        public Header header(Provider<String, D> title) {
            if (header == null)
                header = new Header (title);
            return header;
        }

        /**
         * Configures the header.
         * 
         * @param title
         *                  the title of the header.
         * @param configure
         *                  to configure the header.
         * @return this renderer.
         */
        public PanelGalleryItemCreator header(Provider<String, D> title, Consumer<Header> configure) {
            Header header = header (title);
            if (configure != null)
                configure.accept (header);
            return this;
        }

        /**
         * Adds a section to the panel.
         * 
         * @return the section.
         */
        public Section<D> section() {
            Section<D> section = new Section<D> ();
            sections.add (section);
            return section;
        }

        /**
         * Adds a section to the panel.
         * 
         * @param configure
         *                  to configure the section.
         * @return this renderer.
         */
        public PanelGalleryItemCreator section(Consumer<Section<D>> configure) {
            Section<D> section = section ();
            if (configure != null)
                configure.accept (section);
            return this;
        }
    }

    /************************************************************************
     * CSS
     ************************************************************************/

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.ui.client.gallery.item.DataRendererGalleryItem#styles()
     */
    @Override
    protected ILocalCSS styles() {
        return LocalCSS.instance ();
    }

    /**
     * Styles for the standard item.
     */
    public static interface ILocalCSS extends IContextMenuCSS {

        public String header();

        public String large();

        public String avatar();
    }

    /**
     * Component CSS (standard pattern).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        IContextMenuCSS.CSS,
        "com/effacy/jui/ui/client/gallery/item/PanelGalleryItem.css",
        "com/effacy/jui/ui/client/gallery/item/PanelGalleryItem_Override.css"
    })
    public static abstract class LocalCSS implements ILocalCSS {

        private static LocalCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (LocalCSS) GWT.create (LocalCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }
}
