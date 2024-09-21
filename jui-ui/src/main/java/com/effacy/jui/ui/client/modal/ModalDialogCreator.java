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
package com.effacy.jui.ui.client.modal;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.effacy.jui.core.client.IEditable;
import com.effacy.jui.core.client.IProcessable;
import com.effacy.jui.core.client.IResetable;
import com.effacy.jui.core.client.IResolver;
import com.effacy.jui.core.client.IUpdateListener;
import com.effacy.jui.core.client.component.ComponentCreator;
import com.effacy.jui.core.client.component.IComponent;
import com.effacy.jui.core.client.component.IValueChangeListener;
import com.effacy.jui.core.client.dom.builder.ExistingElementBuilder;
import com.effacy.jui.platform.util.client.Apply;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.ui.client.modal.Modal.IModalListener;

/**
 * Support methods for building modal dialogs.
 *
 * @author Jeremy Buckley
 */
public class ModalDialogCreator {

    /**
     * Convenience to create a config instance.
     * 
     * @param <C>
     *            the component type.
     * @return the config instance.
     */
    public static <C extends IComponent> ModalDialog.Config<C> create() {
        return new ModalDialog.Config<C> ();
    }

    /**
     * @deprecated use {@link #build(IComponent, Consumer)}.
     */
    public static <C extends IComponent> ModalDialog<C> create(C cpt, Consumer<ModalDialog.Config<C>> cfg) {
        return build (cpt, cfg);
    }

    /**
     * Convenience to wrap a component in a dialog.
     * 
     * @param cpt
     *            the component to wrap.
     * @param cfg
     *            to configure the dialog.
     * @return the modal (ready to open).
     */
    public static <C extends IComponent> ModalDialog<C> build(C cpt, Consumer<ModalDialog.Config<C>> cfg) {
        ModalDialog.Config<C> config = new ModalDialog.Config<C> ();
        if (cfg != null)
            cfg.accept (config);
        return new ModalDialog<C> (config, cpt);
    }

    /**
     * Convenience to wrap a component in a dialog.
     * 
     * @param cpt
     *            supplies the component to wrap.
     * @param cfg
     *            to configure the dialog.
     * @return the modal (ready to open).
     */
    public static <C extends IComponent> ModalDialog<C> build(Supplier<C> cpt, Consumer<ModalDialog.Config<C>> cfg) {
        return build (cpt.get (), cfg);
    }

    /**
     * Convenience to wrap a component in a dialog.
     * 
     * @param cfg
     *                to configure the dialog.
     * @param builder
     *                to build the contents.
     * @return the modal (ready to open).
     */
    public static ModalDialog<IComponent> buildWithRender(Consumer<ModalDialog.Config<IComponent>> cfg, Consumer<ExistingElementBuilder> builder) {
        return build (ComponentCreator.build (builder), cfg);
    }

    /**
     * Represents a mechanism to open a dialog.
     * <p>
     * This assumes that some data is passed through to the dialog (V1) and some
     * data is returned by the dialog (V2).
     */
    public interface IDialogOpener<V1, V2> {

        /**
         * Opens the dialog (if open already then nothing will happen).
         * 
         * @param value
         *              (optional) the value to edit or manage.
         * @param cb
         *              the response when the dialog is closed (empty will be passed if
         *              cancelled).
         */
        public void open(V1 value, Consumer<Optional<V2>> cb);

        /**
         * Obtains the underlying dialog.
         * 
         * @return the dialog.
         */
        public ModalDialog<?> dialog();

        /**
         * Adds a listener to the opener.
         * 
         * @param listener
         *                 the listener to add.
         * @return this instance.
         */
        public IDialogOpener<V1, V2> listener(Consumer<ModalDialog<?>> listener);

        /**
         * See {@link #listener(Consumer)}. Provides an alternative name.
         */
        default IDialogOpener<V1, V2> onOpen(Consumer<ModalDialog<?>> listener) {
            return listener (listener);
        }

    }

