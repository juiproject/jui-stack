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

import com.effacy.jui.core.client.component.ComponentCreator;
import com.effacy.jui.core.client.component.SimpleComponent;
import com.effacy.jui.core.client.dom.ActivationHandler;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.builder.Cpt;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.H1;
import com.effacy.jui.core.client.dom.builder.Li;
import com.effacy.jui.core.client.dom.builder.P;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.builder.Ul;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.jquery.JQuery;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.ui.client.InfoBlockCreator;
import com.effacy.jui.ui.client.button.ButtonCreator;
import com.effacy.jui.ui.client.icon.FontAwesome;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;

public class Lesson1c extends LessonPanel {

    /**
     * Construct instance of the lession.
     */
    public Lesson1c() {
        
        // This display a title and descriptive text on the page.
        add (InfoBlockCreator.<Void>$ (cfg -> {}, builder -> {
            builder.header ("Part C: Interaction", header -> {
                header.subtitle ("This lession describes how to interact with a component.");
            });
        })).update (null);

        add (ComponentCreator.build (root -> {
            MyComponent1 cpt = new MyComponent1 ("Sample title", "Sample description");
            Cpt.$ (root, cpt);
            ButtonCreator.$ (root, cfg -> {
                cfg.label ("Update");
                cfg.handler (() -> {
                    cpt.updateTitle ("Title has been updated");
                    cpt.updateDescription ("Description has been updated");
                });
            });
        }));

        add (ComponentCreator.build (root -> {
            MyComponent2 cpt = new MyComponent2 ("Sample title", "Sample description");
            Cpt.$ (root, cpt);
            ButtonCreator.$ (root, cfg -> {
                cfg.label ("Update");
                cfg.handler (() -> {
                    cpt.updateTitle ("Title has been updated");
                    cpt.updateDescription ("Description has been updated");
                });
            });
        }));

        add (ComponentCreator.build (root -> {
            MyComponent3 cpt = new MyComponent3 ("Sample title", "Sample description");
            Cpt.$ (root, cpt);
            ButtonCreator.$ (root, cfg -> {
                cfg.label ("Update");
                cfg.handler (() -> {
                    cpt.updateTitle ("Title has been updated");
                    cpt.updateDescription ("Description has been updated");
                });
            });
        }));

        add (new MyMenuSelector (v -> {
            Logger.info ("Selected: " + v);
        }));

        // Exercise 2
        add (new MyMenuSelectorExercise2 (v -> {
            Logger.info ("Selected: " + v);
        }, "Item a", "Item b", "Item c", "Item d"));

        // Exercise 3
        add (new MyMenuSelectorExercise3 (v -> {
            Logger.info ("Selected: " + v);
        }, "Item a", "Item b", "Item c", "Item d", "Item e"));

        // Exercise 4
        add (new MyMenuSelectorExercise4 (v -> {
            Logger.info ("Selected: " + v);
        }, "Item a", "Item b", "Item c", "Item d", "Item e", "Item f"));

        // Exercise 5
        add (ComponentCreator.build (root -> {
            MyComponent3Exercise5 cpt = new MyComponent3Exercise5 ("Exercise 5 title", "Sample description");
            Cpt.$ (root, cpt);
            ButtonCreator.$ (root, cfg -> {
                cfg.label ("Update");
                cfg.handler (() -> {
                    cpt.updateTitle ("Title has been updated");
                    cpt.updateDescription ("Description has been updated");
                });
            });
        }));
    }

    public static class MyComponent1 extends SimpleComponent {

        private String title;
        
        private String description;

        private Element titleEl;

        private Element descriptionEl;

        public MyComponent1(String title, String description) {
            this.title = title;
            this.description = description;
        }

        public void updateTitle(String title) {
            titleEl.textContent = title;
        }

        public void updateDescription(String description) {
            descriptionEl.textContent = description;
        }

        @Override
        protected INodeProvider buildNode(Element root) {
            return Wrap.$ (root).$ (el -> {
                Div.$ (el).$ (outer -> {
                    H1.$ (outer).by ("title").text (title);
                    P.$ (outer).by ("description").text (description);
                });
            }).build (dom -> {
                titleEl = dom.first ("title");
                descriptionEl = dom.first ("description");
            });
        }
    
    }

    public static class MyComponent2 extends SimpleComponent {

        private String title;
        
        private String description;

        private Element outerEl;

        public MyComponent2(String title, String description) {
            this.title = title;
            this.description = description;
        }

        public void updateTitle(String title) {
            this.title = title;
            refresh ();
        }

        public void updateDescription(String description) {
            this.description = description;
            refresh ();
        }

