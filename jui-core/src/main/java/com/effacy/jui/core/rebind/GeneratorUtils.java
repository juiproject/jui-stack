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
package com.effacy.jui.core.rebind;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.ext.BadPropertyValueException;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.PropertyOracle;
import com.google.gwt.core.ext.SelectionProperty;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JConstructor;
import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JGenericType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JPackage;
import com.google.gwt.core.ext.typeinfo.JParameter;
import com.google.gwt.core.ext.typeinfo.JParameterizedType;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.JTypeParameter;
import com.google.gwt.dev.util.collect.HashSet;

/**
 * Collection of utilities for rebinding.
 *
 * @author Jeremy Buckley
 */
public final class GeneratorUtils {

    /**
     * Finds an annotation on the given type (if not found checks the class
     * hierarchy then the implemented interfaces).
     * 
     * @param type
     *            the type to check.
     * @param annotationType
     *            the annotation class type to look for.
     * @return the annotation (or {@code null}).
     */
    public static <T extends Annotation> T findAnnotation(JClassType type, Class<T> annotationType) {
        JClassType checkType = type;
        while (checkType != null) {
            T annotation = checkType.getAnnotation (annotationType);
            if (annotation != null)
                return annotation;
            for (JClassType interfaceType : checkType.getImplementedInterfaces ()) {
                annotation = interfaceType.getAnnotation (annotationType);
                if (annotation != null)
                    return annotation;
            }
            checkType = checkType.getSuperclass ();
        }
        return null;
    }


    public static String safeTypeIdentifier(JType type, String suffix) {
        return type.getQualifiedSourceName ().replace ('.', '_') + "_" + suffix;
    }


    /**
     * Finds the parameters of the given type.
     * 
     * @param method
     *            the method to find in.
     * @param type
     *            the type for the parameter.
     * @return The parameters (matching).
     */
    public static JParameter [] findParams(JMethod method, JType type) {
        List<JParameter> params = new ArrayList<JParameter> ();
        for (JParameter param : method.getParameters ()) {
            if (param.getType ().equals (type))
                params.add (param);
        }
        return params.toArray (new JParameter[params.size ()]);
    }


    public static <T extends Annotation> T findFirstAnnotation(JMethod method, Class<T> annotationType) {
        T annotation = method.getAnnotation (annotationType);
        if (annotation != null)
            return annotation;
        method = findOverridden (method, false);
        if (method == null)
            return null;
        return findFirstAnnotation (method, annotationType);
    }


    /**
     * Given a method find the next method that is overrides (if any).
     * 
     * @param method
     *            the method to check.
     * @param concrete
     *            only find the override if the override is not abstract.
     * @return The immediate method being overridden.
     */
    public static JMethod findOverridden(JMethod method, boolean concrete) {
        if (method == null)
            return null;

        JMethod oMethod = findMethod (method.getEnclosingType ().getSuperclass (), method.getName (), method.getParameterTypes ());
        if ((oMethod != null) && concrete && oMethod.isAbstract ())
            return findOverridden (oMethod, true);
        return oMethod;
    }


    /**
     * Determines if the passed method is a callable method (concrete) or
     * overrides a concrete method.
     * 
     * @param method
     *            the method to test.
     * @return {@code true} if it can be invoked (or overrides one).
     */
    public static boolean isConcreteOrHadConcreteOverride(JMethod method) {
        if (method == null)
            return false;

        if (!method.isAbstract ())
            return true;

        return (findOverridden (method, true) != null);
    }


    /**
     * Finds the given method in the class type hierarchy (including
     * interfaces).
     * 
     * @param type
     *            the type.
     * @param name
     *            the name of the method.
     * @param args
     *            the method arguments.
     * @return The first matching method or {@code null}.
     */
    public static JMethod findMethod(JClassType type, String name, JType... args) {
        if (type == null)
            return null;

        // Attempt to locate the method in the type hierarchy.
        JMethod method = findMethodInHierarchy (type, name, args);
        if (method != null)
            return method;

        // Not found so try the interfaces (only makes sense if the class is
        // abstract).
        if (type.isAbstract ()) {
            for (JClassType interfaceType : type.getImplementedInterfaces ()) {
                method = interfaceType.findMethod (name, args);
                if (method != null)
                    return method;
            }
        }

        // Nothing found.
        return null;
    }


