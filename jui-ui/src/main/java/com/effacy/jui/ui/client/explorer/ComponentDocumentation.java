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
package com.effacy.jui.ui.client.explorer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.effacy.jui.core.client.component.Component;
import com.effacy.jui.core.client.component.ComponentCreator;
import com.effacy.jui.core.client.component.IComponent;
import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.H5;
import com.effacy.jui.core.client.dom.builder.P;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.builder.Table;
import com.effacy.jui.core.client.dom.builder.Tbody;
import com.effacy.jui.core.client.dom.builder.Td;
import com.effacy.jui.core.client.dom.builder.Th;
import com.effacy.jui.core.client.dom.builder.Thead;
import com.effacy.jui.core.client.dom.builder.Tr;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.ui.client.explorer.ComponentDocumentation.Config.ConfigurationExample;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.effacy.jui.ui.client.panel.PanelCreator;
import com.effacy.jui.ui.client.tabs.TabSet;
import com.effacy.jui.ui.client.tabs.TabbedPanelCreator;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;

public class ComponentDocumentation extends Component<ComponentDocumentation.Config> {

    public static class Config extends Component.Config {

        public enum Type {
            COMPONENT, FRAGMENT;
        }

        private String title;

        private String className;

        private Type type = Type.COMPONENT;

        private List<String> css = new ArrayList<> ();

        private IComponent example;

        private List<String> description = new ArrayList<> ();

        private List<ConfigurationOption> configuration = new ArrayList<> ();

        private List<ConfigurationStyle> styles = new ArrayList<> ();

        private List<ConfigurationExample> examples = new ArrayList<> ();

        public Config title(String title) {
            this.title = title;
            return this;
        }

        public Config className(String className) {
            this.className = className;
            return this;
        }
        
        public Config type(Type type) {
            if (type != null)
                this.type = type;
            return this;
        }

        /**
         * The CSS files used.
         * 
         * @param css
         *            the files.
         * @return this configuration instance.
         */
        public Config css(String... css) {
            for (String style : css) {
                if (StringSupport.empty(style))
                    continue;
                this.css.add (style);
            }
            return this;
        }

        public Config description (String...description) {
            for (String line : description) {
                if (line == null)
                    continue;
                this.description.add (line);
            }
            return this;
        }

        public Config example(IComponent example) {
            this.example = example;
            return this;
        }

        public Config option(String option) {
            return option (option, null);
        }

        public Config option(String option, Consumer<ConfigurationOption> configurer) {
            ConfigurationOption co = new ConfigurationOption(option);
            configuration.add(co);
            if (configurer != null)
                configurer.accept (co);
            return this;
        }

        public Config style(String variable, Consumer<ConfigurationStyle> configurer) {
            ConfigurationStyle co = new ConfigurationStyle (variable);
            styles.add (co);
            if (configurer != null)
                configurer.accept (co);
            return this;
        }

        public Config example(IComponent example, Consumer<ConfigurationExample> configurer) {
            ConfigurationExample co = new ConfigurationExample (example);
            examples.add (co);
            if (configurer != null)
                configurer.accept (co);
            return this;
        }

        public Config example(Supplier<IComponent> example, Consumer<ConfigurationExample> configurer) {
            ConfigurationExample co = new ConfigurationExample (example.get ());
            examples.add (co);
            if (configurer != null)
                configurer.accept (co);
            return this;
        }


        @SuppressWarnings("unchecked")
        @Override
        public ComponentDocumentation build(LayoutData... data) {
            return new ComponentDocumentation(this);
        }

        public class ConfigurationOption {
            private String name;
            private String description;
            private boolean constructor = false;
            private boolean required = false;
            private String valueType;
            private String valueDescription;
            private List<OptionValue> values = new ArrayList<> ();

            public ConfigurationOption(String name) {
                this.name = name;
            }
            
            public ConfigurationOption description(String description) {
                this.description = description;
                return this;
            }

            public ConfigurationOption constructor() {
                this.constructor = true;
                return this;
            }

            public ConfigurationOption required() {
                this.required = true;
                return this;
            }

            public ConfigurationOption valueDescription(String valueDescription) {
                this.valueDescription = valueDescription;
                return this;
            }

            public ConfigurationOption valueType(String valueType) {
                this.valueType = valueType;
                return this;
            }

            public ConfigurationOption value(Object value, String description) {
                this.values.add (new OptionValue (value, description));
                return this;
            }

            public class OptionValue {
                private String value;
                private String description;
                public OptionValue (Object value, String description) {
                    this.value = "[null]";
                    if (value != null) {
                        if (value instanceof Enum)
                            this.value = value.getClass ().getSimpleName() + "." + ((Enum<?>) value).name ();
                        else 
                            this.value = value.toString ();
                    }
                    this.description = description;
                }
            }
        }

        public class ConfigurationStyle {
            
            private String name;
            
            private String description;

            private List<StyleVariable> variables = new ArrayList<> ();

