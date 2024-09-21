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

import java.util.List;

import com.effacy.jui.core.client.component.SimpleComponent;
import com.effacy.jui.core.client.component.layout.VertLayout;
import com.effacy.jui.core.client.control.DelayedModifiedHandler;
import com.effacy.jui.core.client.control.IControl.Value;
import com.effacy.jui.core.client.control.IInvalidListener;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.builder.Cpt;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.H3;
import com.effacy.jui.core.client.dom.builder.Label;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.navigation.INavigationAware;
import com.effacy.jui.core.client.store.ListPaginatedStore;
import com.effacy.jui.core.client.util.UID;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.playground.ui.tutorial.Names;
import com.effacy.jui.ui.client.InfoBlockCreator;
import com.effacy.jui.ui.client.button.ButtonCreator;
import com.effacy.jui.ui.client.control.Controls;
import com.effacy.jui.ui.client.control.TextControl;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.effacy.jui.ui.client.panel.Panel;
import com.effacy.jui.ui.client.table.TableCreator;
import com.effacy.jui.ui.client.table.renderer.TextTableCellRenderer;
import com.effacy.jui.validation.model.validator.Validators;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;

public class Lesson4a extends Panel implements INavigationAware {

    public Lesson4a() {
        super (new Panel.Config ().scrollable ().layout (VertLayout.$ ().spacing (Length.em (1)).build ()).padding (Insets.em (0, 0, 0, 2)));

        // This display a title and descriptive text on the page.
        add (InfoBlockCreator.<Void>$ (cfg -> cfg.padding (Insets.em (2, 1.75, 2, 0.75)), builder -> {
            builder.header ("Part A: Controls", header -> {
                header.subtitle ("This lesson explores controls and how controls can be used in different contexts.");
            });
        })).update (null);

        add (new VariousControls ());
        add (new ControlValues ());
        add (new ControlValuesWithModification ());
        add (new ControlStates ());
        add (new DynamicControls ());
        add (new SearchableTable ());
        add (new ControlValidation ());

        // Exercies
        add (new Exercise2 ());
        add (new Exercise3 ());
    }

    public class VariousControls extends SimpleComponent {

        @Override
        protected INodeProvider buildNode(Element el) {
            return Wrap.$ (el).$ (root -> {
                root.style ("lesson4", "stack", "gap");
                H3.$ (root).text ("Exploring different types of control");
                Div.$ (root).$ (
                    Label.$ ().text ("Text control"),
                    Controls.text (cfg -> {
                        cfg.width (Length.px (150));
                    })
                );
                Div.$ (root).$ (item -> {
                    Label.$ (item).text ("Text area control");
                    Cpt.$ (item, Controls.textarea (cfg -> {
                        cfg.width (Length.px (150));
                    }));
                });
                Div.$ (root).$ (
                    Label.$ ().text ("Selection"),
                    Controls.selector (cfg -> {
                        cfg.width (Length.px (150));
                        cfg.allowEmpty ();
                    }, "Option 1", "Option 2", "Option 3", "Option 4", "Option 5", "Option 6", "Option 7")
                );
                Div.$ (root).$ (
                    Label.$ ().text ("Checkbox"),
                    Controls.check (cfg -> {
                        cfg.width (Length.px (150));
                        cfg.label ("Option to activate");
                    })
                );
                Div.$ (root).$ (
                    Label.$ ().text ("Option group"),
                    Controls.checkGroup (cfg -> {
                        cfg.width (Length.px (250));
                        cfg.option("option1", null, "The first option", "A description for the first option");
                        cfg.option("option2", null, "The second option", "A description for the second option");
                    })
                );
                Div.$ (root).$ (
                    Label.$ ().text ("Multi-check"),
                    Controls.checkMulti (cfg -> {
                        cfg.width (Length.px (250));
                        cfg.option("option1", "Option 1");
                        cfg.option("option2", "Option 2");
                        cfg.option("option3", "Option 3");
                    })
                );
                Div.$ (root).$ (
                    Label.$ ().text ("Radio group"),
                    Controls.radioGroup (cfg -> {
                        cfg.width (Length.px (250));
                        cfg.option("option1", "Option 1", "A description for the first option");
                        cfg.option("option2", "Option 2", "A description for the second option");
                    })
                );
            }).build();
        }
        
    }

    public class ControlValues extends SimpleComponent {

        private TextControl nameCtl;

        @Override
        protected INodeProvider buildNode(Element el) {
            return Wrap.$ (el).$ (root -> {
                root.style ("lesson4", "panel");
                H3.$ (root).text ("Exploring control values");
                Div.$ (root).$ (ctls -> {
                    Div.$ (ctls).$ (
                        Label.$ ().text ("Persons name"),
                        nameCtl = Controls.text (cfg -> {
                            cfg.width (Length.px (150));
                        })
                    );
                    Div.$ (ctls).$ (item -> {
                        ButtonCreator.$ (item, cfg -> {
                            cfg.label ("Process");
                            cfg.handler(() -> {
                                DomGlobal.alert ("Name is \"" + nameCtl.value () + "\"");
                                nameCtl.setValue ("");
                            });
                        });
                    });
                });
            }).build();
        }
        
    }

