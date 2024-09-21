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
package com.effacy.jui.json.client;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gwtproject.json.client.JSONArray;
import org.gwtproject.json.client.JSONBoolean;
import org.gwtproject.json.client.JSONException;
import org.gwtproject.json.client.JSONNull;
import org.gwtproject.json.client.JSONNumber;
import org.gwtproject.json.client.JSONObject;
import org.gwtproject.json.client.JSONParser;
import org.gwtproject.json.client.JSONString;
import org.gwtproject.json.client.JSONValue;

import com.effacy.jui.json.annotation.JsonSerializable;
import com.effacy.jui.json.rebind.SerializationGenerator;
import com.google.gwt.core.client.GWT;

/**
 * Base class for serialization. It is expected that the
 * {@link SerializationGenerator} will be invoked to create an appropriate
 * sub-class that provides {@link ISerializer} instances for the various key
 * types.
 * 
 * @author Jeremy Buckley
 */
public abstract class Serializer {

    /**
     * Map of type name to serializable type.
     */
    private static Map<String, ISerializer> SERIALIZABLE_TYPES;

    /**
     * Singleton instance.
     */
    private static Serializer INSTANCE;

    /**
     * Default constructor.
     */
    protected Serializer() {
        // Nothing.
    }


    /**
     * Gets the instance of the serializer.
     * 
     * @return The instance.
     */
    public static final Serializer getInstance() {
        if (INSTANCE == null)
            INSTANCE = GWT.create (Serializer.class);
        return INSTANCE;
    }


    /**
     * Generate the serialization types.
     * 
     * @return The serialization types.
     */
    private static Map<String, ISerializer> serializableTypes() {
        if (SERIALIZABLE_TYPES == null)
            SERIALIZABLE_TYPES = new HashMap<String, ISerializer> ();
        return SERIALIZABLE_TYPES;
    }


    /**
     * Adds an object serializer for the given type name. The type name may
     * either be a proper Java fully qualified name or one that is returned by
     * <code>JClassType</code> (the latter does not use a $ to delimit inner
     * classes).
     * 
     * @param name
     *            the name of the type.
     * @param obj
     *            the object serializer.
     */
    protected void addObjectSerializer(String name, ISerializer obj) {
        serializableTypes ().put (name.replace ('$', '.'), obj);
    }


    /**
     * Gets the object serializer for the given type. The type name may either
     * be a proper Java fully qualified name or one that is returned by
     * <code>JClassType</code> (the latter does not use a $ to delimit inner
     * classes).
     * 
     * @param name
     *            the name of the type.
     * @return The serializer.
     */
    protected ISerializer getObjectSerializer(String name) {
        return serializableTypes ().get (name.replace ('$', '.'));
    }


    /**
     * Serializes a given object to a JSON string. The object must be annotated
     * with {@link JsonSerializable}.
     * 
     * @param pojo
     *            the object to serialize.
     * @return The serialized object represented in JSON.
     * @throws SerializationException
     *             If the serialization failed.
     */
    public String serialize(Object pojo) throws SerializationException {
        return serializeToJson (pojo).toString ();
    }


    /**
     * Serializes a given object to a JSON string. The object must be annotated
     * with {@link JsonSerializable}.
     * 
     * @param pojo
     *            the object to serialize.
     * @return The serialized object represented in JSON.
     * @throws SerializationException
     *             If the serialization failed.
     */
    public String serialize(Object pojo, IContributor<Object> contributor) throws SerializationException {
        return serializeToJson (pojo, contributor).toString ();
    }


    /**
     * Serializes a given object to a JSON value. The object must be annotated
     * with {@link JsonSerializable}.
     * 
     * @param pojo
     *            the object to serialize.
     * @return The serialized object represented as a {@link JSONValue}.
     * @throws SerializationException
     *             If the serialization failed.
     */
    public JSONValue serializeToJson(Object pojo) throws SerializationException {
        return serializeToJson (pojo, null);
    }


