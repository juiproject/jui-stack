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
import java.util.Optional;
import java.util.Stack;

import com.effacy.jui.core.client.dom.DomSupport;
import com.effacy.jui.core.client.dom.IUIEventHandler;
import com.effacy.jui.core.client.dom.UIEvent;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.text.type.FormattedBlock;
import com.effacy.jui.text.type.FormattedBlock.BlockType;
import com.effacy.jui.text.type.FormattedText;
import com.effacy.jui.text.ui.editor.DiagramBlock.IDiagramBlockCSS;
import com.effacy.jui.text.ui.editor.EquationBlock.IEquationBlockCSS;
import com.effacy.jui.text.ui.editor.NumberedListBlock.INumberedListBlockCSS;
import com.effacy.jui.text.ui.editor.command.ICommand;
import com.effacy.jui.text.ui.editor.tools.IToolBar;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;
import elemental2.dom.MouseEvent;

/**
 * Editor
 *
 * @author Jeremy Buckley
 */
public class Editor implements IUIEventHandler {

    /************************************************************************
     * Debugging
     ************************************************************************/

    /**
     * Various debug modes.
     */
    public enum DebugMode {
        /**
         * Log rendering related activities.
         */
        ACTIVATION(1<<1),
        
        /**
         * Log the flow of events.
         */
        EVENT(1<<2);

        /**
         * Bit flag for the specific debug mode.
         */
        private int flag;

        /**
         * Construct with initial data.
         */
        private DebugMode(int flag) {
            this.flag = flag;
        }

        /**
         * Determines if the flag is set.
         * 
         * @return {@code true} if it is.
         */
        public boolean set() {
            return ((Editor.DEBUG & flag) > 0);
        }
    }

    /**
     * Flag to toggle debug mode.
     */
    private static int DEBUG = 0;

    /**
     * Assigns the passed modes for debugging.
     * 
     * @param modes
     *              the modes.
     */
    public static void debug(DebugMode...modes) {
        DEBUG = 0;
        for (DebugMode mode : modes) {
            if (mode == null)
                continue;
            DEBUG |= mode.flag;
        }
    }

    public interface IEditor {

        /**
         * Navigates to the previous block from the current (active) one. If the
         * previous block is not navigable (see {@link Block#navigable()}) then it will
         * keep going back until it finds the first one that is. If no block is found
         * then no navigation is performed.
         * 
         * @param length
         *               positional hint as to where to place the cursor in the block
         *               when activated.
         */
        public void previous(int length);

        /**
         * Navigates to the next block from the current (active) one. If the
         * next block is not navigable (see {@link Block#navigable()}) then it will
         * keep going forward until it finds the first one that is. If no block is found
         * then no navigation is performed.
         * 
         * @param length
         *               positional hint as to where to place the cursor in the block
         *               when activated.
         */
        public void next(int length);
        public void onInsertNext(Block block, boolean activate);
        public void onInsertPrior(Block block, boolean activate);
        public void onReplace(Block replace, Block block, boolean active);
        public void onMergePrior();

        /**
         * Obtains the prior block to the block that has been passed. If not block is
         * passed then the active block is assumed. If there is no active block then the
         * first block (if it exists) is returned. In all cases if the reference block
         * is the first block then {@code null} is returned.
         * 
         * @param block
         *              the reference block.
         * @return the block prior to the reference (as defined in the description).
         */
        public Block prior(Block block);
        public Block next(Block block);

        /**
         * Source of standard styles to use.
         * 
         * @return the styles.
         */
        public IEditorCSS styles();

        /**
         * Obtains any externally declared toolbar.
         * 
         * @return the toolbar.
         */
        public IToolBar toolbar();
    }

    public static long COUNTER = 0;

    private Element holder;

    private IEditorCSS styles = StandardEditorCSS.instance ();

    private List<Block> blocks = new ArrayList<> ();

    private Block activeBlock;

    private IToolBar toolbar;

