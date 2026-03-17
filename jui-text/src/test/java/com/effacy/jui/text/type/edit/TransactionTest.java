package com.effacy.jui.text.type.edit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import com.effacy.jui.text.type.FormattedBlock;
import com.effacy.jui.text.type.FormattedBlock.BlockType;
import com.effacy.jui.text.type.FormattedLine;
import com.effacy.jui.text.type.FormattedLine.FormatType;
import com.effacy.jui.text.type.FormattedText;
import com.effacy.jui.text.type.edit.step.ChangeFormatStep;
import com.effacy.jui.text.type.edit.step.DeleteBlockStep;
import com.effacy.jui.text.type.edit.step.DeleteTextStep;
import com.effacy.jui.text.type.edit.step.InsertBlockStep;
import com.effacy.jui.text.type.edit.step.InsertTextStep;
import com.effacy.jui.text.type.edit.step.JoinBlocksStep;
import com.effacy.jui.text.type.edit.step.MoveBlockStep;
import com.effacy.jui.text.type.edit.step.ReplaceBlockStep;
import com.effacy.jui.text.type.edit.step.SetBlockIndentStep;
import com.effacy.jui.text.type.edit.step.SetBlockMetaStep;
import com.effacy.jui.text.type.edit.step.SetBlockTypeStep;
import com.effacy.jui.text.type.edit.step.SplitBlockStep;

public class TransactionTest {

    /************************************************************************
     * Helper to build a simple document.
     ************************************************************************/

    private static FormattedText doc(String... texts) {
        FormattedText doc = new FormattedText();
        for (String text : texts)
            doc.block(BlockType.PARA, b -> b.line(text));
        return doc;
    }

    private static FormattedBlock para(String text) {
        FormattedBlock b = new FormattedBlock(BlockType.PARA);
        b.line(text);
        return b;
    }

    private static String textAt(FormattedText doc, int blockIndex) {
        return doc.getBlocks().get(blockIndex).getLines().get(0).getText();
    }

    /************************************************************************
     * InsertBlockStep
     ************************************************************************/

    @Test
    public void testInsertBlock_atStart() {
        FormattedText doc = doc("A", "B");
        Transaction tr = Transaction.create().step(new InsertBlockStep(0, para("X")));
        Transaction inverse = tr.apply(doc).inverse();

        Assertions.assertEquals(3, doc.getBlocks().size());
        Assertions.assertEquals("X", textAt(doc, 0));
        Assertions.assertEquals("A", textAt(doc, 1));
        Assertions.assertEquals("B", textAt(doc, 2));

        // Undo.
        inverse.apply(doc);
        Assertions.assertEquals(2, doc.getBlocks().size());
        Assertions.assertEquals("A", textAt(doc, 0));
        Assertions.assertEquals("B", textAt(doc, 1));
    }

    @Test
    public void testInsertBlock_atEnd() {
        FormattedText doc = doc("A", "B");
        Transaction tr = Transaction.create().step(new InsertBlockStep(2, para("X")));
        Transaction inverse = tr.apply(doc).inverse();

        Assertions.assertEquals(3, doc.getBlocks().size());
        Assertions.assertEquals("X", textAt(doc, 2));

        inverse.apply(doc);
        Assertions.assertEquals(2, doc.getBlocks().size());
    }

    @Test
    public void testInsertBlock_middle() {
        FormattedText doc = doc("A", "B", "C");
        Transaction tr = Transaction.create().step(new InsertBlockStep(1, para("X")));
        Transaction inverse = tr.apply(doc).inverse();

        Assertions.assertEquals(4, doc.getBlocks().size());
        Assertions.assertEquals("A", textAt(doc, 0));
        Assertions.assertEquals("X", textAt(doc, 1));
        Assertions.assertEquals("B", textAt(doc, 2));
        Assertions.assertEquals("C", textAt(doc, 3));

        inverse.apply(doc);
        Assertions.assertEquals(3, doc.getBlocks().size());
        Assertions.assertEquals("B", textAt(doc, 1));
    }

    /************************************************************************
     * DeleteBlockStep
     ************************************************************************/

    @Test
    public void testDeleteBlock() {
        FormattedText doc = doc("A", "B", "C");
        Transaction tr = Transaction.create().step(new DeleteBlockStep(1));
        Transaction inverse = tr.apply(doc).inverse();

        Assertions.assertEquals(2, doc.getBlocks().size());
        Assertions.assertEquals("A", textAt(doc, 0));
        Assertions.assertEquals("C", textAt(doc, 1));

        // Undo restores B at index 1.
        inverse.apply(doc);
        Assertions.assertEquals(3, doc.getBlocks().size());
        Assertions.assertEquals("A", textAt(doc, 0));
        Assertions.assertEquals("B", textAt(doc, 1));
        Assertions.assertEquals("C", textAt(doc, 2));
    }

    /************************************************************************
     * ReplaceBlockStep
     ************************************************************************/

    @Test
    public void testReplaceBlock() {
        FormattedText doc = doc("A", "B", "C");
        Transaction tr = Transaction.create().step(new ReplaceBlockStep(1, para("X")));
        Transaction inverse = tr.apply(doc).inverse();

        Assertions.assertEquals(3, doc.getBlocks().size());
        Assertions.assertEquals("X", textAt(doc, 1));

        // Undo restores original.
        inverse.apply(doc);
        Assertions.assertEquals("B", textAt(doc, 1));
    }

    /************************************************************************
     * SetBlockTypeStep
     ************************************************************************/

    @Test
    public void testSetBlockType() {
        FormattedText doc = doc("A");
        Assertions.assertEquals(BlockType.PARA, doc.getBlocks().get(0).getType());

        Transaction tr = Transaction.create().step(new SetBlockTypeStep(0, BlockType.H1));
        Transaction inverse = tr.apply(doc).inverse();

        Assertions.assertEquals(BlockType.H1, doc.getBlocks().get(0).getType());

        inverse.apply(doc);
        Assertions.assertEquals(BlockType.PARA, doc.getBlocks().get(0).getType());
    }

    /************************************************************************
     * SetBlockIndentStep
     ************************************************************************/

    @Test
    public void testSetBlockIndent() {
        FormattedText doc = doc("A");
        Assertions.assertEquals(0, doc.getBlocks().get(0).getIndent());

        Transaction tr = Transaction.create().step(new SetBlockIndentStep(0, 3));
        Transaction inverse = tr.apply(doc).inverse();

        Assertions.assertEquals(3, doc.getBlocks().get(0).getIndent());

        inverse.apply(doc);
        Assertions.assertEquals(0, doc.getBlocks().get(0).getIndent());
    }

    /************************************************************************
     * MoveBlockStep
     ************************************************************************/

    @Test
    public void testMoveBlock_down() {
        // [A, B, C, D, E] -> move B,C to end -> [A, D, E, B, C]
        FormattedText doc = doc("A", "B", "C", "D", "E");
        Transaction tr = Transaction.create().step(new MoveBlockStep(1, 3, 2));
        Transaction inverse = tr.apply(doc).inverse();

        Assertions.assertEquals(5, doc.getBlocks().size());
        Assertions.assertEquals("A", textAt(doc, 0));
        Assertions.assertEquals("D", textAt(doc, 1));
        Assertions.assertEquals("E", textAt(doc, 2));
        Assertions.assertEquals("B", textAt(doc, 3));
        Assertions.assertEquals("C", textAt(doc, 4));

        // Undo.
        inverse.apply(doc);
        Assertions.assertEquals("A", textAt(doc, 0));
        Assertions.assertEquals("B", textAt(doc, 1));
        Assertions.assertEquals("C", textAt(doc, 2));
        Assertions.assertEquals("D", textAt(doc, 3));
        Assertions.assertEquals("E", textAt(doc, 4));
    }

    @Test
    public void testMoveBlock_up() {
        // [A, B, C, D, E] -> move D,E to index 1 -> [A, D, E, B, C]
        FormattedText doc = doc("A", "B", "C", "D", "E");
        Transaction tr = Transaction.create().step(new MoveBlockStep(3, 1, 2));
        Transaction inverse = tr.apply(doc).inverse();

        Assertions.assertEquals("A", textAt(doc, 0));
        Assertions.assertEquals("D", textAt(doc, 1));
        Assertions.assertEquals("E", textAt(doc, 2));
        Assertions.assertEquals("B", textAt(doc, 3));
        Assertions.assertEquals("C", textAt(doc, 4));

        inverse.apply(doc);
        Assertions.assertEquals("A", textAt(doc, 0));
        Assertions.assertEquals("B", textAt(doc, 1));
        Assertions.assertEquals("C", textAt(doc, 2));
        Assertions.assertEquals("D", textAt(doc, 3));
        Assertions.assertEquals("E", textAt(doc, 4));
    }

    @Test
    public void testMoveBlock_toStart() {
        // [A, B, C] -> move C to start -> [C, A, B]
        FormattedText doc = doc("A", "B", "C");
        Transaction tr = Transaction.create().step(new MoveBlockStep(2, 0, 1));
        Transaction inverse = tr.apply(doc).inverse();

        Assertions.assertEquals("C", textAt(doc, 0));
        Assertions.assertEquals("A", textAt(doc, 1));
        Assertions.assertEquals("B", textAt(doc, 2));

        inverse.apply(doc);
        Assertions.assertEquals("A", textAt(doc, 0));
        Assertions.assertEquals("B", textAt(doc, 1));
        Assertions.assertEquals("C", textAt(doc, 2));
    }

    @Test
    public void testMoveBlock_noop() {
        FormattedText doc = doc("A", "B", "C");
        Transaction tr = Transaction.create().step(new MoveBlockStep(1, 1, 1));
        Transaction inverse = tr.apply(doc).inverse();

        Assertions.assertEquals("A", textAt(doc, 0));
        Assertions.assertEquals("B", textAt(doc, 1));
        Assertions.assertEquals("C", textAt(doc, 2));

        inverse.apply(doc);
        Assertions.assertEquals("B", textAt(doc, 1));
    }

    /************************************************************************
     * Compound transactions (multiple steps)
     ************************************************************************/

    @Test
    public void testCompoundTransaction_splitBlock() {
        // Simulate splitting block 1 ("Hello World") at position 5
        // into "Hello" and " World".
        FormattedText doc = doc("A", "Hello World", "C");
        Transaction tr = Transaction.create()
            .step(new ReplaceBlockStep(1, para("Hello")))
            .step(new InsertBlockStep(2, para(" World")));
        Transaction inverse = tr.apply(doc).inverse();

        Assertions.assertEquals(4, doc.getBlocks().size());
        Assertions.assertEquals("Hello", textAt(doc, 1));
        Assertions.assertEquals(" World", textAt(doc, 2));
        Assertions.assertEquals("C", textAt(doc, 3));

        // Undo restores original.
        inverse.apply(doc);
        Assertions.assertEquals(3, doc.getBlocks().size());
        Assertions.assertEquals("Hello World", textAt(doc, 1));
        Assertions.assertEquals("C", textAt(doc, 2));
    }

    @Test
    public void testCompoundTransaction_mergeBlocks() {
        // Simulate merging block 1 and 2.
        FormattedText doc = doc("A", "Hello", " World", "C");
        Transaction tr = Transaction.create()
            .step(new ReplaceBlockStep(1, para("Hello World")))
            .step(new DeleteBlockStep(2));
        Transaction inverse = tr.apply(doc).inverse();

        Assertions.assertEquals(3, doc.getBlocks().size());
        Assertions.assertEquals("Hello World", textAt(doc, 1));
        Assertions.assertEquals("C", textAt(doc, 2));

        inverse.apply(doc);
        Assertions.assertEquals(4, doc.getBlocks().size());
        Assertions.assertEquals("Hello", textAt(doc, 1));
        Assertions.assertEquals(" World", textAt(doc, 2));
    }

    /************************************************************************
     * EditorState + History integration
     ************************************************************************/

    @Test
    public void testEditorState_applyAndUndo() {
        FormattedText doc = doc("A", "B", "C");
        EditorState state = EditorState.create(doc);
        History history = new History();

        // Apply: insert block.
        Transaction tr = Transaction.create().step(new InsertBlockStep(1, para("X")));
        Transaction inverse = state.apply(tr);
        history.push(inverse);

        Assertions.assertEquals(4, state.doc().getBlocks().size());
        Assertions.assertEquals("X", textAt(state.doc(), 1));

        // Undo.
        Assertions.assertTrue(history.canUndo());
        history.undo(state);
        Assertions.assertEquals(3, state.doc().getBlocks().size());
        Assertions.assertEquals("B", textAt(state.doc(), 1));

        // Redo.
        Assertions.assertTrue(history.canRedo());
        history.redo(state);
        Assertions.assertEquals(4, state.doc().getBlocks().size());
        Assertions.assertEquals("X", textAt(state.doc(), 1));
    }

    @Test
    public void testHistory_multipleUndoRedo() {
        FormattedText doc = doc("A");
        EditorState state = EditorState.create(doc);
        History history = new History();

        // Change 1: insert B.
        Transaction inv1 = state.apply(Transaction.create().step(new InsertBlockStep(1, para("B"))));
        history.push(inv1);
        Assertions.assertEquals(2, state.doc().getBlocks().size());

        // Change 2: insert C.
        Transaction inv2 = state.apply(Transaction.create().step(new InsertBlockStep(2, para("C"))));
        history.push(inv2);
        Assertions.assertEquals(3, state.doc().getBlocks().size());

        // Change 3: set A to H1.
        Transaction inv3 = state.apply(Transaction.create().step(new SetBlockTypeStep(0, BlockType.H1)));
        history.push(inv3);
        Assertions.assertEquals(BlockType.H1, state.doc().getBlocks().get(0).getType());

        // Undo 3: type reverts.
        history.undo(state);
        Assertions.assertEquals(BlockType.PARA, state.doc().getBlocks().get(0).getType());
        Assertions.assertEquals(3, state.doc().getBlocks().size());

        // Undo 2: C removed.
        history.undo(state);
        Assertions.assertEquals(2, state.doc().getBlocks().size());

        // Undo 1: B removed.
        history.undo(state);
        Assertions.assertEquals(1, state.doc().getBlocks().size());
        Assertions.assertEquals("A", textAt(state.doc(), 0));

        // No more undos.
        Assertions.assertFalse(history.undo(state));

        // Redo all 3.
        history.redo(state); // B inserted
        Assertions.assertEquals(2, state.doc().getBlocks().size());
        history.redo(state); // C inserted
        Assertions.assertEquals(3, state.doc().getBlocks().size());
        history.redo(state); // type changed
        Assertions.assertEquals(BlockType.H1, state.doc().getBlocks().get(0).getType());

        // No more redos.
        Assertions.assertFalse(history.redo(state));
    }

    @Test
    public void testHistory_newChangeClearsRedo() {
        FormattedText doc = doc("A");
        EditorState state = EditorState.create(doc);
        History history = new History();

        // Change 1.
        history.push(state.apply(Transaction.create().step(new InsertBlockStep(1, para("B")))));

        // Undo.
        history.undo(state);
        Assertions.assertTrue(history.canRedo());

        // New change clears redo.
        history.push(state.apply(Transaction.create().step(new InsertBlockStep(1, para("C")))));
        Assertions.assertFalse(history.canRedo());
    }

    /************************************************************************
     * Selection (block-relative)
     ************************************************************************/

    @Test
    public void testSelection_cursor() {
        Selection sel = Selection.cursor(2, 5);
        Assertions.assertTrue(sel.isCursor());
        Assertions.assertEquals(2, sel.anchorBlock());
        Assertions.assertEquals(5, sel.anchorOffset());
        Assertions.assertEquals(2, sel.fromBlock());
        Assertions.assertEquals(5, sel.fromOffset());
    }

    @Test
    public void testSelection_range() {
        Selection sel = Selection.range(1, 3, 3, 7);
        Assertions.assertFalse(sel.isCursor());
        Assertions.assertEquals(1, sel.fromBlock());
        Assertions.assertEquals(3, sel.fromOffset());
        Assertions.assertEquals(3, sel.toBlock());
        Assertions.assertEquals(7, sel.toOffset());
    }

    @Test
    public void testSelection_reverseRange() {
        // Head before anchor (selection made backwards).
        Selection sel = Selection.range(3, 7, 1, 3);
        Assertions.assertEquals(1, sel.fromBlock());
        Assertions.assertEquals(3, sel.fromOffset());
        Assertions.assertEquals(3, sel.toBlock());
        Assertions.assertEquals(7, sel.toOffset());
    }

    /************************************************************************
     * Positions utility
     ************************************************************************/

    @Test
    public void testPositions_nodeSize() {
        // PARA("Hello") → nodeSize = 2 + 5 = 7
        Assertions.assertEquals(7, Positions.nodeSize(para("Hello")));

        // Empty PARA → nodeSize = 2 + 0 = 2
        FormattedBlock empty = new FormattedBlock(BlockType.PARA);
        empty.line("");
        Assertions.assertEquals(2, Positions.nodeSize(empty));
    }

    @Test
    public void testPositions_multiLineBlock() {
        // PARA with "Hello" and "World" → contentSize = 5 + 1 + 5 = 11
        FormattedBlock block = new FormattedBlock(BlockType.PARA);
        block.line("Hello");
        block.line("World");
        Assertions.assertEquals(11, Positions.contentSize(block));
        Assertions.assertEquals(13, Positions.nodeSize(block));
    }

    @Test
    public void testPositions_documentLength() {
        // doc("AB", "C") → block0=4, block1=3, total=7
        FormattedText doc = doc("AB", "C");
        Assertions.assertEquals(7, Positions.length(doc));
    }

    @Test
    public void testPositions_blockStart() {
        // doc("AB", "CD", "E")
        // block0: nodeSize=4 (2+2), block1: nodeSize=4 (2+2), block2: nodeSize=3 (2+1)
        FormattedText doc = doc("AB", "CD", "E");
        Assertions.assertEquals(0, Positions.blockStart(doc, 0));
        Assertions.assertEquals(4, Positions.blockStart(doc, 1));
        Assertions.assertEquals(8, Positions.blockStart(doc, 2));
    }

    @Test
    public void testPositions_toFlat() {
        // doc("AB", "CD")
        // Block 0: [open=0] A=1 B=2 [close=3]
        // Block 1: [open=4] C=5 D=6 [close=7]
        FormattedText doc = doc("AB", "CD");
        Assertions.assertEquals(1, Positions.toFlat(doc, 0, 0)); // before A
        Assertions.assertEquals(2, Positions.toFlat(doc, 0, 1)); // before B
        Assertions.assertEquals(3, Positions.toFlat(doc, 0, 2)); // after B (at close)
        Assertions.assertEquals(5, Positions.toFlat(doc, 1, 0)); // before C
        Assertions.assertEquals(7, Positions.toFlat(doc, 1, 2)); // after D
    }

