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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import com.effacy.jui.core.client.component.Component;
import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.dom.DomSupport;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.builder.DomBuilder;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.jquery.JQuery;
import com.effacy.jui.core.client.dom.renderer.template.ITemplateBuilder;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.platform.util.client.With;
import com.effacy.jui.ui.client.NotificationBlock.NotificationBuilder.Notification;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;

/**
 * Represents a mechanism to present a notice (such as an error message).
 *
 * @author Jeremy Buckley
 */
public class NotificationBlock extends Component<NotificationBlock.Config> {

    /************************************************************************
     * Configuration
     ************************************************************************/

    /**
     * Configuration for the component.
     */
    public static class Config extends Component.Config {

        /**
         * Style direction for the component.
         */
        public interface Style {

            /**
             * The CSS styles.
             * 
             * @return the styles.
             */
            public ILocalCSS styles();

            /**
             * Convenience to create a styles instance from the given data.
             * 
             * @param styles
             *               the styles.
             * @return the style instance.
             */
            public static Style create(final ILocalCSS styles) {
                return new Style () {

                    @Override
                    public ILocalCSS styles() {
                        return styles;
                    }
                };
            }

            /**
             * Default styling.
             */
            public static final Style STANDARD = create (StandardLocalCSS.instance ());

            /**
             * Compact styling.
             */
            public static final Style STANDARD_COMPACT = create (StandardCompactLocalCSS.instance ());

            /**
             * Full width styling.
             */
            public static final Style FULL = create (FullLocalCSS.instance ());
        }

        /**
         * See {@link #getStyle()}.
         */
        private Style style = Style.STANDARD;

        /**
         * See {@link #padding(Insets)}.
         */
        private Insets padding;

        /**
         * Construct with defaults.
         */
        public Config() {
            super ();
        }

        /**
         * Construct with a specific style.
         * <p>
         * See also {@link StyledComponentTemplate.Styles}.
         * 
         * @param style
         *              the style.
         */
        public Config(Style style) {
            if (style != null)
                this.style = style;
        }

        /**
         * The style to apply to the section.
         * 
         * @return the style.
         */
        public Style getStyle() {
            return style;
        }

        /**
         * Apply padding around the messages.
         * 
         * @param padding
         *                the padding to apply.
         * @return this configuration instance.
         */
        public Config padding(Insets padding) {
            this.padding = padding;
            return this;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.component.Component.Config#build(com.effacy.jui.core.client.component.layout.LayoutData[])
         */
        @Override
        @SuppressWarnings("unchecked")
        public NotificationBlock build(LayoutData... data) {
            return build (new NotificationBlock (this), data);
        }

    }

    /**
     * Used to build out the contents of the notification.
     */
    public abstract static class NotificationBuilder {

        /**
         * The various themes a notification block has have.
         */
        public enum Theme {
            NOTICE, ERROR, SUCCESS;
        }

        /**
         * A block of messages.
         */
        public class Notification {

            public class Line {

                private String contents;

                public Line contents(String contents) {
                    this.contents = contents;
                    return this;
                }

                public Theme getTheme() {
                    return Notification.this.theme;
                }
            }

            /**
             * The style of messages.
             */
            private Theme theme;

            /**
             * The title.
             */
            private String title;

            /**
             * The main content body.
             */
            private String content;

            /**
             * Lines of messages.
             */
            private List<Line> lines = new ArrayList<Line> ();

            /**
             * Construct with initial configuration.
             * 
             * @param style
             *              the style of block.
             */
            public Notification(Theme theme) {
                this.theme = (theme == null) ? Theme.NOTICE : theme;
            }

            /**
             * Assigns a title.
             * 
             * @param title
             *              the title.
             * @return this content block.
             */
            public Notification title(String title) {
                this.title = title;
                return this;
            }

            /**
             * Assigns the main content body.
             * 
             * @param content
             *                the content.
             * @return this content block.
             */
            public Notification content(String content) {
                this.content = content;
                return this;
            }

