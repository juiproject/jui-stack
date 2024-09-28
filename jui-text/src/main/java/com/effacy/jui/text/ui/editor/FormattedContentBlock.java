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

import java.util.Set;

import com.effacy.jui.core.client.dom.DomSupport;
import com.effacy.jui.core.client.dom.UIEvent;
import com.effacy.jui.core.client.dom.UIEvent.KeyCode;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.platform.util.client.Itr;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.platform.util.client.TimerSupport;
import com.effacy.jui.text.type.FormattedBlock;
import com.effacy.jui.text.type.FormattedBlock.BlockType;
import com.effacy.jui.text.type.FormattedLine.FormatType;
import com.effacy.jui.text.ui.editor.Editor.DebugMode;
import com.effacy.jui.text.ui.editor.Editor.IEditor;

import elemental2.dom.DOMRect;
import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.File;
import elemental2.dom.HTMLImageElement;
import elemental2.dom.Range;
import jsinterop.base.Js;

/**
 * A block that handles formatted text.
 */
public abstract class FormattedContentBlock extends Block implements IFormattedContent {

    /**
     * The container of formatted text.
     */
    private FormattedContent container;

    /**
     * Construct using the passed content block.
     * 
     * @param editor
     *                the owning editor.
     * @param content
     *                the content to render (can be {@code null}).
     */
    public FormattedContentBlock(BlockType type, IEditor editor, FormattedBlock content) {
        super (type, editor, content);
    }

    @Override
    protected void populate(FormattedBlock content) {
        container.populate (content);
    }

    @Override
    protected Element createContainer() {
        Element containerEl = createContentArea ();
        this.container = new FormattedContent (containerEl);
        containerEl.classList.add (styles ().content_editable ());
        containerEl.setAttribute ("contenteditable", true);
        UIEventType.ONFOCUS.attach (containerEl);
        UIEventType.ONBLUR.attach (containerEl);
        UIEventType.ONKEYDOWN.attach (containerEl);
        UIEventType.ONPASTE.attach (containerEl);
        UIEventType.ONKEYUP.attach (containerEl);
        // Needed for tool activation on selection.
        UIEventType.ONMOUSEUP.attach (containerEl);
        return containerEl;
    }

    /**
     * Creates the actual content area.
     * @return the content area.
     */
    protected Element createContentArea() {
        return DomSupport.createDiv ();
    }

    @Override
    protected void refresh(FormattedBlock content) {
        DomSupport.removeAllChildren (container.el ());
        if ((content == null) || content.getLines ().isEmpty ()) {
            container.el ().classList.add (styles().empty());
            container.el ().setAttribute ("placeholder", "Please enter some content!");
        } else {
            Itr.forEach (content.getLines(), (c,v) -> {
                if (!c.first ())
                    container.el ().append (DomGlobal.document.createElement ("br"));
                v.traverse ((text,styles) -> {
                    if (styles.length == 0) {
                        container.el ().append (DomGlobal.document.createTextNode (text));
                    } else {
                        Element el = DomGlobal.document.createElement("span");
                        for (FormatType format : styles) {
                            String style = FormattedContent.resolve (format);
                            if (style != null)
                                el.classList.add (style);
                        }
                        container.el ().append (el);
                        el.append (DomGlobal.document.createTextNode (text));
                    }
                });
            });

            // Ensure the content is clean.
            EditorSupport.clean (container.el ());
        }
    }

    @Override
    public Range apply(Range range, FormatType format) {
        return container.apply (range, format);
    }

    @Override
    public Range remove(Range range, FormatType format) {
        return container.remove (range, format);
    }

    @Override
    public Set<FormatType> formats(Range range) {
        return container.formats (range);
    }

