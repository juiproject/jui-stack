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

import java.util.function.Consumer;

import com.effacy.jui.core.client.component.Component;
import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.TemplateComponent;
import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.renderer.template.ITemplateBuilder;
import com.effacy.jui.core.client.dom.renderer.template.Provider;
import com.effacy.jui.core.client.dom.renderer.template.ProviderBuilder;
import com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Container;
import com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Node;
import com.effacy.jui.core.client.dom.renderer.template.items.BuilderItem;
import com.effacy.jui.core.client.dom.renderer.template.items.BuilderItemContainer;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.ui.client.parts.InfoLine;
import com.effacy.jui.ui.client.parts.InfoLine.IInfoLineCSS;
import com.google.gwt.core.client.GWT;

/**
 * Used to present a configurable block of information. See
 * {@link InfoBlockCreator} for details on content configuration.
 * <p>
 * Note that when rendered this will not generate any content unless (or until)
 * {@link #update(Object)} is called.
 *
 * @author Jeremy Buckley
 */
public class InfoBlock<D> extends TemplateComponent<D, InfoBlock.Config<D>> {

    /**
     * Configuration for the block.
     */
    public static class Config<D> extends Component.Config {

        /**
         * The builder to use.
         */
        private Consumer<InfoBlock<D>.InfoBlockCreator> builder;

        /**
         * See {@link #padding(Insets)}.
         */
        private Insets padding;

        /**
         * Construct configuration.
         * 
         * @param builder
         *                the builder used to build the layout of the block.
         */
        public Config(Consumer<InfoBlock<D>.InfoBlockCreator> builder) {
            this.builder = builder;
        }

        /**
         * Padding for the block.
         * 
         * @param padding
         *                the padding.
         * @return this configuration instance.
         */
        public Config<D> padding(Insets padding) {
            this.padding = padding;
            return this;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.component.Component.Config#build(com.effacy.jui.core.client.component.layout.LayoutData[])
         */
        @SuppressWarnings("unchecked")
        public InfoBlock<D> build(LayoutData... data) {
            return super.build (new InfoBlock<D> (this), data);
        }

    }

    /**
     * Construct with the default configuration.
     * 
     * @param builder
     *                the builder to use to build the layout of the block.
     */
    public InfoBlock(Consumer<InfoBlockCreator> builder) {
        this (new Config<D> (builder));
    }

    /**
     * Construct with configuration.
     * 
     * @param config
     *               the configuration.
     */
    public InfoBlock(Config<D> config) {
        super (config);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.TemplateComponent#template()
     */
    @Override
    protected ITemplateBuilder<D> template() {
        InfoBlockCreator builder = new InfoBlockCreator ();
        configure (builder);
        return builder;
    }

    /**
     * Configures the builder. This will use {@link Config#builder} if supplied.
     * 
     * @param builder
     *                the builder.
     */
    protected void configure(InfoBlockCreator builder) {
        if (config ().builder != null)
            config ().builder.accept (builder);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#onAfterRender()
     */
    @Override
    protected void onAfterRender() {
        super.onAfterRender ();

        if (config ().padding != null)
            config ().padding.padding (getRoot ());
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
    public static interface ILocalCSS extends IInfoLineCSS, IComponentCSS {

        /**
         * Delineates the item.
         */
        public String header();
    }

    /**
     * Component CSS (standard pattern).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        "com/effacy/jui/ui/client/InfoBlock.css",
        "com/effacy/jui/ui/client/InfoBlock_Override.css"
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

    /************************************************************************
     * Creator
     ************************************************************************/

    public class InfoBlockCreator extends BuilderItemContainer<D> {

        /**
         * Adds a header with title.
         * 
         * @param title
         *                the title.
         * @param builder
         *                (optional) to further build out the header.
         * @return the renderer instance.
         */
        public InfoBlockCreator header(String title, Consumer<Header> builder) {
            return header (ProviderBuilder.string (title), builder);
        }

        /**
         * Adds a header with title.
         * 
         * @param title
         *                the title.
         * @param builder
         *                (optional) to further build out the header.
         * @return the renderer instance.
         */
        public InfoBlockCreator header(Provider<String, D> title, Consumer<Header> builder) {
            Header header = new Header (title);
            items.add (header);
            if (builder != null)
                builder.accept (header);
            return this;
        }

        /**
         * Adds an {@link InfoLine} line.
         * 
         * @return the added line.
         */
        public InfoLine<D> line() {
            InfoLine<D> line = new InfoLine<D> (styles ());
            items.add (line);
            return line;
        }

        /**
         * Adds an {@link InfoLine} line.
         * 
         * @param builder
         *                (optional) to further build out the line.
         * @return this renderer instance.
         */
        public InfoBlockCreator line(Consumer<InfoLine<D>> builder) {
            InfoLine<D> line = line ();
            if (builder != null)
                builder.accept (line);
            return this;
        }

        /**
         * A header item that consists of a title and optional supporting structures.
         */
        public class Header extends BuilderItem<D> {

            /**
             * See constructor.
             */
            private Provider<String, D> title;

            /**
             * See {@link #subtitle(Provider)}.
             */
            private Provider<String, D> subtitle;

            /**
             * Construct with a title.
             * 
             * @param title
             *              the title.
             */
            public Header(Provider<String, D> title) {
                this.title = title;
            }

            /**
             * Assigns a sub-title.
             * 
             * @param subtitle
             *                 the sub-title.
             * @return this header instance.
             */
            public Header subtitle(String subtitle) {
                return subtitle (ProviderBuilder.string (subtitle));
            }

            /**
             * Assigns a sub-title.
             * 
             * @param subtitle
             *                 the sub-title.
             * @return this header instance.
             */
            public Header subtitle(Provider<String, D> subtitle) {
                this.subtitle = subtitle;
                return this;
            }

            /**
             * {@inheritDoc}
             *
             * @see com.effacy.jui.core.client.dom.renderer.template.items.BuilderItem#buildImpl(com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Container)
             */
            @Override
            protected Node<D> buildImpl(Container<D> parent) {
                return parent.div (header -> {
                    header.addClassName (styles ().header ());
                    header.h2 (h2 -> {
                        h2.text (title);
                    });
                    if (subtitle != null) {
                        header.p (p -> {
                            p.condition (subtitle);
                            p.text (subtitle).linize ();
                        });
                    }
                });
            }

        }

    }
}