    /**
     * See {@link #dialog(IComponent, Consumer, Consumer, Consumer)}. Passed are the
     * labels for cancel and apply (if {@code null} then the buttons are not
     * created).
     */
    public static <V1, V2, C extends IComponent> IDialogOpener<V1, V2> dialog(C cpt, Consumer<ModalDialog.Config<C>> config, String cancelLabel, String applyLabel) {
        Consumer<ModalDialog.Config<C>.Action> cancelAction = null;
        if (cancelLabel != null)
            cancelAction = a -> a.label (cancelLabel);
        Consumer<ModalDialog.Config<C>.Action> applyAction = null;
        if (applyLabel != null)
            applyAction = a -> a.label (applyLabel);
        return dialog (cpt, config, cancelAction, applyAction);
    }


    /**
     * Creates a standard dialog that contains a component (generally a form of some
     * sort) with a cancel button and action button.
     * <p>
     * When the dialog is opened it is passed an opening value and a callback. If
     * the component implements {@link IEditable} then it's
     * {@link IEditable#edit(Object)} method will be called. However, if an applier
     * has been passed then that will be used instead. In all cases, should the
     * component implement {@link IResetable} then its {@link IResetable#reset()}
     * method will be invoked.
     * <p>
     * When the action button is clicked the component is processed. This can be
     * embodied in the component (so it should implement {@link IProcessable}) or
     * view the assignment of a processor during building (i.e provide a
     * {@link builder} and within that assign the processor using
     * {@link IDialogBuilder#processor(BiConsumer)}). If the processing affirms a
     * non-{@code null} value the dialog will be closed.
     * <p>
     * If no action button is configured (the {@code applyAction} is {@code null})
     * then the component must perform its own update. It may close the dialog
     * directly however that will be invoke any callback. The preferred method
     * (then) is to emit a {@link IValueChangeListener#onValueChanged(Object)}
     * event. This will be picked up by the dialog and passed through to the
     * callback (even if it is {@code null}). The dialog will be closed.
     * 
     * @param <V1>
     *                     the pass in value type.
     * @param <V2>
     *                     the return value type.
     * @param <C>
     *                     the component type.
     * @param cpt
     *                     the component.
     * @param config
     *                     to configure the model.
     * @param cancelAction
     *                     to configure the cancel action (must supply a label, if
     *                     {@code null} then no cancel action will be displayed).
     * @param applyAction
     *                     to configure the apply action.
     * @return an opener associated with the dialog.
     */
    public static <V1, V2, C extends IComponent> IDialogOpener<V1, V2> dialog(C cpt, Consumer<ModalDialog.Config<C>> config, Consumer<ModalDialog.Config<C>.Action> cancelAction, Consumer<ModalDialog.Config<C>.Action> applyAction) {
        return dialog (cpt, config, btns -> {
            if (cancelAction != null) {
                btns.cancel (action -> {
                    cancelAction.accept (action);
                });
            }
            if (applyAction != null) {
                btns.button ((cb, action) -> {
                    action.testId ("apply");
                    applyAction.accept (action);
                    action.defaultHandler (cb);
                });
            }
        });
    }

    /**
     * Used by
     * {@link ModalDialogCreator#dialog(IComponent, Consumer, Consumer, Consumer)}
     * to provide for the declaration of actions.
     */
    public interface IActionConfiguration<C extends IComponent,V2> {
        
        /**
         * Creates a cancel action.
         * 
         * @param configurer
         *                   to configure the action.
         */
        public void cancel(Consumer<ModalDialog.Config<C>.Action> configurer);

        /**
         * See {@link #cancel(Consumer)} but applies the default configuration.
         */
        default void cancel() {
            cancel ((Consumer<ModalDialog.Config<C>.Action>) null);
        }

        /**
         * See {@link #cancel(Consumer)} but applies the past label.
         * 
         * @param label
         *              the label to use.
         */
        default void cancel(String label) {
            cancel (cfg -> {
                if (label != null)
                    cfg.label (label);
            });
        }

