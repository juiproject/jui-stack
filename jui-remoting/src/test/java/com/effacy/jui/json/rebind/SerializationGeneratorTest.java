package com.effacy.jui.json.rebind;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import com.google.gwt.core.ext.BadPropertyValueException;
import com.google.gwt.core.ext.ConfigurationProperty;
import com.google.gwt.core.ext.DelegatingGeneratorContext;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.PropertyOracle;
import com.google.gwt.core.ext.SelectionProperty;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.dev.javac.GeneratedUnit;
import com.google.gwt.dev.javac.StandardGeneratorContext;
import com.google.gwt.dev.javac.testing.GeneratorContextBuilder;
import com.google.gwt.dev.javac.testing.Source;
import com.google.gwt.dev.util.log.PrintWriterTreeLogger;

class SerializationGeneratorTest {

    @Test
    public void simple() throws Exception {
        RebindResult result = build("test.Person", "test/Person.java", """
            package test;
            import com.effacy.jui.json.annotation.JsonSerializable;
            import com.effacy.jui.json.annotation.TypeMode;

            @JsonSerializable(type = TypeMode.SIMPLE)
            public class Person {
                private String name;
                public Person() {}
                public String getName() { return name; }
                public void setName(String name) { this.name = name; }
            }
        """);

        assertEquals ("test.test_Person_TypeSerializer", result.generatedType());
        assertNotNull (result.generatedUnit(), "expected generated serializer source");
        String source = result.generatedUnit().getSource ();
        assertContains(source, """
            public JSONValue serializeToJson(Object object, IContributor contributor) {
                if (object == null)
                    return JSONNull.getInstance();
                
                if (!(object instanceof test.Person))
                    throw new IncompatibleObjectException("Object not a test.Person");
                
                JSONObject result = new JSONObject();
                test.Person castObject = (test.Person) object;
                
                Serializer serializer = Serializer.getInstance();
                result.put ("name", serializer.serializeToJson (castObject.getName(), contributor));
                result.put ("_type", new JSONString ("Person"));
                
                return result;
            }
        """);
        assertContains(source, """
            public Object serializeFromJson(JSONValue jsonValue) throws JSONException {
            if ((jsonValue instanceof JSONNull) || (jsonValue == null))
                return null;
            
            if (!(jsonValue instanceof JSONObject))
                throw new IncompatibleObjectException("Object is not a JSONObject");
            
            JSONObject jsonObject = (JSONObject) jsonValue;
            test.Person result = new test.Person() {};
            
            JSONObject inputJsonObject = null;
            JSONValue fieldJsonValue = null;
            
            fieldJsonValue = jsonObject.get("name");
            result.setName(DeserializerHelper.getString (fieldJsonValue));
            return result;
            }
        """);
    }

    @Test
    public void fieldAsType() throws Exception {
        RebindResult result = build("test.Person", builder -> {
            builder.add (source ("test/Address.java", """
                package test;
                import com.effacy.jui.json.annotation.JsonSerializable;

                @JsonSerializable
                public class Address {
                    private String street;
                    public Address() {}
                    public String getStreet() { return street; }
                    public void setStreet(String street) { this.street = street; }
                }
            """));
            builder.add (source ("test/Person.java", """
                package test;
                import com.effacy.jui.json.annotation.JsonSerializable;

                @JsonSerializable
                public class Person {
                    private Address address;
                    public Person() {}
                    public Address getAddress() { return address; }
                    public void setAddress(Address address) { this.address = address; }
                }
            """));
        });

        assertEquals ("test.test_Person_TypeSerializer", result.generatedType());
        assertNotNull (result.generatedUnit(), "expected generated serializer source");
        String source = result.generatedUnit().getSource ();
        assertContains(source, """
            public class test_Address_SerializableImpl implements ISerializer {
            """);
        assertContains(source, """
            result.put ("address", serializer.serializeToJson (castObject.getAddress(), contributor));
            """);
        assertContains(source, """
            fieldJsonValue = jsonObject.get ("address");
            result.setAddress((test.Address) Serializer.getInstance().deSerializeWithDefault (fieldJsonValue, "test.Address"));
            """);
        assertContains(source, """
            addObjectSerializer ("test.Address", _test_Address_SerializableImpl);
            """);
        assertContains(source, """
            addObjectSerializer ("test.Person", _test_Person_SerializableImpl);
            """);
    }

