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
package com.effacy.jui.ui.client.control;

import java.util.Collection;
import java.util.function.Consumer;

import com.effacy.jui.core.client.control.IControlValue;
import com.effacy.jui.core.client.store.ListStore;
import com.effacy.jui.platform.util.client.StringSupport;

/**
 * A catalog of the standard controls in one place.
 * <p>
 * Note that one may augment these controls in a specific application by
 * extending this class; for example:
 * <tt>
 * public clas MyApplicationControls extends Controls {
 *   ...
 * }
 * </tt>
 * This way the application needs only employ <code>MyApplicationControls</code>
 * as the source of pre-configured controls.
 */
public class Controls {

    /**
     * Creates and returns a {@link TextControl}.
     * 
     * @param cfg
     *            to configure the control.
     * @return the control.
     */
    public static TextControl text(Consumer<TextControl.Config> cfg) {
        return TextControlCreator.build (cfg);
    }

    /**
     * Creates and returns a {@link TextControl}.
     * 
     * @param cfg
     *            to configure the control.
     * @param ctl
     *            to further configure the control.
     * @return the control.
     */
    public static TextControl text(Consumer<TextControl.Config> cfg, Consumer<TextControl> ctl) {
        return TextControlCreator.build (cfg, ctl);
    }

    /**
     * Creates and returns a {@link TextSearchControl}.
     * 
     * @param cfg
     *            to configure the control.
     * @return the control.
     */
    public static <S> TextSearchControl<S> textsearch(Consumer<TextSearchControl.Config<S>> cfg, @SuppressWarnings("unchecked") S...values) {
        return textsearch(cfg, (Consumer<TextSearchControl<S>>) null, values);
    }

    /**
     * Creates and returns a {@link TextSearchControl}.
     * 
     * @param cfg
     *            to configure the control.
     * @param ctl
     *            to further configure the control.
     * @return the control.
     */
    public static <S> TextSearchControl<S> textsearch(Consumer<TextSearchControl.Config<S>> cfg, Consumer<TextSearchControl<S>> ctl, @SuppressWarnings("unchecked") S...values) {
        return TextSearchControlCreator.build (config -> {
            if (cfg != null)
                cfg.accept(config);
            if ((values != null) && (values.length > 0))
                config.store (new ListStore<S>().add (values));
        }, ctl);
    }

    /**
     * Creates and returns a {@link TextAreaControl}.
     * 
     * @param cfg
     *            to configure the control.
     * @return the control.
     */
    public static TextAreaControl textarea(Consumer<TextAreaControl.Config> cfg) {
        return TextAreaControlCreator.build (cfg);
    }

    /**
     * Creates and returns a {@link TextAreaControl}.
     * 
     * @param cfg
     *            to configure the control.
     * @param ctl
     *            to further configure the control.
     * @return the control.
     */
    public static TextAreaControl textarea(Consumer<TextAreaControl.Config> cfg, Consumer<TextAreaControl> ctl) {
        return TextAreaControlCreator.build (cfg, ctl);
    }

    /**
     * Creates and returns a {@link NumberControl}.
     * 
     * @param cfg
     *            to configure the control.
     * @return the control.
     */
    public static NumberControl number(Consumer<NumberControl.Config> cfg) {
        return NumberControlCreator.build (cfg);
    }

    /**
     * Creates and returns a {@link NumberControl}.
     * 
     * @param cfg
     *            to configure the control.
     * @param ctl
     *            to further configure the control.
     * @return the control.
     */
    public static NumberControl number(Consumer<NumberControl.Config> cfg, Consumer<NumberControl> ctl) {
        return NumberControlCreator.build (cfg, ctl);
    }

    /**
     * Creates and returns a {@link SelectionControl}.
     * 
     * @param cfg
     *               to configure the control.
     * @param values
     *               (optional) if present then will create a selection over a fixed
     *               list of these values.
     * @return the control.
     */
    public static <V> SelectionControl<V> selector(Consumer<SelectionControl.Config<V>> cfg, @SuppressWarnings("unchecked") V...values) {
        return selector(cfg, null, values);
    }
    
