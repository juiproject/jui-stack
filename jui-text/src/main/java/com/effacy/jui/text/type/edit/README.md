# Package

Transaction-based state management for the rich text editor. Contains the editing pipeline — steps (atomic document mutations), transactions (grouped steps applied atomically), position mapping, selection tracking, undo/redo history, and high-level editing commands.

The [`step`](./step/) subpackage holds the 12 concrete step implementations.

# Design

## Overview

This package implements a ProseMirror-inspired transaction system for mutating `FormattedText` documents. All document changes flow through immutable transactions composed of atomic steps, giving deterministic undo/redo and consistent position tracking.

## Architecture

```
Commands  ──builds──▶  Transaction  ──contains──▶  Step(s)
                            │                        │
                            │ apply(doc)             │ apply(doc)
                            ▼                        ▼
                     TransactionResult          StepResult
                      ├─ inverse Transaction     ├─ inverse Step
                      └─ Mapping                 └─ StepMap

EditorState  ──applies──▶  Transaction
     │                          │
     │ updates selection        │ mutates doc in place
     │ (explicit or mapped)     │
     ▼                          ▼
  History  ◀──pushes──  inverse Transaction
```

The typical flow for an editing operation:

1. A `Commands` factory method inspects the current `EditorState` and builds a `Transaction` with the appropriate steps and an explicit post-operation `Selection`.
2. `EditorState.apply(tr)` applies the transaction to the document, updates the selection, and returns the inverse transaction.
3. The caller pushes the inverse onto `History` for undo.

## Flat position model

`Positions` defines a flat, document-wide position scheme where every addressable point has a unique integer. Each block contributes `2 + contentSize` positions: an opening boundary token, the content characters (with line breaks counting as 1), and a closing boundary token.

```
Document: PARA("Hello\nWorld"), H1("Title")

[open] H e l l o \n W o r l d [close] [open] T i t l e [close]
  0    1 2 3 4 5  6  7 8 9 10 11  12     13  14 15 16 17 18  19
```

`Selection` uses block-relative coordinates `(blockIndex, charOffset)` for ergonomic use in commands. Conversion between flat and block-relative is done via `Positions.toFlat()` and `Positions.resolve()`.

## Steps

Steps are the atomic mutations. Each `Step.apply(doc)` mutates the document in place and returns a `StepResult` containing:

- The **inverse step** that undoes the change.
- A **StepMap** describing how flat positions shifted.

The `step/` subpackage contains 12 step implementations:

| Step | Purpose | StepMap | Inverse |
|------|---------|---------|---------|
| `InsertBlockStep` | Inserts a block at an index | Inserts `nodeSize` positions | `DeleteBlockStep` |
| `DeleteBlockStep` | Removes a block at an index | Removes `nodeSize` positions | `InsertBlockStep` (captures removed block) |
| `ReplaceBlockStep` | Replaces a block wholesale | Replaces `oldNodeSize` with `newNodeSize` | `ReplaceBlockStep` (captures original) |
| `SetBlockTypeStep` | Changes a block's type | `EMPTY` (no positional change) | `SetBlockTypeStep` (captures old type) |
| `SetBlockIndentStep` | Changes a block's indent level | `EMPTY` | `SetBlockIndentStep` (captures old indent) |
| `MoveBlockStep` | Moves consecutive blocks to a new position | Two-range map (remove + insert) | `MoveBlockStep` (swapped from/to) |
| `InsertTextStep` | Inserts plain text at block offset | Precise: `(pos, 0, len)` | `ReplaceBlockStep` (captures original block clone) |
| `DeleteTextStep` | Deletes a text range within a block | Precise: `(pos, len, 0)` | `ReplaceBlockStep` (captures original block clone) |
| `SplitBlockStep` | Splits a block at an offset into two | Inserts 2 boundary tokens: `(pos, 0, 2)` | `JoinBlocksStep` |
| `JoinBlocksStep` | Merges adjacent same-type blocks | Removes 2 boundary tokens: `(pos, 2, 0)` | `SplitBlockStep` |
| `ChangeFormatStep` | Adds or removes a `FormatType` on a character range | `EMPTY` | `ReplaceBlockStep` (captures original block clone) |
| `SetBlockMetaStep` | Sets or removes a metadata entry on a block | `EMPTY` | `SetBlockMetaStep` (captures old value) |