    /**
     * Attempt to find the specified method (by name and parameter list) in the
     * strict class hierarchy (excludes interfaces for abstract classes).
     * 
     * @param type
     *            the type.
     * @param name
     *            the name of the method.
     * @param args
     *            the method arguments.
     * @return The first matching method or {@code null}.
     */
    public static JMethod findMethodInHierarchy(JClassType type, String name, JType... args) {
        if (type == null)
            return null;

        // Attempt to locate the method in the type hierarchy.
        JMethod method = type.findMethod (name, args);
        if (method == null)
            method = findMethodInHierarchy (type.getSuperclass (), name, args);

        return method;
    }


    /**
     * Given a type converts the type to a fully qualified string (including all
     * parameterizations).
     * 
     * @param type
     *            the type to convert.
     * @return The converted type.
     */
    public static String qualifiedType(JType type) {
        StringBuffer sb = new StringBuffer (type.getQualifiedSourceName ());
        JGenericType gType = type.isGenericType ();
        JParameterizedType pType = type.isParameterized ();
        if ((gType != null) || (pType != null)) {
            boolean start = true;
            for (JType jType : (gType != null) ? gType.getTypeParameters () : pType.getTypeArgs ()) {
                if (!start)
                    sb.append (',');
                else
                    sb.append ('<');
                start = false;
                String qualifiedType = qualifiedType (jType);
                // if (qualifiedType.contains ("extends"))
                // Logger.log ("Extends: " + qualifiedType (jType));
                sb.append (qualifiedType);
            }
            if (!start)
                sb.append ('>');
        }
        return sb.toString ();
    }


    /**
     * Gets all the methods for the given type (implemented or not).
     * 
     * @param type
     *            the type.
     * @param returnTypes
     *            any return types to filter by (assignable to).
     * @return The methods.
     */
    public static List<JMethod> getMethods(JClassType type, JType... returnTypes) {
        List<JMethod> methods = new ArrayList<JMethod> ();
        Set<String> methodNames = new HashSet<String> ();

        for (JClassType klass : type.getFlattenedSupertypeHierarchy ()) {
            LOOP: for (JMethod method : klass.getMethods ()) {
                BREAK: if (returnTypes.length != 0) {
                    JType returnType = method.getReturnType ();
                    for (JType allowedReturnType : returnTypes) {
                        if (testCompatible (allowedReturnType, returnType))
                            break BREAK;
                    }
                    continue LOOP;
                }
                String methodName = hashSignature (method);
                if (!methodNames.contains (methodName)) {
                    methodNames.add (methodName);
                    methods.add (method);
                }
            }
            // type = type.getSuperclass ();
        }

        return methods;
    }


    public static List<JMethod> getNoParamMethods(JClassType type, JType... returnTypes) {
        List<JMethod> methods = new ArrayList<JMethod> ();
        for (JMethod method : getMethods (type, returnTypes)) {
            if (method.getParameters ().length == 0)
                methods.add (method);
        }
        return methods;
    }


    /**
     * Gets all the methods for the given type (implemented or not).
     * 
     * @param type
     *            the type.
     * @param returnTypes
     *            any return types to filter by (assignable to).
     * @return The methods.
     */
    public static List<JField> getFields(JClassType type, JType... fieldTypes) {
        List<JField> fields = new ArrayList<JField> ();
        Set<String> fieldNames = new HashSet<String> ();

        // Get methods from the class hierarchy.
        while (type != null) {
            LOOP: for (JField field : type.getFields ()) {
                BREAK: if (fieldTypes.length != 0) {
                    JType fieldType = field.getType ();
                    for (JType allowedType : fieldTypes) {
                        if (testCompatible (allowedType, fieldType))
                            break BREAK;
                    }
                    continue LOOP;
                }
                String fieldName = field.getName ();
                if (!fieldNames.contains (fieldName)) {
                    fieldNames.add (fieldName);
                    fields.add (field);
                }
            }
            type = type.getSuperclass ();
        }

        return fields;
    }


    /**
     * Gets all the methods for the given type (implemented or not).
     * 
     * @param type
     *            the type.
     * @param returnTypes
     *            any return types to filter by (assignable to).
     * @return The methods.
     */
    public static Collection<JMethod> getLocalMethods(JClassType type, JType... returnTypes) {
        List<JMethod> methods = new ArrayList<JMethod> ();
        Set<String> methodNames = new HashSet<String> ();

        // Get methods from the class hierarchy.
        LOOP: for (JMethod method : type.getMethods ()) {
            BREAK: if (returnTypes.length != 0) {
                JType returnType = method.getReturnType ();
                for (JType allowedReturnType : returnTypes) {
                    if (testCompatible (allowedReturnType, returnType))
                        break BREAK;
                }
                continue LOOP;
            }
            String methodName = simpleSignature (method);
            if (!methodNames.contains (methodName)) {
                methodNames.add (methodName);
                methods.add (method);
            }
        }

        return methods;
    }


