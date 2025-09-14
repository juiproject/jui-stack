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
package com.effacy.jui.ui.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.effacy.jui.core.client.ICompletionCallback;
import com.effacy.jui.core.client.Invoker;
import com.effacy.jui.core.client.component.Component;
import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.Markup;
import com.effacy.jui.core.client.dom.builder.P;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.ui.client.NotificationDialog.NotificationDialogContent;
import com.effacy.jui.ui.client.button.IButtonHandler.IButtonActionCallback;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.effacy.jui.ui.client.modal.ModalDialog;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

import elemental2.dom.Element;

/**
 * Dialog based mechanism for posting various notification, confirmations, etc.
 * <p>
 * To use simply invoke one of the appropriate static methods. The associated
 * dialog is deemed transitory so will be removed from the DOM after it has
 * closed.
 *
 * @author Jeremy Buckley
 */
public class NotificationDialog extends ModalDialog<NotificationDialogContent> {

    /************************************************************************
     * Icon styles
     ************************************************************************/

    /**
     * Icon style for confirmation. This is configurable so can be changed (though
     * changes are applied globally).
     */
    public static String CLS_CONFIRM = FontAwesome.checkCircle ();

    /**
     * Icon style for alert. This is configurable so can be changed (though changes
     * are applied globally).
     */
    public static String CLS_ALERT = FontAwesome.warning ();

    /**
     * Icon style for error. This is configurable so can be changed (though changes
     * are applied globally).
     */
    public static String CLS_ERROR = FontAwesome.bug ();

    /**
     * Icon style for the OK action button. This is configurable so can be changed
     * (though changes are applied globally).
     */
    public static String CLS_ACTION_OK = null;

    /**
     * Icon style for the dismiss action button. This is configurable so can be
     * changed (though changes are applied globally).
     */
    public static String CLS_ACTION_DISMISS = null;

    /************************************************************************
     * Support classes
     ************************************************************************/

    /**
     * The different types of outcomes.
     */
    public enum OutcomeType {
        OK, DISMISS, DISCARD;

        /**
         * A common determination.
         */
        public boolean isOk() {
            return this == OK;
        }
    }

    /************************************************************************
     * Builder
     ************************************************************************/

     /**
      * Used to build out the behaviour of a notification dialog.
      */
     public static class Builder {

        /**
         * The display icon. See constructor.
         */
        private Icon icon;

        /**
         * The dialog title. See constructor.
         */
        private String title;

        /**
         * The width.
         */
        private Length width;

        /**
         * See {@link #renderer(Consumer)}.
         */
        private Consumer<ElementBuilder> renderer;

        /**
         * See {@link #configurer(Consumer)}.
         */
        private Consumer<ModalDialog.Config<NotificationDialogContent>> configurer;

        /**
         * The notices to display in the dialog. See {@link #notice(String)}.
         */
        private List<String> notices = new ArrayList<> ();

        /**
         * See {@link #handler(IOutcomeHandler)}.
         */
        private BiConsumer<OutcomeType, ICompletionCallback> handler;

        /**
         * See {@link #close(Invoker)}.
         */
        private Invoker close;

        /**
         * Construct with required data.
         * 
         * @param icon
         *              the display icon.
         * @param title
         *              the dialog title.
         */
        Builder(Icon icon, String title) {
            this.icon = icon;
            this.title = title;
        }

        /**
         * Assigns a renderer to render the body contents.
         * 
         * @param renderer
         *               to render the contents.
         * @return this builder instance.
         */
        public Builder renderer(Consumer<ElementBuilder> renderer) {
            this.renderer = renderer;
            return this;
        }

        /**
         * Assign a specific width to the dialog. The deafault is 400px.
         * 
         * @param width
         *              the width.
         * @return this builder instance.
         */
        public Builder width(Length width) {
            this.width = width;
            return this;
        }

        /**
         * Adds a notice (body content) to the notification.
         * 
         * @param notice
         *               the line to add.
         * @return this builder instance.
         */
        public Builder notice(String notice) {
            if (notice != null)
                notices.add(notice);
            return this;
        }

        /**
         * Assigns an outcome handler.
         * 
         * @param handler
         *                the handler to assign.
         * @return this builder instance.
         */
        public Builder handler(BiConsumer<OutcomeType, ICompletionCallback> handler) {
            this.handler = handler;
            return this;
        }

