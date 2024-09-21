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
import com.effacy.jui.core.client.component.IComponent;
import com.effacy.jui.core.client.component.SimpleComponent;
import com.effacy.jui.core.client.component.layout.ActionBarLayout;
import com.effacy.jui.core.client.component.layout.ActionBarLayout.HAlignment;
import com.effacy.jui.core.client.component.layout.ActionBarLayoutCreator;
import com.effacy.jui.core.client.component.layout.CardFitLayoutCreator;
import com.effacy.jui.core.client.component.layout.VertLayoutCreator;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.P;
import com.effacy.jui.core.client.dom.builder.Text;
import com.effacy.jui.core.client.dom.jquery.JQuery;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.ui.client.button.ButtonCreator;
import com.effacy.jui.ui.client.panel.Panel;
import com.effacy.jui.ui.client.panel.PanelCreator;
import com.effacy.jui.ui.client.panel.SplitPanel;

import elemental2.dom.Element;

public class Lesson1e extends LessonPanel {

    public Lesson1e() {

        // Create a simple panel and adorn with a border and background.
        Panel panel1 = new Panel.Config().adorn(el -> {
            JQuery.$ (el)
                .css("height", "300px")
                .css("border", "1px solid #ddd")
                .css("background-color", "#fafafa");
        }).build();
        add(panel1);

        panel1.add (ComponentCreator.build(el -> {
            el.css("background-color: #ccc; margin: 1em;");
            Text.$(el, "This is some content");
        }));

        Panel panel2 = new Panel.Config()
            .layout(CardFitLayoutCreator.create(false))
            .adorn(el -> {
                JQuery.$ (el)
                    .css("height", "300px")
                    .css("border", "1px solid #ddd")
                    .css("background-color", "#fafafa");
            })
            .build();
        add(panel2);

        IComponent cpt1 = panel2.add (ComponentCreator.build(el -> {
            el.css("background-color: #ccc; margin: 1em;");
            Text.$(el, "This is some content");
        }));
        IComponent cpt2 = panel2.add (ComponentCreator.build(el -> {
            el.css("background-color: #ccc; margin: 1em;");
            Text.$(el, "This is some other content");
        }));

        // add (ButtonCreator.build (cfg -> {
        //     cfg.label("Show first component");
        //     cfg.handler(() -> panel2.activate(cpt1));
        // }));
        // add (ButtonCreator.build (cfg -> {
        //     cfg.label("Show second component");
        //     cfg.handler(() -> panel2.activate(cpt2));
        // }));

        // add(PanelCreator.build(cfg -> {
        //     cfg.adorn(el -> JQuery.$ (el).css ("height", "3em"));
        //     cfg.layout(ActionBarLayoutCreator.create(bar -> {
        //         bar.zone (HAlignment.LEFT);
        //     }));
        // }, panel -> {
        //     panel.add (ButtonCreator.build (cfg -> {
        //         cfg.label("Show first component");
        //         cfg.handler(() -> panel2.activate(cpt1));
        //     }));
        //     panel.add (ButtonCreator.build (cfg -> {
        //         cfg.label("Show second component");
        //         cfg.handler(() -> panel2.activate(cpt2));
        //     }));
        // }));

        add(PanelCreator.build(cfg -> {
            cfg.adorn(el -> JQuery.$ (el).css ("height", "3em"));
            cfg.layout(ActionBarLayoutCreator.create(bar -> {
                bar.zone (HAlignment.LEFT);
                bar.zone(HAlignment.RIGHT);
            }));
        }, panel -> {
            panel.add (ButtonCreator.build (cfg -> {
                cfg.label("Show first component");
                cfg.handler(() -> panel2.activate(cpt1));
            }), new ActionBarLayout.Data(0));
            panel.add (ButtonCreator.build (cfg -> {
                cfg.label("Show second component");
                cfg.handler(() -> panel2.activate(cpt2));
            }), new ActionBarLayout.Data(1));
        }));

        Panel panel3 = new Panel.Config()
            .scrollable()
            .adorn(el -> {
                JQuery.$ (el)
                    .css("height", "300px")
                    .css("border", "1px solid #ddd")
                    .css("background-color", "#fafafa");
            })
            .build();
        add(panel3);
        panel3.add(ComponentCreator.build(el -> {
            for (int i = 0; i < 100; i++)
                Div.$ (el).text ("This is line " + i);
        }));

        SplitPanel panel4 = new SplitPanel.Config()
            .scrollable()
            .separator()
            .vertical()
            .otherLayout(ActionBarLayoutCreator.create(bar -> {
                bar.zone (HAlignment.LEFT);
                bar.zone(HAlignment.RIGHT);
            }))
            .adorn(el -> {
                JQuery.$ (el)
                    .css("height", "300px")
                    .css("border", "1px solid #ddd")
                    .css("background-color", "#fafafa");
            })
            .build();
        add(panel4);
        IComponent cpt4_1 = panel4.add (ComponentCreator.build(el -> {
            el.css("background-color: #ccc; margin: 1em;");
            Text.$(el, "This is some content");
        }));
        IComponent cpt4_2 = panel4.add (ComponentCreator.build(el -> {
            el.css("background-color: #ccc; margin: 1em;");
            Text.$(el, "This is some other content");
        }));
        panel4.addOther(ButtonCreator.build (cfg -> {
            cfg.label("Show first component");
            cfg.handler(() -> panel4.activate(cpt4_1));
        }), new ActionBarLayout.Data(0));
        panel4.addOther(ButtonCreator.build (cfg -> {
            cfg.label("Show second component");
            cfg.handler(() -> panel4.activate(cpt4_2));
        }), new ActionBarLayout.Data(1));

        LeftRightPanel panel5 = add(new LeftRightPanel());
        panel5.addLeft(ButtonCreator.build(cfg -> cfg.label("Left button 1")));
        panel5.addLeft(ButtonCreator.build(cfg -> cfg.label("Left button 2")));
        panel5.addRight(ButtonCreator.build(cfg -> cfg.label("Right button 1")));
        panel5.addRight(ComponentCreator.build(el -> P.$(el).text ("Some content")));

        // Exercise 2.
        add (new Exercise2("This is exercise 2"));
    }

    public static class LeftRightPanel extends SimpleComponent {
        public LeftRightPanel() {
            findRegionPoint("LEFT").setLayout(VertLayoutCreator.create());
            findRegionPoint("RIGHT").setLayout(VertLayoutCreator.create());
            renderer(el -> {
                el.css ("display: flex; min-height: 200px;");
                Div.$ (el)
                    .css ("flex-grow: 1; background-color: #fdd9d9;")
                    .use (n -> findRegionPoint("LEFT").setElement((Element) n));
                Div.$ (el)
                    .css ("flex-grow: 1; background-color: #f0fdd9;")
                    .use (n -> findRegionPoint("RIGHT").setElement((Element) n));
            });
        }

        public void addLeft(IComponent cpt) {
            findRegionPoint("LEFT").add (cpt);
        }

        public void addRight(IComponent cpt) {
            findRegionPoint("RIGHT").add (cpt);
        }
    }

    public static class Exercise2 extends LeftRightPanel {
        public Exercise2(String content) {
            addLeft(ButtonCreator.build(cfg -> cfg.label("Left button 1")));
            addLeft(ButtonCreator.build(cfg -> cfg.label("Left button 2")));
            addRight(ButtonCreator.build(cfg -> cfg.label("Right button 1")));
            if (!StringSupport.empty(content))
                addRight(ComponentCreator.build(el -> P.$ (el).text (content)));
        }
    }
}

