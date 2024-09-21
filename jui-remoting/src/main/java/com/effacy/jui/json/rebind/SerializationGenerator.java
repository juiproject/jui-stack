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
package com.effacy.jui.json.rebind;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gwtproject.json.client.JSONObject;

import com.effacy.jui.json.annotation.JsonSerializable;
import com.effacy.jui.json.annotation.Transient;
import com.effacy.jui.json.annotation.TypeMode;
import com.effacy.jui.json.client.DeserializerHelper;
import com.effacy.jui.json.client.IContributor;
import com.effacy.jui.json.client.ISerializer;
import com.effacy.jui.json.client.IncompatibleObjectException;
import com.effacy.jui.json.client.Serializer;
import com.effacy.jui.json.client.SerializerHelper;
import com.google.gwt.core.ext.BadPropertyValueException;
import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.PropertyOracle;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JAnnotationType;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameterizedType;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

/**
 * Generator for generating serializers (see {@link ISerializer}) that are used
 * in the serialization and deserialization of objects to and from JSON.
 * <p>
 * 
 * @author Jeremy Buckley
 */
public class SerializationGenerator extends Generator {

    /**
     * If the class name should be included with the serialized JSON. This is
     * used for compatibility with some JSON implementations (i.e. Flex JSON).
     */
    private boolean requiresClass = false;

    /**
     * Class name for the serializer class that will be sub-classed.
     */
    private static final String CLS_SERIALIZER = Serializer.class.getName ();

    public static final String PROPERTY_SERIALIZER_ANNOTATIONS = "serializer.annotations";

    public static final String PROPERTY_SERIALIZER_INTERFACES = "serializer.interfaces";

    public static final String PROPERTY_TRANSIENT_ANNOTATIONS = "transient.annotations";

    private List<String> serializerAnnotationClasses = new ArrayList<String> ();

    private List<String> serializerInterfaceClasses = new ArrayList<String> ();

    private List<String> transientAnnotationClasses = new ArrayList<String> ();

    /**
     * {@inheritDoc}
     * 
     * @see com.google.gwt.core.ext.Generator#generate(com.google.gwt.core.ext.TreeLogger,
     *      com.google.gwt.core.ext.GeneratorContext, java.lang.String)
     */
    @Override
    public String generate(TreeLogger logger, GeneratorContext ctx, String requestedClass) throws UnableToCompleteException {
        TypeOracle typeOracle = ctx.getTypeOracle ();
        PropertyOracle propertyOracle = ctx.getPropertyOracle ();

        // Populate the various annotation classes.
        if (serializerAnnotationClasses.isEmpty ()) {

            // Obtain the serializer annotation classes.
            serializerAnnotationClasses.add (JsonSerializable.class.getName ());
            try {
                for (String value : propertyOracle.getConfigurationProperty (PROPERTY_SERIALIZER_ANNOTATIONS).getValues ()) {
                    JClassType type = typeOracle.findType (value);
                    JAnnotationType annotation = type.isAnnotation ();
                    if (annotation == null)
                        logger.log (TreeLogger.ERROR, "Serializer annotation " + value + " is not an annotation so will be ignored.");
                    else
                        serializerAnnotationClasses.add (annotation.getQualifiedSourceName ());
                }
            } catch (BadPropertyValueException e) {
                // Nothing.
            }

            // Obtain the serializer annotation classes.
            try {
                for (String value : propertyOracle.getConfigurationProperty (PROPERTY_SERIALIZER_INTERFACES).getValues ()) {
                    JClassType type = typeOracle.findType (value);
                    if (type == null)
                        logger.log (TreeLogger.ERROR, "Serializer interface " + value + " does not exist so will be ignored.");
                    else if (type.isInterface () == null)
                        logger.log (TreeLogger.ERROR, "Serializer interface " + value + " is not an interface so will be ignored.");
                    else
                        serializerInterfaceClasses.add (type.getQualifiedSourceName ());
                }
            } catch (BadPropertyValueException e) {
                // Nothing.
            }

            // Obtain the transient annotation classes.
            transientAnnotationClasses.add (Transient.class.getName ());
            try {
                for (String value : propertyOracle.getConfigurationProperty (PROPERTY_TRANSIENT_ANNOTATIONS).getValues ()) {
                    JClassType type = typeOracle.findType (value);
                    JAnnotationType annotation = type.isAnnotation ();
                    if (annotation == null)
                        logger.log (TreeLogger.ERROR, "Serializer annotation " + value + " is not an annotation so will be ignored.");
                    else
                        transientAnnotationClasses.add (annotation.getQualifiedSourceName ());
                }
            } catch (BadPropertyValueException e) {
                // Nothing.
            }

        }

        // Resolve all the types that we support serialization for.
        JClassType [] subTypes = null;
        try {
            subTypes = getSerializableTypes (typeOracle);
        } catch (UnableToCompleteException e) {
            logger.log (TreeLogger.ERROR, "Unable to find serializable types", null);
            throw e;
        }

        // Resolve this class and create a sub-class.
        JClassType serializeClass = typeOracle.findType (requestedClass);
        if (serializeClass == null) {
            logger.log (TreeLogger.ERROR, "Unable to find metadata for type '" + requestedClass + "'", null);
            throw new UnableToCompleteException ();
        }
        String packageName = serializeClass.getPackage ().getName ();
        String className = getSafeIdentifier (serializeClass, "TypeSerializer");
        PrintWriter printWriter = ctx.tryCreate (logger, packageName, className);
        if (printWriter == null)
            return packageName + "." + className;

        // Create the composer for the new class.
        ClassSourceFileComposerFactory composerFactory = new ClassSourceFileComposerFactory (packageName, className);
        composerFactory.setSuperclass (CLS_SERIALIZER);

        // Contribute the imports to the new class.
        contributeImports (composerFactory);

        // Create the source writer for the new class.
        SourceWriter srcWriter = composerFactory.createSourceWriter (ctx, printWriter);
        if (srcWriter == null)
            return packageName + "." + className;

        // Create all the inner classes that are needed.
        contributeInnerClasses (logger, typeOracle, srcWriter, subTypes);

        // Create the constructor.
        srcWriter.println ("public " + className + "() {");
        srcWriter.indent ();
        contributeConstructorCode (typeOracle, srcWriter, subTypes);
        srcWriter.outdent ();
        srcWriter.println ("}");

        // Write out the class definition and signal GWT to use that.
        srcWriter.commit (logger);

        return packageName + "." + className;
    }


