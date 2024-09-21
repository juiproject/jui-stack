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
import java.util.function.BiConsumer;

import com.effacy.jui.core.client.IClosable;
import com.effacy.jui.core.client.ICloseAware;
import com.effacy.jui.core.client.IOpenAware;
import com.effacy.jui.core.client.Invoker;
import com.effacy.jui.core.client.component.Component;
import com.effacy.jui.core.client.component.IComponent;
import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.dom.DomSupport;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.builder.DomBuilder;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.dom.jquery.JQuery;
import com.effacy.jui.core.client.dom.jquery.JQueryElement;
import com.effacy.jui.core.client.observable.IListener;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.platform.util.client.TimerSupport;
import com.google.gwt.core.client.GWT;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;

/**
 * A modal dialog container.
 * <p>
 * This produces a simple modal container that occupies a central portion of the
 * screen (the content area) subject to the given configuration.
 * <p>
 * One particular feature is that the containment area is styled so that it
 * center aligns (vertically and horizontally) anything that is container in
 * that area. If what is being contained includes a "max-height: 100%" this will
 * force the contained to overflow if the screen height is reduced to below the
 * expanse of the contained elements plus padding in the column (see
 * {@link ModalDialog}.
 * <p>
 * Support is also provided for nested modals (i.e. one created while another is
 * being displayed). The model is simple in that a count is maintained of the
 * number of open modals and if a new modal is opened then it has a positioning
 * style applied to it equal to the current modal count. This position adjusts
 * the z-index to ensure the modal with the highest index is displayed over
 * others. There is also a mild horizontal and vertical nudge to show the
 * existence of lower nested modals. Now this model only works when a modals are
 * opened and closed in the right order. For example, opening two modals then
 * closing the first and opening a third the third will have the same index as
 * the second which could lead to conflict. The proper way of opening modals is
 * to either close the pior modal before opening the next or opening a
 * subsequent modal then closing that model before closing the first. Generally
 * this should not be an issue as stacked modals are generally a result of
 * recursive demands.
 * <p>
 * The modal CSS can be modified by declaring a <code>Modal_Override.css</code>
 * (in the same package as this class) and overriding the relevant styles. This
 * is the preferred manner of styling a modal.
 * <p>
 * To use simply create a instance of the modal class and open it when needed
 * (it will automatically be added to a modal specific root panel and rendered
 * to a modal dedicated DIV appended to the document page). A modal may be
 * opened and closed repeatedly as it is not removed at any time. For a purely
 * transient modal override the {@link #onClose()} method with a call to
 * {@link #removeFromParent()} (this will remove the modal from the root panel
 * and from the DOM).
 *
 * @author Jeremy Buckley
 */
public class Modal<V extends IComponent> extends Component<Modal.Config> implements IClosable {

    /**
     * A target element that can be used to apply a blur.
     */
    private static Element BLUR_TARGET;

    /**
     * Assigns a blur element.
     * 
     * @param el
     *           the element.
     */
    public static void setBlurTarget(Element el) {
        if (BLUR_TARGET != null)
            BLUR_TARGET.classList.add (LocalCSS.instance ().blur ());
        BLUR_TARGET = el;
    }

    /************************************************************************
     * Open and close handler.
     * <p>
     * Allows one to externally listen to the opening and closing of dialogs.
     ************************************************************************/

    /**
     * The currently active dialogs.
     */
    private static List<Modal<?>> DIALOGS = new ArrayList<> ();

    /**
     * Open handlers (see {@link #addOpenHandler(BiConsumer)}).
     */
    private static List<BiConsumer<List<Modal<?>>, Modal<?>>> OPEN_HANDLERS;

    /**
     * Close handlers (see {@link #addCloseHandler(BiConsumer)}).
     */
    private static List<BiConsumer<List<Modal<?>>, Modal<?>>> CLOSE_HANDLERS;

