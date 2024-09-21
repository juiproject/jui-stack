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
package com.effacy.jui.core.client.component;

import java.util.ArrayList;
import java.util.List;

import com.effacy.jui.core.client.IActivateAware;
import com.effacy.jui.core.client.dom.DomSupport;
import com.effacy.jui.core.client.dom.IDomSelector;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.dom.css.Position;
import com.effacy.jui.core.client.navigation.INavigationAware;
import com.effacy.jui.core.client.navigation.INavigationHandler.NavigationContext;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.platform.util.client.TimerSupport;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;

import elemental2.dom.Element;
import jsinterop.base.Js;

/**
 * Base class to implement deferred component loading. This should be used in
 * cases where a component is added to region or attachment point but will be
 * created only when it is needed (that is, when the component is rendered).
 * <p>
 * A typical scenario is when one wants to define a split point for code
 * splitting. The GWT compilier will split around the use of
 * {@link GWT#runAsync(com.google.gwt.core.client.RunAsyncCallback)} and so a
 * sub-class should implement {@link #load(IDeferredComponentCallback)} using
 * this construct. The {@link IDeferredComponentCallback} interface is designed
 * to be aligned with {@link RunAsyncCallback} and so one simply delegates
 * through. Note that we cannot embed the mechanism in the class as the GWT
 * compiler will then only create one split point and bundle all the components
 * into that split (as the splits are determined by class at compile time).
 * <p>
 * Note that care should be taken when using this as sizing the component will
 * not result in the sizing being passed through to the deferred component.
 * However, when the component is shown a show is delegated through to the
 * wrapped component and so it may make use of the
 * {@link AbstractBaseComponent#onShow()} method.
 * 
 * @author Jeremy Buckley
 */
public abstract class DeferredComponent<C extends IComponent> extends Component<Component.Config> {

    /**
     * Callback passed to the override of the
     * {@link DeferredComponent#load(IDeferredComponentCallback)} method.
     */
    public interface IDeferredComponentCallback<CPT> {

        /**
         * If the loading failed.
         */
        public void onFailure();

        /**
         * If the loading was successful and the component being deferred was created.
         * 
         * @param component
         *                  the component that is proxied by the deferred component.
         */
        public void onSuccess(CPT component);
    }

    /**
     * Used to lodge a job that should run once the component loads.
     */
    public interface IDeferredRunner {

        /**
         * Execute the runner with the component that the deferred component wraps.
         * 
         * @param component
         *                  the component.
         */
        public void run(IComponent component);
    }

    /**
     * Enumeration of loading status.
     */
    public enum Status {
        PENDING, LOADING, LOADED, FAILED;
    }

    /**
     * The component that was deferred.
     */
    private C component;

    /**
     * If the component should be shown on loading.
     */
    private boolean showOnLoad;

    /**
     * The loading status.
     */
    private Status status = Status.PENDING;

    /**
     * The maximum number of retries to load.
     */
    private int maxRetries;

    /**
     * The minimum height of the loading area (where the spinner and error messages
     * are displayed).
     */
    private int minHeight;

    /**
     * List of runnables to execute when the component has loaded.
     */
    private List<IDeferredRunner> runOnLoad;

    /**
     * The default maximum retries (<code>1</code>).
     */
    public static final int DEFAULT_MAX_RETRIES = 1;

    /**
     * The default minimum height (<code>400</code>).
     */
    public static final int DEFAULT_MIN_HEIGHT = 400;

    /**
     * Construct with defaults (see {@link #DEFAULT_MIN_HEIGHT} and
     * {@link #DEFAULT_MAX_RETRIES}).
     */
    public DeferredComponent() {
        this (DEFAULT_MIN_HEIGHT, DEFAULT_MAX_RETRIES);
    }

    /**
     * Construct with the given minimum height and the default maximum retries (see
     * {@link #DEFAULT_MAX_RETRIES}).
     * 
     * @param minHeight
     *                  the minimum height (in pixels) of the loading spinner area.
     */
    public DeferredComponent(int minHeight) {
        this (minHeight, DEFAULT_MAX_RETRIES);
    }

    /**
     * Construct with the given number of maximum retries and minimum height.
     * 
     * @param minHeight
     *                   the minimum height (in pixels) of the loading spinner area.
     * @param maxRetries
     *                   the maximum number of retries (0 will attempt to load
     *                   once).
     */
    public DeferredComponent(int minHeight, int maxRetries) {
        this.minHeight = Math.max (0, minHeight);
        this.maxRetries = Math.max (0, maxRetries);
    }

    /**
     * Assigns a minimum height.
     * 
     * @param minHeight
     *                  the minimum height (in pixels) of the loading spinner area.
     * @return this instance.
     */
    public DeferredComponent<C> setMinHeight(int minHeight) {
        this.minHeight = minHeight;
        return this;
    }

    /**
     * Determines if the component has loaded.
     * 
     * @return {@code true} if it has.
     */
    public boolean isLoaded() {
        return Status.LOADED.equals (status);
    }

