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
package com.effacy.jui.json.parser;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AbstractTypeHierarchyTraversingFilter;
import org.springframework.util.ClassUtils;

import com.effacy.jui.json.annotation.JsonSerializable;
import com.effacy.jui.json.annotation.Transient;
import com.effacy.jui.json.annotation.TypeMode;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

/**
 * Default implementation of {@link IJsonSerializableParser} using the CodeHaus
 * Jackson parser (see {@link ObjectMapper}).
 * <p>
 * This works by mapping each class annotated with {@link JsonSerializable} (and
 * with {@link JsonSerializable#type()} not {@link TypeMode#NONE}) to an
 * internal deserializer. All class that extend such an annotated class are then
 * registered against this deserializer. The custom deserializer looks for the
 * <code>_type</code> property from which is looks up the extending class then
 * just performs a normal deserialisation for that class. The parser needs to
 * know where to resolve these classes and that is performed by calling
 * {@link #scanPackages(String)} to provide a list of packages to scan and
 * create special deserializers for.
 * <p>
 * There is one special case with the above and that is when the annotated class
 * itself is not abstract (so can be validly instantiated as itself). In this
 * case the special deserializer delegates to the default deserializer for the
 * class (obtained from an internal mapper).
 * 
 * @author Jeremy Buckley
 */
public class JsonSerializableParser implements IJsonSerializableParser {

    /**
     * Jackson object mapper (customized).
     */
    private ObjectMapper mapper;

    /**
     * Commons logging.
     */
    private Logger LOG = LoggerFactory.getLogger (JsonSerializableParser.class);

    /**
     * Default constructor.
     */
    public JsonSerializableParser() {
        mapper = JsonMapper.builder ()
            .configure (DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure (SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .annotationIntrospector (new JacksonAnnotationIntrospector () {

                @Override
                protected boolean _isIgnorable(Annotated a) {
                    return super._isIgnorable(a) || a.hasAnnotation (Transient.class);
                }

            })
            .build ();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.json.parser.IJsonSerializableParser#fromJson(java.lang.String,
     *      java.lang.Class)
     */
    @Override
    public <V> V fromJson(String jsonString, Class<V> type) throws JsonParserException {
        try {
            return mapper.readValue (jsonString, type);
        } catch (Throwable e) {
            throw new JsonParserException (e);
        }
    }

    
    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.json.parser.IJsonSerializableParser#fromJsonList(java.lang.String,
     *      java.lang.Class)
     */
    @Override
    public <V> List<V> fromJsonList(String jsonString, Class<V> type) throws JsonParserException {
        try {
            return mapper.readValue (jsonString, mapper.getTypeFactory ().constructCollectionType (List.class, type));
        } catch (Throwable e) {
            throw new JsonParserException (e);
        }
    }



    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.rpc.extdirect.json.IJsonParser#toJson(java.lang.Object)
     */
    @Override
    public String toJson(Object object) throws JsonParserException {
        StringWriter sw = new StringWriter ();
        try {
            mapper.writeValue (sw, object);
        } catch (Throwable e) {
            throw new JsonParserException (e);
        }
        return sw.toString ();
    }

    /**
     * See {@link #scanPackages(Consumer, String...)}. Does not pass a consumer for additions.
     */
    public JsonSerializableParser scanPackages(String... packages) {
        return scanPackages((Consumer<SimpleModule>) null, packages);
    }

    /**
     * Sets the packages to scan for serialization handlers.
     * 
     * @param additions
     *                  used to add any additional (de-)serialisers as desired.
     * @param packages
     *                  the packages (can be separate or one string
     *                  comma-separated).
     */
    @SuppressWarnings("unchecked")
    public JsonSerializableParser scanPackages(Consumer<SimpleModule> additions, String... packages) {
        // TODO: Verify this works properly. It appears that if the root element is
        // generic then we end up with a stackoverflow (for the cases where the API
        // is used then the generic is embedded in a known class).

        // Collect together classes.
        List<Class<?>> classes = new ArrayList<Class<?>> ();
        JsonSerializableScanner scanner = new JsonSerializableScanner ();
        for (String p : packages) {
            if (p == null)
                continue;
            for (String pkg : p.split (","))
                classes.addAll (scanner.getJsonSerializableClasses (pkg));
        }
        if (classes.isEmpty ())
            return this;

        // Obtain the base types.
        Map<Class<?>, PolymorphicDeserialiser> deserializers = new HashMap<Class<?>, PolymorphicDeserialiser> ();
        for (Class<?> type : classes) {
            JsonSerializable annotation = type.getAnnotation (JsonSerializable.class);
            if ((annotation != null) && (annotation.type () != TypeMode.NONE)) {
                // Excluded classes should use the default deserializer, not the
                // PolymorphicDeserialiser
                // if (!excludedClasses.contains (type.getName ())) {
                deserializers.put (type, new PolymorphicDeserialiser (type, annotation.type ()));
                // } else if (LOG.isDebugEnabled ()) {
                //     LOG.debug ("Generic found (but in exclusion list): " + type.getName ());
                // }
            }
        }

        // Map class to base types for serialization.
        for (Class<?> type : classes) {
            if (Modifier.isAbstract (type.getModifiers ()))
                continue;
            for (Class<?> baseType : deserializers.keySet ()) {
                if (baseType.isAssignableFrom (type)) {
                    deserializers.get (baseType).register (type);
                }
            }
        }

        // Log out all all registered class.
        if (LOG.isDebugEnabled()) {
            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw)) {
                pw.println ("Registered the following polymorphic classes:");
                deserializers.forEach((klass,deserializer) -> {
                    pw.println ("> " + klass.getSimpleName());
                    deserializer.registry.forEach((subklass,action) -> {
                        pw.println ("  - " + subklass);
                    });
                });
                pw.flush ();
                LOG.debug (sw.toString ());
            } catch (Throwable e) {
                LOG.error ("Uncaught exception logging registered deserialisers", e);
            }
        }