    /**
     * Adds a handler that is invoked when a dialog is opened. Passed is a list of
     * dialogs already open (excluding the one being opened) and the dialog that is
     * being opened.
     * 
     * @param handler
     *                the handler.
     */
    public static void addOpenHandler(BiConsumer<List<Modal<?>>, Modal<?>> handler) {
        if (handler == null)
            return;
        if (OPEN_HANDLERS == null)
            OPEN_HANDLERS = new ArrayList<> ();
        OPEN_HANDLERS.add (handler);
    }

    /**
     * Adds a handler that is invoked when a dialog is closed. Passed is a list of
     * dialogs already open (excluding the one being closed) and the dialog that is
     * being closed.
     * 
     * @param handler
     *                the handler.
     */
    public static void addCloseHandler(BiConsumer<List<Modal<?>>, Modal<?>> handler) {
        if (handler == null)
            return;
        if (CLOSE_HANDLERS == null)
            CLOSE_HANDLERS = new ArrayList<> ();
        CLOSE_HANDLERS.add (handler);
    }

    /************************************************************************
     * Configuration and listeners.
     ************************************************************************/

    /**
     * Enumerates the various types of modal. This relates primarily to how the
     * modal is positioned and related to changes in screen size and contents. It's
     * not related to any visual form (though a visual form may be sub-ordinate).
     */
    public enum Type {
        /**
         * Located at the center of the screen and repositions. Dialog expands with
         * contents to a threshold then the contents scroll.
         */
        CENTER,

        /**
         * Located near the top of the screen and repositions horizontally only. Dialog
         * expands with contents to a threshold then the contents scroll.
         */
        TOP,

        /**
         * Full height dialog that "slides" in from the right.
         */
        SLIDER;
    }

    /**
     * Configuration for the modal.
     */
    public static class Config extends Component.Config {

        /**
         * See {@link #setType(Modal.Type)}.
         */
        protected Type type = Type.CENTER;

        /**
         * See {@link #setWidth(Length)}.
         */
        protected Length width = Length.pct (80);

        /**
         * See {@link #setMaxWidth(Length)}.
         */
        protected Length maxWidth;

        /**
         * See {@link #setMinWidth(Length)}.
         */
        protected Length minWidth;

        /**
         * See {@link #setHeight(Length)}.
         */
        protected Length height;

        /**
         * See {@link #minHeight(Length)}.
         */
        protected Length minHeight;

        /**
         * See {@link #removeOnClose(boolean)}.
         */
        protected boolean removeOnClose = false;

        /**
         * Default constructor.
         */
        public Config() {
            // Nothing.
        }

        /**
         * Getter for {@link #type(Type)}.
         */
        public Modal.Type getType() {
            return type;
        }

        /**
         * Assign the display type of the modal dialog.
         * 
         * @param type
         *             the display type.
         * @return this configuration instance.
         */
        public Config type(Modal.Type type) {
            if (type != null)
                this.type = type;
            return this;
        }

        /**
         * Assigns the width of the modal.
         * <p>
         * Note that the default width is 80%.
         * 
         * @param width
         *              the width.
         * @return this configuration instance.
         */
        public Config width(Length width) {
            this.width = width;
            return this;
        }

        /**
         * Assigns a maximum width of the modal. This is useful when the width is
         * specified as a percentage and you don't want it to grow too wide.
         * 
         * @param maxWidth
         *                 the maximum width.
         * @return this configuration instance.
         */
        public Config maxWidth(Length maxWidth) {
            this.maxWidth = maxWidth;
            return this;
        }

        /**
         * Assigns a minimum width of the modal. This is useful when the width is
         * specified as a percentage and you don't want it to grow too thin.
         * 
         * @param minWidth
         *                 the minimum width.
         * @return this configuration instance.
         */
        public Config minWidth(Length minWidth) {
            this.minWidth = minWidth;
            return this;
        }

        /**
         * Assigns the height (where relevant) of the modal.
         * <p>
         * Note that the default width is 80%.
         * 
         * @param height
         *              the height.
         * @return this configuration instance.
         */
        public Config height(Length height) {
            this.height = height;
            return this;
        }

