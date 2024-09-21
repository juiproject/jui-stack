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
package com.effacy.jui.platform.core.rebind;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.dev.resource.Resource;
import com.google.gwt.dev.util.Util;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

public class JsNativeInjectorGenerator extends Generator {
    public String generate(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {
        logger.log (TreeLogger.INFO, "Generating " + typeName);
        try {
            // Get the type oracle.
            TypeOracle typeOracle = context.getTypeOracle ();

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
                composerFactory.addImplementedInterface (typeName);
                composerFactory.addImport (com.effacy.jui.platform.util.client.ScriptInjector.class.getName ());
                SourceWriter sw = composerFactory.createSourceWriter (context, printWriter);

                sw.println ("public " + className + "() {");
                sw.indent ();

                // For now we only include JUI resources this way. May open this up later.
                byte [] data = new byte [0];
                for (String item : context.getResourcesOracle ().getPathNames ()) {
                    if (item.endsWith (".native.js") && item.startsWith ("com/effacy/jui")) {
                        logger.log (Type.INFO, "Added " + item);
                        Resource resource = context.getResourcesOracle().getResource(item);
                        try {
                            byte [] bytes = Util.readURLAsBytes ( new URL(resource.getLocation()));
                            if (bytes != null) {
                                if (data.length == 0) {
                                    data = bytes;
                                } else {
                                    byte [] composed = new byte [data.length + bytes.length];
                                    System.arraycopy (data, 0, composed, 0, data.length);
                                    System.arraycopy (bytes, 0, composed, data.length, bytes.length);
                                    data = composed;
                                }
                            }
                        } catch (MalformedURLException e) {
                            logger.log (TreeLogger.WARN, "Problem loading '" + item + "', skipping");
                        }
                    }
                }
                if (data.length == 0) {
                    logger.log (Type.INFO, "No resources found");
                } else {
                    String outputName = Util.computeStrongName (data) + ".js";
                    OutputStream out = context.tryCreateResource (logger, outputName);
                    try {
                        out.write (data);
                        context.commitResource (logger, out);
                        sw.println ("ScriptInjector.injectFromModuleBase (\"" + outputName + "\");");
                    } catch (IOException e) {
                        logger.log (TreeLogger.ERROR, "Unable to write data to output name " + outputName, e);
                        throw new UnableToCompleteException();
                    }
                }

                sw.outdent ();
                sw.println ("}");
                
                // Commit the changes.
                sw.commit (logger);
            }

            // Return the fully qualified class name.
            return packageName + "." + className;
        } catch (UnableToCompleteException e) {
            throw e;
        } catch (Throwable e) {
            logger.log (TreeLogger.ERROR, "Problem generating '" + typeName + "'", e);
            throw new UnableToCompleteException ();
        }
    }
}
