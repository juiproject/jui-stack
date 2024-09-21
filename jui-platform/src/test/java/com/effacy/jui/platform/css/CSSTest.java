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
package com.effacy.jui.platform.css;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.effacy.jui.platform.css.client.CssResource.Combine;
import com.effacy.jui.platform.css.rebind.CssProcessor;
import com.effacy.jui.platform.css.rebind.parser.ExpressionParser.ExpressionParserException;

public class CSSTest {
    
    @Test
    public void testCssGeneration001() throws Exception{
        String file = load ("Test001.css");

        CssProcessor processor = new CssProcessor ().load (file);
        Assertions.assertEquals(".test1>div>.test2{border:1px solid green}.test2{width:1em}", processor.export ());

        processor.remap(v ->  "prefix-" + v);
        Assertions.assertEquals (".prefix-test1>div>.prefix-test2{border:1px solid green}.prefix-test2{width:1em}", processor.export ());
        Assertions.assertEquals ("prefix-test1", processor.mappedSelector ("test1"));
        Assertions.assertEquals ("prefix-test2", processor.mappedSelector ("test2"));
    }

    @Test
    public void testCssGenerationComposeAppend() throws Exception{
        String file1 = load ("Test001.css");
        String file2 = load ("Test002.css");

        CssProcessor processor = new CssProcessor ().combine(Combine.APPEND).load (file1).load(file2);
        Assertions.assertEquals(".test1>div>.test2{border:1px solid green}.test2{width:1em}.test2{border-radius:2em;height:10em}", processor.export ());
    }

    @Test
    public void testCssGenerationComposeReplace() throws Exception{
        String file1 = load ("Test001.css");
        String file2 = load ("Test002.css");

        CssProcessor processor = new CssProcessor ().combine(Combine.REPLACE).load (file1).load(file2);
        Assertions.assertEquals(".test1>div>.test2{border:1px solid green}.test2{border-radius:2em;height:10em}", processor.export ());
    }

    @Test
    public void testCssGenerationComposeMerge01() throws Exception{
        String file1 = load ("Test001.css");
        String file2 = load ("Test002.css");

        CssProcessor processor = new CssProcessor ().combine(Combine.MERGE).load (file1).load(file2);
        Assertions.assertEquals(".test1>div>.test2{border:1px solid green}.test2{width:1em;border-radius:2em;height:10em}", processor.export ());
    }

    @Test
    public void testCssGenerationComposeMerge02() throws Exception{
        String file1 = load ("Test021.css");
        String file2 = load ("Test022.css");

        CssProcessor processor = new CssProcessor ().combine(Combine.MERGE).load (file1).load(file2);
        Assertions.assertEquals (".component{--cpt-form-group-depth0-gap:3em;--cpt-form-group-depth0-gap:2em}", processor.export ());
    }

    @Test
    public void testCssGenerationDefEval001() throws Exception{
        String file1 = load ("Test003.css");

        CssProcessor processor = new CssProcessor ().load (file1);
        Assertions.assertEquals(".test1{border-radius:__RADIUS__;background-color:__COLOR__;height:10em}", processor.export ());
        Assertions.assertTrue (processor.substitutions ().containsKey ("__RADIUS__"));
        Assertions.assertEquals("\"2px\"", processor.substitutions ().get("__RADIUS__"));
        Assertions.assertTrue (processor.substitutions ().containsKey ("__COLOR__"));
        Assertions.assertEquals("com.effacy.jui.ui.client.Theme.color()", processor.substitutions ().get("__COLOR__"));
    }

    @Test
    public void testCssGenerationDefEval002() throws Exception {
        String file1 = load ("Test005.css");

        CssProcessor processor = new CssProcessor ().load (file1);
        Assertions.assertEquals(".theme{--eff-color-alt:__COLORALT__}", processor.export ());
        Assertions.assertTrue (processor.substitutions ().containsKey ("__COLORALT__"));
        Assertions.assertEquals("com.effacy.jui.ui.client.Theme.colorAlt()", processor.substitutions ().get("__COLORALT__"));
    }

    @Test
    public void testCssGenerationDefEvalFail() throws Exception {
        String file1 = load ("Test004.css");
        try {
            new CssProcessor ().load (file1);
        } catch (ExpressionParserException e) {
            System.out.println (e.getMessage ());
        }
    }