### Precise vs coarse StepMaps

Text-level steps (`InsertTextStep`, `DeleteTextStep`) produce **precise** forward StepMaps that only shift positions at or after the edit point. Their inverses use `ReplaceBlockStep` which produces a **coarse** StepMap (treats the entire block content as replaced). This is acceptable because cursor placement for undo/redo is handled by explicit selection (see below), not by mapping through the inverse's StepMap.

## Transactions and selection

A `Transaction` is an ordered list of steps applied atomically. `Transaction.apply(doc)` applies each step in sequence, collects inverse steps (in reverse order), and composes the StepMaps into a `Mapping`.

Transactions carry an optional explicit `Selection`. When `EditorState.apply(tr)` processes a transaction:

- **With explicit selection**: the selection is used directly, bypassing StepMap mapping. This is the normal case for all `Commands`-generated transactions.
- **Without explicit selection**: the previous selection is mapped through the `Mapping` to compute its new position. This is a fallback for programmatic step-only transactions.

Before applying, `EditorState` saves the current (pre-apply) selection on the inverse transaction. This ensures undo restores the original cursor position. The forward-to-undo-to-redo cycle:

```
Forward:  steps + selection=cursor(0,7)
          EditorState saves pre-apply selection cursor(0,8) on inverse
Undo:     inverse steps + selection=cursor(0,8)  ← original restored
Redo:     inverse-of-inverse steps + selection=cursor(0,7)  ← forward restored
```

## Commands

`Commands` is a stateless factory that builds transactions for standard editing operations. Each method inspects the `EditorState`, returns a `Transaction` with steps and explicit selection, or `null` if the operation cannot be performed (e.g., cannot outdent at indent 0).

| Command | Keyboard | Behaviour |
|---------|----------|-----------|
| `insertText` | Typing | Deletes selection (single or multi-block) then inserts |
| `deleteCharBefore` | Backspace | Deletes selection, or char before cursor, or joins with previous block |
| `deleteCharAfter` | Delete | Deletes selection, or char after cursor, or joins with next block |
| `deleteWordBefore` | Ctrl+Backspace | Deletes selection, or word before cursor, or joins with previous block |
| `deleteWordAfter` | Ctrl+Delete | Deletes selection, or word after cursor, or joins with next block |
| `deleteSelection` | — | Deletes selected range (single or multi-block) |
| `splitBlock` | Enter | Deletes selection if any, then splits block at cursor |
| `joinWithPrevious` | — | Merges with previous block (same type required) |
| `joinWithNext` | — | Merges with next block (same type required) |
| `selectAll` | Ctrl+A | Selects entire document (no steps, only sets selection) |
| `setBlockType` | — | Changes block type; range selection applies to all blocks in range |
| `indent` | Tab | Increments indent (max 5); range selection applies to all blocks in range |
| `outdent` | Shift+Tab | Decrements indent (min 0); range selection applies to all blocks in range |
| `moveBlockUp` | — | Moves block + indent-children above the sibling group above |
| `moveBlockDown` | — | Moves block + indent-children below the sibling group below |
| `insertBlockAfter` | — | Inserts an empty block after the cursor's block |
| `insertBlockBefore` | — | Inserts an empty block before the cursor's block |
| `deleteBlock` | — | Deletes the cursor's block (moves cursor to previous or next block) |
| `replaceBlock` | — | Replaces the cursor's block with a given block |
| `applyFormat` | — | Adds a `FormatType` to the selected range; multi-block applies per block |
| `removeFormat` | — | Removes a `FormatType` from the selected range; multi-block applies per block |
| `toggleFormat` | — | Toggles a `FormatType` on the selected range (adds if absent, removes if all formatted); multi-block |
| `applyLink` | — | Applies `FormatType.A` with a link URL on the selected range; multi-block |
| `removeLink` | — | Removes link formatting and URL metadata from the selected range; multi-block |
| `updateLink` | — | Updates the URL metadata on existing link formatting; multi-block |
| `clearFormatting` | — | Strips all formatting from the selected range (single or multi-block) |
| `duplicateBlock` | — | Inserts a clone of the cursor's block immediately after it |
| `toggleBlockType` | — | Toggles block type: sets target type if different, reverts to PARA if same |
| `setBlockMeta` | — | Sets or removes a metadata entry on the cursor's block |
| `extractSelection` | — | Read-only query; returns a `FormattedText` containing the selected content |
| `paste` | Ctrl+V | Inserts `FormattedText` at cursor; deletes selection first if present |
| `pasteText` | — | Converts plain text (splitting on `\n`) to `FormattedText` and delegates to `paste` |

