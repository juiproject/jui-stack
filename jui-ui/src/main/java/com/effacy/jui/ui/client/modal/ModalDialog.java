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

import java.beans.Transient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.effacy.jui.core.client.ICompletionCallback;
import com.effacy.jui.core.client.IProcessable;
import com.effacy.jui.core.client.IResetable;
import com.effacy.jui.core.client.Invoker;
import com.effacy.jui.core.client.component.IComponent;
import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.layout.ActionBarLayout;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.A;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.DomBuilder;
import com.effacy.jui.core.client.dom.builder.H1;
import com.effacy.jui.core.client.dom.builder.H2;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.dom.jquery.JQuery;
import com.effacy.jui.core.client.dom.jquery.JQueryElement;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.rpc.extdirect.IActionHandler;
import com.effacy.jui.ui.client.button.Button;
import com.effacy.jui.ui.client.button.IButton;
import com.effacy.jui.ui.client.button.IButtonHandler.IButtonActionCallback;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;
import jsinterop.base.Js;

/**
 * A specific type of {@link Modal} that includes a header, footer and
 * scrollable content area. The header includes support for a title (and
 * optional sub-title) along with a close action. The footer contains a toolbar
 * for buttons.
 * <p>
 * Implementation may be by configuration (providing action buttons and content)
 * or by sub-classing. If sub-classing one should implement the
 * {@link #populateActions()} method or providing button actions during
 * configuration. Within action buttons should be added to the footer toolbar
 * with the appropriate action handlers. The sub-class can provides contents by
 * implementing the {@link #configureContents(IComponent)} and passing
 * {@code null} for the contents through the constructor.
 * <p>
 * For closing there is a separate {@link #closeAction()}. This is invoked by
 * the close action in the title and can be called programmatically (i.e. from a
 * cancel button). This will invoke {@link #onCloseAction()} allowing specific
 * behaviours when the dialog is being actively closed (aka dismissed). In all
 * cases {@link #onClose()} is invoked (whether dismissed or not).
 * <p>
 * As with {@link Modal} styles can be applied by providing a
 * <code>ModalDialog_Override.css</code>.
 * 
 * @author Jeremy Buckley
 */
public class ModalDialog<V extends IComponent> extends Modal<V> {

    /**
     * Used to register a dialog against.
     */
    public interface IDialogRegister {

        /**
         * Register the dialog.
         * 
         * @param dialog
         *               the dialog.
         */
        public void register(ModalDialog<?> dialog);
    }

    /************************************************************************
     * Configuration and listeners.
     ************************************************************************/

    /**
     * An action handler for an individual action.
     */
    @FunctionalInterface
    public interface IDialogActionHandler<C extends IComponent> {

        /**
         * Handle the action.
         * 
         * @param cb
         *           the callback.
         */
        public void action(ICallback<C> cb);

        /**
         * Callback for the action handler.
         */
        public interface ICallback<C extends IComponent> {

            /**
             * Means that the action has completed successfully and the dialog can close.
             */
            public void success();

            /**
             * Means the action has failed and the dialog should not close.
             */
            public void fail();

            /**
             * Invokes {@link #fail()}. Name is choosen so as to not actually imply failure,
             * but rather the dialog continues to stay open.
             */
            default public void done() {
                fail();
            }

            /**
             * Access to the contents of the modal.
             * 
             * @return the contents.
             */
            public C contents();

            /**
             * The modal dialog instance.
             * 
             * @return the dialog.
             */
            public ModalDialog<C> modal();
        }

    }

    /**
     * Configuration for the dialog.
     */
    public static class Config<C extends IComponent> extends Modal.Config {

        /**
         * Generalized style (visual representation).
         */
        public interface Style {

            /**
             * The styles to apply.
             */
            public ILocalCSS styles();

            /**
             * Close icon to use.
             * 
             * @return the icon CSS.
             */
            default public String closeIcon() {
                return FontAwesome.times ();
            }
        }

        /**
         * Standard styles for dialogs. Note that you can supply your own styles by
         * implementing {@link Style}.
         */
        public enum ModalStyle implements Style {

            /**
             * Action bar distinguished from body.
             */
            STANDARD(StandardLocalCSS.instance ()),

            /**
             * Action bar separated from body.
             */
            SEPARATED(SeparatedLocalCSS.instance ()),

            /**
             * Action bar blends into body.
             */
            UNIFORM(UniformLocalCSS.instance ());

            /**
             * See {@link #styles()}.
             */
            private ILocalCSS styles;