    /**
     * Tests assignment compatibility for a type on the left (being assigned to)
     * against a type on the right (being assigned from).
     * 
     * @param leftType
     *            the assigned to type.
     * @param rightType
     *            the assigned from type.
     * @return {@code true} if the assignment is permitted.
     */
    public static boolean testCompatible(JType leftType, JType rightType) {
        if ((leftType.isClassOrInterface () == null) || (rightType.isClassOrInterface () == null))
            return leftType.equals (rightType);
        return leftType.isClassOrInterface ().isAssignableFrom (rightType.isClassOrInterface ());
    }


    /**
     * Creates a simple signature for the passed method.
     * 
     * @param method
     *            the method.
     * @return The hash code.
     */
    protected static String simpleSignature(JMethod method) {
        StringBuffer sb = new StringBuffer (method.getName ());
        sb.append ('(');
        for (JParameter param : method.getParameters ()) {
            sb.append (',');
            sb.append (param.getName ());
            sb.append (':');
            sb.append (param.getType ().getSimpleSourceName ());
        }
        sb.append ("):");
        sb.append (method.getReturnType ().getSimpleSourceName ());
        return sb.toString ();
    }


    /**
     * Creates a simple signature for the passed method.
     * 
     * @param method
     *            the method.
     * @return The hash code.
     */
    protected static String hashSignature(JMethod method) {
        StringBuffer sb = new StringBuffer (method.getName ());
        sb.append ('(');
        for (JParameter param : method.getParameters ()) {
            sb.append (',');
            sb.append (param.getType ().getSimpleSourceName ());
        }
        sb.append ("):");
        sb.append (method.getReturnType ().getSimpleSourceName ());
        return sb.toString ();
    }


    /**
     * Determines if the passed
     * 
     * @param type
     * @return
     */
    public static ReturnType getReturnType(JType type, String baseType) {
        if (type == null)
            return ReturnType.INVALID;
        ReturnType returnType = ReturnType.SINGLE;
        if (type.isArray () != null) {
            type = type.isArray ().getComponentType ();
            returnType = ReturnType.ARRAY;
        } else {
            JParameterizedType pType = type.isParameterized ();
            if (pType != null) {
                if (!pType.getErasedType ().getQualifiedSourceName ().equals (List.class.getName ()))
                    return ReturnType.INVALID;
                type = pType.getTypeArgs ()[0];
                returnType = ReturnType.LIST;
            }
        }
        return type.getQualifiedSourceName ().startsWith (baseType) ? returnType : ReturnType.INVALID;
    }


    /**
     * Determines if the passed
     * 
     * @param type
     * @return
     */
    public static ReturnType getReturnType(JType type, JType baseType) {
        if (type == null)
            return ReturnType.INVALID;
        ReturnType returnType = ReturnType.SINGLE;
        if (type.isArray () != null) {
            type = type.isArray ().getComponentType ();
            returnType = ReturnType.ARRAY;
        } else if (type.getQualifiedSourceName ().equals (List.class.getName ()) && (type.isGenericType () != null)) {
            type = type.isGenericType ().getTypeParameters ()[0];
            returnType = ReturnType.LIST;
        }
        return type.equals (baseType) ? returnType : ReturnType.INVALID;
    }

    /**
     * Enumerates various return types.
     */
    public enum ReturnType {
        INVALID, SINGLE, ARRAY, LIST;
    }

    /**
     * Determines if the passed type is a VOID.
     * 
     * @param type
     *            the type to compare.
     * @return {@code true} if the type is a void.
     */
    public static boolean isVoid(JType type) {
        // Compare standard VOID primitive type instance directly.
        if (JPrimitiveType.VOID.equals (type))
            return true;

        // Compare using boxed source name if types are different instances but
        // represent the same.
        JPrimitiveType pType = type.isPrimitive ();
        if (pType == null)
            return false;
        return pType.getQualifiedBoxedSourceName ().equals (JPrimitiveType.VOID.getQualifiedBoxedSourceName ());
    }


    /**
     * Determines if the passed type is a Boolean.
     * 
     * @param type
     *            the type to compare.
     * @return {@code true} if the type is a void.
     */
    public static boolean isBoolean(JType type) {
        // Compare standard BOOLEAN primitive type instance directly.
        if (JPrimitiveType.BOOLEAN.equals (type))
            return true;

        // Compare using boxed source name if types are different instances but
        // represent the same.
        JPrimitiveType pType = type.isPrimitive ();
        if (pType == null)
            return false;
        return pType.getQualifiedBoxedSourceName ().equals (JPrimitiveType.BOOLEAN.getQualifiedBoxedSourceName ());
    }