        /**
         * The minimum height of the content area.
         * <p>
         * The content area is specified to be 100% height with scrolling where its
         * contents overflow. In a popup dialog the windowing shrinks to the match the
         * height of the content, but within specific bounds (at which point the
         * contents scroll). If the contents are dynamic this can lead to the popup
         * "bouncing around" (in that its height varies with changes in the contents)
         * which can be distracting. A solution to this is to specify a minimum content
         * height.
         * 
         * @param minHeight
         *                  the minimum height.
         * @return this configuration instance.
         */
        public Config minHeight(Length minHeight) {
            this.minHeight = minHeight;
            return this;
        }

        /**
         * Convenience to call {@link #removeOnClose(boolean)} passing {@code true}.
         */
        public Config removeOnClose() {
            return removeOnClose (true);
        }

        /**
         * Determines if the modal should be removed from the DOM and disposed of.
         * 
         * @param removeOnClose
         *                      {@code true} to remove.
         * @return this configuration instance.
         */
        public Config removeOnClose(boolean removeOnClose) {
            this.removeOnClose = removeOnClose;
            return this;
        }
    }

    /**
     * Provides a mechanism to close a model. This listener will be added to any
     * content component.
     */
    public interface IModalController extends IListener {

        /**
         * Closes the modal.
         */
        public void close();

        /**
         * Convenience to construct a listener.
         * 
         * @param close
         *              to be invoked when the close event is emitted.
         * @return the listener.
         */
        public static IModalController create(Invoker close) {
            return new IModalController () {

                @Override
                public void close() {
                    close.invoke ();
                }

            };
        }
    }

    /**
     * A content component may implement this and will have the various modal
     * lifecycle methods called on it.
     */
    public interface IModalAware extends IOpenAware, ICloseAware {