    /**
     * Contribute code to the body of the constructor.
     * 
     * @param typeOracle
     *            the type oracle.
     * @param srcWriter
     *            the source writer.
     * @param serializableTypes
     *            the types that we are supporting for serialization.
     */
    protected void contributeConstructorCode(TypeOracle typeOracle, SourceWriter sw, JClassType [] serializableTypes) {
        for (JClassType type : serializableTypes) {
            String implClass = getSafeIdentifier (type, "SerializableImpl");
            sw.println (implClass + " _" + implClass + " = new " + implClass + " ();");
            sw.println ("addObjectSerializer (\"" + type.getQualifiedSourceName () + "\", _" + implClass + ");");
            JsonSerializable annotation = findAnnotation (type, JsonSerializable.class);
            if ((annotation != null) && TypeMode.SIMPLE.equals (annotation.type ()))
                sw.println ("addObjectSerializer (\"" + type.getName () + "\", _" + implClass + ");");
        }
    }


    /**
     * Contribute the imports to the sub-class of {@link Serializer}.
     * 
     * @param composerFactory
     *            the composer factory for the sub-class.
     * @param serializableTypes
     *            the types that we support serialization for.
     */
    protected void contributeImports(ClassSourceFileComposerFactory composerFactory) {
        // Add the java imports.
        composerFactory.addImport (java.util.Collection.class.getName ());
        composerFactory.addImport (java.util.List.class.getName ());
        composerFactory.addImport (java.util.Map.class.getName ());
        composerFactory.addImport (java.util.HashMap.class.getName ());
        composerFactory.addImport (java.util.ArrayList.class.getName ());
        composerFactory.addImport (java.util.LinkedList.class.getName ());
        composerFactory.addImport (java.util.Stack.class.getName ());
        composerFactory.addImport (java.util.Vector.class.getName ());
        composerFactory.addImport (java.util.Set.class.getName ());
        composerFactory.addImport (java.util.TreeSet.class.getName ());
        composerFactory.addImport (java.util.HashSet.class.getName ());
        composerFactory.addImport (java.util.LinkedHashSet.class.getName ());
        composerFactory.addImport (java.util.SortedSet.class.getName ());
        composerFactory.addImport (java.util.Date.class.getName ());

        // Add the GWT imports.
        composerFactory.addImport (com.google.gwt.core.client.GWT.class.getName ());
        composerFactory.addImport (org.gwtproject.json.client.JSONNull.class.getName ());
        composerFactory.addImport (org.gwtproject.json.client.JSONNumber.class.getName ());
        composerFactory.addImport (org.gwtproject.json.client.JSONString.class.getName ());
        composerFactory.addImport (org.gwtproject.json.client.JSONValue.class.getName ());
        composerFactory.addImport (org.gwtproject.json.client.JSONObject.class.getName ());
        composerFactory.addImport (org.gwtproject.json.client.JSONArray.class.getName ());
        composerFactory.addImport (org.gwtproject.json.client.JSONBoolean.class.getName ());
        composerFactory.addImport (org.gwtproject.json.client.JSONParser.class.getName ());
        composerFactory.addImport (org.gwtproject.json.client.JSONException.class.getName ());

        // Add the imports from this module.
        composerFactory.addImport (ISerializer.class.getName ());
        composerFactory.addImport (IContributor.class.getName ());
        composerFactory.addImport (JsonSerializable.class.getName ());
        composerFactory.addImport (IncompatibleObjectException.class.getName ());
        composerFactory.addImport (SerializerHelper.class.getName ());
        composerFactory.addImport (DeserializerHelper.class.getName ());
    }


