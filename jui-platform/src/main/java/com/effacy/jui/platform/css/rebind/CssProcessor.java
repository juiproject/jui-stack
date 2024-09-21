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
package com.effacy.jui.platform.css.rebind;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.css.rebind.parser.ExpressionParser;
import com.effacy.jui.platform.css.rebind.parser.ExpressionParser.ExpressionParserException;
import com.effacy.jui.platform.css.rebind.parser.ExpressionParser.ParsedExpression;
import com.helger.commons.system.ENewLineMode;
import com.helger.css.ECSSVersion;
import com.helger.css.decl.AbstractHasTopLevelRules;
import com.helger.css.decl.CSSExpression;
import com.helger.css.decl.CSSSelector;
import com.helger.css.decl.CSSSelectorMemberNot;
import com.helger.css.decl.CSSSelectorSimpleMember;
import com.helger.css.decl.CSSStyleRule;
import com.helger.css.decl.CascadingStyleSheet;
import com.helger.css.decl.ICSSSelectorMember;
import com.helger.css.reader.CSSReader;
import com.helger.css.writer.CSSWriter;
import com.helger.css.writer.CSSWriterSettings;

public class CssProcessor {

    private CssResource.Combine combine = CssResource.Combine.APPEND;
    
    private CascadingStyleSheet css;

    private Map<String,String> cache = new HashMap<>();

    private Map<String,String> substitutions = new HashMap<>();

    public CssProcessor clear() {
        css = null;
        cache.clear ();
        substitutions.clear ();
        return this;
    }

    public void substitutions(BiConsumer<String,String> visitor) {
        substitutions.forEach (visitor);
    }

    public Map<String,String> substitutions() {
        return substitutions;
    }

    public CssProcessor combine(CssResource.Combine combine) {
        if (combine != null)
            this.combine = combine;
        return this;
    }

    public CssProcessor load(String content) throws IOException, ExpressionParserException {
        cache = new HashMap<>();
        if (content != null) {
            if (css == null) {
                css = build (content);
            } else if (CssResource.Combine.REPLACE == combine) {
                CascadingStyleSheet appendCss = build (content);
                if (appendCss != null) {
                    appendCss.getAllStyleRules().forEach (rule -> {
                        if (rule == null)
                            return;
                        boolean found = false;
                        int idx = 0;
                        for (CSSStyleRule brule : css.getAllStyleRules()) {
                            if (matchBySelector(rule, brule)) {
                                found = true;
                                css.removeRule (idx);
                                css.addRule (idx, rule);
                                break;
                            }
                            idx++;
                        }
                        if (!found)
                            css.addRule (rule);
                    });
                }
            } else if (CssResource.Combine.MERGE == combine) {
                CascadingStyleSheet appendCss = build (content);
                if (appendCss != null) {
                    appendCss.getAllStyleRules().forEach (rule -> {
                        if (rule == null)
                            return;
                        boolean found = false;
                        for (CSSStyleRule brule : css.getAllStyleRules()) {
                            if (matchBySelector(rule, brule)) {
                                found = true;
                                rule.getAllDeclarations().forEach (decl -> {
                                    // At this stage we append the rule rather than remove any prior matching rule.
                                    // This should be OK as CSS takes the last declaration for a given property
                                    // rather than any prior (and comfortably takes care of the case of consolidated
                                    // properties such as margin).
                                    if (decl != null)
                                        brule.addDeclaration (decl);
                                });
                                break;
                            }
                        }
                        if (!found)
                            css.addRule (rule);
                    });
                }
            } else {
                CascadingStyleSheet appendCss = build (content);
                if (appendCss != null) {
                    appendCss.getAllRules().forEach (rule -> {
                        if (rule != null)
                            css.addRule (rule);
                    });
                }
            }
        }
        return this;
    }