        /**
         * Invoked when a close has been requested. If this returns {@code true} then
         * the close request will be propagated.
         * 
         * @param cb
         *           a callback to confirm the close.
         */
        default public void onModalCloseRequested(IModalController cb) {
            cb.close ();
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.ICloseAware#onClose()
         */
        @Override
        default public void onClose() {
            // Nothing.
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.IOpenAware#onOpen()
         */
        @Override
        default public void onOpen() {
            // Nothing.
        }
    }

    /**
     * External listener to modal lifecycle events.
     */
    public interface IModalListener extends IListener {

        /**
         * Indicates that the modal is being opened.
         */
        public void onOpen();

        /**
         * Indicates that the modal is being closed.
         */
        public void onClose();

        /**
         * Creates a listener that invokes the passed close handler.
         * 
         * @param closeHandler
         *                     the handler.
         * @return the listener.
         */
        public static IModalListener close(Invoker closeHandler) {
            return new IModalListener () {

                @Override
                public void onOpen() {
                    // Nothing.
                }

                @Override
                public void onClose() {
                    if (closeHandler != null)
                        closeHandler.invoke ();
                }

            };
        }

        /**
         * Creates a listener that invokes the passed close handler.
         * 
         * @param closeHandler
         *                     the handler.
         * @return the listener.
         */
        public static IModalListener open(Invoker openHandler) {
            return new IModalListener () {

                @Override
                public void onOpen() {
                    if (openHandler != null)
                        openHandler.invoke ();
                }

                @Override
                public void onClose() {
                    // Nothing.
                }

            };
        }
    }

    /************************************************************************
     * Members.
     ************************************************************************/

    /**
     * ID for the top-level modals DIV. This is used to associated a root panel
     * specifically for modals.
     */
    private static String CONTAINER_NAME = "modal-dialogs-container";

    /**
     * The top-level DIV that contains the modals.
     */
    private static Element TOP_EL;
    static {
        TOP_EL = DomSupport.createElement ("div");
        TOP_EL.id = CONTAINER_NAME;
        DomGlobal.document.body.appendChild (TOP_EL);
    }

    /**
     * The contents component.
     */
    private V contents;

    /**
     * See {@link #isOpen()}.
     */
    private boolean open;

    /**
     * Internal flag indicating if the modal has been rendered.
     */
    private boolean _rendered = false;

    /************************************************************************
     * Construction.
     ************************************************************************/

    /**
     * Construct with the default configuration.
     * 
     * @param contents
     *                 the contents of the modal.
     */
    public Modal(V contents) {
        this (new Modal.Config (), contents);
    }

    /**
     * Constructor for sub-classes.
     * 
     * @param config
     *                 the configuration.
     * @param contents
     *                 the contents.
     */
    protected <C extends Modal.Config> Modal(C config, V contents) {
        super (config);
        this.contents = contents;
        if (this.contents == null)
            this.contents = createContents ();
        this.contents = configureContents (this.contents);
        this.contents.addListener (IModalController.create (() -> Modal.this.close ()));
    }

    /**
     * Configures the contents.
     * 
     * @param contents
     *                 the contents to configure (as passed).
     * @return the configured contents (will replace any passed contents; the
     *         default being those contents passed).
     */
    protected V configureContents(V contents) {
        return contents;
    }

    /**
     * Called during construction when the passed contents is {@code null}. This
     * will be passed to {@link #configureContents(IComponent)}.
     * 
     * @return the contents.
     */
    protected V createContents() {
        return null;
    }

    /**
     * Obtains the contents as passed during configuration or by
     * {@link #configureContents(IComponent)}.
     * 
     * @return the contents.
     */
    public V contents() {
        return contents;
    }

    /**
     * Determines if the modal is open (displayed).
     * 
     * @return {@code true} if it is.
     */
    public boolean isOpen() {
        return open;
    }

    /**
     * Opens the modal.
     * <p>
     * If the modal is already opened then nothing will be done (including events
     * being fired).
     * <p>
     * Contents will be scrolled to the top. This will occur before the contents
     * {@link IOpenAware#onOpen()} method is called (if the contents implements
     * {@link IOpenAware}).
     */
    public void open() {
        // If already open then nothing to be done.
        if (open)
            return;

        // If we haven't rendered yet we need to insert the dialog into the
        // dialog container at the root panel.
        if (!_rendered) {
            _rendered = true;
            try {
                // Bind the modal to the DOM (and initiate rendering).
                bind (CONTAINER_NAME);
            } catch (Throwable e) {
                // Sometimes we get onAttach being called twice. Not sure but
                // appears to be related to the browser event loop (i.e. things
                // happening too fast). It seems to be harmless so just swallow
                // the exception.
            }

            // We need to exit the browser render loop before opening now.
            TimerSupport.defer (() -> open ());
            return;
        }

        // Rendered and not yet open, so go through the process.
        fireEvent (IModalListener.class).onOpen ();
        onOpen ();
        _open (DIALOGS.size ());
        _scrollToTop ();
        open = true;
        if (contents instanceof IOpenAware)
            ((IOpenAware) contents).onOpen ();
        if (OPEN_HANDLERS != null) {
            OPEN_HANDLERS.forEach (handler -> {
                try {
                    handler.accept (DIALOGS, this);
                } catch (Throwable e) {
                    Logger.reportUncaughtException (e);
                }
            });
        }
        DIALOGS.add (this);
        if (BLUR_TARGET != null)
            BLUR_TARGET.classList.add (LocalCSS.instance ().blur ());
    }

    /**
     * Opens the modal at the given level.
     * 
     * @param level
     *              (the number of dialogs currently open (between 0 and 6).
     */
    protected void _open(int level) {
        getRoot ().classList.add (styles ().show ());
        getRoot ().classList.remove (styles ().z1 ());
        getRoot ().classList.remove (styles ().z2 ());
        getRoot ().classList.remove (styles ().z3 ());
        getRoot ().classList.remove (styles ().z4 ());
        getRoot ().classList.remove (styles ().z5 ());
        getRoot ().classList.remove (styles ().z6 ());
        if (level == 1)
            getRoot ().classList.add (styles ().z1 ());
        else if (level == 2)
            getRoot ().classList.add (styles ().z2 ());
        else if (level == 3)
            getRoot ().classList.add (styles ().z3 ());
        else if (level == 4)
            getRoot ().classList.add (styles ().z4 ());
        else if (level == 5)
            getRoot ().classList.add (styles ().z5 ());
        else if (level >= 6)
            getRoot ().classList.add (styles ().z6 ());
        if (config ().type == Modal.Type.SLIDER) {
            // This is needed to ensure that the dialog is displayed then
            // opened (by assigning the "slider" style to the width
            // element). This allows the CSS transaction on the position
            // "left" to have an effect allowing the dialog to appear to
            // slide out.
            TimerSupport.defer (() -> widthEl.addClass (styles ().slider ()));
        }
    }

    /**
     * Invoked by {@link #open()} when the dialog is opened. This is called after
     * {@link IModalListener#onOpen()}.
     */
    protected void onOpen() {
        // Nothing.
    }

    /**
     * Closes (hides) the modal.
     * <p>
     * If the modal is already closed then nothing will be done (including events
     * being fired).
     */
    public void close() {
        if (!open)
            return;
        onCloseRequested (new IModalController () {

            @Override
            public void close() {
                _close ();
            }
        });
    }

    /**
     * Internal. Called by {@link #close()} to form an actual close.
     */
    protected void _close() {
        // Double check that this is not open (in case the contents were modal
        // aware and there was a close between the contents confirming a close).
        if (!open)
            return;
        onClose ();
        fireEvent (IModalListener.class).onClose ();
        getRoot ().classList.remove (styles ().show ());
        if (config ().type == Modal.Type.SLIDER)
            widthEl.removeClass (styles ().slider ());
        open = false;
        DIALOGS.remove (this);
        onAfterClose ();
        if (contents instanceof ICloseAware)
            ((ICloseAware) contents).onClose ();
        if (CLOSE_HANDLERS != null) {
            CLOSE_HANDLERS.forEach (handler -> {
                try {
                    handler.accept (DIALOGS, this);
                } catch (Throwable e) {
                    // Nothing.
                }
            });
        }
        if (DIALOGS.isEmpty ()) {
            if (BLUR_TARGET != null)
                BLUR_TARGET.classList.remove (LocalCSS.instance ().blur ());
        }
    }

    /**
     * Invoked just prior to the modal being closed.
     * <p>
     * The default behaviour to to determine if the contents implements
     * {@link IModalAware} and if it does then to delegate to that. If it does not
     * then it calls {@link #onCloseRequested()} and proceeds if the return value is
     * {@code true}.
     */
    protected void onCloseRequested(IModalController cb) {
        if (contents instanceof IModalAware)
            ((IModalAware) contents).onModalCloseRequested (cb);
        else if (onCloseRequested ())
            cb.close ();
    }

    /**
     * See {@link #onCloseRequested()}.
     * 
     * @return {@code true} if to close.
     */
    protected boolean onCloseRequested() {
        return true;
    }

    /**
     * Invoked just prior to the modal being closed.
     */
    protected void onClose() {
        // Nothing.
    }

    /**
     * Invoked just after to the modal was closed.
     * <p>
     * The default behaviour is to remove the dialog (see
     * {@link #removeFromParent()}) if {@link Config#removeOnClose(boolean)} was
     * set.
     */
    protected void onAfterClose() {
        if (config ().removeOnClose)
            dispose ();
    }

    /**
     * Scrolls the content area to the top.
     */
    public void scrollContentsToTop() {
        if (!isRendered ())
            return;
        _scrollToTop ();
    }

    /**
     * Scroll the contents area to the top.
     */
    protected void _scrollToTop() {
        if (contentsEl != null)
            contentsEl.scrollTop = 0;
    }

    /************************************************************************
     * Presentation.
     ************************************************************************/

    /**
     * Element that contains the contents.
     */
    protected Element contentsEl;

    /**
     * Element that defines the width (for application of the width configuration).
     */
    protected JQueryElement widthEl;

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#onAfterRender()
     */
    @Override
    protected void onAfterRender() {
        // Bind the contents to the content element.
        super.onAfterRender ();
        Length width = config ().width;
        if (width == null)
            width = Length.pct (80);
        CSS.WIDTH.with (width).apply (widthEl);
        if (config ().minWidth != null)
            CSS.MIN_WIDTH.with (config ().minWidth).apply (widthEl);
        if (config ().maxWidth != null)
            CSS.MAX_WIDTH.with (config ().maxWidth).apply (widthEl);
        if (config ().type == Modal.Type.CENTER) {
            widthEl.addClass (styles ().center ());
        } else if (config ().type == Modal.Type.SLIDER) {
            getRoot ().classList.add (styles ().slider ());
            CSS.LEFT.with (width).apply (widthEl);
        }
        if (config ().minHeight != null)
            CSS.MIN_HEIGHT.with (config ().minHeight).apply (JQuery.$ (contentsEl));
        if (config ().height != null)
            CSS.HEIGHT.with (config ().height).apply (JQuery.$ (contentsEl));
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#buildNode(com.effacy.jui.core.client.component.Component.Config)
     */
    @Override
    protected INodeProvider buildNode(Config data) {
        return DomBuilder.div (outer -> {
            outer.addClassName (styles ().outer ());
            outer.div ().addClassName (styles ().mask ());
            outer.div (inner -> {
                inner.addClassName (styles ().inner ());
                inner.div (width -> {
                    width.by ("width");
                    width.addClassName (styles ().width ());
                    width.div (height -> {
                        height.addClassName (styles ().height ());
                        height.div (wrap -> {
                            wrap.addClassName (styles ().wrap ());
                            wrap.id ("contents").by ("contents");
                            wrap.apply (attach (this.contents));
                        });
                    });
                });
            });
        }).build (tree -> {
            widthEl = JQuery.$ ((Element) tree.first ("width"));
            contentsEl = tree.first ("contents");
        });
    }

    /************************************************************************
     * CSS styles.
     ************************************************************************/

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#styles()
     */
    @Override
    protected ILocalCSS styles() {
        return LocalCSS.instance ();
    }

    /**
     * CSS styles.
     */
    public interface ILocalCSS extends IComponentCSS {

        /**
         * Style applied at the component level to present as a slider (from the right).
         */
        public String slider();

        /**
         * Style applied at the component level to vertically center the modal.
         */
        public String center();

        /**
         * Style applied when a modal is opened (being shown).
         */
        public String show();

        /**
         * Outer container for the modal (spans the viewport).
         */
        public String outer();

        /**
         * Background mask for the modal.
         */
        public String mask();

        /**
         * Structural element.
         */
        public String inner();

        /**
         * Dialog contents.
         */
        public String contents();

        /**
         * Structural element.
         */
        public String width();

        /**
         * Structural element.
         */
        public String height();

        /**
         * Structural element.
         */
        public String wrap();

        /**
         * Adjusts the z-index for the modal (allowing modals on modals).
         */
        public String z1();

        /**
         * See {@link #z1()}.
         */
        public String z2();

        /**
         * See {@link #z1()}.
         */
        public String z3();

        /**
         * See {@link #z1()}.
         */
        public String z4();

        /**
         * See {@link #z1()}.
         */
        public String z5();

        /**
         * See {@link #z1()}.
         */
        public String z6();

        /**
         * To apply to blur.
         */
        public String blur();

    }

    /**
     * Component CSS.
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        "com/effacy/jui/ui/client/modal/Modal.css",
        "com/effacy/jui/ui/client/modal/Modal_Override.css"
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
