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



import com.effacy.jui.core.client.component.ComponentCreator;
import com.effacy.jui.core.client.component.layout.CardFitLayout.Config.Effect;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.Button;
import com.effacy.jui.core.client.dom.builder.Code;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.H1;
import com.effacy.jui.core.client.dom.builder.H2;
import com.effacy.jui.core.client.dom.builder.I;
import com.effacy.jui.core.client.dom.builder.Li;
import com.effacy.jui.core.client.dom.builder.Ol;
import com.effacy.jui.core.client.dom.builder.P;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.builder.Strong;
import com.effacy.jui.core.client.dom.builder.Text;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Color;
import com.effacy.jui.core.client.dom.css.Decimal;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.util.Random;
import com.effacy.jui.ui.client.InfoBlockCreator;
import com.effacy.jui.ui.client.button.ButtonCreator;
import com.effacy.jui.ui.client.control.TextControl;
import com.effacy.jui.ui.client.control.TextControlCreator;
import com.effacy.jui.ui.client.fragments.Icon;
import com.effacy.jui.ui.client.fragments.Stack;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.effacy.jui.ui.client.navigation.TabNavigator;
import com.effacy.jui.ui.client.navigation.TabNavigatorCreator;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;

public class Lesson1a extends LessonPanel {

    protected Lesson1a() {
        // This display a title and descriptive text on the page.
        add (InfoBlockCreator.<Void>$ (cfg -> {}, builder -> {
            builder.header ("Part A: Rendering", header -> {
                header.subtitle ("This lession describes how to render DOM and build simple components.");
            });
        })).update (null);


        add (ComponentCreator.build (root -> {
            P.$ (root).text ("This is some text 1");
        }));

        add (ComponentCreator.build (root -> {
            P.$ (root).$ (
                Em.$ ().style (FontAwesome.user ()),
                Span.$ ().text ("Some content")
            );
        }));

        add (ComponentCreator.build (root -> {
            P.$ (root).$ (outer -> {
                Em.$ (outer).style (FontAwesome.user ());
                Text.$ (outer, "Some content");
            });
        }));
        
        add (ComponentCreator.build (root -> {
            for (int i = 0; i < 5; i++) {
                final int count = i + 1;
                P.$ (root).$ (outer -> {
                    Em.$ (outer).style (FontAwesome.user ());
                    Text.$ (outer, "Some content " + count);
                });
            }
        }));

        add (ComponentCreator.build (root -> {
            P.$ (root).css ("display", "flex").css ("flex-direction", "row").css ("gap", "1em").css ("align-items", "baseline").$ (outer -> {
                Em.$ (outer).style (FontAwesome.user ());
                Text.$ (outer, "Some content");
            });
        }));

        add (ComponentCreator.build (root -> {
            P.$ (root).$ (outer -> {
                outer.css ("display", "flex");
                outer.css ("flex-direction", "row");
                outer.css ("gap", "1em");
                outer.css ("align-items", "baseline");
            }).$ (outer -> {
                Em.$ (outer).style (FontAwesome.user());
                Text.$ (outer, "Some content");
            });
        }));

        add (ComponentCreator.build (root -> {
            P.$ (root).$ (outer -> {
                outer.css ("display", "flex");
                outer.css ("flex-direction", "row");
                outer.css (CSS.GAP, Length.em (1));
                outer.css ("align-items", "baseline");
            }).$ (outer -> {
                Em.$ (outer).style (FontAwesome.user());
                Text.$ (outer, "Some content");
            });
        }));

        add (ComponentCreator.build (root -> {
            P.$ (root).$ (outer -> {
                outer.style ("lesson1_mystyle");
                Em.$ (outer).style (FontAwesome.user());
                Text.$ (outer, "Some content");
            });
        }));

        add (ComponentCreator.build (root -> {
            Stack.$ (root).horizontal ().gap (Length.em (1)).$ (stack -> {
                Icon.$ (stack, FontAwesome.user ())
                    .color (Color.raw ("green"));
                // Note that onclick() is declared on the fragment class
                // and is not the onclick used in later examples.
                Icon.$ (stack, FontAwesome.anchorLock ())
                    .color (Color.raw ("blue"))
                    .onclick (() -> {
                        DomGlobal.window.alert ("Icon clicked!");
                    });
                Icon.$ (stack, FontAwesome.appleAlt ())
                    .color (Color.raw ("magenta"));
            });
        }));

        add (ComponentCreator.build (root -> {;
            Div.$ (root).$ (area -> {
                area.css ("width", "2em");
                area.css ("height", "2em");
                area.css ("border", "1px solid #ccc");
                area.css ("background", "#f1f1f1");
                area.on ((e,n) -> {
                    CSS.BACKGROUND_COLOR.apply ((Element) n, Color.raw ("#ccc"));
                }, UIEventType.ONMOUSEENTER);
                area.on ((e,n) -> {
                    CSS.BACKGROUND_COLOR.apply ((Element) n, Color.raw ("#f1f1f1"));
                }, UIEventType.ONMOUSELEAVE);
            });
        }));

        add (ComponentCreator.build (root -> {
            Button.$ (root).$ ( btn -> {
                btn.text ("Click me");
                btn.onclick (e -> {
                    DomGlobal.window.alert ("Link clicked!");
                });
            });
        }));

        add (ComponentCreator.build (root -> {
            root.insert (new com.effacy.jui.ui.client.button.Button.Config ()
                .label ("Click me")
                .handler (() -> {
                    DomGlobal.window.alert ("Link clicked!");
                }).build ());
        }));

        add (ComponentCreator.build (root -> {
            Div.$ (root).$ (
                P.$ ().text ("Click the button:"),
                new com.effacy.jui.ui.client.button.Button.Config ()
                    .label ("Click me")
                    .handler (() -> {
                        DomGlobal.window.alert ("Link clicked!");
                    }).build ()
            );
        }));

        add (ComponentCreator.build (root -> {
            ButtonCreator.$ (root, cfg -> {
                cfg.label ("Click me");
                cfg.handler (() -> {
                    DomGlobal.window.alert ("Link clicked!");
                });
            });
        }));

        add (ComponentCreator.build (root -> {
            Div.$ (root).$ (
                P.$ ().text ("Click the button:"),
                ButtonCreator.build (cfg -> {
                    cfg.label ("Click me");
                    cfg.handler (() -> {
                        DomGlobal.window.alert ("Link clicked!");
                    });
                })
            );
        }));

        add (ComponentCreator.build (root -> {
            Div.$ (root).$ (inner -> {
                P.$ (inner).text ("Click the button:");
                ButtonCreator.$ (inner, cfg -> {
                    cfg.label ("Click me");
                    cfg.handler (() -> {
                        DomGlobal.window.alert ("Link clicked!");
                    });
                });
            });
        }));

        add (ComponentCreator.build (root -> {
            P.$ (root).$ (p -> p.text ("The following is a set of tabs:"));
            TabNavigatorCreator.$ (root, cfg -> {
                cfg.style (TabNavigator.Config.Style.HORIZONTAL_UNDERLINE)
                    .padding (Insets.em (1))
                    .effect (Effect.FADE_IN);
                cfg.tab ("tab1", "Tab 1", ComponentCreator.build (tab -> {
                    P.$ (tab).$ (p -> p.text ("The first tab"));
                }));
                cfg.tab ("tab2", "Tab 2", ComponentCreator.build (tab -> {
                    P.$ (tab).$ (p -> p.text ("The second tab"));
                }));
                cfg.tab ("tab3", "Tab 3", ComponentCreator.build (tab -> {
                    P.$ (tab).$ (p -> p.text ("The third tab"));
                }));
            }, panel -> {
                panel.css (n -> CSS.HEIGHT.apply (n, Length.em (10)));
            });
        }));

        // Exercise 1
        // This is based on formatting the exercises section in the lesson plan.

        add (ComponentCreator.build (root -> {
            root.css (CSS.WIDTH, Length.px (600));
            H1.$ (root).css (CSS.FONT_WEIGHT, Decimal.of (500)).text ("Exercises");
            H2.$ (root).css (CSS.FONT_WEIGHT, Decimal.of (500)).text ("A small set of exercises to practice your skill against");
            P.$ (root).$ (
                // This could have been done using the lambda-expression approach,
                // however, for textual content, this is more readable.
                Text.$ ("Using the tools and techniques that you have been exposed to in this part try to create the following as inline components:"),
                Ol.$ ().$ (
                    Li.$ ().$ (
                        Text.$ ("A component that renders a simple article consisting of a title, sub-title and body content (which should include a list of items). You should provide your own content for this exercise and use direct CSS styles to format. "),
                        I.$ ().text ("A a stretch goal, modify lessons.css to add a CSS class (or classes) to apply the styling.")
                    ),
                    Li.$ ().text ("A component that displays a light blue square and when the user clicks in the square a window alert dialog appears stating which quadrant of the square the user clicked in (being top-left, top-right, bottom-left or bottom-right)."),
                    Li.$ ().$ (
                        Strong.$ ().text ("Advanced"),
                        Text.$ (" A component that displays a "),
                        Code.$ ().text ("TextControl"),
                        Text.$ (" component and a button that, when clicked, sets the value of the text control to one of five random values (a random value can be obtained using "),
                        Code.$ ().text ("Random.nextInt(int)"),
                        Text.$ (").")
                    )
                )
            );
        }));

        // Exercise 2
        // One possible solution (there are many):
        // We construct an outer DIV with two inner DIV's as rows. Each row contains two inner DIVS's
        // as cells. The cells have fixed (equal) dimensions as so setout the quadrants. Flexbox is
        // used for the layout within each row and inline-block on the outer DIV to contract in the
        // line.
        add (ComponentCreator.build (root -> {
            Div.$ (root).$ (outer -> {
                outer.css (CSS.BACKGROUND_COLOR, Color.raw ("lightblue"));
                outer.css ("display", "inline-block");
                outer.css ("cursor", "pointer");
                Div.$ (outer).css ("display: flex; flex-direction: row").$ (row -> {
                    Div.$ (row).css (CSS.WIDTH, Length.em(5)).css (CSS.HEIGHT, Length.em(5)).onclick (e -> {
                        DomGlobal.window.alert ("Top-left");
                    });
                    Div.$ (row).css (CSS.WIDTH, Length.em(5)).css (CSS.HEIGHT, Length.em(5)).onclick (e -> {
                        DomGlobal.window.alert ("Top-right");
                    });
                });
                Div.$ (outer).css ("display: flex; flex-direction: row").$ (row -> {
                    Div.$ (row).css (CSS.WIDTH, Length.em(5)).css (CSS.HEIGHT, Length.em(5)).onclick (e -> {
                        DomGlobal.window.alert ("Bottom-left");
                    });
                    Div.$ (row).css (CSS.WIDTH, Length.em(5)).css (CSS.HEIGHT, Length.em(5)).onclick (e -> {
                        DomGlobal.window.alert ("Bottom-right");
                    });
                });
            });
        }));

        // Exercise 3
        // Here we leverage lambda-expressions and create the labels and text control
        // as variables that we reference when building out the DOM structure. DIV's
        // are used to enforce our desired layout (albiet a basic one).
        add (ComponentCreator.build (root -> {
            String [] labels = {
                "Label 1", "Label 2", "Label 3", "Label 4", "Label 5"
            };
            TextControl ctl = TextControlCreator.build (cfg -> {
                cfg.width (Length.em (15));
            });
            Div.$ (root).$ (ctl);
            Div.$ (root).$ (
                ButtonCreator.build (cfg -> {
                    cfg.label ("Change label");
                    cfg.handler (() -> {
                        ctl.setValue (labels[Random.nextInt (labels.length - 1)]);
                    });
                })
            );
        }));
    }

}