    public class ControlValuesWithModification extends SimpleComponent {

        private TextControl nameCtl;

        @Override
        protected INodeProvider buildNode(Element el) {
            return Wrap.$ (el).$ (root -> {
                root.style ("lesson4", "panel");
                H3.$ (root).text ("Exploring control value assignment");
                Div.$ (root).$ (ctls -> {
                    Div.$ (ctls).$ (
                        Label.$ ().text ("Random value"),
                        nameCtl = Controls.text (cfg -> {
                            cfg.width (Length.px (150));
                            cfg.modifiedHandler ((ctx, val, prior) -> {
                                DomGlobal.alert("Value is \"" + nameCtl.value () + "\"");
                            });
                            cfg.readOnly();
                        })
                    );
                    Div.$ (ctls).$ (item -> {
                        ButtonCreator.$ (item, cfg -> {
                            cfg.label ("Set value");
                            cfg.handler (() -> {
                                nameCtl.setValue (UID.createUID ());
                            });
                        });
                    });
                    Div.$ (ctls).$ (item -> {
                        ButtonCreator.$ (item, cfg -> {
                            cfg.label ("Set value quietly");
                            cfg.handler(() -> {
                                nameCtl.setValue (Value.of (UID.createUID ()).quiet ());
                            });
                        });
                    });
                });
            }).build();
        }
        
    }

    public class ControlStates extends SimpleComponent {

        @Override
        protected INodeProvider buildNode(Element el) {
            return Wrap.$ (el).$ (root -> {
                root.style ("lesson4", "panel");
                H3.$ (root).text ("Exploring controls in different states");
                Div.$ (root).$ (ctls -> {
                    Div.$ (ctls).$ (
                        Label.$ ().text ("Normal"),
                        Controls.text (cfg -> {
                            cfg.placeholder ("First name");
                            cfg.width (Length.px (150));
                        })
                    );
                    Div.$ (ctls).$ (
                        Label.$ ().text ("Disabled"),
                        Controls.text (cfg -> {
                            cfg.placeholder ("First name");
                            cfg.width (Length.px (150));
                        }, ctl -> ctl.disable ())
                    );
                    Div.$ (ctls).$ (
                        Label.$ ().text ("Read only"),
                        Controls.text (cfg -> {
                            cfg.width (Length.px (150));
                        }, ctl -> {
                            ctl.readOnly (true);
                            ctl.setValue ("Jill Jones");
                        })
                    );
                    Div.$ (ctls).$ (
                        Label.$ ().text ("Waiting"),
                        Controls.text (cfg -> {
                            cfg.placeholder ("Jill Jones");
                            cfg.width (Length.px (150));
                        }, ctl -> ctl.waiting (true))
                    );
                    Div.$ (ctls).$ (
                        Label.$ ().text ("Error"),
                        Controls.text (cfg -> {
                            cfg.placeholder ("Jill Jones");
                            cfg.width (Length.px (150));
                        }, ctl -> ctl.invalidator ().invalidate ())
                    );
                });
            }).build();
        }
        
    }

    public class DynamicControls extends SimpleComponent {

        private TextControl otherTitleCtl;

        @Override
        protected INodeProvider buildNode(Element el) {
            return Wrap.$ (el).$ (root -> {
                root.style ("lesson4", "panel");
                H3.$ (root).text ("Explores enabling and disabling a control based on another controls value");
                Div.$ (root).$ (ctls -> {
                    Div.$ (ctls).$ (
                        Label.$ ().text ("Title"),
                        Controls.selector (cfg -> {
                            cfg.allowSearch (false);
                            cfg.width (Length.px (150));
                            cfg.modifiedHandler((ctl, val, prior) -> {
                                if ("Other".equals (val)) {
                                    otherTitleCtl.enable ();
                                } else {
                                    otherTitleCtl.setValue ("");
                                    otherTitleCtl.disable ();
                                }
                            });
                        }, "Mr", "Mrs", "Ms", "Miss", "Mx", "Master", "Other")
                    );
                    Div.$ (ctls).$ (
                        Label.$ ().text ("Other title"),
                        otherTitleCtl = Controls.text (cfg -> {
                            cfg.placeholder ("Other title");
                            cfg.width (Length.px (250));
                        }, ctl -> ctl.disable ())
                    );
                });
            }).build();
        }
        
    }

    public class SearchableTable extends SimpleComponent {

        private SampleRecordStore store = new SampleRecordStore ();
        {
            store.load (10);
        }

