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
package com.effacy.jui.rpc.extdirect.annotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.effacy.jui.rpc.extdirect.IActionProvider;
import com.effacy.jui.rpc.extdirect.Router;

/**
 * A router that supports annotated beans and action handlers. It also supports
 * annotations for provider properties (see {@link RemoteProperties} and
 * {@link RemoteProperty}).
 * 
 * @author Jeremy Buckley
 */
public class AnnotatedRouter extends Router {

    /**
     * Default constructor. This will set the remote config (see
     * {@link #setRemotingConfig(Map)}) with the map returned by
     * {@link #getProviderPropeties()}). Nominally this map will be populated
     * from the {@link RemoteProperties} and {@link RemoteProperty} annotations
     * on the router.
     */
    protected AnnotatedRouter() {
        this.setRemotingConfig (getProviderPropeties ());
    }


    /**
     * Gets all the annotated properties on this router. Sub-classes may
     * override and contribute to this.
     * 
     * @return The properties for the provider.
     */
    protected Map<String, String> getProviderPropeties() {
        Map<String, String> properties = new HashMap<String, String> ();
        contributeProviderProperties (properties, this.getClass ());
        return properties;
    }


    /**
     * Collects the annotated properties from the given class, then recursively
     * applies to the super class until we reach this class. Ensures that the
     * full class hierarchy is processed for annotations.
     * 
     * @param properties
     *            the properties to contribute to.
     * @param klass
     *            the class to process.
     */
    private void contributeProviderProperties(Map<String, String> properties, Class<?> klass) {
        // Collect the properties for the given class.
        List<RemoteProperty> remoteProperties = new ArrayList<RemoteProperty> ();
        RemoteProperty annotatedProperty = klass.getAnnotation (RemoteProperty.class);
        if (annotatedProperty != null)
            remoteProperties.add (annotatedProperty);
        RemoteProperties annotatedProperties = klass.getAnnotation (RemoteProperties.class);
        if (annotatedProperties != null) {
            for (RemoteProperty property : annotatedProperties.value ())
                remoteProperties.add (property);
        }
        if (!remoteProperties.isEmpty ()) {
            for (RemoteProperty property : remoteProperties)
                properties.put (property.name (), property.value ());
        }

        // Crawl up to the super class and repeat until we reach this class.
        klass = klass.getSuperclass ();
        if ((klass != null) && !klass.equals (Object.class) && !klass.equals (AnnotatedRouter.class))
            contributeProviderProperties (properties, klass);
    }


    /**
     * Adds a single bean as a handler. Note that the handler is only actually
     * added if there are methods exposed on it.
     * 
     * @param bean
     *            the bean to add.
     */
    public void addAction(Object bean) {
        AnnotatedActionHandler handler = new AnnotatedActionHandler (bean);
        if (!handler.getMethodMetadata ().isEmpty ())
            addAction (handler);
    }


    /**
     * Sets a single action.
     * 
     * @param bean
     *            the bean (annotated).
     */
    public void setAction(Object bean) {
        addAction (bean);
    }


    /**
     * Adds a list of action beans (annotated).
     * 
     * @param beans
     *            the beans to add.
     */
    public void setActions(List<Object> beans) {
        for (Object bean : beans)
            addAction (bean);
    }


    /**
     * Sets actions through a provider.
     * 
     * @param provider
     *            the provider.
     */
    public void setActions(IActionProvider provider) {
        for (Object bean : provider.getActions ())
            addAction (bean);
    }
}