    @Test
    public void listField() throws Exception {
        RebindResult result = build("test.Person", "test/Person.java", """
            package test;
            import java.util.List;
            import com.effacy.jui.json.annotation.JsonSerializable;

            @JsonSerializable
            public class Person {
                private List<String> tags;
                public Person() {}
                public List<String> getTags() { return tags; }
                public void setTags(List<String> tags) { this.tags = tags; }
            }
        """);

        assertEquals ("test.test_Person_TypeSerializer", result.generatedType());
        assertNotNull (result.generatedUnit(), "expected generated serializer source");
        String source = result.generatedUnit().getSource ();
        assertContains(source, """
            result.put ("tags", serializer.serializeToJson (castObject.getTags(), contributor));
            """);
        assertContains(source, """
            fieldJsonValue = jsonObject.get("tags");
            java.util.List<java.lang.String> tagsCol = new java.util.ArrayList<java.lang.String> ();
            if ((fieldJsonValue != null) && !(fieldJsonValue instanceof JSONNull)) {
                if (!(fieldJsonValue instanceof JSONArray))
                    throw new IncompatibleObjectException("tags fieldJsonValue not a JSONObject");
                JSONArray jsonArray = (JSONArray) fieldJsonValue;
                for (int i = 0, len = jsonArray.size (); i < len; i++) {
                    fieldJsonValue = jsonArray.get (i);
                    tagsCol.add (DeserializerHelper.getString (fieldJsonValue));
                }
            }
            result.setTags (tagsCol);
            """);
    }

    @Test
    public void mapField() throws Exception {
        RebindResult result = build("test.Person", "test/Person.java", """
            package test;
            import java.util.Map;
            import com.effacy.jui.json.annotation.JsonSerializable;

            @JsonSerializable
            public class Person {
                private java.util.Map<String,String> tags;
                public Person() {}
                public java.util.Map<String,String> getTags() { return tags; }
                public void setTags(java.util.Map<String,String> tags) { this.tags = tags; }
            }
        """);

        assertEquals ("test.test_Person_TypeSerializer", result.generatedType());
        assertNotNull (result.generatedUnit(), "expected generated serializer source");
        String source = result.generatedUnit().getSource ();
        //System.out.println(source);
        assertContains(source, """
            public JSONValue serializeToJson(Object object, IContributor contributor) {
                if (object == null)
                    return JSONNull.getInstance();
                
                if (!(object instanceof test.Person))
                    throw new IncompatibleObjectException("Object not a test.Person");
                
                JSONObject result = new JSONObject();
                test.Person castObject = (test.Person) object;
                
                Serializer serializer = Serializer.getInstance();
                result.put ("tags", serializer.serializeToJson (castObject.getTags(), contributor));
                
                return result;
            }
        """);
        assertContains(source, """
        public Object serializeFromJson(JSONValue jsonValue) throws JSONException {
            if ((jsonValue instanceof JSONNull) || (jsonValue == null))
                return null;
            
            if (!(jsonValue instanceof JSONObject))
                throw new IncompatibleObjectException("Object is not a JSONObject");
            
            JSONObject jsonObject = (JSONObject) jsonValue;
            test.Person result = new test.Person() {};
            
            JSONObject inputJsonObject = null;
            JSONValue fieldJsonValue = null;
            
            fieldJsonValue = jsonObject.get("tags");
            if ((fieldJsonValue == null) || (fieldJsonValue instanceof JSONNull)) {
                result.setTags (null);
            } else {
                if (!(fieldJsonValue instanceof JSONObject))
                    throw new IncompatibleObjectException("Value is not a JSONObject");
                inputJsonObject = (JSONObject) fieldJsonValue;
                HashMap<java.lang.String, java.lang.String> setTagsCol = new HashMap<java.lang.String, java.lang.String>();
                for (String mapKey : inputJsonObject.keySet ()) {
                    fieldJsonValue = inputJsonObject.get (mapKey);
                    setTagsCol.put (mapKey, DeserializerHelper.getString (fieldJsonValue));
                }
                result.setTags(setTagsCol);
            }
            return result;
        }
        """);
    }

