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

import java.util.ArrayList;
import java.util.List;

import com.effacy.jui.core.client.Invoker;
import com.effacy.jui.core.client.component.ComponentCreator;
import com.effacy.jui.core.client.component.StateComponent;
import com.effacy.jui.core.client.component.StateComponentCreator;
import com.effacy.jui.core.client.dom.builder.Cpt;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.Input;
import com.effacy.jui.core.client.dom.builder.Li;
import com.effacy.jui.core.client.dom.builder.P;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.builder.Ul;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.jquery.JQuery;
import com.effacy.jui.core.client.state.LifecycleStateVariable;
import com.effacy.jui.core.client.state.StateVariable;
import com.effacy.jui.core.client.state.ValueStateVariable;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.platform.util.client.TimerSupport;
import com.effacy.jui.ui.client.InfoBlockCreator;
import com.effacy.jui.ui.client.button.Button;
import com.effacy.jui.ui.client.button.ButtonCreator;
import com.effacy.jui.ui.client.panel.PanelCreator;

import elemental2.dom.DomGlobal;

public class Lesson1d extends LessonPanel {

    private int buttonCounter = 0;

    private boolean buttonCounterRunning = false;

    /**
     * Construct instance of the lession.
     */
    public Lesson1d() {
        
        // This display a title and descriptive text on the page.
        add (InfoBlockCreator.<Void>$ (cfg -> {}, builder -> {
            builder.header ("Part D: State", header -> {
                header.subtitle ("This lession describes how to develop components with state.");
            });
        })).update (null);

        // A simple update button that uses a state variable.
        ValueStateVariable<String> buttonLabel = new ValueStateVariable<String> ("Button count 0");
        add (PanelCreator.buttonBar (null, bar -> {
            bar.add (new MyButtonWithState (buttonLabel, () -> {
                DomGlobal.window.alert ("Clicked");
            }));
            bar.add (new MyButtonWithState (buttonLabel, () -> {
                DomGlobal.window.alert ("Clicked");
            }));
            bar.add (new MyButtonWithState (buttonLabel, () -> {
                DomGlobal.window.alert ("Clicked");
            }));
            bar.add (ButtonCreator.build (cfg -> {
                cfg.label ("Start updating buttons");
                cfg.handler (() -> {
                    if (buttonCounterRunning)
                        return;
                    buttonCounterRunning = true;
                    TimerSupport.repeat (() -> {
                        // Upate the state variable.
                        buttonLabel.assign ("Button count " + (++buttonCounter));
                    }, 2000);
                });
            }));
        }));

        add (ComponentCreator.build (root -> {
            ErrorState errors = new ErrorState ();
            Cpt.$ (root, new ErrorMessage (errors));
            Cpt.$ (root, PanelCreator.buttonBar (null, bar -> {
                bar.add(ButtonCreator.build (btn -> {
                    btn.label ("Generate errors");
                    btn.handler (() -> {
                        errors.errors (
                            "First error message",
                            "Second error message"
                        );
                    });
                }), null);
                bar.add(ButtonCreator.build (btn -> {
                    btn.label ("Clear errors");
                    btn.handler (() -> {
                        errors.clear ();
                    });
                }), null);
            }));
        }));

        add (ComponentCreator.build (root -> {
            SimpleMenu menu = new SimpleMenu ();
            menu.hide ();
            Cpt.$ (root, menu);
            ButtonCreator.$ (root, btn -> {
                btn.label ("Load menu");
                btn.handler (() -> {
                    menu.show ();
                    menu.load ();
                });
            });
        }));

        // Component.DEBUG_RENDER = true;
        // Example of a state component with a child (and re-renders).
        add (ComponentCreator.build (root -> {
            StateComponentCreator.$ (root, new ValueStateVariable<Integer>(0), (state, el) -> {
                P.$ (el).$ (p -> p.text ("Counter: " + state.value ()));
                ButtonCreator.$ (el, cfg -> {
                    cfg.label ("Increase counter");
                    cfg.handler (() -> {
                        state.assign (state.value() + 1);
                    });
                });
            });
        }));

        // Example of a component that reuses children during re-render.
        add (new ReRenderWithReUse ());

        // Same as the previous but inlined.
        add (ComponentCreator.build (root -> {
            ValueStateVariable<Integer> counter = new ValueStateVariable<Integer> (0);
            Button btn = ButtonCreator.build (cfg -> {
                cfg.label ("Increase counter");
                cfg.handler (() -> {
                    counter.assign (counter.value() + 1);
                });
            });
            StateComponentCreator.$ (root, counter, (state, el) -> {
                P.$ (el).$ (p -> p.text ("Counter: " + state.value ()));
                Cpt.$ (el, btn);
            }, null, cpt -> {
                cpt.reuse (btn);
            });
        }));

        // Exercise 2.
        add (ComponentCreator.build (root -> {
            Div.$ (root).$ (
                StateComponentCreator.build(UserNameStateExercise1.instance(), (state, el) -> {
                    if (StringSupport.empty (state.value()))
                        Div.$ (el).text ("No name");
                    else
                        Div.$ (el).text (state.value());
                }),
                Div.$ ().css ("display: flex; gap: 1em;").$ (
                    Input.$ ("test"),
                    com.effacy.jui.core.client.dom.builder.Button.$ ()
                        .text ("Change name")
                        .onclick((e, n) -> {
                            String name = (String) JQuery.$ (n.parentElement).find("input").val();
                            UserNameStateExercise1.instance().modify(state -> {
                                state.assign (name);
                            });
                        })
                )
            );
        }));

        
    }

