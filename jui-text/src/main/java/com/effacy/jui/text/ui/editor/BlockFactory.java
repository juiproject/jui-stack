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

import com.effacy.jui.text.type.FormattedBlock;
import com.effacy.jui.text.ui.editor.Editor.IEditor;

public class BlockFactory {

    /**
     * Factory for creating blocks.
     */
    @FunctionalInterface
    public interface IBlockFactory {
    
        /**
         * Creates a block based on the given content block.
         * 
         * @param editor
         *               the underlying editor to bind the block to.
         * @param blk
         *               the block content.
         * @return the block (this is optional if a block could not be created based on
         *         the content).
         */
        public Optional<Block> create(IEditor editor, FormattedBlock blk);
    }

    /**
     * Registration of various factories.
     */
    private static List<IBlockFactory> FACTORIES = new ArrayList<>();
    static {
        FACTORIES.add ((editor,blk) -> {
            if (FormattedBlock.BlockType.PARA != blk.getType())
                return Optional.empty ();
            return Optional.of (new ParagraphBlock (editor, blk));
        });
        FACTORIES.add ((editor,blk) -> {
            if (FormattedBlock.BlockType.H1 != blk.getType())
                return Optional.empty ();
            return Optional.of (new HeadingBlock (editor, 1, blk));
        });
        FACTORIES.add ((editor,blk) -> {
            if (FormattedBlock.BlockType.H2 != blk.getType())
                return Optional.empty ();
            return Optional.of (new HeadingBlock (editor, 2, blk));
        });
        FACTORIES.add ((editor,blk) -> {
            if (FormattedBlock.BlockType.H3 != blk.getType())
                return Optional.empty ();
            return Optional.of (new HeadingBlock (editor, 3, blk));
        });
        FACTORIES.add ((editor,blk) -> {
            if (FormattedBlock.BlockType.DIA != blk.getType())
                return Optional.empty ();
            return Optional.of (new DiagramBlock (editor, blk));
        });
        FACTORIES.add ((editor,blk) -> {
            if (FormattedBlock.BlockType.EQN != blk.getType())
                return Optional.empty ();
            return Optional.of (new EquationBlock (editor, blk));
        });
        FACTORIES.add ((editor,blk) -> {
            if ((FormattedBlock.BlockType.NLIST != blk.getType()) && (FormattedBlock.BlockType.OLIST != blk.getType()))
                return Optional.empty ();
            return Optional.of (new NumberedListBlock (editor, blk));
        });
    }

    /**
     * Registers a factory.
     * 
     * @param factory
     *                the factory to register.
     */
    public static void register(IBlockFactory factory) {
        if (factory == null)
            return;
        FACTORIES.add (factory);
    }

    /**
     * Creates a block based on the given block content.
     * 
     * @param editor
     *               the underlying editor.
     * @param blk
     *               the block content.
     * @return the block (if was able to be created).
     */
    public static Optional<Block> create(IEditor editor, FormattedBlock blk) {
        if (blk == null)
            return Optional.empty ();
        for (IBlockFactory factory : FACTORIES) {
            Optional<Block> block = factory.create (editor, blk);
            if (block.isPresent())
                return block;
        }
        return Optional.empty ();
    }
}
