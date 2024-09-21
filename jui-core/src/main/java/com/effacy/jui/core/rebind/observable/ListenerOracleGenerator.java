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
package com.effacy.jui.core.rebind.observable;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.effacy.jui.core.client.observable.IListener;
import com.effacy.jui.core.client.observable.ListenerOracle;
import com.effacy.jui.core.client.observable.ListenerOracle.IListenerFactory;
import com.effacy.jui.core.rebind.GeneratorUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

/**
 * Generator for creating instances (or extends of classes that implement)
 * {@link IListener}.
 * 
 * @author Jeremy Buckley
 */
public class ListenerOracleGenerator extends Generator {

    /**
     * {@inheritDoc}
     * 
     * @see com.google.gwt.core.ext.Generator#generate(com.google.gwt.core.ext.TreeLogger,
     *      com.google.gwt.core.ext.GeneratorContext, java.lang.String)
     */
    @Override
    public String generate(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {
        // Get the type oracle.
        TypeOracle typeOracle = context.getTypeOracle ();

        // Resolve the type being generated and create the target implementation
        // package and class name.
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
            // Create the composer for the new class.
            ClassSourceFileComposerFactory composerFactory = new ClassSourceFileComposerFactory (packageName, className);
            if (type.isInterface () != null)
                composerFactory.addImplementedInterface (typeName);
            else
                composerFactory.setSuperclass (typeName);

            // Contribute the imports to the new class.
            contributeImports (composerFactory);

            // Create the source writer for the new class.
            SourceWriter sw = composerFactory.createSourceWriter (context, printWriter);

            // Write the source body.
            writeSource (logger, context, sw, type, className);

            // Commit the changes.
            sw.commit (logger);
        }

        // Return the fully qualified class name.
        return packageName + "." + className;
    }


    /**
     * Writes the source (content) of the class.
     * 
     * @param logger
     *            tree logger.
     * @param typeOracle
     *            the type oracle.
     * @param sw
     *            the source writer.
     * @param type
     *            the type being generated for.
     * @param enclosingClassName
     *            the name of the class being created.
     * @throws UnableToCompleteException
     *             On error.
     */
    protected void writeSource(TreeLogger logger, GeneratorContext context, SourceWriter sw, JClassType type, String className) throws UnableToCompleteException {
        List<ListenerFactory> factories = writeListenerFactories (logger, context.getTypeOracle (), sw);

        sw.println ("public " + className + "() {");
        sw.indent ();
        for (ListenerFactory factory : factories)
            sw.println ("register (new " + factory.factoryClassName + " (), " + factory.listenerClassName + ".class);");
        sw.outdent ();
        sw.println ("}");
    }


    /**
     * Generates all the listener factories.
     * 
     * @param logger
     *            tree logger.
     * @param oracle
     *            type oracle.
     * @param sw
     *            source writer.
     * @return The generated factories.
     */
    protected List<ListenerFactory> writeListenerFactories(TreeLogger logger, TypeOracle oracle, SourceWriter sw) {
        List<ListenerFactory> factories = new ArrayList<ListenerFactory> ();

        JClassType listenerType = oracle.findType (IListener.class.getName ());
        for (JClassType type : oracle.getTypes ()) {
            if ((type.isInterface () != null) && type.isAssignableTo (listenerType) && !type.equals (listenerType))
                factories.add (writeListenerFactory (logger, sw, type));
        }

        return factories;
    }


