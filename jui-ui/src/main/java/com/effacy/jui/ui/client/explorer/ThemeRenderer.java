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
import com.effacy.jui.ui.client.modal.ModalDialogCreator;
import com.effacy.jui.ui.client.control.Controls;
import com.effacy.jui.ui.client.control.builder.ControlForm;
import com.effacy.jui.ui.client.control.builder.ControlFormCreator;
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
public class ThemeRenderer extends NodeProviderDataRenderer<List<ThemeRenderer.ThemeStyle>> {

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
                            ModalDialogCreator.build (ControlFormCreator.build (cfg -> {
                                cfg.style (ControlForm.Config.Style.COMPACT);
                            }, panel -> {
                                panel.group (grp -> {
                                    grp.control ("color", "The new value (default is given):", Controls.text (cfg -> {
                                        cfg.acceptor ("color");
                                        cfg.validator (NotEmptyValidator.validator ("please enter the HEX of your colour"));
                                    }, ctl -> {
                                        if (item.code != null)
                                            ctl.setValue (item.code);
                                    }));
                                });
                            }), cfg -> {
                                cfg.title ("Modify the colour").width (Length.px (405)).closable ().removeOnClose ();
                                cfg.action (a -> a.label ("Modify").handler (ah -> {
                                    if (ah.contents ().validate ()) {
                                        String color = (String) ah.contents ().value ("color");
                                        color = color.toLowerCase ();
                                        CSSStyleDeclaration decl = DomGlobal.document.body.style;
                                        decl.setProperty (item.reference, color);
                                        color = decl.getPropertyValue (item.reference);
                                        JQuery.$ (n).find ("h6").text (color);
                                        ah.success ();
                                    } else {
                                        ah.fail ();
                                    }
                                }));
                            }).open ();
                        });
                    }
                    if (item.type != ThemeStyle.Type.HEADER) {
                        div.style (styles.wrapper ());
                    } else {
                        div.style (styles.header ());
                    }
                    if (item.type == ThemeStyle.Type.HEADER) {
                        div.h2 (header -> header.text (item.header));
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
                Map<String, Map<String, String>> css = Theme.styles ().getCssDeclarations ();
                Map<String, String> theme = css.get (".theme");
                if (theme != null) {
                    this.code = theme.get (reference);
                }
            }
        }

        public static List<ThemeStyle> colors() {
            List<ThemeStyle> styles = new ArrayList<> ();

            styles.add (new ThemeStyle ("Reference palette: primary"));
            addRange (styles, "--jui-color-primary", "Primary colour tone");
            styles.add (new ThemeStyle ("Reference palette: secondary"));
            addRange (styles, "--jui-color-secondary", "Secondary colour tone");
            styles.add (new ThemeStyle ("Reference palette: ink"));
            addRange (styles, "--jui-color-ink", "Ink (cool neutral) tone");
            styles.add (new ThemeStyle ("Reference palette: neutral"));
            addRange (styles, "--jui-color-neutral", "Neutral colour tone");
            styles.add (new ThemeStyle ("Reference palette: error"));
            addRange (styles, "--jui-color-error", "Error colour tone");
            styles.add (new ThemeStyle ("Reference palette: warning"));
            addRange (styles, "--jui-color-warning", "Warning colour tone");
            styles.add (new ThemeStyle ("Reference palette: success"));
            addRange (styles, "--jui-color-success", "Success colour tone");
            styles.add (new ThemeStyle ("Reference palette: info"));
            addRange (styles, "--jui-color-info", "Info colour tone");

            styles.add (new ThemeStyle ("Reference palette: auxiliary"));
            styles.add (new ThemeStyle ("--jui-color-aux-white", "Absolute white"));
            styles.add (new ThemeStyle ("--jui-color-aux-black", "Absolute black"));

            return styles;
        }

        public static List<ThemeStyle> topography() {
            List<ThemeStyle> styles = new ArrayList<> ();

            styles.add (new ThemeStyle ("Role tokens: surfaces"));
            styles.add (new ThemeStyle ("--jui-role-surface-canvas", "Application canvas surface"));
            styles.add (new ThemeStyle ("--jui-role-surface-raised", "Raised component surface"));
            styles.add (new ThemeStyle ("--jui-role-surface-muted", "Muted chrome surface"));
            styles.add (new ThemeStyle ("--jui-role-surface-sunken", "Sunken or disabled surface"));
            styles.add (new ThemeStyle ("--jui-role-surface-overlay", "Overlay or dialog surface"));
            styles.add (new ThemeStyle ("--jui-role-surface-accent", "Accent-tinted surface"));
            styles.add (new ThemeStyle ("--jui-role-surface-error", "Error-tinted surface"));

            styles.add (new ThemeStyle ("Role tokens: text and borders"));
            styles.add (new ThemeStyle ("--jui-role-text-default", "Default body text"));
            styles.add (new ThemeStyle ("--jui-role-text-muted", "Muted support text"));
            styles.add (new ThemeStyle ("--jui-role-text-heading", "Heading text"));
            styles.add (new ThemeStyle ("--jui-role-text-link", "Interactive link text"));
            styles.add (new ThemeStyle ("--jui-role-border-subtle", "Subtle border"));
            styles.add (new ThemeStyle ("--jui-role-border-default", "Default border"));
            styles.add (new ThemeStyle ("--jui-role-border-strong", "Strong border"));
            styles.add (new ThemeStyle ("--jui-role-border-contrast", "High-contrast border/text helper"));

            styles.add (new ThemeStyle ("Role tokens: interaction and feedback"));
            styles.add (new ThemeStyle ("--jui-role-interactive-primary", "Primary interactive colour"));
            styles.add (new ThemeStyle ("--jui-role-interactive-primary-hover", "Primary interactive hover"));
            styles.add (new ThemeStyle ("--jui-role-interactive-primary-on", "Text/icon on primary interactive surfaces"));
            styles.add (new ThemeStyle ("--jui-role-feedback-info", "Informational feedback"));
            styles.add (new ThemeStyle ("--jui-role-feedback-success", "Success feedback"));
            styles.add (new ThemeStyle ("--jui-role-feedback-warning", "Warning feedback"));
            styles.add (new ThemeStyle ("--jui-role-feedback-error", "Error feedback"));
            styles.add (new ThemeStyle ("--jui-role-focus-ring", "Focus ring colour"));
            styles.add (new ThemeStyle ("--jui-role-focus-shadow", "Focus shadow colour"));

            styles.add (new ThemeStyle ("Scale tokens: spacing"));
            styles.add (new ThemeStyle (Type.GEOM, "--jui-space-1", null, "4px spacing step"));
            styles.add (new ThemeStyle (Type.GEOM, "--jui-space-2", null, "8px spacing step"));
            styles.add (new ThemeStyle (Type.GEOM, "--jui-space-3", null, "12px spacing step"));
            styles.add (new ThemeStyle (Type.GEOM, "--jui-space-4", null, "16px spacing step"));
            styles.add (new ThemeStyle (Type.GEOM, "--jui-space-6", null, "24px spacing step"));
            styles.add (new ThemeStyle (Type.GEOM, "--jui-space-8", null, "32px spacing step"));
            styles.add (new ThemeStyle (Type.GEOM, "--jui-space-12", null, "48px spacing step"));

            styles.add (new ThemeStyle ("Scale tokens: typography"));
            styles.add (new ThemeStyle (Type.FONT, "--jui-font-family-sans", null, "Primary sans-serif font family"));
            styles.add (new ThemeStyle (Type.FONT, "--jui-font-family-mono", null, "Monospace font family"));
            styles.add (new ThemeStyle (Type.GEOM, "--jui-font-size-sm", null, "Small font size"));
            styles.add (new ThemeStyle (Type.GEOM, "--jui-font-size-md", null, "Default font size"));
            styles.add (new ThemeStyle (Type.GEOM, "--jui-font-size-lg", null, "Large font size"));
            styles.add (new ThemeStyle (Type.GEOM, "--jui-font-weight-medium", null, "Medium font weight"));
            styles.add (new ThemeStyle (Type.GEOM, "--jui-font-weight-semibold", null, "Semibold font weight"));

            styles.add (new ThemeStyle ("Scale tokens: geometry and motion"));
            styles.add (new ThemeStyle (Type.GEOM, "--jui-radius-xs", null, "Extra small radius"));
            styles.add (new ThemeStyle (Type.GEOM, "--jui-radius-sm", null, "Small radius"));
            styles.add (new ThemeStyle (Type.GEOM, "--jui-radius-md", null, "Medium radius"));
            styles.add (new ThemeStyle (Type.GEOM, "--jui-radius-lg", null, "Large radius"));
            styles.add (new ThemeStyle (Type.GEOM, "--jui-control-height", null, "Standard control height"));
            styles.add (new ThemeStyle (Type.GEOM, "--jui-duration-fast", null, "Fast interaction duration"));
            styles.add (new ThemeStyle (Type.GEOM, "--jui-duration-standard", null, "Standard interaction duration"));

            return styles;
        }

        public static List<ThemeStyle> components() {
            List<ThemeStyle> styles = new ArrayList<> ();

            styles.add (new ThemeStyle ("Component tokens: controls"));
            styles.add (new ThemeStyle (Type.FONT, "--jui-comp-control-font-family", null, "Font family used by controls"));
            styles.add (new ThemeStyle (Type.GEOM, "--jui-comp-control-height", null, "Default control height"));
            styles.add (new ThemeStyle ("--jui-comp-control-surface", "Default control surface"));
            styles.add (new ThemeStyle ("--jui-comp-control-border", "Default control border"));
            styles.add (new ThemeStyle ("--jui-comp-control-text", "Default control text"));
            styles.add (new ThemeStyle ("--jui-comp-control-action", "Default control action/icon colour"));
            styles.add (new ThemeStyle ("--jui-comp-control-focus", "Default control focus ring"));

            styles.add (new ThemeStyle ("Component tokens: buttons"));
            styles.add (new ThemeStyle (Type.GEOM, "--jui-comp-button-height", null, "Default button height"));
            styles.add (new ThemeStyle ("--jui-comp-button-surface", "Default button background"));
            styles.add (new ThemeStyle ("--jui-comp-button-surface-hover", "Default button hover background"));
            styles.add (new ThemeStyle ("--jui-comp-button-border", "Default button border"));
            styles.add (new ThemeStyle ("--jui-comp-button-text", "Default button text"));
            styles.add (new ThemeStyle ("--jui-comp-button-outline-surface", "Outlined button background"));
            styles.add (new ThemeStyle ("--jui-comp-button-link-text", "Link button text"));
            styles.add (new ThemeStyle ("--jui-comp-button-danger-surface", "Danger button background"));
            styles.add (new ThemeStyle ("--jui-comp-button-success-surface", "Success button background"));
            styles.add (new ThemeStyle ("--jui-comp-button-warning-surface", "Warning button background"));

            styles.add (new ThemeStyle ("Component tokens: navigators"));
            styles.add (new ThemeStyle ("--jui-comp-tabset-surface", "Navigator chrome surface"));
            styles.add (new ThemeStyle ("--jui-comp-tabset-surface-hover", "Navigator hover surface"));
            styles.add (new ThemeStyle ("--jui-comp-tabset-surface-active", "Navigator active surface"));
            styles.add (new ThemeStyle ("--jui-comp-tabset-border", "Navigator border"));
            styles.add (new ThemeStyle ("--jui-comp-tabset-text", "Navigator text"));
            styles.add (new ThemeStyle ("--jui-comp-tabset-text-active", "Navigator active text"));

            styles.add (new ThemeStyle ("Component tokens: dialogs, tables, notifications and forms"));
            styles.add (new ThemeStyle ("--jui-comp-dialog-surface", "Dialog surface"));
            styles.add (new ThemeStyle ("--jui-comp-dialog-border", "Dialog border"));
            styles.add (new ThemeStyle ("--jui-comp-dialog-header-surface", "Dialog header surface"));
            styles.add (new ThemeStyle ("--jui-comp-dialog-header-divider", "Dialog header divider"));
            styles.add (new ThemeStyle ("--jui-comp-dialog-footer-surface", "Dialog footer surface"));
            styles.add (new ThemeStyle ("--jui-comp-dialog-footer-divider", "Dialog footer divider"));
            styles.add (new ThemeStyle ("--jui-comp-table-header-surface", "Table header surface"));
            styles.add (new ThemeStyle ("--jui-comp-table-row-border", "Table row divider"));
            styles.add (new ThemeStyle ("--jui-comp-notification-info-accent", "Notification accent"));
            styles.add (new ThemeStyle ("--jui-comp-notification-error-surface", "Notification error surface"));
            styles.add (new ThemeStyle ("--jui-comp-form-header", "Form heading colour"));
            styles.add (new ThemeStyle ("--jui-comp-form-error-surface", "Form error surface"));

            styles.add (new ThemeStyle ("Legacy compatibility"));
            styles.add (new ThemeStyle ("--jui-ctl-bg", "Compatibility alias for the new control surface token"));
            styles.add (new ThemeStyle ("--jui-btn-bg", "Compatibility alias for the new button surface token"));
            styles.add (new ThemeStyle ("--jui-tabset-bg-02", "Compatibility alias for the new navigator chrome token"));
            styles.add (new ThemeStyle ("--jui-text", "Compatibility alias for the new default text role"));

            return styles;
        }

        private static void addRange(List<ThemeStyle> styles, String prefix, String description) {
            String[] suffixes = { "05", "10", "20", "30", "40", "50", "60", "70", "80", "90" };
            for (String suffix : suffixes) {
                styles.add (new ThemeStyle (prefix + suffix, description));
            }
        }
    }

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