    @Test
    public void testPositions_resolve() {
        // doc("AB", "CD")
        FormattedText doc = doc("AB", "CD");

        // Block 0 open.
        ResolvedPosition r0 = Positions.resolve(doc, 0);
        Assertions.assertEquals(0, r0.blockIndex());
        Assertions.assertTrue(r0.isAtBoundary());

        // 'A' at position 1.
        ResolvedPosition r1 = Positions.resolve(doc, 1);
        Assertions.assertEquals(0, r1.blockIndex());
        Assertions.assertEquals(0, r1.lineIndex());
        Assertions.assertEquals(0, r1.charInLine());
        Assertions.assertEquals(0, r1.blockOffset());

        // 'B' at position 2.
        ResolvedPosition r2 = Positions.resolve(doc, 2);
        Assertions.assertEquals(0, r2.blockIndex());
        Assertions.assertEquals(0, r2.lineIndex());
        Assertions.assertEquals(1, r2.charInLine());
        Assertions.assertEquals(1, r2.blockOffset());

        // Block 0 close at position 3.
        ResolvedPosition r3 = Positions.resolve(doc, 3);
        Assertions.assertEquals(0, r3.blockIndex());
        Assertions.assertTrue(r3.isAtBoundary());
        Assertions.assertEquals(2, r3.blockOffset()); // at content end

        // Block 1 open at position 4.
        ResolvedPosition r4 = Positions.resolve(doc, 4);
        Assertions.assertEquals(1, r4.blockIndex());
        Assertions.assertTrue(r4.isAtBoundary());

        // 'C' at position 5.
        ResolvedPosition r5 = Positions.resolve(doc, 5);
        Assertions.assertEquals(1, r5.blockIndex());
        Assertions.assertEquals(0, r5.lineIndex());
        Assertions.assertEquals(0, r5.charInLine());
    }

    @Test
    public void testPositions_resolveMultiLine() {
        // PARA("Hello\nWorld") → [open] H e l l o \n W o r l d [close]
        //                          0    1 2 3 4 5  6  7 8 9 10 11  12
        FormattedText doc = new FormattedText();
        doc.block(BlockType.PARA, b -> { b.line("Hello"); b.line("World"); });

        // 'W' at position 7 → line 1, char 0.
        ResolvedPosition r = Positions.resolve(doc, 7);
        Assertions.assertEquals(0, r.blockIndex());
        Assertions.assertEquals(1, r.lineIndex());
        Assertions.assertEquals(0, r.charInLine());
        Assertions.assertEquals(6, r.blockOffset());
    }

    @Test
    public void testPositions_toFlatAndResolveRoundTrip() {
        FormattedText doc = doc("Hello", "World", "!");
        // Round-trip: block 1, offset 3 → flat → resolve → same coordinates.
        int flat = Positions.toFlat(doc, 1, 3);
        ResolvedPosition r = Positions.resolve(doc, flat);
        Assertions.assertEquals(1, r.blockIndex());
        Assertions.assertEquals(3, r.blockOffset());
        Assertions.assertEquals(0, r.lineIndex());
        Assertions.assertEquals(3, r.charInLine());
    }

    /************************************************************************
     * StepMap
     ************************************************************************/

    @Test
    public void testStepMap_empty() {
        Assertions.assertEquals(5, StepMap.EMPTY.map(5, 1));
        Assertions.assertEquals(0, StepMap.EMPTY.map(0, -1));
    }

    @Test
    public void testStepMap_insertion() {
        // Insert 3 positions at position 5.
        StepMap map = StepMap.of(5, 0, 3);
        Assertions.assertEquals(4, map.map(4, 1));   // before → unchanged
        Assertions.assertEquals(5, map.map(5, -1));   // at insert, bias left → stays
        Assertions.assertEquals(8, map.map(5, 1));    // at insert, bias right → after inserted
        Assertions.assertEquals(13, map.map(10, 1));  // after → shifted by +3
    }

    @Test
    public void testStepMap_deletion() {
        // Delete 3 positions at position 5 (old 5,6,7 → gone).
        StepMap map = StepMap.of(5, 3, 0);
        Assertions.assertEquals(4, map.map(4, 1));   // before → unchanged
        Assertions.assertEquals(5, map.map(6, -1));   // within deleted, bias left → collapse to start
        Assertions.assertEquals(5, map.map(6, 1));    // within deleted, bias right → collapse to start (newSize=0)
        Assertions.assertEquals(5, map.map(8, 1));    // just after → shifted by -3
        Assertions.assertEquals(7, map.map(10, 1));   // after → shifted by -3
    }

    @Test
    public void testStepMap_replacement() {
        // Replace 3 positions at position 5 with 5 positions.
        StepMap map = StepMap.of(5, 3, 5);
        Assertions.assertEquals(4, map.map(4, 1));    // before → unchanged
        Assertions.assertEquals(5, map.map(6, -1));    // within, bias left → start
        Assertions.assertEquals(10, map.map(6, 1));    // within, bias right → end of new
        Assertions.assertEquals(12, map.map(10, 1));   // after → shifted by +2
    }

    /************************************************************************
     * Mapping (composed StepMaps)
     ************************************************************************/

    @Test
    public void testMapping_insert() {
        // Insert block "XY" (nodeSize=4) at block 1 of doc("AB", "CD").
        // Block 1 starts at position 4 in old doc.
        // StepMap: (4, 0, 4) — insert 4 positions at pos 4.
        FormattedText doc = doc("AB", "CD");
        TransactionResult result = Transaction.create()
            .step(new InsertBlockStep(1, para("XY")))
            .apply(doc);

        Mapping mapping = result.mapping();

        // Position 1 (A in block 0) → unchanged.
        Assertions.assertEquals(1, mapping.map(1, 1));

        // Position 5 (C in old block 1, now block 2) → shifted by 4.
        Assertions.assertEquals(9, mapping.map(5, 1));
    }

    /************************************************************************
     * Selection flat position conversion
     ************************************************************************/

    @Test
    public void testSelection_flatConversion() {
        FormattedText doc = doc("AB", "CD");
        Selection sel = Selection.cursor(1, 1); // before D

        // toFlat: blockStart(1)=4, + 1 + 1 = 6.
        Assertions.assertEquals(6, sel.anchorFlat(doc));
        Assertions.assertEquals(6, sel.headFlat(doc));
    }

    @Test
    public void testSelection_fromFlat() {
        FormattedText doc = doc("AB", "CD");
        // Flat position 6 = block 1, offset 1 (before D).
        Selection sel = Selection.fromFlat(doc, 6, 6);
        Assertions.assertTrue(sel.isCursor());
        Assertions.assertEquals(1, sel.anchorBlock());
        Assertions.assertEquals(1, sel.anchorOffset());
    }

    /************************************************************************
     * EditorState selection mapping
     ************************************************************************/

    @Test
    public void testEditorState_selectionMappedAfterInsert() {
        // Cursor in block 1 at offset 1 (before "D" in "CD").
        FormattedText doc = doc("AB", "CD");
        EditorState state = EditorState.create(doc, Selection.cursor(1, 1));

        // Insert block "XY" at index 1 (before "CD").
        state.apply(Transaction.create().step(new InsertBlockStep(1, para("XY"))));

        // "CD" is now at block 2, cursor should follow.
        Selection sel = state.selection();
        Assertions.assertEquals(2, sel.anchorBlock());
        Assertions.assertEquals(1, sel.anchorOffset());
    }

    @Test
    public void testEditorState_selectionMappedAfterDelete() {
        // Cursor in block 2 ("C") at offset 0.
        FormattedText doc = doc("A", "B", "C");
        EditorState state = EditorState.create(doc, Selection.cursor(2, 0));

        // Delete block 0 ("A").
        state.apply(Transaction.create().step(new DeleteBlockStep(0)));

        // "C" is now block 1.
        Selection sel = state.selection();
        Assertions.assertEquals(1, sel.anchorBlock());
        Assertions.assertEquals(0, sel.anchorOffset());
    }

    @Test
    public void testEditorState_selectionMappedAfterReplace() {
        // Cursor in block 1 at offset 4 (after "ABCD" → end of block).
        FormattedText doc = doc("X", "ABCD");
        EditorState state = EditorState.create(doc, Selection.cursor(1, 4));

        // Replace block 1 with shorter text "AB".
        state.apply(Transaction.create().step(new ReplaceBlockStep(1, para("AB"))));

        // Cursor was at offset 4 which is now beyond block end (len 2).
        // StepMap maps within replaced range → bias right = end of new content.
        Selection sel = state.selection();
        Assertions.assertEquals(1, sel.anchorBlock());
        Assertions.assertEquals(2, sel.anchorOffset());
    }