    private IEditor editor = new IEditor() {

        @Override
        public void previous(int length) {
            if (activeBlock == null)
                return;
            int idx = blocks.indexOf (activeBlock);
            Block previousBlk;
            do {
                idx--;
                if (idx < 0)
                    return;
                previousBlk = blocks.get (idx);
                if (previousBlk == null)
                    return;
            } while (!previousBlk.navigable());
            activate (previousBlk, length);
        }

        @Override
        public void next(int length) {
            if (activeBlock == null)
                return;
            int idx = blocks.indexOf (activeBlock);
            if (idx < 0)
                return;
            Block nextBlk;
            do {
                idx++;
                if (idx >= blocks.size ())
                    return;
                nextBlk = blocks.get (idx);
                if (nextBlk == null)
                    return;
            } while (!nextBlk.navigable ());
            activate (nextBlk, length);
        }

        @Override
        public IEditorCSS styles() {
            return Editor.this.styles ();
        }

        @Override
        public void onInsertNext(Block block, boolean activate) {
            Editor.this.actionInsertNext (block, activate);
        }

        @Override
        public void onInsertPrior(Block block, boolean activate) {
            Editor.this.actionInsertPrior (block, activate);
        }

        @Override
        public void onReplace(Block replace, Block block, boolean active) {
            Editor.this.actionReplaceBlock(replace, block, active);
        }

        @Override
        public void onMergePrior() {
            Editor.this.actionMergePrior ();
        }

        @Override
        public Block prior(Block block) {
            if (blocks.isEmpty ())
                return null;
            if (block == null)
                block = activeBlock;
            if (block == null)
                return blocks.get (0);
            int idx = blocks.indexOf (block);
            if (idx <= 0)
                return null;
            return blocks.get (idx - 1);
        }

        @Override
        public Block next(Block block) {
            if (blocks.isEmpty ())
                return null;
            if (block == null)
                block = activeBlock;
            if (block == null)
                return blocks.get (blocks.size () - 1);
            int idx = blocks.indexOf (block);
            if (idx < 0)
                return null;
            if (idx >= blocks.size () - 1)
                return null;
            return blocks.get (idx + 1);
        }

        @Override
        public IToolBar toolbar() {
            return toolbar;
        }
        
    };

    /**
     * Command stack.
     */
    private Stack<ICommand> commands = new Stack<>();

    /**
     * Reverts the last command on the command stack.
     */
    public void revertLastCommand() {
        if (!commands.isEmpty()) {
            ICommand command = commands.pop();
            command.revert();
        }
    }

    /**
     * Default constructor. Note that it is expected that the editor will be bound
     * using {@link #bind(Element)}.
     */
    public Editor() {
        // Nothing.
    }

    /**
     * Construct with an element to bind to (see {@link #bind(Element)}).
     * 
     * @param holder
     *               the element to bind to.
     */
    public Editor(Element holder) {
        bind (holder);
    }

    /**
     * Binds the editor to an element. This will render the editor structures and
     * attach event handlers. It is expected that the including component will
     * implement event hooking and will delegate them to the editor.
     * 
     * @param holder
     *               the element to bind to.
     * @return this editor instance.
     */
    public Editor bind(Element holder) {
        if (holder == null)
            return this;
        this.holder = holder;
        this.holder.classList.add (styles ().editor ());
        UIEventType.DRAGOVER.attach (holder);
        UIEventType.DRAGLEAVE.attach (holder);
        UIEventType.DROP.attach (holder);
        UIEventType.ONMOUSEDOWN.attach (holder);
        update (preBindContent);
        preBindContent = null;
        return this;
    }

    /**
     * Registers a toolbar handler.
     * <p>
     * Only use this if the toolbar is being managed externally to the editor.
     * Otherwise the editor will employ an internal (contextual) toolbar.
     * 
     * @param toolbar
     *                the toolbar (interface to) to register.
     * @return this editor instance.
     */
    public Editor toolbar(IToolBar toolbar) {
        this.toolbar = toolbar;
        return this;
    }

    /**
     * Content assigned prior to the editor being bound. Once bound this content will be rendered.
     */
    private FormattedText preBindContent;

    /**
     * Updates the editor to display the given content.
     * <p>
     * If the content is {@code null} then the editor will be updated with a seeding
     * (empty) paragraph.
     * 
     * @param content
     *                the content to render into the editor.
     */
    public void update(FormattedText content) {
        if (holder == null) {
            this.preBindContent = content;
            return;
        }

        // Clear any existing content.
        blocks.clear ();

        // Build out content.
        if (content != null) {
            for (FormattedBlock blk : content) {
                Optional<Block> block = BlockFactory.create (editor, blk);
                if (block.isPresent ())
                    blocks.add (block.get ());
            }
        } 
        
        // If there is no content then seed with an empty paragraph.
        if (blocks.isEmpty())
            blocks.add (BlockFactory.create (editor, new FormattedBlock (BlockType.PARA)).get());

        DomSupport.removeAllChildren (holder);
        blocks.forEach (blk -> domInsertBlockAfter (blk, null));

        activate (blocks.get (blocks.size() - 1), 0);
    }