    @Override
    public boolean propagateActivationEvent(UIEvent event) {
        return (event.isEvent(UIEventType.ONMOUSEDOWN) && DomSupport.isChildOf(event.getTarget(), container.el ()));
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.ui.client.editor.Block#handleEvent(com.effacy.jui.core.client.dom.UIEvent)
     */
    @Override
    protected boolean _handleEvent(UIEvent event) {
        if (event.isEvent (UIEventType.ONPASTE))
            return onPaste (event);

        // Process specific key-based behaviours.
        if (event.isEvent (UIEventType.ONKEYDOWN)) {
            // For now just invalidate (rather than trying to be tricky).
            invalidate ();

            // Deactivate tools on key presses.
            deactivateSidetool ();
            deactivateToolbar ();

            // Process tabbing (indent and outdent).
            if (KeyCode.TAB.is (event)) {
                if (onTabKey(event)) {
                    event.stopEvent();
                    return true;
                }
            }

            // Enter key splits the paragraph (including endpoint splits which are just
            // insertions of empty paragraphs).
            if (KeyCode.ENTER.is (event) && !event.isShiftKey ()) {
                if (onEnterKey (event)) {
                    event.stopEvent ();
                    return true;
                }
            }

            // Backspace at the begining performs a merge into the prior (if there is one),
            if (KeyCode.BACKSPACE.is (event)) {
                if (onBackSpaceKey (event)) {
                    event.stopEvent ();
                    return true;
                }
            }
        }

        // Perform general cleanups and state management.
        if (event.isEvent (UIEventType.ONKEYDOWN, UIEventType.ONKEYUP, UIEventType.ONMOUSEUP)) {
            // Check if empty and place the empty style (or remove accordingly). This is
            // deferred to the end of the UI loop.
            TimerSupport.defer (() -> {
                if (contentEl ().childNodes.length == 0) {
                    contentEl ().classList.add (styles ().empty ());
                } else {
                    contentEl ().classList.remove (styles ().empty ());
                    EditorSupport.clean (contentEl ());
                }
            });

            // Check selection to generate an activation of tool state.
            Range r = DomGlobal.document.getSelection ().getRangeAt (0);
            if (EditorSupport.bounded (contentEl (), r))
                updateSelection (r);

            return true;
        }

        return super._handleEvent (event);
    }

    /**
     * Action a paste from the clipboard.
     * 
     * @param event
     *              the underlying event.
     * @return {@code true} if the event was handled.
     */
    protected boolean onPaste(UIEvent event) {
        elemental2.dom.ClipboardEvent e = Js.cast (event.getEvent ());
        if (DomGlobal.document.getSelection().rangeCount > 0) {
            Range r = DomGlobal.document.getSelection().getRangeAt (0);
            invalidate ();
            int start = EditorSupport.positionAtStart (contentEl (), r);
            int end = EditorSupport.positionAtEnd (contentEl (), r);
            Element el = DomGlobal.document.createElement ("div");
            if (e.clipboardData.files.length > 0) {
                File file = e.clipboardData.files.getAt (0);
                if (file.type.startsWith ("image/")) {
                    HTMLImageElement img = Js.cast (DomGlobal.document.createElement("img"));
                    contentEl ().append (img);
                    img.src = elemental2.dom.URL.createObjectURL (file);
                    Logger.info ("File: " + file.type + " " + file.name + " " + file.size);
                }
            } else {
                // Assume paste is HTML content.
                String clipboard = e.clipboardData.getData ("text/html");
                Element n = EditorSupport.paste (clipboard);
                if (n != null) {
                    FormattedBlock blk = new FormattedBlock (type);
                    new FormattedContent (n).populate (blk);
                    n.remove ();
                    el.innerHTML = clipboard;
                    String text = DomSupport.innerText (el);
                    el.remove ();
                    content ().remove (start, end - start).insert (start, text);
                    refresh ();
                    EditorSupport.positionCursorAt(contentEl(), start + text.length ());
                }
            }
        }
        event.stopEvent ();
        return true;
    }

    /**
     * Action a press on the tab key.
     * 
     * @param event
     *              the underlying event.
     * @return {@code true} if the event was handled.
     */
    protected boolean onTabKey(UIEvent event) {
        // Cannot tab if is the first item.
        if (priorBlock() != null) {
            if (event.isShiftKey())
                outdent ();
            else
                indent ();
        }
        return true;
    }

    /**
     * Action a press on the enter key.
     * 
     * @param event
     *              the underlying event.
     * @return {@code true} if the event was handled.
     */
    protected boolean onEnterKey(UIEvent event) {
        if (EditorSupport.isCursorAtEnd (contentEl ())) {
            // If we are at the end then we just insert a new block after and transition to
            // that (PA.02).
            editor ().onInsertNext (createSplitBlock (new FormattedBlock (type).indent (this.indent)), true);
        } else if (EditorSupport.isCursorAtStart (contentEl ())) {
            // If we are at the start then we just insert a new block prior but do not
            // transition to it (PA.01)
            editor ().onInsertPrior (createSplitBlock (new FormattedBlock (type).indent (this.indent)), false);
        } else {
            // Obtain the current content and split. We call refresh as that content will be modified.
            Block blk = createSplitBlock (content ().split (EditorSupport.positionOfCursor (contentEl ())).transform (type));
            editor ().onInsertNext (blk, true);
            refresh ();
        }
        return true;
    }

    /**
     * Action a press on the backspace key.
     * 
     * @param event
     *              the underlying event.
     * @return {@code true} if the event was handled.
     */
    protected boolean onBackSpaceKey(UIEvent event) {
        if (EditorSupport.insertionPoint () && (EditorSupport.positionOfCursor (contentEl ()) == 0)) {
            // This is a very special position in that it only occurs at the very start of
            // the enclosing content editable.
            editor ().onMergePrior ();
            return true;
        }
        return false;
    }

    /**
     * Called by {@link #onEnterKey(UIEvent)} when generating a split of the
     * content. This should return a block suitable to contain the second half of
     * the split (generally this is just an instance of the block class).
     * 
     * @param content
     *                the content that constitutes the new block.
     * @return the block.
     */
    protected abstract Block createSplitBlock(FormattedBlock content);

    /**
     * Activates (or de-activates) tooling based on the passed selection range.
     *
     * @param r
     *          the range.
     */
    protected void updateSelection(Range r) {
        if ((r == null) || EditorSupport.insertionPoint (r)) {
            deactivateToolbar ();
        } else {
            DOMRect rect = r.getBoundingClientRect ();
            DOMRect containerRect = container.el ().getBoundingClientRect ();
            int top = (int) (rect.bottom - containerRect.top) + 5;
            int left = (int) (rect.left - containerRect.left);
            activateToolbar (top, left);
        }
    }

    @Override
    public boolean canMerge(Block source) {
        return source.type ().is (BlockType.PARA, BlockType.H1, BlockType.H2, BlockType.H3, BlockType.NLIST);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.ui.client.editor.Block#merge(com.effacy.jui.ui.client.editor.Block)
     */
    @Override
    public void merge(Block source) {
        int position = content ().length ();
        content ().merge (source.content ().transform (type));
        refresh ();

        container.el ().focus ();

        EditorSupport.positionCursorAt (contentEl (), position);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.ui.client.editor.Block#deactivate()
     */
    @Override
    public void deactivate() {
        super.deactivate ();
        container.el ().classList.remove (styles ().empty ());
        updateSelection (null);
    }

    @Override
    public void activate(int length) {
        if (DebugMode.ACTIVATION.set())
            Logger.log ("{editor-block-activate} [length=" + length + "] [" + toString () + "]");
        EditorSupport.positionCursorAt (container.el (), length);
    }

    @Override
    protected Element contentEl() {
        return container.el ();
    }

    
}

