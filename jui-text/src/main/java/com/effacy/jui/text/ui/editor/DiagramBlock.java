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
package com.effacy.jui.text.ui.editor;

import java.util.ArrayList;
import java.util.List;

import com.effacy.jui.core.client.component.SimpleComponent;
import com.effacy.jui.core.client.dom.DomSupport;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.UIEvent;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.Img;
import com.effacy.jui.core.client.dom.builder.Label;
import com.effacy.jui.core.client.dom.builder.P;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.dom.jquery.JQuery;
import com.effacy.jui.platform.css.client.CssDeclaration;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.platform.util.client.ScriptInjector;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.text.type.FormattedBlock;
import com.effacy.jui.text.type.FormattedBlock.BlockType;
import com.effacy.jui.text.ui.editor.Editor.DebugMode;
import com.effacy.jui.text.ui.editor.Editor.IEditor;
import com.effacy.jui.ui.client.control.Controls;
import com.effacy.jui.ui.client.control.TextAreaControl;
import com.effacy.jui.ui.client.control.TextControl;
import com.effacy.jui.ui.client.fragments.Btn;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.effacy.jui.ui.client.modal.ModalDialogCreator;

import elemental2.dom.Element;

/**
 * Diagram block that makes use of {@link http://plantuml.com}.
 */
public class DiagramBlock extends Block {

    /**
     * Loading the associated JS.
     */
    static private List<Runnable> LOADING = new ArrayList<> ();
    static {
        // This is the hosted version, the default is that bundled
        // ScriptInjector.injectFromUrl ("https://cdn.rawgit.com/jmnote/plantuml-encoder/d133f316/dist/plantuml-encoder.min.js", outcome -> {
        //     LOADED = true;
        // });
        ScriptInjector.injectFromModuleBase ("plantuml-encoder.min.js", outcome -> {
            if (LOADING != null) {
                LOADING.forEach (r -> r.run ());
                LOADING = null;
            }
        });
    }

    /**
     * Meta-data field name for the caption.
     */
    public static String META_CAPTION = "caption";

    /**
     * The base URL for converting encoded UML source to an image. The default is
     * the PlantUML demonstration URL. The jui-layground project provides an example
     * of how one can host ones own converter (see the class {@code UMLController}).
     * <p>
     * If you want to use a different base then modify this in your applications
     * entry point.
     */
    public static String BASE_URL = "//www.plantuml.com/plantuml/img/";

    /**
     * The message to display when there is no diagrm defined (i.e. empty source).
     */
    public static String NO_DIAGRAM_MESSAGE = "No diagram to show!";

    /**
     * Example source for a UML diagram.
     */
    public static final String DEMO_1 = "Alice -> Jill: Authentication Request\n"
        + "Jill --> Alice: Authentication Response\n"
        + "Alice -> Jill: Another authentication Request\n"
        + "Alice <-- Jill: another authentication Response";

    /**
     * Example source for a DITAA diagram.
     */
    public static final String DEMO_2 = "@startditaa\n"
        + "+--------+   +-------+    +-------+\n"
        + "|        +---+ ditaa +--> |       |\n"
        + "|  Text  |   +-------+    |diagram|\n"
        + "|Document|   |!magic!|    |       |\n"
        + "|     {d}|   |       |    |       |\n"
        + "+---+----+   +-------+    +-------+\n"
        + "    :                         ^\n"
        + "    |       Lots of work      |\n"
        + "    +-------------------------+\n"
        + "@endditaa\n";

    /**
     * The image element to render the content.
     */
    private Element imageEl;

    /**
     * The element that contains the caption.
     */
    private Element captionEl;

    /**
     * Construct with editor and some initial source.
     * 
     * @param editor
     *                the editor.
     * @param source
     *                the source.
     * @param caption
     *                the caption.
     */
    public DiagramBlock(IEditor editor, FormattedBlock blk) {
        super (BlockType.DIA, editor, blk);
    }

    @Override
    public Block clone() {
        return new DiagramBlock (editor, content ().clone ());
    }

    @Override
    public boolean navigable() {
        return false;
    }

    @Override
    public String label() {
        return "Diagram";
    }

    @Override
    protected Element createContainer() {
        Element containerEl = super.createContainer();
        Wrap.$ (containerEl).style (styles ().diagram (), styles().content_editable()).$ (el -> {
            el.on (UIEventType.ONCLICK);
            Img.$ (el).by ("image");
            Div.$ (el).style (styles().diagram_empty ()).$ (empty -> {
                Em.$ (empty).style (FontAwesome.images ());
                P.$ (empty).text (NO_DIAGRAM_MESSAGE);
            });
            P.$ (el).by ("caption");
        }).build (dom -> {
            imageEl = dom.first ("image");
            captionEl = dom.first ("caption");
        });
        return containerEl;
    }

    /**
     * Updates the diagram with new source.
     * 
     * @param source
     *               the source to update.
     */
    protected void update(String source, String caption) {
        FormattedBlock blk = new FormattedBlock (type);
        if (!StringSupport.empty (caption))
            blk.meta (META_CAPTION, caption);
        if (!StringSupport.empty (source))
            blk.split (source);
        update (blk);
    }

