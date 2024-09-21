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
import java.util.Map;

import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.builder.DomBuilder;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.dom.jquery.JQuery;
import com.effacy.jui.core.client.dom.renderer.NodeProviderDataRenderer;
import com.effacy.jui.platform.css.client.CssDeclaration;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.ui.client.Theme;
import com.effacy.jui.ui.client.control.Controls;
import com.effacy.jui.ui.client.control.builder.ControlForm;
import com.effacy.jui.ui.client.control.builder.ControlFormCreator;
import com.effacy.jui.ui.client.explorer.ThemeRenderer.ThemeStyle;
import com.effacy.jui.ui.client.modal.ModalDialogCreator;
import com.effacy.jui.validation.model.validator.NotEmptyValidator;
import com.google.gwt.core.client.GWT;

import elemental2.dom.CSSStyleDeclaration;
import elemental2.dom.DomGlobal;
import elemental2.dom.Node;

/**
 * A simple renderer that includes it's own styles. It generates a nicely
 * formatted list of the different CSS variables used in the standard Theme.
 * <p>
 * This renderer takes no data (the data is sourced internally as
 * {@link ThemeStyle}) and so in generic over {@link Void}.
 *
 * @author Jeremy Buckley
 */
public class ThemeRenderer extends NodeProviderDataRenderer<List<ThemeStyle>> {

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.dom.renderer.NodeDataRenderer#generate(java.lang.Object)
     */
    @Override
    protected INodeProvider generate(List<ThemeStyle> data) {
        IThemeRendererCSSS styles = ThemeRendererCSS.instance ();
        return DomBuilder.div (outer -> {
            outer.addClassName (styles.outer ());
            for (ThemeStyle item : data) {
                outer.div (div -> {
                    if (item.type == ThemeStyle.Type.COLOR) {
                        div.css ("cursor", "pointer");
                        div.onclick ((e,n) -> {
                            // Dialog with a single text field to change the code.
                            ModalDialogCreator.build (ControlFormCreator.build (cfg -> {
                                cfg.style (ControlForm.Config.Style.COMPACT);
                            }, panel -> {
                                // ControlSection sec1 = panel.section ().build ();
                                panel.group (grp -> {
                                    grp.control ("color", "The new value (default is given):", Controls.text (cfg -> {
                                        cfg.acceptor ("color");
                                        cfg.validator (NotEmptyValidator.validator ("please enter the HEX of your colour"));
                                    }, ctl -> {
                                        if (item.code != null)
                                            ctl.setValue (item.code);
                                    }), cell -> {
                                        cell.grow (1);
                                    });
                                });
                            }), cfg -> {
                                cfg.title ("Modify the colour").width (Length.px (405)).closable ().removeOnClose ();
                                cfg.action (a -> a.label ("Modify").handler (ah -> {
                                    if (ah.contents ().validate ()) {
                                        String color = (String) ah.contents ().value ("color");
                                        color = color.toLowerCase ();
                                        CSSStyleDeclaration decl = DomGlobal.document.body.style;
                                        decl.setProperty (item.reference, color);
                                        color = decl.getPropertyValue(item.reference);
                                        JQuery.$ (n).find ("h6").text (color);
                                        ah.success ();
                                    } else
                                        ah.fail ();
                                }));
                            }).open ();
                        });
                    }
                    if (item.type != ThemeStyle.Type.HEADER)
                        div.style (styles.wrapper ());
                    else
                        div.style (styles.header ());
                    if (item.type == ThemeStyle.Type.HEADER) {
                        div.h2 (header -> {
                            header.text (item.header);
                        });
                    }
                    if (item.type == ThemeStyle.Type.COLOR) {
                        div.span (swatch -> {
                            swatch.style (styles.swatch ());
                            swatch.css ("background-color", "var(" + item.reference + ")");
                        });
                    }
                    if (item.type == ThemeStyle.Type.FONT) {
                        div.span (font -> {
                            font.style (styles.font ());
                            font.css ("font-family", "var(" + item.reference + ")");
                        });
                    }
                    if (item.type != ThemeStyle.Type.HEADER) {
                        div.div (label -> {
                            label.style (styles.label ());
                            label.h5 ().text (item.reference);
                            label.h6 ().text (item.code);
                            label.p ().text (item.description);
                        });
                    }
                });
            }
        }).build ();
    }