### Multi-block delete strategy

When a selection spans from `(fromBlock, fromOffset)` to `(toBlock, toOffset)`, the transaction is built with steps ordered to keep block indices stable:

1. `ReplaceBlockStep(toBlock, rightPortion)` — truncate last block to content after `toOffset`
2. `DeleteBlockStep(i)` for intermediates in reverse order
3. `ReplaceBlockStep(fromBlock, leftPortion)` — truncate first block to content before `fromOffset`
4. `JoinBlocksStep(fromBlock)` — only if both endpoint blocks share the same type

### Multi-block property commands

`setBlockType`, `indent`, and `outdent` support range selections. When the selection spans multiple blocks, the step is applied to every block in `[fromBlock, toBlock]` that isn't already at the target value (or limit). These steps all produce `StepMap.EMPTY`, so multiple steps in one transaction don't cause index shifting. The command returns `null` if no block would change.

### Word-level deletion

`deleteWordBefore` and `deleteWordAfter` use word boundary helpers to find the deletion range. The helpers scan through characters using `isWordChar` (letters, digits, underscore):

- **At a word character**: scan through consecutive word characters to find the boundary.
- **At a non-word character**: scan through non-word characters, then through word characters.

At block boundaries (offset 0 or content end), the commands delegate to `joinWithPrevious` / `joinWithNext` respectively.

### Multi-block formatting and link commands

`applyFormat`, `removeFormat`, `toggleFormat`, `applyLink`, `removeLink`, and `updateLink` all support multi-block selections. When the selection spans blocks, the command iterates over each block in `[fromBlock, toBlock]`, computes the per-block character range (full content for middle blocks, partial for first/last), and adds a step per block. All these steps produce `StepMap.EMPTY`, so indices remain stable.

### Clear formatting

`clearFormatting` strips all formatting from the selected range. It uses the `stripFormattingInRange` helper, which handles four overlap cases between existing format entries and the strip range: no overlap (keep), fully contained (remove), spanning both ends (split into two entries), and partial overlap (trim). The helper operates on a cloned block to produce a `ReplaceBlockStep`.

### Link operations

`applyLink` applies `FormatType.A` via `ChangeFormatStep` and then sets `META_LINK` metadata on the affected format entries. `removeLink` clears the metadata first, then removes `FormatType.A` — this ordering ensures that empty format entries are fully cleaned up. `updateLink` modifies only the URL metadata without touching the format type.

Both `setLinkMetaOnRange` and `clearLinkMetaOnRange` defensively copy the format's meta map before mutating it. This prevents corruption of undo state caused by `FormattedLine.clone()` sharing meta maps by reference (see design considerations).

### Clipboard operations

`extractSelection` is a read-only query that returns a `FormattedText` containing the formatted content of the current selection. For single-block selections, it clones the block, splits twice to isolate the range, and wraps the result. For multi-block selections, it extracts the tail of the first block, clones middle blocks, and extracts the head of the last block. The original document is never modified.

