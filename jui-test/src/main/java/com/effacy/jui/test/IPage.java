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
package com.effacy.jui.test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Represents a page of DOM content.
 */
public interface IPage {

    /**
     * Represents a (DOM) node in a page with the various actions that can be
     * performed on it.
     */
    public interface INode {

        /**
         * The (tag) name of the node.
         * 
         * @return the name.
         */
        public String nodeName();
        
        /**
         * Performs a (mouse) click on the action.
         * 
         * @throws Exception on error.
         */
        public void click() throws Exception;

        /**
         * Assigns a (generally input) value to the node (where this is supported).
         * 
         * @param value the value to assign.
         * @return {@code true} if the assignment was successful (i.e. the node supports
         *         assignment).
         */
        public boolean assignValue(String value);

        /**
         * Obtains the (generally input) value from the node (where this is supported).
         * 
         * @return the value (or {@code null} if not supported).
         */
        public String value();

        /**
         * Obtains the named attribute on the node.
         * 
         * @param name the name of the attribute.
         * @return the value of the attribute.
         */
        public String attribute(String name);

        /**
         * Obtains the text content of the node.
         * 
         * @return the text content.
         */
        public String textContent();

        /**
         * Selections nodes by a XPath. Note that for HtmlUnit "//" will be treated as
         * global so use ".//" to search localised to the node.
         * 
         * @param xPath the x-path.
         * @return the matching nodes.
         */
        public List<INode> selectByXPath(String xPath);

        /**
         * See {@link #selectByRef(String, boolean)} but with no boundary check.
         */
        default public INode selectByRef(String testRef) {
            return selectByRef(testRef, false);
        }

        /**
         * Finds an element under this element by its {@code test-ref}.
         * <p>
         * One may bound this check to the current component where component boundaries
         * are delineated by the existence of the {@code test-cpt} attribute. This means
         * that the node path from this node to the matching node cannot transition
         * across a component boundary. This is relevant for those components that
         * include child components such that the child components could include the
         * same reference and by imposing the boundary check ensures that we don't pick
         * up the wrong reference. It is a more expensive check so should be used only
         * when the risk of crossing boundaries is problematic.
         * 
         * @param testRef
         *                the ref to search for.
         * @param bounded
         *                {@code true} if this search should be bounded to the current
         *                component.
         * @return first matching node.
         */
        public INode selectByRef(String testRef, boolean bounded);

        /**
         * Obtains the parent node.
         * 
         * @return the parent node (or {@code null} if there is none).
         */
        public INode parent();

        /**
         * Finds the first matching immediate child.
         * 
         * @param selector the selector for the child.
         * @return the matching child.
         */
        public Optional<INode> matchChild(Predicate<INode> selector);

        /**
         * Unwraps the node to the underlying implementation. For example, if HtmlUnit
         * is used then this will return the underlying {@link DomElement}.
         * 
         * @return the underlying object that represents the node.
         */
        public <T> T unwrap();

        /**
         * Prints the node for debugging.
         */
        public void print();
    }

    /**
     * Resolves the passed test ID to the ID that will be looked up (for reference).
     * <p>
     * If the passed test ID is empty (or {@code null}) the only the scoping test ID
     * will be returned (and if that is {@code null} then the empty string is
     * returned). This way you can always resolve the scoping ID by passing
     * {@code null}.
     * 
     * @param testId the test ID.
     * @return the fully resolved test ID.
     */
    public String resolveTestId(String testId);

    /**
     * Selects a node using the {@code test-id}.
     * 
     * @param testId the test ID.
     * @return the associated node (or {@code null} if there is no match).
     */
    public INode selectById(String testId);

    /**
     * Prints the page to {@code stdout}.
     */
    default public void print() throws IOException {
        print (null);
    }

    /**
     * Prints the page to the passed file.
     * 
     * @param file the file to print the page (if {@code null} then should be
     *             {@code stdout}).
     */
    public void print(File file) throws IOException;

    /**
     * Obtains the root node of the page.
     * 
     * @return the node.
     */
    public INode root();

    /**
     * Creates a page instance for which all test ID's are prefixed by the given
     * string (i.e. see {@link #select(String)}).
     * 
     * @param testIdPrefix the prefix (no period at the end is expected, this will
     *                     be added).
     * @return the page instance.
     */
    public IPage wrap(String testIdPrefix);

    /**
     * Sleep for the given period of time.
     * 
     * @param millis
     *               the time in ms.
     * @return this page instance.
     */
    public IPage sleep(int millis);

}