            private List<String> css = new ArrayList<> ();

            public ConfigurationStyle(String name) {
                this.name = name;
            }
            

            public ConfigurationStyle description(String description) {
                this.description = description;
                return this;
            }
            

            /**
             * The CSS files used.
             * 
             * @param css
             *            the files.
             * @return this configuration instance.
             */
            public ConfigurationStyle css(String... css) {
                for (String style : css) {
                    if (StringSupport.empty(style))
                        continue;
                    this.css.add (style);
                }
                return this;
            }


            public ConfigurationStyle variable(String variable, String description) {
                this.variables.add (new StyleVariable (variable, description));
                return this;
            }

            public class StyleVariable {
                private String variable;
                private String description;
                public StyleVariable (String variable, String description) {
                    this.variable = variable;
                    this.description = description;
                }
            }
        }

        public class ConfigurationExample {
                
            private IComponent example;
            
            private String description;

            private String code;

            public ConfigurationExample(IComponent example) {
                this.example = example;
            }
            
            public ConfigurationExample description(String description) {
                this.description = description;
                return this;
            }
            
            public ConfigurationExample code(String code) {
                this.code = code;
                return this;
            }


        }

    }

    public ComponentDocumentation (Config config) {
        super (config);
    }

    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$ (el).$ (root -> {
            // Header with description.
            Div.$ (root).style (styles().header ()).$ (header -> {
                Div.$ (header).style (styles ().left ()).$ (left -> {
                    H5.$ (left).$ (title -> {
                        title.text (StringSupport.empty (config ().title, "No title"));
                        if (config ().type == ComponentDocumentation.Config.Type.FRAGMENT)
                            Span.$ (title).text ("Fragment");
                        // else if (config ().type == ComponentDocumentation.Config.Type.COMPONENT)
                        //    Span.$ (title).text ("Component");
                    });
                    if (!StringSupport.empty (config ().className)) {
                        Div.$ (left).style (styles ().code ()).$ (code -> {
                            Span.$ (code).text (config ().className);
                        });
                    }
                    for (String line : config ().description)
                        P.$ (left).text (line);
                });
                if (config ().example != null)
                    Div.$ (header).style (styles ().right ()).render (config().example);
            });

            // Body containing a tabbed panel.
            Div.$ (root).style (styles ().body ()).$ (body -> {
                TabbedPanelCreator.$ (body, cfg -> cfg.style (TABSET_STYLE).padding (Insets.em (1)), tpanel -> {
                    tpanel.tab ("options", "Options", buildOptions ());
                    if (Config.Type.FRAGMENT != config().type)
                        tpanel.tab ("behaviour", "Behaviour", buildBehaviour ());
                    tpanel.tab ("styles", "Styles", buildStyles ());
                    tpanel.tab ("gallery", "Gallery", buildGallery ());
                });
            });
        }).build();
        
    }

    protected IComponent buildOptions() {
        return ComponentCreator.build (r -> {
            if (config ().configuration.isEmpty()) {
                if (config().type == Config.Type.FRAGMENT)
                    P.$ (r).text ("No configuration options for this fragment");
                else
                    P.$ (r).text ("No configuration options for this component");
            } else {
                if (config().type == Config.Type.FRAGMENT)
                    P.$ (r).text ("Options either passed through the fragments constructor or configured as an option against the fragment instance.");
                else
                    P.$ (r).text ("Options either passed through the components configuration class or through its constructors.");
            }
            if (!config ().configuration.isEmpty()) {
                Table.$ (r).style (styles ().table ()).$ (tbl-> {
                    Thead.$ (tbl).$ (thead -> {
                        Tr.$ (thead).$ (row -> {
                            Th.$ (row).style (styles ().option ()).text ("Option");
                            Th.$ (row).style (styles ().description ()).text ("Description");
                            Th.$ (row).style (styles ().value ()).text ("Value");
                        });
                    });
                    Tbody.$ (tbl).$ (tbody -> {
                        config ().configuration.forEach (option -> {
                            Tr.$ (tbody).$ (row -> {
                                Td.$ (row).$ (opt -> {
                                    opt.style (styles ().option ());
                                    if (option.required)
                                        opt.style (styles ().required ());
                                    opt.text (option.name);
                                    if (option.constructor)
                                        Span.$ (opt).text ("C");
                                });
                                Td.$ (row).$ (opt -> { 
                                    opt.style (styles ().description ());
                                    opt.text (StringSupport.empty (option.description, "No description availble"));
                                });
                                Td.$ (row).$ (opt -> {
                                    opt.style (styles ().value ());
                                    if (!StringSupport.empty (option.valueType))
                                        Div.$ (opt).style (styles ().type ()).text (option.valueType);
                                    if (!StringSupport.empty (option.valueDescription))
                                        Div.$ (opt).style (styles ().description ()).text (option.valueDescription);
                                    if (!option.values.isEmpty ()) {
                                        Div.$ (opt).style (styles ().values ()).$ (outer -> {
                                            option.values.forEach (value -> {
                                                Div.$ (outer).$ (item -> {
                                                    Div.$ (item).style (styles ().code ()).text (value.value);
                                                    if (!StringSupport.empty(value.description))
                                                        Div.$ (item).text (value.description);
                                                });
                                            });
                                        });
                                    }
                                });
                            });
                        });
                    });
                });
            }
        });
    }

    protected IComponent buildBehaviour() {
        return PanelCreator.build ();
    }

    protected IComponent buildStyles() {
        return ComponentCreator.build (r -> {
            if (config ().styles.isEmpty()) 
                P.$ (r).text ("No style information is available for this component.");
            else
                P.$ (r).text ("Styles are assigned to the component by way of its configuration.");
            if (!config ().css.isEmpty ()) {
                Div.$ (r).style (styles ().css ()).$ (css -> {
                    for (String style : config ().css)
                        Span.$ (css).text (style);
                });
            }
            if (!config ().styles.isEmpty()) {
                Table.$ (r).style (styles ().table ()).$ (
                    Thead.$ ().$ (
                        Tr.$ ().$ (
                            Th.$ ().style (styles ().option ()).text ("Style"),
                            Th.$ ().style (styles ().description ()).text ("Description"),
                            Th.$ ().style (styles ().value ()).text ("Theme variables")
                        )
                    ),
                    Tbody.$ ().$ (tbody -> {
                        config ().styles.forEach (style -> {
                            Tr.$ (tbody).$ (
                                Td.$ ().style (styles ().option ()).text (style.name),
                                Td.$ ().style (styles ().description ()).$ (opt -> {
                                    Div.$ (opt).text (StringSupport.empty (style.description, "No description availble"));
                                    if (!style.css.isEmpty ()) {
                                        Div.$ (opt).style (styles ().css ()). $(css -> {
                                            for (String cssStyle : style.css)
                                                Span.$ (css).text (cssStyle);
                                        });
                                    }
                                }),
                                Td.$ ().style (styles ().value ()).$ (opt -> {
                                    if (!style.variables.isEmpty ()) {
                                        Table.$ (opt).$ (outer -> {
                                            style.variables.forEach (value -> {
                                                Tr.$ (outer).$ (item -> {
                                                    Td.$ (item).style (styles ().type ()).text (value.variable);
                                                    if (StringSupport.empty(value.description))
                                                        Td.$ (item).style (styles ().description ()).text ("-");
                                                    else
                                                        Td.$ (item).style (styles ().description ()).text (value.description);
                                                });
                                            });
                                        });
                                    }
                                })
                            );
                        }); 
                    })
                );
            }
        });
    }

    protected IComponent buildGallery() {
        return ComponentCreator.build (r -> {
            r.style (styles ().gallery ());
            if (config ().examples.isEmpty()) 
                P.$ (r).text ("No examples are available for this component.");
            if (!config ().examples.isEmpty ()) {
                for (ConfigurationExample example : config ().examples) {
                    Div.$ (r).style (styles ().wrapper ()).$ (wrapper -> {
                        Div.$ (wrapper).render (example.example);
                        Div.$ (wrapper).$ (desc -> {
                            if (StringSupport.empty (example.description))
                                desc.text ("-"); 
                            else
                                desc.text (example.description, true);
                        });
                        Div.$ (wrapper).$ (code -> {
                            if (StringSupport.empty (example.code)) {
                                code.text ("-");
                            } else {
                                code.addClassName (styles ().code ());
                                code.text (example.code, true);
                            }
                        });
                    });
                }
            }
        });
    }

    /************************************************************************
     * Local CSS
     ************************************************************************/

    @Override
    protected ILocalCSS styles() {
        return LocalCSS.instance ();
    }

    public interface ILocalCSS extends IComponentCSS {

        public String header();

        public String left();

        public String right();

        public String code();

        public String required();

        public String body();

        public String table();

        public String option();

        public String value();

        public String type();

        public String values();

        public String description();

        public String css();

        public String gallery();

        public String wrapper(); 
        
    }

    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        "com/effacy/jui/ui/client/explorer/ComponentDocumentation.css",
        "com/effacy/jui/ui/client/explorer/ComponentDocumentation_Override.css"
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
     * Custom tabs (slightly more compact)
     ************************************************************************/
    
    public static final TabSet.Config.Style TABSET_STYLE = TabSet.Config.Style.create (TabSetLocalCSS.instance (), false, FontAwesome.minus (), FontAwesome.plus ());

    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        TabSet.ILocalCSS.CSS,
        "com/effacy/jui/ui/client/tabs/TabSet_HorizontalUnderline.css",
        "com/effacy/jui/ui/client/explorer/ComponentDocumentation_TabSet.css"
    })
    public static abstract class TabSetLocalCSS implements TabSet.ILocalCSS {

        private static TabSetLocalCSS STYLES;

        public static TabSet.ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (TabSetLocalCSS) GWT.create (TabSetLocalCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }
}