    /**
     * De-serializes a JSON value to the specified class.
     * <p>
     * Note: this has been modified so that maven will compile by adding the
     * cast '(T)'
     * 
     * @param jsonValue
     *            the json value to de-serialize.
     * @param klass
     *            the class to convert to.
     * @return An instance of the converted class.
     * @throws SerializationException
     *             If the serialization failed.
     */
    @SuppressWarnings("unchecked")
    public <T> T deSerialize(JSONValue jsonValue, Class<T> klass) throws SerializationException {
        return (T) deSerialize (jsonValue, klass.getName ());
    }


    /**
     * De-serializes a JSON value assuming that the class type is encoded in the
     * passed value under the key <code>_type</code>.
     * 
     * @param jsonValue
     *            the JSON value to de-serialize.
     * @return The de-serialized value.
     * @throws SerializationException
     *             If there was a problem de-serializing (such as not finding
     *             the type parameter).
     */
    @SuppressWarnings("unchecked")
    public <T> T deSerialize(JSONValue jsonValue) throws SerializationException {
        if (jsonValue == null)
            return null;
        JSONObject jsonObject = jsonValue.isObject ();
        if (jsonObject == null)
            return null;
        JSONValue typeClass = jsonObject.get ("_type");
        if (typeClass == null)
            throw new SerializationException ("Can't find object serializer for value (no _type parameter to determine type from)");
        JSONString sTypeClass = typeClass.isString ();
        if (sTypeClass == null)
            throw new SerializationException ("Can't find object serializer for value (_type parameter found but not a string)");
        return (T) deSerialize (jsonValue, (String) sTypeClass.stringValue ());
    }


    /**
     * De-serializes a JSON value trying to find the type in the key
     * <code>_type</code> and defaulting to another className if needed.
     * 
     * @param jsonValue
     *            the JSON value to de-serialize.
     * @param defaultClassNames
     *            the names of the classes to convert to
     * @return The de-serialized value.
     * @throws SerializationException
     *             If there was a problem de-serializing
     */
    @SuppressWarnings("unchecked")
    public <T> T deSerializeWithDefault(JSONValue jsonValue, String... defaultClassNames) throws SerializationException {
        String message = "";
        try {
            return deSerialize (jsonValue);
        } catch (SerializationException e) {
            message += e.getMessage ();
            for (String className : defaultClassNames) {
                try {
                    return deSerialize (jsonValue, className);
                } catch (SerializationException t) {
                    message += t.getMessage ();
                }
            }
        }
        throw new SerializationException (message);
    }


    /**
     * De-serializes a JSON value to the specified class.
     * 
     * @param jsonValue
     *            the json value to de-serialize.
     * @param className
     *            the name of the class to convert to.
     * @return An instance of the converted class.
     * @throws SerializationException
     *             If the serialization failed.
     */
    @SuppressWarnings("unchecked")
    public <T> T deSerialize(JSONValue jsonValue, String className) throws SerializationException {
        if ((jsonValue == null) || className.equals (Void.class.getName ()))
            return null;
        ISerializer serializer = getObjectSerializer (className);
        if (serializer == null)
            throw new SerializationException ("Can't find object serializer for " + className);
        try {
            return (T) serializer.serializeFromJson (jsonValue);
        } catch (JSONException e) {
            throw new SerializationException (e);
        }
    }


    /**
     * De-serializes a JSON array to an array of the specified class.
     * 
     * @param jsonArray
     *            the json value to de-serialize.
     * @param klass
     *            the class to convert to.
     * @return An instance of the converted class.
     * @throws SerializationException
     *             If the serialization failed.
     */
    public <T> List<T> deSerialize(JSONArray jsonArray, Class<T> klass) throws SerializationException {
        if (klass.equals (Void.class))
            return new ArrayList<T> ();
        ISerializer serializer = getObjectSerializer (klass.getName ());
        if (serializer == null)
            throw new SerializationException ("Can't find object serializer for " + klass.getName ());
        try {
            List<T> items = new ArrayList<T> ();
            if (jsonArray == null)
                return items;
            for (int i = 0; i < jsonArray.size (); i++)
                items.add ((T) deSerialize (jsonArray.get (i), klass));
            return items;
        } catch (JSONException e) {
            throw new SerializationException (e);
        }
    }