    protected native CSSStyleDeclaration computedStyle(Node el) /*-{
        return $wnd.getComputedStyle(el);
    }-*/;

    /**
     * Theme is used as the (internally supplied) data to the renderer.
     */
    public static class ThemeStyle {
        enum Type {
            HEADER, COLOR, GEOM, FONT;
        }

        public Type type;
        public String header;
        public String reference;
        public String code;
        public String description;

        public ThemeStyle(String header) {
            this.type = Type.HEADER;
            this.header = header;
        }

        public ThemeStyle(String reference, String description) {
            this (Type.COLOR, reference, null, description);
        }

        public ThemeStyle(String reference, String code, String description) {
            this (Type.COLOR, reference, code, description);
        }

        public ThemeStyle(Type type, String reference, String code, String description) {
            this.type = type;
            this.reference = reference;
            this.code = code;
            this.description = description;
            if (code == null) {
                Map<String,Map<String,String>> css = Theme.styles ().getCssDeclarations ();
                this.code = css.get(".theme").get(reference);
            }
        }

        public static List<ThemeStyle> colors() {
            List<ThemeStyle> styles = new ArrayList<> ();

            styles.add (new ThemeStyle ("Primary tones"));
            styles.add (new ThemeStyle ("--jui-color-primary05", "Primary colour tone"));
            styles.add (new ThemeStyle ("--jui-color-primary10", "Primary colour tone"));
            styles.add (new ThemeStyle ("--jui-color-primary20", "Primary colour tone"));
            styles.add (new ThemeStyle ("--jui-color-primary30", "Primary colour tone"));
            styles.add (new ThemeStyle ("--jui-color-primary40", "Primary colour tone"));
            styles.add (new ThemeStyle ("--jui-color-primary50", "Primary colour tone"));
            styles.add (new ThemeStyle ("--jui-color-primary60", "Primary colour tone"));
            styles.add (new ThemeStyle ("--jui-color-primary70", "Primary colour tone"));
            styles.add (new ThemeStyle ("--jui-color-primary80", "Primary colour tone"));
            styles.add (new ThemeStyle ("--jui-color-primary90", "Primary colour tone"));

            styles.add (new ThemeStyle ("Secondary tones"));
            styles.add (new ThemeStyle ("--jui-color-secondary05", "Secondary colour tone"));
            styles.add (new ThemeStyle ("--jui-color-secondary10", "Secondary colour tone"));
            styles.add (new ThemeStyle ("--jui-color-secondary20", "Secondary colour tone"));
            styles.add (new ThemeStyle ("--jui-color-secondary30", "Secondary colour tone"));
            styles.add (new ThemeStyle ("--jui-color-secondary40", "Secondary colour tone"));
            styles.add (new ThemeStyle ("--jui-color-secondary50", "Secondary colour tone"));
            styles.add (new ThemeStyle ("--jui-color-secondary60", "Secondary colour tone"));
            styles.add (new ThemeStyle ("--jui-color-secondary70", "Secondary colour tone"));
            styles.add (new ThemeStyle ("--jui-color-secondary80", "Secondary colour tone"));
            styles.add (new ThemeStyle ("--jui-color-secondary90", "Secondary colour tone"));

            styles.add (new ThemeStyle ("Tertiary tones"));
            styles.add (new ThemeStyle ("--jui-color-tertiary05", "Tertiary colour tone"));
            styles.add (new ThemeStyle ("--jui-color-tertiary10", "Tertiary colour tone"));
            styles.add (new ThemeStyle ("--jui-color-tertiary20", "Tertiary colour tone"));
            styles.add (new ThemeStyle ("--jui-color-tertiary30", "Tertiary colour tone"));
            styles.add (new ThemeStyle ("--jui-color-tertiary40", "Tertiary colour tone"));
            styles.add (new ThemeStyle ("--jui-color-tertiary50", "Tertiary colour tone"));
            styles.add (new ThemeStyle ("--jui-color-tertiary60", "Tertiary colour tone"));
            styles.add (new ThemeStyle ("--jui-color-tertiary70", "Tertiary colour tone"));
            styles.add (new ThemeStyle ("--jui-color-tertiary80", "Tertiary colour tone"));
            styles.add (new ThemeStyle ("--jui-color-tertiary90", "Tertiary colour tone"));

            styles.add (new ThemeStyle ("Neutral tones"));
            styles.add (new ThemeStyle ("--jui-color-neutral05", "Neutral colour tone"));
            styles.add (new ThemeStyle ("--jui-color-neutral10", "Neutral colour tone"));
            styles.add (new ThemeStyle ("--jui-color-neutral20", "Neutral colour tone"));
            styles.add (new ThemeStyle ("--jui-color-neutral30", "Neutral colour tone"));
            styles.add (new ThemeStyle ("--jui-color-neutral40", "Neutral colour tone"));
            styles.add (new ThemeStyle ("--jui-color-neutral50", "Neutral colour tone"));
            styles.add (new ThemeStyle ("--jui-color-neutral60", "Neutral colour tone"));
            styles.add (new ThemeStyle ("--jui-color-neutral70", "Neutral colour tone"));
            styles.add (new ThemeStyle ("--jui-color-neutral80", "Neutral colour tone"));
            styles.add (new ThemeStyle ("--jui-color-neutral90", "Neutral colour tone"));

            styles.add (new ThemeStyle ("Error tones"));
            styles.add (new ThemeStyle ("--jui-color-error05", "Error colour tone"));
            styles.add (new ThemeStyle ("--jui-color-error10", "Error colour tone"));
            styles.add (new ThemeStyle ("--jui-color-error20", "Error colour tone"));
            styles.add (new ThemeStyle ("--jui-color-error30", "Error colour tone"));
            styles.add (new ThemeStyle ("--jui-color-error40", "Error colour tone"));
            styles.add (new ThemeStyle ("--jui-color-error50", "Error colour tone"));
            styles.add (new ThemeStyle ("--jui-color-error60", "Error colour tone"));
            styles.add (new ThemeStyle ("--jui-color-error70", "Error colour tone"));
            styles.add (new ThemeStyle ("--jui-color-error80", "Error colour tone"));
            styles.add (new ThemeStyle ("--jui-color-error90", "Error colour tone"));

            styles.add (new ThemeStyle ("Warning tones"));
            styles.add (new ThemeStyle ("--jui-color-warning05", "Warning colour tone"));
            styles.add (new ThemeStyle ("--jui-color-warning10", "Warning colour tone"));
            styles.add (new ThemeStyle ("--jui-color-warning20", "Warning colour tone"));
            styles.add (new ThemeStyle ("--jui-color-warning30", "Warning colour tone"));
            styles.add (new ThemeStyle ("--jui-color-warning40", "Warning colour tone"));
            styles.add (new ThemeStyle ("--jui-color-warning50", "Warning colour tone"));
            styles.add (new ThemeStyle ("--jui-color-warning60", "Warning colour tone"));
            styles.add (new ThemeStyle ("--jui-color-warning70", "Warning colour tone"));
            styles.add (new ThemeStyle ("--jui-color-warning80", "Warning colour tone"));
            styles.add (new ThemeStyle ("--jui-color-warning90", "Warning colour tone"));

            styles.add (new ThemeStyle ("Success tones"));
            styles.add (new ThemeStyle ("--jui-color-success05", "Success colour tone"));
            styles.add (new ThemeStyle ("--jui-color-success10", "Success colour tone"));
            styles.add (new ThemeStyle ("--jui-color-success20", "Success colour tone"));
            styles.add (new ThemeStyle ("--jui-color-success30", "Success colour tone"));
            styles.add (new ThemeStyle ("--jui-color-success40", "Success colour tone"));
            styles.add (new ThemeStyle ("--jui-color-success50", "Success colour tone"));
            styles.add (new ThemeStyle ("--jui-color-success60", "Success colour tone"));
            styles.add (new ThemeStyle ("--jui-color-success70", "Success colour tone"));
            styles.add (new ThemeStyle ("--jui-color-success80", "Success colour tone"));
            styles.add (new ThemeStyle ("--jui-color-success90", "Success colour tone"));

            styles.add (new ThemeStyle ("Auxillary colours"));
            styles.add (new ThemeStyle ("--jui-color-aux-focus1", "Focus outline"));
            styles.add (new ThemeStyle ("--jui-color-aux-focus2", "Focus outline (shadow)"));
            styles.add (new ThemeStyle ("--jui-color-aux-blue", "System blue (used by controls)"));
            styles.add (new ThemeStyle ("--jui-color-aux-white", "Absolute white"));
            styles.add (new ThemeStyle ("--jui-color-aux-black", "Absolute black"));

            return styles;
        }