    /**
     * Determines if the passed type is an integer.
     * 
     * @param type
     *            the type to check.
     * @return {@code true} if it is an integer.
     */
    public static boolean isInteger(JType type) {
        if (JPrimitiveType.INT.equals (type))
            return true;

        JPrimitiveType pType = type.isPrimitive ();
        if (pType == null)
            return false;
        return pType.getQualifiedBoxedSourceName ().equals (JPrimitiveType.INT.getQualifiedBoxedSourceName ());
    }


    public static boolean isConstructorSignature(JConstructor constructor, String... types) {
        if (types.length != constructor.getParameters ().length)
            return false;
        for (int i = 0, len = types.length; i < len; i++)
            if (!types[i].equals (constructor.getParameterTypes ()[i].getQualifiedSourceName ()))
                return false;
        return true;
    }


    /**
     * Generates a simple hash of a method.
     * 
     * @param method
     * @return
     */
    public static String methodHash(JMethod method) {
        StringBuffer sb = new StringBuffer ();
        if (isVoid (method.getReturnType ()))
            sb.append ("void ");
        else
            sb.append (qualifiedType (method.getReturnType ()) + ":");
        sb.append (method.getName ());
        sb.append ("(");
        int i = 0;
        for (JParameter param : method.getParameters ()) {
            if (i++ > 0)
                sb.append (",");
            sb.append (qualifiedType (param.getType ()));
        }
        sb.append (")");
        return sb.toString ();
    }


    /**
     * Creates a source code representation of the signature of the passed
     * method suitable for override.
     * 
     * @param method
     *            the method.
     * @return The signature.
     */
    public static String methodSignature(JMethod method) {
        return methodSignature (method, false);
    }


    /**
     * Creates a source code representation of the signature of the passed
     * method suitable for override.
     * 
     * @param method
     *            the method.
     * @param suppressScope
     *            {@code true} to suppress the scope (aka public, private or
     *            protected).
     * @return The signature.
     */
    public static String methodSignature(JMethod method, boolean suppressScope) {
        StringBuffer sb = new StringBuffer ();
        if (!suppressScope) {
            if (method.isProtected ())
                sb.append ("protected ");
            else if (method.isPublic ())
                sb.append ("public ");
        }

        if (isVoid (method.getReturnType ()))
            sb.append ("void ");
        else
            sb.append (qualifiedType (method.getReturnType ()) + " ");
        sb.append (method.getName ());
        sb.append ("(");
        sb.append (methodParams (method, true));
        sb.append (")");
        return sb.toString ();
    }


    public static String methodParams(JMethod method, boolean withTypes) {
        return methodParams (method, withTypes, false);
    }


    public static String methodParams(JMethod method, boolean withTypes, boolean qualifiedTypes) {
        StringBuffer sb = new StringBuffer ();
        int i = 0;
        for (JParameter param : method.getParameters ()) {
            if (i++ > 0)
                sb.append (", ");
            if (withTypes) {
                if (qualifiedTypes) {
                    sb.append (qualifiedType (param.getType ()));
                } else {
                    JTypeParameter paramType = param.getType ().isTypeParameter ();
                    if (paramType == null)
                        sb.append (param.getType ().getQualifiedSourceName ().replace ('$', '.'));
                    else
                        sb.append (paramType.getBaseType ().getQualifiedSourceName ().replace ('$', '.'));
                }
                sb.append (" ");
            }
            sb.append (param.getName ());
        }
        return sb.toString ();
    }


    /**
     * Converts a package relative path into an absolute path.
     * 
     * @param pkg
     *            the package
     * @param path
     *            a path relative to the package
     * @return an absolute path
     */
    public static String getPathRelativeToPackage(JPackage pkg, String path) {
        return pkg.getName ().replace ('.', '/') + '/' + path;
    }


    /**
     * Obtains the current locale.
     * 
     * @param logger
     *            the tree logger.
     * @param genContext
     *            the generator context.
     * @return The current locale.
     */
    public static String getLocale(TreeLogger logger, GeneratorContext context) {
        String locale;
        try {
            PropertyOracle oracle = context.getPropertyOracle ();
            SelectionProperty prop = oracle.getSelectionProperty (logger, "locale");
            locale = prop.getCurrentValue ();
        } catch (BadPropertyValueException e) {
            locale = null;
        }
        return locale;
    }


    /**
     * Private constructor.
     */
    private GeneratorUtils() {
        // Nothing.
    }
}