    /**
     * De-serializes a JSON string to the specified class.
     * 
     * @param jsonString
     *            the json string to de-serialize.
     * @param klass
     *            the class to convert to.
     * @return An instance of the converted class.
     * @throws SerializationException
     *             If the serialization failed.
     */
    public <T> T deSerialize(String jsonString, Class<T> klass) throws SerializationException {
        try {
            JSONValue value = JSONParser.parseLenient (jsonString);
            return deSerialize (value, klass);
        } catch (JSONException e) {
            throw new SerializationException (e);
        }
    }


    /**
     * De-serializes a JSON string to an array of the specified class.
     * 
     * @param jsonString
     *            the json string to de-serialize.
     * @param klass
     *            the class to convert to.
     * @return An instance of the converted class.
     * @throws SerializationException
     *             If the serialization failed.
     */
    @SuppressWarnings("deprecation")
    public <T> List<T> deSerializeArray(String jsonString, Class<T> klass) throws SerializationException {
        try {
            JSONValue value = JSONParser.parse (jsonString);
            return deSerialize (value.isArray (), klass);
        } catch (JSONException e) {
            throw new SerializationException (e);
        }
    }


    /**
     * Clones an object by doing a serialize followed by a deserialize.
     * 
     * @param <T>
     *            Type of object to clone.
     * @param object
     *            Instance to clone.
     * @return Cloned instance.
     */
    @SuppressWarnings("unchecked")
    public <T> T clone(T object) {
        return (T) deSerialize (serialize (object), object.getClass ());
    }


    /**
     * Serializes a given object to a JSON value. The object must be annotated
     * with {@link JsonSerializable}.
     * 
     * @param pojo
     *            the object to serialize.
     * @return The serialized object represented as a {@link JSONValue}.
     * @throws SerializationException
     *             If the serialization failed.
     */
    public JSONValue serializeToJson(Object pojo, IContributor<Object> contributor) throws SerializationException {
        if (pojo == null)
            return JSONNull.getInstance ();
        if (pojo instanceof JSONValue)
            return (JSONValue) pojo;
        if (pojo instanceof Map<?, ?>) {
            JSONObject jsonValue = new JSONObject ();
            for (Object key : ((Map<?, ?>) pojo).keySet ()) {
                Object value = ((Map<?, ?>) pojo).get (key);
                jsonValue.put (key.toString (), serializeToJson (value, contributor));
            }
            return jsonValue;
        }
        if (pojo instanceof Collection<?>) {
            JSONArray jsonValue = new JSONArray ();
            int i = 0;
            for (Object value : (Collection<?>) pojo)
                jsonValue.set (i++, serializeToJson (value, contributor));
            return jsonValue;
        }
        if (pojo.getClass ().isArray ()) {
            JSONArray jsonValue = new JSONArray ();
            for (int i = 0, len = Array.getLength (pojo); i < len; i++)
                jsonValue.set (i, serializeToJson (Array.get (pojo, i), contributor));
            return jsonValue;
        }
        if (pojo instanceof String)
            return new JSONString ((String) pojo);
        if (pojo instanceof Boolean)
            return JSONBoolean.getInstance ((Boolean) pojo);
        if (pojo instanceof Integer)
            return new JSONNumber ((double) ((Integer) pojo));
        if (pojo instanceof Long)
            return new JSONNumber ((double) ((Long) pojo));
        if (pojo instanceof Double)
            return new JSONNumber ((double) ((Double) pojo));
        if (pojo instanceof Float)
            return new JSONNumber ((double) ((Float) pojo));
        if (pojo instanceof Date)
            return SerializerHelper.getDate ((Date) pojo);
        if (pojo instanceof Enum)
            return SerializerHelper.getEnum ((Enum<?>) pojo);
        if (pojo instanceof Void)
            return JSONNull.getInstance ();

        // Look up the serializer.
        ISerializer serializer = getObjectSerializer (pojo.getClass ().getName ());
        BREAK: if (serializer == null) {
            if (SerializerHelper.isAnonymous (pojo.getClass ())) {
                serializer = getObjectSerializer (pojo.getClass ().getSuperclass ().getName ());
                if (serializer != null)
                    break BREAK;
            }
            return JSONNull.getInstance ();
        }
        if (contributor == null)
            return serializer.serializeToJson (pojo, contributor);
        JSONObject jsonObject = new JSONObject ();
        Injector injector = new Injector (jsonObject, contributor);
        contributor.contribute (pojo, injector);
        if (injector.isCancelled ())
            return jsonObject;
        if (jsonObject.keySet ().isEmpty ())
            return serializer.serializeToJson (pojo, contributor);
        JSONObject value = (JSONObject) serializer.serializeToJson (pojo, contributor);
        for (String key : jsonObject.keySet ())
            value.put (key, jsonObject.get (key));
        return value;
    }


