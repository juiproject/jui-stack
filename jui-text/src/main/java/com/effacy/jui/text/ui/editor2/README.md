# Package

`Editor` is a `SimpleComponent` that renders a `FormattedText` document as editable content. All mutations flow through the transaction system (`Commands`, `EditorState`, `History`) and the DOM is fully re-rendered after each transaction.

## Architecture

The editor intercepts all browser input via `beforeinput` events (calling `preventDefault()` on every one) and translates them into transactions. Keyboard shortcuts are handled via `keydown`. The DOM is never mutated by the browser — only by the editor's own `render()` method after applying a transaction. This "controlled contenteditable" approach guarantees the DOM always matches the model.

Selection is synchronised in two directions: DOM-to-state on `selectionchange` events (so the model knows where the cursor is before a command runs), and state-to-DOM after each render (so the cursor is restored to the correct position). The JS bridge (`EditorSupport2` / `jui_text_editor2.js`) handles the mapping between DOM nodes and block-level character offsets.

Every transaction triggers a full re-render (clear `innerHTML`, rebuild all blocks). For typical document sizes this is fast enough since DOM operations on a handful of elements are cheap. If it becomes a performance concern with large documents, an incremental render could diff previous and current block lists and only update changed blocks.

### Block handler registry

Rendering and event handling for each block family is delegated to a pluggable `IBlockHandler`. The editor maintains an ordered list of handlers; for every operation it iterates the list and delegates to the first handler whose `accepts(BlockType)` returns `true`. This allows new block types to be added without modifying `Editor` itself.

`StandardBlockHandler` covers paragraph, heading, and list types. `TableBlockHandler` covers `TABLE` blocks. New block types are registered in `Editor`'s constructor via `handlers.add(...)`. Each handler can override only the lifecycle methods it needs: `beginRender`, `render`, `afterRender`, `beforeApplyTransaction`, `handleKeyDown`, `handleBeforeInput`, `handlePaste`, `handleFormatToggle`, and `focusBlock`.

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

### Table cell navigation

When focus is inside a table cell, keydown is consumed by `TableBlockHandler` and does not reach the editor's standard key processing.

| Input | Behaviour |
|-------|-----------|
| Enter | Move focus to the same column in the next row. No-op in the last row. |
| Tab | Move focus to the next cell (wraps to next row). No-op after last cell in last row. |
| Shift+Tab | Move focus to the previous cell (wraps to previous row). No-op before first cell. |
| ArrowDown / ArrowUp | Move focus to the same column in the next / previous row. |
| ArrowRight | Move focus to the next cell only when the cursor is at the end of the cell's text. |
| ArrowLeft | Move focus to the previous cell only when the cursor is at the start of the cell's text. |
| Ctrl+B / I / U | Toggle bold / italic / underline on the selection within the cell. |

### Column resizing

Each column border (except the rightmost) has an invisible 6 px drag handle positioned over the cell border. Dragging redistributes width between the two adjacent columns while keeping the table at full container width. Widths are stored as integer percentages summing to 100 in `meta("colwidths")` (e.g. `"33,33,34"`). The minimum column width is 5 %. Releasing the mouse persists the new widths to the model via a silent transaction (undo-able).

## Block types

| Type | Rendering | Toolbar |
|------|-----------|---------|
| PARA | `<p>` | Paragraph button |
| H1 | `<h1>` | H1 button |
| H2 | `<h2>` | H2 button |
| H3 | `<h3>` | H3 button |
| NLIST | `<p>` with bullet marker via CSS `::before` | Bullet list button (toggle) |
| OLIST | `<p>` with numbered marker via CSS `::before` and `data-list-index` | Numbered list button (toggle) |
| TABLE | `<div>` wrapper containing `<table>` with per-cell contenteditable inner divs | Insert table button |

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

### Table rendering

A TABLE block renders as:

```
<div class="tableWrapper" contenteditable="false" data-block-index="N">
  <table class="table" style="table-layout: fixed">
    <colgroup>
      <col data-col-index="0" style="width: 33%">
      ...
    </colgroup>
    <tr>
      <td class="tableCell">
        <div class="tableCellContent" contenteditable="true"
             data-table-index="N" data-row="R" data-col="C">
          [content]
        </div>
        <div class="colResizeHandle"/>   <!-- last column omitted -->
      </td>
      ...
    </tr>
    ...
  </table>
  <div class="tableAddCol">+</div>
  <div class="tableAddRow">+</div>
</div>
```

