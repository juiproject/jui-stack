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
package com.effacy.jui.rpc.rebind;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.gwtproject.timer.client.Timer;

import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.rpc.client.IRemoteMethod;
import com.effacy.jui.rpc.client.IRemoteMethodCallback;
import com.effacy.jui.rpc.client.IRemoteMethodInjectable;
import com.effacy.jui.rpc.client.MethodRegistry;
import com.effacy.jui.rpc.client.RemoteMethod;
import com.effacy.jui.rpc.extdirect.client.annotation.ExcludeMethod;
import com.effacy.jui.rpc.extdirect.client.annotation.ServiceConfiguration;
import com.effacy.jui.rpc.extdirect.client.annotation.ServiceProvider;
import com.effacy.jui.rpc.extdirect.client.service.IService;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameter;
import com.google.gwt.core.ext.typeinfo.JParameterizedType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

/**
 * Takes a service interface which extends {@link IService} and builds an
 * implementation by looking up each method in the {@link MethodRegistry}. It
 * will also scan for classes that implement {@link IServiceProvider} and the
 * service interface and add its methods to the registry.
 * 
 * @author Steve Baker
 */
public class ServiceGenerator extends Generator {

    /**
     * {@inheritDoc}
     * 
     * @see com.google.gwt.core.ext.Generator#generate(com.google.gwt.core.ext.TreeLogger,
     *      com.google.gwt.core.ext.GeneratorContext, java.lang.String)
     */
    @Override
    public String generate(TreeLogger logger, GeneratorContext ctx, String requestedClass) throws UnableToCompleteException {
        TypeOracle typeOracle = ctx.getTypeOracle ();

        // Resolve this class and create a sub-class.
        JClassType serializeClass = typeOracle.findType (requestedClass);
        if (serializeClass == null) {
            logger.log (TreeLogger.ERROR, "Unable to find metadata for type '" + requestedClass + "'", null);
            throw new UnableToCompleteException ();
        }
        String packageName = serializeClass.getPackage ().getName ();
        String className = serializeClass.getSimpleSourceName () + "_Impl";
        PrintWriter printWriter = ctx.tryCreate (logger, packageName, className);
        String qualifiedName = packageName + "." + className;
        if (printWriter == null)
            return qualifiedName;
        // StringWriter sw = new StringWriter ();
        // printWriter = new PrintWriter (sw);

        // Create the composer for the new class.
        ClassSourceFileComposerFactory composerFactory = new ClassSourceFileComposerFactory (packageName, className);
        composerFactory.addImplementedInterface (IService.class.getName ());
        composerFactory.addImplementedInterface (serializeClass.getSimpleSourceName ());

        // Contribute the imports to the new class.
        composerFactory.addImport (MethodRegistry.class.getName ());
        composerFactory.addImport (IRemoteMethod.class.getName ());
        composerFactory.addImport (RemoteMethod.class.getName ());
        composerFactory.addImport (IRemoteMethodCallback.class.getName ());
        composerFactory.addImport (IRemoteMethodInjectable.class.getName ());
        composerFactory.addImport (Timer.class.getName ());
        composerFactory.addImport (List.class.getName ());
        composerFactory.addImport (ArrayList.class.getName ());
        composerFactory.addImport (Logger.class.getName ());
        composerFactory.addImport (GWT.class.getName ());

        // Create the source writer for the new class.
        SourceWriter srcWriter = composerFactory.createSourceWriter (ctx, printWriter);
        if (srcWriter == null)
            return qualifiedName;

        // Contribute the constructor.
        contributeConstructor (typeOracle, srcWriter, className, serializeClass);

        // Contribute the various methods.
        contributeMethods (typeOracle, srcWriter, serializeClass);

        // Write out the class definition and signal GWT to use that.
        srcWriter.commit (logger);
        
        return qualifiedName;
    }