    public static class MyButtonWithState extends StateComponent<ValueStateVariable<String>> {

        public MyButtonWithState(ValueStateVariable<String> label, Invoker handler) {
            super (label);

            renderer (root -> {
                 ButtonCreator.$ (root, btn -> {
                    btn.label (state ().value ());
                    btn.handler(() -> handler.invoke ());
                });
            });
        }

    }

    public static class ErrorState extends StateVariable<ErrorState> {

        private List<String> items = new ArrayList<>();

        public void clear() {
            modify (v -> {
                v.items.clear ();
            });
        }

        public void errors(String...messages) {
            modify (v -> {
                for (String message : messages)
                    v.items.add (message);
            });
        }

    }

    public static class ErrorMessage extends StateComponent<ErrorState> {

        public ErrorMessage(ErrorState state) {
            super (state);
            renderer (root -> {
                if (!state ().items.isEmpty()) {
                    P.$ (root).$ (p -> p.text ("There was a problem:"));
                    Ul.$ (root).$ (list -> {
                        state ().items.forEach (item -> {
                            Li.$ (list).text (item);
                        });
                    });
                }
            });
        }

    }

    public static class SimpleMenu extends StateComponent<SimpleMenu.MenuItems> {

        public static class MenuItems extends LifecycleStateVariable<MenuItems> {

            private List<String> items = new ArrayList<>();

            public void load() {
                loading ();

                // Mimic a remote load.
                TimerSupport.timer (() -> {
                    modify (v -> {
                        v.items.clear ();
                        v.items.add ("Menu item 1");
                        v.items.add ("Menu item 2");
                        v.items.add ("Menu item 3");
                    });
                }, 300);
            }

        }

        public SimpleMenu() {
            super (new MenuItems ());
            renderer (root -> {
                root.css (CSS.PADDING, Insets.em (1)).css ("border", "1px solid #ccc");
                if (state ().isLoading()) {
                    Span.$ (root).text ("Loading...");
                } else {
                    Ul.$ (root).$ (list -> {
                        state ().items.forEach (item -> {
                            Li.$ (list).text (item);
                        });
                    });
                }
            });
        }

        public void load() {
            state().load ();
        }

    }

    public class ReRenderWithReUse extends StateComponent<ValueStateVariable<Integer>> {

        public ReRenderWithReUse() {
            super (new ValueStateVariable<Integer>(0));

            Button btn = reuse (ButtonCreator.build (cfg -> {
                cfg.label ("Increase counter");
                cfg.handler (() -> {
                    state ().assign (state ().value () + 1);
                });
            }));

            renderer (root -> {
                P.$ (root).$ (p -> p.text ("Counter: " + state.value ()));
                Cpt.$ (root, btn);
            });
        }
        
    }

    public static class UserNameStateExercise1 extends ValueStateVariable<String> {

        private final static UserNameStateExercise1 INSTANCE = new UserNameStateExercise1();

        public static UserNameStateExercise1 instance() {
            return INSTANCE;
        }
    }

}
