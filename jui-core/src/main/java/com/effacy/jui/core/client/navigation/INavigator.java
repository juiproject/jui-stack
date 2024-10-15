package com.effacy.jui.core.client.navigation;

import com.effacy.jui.core.client.navigation.INavigationHandler.NavigationContext;

/**
 * This declares a minimal navigation mechanism. Generally this is used to pass
 * a navigator through to something that only needs to navigate (and need not
 * have access to a richer navigation mechanism).
 */
public interface INavigator {

    /**
     * Navigate to the given (relative) path using the given context.
     * 
     * @param context
     *                the navigation context (can be {@code null} in which case a
     *                default used).
     * @param path
     *                the path to navigate to.
     */
    public void navigate(NavigationContext context, String path);

    /**
     * See {@link #navigate(NavigationContext, String)} but to use the default
     * navigation context.
     */
    default public void navigate(String path) {
        navigate(null, path);
    }

}