    /**
     * Writes a single listener factory for the given listener type.
     * 
     * @param logger
     *            tree logger.
     * @param sw
     *            source writer.
     * @param type
     *            the listener type.
     * @return The factory information.
     */
    protected ListenerFactory writeListenerFactory(TreeLogger logger, SourceWriter sw, JClassType type) {
        ListenerFactory factory = new ListenerFactory ();
        factory.listenerClassName = type.getQualifiedSourceName ().replace ('$', '.');
        factory.factoryClassName = factory.listenerClassName.replace ('.', '_') + "_Factory";

        sw.println ("public class " + factory.factoryClassName + " implements IListenerFactory<" + type.getQualifiedSourceName ().replace ('$', '.') + "> {");
        sw.indent ();
        sw.println ("public " + factory.listenerClassName + " createDispatcher (Collection<IListener> listenerList, final String debugString) {");
        sw.indent ();
        sw.println ("final List<" + factory.listenerClassName + "> listeners = new ArrayList<" + factory.listenerClassName + "> ();");
        sw.println ("if (listenerList != null) {");
        sw.indent ();
        sw.println ("for (IListener listener : listenerList) {");
        sw.indent ();
        sw.println ("if (listener instanceof " + factory.listenerClassName + ")");
        sw.indent ();
        sw.println ("listeners.add ((" + factory.listenerClassName + ") listener);");
        sw.outdent ();
        sw.outdent ();
        sw.println ("}");
        sw.outdent ();
        sw.println ("}");
        sw.println ("return new " + factory.listenerClassName + " () {");
        sw.indent ();
        for (JMethod method : GeneratorUtils.getMethods (type, JPrimitiveType.VOID, JPrimitiveType.BOOLEAN)) {
            boolean returns = method.getReturnType ().equals (JPrimitiveType.BOOLEAN);
            if (returns)
                sw.println ("public boolean " + method.getName () + "(" + GeneratorUtils.methodParams (method, true, false) + ") {");
            else
                sw.println ("public void " + method.getName () + "(" + GeneratorUtils.methodParams (method, true, false) + ") {");
            sw.indent ();
            sw.println ("if (debugString != null)");
            sw.indent ();
            sw.println ("GWT.log (\"Event::\" + debugString + \"::" + method.getName () + "\");");
            sw.outdent ();
            sw.println ("for (" + factory.listenerClassName + " listener : listeners) {");
            sw.indent ();
            sw.println ("com.effacy.jui.core.client.util.MetricsTimer.stamp(\"EventDispatcher\", \"EventDispatcher(" + factory.listenerClassName + "." + method.getName () + ")\");");
            sw.println ("try {");
            sw.indent ();
            if (returns) {
                sw.println ("if (listener." + method.getName () + " (" + GeneratorUtils.methodParams (method, false, false) + "))");
                sw.indent ();
                sw.println ("return true;");
                sw.outdent ();
            } else {
                sw.println ("listener." + method.getName () + " (" + GeneratorUtils.methodParams (method, false, false) + ");");
            }
            sw.outdent ();
            sw.println ("} catch (Throwable e) {");
            sw.indent ();
            sw.println ("GWT.reportUncaughtException (e);");
            sw.outdent ();
            sw.println ("}");
            sw.outdent ();
            sw.println ("}");
            if (returns)
                sw.println ("return false;");
            sw.outdent ();
            sw.println ("}");

        }
        sw.outdent ();
        sw.println ("};");
        sw.outdent ();
        sw.println ("}");
        sw.outdent ();
        sw.println ("}");

        return factory;
    }


    /**
     * Contribute the imports.
     * 
     * @param composerFactory
     *            the composer factory for the sub-class.
     * @param serializableTypes
     *            the types that we support serialization for.
     */
    protected void contributeImports(ClassSourceFileComposerFactory composerFactory) {
        // Add the java imports.
        composerFactory.addImport (java.lang.String.class.getName ());
        composerFactory.addImport (java.util.Collection.class.getName ());
        composerFactory.addImport (java.util.List.class.getName ());
        composerFactory.addImport (java.util.ArrayList.class.getName ());
        composerFactory.addImport (java.util.Map.class.getName ());
        composerFactory.addImport (java.util.HashMap.class.getName ());

        // Add the GWT imports.
        composerFactory.addImport (GWT.class.getName ());
        composerFactory.addImport (ListenerOracle.class.getName ());
        composerFactory.addImport (IListenerFactory.class.getName ().replace ('$', '.'));
        composerFactory.addImport (Throwable.class.getName ());
    }

    public class ListenerFactory {

        public String factoryClassName;

        public String listenerClassName;
    }

}
