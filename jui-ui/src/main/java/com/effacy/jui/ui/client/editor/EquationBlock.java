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
package com.effacy.jui.ui.client.editor;

import java.util.ArrayList;
import java.util.List;

import com.effacy.jui.core.client.component.SimpleComponent;
import com.effacy.jui.core.client.dom.DomSupport;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.UIEvent;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.P;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.css.CSSInjector;
import com.effacy.jui.core.client.dom.jquery.JQuery;
import com.effacy.jui.platform.css.client.CssDeclaration;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.platform.util.client.ScriptInjector;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.ui.client.control.Controls;
import com.effacy.jui.ui.client.control.TextAreaControl;
import com.effacy.jui.ui.client.editor.Editor.DebugMode;
import com.effacy.jui.ui.client.editor.Editor.IEditor;
import com.effacy.jui.ui.client.editor.model.ContentBlock;
import com.effacy.jui.ui.client.editor.model.ContentBlock.BlockType;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.effacy.jui.ui.client.modal.ModalDialogCreator;

import elemental2.dom.Element;

/**
 * See katex.org. On the server side see
 * https://github.com/opencollab/jlatexmath.
 * <p>
 * Makes use of {@link EditorSupport#latex(elemental2.dom.Node, String, boolean)}.
 */
public class EquationBlock extends Block {

    /**
     * The KaTeX version (used to retrieve the relevant JS and CSS).
     */
    public static String VERSION = "0.16.9";

    /**
     * Loading the associated JS.
     */
    static private List<Runnable> LOADING = new ArrayList<> ();
    static {
        ScriptInjector.injectFromUrl ("https://cdn.jsdelivr.net/npm/katex@" + VERSION + "/dist/katex.min.js", outcome -> {
            if (LOADING != null) {
                LOADING.forEach (r -> r.run ());
                LOADING = null;
            }
        });
        CSSInjector.injectFromUrl ("https://cdn.jsdelivr.net/npm/katex@" + VERSION + "/dist/katex.min.css");
    }

    /**
     * The location to render the component.
     */
    private Element equationEl;

    /**
     * The message to display when there is no equation defined (i.e. empty source).
     */
    public static String NO_EQUATION_MESSAGE = "No equation to show!";

    /**
     * The message to display when there is an error with the equation.
     */
    public static String ERROR_EQUATION_MESSAGE = "Error with the equation!";

    /**
     * Example source for a single-line equation.
     */
    public static final String DEMO_1 = "\\int^2_1 e^{-x^2}dx";

    /**
     * Example source for a multi-line equation.
     */
    public static final String DEMO_2 = "\\begin{align*}\n"
        + "x&=y         & w&=z             & a&=b+c\\\\\n"
        + "2x&=-y       & 3w&=\\frac{1}{2}z & a&=b\\\\\n"
        + "-4 + 5x&=2+y & w+2&=-1+w        & ab&=cb\\\\\n"
        + "\\end{align*}\n";

    /**
     * Construct with editor and some initial source.
     * 
     * @param editor
     *                the editor.
     * @param source
     *                the source.
     */
    public EquationBlock(IEditor editor, ContentBlock blk) {
        super (BlockType.EQUATION, editor, blk);
    }

    @Override
    public Block clone() {
        return new EquationBlock (editor, content().clone ());
    }
    
    @Override
    public boolean navigable() {
        return false;
    }

    @Override
    public String label() {
        return "Equation";
    }

    @Override
    protected Element createContainer() {
        Element containerEl = super.createContainer();
        Wrap.$ (containerEl).style (styles().equation (), styles().content_editable()).$ (root -> {
            root.on (UIEventType.ONCLICK);
            Div.$ (root).by ("equation");
            Div.$ (root).style (styles ().equation_empty ()).$ (empty -> {
                Em.$ (empty).style (FontAwesome.squareRootVariable ());
                P.$ (empty).text (NO_EQUATION_MESSAGE);
            });
            Div.$ (root).style (styles ().equation_error ()).$ (empty -> {
                Em.$ (empty).style (FontAwesome.bug ());
                P.$ (empty).text (ERROR_EQUATION_MESSAGE);
            });
        }).build (dom -> {
            equationEl = dom.first ("equation");
        });
        return containerEl;
    }

