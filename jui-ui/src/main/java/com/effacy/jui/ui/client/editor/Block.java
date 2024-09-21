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

import com.effacy.jui.core.client.dom.DomSupport;
import com.effacy.jui.core.client.dom.IUIEventHandler;
import com.effacy.jui.core.client.dom.UIEvent;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.platform.util.client.TimerSupport;
import com.effacy.jui.ui.client.editor.Editor.DebugMode;
import com.effacy.jui.ui.client.editor.Editor.IEditor;
import com.effacy.jui.ui.client.editor.Editor.IEditorCSS;
import com.effacy.jui.ui.client.editor.model.ContentBlock;
import com.effacy.jui.ui.client.editor.model.ContentBlock.BlockType;
import com.effacy.jui.ui.client.editor.tools.ITool;
import com.effacy.jui.ui.client.editor.tools.ITool.IToolContext;
import com.effacy.jui.ui.client.editor.tools.IToolBar;
import com.effacy.jui.ui.client.editor.tools.ToolBar;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.HTMLElement;
import elemental2.dom.Range;
import jsinterop.base.Js;

/**
 * Block
 *
 * @author Jeremy Buckley
 */
public abstract class Block implements IUIEventHandler {

    public enum ToolbarStyle {
        INLINE, INLINE_CLOSE_ON_LEAVE;
    }

    private static Element DRAG_CONTAINER;

    /**
     * The root element of the block.
     */
    protected Element rootEl;

    /**
     * The root of the container that contains the block content. A block may
     * structure this region so that the actual content resides elsewhere.
     */
    protected Element containerEl;

    private Element inlineToolEl;

    private IToolBar toolbar;

    private Element sideToolEl;

    protected IEditor editor;

    private String uuid;

    private ToolbarStyle toolbarStyle = ToolbarStyle.INLINE;

    protected BlockType type;

    /**
     * The currently valid content.
     */
    private ContentBlock content;

    /**
     * The current indentation.
     */
    protected int indent = -1;

    /**
     * Constructs a block.
     * 
     * @param type
     *                the block type.
     * @param editor
     *                the editor.
     * @param content
     *                seeding content.
     */
    protected Block(BlockType type, IEditor editor, ContentBlock content) {
        this.type = type;
        this.editor = editor;
        this.content = content;
    }

    /**
     * The type of block this is.
     * 
     * @return the type.
     */
    public BlockType type() {
        return type;
    }

    /**
     * Clones the block.
     */
    public abstract Block clone();


    protected IEditorCSS styles() {
        return editor.styles ();
    }

    public IEditor editor() {
        return editor;
    }

    /**
     * The block is able to be navigated to (i.e. using navigation keys, etc).
     * 
     * @return {@code true} if it can.
     */
    public boolean navigable() {
        return true;
    }

    /**
     * Display label for the block type. This is used for menus.
     * 
     * @return the label.
     */
    abstract public String label();

    /**
     * When the block is activated (i.e. by a mouse click) then normally we stop the
     * event. However for some blocks we want to capture that event for further
     * processing (for example, a mouse click may end up positioning the cursor).
     * <p>
     * Here we allow the block to determine if such an event should propagate to the
     * block (or otherwise naturally propagate).
     * 
     * @param event
     *              the activation event.
     * @return {@code true} to propagate the event (the default is not to).
     */
    public boolean propagateActivationEvent(UIEvent event) {
        return false;
    }

    public boolean activationEvent(UIEvent event) {
        if (!event.isEvent(UIEventType.ONMOUSEDOWN))
            return false;
        return !DomSupport.isChildOf (event.getTarget(), sideToolEl);
    }

    /*************************************************************************
     * Toolbar.
     *************************************************************************/

    protected List<ITool> toolsForSelection() {
        return new ArrayList<>();
    }

    protected void activateToolbar(int top, int left) {
        // Create a tool context.
        IToolContext context = new IToolContext () {

            @Override
            public IEditor editor() {
                return editor;
            }

            @Override
            public Block block() {
                return Block.this;
            }

            @Override
            public Range range() {
                return DomGlobal.document.getSelection().getRangeAt (0);
            }

            @Override
            public void updateRange(Range range) {
                DomGlobal.document.getSelection ().removeAllRanges ();
                if (range != null)
                    DomGlobal.document.getSelection().addRange (range);
            }
            
        };

        // Determine if we already have a toolbar from the editor (an external one).
        if (editor.toolbar() != null) {
            editor.toolbar().activate (context);
            return;
        }

        // Clear out the tool and populate with content.
        List<ITool> tools = toolsForSelection ();
        if ((tools != null) && !tools.isEmpty ()) {
            DomSupport.removeAllChildren (inlineToolEl);
            Wrap.$ (inlineToolEl).$ (tool -> {
                toolbar = ToolBar.$ (tool, toolbar -> {
                    toolsForSelection ().forEach (tl -> toolbar.add (tl));
                });
            }).build ();
            toolbar.activate (context);
        }
        
        // Perform a left adjustment if the position would result in the toolbar going
        // past the right side of the content area.
        int adj = rootEl.clientWidth - (left + inlineToolEl.clientWidth);
        if (adj < 0)
            left += adj;
        CSS.LEFT.apply (inlineToolEl, Length.px (left));
        CSS.TOP.apply (inlineToolEl, Length.px (top));
        rootEl.classList.add (styles ().inline_tool_show ());
    }