        // Register module of deserializers.
        SimpleModule module = new SimpleModule ("GenericsModule", new Version (1, 0, 0, null, null, null));
        for (Class<?> type : deserializers.keySet ()) {
            PolymorphicDeserialiser deserializer = deserializers.get (type);
            module.addDeserializer ((Class<Object>) type, deserializer);
        }
        if (additions != null)
            additions.accept (module);
        mapper.registerModule (module);

        return this;
    }

    /**
     * Deserializer implementation for handling polymorphism.
     */
    public static class PolymorphicDeserialiser extends StdDeserializer<Object> {

        /**
         * Registry of type name to class type.
         */
        private Map<String, Class<?>> registry = new HashMap<String, Class<?>> ();

        /**
         * The type name strategy.
         */
        private TypeMode mode;

        /**
         * Construct with the base type and type mode.
         * 
         * @param baseType
         *            the base type.
         * @param mode
         *            the type mode.
         */
        public PolymorphicDeserialiser(Class<?> baseType, TypeMode mode) {
            super (baseType);
            this.mode = mode;
        }


        /**
         * Registers a class (which is expected to descend from the base type
         * specified in the constructor). Note that in the case of inner
         * classes, rather than separating the class names with a dollar sign,
         * this is replaced by a period.
         * 
         * @param type
         *            the type to register.
         */
        public void register(Class<?> type) {
            if (TypeMode.SIMPLE.equals (mode)) {
                String name = type.getName ();
                int i = name.lastIndexOf ('.');
                name = name.substring (i + 1);
                registry.put (name.replace ('$', '.'), type);
            } else if (TypeMode.FULL.equals (mode))
                registry.put (type.getName (), type);
        }


        /**
         * {@inheritDoc}
         * 
         * @see org.codehaus.jackson.map.JsonDeserializer#deserialize(org.codehaus.jackson.JsonParser,
         *      org.codehaus.jackson.map.DeserializationContext)
         */
        @Override
        public Object deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            ObjectMapper mapper = (ObjectMapper) jp.getCodec ();
            ObjectNode root = (ObjectNode) mapper.readTree (jp);
            JsonNode typeName = root.get ("_type");
            if ((typeName != null) && (typeName instanceof TextNode)) {
                String key = typeName.asText ();
                if (registry.containsKey (key)) {
                    Class<?> klass = registry.get (key);
                    return mapper.treeToValue (root, klass);
                }
            }
            return mapper.treeToValue (root, LinkedHashMap.class);
        }
    }

    /**
     * Scans for classes and interfaces that have the {@link JsonSerializable}
     * with a non-{@link TypeMode#NONE} type strategy in their hierarchy.
     */
    public class JsonSerializableScanner extends ClassPathScanningCandidateComponentProvider {

        /**
         * Default constructor.
         */
        public JsonSerializableScanner() {
            super (false);
            addIncludeFilter (new TypeModeFilter ());
        }


        /**
         * Gets the classes that match the scanning rules.
         * 
         * @param basePackage
         *            the base package to search from.
         * @return the matching classes.
         */
        public Collection<Class<?>> getJsonSerializableClasses(String basePackage) {
            basePackage = (basePackage == null) ? "" : basePackage;
            List<Class<?>> classes = new ArrayList<Class<?>> ();
            for (BeanDefinition candidate : findCandidateComponents (basePackage)) {
                try {
                    Class<?> klass = ClassUtils.resolveClassName (candidate.getBeanClassName (), ClassUtils.getDefaultClassLoader ());
                    JsonSerializable annotation = findAnnotation (klass, JsonSerializable.class);
                    if ((annotation != null) || (!klass.isInterface () && !Modifier.isAbstract (klass.getModifiers ())))
                        classes.add (klass);
                } catch (Throwable e) {
                    LOG.error ("Problem while scanning for classes:", e);
                }
            }
            return classes;
        }


        /**
         * {@inheritDoc}
         * 
         * @see org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider#isCandidateComponent(org.springframework.beans.factory.annotation.AnnotatedBeanDefinition)
         */
        @Override
        protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
            return beanDefinition.getMetadata ().isIndependent ();
        }


        /**
         * Finds the annotation of the given type on the class.
         * 
         * @param klass
         *            the class to find the annotations for.
         * @param annotationClass
         *            the annotation class.
         * @return The (top-most) annotation.
         */
        protected <A extends Annotation> A findAnnotation(Class<?> klass, Class<A> annotationClass) {
            return klass.getAnnotation (annotationClass);
        }
    }

    /**
     * Filter that filters classes (or interfaces) that are annotated with
     * {@link JsonSerializable} and have a non-{@link TypeMode#NONE} type
     * strategy applied.
     */
    public static class TypeModeFilter extends AbstractTypeHierarchyTraversingFilter {

        /**
         * Create a new AnnotationTypeFilter for the given annotation type.
         */
        public TypeModeFilter() {
            super (true, true);
        }


        /**
         * {@inheritDoc}
         * 
         * @see org.springframework.core.type.filter.AbstractTypeHierarchyTraversingFilter#matchSelf(org.springframework.core.type.classreading.MetadataReader)
         */
        @Override
        protected boolean matchSelf(MetadataReader metadataReader) {
            AnnotationMetadata metadata = metadataReader.getAnnotationMetadata ();
            if (metadata.hasAnnotation (JsonSerializable.class.getName ()) || metadata.hasMetaAnnotation (JsonSerializable.class.getName ())) {
                Map<String, Object> annotationAttribs = metadata.getAnnotationAttributes (JsonSerializable.class.getName ());
                if (!annotationAttribs.containsKey ("type"))
                    return false;
                TypeMode mode = (TypeMode) annotationAttribs.get ("type");
                if (TypeMode.NONE.equals (mode))
                    return false;
                return true;
            }
            return false;
        }


        /**
         * {@inheritDoc}
         * 
         * @see org.springframework.core.type.filter.AbstractTypeHierarchyTraversingFilter#matchSuperClass(java.lang.String)
         */
        @Override
        protected Boolean matchSuperClass(String superClassName) {
            if (Object.class.getName ().equals (superClassName))
                return Boolean.FALSE;
            if (superClassName.startsWith ("java.")) {
                try {
                    Class<?> klass = getClass ().getClassLoader ().loadClass (superClassName);
                    JsonSerializable annotation = klass.getAnnotation (JsonSerializable.class);
                    if (annotation == null)
                        return false;
                    return !TypeMode.NONE.equals (annotation.type ());
                } catch (ClassNotFoundException ex) {
                    // Nothing.
                }
            }
            return null;
        }


        /**
         * {@inheritDoc}
         * 
         * @see org.springframework.core.type.filter.AbstractTypeHierarchyTraversingFilter#match(org.springframework.core.type.classreading.MetadataReader,
         *      org.springframework.core.type.classreading.MetadataReaderFactory)
         */
        @Override
        public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) {
            try {
                return super.match (metadataReader, metadataReaderFactory);
            } catch (Throwable e) {
                return false;
            }
        }
    }

}