            /**
             * Private constructor.
             */
            private ModalStyle(ILocalCSS styles) {
                this.styles = styles;
            }

            /**
             * {@inheritDoc}
             *
             * @see com.effacy.jui.core.client.modal.ModalDialog.Config.Style#styles()
             */
            @Override
            public ILocalCSS styles() {
                return styles;
            }

        }

        /**
         * See {@link #getStyle()}.
         */
        protected Style style = ModalStyle.STANDARD;

        /**
         * See {@link #title(String)}.
         */
        private String title;

        /**
         * See {@link #titleWrap()}.
         */
        private boolean titleWrap;

        /**
         * See {@link #subtitle(String)}.
         */
        private String subtitle;

        /**
         * See {@link #subtitleIcon(String)}.
         */
        private String subtitleIcon;

        /**
         * See {@link #closable(boolean)}.
         */
        private boolean closable = true;

        /**
         * See {@link #getCloseIconStyle()} (custom case).
         */
        private String closeIcon;

        /**
         * See {@link #getCloseText()}.
         */
        private String closeText;

        /**
         * See {@link #setPadding(Length)}.
         */
        private Insets padding;

        /**
         * See {@link #getActions()}.
         */
        private List<Config<C>.Action> actions = new ArrayList<> ();

        /**
         * See {@link #closeAction(IActionHandler)}.
         */
        private Invoker closeAction;

        /**
         * See {@link #dialogCss(String)}.
         */
        private String dialogCss;