    /**
     * Creates and returns a {@link SelectionControl}.
     * 
     * @param cfg
     *               to configure the control.
     * @param ctl
     *               to further configure the control.
     * @param values
     *               (optional) if present then will create a selection over a fixed
     *               list of these values.
     * @return the control.
     */
    public static <V> SelectionControl<V> selector(Consumer<SelectionControl.Config<V>> cfg, Consumer<SelectionControl<V>> ctl, @SuppressWarnings("unchecked") V...values) {
        if ((values != null) && (values.length > 0)) {
            SelectionControl.Config<V> config = new SelectionControl.Config<V> ();
            config.store (new ListStore<V>().add (values));
            if (cfg != null)
                cfg.accept (config);
            return config.build ();
        }
        return SelectionControlCreator.build (cfg, ctl);
    }


    /**
     * Creates and returns a {@link MultiSelectionControl}.
     * 
     * @param cfg
     *               to configure the control.
     * @param values
     *               (optional) if present then will create a selection over a fixed
     *               list of these values.
     * @return the control.
     */
    public static <V> MultiSelectionControl<V> multiselector(Consumer<MultiSelectionControl.Config<V>> cfg, @SuppressWarnings("unchecked") V...values) {
        return multiselector(cfg, null, values);
    }
    
    /**
     * Creates and returns a {@link MultiSelectionControl}.
     * 
     * @param cfg
     *               to configure the control.
     * @param ctl
     *               to further configure the control.
     * @param values
     *               (optional) if present then will create a selection over a fixed
     *               list of these values.
     * @return the control.
     */
    public static <V> MultiSelectionControl<V> multiselector(Consumer<MultiSelectionControl.Config<V>> cfg, Consumer<MultiSelectionControl<V>> ctl, @SuppressWarnings("unchecked") V...values) {
        if ((values != null) && (values.length > 0)) {
            MultiSelectionControl.Config<V> config = new MultiSelectionControl.Config<V> ();
            config.store (new ListStore<V>().add (values));
            if (cfg != null)
                cfg.accept (config);
            return config.build ();
        }
        return MultiSelectionControlCreator.build (cfg, ctl);
    }

    /**
     * Creates and returns a {@link CheckControl}.
     * 
     * @param cfg
     *            to configure the control.
     * @return the control.
     */
    public static CheckControl check(Consumer<CheckControl.Config> cfg) {
        return check (cfg, null);
    }

    /**
     * Creates and returns a {@link CheckControl}.
     * 
     * @param cfg
     *            to configure the control.
     * @param ctl
     *            to act on the control.
     * @return the control.
     */
    public static CheckControl check(Consumer<CheckControl.Config> cfg, Consumer<CheckControl> ctl) {
        CheckControl.Config config = new CheckControl.Config ();
        if (cfg != null)
            cfg.accept (config);
        CheckControl control = config.build ();
        if (ctl != null)
            ctl.accept (control);
        return control;
    }

    /**
     * Creates and returns a {@link MultiCheckControl}.
     * 
     * @param cfg
     *            to configure the control.
     * @return the control.
     */
    public static <V> MultiCheckControl<V> checkMulti(Consumer<MultiCheckControl.Config<V>> cfg) {
        return checkMulti (cfg, null);
    }

    /**
     * Creates and returns a {@link MultiCheckControl}.
     * 
     * @param cfg
     *            to configure the control.
     * @param ctl
     *            to act on the control.
     * @return the control.
     */
    public static <V> MultiCheckControl<V> checkMulti(Consumer<MultiCheckControl.Config<V>> cfg, Consumer<MultiCheckControl<V>> ctl) {
        MultiCheckControl.Config<V> config = new MultiCheckControl.Config<V> ();
        if (cfg != null)
            cfg.accept (config);
        MultiCheckControl<V> control = config.build ();
        if (ctl != null)
            ctl.accept (control);
        return control;
    }