        protected void refresh() {
            buildInto (outerEl, outer -> {
                H1.$ (outer).text (title).onclick (e -> {
                    DomGlobal.window.alert ("Title clicked!");
                });
                P.$ (outer).text (description);
            });
        }

        @Override
        protected INodeProvider buildNode(Element root) {
            return Wrap.$ (root).$ (el -> {
                Div.$ (el).by ("outer").$ (outer -> {
                    H1.$ (outer).text (title).onclick (e -> {
                        DomGlobal.window.alert ("Title clicked!");
                    });
                    P.$ (outer).text (description);
                });
            }).build (dom -> {
                outerEl = dom.first ("outer");
            });
        }
    
    }

    public static class MyComponent3 extends SimpleComponent {

        private String title;
        
        private String description;

        public MyComponent3(String title, String description) {
            this.title = title;
            this.description = description;
        }

        public void updateTitle(String title) {
            this.title = title;
            rerender ();
        }

        public void updateDescription(String description) {
            this.description = description;
            rerender ();
        }

        @Override
        protected INodeProvider buildNode(Element root) {
            return Wrap.$ (root).$ (el -> {
                Div.$ (el).$ (outer -> {
                    H1.$ (outer).by ("title").text (title);
                    P.$ (outer).by ("description").text (description);
                });
            }).build ();
        }
    
    }

    public static class MyMenuSelector extends SimpleComponent {

        private Consumer<String> handler;

        private Element labelEl;

        private Element selectorEl;

        // Indicates the the selector is open (showing).
        private boolean open = false;

        public MyMenuSelector(Consumer<String> handler) {
            this.handler = handler;
        }

        protected INodeProvider buildNode(Element root) {
            return Wrap.$ (root).$ (outer -> {
                Div.$ (outer).$ (activator -> {
                    activator.css ("display", "flex");
                    activator.css ("gap", "0.5em");
                    activator.css ("cursor", "pointer");
                    activator.onclick (e -> {
                        // Toggle the selector.
                        if (open) {
                            open = false;
                            JQuery.$ (selectorEl).hide ();
                        } else {
                            open = true;
                            JQuery.$ (selectorEl).show ();
                        }
                    });
                    Span.$ (activator).by ("label").text ("Select item");
                    Em.$ (activator).style (FontAwesome.caretDown ());
                });
                Div.$ (outer).by ("selector").$ (
                    Ul.$ ().$ (ul -> {
                        ul.css ("list-style", "none");
                        ul.css ("padding", "0");
                        ul.css ("cursor", "pointer");
                    }).$ (
                        Li.$ ().text ("Item 1").onclick (e -> {
                            onItemClick ("Item 1");
                        }),
                        Li.$ ().text ("Item 2").onclick (e -> {
                            onItemClick ("Item 2");
                        }),
                        Li.$ ().text ("Item 3").onclick (e -> {
                            onItemClick ("Item 3");
                        })
                    )
                );
            }).build (dom -> {
                labelEl = dom.first ("label");
                selectorEl = dom.first ("selector");
                // Make sure the selector starts in a hidden state.
                JQuery.$ (selectorEl).hide ();
            });
        }

        protected void onItemClick(String selection) {
            // Update the label.
            labelEl.textContent = selection;

            // Hide the selector.
            open = false;
            JQuery.$ (selectorEl).hide ();

            // Notify the handler of a selection.
            handler.accept (selection);
        }
    }

    public static class MyComponentExercise1 extends SimpleComponent {

        private String title;
        
        private String description;

        private Element titleEl;

        private Element descriptionEl;

        public MyComponentExercise1(String title, String description) {
            this.title = title;
            this.description = description;
        }

        public void updateTitle(String title) {
            if (!isRendered ()) {
                this.title = title;
                return;
            }
            titleEl.textContent = title;
        }

        public void updateDescription(String description) {
            if (!isRendered ()) {
                this.description = description;
                return;
            }
            descriptionEl.textContent = description;
        }

        @Override
        protected INodeProvider buildNode(Element root) {
            return Wrap.$ (root).$ (el -> {
                Div.$ (el).$ (outer -> {
                    if (!StringSupport.empty (title))
                        H1.$ (outer).by ("title").text (title);
                    if (!StringSupport.empty (description))
                        P.$ (outer).by ("description").text (description);
                });
            }).build (dom -> {
                titleEl = dom.first ("title");
                descriptionEl = dom.first ("description");
            });
        }
    
    }

    public static class MyMenuSelectorExercise2 extends SimpleComponent {

        private Consumer<String> handler;

        private String[] items;

        private Element labelEl;

        private Element selectorEl;

        private boolean open = false;

        public MyMenuSelectorExercise2(Consumer<String> handler, String...items) {
            this.handler = handler;
            this.items = items;
        }