        /**
         * See {@link #contentsCss(String)}.
         */
        private String contentsCss;

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.modal.Modal.Config#testId(String)
         */
        @Override
        @SuppressWarnings("unchecked")
        public Config<C> testId(String testId) {
            return (Config<C>) super.testId (testId);
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.modal.Modal.Config#type(com.effacy.jui.core.client.modal.Modal.Type)
         */
        @Override
        @SuppressWarnings("unchecked")
        public Config<C> type(Type type) {
            return (Config<C>) super.type (type);
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.modal.Modal.Config#width(com.effacy.jui.core.client.dom.css.Length)
         */
        @Override
        @SuppressWarnings("unchecked")
        public Config<C> width(Length width) {
            return (Config<C>) super.width (width);
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.modal.Modal.Config#maxWidth(com.effacy.jui.core.client.dom.css.Length)
         */
        @Override
        @SuppressWarnings("unchecked")
        public Config<C> maxWidth(Length maxWidth) {
            return (Config<C>) super.maxWidth (maxWidth);
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.modal.Modal.Config#minWidth(com.effacy.jui.core.client.dom.css.Length)
         */
        @Override
        @SuppressWarnings("unchecked")
        public Config<C> minWidth(Length minWidth) {
            return (Config<C>) super.minWidth (minWidth);
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.modal.Modal.Config#height(com.effacy.jui.core.client.dom.css.Length)
         */
        @Override
        @SuppressWarnings("unchecked")
        public Config<C> height(Length height) {
            return (Config<C>) super.height (height);
        }

        /**
         * Assign CSS to the dialog element.
         * 
         * @param dialogCss
         *                  the dialog CSS.
         * @return this configuration instance.
         */
        public Config<C> dialogCss(String dialogCss) {
            this.dialogCss = dialogCss;
            return this;
        }

        /**
         * Assign CSS to the content area.
         * 
         * @param contentsCss
         *                    the content CSS.
         * @return this configuration instance.
         */
        public Config<C> contentsCss(String contentsCss) {
            this.contentsCss = contentsCss;
            return this;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.modal.Modal.Config#minHeight(com.effacy.jui.core.client.dom.css.Length)
         */
        @Override
        @SuppressWarnings("unchecked")
        public Config<C> minHeight(Length minHeight) {
            return (Config<C>) super.minHeight (minHeight);
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.modal.Modal.Config#removeOnClose()
         */
        @Override
        @SuppressWarnings("unchecked")
        public Config<C> removeOnClose() {
            return (Config<C>) super.removeOnClose ();
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.modal.Modal.Config#removeOnClose(boolean)
         */
        @Override
        @SuppressWarnings("unchecked")
        public Config<C> removeOnClose(boolean removeOnClose) {
            return (Config<C>) super.removeOnClose (removeOnClose);
        }

        /**
         * Sets the style for the dialog.
         * 
         * @param style
         *              the style.
         * @return this configuration instance.
         */
        public Config<C> style(Style style) {
            if (style != null)
                this.style = style;
            return this;
        }

        /**
         * Sets the title for the dialog.
         * 
         * @param title
         *              the title.
         * @return this configuration instance.
         */
        public Config<C> title(String title) {
            this.title = title;
            return this;
        }

        /**
         * Convenience to call {@link #titleWrap(boolean)} with {@code true}.
         * 
         * @return this configuration instance.
         */
        public Config<C> titleWrap() {
            return titleWrap (true);
        }

        /**
         * Wraps the title if it is too long for the title bar.
         * <p>
         * Normally titles won't wrap but will be shortened with ellipses.
         * 
         * @param titleWrap
         *              {@code true} to allow the title to wrap.
         * @return this configuration instance.
         */
        public Config<C> titleWrap(boolean titleWrap) {
            this.titleWrap = titleWrap;
            return this;
        }

        /**
         * Sets the subtitle for the dialog.
         * 
         * @param subtitle
         *                 the subtitle.
         * @return this configuration instance.
         */
        public Config<C> subtitle(String subtitle) {
            this.subtitle = subtitle;
            return this;
        }

        /**
         * Sets an icon to use along with the subtitle.
         * 
         * @param subtitleIcon
         *                     the subtitle icon.
         * @return this configuration instance.
         */
        public Config<C> subtitleIcon(String subtitleIcon) {
            this.subtitleIcon = subtitleIcon;
            return this;
        }

        /**
         * Determines if the dialog is closable.
         * 
         * @param closable
         *                 {@code true} if it is (which is the default).
         * @return this configuration instance.
         */
        public Config<C> closable(boolean closable) {
            this.closable = closable;
            return this;
        }

        /**
         * Convenience to call {@link #closable(boolean)} with {@code true}.
         * 
         * @return this configuration instance.
         */
        public Config<C> closable() {
            return closable (true);
        }

        /**
         * Convenience to call {@link #closable(boolean)} with {@code true} and to
         * assign a specific icon and / or display text (if none are supplied the
         * default close icon is used along with its close behaviour).
         * 
         * @return this configuration instance.
         */
        public Config<C> closable(String icon, String text) {
            this.closeIcon = icon;
            this.closeText = text;
            return closable (true);
        }

        /**
         * Assigns padding to the content area.
         * 
         * @param padding
         *                the padding to apply (default is none).
         * @return this configuration instance.
         */
        public Config<C> padding(Length padding) {
            if (padding == null)
                return padding ((Insets) null);
            return padding (Insets.ln (padding, padding, padding, padding));
        }

        /**
         * Assigns padding to the content area.
         * 
         * @param padding
         *                the padding to apply (default is none).
         * @return this configuration instance.
         */
        public Config<C> padding(Insets padding) {
            this.padding = padding;
            return this;
        }

        /**
         * Handler to be processed when the close action has been invoked.
         * 
         * @param closeAction
         *                    the handler.
         * @return this configuration instance.
         */
        public Config<C> closeAction(Invoker closeAction) {
            this.closeAction = closeAction;
            return this;
        }

        /**
         * Actions (buttons) to add directly.
         * 
         * @return the actions.
         */
        public List<Action> getActions() {
            return actions;
        }

        /**
         * Adds an action.
         * 
         * @return the action to configure.
         */
        public Action action() {
            Action action = new Action ();
            actions.add (action);
            return action;
        }

        /**
         * Adds an action.
         * 
         * @param configure
         *                  to configure the action.
         * @return this configuration instance.
         */
        public Config<C> action(Consumer<Config<C>.Action> configure) {
            Action action = new Action ();
            if (configure != null)
                configure.accept (action);
            actions.add (action);
            return this;
        }

        /**
         * The style of dialog.
         * 
         * @return the style.
         */
        @Transient
        public Style getStyle() {
            return style;
        }

        /**
         * Getter for {@link #setTitle(String)}.
         */
        public String getTitle() {
            return title;
        }

        /**
         * Getter for {@link #setSubtitle(String)}.
         */
        public String getSubtitle() {
            return subtitle;
        }

        /**
         * Getter for {@link #subtitleIcon}.
         */
        public String getSubtitleIcon() {
            return subtitleIcon;
        }

        /**
         * Getter for {@link #setClosable(boolean)}.
         */
        public boolean isClosable() {
            return closable;
        }

        /**
         * The icon style for the close action.
         * <p>
         * The default comes from {@link #getStyle()}.
         * 
         * @return the icon style.
         */
        public String getCloseIconStyle() {
            if (closeIcon != null)
                return closeIcon;
            return getStyle ().closeIcon ();
        }

        /**
         * Any close text to use.
         * 
         * @return the text.
         */
        public String getCloseText() {
            return closeText;
        }

        /**
         * The default close applies when there is no custom close icon or text. Note
         * that {@link #getCloseIconStyle()} will return the default close icon. return
         * {@code true} if the close is the default.
         */
        public boolean isCloseDefault() {
            return StringSupport.empty (closeIcon) && StringSupport.empty (closeText);
        }

        /**
         * Captures a standard action.
         */
        public class Action {

            /**
             * The button style to use.
             */
            protected Button.Config.Style buttonStyle = Button.Config.Style.NORMAL;

            /**
             * Display label.
             */
            protected String label;

            /**
             * Test ID to apply to the button.
             */
            protected String testId;

            /**
             * Action reference.
             */
            protected Object reference;

            /**
             * Action handler.
             */
            protected IDialogActionHandler<C> handler;

            /**
             * Invoked on registration.
             */
            protected BiConsumer<ModalDialog<C>, IButton> register;

            /**
             * To appear on the left.
             */
            protected boolean left;

            /**
             * See {@link #icon(String, boolean)}.
             */
            protected String iconStyle;

            /**
             * See {@link #icon(String, boolean)}.
             */
            protected boolean iconOnRight;

            /**
             * Sets the label.
             * 
             * @param label
             *              the label.
             * @return this action configuration.
             */
            public Action label(String label) {
                this.label = label;
                return this;
            }

            /**
             * Assigns a test ID.
             * <p>
             * This will always have the prefix "btn_" applied. If it is not set then the
             * label will be used; it will be converted to lowercase and have spaces
             * replaced by underscores.
             * 
             * @param testId
             *               the test ID.
             * @return this action configuration.
             */
            public Action testId(String testId) {
                this.testId = testId;
                return this;
            }

            /**
             * Specifies an icon CSS.
             * 
             * @param iconStyle
             *                  the icon CSS (i.e. {@link FontAwesome}).
             * @return this action configuration.
             */
            public Action icon(String iconStyle) {
                this.iconStyle = iconStyle;
                return this;
            }

            /**
             * Specifies an icon CSS.
             * 
             * @param iconStyle
             *                  the icon CSS (i.e. {@link FontAwesome}).
             * @param right
             *                  {@code true} if the icon should appear on the right side of
             *                  the button.
             * @return this action configuration.
             */
            public Action icon(String iconStyle, boolean right) {
                this.iconStyle = iconStyle;
                this.iconOnRight = right;
                return this;
            }

            /**
             * Sets the action reference.
             * 
             * @param reference
             *                  the reference.
             * @return this action configuration.
             */
            public Action reference(Object reference) {
                this.reference = reference;
                return this;
            }

            /**
             * Sets the action handler.
             * 
             * @param handler
             *                the handler.
             * @return this action configuration.
             */
            public Action handler(IDialogActionHandler<C> handler) {
                this.handler = handler;
                return this;
            }

            /**
             * Assigns the default handler. This is a special handler that inspects the
             * contents and acts accordingly. If the contents implements
             * {@link IProcessable} then it will attempt to process it. If there is no
             * default bahavuour for the contents then an empty result is returned.
             * 
             * @param cb
             *           the callback to return the result to.
             * @return this action instance.
             */
            @SuppressWarnings("unchecked")
            public <V2> Action defaultHandler(Consumer<Optional<V2>> cb) {
                return handler (a -> {
                    if (a.contents () instanceof IProcessable) {
                        ((IProcessable<V2>) a.contents ()).process (outcome -> {
                            if (outcome.isPresent ()) {
                                if (cb != null)
                                    cb.accept (outcome);
                                a.success ();
                            } else
                                a.fail ();
                        });
                    } else {
                        if (cb != null)
                            cb.accept (Optional.empty ());
                        a.success ();
                    }
                });
            }

            /**
             * Specifies a handler to be invoked once the button has been created and
             * registered with the dialog.
             * 
             * @param register
             *                 to process the button when it has been created.
             * @return this action configuration.
             */
            public Action register(BiConsumer<ModalDialog<C>, IButton> register) {
                this.register = register;
                return this;
            }

            public Action left(boolean left) {
                this.left = left;
                return this;
            }

            public Action normal() {
                this.buttonStyle = Button.Config.Style.NORMAL;
                return this;
            }

            public Action link() {
                this.buttonStyle = Button.Config.Style.LINK;
                return this;
            }

            public Action danger() {
                this.buttonStyle = Button.Config.Style.NORMAL_DANGER;
                return this;
            }

            public Action outlined() {
                this.buttonStyle = Button.Config.Style.OUTLINED;
                return this;
            }

            /**
             * Sets the button style.
             * 
             * @param style
             *              the style.
             * @return this action configuration.
             */
            public Action style(Button.Config.Style style) {
                this.buttonStyle = style;
                return this;
            }
        }

    }

    /************************************************************************
     * Members.
     ************************************************************************/

    /**
     * ID for the actions region.
     */
    private static String RG_ACTIONS = "actions";

    /**
     * The actions.
     */
    private Map<Object, IButton> actions = new HashMap<> ();

    /************************************************************************
     * Construction.
     ************************************************************************/

    /**
     * Construct with the default configuration. Contents to be provided by
     * {@link #createContents()}.
     */
    public ModalDialog() {
        this (new ModalDialog.Config<V> (), null);
    }

    /**
     * Construct with the default configuration and contents.
     * <p>
     * If the contents implement {@link IDialogRegister} then it will be registered
     * with this dialog..
     * 
     * @param contents
     *                 the contents of the modal.
     */
    public ModalDialog(V contents) {
        this (new ModalDialog.Config<V> (), contents);
    }

    /**
     * Construct a modal. Contents to be provided by {@link #createContents()}.
     * <p>
     * Note that you can configure actions either directly in the configuration or
     * by overriding {@link #populateActions()}. The latter is called prior to
     * rendering so it is safe to add the actions to the configuration after it has
     * been passed to the super class constructor (this allows the actions to
     * declare handlers that refer to class methods).
     * 
     * @param config
     *               the configuration to apply.
     */
    public ModalDialog(ModalDialog.Config<V> config) {
        this (config, null);
    }

    /**
     * Construct a modal with contents.
     * <p>
     * If the contents implement {@link IDialogRegister} then it will be registered
     * with this dialog..
     * <p>
     * See notes on {@link #ModalDialog(Config)}.
     * 
     * @param config
     *                 the configuration to apply.
     * @param contents
     *                 the contents of the modal.
     */
    public ModalDialog(ModalDialog.Config<V> config, V contents) {
        super (config, contents);
        if (contents instanceof IDialogRegister)
            ((IDialogRegister) contents).register (this);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#onBeforeRender()
     */
    @Override
    protected void onBeforeRender() {
        populateActions ();
        super.onBeforeRender ();
    }

    /**
     * Populates the various actions. The default is to add the action buttons from
     * the configuration.
     * <p>
     * Note this is not called from the constructor but by
     * {@link #onBeforeRender()}.
     */
    protected void populateActions() {
        for (Config<V>.Action action : config ().getActions ()) {
            IButton btn;
            String testId = action.testId;
            if (StringSupport.empty (testId))
                testId = action.label.toLowerCase ().replace (' ','_');
            btn = addAction (
                new Button.Config ()
                    .style (action.buttonStyle)
                    .testId ("btn_" + testId)
                    .label (action.label)
                    .icon (action.iconStyle)
                    .iconOnRight (action.iconOnRight)
                    .behaviour (Button.Config.Behaviour.WAIT)
                    .handler (cb -> {
                        actionDispacther (action, cb);
                    }).build (),
                action.left
            );
            actions.put (action.reference, btn);
            if (action.register != null)
                action.register.accept (this, btn);
        }
    }

    /**
     * Enables the action of the given reference(s).
     * 
     * @param references
     *                   the references.
     */
    public void enable(Object... references) {
        if (references.length == 0) {
            super.enable ();
        } else {
            for (Object reference : references) {
                IButton btn = actions.get (reference);
                if (btn != null)
                    btn.enable ();
            }
        }
    }

    /**
     * Disables the action of the given reference.
     * 
     * @param references
     *                   the references.
     */
    public void disable(Object... references) {
        if (references.length == 0) {
            super.disable ();
        } else {
            for (Object reference : references) {
                IButton btn = actions.get (reference);
                if (btn != null)
                    btn.disable ();
            }
        }
    }

    /**
     * Shows the action of the given reference(s).
     * 
     * @param references
     *                   the references.
     */
    public void show(Object... references) {
        if (references.length == 0) {
            super.show ();
        } else {
            for (Object reference : references) {
                IButton btn = actions.get (reference);
                if (btn != null)
                    btn.show ();
            }
        }
    }

    /**
     * Hides the action of the given reference(s).
     * 
     * @param references
     *                   the references.
     */
    public void hide(Object... references) {
        if (references.length == 0) {
            super.hide ();
        } else {
            for (Object reference : references) {
                IButton btn = actions.get (reference);
                if (btn != null)
                    btn.hide ();
            }
        }
    }

    /**
     * Updates the label of the action of the given reference.
     * 
     * @param reference
     *                  the reference.
     * @param label
     *                  the new label.
     */
    public void updateLabel(Object reference, String label) {
        IButton btn = actions.get (reference);
        if (btn != null)
            btn.updateLabel (label);
    }

    /**
     * Registers an action handler for the given reference. This will replace any
     * existing handler.
     * <p>
     * This is used to overrde the default handling as defined by configuration (for
     * example, a model creator).
     * 
     * @param reference
     *                  the reference.
     * @param handler
     *                  the handler.
     */
    public void actionHandler(Object reference, IDialogActionHandler<V> handler) {
        if ((reference == null) || (handler == null))
            return;
        config().getActions().forEach(action -> {
            if ((action.reference != null) && action.reference.equals(reference))
                action.handler = handler;
        });
    }

    /**
     * Dispatches an action and closes the dialog once the callback handler has
     * completed.
     * 
     * @param action
     *               the action.
     * @param cb
     *               the callback.
     */
    protected void actionDispacther(Config<V>.Action action, final IButtonActionCallback cb) {
        if (action.handler != null) {
            action.handler.action (new IDialogActionHandler.ICallback<V> () {

                @Override
                public void success() {
                    close ();
                    if (cb != null)
                        cb.complete ();
                }

                @Override
                public void fail() {
                    if (cb != null)
                        cb.complete ();
                }

                @Override
                public V contents() {
                    return ModalDialog.this.contents ();
                }

                @Override
                public ModalDialog<V> modal() {
                    return ModalDialog.this;
                }

            });
        } else {
            handleAction (action.reference, new IDialogActionHandler.ICallback<V> () {

                @Override
                public void success() {
                    close ();
                    if (cb != null)
                        cb.complete ();
                }

                @Override
                public void fail() {
                    if (cb != null)
                        cb.complete ();
                }

                @Override
                public V contents() {
                    return ModalDialog.this.contents ();
                }

                @Override
                public ModalDialog<V> modal() {
                    return ModalDialog.this;
                }

            });
        }
    }

    /**
     * Handles an action. The default is to delegate to
     * {@link #handleAction(Object, ICompletionCallback)} where the completion
     * callback will call {@link IActionHandlerCallback#success()} on completion.
     * 
     * @param reference
     *                  the action reference.
     * @param cb
     *                  a callback.
     */
    protected void handleAction(Object reference, final IDialogActionHandler.ICallback<V> cb) {
        handleAction (reference, new IButtonActionCallback () {

            @Override
            public void complete() {
                if (cb != null)
                    cb.success ();
            }

        });
    }

    /**
     * Handles an action. The default is to complete.
     * 
     * @param reference
     *                  the action reference.
     * @param cb
     *                  a callback.
     */
    protected void handleAction(Object reference, IButtonActionCallback cb) {
        cb.complete ();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.AbstractBaseComponent#config()
     */
    @Override
    @SuppressWarnings("unchecked")
    public ModalDialog.Config<V> config() {
        // We need to cast this up as Modal does not include config in its generics
        // list.
        return (ModalDialog.Config<V>) super.config ();
    }

    /************************************************************************
     * Action bar (bottom).
     ************************************************************************/

    /**
     * Adds a component to the right side of the toolbar.
     * 
     * @param component
     *                  the component to add.
     * @return the passed component.
     */
    protected <C extends IComponent> C addAction(C component) {
        return addAction (component, false);
    }

    /**
     * Adds a component to the right side of the toolbar.
     * 
     * @param component
     *                  the component to add.
     * @param left
     *                  {@code true} if to appear on the left.
     * @return the passed component.
     */
    protected <C extends IComponent> C addAction(C component, boolean left) {
        openActions ();
        if (left)
            findRegionPoint (RG_ACTIONS).add (component, new ActionBarLayout.Data (0));
        else
            findRegionPoint (RG_ACTIONS).add (component, new ActionBarLayout.Data (1));
        return component;
    }

    /************************************************************************
     * Overrides and handlers.
     ************************************************************************/

    /**
     * Mimics clicking on the close action. This will invoked
     * {@link #onCloseAction()} so as to distinguish itself from a close by any
     * other means.
     */
    public void closeAction() {
        onCloseAction ();
        if (config ().closeAction != null)
            config ().closeAction.invoke ();
        close ();
    }

    /**
     * The close action was invoked.
     */
    protected void onCloseAction() {
        // Nothing.
    }

    /**
     * Updates the title. This is HTML safe.
     * 
     * @param title
     *              the new title.
     */
    public void updateTitle(String title) {
        config ().title (title);
        if (isRendered ())
            titleEl.text (title);
    }

    /**
     * Updates the sub-title. This is HTML safe.
     * 
     * @param subtitle
     *                 the new sub-title.
     */
    public void updateSubtitle(String subtitle) {
        config ().subtitle (subtitle);
        if (isRendered ()) {
            if (subtitle == null)
                subtitle = "";
            subtitleEl.text (subtitle);
            if (!StringSupport.empty (subtitle))
                getRoot ().classList.remove (styles ().compact ());
            else
                getRoot ().classList.add (styles ().compact ());
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.modal.Modal#onOpen()
     */
    @Override
    protected void onOpen() {
        super.onOpen ();

        // Reset the buttons.
        findRegionPoint (RG_ACTIONS).forEach (cpt -> {
            if (cpt instanceof IResetable)
                ((IResetable) cpt).reset ();
        });
    }

    /************************************************************************
     * Presentation.
     ************************************************************************/

    /**
     * See {@link #getActions()}.
     */
    protected Element actionsEl;

    /**
     * Access to the title element (used to apply padding).
     */
    protected JQueryElement titleEl;

    /**
     * Access to the sub-title element (used to apply padding).
     */
    protected JQueryElement subtitleEl;

    /**
     * Internal. Determines if the action section is open.
     */
    protected boolean _actionsOpen = false;

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.modal.Modal#onAfterRender()
     */
    @Override
    protected void onAfterRender() {
        super.onAfterRender ();

        if (config ().padding != null) {
            CSS.PADDING.with (config ().padding).apply ((elemental2.dom.Element) Js.cast (contentsEl));
            if (config ().padding.getLeft () != null)
                CSS.MARGIN_LEFT.with (config ().padding.getLeft ()).apply (titleEl);
        }

        if (StringSupport.empty (config ().subtitle))
            getRoot ().classList.add (styles ().compact ());

        // If there are no actions then hide the actions bar.
        if (!_actionsOpen)
            JQuery.$ (actionsEl).hide ();
    }

    /**
     * Show the actions.
     */
    public void openActions() {
        _actionsOpen = true;
        if (isRendered ())
            JQuery.$ (actionsEl).show ();
    }

    /**
     * Element to wrap a region around for the toolbar in the footer.
     */
    public Element getActions() {
        return actionsEl;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.modal.Modal#buildNode(com.effacy.jui.core.client.modal.Modal.Config)
     */
    @Override
    @SuppressWarnings("unchecked")
    protected INodeProvider buildNode(Modal.Config config) {
        Config<V> data = (Config<V>) config;
        return DomBuilder.div (outer -> {
            outer.style (styles ().outer ());
            Div.$ (outer).$ (mask -> {
                mask.style (styles ().mask ());
            });
            Div.$ (outer).$ (inner -> {
                inner.style (styles ().inner ());
                Div.$ (inner).$ (width -> {
                    width.by ("width");
                    width.style (styles ().width ());
                    Div.$ (width).$ (height -> {
                        height.style (styles ().height ());
                        Div.$ (height).$ (wrap -> {
                            wrap.style (styles ().wrap ());
                            Div.$(wrap).$ (dialog -> {
                                dialog.style (styles ().dialog ());
                                if (config.minHeight != null)
                                    dialog.css (CSS.MIN_HEIGHT, config.minHeight);
                                if (!StringSupport.empty(((ModalDialog.Config<?>) config).dialogCss))
                                    dialog.css(((ModalDialog.Config<?>) config).dialogCss);
                                Div.$ (dialog).$ (header -> {
                                    header.style (styles ().header ());
                                    H1.$(header).$ (h1-> {
                                        h1.testRef ("dialog_title").by ("title").text (data.getTitle ());
                                        if (config ().titleWrap)
                                            h1.style (styles ().titleWrap ());
                                    });
                                    H2.$(header).$ (h2 -> {
                                        h2.testRef ("dialog_subtitle").by ("subtitle");
                                        if (!StringSupport.empty (data.getSubtitleIcon ()))
                                            h2.em ().style (data.getSubtitleIcon ());
                                        h2.span ().text (data.getSubtitle ());
                                    });
                                    if (data.isClosable ()) {
                                        A.$(header).$ (a -> {
                                            a.id ("close");
                                            a.testRef ("dialog_close");
                                            // Register onclick handler for the close icon.
                                            a.on (e -> closeAction (), UIEventType.ONCLICK);
                                            if (data.isCloseDefault ())
                                                a.style (styles ().closeDefault ());
                                            a.em ().style (data.getCloseIconStyle ());
                                            if (!StringSupport.empty (data.getCloseText ()))
                                                a.span ().text (data.getCloseText ());
                                        });
                                    }
                                });
                                Div.$ (dialog).$ (body -> {
                                    body.style (styles ().body ());
                                    Div.$ (body).$ (contents -> {
                                        contents.by ("contents");
                                        // Register attachment point to the contents element.
                                        contents.apply (attach (contents ()));
                                        contents.style (styles ().contents ());
                                        if (!StringSupport.empty(((ModalDialog.Config<?>) config).contentsCss))
                                            contents.css(((ModalDialog.Config<?>) config).contentsCss);
                                    });
                                });
                                Div.$ (dialog).$ (footer -> {
                                    footer.id ("actions").by ("actions");
                                    footer.style (styles ().footer ());
                                    // Register action bar region to footer to contain actions.
                                    footer.apply (region (RG_ACTIONS, new ActionBarLayout.Config (2, Length.em (0.5)).build ()));
                                });
                            });
                        });
                    });
                });
            });
        }).build (tree -> {
            widthEl = JQuery.$ ((Element) tree.first ("width"));
            contentsEl = tree.first ("contents");
            actionsEl = tree.first ("actions");
            titleEl = JQuery.$ ((Element) tree.first ("title"));
            subtitleEl = JQuery.$ ((Element) tree.first ("subtitle"));
        });
    }

    /************************************************************************
     * CSS styles.
     ************************************************************************/

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.modal.Modal#styles()
     */
    @Override
    protected ILocalCSS styles() {
        return config ().getStyle ().styles ();
    }

    /**
     * Local CSS for the dialog.
     */
    public interface ILocalCSS extends Modal.ILocalCSS {

        /**
         * Wraps the dialog (delineates the extent of the dialog box).
         */
        public String dialog();

        /**
         * Dialog header.
         */
        public String header();

        /**
         * To wrap (the title).
         */
        public String titleWrap();

        /**
         * Dialog body.
         */
        public String body();

        /**
         * Dialog footer.
         */
        public String footer();

        /**
         * Adjusts the height of the header to include the sub-title.
         */
        public String compact();

        /**
         * The close is the default close.
         */
        public String closeDefault();
    }

    /**
     * Standard CSS.
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        "com/effacy/jui/ui/client/modal/Modal.css",
        "com/effacy/jui/ui/client/modal/Modal_Override.css",
        "com/effacy/jui/ui/client/modal/ModalDialog.css",
        "com/effacy/jui/ui/client/modal/ModalDialog_Override.css"
    })
    public static abstract class StandardLocalCSS implements ILocalCSS {

        private static StandardLocalCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (StandardLocalCSS) GWT.create (StandardLocalCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }

    /**
     * Separated CSS.
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        "com/effacy/jui/ui/client/modal/Modal.css",
        "com/effacy/jui/ui/client/modal/Modal_Override.css",
        "com/effacy/jui/ui/client/modal/ModalDialog.css",
        "com/effacy/jui/ui/client/modal/ModalDialog_Override.css",
        "com/effacy/jui/ui/client/modal/ModalDialog_Separated.css",
        "com/effacy/jui/ui/client/modal/ModalDialog_Separated_Override.css"
    })
    public static abstract class SeparatedLocalCSS implements ILocalCSS {

        /********************************************************************
         * Local instance
         ********************************************************************/

        private static SeparatedLocalCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (SeparatedLocalCSS) GWT.create (SeparatedLocalCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES; 
        }
    }

    /**
     * Uniform CSS.
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        "com/effacy/jui/ui/client/modal/Modal.css",
        "com/effacy/jui/ui/client/modal/Modal_Override.css",
        "com/effacy/jui/ui/client/modal/ModalDialog.css",
        "com/effacy/jui/ui/client/modal/ModalDialog_Override.css",
        "com/effacy/jui/ui/client/modal/ModalDialog_Uniform.css",
        "com/effacy/jui/ui/client/modal/ModalDialog_Uniform_Override.css"
    })
    public static abstract class UniformLocalCSS implements ILocalCSS {

        private static UniformLocalCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (UniformLocalCSS) GWT.create (UniformLocalCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }

}