    /************************************************************************
     * Support methods.
     ************************************************************************/

    protected void assertContains(String source, String expected) {
        String normalizedSource = source.replaceAll ("\\s+", " ").trim ();
        String normalizedExpected = expected.replaceAll ("\\s+", " ").trim ();
        assertTrue (
            normalizedSource.contains (normalizedExpected),
            () -> "Expected generated source to contain:\n" + expected + "\n\nActual source was:\n" + source
        );
    }

    public record RebindResult(String generatedType, GeneratedUnit generatedUnit) {}

    protected RebindResult build(String type, String file, String source) throws Exception {
        return build(type, builder -> builder.add(source(file, source)));
    }

    /**
     * Obtains a suitably configured builder for testing the serialization
     * generator. This includes source for all relevant classes and annotations, and
     * can be extended with additional sources as needed by individual tests.
     * 
     * @return the builder.
     */
    protected RebindResult build(String type, Consumer<GeneratorContextBuilder> config) throws Exception {
        GeneratorContextBuilder builder = GeneratorContextBuilder.newCoreBasedBuilder();
        builder.add (source (
            "com/effacy/jui/json/annotation/TypeMode.java",
            """
            package com.effacy.jui.json.annotation;
            public enum TypeMode { NONE, SIMPLE, FULL; }
            """
        ));
        builder.add (source (
            "com/effacy/jui/json/annotation/JsonSerializable.java",
            """
            package com.effacy.jui.json.annotation;
            public @interface JsonSerializable {
              TypeMode type() default TypeMode.NONE;
              boolean settersRequired() default false;
            }
            """
        ));
        builder.add (source (
            "com/effacy/jui/json/annotation/Transient.java",
            """
            package com.effacy.jui.json.annotation;
            public @interface Transient {}
            """
        ));
        builder.add (source (
            "com/effacy/jui/json/client/Serializer.java",
            """
            package com.effacy.jui.json.client;
            import org.gwtproject.json.client.JSONValue;
            public abstract class Serializer {
              protected void addObjectSerializer(String name, ISerializer obj) {}
              public static Serializer getInstance() { return null; }
              public Object deSerialize(JSONValue value) { return null; }
              public Object deSerializeWithDefault(JSONValue value, String... names) { return null; }
              public JSONValue serializeToJson(Object value, IContributor contributor) { return null; }
            }
            """
        ));
        builder.add (source (
            "com/effacy/jui/json/client/ISerializer.java",
            """
            package com.effacy.jui.json.client;
            import org.gwtproject.json.client.JSONValue;
            public interface ISerializer {
              JSONValue serializeToJson(Object object, IContributor contributor);
              Object serializeFromJson(JSONValue jsonValue);
            }
            """
        ));
        builder.add (source (
            "com/effacy/jui/json/client/IContributor.java",
            """
            package com.effacy.jui.json.client;
            public interface IContributor<T> {}
            """
        ));
        builder.add (source (
            "com/effacy/jui/json/client/IncompatibleObjectException.java",
            """
            package com.effacy.jui.json.client;
            public class IncompatibleObjectException extends RuntimeException {
              public IncompatibleObjectException(String message) { super(message); }
            }
            """
        ));
        builder.add (source (
            "com/effacy/jui/json/client/SerializerHelper.java",
            """
            package com.effacy.jui.json.client;
            public final class SerializerHelper {
              private SerializerHelper() {}
            }
            """
        ));
        builder.add (source (
            "com/effacy/jui/json/client/DeserializerHelper.java",
            """
            package com.effacy.jui.json.client;
            import org.gwtproject.json.client.JSONValue;
            public final class DeserializerHelper {
              private DeserializerHelper() {}
              public static String getString(JSONValue value) { return null; }
            }
            """
        ));
        builder.add (source (
            "com/google/gwt/core/client/GWT.java",
            """
            package com.google.gwt.core.client;
            public class GWT {
              public static <T> T create(Class<?> type) { return null; }
            }
            """
        ));
        builder.add (source (
            "org/gwtproject/json/client/JSONValue.java",
            """
            package org.gwtproject.json.client;
            public class JSONValue {
              public JSONObject isObject() { return null; }
              public JSONArray isArray() { return null; }
              public JSONString isString() { return null; }
            }
            """
        ));
        builder.add (source (
            "org/gwtproject/json/client/JSONObject.java",
            """
            package org.gwtproject.json.client;
            public class JSONObject extends JSONValue {
              public JSONValue get(String key) { return null; }
              public void put(String key, JSONValue value) {}
              public java.util.Set<String> keySet() { return null; }
            }
            """
        ));
        builder.add (source (
            "org/gwtproject/json/client/JSONArray.java",
            """
            package org.gwtproject.json.client;
            public class JSONArray extends JSONValue {
              public int size() { return 0; }
              public JSONValue get(int i) { return null; }
            }
            """
        ));
        builder.add (source (
            "org/gwtproject/json/client/JSONString.java",
            """
            package org.gwtproject.json.client;
            public class JSONString extends JSONValue {
              public JSONString(String value) {}
            }
            """
        ));
        builder.add (source (
            "org/gwtproject/json/client/JSONNull.java",
            """
            package org.gwtproject.json.client;
            public class JSONNull extends JSONValue {
              public static JSONNull getInstance() { return null; }
            }
            """
        ));
        builder.add (source (
            "org/gwtproject/json/client/JSONNumber.java",
            """
            package org.gwtproject.json.client;
            public class JSONNumber extends JSONValue {
              public JSONNumber(double value) {}
            }
            """
        ));
        builder.add (source (
            "org/gwtproject/json/client/JSONBoolean.java",
            """
            package org.gwtproject.json.client;
            public class JSONBoolean extends JSONValue {
              public static JSONBoolean getInstance(boolean value) { return null; }
            }
            """
        ));
        builder.add (source (
            "org/gwtproject/json/client/JSONParser.java",
            """
            package org.gwtproject.json.client;
            public final class JSONParser {
              private JSONParser() {}
            }
            """
        ));
        builder.add (source (
            "org/gwtproject/json/client/JSONException.java",
            """
            package org.gwtproject.json.client;
            public class JSONException extends RuntimeException {
              public JSONException() {}
            }
            """
        ));
        if (config != null)
            config.accept (builder);
        StandardGeneratorContext baseContext = (StandardGeneratorContext) builder.buildGeneratorContext();
        GeneratorContext context = new DelegatingGeneratorContext(baseContext) {
            @Override
            public PropertyOracle getPropertyOracle() {
                return new PropertyOracle() {
                    @Override
                    public ConfigurationProperty getConfigurationProperty(String propertyName) throws BadPropertyValueException {
                        return new ConfigurationProperty() {
                            @Override
                            public String getName() {
                                return propertyName;
                            }

                            @Override
                            public java.util.List<String> getValues() {
                                return Collections.emptyList ();
                            }
                        };
                    }

                    @Override
                    public SelectionProperty getSelectionProperty(TreeLogger logger, String propertyName) throws BadPropertyValueException {
                        throw new BadPropertyValueException (propertyName);
                    }
                };
            }
        };
        SerializationGenerator generator = new SerializationGenerator();
        PrintWriterTreeLogger logger = new PrintWriterTreeLogger();
        logger.setMaxDetail (TreeLogger.ALL);
        String generatedType = generator.generate (logger, context, type);
        Map<String, GeneratedUnit> generatedUnits = baseContext.getGeneratedUnitMap();
        GeneratedUnit generatedUnit = generatedUnits.get (generatedType);
        return new RebindResult(generatedType, generatedUnit);
    }

    private static Source source(String path, String content) {
        return new Source() {
            @Override
            public String getPath() {
                return path;
            }

            @Override
            public String getSource() {
                return content;
            }
        };
    }
}