The outer wrapper has `contenteditable="false"` so the editor's own `beforeinput` / `selectionchange` logic ignores it. Each cell has an inner `tableCellContent` div that is the actual `contenteditable="true"` element, carrying `data-table-index`, `data-row`, and `data-col` attributes. The resize handle is a sibling of this div, outside the contenteditable scope. Column widths use `<col>` elements under a `<colgroup>` so `table-layout: fixed` respects the explicit percentages.

## Toolbar and tools

The toolbar is decoupled from the editor via the `ITool` interface. Each tool is a stateless descriptor that renders a button into the toolbar and optionally returns a `Handle` for tracking active state. Standard tools are available as constants on `Tools` and custom tools can be created by implementing `ITool` directly or using the factory methods on `Tools`.

### Architecture

`ITool` instances are stateless — safe as `static final` constants. When the toolbar renders, it calls `tool.render(ctx)` on each tool, passing an `ITool.Context` that provides:

- `parent()` — the DOM container to insert elements into
- `commands()` — the `IEditorCommands` interface for driving the editor (may be `null` during initial render, so action handlers must read this lazily at click time)
- `styles()` — CSS classes for toolbar buttons (`tbtn`, `tbtnActive`, `tbtnSep`)

The returned `Handle` (or `null` for stateless tools like separators) receives `updateState(BlockType, Set<FormatType>)` and `updateCellState(Set<FormatType>)` callbacks whenever the editor selection changes, allowing the button to toggle its active appearance.

Popup-based tools (link, variable) use `ToolPopupPanel` as a shared base for floating panels. The base class handles singleton tracking (at most one popup open at a time), fixed positioning below an anchor element, outside-click dismissal, and cleanup. Subclasses implement `buildContent(Element)` and optionally override `onShown()`.

### Standard tools

`Tools` provides ready-to-use constants:

| Constant | Description |
|----------|-------------|
| `BOLD`, `ITALIC`, `UNDERLINE`, `STRIKETHROUGH`, `SUBSCRIPT`, `SUPERSCRIPT`, `CODE`, `HIGHLIGHT` | Inline format toggles |
| `H1`, `H2`, `H3`, `PARAGRAPH` | Block type setters |
| `BULLET_LIST`, `NUMBERED_LIST` | Block type toggles |
| `TABLE` | Inserts a 2x3 table |
| `SEPARATOR` | Visual divider between tool groups |

Link and variable tools require a data source and are created via factory methods rather than constants:

```java
Tools.link(r -> Em.$(r).style(FontAwesome.link()), "Link", MyApp::filterLinks)
Tools.variable("{}", "Variable", MyApp::filterVariables)
```

### Configuration

Tools are configured on `EditorToolbar.Config`. When not configured, `Tools.all()` is used as the default (excludes link and variable since they need a data source).

```java
new FormattedTextEditor(new FormattedTextEditor.Config()
    .editor(new Editor.Config())
    .toolbar(new EditorToolbar.Config()
        .tools(Tools.BOLD, Tools.ITALIC, Tools.UNDERLINE,
               Tools.SEPARATOR,
               Tools.H1, Tools.H2, Tools.H3,
               Tools.SEPARATOR,
               Tools.link("Link", "Link", MyApp::filterLinks),
               Tools.SEPARATOR,
               Tools.variable("{}", "Variable", MyApp::filterVariables))));
```

### Creating custom tools

#### Using factory methods

`Tools` provides factory methods for the common patterns: format toggles, block type setters/toggles, and stateless actions. Each has a `String` label overload and a `Consumer<ElementBuilder>` overload for custom button content (e.g. FontAwesome icons).

A stateless action tool that inserts a 4x4 table:

```java
ITool bigTable = Tools.action(
    r -> Em.$(r).style(FontAwesome.tableColumns()),
    "Insert 4x4 Table",
    cmd -> cmd.insertTable(4, 4));
```

A tool that applies both bold and italic:

```java
ITool boldItalic = Tools.action("B/I", "Bold + Italic", cmd -> {
    cmd.toggleFormat(FormatType.BLD);
    cmd.toggleFormat(FormatType.ITL);
});
```

#### Implementing ITool directly

For tools that need active-state tracking or custom behaviour beyond what the factory methods offer, implement `ITool` directly. The `render` method receives the toolbar context and returns a `Handle`.

A tool that inserts random placeholder text:

```java
ITool loremIpsum = ctx -> {
    Button.$(ctx.parent())
        .style(ctx.styles().tbtn())
        .text("Lorem")
        .attr("title", "Insert placeholder text")
        .on(e -> {
            e.stopEvent();
            if (ctx.commands() != null)
                ctx.commands().insertText("Lorem ipsum dolor sit amet.");
        }, UIEventType.ONMOUSEDOWN);
    return null; // stateless — no active state to track
};
```

