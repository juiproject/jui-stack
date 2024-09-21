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
package com.effacy.jui.playground.ui.lessons;

import java.util.function.Consumer;

import com.effacy.jui.core.client.Invoker;
import com.effacy.jui.core.client.component.Component;
import com.effacy.jui.core.client.component.ComponentCreator;
import com.effacy.jui.core.client.component.ComponentCreatorSupport;
import com.effacy.jui.core.client.component.SimpleComponent;
import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.H5;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.core.client.dom.builder.P;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.ui.client.InfoBlockCreator;
import com.effacy.jui.ui.client.button.ButtonCreator;
import com.effacy.jui.ui.client.fragments.Icon;
import com.effacy.jui.ui.client.fragments.Stack;
import com.effacy.jui.ui.client.icon.FontAwesome;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;

public class Lesson1b  extends LessonPanel {

    protected Lesson1b() {

        // This display a title and descriptive text on the page.
        add (InfoBlockCreator.<Void>$ (cfg -> {}, builder -> {
            builder.header ("Part B: Prototyping", header -> {
                header.subtitle ("This lession describes how to prototype a component then create the component.");
            });
        })).update (null);

        add (ComponentCreator.build (root -> {
            Stack.$ (root).horizontal ().gap (Length.em (1)).$ (stack -> {
                ComponentCreator.$ (stack, el -> {
                    el.style ("action_panel");
                    H5.$ (el).$ (h -> {
                        Span.$ (h).text ("This is the title");
                        Icon.$ (h, FontAwesome.user ());
                    });
                    P.$ (el).text ("This is a description of the action represented by this panel");
                    Div.$ (el).$ (d -> {
                        ButtonCreator.$ (d, cfg -> {
                            cfg.label("Go there");
                            cfg.handler (() -> {
                                DomGlobal.window.alert ("CLICK!");
                            });
                        });
                    });
                });
            });
        }));

        add (ComponentCreator.build (root -> {
            Stack.$ (root).horizontal ().gap (Length.em (1)).$ (
                new ActionPanel1 (),
                new ActionPanel1 (),
                new ActionPanel1 ()
            );
        }));

        add (ComponentCreator.build (root -> {
            Stack.$ (root).horizontal ().gap (Length.em (1)).$ (stack -> {
                stack.insert (new ActionPanel1 ());
                stack.insert (new ActionPanel1 ());
                stack.insert (new ActionPanel1 ());
            });
        }));

        add (ComponentCreator.build (root -> {
            Stack.$ (root).horizontal ().gap (Length.em (1)).$ (
                new ActionPanel2.Config ()
                    .title ("Title of this one")
                    .description("Description of this one, different from the others")
                    .actionLabel("Open one")
                    .actionHandler(() -> DomGlobal.window.alert("Clicked on one!"))
                    .build (),
                new ActionPanel2.Config ()
                    .title ("Title of this two")
                    .description("Description of this two, different from the others")
                    .actionLabel("Open two")
                    .actionHandler(() -> DomGlobal.window.alert("Clicked on two!"))
                    .build (),
                new ActionPanel2.Config ()
                    .title ("Title of this three")
                    .description("Description of this three, different from the others")
                    .actionLabel("Open three")
                    .actionHandler(() -> DomGlobal.window.alert("Clicked on three!"))
                    .build ()
            );
        }));

        add (ComponentCreator.build (root -> {
            Stack.$ (root).horizontal ().gap (Length.em (1)).$ (
                ActionPanel2Creator.build (cfg -> {
                    cfg.title ("Title of this one");
                    cfg.description("Description of this one, different from the others");
                    cfg.actionLabel("Open one");
                    cfg.actionHandler(() -> DomGlobal.window.alert("Clicked on one!"));
                }),
                ActionPanel2Creator.build (cfg -> {
                    cfg.title ("Title of this two");
                    cfg.description("Description of this two, different from the others");
                    cfg.actionLabel("Open two");
                    cfg.actionHandler(() -> DomGlobal.window.alert("Clicked on two!"));
                }),
                ActionPanel2Creator.build (cfg -> {
                    cfg.title ("Title of this three");
                    cfg.description ("Description of this three, different from the others");
                    cfg.actionLabel ("Open three");
                    cfg.actionHandler (() -> DomGlobal.window.alert ("Clicked on three!"));
                })
            );
        }));

        add (ComponentCreator.build (root -> {
            Stack.$ (root).horizontal ().gap (Length.em (1)).$ (stack -> {
                for (int i = 1; i < 4; i++) {
                    final int count = i;
                    ActionPanel2Creator.$ (stack, cfg -> {
                        cfg.title ("Title of this " + count);
                        cfg.description("Description of this " + count + ", different from the others");
                        cfg.actionLabel("Open " + count);
                        cfg.actionHandler(() -> DomGlobal.window.alert("Clicked on " + count + "!"));
                    });
                }
            });
        }));

        // Exercise1.
        add (new Exercise1 ("Title for exercise 1", "This is the component for exercise 1", "Open me", () -> {
            DomGlobal.window.alert ("Clicked!");
        }));

        // Exercise2.
        add (new Exercise2 (v -> {
            Logger.info (v.name ());
        }));

        // Exercise3.
        add (Exercise3Creator.build (cfg -> {
            cfg.handler (v -> {
                Logger.info (v.name ());
            });
        }));
    }