            public Notification line(Consumer<Line> config) {
                Line line = line ();
                config.accept (line);
                return this;
            }

            public Line line() {
                Line line = new Line ();
                lines.add (line);
                return line;
            }

            public Notification line(String contents) {
                Line line = line ();
                line.contents (contents);
                return this;
            }

            public Notification line(Collection<String> contents) {
                for (String content : contents) {
                    Line line = line ();
                    line.contents (content);
                }
                return this;
            }

            /**
             * Short cut to render the builder.
             */
            public void render() {
                NotificationBuilder.this.render ();
            }

        }

        /**
         * Collection of notifications.
         */
        protected List<Notification> notifications = new ArrayList<Notification> ();

        /**
         * Creates a notification.
         * 
         * @param theme
         *              the theme to apply.
         * @return the notification.
         */
        public Notification notification(NotificationBuilder.Theme theme) {
            Notification notification = new Notification (theme);
            notifications.add (notification);
            return notification;
        }

        /**
         * See {@link #notification()} but provides a lambda-friendly mechanism for
         * configuring the notification.
         * <p>
         * The notification will automatically be rendered.
         * 
         * @param theme
         *               the theme to apply.
         * @param config
         *               the configuration.
         * @return this builder instance.
         */
        public NotificationBuilder notification(NotificationBuilder.Theme theme, Consumer<Notification> config) {
            Notification notification = notification (theme);
            config.accept (notification);
            notification.render ();
            return this;
        }

        /**
         * Render the messages.
         */
        public abstract void render();
    }

    /************************************************************************
     * Construction
     ************************************************************************/

    /**
     * Construct with configuration.
     * 
     * @param config
     *               the configuration.
     */
    public NotificationBlock(NotificationBlock.Config config) {
        super (config);
    }

    /**
     * Obtain a builder to build a set of messages from. Once build one must call
     * {@link NotificationBuilder#render()} (or {@link Notification#render()}) to
     * render the messages into the panel.
     * 
     * @return a builder.
     */
    public NotificationBuilder builder() {
        return new NotificationBuilder () {

            @Override
            public void render() {
                NotificationBlock.this.render (this);
            }

        };
    }

    /**
     * Clears out the contents of the block. This does not hide the component but
     * the component will not display any content or have any height.
     * <p>
     * Some layouts re-configure when a component is hidden so that should be a
     * consideration.
     */
    public void clear() {
        builder ().render ();
    }

    /************************************************************************
     * Presentation
     ************************************************************************/

    /**
     * Messages area.
     */
    protected Element contentEl;

    /**
     * Messages to render if set prior to rendering.
     */
    private NotificationBuilder builderPreRender;

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#buildNode(com.effacy.jui.core.client.component.Component.Config)
     */
    @Override
    protected INodeProvider buildNode(Config data) {
        return DomBuilder.div (wrap -> {
            wrap.by ("wrap");
            wrap.addClassName (styles ().wrap ());
        }).build (tree -> {
            contentEl = tree.first ("wrap");
        });
    }

