package com.effacy.jui.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Used to test a {@link Test} object using natural language expressions.
 * <p>
 * To use one creates an instance of {@link NLTester} parameterised by the type
 * of object being tested. To this one regsisters (via
 * {@link #register(String, BiConsumer)}) one-line test templates with their
 * respective implementation (by way of an action).
 * <p>
 * Having configured it may be employed against a specific instance along with a
 * multi-line string containing the various tests to perform. The tests are
 * bundled on separate lines and each is macthed against a registered template.
 * Where a template match is found any variables are extracted (as declared by
 * the template) and passed to the action to use to configure an explicit set of
 * tests against the object.
 * <p>
 * Templates are fairly limited where variables are deliminated by brackets (and
 * should have no whitespace). An example being:
 * <tt>
 *   The number of elements in the list is {n}
 * </tt>
 * Putting this to task in a tester:
 * <tt>
 *  var tester = new NLTester<List<Double>>()
 *     .register ("The number of elements in the list is {n}", (v,p) -> {
 *         v.is(w -> w.size(), p.asInt("n"));
 *     });
 * 
 *   ...
 * 
 *   List<Double> testList = List.of (2.2, 2.3, 2.4);
 *   tester.test(testList,"""
 *     The number of elements in the list is 3
 *   """)
 * </tt>
 */
public class NLTester<V> {

    /**
     * Represents a collection of properties extracted from a single test statement.
     */
    public static class PropertySet {
    
        /**
         * Collection of extracted properties.
         */
        private Map<String,String> properties;

        /**
         * Construct with underlying properties extracted from the test statement.
         * 
         * @param properties
         *                   the properties.
         */
        public PropertySet(Map<String,String> properties) {
            this.properties = (properties == null) ? new HashMap<>() : properties;
        }

        /**
         * Obtains the raw property of the given name. I.e. for the test template "The
         * number {n}" against the test statement "The number 33" will map the property
         * "n" to the value "23".
         * 
         * @param key
         *            the property name.
         * @return the property value.
         */
        public String get(String key) {
            return properties.get(key);
        }

        /**
         * See {@link #get(String)} but converts to an integer ({@code null} values are
         * passed through). If it does not parse an assertion error is generated.
         * 
         * @param key
         *            the property name.
         * @return the property value as an integer.
         */
        public Integer asInt(String key) {
            String v = properties.get(key);
            try {
                return (v == null) ? null : Integer.parseInt(v);
            } catch (NumberFormatException e) {
                throw new AssertionError("Expected an int but got: " + v);
            }
        }

        /**
         * See {@link #get(String)} but converts to a long ({@code null} values are
         * passed through). If it does not parse an assertion error is generated.
         * 
         * @param key
         *            the property name.
         * @return the property value as a long.
         */
        public Long asLong(String key) {
            String v = properties.get(key);
            try {
                return (v == null) ? null : Long.parseLong(v);
            } catch (NumberFormatException e) {
                throw new AssertionError("Expected a long but got: " + v);
            }
        }

        /**
         * See {@link #get(String)} but converts to a double ({@code null} values are
         * passed through). If it does not parse an assertion error is generated.
         * 
         * @param key
         *            the property name.
         * @return the property value as a double.
         */
        public Double asDouble(String key) {
            String v = properties.get(key);
            try {
                return (v == null) ? null : Double.parseDouble(v);
            } catch (NumberFormatException e) {
                throw new AssertionError("Expected a double but got: " + v);
            }
        }
        
    }

    /**
     * Construct instance of the tester. Templates are expected to be added via
     * {@link #register(String,BiConsumer<>)}.
     */
    public NLTester() {
        // Nothing.
    }

    /**
     * Constructs an instance with a lambda-expression to used to register
     * templates.
     * 
     * @param builder the builder expression.
     */
    public NLTester(Consumer<NLTester<V>> builder) {
        if (builder != null)
            builder.accept(this);
    }

    /**
     * Registers a single template with a action that implements the test
     * represented by statements that adhere to the template.
     * <p>
     * When the template matches a test statement the variables declared in the
     * template are used to extract their corresponding values from the test
     * statement. These are bundled into a {@link PropertySet} and passed, along
     * with the object being tested, to the action. See {@link #test(Test, String)}.
     * 
     * @param template
     *                 the template expression.
     * @param action
     *                 the test action to perform.
     * @return this tester instance.
     */
    public NLTester<V> register(String template, BiConsumer<Test<V>,PropertySet> action) {
        if ((template != null) && (action != null))
            processors.add(new Processor<V> (template, action));
        return this;
    }

    /**
     * See {@link #test(Test, String)} but using the underlying object.
     */
    public Test<V> test(V object, String script) {
        return test(Test.$(object), script, null);
    }

    /**
     * See {@link #test(Object, String, Map)} but using the underlying object.
     */
    public Test<V> test(V object, String script, Map<String,Object> replacements) {
        return test(Test.$(object), script, replacements);
    }

    /**
     * Tests the given object subject to the tests in the given script.
     * 
     * @param object
     *               the object under test as a {@link Test}.
     * @param script
     *               the script to execute.
     * @return the passed object.
     */
    public Test<V> test(Test<V> object, String script) {
        return test(object, script, null);
    }

    /**
     * Tests the given object subject to the tests in the given script.
     * <p>
     * Sometimes a script will include references to variables in itself (i.e.
     * subsitituting ID's from variables). One approach is to construct the script
     * using these data while the other is to provide a replacement mechanism. For
     * multi-line strings the latter is the most effective and this is provided by
     * the <code>replacements</code> map. If present then each variable that is
     * surrounded in double braces will be replaced.
     * 
     * @param object
     *                     the object under test.
     * @param script
     *                     the script to execute.
     * @param replacements
     *                     a map of replacements to make into the script.
     * @return the passed object.
     */
    public Test<V> test(Test<V> object, String script, Map<String,Object> replacements) {
        // Perform any replacements.
        if (replacements != null) {
            for (Map.Entry<String,Object> r : replacements.entrySet()) {
                script = script.replace("{{" + r.getKey() + "}}", "" + r.getValue());
            }
        }

        // Extract each line (removing comments).
        List<String> lines = new ArrayList<>();
        for (String line : script.split(System.lineSeparator())) {
            line = line.trim();
            if (line.isBlank())
                continue;
            if (line.startsWith("#") || line.startsWith("//"))
                continue;
            lines.add(line);
        }

        int idx = 0;
        LINE: for (String line : lines) {
            idx++;
            for (Processor<V> processor : processors) {
                try {
                    if (processor.process(object, line))
                        continue LINE;
                } catch (AssertionError e) {
                    throw new AssertionError("At line " + idx + " " + e.getMessage(), e.getCause());
                }
            }
            // If we are here there is no match for the line.
            throw new AssertionError("There was no registered action for the test expressed on line " + idx);
        }
        return object;
    }

    /**
     * Registered template processors.
     */
    private List<Processor<V>> processors = new ArrayList<>();

    /**
     * A template processor. This matches its underlying template, performs a
     * variable extraction and enacts the associated test action.
     */
    static class Processor<W> {

        /**
         * The template used for matching and variable extraction.
         */
        private String template;

        /**
         * The test action to perform on a match.
         */
        private BiConsumer<Test<W>,PropertySet> action;

        /**
         * Construct with template and associated test action.
         * 
         * @param template
         *                 the template.
         * @param action
         *                 the test action to invoke when there is a match.
         */
        Processor(String template, BiConsumer<Test<W>,PropertySet> action) {
            this.template = template;
            this.action = action;
        }

        /**
         * Processes a test object against a single test statemenet.
         * <p>
         * Application of the test action is expected to generate a suitable
         * {@link AssertionError}.
         * 
         * @param object
         *               the object under test.
         * @param line
         *               the test statement to attempt to execute.
         * @return {@code true} if there was a match.
         */
        public boolean process(Test<W> object, String line) {
            Optional<Map<String, String>> extracted = matchAgainstTemplate(line);
            if (!extracted.isPresent())
                return false;
            action.accept(object, new PropertySet(extracted.get()));
            return true;
        }

        /**
         * Used by {@link #process(Test, String)} to match a template and extract
         * variables.
         * 
         * @param input
         *              the test statement to match.
         * @return if no match then the optional will be empty, otherwise it will
         *         contain a variable map (which may be empty).
         */
        protected Optional<Map<String, String>> matchAgainstTemplate(String input) {
            String regex = template.replaceAll("\\{(\\w+)\\}", "(?<$1>.+?)");
            Pattern compiledPattern = Pattern.compile("^" + regex + "$", Pattern.CASE_INSENSITIVE);
            Matcher matcher = compiledPattern.matcher(input);

            Map<String, String> result = new HashMap<>();
            if (matcher.matches()) {
                for (String groupName : extractGroupNames(template))
                    result.put(groupName, matcher.group(groupName));
                return Optional.of(result);
            }
            return Optional.empty();
        }

        /**
         * Used by {@link #matchAgainstTemplate(String)}.
         */
        protected List<String> extractGroupNames(String pattern) {
            List<String> names = new ArrayList<>();
            Matcher matcher = Pattern.compile("\\{(\\w+)\\}").matcher(pattern);
            while (matcher.find())
                names.add(matcher.group(1));
            return names;
        }
    }

}
