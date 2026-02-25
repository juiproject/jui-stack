# editor2 — Transaction-based rich text editor

`Editor` is a `SimpleComponent` that renders a `FormattedText` document as editable content. All mutations flow through the transaction system (`Commands`, `EditorState`, `History`) and the DOM is fully re-rendered after each transaction.

## Architecture

The editor intercepts all browser input via `beforeinput` events (calling `preventDefault()` on every one) and translates them into transactions. Keyboard shortcuts are handled via `keydown`. The DOM is never mutated by the browser — only by the editor's own `render()` method after applying a transaction. This "controlled contenteditable" approach guarantees the DOM always matches the model.

Selection is synchronised in two directions: DOM-to-state on `selectionchange` events (so the model knows where the cursor is before a command runs), and state-to-DOM after each render (so the cursor is restored to the correct position). The JS bridge (`EditorSupport2` / `jui_text_editor2.js`) handles the mapping between DOM nodes and block-level character offsets.

Every transaction triggers a full re-render (clear `innerHTML`, rebuild all blocks). For typical document sizes this is fast enough since DOM operations on a handful of elements are cheap. If it becomes a performance concern with large documents, an incremental render could diff previous and current block lists and only update changed blocks.

## Editing rules

### Text input

| Input | Behaviour |
|-------|-----------|
| Character | Inserts text at cursor. With a range selection, replaces the selection. |
| Backspace | Deletes one character before cursor. At a line break, joins the two lines. At offset 0 of an indented block, reduces indent by one level. At offset 0 of a non-indented list item, converts to paragraph (exits the list). At offset 0 of a non-indented, non-list block, joins with the previous block (regardless of type). With a range selection, deletes the selection. |
| Delete | Deletes one character after cursor. At a line break, joins the two lines. At end of block, joins with the next block (regardless of type — e.g. list item absorbs following paragraph). With a range selection, deletes the selection. |
| Ctrl+Backspace | Deletes the word before the cursor. At offset 0, joins with previous block. |
| Ctrl+Delete | Deletes the word after the cursor. At end of block, joins with next block. |

### Block operations

| Input | Behaviour |
|-------|-----------|
| Enter | Splits the block at the cursor. Special cases below. |
| Shift+Enter | Inserts a line break within the current block (new `FormattedLine`, same block). |
| Tab | Increases block indent by one level (max 5). |
| Shift+Tab | Decreases block indent by one level (min 0). |

### Enter special cases

| Context | Behaviour |
|---------|-----------|
| End of heading (H1/H2/H3) | New block becomes a paragraph (configurable via `paragraphAfterHeading(boolean)`, default `true`). |
| Empty list item (NLIST/OLIST) | Converts the block to a paragraph, exiting the list. |
| All other cases | Splits the block; the new block inherits the type of the original. |

### Formatting shortcuts

| Shortcut | Format |
|----------|--------|
| Ctrl+B | Bold |
| Ctrl+I | Italic |
| Ctrl+U | Underline |

These toggle the format on the selected range. With a cursor (no selection) they are no-ops.

### Undo / redo

| Shortcut | Action |
|----------|--------|
| Ctrl+Z | Undo |
| Ctrl+Shift+Z / Ctrl+Y | Redo |

### Clipboard

| Shortcut | Action |
|----------|--------|
| Ctrl+C | Copy — handled natively by the browser (DOM reflects model). |
| Ctrl+X | Cut — browser copies selection to clipboard; editor deletes selection via transaction. |
| Ctrl+V | Paste — plain text is read from the clipboard and inserted via `Commands.pasteText`. Multi-line text is split into separate PARA blocks. |

## Block types

| Type | Rendering | Toolbar |
|------|-----------|---------|
| PARA | `<p>` | Paragraph button |
| H1 | `<h1>` | H1 button |
| H2 | `<h2>` | H2 button |
| H3 | `<h3>` | H3 button |
| NLIST | `<p>` with bullet marker via CSS `::before` | Bullet list button (toggle) |
| OLIST | `<p>` with numbered marker via CSS `::before` and `data-list-index` | Numbered list button (toggle) |

List toolbar buttons use `toggleBlockType` — clicking when the block is already that list type converts it back to a paragraph.

Ordered list numbering is indent-aware. Each indent level maintains its own counter. Indenting to a deeper level restarts numbering at 1; outdenting resumes the counter at the shallower level. A non-OLIST block resets all counters. For example:

```
1. First item          (indent 0, numeric)
2. Second item         (indent 0)
   a. Sub-item A       (indent 1, alpha, restarts)
   b. Sub-item B       (indent 1)
3. Third item          (indent 0, continues)
   a. Sub-item C       (indent 1, alpha, restarts)
      i. Deep item     (indent 2, roman, restarts)
   b. Sub-item D       (indent 1, continues)
4. Fourth item         (indent 0, continues)
```

The marker style cycles by indent level: numeric (0), lowercase alpha (1), lowercase roman (2), then repeats. This is configurable via `listIndexFormatter(IListIndexFormatter)` — the formatter receives the indent level and counter value and returns the display string.