        /**
         * Assigns an outcome handler.
         * 
         * @param handler
         *                the handler to assign.
         * @return this builder instance.
         */
        public Builder handler(Consumer<OutcomeType> handler) {
            this.handler = (type, cp) -> {
                if (handler != null)
                    handler.accept (type);
                cp.complete ();
            };
            return this;
        }

        /**
         * If assigned this will open the dialog for configuration of the actions.
         * 
         * @param configurer
         *                   to configure the dialog actions.
         */
        public Builder configurer(Consumer<ModalDialog.Config<NotificationDialogContent>> configurer) {
            this.configurer = configurer;
            return this;
        }

        /**
         * Assigns a handler that is called when the dialog is closed.
         * 
         * @param close
         *              the handler.
         * @return this builder instance.
         */
        public Builder close(Invoker close) {
            this.close = close;
            return this;
        }

        /**
         * Opens the notification dialog.
         */
        public void open() {
            new NotificationDialog (title, width, icon, configurer, renderer, (type, cb) -> {
                if (handler != null) {
                    handler.accept (type, () -> {
                        cb.complete();
                        if (close != null)
                            close.invoke ();
                    });
                } else {
                    cb.complete ();
                    if (close != null)
                        close.invoke ();
                }
            }, notices.toArray (new String [notices.size ()])).open ();
        }
     }

    /************************************************************************
     * Labels
     ************************************************************************/

    /**
     * Standard labels.
     */
    public static interface Labels extends Messages {

        @DefaultMessage("Cancel")
        public String cancel();

        @DefaultMessage("OK")
        public String ok();

        @DefaultMessage("Dismiss")
        public String dismiss();

        @DefaultMessage("Discard")
        public String discard();

        @DefaultMessage("Save")
        public String saveChanges();

        @DefaultMessage("Discard changes")
        public String discardChanges();
    }

    /**
     * Accessible instance.
     */
    public static final Labels LABELS = GWT.create (Labels.class);

    /************************************************************************
     * Invocation methods.
     ************************************************************************/

    public static void custom(Icon icon, String title, Consumer<Builder> config) {
        Builder builder = new Builder (icon, title);
        config.accept (builder);
        builder.open ();
    }

    /**
     * Creates a builder for a confirmation dialog.
     * <p>
     * Once configured call {@link Builder#open()} to show the dialog.
     * 
     * @param title
     *              the title of the dialog.
     * @return the builder.
     */
    public static Builder confirm(String title) {
        return new Builder(Icon.CONFIRM, title);
    }

    /**
     * Displays a confirmation dialog.
     * 
     * @param title
     *                the title of the dialog.
     * @param notice
     *                the body content.
     * @param handler
     *                (optional) invoked when an action is performed from the dialog
     *                (including close which appears as a dismiss).
     */
    public static void confirm(String title, String notice, Consumer<OutcomeType> handler) {
        confirm (title).notice (notice).handler (handler).open ();
    }

    /**
     * Displays a confirmation dialog.
     * 
     * @param title
     *                the title of the dialog.
     * @param notice
     *                the body content builder.
     * @param handler
     *                (optional) invoked when an action is performed from the dialog
     *                (including close which appears as a dismiss).
     */
    public static void confirm(String title, Consumer<ElementBuilder> notice, Consumer<OutcomeType> handler) {
        confirm (title).renderer (notice).handler (handler).open ();
    }

    /**
     * Displays a confirmation dialog.
     * 
     * @param title
     *                the title of the dialog.
     * @param notice
     *                the body content.
     * @param handler
     *                (optional) invoked when an action is performed from the dialog
     *                (including close which appears as a dismiss).
     */
    public static void confirm(String title, String notice, BiConsumer<OutcomeType,ICompletionCallback> handler) {
        confirm (title).notice (notice).handler (handler).open ();
    }

    /**
     * Displays a confirmation dialog.
     * 
     * @param title
     *                the title of the dialog.
     * @param notice
     *                the body content builder.
     * @param handler
     *                (optional) invoked when an action is performed from the dialog
     *                (including close which appears as a dismiss).
     */
    public static void confirm(String title, Consumer<ElementBuilder> notice, BiConsumer<OutcomeType,ICompletionCallback> handler) {
        confirm (title).renderer (notice).handler (handler).open ();
    }