    /**
     * Constribute inner classes.
     * 
     * @param typeOracle
     *            the type oracle.
     * @param srcWriter
     *            the source writer to write the source code to.
     * @param serializableTypes
     *            the types that we are supporting serialization for.
     */
    protected void contributeInnerClasses(TreeLogger logger, TypeOracle typeOracle, SourceWriter sw, JClassType [] serializableTypes) {
        // Create a serializer for each interface that supports Serializable.
        for (JClassType type : serializableTypes) {
            sw.println ("public class " + getSafeIdentifier (type, "SerializableImpl") + " implements ISerializer {");
            sw.indent ();
            sw.println ();
            sw.println ("public " + getSafeIdentifier (type, "SerializableImpl") + "() {}");
            sw.println ();
            try {
                JsonSerializable annotation = findAnnotation (type, JsonSerializable.class);
                TypeMode typeMode = (annotation == null) ? TypeMode.NONE : annotation.type ();
                generateSerializeToJsonMethod (sw, typeOracle, type, typeMode);
                sw.println ();
                generateSerializeFromJsonMethod (sw, typeOracle, type);
                sw.println ();
            } catch (Exception e) {
                logger.log (TreeLogger.ERROR, "Problem generating (de-)serializer for \"" + type.toString () + "\"", e);
            }
            sw.outdent ();
            sw.println ("}");
        }
    }


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
    protected <T extends Annotation> T findAnnotation(JClassType type, Class<T> annotationType) {
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


    /**
     * Creates the {@link ISerializer#serializeToJson(Object, IContributor)}
     * method. This essentials creates an instance of {@link JSONObject} and
     * populates it with properties from the object (accessed through the
     * defining classes getters). An instance of the {@link Serializer} is used
     * to serialize the property values themselves.
     * 
     * @param sw
     *            the source writer to use.
     * @param typeOracle
     *            the type oracle.
     * @param baseType
     *            the (base) type of object.
     * @throws NotFoundException
     *             If the type was not found.
     */
    private void generateSerializeToJsonMethod(SourceWriter sw, TypeOracle typeOracle, JClassType baseType, TypeMode typeMode) throws NotFoundException {
        // Create the method.
        sw.println ("public JSONValue serializeToJson(Object object, IContributor contributor) {");
        sw.indent ();

        // If the object passed is null then return a JSONNull.
        sw.println ("if (object == null)");
        sw.indent ();
        sw.println ("return JSONNull.getInstance();");
        sw.outdent ();
        sw.println ();

        // If the object is not of the required base type, then throw an
        // exception (generally this should not occur but is specified for
        // safety).
        sw.println ("if (!(object instanceof " + baseType.getQualifiedSourceName () + "))");
        sw.indent ();
        sw.println ("throw new IncompatibleObjectException(\"Object not a " + baseType.getQualifiedSourceName () + "\");");
        sw.outdent ();
        sw.println ();

        // Create the result object as a JSONObject (values are added to this).
        sw.println ("JSONObject result = new JSONObject();");

        // Cast the passed object to the base type.
        sw.println (baseType.getQualifiedSourceName () + " castObject = (" + baseType.getQualifiedSourceName () + ") object;");
        sw.println ();

        // Assign values to the JSONObject based on the getters.
        sw.println ("Serializer serializer = Serializer.getInstance();");
        for (JMethod method : extractGetters (baseType))
            sw.println ("result.put (\"" + getPropertyName (method) + "\", serializer.serializeToJson (castObject." + method.getName () + "(), contributor));");

        // Put class type for compatibility with flex JSON [de]serialisation
        if (requiresClass)
            sw.println ("result.put (\"class\", new JSONString (\"" + baseType.getQualifiedSourceName () + "\"));");
        if (TypeMode.SIMPLE.equals (typeMode))
            sw.println ("result.put (\"_type\", new JSONString (\"" + baseType.getName () + "\"));");
        else if (TypeMode.FULL.equals (typeMode))
            sw.println ("result.put (\"_type\", new JSONString (\"" + baseType.getQualifiedSourceName () + "\"));");

        // Return the serialized object.
        sw.println ();
        sw.println ("return result;");
        sw.outdent ();
        sw.println ("}");
    }


    /**
     * Generates the
     * {@link ISerializer#serializeFromJson(com.effacy.jui.json.client.jso.JSONValue, String)}
     * method.
     * 
     * @param typeName
     *            the type name of the type being deserialized.
     * @return The implementation code.
     * @throws NotFoundException
     *             If some object in the type could not be associated with a
     *             serializer.
     * @throws UnableToCompleteException
     */
    private void generateSerializeFromJsonMethod(SourceWriter sw, TypeOracle typeOracle, JClassType baseType) throws NotFoundException, UnableToCompleteException {
        sw.println ("public Object serializeFromJson(JSONValue jsonValue) throws JSONException {");
        sw.indent ();

        // Ensure there is a default constructor, if not then we don't support
        // de-serialization.
        try {
            baseType.getConstructor (new JType[0]);
        } catch (NotFoundException e) {
            sw.println ("return null;");
            sw.outdent ();
            sw.println ("}");
            return;
        }

        // Return null if the given object is JSONNULL or null.
        sw.println ("if ((jsonValue instanceof JSONNull) || (jsonValue == null))");
        sw.indent ();
        sw.println ("return null;");
        sw.outdent ();
        sw.println ();

        // Throw Incompatible exception is JsonValue is not an instance of
        // JsonObject
        sw.println ("if (!(jsonValue instanceof JSONObject))");
        sw.indent ();
        sw.println ("throw new IncompatibleObjectException(\"Object is not a JSONObject\");");
        sw.outdent ();
        sw.println ();

        // Cast the passed object as a JSONObject.
        sw.println ("JSONObject jsonObject = (JSONObject) jsonValue;");

        // Create an instance of the base type (note that we create this as an
        // anonymous inner class so that we can get around a protected
        // constructor).
        String baseTypeName = baseType.getQualifiedSourceName ();
        sw.println (baseTypeName + " result = new " + baseTypeName + "() {};");
        sw.println ();

        sw.println ("JSONObject inputJsonObject = null;");
        sw.println ("JSONValue fieldJsonValue = null;");
        sw.println ();

        // Assign value by setter.
        for (JMethod setter : extractSetters (baseType)) {
            JType propertyType = setter.getParameters ()[0].getType ();
            String propertyName = getPropertyName (setter);

            if (propertyType.isPrimitive () != null) {
                JPrimitiveType fieldPrimitiveType = (JPrimitiveType) propertyType;
                JClassType fieldBoxedType = typeOracle.getType (fieldPrimitiveType.getQualifiedBoxedSourceName ());
                if (fieldBoxedType.getQualifiedSourceName ().equals ("java.lang.Short")) {
                    sw.println ("fieldJsonValue = jsonObject.get (\"" + propertyName + "\");");
                    sw.println ("result." + setter.getName () + " (DeserializerHelper.getShort (fieldJsonValue));");
                } else if (fieldBoxedType.getQualifiedSourceName ().equals ("java.lang.Byte")) {
                    sw.println ("fieldJsonValue = jsonObject.get (\"" + propertyName + "\");");
                    sw.println ("result." + setter.getName () + " (DeserializerHelper.getByte (fieldJsonValue));");
                } else if (fieldBoxedType.getQualifiedSourceName ().equals ("java.lang.Long")) {
                    sw.println ("fieldJsonValue = jsonObject.get (\"" + propertyName + "\");");
                    sw.println ("result." + setter.getName () + " (DeserializerHelper.getLong (fieldJsonValue));");
                } else if (fieldBoxedType.getQualifiedSourceName ().equals ("java.lang.Integer")) {
                    sw.println ("fieldJsonValue = jsonObject.get (\"" + propertyName + "\");");
                    sw.println ("result." + setter.getName () + "(DeserializerHelper.getInt (fieldJsonValue));");
                } else if (fieldBoxedType.getQualifiedSourceName ().equals ("java.lang.Float")) {
                    sw.println ("fieldJsonValue = jsonObject.get (\"" + propertyName + "\");");
                    sw.println ("result." + setter.getName () + "(DeserializerHelper.getFloat (fieldJsonValue));");
                } else if (fieldBoxedType.getQualifiedSourceName ().equals ("java.lang.Double")) {
                    sw.println ("fieldJsonValue = jsonObject.get (\"" + propertyName + "\");");
                    sw.println ("result." + setter.getName () + "(DeserializerHelper.getDouble (fieldJsonValue));");
                } else if (fieldBoxedType.getQualifiedSourceName ().equals ("java.lang.Boolean")) {
                    sw.println ("fieldJsonValue = jsonObject.get (\"" + propertyName + "\");");
                    sw.println ("result." + setter.getName () + "(DeserializerHelper.getBoolean (fieldJsonValue));");
                } else if (fieldBoxedType.getQualifiedSourceName ().equals ("java.lang.Character")) {
                    sw.println ("fieldJsonValue = jsonObject.get (\"" + propertyName + "\");");
                    sw.println ("result." + setter.getName () + "(DeserializerHelper.getShort (fieldJsonValue));");
                }
            } else {
                JClassType fieldClassType = (JClassType) propertyType;
                if (fieldClassType.getQualifiedSourceName ().equals ("java.lang.Short")) {
                    sw.println ("fieldJsonValue = jsonObject.get (\"" + propertyName + "\");");
                    sw.println ("result." + setter.getName () + "(DeserializerHelper.getShort (fieldJsonValue));");
                } else if (fieldClassType.getQualifiedSourceName ().equals ("java.lang.Byte")) {
                    sw.println ("fieldJsonValue = jsonObject.get (\"" + propertyName + "\");");
                    sw.println ("result." + setter.getName () + "(DeserializerHelper.getByte (fieldJsonValue));");
                } else if (fieldClassType.getQualifiedSourceName ().equals ("java.lang.Long")) {
                    sw.println ("fieldJsonValue = jsonObject.get (\"" + propertyName + "\");");
                    sw.println ("result." + setter.getName () + "(DeserializerHelper.getLong (fieldJsonValue));");
                } else if (fieldClassType.getQualifiedSourceName ().equals ("java.lang.Integer")) {
                    sw.println ("fieldJsonValue = jsonObject.get (\"" + propertyName + "\");");
                    sw.println ("result." + setter.getName () + "(DeserializerHelper.getInt (fieldJsonValue));");
                } else if (fieldClassType.getQualifiedSourceName ().equals ("java.lang.Float")) {
                    sw.println ("fieldJsonValue = jsonObject.get (\"" + propertyName + "\");");
                    sw.println ("result." + setter.getName () + "(DeserializerHelper.getFloat (fieldJsonValue));");
                } else if (fieldClassType.getQualifiedSourceName ().equals ("java.lang.Double")) {
                    sw.println ("fieldJsonValue = jsonObject.get (\"" + propertyName + "\");\n");
                    sw.println ("result." + setter.getName () + "(DeserializerHelper.getDouble (fieldJsonValue));");
                } else if (fieldClassType.getQualifiedSourceName ().equals ("java.lang.Boolean")) {
                    sw.println ("fieldJsonValue = jsonObject.get (\"" + propertyName + "\");\n");
                    sw.println ("result." + setter.getName () + "(DeserializerHelper.getBoolean (fieldJsonValue));");
                } else if (fieldClassType.getQualifiedSourceName ().equals ("java.lang.Character")) {
                    sw.println ("fieldJsonValue = jsonObject.get (\"" + propertyName + "\");");
                    sw.println ("result." + setter.getName () + "(DeserializerHelper.getShort (fieldJsonValue));");
                } else if (fieldClassType.getQualifiedSourceName ().equals ("java.util.Date")) {
                    sw.println ("fieldJsonValue = jsonObject.get (\"" + propertyName + "\");");
                    sw.println ("result." + setter.getName () + "(DeserializerHelper.getDate (fieldJsonValue));");
                } else if (fieldClassType.isEnum () != null) {
                    sw.println ("fieldJsonValue = jsonObject.get (\"" + propertyName + "\");");
                    sw.println ("result." + setter.getName () + "(DeserializerHelper.getEnum (" + fieldClassType.getQualifiedSourceName () + ".class, fieldJsonValue));");
                } else if (isJsonSerializable (fieldClassType, true)) {
                    sw.println ("fieldJsonValue = jsonObject.get (\"" + propertyName + "\");");
                    sw.println ("result." + setter.getName () + "((" + fieldClassType.getQualifiedSourceName () + ") Serializer.getInstance().deSerializeWithDefault (fieldJsonValue, \"" + fieldClassType.getQualifiedSourceName () + "\"));");
                } else if (isJsonSerializable (fieldClassType, false)) {
                    sw.println ("fieldJsonValue = jsonObject.get (\"" + propertyName + "\");");
                    sw.println ("result." + setter.getName () + "((" + fieldClassType.getQualifiedSourceName () + ") Serializer.getInstance().deSerialize (fieldJsonValue));");
                } else if (fieldClassType.isAssignableTo (typeOracle.getType ("java.util.Collection"))) {
                    sw.println ("fieldJsonValue = jsonObject.get(\"" + propertyName + "\");");
                    deserializeCollection (typeOracle, sw, fieldClassType, setter, propertyName);
                } else if (fieldClassType.isAssignableTo (typeOracle.getType ("java.util.Map"))) {
                    sw.println ("fieldJsonValue = jsonObject.get(\"" + propertyName + "\");");
                    deserializeMap (typeOracle, sw, fieldClassType, setter);
                } else if (fieldClassType.getQualifiedSourceName ().equals ("java.lang.String")) {
                    sw.println ("fieldJsonValue = jsonObject.get(\"" + propertyName + "\");");
                    sw.println ("result." + setter.getName () + "(DeserializerHelper.getString (fieldJsonValue));");
                }
            }
        }

        sw.println ("return result;");
        sw.outdent ();
        sw.println ("}");
    }


    /**
     * Writes code to deserialize a map.
     * 
     * @param typeOracle
     *            the type oracle.
     * @param sw
     *            the source writer.
     * @param fieldClassType
     *            the field type.
     * @param setter
     *            the setter.
     * @throws UnableToCompleteException
     *             On error.
     * @throws NotFoundException
     *             If class not found.
     */
    protected void deserializeMap(TypeOracle typeOracle, SourceWriter sw, JClassType fieldClassType, JMethod setter) throws UnableToCompleteException, NotFoundException {
        // Return null if JSON object is null
        sw.println ("if (fieldJsonValue == null) {");
        sw.indent ();
        sw.println ("result." + setter.getName () + " (null);");
        sw.outdent ();
        sw.println ("} else {");
        sw.indent ();

        // Throw Incompatible exception if the JSON object is not a object
        sw.println ("if (!(fieldJsonValue instanceof JSONObject))");
        sw.indent ();
        sw.println ("throw new IncompatibleObjectException(\"Value is not a JSONObject\");");
        sw.outdent ();

        // Start deSerilisation
        sw.println ("inputJsonObject = (JSONObject) fieldJsonValue;");

        String fieldTypeQualifiedName = fieldClassType.getQualifiedSourceName ();
        JParameterizedType parameterizedType = (JParameterizedType) fieldClassType;
        JClassType keyType = parameterizedType.getTypeArgs ()[0];
        String keyTypeName = keyType.getQualifiedSourceName ();
        if (!keyTypeName.equals ("java.lang.String"))
            throw new UnableToCompleteException ();
        fieldClassType = parameterizedType.getTypeArgs ()[1];
        String parameterSimpleName = fieldClassType.getParameterizedQualifiedSourceName ();
        String fieldColName = setter.getName () + "Col";
        if (fieldTypeQualifiedName.equals ("java.util.Map") || fieldTypeQualifiedName.equals ("java.util.HashMap"))
            sw.println ("HashMap<" + keyTypeName + ", " + parameterSimpleName + "> " + fieldColName + " = new HashMap<" + keyTypeName + ", " + parameterSimpleName + ">();");
        sw.println ("for (String mapKey : inputJsonObject.keySet ()) {");
        sw.indent ();
        sw.println ("fieldJsonValue = inputJsonObject.get (mapKey);");
        if (fieldClassType.getQualifiedSourceName ().equals ("java.lang.Short")) {
            sw.println (fieldColName + ".put (mapKey, DeserializerHelper.getShort(fieldJsonValue));");
        } else if (fieldClassType.getQualifiedSourceName ().equals ("java.lang.Byte")) {
            sw.println (fieldColName + ".put (mapKey, DeserializerHelper.getByte(fieldJsonValue));");
        } else if (fieldClassType.getQualifiedSourceName ().equals ("java.lang.Long")) {
            sw.println (fieldColName + ".put (mapKey, DeserializerHelper.getLong(fieldJsonValue));");
        } else if (fieldClassType.getQualifiedSourceName ().equals ("java.lang.Integer")) {
            sw.println (fieldColName + ".put (mapKey, DeserializerHelper.getInt(fieldJsonValue));");
        } else if (fieldClassType.getQualifiedSourceName ().equals ("java.lang.Float")) {
            sw.println (fieldColName + ".put (mapKey, DeserializerHelper.getFloat(fieldJsonValue));");
        } else if (fieldClassType.getQualifiedSourceName ().equals ("java.lang.Double")) {
            sw.println (fieldColName + ".put (mapKey, DeserializerHelper.getDouble(fieldJsonValue));");
        } else if (fieldClassType.getQualifiedSourceName ().equals ("java.lang.Boolean")) {
            sw.println (fieldColName + ".put (mapKey, DeserializerHelper.getBoolean(fieldJsonValue));");
        } else if (fieldClassType.getQualifiedSourceName ().equals ("java.lang.Character")) {
            sw.println (fieldColName + ".put (mapKey, DeserializerHelper.getShort(fieldJsonValue));");
        } else if (fieldClassType.getQualifiedSourceName ().equals ("java.util.Date")) {
            sw.println (fieldColName + ".put (mapKey, DeserializerHelper.getDate(fieldJsonValue));");
        } else if (isJsonSerializable (fieldClassType, true)) {
            sw.println (fieldColName + ".put (mapKey, (" + fieldClassType.getQualifiedSourceName () + ") Serializer.getInstance().deSerializeWithDefault(fieldJsonValue, \"" + fieldClassType.getQualifiedSourceName () + "\"));");
        } else if (isJsonSerializable (fieldClassType, false)) {
            sw.println (fieldColName + ".put (mapKey, (" + fieldClassType.getQualifiedSourceName () + ") Serializer.getInstance().deSerialize(fieldJsonValue));");
        } else if (fieldClassType.getQualifiedSourceName ().equals ("java.lang.String")) {
            sw.println (fieldColName + ".put (mapKey, DeserializerHelper.getString (fieldJsonValue));");
        } else if (fieldClassType.isAssignableTo (typeOracle.getType ("java.util.Collection"))) {
            String subListName = setter.getName () + "SubList";
            deserializeCollection (typeOracle, sw, fieldClassType, null, subListName);
            sw.println (fieldColName + ".put (mapKey, " + subListName + "Col);");
        }
        sw.outdent ();
        sw.println ("}");
        sw.println ("result." + setter.getName () + "(" + fieldColName + ");");
        sw.outdent ();
        sw.println ("}");
    }


    /**
     * Deserialize a collection.
     * 
     * @param typeOracle
     *            the type oracle.
     * @param sw
     *            the source writer.
     * @param fieldClassType
     *            the field type.
     * @param setter
     *            the setter.
     * @param fieldName
     *            unique name.
     * @throws NotFoundException
     *             if not found.
     */
    private void deserializeCollection(TypeOracle typeOracle, SourceWriter sw, JClassType fieldClassType, JMethod setter, String fieldName) throws NotFoundException {
        // Setup the field name and variable for population.
        String fieldColName = fieldName + "Col";
        String fieldTypeQualifiedName = getQualifiedSourceName (fieldClassType);
        sw.println (fieldTypeQualifiedName + " " + fieldColName + " = new " + getImplementingClass (fieldTypeQualifiedName) + " ();");

        // Check if the value being set is null.
        sw.println ("if (fieldJsonValue != null) {");
        sw.indent ();

        // If it is not null, then check that it is an array.
        sw.println ("if (!(fieldJsonValue instanceof JSONArray))");
        sw.indent ();
        sw.println ("throw new IncompatibleObjectException(\"" + fieldName + " fieldJsonValue not a JSONObject\");");
        sw.outdent ();

        // Perform the deserialisation.
        sw.println ("JSONArray jsonArray = (JSONArray) fieldJsonValue;");
        sw.println ("for (int i = 0, len = jsonArray.size (); i < len; i++) {");
        sw.indent ();
        sw.println ("fieldJsonValue = jsonArray.get (i);");
        JParameterizedType parameterizedType = (JParameterizedType) fieldClassType;
        JClassType pType = parameterizedType.getTypeArgs ()[0];
        String pTypeQn = pType.getQualifiedSourceName ();
        if (pTypeQn.startsWith ("java.lang.Short")) {
            sw.println (fieldColName + ".add (DeserializerHelper.getShort (fieldJsonValue));");
        } else if (pTypeQn.startsWith ("java.lang.Byte")) {
            sw.println (fieldColName + ".add (DeserializerHelper.getByte (fieldJsonValue));");
        } else if (pTypeQn.startsWith ("java.lang.Long")) {
            sw.println (fieldColName + ".add (DeserializerHelper.getLong (fieldJsonValue));");
        } else if (pTypeQn.startsWith ("java.lang.Integer")) {
            sw.println (fieldColName + ".add (DeserializerHelper.getInt (fieldJsonValue));");
        } else if (pTypeQn.startsWith ("java.lang.Float")) {
            sw.println (fieldColName + ".add (DeserializerHelper.getFloat (fieldJsonValue));");
        } else if (pTypeQn.startsWith ("java.lang.Double")) {
            sw.println (fieldColName + ".add (DeserializerHelper.getDouble (fieldJsonValue));");
        } else if (pTypeQn.startsWith ("java.lang.Boolean")) {
            sw.println (fieldColName + ".add (DeserializerHelper.getBoolean (fieldJsonValue));");
        } else if (pTypeQn.startsWith ("java.lang.Character")) {
            sw.println (fieldColName + ".add (DeserializerHelper.getShort (fieldJsonValue));");
        } else if (pTypeQn.startsWith ("java.util.Date")) {
            sw.println (fieldColName + ".add (DeserializerHelper.getDate (fieldJsonValue));");
        } else if (pType.isEnum () != null) {
            sw.println (fieldColName + ".add (DeserializerHelper.getEnum (" + pTypeQn + ".class, fieldJsonValue));");
        } else if (isJsonSerializable (pType, true)) {
            sw.println (fieldColName + ".add ((" + pTypeQn + ") Serializer.getInstance ().deSerializeWithDefault (fieldJsonValue, \"" + pTypeQn + "\"));");
        } else if (isJsonSerializable (pType, false)) {
            sw.println (fieldColName + ".add ((" + pTypeQn + ") Serializer.getInstance ().deSerialize (fieldJsonValue));");
        } else if (pTypeQn.startsWith ("java.lang.String")) {
            sw.println (fieldColName + ".add (DeserializerHelper.getString (fieldJsonValue));\n");
        }
        sw.outdent ();
        sw.println ("}");
        sw.outdent ();
        sw.println ("}");
        if (setter != null)
            sw.println ("result." + setter.getName () + " (" + fieldColName + ");");
    }


    /**
     * Gets the qualified source name of the passed type. This incorporates all
     * the parameters.
     * 
     * @param type
     *            the type.
     * @return The types qualified source name.
     */
    protected String getQualifiedSourceName(JClassType type) {
        JParameterizedType pType = type.isParameterized ();
        if (pType != null)
            return pType.getParameterizedQualifiedSourceName ();
        return type.getQualifiedSourceName ();
    }


    /**
     * Attempts to resolve the passed type to an implementation of that type.
     * Used for the basic container classes. Note that this supports generics.
     * 
     * @param type
     *            the type to convert.
     * @return The implementing type (inclusive of generic parameters).
     */
    protected String getImplementingClass(String type) {
        if (type.startsWith ("java.util.List"))
            return type.replace ("java.util.List", "java.util.ArrayList");
        if (type.startsWith ("java.util.Set"))
            return type.replace ("java.util.Set", "java.util.HashSet");
        if (type.startsWith ("java.util.SortedSet"))
            return type.replace ("java.util.SortedSet", "java.util.TreeSet");
        return type;
    }


    /**
     * Gets the underlying property name represented by the passed method (which
     * should be a getter or a setter).
     * 
     * @param method
     *            the getter / setter method.
     * @return The underlying property name (maybe {@code null} if the method is
     *         not a getter or setter).
     */
    protected String getPropertyName(JMethod method) {
        String name = method.getName ();
        if (name.startsWith ("get") || name.startsWith ("set"))
            return Character.toLowerCase (name.charAt (3)) + name.substring (4);
        if (name.startsWith ("is"))
            return Character.toLowerCase (name.charAt (2)) + name.substring (3);
        return null;
    }


    /**
     * Creates identifiers based on the given class type. To avoid name
     * collisions, this uses the fully qualified source name and replaces any
     * periods (from the package component or from inner class delimitation)
     * with underscores.
     * 
     * @param type
     *            the type to derive a name from.
     * @suffix suffix to append to identifier
     * @return The An identifier which can be used for class types and
     *         variables.
     */
    protected String getSafeIdentifier(JClassType type, String suffix) {
        return type.getQualifiedSourceName ().replace ('.', '_') + "_" + suffix;
    }


    /**
     * Extract the getters from the specified type (and its super-types). Uses
     * the annotation on the type to determine if setters are required.
     * 
     * @param type
     *            the type to extract from.
     * @return The getter methods.
     */
    protected Collection<JMethod> extractGetters(JClassType type) {
        List<JMethod> getters = new ArrayList<JMethod> ();

        // Determine if setters are required to match a getter.
        JsonSerializable annotation = type.getAnnotation (JsonSerializable.class);
        boolean settersRequired = (annotation != null) && annotation.settersRequired ();

        // Extract the getters.
        while ((type != null) && !isObject (type)) {
            Set<String> methodNames = new HashSet<String> ();
            for (JMethod method : type.getMethods ())
                methodNames.add (method.getName ());
            for (JMethod method : type.getMethods ()) {
                if (method.isPublic () && isGetter (method)) {
                    if (!settersRequired || methodNames.contains ("set" + method.getName ().substring (3)))
                        getters.add (method);
                }
            }
            type = type.getSuperclass ();
        }
        return getters;
    }


    /**
     * Extract the setters from the specified type (and its super-types).
     * 
     * @param type
     *            the type to extract from.
     * @return The setters methods.
     */
    protected Collection<JMethod> extractSetters(JClassType type) {
        Map<String, JMethod> setters = new HashMap<String, JMethod> ();
        while ((type != null) && !isObject (type)) {
            Set<String> methodNames = new HashSet<String> ();
            for (JMethod method : type.getMethods ())
                methodNames.add (method.getName ());
            for (JMethod method : type.getMethods ()) {
                if (method.isPublic () && isSetter (method) && !setters.containsKey (method.getName ())) {
                    if (methodNames.contains ("get" + method.getName ().substring (3)) || methodNames.contains ("is" + method.getName ().substring (3)))
                        setters.put (method.getName (), method);
                }
            }
            type = type.getSuperclass ();
        }
        return setters.values ();
    }


    /**
     * Determines if the passed class is the base object class.
     * 
     * @param type
     *            the type to test.
     * @return {@code true} if it is the object class.
     */
    protected boolean isObject(JClassType type) {
        return Object.class.getName ().equals (type.getQualifiedSourceName ());
    }


    /**
     * Determines if a method is a setter.
     * 
     * @param method
     *            the method to test.
     * @return If it is a setter.
     */
    protected boolean isSetter(JMethod method) {
        if (method.getParameters ().length != 1)
            return false;
        String name = method.getName ();
        if (name.startsWith ("set") && (name.length () > 3))
            return true;
        return method.getReturnType () == null;
    }


    /**
     * Determines if a method is a getter that could be serialized. Any getter
     * method annotated with {@link Transient} is ignored.
     * 
     * @param method
     *            the method to test.
     * @return If it is a getter.
     */
    protected boolean isGetter(JMethod method) {
        if (method.getParameters ().length > 0)
            return false;
        String name = method.getName ();
        for (Annotation annotation : method.getAnnotations ()) {
            if (transientAnnotationClasses.contains (annotation.annotationType ().getName ()))
                return false;
        }
        if (name.startsWith ("get") && (name.length () > 3))
            return true;
        if (name.startsWith ("is") && (name.length () > 2))
            return true;
        return false;
    }


    /**
     * Determines if the passed type is serializable, which means that it must
     * be appropriately annotated (and not {@code null}). If the type is a root
     * type (meaning that it is instantiated in-and-of itself) then this implies
     * some additional checked which is flagged by setting the {@code root}
     * parameter to {@code true}. In this case the type passed should not be
     * abstract or generic (abstracts are never instantiated directly and
     * generics cannot have a unique serializer / de-serializer generated for
     * them as erasure eliminates the parameter type and so can never itself be
     * associated with a serializer / de-serializer).
     * 
     * @param type
     *            the type to test.
     * @param root
     *            {@code true} if the type is a root type and should be checked
     *            to be an allowable root serializable.
     * @return {@code true} if it is serializable.
     */
    protected boolean isJsonSerializable(JClassType type, boolean root) {
        if (root && ((type == null) || type.isAbstract ()))
            return false;
        while (type != null) {
            for (Annotation typeAnnotation : type.getAnnotations ()) {
                if (serializerAnnotationClasses.contains (typeAnnotation.annotationType ().getName ()))
                    return true;
            }
            for (JClassType interfaceType : type.getImplementedInterfaces ()) {
                if (serializerInterfaceClasses.contains (interfaceType.getQualifiedSourceName ()))
                    return true;
                for (Annotation typeAnnotation : interfaceType.getAnnotations ()) {
                    if (serializerAnnotationClasses.contains (typeAnnotation.annotationType ().getName ()))
                        return true;
                }
            }
            type = type.getSuperclass ();
        }
        return false;
    }


    /**
     * Get all the classes that we support serialization for.
     * 
     * @param typeOracle
     *            the type oracle.
     * @return The classes that we support serialization for.
     * @throws UnableToCompleteException
     *             If there was a problem.
     */
    protected JClassType [] getSerializableTypes(TypeOracle typeOracle) throws UnableToCompleteException {
        List<JClassType> serializableTypes = new ArrayList<JClassType> ();
        for (JClassType type : typeOracle.getTypes ()) {
            if (type.isAbstract ())
                continue;
            if (type.isInterface () != null)
                continue;
            if (isJsonSerializable (type, true))
                serializableTypes.add (type);
        }
        return serializableTypes.toArray (new JClassType[serializableTypes.size ()]);
    }
}