## Rendering details

Each `FormattedBlock` maps to a single DOM element with a `data-block-index` attribute. Within a block, lines are separated by `<br>` elements. Formatted text segments are wrapped in `<span>` elements with CSS classes (`fmt_bold`, `fmt_italic`, etc.). Links use `<a>` elements.

When the last line of a block is empty, a trailing `<br data-trailing="true">` is appended so the browser can position the cursor on the empty line. The JS support functions skip trailing BRs in character counting and offset resolution.

Block elements use `white-space: pre-wrap` to preserve whitespace faithfully. Without this, the browser collapses trailing spaces and consecutive spaces under the default `white-space: normal` rule.

## Contenteditable issues and resolutions

This section documents browser quirks encountered during development and how they were resolved. These are important to understand when modifying the rendering or event handling code.

### Trailing whitespace not visible

**Problem**: A space typed at the end of a line doesn't render — the browser collapses trailing whitespace in text nodes under `white-space: normal`.

**Resolution**: Block elements use `white-space: pre-wrap`, which preserves whitespace sequences and trailing spaces while still allowing line wrapping.

### Consecutive spaces collapsed

**Problem**: Typing a space after another space produces no visible effect — the browser collapses consecutive spaces under `white-space: normal`.

**Resolution**: Same as above — `white-space: pre-wrap` on block elements preserves all space characters.

### Cursor not visible after Shift+Enter at end of block

**Problem**: After inserting a line break at the end of a block (Shift+Enter), the cursor appears to stay in place. The DOM has `"text"<br>` but the browser has nowhere to position the cursor on the new empty line.

**Resolution**: When the last line of a block is empty, a trailing `<br data-trailing="true">` is appended after the separator `<br>`. The JS support functions (`_linesWalk`, `_resolvePosition`) are aware of this marker and skip it in character counting. `_resolvePosition` returns the position before the trailing BR so the cursor appears on the empty line.

### Backspace at line break within a block does nothing

**Problem**: Positioning the cursor at the start of the second line in a multi-line block and pressing Backspace has no effect. `DeleteTextStep` delegates to `FormattedBlock.remove()`, which cannot delete line breaks because they are implicit separators between `FormattedLine` objects (not characters in a line).

**Resolution**: `Commands.deleteCharBefore` checks whether the character at `offset - 1` is `'\n'` (via `charAt()`). If so, it calls `joinLinesAt()` instead of `DeleteTextStep`. `joinLinesAt()` clones the block, walks lines to find the break point, merges the two adjacent lines via `FormattedLine.merge()`, and replaces the block with `ReplaceBlockStep`. The same logic applies to `deleteCharAfter` for the Delete key.

### Enter at end of heading continues as heading

**Problem**: Pressing Enter at the end of an H1/H2/H3 block creates another heading block (the new block inherits the type from `SplitBlockStep`). The expected behaviour is that the new block becomes a paragraph.

**Resolution**: In `handleBeforeInput` for `insertParagraph`, after building the `splitBlock` transaction, the editor checks whether the cursor is at the end of a heading block. If so, it appends a `SetBlockTypeStep(blockIdx + 1, PARA)` to the same transaction (single undo unit). This is controlled by the `paragraphAfterHeading` flag (default `true`).

### Toolbar buttons steal focus from editor

**Problem**: Clicking a toolbar button moves focus away from the `contenteditable` element, causing the selection to be lost before the command can read it.

**Resolution**: Toolbar buttons use `mousedown` with `preventDefault()` instead of `click`. This prevents the browser from moving focus out of the editor. The button's action reads the current selection and applies the transaction while focus remains in the editor.

### Backspace/Delete at boundary between different block types does nothing

**Problem**: Pressing Backspace at offset 0 of a paragraph that follows a list item (or any different block type) has no effect. Similarly, pressing Delete at the end of a list item before a paragraph does nothing. `Commands.joinWithPrevious` and `joinWithNext` return `null` when the adjacent blocks have different types, because `JoinBlocksStep` requires same-type blocks.

**Resolution**: `Commands.forceJoinWithPrevious` and `forceJoinWithNext` handle cross-type joins by cloning the surviving block, merging the other block's content via `mergeBlockContent`, then replacing + deleting. Same-type joins still use the efficient `JoinBlocksStep`. The editor calls these force variants instead of `deleteCharBefore`/`deleteCharAfter` at block boundaries.

## Files

| File | Purpose |
|------|---------|
| `Editor.java` | Main component — rendering, event handling, toolbar, CSS |
| `EditorSupport2.java` | JsInterop bridge for selection read/write and input event helpers |
| `jui_text_editor2.js` | Native JS — leaf traversal, line parsing, character counting, offset resolution, selection read/set |

## Public API

```java
Editor editor = new Editor();
editor.paragraphAfterHeading(true);  // default
editor.listIndexFormatter((indent, counter) -> String.valueOf(counter)); // all numeric
editor.load(document);               // load a FormattedText
FormattedText result = editor.value(); // retrieve current document
```