A tool with active-state tracking (highlights when a specific format is active):

```java
ITool customFormat = ctx -> {
    Element[] btn = new Element[1];
    Button.$(ctx.parent())
        .style(ctx.styles().tbtn())
        .text("HL")
        .attr("title", "Highlight")
        .use(n -> btn[0] = (Element) n)
        .on(e -> {
            e.stopEvent();
            if (ctx.commands() != null)
                ctx.commands().toggleFormat(FormatType.HL);
        }, UIEventType.ONMOUSEDOWN);
    return new ITool.Handle() {

        @Override
        public void updateState(BlockType activeBlockType, Set<FormatType> activeFormats) {
            if (activeFormats.contains(FormatType.HL))
                btn[0].classList.add(ctx.styles().tbtnActive());
            else
                btn[0].classList.remove(ctx.styles().tbtnActive());
        }

        @Override
        public void updateCellState(Set<FormatType> activeFormats) {
            updateState(null, activeFormats);
        }
    };
};
```

### IEditorCommands

The `IEditorCommands` interface is how tools drive the editor. All commands operate on the editor's current selection.

| Method | Description |
|--------|-------------|
| `toggleFormat(FormatType)` | Toggles an inline format on the selection |
| `setBlockType(BlockType)` | Sets the block type of the current block |
| `toggleBlockType(BlockType)` | Toggles a block type (e.g. list on/off) |
| `insertTable(int rows, int cols)` | Inserts a table after the current block |
| `insertText(String text)` | Inserts text at the cursor, replacing any selection |
| `syncSelection()` | Freezes the DOM selection into the editor's internal state — call before opening popups |
| `currentLink()` | Returns the link URL at the cursor, or `null` |
| `applyLink(String url)` | Applies a link URL to the current range selection |
| `removeLink()` | Removes the link from the current selection |
| `applyVariable(String name, String label)` | Inserts a variable at the cursor |

## Files

| File | Purpose |
|------|---------|
| `Editor.java` | Main component — rendering, event handling, CSS |
| `IBlockHandler.java` | Pluggable block handler interface — render, event hooks, focus |
| `StandardBlockHandler.java` | Handler for PARA, H1–H3, NLIST, OLIST block types |
| `TableBlockHandler.java` | Handler for TABLE blocks — cell editing, column resizing, CSS |
| `IEditorContext.java` | Context passed to block handlers — editor element, state, transaction helpers |
| `EditorSupport2.java` | JsInterop bridge for selection read/write and input event helpers |
| `jui_text_editor2.js` | Native JS — leaf traversal, line parsing, character counting, offset resolution, selection read/set, cell helpers |
| `ITool.java` | Tool contract — stateless descriptor with `render(Context)` returning a `Handle` |
| `Tools.java` | Standard tool constants and factory methods |
| `IEditorCommands.java` | Command interface through which tools drive the editor |
| `EditorToolbar.java` | Default toolbar component — renders `ITool` instances, manages active state |
| `IEditorToolbar.java` | Toolbar contract — `bind`, `updateState`, `updateCellState`, `Position` |
| `FormattedTextEditor.java` | JUI control composing `Editor` + `IEditorToolbar` with value management |
| `ToolPopupPanel.java` | Shared base for floating popup panels — positioning, dismiss, singleton tracking |
| `LinkPanel.java` | Popup panel for applying/editing/removing links |
| `VariablePanel.java` | Popup panel for selecting and inserting variables |

## Public API

```java
// Minimal setup with default tools (no link/variable)
FormattedTextEditor editor = new FormattedTextEditor(new FormattedTextEditor.Config()
    .editor(new Editor.Config())
    .toolbar(new EditorToolbar.Config()));
editor.setValue(Value.of(myDocument));
FormattedText result = editor.getValue();

// Custom tool selection with link and variable support
FormattedTextEditor editor = new FormattedTextEditor(new FormattedTextEditor.Config()
    .editor(new Editor.Config())
    .toolbar(new EditorToolbar.Config()
        .tools(Tools.BOLD, Tools.ITALIC, Tools.UNDERLINE,
               Tools.SEPARATOR,
               Tools.H1, Tools.H2, Tools.H3,
               Tools.SEPARATOR,
               Tools.TABLE,
               Tools.SEPARATOR,
               Tools.link(r -> Em.$(r).style(FontAwesome.link()), "Link", MyApp::filterLinks),
               Tools.SEPARATOR,
               Tools.variable("{}", "Variable", MyApp::filterVariables))));
```