    @SuppressWarnings("null")
	protected CascadingStyleSheet build(String content) throws IOException, ExpressionParserException {
        if (content == null)
            content = "";

        // We need to extract any definitions. These must reside entirely on one line and
        // encompass the entire line (after trimming). They will be removed prior to
        // processing the CSS content which is pre-processed by replacing evaluation
        // references with their substitutions.
        Map<String,String> localSubstitutions = new HashMap<>();
        if (content.contains ("@def") || content.contains ("@eval")) {
            StringBuilder sb = new StringBuilder ();
            try (BufferedReader br = new BufferedReader (new StringReader (content))) {
                int lineNumber = 1;
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.trim ().startsWith ("@def") || line.trim().startsWith ("@eval")) {
                        try {
                            ParsedExpression exp = ExpressionParser.parse (line);
                            if (exp != null) {
                                // All references are placed in lower case with their respect substitution in upper case (to avoid name pollution).
                                localSubstitutions.put (exp.reference (), exp.expression ());
                            }
                        } catch (ExpressionParserException e) {
                            // We need to replace the line number.
                            String mesg = e.getMessage();
                            String updatedMessage = null;
                            int i = mesg.indexOf("line ");
                            if (i > 0) {
                                int j = mesg.indexOf(",", i);
                                if (j > i) {
                                    updatedMessage = mesg.substring(0, i + 5);
                                    updatedMessage += lineNumber;
                                    updatedMessage += mesg.substring (j);
                                }
                            }
                            if (updatedMessage == null)
                                updatedMessage = mesg;
                            updatedMessage = updatedMessage + " actual line: \"" + line + "\"";  
                            throw new ExpressionParserException (updatedMessage);
                        }
                    } else {
                        sb.append (line).append ("\n");
                    }
                    lineNumber++;
                }
            }
            content = sb.toString ();
        }

        // Parse the CSS itself now that the definitions have been removed. If there are
        // substitutions we need to update the CSS accordingly.
		CascadingStyleSheet css = CSSReader.readFromString (content, StandardCharsets.UTF_8, ECSSVersion.CSS30);
        if (!localSubstitutions.isEmpty ()) {
            Set<String> substitutionsFound = new HashSet<>();
            css.getAllStyleRules ().forEach (rule -> {
                rule.getAllDeclarations().forEach (decl -> {
                    CSSExpression exp = decl.getExpression ();
                    Map<Integer,String> replacements = new HashMap<>();
                    exp.getAllSimpleMembers ().forEach (member -> {
                        String value = member.getValue ();
                        Integer idx = null;
                        for (String reference : localSubstitutions.keySet ()) {
                            if (contains (value, reference)) {
                                if (!substitutionsFound.contains (reference))
                                    substitutionsFound.add (reference);
                                if (idx == null)
                                    idx = exp.getAllMembers ().indexOf (member);
                                value = value.replace (reference, "__" + reference.toUpperCase () + "__");
                            }
                        }
                        if (idx != null) {
                            // We have at least one substitution.
                            replacements.put (idx, value);
                        }
                    });
                    replacements.forEach ((k,v) -> {
                        exp.removeMember (k);
                        exp.addTermSimple (k, v);
                    });
                });
            });

            // Update the substitutions to match.
            substitutionsFound.forEach (ref -> {
                substitutions.put ("__" + ref.toUpperCase () + "__", localSubstitutions.get (ref));
            });
        }

        return css;
    }

    private boolean contains(String target, String reference) {
        if (target == null)
            return false;
        for (String part : target.split(" ")) {
            part = part.trim();
            if (part.equals(reference))
                return true;
        }
        return false;
    }

    private boolean matchBySelector(CSSStyleRule rule1, CSSStyleRule rule2) {
        Set<String> s1 = new HashSet<>();
        rule1.getAllSelectors().forEach(selector -> {
            s1.add (selector.getAsCSSString());
        });
        Set<String> s2 = new HashSet<>();
        rule2.getAllSelectors().forEach(selector -> {
            s2.add (selector.getAsCSSString());
        });
        if (s1.size() != s2.size())
            return false;
        for (String selector : s1) {
            if (!s2.contains (selector))
                return false;
        }
        return true;
    }

    /**
     * Given an original selector (without the leading period) returned the mapped
     * version.
     * <p>
     * Note that the mappings are performed by a call to {@link #remap(Function)}.
     * 
     * @param original
     *                 the original selector.
     * @return the mapped selector.
     */
    public String mappedSelector(String original) {
        if (original == null)
            return original;
        String mapped = cache.get (original);
        return (mapped == null) ? original : mapped;
    }

    /**
     * Remaps (that is, renames) selectors.
     * 
     * @param selectorMap
     *                    the function that maps a given selector to the renamed
     *                    version of that selector (this should be consistent in
     *                    that being applied to the same selector should result in
     *                    the same mapping of that selector).
     * @return this processor.
     */
    public CssProcessor remap(Function<String,String> selectorMap) {
        if (selectorMap == null)
            return this;
        _remap (css, selectorMap);
        return this;
    }

    /**
     * Called by {@link #remap(Function)}. This is recursive through nested rule
     * sets (for example, it will process media and supports rules that contain
     * style rules).
     * 
     * @param tlr
     *                    the top-level rules to process.
     * @param selectorMap
     *                    the selector map.
     */
    protected void _remap(AbstractHasTopLevelRules tlr, Function<String,String> selectorMap) {
        tlr.getAllStyleRules ().forEach (rule -> {
            rule.getAllSelectors ().forEach (selector -> {
                process (selector, selectorMap);
            });
        });
        tlr.getAllMediaRules().forEach (rule -> {
            _remap (rule, selectorMap);
        });
        tlr.getAllSupportsRules().forEach (rule -> {
            _remap (rule, selectorMap);
        });
    }

    /**
     * Called by {@link #remap(Function)} to remap members of the passed selector.
     * 
     * @param selector
     *                    the selector to remap selectors for.
     * @param selectorMap
     *                    the selector re-mapper.
     */
    @SuppressWarnings("null")
	protected void process(CSSSelector selector, Function<String,String> selectorMap) {
        List<ICSSSelectorMember> revised = new ArrayList<>();
        selector.getAllMembers ().forEach (member -> {
            // A simple member is one that can be mapped directly.
            if (member instanceof CSSSelectorSimpleMember) {
                String value = member.getAsCSSString ();
                if (value.startsWith (".")) {
                    String strippedValue = value.substring (1);
                    String mapped = cache.get (strippedValue);
                    if (mapped == null) {
                        mapped = selectorMap.apply (strippedValue);
                        if (mapped != null)
                            cache.put (strippedValue, mapped);
                    }
                    if ((mapped != null) && !mapped.equals (value)) {
                        revised.add (new CSSSelectorSimpleMember ("." + mapped));
                        return;
                    }
                }
            }
            // A NOT contains nested selectors that need to be processed separately.
            else if (member instanceof CSSSelectorMemberNot) {
                ((CSSSelectorMemberNot) member).getAllSelectors ().forEach(sel -> {
                    process (sel, selectorMap);
                });
            }
            revised.add (member);
        });
        if (!revised.isEmpty ()) {
            selector.removeAllMembers ();
            revised.forEach (member -> selector.addMember (member));
        }
    }

	public String export() {
        if (css == null)
            return "";
        CSSWriter writer = new CSSWriter (new CSSWriterSettings (ECSSVersion.CSS30, false)
            .setNewLineMode (ENewLineMode.UNIX)
            .setOptimizedOutput (true)
            .setRemoveUnnecessaryCode (true));
        writer.setHeaderText ("");
        return writer.getCSSAsString (css);
    }

    /**
     * Generates a map of declarations.
     * 
     * @return the declarations.
     */
    public Map<String,Map<String,String>> declarations() {
        Map<String,Map<String,String>> results = new HashMap<>();
        css.getAllStyleRules().forEach(rule -> {
            rule.getAllSelectors().forEach(selector -> {
                selector.getAllMembers().forEach(member -> {
                    Map<String,String> declarations = new HashMap<>();
                    results.put (member.getAsCSSString(), declarations);
                    rule.getAllDeclarations().forEach(decl -> {
                        declarations.put(decl.getProperty(), decl.getExpressionAsCSSString());
                    });
                });
            });
        });
        return results;
    }

    public String exportAsString() {
        String out = export ();
        out = out.replace ("\n", "\\n");
        out = out.replace ("\\", "\\\\");
        out = out.replace("\"", "\\\"");
        return "\"" + out + "\"";
    }
}