    protected void deactivateToolbar() {
        if (editor.toolbar() != null) {
            editor.toolbar ().deactivate ();
        } else if (toolbar != null) {
            toolbar.dispose ();
            toolbar = null;
            rootEl.classList.remove (styles ().inline_tool_show ());
        }
    }

    /*************************************************************************
     * Side tooling.
     *************************************************************************/

    protected void activateSidetool() {
        rootEl.classList.add (styles ().side_tool ());
    }

    protected void deactivateSidetool() {
        rootEl.classList.remove (styles ().side_tool ());
    }

    /*************************************************************************
     * Contents.
     *************************************************************************/

    /**
     * Invalidates the content.
     */
    protected void invalidate() {
        this.content = null;
    }

    /**
     * Obtains the content (current state).
     * 
     * @return the content.
     */
    public final ContentBlock content() {
        if (this.content == null) {
            this.content = new ContentBlock (type);
            this.content.indent (indent);
            populate (this.content);
        }
        return content;
    }

    /**
     * Populates the passed content.
     * <p>
     * This is expected to be overridden.
     * 
     * @param content
     *                the content to populate.
     */
    protected void populate(ContentBlock content) {
        // Nothing.
    }

    /*************************************************************************
     * Change notification.
     *************************************************************************/

    /**
     * This is invoked when the block prior has changed (in case this block has a
     * dependency).
     * 
     * @return {@code true} to cascade the change.
     */
    public boolean onPriorChanged() {
        return false;
    }

    /*************************************************************************
     * Rendering.
     *************************************************************************/

    /**
     * Renders the block returning an un-attached element for inclusion into the
     * DOM.
     * <p>
     * This calls {@link #createContainer()} to create the content container element
     * that provides display and edit for the block. It also calls
     * {@link #refresh()} to apply the current content to the container element.
     * 
     * @return the element.
     */
    public Element render() {
        if (rootEl != null)
            return rootEl;
        
        DomSupport.createDiv (null, root -> {
            this.rootEl = root;
            root.classList.add (styles ().block ());
            root.setAttribute ("uuid", uuid = DomSupport.uuid ());

            // Our test reference needs to be consistent (i.e. repeatable when inserting
            // content in the same order). So we use a counter.
            root.setAttribute ("test-ref", "block-" + (Editor.COUNTER++));

            // Hook enter and leave for mouse entering an leaving the block.
            UIEventType.ONMOUSEENTER.attach (root);
            UIEventType.ONMOUSELEAVE.attach (root);

            // Inline tool. The mousedown allows us to stop removal of range.
            inlineToolEl = DomSupport.createDiv (root, inline -> {
                inline.classList.add (styles ().inline_tool ());
                inline.setAttribute ("test-ref", "block-inline");
            });
            UIEventType.ONMOUSEDOWN.attach (inlineToolEl);
            
            // Side tool.
            sideToolEl = DomSupport.createDiv (root, side -> {
                side.classList.add (styles ().side_tool ());
                side.setAttribute ("test-ref", "block-side");
                DomSupport.createEm (side, plus -> {
                    plus.classList.add ("fa", "fa-plus");
                    plus.setAttribute ("test-ref", "block-side-plus");
                    UIEventType.ONCLICK.attach (plus);
                });
                DomSupport.createEm (side, grip -> {
                    grip.classList.add ("fa", "fa-grip-vertical", styles ().side_tool_move ());
                    grip.setAttribute ("test-ref", "block-side-grip");
                    grip.draggable = true;
                    UIEventType.DRAGSTART.attach (grip);
                });
            });

            // Container.
            containerEl = createContainer();
            root.append (containerEl);
            containerEl.classList.add (styles ().content ());
            containerEl.setAttribute ("test-ref", "block-container");
            UIEventType.ONKEYPRESS.attach (containerEl);
            // Mouse moving in the container should re-activate the side tool. Must do this
            // here and not on the block otherwise the drag will not work (it seems to
            // interfere with that).
            UIEventType.ONMOUSEMOVE.attach (containerEl);
        });

        refresh ();

        return rootEl;
    }