    /**
     * Creates a builder for an alert dialog.
     * <p>
     * Once configured call {@link Builder#open()} to show the dialog.
     * 
     * @param title
     *              the title of the dialog.
     * @return the builder.
     */
    public static Builder alert(String title) {
        return new Builder(Icon.ALERT, title);
    }

    /**
     * Displays an alert dialog.
     * 
     * @param title
     *                the title of the dialog.
     * @param notice
     *                the body content.
     * @param handler
     *                (optional) invoked when an action is performed from the dialog
     *                (including close which appears as a dismiss).
     */
    public static void alert(String title, String notice, Consumer<OutcomeType> handler) {
        alert (title).notice (notice).handler (handler).open ();
    }

    /**
     * Displays an alert dialog.
     * 
     * @param title
     *                the title of the dialog.
     * @param notice
     *                the body content builder.
     * @param handler
     *                (optional) invoked when an action is performed from the dialog
     *                (including close which appears as a dismiss).
     */
    public static void alert(String title, Consumer<ElementBuilder> notice, Consumer<OutcomeType> handler) {
        alert (title).renderer (notice).handler (handler).open ();
    }

    /**
     * Creates a builder for an error dialog.
     * <p>
     * Once configured call {@link Builder#open()} to show the dialog.
     * 
     * @param title
     *              the title of the dialog.
     * @return the builder.
     */
    public static Builder error(String title) {
        return new Builder(Icon.ERROR, title);
    }

    /**
     * Displays an error dialog.
     * 
     * @param title
     *                the title of the dialog.
     * @param notice
     *                the body content.
     * @param handler
     *                (optional) invoked when an action is performed from the dialog
     *                (including close which appears as a dismiss).
     */
    public static void error(String title, String notice, Consumer<OutcomeType> handler) {
        error (title).notice (notice).handler (handler).open ();
    }

    /**
     * Displays an error dialog.
     * 
     * @param title
     *                the title of the dialog.
     * @param notice
     *                the body content builder.
     * @param handler
     *                (optional) invoked when an action is performed from the dialog
     *                (including close which appears as a dismiss).
     */
    public static void error(String title, Consumer<ElementBuilder> notice, Consumer<OutcomeType> handler) {
        error (title).renderer (notice).handler (handler).open ();
    }

    /**
     * Creates a builder for a save dialog.
     * <p>
     * Once configured call {@link Builder#open()} to show the dialog.
     * 
     * @param title
     *              the title of the dialog.
     * @return the builder.
     */
    public static Builder save(String title) {
        return new Builder(Icon.SAVE, title);
    }

    /**
     * Displays a save dialog.
     * 
     * @param title
     *                the title of the dialog.
     * @param notice
     *                the body content.
     * @param handler
     *                (optional) invoked when an action is performed from the dialog
     *                (including close which appears as a dismiss).
     */
    public static void save(String title, String notice, Consumer<OutcomeType> handler) {
        save (title).notice (notice).handler (handler).open ();
    }

    /**
     * Displays a save dialog.
     * 
     * @param title
     *                the title of the dialog.
     * @param notice
     *                the body content builder.
     * @param handler
     *                (optional) invoked when an action is performed from the dialog
     *                (including close which appears as a dismiss).
     */
    public static void save(String title, Consumer<ElementBuilder> notice, Consumer<OutcomeType> handler) {
        save (title).renderer (notice).handler (handler).open ();
    }

    /**
     * Displays a save dialog.
     * 
     * @param title
     *                the title of the dialog.
     * @param notice
     *                the body content.
     * @param handler
     *                (optional) invoked when an action is performed from the dialog
     *                (including close which appears as a dismiss).
     */
    public static void save(String title, String notice, BiConsumer<OutcomeType,ICompletionCallback> handler) {
        save (title).notice (notice).handler (handler).open ();
    }

    /**
     * Displays a save dialog.
     * 
     * @param title
     *                the title of the dialog.
     * @param notice
     *                the body content builder.
     * @param handler
     *                (optional) invoked when an action is performed from the dialog
     *                (including close which appears as a dismiss).
     */
    public static void save(String title, Consumer<ElementBuilder> notice, BiConsumer<OutcomeType,ICompletionCallback> handler) {
        save (title).renderer (notice).handler (handler).open ();
    }