    private static String T006 = ".theme{--eff-color:__COLOR__;--eff-color-20:__COLOR20__;--eff-color-40:__COLOR40__;--eff-color-alt:__COLORALT__;--eff-color-bg:__COLORBACKGROUND__;--eff-color-focus:__COLORFOCUSBORDER__;--eff-color-focus-border:__COLORFOCUSBORDEROUTER__;--eff-color-invalid:__COLORERRORBORDER__;--eff-color-invalid-border:__COLORERRORBORDEROUTER__;--eff-color-disabled:__COLORDISABLED__;--eff-color-disabled-bg:__COLORDISABLEDBACKGROUND__;--eff-color-ro:__COLORREADONLY__;--eff-color-ro-bg:__COLORREADONLYBACKGROUND__;--eff-color-waiting-bg:__COLORWAITINGBACKGROUND__;--eff-color-waiting-bg-light:__COLORWAITINGBACKGROUNDLIGHT__;--eff-color-waiting-border:__COLORWAITINGBORDER__;--eff-color-link:__COLORLINK__;--eff-color-btn-disabled:__COLORBUTTONDISABLED__;--eff-color-btn:__COLORBUTTON__;--eff-color-btn-hover:__COLORBUTTONHOVER__;--eff-color-btn-text:__COLORBUTTONTEXT__;--eff-color-btn-danger:__COLORBUTTONDANGER__;--eff-color-btn-danger-hover:__COLORBUTTONDANGERHOVER__;--eff-color-btn-danger-text:__COLORBUTTONDANGERTEXT__;--eff-color-btn-warning:__COLORBUTTONWARNING__;--eff-color-btn-warning-hover:__COLORBUTTONWARNINGHOVER__;--eff-color-btn-warning-text:__COLORBUTTONWARNINGTEXT__;--eff-color-btn-success:__COLORBUTTONSUCCESS__;--eff-color-btn-success-hover:__COLORBUTTONSUCCESSHOVER__;--eff-color-btn-success-text:__COLORBUTTONSUCCESSTEXT__;--eff-color-loading:__COLORLOADING__;--eff-border-radius:__BORDERRADIUS__;--eff-border-radius-soft:__BORDERRADIUSSOFT__;--eff-color-bg-primary:__COLORBACKGROUNDPRIMARY__;--eff-color-bg-secondary:__COLORBACKGROUNDSECONDARY__;--eff-color-bg-secondary120:__COLORBACKGROUNDSECONDARY120__;--eff-color-bg-secondary140:__COLORBACKGROUNDSECONDARY140__;--eff-color-text:__COLORTEXT__;--eff-color-text20:__COLORTEXT20__;--eff-color-text40:__COLORTEXT40__;--eff-color-border:__COLORBORDER__;--eff-color-border-sep:__COLORBORDERSEPARATOR__;--eff-color-ctl-active:__COLORCONTROLACTIVE__;--eff-color-ctl-text:__COLORCONTROLTEXT__;--eff-color-ctl-info:__COLORCONTROLINFO__;--eff-color-ctl-bg:__COLORCONTROLBACKGROUND__;--eff-color-ctl-bg-alt:__COLORCONTROLBACKGROUNDALT__;--eff-color-ctl-border:__COLORCONTROLBORDER__;--eff-color-ctl-ind:__COLORCONTROLINDICATOR__;--eff-color-ctl-place:__COLORCONTROLPLACEHOLDER__;--eff-color-ctl-hover:__COLORCONTROLHOVER__;--eff-color-ctl-ro-text:__COLORCONTROLROTEXT__;--eff-color-ctl-ro-bg:__COLORCONTROLROBACKGROUND__;--eff-color-ctl-ro-border:__COLORCONTROLROBORDER__;--eff-color-ctl-ro-ind:__COLORCONTROLROINDICATOR__;--eff-opacity-ctl-ro:__OPACITYCONTROLRO__;--eff-color-ctl-dis-text:__COLORCONTROLDISABLEDTEXT__;--eff-color-ctl-dis-bg:__COLORCONTROLDISABLEDBACKGROUND__;--eff-color-ctl-dis-border:__COLORCONTROLDISABLEDBORDER__;--eff-color-ctl-dis-ind:__COLORCONTROLDISABLEDINDICATOR__;--eff-opacity-ctl-dis:__OPACITYCONTROLDISABLED__;--eff-color-ctl-err-text:__COLORCONTROLERRORTEXT__;--eff-color-ctl-err-bg:__COLORCONTROLERRORBACKGROUND__;--eff-color-ctl-err-border:__COLORCONTROLERRORBORDER__;--eff-color-ctl-err-border-shadow:__COLORCONTROLERRORBORDERSHADOW__;--eff-color-ctl-err-ind:__COLORCONTROLERRORINDICATOR__;--eff-color-ctl-focus-border:__COLORCONTROLFOCUSBORDER__;--eff-color-ctl-focus-border-shadow:__COLORCONTROLFOCUSBORDERSHADOW__;--eff-font-ctl:__FONTCONTROL__}@keyframes eff-waiting-bg{from{background-color:var(--eff-color-waiting-bg)}to{background-color:var(--eff-color-waiting-bg-light)}}@keyframes eff-open{0%{display:none;opacity:0}1%{display:block;opacity:0}100%{opacity:1}}::placeholder{color:var(--eff-color-ctl-place)}input,textarea{font-family:var(--eff-font-ctl)}.loader,.loader:before,.loader:after{background:var(--eff-color-loading);animation:eff-loader 1s infinite ease-in-out;width:1em;height:4em;border-radius:1em}.loader{animation-delay:0.16s;color:var(--eff-color-loading);text-indent:-9999em;margin:2em;position:relative;transform:translateZ(0)}.loader:before,.loader:after{position:absolute;top:0;content:''}.loader:before{animation-delay:0.32s;left:-1.5em}.loader:after{left:1.5em}@keyframes eff-loader{0%,80%,100%{box-shadow:0 0;height:4em}40%{box-shadow:0 -2em;height:5em}}.fade{animation:eff-fade 0.2s ease-in}@keyframes eff-fade{0%{opacity:0}100%{opacity:1}}";