        /**
         * Create an action.
         * <p>
         * The configurer is passed both a callback
         * 
         * @param configurer
         *                   to configure the action.
         */
        public void button(BiConsumer<Consumer<Optional<V2>>,ModalDialog.Config<C>.Action> configurer);
    }

    public static <V1, V2, C extends IComponent> IDialogOpener<V1, V2> dialog(C cpt, Consumer<ModalDialog.Config<C>> config, Consumer<IActionConfiguration<C,V2>> actions) {
        return new IDialogOpener<V1, V2> () {

            /**
             * The callback on completion.
             */
            private Consumer<Optional<V2>> cb;

            /**
             * Open listeners.
             */
            private List<Consumer<ModalDialog<?>>> listeners;


            @Override
            public IDialogOpener<V1, V2> listener(Consumer<ModalDialog<?>> listener) {
                if (listener == null)
                    return this;
                if (listeners == null)
                    listeners = new ArrayList<> ();
                listeners.add (listener);
                return this;
            }

            @SuppressWarnings("unchecked")
            private ModalDialog<C> modal = Apply.$ (ModalDialogCreator.create (cpt, cfg -> {
                if (config != null)
                    config.accept (cfg);

                // On a close action pass through an empty result.
                cfg.closeAction (() -> {
                    if (cb != null)
                        cb.accept (Optional.empty ());
                });

                // Process variaous actions.
                if (actions != null) {
                    actions.accept (new IActionConfiguration<C,V2> () {
                        public void cancel(Consumer<ModalDialog.Config<C>.Action> configurer) {
                            cfg.action (c -> {
                                // Assign the label here and allow the configurer to override.
                                c.label ("cancel");
                                // Assign link here and allow the configurer to override (for example, an
                                // informational dialog).
                                c.link ();
                                if (configurer != null)
                                    configurer.accept (c);
                                c.testId ("cancel");
                                c.reference ("cancel");
                                c.handler (a -> {
                                    if (cb != null)
                                        cb.accept (Optional.empty ());
                                    a.success ();
                                });
                            });
                        }
                        public void button(BiConsumer<Consumer<Optional<V2>>,ModalDialog.Config<C>.Action> configurer) {
                            cfg.action (c -> {
                                if (configurer != null)
                                    configurer.accept (v -> {
                                        if (cb != null)
                                            cb.accept (v);
                                    }, c);
                            });
                        }
                    });
                }
            }), modal -> {
                modal.contents ().addListener (IValueChangeListener.create (v -> {
                    if (cb != null)
                        cb.accept (Optional.ofNullable ((V2) v));
                    modal.close ();
                }));
                modal.addListener (IModalListener.close (() -> cb = null));
                modal.contents ().addListener (IUpdateListener.<V2>update (v -> {
                    if (cb != null)
                        cb.accept (Optional.ofNullable ((V2) v));
                }));
            });

            @Override
            public ModalDialog<?> dialog() {
                return modal;
            }

            @Override
            @SuppressWarnings("unchecked")
            public void open(V1 value, Consumer<Optional<V2>> cb) {
                this.cb = cb;
                // We open the model first, which ensures that it is rendererd befor we apply
                // any edit or reset (and these do not need to worry about checking render state
                // or retaining the data until render).
                modal.open ();
                if (modal.contents () instanceof IEditable) {
                    if (value instanceof IResolver)
                        ((IEditable<V1>) modal.contents ()).edit ((IResolver<V1>) value);
                    else
                        ((IEditable<V1>) modal.contents ()).edit (value);
                }
                if (modal.contents () instanceof IResetable)
                    ((IResetable) modal.contents ()).reset ();
                if (listeners != null) {
                    listeners.forEach (listener -> {
                        try {
                            listener.accept(modal);
                        } catch (Throwable e) {
                            Logger.reportUncaughtException (e);
                        }
                    });
                }
            }

        };
    }
}