    /************************************************************************
     * Implementation
     ************************************************************************/

    /**
     * Various icons to display.
     */
    public enum Icon {
        CONFIRM, ALERT, ERROR, SAVE;
    }

    /**
     * The registered outcome handler.
     */
    private BiConsumer<OutcomeType, ICompletionCallback> handler;

    protected NotificationDialog(String title, Length width, Icon icon, Consumer<ModalDialog.Config<NotificationDialogContent>> configurer, Consumer<ElementBuilder> renderer, BiConsumer<OutcomeType, ICompletionCallback> handler, String... notices) {
        super (createConfig (title, width, config -> {
            if (configurer != null) {
                configurer.accept(config);
            } else {
                if (Icon.CONFIRM == icon) {
                    config
                        .action (a -> a.label (LABELS.cancel ().toLowerCase ()).reference (OutcomeType.DISMISS).link ())
                        .action (a -> a.label (LABELS.ok ()).reference (OutcomeType.OK).icon (CLS_ACTION_OK));
                } else if (Icon.SAVE == icon) {
                    config
                        .action (a -> a.label (LABELS.cancel ().toLowerCase ()).reference (OutcomeType.DISMISS).link ())
                        .action (a -> a.label (LABELS.discardChanges ().toLowerCase ()).reference (OutcomeType.DISCARD).outlined ())
                        .action (a -> a.label (LABELS.saveChanges ()).reference (OutcomeType.OK).icon (CLS_ACTION_OK));
                } else {
                    config
                        .action (a -> a.label (LABELS.dismiss ()).reference (OutcomeType.DISMISS).icon (CLS_ACTION_DISMISS));
                }
            }
        }), new NotificationDialogContent.Config (icon, renderer, notices).build ());
        this.handler = handler;
    }

    /**
     * Convenience to create the suitable configuration given the configuration
     * data.
     */
    private static ModalDialog.Config<NotificationDialogContent> createConfig(String title, Length width, Consumer<ModalDialog.Config<NotificationDialogContent>> configurer) {
        ModalDialog.Config<NotificationDialogContent> config = new ModalDialog.Config<NotificationDialogContent> ()
            .width ((width == null) ? Length.px(400) : width)
            .title (title)
            .padding (Length.px (20));
        if (configurer != null)
            configurer.accept(config);
        return config;
    }