    /**
     * CSS styles to employ.
     * 
     * @return the styles.
     */
    protected IEditorCSS styles() {
        return styles;
    }

    protected Element domCreateSpacer() {
        return DomSupport.createDiv (holder, spacer -> {
            spacer.classList.add (styles ().spacer ());
            DomSupport.createDiv (spacer);
        });
    }

    /**
     * DOM only operation to insert the passed block after the reference block. The
     * inserted block will be rendered during this process.
     * 
     * @param block
     *                  the block to insert.
     * @param reference
     *                  the reference block (if this is {@code null} then the last
     *                  place is used).
     */
    void domInsertBlockAfter(Block block, Block reference) {
        if (block == null)
            return;
        Element pivotEl = null;
        if (reference != null) {
            // Pivot is the spacer after the relative block.
            pivotEl = reference.getRootEl ().nextElementSibling;
        } else if (holder.childNodes.length == 0) {
            // If there are no children create the solo spacer.
            pivotEl = domCreateSpacer ();
        } else {
            // If there are children then the last element is the spacer to use as the
            // pivot.
            pivotEl = holder.lastElementChild;
        }

        // Render the block and insert prior to the pivot creating a spacer above it.
        Element blockEl = block.render ();
        holder.insertBefore (blockEl, pivotEl);
        Element spacerEl = domCreateSpacer();
        holder.insertBefore (spacerEl, blockEl);

        // Apply the indent to the spacer after the inserted block.
        Element afterSpacerEl = blockEl.parentElement.nextElementSibling;
        if (afterSpacerEl != null) {
            int indent = block.indent;
            afterSpacerEl.classList.remove (styles ().block_indent1 (), styles ().block_indent2 (), styles ().block_indent3 (), styles ().block_indent4 (), styles ().block_indent5 ());
            if (indent == 1)
                afterSpacerEl.classList.add (styles ().block_indent1 ());
            else if (indent == 2)
                afterSpacerEl.classList.add (styles ().block_indent2 ());
            else if (indent == 3)
                afterSpacerEl.classList.add (styles ().block_indent3 ());
            else if (indent == 4)
                afterSpacerEl.classList.add (styles ().block_indent4 ());
            else if (indent == 5)
                afterSpacerEl.classList.add (styles ().block_indent5 ());
        }
    }

    /**
     * DOM only operation to insert the passed block before the reference block. The
     * inserted block will be rendered during this process.
     * 
     * @param block
     *                  the block to insert.
     * @param reference
     *                  the reference block (if this is {@code null} then the first
     *                  place is used).
     */
    void domInsertBlockBefore(Block block, Block relative) {
        Element pivotEl = null;
        if (relative != null) {
            // Pivot is the spacer after the relative block.
            pivotEl = relative.getRootEl ().previousElementSibling;
        } else if (holder.childNodes.length == 0) {
            // If there are no children create the solo spacer.
            pivotEl = domCreateSpacer();
        } else {
            // If there are children then the first element is the spacer to use as the
            // pivot.
            pivotEl = holder.firstElementChild;
        }

        // Render the block and insert prior to the pivot creating a spacer above it.
        Element blockEl = block.render ();
        holder.insertBefore(blockEl, pivotEl);
        Element spacerEl = domCreateSpacer();
        holder.insertBefore (spacerEl, blockEl);
    }

    /**
     * DOM only operation to remove the passed block from the DOM.
     * 
     * @param block
     *              the block to remove.
     */
    void domRemoveBlock(Block block) {
        if (block == null)
            return;
        Element blockEl = block.getRootEl ();
        Element spacerEl = blockEl.previousElementSibling;
        blockEl.remove ();
        spacerEl.remove ();
        if (this.activeBlock == block)
            this.activeBlock = null;
    }

    /**
     * Insert the passed block prior to the active block, but keeps the active block
     * active.
     * <p>
     * This will invoke a change cascade (that calls {@link Block#onPriorChanged()}).
     * 
     * @param block
     *              the block to insert.
     * @param activate
     *                  {@code true} if the new block should be make the active
     *                  block.
     */
    void actionInsertPrior(Block block, boolean activate) {
        if (block == null)
            return;

        if (activeBlock != null)
            blocks.add ( blocks.indexOf (activeBlock), block);
        else
            blocks.add (0, block);
        domInsertBlockBefore (block, activeBlock);
        
        if (activate)
            activate (block, 0);

        cascadeChange (block);
    }

