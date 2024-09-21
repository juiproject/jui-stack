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
package com.effacy.jui.platform.rebind;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.ext.BadPropertyValueException;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.PropertyOracle;
import com.google.gwt.core.ext.SelectionProperty;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JPackage;
import com.google.gwt.dev.resource.Resource;
import com.google.gwt.dev.resource.ResourceOracle;
import com.google.gwt.dev.util.Util;

public class ResourceSupport {

    /**
     * Finds the resources declared against a method.
     * 
     * @param logger
     *                 the tree logger.
     * @param context
     *                 the generator context.
     * @param pkg
     *                 the enclosing package.
     * @param resource
     *                 the resource specifications.
     * @param optional
     *                 {@code true} if is it OK that a resource does not exist.
     * @return List of resources.
     * @throws UnableToCompleteException
     *                                   If there was a problem.
     */
    public static IResource [] findResources(TreeLogger logger, GeneratorContext context, JPackage pkg, String[] resources, boolean optional) throws UnableToCompleteException {
        List<String> lresources = new ArrayList<> ();
        for (String resource : resources)
            lresources.add (resource);
        return findResources (logger, context, pkg, lresources, optional);
    }

    /**
     * Finds the resources declared against a method.
     * 
     * @param logger
     *                 the tree logger.
     * @param context
     *                 the generator context.
     * @param pkg
     *                 the enclosing package.
     * @param resource
     *                 the resource specifications.
     * @param optional
     *                 {@code true} if is it OK that a resource does not exist.
     * @return List of resources.
     * @throws UnableToCompleteException
     *                                   If there was a problem.
     */
    public static IResource [] findResources(TreeLogger logger, GeneratorContext context, JPackage pkg, List<String> resources, boolean optional) throws UnableToCompleteException {
        String locale = getLocale (logger, context);
        List<IResource> urls = new ArrayList<>();
        ILocator [] locators = getDefaultLocators (context);
        for (String resource : resources) {
            IResource resourceRef = null;

            // Attempt to resolve the resource using the locators.
            for (ILocator locator : locators) {
                resourceRef = locator.locate (getPathRelativeToPackage (pkg, resource), locale);
                if (resourceRef == null)
                    resourceRef = locator.locate (resource, locale);
                if (resourceRef != null)
                    break;
            }

            // If there is no matching resource the error.
            if (resourceRef == null) {
                if (!optional) {
                    logger.log (TreeLogger.ERROR, "Resource " + resource + " not found. Is the name specified as Class.getResource() would expect?");
                    throw new UnableToCompleteException ();
                } else
                    logger.log (TreeLogger.WARN, "Resource " + resource + " not found. Declared optional so skipping.");
            } else {
                // Add the resource.
                urls.add (resourceRef);
            }
        }
        return urls.toArray (new IResource[urls.size ()]);
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
     * Encapsulates a resource allowing contents of the resource to be
     * retrieved.a
     */
    public interface IResource {

        /**
         * Obtains the contents of the resource as a string.
         * 
         * @return the contents as a string.
         */
        public String getResourceAsString();

        /**
         * Obtains the contents of the resource as a URL.
         * 
         * @return the resource.
         */
        public URL getResourceAsUrl();
    }

    /**
     * Cast of a resource referenced as a URL.
     */
    public static class URLResource implements IResource {

        /**
         * The URL.
         */
        private URL url;

        /**
         * Construct from a URL.
         * 
         * @param url
         *            the URL.
         */
        public URLResource(URL url) {
            this.url = url;
        }

        @Override
        public String getResourceAsString() {
            return Util.readURLAsString (url);
        }

        @Override
        public URL getResourceAsUrl() {
            return url;
        }

    }


    /**
     * Get default list of resource Locators, in the default order.
     * 
     * @param context
     *            the generator context.
     * @return The (ordered) locators.
     */
    public static ILocator [] getDefaultLocators(GeneratorContext context) {
        ILocator [] locators = {
            new ResourceOracleLocator (context.getResourcesOracle ()), new ClassLoaderLocator (Thread.currentThread ().getContextClassLoader ())
        };
        return locators;
    }

    /**
     * Implements a specific strategy to locate a resource.
     */
    public interface ILocator {

        /**
         * Locates a resource within the given locale.
         * 
         * @param resourceName
         *            the name of the resource.
         * @param locale
         *            the locale.
         * @return The associated resource (or {@code null}).
         */
        public IResource locate(String resourceName, String locale);


        /**
         * Locate a resource in a locale independent manner.
         * 
         * @param resourceName
         *            the name of the resource.
         * @return The associated resource (or {@code null}).
         */
        public IResource locate(String resourceName);
    }

    /**
     * Support class for implementing {@link ILocator}'s. Provides a default
     * implementation of {@link ILocator#locate(String, String)}.
     */
    public static abstract class Locator implements ILocator {

        /**
         * {@inheritDoc}
         * 
         * @see com.effacy.jui.core.rebind.common.rebind.dom.rebind.GeneratorUtils.ILocator#locate(java.lang.String,
         *      java.lang.String)
         */
        public IResource locate(String resourceName, String locale) {
            if (locale == null)
                return locate (resourceName);

            // Convert language_country_variant to independent pieces
            String [] localeSegments = locale.split ("_");
            int lastDot = resourceName.lastIndexOf (".");
            String prefix = lastDot == -1 ? resourceName : resourceName.substring (0, lastDot);
            String extension = lastDot == -1 ? "" : resourceName.substring (lastDot);

            for (int i = localeSegments.length - 1; i >= -1; i--) {
                String localeInsert = "";
                for (int j = 0; j <= i; j++)
                    localeInsert += "_" + localeSegments[j];

                IResource resource = locate (prefix + localeInsert + extension);
                if (resource != null)
                    return resource;

            }

            // Nothing found.
            return null;
        }
    }

    /**
     * Locator based on a classloader.
     */
    public static class ClassLoaderLocator extends Locator {

        /**
         * The class loader.
         */
        private ClassLoader classLoader;

        /**
         * Construct with classloader.
         * 
         * @param classLoader
         *            the classloader to use.
         */
        public ClassLoaderLocator(ClassLoader classLoader) {
            this.classLoader = classLoader;
        }


        /**
         * {@inheritDoc}
         * 
         * @see com.effacy.jui.core.rebind.common.rebind.dom.rebind.GeneratorUtils.ILocator#locate(java.lang.String)
         */
        public IResource locate(String resourceName) {
            URL url = classLoader.getResource (resourceName);
            return (url == null) ? null : new URLResource (url);
        }
    }

    /**
     * Locator based on the resources oracle.
     */
    public static class ResourceOracleLocator extends Locator {

        /**
         * The oracle.
         */
        private ResourceOracle oracle;

        /**
         * Construct with oracle.
         * 
         * @param oracle
         *            the oracle.
         */
        public ResourceOracleLocator(ResourceOracle oracle) {
            this.oracle = oracle;
        }


        /**
         * {@inheritDoc}
         * 
         * @see com.effacy.jui.core.rebind.common.rebind.dom.rebind.GeneratorUtils.ILocator#locate(java.lang.String)
         */
        @SuppressWarnings("deprecation")
        public IResource locate(String resourceName) {
            final Resource r = oracle.getResource (resourceName);
            if (r == null)
                return null;

            // TODO: getURL may be fully deprecated or may not always work. This
            // should be changed to extract the data from the resource directly.
            // Need to consider this if our notion of resource reference expands
            // or when upgrading to a new version of GWT.
            return new URLResource (r.getURL ());
        }
    }
}