    /**
     * Contribute the constructor for the class.
     * 
     * @param typeOracle
     * @param srcWriter
     * @param serviceClass
     */
    private void contributeConstructor(TypeOracle typeOracle, SourceWriter srcWriter, String className, JClassType serviceClass) {
        srcWriter.println ("  public " + className + "() throws Throwable {");
        srcWriter.println ("    MethodRegistry registry = MethodRegistry.getInstance ();");

        // The name of the service action.
        String actionName = serviceClass.getName ();

        JClassType remoteResponseInterface = typeOracle.findType (IRemoteMethodCallback.class.getName ());
        int i = 0;
        for (JClassType cls : serviceClass.getSubtypes ()) {
            if (cls.getAnnotation (ServiceProvider.class) != null) {
                String name = "service" + (i++);
                srcWriter.println ("    final " + cls.getQualifiedSourceName () + " " + name + " = new " + cls.getQualifiedSourceName () + "();");
                LOOP: for (JMethod method : serviceClass.getMethods ()) {
                    String methodName = method.getName ();
                    JMethod methodImpl = findImplementingMethod (cls, method);
                    if (methodImpl == null)
                        methodImpl = method;
                    int delay = 0;
                    if (methodImpl.getAnnotation (ExcludeMethod.class) != null)
                        continue LOOP;
                    ServiceConfiguration conf = methodImpl.getAnnotation (ServiceConfiguration.class);
                    if (conf != null)
                        delay = conf.delay ();
                    List<String> parameterTypes = new ArrayList<String> ();
                    JClassType returnType = null;
                    for (JParameter parameter : method.getParameters ()) {
                        JType parameterType = parameter.getType ();
                        JParameterizedType callbackType = parameterType.isParameterized ();
                        if ((callbackType != null) && callbackType.isAssignableTo (remoteResponseInterface))
                            returnType = callbackType.getTypeArgs ()[0];
                        else if (parameterType.isPrimitive () == null)
                            parameterTypes.add (parameterType.getQualifiedSourceName ());
                        else
                            parameterTypes.add (parameterType.isPrimitive ().getQualifiedBoxedSourceName ());
                    }

                    srcWriter.println ("    registry.register (\"" + actionName + "\",\"" + methodName + "\",new RemoteMethod<" + getQualifiedSourceName (returnType) + ">() {");
                    //srcWriter.println ("      public void invoke(final IRemoteMethodCallback<" + getQualifiedSourceName (returnType) + "> callback, final Object... args) {");
                    srcWriter.println ("      public void invoke(final IRemoteMethodCallback<" + getQualifiedSourceName (returnType) + "> callback, final List<Object> args) {");
                    srcWriter.println ("        long startTime = System.currentTimeMillis ();");
                    String log = actionName + "." + methodName;
                    if (delay > 0)
                        log += "[" + delay + "ms delay]";
                    if (delay > 0)
                        srcWriter.println ("        Timer t = new Timer () { public void run() { try {");
                    srcWriter.println ("        invokeMethod(callback, args);");
                    if (delay > 0)
                        srcWriter.println ("        } catch (Throwable e) { if (callback != null) { callback.onTransportError (e.getLocalizedMessage ()); } } } }; t.schedule (" + delay + ");");
                    srcWriter.println ("        startTime = System.currentTimeMillis () - startTime;");
                    srcWriter.println ("        if (!GWT.isScript ()) Logger.log(\"" + log + "{\" + startTime + \"ms}\");");
                    srcWriter.println ("      }");
                    //srcWriter.println ("      protected void invokeMethod(IRemoteMethodCallback<" + getQualifiedSourceName (returnType) + "> callback, Object... args) {");
                    srcWriter.println ("      protected void invokeMethod(IRemoteMethodCallback<" + getQualifiedSourceName (returnType) + "> callback, List<Object> args) {");
                    srcWriter.print ("        " + name + "." + method.getName () + " (");
                    for (int j = 0, len = parameterTypes.size (); j < len; j++)
                        srcWriter.print ("(" + parameterTypes.get (j) + ") args[" + j + "],");
                    srcWriter.println ("callback);");
                    srcWriter.println ("      }");
                    srcWriter.println ("    });");
                }
            }
        }
        srcWriter.println ("}");
    }


    /**
     * Attempts to locate the implementation of a method declared in an
     * interface in an implementation of that interface.
     * 
     * @param klass
     *            the implementing class.
     * @param method
     *            the method in the interface.
     * @return The implementing method in the implementing class.
     */
    protected JMethod findImplementingMethod(JClassType klass, JMethod method) {
        String methodDecl = comparableDeclaration (method);
        for (JMethod m : klass.getMethods ()) {
            String mDecl = comparableDeclaration (m);
            if (mDecl.equals (methodDecl))
                return m;
        }
        return null;
    }