        protected INodeProvider buildNode(Element el) {
            return Wrap.$ (el).$ (root -> {
                Div.$ (root).$ (activator -> {
                    activator.css ("display", "flex");
                    activator.css ("gap", "0.5em");
                    activator.css ("cursor", "pointer");
                    activator.onclick (e -> {
                        if (open) {
                            open = false;
                            JQuery.$ (selectorEl).hide ();
                        } else {
                            open = true;
                            JQuery.$ (selectorEl).show ();
                        }
                    });
                    Span.$ (activator).by ("label").text ("Select item");
                    Em.$ (activator).style (FontAwesome.caretDown ());
                });
                Div.$ (root).by ("selector").$ (
                    Ul.$ ().$ (ul -> {
                        ul.css ("list-style", "none");
                        ul.css ("padding", "0");
                        ul.css ("cursor", "pointer");
                        for (String item : items) {
                            Li.$ (ul).text (item).onclick (e -> {
                                onItemClick (item);
                            });
                        }
                    })
                );
            }).build (dom -> {
                labelEl = dom.first ("label");
                selectorEl = dom.first ("selector");
                JQuery.$ (selectorEl).hide ();
            });
        }

        protected void onItemClick(String selection) {
            labelEl.textContent = selection;
            open = false;
            JQuery.$ (selectorEl).hide ();
            handler.accept (selection);
        }
    }

    public static class MyMenuSelectorExercise3 extends SimpleComponent {

        private Consumer<String> handler;

        private String[] items;

        private Element labelEl;

        public MyMenuSelectorExercise3(Consumer<String> handler, String...items) {
            this.handler = handler;
            this.items = items;
        }

        protected INodeProvider buildNode(Element el) {
            return Wrap.$ (el).$ (root -> {
                root.style("l1c");
                Div.$ (root).$ (activator -> {
                    activator.style ("l1c_activator");
                    activator.onclick (e -> {
                        if (getRoot ().classList.contains ("l1c_open"))
                            getRoot ().classList.remove ("l1c_open");
                        else
                            getRoot ().classList.add ("l1c_open");
                    });
                    Span.$ (activator).by ("label").text ("Select item");
                    Em.$ (activator).style (FontAwesome.caretDown ());
                });
                Div.$ (root).style ("l1c_selector").$ (
                    Ul.$ ().$ (ul -> {
                        for (String item : items) {
                            Li.$ (ul).text (item).onclick (e -> {
                                onItemClick (item);
                            });
                        }
                    })
                );
            }).build (dom -> {
                labelEl = dom.first ("label");
            });
        }

        protected void onItemClick(String selection) {
            labelEl.textContent = selection;
            getRoot ().classList.remove ("l1c_open");
            handler.accept (selection);
        }
    }

    public static class MyMenuSelectorExercise4 extends SimpleComponent {

        private Consumer<String> handler;

        private String[] items;

        private Element labelEl;

        private ActivationHandler activation;

        public MyMenuSelectorExercise4(Consumer<String> handler, String...items) {
            this.handler = handler;
            this.items = items;
        }

        protected INodeProvider buildNode(Element el) {
            return Wrap.$ (el).$ (root -> {
                root.style("l1c");
                Div.$ (root).$ (activator -> {
                    activator.by ("activator");
                    activator.style ("l1c_activator");
                    activator.onclick (e -> {
                        activation.toggle ();
                    });
                    Span.$ (activator).by ("label").text ("Select item");
                    Em.$ (activator).style (FontAwesome.caretDown ());
                });
                Div.$ (root).style ("l1c_selector").$ (
                    Ul.$ ().$ (ul -> {
                        for (String item : items) {
                            Li.$ (ul).text (item).onclick (e -> {
                                onItemClick (item);
                            });
                        }
                    })
                );
            }).build (dom -> {
                labelEl = dom.first ("label");
                activation = new ActivationHandler (dom.first ("activator"), el, "l1c_open");
            });
        }

        protected void onItemClick(String selection) {
            labelEl.textContent = selection;
            activation.close ();
            handler.accept (selection);
        }
    }

    public static class MyComponent3Exercise5 extends SimpleComponent {

        private String title;
        
        private String description;
    
        public MyComponent3Exercise5(String title, String description) {
            this.title = title;
            this.description = description;
            renderer (root -> {
                Div.$ (root).$ (outer -> {
                    H1.$ (outer).text (this.title);
                    P.$ (outer).text (this.description);
                });
            });
        }
    
        public void updateTitle(String title) {
            this.title = title;
            rerender ();
        }
    
        public void updateDescription(String description) {
            this.description = description;
            rerender ();
        }
    
    }
}