    public static class ActionPanel1 extends SimpleComponent {

        @Override
        protected INodeProvider buildNode(Element root) {
            return Wrap.$ (root).$ (el -> {
                el.style ("action_panel");
                H5.$ (el).$ (h -> {
                    Span.$ (h).text ("This is the title");
                    Icon.$ (h, FontAwesome.user ());
                });
                P.$ (el).text ("This is a description of the action represented by this panel");
                Div.$ (el).$ (d -> {
                    ButtonCreator.$ (d, cfg -> {
                        cfg.label ("Go there");
                        cfg.handler (() -> {
                            DomGlobal.window.alert ("CLICK!");
                        });
                    });
                });
            }).build ();
        }
    
    }

    public static class ActionPanel1InConstructor extends SimpleComponent {

        public ActionPanel1InConstructor() {
            renderer(el -> {
                el.style ("action_panel");
                H5.$ (el).$ (h -> {
                    Span.$ (h).text ("This is the title");
                    Icon.$ (h, FontAwesome.user ());
                });
                P.$ (el).text ("This is a description of the action represented by this panel");
                Div.$ (el).$ (d -> {
                    ButtonCreator.$ (d, cfg -> {
                        cfg.label ("Go there");
                        cfg.handler (() -> {
                            DomGlobal.window.alert ("CLICK!");
                        });
                    });
                });
            });
        }
    
    }

    public static class ActionPanel2 extends Component<ActionPanel2.Config> {

        public static class Config extends Component.Config {

            /**
             * See {@link #title(String)}.
             */
            protected String title;
            
            /**
             * See {@link #description(String)}.
             */
            protected String description;
            
            /**
             * See {@link #actionLabel(String)}.
             */
            protected String actionLabel;
            
            /**
             * See {@link #actionHandler(Invoker)}.
             */
            protected Invoker actionHandler;
            
            /**
             * Assigns the title (appears at the top).
             * 
             * @param title
             *              the title to display.
             * @return this configuration instance.
             */
            public Config title(String title) {
                this.title = title;
                return this;
            }

            /**
             * Assigns the descripion (appears below the title).
             * 
             * @param descripion
             *                   the descripion to display.
             * @return this configuration instance.
             */
            public Config description(String description) {
                this.description = description;
                return this;
            }

            /**
             * Assigns the a title for the action button.
             * 
             * @param actionLabel
             *                   the label to display.
             * @return this configuration instance.
             */
            public Config actionLabel(String actionLabel) {
                this.actionLabel = actionLabel;
                return this;
            }