    /**
     * Create a representation of a method that permits reasonable comparison.
     * Key are the return types, argument types (and positions) and the method
     * name. Generally used to compare method signatures from different types
     * (i.e. interface and implementation).
     * 
     * @param method
     *            the method to generate the representation for.
     * @return The comparable representation as a string (for string
     *         comparison).
     */
    protected String comparableDeclaration(JMethod method) {
        StringBuffer sb = new StringBuffer ();
        sb.append (method.getReturnType ());
        sb.append (" ");
        sb.append (method.getName ());
        sb.append ("(");
        boolean start = true;
        for (JParameter param : method.getParameters ()) {
            if (!start)
                sb.append (",");
            start = false;
            sb.append (param.getType ().getQualifiedSourceName ());
        }
        sb.append (")");
        return sb.toString ();
    }


    /**
     * Go through each service method and delegate to the method found in the
     * registry.
     * 
     * @param typeOracle
     * @param srcWriter
     * @param serializeClass
     */
    private void contributeMethods(TypeOracle typeOracle, SourceWriter srcWriter, JClassType serializeClass) {
        JClassType callbackType = typeOracle.findType (IRemoteMethodCallback.class.getName ());
        for (JMethod method : serializeClass.getMethods ()) {
            JParameter callback = null;
            List<JParameter> otherParams = new ArrayList<JParameter> ();
            for (JParameter param : method.getParameters ()) {
                JClassType paramClass = param.getType ().isClassOrInterface (); 
                if (paramClass != null && callbackType.isAssignableFrom (paramClass)) {
                    callback = param;
                } else {
                    otherParams.add (param);
                }
            }
            srcWriter.print (method.getReadableDeclaration (false, false, false, false, true));
            srcWriter.println ("{");
            srcWriter.indent ();

            srcWriter.print ("IRemoteMethod m = MethodRegistry.getInstance ().lookup(\"");
            srcWriter.print (serializeClass.getName ());
            srcWriter.print ("\", \"");
            srcWriter.print (method.getName ());
            srcWriter.println ("\");");

            if (callback.getType ().isParameterized () != null) {
                JClassType type = callback.getType ().isParameterized ().getTypeArgs ()[0];
                if (!type.getQualifiedSourceName ().contains ("extends")) {
                    srcWriter.println ("if (m instanceof IRemoteMethodInjectable)");
                    srcWriter.indent ();
                    srcWriter.print ("((IRemoteMethodInjectable) m).setResponseClass (");
                    srcWriter.print (type.getQualifiedSourceName ());
                    srcWriter.println (".class);");
                    srcWriter.outdent ();
                }
            }

            srcWriter.println ("List<Object> argList = new ArrayList<Object>();");
            for (JParameter param : otherParams) {
                srcWriter.print ("argList.add (");
                srcWriter.print (param.getName ());
                srcWriter.println (");");
            }
            srcWriter.print ("m.invoke (");
            srcWriter.print (callback != null ? callback.getName () : "null");
            srcWriter.print (", argList");

            // The following was the previous way of passing arguments as varargs.
            // srcWriter.print ("m.invoke (");
            // srcWriter.print (callback != null ? callback.getName () : "null");
            // for (JParameter param : otherParams) {
            //     srcWriter.print (", ");
            //     srcWriter.print (param.getName ());
            // }

            srcWriter.println (");");
            srcWriter.outdent ();
            srcWriter.println ("}");

        }
    }


    /**
     * Gets the fully qualified class name.
     * 
     * @param type
     *            the type.
     * @return The fully qualified name.
     */
    protected String getQualifiedSourceName(JClassType type) {
        JParameterizedType pType = type.isParameterized ();
        if (pType == null)
            return type.getQualifiedSourceName ();
        String typeClass = type.getQualifiedSourceName () + "<";
        boolean bStart = true;
        for (JClassType typeArg : pType.getTypeArgs ()) {
            if (!bStart)
                typeClass += ",";
            else
                bStart = false;
            typeClass += getQualifiedSourceName (typeArg);
        }
        typeClass += ">";
        return typeClass;
    }

}