        public static List<ThemeStyle> topography() {
            List<ThemeStyle> styles = new ArrayList<> ();

            styles.add (new ThemeStyle ("Geometry"));
            styles.add (new ThemeStyle (Type.GEOM, "--jui-border-radius", null, "Standard border radius"));
            styles.add (new ThemeStyle (Type.GEOM, "--jui-border-radius-soft",  null, "Softer border radius"));
            styles.add (new ThemeStyle (Type.GEOM, "--jui-border-radius-hard",  null, "Harder border radius"));

            styles.add (new ThemeStyle ("States"));
            styles.add (new ThemeStyle ("--jui-state-focus", "Focus border colour"));
            styles.add (new ThemeStyle ("--jui-state-focus-offset", "Focus border colour (shadow)"));
            styles.add (new ThemeStyle ("--jui-state-disabled", "Disabled colour"));
            styles.add (new ThemeStyle ("--jui-state-disabled-offset", "Disabled colour (alternative)"));
            styles.add (new ThemeStyle ("--jui-state-error", "Error text"));
            styles.add (new ThemeStyle ("--jui-state-error-offset", "Error indicator (highlights)"));
            styles.add (new ThemeStyle ("--jui-state-waiting", "Waiting text"));
            styles.add (new ThemeStyle ("--jui-state-waiting-bg", "Waiting background"));
            styles.add (new ThemeStyle ("--jui-state-waiting-bg-offset", "Waiting background (transition)"));

            styles.add (new ThemeStyle ("Line"));
            styles.add (new ThemeStyle ("--jui-line", "Standard line"));
            styles.add (new ThemeStyle ("--jui-line-dark", "Darker line)"));
            styles.add (new ThemeStyle ("--jui-line-light", "Lighter line"));

            styles.add (new ThemeStyle ("Text"));
            styles.add (new ThemeStyle ("--jui-text", "Body text"));
            styles.add (new ThemeStyle ("--jui-text-offset", "Offset against text (background)"));
            styles.add (new ThemeStyle ("--jui-text-header", "Header text"));
            styles.add (new ThemeStyle ("--jui-text-header-sub", "Sub-heading text"));
            styles.add (new ThemeStyle ("--jui-text-subtle", "Informational text"));
            styles.add (new ThemeStyle ("--jui-text-link", "Text links"));
            styles.add (new ThemeStyle ("--jui-text-link-hover", "Text links (hover)"));
            styles.add (new ThemeStyle ("--jui-text-error", "Error message text"));
            styles.add (new ThemeStyle ("--jui-text-disabled", "Disabled text"));
        
            return styles;
        }