        @Override
        protected INodeProvider buildNode(Element el) {
            return Wrap.$ (el).$ (root -> {
                root.style ("lesson4", "stack");
                H3.$ (root).text ("Controls for search");
                Cpt.$ (root, Controls.text(cfg -> {
                    cfg.placeholder ("Search table");
                    cfg.iconLeft (FontAwesome.search ());
                    cfg.clearAction ();
                    cfg.width (Length.px (200));
                    cfg.modifiedHandler (DelayedModifiedHandler.create (300, (ctl, val, prior) -> {
                        if (StringSupport.empty (val))
                            store.filter (null);
                        else
                            store.filter (r -> 
                                r.name.toLowerCase ().contains (val.toLowerCase ()) ||
                                r.description.toLowerCase ().contains (val.toLowerCase ())
                            );
                    }));
                }));
                Div.$(root).$ (container -> {
                    container.css (CSS.HEIGHT, Length.px (300)).css (CSS.WIDTH, Length.px (500)).css (CSS.MARGIN_BOTTOM, Length.em (2));
                    TableCreator.$ (container, cfg -> {
                        cfg.header ("Name", header -> {
                            header.renderer (TextTableCellRenderer.create (r -> r.name));
                            header.width (Length.em (5));
                        }).header ("Description", header -> {
                            header.renderer (TextTableCellRenderer.create (r -> r.description));
                            header.width (Length.em (10));
                        });
                    }, store);
                });
            }).build ();
        }

        public class SampleRecord {

            private String name;

            private String description;

            public SampleRecord(String name, String description) {
                this.name = name;
                this.description = description;
            }
        }

        public class SampleRecordStore extends ListPaginatedStore<SampleRecord> {

            @Override
            protected void populate(List<SampleRecord> records) {
                for (int i = 0; i < 100; i++)
                    records.add (new SampleRecord (Names.NAMES[i], "Description " + i));
            }

        }

    }

    public class ControlValidation extends SimpleComponent {

        private TextControl nameCtl;

        @Override
        protected INodeProvider buildNode(Element el) {
            return Wrap.$ (el).$ (root -> {
                root.style ("lesson4", "panel");
                H3.$ (root).text ("Explores enabling and disabling a control based on another controls value");
                Div.$ (root).$ (ctls -> {
                    Div.$ (ctls).$ (
                        Label.$ ().text ("Person's name"),
                        nameCtl = Controls.text (cfg -> {
                            cfg.placeholder ("Full name");
                            cfg.width (Length.px (250));
                            cfg.validator(
                                Validators.notEmpty ("Cannot be empty"),
                                Validators.length (0, 20, "Cannot be more than 20 characters")
                            );
                        }, ctl -> {
                            ctl.addListener(IInvalidListener.create((c, errors) -> {
                                Logger.log ("Control is invalid:");
                                errors.forEach (error -> Logger.log (" " + error));
                            }, null));
                        })
                    );
                    ButtonCreator.$ (ctls, cfg -> {
                        cfg.label ("Validate");
                        cfg.handler (() -> {
                            nameCtl.validate();
                        });
                    });
                });
            }).build();
        }
        
    }

    public class Exercise2 extends SimpleComponent {

        private TextControl field1Ctl;

        private TextControl field2Ctl;

        public Exercise2() {
            renderer (root -> {
                root.style ("lesson4", "panel");
                H3.$ (root).text ("Candidate solution for exercise 2");
                Div.$ (root).css ("display: flex; gap: 1em;").$ (
                    field1Ctl = Controls.text (cfg -> {
                        cfg.width (Length.px (150));
                        cfg.modifiedHandler((c,val,prior) -> {
                            field2Ctl.setValue (Value.of (val).quiet());
                        });
                    }),
                    field2Ctl = Controls.text (cfg -> {
                        cfg.width (Length.px (150));
                        cfg.modifiedHandler((c,val,prior) -> {
                            field1Ctl.setValue (Value.of (val).quiet());
                        });
                    })
                );
            });
        }
        
    }

    public class Exercise3 extends SimpleComponent {

        private TextControl field1Ctl;

        private TextControl field2Ctl;

        public Exercise3() {
            renderer (root -> {
                root.style ("lesson4", "panel");
                H3.$ (root).text ("Candidate solution for exercise 3");
                Div.$ (root).css ("display: flex; gap: 1em;").$ (
                    field1Ctl = Controls.text (cfg -> {
                        cfg.width (Length.px (150));
                        cfg.modifiedHandler(DelayedModifiedHandler.create (300, (c,val,prior) -> {
                            field2Ctl.setValue (Value.of (val).quiet());
                        }));
                    }),
                    field2Ctl = Controls.text (cfg -> {
                        cfg.width (Length.px (150));
                        cfg.modifiedHandler((c,val,prior) -> {
                            field1Ctl.setValue (Value.of (val).quiet());
                        });
                    })
                );
            });
        }
        
    }

}