# Appendix

## Contenteditable issues and resolutions

This section documents browser quirks encountered during development and how they were resolved. These are important to understand when modifying the rendering or event handling code.

|Problem|Description|Resolution|
|-------|-------|----------|
|Trailing whitespace not visible|A space typed at the end of a line doesn't render — the browser collapses trailing whitespace in text nodes under `white-space: normal`.|Block elements use `white-space: pre-wrap`, which preserves whitespace sequences and trailing spaces while still allowing line wrapping.|
|Consecutive spaces collapsed|Typing a space after another space produces no visible effect — the browser collapses consecutive spaces under `white-space: normal`.|Same as above — `white-space: pre-wrap` on block elements preserves all space characters.|
|Cursor not visible after Shift+Enter at end of block|After inserting a line break at the end of a block (Shift+Enter), the cursor appears to stay in place. The DOM has `"text"<br>` but the browser has nowhere to position the cursor on the new empty line.|When the last line of a block is empty, a trailing `<br data-trailing="true">` is appended after the separator `<br>`. The JS support functions (`_linesWalk`, `_resolvePosition`) are aware of this marker and skip it in character counting. `_resolvePosition` returns the position before the trailing BR so the cursor appears on the empty line.|
|Backspace at line break within a block does nothing|Positioning the cursor at the start of the second line in a multi-line block and pressing Backspace has no effect. `DeleteTextStep` delegates to `FormattedBlock.remove()`, which cannot delete line breaks because they are implicit separators between `FormattedLine` objects (not characters in a line).|`Commands.deleteCharBefore` checks whether the character at `offset - 1` is `'\n'` (via `charAt()`). If so, it calls `joinLinesAt()` instead of `DeleteTextStep`. `joinLinesAt()` clones the block, walks lines to find the break point, merges the two adjacent lines via `FormattedLine.merge()`, and replaces the block with `ReplaceBlockStep`. The same logic applies to `deleteCharAfter` for the Delete key.|
|Enter at end of heading continues as heading|Pressing Enter at the end of an H1/H2/H3 block creates another heading block (the new block inherits the type from `SplitBlockStep`). The expected behaviour is that the new block becomes a paragraph.|In `handleBeforeInput` for `insertParagraph`, after building the `splitBlock` transaction, the editor checks whether the cursor is at the end of a heading block. If so, it appends a `SetBlockTypeStep(blockIdx + 1, PARA)` to the same transaction (single undo unit). This is controlled by the `paragraphAfterHeading` flag (default `true`).|
|Toolbar buttons steal focus from editor|Clicking a toolbar button moves focus away from the `contenteditable` element, causing the selection to be lost before the command can read it.|Toolbar buttons use `mousedown` with `preventDefault()` instead of `click`. This prevents the browser from moving focus out of the editor. The button's action reads the current selection and applies the transaction while focus remains in the editor.|
|Backspace/Delete at boundary between different block types does nothing|Pressing Backspace at offset 0 of a paragraph that follows a list item (or any different block type) has no effect. Similarly, pressing Delete at the end of a list item before a paragraph does nothing. `Commands.joinWithPrevious` and `joinWithNext` return `null` when the adjacent blocks have different types, because `JoinBlocksStep` requires same-type blocks.|`Commands.forceJoinWithPrevious` and `forceJoinWithNext` handle cross-type joins by cloning the surviving block, merging the other block's content via `mergeBlockContent`, then replacing + deleting. Same-type joins still use the efficient `JoinBlocksStep`. The editor calls these force variants instead of `deleteCharBefore`/`deleteCharAfter` at block boundaries.|
|Table cell resize handle causes double-height empty rows|Placing an absolutely-positioned element (the column resize handle) inside a `contenteditable` `<td>` causes empty cells to render at double line-height in Chrome. Chrome adds an implicit cursor node after non-editable children, and the combination of a `<br>` cursor anchor with an absolutely-positioned sibling inflates the row height.|The `<td>` is no longer `contenteditable`. Instead each cell contains an inner `<div class="tableCellContent" contenteditable="true">` for editable content and a sibling `<div class="colResizeHandle">` for the drag handle. The handle is outside the contenteditable scope so browser normalisation never interacts with it. This mirrors Notion's cell DOM structure. `EditorSupport2.cellFromSelection` was updated to match the inner div (identified by `contenteditable="true"` and `data-table-index`) rather than `TD/TH` elements.|