    /**
     * Inserts the passed (newly constructed and not yet attached) block to the slot
     * immediately after the currently active block then make this new block active.
     * <p>
     * This will invoke a change cascade (that calls {@link Block#onPriorChanged()}).
     * 
     * @param block
     *              the block to insert.
     * @param activate
     *                  {@code true} if the new block should be make the active
     *                  block.
     */
    void actionInsertNext(Block block, boolean activate) {
        if (block == null)
            return;

        if (activeBlock != null)
            blocks.add (blocks.indexOf (activeBlock) + 1, block);
        else
            blocks.add (block);
        domInsertBlockAfter (block, activeBlock);
        
        if (activate)
            activate (block, 0);

        cascadeChange (block);
    }

    /**
     * Replaces one block with another block (permanently removing the first).
     * <p>
     * This will invoke a change cascade (that calls
     * {@link Block#onPriorChanged()}).
     * 
     * @param toReplace
     *                  the block that is going to be replace.
     * @param block
     *                  the block that will replace it (should be new).
     * @param activate
     *                  {@code true} if the new block should be make the active
     *                  block.
     */
    void actionReplaceBlock(Block toReplace, Block block, boolean activate) {
        if ((block == null) || (toReplace == null))
            return;
        blocks.add (blocks.indexOf (toReplace) + 1, block);
        blocks.remove (toReplace);

        // Apply DOM changes.
        domInsertBlockAfter (block, toReplace);
        domRemoveBlock (toReplace);

        if (activate)
            activate(block, 0);

        cascadeChange (block);
    }

    /**
     * Attempts a merge of the active block and the block prior to it (if there is
     * one). The relies on a merge-compatibility match between the two block (namely
     * that the prior block is able to absorb the active block). If successful the
     * active block is removed and the prior one becomes the new active block.
     * <p>
     * This will invoke a change cascade (that calls {@link Block#onPriorChanged()}).
     */
    void actionMergePrior() {
        if (activeBlock != null) {
            int idx = blocks.indexOf (activeBlock);
            if (idx <= 0) {
                // Nothing to do.
                return;
            }

            // We need to retain the active block as a merge may end up changing dues to a
            // loss of focus.
            Block merge = activeBlock;
            Block prior = blocks.get (idx - 1);

            if (prior.canMerge (merge)) {
                // Remove block prior to merge (which will refresh).
                blocks.remove (merge);
                prior.merge (merge);
                domRemoveBlock (merge);
                activate (prior, 0);
                cascadeChange (prior);
            }
        }
    }

    /**
     * See {@link #cascadeChange(Block)} but applies to all block in the collection.
     * This is less efficient but will ensure any change is properly reflected.
     */
    protected void cascadeChangeAll() {
        blocks.forEach (block -> block.onPriorChanged());
    }

    /**
     * Implements a change cascade from the passed block. The process is to
     * determine the next block in the chain and invoke that blocks
     * {@link Block#onPriorChanged()} method. If that method returns {@code true}
     * then we continue the process for that block.
     * 
     * @param block
     *              the block to chain from (starts with the block after this).
     */
    protected void cascadeChange(Block block) {
        int idx = blocks.indexOf (block);
        while ((idx >= 0) && (++idx <= blocks.size ()) && blocks.get (idx).onPriorChanged ()) 
            ;
    }

    /**
     * Used for efficiency. This is the last block that mouse activity was recorded
     * against.
     */
    private Block lastMouseActivityBlock;

    protected Block matchBlock(Element targetBlockEl) {
        if (targetBlockEl == null)
            return null;
        if ((lastMouseActivityBlock != null) && (targetBlockEl == lastMouseActivityBlock.getRootEl ()))
            return lastMouseActivityBlock;
        for (Block blk : blocks) {
            if (targetBlockEl == blk.getRootEl ())
                return blk;
        }
        return null;
    }