    /**
     * Creates a container element (with all appropriate internal structures needed
     * to render the content).
     * <p>
     * Note that {@link #refresh(com.effacy.jui.ui.client.editor.model.Content.ContentBlock)} is
     * responsible for rendering the content into the container.
     * 
     * @return the container element.
     */
    protected Element createContainer() {
        return DomSupport.createDiv ();
    }

    /**
     * Updates the content of the block and performs a refresh.
     * 
     * @param content
     *                the content to update.
     */
    public void update(ContentBlock content) {
        if (content == null)
            return;
        if (this.type != content.type ()) {
            Logger.error ("Block type is " + type.name() + " but passed content was " + content.type());
            return;
        }
        this.content = content;
        refresh ();
    }

    /**
     * Call to refresh using the current content.
     * <p>
     * This calls {@link #refresh(com.effacy.jui.ui.client.editor.model.Content.ContentBlock)} to implement the actual refresh.
     */
    protected final void refresh() {
        refresh (this.content);
        refreshIndent ();
    }

    /**
     * Refreshes the editor based on the passed content.
     * <p>
     * To initiate a refresh one should invoke
     * {@link #update(com.effacy.jui.ui.client.editor.model.Content.ContentBlock)} (which include
     * passing the revised content).
     */
    protected abstract void refresh(ContentBlock content);

    /**
     * Refreshes the indent of the content.
     */
    protected void refreshIndent() {
        if (indent < 0)
            indent = (content != null) ? content.indent() : 0;
        rootEl.classList.remove(styles ().block_indent1 (), styles ().block_indent2 (), styles ().block_indent3 (), styles ().block_indent4 (), styles ().block_indent5 ());
        if (indent == 1)
            rootEl.classList.add (styles ().block_indent1 ());
        else if (indent == 2)
            rootEl.classList.add (styles ().block_indent2 ());
        else if (indent == 3)
            rootEl.classList.add (styles ().block_indent3 ());
        else if (indent == 4)
            rootEl.classList.add (styles ().block_indent4 ());
        else if (indent == 5)
            rootEl.classList.add (styles ().block_indent5 ());
    }

    /**
     * Obtains the block prior to this one.
     * 
     * @return the block (or {@code null} if the first).
     */
    protected Block priorBlock() {
        return editor.prior (this);
    }

    /**
     * Obtains the block after to this one.
     * 
     * @return the block (or {@code null} if the last).
     */
    protected Block nextBlock() {
        return editor.next (this);
    }

    /**
     * Apply an indent by one.
     */
    public void indent() {
        if (indent < 5) {
            indent++;
            if (this.content != null)
                this.content.indent (indent);
            refreshIndent ();
        }
    }

    /**
     * Apply an outdent by one.
     */
    public void outdent() {
        if (indent > 0) {
            indent--;
            if (this.content != null)
                this.content.indent (indent);
            refreshIndent ();
        }
    }

    /**
     * The root element of the block.
     */
    public Element getRootEl() {
        return rootEl;
    }

    /**
     * The UUID for the block.
     * 
     * @return the UUID.
     */
    public String getUuid() {
        return uuid;
    }

    protected Element contentEl() {
        return containerEl;
    }

    /************************************************************************
     * Standard events.
     ************************************************************************/

     /**
      * Activates the block using the passed hint as to where to locate the cursor
      * (derived from the state prior to activate).
      * 
      * @param length
      *               the location hint for the cursor (for end use a number less
      *               than zero).
      */
    public void activate(int length) {
        if (DebugMode.ACTIVATION.set())
            Logger.log ("{editor-block-activate} [length=" + length + "] [" + toString () + "]");
        contentEl().focus ();
    }

    /**
     * Deactivates the block. This is called when there is a transition to another
     * block.
     */
    public void deactivate() {
        if (DebugMode.ACTIVATION.set())
            Logger.log ("{editor-block-deactivate} [" + toString () + "]");
        deactivateSidetool();
        deactivateToolbar();
    }

    /**
     * Invoked when the mouse enters the blocks bounds.
     */
    public void mouseEnter() {
        activateSidetool ();
    }

    /**
     * Invoked when the mouse moves in the blocks bounds.
     */
    public void mouseMove() {
        activateSidetool ();
    }

    /**
     * Invoked when the mouse leaves the blocks bounds.
     */
    public void mouseLeave() {
        deactivateSidetool ();
        if (toolbarStyle == ToolbarStyle.INLINE_CLOSE_ON_LEAVE)
            deactivateToolbar ();
    }

    /**
     * Invoked when the up arrow key has been pressed (predicated is that this is
     * the active block).
     * 
     * @return {@code true} if the action was handled.
     */
    public boolean actionUp() {
        return false;
    }

    /**
     * Invoked when the down arrow key has been pressed (predicated is that this is
     * the active block).
     * 
     * @return {@code true} if the action was handled.
     */
    public boolean actionDown() {
        return false;
    }

