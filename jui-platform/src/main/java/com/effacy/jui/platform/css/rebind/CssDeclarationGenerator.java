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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.css.client.CssResource.Font;
import com.effacy.jui.platform.css.client.CssResource.Fonts;
import com.effacy.jui.platform.css.client.CssResource.UseStyle;
import com.effacy.jui.platform.css.rebind.parser.ExpressionParser.ExpressionParserException;
import com.effacy.jui.platform.rebind.ResourceSupport;
import com.effacy.jui.platform.rebind.ResourceSupport.IResource;
import com.google.gwt.core.ext.ConfigurationProperty;
import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.dev.util.Util;
import com.google.gwt.thirdparty.guava.common.io.BaseEncoding;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

public class CssDeclarationGenerator extends Generator {

    private JClassType stringType;

    private static long COUNT = 1;

    /**
     * Property declared in Platform.gwt.xml to determine the nature of the
     * CSS obfuscation. Uses "stable" for stable names (unique even when partially
     * process, as is needed when incrementally compiling) and "obf" for obfuscated
     * (this is the default and the value deferred to when the actualy value is not
     * determined).
     */
    private static final String KEY_STYLE = "CssDeclaration.style";

    /**
     * As with {@link #KEY_STYLE} this is the version as used by the GWT code
     * server, which is included for backward compatibility.
     */
    private static final String KEY_STYLE_GWT = "CssResource.style";