    @Test
    public void testEditorState_selectionStableForTypeChange() {
        // Cursor in block 0 at offset 1.
        FormattedText doc = doc("AB");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 1));

        // Change type — no position changes.
        state.apply(Transaction.create().step(new SetBlockTypeStep(0, BlockType.H1)));

        Selection sel = state.selection();
        Assertions.assertEquals(0, sel.anchorBlock());
        Assertions.assertEquals(1, sel.anchorOffset());
    }

    /************************************************************************
     * InsertTextStep
     ************************************************************************/

    @Test
    public void testInsertText_middle() {
        FormattedText doc = doc("Hello");
        Transaction tr = Transaction.create().step(new InsertTextStep(0, 3, "XY"));
        TransactionResult result = tr.apply(doc);

        Assertions.assertEquals("HelXYlo", textAt(doc, 0));

        // Undo.
        result.inverse().apply(doc);
        Assertions.assertEquals("Hello", textAt(doc, 0));
    }

    @Test
    public void testInsertText_atStart() {
        FormattedText doc = doc("World");
        Transaction.create().step(new InsertTextStep(0, 0, "Hello ")).apply(doc);
        Assertions.assertEquals("Hello World", textAt(doc, 0));
    }

    @Test
    public void testInsertText_atEnd() {
        FormattedText doc = doc("Hello");
        Transaction.create().step(new InsertTextStep(0, 5, " World")).apply(doc);
        Assertions.assertEquals("Hello World", textAt(doc, 0));
    }

    @Test
    public void testInsertText_preciseStepMap() {
        // doc("Hello") → [open=0] H=1 e=2 l=3 l=4 o=5 [close=6]
        // Insert "XY" at offset 3 → StepMap(4, 0, 2)
        FormattedText doc = doc("Hello");
        TransactionResult result = Transaction.create()
            .step(new InsertTextStep(0, 3, "XY"))
            .apply(doc);

        Mapping mapping = result.mapping();
        // Position 1 (H) → unchanged.
        Assertions.assertEquals(1, mapping.map(1, 1));
        // Position 3 (first l) → unchanged.
        Assertions.assertEquals(3, mapping.map(3, 1));
        // Position 4 (second l) → shifted by 2 → 6.
        Assertions.assertEquals(6, mapping.map(4, 1));
        // Position 5 (o) → shifted by 2 → 7.
        Assertions.assertEquals(7, mapping.map(5, 1));
    }

    @Test
    public void testInsertText_preservesFormatting() {
        // Insert text in a block with bold formatting, undo should restore formatting.
        FormattedText doc = new FormattedText();
        doc.block(BlockType.PARA, b -> b.line(l -> l.append("Hello", FormatType.BLD)));

        Transaction tr = Transaction.create().step(new InsertTextStep(0, 3, "XY"));
        TransactionResult result = tr.apply(doc);

        // Undo restores original block including formatting.
        result.inverse().apply(doc);
        FormattedLine line = doc.getBlocks().get(0).getLines().get(0);
        Assertions.assertEquals("Hello", line.getText());
        Assertions.assertEquals(1, line.getFormatting().size());
        Assertions.assertEquals(FormatType.BLD, line.getFormatting().get(0).getFormats().get(0));
    }

    /************************************************************************
     * DeleteTextStep
     ************************************************************************/

    @Test
    public void testDeleteText_middle() {
        FormattedText doc = doc("Hello World");
        Transaction tr = Transaction.create().step(new DeleteTextStep(0, 5, 6));
        TransactionResult result = tr.apply(doc);

        Assertions.assertEquals("Hello", textAt(doc, 0));

        // Undo restores.
        result.inverse().apply(doc);
        Assertions.assertEquals("Hello World", textAt(doc, 0));
    }

    @Test
    public void testDeleteText_fromStart() {
        FormattedText doc = doc("Hello");
        Transaction.create().step(new DeleteTextStep(0, 0, 3)).apply(doc);
        Assertions.assertEquals("lo", textAt(doc, 0));
    }

    @Test
    public void testDeleteText_preciseStepMap() {
        // doc("Hello World") → [open=0] content=1..11 [close=12]
        // Delete 3 chars at offset 5 → StepMap(6, 3, 0)
        FormattedText doc = doc("Hello World");
        TransactionResult result = Transaction.create()
            .step(new DeleteTextStep(0, 5, 3))
            .apply(doc);

        Mapping mapping = result.mapping();
        // Position 1 (H) → unchanged.
        Assertions.assertEquals(1, mapping.map(1, 1));
        // Position 5 (o) → unchanged (just before deletion).
        Assertions.assertEquals(5, mapping.map(5, 1));
        // Position 9 (r, first char after deleted range) → shifted by -3 → 6.
        Assertions.assertEquals(6, mapping.map(9, 1));
    }

    @Test
    public void testDeleteText_undoRestoresFormatting() {
        // Delete bold text, undo should restore formatting.
        FormattedText doc = new FormattedText();
        doc.block(BlockType.PARA, b -> b.line(l -> l.append("Hello", FormatType.BLD)));

        TransactionResult result = Transaction.create()
            .step(new DeleteTextStep(0, 2, 2))
            .apply(doc);

        Assertions.assertEquals("Heo", textAt(doc, 0));

        // Undo restores formatting.
        result.inverse().apply(doc);
        FormattedLine line = doc.getBlocks().get(0).getLines().get(0);
        Assertions.assertEquals("Hello", line.getText());
        Assertions.assertEquals(1, line.getFormatting().size());
    }

    /************************************************************************
     * SplitBlockStep
     ************************************************************************/

    @Test
    public void testSplitBlock_middle() {
        FormattedText doc = doc("Hello World");
        Transaction tr = Transaction.create().step(new SplitBlockStep(0, 5));
        TransactionResult result = tr.apply(doc);

        Assertions.assertEquals(2, doc.getBlocks().size());
        Assertions.assertEquals("Hello", textAt(doc, 0));
        Assertions.assertEquals(" World", textAt(doc, 1));

        // Undo joins back.
        result.inverse().apply(doc);
        Assertions.assertEquals(1, doc.getBlocks().size());
        Assertions.assertEquals("Hello World", textAt(doc, 0));
    }

    @Test
    public void testSplitBlock_atStart() {
        FormattedText doc = doc("Hello");
        Transaction.create().step(new SplitBlockStep(0, 0)).apply(doc);

        Assertions.assertEquals(2, doc.getBlocks().size());
        // split(0) moves everything to the right portion.
        Assertions.assertEquals("Hello", textAt(doc, 1));
    }

    @Test
    public void testSplitBlock_atEnd() {
        FormattedText doc = doc("Hello");
        Transaction.create().step(new SplitBlockStep(0, 5)).apply(doc);

        Assertions.assertEquals(2, doc.getBlocks().size());
        Assertions.assertEquals("Hello", textAt(doc, 0));
    }

    @Test
    public void testSplitBlock_stepMap() {
        // doc("Hello") → [open=0] H=1 e=2 l=3 l=4 o=5 [close=6]
        // Split at offset 3 → inserts 2 boundary tokens at flat pos 4.
        // After: [open=0] H=1 e=2 l=3 [close=4] [open=5] l=6 o=7 [close=8]
        FormattedText doc = doc("Hello");
        TransactionResult result = Transaction.create()
            .step(new SplitBlockStep(0, 3))
            .apply(doc);

        Mapping mapping = result.mapping();
        // Position 1 (H) → unchanged.
        Assertions.assertEquals(1, mapping.map(1, 1));
        // Position 3 (first l) → unchanged.
        Assertions.assertEquals(3, mapping.map(3, 1));
        // Position 4 (second l) → shifted by +2 → 6.
        Assertions.assertEquals(6, mapping.map(4, 1));
        // Position 5 (o) → shifted by +2 → 7.
        Assertions.assertEquals(7, mapping.map(5, 1));
    }

    @Test
    public void testSplitBlock_withOtherBlocks() {
        FormattedText doc = doc("A", "Hello World", "B");
        Transaction.create().step(new SplitBlockStep(1, 5)).apply(doc);

        Assertions.assertEquals(4, doc.getBlocks().size());
        Assertions.assertEquals("A", textAt(doc, 0));
        Assertions.assertEquals("Hello", textAt(doc, 1));
        Assertions.assertEquals(" World", textAt(doc, 2));
        Assertions.assertEquals("B", textAt(doc, 3));
    }

    /************************************************************************
     * JoinBlocksStep
     ************************************************************************/

    @Test
    public void testJoinBlocks() {
        FormattedText doc = doc("Hello", " World");
        Transaction tr = Transaction.create().step(new JoinBlocksStep(0));
        TransactionResult result = tr.apply(doc);

        Assertions.assertEquals(1, doc.getBlocks().size());
        Assertions.assertEquals("Hello World", textAt(doc, 0));

        // Undo splits back.
        result.inverse().apply(doc);
        Assertions.assertEquals(2, doc.getBlocks().size());
        Assertions.assertEquals("Hello", textAt(doc, 0));
        Assertions.assertEquals(" World", textAt(doc, 1));
    }

    @Test
    public void testJoinBlocks_stepMap() {
        // doc("AB", "CD") → [open=0] A=1 B=2 [close=3] [open=4] C=5 D=6 [close=7]
        // Join removes 2 boundary tokens at pos 3 (close of block 0).
        // After: [open=0] A=1 B=2 C=3 D=4 [close=5]
        FormattedText doc = doc("AB", "CD");
        TransactionResult result = Transaction.create()
            .step(new JoinBlocksStep(0))
            .apply(doc);

        Mapping mapping = result.mapping();
        // Position 1 (A) → unchanged.
        Assertions.assertEquals(1, mapping.map(1, 1));
        // Position 2 (B) → unchanged.
        Assertions.assertEquals(2, mapping.map(2, 1));
        // Position 5 (C in old doc) → shifted by -2 → 3.
        Assertions.assertEquals(3, mapping.map(5, 1));
        // Position 6 (D in old doc) → shifted by -2 → 4.
        Assertions.assertEquals(4, mapping.map(6, 1));
    }

    @Test
    public void testJoinBlocks_withSurroundingBlocks() {
        FormattedText doc = doc("A", "Hello", " World", "B");
        Transaction.create().step(new JoinBlocksStep(1)).apply(doc);

        Assertions.assertEquals(3, doc.getBlocks().size());
        Assertions.assertEquals("A", textAt(doc, 0));
        Assertions.assertEquals("Hello World", textAt(doc, 1));
        Assertions.assertEquals("B", textAt(doc, 2));
    }

    @Test
    public void testSplitAndJoin_roundTrip() {
        // Split then undo (join), verify original document is restored.
        FormattedText doc = doc("Hello World");
        TransactionResult splitResult = Transaction.create()
            .step(new SplitBlockStep(0, 5))
            .apply(doc);

        Assertions.assertEquals(2, doc.getBlocks().size());

        // Undo (join).
        TransactionResult joinResult = splitResult.inverse().apply(doc);
        Assertions.assertEquals(1, doc.getBlocks().size());
        Assertions.assertEquals("Hello World", textAt(doc, 0));

        // Redo (split again).
        joinResult.inverse().apply(doc);
        Assertions.assertEquals(2, doc.getBlocks().size());
        Assertions.assertEquals("Hello", textAt(doc, 0));
    }

    /************************************************************************
     * Explicit selection on Transaction
     ************************************************************************/

    @Test
    public void testTransaction_explicitSelection() {
        // With explicit selection, EditorState should use it instead of mapping.
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));

        Transaction tr = Transaction.create()
            .step(new InsertTextStep(0, 0, "XY"))
            .setSelection(Selection.cursor(0, 2));
        state.apply(tr);

        // Selection should be at offset 2 (after "XY"), not mapped from offset 0.
        Assertions.assertEquals(0, state.selection().anchorBlock());
        Assertions.assertEquals(2, state.selection().anchorOffset());
    }

    @Test
    public void testEditorState_inverseSavesPreApplySelection() {
        // The inverse transaction should carry the pre-apply selection.
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 3));

        Transaction tr = Transaction.create()
            .step(new InsertTextStep(0, 3, "XY"))
            .setSelection(Selection.cursor(0, 5));
        Transaction inverse = state.apply(tr);

        // Inverse should have the pre-apply selection (cursor at offset 3).
        Assertions.assertNotNull(inverse.selection());
        Assertions.assertEquals(0, inverse.selection().anchorBlock());
        Assertions.assertEquals(3, inverse.selection().anchorOffset());
    }

    @Test
    public void testEditorState_undoRedoCursorCycle() {
        // Full undo/redo cycle: type "XY" at offset 3, undo, redo.
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 3));
        History history = new History();

        // Type "XY".
        Transaction tr = Transaction.create()
            .step(new InsertTextStep(0, 3, "XY"))
            .setSelection(Selection.cursor(0, 5));
        history.push(state.apply(tr));

        Assertions.assertEquals("HelXYlo", textAt(doc, 0));
        Assertions.assertEquals(5, state.selection().anchorOffset());

        // Undo: cursor back to 3.
        history.undo(state);
        Assertions.assertEquals("Hello", textAt(doc, 0));
        Assertions.assertEquals(3, state.selection().anchorOffset());

        // Redo: cursor back to 5.
        history.redo(state);
        Assertions.assertEquals("HelXYlo", textAt(doc, 0));
        Assertions.assertEquals(5, state.selection().anchorOffset());
    }

    /************************************************************************
     * Commands
     ************************************************************************/

    @Test
    public void testCommands_insertText() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 5));

        Transaction tr = Commands.insertText(state, " World");
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals("Hello World", textAt(doc, 0));
        Assertions.assertEquals(11, state.selection().anchorOffset());
    }

    @Test
    public void testCommands_insertText_withSelection() {
        // Selecting "ell" in "Hello" and typing "a" → "Hao".
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.range(0, 1, 0, 4));

        Transaction tr = Commands.insertText(state, "a");
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals("Hao", textAt(doc, 0));
        Assertions.assertEquals(2, state.selection().anchorOffset());
    }

    @Test
    public void testCommands_insertText_emptyReturnsNull() {
        EditorState state = EditorState.create(doc("Hello"), Selection.cursor(0, 0));
        Assertions.assertNull(Commands.insertText(state, ""));
        Assertions.assertNull(Commands.insertText(state, null));
    }

    @Test
    public void testCommands_deleteCharBefore() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 3));

        Transaction tr = Commands.deleteCharBefore(state);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals("Helo", textAt(doc, 0));
        Assertions.assertEquals(2, state.selection().anchorOffset());
    }

    @Test
    public void testCommands_deleteCharBefore_atBlockStart_joins() {
        FormattedText doc = doc("Hello", " World");
        EditorState state = EditorState.create(doc, Selection.cursor(1, 0));

        Transaction tr = Commands.deleteCharBefore(state);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals(1, doc.getBlocks().size());
        Assertions.assertEquals("Hello World", textAt(doc, 0));
        Assertions.assertEquals(0, state.selection().anchorBlock());
        Assertions.assertEquals(5, state.selection().anchorOffset());
    }

    @Test
    public void testCommands_deleteCharBefore_atDocStart_returnsNull() {
        EditorState state = EditorState.create(doc("Hello"), Selection.cursor(0, 0));
        Assertions.assertNull(Commands.deleteCharBefore(state));
    }

    @Test
    public void testCommands_deleteCharAfter() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 2));

        Transaction tr = Commands.deleteCharAfter(state);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals("Helo", textAt(doc, 0));
        Assertions.assertEquals(2, state.selection().anchorOffset());
    }

    @Test
    public void testCommands_deleteCharAfter_atBlockEnd_joins() {
        FormattedText doc = doc("Hello", " World");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 5));

        Transaction tr = Commands.deleteCharAfter(state);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals(1, doc.getBlocks().size());
        Assertions.assertEquals("Hello World", textAt(doc, 0));
        Assertions.assertEquals(5, state.selection().anchorOffset());
    }

    @Test
    public void testCommands_deleteCharAfter_atDocEnd_returnsNull() {
        EditorState state = EditorState.create(doc("Hello"), Selection.cursor(0, 5));
        Assertions.assertNull(Commands.deleteCharAfter(state));
    }

    @Test
    public void testCommands_deleteSelection() {
        FormattedText doc = doc("Hello World");
        EditorState state = EditorState.create(doc, Selection.range(0, 5, 0, 11));

        Transaction tr = Commands.deleteSelection(state);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals("Hello", textAt(doc, 0));
        Assertions.assertEquals(5, state.selection().anchorOffset());
    }

    @Test
    public void testCommands_deleteSelection_cursor_returnsNull() {
        EditorState state = EditorState.create(doc("Hello"), Selection.cursor(0, 3));
        Assertions.assertNull(Commands.deleteSelection(state));
    }

    @Test
    public void testCommands_splitBlock() {
        FormattedText doc = doc("Hello World");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 5));

        Transaction tr = Commands.splitBlock(state);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals(2, doc.getBlocks().size());
        Assertions.assertEquals("Hello", textAt(doc, 0));
        Assertions.assertEquals(" World", textAt(doc, 1));
        Assertions.assertEquals(1, state.selection().anchorBlock());
        Assertions.assertEquals(0, state.selection().anchorOffset());
    }

    @Test
    public void testCommands_splitBlock_withRange_deletesAndSplits() {
        // "Hello" with selection [1..3) → delete "el" → "Hlo" → split at 1 → ["H", "lo"]
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.range(0, 1, 0, 3));
        Transaction tr = Commands.splitBlock(state);
        Assertions.assertNotNull(tr);
        state.apply(tr);
        Assertions.assertEquals(2, doc.getBlocks().size());
        Assertions.assertEquals("H", textAt(doc, 0));
        Assertions.assertEquals("lo", textAt(doc, 1));
    }

    @Test
    public void testCommands_joinWithPrevious() {
        FormattedText doc = doc("Hello", " World");
        EditorState state = EditorState.create(doc, Selection.cursor(1, 0));

        Transaction tr = Commands.joinWithPrevious(state);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals(1, doc.getBlocks().size());
        Assertions.assertEquals("Hello World", textAt(doc, 0));
        Assertions.assertEquals(0, state.selection().anchorBlock());
        Assertions.assertEquals(5, state.selection().anchorOffset());
    }

    @Test
    public void testCommands_joinWithPrevious_firstBlock_returnsNull() {
        EditorState state = EditorState.create(doc("Hello"), Selection.cursor(0, 0));
        Assertions.assertNull(Commands.joinWithPrevious(state));
    }

    @Test
    public void testCommands_joinWithPrevious_differentTypes_returnsNull() {
        FormattedText doc = new FormattedText();
        doc.block(BlockType.H1, b -> b.line("Title"));
        doc.block(BlockType.PARA, b -> b.line("Content"));
        EditorState state = EditorState.create(doc, Selection.cursor(1, 0));
        Assertions.assertNull(Commands.joinWithPrevious(state));
    }

    @Test
    public void testCommands_splitThenUndoRedo() {
        // Full undo/redo cycle for split block.
        FormattedText doc = doc("Hello World");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 5));
        History history = new History();

        // Split.
        history.push(state.apply(Commands.splitBlock(state)));
        Assertions.assertEquals(2, doc.getBlocks().size());
        Assertions.assertEquals(1, state.selection().anchorBlock());
        Assertions.assertEquals(0, state.selection().anchorOffset());

        // Undo.
        history.undo(state);
        Assertions.assertEquals(1, doc.getBlocks().size());
        Assertions.assertEquals("Hello World", textAt(doc, 0));
        Assertions.assertEquals(0, state.selection().anchorBlock());
        Assertions.assertEquals(5, state.selection().anchorOffset());

        // Redo.
        history.redo(state);
        Assertions.assertEquals(2, doc.getBlocks().size());
        Assertions.assertEquals(1, state.selection().anchorBlock());
        Assertions.assertEquals(0, state.selection().anchorOffset());
    }

    /************************************************************************
     * Commands.insertLineBreak
     ************************************************************************/

    @Test
    public void testCommands_insertLineBreak_midLine() {
        // "Hello World" with cursor at 5 → ["Hello", " World"] in same block.
        FormattedText doc = doc("Hello World");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 5));

        Transaction tr = Commands.insertLineBreak(state);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals(1, doc.getBlocks().size());
        List<FormattedLine> lines = doc.getBlocks().get(0).getLines();
        Assertions.assertEquals(2, lines.size());
        Assertions.assertEquals("Hello", lines.get(0).getText());
        Assertions.assertEquals(" World", lines.get(1).getText());
        Assertions.assertEquals(0, state.selection().anchorBlock());
        Assertions.assertEquals(6, state.selection().anchorOffset());
    }

    @Test
    public void testCommands_insertLineBreak_atStart() {
        // "Hello" with cursor at 0 → ["", "Hello"].
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));

        state.apply(Commands.insertLineBreak(state));

        List<FormattedLine> lines = doc.getBlocks().get(0).getLines();
        Assertions.assertEquals(2, lines.size());
        Assertions.assertEquals("", lines.get(0).getText());
        Assertions.assertEquals("Hello", lines.get(1).getText());
        Assertions.assertEquals(1, state.selection().anchorOffset());
    }

    @Test
    public void testCommands_insertLineBreak_atEnd() {
        // "Hello" with cursor at 5 → ["Hello", ""].
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 5));

        state.apply(Commands.insertLineBreak(state));

        List<FormattedLine> lines = doc.getBlocks().get(0).getLines();
        Assertions.assertEquals(2, lines.size());
        Assertions.assertEquals("Hello", lines.get(0).getText());
        Assertions.assertEquals("", lines.get(1).getText());
        Assertions.assertEquals(6, state.selection().anchorOffset());
    }

    @Test
    public void testCommands_insertLineBreak_multiLine_atBoundary() {
        // Block with ["Hello", "World"], cursor at offset 5 (end of line 0)
        // → ["Hello", "", "World"].
        FormattedText doc = new FormattedText();
        doc.block(BlockType.PARA, b -> { b.line("Hello"); b.line("World"); });
        EditorState state = EditorState.create(doc, Selection.cursor(0, 5));

        state.apply(Commands.insertLineBreak(state));

        List<FormattedLine> lines = doc.getBlocks().get(0).getLines();
        Assertions.assertEquals(3, lines.size());
        Assertions.assertEquals("Hello", lines.get(0).getText());
        Assertions.assertEquals("", lines.get(1).getText());
        Assertions.assertEquals("World", lines.get(2).getText());
        Assertions.assertEquals(6, state.selection().anchorOffset());
    }

    @Test
    public void testCommands_insertLineBreak_multiLine_atSecondLineStart() {
        // Block with ["Hello", "World"], cursor at offset 6 (start of line 1)
        // → ["Hello", "", "World"].
        FormattedText doc = new FormattedText();
        doc.block(BlockType.PARA, b -> { b.line("Hello"); b.line("World"); });
        EditorState state = EditorState.create(doc, Selection.cursor(0, 6));

        state.apply(Commands.insertLineBreak(state));

        List<FormattedLine> lines = doc.getBlocks().get(0).getLines();
        Assertions.assertEquals(3, lines.size());
        Assertions.assertEquals("Hello", lines.get(0).getText());
        Assertions.assertEquals("", lines.get(1).getText());
        Assertions.assertEquals("World", lines.get(2).getText());
        Assertions.assertEquals(7, state.selection().anchorOffset());
    }

    @Test
    public void testCommands_insertLineBreak_withSingleBlockSelection() {
        // "Hello World" with selection [1..4) → delete "ell" → "Ho World"
        // → line break at 1 → ["H", "o World"].
        FormattedText doc = doc("Hello World");
        EditorState state = EditorState.create(doc, Selection.range(0, 1, 0, 4));

        state.apply(Commands.insertLineBreak(state));

        Assertions.assertEquals(1, doc.getBlocks().size());
        List<FormattedLine> lines = doc.getBlocks().get(0).getLines();
        Assertions.assertEquals(2, lines.size());
        Assertions.assertEquals("H", lines.get(0).getText());
        Assertions.assertEquals("o World", lines.get(1).getText());
        Assertions.assertEquals(2, state.selection().anchorOffset());
    }

    @Test
    public void testCommands_insertLineBreak_withMultiBlockSelection() {
        // Block 0: "Hello", Block 1: "World"
        // Selection from (0,3) to (1,2) → delete "lo" + "Wo" → "Hel" + "rld"
        // → merge → "Helrld" → line break at 3 → ["Hel", "rld"] in one block.
        FormattedText doc = doc("Hello", "World");
        EditorState state = EditorState.create(doc, Selection.range(0, 3, 1, 2));

        state.apply(Commands.insertLineBreak(state));

        Assertions.assertEquals(1, doc.getBlocks().size());
        List<FormattedLine> lines = doc.getBlocks().get(0).getLines();
        Assertions.assertEquals(2, lines.size());
        Assertions.assertEquals("Hel", lines.get(0).getText());
        Assertions.assertEquals("rld", lines.get(1).getText());
        Assertions.assertEquals(4, state.selection().anchorOffset());
    }

    @Test
    public void testCommands_insertLineBreak_undoRedo() {
        FormattedText doc = doc("Hello World");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 5));
        History history = new History();

        // Insert line break.
        history.push(state.apply(Commands.insertLineBreak(state)));
        Assertions.assertEquals(1, doc.getBlocks().size());
        Assertions.assertEquals(2, doc.getBlocks().get(0).getLines().size());

        // Undo.
        history.undo(state);
        Assertions.assertEquals(1, doc.getBlocks().size());
        Assertions.assertEquals(1, doc.getBlocks().get(0).getLines().size());
        Assertions.assertEquals("Hello World", textAt(doc, 0));
        Assertions.assertEquals(5, state.selection().anchorOffset());

        // Redo.
        history.redo(state);
        Assertions.assertEquals(2, doc.getBlocks().get(0).getLines().size());
        Assertions.assertEquals(6, state.selection().anchorOffset());
    }

    @Test
    public void testCommands_insertLineBreak_doesNotCreateNewBlock() {
        // Ensure line break stays within the same block (unlike splitBlock).
        FormattedText doc = doc("Hello", "World");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 3));

        state.apply(Commands.insertLineBreak(state));

        // Still 2 blocks; block 0 now has 2 lines.
        Assertions.assertEquals(2, doc.getBlocks().size());
        Assertions.assertEquals(2, doc.getBlocks().get(0).getLines().size());
        Assertions.assertEquals("Hel", doc.getBlocks().get(0).getLines().get(0).getText());
        Assertions.assertEquals("lo", doc.getBlocks().get(0).getLines().get(1).getText());
        Assertions.assertEquals("World", textAt(doc, 1));
    }

    /************************************************************************
     * Commands: deleteChar at line breaks within a block
     ************************************************************************/

    @Test
    public void testCommands_deleteCharBefore_atLineBreak() {
        // Block with ["Hello", "World"], cursor at offset 6 (start of "World").
        // Backspace should join lines → ["HelloWorld"].
        FormattedText doc = new FormattedText();
        doc.block(BlockType.PARA, b -> { b.line("Hello"); b.line("World"); });
        EditorState state = EditorState.create(doc, Selection.cursor(0, 6));

        Transaction tr = Commands.deleteCharBefore(state);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        List<FormattedLine> lines = doc.getBlocks().get(0).getLines();
        Assertions.assertEquals(1, lines.size());
        Assertions.assertEquals("HelloWorld", lines.get(0).getText());
        Assertions.assertEquals(5, state.selection().anchorOffset());
    }

    @Test
    public void testCommands_deleteCharAfter_atLineBreak() {
        // Block with ["Hello", "World"], cursor at offset 5 (end of "Hello").
        // Delete should join lines → ["HelloWorld"].
        FormattedText doc = new FormattedText();
        doc.block(BlockType.PARA, b -> { b.line("Hello"); b.line("World"); });
        EditorState state = EditorState.create(doc, Selection.cursor(0, 5));

        Transaction tr = Commands.deleteCharAfter(state);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        List<FormattedLine> lines = doc.getBlocks().get(0).getLines();
        Assertions.assertEquals(1, lines.size());
        Assertions.assertEquals("HelloWorld", lines.get(0).getText());
        Assertions.assertEquals(5, state.selection().anchorOffset());
    }

    @Test
    public void testCommands_deleteCharBefore_atLineBreak_threeLines() {
        // Block with ["A", "B", "C"], cursor at offset 2 (start of "B").
        // Backspace joins "A" and "B" → ["AB", "C"].
        FormattedText doc = new FormattedText();
        doc.block(BlockType.PARA, b -> { b.line("A"); b.line("B"); b.line("C"); });
        EditorState state = EditorState.create(doc, Selection.cursor(0, 2));

        state.apply(Commands.deleteCharBefore(state));

        List<FormattedLine> lines = doc.getBlocks().get(0).getLines();
        Assertions.assertEquals(2, lines.size());
        Assertions.assertEquals("AB", lines.get(0).getText());
        Assertions.assertEquals("C", lines.get(1).getText());
        Assertions.assertEquals(1, state.selection().anchorOffset());
    }

    @Test
    public void testCommands_deleteCharBefore_atLineBreak_undoRedo() {
        // Undo should restore the line break.
        FormattedText doc = new FormattedText();
        doc.block(BlockType.PARA, b -> { b.line("Hello"); b.line("World"); });
        EditorState state = EditorState.create(doc, Selection.cursor(0, 6));
        History history = new History();

        history.push(state.apply(Commands.deleteCharBefore(state)));
        Assertions.assertEquals(1, doc.getBlocks().get(0).getLines().size());
        Assertions.assertEquals("HelloWorld", doc.getBlocks().get(0).getLines().get(0).getText());

        // Undo.
        history.undo(state);
        Assertions.assertEquals(2, doc.getBlocks().get(0).getLines().size());
        Assertions.assertEquals("Hello", doc.getBlocks().get(0).getLines().get(0).getText());
        Assertions.assertEquals("World", doc.getBlocks().get(0).getLines().get(1).getText());
        Assertions.assertEquals(6, state.selection().anchorOffset());

        // Redo.
        history.redo(state);
        Assertions.assertEquals(1, doc.getBlocks().get(0).getLines().size());
        Assertions.assertEquals("HelloWorld", doc.getBlocks().get(0).getLines().get(0).getText());
    }

    /************************************************************************
     * Commands undo/redo
     ************************************************************************/

    @Test
    public void testCommands_deleteCharBefore_undoRedo() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 3));
        History history = new History();

        // Delete 'l' at offset 2.
        history.push(state.apply(Commands.deleteCharBefore(state)));
        Assertions.assertEquals("Helo", textAt(doc, 0));
        Assertions.assertEquals(2, state.selection().anchorOffset());

        // Undo: cursor back to 3.
        history.undo(state);
        Assertions.assertEquals("Hello", textAt(doc, 0));
        Assertions.assertEquals(3, state.selection().anchorOffset());

        // Redo: cursor back to 2.
        history.redo(state);
        Assertions.assertEquals("Helo", textAt(doc, 0));
        Assertions.assertEquals(2, state.selection().anchorOffset());
    }

    /************************************************************************
     * Commands.moveBlockUp / moveBlockDown (indent-aware)
     ************************************************************************/

    private static FormattedText docWithIndents(String[] texts, int[] indents) {
        FormattedText doc = new FormattedText();
        for (int i = 0; i < texts.length; i++) {
            final int indent = indents[i];
            final String text = texts[i];
            doc.block(BlockType.PARA, b -> { b.line(text); b.indent(indent); });
        }
        return doc;
    }

    @Test
    public void testCountIndentChildren_none() {
        // [A(0), B(0), C(0)] — A has no indent-children.
        FormattedText doc = docWithIndents(
            new String[] { "A", "B", "C" },
            new int[] { 0, 0, 0 }
        );
        Assertions.assertEquals(0, Commands.countIndentChildren(doc.getBlocks(), 0));
    }

    @Test
    public void testCountIndentChildren_some() {
        // [A(0), B(1), C(2), D(1), E(0)] — A has 3 children (B, C, D).
        FormattedText doc = docWithIndents(
            new String[] { "A", "B", "C", "D", "E" },
            new int[] { 0, 1, 2, 1, 0 }
        );
        Assertions.assertEquals(3, Commands.countIndentChildren(doc.getBlocks(), 0));
    }

    @Test
    public void testCountIndentChildren_atEnd() {
        // [A(0), B(1), C(1)] — A has 2 children (B, C).
        FormattedText doc = docWithIndents(
            new String[] { "A", "B", "C" },
            new int[] { 0, 1, 1 }
        );
        Assertions.assertEquals(2, Commands.countIndentChildren(doc.getBlocks(), 0));
    }

    @Test
    public void testMoveBlockUp_simple() {
        // [A(0), B(0)] → move B up → [B, A].
        FormattedText doc = docWithIndents(
            new String[] { "A", "B" },
            new int[] { 0, 0 }
        );
        EditorState state = EditorState.create(doc, Selection.cursor(1, 0));

        Transaction tr = Commands.moveBlockUp(state, 1);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals("B", textAt(doc, 0));
        Assertions.assertEquals("A", textAt(doc, 1));
    }

    @Test
    public void testMoveBlockUp_withChildren() {
        // [A(0), B(1), C(0), D(1)] → move C (with child D) up → [C, D, A, B].
        FormattedText doc = docWithIndents(
            new String[] { "A", "B", "C", "D" },
            new int[] { 0, 1, 0, 1 }
        );
        EditorState state = EditorState.create(doc, Selection.cursor(2, 0));

        Transaction tr = Commands.moveBlockUp(state, 2);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals("C", textAt(doc, 0));
        Assertions.assertEquals("D", textAt(doc, 1));
        Assertions.assertEquals("A", textAt(doc, 2));
        Assertions.assertEquals("B", textAt(doc, 3));
    }

    @Test
    public void testMoveBlockUp_atTop_returnsNull() {
        FormattedText doc = docWithIndents(
            new String[] { "A", "B" },
            new int[] { 0, 0 }
        );
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));
        Assertions.assertNull(Commands.moveBlockUp(state, 0));
    }

    @Test
    public void testMoveBlockDown_simple() {
        // [A(0), B(0)] → move A down → [B, A].
        FormattedText doc = docWithIndents(
            new String[] { "A", "B" },
            new int[] { 0, 0 }
        );
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));

        Transaction tr = Commands.moveBlockDown(state, 0);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals("B", textAt(doc, 0));
        Assertions.assertEquals("A", textAt(doc, 1));
    }

    @Test
    public void testMoveBlockDown_withChildren() {
        // [A(0), B(1), C(0), D(1)] → move A (with child B) down → [C, D, A, B].
        FormattedText doc = docWithIndents(
            new String[] { "A", "B", "C", "D" },
            new int[] { 0, 1, 0, 1 }
        );
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));

        Transaction tr = Commands.moveBlockDown(state, 0);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals("C", textAt(doc, 0));
        Assertions.assertEquals("D", textAt(doc, 1));
        Assertions.assertEquals("A", textAt(doc, 2));
        Assertions.assertEquals("B", textAt(doc, 3));
    }

    @Test
    public void testMoveBlockDown_atBottom_returnsNull() {
        FormattedText doc = docWithIndents(
            new String[] { "A", "B" },
            new int[] { 0, 0 }
        );
        EditorState state = EditorState.create(doc, Selection.cursor(1, 0));
        Assertions.assertNull(Commands.moveBlockDown(state, 1));
    }

    @Test
    public void testMoveBlockDown_withChildrenAtBottom_returnsNull() {
        // [A(0), B(1), C(1)] — A's group spans all blocks, can't move down.
        FormattedText doc = docWithIndents(
            new String[] { "A", "B", "C" },
            new int[] { 0, 1, 1 }
        );
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));
        Assertions.assertNull(Commands.moveBlockDown(state, 0));
    }

    @Test
    public void testMoveBlockUp_undoRedo() {
        // [A(0), B(0)] → move B up → [B, A] → undo → [A, B] → redo → [B, A].
        FormattedText doc = docWithIndents(
            new String[] { "A", "B" },
            new int[] { 0, 0 }
        );
        EditorState state = EditorState.create(doc, Selection.cursor(1, 0));
        History history = new History();

        history.push(state.apply(Commands.moveBlockUp(state, 1)));
        Assertions.assertEquals("B", textAt(doc, 0));
        Assertions.assertEquals("A", textAt(doc, 1));

        history.undo(state);
        Assertions.assertEquals("A", textAt(doc, 0));
        Assertions.assertEquals("B", textAt(doc, 1));

        history.redo(state);
        Assertions.assertEquals("B", textAt(doc, 0));
        Assertions.assertEquals("A", textAt(doc, 1));
    }

    /************************************************************************
     * Multi-block deleteSelection
     ************************************************************************/

    @Test
    public void testDeleteSelection_multiBlock_adjacent() {
        // [Hello, World] select "llo" + "Wor" → delete → "Held"
        FormattedText doc = doc("Hello", "World");
        EditorState state = EditorState.create(doc, Selection.range(0, 2, 1, 3));

        Transaction tr = Commands.deleteSelection(state);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals(1, doc.getBlocks().size());
        Assertions.assertEquals("Held", textAt(doc, 0));
        Assertions.assertEquals(0, state.selection().anchorBlock());
        Assertions.assertEquals(2, state.selection().anchorOffset());
    }

    @Test
    public void testDeleteSelection_multiBlock_withIntermediates() {
        // [AAA, BBB, CCC, DDD] select from A:1 to D:2 → "ADDD".substring(0,1) + "DDD".substring(2) = "AD"
        FormattedText doc = doc("AAA", "BBB", "CCC", "DDD");
        EditorState state = EditorState.create(doc, Selection.range(0, 1, 3, 2));

        Transaction tr = Commands.deleteSelection(state);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals(1, doc.getBlocks().size());
        Assertions.assertEquals("AD", textAt(doc, 0));
    }

    @Test
    public void testDeleteSelection_multiBlock_differentTypes() {
        // [PARA:"Hello", H1:"World"] → no join, two truncated blocks remain.
        FormattedText doc = new FormattedText();
        doc.block(BlockType.PARA, b -> b.line("Hello"));
        doc.block(BlockType.H1, b -> b.line("World"));
        EditorState state = EditorState.create(doc, Selection.range(0, 3, 1, 2));

        Transaction tr = Commands.deleteSelection(state);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals(2, doc.getBlocks().size());
        Assertions.assertEquals("Hel", textAt(doc, 0));
        Assertions.assertEquals(BlockType.PARA, doc.getBlocks().get(0).getType());
        Assertions.assertEquals("rld", textAt(doc, 1));
        Assertions.assertEquals(BlockType.H1, doc.getBlocks().get(1).getType());
    }

    @Test
    public void testDeleteSelection_multiBlock_fromStart() {
        // [Hello, World] select from 0:0 to 1:3 → "ld"
        FormattedText doc = doc("Hello", "World");
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 1, 3));

        Transaction tr = Commands.deleteSelection(state);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals(1, doc.getBlocks().size());
        Assertions.assertEquals("ld", textAt(doc, 0));
        Assertions.assertEquals(0, state.selection().anchorOffset());
    }

    @Test
    public void testDeleteSelection_multiBlock_toEnd() {
        // [Hello, World] select from 0:3 to 1:5 → "Hel"
        FormattedText doc = doc("Hello", "World");
        EditorState state = EditorState.create(doc, Selection.range(0, 3, 1, 5));

        Transaction tr = Commands.deleteSelection(state);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals(1, doc.getBlocks().size());
        Assertions.assertEquals("Hel", textAt(doc, 0));
    }

    @Test
    public void testDeleteSelection_multiBlock_undoRestores() {
        FormattedText doc = doc("Hello", "Middle", "World");
        EditorState state = EditorState.create(doc, Selection.range(0, 2, 2, 3));
        History history = new History();

        history.push(state.apply(Commands.deleteSelection(state)));
        Assertions.assertEquals(1, doc.getBlocks().size());
        Assertions.assertEquals("Held", textAt(doc, 0));

        history.undo(state);
        Assertions.assertEquals(3, doc.getBlocks().size());
        Assertions.assertEquals("Hello", textAt(doc, 0));
        Assertions.assertEquals("Middle", textAt(doc, 1));
        Assertions.assertEquals("World", textAt(doc, 2));
        // Selection restored to the original range anchor.
        Assertions.assertEquals(0, state.selection().anchorBlock());
        Assertions.assertEquals(2, state.selection().anchorOffset());
    }

    /************************************************************************
     * Multi-block insertText
     ************************************************************************/

    @Test
    public void testInsertText_multiBlock() {
        // [Hello, World] select "llo"+"Wor" → type "X" → "HeXld"
        FormattedText doc = doc("Hello", "World");
        EditorState state = EditorState.create(doc, Selection.range(0, 2, 1, 3));

        Transaction tr = Commands.insertText(state, "X");
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals(1, doc.getBlocks().size());
        Assertions.assertEquals("HeXld", textAt(doc, 0));
        Assertions.assertEquals(0, state.selection().anchorBlock());
        Assertions.assertEquals(3, state.selection().anchorOffset());
    }

    @Test
    public void testInsertText_multiBlock_undoRestores() {
        FormattedText doc = doc("Hello", "World");
        EditorState state = EditorState.create(doc, Selection.range(0, 2, 1, 3));
        History history = new History();

        history.push(state.apply(Commands.insertText(state, "X")));
        Assertions.assertEquals("HeXld", textAt(doc, 0));

        history.undo(state);
        Assertions.assertEquals(2, doc.getBlocks().size());
        Assertions.assertEquals("Hello", textAt(doc, 0));
        Assertions.assertEquals("World", textAt(doc, 1));
    }

    /************************************************************************
     * splitBlock with selection
     ************************************************************************/

    @Test
    public void testSplitBlock_singleBlockSelection() {
        // [Hello] select "ll" (offset 2-4) → delete + split → ["He", "o"]
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.range(0, 2, 0, 4));

        Transaction tr = Commands.splitBlock(state);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals(2, doc.getBlocks().size());
        Assertions.assertEquals("He", textAt(doc, 0));
        Assertions.assertEquals("o", textAt(doc, 1));
        Assertions.assertEquals(1, state.selection().anchorBlock());
        Assertions.assertEquals(0, state.selection().anchorOffset());
    }

    @Test
    public void testSplitBlock_multiBlockSelection() {
        // [Hello, World] select "llo"+"Wor" → delete + split → ["He", "ld"]
        FormattedText doc = doc("Hello", "World");
        EditorState state = EditorState.create(doc, Selection.range(0, 2, 1, 3));

        Transaction tr = Commands.splitBlock(state);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals(2, doc.getBlocks().size());
        Assertions.assertEquals("He", textAt(doc, 0));
        Assertions.assertEquals("ld", textAt(doc, 1));
        Assertions.assertEquals(1, state.selection().anchorBlock());
        Assertions.assertEquals(0, state.selection().anchorOffset());
    }

    @Test
    public void testSplitBlock_multiBlockSelection_undoRestores() {
        FormattedText doc = doc("Hello", "World");
        EditorState state = EditorState.create(doc, Selection.range(0, 2, 1, 3));
        History history = new History();

        history.push(state.apply(Commands.splitBlock(state)));
        Assertions.assertEquals(2, doc.getBlocks().size());
        Assertions.assertEquals("He", textAt(doc, 0));
        Assertions.assertEquals("ld", textAt(doc, 1));

        history.undo(state);
        Assertions.assertEquals(2, doc.getBlocks().size());
        Assertions.assertEquals("Hello", textAt(doc, 0));
        Assertions.assertEquals("World", textAt(doc, 1));
    }

    /************************************************************************
     * Block-level commands: setBlockType, indent, outdent
     ************************************************************************/

    @Test
    public void testSetBlockTypeCommand() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 3));

        Transaction tr = Commands.setBlockType(state, BlockType.H1);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals(BlockType.H1, doc.getBlocks().get(0).getType());
        Assertions.assertEquals(3, state.selection().anchorOffset());
    }

    @Test
    public void testSetBlockTypeCommand_sameType_returnsNull() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));
        Assertions.assertNull(Commands.setBlockType(state, BlockType.PARA));
    }

    @Test
    public void testSetBlockTypeCommand_undoRestores() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));
        History history = new History();

        history.push(state.apply(Commands.setBlockType(state, BlockType.H2)));
        Assertions.assertEquals(BlockType.H2, doc.getBlocks().get(0).getType());

        history.undo(state);
        Assertions.assertEquals(BlockType.PARA, doc.getBlocks().get(0).getType());
    }

    @Test
    public void testIndent() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));

        Transaction tr = Commands.indent(state);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals(1, doc.getBlocks().get(0).getIndent());
    }

    @Test
    public void testIndent_atMax_returnsNull() {
        FormattedText doc = new FormattedText();
        doc.block(BlockType.PARA, b -> { b.line("Hello"); b.indent(5); });
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));
        Assertions.assertNull(Commands.indent(state));
    }

    @Test
    public void testOutdent() {
        FormattedText doc = new FormattedText();
        doc.block(BlockType.PARA, b -> { b.line("Hello"); b.indent(3); });
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));

        Transaction tr = Commands.outdent(state);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals(2, doc.getBlocks().get(0).getIndent());
    }

    @Test
    public void testOutdent_atMin_returnsNull() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));
        Assertions.assertNull(Commands.outdent(state));
    }

    @Test
    public void testIndent_undoRestores() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));
        History history = new History();

        history.push(state.apply(Commands.indent(state)));
        Assertions.assertEquals(1, doc.getBlocks().get(0).getIndent());

        history.undo(state);
        Assertions.assertEquals(0, doc.getBlocks().get(0).getIndent());
    }

    /************************************************************************
     * ChangeFormatStep
     ************************************************************************/

    @Test
    public void testChangeFormatStep_add() {
        // "Hello" with no formatting → add BLD to [1,4) → "ell" is bold.
        FormattedText doc = doc("Hello");
        Transaction tr = Transaction.create().step(new ChangeFormatStep(0, 1, 3, FormatType.BLD, true));
        Transaction inverse = tr.apply(doc).inverse();

        java.util.List<FormattedLine.Format> fmts = doc.getBlocks().get(0).getLines().get(0).getFormatting();
        Assertions.assertEquals(1, fmts.size());
        Assertions.assertEquals(1, fmts.get(0).getIndex());
        Assertions.assertEquals(3, fmts.get(0).getLength());
        Assertions.assertTrue(fmts.get(0).getFormats().contains(FormatType.BLD));

        // Undo restores no formatting.
        inverse.apply(doc);
        Assertions.assertTrue(doc.getBlocks().get(0).getLines().get(0).getFormatting().isEmpty());
    }

    @Test
    public void testChangeFormatStep_remove() {
        // "Hello" with BLD on [0,5) → remove BLD from [1,3) → BLD remains on [0,1) and [4,1).
        FormattedText doc = new FormattedText();
        doc.block(BlockType.PARA, b -> {
            FormattedLine line = new FormattedLine();
            line.setText("Hello");
            line.getFormatting().add(new FormattedLine.Format(0, 5, FormatType.BLD));
            b.getLines().add(line);
        });

        Transaction tr = Transaction.create().step(new ChangeFormatStep(0, 1, 3, FormatType.BLD, false));
        Transaction inverse = tr.apply(doc).inverse();

        java.util.List<FormattedLine.Format> fmts = doc.getBlocks().get(0).getLines().get(0).getFormatting();
        Assertions.assertEquals(2, fmts.size());
        // [0,1) BLD
        Assertions.assertEquals(0, fmts.get(0).getIndex());
        Assertions.assertEquals(1, fmts.get(0).getLength());
        // [4,1) BLD
        Assertions.assertEquals(4, fmts.get(1).getIndex());
        Assertions.assertEquals(1, fmts.get(1).getLength());

        // Undo restores original single [0,5) BLD.
        inverse.apply(doc);
        fmts = doc.getBlocks().get(0).getLines().get(0).getFormatting();
        Assertions.assertEquals(1, fmts.size());
        Assertions.assertEquals(0, fmts.get(0).getIndex());
        Assertions.assertEquals(5, fmts.get(0).getLength());
    }

    /************************************************************************
     * FormattedLine format methods (tested via block)
     ************************************************************************/

    @Test
    public void testAddFormat_unformattedRange() {
        FormattedText doc = doc("Hello");
        doc.getBlocks().get(0).addFormat(0, 5, FormatType.BLD);

        java.util.List<FormattedLine.Format> fmts = doc.getBlocks().get(0).getLines().get(0).getFormatting();
        Assertions.assertEquals(1, fmts.size());
        Assertions.assertEquals(0, fmts.get(0).getIndex());
        Assertions.assertEquals(5, fmts.get(0).getLength());
        Assertions.assertTrue(fmts.get(0).getFormats().contains(FormatType.BLD));
    }

    @Test
    public void testAddFormat_alreadyFormatted_addsType() {
        // "Hello" with BLD on [0,5) → add ITL on [0,5) → both types present.
        FormattedText doc = doc("Hello");
        doc.getBlocks().get(0).addFormat(0, 5, FormatType.BLD);
        doc.getBlocks().get(0).addFormat(0, 5, FormatType.ITL);

        java.util.List<FormattedLine.Format> fmts = doc.getBlocks().get(0).getLines().get(0).getFormatting();
        Assertions.assertEquals(1, fmts.size());
        Assertions.assertTrue(fmts.get(0).getFormats().contains(FormatType.BLD));
        Assertions.assertTrue(fmts.get(0).getFormats().contains(FormatType.ITL));
    }

    @Test
    public void testAddFormat_idempotent() {
        // Adding BLD twice should not create duplicates.
        FormattedText doc = doc("Hello");
        doc.getBlocks().get(0).addFormat(0, 5, FormatType.BLD);
        doc.getBlocks().get(0).addFormat(0, 5, FormatType.BLD);

        java.util.List<FormattedLine.Format> fmts = doc.getBlocks().get(0).getLines().get(0).getFormatting();
        Assertions.assertEquals(1, fmts.size());
        Assertions.assertEquals(1, fmts.get(0).getFormats().size());
    }

    @Test
    public void testAddFormat_partialOverlap() {
        // "Hello" with BLD on [0,3) → add ITL on [2,3) → splits into:
        // [0,2) BLD, [2,1) BLD+ITL, [3,2) ITL
        FormattedText doc = doc("Hello");
        doc.getBlocks().get(0).addFormat(0, 3, FormatType.BLD);
        doc.getBlocks().get(0).addFormat(2, 3, FormatType.ITL);

        java.util.List<FormattedLine.Format> fmts = doc.getBlocks().get(0).getLines().get(0).getFormatting();
        Assertions.assertEquals(3, fmts.size());
        // [0,2) BLD only
        Assertions.assertEquals(0, fmts.get(0).getIndex());
        Assertions.assertEquals(2, fmts.get(0).getLength());
        Assertions.assertEquals(1, fmts.get(0).getFormats().size());
        Assertions.assertTrue(fmts.get(0).getFormats().contains(FormatType.BLD));
        // [2,1) BLD + ITL
        Assertions.assertEquals(2, fmts.get(1).getIndex());
        Assertions.assertEquals(1, fmts.get(1).getLength());
        Assertions.assertTrue(fmts.get(1).getFormats().contains(FormatType.BLD));
        Assertions.assertTrue(fmts.get(1).getFormats().contains(FormatType.ITL));
        // [3,2) ITL only
        Assertions.assertEquals(3, fmts.get(2).getIndex());
        Assertions.assertEquals(2, fmts.get(2).getLength());
        Assertions.assertEquals(1, fmts.get(2).getFormats().size());
        Assertions.assertTrue(fmts.get(2).getFormats().contains(FormatType.ITL));
    }

    @Test
    public void testAddFormat_spansMultipleFormats() {
        // "Hello" with BLD on [0,2) and ITL on [3,2) → add UL on [0,5)
        // Expect: [0,2) BLD+UL, [2,1) UL, [3,2) ITL+UL
        FormattedText doc = doc("Hello");
        FormattedLine line = doc.getBlocks().get(0).getLines().get(0);
        line.getFormatting().add(new FormattedLine.Format(0, 2, FormatType.BLD));
        line.getFormatting().add(new FormattedLine.Format(3, 2, FormatType.ITL));

        doc.getBlocks().get(0).addFormat(0, 5, FormatType.UL);

        java.util.List<FormattedLine.Format> fmts = line.getFormatting();
        Assertions.assertEquals(3, fmts.size());
        // [0,2) BLD+UL
        Assertions.assertEquals(0, fmts.get(0).getIndex());
        Assertions.assertEquals(2, fmts.get(0).getLength());
        Assertions.assertTrue(fmts.get(0).getFormats().contains(FormatType.BLD));
        Assertions.assertTrue(fmts.get(0).getFormats().contains(FormatType.UL));
        // [2,1) UL only (gap fill)
        Assertions.assertEquals(2, fmts.get(1).getIndex());
        Assertions.assertEquals(1, fmts.get(1).getLength());
        Assertions.assertEquals(1, fmts.get(1).getFormats().size());
        Assertions.assertTrue(fmts.get(1).getFormats().contains(FormatType.UL));
        // [3,2) ITL+UL
        Assertions.assertEquals(3, fmts.get(2).getIndex());
        Assertions.assertEquals(2, fmts.get(2).getLength());
        Assertions.assertTrue(fmts.get(2).getFormats().contains(FormatType.ITL));
        Assertions.assertTrue(fmts.get(2).getFormats().contains(FormatType.UL));
    }

    @Test
    public void testRemoveFormat_fullRange() {
        // "Hello" with BLD on [0,5) → remove BLD → no formatting.
        FormattedText doc = doc("Hello");
        doc.getBlocks().get(0).addFormat(0, 5, FormatType.BLD);
        doc.getBlocks().get(0).removeFormat(0, 5, FormatType.BLD);

        Assertions.assertTrue(doc.getBlocks().get(0).getLines().get(0).getFormatting().isEmpty());
    }

    @Test
    public void testRemoveFormat_partialRange() {
        // "Hello" with BLD on [0,5) → remove BLD from [2,2) → BLD on [0,2) and [4,1).
        FormattedText doc = doc("Hello");
        doc.getBlocks().get(0).addFormat(0, 5, FormatType.BLD);
        doc.getBlocks().get(0).removeFormat(2, 2, FormatType.BLD);

        java.util.List<FormattedLine.Format> fmts = doc.getBlocks().get(0).getLines().get(0).getFormatting();
        Assertions.assertEquals(2, fmts.size());
        Assertions.assertEquals(0, fmts.get(0).getIndex());
        Assertions.assertEquals(2, fmts.get(0).getLength());
        Assertions.assertEquals(4, fmts.get(1).getIndex());
        Assertions.assertEquals(1, fmts.get(1).getLength());
    }

    @Test
    public void testRemoveFormat_preservesOtherTypes() {
        // "Hello" with BLD+ITL on [0,5) → remove BLD → ITL remains.
        FormattedText doc = doc("Hello");
        doc.getBlocks().get(0).addFormat(0, 5, FormatType.BLD);
        doc.getBlocks().get(0).addFormat(0, 5, FormatType.ITL);
        doc.getBlocks().get(0).removeFormat(0, 5, FormatType.BLD);

        java.util.List<FormattedLine.Format> fmts = doc.getBlocks().get(0).getLines().get(0).getFormatting();
        Assertions.assertEquals(1, fmts.size());
        Assertions.assertFalse(fmts.get(0).getFormats().contains(FormatType.BLD));
        Assertions.assertTrue(fmts.get(0).getFormats().contains(FormatType.ITL));
    }

    @Test
    public void testRemoveFormat_preservesMetadata() {
        // Format with BLD + link meta → remove BLD → format entry kept (meta present).
        FormattedText doc = doc("Hello");
        FormattedLine line = doc.getBlocks().get(0).getLines().get(0);
        FormattedLine.Format fmt = new FormattedLine.Format(0, 5, FormatType.BLD);
        fmt.getMeta().put("link", "http://example.com");
        line.getFormatting().add(fmt);

        doc.getBlocks().get(0).removeFormat(0, 5, FormatType.BLD);

        java.util.List<FormattedLine.Format> fmts = line.getFormatting();
        Assertions.assertEquals(1, fmts.size());
        Assertions.assertEquals(0, fmts.get(0).getFormats().size());
        Assertions.assertEquals("http://example.com", fmts.get(0).getMeta().get("link"));
    }

    @Test
    public void testHasFormat_fullCoverage() {
        FormattedText doc = doc("Hello");
        doc.getBlocks().get(0).addFormat(0, 5, FormatType.BLD);
        Assertions.assertTrue(doc.getBlocks().get(0).hasFormat(0, 5, FormatType.BLD));
    }

    @Test
    public void testHasFormat_partialCoverage() {
        FormattedText doc = doc("Hello");
        doc.getBlocks().get(0).addFormat(0, 3, FormatType.BLD);
        Assertions.assertFalse(doc.getBlocks().get(0).hasFormat(0, 5, FormatType.BLD));
    }

    @Test
    public void testHasFormat_noCoverage() {
        FormattedText doc = doc("Hello");
        Assertions.assertFalse(doc.getBlocks().get(0).hasFormat(0, 5, FormatType.BLD));
    }

    @Test
    public void testHasFormat_wrongType() {
        FormattedText doc = doc("Hello");
        doc.getBlocks().get(0).addFormat(0, 5, FormatType.BLD);
        Assertions.assertFalse(doc.getBlocks().get(0).hasFormat(0, 5, FormatType.ITL));
    }

    /************************************************************************
     * Commands: applyFormat, removeFormat, toggleFormat
     ************************************************************************/

    @Test
    public void testApplyFormat_cursorSelection_returnsNull() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));
        Assertions.assertNull(Commands.applyFormat(state, FormatType.BLD));
    }

    @Test
    public void testRemoveFormatCommand_cursorSelection_returnsNull() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));
        Assertions.assertNull(Commands.removeFormat(state, FormatType.BLD));
    }

    @Test
    public void testApplyFormat_rangeSelection() {
        // Select [1,4) in "Hello" → apply BLD → "ell" is bold.
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.range(0, 1, 0, 4));

        Transaction tr = Commands.applyFormat(state, FormatType.BLD);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        java.util.List<FormattedLine.Format> fmts = doc.getBlocks().get(0).getLines().get(0).getFormatting();
        Assertions.assertEquals(1, fmts.size());
        Assertions.assertEquals(1, fmts.get(0).getIndex());
        Assertions.assertEquals(3, fmts.get(0).getLength());
        Assertions.assertTrue(fmts.get(0).getFormats().contains(FormatType.BLD));
    }

    @Test
    public void testApplyFormat_selectionPreserved() {
        FormattedText doc = doc("Hello");
        Selection sel = Selection.range(0, 1, 0, 4);
        EditorState state = EditorState.create(doc, sel);

        state.apply(Commands.applyFormat(state, FormatType.BLD));

        // Selection should be preserved after formatting.
        Assertions.assertEquals(0, state.selection().fromBlock());
        Assertions.assertEquals(1, state.selection().fromOffset());
        Assertions.assertEquals(0, state.selection().toBlock());
        Assertions.assertEquals(4, state.selection().toOffset());
    }

    @Test
    public void testRemoveFormatCommand_rangeSelection() {
        FormattedText doc = doc("Hello");
        doc.getBlocks().get(0).addFormat(0, 5, FormatType.BLD);
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 0, 5));

        Transaction tr = Commands.removeFormat(state, FormatType.BLD);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertTrue(doc.getBlocks().get(0).getLines().get(0).getFormatting().isEmpty());
    }

    @Test
    public void testToggleFormat_addsWhenAbsent() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 0, 5));

        Transaction tr = Commands.toggleFormat(state, FormatType.BLD);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertTrue(doc.getBlocks().get(0).hasFormat(0, 5, FormatType.BLD));
    }

    @Test
    public void testToggleFormat_removesWhenPresent() {
        FormattedText doc = doc("Hello");
        doc.getBlocks().get(0).addFormat(0, 5, FormatType.BLD);
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 0, 5));

        Transaction tr = Commands.toggleFormat(state, FormatType.BLD);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertFalse(doc.getBlocks().get(0).hasFormat(0, 5, FormatType.BLD));
    }

    @Test
    public void testToggleFormat_cursorSelection_returnsNull() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));
        Assertions.assertNull(Commands.toggleFormat(state, FormatType.BLD));
    }

    @Test
    public void testToggleFormat_multiBlock() {
        // Two blocks, neither bold → toggle adds to both.
        FormattedText doc = doc("Hello", "World");
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 1, 5));

        Transaction tr = Commands.toggleFormat(state, FormatType.BLD);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertTrue(doc.getBlocks().get(0).hasFormat(0, 5, FormatType.BLD));
        Assertions.assertTrue(doc.getBlocks().get(1).hasFormat(0, 5, FormatType.BLD));
    }

    @Test
    public void testToggleFormat_multiBlock_removesWhenAllPresent() {
        FormattedText doc = doc("Hello", "World");
        doc.getBlocks().get(0).addFormat(0, 5, FormatType.BLD);
        doc.getBlocks().get(1).addFormat(0, 5, FormatType.BLD);
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 1, 5));

        Transaction tr = Commands.toggleFormat(state, FormatType.BLD);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertFalse(doc.getBlocks().get(0).hasFormat(0, 5, FormatType.BLD));
        Assertions.assertFalse(doc.getBlocks().get(1).hasFormat(0, 5, FormatType.BLD));
    }

    @Test
    public void testToggleFormat_multiBlock_addsWhenPartiallyPresent() {
        // First block bold, second not → toggle should add to both.
        FormattedText doc = doc("Hello", "World");
        doc.getBlocks().get(0).addFormat(0, 5, FormatType.BLD);
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 1, 5));

        Transaction tr = Commands.toggleFormat(state, FormatType.BLD);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertTrue(doc.getBlocks().get(0).hasFormat(0, 5, FormatType.BLD));
        Assertions.assertTrue(doc.getBlocks().get(1).hasFormat(0, 5, FormatType.BLD));
    }

    @Test
    public void testApplyFormat_undoRestores() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 0, 5));
        History history = new History();

        history.push(state.apply(Commands.applyFormat(state, FormatType.BLD)));
        Assertions.assertTrue(doc.getBlocks().get(0).hasFormat(0, 5, FormatType.BLD));

        history.undo(state);
        Assertions.assertTrue(doc.getBlocks().get(0).getLines().get(0).getFormatting().isEmpty());
    }

    @Test
    public void testToggleFormat_undoRestores() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 0, 5));
        History history = new History();

        // Toggle on.
        history.push(state.apply(Commands.toggleFormat(state, FormatType.BLD)));
        Assertions.assertTrue(doc.getBlocks().get(0).hasFormat(0, 5, FormatType.BLD));

        // Undo → back to no formatting.
        history.undo(state);
        Assertions.assertTrue(doc.getBlocks().get(0).getLines().get(0).getFormatting().isEmpty());

        // Redo → bold again.
        history.redo(state);
        Assertions.assertTrue(doc.getBlocks().get(0).hasFormat(0, 5, FormatType.BLD));
    }

    @Test
    public void testApplyFormat_multiBlock() {
        // Apply bold across two blocks.
        FormattedText doc = doc("Hello", "World");
        EditorState state = EditorState.create(doc, Selection.range(0, 2, 1, 3));

        Transaction tr = Commands.applyFormat(state, FormatType.BLD);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        // Block 0: bold from offset 2 to end (length 3).
        Assertions.assertTrue(doc.getBlocks().get(0).hasFormat(2, 3, FormatType.BLD));
        // Block 1: bold from offset 0 to 3.
        Assertions.assertTrue(doc.getBlocks().get(1).hasFormat(0, 3, FormatType.BLD));
    }

    @Test
    public void testRemoveFormatCommand_multiBlock() {
        // Remove bold across two blocks.
        FormattedText doc = doc("Hello", "World");
        doc.getBlocks().get(0).addFormat(0, 5, FormatType.BLD);
        doc.getBlocks().get(1).addFormat(0, 5, FormatType.BLD);
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 1, 5));

        Transaction tr = Commands.removeFormat(state, FormatType.BLD);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertTrue(doc.getBlocks().get(0).getLines().get(0).getFormatting().isEmpty());
        Assertions.assertTrue(doc.getBlocks().get(1).getLines().get(0).getFormatting().isEmpty());
    }

    @Test
    public void testAddFormat_preservesMetadata() {
        // Format with link meta → add BLD → meta preserved on overlap portion.
        FormattedText doc = doc("Hello");
        FormattedLine line = doc.getBlocks().get(0).getLines().get(0);
        FormattedLine.Format fmt = new FormattedLine.Format(0, 5, FormatType.A);
        fmt.getMeta().put("link", "http://example.com");
        line.getFormatting().add(fmt);

        doc.getBlocks().get(0).addFormat(0, 5, FormatType.BLD);

        java.util.List<FormattedLine.Format> fmts = line.getFormatting();
        Assertions.assertEquals(1, fmts.size());
        Assertions.assertTrue(fmts.get(0).getFormats().contains(FormatType.A));
        Assertions.assertTrue(fmts.get(0).getFormats().contains(FormatType.BLD));
        Assertions.assertEquals("http://example.com", fmts.get(0).getMeta().get("link"));
    }

    /************************************************************************
     * Commands: insertBlockAfter, insertBlockBefore
     ************************************************************************/

    @Test
    public void testInsertBlockAfter() {
        FormattedText doc = doc("A", "B", "C");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));

        Transaction tr = Commands.insertBlockAfter(state, 1, BlockType.PARA);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals(4, doc.getBlocks().size());
        Assertions.assertEquals("A", textAt(doc, 0));
        Assertions.assertEquals("B", textAt(doc, 1));
        Assertions.assertEquals("", textAt(doc, 2));
        Assertions.assertEquals("C", textAt(doc, 3));
        Assertions.assertEquals(2, state.selection().anchorBlock());
        Assertions.assertEquals(0, state.selection().anchorOffset());
    }

    @Test
    public void testInsertBlockAfter_atEnd() {
        FormattedText doc = doc("A", "B");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));

        state.apply(Commands.insertBlockAfter(state, 1, BlockType.H1));

        Assertions.assertEquals(3, doc.getBlocks().size());
        Assertions.assertEquals(BlockType.H1, doc.getBlocks().get(2).getType());
    }

    @Test
    public void testInsertBlockAfter_outOfBounds_returnsNull() {
        FormattedText doc = doc("A");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));
        Assertions.assertNull(Commands.insertBlockAfter(state, 5, BlockType.PARA));
        Assertions.assertNull(Commands.insertBlockAfter(state, -1, BlockType.PARA));
    }

    @Test
    public void testInsertBlockAfter_nullType_returnsNull() {
        FormattedText doc = doc("A");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));
        Assertions.assertNull(Commands.insertBlockAfter(state, 0, null));
    }

    @Test
    public void testInsertBlockAfter_undoRestores() {
        FormattedText doc = doc("A", "B");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 1));
        History history = new History();

        history.push(state.apply(Commands.insertBlockAfter(state, 0, BlockType.PARA)));
        Assertions.assertEquals(3, doc.getBlocks().size());

        history.undo(state);
        Assertions.assertEquals(2, doc.getBlocks().size());
        Assertions.assertEquals("A", textAt(doc, 0));
        Assertions.assertEquals("B", textAt(doc, 1));
    }

    @Test
    public void testInsertBlockBefore() {
        FormattedText doc = doc("A", "B", "C");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));

        Transaction tr = Commands.insertBlockBefore(state, 1, BlockType.PARA);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals(4, doc.getBlocks().size());
        Assertions.assertEquals("A", textAt(doc, 0));
        Assertions.assertEquals("", textAt(doc, 1));
        Assertions.assertEquals("B", textAt(doc, 2));
        Assertions.assertEquals("C", textAt(doc, 3));
        Assertions.assertEquals(1, state.selection().anchorBlock());
    }

    @Test
    public void testInsertBlockBefore_atStart() {
        FormattedText doc = doc("A");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));

        state.apply(Commands.insertBlockBefore(state, 0, BlockType.H1));

        Assertions.assertEquals(2, doc.getBlocks().size());
        Assertions.assertEquals(BlockType.H1, doc.getBlocks().get(0).getType());
        Assertions.assertEquals("A", textAt(doc, 1));
    }

    @Test
    public void testInsertBlockBefore_outOfBounds_returnsNull() {
        FormattedText doc = doc("A");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));
        Assertions.assertNull(Commands.insertBlockBefore(state, 5, BlockType.PARA));
    }

    /************************************************************************
     * Commands: deleteBlock, replaceBlock
     ************************************************************************/

    @Test
    public void testDeleteBlockCommand() {
        FormattedText doc = doc("A", "B", "C");
        EditorState state = EditorState.create(doc, Selection.cursor(1, 0));

        Transaction tr = Commands.deleteBlock(state, 1);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals(2, doc.getBlocks().size());
        Assertions.assertEquals("A", textAt(doc, 0));
        Assertions.assertEquals("C", textAt(doc, 1));
        // Cursor moves to previous block.
        Assertions.assertEquals(0, state.selection().anchorBlock());
    }

    @Test
    public void testDeleteBlockCommand_firstBlock() {
        FormattedText doc = doc("A", "B");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));

        state.apply(Commands.deleteBlock(state, 0));

        Assertions.assertEquals(1, doc.getBlocks().size());
        Assertions.assertEquals("B", textAt(doc, 0));
        // Cursor stays at block 0.
        Assertions.assertEquals(0, state.selection().anchorBlock());
    }

    @Test
    public void testDeleteBlockCommand_lastRemaining_returnsNull() {
        FormattedText doc = doc("A");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));
        Assertions.assertNull(Commands.deleteBlock(state, 0));
    }

    @Test
    public void testDeleteBlockCommand_outOfBounds_returnsNull() {
        FormattedText doc = doc("A", "B");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));
        Assertions.assertNull(Commands.deleteBlock(state, 5));
        Assertions.assertNull(Commands.deleteBlock(state, -1));
    }

    @Test
    public void testDeleteBlockCommand_undoRestores() {
        FormattedText doc = doc("A", "B", "C");
        EditorState state = EditorState.create(doc, Selection.cursor(1, 1));
        History history = new History();

        history.push(state.apply(Commands.deleteBlock(state, 1)));
        Assertions.assertEquals(2, doc.getBlocks().size());

        history.undo(state);
        Assertions.assertEquals(3, doc.getBlocks().size());
        Assertions.assertEquals("B", textAt(doc, 1));
    }

    @Test
    public void testReplaceBlockCommand() {
        FormattedText doc = doc("A", "B");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));

        FormattedBlock replacement = para("NewContent");
        Transaction tr = Commands.replaceBlock(state, 0, replacement);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals("NewContent", textAt(doc, 0));
        Assertions.assertEquals("B", textAt(doc, 1));
        Assertions.assertEquals(0, state.selection().anchorBlock());
        Assertions.assertEquals(0, state.selection().anchorOffset());
    }

    @Test
    public void testReplaceBlockCommand_null_returnsNull() {
        FormattedText doc = doc("A");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));
        Assertions.assertNull(Commands.replaceBlock(state, 0, null));
    }

    @Test
    public void testReplaceBlockCommand_outOfBounds_returnsNull() {
        FormattedText doc = doc("A");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));
        Assertions.assertNull(Commands.replaceBlock(state, 5, para("X")));
    }

    @Test
    public void testReplaceBlockCommand_undoRestores() {
        FormattedText doc = doc("Original");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 3));
        History history = new History();

        history.push(state.apply(Commands.replaceBlock(state, 0, para("Replaced"))));
        Assertions.assertEquals("Replaced", textAt(doc, 0));

        history.undo(state);
        Assertions.assertEquals("Original", textAt(doc, 0));
    }

    /************************************************************************
     * Commands: applyLink, removeLink, updateLink
     ************************************************************************/

    @Test
    public void testApplyLink() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 0, 5));

        Transaction tr = Commands.applyLink(state, "http://example.com");
        Assertions.assertNotNull(tr);
        state.apply(tr);

        java.util.List<FormattedLine.Format> fmts = doc.getBlocks().get(0).getLines().get(0).getFormatting();
        Assertions.assertEquals(1, fmts.size());
        Assertions.assertTrue(fmts.get(0).getFormats().contains(FormatType.A));
        Assertions.assertEquals("http://example.com", fmts.get(0).getMeta().get(FormattedLine.META_LINK));
    }

    @Test
    public void testApplyLink_cursorSelection_returnsNull() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));
        Assertions.assertNull(Commands.applyLink(state, "http://example.com"));
    }

    @Test
    public void testApplyLink_emptyUrl_returnsNull() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 0, 5));
        Assertions.assertNull(Commands.applyLink(state, ""));
        Assertions.assertNull(Commands.applyLink(state, null));
    }

    @Test
    public void testApplyLink_multiBlock() {
        FormattedText doc = doc("Hello", "World");
        EditorState state = EditorState.create(doc, Selection.range(0, 2, 1, 3));

        Transaction tr = Commands.applyLink(state, "http://example.com");
        Assertions.assertNotNull(tr);
        state.apply(tr);

        // Block 0: link from offset 2 to end (length 3).
        Assertions.assertTrue(doc.getBlocks().get(0).hasFormat(2, 3, FormatType.A));
        FormattedLine.Format fmt0 = doc.getBlocks().get(0).getLines().get(0).getFormatting().get(0);
        Assertions.assertEquals("http://example.com", fmt0.getMeta().get(FormattedLine.META_LINK));

        // Block 1: link from offset 0 to 3.
        Assertions.assertTrue(doc.getBlocks().get(1).hasFormat(0, 3, FormatType.A));
        FormattedLine.Format fmt1 = doc.getBlocks().get(1).getLines().get(0).getFormatting().get(0);
        Assertions.assertEquals("http://example.com", fmt1.getMeta().get(FormattedLine.META_LINK));
    }

    @Test
    public void testApplyLink_undoRestores() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 0, 5));
        History history = new History();

        history.push(state.apply(Commands.applyLink(state, "http://example.com")));
        Assertions.assertTrue(doc.getBlocks().get(0).getLines().get(0).getFormatting().get(0).getFormats().contains(FormatType.A));

        history.undo(state);
        Assertions.assertTrue(doc.getBlocks().get(0).getLines().get(0).getFormatting().isEmpty());
    }

    @Test
    public void testRemoveLink() {
        // Set up a link first.
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 0, 5));
        state.apply(Commands.applyLink(state, "http://example.com"));

        // Now remove it.
        Transaction tr = Commands.removeLink(state);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        java.util.List<FormattedLine.Format> fmts = doc.getBlocks().get(0).getLines().get(0).getFormatting();
        // No format entries should remain (A was the only type, no other meta).
        Assertions.assertTrue(fmts.isEmpty());
    }

    @Test
    public void testRemoveLink_undoRestores() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 0, 5));
        state.apply(Commands.applyLink(state, "http://example.com"));
        History history = new History();

        history.push(state.apply(Commands.removeLink(state)));
        Assertions.assertTrue(doc.getBlocks().get(0).getLines().get(0).getFormatting().isEmpty());

        history.undo(state);
        java.util.List<FormattedLine.Format> fmts = doc.getBlocks().get(0).getLines().get(0).getFormatting();
        Assertions.assertEquals(1, fmts.size());
        Assertions.assertTrue(fmts.get(0).getFormats().contains(FormatType.A));
        Assertions.assertEquals("http://example.com", fmts.get(0).getMeta().get(FormattedLine.META_LINK));
    }

    @Test
    public void testUpdateLink_changesUrl() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 0, 5));
        state.apply(Commands.applyLink(state, "http://old.com"));

        Transaction tr = Commands.updateLink(state, "http://new.com");
        Assertions.assertNotNull(tr);
        state.apply(tr);

        java.util.List<FormattedLine.Format> fmts = doc.getBlocks().get(0).getLines().get(0).getFormatting();
        Assertions.assertEquals(1, fmts.size());
        Assertions.assertTrue(fmts.get(0).getFormats().contains(FormatType.A));
        Assertions.assertEquals("http://new.com", fmts.get(0).getMeta().get(FormattedLine.META_LINK));
    }

    @Test
    public void testUpdateLink_noExistingLink_addsLink() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 0, 5));

        state.apply(Commands.updateLink(state, "http://new.com"));

        java.util.List<FormattedLine.Format> fmts = doc.getBlocks().get(0).getLines().get(0).getFormatting();
        Assertions.assertEquals(1, fmts.size());
        Assertions.assertTrue(fmts.get(0).getFormats().contains(FormatType.A));
        Assertions.assertEquals("http://new.com", fmts.get(0).getMeta().get(FormattedLine.META_LINK));
    }

    @Test
    public void testUpdateLink_undoRestores() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 0, 5));
        state.apply(Commands.applyLink(state, "http://old.com"));
        History history = new History();

        history.push(state.apply(Commands.updateLink(state, "http://new.com")));
        Assertions.assertEquals("http://new.com", doc.getBlocks().get(0).getLines().get(0).getFormatting().get(0).getMeta().get(FormattedLine.META_LINK));

        history.undo(state);
        Assertions.assertEquals("http://old.com", doc.getBlocks().get(0).getLines().get(0).getFormatting().get(0).getMeta().get(FormattedLine.META_LINK));
    }

    /************************************************************************
     * SetBlockMetaStep and setBlockMeta command
     ************************************************************************/

    @Test
    public void testSetBlockMetaStep() {
        FormattedText doc = doc("Hello");
        Transaction tr = Transaction.create().step(new SetBlockMetaStep(0, "caption", "My caption"));
        Transaction inverse = tr.apply(doc).inverse();

        Assertions.assertEquals("My caption", doc.getBlocks().get(0).getMeta().get("caption"));

        // Undo removes the key.
        inverse.apply(doc);
        Assertions.assertNull(doc.getBlocks().get(0).getMeta().get("caption"));
    }

    @Test
    public void testSetBlockMetaStep_overwrite() {
        FormattedText doc = doc("Hello");
        doc.getBlocks().get(0).getMeta().put("key", "old");

        Transaction tr = Transaction.create().step(new SetBlockMetaStep(0, "key", "new"));
        Transaction inverse = tr.apply(doc).inverse();

        Assertions.assertEquals("new", doc.getBlocks().get(0).getMeta().get("key"));

        inverse.apply(doc);
        Assertions.assertEquals("old", doc.getBlocks().get(0).getMeta().get("key"));
    }

    @Test
    public void testSetBlockMeta_command() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 2));

        Transaction tr = Commands.setBlockMeta(state, "lang", "java");
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals("java", doc.getBlocks().get(0).getMeta().get("lang"));
        // Selection preserved.
        Assertions.assertEquals(0, state.selection().anchorBlock());
        Assertions.assertEquals(2, state.selection().anchorOffset());
    }

    @Test
    public void testSetBlockMeta_removeKey() {
        FormattedText doc = doc("Hello");
        doc.getBlocks().get(0).getMeta().put("lang", "java");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));

        Transaction tr = Commands.setBlockMeta(state, "lang", null);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertNull(doc.getBlocks().get(0).getMeta().get("lang"));
    }

    @Test
    public void testSetBlockMeta_sameValue_returnsNull() {
        FormattedText doc = doc("Hello");
        doc.getBlocks().get(0).getMeta().put("lang", "java");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));
        Assertions.assertNull(Commands.setBlockMeta(state, "lang", "java"));
    }

    @Test
    public void testSetBlockMeta_nullKey_returnsNull() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));
        Assertions.assertNull(Commands.setBlockMeta(state, null, "value"));
        Assertions.assertNull(Commands.setBlockMeta(state, "", "value"));
    }

    @Test
    public void testSetBlockMeta_undoRestores() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));
        History history = new History();

        history.push(state.apply(Commands.setBlockMeta(state, "lang", "java")));
        Assertions.assertEquals("java", doc.getBlocks().get(0).getMeta().get("lang"));

        history.undo(state);
        Assertions.assertNull(doc.getBlocks().get(0).getMeta().get("lang"));
    }

    /************************************************************************
     * Word boundary helpers
     ************************************************************************/

    @Test
    public void testCharAt_singleLine() {
        FormattedText doc = doc("Hello");
        FormattedBlock block = doc.getBlocks().get(0);
        Assertions.assertEquals('H', Commands.charAt(block, 0));
        Assertions.assertEquals('o', Commands.charAt(block, 4));
    }

    @Test
    public void testCharAt_multiLine() {
        // "Hello\nWorld" → H=0 e=1 l=2 l=3 o=4 \n=5 W=6 o=7 r=8 l=9 d=10
        FormattedText doc = new FormattedText();
        doc.block(BlockType.PARA, b -> { b.line("Hello"); b.line("World"); });
        FormattedBlock block = doc.getBlocks().get(0);
        Assertions.assertEquals('o', Commands.charAt(block, 4));
        Assertions.assertEquals('\n', Commands.charAt(block, 5));
        Assertions.assertEquals('W', Commands.charAt(block, 6));
        Assertions.assertEquals('d', Commands.charAt(block, 10));
    }

    @Test
    public void testFindWordStart_atWordEnd() {
        // "Hello World", offset 11 (end). Char at 10='d' (word). Scan back: d,l,r,o,W → 6.
        FormattedText doc = doc("Hello World");
        FormattedBlock block = doc.getBlocks().get(0);
        Assertions.assertEquals(6, Commands.findWordStart(block, 11));
    }

    @Test
    public void testFindWordStart_afterSpace() {
        // "Hello World", offset 6 (at 'W'). Char at 5=' ' (non-word).
        // Scan non-word: space. Then word: o,l,l,e,H → 0.
        FormattedText doc = doc("Hello World");
        FormattedBlock block = doc.getBlocks().get(0);
        Assertions.assertEquals(0, Commands.findWordStart(block, 6));
    }

    @Test
    public void testFindWordStart_atStart() {
        FormattedText doc = doc("Hello");
        Assertions.assertEquals(0, Commands.findWordStart(doc.getBlocks().get(0), 0));
    }

    @Test
    public void testFindWordStart_acrossLineBreak() {
        // "Hello\nWorld", offset 6 (='W'). Char at 5='\n' (non-word).
        // Scan non-word: \n. Then word: o,l,l,e,H → 0.
        FormattedText doc = new FormattedText();
        doc.block(BlockType.PARA, b -> { b.line("Hello"); b.line("World"); });
        Assertions.assertEquals(0, Commands.findWordStart(doc.getBlocks().get(0), 6));
    }

    @Test
    public void testFindWordEnd_atWordStart() {
        // "Hello World", offset 0. Char at 0='H' (word). Scan: H,e,l,l,o → 5.
        FormattedText doc = doc("Hello World");
        Assertions.assertEquals(5, Commands.findWordEnd(doc.getBlocks().get(0), 0));
    }

    @Test
    public void testFindWordEnd_atSpace() {
        // "Hello World", offset 5. Char at 5=' ' (non-word).
        // Scan non-word: space. Then word: W,o,r,l,d → 11.
        FormattedText doc = doc("Hello World");
        Assertions.assertEquals(11, Commands.findWordEnd(doc.getBlocks().get(0), 5));
    }

    @Test
    public void testFindWordEnd_atEnd() {
        FormattedText doc = doc("Hello");
        Assertions.assertEquals(5, Commands.findWordEnd(doc.getBlocks().get(0), 5));
    }

    /************************************************************************
     * Commands: deleteWordBefore
     ************************************************************************/

    @Test
    public void testDeleteWordBefore_midWord() {
        // "Hello World", cursor at 8 (between 'r' and 'l'). Char at 7='o' (word).
        // Scan word: o,W → 6. Deletes "Wo" → "Hello rld".
        FormattedText doc = doc("Hello World");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 8));

        Transaction tr = Commands.deleteWordBefore(state);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals("Hello rld", textAt(doc, 0));
        Assertions.assertEquals(0, state.selection().anchorBlock());
        Assertions.assertEquals(6, state.selection().anchorOffset());
    }

    @Test
    public void testDeleteWordBefore_afterSpace() {
        // "Hello World", cursor at 6 (at 'W' position, i.e. after space).
        // findWordStart(6): char at 5=' ', scan non-word→5, scan word→0. Deletes "Hello ".
        FormattedText doc = doc("Hello World");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 6));

        state.apply(Commands.deleteWordBefore(state));

        Assertions.assertEquals("World", textAt(doc, 0));
        Assertions.assertEquals(0, state.selection().anchorOffset());
    }

    @Test
    public void testDeleteWordBefore_endOfWord() {
        // "Hello World", cursor at 11 (end). Deletes "World" → "Hello ".
        FormattedText doc = doc("Hello World");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 11));

        state.apply(Commands.deleteWordBefore(state));

        Assertions.assertEquals("Hello ", textAt(doc, 0));
        Assertions.assertEquals(6, state.selection().anchorOffset());
    }

    @Test
    public void testDeleteWordBefore_atBlockStart_joins() {
        // Two same-type blocks, cursor at start of second → join.
        FormattedText doc = doc("Hello", "World");
        EditorState state = EditorState.create(doc, Selection.cursor(1, 0));

        Transaction tr = Commands.deleteWordBefore(state);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals(1, doc.getBlocks().size());
        Assertions.assertEquals("HelloWorld", textAt(doc, 0));
    }

    @Test
    public void testDeleteWordBefore_atDocStart_returnsNull() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));
        Assertions.assertNull(Commands.deleteWordBefore(state));
    }

    @Test
    public void testDeleteWordBefore_withRangeSelection() {
        // Range selection → delegates to deleteSelection.
        FormattedText doc = doc("Hello World");
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 0, 5));

        state.apply(Commands.deleteWordBefore(state));

        Assertions.assertEquals(" World", textAt(doc, 0));
    }

    @Test
    public void testDeleteWordBefore_undoRestores() {
        FormattedText doc = doc("Hello World");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 11));
        History history = new History();

        history.push(state.apply(Commands.deleteWordBefore(state)));
        Assertions.assertEquals("Hello ", textAt(doc, 0));

        history.undo(state);
        Assertions.assertEquals("Hello World", textAt(doc, 0));
    }

    /************************************************************************
     * Commands: deleteWordAfter
     ************************************************************************/

    @Test
    public void testDeleteWordAfter_midWord() {
        // "Hello World", cursor at 2 (after "He"). Deletes "llo" → "He World".
        FormattedText doc = doc("Hello World");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 2));

        Transaction tr = Commands.deleteWordAfter(state);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals("He World", textAt(doc, 0));
        Assertions.assertEquals(2, state.selection().anchorOffset());
    }

    @Test
    public void testDeleteWordAfter_atSpace() {
        // "Hello World", cursor at 5 (at ' '). Deletes " World" → "Hello".
        FormattedText doc = doc("Hello World");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 5));

        state.apply(Commands.deleteWordAfter(state));

        Assertions.assertEquals("Hello", textAt(doc, 0));
        Assertions.assertEquals(5, state.selection().anchorOffset());
    }

    @Test
    public void testDeleteWordAfter_atBlockEnd_joins() {
        FormattedText doc = doc("Hello", "World");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 5));

        Transaction tr = Commands.deleteWordAfter(state);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals(1, doc.getBlocks().size());
        Assertions.assertEquals("HelloWorld", textAt(doc, 0));
    }

    @Test
    public void testDeleteWordAfter_atDocEnd_returnsNull() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 5));
        Assertions.assertNull(Commands.deleteWordAfter(state));
    }

    @Test
    public void testDeleteWordAfter_undoRestores() {
        FormattedText doc = doc("Hello World");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));
        History history = new History();

        history.push(state.apply(Commands.deleteWordAfter(state)));
        Assertions.assertEquals(" World", textAt(doc, 0));

        history.undo(state);
        Assertions.assertEquals("Hello World", textAt(doc, 0));
    }

    /************************************************************************
     * Commands: selectAll
     ************************************************************************/

    @Test
    public void testSelectAll_singleBlock() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 3));

        Transaction tr = Commands.selectAll(state);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals(0, state.selection().fromBlock());
        Assertions.assertEquals(0, state.selection().fromOffset());
        Assertions.assertEquals(0, state.selection().toBlock());
        Assertions.assertEquals(5, state.selection().toOffset());
    }

    @Test
    public void testSelectAll_multiBlock() {
        FormattedText doc = doc("Hello", "World", "!");
        EditorState state = EditorState.create(doc, Selection.cursor(1, 2));

        state.apply(Commands.selectAll(state));

        Assertions.assertEquals(0, state.selection().fromBlock());
        Assertions.assertEquals(0, state.selection().fromOffset());
        Assertions.assertEquals(2, state.selection().toBlock());
        Assertions.assertEquals(1, state.selection().toOffset());
    }

    @Test
    public void testSelectAll_alreadySelected_returnsNull() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 0, 5));
        Assertions.assertNull(Commands.selectAll(state));
    }

    @Test
    public void testSelectAll_noSteps() {
        // Transaction should have no steps (only selection change).
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));
        Transaction tr = Commands.selectAll(state);
        Assertions.assertNotNull(tr);
        // Apply and verify it works (no mutation, just selection).
        state.apply(tr);
        Assertions.assertFalse(state.selection().isCursor());
    }

    /************************************************************************
     * Multi-block setBlockType
     ************************************************************************/

    @Test
    public void testSetBlockType_rangeSelection() {
        FormattedText doc = doc("A", "B", "C");
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 2, 1));

        Transaction tr = Commands.setBlockType(state, BlockType.H1);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals(BlockType.H1, doc.getBlocks().get(0).getType());
        Assertions.assertEquals(BlockType.H1, doc.getBlocks().get(1).getType());
        Assertions.assertEquals(BlockType.H1, doc.getBlocks().get(2).getType());
    }

    @Test
    public void testSetBlockType_rangeSelection_skipsMatching() {
        FormattedText doc = new FormattedText();
        doc.block(BlockType.PARA, b -> b.line("A"));
        doc.block(BlockType.H1, b -> b.line("B"));
        doc.block(BlockType.PARA, b -> b.line("C"));
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 2, 1));

        state.apply(Commands.setBlockType(state, BlockType.H1));

        // All are now H1.
        Assertions.assertEquals(BlockType.H1, doc.getBlocks().get(0).getType());
        Assertions.assertEquals(BlockType.H1, doc.getBlocks().get(1).getType());
        Assertions.assertEquals(BlockType.H1, doc.getBlocks().get(2).getType());
    }

    @Test
    public void testSetBlockType_rangeSelection_allSameType_returnsNull() {
        FormattedText doc = new FormattedText();
        doc.block(BlockType.H1, b -> b.line("A"));
        doc.block(BlockType.H1, b -> b.line("B"));
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 1, 1));
        Assertions.assertNull(Commands.setBlockType(state, BlockType.H1));
    }

    @Test
    public void testSetBlockType_rangeSelection_undoRestores() {
        FormattedText doc = doc("A", "B");
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 1, 1));
        History history = new History();

        history.push(state.apply(Commands.setBlockType(state, BlockType.H1)));
        Assertions.assertEquals(BlockType.H1, doc.getBlocks().get(0).getType());
        Assertions.assertEquals(BlockType.H1, doc.getBlocks().get(1).getType());

        history.undo(state);
        Assertions.assertEquals(BlockType.PARA, doc.getBlocks().get(0).getType());
        Assertions.assertEquals(BlockType.PARA, doc.getBlocks().get(1).getType());
    }

    /************************************************************************
     * Multi-block indent / outdent
     ************************************************************************/

    @Test
    public void testIndent_rangeSelection() {
        FormattedText doc = doc("A", "B", "C");
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 2, 1));

        state.apply(Commands.indent(state));

        Assertions.assertEquals(1, doc.getBlocks().get(0).getIndent());
        Assertions.assertEquals(1, doc.getBlocks().get(1).getIndent());
        Assertions.assertEquals(1, doc.getBlocks().get(2).getIndent());
    }

    @Test
    public void testIndent_rangeSelection_someAtMax() {
        FormattedText doc = new FormattedText();
        doc.block(BlockType.PARA, b -> { b.line("A"); b.indent(3); });
        doc.block(BlockType.PARA, b -> { b.line("B"); b.indent(5); });
        doc.block(BlockType.PARA, b -> { b.line("C"); b.indent(2); });
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 2, 1));

        state.apply(Commands.indent(state));

        Assertions.assertEquals(4, doc.getBlocks().get(0).getIndent());
        Assertions.assertEquals(5, doc.getBlocks().get(1).getIndent()); // unchanged
        Assertions.assertEquals(3, doc.getBlocks().get(2).getIndent());
    }

    @Test
    public void testIndent_rangeSelection_allAtMax_returnsNull() {
        FormattedText doc = new FormattedText();
        doc.block(BlockType.PARA, b -> { b.line("A"); b.indent(5); });
        doc.block(BlockType.PARA, b -> { b.line("B"); b.indent(5); });
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 1, 1));
        Assertions.assertNull(Commands.indent(state));
    }

    @Test
    public void testIndent_rangeSelection_undoRestores() {
        FormattedText doc = doc("A", "B");
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 1, 1));
        History history = new History();

        history.push(state.apply(Commands.indent(state)));
        Assertions.assertEquals(1, doc.getBlocks().get(0).getIndent());
        Assertions.assertEquals(1, doc.getBlocks().get(1).getIndent());

        history.undo(state);
        Assertions.assertEquals(0, doc.getBlocks().get(0).getIndent());
        Assertions.assertEquals(0, doc.getBlocks().get(1).getIndent());
    }

    @Test
    public void testOutdent_rangeSelection() {
        FormattedText doc = new FormattedText();
        doc.block(BlockType.PARA, b -> { b.line("A"); b.indent(2); });
        doc.block(BlockType.PARA, b -> { b.line("B"); b.indent(2); });
        doc.block(BlockType.PARA, b -> { b.line("C"); b.indent(2); });
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 2, 1));

        state.apply(Commands.outdent(state));

        Assertions.assertEquals(1, doc.getBlocks().get(0).getIndent());
        Assertions.assertEquals(1, doc.getBlocks().get(1).getIndent());
        Assertions.assertEquals(1, doc.getBlocks().get(2).getIndent());
    }

    @Test
    public void testOutdent_rangeSelection_someAtMin() {
        FormattedText doc = new FormattedText();
        doc.block(BlockType.PARA, b -> { b.line("A"); b.indent(0); });
        doc.block(BlockType.PARA, b -> { b.line("B"); b.indent(2); });
        doc.block(BlockType.PARA, b -> { b.line("C"); b.indent(1); });
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 2, 1));

        state.apply(Commands.outdent(state));

        Assertions.assertEquals(0, doc.getBlocks().get(0).getIndent()); // unchanged
        Assertions.assertEquals(1, doc.getBlocks().get(1).getIndent());
        Assertions.assertEquals(0, doc.getBlocks().get(2).getIndent());
    }

    @Test
    public void testOutdent_rangeSelection_allAtMin_returnsNull() {
        FormattedText doc = doc("A", "B");
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 1, 1));
        Assertions.assertNull(Commands.outdent(state));
    }

    @Test
    public void testOutdent_rangeSelection_undoRestores() {
        FormattedText doc = new FormattedText();
        doc.block(BlockType.PARA, b -> { b.line("A"); b.indent(2); });
        doc.block(BlockType.PARA, b -> { b.line("B"); b.indent(3); });
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 1, 1));
        History history = new History();

        history.push(state.apply(Commands.outdent(state)));
        Assertions.assertEquals(1, doc.getBlocks().get(0).getIndent());
        Assertions.assertEquals(2, doc.getBlocks().get(1).getIndent());

        history.undo(state);
        Assertions.assertEquals(2, doc.getBlocks().get(0).getIndent());
        Assertions.assertEquals(3, doc.getBlocks().get(1).getIndent());
    }

    /************************************************************************
     * Phase 8: Multi-block formatting
     ************************************************************************/

    @Test
    public void testApplyFormat_multiBlock_undoRestores() {
        FormattedText doc = doc("Hello", "World");
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 1, 5));
        History history = new History();

        history.push(state.apply(Commands.applyFormat(state, FormatType.BLD)));
        Assertions.assertTrue(doc.getBlocks().get(0).hasFormat(0, 5, FormatType.BLD));
        Assertions.assertTrue(doc.getBlocks().get(1).hasFormat(0, 5, FormatType.BLD));

        history.undo(state);
        Assertions.assertTrue(doc.getBlocks().get(0).getLines().get(0).getFormatting().isEmpty());
        Assertions.assertTrue(doc.getBlocks().get(1).getLines().get(0).getFormatting().isEmpty());
    }

    @Test
    public void testRemoveFormat_multiBlock_undoRestores() {
        FormattedText doc = doc("Hello", "World");
        doc.getBlocks().get(0).addFormat(0, 5, FormatType.ITL);
        doc.getBlocks().get(1).addFormat(0, 5, FormatType.ITL);
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 1, 5));
        History history = new History();

        history.push(state.apply(Commands.removeFormat(state, FormatType.ITL)));
        Assertions.assertTrue(doc.getBlocks().get(0).getLines().get(0).getFormatting().isEmpty());
        Assertions.assertTrue(doc.getBlocks().get(1).getLines().get(0).getFormatting().isEmpty());

        history.undo(state);
        Assertions.assertTrue(doc.getBlocks().get(0).hasFormat(0, 5, FormatType.ITL));
        Assertions.assertTrue(doc.getBlocks().get(1).hasFormat(0, 5, FormatType.ITL));
    }

    /************************************************************************
     * Phase 8: Multi-block link commands
     ************************************************************************/

    @Test
    public void testApplyLink_multiBlock_undoRestores() {
        FormattedText doc = doc("Hello", "World");
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 1, 5));
        History history = new History();

        history.push(state.apply(Commands.applyLink(state, "http://example.com")));
        Assertions.assertTrue(doc.getBlocks().get(0).hasFormat(0, 5, FormatType.A));
        Assertions.assertTrue(doc.getBlocks().get(1).hasFormat(0, 5, FormatType.A));

        history.undo(state);
        Assertions.assertTrue(doc.getBlocks().get(0).getLines().get(0).getFormatting().isEmpty());
        Assertions.assertTrue(doc.getBlocks().get(1).getLines().get(0).getFormatting().isEmpty());
    }

    @Test
    public void testRemoveLink_multiBlock() {
        FormattedText doc = doc("Hello", "World");
        doc.getBlocks().get(0).addFormat(0, 5, FormatType.A);
        doc.getBlocks().get(1).addFormat(0, 5, FormatType.A);
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 1, 5));

        Transaction tr = Commands.removeLink(state);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertTrue(doc.getBlocks().get(0).getLines().get(0).getFormatting().isEmpty());
        Assertions.assertTrue(doc.getBlocks().get(1).getLines().get(0).getFormatting().isEmpty());
    }

    @Test
    public void testRemoveLink_multiBlock_undoRestores() {
        FormattedText doc = doc("Hello", "World");
        doc.getBlocks().get(0).addFormat(0, 5, FormatType.A);
        doc.getBlocks().get(1).addFormat(0, 5, FormatType.A);
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 1, 5));
        History history = new History();

        history.push(state.apply(Commands.removeLink(state)));
        Assertions.assertTrue(doc.getBlocks().get(0).getLines().get(0).getFormatting().isEmpty());
        Assertions.assertTrue(doc.getBlocks().get(1).getLines().get(0).getFormatting().isEmpty());

        history.undo(state);
        Assertions.assertTrue(doc.getBlocks().get(0).hasFormat(0, 5, FormatType.A));
        Assertions.assertTrue(doc.getBlocks().get(1).hasFormat(0, 5, FormatType.A));
    }

    @Test
    public void testUpdateLink_multiBlock() {
        FormattedText doc = doc("Hello", "World");
        // Apply link on both blocks first.
        doc.getBlocks().get(0).addFormat(0, 5, FormatType.A);
        doc.getBlocks().get(0).getLines().get(0).getFormatting().get(0).getMeta().put(FormattedLine.META_LINK, "http://old.com");
        doc.getBlocks().get(1).addFormat(0, 5, FormatType.A);
        doc.getBlocks().get(1).getLines().get(0).getFormatting().get(0).getMeta().put(FormattedLine.META_LINK, "http://old.com");
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 1, 5));

        Transaction tr = Commands.updateLink(state, "http://new.com");
        Assertions.assertNotNull(tr);
        state.apply(tr);

        FormattedLine.Format fmt0 = doc.getBlocks().get(0).getLines().get(0).getFormatting().get(0);
        Assertions.assertEquals("http://new.com", fmt0.getMeta().get(FormattedLine.META_LINK));
        FormattedLine.Format fmt1 = doc.getBlocks().get(1).getLines().get(0).getFormatting().get(0);
        Assertions.assertEquals("http://new.com", fmt1.getMeta().get(FormattedLine.META_LINK));
    }

    @Test
    public void testUpdateLink_multiBlock_undoRestores() {
        FormattedText doc = doc("Hello", "World");
        doc.getBlocks().get(0).addFormat(0, 5, FormatType.A);
        doc.getBlocks().get(0).getLines().get(0).getFormatting().get(0).getMeta().put(FormattedLine.META_LINK, "http://old.com");
        doc.getBlocks().get(1).addFormat(0, 5, FormatType.A);
        doc.getBlocks().get(1).getLines().get(0).getFormatting().get(0).getMeta().put(FormattedLine.META_LINK, "http://old.com");
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 1, 5));
        History history = new History();

        history.push(state.apply(Commands.updateLink(state, "http://new.com")));
        Assertions.assertEquals("http://new.com", doc.getBlocks().get(0).getLines().get(0).getFormatting().get(0).getMeta().get(FormattedLine.META_LINK));

        history.undo(state);
        Assertions.assertEquals("http://old.com", doc.getBlocks().get(0).getLines().get(0).getFormatting().get(0).getMeta().get(FormattedLine.META_LINK));
        Assertions.assertEquals("http://old.com", doc.getBlocks().get(1).getLines().get(0).getFormatting().get(0).getMeta().get(FormattedLine.META_LINK));
    }

    /************************************************************************
     * Phase 8: clearFormatting
     ************************************************************************/

    @Test
    public void testClearFormatting_cursorReturnsNull() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));
        Assertions.assertNull(Commands.clearFormatting(state));
    }

    @Test
    public void testClearFormatting_singleBlock() {
        // Bold on "Hello" [0,5), select [1,4) → clears bold from "ell".
        FormattedText doc = doc("Hello");
        doc.getBlocks().get(0).addFormat(0, 5, FormatType.BLD);
        EditorState state = EditorState.create(doc, Selection.range(0, 1, 0, 4));

        Transaction tr = Commands.clearFormatting(state);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        // Should have two remaining format entries: [0,1) and [4,1) for the portions outside the selection.
        java.util.List<FormattedLine.Format> fmts = doc.getBlocks().get(0).getLines().get(0).getFormatting();
        Assertions.assertEquals(2, fmts.size());
        // Left portion: [0,1).
        Assertions.assertEquals(0, fmts.get(0).getIndex());
        Assertions.assertEquals(1, fmts.get(0).getLength());
        // Right portion: [4,1).
        Assertions.assertEquals(4, fmts.get(1).getIndex());
        Assertions.assertEquals(1, fmts.get(1).getLength());
    }

    @Test
    public void testClearFormatting_multiBlock() {
        FormattedText doc = doc("Hello", "World");
        doc.getBlocks().get(0).addFormat(0, 5, FormatType.BLD);
        doc.getBlocks().get(1).addFormat(0, 5, FormatType.ITL);
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 1, 5));

        Transaction tr = Commands.clearFormatting(state);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertTrue(doc.getBlocks().get(0).getLines().get(0).getFormatting().isEmpty());
        Assertions.assertTrue(doc.getBlocks().get(1).getLines().get(0).getFormatting().isEmpty());
    }

    @Test
    public void testClearFormatting_multipleFormatTypes() {
        // Multiple format types on same range.
        FormattedText doc = doc("Hello");
        doc.getBlocks().get(0).addFormat(0, 5, FormatType.BLD);
        doc.getBlocks().get(0).addFormat(0, 5, FormatType.ITL);
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 0, 5));

        state.apply(Commands.clearFormatting(state));

        Assertions.assertTrue(doc.getBlocks().get(0).getLines().get(0).getFormatting().isEmpty());
    }

    @Test
    public void testClearFormatting_undoRestores() {
        FormattedText doc = doc("Hello");
        doc.getBlocks().get(0).addFormat(0, 5, FormatType.BLD);
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 0, 5));
        History history = new History();

        history.push(state.apply(Commands.clearFormatting(state)));
        Assertions.assertTrue(doc.getBlocks().get(0).getLines().get(0).getFormatting().isEmpty());

        history.undo(state);
        Assertions.assertTrue(doc.getBlocks().get(0).hasFormat(0, 5, FormatType.BLD));
    }

    /************************************************************************
     * Phase 8: duplicateBlock
     ************************************************************************/

    @Test
    public void testDuplicateBlock() {
        FormattedText doc = doc("Hello", "World");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 3));

        Transaction tr = Commands.duplicateBlock(state, 0);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals(3, doc.getBlocks().size());
        Assertions.assertEquals("Hello", textAt(doc, 0));
        Assertions.assertEquals("Hello", textAt(doc, 1));
        Assertions.assertEquals("World", textAt(doc, 2));
        // Cursor moves to new block.
        Assertions.assertEquals(1, state.selection().anchorBlock());
        Assertions.assertEquals(0, state.selection().anchorOffset());
    }

    @Test
    public void testDuplicateBlock_invalidIndex() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc);
        Assertions.assertNull(Commands.duplicateBlock(state, -1));
        Assertions.assertNull(Commands.duplicateBlock(state, 1));
    }

    @Test
    public void testDuplicateBlock_undoRestores() {
        FormattedText doc = doc("Hello", "World");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));
        History history = new History();

        history.push(state.apply(Commands.duplicateBlock(state, 0)));
        Assertions.assertEquals(3, doc.getBlocks().size());

        history.undo(state);
        Assertions.assertEquals(2, doc.getBlocks().size());
        Assertions.assertEquals("Hello", textAt(doc, 0));
        Assertions.assertEquals("World", textAt(doc, 1));
    }

    /************************************************************************
     * Phase 8: toggleBlockType
     ************************************************************************/

    @Test
    public void testToggleBlockType_cursorToggleOn() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));

        Transaction tr = Commands.toggleBlockType(state, BlockType.H1);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals(BlockType.H1, doc.getBlocks().get(0).getType());
    }

    @Test
    public void testToggleBlockType_cursorToggleOff() {
        // Block is already H1 → toggle H1 should revert to PARA.
        FormattedText doc = new FormattedText();
        doc.block(BlockType.H1, b -> b.line("Title"));
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));

        Transaction tr = Commands.toggleBlockType(state, BlockType.H1);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals(BlockType.PARA, doc.getBlocks().get(0).getType());
    }

    @Test
    public void testToggleBlockType_rangeAllSame_revertsToParas() {
        FormattedText doc = new FormattedText();
        doc.block(BlockType.H2, b -> b.line("A"));
        doc.block(BlockType.H2, b -> b.line("B"));
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 1, 1));

        Transaction tr = Commands.toggleBlockType(state, BlockType.H2);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals(BlockType.PARA, doc.getBlocks().get(0).getType());
        Assertions.assertEquals(BlockType.PARA, doc.getBlocks().get(1).getType());
    }

    @Test
    public void testToggleBlockType_rangeMixed_setsAll() {
        FormattedText doc = new FormattedText();
        doc.block(BlockType.PARA, b -> b.line("A"));
        doc.block(BlockType.H2, b -> b.line("B"));
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 1, 1));

        Transaction tr = Commands.toggleBlockType(state, BlockType.H2);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals(BlockType.H2, doc.getBlocks().get(0).getType());
        Assertions.assertEquals(BlockType.H2, doc.getBlocks().get(1).getType());
    }

    @Test
    public void testToggleBlockType_allAlreadyPara_toggleToPara_returnsNull() {
        // All blocks are PARA and we toggle to PARA → nothing changes.
        FormattedText doc = doc("A", "B");
        EditorState state = EditorState.create(doc, Selection.range(0, 0, 1, 1));
        Assertions.assertNull(Commands.toggleBlockType(state, BlockType.PARA));
    }

    @Test
    public void testToggleBlockType_undoRestores() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));
        History history = new History();

        history.push(state.apply(Commands.toggleBlockType(state, BlockType.H1)));
        Assertions.assertEquals(BlockType.H1, doc.getBlocks().get(0).getType());

        history.undo(state);
        Assertions.assertEquals(BlockType.PARA, doc.getBlocks().get(0).getType());
    }

    /************************************************************************
     * Phase 9: extractSelection
     ************************************************************************/

    @Test
    public void testExtractSelection_cursorReturnsNull() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 3));
        Assertions.assertNull(Commands.extractSelection(state));
    }

    @Test
    public void testExtractSelection_singleBlock() {
        FormattedText doc = doc("Hello World");
        EditorState state = EditorState.create(doc, Selection.range(0, 6, 0, 11));

        FormattedText extracted = Commands.extractSelection(state);
        Assertions.assertNotNull(extracted);
        Assertions.assertEquals(1, extracted.getBlocks().size());
        Assertions.assertEquals("World", extracted.getBlocks().get(0).getLines().get(0).getText());
    }

    @Test
    public void testExtractSelection_singleBlock_partialRange() {
        FormattedText doc = doc("Hello World");
        EditorState state = EditorState.create(doc, Selection.range(0, 2, 0, 7));

        FormattedText extracted = Commands.extractSelection(state);
        Assertions.assertEquals("llo W", extracted.getBlocks().get(0).getLines().get(0).getText());
    }

    @Test
    public void testExtractSelection_multiBlock() {
        FormattedText doc = doc("Hello", "Middle", "World");
        EditorState state = EditorState.create(doc, Selection.range(0, 3, 2, 3));

        FormattedText extracted = Commands.extractSelection(state);
        Assertions.assertEquals(3, extracted.getBlocks().size());
        Assertions.assertEquals("lo", extracted.getBlocks().get(0).getLines().get(0).getText());
        Assertions.assertEquals("Middle", extracted.getBlocks().get(1).getLines().get(0).getText());
        Assertions.assertEquals("Wor", extracted.getBlocks().get(2).getLines().get(0).getText());
    }

    @Test
    public void testExtractSelection_preservesFormatting() {
        FormattedText doc = doc("Hello");
        doc.getBlocks().get(0).addFormat(0, 5, FormatType.BLD);
        EditorState state = EditorState.create(doc, Selection.range(0, 1, 0, 4));

        FormattedText extracted = Commands.extractSelection(state);
        // Extracted "ell" should have bold formatting.
        Assertions.assertTrue(extracted.getBlocks().get(0).hasFormat(0, 3, FormatType.BLD));
    }

    @Test
    public void testExtractSelection_doesNotModifyOriginal() {
        FormattedText doc = doc("Hello", "World");
        EditorState state = EditorState.create(doc, Selection.range(0, 2, 1, 3));

        Commands.extractSelection(state);

        // Original document is unchanged.
        Assertions.assertEquals("Hello", textAt(doc, 0));
        Assertions.assertEquals("World", textAt(doc, 1));
    }

    /************************************************************************
     * Phase 9: paste single-block
     ************************************************************************/

    @Test
    public void testPaste_nullReturnsNull() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 5));
        Assertions.assertNull(Commands.paste(state, null));
    }

    @Test
    public void testPaste_emptyReturnsNull() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 5));
        Assertions.assertNull(Commands.paste(state, new FormattedText()));
    }

    @Test
    public void testPaste_singleBlock_atCursor() {
        // Paste "XY" into "Hello" at offset 5 → "HelloXY".
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 5));
        FormattedText clipboard = doc("XY");

        Transaction tr = Commands.paste(state, clipboard);
        Assertions.assertNotNull(tr);
        state.apply(tr);

        Assertions.assertEquals("HelloXY", textAt(doc, 0));
        Assertions.assertEquals(0, state.selection().anchorBlock());
        Assertions.assertEquals(7, state.selection().anchorOffset());
    }

    @Test
    public void testPaste_singleBlock_atMiddle() {
        // Paste "XY" into "Hello" at offset 2 → "HeXYllo".
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 2));
        FormattedText clipboard = doc("XY");

        state.apply(Commands.paste(state, clipboard));

        Assertions.assertEquals("HeXYllo", textAt(doc, 0));
        Assertions.assertEquals(4, state.selection().anchorOffset());
    }

    @Test
    public void testPaste_singleBlock_preservesFormatting() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 5));
        FormattedText clipboard = new FormattedText();
        clipboard.block(BlockType.PARA, b -> {
            b.line("XY");
            b.addFormat(0, 2, FormatType.BLD);
        });

        state.apply(Commands.paste(state, clipboard));

        Assertions.assertEquals("HelloXY", textAt(doc, 0));
        // Bold should be on the pasted portion [5,7).
        Assertions.assertTrue(doc.getBlocks().get(0).hasFormat(5, 2, FormatType.BLD));
    }

    @Test
    public void testPaste_singleBlock_overSelection() {
        // Select [1,4) in "Hello" → paste "XY" → "HXYo".
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.range(0, 1, 0, 4));
        FormattedText clipboard = doc("XY");

        state.apply(Commands.paste(state, clipboard));

        Assertions.assertEquals("HXYo", textAt(doc, 0));
        Assertions.assertEquals(3, state.selection().anchorOffset());
    }

    @Test
    public void testPaste_singleBlock_undoRestores() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 5));
        History history = new History();
        FormattedText clipboard = doc("XY");

        history.push(state.apply(Commands.paste(state, clipboard)));
        Assertions.assertEquals("HelloXY", textAt(doc, 0));

        history.undo(state);
        Assertions.assertEquals("Hello", textAt(doc, 0));
    }

    /************************************************************************
     * Phase 9: paste multi-block
     ************************************************************************/

    @Test
    public void testPaste_multiBlock_twoBlocks() {
        // Paste "AB" + "CD" into "Hello" at offset 2 → "HeAB", "CDllo".
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 2));
        FormattedText clipboard = doc("AB", "CD");

        state.apply(Commands.paste(state, clipboard));

        Assertions.assertEquals(2, doc.getBlocks().size());
        Assertions.assertEquals("HeAB", textAt(doc, 0));
        Assertions.assertEquals("CDllo", textAt(doc, 1));
        Assertions.assertEquals(1, state.selection().anchorBlock());
        Assertions.assertEquals(2, state.selection().anchorOffset());
    }

    @Test
    public void testPaste_multiBlock_threeBlocks() {
        // Paste "AB" + "MID" + "CD" into "Hello" at offset 2 → "HeAB", "MID", "CDllo".
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 2));
        FormattedText clipboard = doc("AB", "MID", "CD");

        state.apply(Commands.paste(state, clipboard));

        Assertions.assertEquals(3, doc.getBlocks().size());
        Assertions.assertEquals("HeAB", textAt(doc, 0));
        Assertions.assertEquals("MID", textAt(doc, 1));
        Assertions.assertEquals("CDllo", textAt(doc, 2));
        Assertions.assertEquals(2, state.selection().anchorBlock());
        Assertions.assertEquals(2, state.selection().anchorOffset());
    }

    @Test
    public void testPaste_multiBlock_atBlockStart() {
        // Paste "AB" + "CD" into "Hello" at offset 0 → "AB", "CDHello".
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 0));
        FormattedText clipboard = doc("AB", "CD");

        state.apply(Commands.paste(state, clipboard));

        Assertions.assertEquals(2, doc.getBlocks().size());
        Assertions.assertEquals("AB", textAt(doc, 0));
        Assertions.assertEquals("CDHello", textAt(doc, 1));
    }

    @Test
    public void testPaste_multiBlock_atBlockEnd() {
        // Paste "AB" + "CD" into "Hello" at offset 5 → "HelloAB", "CD".
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 5));
        FormattedText clipboard = doc("AB", "CD");

        state.apply(Commands.paste(state, clipboard));

        Assertions.assertEquals(2, doc.getBlocks().size());
        Assertions.assertEquals("HelloAB", textAt(doc, 0));
        Assertions.assertEquals("CD", textAt(doc, 1));
    }

    @Test
    public void testPaste_multiBlock_overMultiBlockSelection() {
        // Select from block 0 offset 2 to block 1 offset 3 in "Hello", "World".
        // Paste "AB" + "CD" → delete selection → paste at (0, 2).
        FormattedText doc = doc("Hello", "World");
        EditorState state = EditorState.create(doc, Selection.range(0, 2, 1, 3));
        FormattedText clipboard = doc("AB", "CD");

        state.apply(Commands.paste(state, clipboard));

        // After delete: "Held" (joined since same type). After paste "AB"+"CD" at offset 2: "HeAB", "CDld".
        Assertions.assertEquals(2, doc.getBlocks().size());
        Assertions.assertEquals("HeAB", textAt(doc, 0));
        Assertions.assertEquals("CDld", textAt(doc, 1));
    }

    @Test
    public void testPaste_multiBlock_undoRestores() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 2));
        History history = new History();
        FormattedText clipboard = doc("AB", "CD");

        history.push(state.apply(Commands.paste(state, clipboard)));
        Assertions.assertEquals(2, doc.getBlocks().size());
        Assertions.assertEquals("HeAB", textAt(doc, 0));
        Assertions.assertEquals("CDllo", textAt(doc, 1));

        history.undo(state);
        Assertions.assertEquals(1, doc.getBlocks().size());
        Assertions.assertEquals("Hello", textAt(doc, 0));
    }

    /************************************************************************
     * Phase 9: pasteText
     ************************************************************************/

    @Test
    public void testPasteText_singleLine() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 5));

        state.apply(Commands.pasteText(state, "XY"));

        Assertions.assertEquals("HelloXY", textAt(doc, 0));
    }

    @Test
    public void testPasteText_multiLine() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 2));

        state.apply(Commands.pasteText(state, "AB\nCD\nEF"));

        Assertions.assertEquals(3, doc.getBlocks().size());
        Assertions.assertEquals("HeAB", textAt(doc, 0));
        Assertions.assertEquals("CD", textAt(doc, 1));
        Assertions.assertEquals("EFllo", textAt(doc, 2));
    }

    @Test
    public void testPasteText_undoRestores() {
        FormattedText doc = doc("Hello");
        EditorState state = EditorState.create(doc, Selection.cursor(0, 5));
        History history = new History();

        history.push(state.apply(Commands.pasteText(state, "AB\nCD")));
        Assertions.assertEquals(2, doc.getBlocks().size());

        history.undo(state);
        Assertions.assertEquals(1, doc.getBlocks().size());
        Assertions.assertEquals("Hello", textAt(doc, 0));
    }

    /************************************************************************
     * Phase 9: round-trip (extract then paste)
     ************************************************************************/

    @Test
    public void testRoundTrip_extractThenPaste() {
        // Extract "llo W" from "Hello World", paste into "Target" at offset 3.
        FormattedText doc = doc("Hello World");
        EditorState state = EditorState.create(doc, Selection.range(0, 2, 0, 7));
        FormattedText extracted = Commands.extractSelection(state);

        // Paste into a separate document.
        FormattedText doc2 = doc("Target");
        EditorState state2 = EditorState.create(doc2, Selection.cursor(0, 3));
        state2.apply(Commands.paste(state2, extracted));

        Assertions.assertEquals("Tarllo Wget", textAt(doc2, 0));
    }

    @Test
    public void testRoundTrip_extractMultiBlock_pasteThenUndo() {
        FormattedText doc = doc("Hello", "Middle", "World");
        EditorState state = EditorState.create(doc, Selection.range(0, 3, 2, 3));
        FormattedText extracted = Commands.extractSelection(state);

        // Paste into another doc.
        FormattedText doc2 = doc("Target");
        EditorState state2 = EditorState.create(doc2, Selection.cursor(0, 3));
        History history = new History();

        history.push(state2.apply(Commands.paste(state2, extracted)));
        Assertions.assertEquals(3, doc2.getBlocks().size());
        Assertions.assertEquals("Tarlo", textAt(doc2, 0));
        Assertions.assertEquals("Middle", textAt(doc2, 1));
        Assertions.assertEquals("Worget", textAt(doc2, 2));

        history.undo(state2);
        Assertions.assertEquals(1, doc2.getBlocks().size());
        Assertions.assertEquals("Target", textAt(doc2, 0));
    }
}