    @Override
    protected boolean _handleEvent(UIEvent event) {
        if (DebugMode.EVENT.set () && !event.isEvent (UIEventType.ONMOUSEENTER, UIEventType.ONMOUSELEAVE, UIEventType.ONMOUSEMOVE))
            Logger.trace ("{editor-event::equation_block}", event.toString ());
        if (event.isEvent(UIEventType.ONCLICK)) {
            if (DebugMode.EVENT.set ())
                Logger.trace ("{editor-event::equation_block}", event.toString () + " -> open dialog");
            new EditDialog ().open ();
            return true;
        }
        if (DebugMode.EVENT.set () && !event.isEvent (UIEventType.ONMOUSEENTER, UIEventType.ONMOUSELEAVE, UIEventType.ONMOUSEMOVE))
            Logger.trace ("{editor-event::diagram_block}", event.toString () + " -> propagate down");
        return super._handleEvent(event);
    }

    /**
     * Updates the content with new source.
     * 
     * @param source
     *               the source.
     */
    protected void update(String source) {
        ContentBlock blk = new ContentBlock (type);
        if (!StringSupport.empty (source))
            blk.split (source);

        update (blk);
    }

    @Override
    protected void refresh(ContentBlock content) {
        if (content == null) 
            return;

        String source = content.contractLines ();
        containerEl.classList.remove (styles ().equation_empty (), styles ().equation_error());
        if (StringSupport.empty(source)) {
            containerEl.classList.add (styles().equation_empty());
        } else {
            if (LOADING != null) {
                LOADING.add (() -> {
                    String err = EditorSupport.latex (equationEl, source, true);
                    if (!StringSupport.empty (err))
                        containerEl.classList.add (styles().equation_error ());
                });
            } else {
                String err = EditorSupport.latex (equationEl, source, true);
                if (!StringSupport.empty (err))
                    containerEl.classList.add (styles().equation_error ());
            }
        }
    }

    

    /**
     * Dialog panel for the diagram editor. Modal is wrapped.
     */
    class EditDialog extends SimpleComponent {

        /**
         * Wraps this panel into a dialog.
         */
        public void open() {
            ModalDialogCreator.build (this, cfg -> {
                cfg.title ("Equation editor");
                cfg.removeOnClose ();
                cfg.action().label ("close").link ();
                cfg.action ().label ("Apply").icon (FontAwesome.turnDown (FontAwesome.Option.FLIP_HORIZONTAL)).handler (ctx -> {
                    ctx.contents ().apply ();
                    ctx.success ();
                });
            }).open ();
        }

        /**
         * Location to preview the equation.
         */
        private Element equationEl;

        /**
         * Rendering of error messages.
         */
        private Element errorEl;

        /**
         * The source entry control.
         */
        private TextAreaControl contentCtl;

        @Override
        protected INodeProvider buildNode(Element el) {
            IEquationBlockCSS styles = EquationBlock.this.styles ();
            String source = EquationBlock.this.content ().contractLines ();
            return Wrap.$ (el).$ (root -> {
                root.style (styles.equation_editor ());
                Div.$ (root).style (styles.equation_editor_top ()).$ (outer -> {
                    Div.$ (outer).style (styles.equation_editor_source ()).$ (left -> {
                        left.insert (contentCtl = Controls.textarea (cfg -> {
                            cfg.max (3000);
                            cfg.placeholder ("Code for the equation");
                            cfg.nowrap ();
                            cfg.modifiedHandler ((ctl, val, prior) -> {
                                if (val == null)
                                    val = "";
                                refresh (val);
                            });
                        }, ctl -> {
                            ctl.setValue (source);
                        }));
                    });
                    Div.$ (outer).style (styles.equation_editor_preview ()).$ (preview -> {
                        Div.$ (preview).by ("equation");
                        Div.$ (preview).style (styles.equation_editor_error ()).by ("error");
                    });
                });
            }).build (dom -> {
                equationEl = dom.first ("equation");
                errorEl = dom.first ("error");
                if (!StringSupport.empty (source))
                    refresh (source);
            });
        }

        /**
         * Applies changes.
         */
        public void apply() {
            EquationBlock.this.update (contentCtl.value ());
        }

        /**
         * Refreshes the content, including placement of errors.
         */
        protected void refresh(String source) {
            String err = EditorSupport.latex (equationEl, source, true);
            if (!StringSupport.empty (err)) {
                if (err.startsWith ("KaTeX parse error:"))
                    err = err.substring (19);
                DomSupport.innerText (errorEl, err);
                JQuery.$ (errorEl).show ();
            } else
                JQuery.$ (errorEl).hide ();
        }
    }

    /**
     * Styles needed for diagrams.
     */
    public interface IEquationBlockCSS extends CssDeclaration {

        public String equation();

        public String equation_empty();

        public String equation_error();

        public String equation_editor();

        public String equation_editor_top();

        public String equation_editor_source();

        public String equation_editor_preview();

        public String equation_editor_error();
    }
    
}