    /**
     * Run the following when the component has loaded. If it already has loaded it
     * will be run immediately.
     * 
     * @param runnable
     *                 the runnable to run.
     */
    public void run(IDeferredRunner runnable) {
        if (runnable == null)
            return;
        if (isLoaded ()) {
            runnable.run (component ());
        } else {
            if (runOnLoad == null)
                runOnLoad = new ArrayList<IDeferredRunner> ();
            runOnLoad.add (runnable);
        }
    }

    /**
     * Called to load the component. This needs to be overridden by the sub-class
     * (generally an anonymous inner class) and should use
     * {@link GWT#runAsync(com.google.gwt.core.client.RunAsyncCallback)} to ensure
     * that code splitting is appropriately applied.
     * 
     * @param cb
     *           the callback to indicate failure or success (with the component).
     */
    protected abstract void load(IDeferredComponentCallback<C> cb);

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#onRender(elemental2.dom.Element)
     */
    @Override
    protected IDomSelector onRender(Element el) {
        CSS.POSITION.apply (el, Position.RELATIVE);
        el.appendChild (createLoadingSpinner ());
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.AbstractBaseComponent#onAfterRender()
     */
    @Override
    protected void onAfterRender() {
        if (Status.PENDING.equals (status))
            load (maxRetries);
        super.onAfterRender ();
    }

    /**
     * Implements loading with the given retries remaining.
     * 
     * @param retryCount
     *                   the number of retries remaining.
     */
    private void load(final int remainingRetries) {
        if (!Status.LOADING.equals (status)) {
            status = Status.LOADING;
            loadImpl (remainingRetries);
        }
    }

    /**
     * Implements loading with the given retries remaining.
     * 
     * @param retryCount
     *                   the number of retries remaining.
     */
    private void loadImpl(final int remainingRetries) {
        if (remainingRetries < 0) {
            onFailure ();
        } else {
            try {
                load (new IDeferredComponentCallback<C> () {

                    @Override
                    public void onFailure() {
                        if (remainingRetries < 0) {
                            onFailure ();
                        } else {
                            TimerSupport.timer (() -> loadImpl (remainingRetries - 1), 100);
                        }
                    }

                    @Override
                    public void onSuccess(C component) {
                        DeferredComponent.this.onSuccess (component);
                    }
                });
            } catch (Throwable e) {
                Logger.reportUncaughtException (e);
                onFailure ();
            }
        }
    }

    /**
     * Called on failure to load.
     */
    protected void onFailure() {
        status = Status.FAILED;
        DomSupport.removeAllChildren (getRoot ());
        getRoot ().appendChild (createErrorNotification ());
    }

    /**
     * Called on a succesful load and creation of the component. The component will
     * be registered.
     * 
     * @param component
     *                  the component.
     */
    protected void onSuccess(C component) {
        this.status = Status.LOADED;
        this.component = component;

        DomSupport.removeAllChildren (getRoot ());
        if (component != null) {
            registerComponent (component, getRoot ());
            if (showOnLoad) {
                component.show ();
            }
            Logger.log ("HERE: " + component.getRoot ());
            CSS.POSITION.apply ((elemental2.dom.Element) Js.cast (component.getRoot ()), Position.RELATIVE);
            CSS.INSETS.apply ((elemental2.dom.Element) Js.cast (component.getRoot ()), Length.px (0));
        }
        if (runOnLoad != null) {
            for (IDeferredRunner runnable : runOnLoad) {
                try {
                    runnable.run (this.component);
                } catch (Throwable e) {
                    Logger.reportUncaughtException (e);
                }
            }
        }
        if (isRendered ()) {
            if (component () instanceof INavigationAware)
                ((INavigationAware) component ()).onNavigateTo (new NavigationContext (NavigationContext.Source.INTERNAL, false));
            else if (component () instanceof IActivateAware)
                ((IActivateAware) component ()).onActivated ();
        }
    }

    /**
     * The (loaded) component.
     * 
     * @return the component.
     */
    public C component() {
        return component;
    }

    /**
     * The loading status.
     * 
     * @return the status.
     */
    public Status status() {
        return status;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.AbstractBaseComponent#onShow()
     */
    @Override
    protected void onShow() {
        super.onShow ();
        if (Status.LOADED.equals (status)) {
            if (component != null)
                component.show ();
        } else {
            showOnLoad = true;
            load (maxRetries);
        }
    }

    /**
     * Creates the error notification on failure to load.
     * 
     * @return the error notification element (to be added to the root element).
     */
    protected Element createErrorNotification() {
        Element wrapperEl = DomSupport.createDiv ();
        Element spinnerEl = DomSupport.createDiv ();
        spinnerEl.innerHTML = "Possible network error loading component";
        wrapperEl.appendChild (spinnerEl);
        return wrapperEl;
    }

    /**
     * Creates the loading spinner that is displayed while the rendering is
     * occurring.
     * 
     * @return the loading spinner element (to be added to the root element).
     */
    protected Element createLoadingSpinner() {
        Element wrapperEl = DomSupport.createDiv ();
        Element spinnerEl = DomSupport.createDiv ();
        //spinnerEl.classList.add (FontAwesome.circleNotch (FontAwesome.Option.SPINNING));
        wrapperEl.appendChild (spinnerEl);
        return wrapperEl;
    }
}