    @Override
    public void closeAction() {
        handleAction (OutcomeType.DISMISS, () -> close ());
    }


    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.dialog.modal.ModalDialog#handleAction(java.lang.Object,
     *      com.effacy.jui.core.client.gwt.core.client.cb.ICompletionCallback)
     */
    @Override
    protected void handleAction(Object reference, IButtonActionCallback cb) {
        // Note that when the button callback is enacted the dialog will close (this
        // being the default action for this method).
        if (reference == null) {
            cb.complete ();
        } else if (reference instanceof OutcomeType) {
            handleOutcome ((OutcomeType) reference, cb);
        } else {
            handleOutcome (OutcomeType.OK, cb);
        }
    }

    /**
     * Used internally to indicate that an action has been performed.
     * 
     * @param type
     *             the outcome defined by the action performed.
     * @param cb
     *             a callback to be called when the action has completed.
     */
    protected void handleOutcome(OutcomeType type, IButtonActionCallback cb) {
        onOutcome (type, () -> {
            if (cb != null)
                cb.complete ();
        });
    }

    /**
     * An action has been invoked. This will be called prior to closing the dialog
     * (before {@link #close()} is called).
     * <p>
     * Not that this method will dispatch to any registered handler (see
     * {@link IOutcomeHandler} as set during construction).
     * 
     * @param type
     *             the action type.
     * @param cb
     *             callback to indicate completion.
     */
    protected void onOutcome(OutcomeType type, ICompletionCallback cb) {
        if (handler != null)
            handler.accept (type, cb);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.dialog.modal.Modal#onClose()
     */
    protected void onAfterClose() {
        // Always remove (regardless of configuration).
        dispose ();
    }

    /************************************************************************
     * Content for the dialog.
     ************************************************************************/

    public static class NotificationDialogContent extends Component<NotificationDialogContent.Config> {

        /**
         * Configuration for the notification contents.
         */
        public static class Config extends Component.Config {

            /**
             * See {@link #isIconAlert()}, etc.
             */
            private Icon icon;

            /**
             * See {@link #getContents()}.
             */
            private List<String> contents = new ArrayList<String> ();

            private Consumer<ElementBuilder> renderer;

            /**
             * Construct configuration for content.
             * 
             * @param icon
             *                 the icon to display.
             * @param contents
             *                 list of contents paragraphs.
             */
            public Config(Icon icon, Consumer<ElementBuilder> renderer, String... contents) {
                this.renderer = renderer;
                this.icon = icon;
                for (String content : contents) {
                    // Contents are escaped so can be injected as HTML.
                    //this.contents.add (new SafeHtmlBuilder ().appendEscaped (content).toSafeHtml ().asString ());
                    // Don't need to escape as the content will be added as text (not html content).
                    this.contents.add (content);
                }
            }

            /**
             * If the icon is a confirmation icon (used to apply a suitable style to the em
             * element).
             */
            public boolean isIconConfirm() {
                return (Icon.CONFIRM == icon);
            }

            /**
             * If the icon is an alert icon (used to apply a suitable style to the em
             * element).
             * <p>
             * Note that for now this also includes {@link Icon#SAVE}.
             */
            public boolean isIconAlert() {
                return (Icon.ALERT == icon) || (Icon.SAVE == icon);
            }

            /**
             * If the icon is an error icon (used to apply a suitable style to the em
             * element).
             */
            public boolean isIconError() {
                return (Icon.ERROR == icon);
            }

            /**
             * The icon style to used. This is based on the icon and looks up the style from
             * the static strings associated with the {@link NotificationDialog} (which
             * allows for global configuration).
             */
            public String getIconStyle() {
                if (isIconConfirm ())
                    return CLS_CONFIRM;
                if (isIconAlert ())
                    return CLS_ALERT;
                if (isIconError ())
                    return CLS_ERROR;
                return "";
            }

            /**
             * List of notices to display as paragraphs. These are HTML escaped.
             */
            public List<String> getContents() {
                return contents;
            }

            public Consumer<ElementBuilder> getRenderer() {
                return renderer;
            }

            public NotificationDialogContent build() {
                return new NotificationDialogContent (this);
            }
        }

        protected NotificationDialogContent(Config config) {
            super (config);
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.component.RendererComponent#styles()
         */
        @Override
        protected ILocalCSS styles() {
            return LocalCSS.instance ();
        }


        @Override
        protected INodeProvider buildNode(Element el) {
            return Wrap.$ (el).$ (root -> {
                Div.$ (root).$ (outer -> {
                    outer.style (styles ().outer ());
                    if (config().isIconConfirm ())
                        Em.$ (outer).style (styles ().confirm (), CLS_CONFIRM);
                    else if (config().isIconAlert ())
                        Em.$ (outer).style (styles ().alert (), CLS_ALERT);
                    else if (config().isIconError ())
                        Em.$ (outer).style (styles ().error (), CLS_ERROR);
                    if (config ().getRenderer () != null) {
                        config ().getRenderer ().accept (outer);
                    } else {
                        config().getContents().forEach(notice -> {
                            P.$ (outer).$ (
                                Markup.$(notice)
                            );
                        });
                    }
            });
            }).build ();
        }

        public interface ILocalCSS extends IComponentCSS {

            /**
             * Framing style.
             */
            public String outer();

            /**
             * Applied to the icon em.
             */
            public String confirm();

            /**
             * Applied to the icon em.
             */
            public String alert();

            /**
             * Applied to the icon em.
             */
            public String error();
        }

        /**
         * Component CSS.
         */
        @CssResource({
            IComponentCSS.COMPONENT_CSS,
            "com/effacy/jui/ui/client/NotificationDialog.css",
            "com/effacy/jui/ui/client/NotificationDialog_Override.css"
        })
        public static abstract class LocalCSS implements ILocalCSS {

            private static LocalCSS STYLES;

            public static ILocalCSS instance() {
                if (STYLES == null) {
                    STYLES = (LocalCSS) GWT.create (LocalCSS.class);
                    STYLES.ensureInjected ();
                }
                return STYLES;
            }
        }

    }

}