`paste` inserts a `FormattedText` at the cursor position. If there is a range selection, it is deleted first. The command builds left and right remnants directly from the original document state — the left remnant is `fromBlock[0..fromOffset)` and the right remnant is `toBlock[toOffset..end)`. For single-block paste, the pasted content is merged between these remnants using `mergeBlockContent`. For multi-block paste, the first pasted block merges with the left remnant, the last pasted block merges with the right remnant, and middle blocks are inserted as-is.

`pasteText` converts a plain text string into a `FormattedText` (splitting on `\n` into separate PARA blocks) and delegates to `paste`.

### Indent-aware block moving

`moveBlockUp` and `moveBlockDown` treat a block and its indent-children (consecutive following blocks with strictly greater indent) as a group. The group swaps position with the adjacent sibling group. The sibling is found by walking backward (for up) or forward (for down) and skipping blocks at higher indent levels. Movement across parent boundaries (encountering a block at a lower indent) is rejected by returning `null`.

## Design considerations for maintainers

**Steps must be self-inverse-producing.** Every `Step.apply()` must return an inverse that, when applied to the mutated document, restores the original state exactly. The inverse also needs a correct StepMap (at minimum `StepMap.EMPTY` for steps that don't change positions).

**Block indices shift during multi-step transactions.** When building transactions with multiple steps that insert or delete blocks, the order of steps matters. Process from back to front (higher indices first) so that earlier indices remain valid. See `addDeleteRangeSteps` for the canonical pattern.

**JoinBlocksStep requires same block types.** `FormattedBlock.merge()` silently does nothing when types differ, but `JoinBlocksStep` still removes the second block. Commands must guard against mismatched types before adding a join step.

**Explicit selection is the primary cursor mechanism.** All `Commands` methods set explicit selection on the transaction. The StepMap-based mapping fallback in `EditorState.apply()` exists for cases where steps are added to a transaction without a corresponding `Commands` wrapper. If you add a new command, always set an explicit selection.

**ReplaceBlockStep captures a clone, not a reference.** Text-level steps clone the block before mutating it. The clone becomes the inverse's payload. This is critical because steps mutate in place — without cloning, the inverse would reference the already-mutated block.

**FormattedBlock.split(offset) mutates in place.** `split(n)` truncates the block to `[0..n)` and returns the `[n..end)` portion as a new block. It does not return a copy of the original. When building truncated blocks for multi-block delete, clone first, then split.

**FormattedLine.clone() shares format meta by reference.** The `Format` copy constructor copies the `meta` map reference, not the map contents. This means mutating a cloned format's metadata also mutates the original. Any code that modifies format metadata on a cloned block (e.g., link helpers) must defensively copy the meta map first: `fmt.setMeta(new HashMap<>(fmt.getMeta()))`. Failing to do so corrupts the undo state stored in `ReplaceBlockStep` inverses.

**Link removal order matters.** `removeLink` must clear `META_LINK` metadata before removing `FormatType.A`. The `removeFormat` implementation preserves format entries when `meta` is non-empty even if all format types are removed. Clearing metadata first ensures the entry is fully cleaned up.

**Step-less transactions are valid.** `selectAll` returns a transaction with no steps and only an explicit selection. This is a legitimate pattern — the caller decides whether to push to `History` (typically not, since there is nothing to undo).

**`mergeBlockContent` bypasses type restrictions.** `FormattedBlock.merge(other)` silently does nothing when block types differ, and `FormattedBlock.insert(start, blk)` has an incomplete implementation for internal insertion. The `mergeBlockContent` helper works around both by operating directly on lines: it merges the first source line into the target's last line via `FormattedLine.merge()` (which has no type restriction), then appends remaining source lines. Use this helper whenever type-agnostic block content concatenation is needed.

**Paste must not build blocks from pre-mutation state.** When paste deletes a selection before inserting, the replacement blocks must account for the deletion. Building blocks by cloning from `state.doc()` and then adding separate delete steps causes stale-state bugs — by the time the `ReplaceBlockStep` executes, earlier delete steps have already modified the document. The correct approach is to build the left and right remnants directly (via `clone` + `split`) and use `DeleteBlockStep` only for removing intermediate blocks, incorporating the deletion into the block construction itself.