    protected Block matchBlock(String uuid) {
        Optional<Block> block = blocks.stream ().filter (blk -> blk.getUuid ().equals (uuid)).findFirst ();
        return block.isPresent() ? block.get() : null;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.dom.IUIEventHandler#handleEvent(com.effacy.jui.core.client.dom.UIEvent)
     */
    @Override
    public boolean handleEvent(UIEvent event) {
        if (DebugMode.EVENT.set () && !event.isEvent (UIEventType.ONMOUSEENTER, UIEventType.ONMOUSELEAVE, UIEventType.ONMOUSEMOVE))
            Logger.trace ("{editor-event::editor}", event.toString ());

        Element targetEl = event.getTarget ();
        Element blockEl = DomSupport.parent (targetEl, "." + styles ().block (), holder);
        Element spacerEl = (blockEl != null) ? null : DomSupport.parent (targetEl, "." + styles ().spacer (), holder);

        // If we have a toolbar check that first.
        if ((toolbar != null) && toolbar.handleEvent (event)) {
            if (DebugMode.EVENT.set () && !event.isEvent (UIEventType.ONMOUSEENTER, UIEventType.ONMOUSELEAVE, UIEventType.ONMOUSEMOVE))
                Logger.trace ("{editor-event::editor}", event.toString () + "-* handled by toolbar");
            return true;
        }
        
        // Special case of drag over, drag leave and drop which are bound to the editor.
        if (event.isEvent(UIEventType.DRAGLEAVE)) {
            // Clear all active on a leave.
            DomSupport.forEach (holder, child -> {
                if (child instanceof Element)
                    ((Element) child).classList.remove (styles ().active ());
            });
            return true;
        }
        if (event.isEvent (UIEventType.DRAGOVER)) {
            if (spacerEl != null) {
                spacerEl.classList.add (styles ().active ());
            } else if (blockEl != null) {
                Block blk = matchBlock (blockEl);
                if (blk == null)
                    return true;
                lastMouseActivityBlock = blk;
                if (event.getEvent() instanceof MouseEvent) {
                    int offsetY = (int)((MouseEvent) event.getEvent ()).offsetY;
                    int height = event.getTarget().clientHeight;
                    int topThreshold = height / 3;
                    int bottomThreshold = height - topThreshold;
                    if (offsetY < topThreshold) {
                        blockEl.previousElementSibling.classList.add (styles ().active ());
                        blockEl.nextElementSibling.classList.remove (styles ().active ());
                    } else if (offsetY > bottomThreshold) {
                        blockEl.previousElementSibling.classList.remove (styles ().active ());
                        blockEl.nextElementSibling.classList.add (styles ().active ());
                    } else {
                        blockEl.previousElementSibling.classList.remove (styles ().active ());
                        blockEl.nextElementSibling.classList.remove (styles ().active ());
                    }
                    return true;
                }
            }
            return true;
        }
        if (event.isEvent(UIEventType.DROP)) {
            // Find the active state.
            Element spacer = (Element) DomSupport.find (holder, child -> {
                if (!(child instanceof Element))
                    return false;
                return ((Element) child).classList.contains (styles ().active ());
            });
            if (spacer == null)
                return true;
            spacer.classList.remove (styles ().active ());
            String uuid = event.getDataTransfer ().getData ("text");
            Block blockToMove = matchBlock (uuid);
            if (blockToMove == null)
                return true;
            if (blockToMove.getRootEl () == spacer.previousElementSibling)
                return true;
            if (blockToMove.getRootEl () == spacer.nextElementSibling)
                return true;
            // Here we need to clone the node. The reason being is that we need to keep
            // event handlers in place.
            Block clonedBlock = blockToMove.clone ();
            blocks.remove (blockToMove);
            domRemoveBlock (blockToMove);
            if (spacer.nextElementSibling == null) {
                // We are adding to the end.
                activeBlock = null;
                actionInsertNext (clonedBlock, true);
            } else {
                activeBlock = matchBlock(spacer.nextElementSibling.getAttribute ("uuid"));
                actionInsertPrior (clonedBlock, true);
            }
            cascadeChangeAll ();
            return true;
        }

        if (event.isEvent(UIEventType.DRAGSTART, UIEventType.DRAGEND)) {
            // Clear all activate states.
            DomSupport.forEach (holder, child -> {
                if (child instanceof Element)
                    ((Element) child).classList.remove (styles ().active ());
            });
            // Initialise or terminate the drag image.
            Block blk = matchBlock(blockEl);
            if (blk != null) {
                lastMouseActivityBlock = blk;
                if (event.isEvent(UIEventType.DRAGSTART)) {
                    blk.dragStart (event);
                    event.getDataTransfer ().setData ("text", blk.getUuid ());
                } else {
                    blk.dragEnd (event);
                }
            }
            return false;
        }

        // These events need to be passed onto the relevent block regardless.
        if (event.isEvent (UIEventType.ONMOUSEENTER, UIEventType.ONMOUSELEAVE, UIEventType.ONMOUSEMOVE)) {
            // Check the last mouse activity block.
            Block blk = matchBlock (blockEl);
            if (blk != null) {
                lastMouseActivityBlock = blk;
                if (event.isEvent(UIEventType.ONMOUSEMOVE))
                    blk.mouseMove ();
                else if (event.isEvent(UIEventType.ONMOUSEENTER))
                    blk.mouseEnter();
                else
                    blk.mouseLeave();
            }
            return false;
        }

        // On a focus or click event we need to find and activate the relevant block.
        if (event.isEvent (UIEventType.ONFOCUS, UIEventType.ONCLICK, UIEventType.ONMOUSEDOWN)) {
            // Obtain the block from the associated element.
            Block blk = matchBlock (blockEl);
            if (blk != null) {
                // If this is the active block then we check for a click, we then delegate it to
                // the block.
                if (this.activeBlock == blk) {
                    if (event.isEvent(UIEventType.ONCLICK)) {
                        if (DebugMode.EVENT.set ())
                            Logger.trace ("{editor-event::editor}", event.toString () + " -> dispatch to block");
                        return activeBlock.handleEvent (event);
                    }
                }
                // If it is not the active block then we check to see if we should activate it.
                // Once active if it a click then we delegate (as above).
                else if (blk.activationEvent (event)) {
                    activate (blk, event.isEvent(UIEventType.ONMOUSEDOWN) ? -1 : 0);
                    if (DebugMode.EVENT.set ())
                        Logger.trace ("{editor-event::editor}", event.toString () + " -> activate block");
                    if (event.isEvent(UIEventType.ONCLICK)) {
                        if (DebugMode.EVENT.set ())
                            Logger.trace ("{editor-event::editor}", event.toString () + " -> dispatch to block");
                        return activeBlock.handleEvent (event);
                    }
                    return !blk.propagateActivationEvent(event);
                }
            }
            return false;
        }

        if (activeBlock == null)
            return false;
        if (blockEl != activeBlock.getRootEl ())
            return false;

        if (DebugMode.EVENT.set () && !event.isEvent (UIEventType.ONMOUSEENTER, UIEventType.ONMOUSELEAVE, UIEventType.ONMOUSEMOVE))
            Logger.trace ("{editor-event::editor}", event.toString () + " -> dispatch to block");
        return activeBlock.handleEvent (event);
    }

    private boolean activateRunning = false;

    /**
     * Activates the passed block using the passed length as a hint as to where to
     * place the cursor.
     * 
     * @param <B>
     * @param block
     *               the block being activated (can be {@code null} if just
     *               deactivating the current block).
     * @param length
     *               the hint to locate the cursor (if not relevant or known then
     *               just use 0).
     * @return the passed block.
     */
    protected <B extends Block> B activate(B block, int length) {
        if (activateRunning)
            return block;
        try {
            activateRunning = true;
            if (block == this.activeBlock)
                return block;
            if (this.activeBlock != null)
                this.activeBlock.deactivate ();
            this.activeBlock = block;
            if (this.activeBlock != null)
                this.activeBlock.activate (Math.max (length, -1));
            return block;
        } finally {
            activateRunning = false;
        }
    }

    public static interface IEditorCSS extends IDiagramBlockCSS, IEquationBlockCSS, INumberedListBlockCSS {

        public String editor();

        public String drag_image();

        public String block();

        public String content();

        public String content_editable();

        public String side_tool();

        public String side_tool_move();

        public String paragraph();

        public String empty();

        public String spacer();

        public String active();

        public String inline_tool();

        public String inline_tool_show();

        public String block_indent1();

        public String block_indent2();

        public String block_indent3();

        public String block_indent4();

        public String block_indent5();
    }

    /**
     * Component CSS (standard pattern).
     */
    @CssResource({
        "com/effacy/jui/text/ui/editor/Editor.css",
        "com/effacy/jui/text/ui/editor/Editor_Override.css"
    })
    public static abstract class StandardEditorCSS implements IEditorCSS {

        private static StandardEditorCSS STYLES;

        public static IEditorCSS instance() {
            if (STYLES == null) {
                STYLES = (StandardEditorCSS) GWT.create (StandardEditorCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }
}