            /**
             * Assigns an action handler for the action button (that is invoked when the
             * button is clicked).
             * 
             * @param actionHandler
             *                      the action handler.
             * @return this configuration instance.
             */
            public Config actionHandler(Invoker actionHandler) {
                this.actionHandler = actionHandler;
                return this;
            }

            @Override
            @SuppressWarnings("unchecked")
            public ActionPanel2 build(LayoutData... data) {
                return new ActionPanel2(this);
            }

        }

        public ActionPanel2(Config config) {
            super(config);
        }

        @Override
        protected INodeProvider buildNode(Element root) {
            return Wrap.$ (root).$ (el -> {
                el.style ("action_panel");
                H5.$ (el).$ (h -> {
                    Span.$ (h).text (config().title);
                    Icon.$ (h, FontAwesome.user ());
                });
                P.$ (el).text (config().description);
                Div.$ (el).$ (d -> {
                    ButtonCreator.$ (d, cfg -> {
                        cfg.label(config().actionLabel);
                        cfg.handler (config().actionHandler);
                    });
                });
            }).build ();
        }
        
    }

    public static class ActionPanel2Creator {

        /**
         * Convenience to build an action panel.
         * 
         * @param el
         *            the element to build into.
         * @param cfg
         *            to configure the action panel.
         * @return the button instance.
         */
        public static ActionPanel2 $(IDomInsertableContainer<?> el, Consumer<ActionPanel2.Config> cfg) {
            return ComponentCreatorSupport.$ (el, new ActionPanel2.Config (), cfg, null);
        }

        /**
         * Convenience to build the action panel.
         * 
         * @param cfg
         *             to configure the action panel.
         * @param data
         *             (optional) layout data to associate with the instance.
         * @return the action panel instance.
         */
        public static ActionPanel2 build(Consumer<ActionPanel2.Config> cfg, LayoutData...data) {
            return ComponentCreatorSupport.build (new ActionPanel2.Config (), cfg, null, data);
        }
    }

    public static class Exercise1 extends SimpleComponent {

        private String title;
        
        private String descrition;
        
        private String actionLabel;

        private Invoker actionHandler;

        public Exercise1(String title, String descrition, String actionLabel, Invoker actionHandler) {
            this.title = title;
            this.descrition = descrition;
            this.actionLabel = actionLabel;
            this.actionHandler = actionHandler;
        }

        @Override
        protected INodeProvider buildNode(Element root) {
            return Wrap.$ (root).$ (el -> {
                el.style ("action_panel");
                H5.$ (el).$ (h -> {
                    Span.$ (h).text (title);
                    Icon.$ (h, FontAwesome.user ());
                });
                P.$ (el).text (descrition);
                Div.$ (el).$ (d -> {
                    ButtonCreator.$ (d, cfg -> {
                        cfg.label (actionLabel);
                        cfg.handler (actionHandler);
                    });
                });
            }).build ();
        }
    
    }

    public static class Exercise2 extends SimpleComponent {

        public enum Direction {
            UP, DOWN, LEFT, RIGHT;
        }

        private Consumer<Direction> handler;

        public Exercise2(Consumer<Direction> handler) {
            this.handler = handler;
        }