    /**
     * {@inheritDoc}
     *
     * @see com.google.gwt.core.ext.Generator#generate(com.google.gwt.core.ext.TreeLogger,
     *      com.google.gwt.core.ext.GeneratorContext, java.lang.String)
     */
    @Override
    public String generate(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {
        logger.log (TreeLogger.INFO, "Generating " + typeName);
        try {
            // Determine if we are generating nice names. This is specified in the
            // "CssDeclaration.style" as being "stable". This is set in the recompiler of
            // the code server to generate nice names.
            ConfigurationProperty styleProp = context.getPropertyOracle ().getConfigurationProperty (KEY_STYLE);
            boolean stableCssNames = "stable".equals (styleProp.getValues ().get (0));
            if (!stableCssNames) {
                // This is for backward compatibility with GWT and its code server.
                styleProp = context.getPropertyOracle ().getConfigurationProperty (KEY_STYLE_GWT);
                stableCssNames = "stable".equals (styleProp.getValues ().get (0));
            }

            // Get the type oracle.
            TypeOracle typeOracle = context.getTypeOracle ();
            stringType = typeOracle.getType (String.class.getName ());

            // Resolve the type being generated and create the target
            // implementation package and class name.
            JClassType type = typeOracle.findType (typeName);
            if (type == null) {
                logger.log (TreeLogger.ERROR, "Unable to find metadata for type '" + typeName + "'", null);
                throw new UnableToCompleteException ();
            }
            String packageName = type.getPackage ().getName ();
            String className = type.getQualifiedSourceName ().replace ('.', '_') + "_Impl";

            // Create the class source code.
            PrintWriter printWriter = context.tryCreate (logger, packageName, className);
            if (printWriter != null) {
                ClassSourceFileComposerFactory composerFactory = new ClassSourceFileComposerFactory (packageName, className);
                if (type.isInterface () != null) {
                    composerFactory.addImplementedInterface (typeName);
                } else {
                    composerFactory.setSuperclass (typeName);
                }
                SourceWriter sw = composerFactory.createSourceWriter (context, printWriter);

                // Get the resources and determine if we are being strict (so that every declared style must have a
                // corresponding CSS entry).
                List<String> resources = new ArrayList<> ();
                CssResource resourceDeclaration = type.getAnnotation (CssResource.class);
                boolean strict = true;
                boolean generateCssDecarations = false;
                if (resourceDeclaration != null) {
                    for (String resource : resourceDeclaration.value())
                        resources.add (resource);
                    strict = resourceDeclaration.strict ();
                    generateCssDecarations = resourceDeclaration.generateCssDecarations ();
                }

                CssProcessor processor = new CssProcessor ();
                for (IResource resource : ResourceSupport.findResources (logger, context, type.getPackage (), resources, false))
                    processor.load (resource.getResourceAsString ());
                Map<String,Map<String,String>> declarations = generateCssDecarations ? processor.declarations () : null;

                // Here we loop over each method and determine if it matches the profile for a
                // style class declaration. If it does we place a reference in the
                // styleNameToMethod map (since the name of the method matches a CSS style). If
                // the method is annotated with @UseStyle then we are directly associated that
                // method with a differently named style and that should not be subject to
                // obfuscation. In that case we add the style to a set of unobfuscated styles
                // and we associated a map that maps the method name to that stlye (so it can be
                // returned).
                Set<String> unobfuscatedClassNames = new HashSet<>();
                Map<String,JMethod> styleNameToMethod = new HashMap<>();
                Map<String,String> methodToStyleName = new HashMap<>();
                for (JMethod method : abstractMethodsFor (type)) {
                    String name = method.getName ();
                    styleNameToMethod.put (name, method);
                    UseStyle useStyleAn = method.getAnnotation (UseStyle.class);
                    if ((useStyleAn != null) && !useStyleAn.value().isBlank ()) {
                        unobfuscatedClassNames.add (name);
                        methodToStyleName.put (name, useStyleAn.value ().trim ());
                    }
                }

                // Remap the selectors. A selector will be re-mapped only if it matches a
                // method and it does not appear in the list of unobfuscated styles.
                if (!stableCssNames) {
                    processor.remap (v -> {
                        if (unobfuscatedClassNames.contains (v))
                            return v;
                        if (styleNameToMethod.containsKey (v))
                            return "JUI" +  String.format ("%06d", COUNT++);
                        return v;
                    });
                } else {
                    String prefix = "JUI-" + type.getQualifiedSourceName ().replaceAll ("[.$]", "-") + "-";
                    processor.remap (v -> {
                        if (unobfuscatedClassNames.contains (v))
                            return v;
                        if (styleNameToMethod.containsKey (v))
                            return prefix + v;
                        return v;
                    });
                }
                
                // Generate replacement methods.
                List<String> missingClasses = new ArrayList<>();
                for (String name : styleNameToMethod.keySet()) {
                    JMethod method = styleNameToMethod.get (name);
                    String styleNameToUse;
                    if (methodToStyleName.containsKey (name)) {
                        // Here we have used @UseStyle to map the method to a specific (unobfuscated)
                        // style.
                        styleNameToUse = methodToStyleName.get (name);
                    } else {
                        // Here the method name matches a style so we need to obtain the obfuscated
                        // version of that style.
                        styleNameToUse = processor.mappedSelector (name);
                        if (strict && styleNameToUse.equals (name))
                            missingClasses.add (name);
                    }
                    sw.println ("public java.lang.String " + method.getName() + "() {");
                    sw.indent ();
                    sw.println ("return \"" + styleNameToUse + "\";");
                    sw.outdent ();
                    sw.println ("}");
                }

                // If there are any missing classes, then fail.
                if (!missingClasses.isEmpty()) {
                    TreeLogger sub = logger.branch (TreeLogger.ERROR, "Problem generating '" + typeName + "'");
                    missingClasses.forEach (name -> {
                        sub.log (TreeLogger.ERROR, "Declared class '" + name + "' does not appear in the CSS");
                    });                           
                    throw new UnableToCompleteException ();
                }

                // Process any fonts that have been declared.
                List<Font> fontsToProcess = new ArrayList<>();
                if (type.getAnnotation (Font.class) != null)
                    fontsToProcess.add (type.getAnnotation (Font.class));
                if (type.getAnnotation (Fonts.class) != null) {
                    for (Font font : type.getAnnotation (Fonts.class).value()) {
                        if (font != null)
                            fontsToProcess.add (font);
                    }
                }

                // Generate the CSS output.
                sw.println ("public String getCssText() {");
                sw.indent ();
                sw.print ("return ");
                LOOP: for (Font font : fontsToProcess) {
                    IResource[] resolved = null;

                    // We need to do some pre-checking here where fonts are optional.
                    if (font.optional ()) {
                        if (font.sources ().length == 0)
                            continue LOOP;
                        if (!font.useModuleBase ()) {
                            resolved = ResourceSupport.findResources (logger, context, type.getPackage (), font.sources (), true);
                            if (resolved.length == 0)
                                continue LOOP;
                        }
                    }

                    // Generate the font entry.
                    sw.print ("\"@font-face{");
                    sw.print ("font-family:'");
                    sw.print (font.name ());
                    sw.print ("';");
                    sw.print ("font-style:" + font.style () + ";");
                    sw.print ("font-weight:" + font.weight () + ";");
                    sw.print ("font-display:block;");
                    if (font.useModuleBase ()) {
                        for (String source : font.sources()) {
                            String formatType = null;
                            if (source.endsWith (".ttf"))
                                formatType = "truetype";
                            else if (source.endsWith (".otf"))
                                formatType = "embedded-opentype";
                            else if (source.endsWith (".woff"))
                                formatType = "woff";
                            else if (source.endsWith (".woff2"))
                                formatType = "woff2";
                            sw.print ("src:url('\" + ");
                            sw.print ("com.google.gwt.core.client.GWT.getModuleBaseForStaticFiles () + \"" + source);
                            sw.print ("') format('" + formatType + "');");
                        }
                    } else {
                        if (resolved == null)
                            resolved = ResourceSupport.findResources (logger, context, type.getPackage (), font.sources(), font.optional ());
                        for (IResource resource : resolved) {
                            URL url = resource.getResourceAsUrl ();
                            String lower = url.getPath ().toLowerCase ();
                            if (lower.endsWith (".ttf")) {
                                String outputUrlExpression = deploy (logger, context, url, "application/x-font-ttf", font.noinline ());
                                sw.print ("src:url('\" + ");
                                sw.print (outputUrlExpression);
                                sw.print (" + \"') format('truetype');");
                            } else if (lower.endsWith (".otf")) {
                                String outputUrlExpression = deploy (logger, context, url, "application/x-font-ttf", font.noinline ());
                                sw.print ("src:url('\" + ");
                                sw.print (outputUrlExpression);
                                sw.print (" + \"?#iefix') format('embedded-opentype');");
                            } else if (lower.endsWith (".woff")) {
                                String outputUrlExpression = deploy (logger, context, url, "application/octet-stream", font.noinline ());
                                sw.print ("src:url('\" + ");
                                sw.print (outputUrlExpression);
                                sw.print (" + \"') format('woff');");
                            } else if (lower.endsWith (".woff2")) {
                                String outputUrlExpression = deploy (logger, context, url, "application/octet-stream", font.noinline ());
                                sw.print ("src:url('\" + ");
                                sw.print (outputUrlExpression);
                                sw.print (" + \"') format('woff2');");
                            }
                        }
                    }
                    sw.println ("}\" + //");
                }
                sw.print (processor.exportAsString ());
                processor.substitutions((ref,exp) -> {
                    sw.print (".replace (\"" + ref + "\"," + exp + ")");
                });
                sw.println (";");
                sw.outdent ();
                sw.println ("}");

                sw.println ("private boolean injected;");
                sw.println ("public boolean ensureInjected() {");
                sw.indent ();
                sw.println("if (!injected) {");
                sw.indent ();
                sw.println ("injected = true;");
                sw.println ("com.effacy.jui.platform.css.client.CssInjector.inject (getCssText ());");
                sw.println ("return true;");
                sw.outdent ();
                sw.println("}");
                sw.println ("return false;");
                sw.outdent ();
                sw.println ("}");

                if (declarations != null) {
                    sw.println ("public java.util.Map<String,java.util.Map<String,String>> getCssDeclarations() {");
                    sw.indent ();
                    sw.println ("java.util.Map<java.lang.String,java.util.Map<java.lang.String,java.lang.String>> results = new java.util.HashMap<>();");
                    sw.println ("java.util.Map<java.lang.String,java.lang.String> declarations;");
                    declarations.entrySet ().forEach (selector -> {
                        sw.println ("declarations = new java.util.HashMap<>();");
                        selector.getValue ().entrySet ().forEach (e -> {
                            sw.println ("declarations.put (\"" + e.getKey ().replace("\"", "\\\"") + "\",\"" +  e.getValue ().replace("\"", "\\\"") + "\");");
                        });
                        sw.println ("results.put (\"" + selector.getKey ().replace("\"", "\\\"") + "\",declarations);");
                    });
                    sw.println ("return results;");
                    sw.outdent ();
                    sw.println ("}");
                }
                
                // Commit the changes.
                sw.commit (logger);
            }

            // Return the fully qualified class name.
            return packageName + "." + className;
        } catch (UnableToCompleteException e) {
            throw e;
        } catch (ExpressionParserException e) {
            logger.branch (TreeLogger.ERROR, "Problem generating '" + typeName + "'") //
                .log (TreeLogger.ERROR, "Unable to parse line: " + e.getMessage());
            throw new UnableToCompleteException ();
        } catch (Throwable e) {
            logger.log (TreeLogger.ERROR, "Problem generating '" + typeName + "'", e);
            throw new UnableToCompleteException ();
        }
    }

    /**
     * Extracts those method that sensibly represent a CSS style (no argument and
     * returns a {@link String}). Excluded is `String getCssText()`.
     * 
     * @param type
     *             the class type to extract methods from.
     * @return a list of matching style methods.
     */
    protected List<JMethod> abstractMethodsFor(JClassType type) {
        List<JMethod> methods = new ArrayList<>();
        for (JClassType klass : type.getFlattenedSupertypeHierarchy ()) {
            for (JMethod method : klass.getMethods()) {
                if ("getCssText".equals (method.getName ()))
                    continue;
                if (!method.isAbstract ())
                    continue;
                if (!method.getReturnType().equals(stringType) || (method.getParameters().length != 0))
                    continue;
                methods.add (method);
            }
        }
        return methods;
    }

    protected static final int MAX_INLINE_SIZE = 2 << 15;

    public static final int MAX_ENCODED_SIZE = (2 << 15) - 1;
    

    /**
     * Obtains a referential form of the resource. The default is to inline the
     * resource if it is small enough (see {@link #MAX_INLINE_SIZE} and
     * {@link #MAX_ENCODED_SIZE}) otherwise it is referenced as a URL (which means
     * that a suitable artefact will be created and stashed for deployment as a
     * compilation artefact).
     * 
     * @param logger
     *                   the context logger.
     * @param context
     *                   the generator context.
     * @param resource
     *                   the resource being requested.
     * @param mimeType
     *                   the mime type to use for the resource.
     * @param noInlining
     *                   {@code true} to force reference as a resource and not to
     *                   inline.
     * @return the associated reference for inclusion in the CSS.
     * @throws UnableToCompleteException
     *                                   on error.
     */
    protected String deploy(TreeLogger logger, GeneratorContext context, URL resource, String mimeType, boolean noInlining) throws UnableToCompleteException {
        // Obtain the filename.
        String path = resource.getPath ();
        String fileName = path.substring (path.lastIndexOf ('/') + 1);

        // Extract the data.
        byte[] data = Util.readURLAsBytes (resource);

        // Determine a mime type if one has not been passed.
        String finalMimeType;
        try {
            finalMimeType = (mimeType != null) ? mimeType : resource.openConnection ().getContentType ();
        } catch (IOException e) {
            logger.log (TreeLogger.ERROR, "Unable to determine mime type of resource", e);
            throw new UnableToCompleteException();
        }

        // First attempt to inline, bit only when the content is small enough.
        if (!noInlining && (data.length < MAX_INLINE_SIZE)) {
            String base64Contents = BaseEncoding.base64 ().encode (data).replaceAll ("\\s+", "");
            String encoded = "\"data:" + finalMimeType.replaceAll ("\"", "\\\\\"") + ";base64," + base64Contents + "\"";
            if (encoded.length () < MAX_ENCODED_SIZE)
                return encoded;
        }

        // We will use a hash for the name (the hash is generated from the content).
        // This will have the original extension applied and between the hashed name and
        // the extension will contain ".cache." to make it clear that this is a
        // generated resource.
        String strongName = Util.computeStrongName (data);
        int idx = fileName.lastIndexOf ('.');
        String extension = (idx > 0) ? fileName.substring (idx + 1) : "noext";
        String outputName = strongName + ".cache." + extension;

        // Create a stream to write the output to. However, this will be null if the
        // resource has already been created. In this instance it is most likely that
        // the content has not changed and we don't need to write this out.
        OutputStream out = context.tryCreateResource (logger, outputName);

        // This would be null if the resource has already been created in the
        // output (because two or more resources had identical content).
        if (out != null) {
            try {
                out.write (data);
                context.commitResource (logger, out);
            } catch (IOException e) {
                logger.log (TreeLogger.ERROR, "Unable to write data to output name " + outputName, e);
                throw new UnableToCompleteException();
            }
        }

        // Return an embeddable expression.
        return "com.google.gwt.core.client.GWT.getModuleBaseForStaticFiles () + \"" + outputName + "\"";
    }
}