    @Override
    protected void refresh(FormattedBlock content) {
        if (content == null) 
            return;

        // Update the caption.
        if (StringSupport.empty (content.meta (META_CAPTION))) {
            JQuery.$ (captionEl).hide ();
        } else {
            DomSupport.innerText (captionEl, content.meta (META_CAPTION));
            JQuery.$ (captionEl).show ();
        }

        // Update the diagram.
        String source = content.contractLines();
        if (StringSupport.empty (source)) {
            containerEl.classList.add (styles ().diagram_empty ());
            return;
        }
        containerEl.classList.remove (styles ().diagram_empty ());
        if (LOADING != null) {
            LOADING.add (() -> {
                String url = EditorSupport.diagram (BASE_URL, source);
                imageEl.setAttribute ("src", url);
            });
        } else {
            String url = EditorSupport.diagram (BASE_URL, source);
            imageEl.setAttribute ("src", url);
        }
    }

    @Override
    protected boolean _handleEvent(UIEvent event) {
        if (DebugMode.EVENT.set () && !event.isEvent (UIEventType.ONMOUSEENTER, UIEventType.ONMOUSELEAVE, UIEventType.ONMOUSEMOVE))
            Logger.trace ("{editor-event::diagram_block}", event.toString ());
        if (event.isEvent(UIEventType.ONCLICK)) {
            if (DebugMode.EVENT.set ())
                Logger.trace ("{editor-event::diagram_block}", event.toString () + " -> open dialog");
            new EditDialog ().open ();
            return true;
        }
        if (DebugMode.EVENT.set () && !event.isEvent (UIEventType.ONMOUSEENTER, UIEventType.ONMOUSELEAVE, UIEventType.ONMOUSEMOVE))
            Logger.trace ("{editor-event::diagram_block}", event.toString () + " -> propagate down");
        return super._handleEvent(event);
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
                cfg.title ("Diagram editor");
                cfg.removeOnClose ();
                cfg.action().label ("close").link ();
                cfg.action ().label ("Apply").icon (FontAwesome.turnDown (FontAwesome.Option.FLIP_HORIZONTAL)).handler (ctx -> {
                    ctx.contents ().apply ();
                    ctx.success ();
                });
            }).open ();
        }

        /**
         * Holds the image for preview.
         */
        private Element imageEl;

        /**
         * The preview element to toggle preview mode.
         */
        private Element previewEl;

        /**
         * The source entry control.
         */
        private TextAreaControl contentCtl;

        /**
         * The caption entry control.
         */
        private TextControl captionCtl;

        /**
         * The source at last preview, used to activate preview mode after changes
         * between previews.
         */
        private String lastPreviewSource;

        @Override
        protected INodeProvider buildNode(Element el) {
            IDiagramBlockCSS styles = DiagramBlock.this.styles ();
            FormattedBlock content = DiagramBlock.this.content();
            String source = content.contractLines ();
            String caption = content.meta (META_CAPTION);
            return Wrap.$ (el).$ (root -> {
                root.style (DiagramBlock.this.styles ().diagram_editor ());
                Div.$ (root).style (styles.diagram_editor_top ()).$ (outer -> {
                    Div.$ (outer).style (styles.diagram_editor_source ()).$ (left -> {
                        left.insert (contentCtl = Controls.textarea (cfg -> {
                            cfg.max (3000);
                            cfg.placeholder ("Code for the diagram");
                            cfg.nowrap ();
                            cfg.modifiedHandler ((ctl, val, prior) -> {
                                if (val == null)
                                    val = "";
                                if (!val.equals (lastPreviewSource)) {
                                    // Activate preview.
                                    previewEl.classList.add (styles.diagram_editor_mask ());
                                } else {
                                    previewEl.classList.remove (styles.diagram_editor_mask ());
                                }
                            });
                        }, ctl -> {
                            ctl.setValue (source);
                            this.lastPreviewSource = source;
                        }));
                    });
                    Div.$ (outer).style (styles.diagram_editor_preview ()).$ (preview -> {
                        preview.by ("preview");
                        if (StringSupport.empty(source))
                            Img.$ (preview).by ("image");
                        else
                            Img.$ (preview).by ("image").attr("src", EditorSupport.diagram (BASE_URL, source));
                        Div.$ (preview).style (styles.diagram_editor_mask ());
                        Div.$ (preview).$ (btn -> {
                            Btn.$ (btn, "Preview").icon (FontAwesome.eye ()).variant (Btn.Variant.STANDARD_EXPANDED).onclick (() -> {
                                preview ();
                            });
                        });
                    });
                });
                Div.$ (root).style(styles.diagram_editor_caption ()).$ (cp -> {
                    Label.$ (cp).text ("Caption");
                    cp.insert (captionCtl = Controls.text (cfg -> {
                        cfg.width (Length.pct(100));
                        cfg.max (1000);
                        cfg.placeholder ("(Optional) caption to display under the diagram");
                    }, ctl -> {
                        ctl.setValue (caption);
                    }));
                });
            }).build (dom -> {
                imageEl = dom.first ("image");
                previewEl = dom.first ("preview");
            });
        }

        /**
         * Generates a preview.
         */
        public void preview() {
            lastPreviewSource = contentCtl.value ();
            String url = EditorSupport.diagram (BASE_URL, lastPreviewSource);
            imageEl.setAttribute ("src", url);
            previewEl.classList.remove (DiagramBlock.this.styles ().diagram_editor_mask ());
        }

        /**
         * Applies changes.
         */
        public void apply() {
            DiagramBlock.this.update (contentCtl.value (), captionCtl.value ());
        }
     }

    /**
     * Styles needed for diagrams.
     */
    public interface IDiagramBlockCSS extends CssDeclaration {

        public String diagram();
        
        public String diagram_empty();

        public String diagram_editor();

        public String diagram_editor_top();

        public String diagram_editor_caption();

        public String diagram_editor_source();

        public String diagram_editor_preview();

        public String diagram_editor_mask();
    }
    
}