        public static List<ThemeStyle> components() {
            List<ThemeStyle> styles = new ArrayList<> ();

            styles.add (new ThemeStyle ("Controls"));
            styles.add (new ThemeStyle (Type.FONT, "--jui-ctl-font", null, "Font to use for controls"));
            styles.add (new ThemeStyle ("--jui-ctl-active", ""));
            styles.add (new ThemeStyle (Type.GEOM, "--jui-ctl-opacity-readonly", null, "Opacity for disabled state"));
            styles.add (new ThemeStyle ("--jui-ctl-bg", "Background (text entry)"));
            styles.add (new ThemeStyle ("--jui-ctl-bg-disabled", "Background (disabled)"));
            styles.add (new ThemeStyle ("--jui-ctl-bg-readonly", "Background (read only)"));
            styles.add (new ThemeStyle ("--jui-ctl-bg-offset", "Background (darker)"));
            styles.add (new ThemeStyle ("--jui-ctl-border", "Border color"));
            styles.add (new ThemeStyle (Type.GEOM, "--jui-ctl-border-radius", null, "Border radius"));
            styles.add (new ThemeStyle ("--jui-ctl-text", "Text color (entry)"));
            styles.add (new ThemeStyle ("--jui-ctl-text-placeholder", "Placeholder color"));
            styles.add (new ThemeStyle ("--jui-ctl-text-disabled", "Text color (disabled)"));
            styles.add (new ThemeStyle ("--jui-ctl-text-readonly", "Text color (read only)"));
            styles.add (new ThemeStyle ("--jui-ctl-text-header", "Text color (header)"));
            styles.add (new ThemeStyle ("--jui-ctl-text-subtle", "Text color (subtle)"));
            styles.add (new ThemeStyle ("--jui-ctl-text-link", "Text color (link)"));
            styles.add (new ThemeStyle ("--jui-ctl-text-offset", "Background"));
            styles.add (new ThemeStyle ("--jui-ctl-action", "Action icon color"));
            styles.add (new ThemeStyle ("--jui-ctl-action-disabled", "Action icon color (disabled)"));
            styles.add (new ThemeStyle ("--jui-ctl-action-readonly", "Action icon color (read only)"));
            styles.add (new ThemeStyle ("--jui-ctl-action-hover", "Action icon color (hover)"));
            styles.add (new ThemeStyle ("--jui-ctl-focus", "Focus outline color"));
            styles.add (new ThemeStyle ("--jui-ctl-focus-offset", "Focus outline color (shadow)"));
            styles.add (new ThemeStyle ("--jui-ctl-err-focus", "Error outline color"));
            styles.add (new ThemeStyle ("--jui-ctl-err-focus-offset", "Error outline color (shadow)"));

            styles.add (new ThemeStyle ("Buttons"));
            styles.add (new ThemeStyle ("--jui-btn-bg", "Background color"));
            styles.add (new ThemeStyle ("--jui-btn-bg-hover", "Background color (hover)"));
            styles.add (new ThemeStyle ("--jui-btn-bg-disabled", "Background color (disabled)"));
            styles.add (new ThemeStyle ("--jui-btn-bg-disabled-offset", "Background color (disabled darker)"));
            styles.add (new ThemeStyle ("--jui-btn-border", "Border color"));
            styles.add (new ThemeStyle (Type.GEOM, "--jui-btn-border-radius", null, "Border radius"));
            styles.add (new ThemeStyle ("--jui-btn-text", "Text color"));

            styles.add (new ThemeStyle ("--jui-btn-danger-bg", "Danger variant"));
            styles.add (new ThemeStyle ("--jui-btn-danger-bg-hover", "Danger variant"));
            styles.add (new ThemeStyle ("--jui-btn-danger-border", "Danger variant"));

            styles.add (new ThemeStyle ("--jui-btn-warning-bg", "Warning variant"));
            styles.add (new ThemeStyle ("--jui-btn-warning-bg-hover", "Warning variant"));
            styles.add (new ThemeStyle ("--jui-btn-warning-border", "Warning variant"));

            styles.add (new ThemeStyle ("--jui-btn-success-bg", "Success variant"));
            styles.add (new ThemeStyle ("--jui-btn-success-bg-hover", "Success variant"));
            styles.add (new ThemeStyle ("--jui-btn-success-border", "Success variant"));

            return styles;
        }
    }

    /**
     * Styles for the standard item.
     */
    public static interface IThemeRendererCSSS extends CssDeclaration {

        public String outer();

        public String wrapper();

        public String header();

        public String label();

        public String swatch();

        public String font();

    }

    @CssResource("com/effacy/jui/ui/client/explorer/ThemeRenderer.css")
    public static abstract class ThemeRendererCSS implements IThemeRendererCSSS {

        private static ThemeRendererCSS STYLES;

        public static IThemeRendererCSSS instance() {
            if (STYLES == null) {
                STYLES = (ThemeRendererCSS) GWT.create (ThemeRendererCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }
}