    /**
     * Render content for the notification based on the configuration of the passed
     * builder.
     * 
     * @param builder
     *                the builder.
     */
    public void render(NotificationBuilder builder) {
        if (!isRendered ()) {
            builderPreRender = builder;
        } else {
            DomSupport.removeAllChildren (contentEl);
            if ((builder == null) || builder.notifications.isEmpty ()) {
                JQuery.$ (contentEl).hide ();
            } else {
                JQuery.$ (contentEl).show ();
                ITemplateBuilder.<NotificationBuilder>renderer ("NotificationBlock_" + styles ().getClass ().getName (), root -> {
                    root.div (outer -> {
                        With.$ (outer.loop (v -> v.notifications), notification -> {
                            notification.addClassName (n -> {
                                if (n.theme == NotificationBuilder.Theme.SUCCESS)
                                    return styles ().theme_success ();
                                if (n.theme == NotificationBuilder.Theme.ERROR)
                                    return styles ().theme_error ();
                                return styles ().theme ();
                            });
                            notification.addClassName (styles ().notification ());
                            notification.div (side -> {
                                side.addClassName (styles ().left ());
                                side.em ().condition (d -> d.theme == NotificationBuilder.Theme.NOTICE).addClassName (FontAwesome.circleExclamation ());
                                side.em ().condition (d -> d.theme == NotificationBuilder.Theme.SUCCESS).addClassName (FontAwesome.circleCheck ());
                                side.em ().condition (d -> d.theme == NotificationBuilder.Theme.ERROR).addClassName (FontAwesome.circleExclamation ());
                            });
                            notification.h3 (h -> {
                                h.condition (d -> !StringSupport.empty (d.title));
                                h.text (d -> d.title);
                            });
                            notification.p (p -> {
                                p.condition (n -> !StringSupport.empty (n.content));
                                p.span ().text (d -> d.content);
                            });
                            notification.ul (ul -> {
                                ul.condition (w -> !w.lines.isEmpty ());
                                With.$ (ul.li ().loop (w -> w.lines), w -> {
                                    w.span ().text (d -> d.contents);
                                });
                            });
                        });
                    });
                }).render (contentEl, builder);
            }
        }
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
            config ().padding.padding (contentEl);

        if (builderPreRender != null) {
            render (builderPreRender);
            builderPreRender = null;
        } else
            JQuery.$ (contentEl).hide ();
    }

    /************************************************************************
     * CSS styles
     ************************************************************************/

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#styles()
     */
    public ILocalCSS styles() {
        return config ().style.styles ();
    }

    public static interface ILocalCSS extends IComponentCSS {

        /**
         * Base CSS.
         */
        public static final String CSS = "com/effacy/jui/ui/client/NotificationBlock.css";

        /**
         * Base CSS override.
         */
        public static final String CSS_OVERRIDE = "com/effacy/jui/ui/client/NotificationBlock_Override.css";

        /**
         * Theme for {@link NotificationBuilder.Theme#NOTICE}.
         */
        public String theme();

        /**
         * Theme for {@link NotificationBuilder.Theme#SUCCESS}.
         */
        public String theme_success();

        /**
         * Theme for {@link NotificationBuilder.Theme#ERROR}.
         */
        public String theme_error();

        /**
         * Wraps the collection of notifications.
         */
        public String wrap();

        /**
         * Left section.
         */
        public String left();

        /**
         * Wraps a single notification.
         * 
         * @return the notification.
         */
        public String notification();

    }

    /**
     * Component CSS (standard).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        ILocalCSS.CSS,
        ILocalCSS.CSS_OVERRIDE
    })
    public static abstract class StandardLocalCSS implements ILocalCSS {

        private static StandardLocalCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (StandardLocalCSS) GWT.create (StandardLocalCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }

    /**
     * Component CSS (standard).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        ILocalCSS.CSS, ILocalCSS.CSS_OVERRIDE,
        "com/effacy/jui/ui/client/NotificationBlock_Compact.css",
        "com/effacy/jui/ui/client/NotificationBlock_Compact_Override.css"
    })
    public static abstract class StandardCompactLocalCSS implements ILocalCSS {

        private static StandardCompactLocalCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (StandardCompactLocalCSS) GWT.create (StandardCompactLocalCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }

    /**
     * Component CSS (standard).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        ILocalCSS.CSS,
        ILocalCSS.CSS_OVERRIDE,
        "com/effacy/jui/ui/client/NotificationBlock_Full.css",
        "com/effacy/jui/ui/client/NotificationBlock_Full_Override.css"
    })
    public static abstract class FullLocalCSS implements ILocalCSS {

        private static FullLocalCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (FullLocalCSS) GWT.create (FullLocalCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }

}