        @Override
        protected INodeProvider buildNode(Element root) {
            return Wrap.$ (root).$ (el -> {
                Div.$ (el).$ (ctl -> {
                    ctl.css (CSS.WIDTH, Length.em (10));

                    // Use a grid display for a 3x3 evenly spaced layout.
                    ctl.css ("display", "grid");
                    ctl.css ("grid-template-columns", "1fr 1fr 1fr");
                    ctl.css ("grid-template-rows", "1fr 1fr 1fr");

                    // Top row.
                    Div.$ (ctl);
                    ButtonCreator.$ (ctl, cfg -> {
                        cfg.icon (FontAwesome.arrowUp ());
                        cfg.handler (() -> handler.accept (Direction.UP));
                    });
                    Div.$ (ctl);

                    // Middle row.
                    ButtonCreator.$ (ctl, cfg -> {
                        cfg.icon (FontAwesome.arrowLeft ());
                        cfg.handler (() -> handler.accept (Direction.LEFT));
                    });
                    Div.$ (ctl);
                    ButtonCreator.$ (ctl, cfg -> {
                        cfg.icon (FontAwesome.arrowRight ());
                        cfg.handler (() -> handler.accept (Direction.RIGHT));
                    });

                    // Bottom row.
                    Div.$ (ctl);
                    ButtonCreator.$ (ctl, cfg -> {
                        cfg.icon (FontAwesome.arrowDown ());
                        cfg.handler (() -> handler.accept (Direction.DOWN));
                    });
                    Div.$ (ctl);
                });
            }).build ();
        }
    
    }

    public static class Exercise3 extends Component<Exercise3.Config> {

        public enum Direction {
            UP, DOWN, LEFT, RIGHT;
        }

        public static class Config extends Component.Config {

            private Consumer<Direction> handler;

            public Config handler(Consumer<Direction> handler) {
                this.handler = handler;
                return this;
            }

            @Override
            @SuppressWarnings("unchecked")
            public Exercise3 build(LayoutData... data) {
                return new Exercise3 (this);
            }
        }

        public Exercise3(Config config) {
            super (config);
        }

        @Override
        protected INodeProvider buildNode(Element root) {
            return Wrap.$ (root).$ (el -> {
                Div.$ (el).$ (ctl -> {
                    ctl.css (CSS.WIDTH, Length.em (10));

                    // Use a grid display for a 3x3 evenly spaced layout.
                    ctl.css ("display", "grid");
                    ctl.css ("grid-template-columns", "1fr 1fr 1fr");
                    ctl.css ("grid-template-rows", "1fr 1fr 1fr");

                    // Top row.
                    Div.$ (ctl);
                    ButtonCreator.$ (ctl, cfg -> {
                        cfg.icon (FontAwesome.arrowUp ());
                        cfg.handler (() -> config ().handler.accept (Direction.UP));
                    });
                    Div.$ (ctl);

                    // Middle row.
                    ButtonCreator.$ (ctl, cfg -> {
                        cfg.icon (FontAwesome.arrowLeft ());
                        cfg.handler (() -> config ().handler.accept (Direction.LEFT));
                    });
                    Div.$ (ctl);
                    ButtonCreator.$ (ctl, cfg -> {
                        cfg.icon (FontAwesome.arrowRight ());
                        cfg.handler (() -> config ().handler.accept (Direction.RIGHT));
                    });

                    // Bottom row.
                    Div.$ (ctl);
                    ButtonCreator.$ (ctl, cfg -> {
                        cfg.icon (FontAwesome.arrowDown ());
                        cfg.handler (() -> config ().handler.accept (Direction.DOWN));
                    });
                    Div.$ (ctl);
                });
            }).build ();
        }
    
    }

    public static class Exercise3Creator {

        public static Exercise3 $(IDomInsertableContainer<?> el, Consumer<Exercise3.Config> cfg) {
            return ComponentCreatorSupport.$ (el, new Exercise3.Config (), cfg, null);
        }

        public static Exercise3 build(Consumer<Exercise3.Config> cfg) {
            return ComponentCreatorSupport.build (new Exercise3.Config (), cfg, null);
        }
    }

    public static class Exercise2Creator {

        public static Exercise2 $(IDomInsertableContainer<?> el, Consumer<Exercise2.Direction> handler) {
            Exercise2 cpt = build (handler);
            el.insert (cpt);
            return cpt;
        }

        public static Exercise2 build(Consumer<Exercise2.Direction> handler) {
            return new Exercise2 (handler);
        }
    }
    

}