    /**
     * Given a list of objects, serialize these collectively into a single JSON
     * object.
     * 
     * @param contributor
     *            the contributor to apply as necessary.
     * @param pojos
     *            the objects to convert.
     * @return The serialized merge.
     */
    public JSONObject mergeToJsonObject(IContributor<Object> contributor, List<Object> pojos) {
        JSONObject result = new JSONObject ();

        int i = 0;
        for (Object pojo : pojos) {
            if (pojo != null) {
                JSONObject obj = serializeToJsonObject (contributor, pojo, "data" + (i++));
                if (obj != null) {
                    for (String key : obj.keySet ())
                        result.put (key, obj.get (key));
                }
            }
        }

        return result;
    }


    /**
     * Converts an object to a JSONObject. If the passed object is an array or
     * primitive then it is added to an empty object under the given property
     * name, otherwise it is converted wholesale.
     * 
     * @param contributor
     *            an (optional) contributor to use during conversion.
     * @param pojo
     *            the object to convert.
     * @param property
     *            the property to convert.
     * @return The JSON object.
     */
    protected JSONObject serializeToJsonObject(IContributor<Object> contributor, Object pojo, String property) {
        if (pojo == null)
            return null;

        // Try to convert to a JSONValue.
        if (!(pojo instanceof JSONValue))
            pojo = Serializer.getInstance ().serializeToJson (pojo, contributor);

        // Convert the JSONValue / JSONArray.
        if (pojo instanceof JSONObject)
            return (JSONObject) pojo;

        JSONObject jsonObject = new JSONObject ();
        jsonObject.put (property, (JSONValue) pojo);
        return jsonObject;
    }

    /**
     * Local implementation of {@link IContributorInjector}.
     */
    private class Injector implements IContributorInjector {

        /**
         * The value being contributed to.
         */
        private JSONObject value;

        /**
         * The contributor.
         */
        private IContributor<Object> contributor;

        /**
         * Flag indicating if the contribution process has been cancelled.
         */
        private boolean cancelled;

        /**
         * Construct with the object to contribute to and the contributor (to
         * pass through).
         * 
         * @param value
         *            the value being contributed to.
         * @param contributor
         *            the contributor to pass through.
         */
        public Injector(JSONObject value, IContributor<Object> contributor) {
            this.value = value;
            this.contributor = contributor;
        }


        /**
         * {@inheritDoc}
         * 
         * @see com.effacy.jui.json.client.IContributorInjector#put(java.lang.String,
         *      java.lang.Object)
         */
        @Override
        public void put(String key, Object item) {
            ((JSONObject) value).put (key, serializeToJson (item, contributor));
        }


        /**
         * {@inheritDoc}
         * 
         * @see com.effacy.jui.json.client.IContributorInjector#cancel()
         */
        @Override
        public void override() {
            this.cancelled = true;
        }


        /**
         * Determines if the contribution process has been cancelled.
         * 
         * @return {@code true} if it has.
         */
        public boolean isCancelled() {
            return cancelled;
        }
    }
}