    /**
     * Here we test a large CSS with quite a few declarations. This is more to
     * detect and subtle changes.
     */
    @Test
    public void testCssGenerationLarge() throws Exception {
        String file1 = load ("Test006.css");
        CssProcessor processor = new CssProcessor ().combine(Combine.MERGE).load (file1);
        Assertions.assertEquals (T006, processor.export());
    }
    
    /**
     * Here we test the inclusion of a not.
     */
    @Test
    public void testCssGenerationNot() throws Exception {
        String file1 = load ("Test007.css");
        CssProcessor processor = new CssProcessor ().combine (Combine.MERGE).load (file1);

        processor.remap(v ->  "prefix-" + v);
        Assertions.assertEquals (".prefix-style1:not(.prefix-style2){background:#ccc}", processor.export());
    }

    private static String T031 = ".cpt_survey .outer{display:flex;flex-direction:row}@media screen and (max-width:775px){.cpt_survey .outer{flex-direction:column}}";

    private static String T031_MAPPED = ".prefix-cpt_survey .prefix-outer{display:flex;flex-direction:row}@media screen and (max-width:775px){.prefix-cpt_survey .prefix-outer{flex-direction:column}}";

    /**
     * Test a CSS with one selector then an @media variation on that selector.
     */
    @Test
    public void testCssGenerationMedia01() throws Exception {
        String file1 = load ("Test031.css");
        CssProcessor processor = new CssProcessor ().combine (Combine.MERGE).load (file1);
        Assertions.assertEquals (T031, processor.export());

        processor.remap (v ->  "prefix-" + v);
        Assertions.assertEquals (T031_MAPPED, processor.export ());
        Assertions.assertEquals ("prefix-cpt_survey", processor.mappedSelector ("cpt_survey"));
        Assertions.assertEquals ("prefix-outer", processor.mappedSelector ("outer"));
    }

    private static String T032 = ".cpt_survey .outer{display:flex;flex-direction:row}@media screen and (max-width:775px){.cpt_survey .outer{flex-direction:column}.hubba{flex-flow:column}}";

    private static String T032_MAPPED = ".prefix-cpt_survey .prefix-outer{display:flex;flex-direction:row}@media screen and (max-width:775px){.prefix-cpt_survey .prefix-outer{flex-direction:column}.prefix-hubba{flex-flow:column}}";


    /**
     * Test a CSS with one selector then an @media variation on that selector but
     * include a brand new selector (not a usual case but demonstrates that
     * selectors are properly processed).
     */
    @Test
    public void testCssGenerationMedia02() throws Exception {
        String file1 = load ("Test032.css");
        CssProcessor processor = new CssProcessor ().combine (Combine.MERGE).load (file1);
        Assertions.assertEquals (T032, processor.export());

        processor.remap (v ->  "prefix-" + v);
        Assertions.assertEquals (T032_MAPPED, processor.export ());
        Assertions.assertEquals ("prefix-cpt_survey", processor.mappedSelector ("cpt_survey"));
        Assertions.assertEquals ("prefix-outer", processor.mappedSelector ("outer"));
    }
    

    protected String load(String name) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader (new InputStreamReader (CSSTest.class.getResourceAsStream (name), "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null)  {
                sb.append(line).append("\n");
            }
            return sb.toString ();
        }
    }
}