    /**
     * Creates and returns a {@link SelectionGroupControl} configured for checkbox
     * selection.
     * 
     * @param cfg
     *            to configure the control.
     * @return the control.
     */
    public static <V> SelectionGroupControl<V> checkGroup(Consumer<SelectionGroupControl.Config<V>> cfg) {
        return checkGroup (cfg, null);
    }

    /**
     * Creates and returns a {@link SelectionGroupControl} configured for checkbox
     * selection.
     * 
     * @param cfg
     *            to configure the control.
     * @param ctl
     *            to act on the control.
     * @return the control.
     */
    public static <V> SelectionGroupControl<V> checkGroup(Consumer<SelectionGroupControl.Config<V>> cfg, Consumer<SelectionGroupControl<V>> ctl) {
        SelectionGroupControl.Config<V> config = new SelectionGroupControl.Config<V> ();
        if (cfg != null)
            cfg.accept (config);
        SelectionGroupControl<V> control = config.build ();
        if (ctl != null)
            ctl.accept (control);
        return control;
    }

    /**
     * Creates and returns a {@link SelectionGroupControl} configured for radio
     * selection.
     * 
     * @param cfg
     *            to configure the control.
     * @return the control.
     */
    public static <V> SelectionGroupControl<V> radioGroup(Consumer<SelectionGroupControl.Config<V>> cfg) {
        return radioGroup (cfg, null);
    }


    /**
     * Creates and returns a {@link SelectionGroupControl} configured for radio
     * selection.
     * 
     * @param cfg
     *            to configure the control.
     * @param ctl
     *            to act on the control.
     * @return the control.
     */
    public static <V> SelectionGroupControl<V> radioGroup(Consumer<SelectionGroupControl.Config<V>> cfg, Consumer<SelectionGroupControl<V>> ctl) {
        SelectionGroupControl.Config<V> config = new SelectionGroupControl.Config<V> ();
        config.radio ();
        if (cfg != null)
            cfg.accept (config);
        SelectionGroupControl<V> control = config.build ();
        if (ctl != null)
            ctl.accept (control);
        return control;
    }

    /**
     * Creates and returns a {@link FileUploadControl}.
     * 
     * @param cfg
     *            to configure the control.
     * @return the control.
     */
    public static FileUploadControl fileUpload(Consumer<FileUploadControl.Config> cfg) {
        return fileUpload (cfg, null);
    }

    /**
     * Creates and returns a {@link FileUploadControl}.
     * 
     * @param cfg
     *            to configure the control.
     * @param ctl
     *            to act on the control.
     * @return the control.
     */
    public static FileUploadControl fileUpload(Consumer<FileUploadControl.Config> cfg, Consumer<FileUploadControl> ctl) {
        FileUploadControl.Config config = new FileUploadControl.Config ();
        if (cfg != null)
            cfg.accept (config);
        FileUploadControl control = config.build ();
        if (ctl != null)
            ctl.accept (control);
        return control;
    }

    /************************************************************************
     * Utilities for dealing with values.
     ************************************************************************/

    /**
     * Safely retrieves a value.
     * 
     * @param <V>          the value type.
     * @param value
     *                     the value that has been retrieved.
     * @param defaultValue
     *                     the default value if the value is {@code null}.
     * @return the safe value.
     */
    public static <V> V safe(V value, V defaultValue) {
        return (value == null) ? defaultValue : value;
    }

    /**
     * Support method for testing emptiness.
     * <p>
     * If the passed value is of type {@link IControlValue} then it will be
     * unwrapped and the unwrapped value tested.
     * 
     * @param value
     *              the value to test.
     * @return {@code true} if is empty.
     */
    public static <V> boolean empty(V value) {
        if (value == null)
            return true;
        if (value instanceof IControlValue)
            return empty (((IControlValue<?>) value).value ());
        if (value instanceof String)
            return StringSupport.empty(((String) value));
        if (value instanceof Collection)
            return ((Collection<?>) value).isEmpty();
        return false;
    }
}