    /**
     * Invoked when the left arrow key has been pressed (predicated is that this is
     * the active block).
     * 
     * @return {@code true} if the action was handled.
     */
    public boolean actionLeft() {
        return false;
    }

    /**
     * Invoked when the right arrow key has been pressed (predicated is that this is
     * the active block).
     * 
     * @return {@code true} if the action was handled.
     */
    public boolean actionRight() {
        return false;
    }

    /**
     * Invoked to configure a drag start event. This only needs to assign the image
     * to use.
     * 
     * @param event
     *              the event.
     */
    public void dragStart(UIEvent event) {
        if (DRAG_CONTAINER == null) {
            DRAG_CONTAINER = DomSupport.createDiv(DomGlobal.document.body);
            DRAG_CONTAINER.classList.add (styles ().editor (), styles ().drag_image ());
        }

        Element outer = DomSupport.createDiv (DRAG_CONTAINER);
        outer.classList.add (styles ().block ());
        CSS.WIDTH.apply (outer, Length.px (containerEl.clientWidth + 50));
        CSS.PADDING_LEFT.apply (outer, Length.px (50));
        HTMLElement el = Js.cast (containerEl.cloneNode (true));
        outer.append (el);
        event.getDataTransfer ().effectAllowed = "move";
        event.getDataTransfer ().setDragImage (el, 0, 0);
        TimerSupport.defer (() -> deactivateSidetool ());
    }

    public void dragEnd(UIEvent event) {
        if (DRAG_CONTAINER != null)
            DomSupport.removeAllChildren (DRAG_CONTAINER);
    }

    /**
     * Determines if it is possible to merge the passed source into this block.
     * 
     * @param source
     *               the source to merge in.
     * @return {@code true} if it is possible.
     */
    public boolean canMerge(Block source) {
        return false;
    }

    public void merge(Block source) {
        // Nothing by default.
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.dom.IUIEventHandler#handleEvent(com.effacy.jui.core.client.dom.UIEvent)
     */
    @Override
    public  boolean handleEvent(UIEvent event) {
        if (DebugMode.EVENT.set () && !event.isEvent (UIEventType.ONMOUSEENTER, UIEventType.ONMOUSELEAVE, UIEventType.ONMOUSEMOVE))
            Logger.trace ("{editor-event::block}", event.toString ());
        
        // Stopping events on mousedown in the toolbar prevents removal of selection
        // range.
        if (event.isEvent (UIEventType.ONMOUSEDOWN)) {
            if ((toolbar != null) && DomSupport.isChildOf (event.getTarget(), inlineToolEl)) {
                event.stopEvent();
                return true;
            }
        }

        // On blur we need to deactive the editor toolbar.
        if (event.isEvent(UIEventType.ONBLUR)) {
            if (DebugMode.EVENT.set ())
                Logger.trace ("{editor-event::block}", event.toString () + " -> deactivate toolbar");
            deactivateToolbar ();
        }
        
        // On click we check the toolbar (if there is one)
        if (event.isEvent(UIEventType.ONCLICK)) {
            // Check tools.
            if ((toolbar != null) && toolbar.handleEvent (event)) {
                if (DebugMode.EVENT.set ())
                    Logger.trace ("{editor-event::block}", event.toString () + " -> handled by toolbar");
                return true;
            }
            if (DebugMode.EVENT.set ())
                Logger.trace ("{editor-event::block}", event.toString () + " -> propagate up");
            return _handleEvent (event); 
        } 

        if (event.isEvent (UIEventType.ONKEYDOWN)) {
            if (event.isUpKey() || event.isLeftKey ()) {
                if (event.isUpKey() && actionUp ())
                    return true;
                if (event.isLeftKey() && actionLeft ())
                    return true;
                // Move to the previous block and stop the event (to prevent cursor relocation).
                int pos = EditorSupport.positionOfCursor (contentEl ());
                if (pos == 0) {
                    editor.previous (-1);
                    event.stopEvent();
                    return true;
                }
            } else if (event.isDownKey() || event.isRightKey()) {
                if (event.isDownKey() && actionDown ())
                    return true;
                if (event.isRightKey() && actionRight ())
                    return true;
                // Move to the next block and stop the event (to prevent cursor relocation).
                int pos = EditorSupport.positionOfCursor (contentEl ());
                int end = EditorSupport.numberOfCharacters (contentEl ());
                if (pos >= end) {
                    editor.next (0);
                    event.stopEvent();
                    return true;
                }
            }
        }

        if (DebugMode.EVENT.set () && !event.isEvent (UIEventType.ONMOUSEENTER, UIEventType.ONMOUSELEAVE, UIEventType.ONMOUSEMOVE))
            Logger.trace ("{editor-event::block}", event.toString () + " -> propagate up");
        return _handleEvent (event);
    }

    protected boolean _handleEvent(UIEvent event) {
        return false;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + uuid;
    }

    
}
