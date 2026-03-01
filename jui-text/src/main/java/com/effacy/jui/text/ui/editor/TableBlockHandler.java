package com.effacy.jui.text.ui.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.text.type.FormattedBlock;
import com.effacy.jui.text.type.FormattedBlock.BlockType;
import com.effacy.jui.text.type.FormattedLine;
import com.effacy.jui.text.type.FormattedLine.FormatType;
import com.effacy.jui.text.type.edit.Commands;
import com.effacy.jui.text.type.edit.Transaction;
import com.effacy.jui.text.type.edit.step.ReplaceBlockStep;
import com.google.gwt.core.client.GWT;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.KeyboardEvent;
import elemental2.dom.MouseEvent;
import jsinterop.base.Js;

/**
 * Handles {@link BlockType#TABLE} blocks.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Rendering the table wrapper, {@code <table>}, rows, and cells including
 *       add-row / add-column hover controls.</li>
 *   <li>Making each cell {@code contenteditable} and attaching focus, blur, and
 *       keydown listeners for cell-level editing and navigation.</li>
 *   <li>Guarding the editor's {@code beforeinput} and {@code paste} event
 *       handlers so browser-native input is allowed inside cells.</li>
 *   <li>Inline format toggling (bold, italic, underline, etc.) within cells via
 *       the toolbar or keyboard shortcuts.</li>
 *   <li>Syncing cell DOM content back to the model on blur and before
 *       structural transactions (add row / add column).</li>
 *   <li>Re-focusing the last active cell after a full re-render.</li>
 * </ul>
 */
public class TableBlockHandler implements IBlockHandler {

    /************************************************************************
     * Cell focus state.
     ************************************************************************/

    /**
     * Block index of the currently focused table, or {@code -1} when no cell
     * is focused.
     */
    private int focusedTableIndex = -1;

    /**
     * Row index of the currently focused cell.
     */
    private int focusedTableRow = -1;

    /**
     * Column index of the currently focused cell.
     */
    private int focusedTableCol = -1;

    /**
     * {@code textContent} of the focused cell at focus time. Used on blur to
     * detect whether the cell's plain text changed and needs to be synced.
     */
    private String focusedCellInitialContent = null;

    /************************************************************************
     * Column resize state.
     ************************************************************************/

    private int resizeTableIndex = -1;
    private int resizeColIndex = -1;
    private double resizeStartX = 0;
    private double resizeTableWidth = 0;
    private double resizeOrigLeft = 0;
    private double resizeOrigRight = 0;
    private int[] resizeColWidths = null;
    private elemental2.dom.Element resizeTableEl = null;
    private IEditorContext resizeCtx = null;
    private elemental2.dom.EventListener resizeMoveListener = null;
    private elemental2.dom.EventListener resizeEndListener = null;

    /************************************************************************
     * IBlockHandler.
     ************************************************************************/

    @Override
    public boolean accepts(BlockType type) {
        return type == BlockType.TABLE;
    }

    @Override
    public Element render(FormattedBlock block, int blockIndex, IEditorContext ctx) {
        return renderTable(block, blockIndex, ctx);
    }

    @Override
    public void afterRender(IEditorContext ctx) {
        // Re-focus the cell that was active before the re-render.
        if (focusedTableIndex >= 0)
            focusCell(focusedTableIndex, focusedTableRow, focusedTableCol, false, ctx);
    }

    @Override
    public void beforeApplyTransaction(IEditorContext ctx) {
        syncFocusedCell(ctx);
    }

    @Override
    public boolean handleKeyDown(KeyboardEvent ke, IEditorContext ctx) {
        // Cell keydown listeners call ke.stopPropagation(), so cell keys
        // never reach here. This guard is a defensive backstop only.
        return isInsideTableCell(ke.target, ctx);
    }

    @Override
    public boolean handleBeforeInput(elemental2.dom.Event evt, IEditorContext ctx) {
        // Let the browser handle input inside contenteditable cells natively.
        return isInsideTableCell(evt.target, ctx);
    }

    @Override
    public boolean handlePaste(elemental2.dom.Event evt, IEditorContext ctx) {
        // Let the browser handle paste inside contenteditable cells natively.
        return isInsideTableCell(evt.target, ctx);
    }

    @Override
    public boolean handleSelectionChange(IEditorContext ctx) {
        elemental2.dom.Element cellEl = Js.uncheckedCast(EditorSupport.cellFromSelection(ctx.editorEl()));
        if (cellEl == null)
            return false;
        ctx.notifyCellSelection(computeCellFormatState(cellEl, ctx));
        return true;
    }

    @Override
    public void focusBlock(int blockIndex, IEditorContext ctx) {
        focusCell(blockIndex, 0, 0, true, ctx);
    }

    @Override
    public boolean handleFormatToggle(FormatType type, IEditorContext ctx) {
        elemental2.dom.Element cellEl = Js.uncheckedCast(
                EditorSupport.cellFromSelection(ctx.editorEl()));
        if (cellEl == null)
            return false;
        applyToggleFormatInCellEl(type, cellEl, ctx);
        return true;
    }

    /************************************************************************
     * Table rendering.
     ************************************************************************/

    /**
     * Renders a TABLE block as a wrapper containing an editable {@code <table>}
     * and hover controls for adding rows/columns. Each cell is contenteditable
     * and has focus/blur/keydown listeners for cell-level editing and navigation.
     */
    private Element renderTable(FormattedBlock table, int index, IEditorContext ctx) {
        Element wrapper = DomGlobal.document.createElement("div");
        wrapper.classList.add(styles().tableWrapper());
        wrapper.setAttribute("contenteditable", "false");
        wrapper.setAttribute("data-block-index", String.valueOf(index));

        // Read metadata.
        int headers = 0;
        String headersStr = table.meta("headers");
        if (headersStr != null) {
            try {
                headers = Integer.parseInt(headersStr);
            } catch (NumberFormatException e) {
                // Ignore.
            }
        }
        String alignStr = table.meta("align");
        String[] align = (alignStr != null) ? alignStr.split(",") : null;

        // Count rows and columns for navigation bounds.
        int numRows = 0;
        int numCols = 0;
        for (FormattedBlock row : table.getBlocks()) {
            if (row.getType() != BlockType.TROW)
                continue;
            numRows++;
            int c = 0;
            for (FormattedBlock cell : row.getBlocks()) {
                if (cell.getType() == BlockType.TCELL)
                    c++;
            }
            if (c > numCols)
                numCols = c;
        }

        // Parse column widths.
        int[] colWidths = parseColWidths(table.meta("colwidths"), numCols);

        // Build <table>.
        final int fNumRows = numRows;
        final int fNumCols = numCols;
        Element tableEl = DomGlobal.document.createElement("table");
        tableEl.classList.add(styles().table());
        ((elemental2.dom.HTMLElement) tableEl).style.setProperty("table-layout", "fixed");

        // Add <colgroup> to set explicit column widths.
        Element colgroup = DomGlobal.document.createElement("colgroup");
        for (int i = 0; i < numCols; i++) {
            Element col = DomGlobal.document.createElement("col");
            col.setAttribute("data-col-index", String.valueOf(i));
            ((elemental2.dom.HTMLElement) col).style.setProperty("width", colWidths[i] + "%");
            colgroup.appendChild(col);
        }
        tableEl.appendChild(colgroup);

        int rowIndex = 0;
        for (FormattedBlock row : table.getBlocks()) {
            if (row.getType() != BlockType.TROW)
                continue;
            Element tr = DomGlobal.document.createElement("tr");
            int cellIndex = 0;
            for (FormattedBlock cell : row.getBlocks()) {
                if (cell.getType() != BlockType.TCELL)
                    continue;
                boolean isHeader = (rowIndex < headers);
                Element td = DomGlobal.document.createElement(isHeader ? "th" : "td");
                td.classList.add(styles().tableCell());
                if ((align != null) && (cellIndex < align.length)) {
                    if ("C".equals(align[cellIndex].trim()))
                        ((elemental2.dom.HTMLElement) td).style.set("text-align", "center");
                    else if ("R".equals(align[cellIndex].trim()))
                        ((elemental2.dom.HTMLElement) td).style.set("text-align", "right");
                }

                // Inner contenteditable div — separates editable content from the
                // absolutely-positioned resize handle, which prevents the handle from
                // interfering with the cursor, causing double-height empty rows, or
                // accidentally receiving typed content (Notion uses the same pattern).
                Element contentDiv = DomGlobal.document.createElement("div");
                contentDiv.classList.add(styles().tableCellContent());
                contentDiv.setAttribute("contenteditable", "true");
                contentDiv.setAttribute("data-table-index", String.valueOf(index));
                contentDiv.setAttribute("data-row", String.valueOf(rowIndex));
                contentDiv.setAttribute("data-col", String.valueOf(cellIndex));

                // Render cell content (single line only — cells are paragraph-like).
                List<FormattedLine> lines = cell.getLines();
                boolean hasText = ((lines != null) && !lines.isEmpty()) && (lines.get(0).length() > 0);
                if (hasText)
                    ctx.renderLine(contentDiv, lines.get(0));

                // Cell event listeners on the inner content div.
                final int capturedRow = rowIndex;
                final int capturedCol = cellIndex;
                contentDiv.addEventListener("focus", evt -> {
                    focusedTableIndex = index;
                    focusedTableRow = capturedRow;
                    focusedTableCol = capturedCol;
                    focusedCellInitialContent = ((elemental2.dom.HTMLElement) evt.target).textContent;
                });
                contentDiv.addEventListener("blur", evt -> {
                    syncCellToModel(index, capturedRow, capturedCol,
                            (elemental2.dom.Element) Js.uncheckedCast(evt.target), ctx);
                    focusedTableIndex = -1;
                    focusedTableRow = -1;
                    focusedTableCol = -1;
                });
                contentDiv.addEventListener("keydown", evt -> {
                    handleCellKeyDown(Js.uncheckedCast(evt), index, capturedRow, capturedCol,
                            fNumRows, fNumCols, ctx);
                });
                td.appendChild(contentDiv);

                // Resize handle — sibling of the content div, outside contenteditable.
                if (cellIndex < (numCols - 1)) {
                    Element handle = DomGlobal.document.createElement("div");
                    handle.classList.add(styles().colResizeHandle());
                    final int capturedColForHandle = cellIndex;
                    handle.addEventListener("mousedown", hevt -> {
                        hevt.preventDefault();
                        hevt.stopPropagation();
                        MouseEvent me = Js.uncheckedCast(hevt);
                        int[] currentWidths = readColWidths(tableEl, fNumCols);
                        startColResize(index, capturedColForHandle, me.clientX, tableEl, currentWidths, ctx);
                    });
                    td.appendChild(handle);
                }

                // Row handle — left side of cell; reveals on hover; click opens row context menu.
                Element rowHandle = DomGlobal.document.createElement("div");
                rowHandle.classList.add(styles().tableRowHandle());
                rowHandle.addEventListener("mousedown", hevt -> {
                    hevt.preventDefault();
                    hevt.stopPropagation();
                    showRowMenu(rowHandle, index, capturedRow, ctx);
                });
                td.appendChild(rowHandle);

                // Column handle — top of cell; reveals on hover; click opens column context menu.
                Element colHandle = DomGlobal.document.createElement("div");
                colHandle.classList.add(styles().tableColHandle());
                colHandle.addEventListener("mousedown", hevt -> {
                    hevt.preventDefault();
                    hevt.stopPropagation();
                    showColMenu(colHandle, index, capturedCol, ctx);
                });
                td.appendChild(colHandle);

                tr.appendChild(td);
                cellIndex++;
            }
            tableEl.appendChild(tr);
            rowIndex++;
        }
        wrapper.appendChild(tableEl);

        // Add column control (right edge).
        Element addCol = DomGlobal.document.createElement("div");
        addCol.classList.add(styles().tableAddCol());
        addCol.textContent = "+";
        addCol.addEventListener("mousedown", evt -> {
            evt.preventDefault();
            evt.stopPropagation();
            ctx.applyTransaction(Commands.addTableColumn(ctx.state(), index));
        });
        wrapper.appendChild(addCol);

        // Add row control (bottom edge).
        Element addRow = DomGlobal.document.createElement("div");
        addRow.classList.add(styles().tableAddRow());
        addRow.textContent = "+";
        addRow.addEventListener("mousedown", evt -> {
            evt.preventDefault();
            evt.stopPropagation();
            ctx.applyTransaction(Commands.addTableRow(ctx.state(), index));
        });
        wrapper.appendChild(addRow);

        return wrapper;
    }

    /************************************************************************
     * Column width helpers.
     ************************************************************************/

    /**
     * Returns an array of equal integer column widths (percentages summing to
     * 100) distributed across {@code cols} columns. Any remainder is added to
     * the last column.
     */
    private static int[] equalColWidths(int cols) {
        int base = 100 / cols;
        int remainder = 100 - base * cols;
        int[] widths = new int[cols];
        for (int i = 0; i < cols; i++)
            widths[i] = (i < (cols - 1)) ? base : (base + remainder);
        return widths;
    }

    /**
     * Parses the {@code "colwidths"} metadata string into an array of integer
     * percentages. Falls back to equal widths for any missing or malformed
     * entries.
     */
    private static int[] parseColWidths(String meta, int numCols) {
        int[] widths = equalColWidths(numCols);
        if (meta == null)
            return widths;
        String[] parts = meta.split(",");
        for (int i = 0; (i < parts.length) && (i < numCols); i++) {
            try {
                widths[i] = Integer.parseInt(parts[i].trim());
            } catch (NumberFormatException e) {
                // keep default
            }
        }
        return widths;
    }

    /**
     * Reads the current column widths from the {@code <col>} elements in the
     * given table element. Used at drag-start so dragging always works from the
     * live DOM state.
     */
    private static int[] readColWidths(Element tableEl, int numCols) {
        int[] widths = equalColWidths(numCols);
        for (int i = 0; i < numCols; i++) {
            Element col = tableEl.querySelector("col[data-col-index='" + i + "']");
            if (col == null)
                continue;
            String w = ((elemental2.dom.HTMLElement) col).style.getPropertyValue("width");
            if ((w != null) && w.endsWith("%")) {
                try {
                    // Parse as double to handle float-precision values written by
                    // handleResizeMove (e.g. "30.456%"); round to nearest integer.
                    widths[i] = (int) Math.round(Double.parseDouble(w.substring(0, w.length() - 1).trim()));
                } catch (NumberFormatException e) {
                    // keep default
                }
            }
        }
        return widths;
    }

    /**
     * Converts a width array to the comma-separated metadata string stored in
     * {@code meta("colwidths")}.
     */
    private static String joinColWidths(int[] widths) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < widths.length; i++) {
            if (i > 0)
                sb.append(',');
            sb.append(widths[i]);
        }
        return sb.toString();
    }

    /************************************************************************
     * Column resize drag logic.
     ************************************************************************/

    /**
     * Begins a column resize drag. Captures drag state and attaches
     * document-level {@code mousemove} / {@code mouseup} listeners.
     */
    private void startColResize(int tableIndex, int colIndex, double startX,
            Element tableEl, int[] widths, IEditorContext ctx) {
        resizeTableIndex = tableIndex;
        resizeColIndex   = colIndex;
        resizeStartX     = startX;
        resizeTableWidth = ((elemental2.dom.HTMLElement) tableEl).offsetWidth;
        resizeOrigLeft   = widths[colIndex];
        resizeOrigRight  = widths[colIndex + 1];
        resizeTableEl    = tableEl;
        resizeCtx        = ctx;
        resizeColWidths  = Arrays.copyOf(widths, widths.length);

        // Blur any focused contenteditable cell so text-cursor flicker cannot
        // interfere with the drag gesture.
        elemental2.dom.HTMLElement activeEl = Js.uncheckedCast(DomGlobal.document.activeElement);
        if (activeEl != null)
            activeEl.blur();

        // Suppress text selection during drag.
        elemental2.dom.HTMLElement body = Js.uncheckedCast(DomGlobal.document.body);
        body.style.setProperty("user-select", "none");
        body.style.setProperty("-webkit-user-select", "none");

        resizeMoveListener = e -> handleResizeMove(Js.uncheckedCast(e));
        resizeEndListener  = e -> endColResize();
        DomGlobal.document.addEventListener("mousemove", resizeMoveListener);
        DomGlobal.document.addEventListener("mouseup",   resizeEndListener);
    }

    /**
     * Handles {@code mousemove} during a column resize drag. Redistributes
     * width between the left and right adjacent columns and updates the DOM
     * immediately.
     */
    private void handleResizeMove(MouseEvent me) {
        if (resizeTableWidth <= 0)
            return;
        double minPct   = 5.0;
        double total    = resizeOrigLeft + resizeOrigRight;
        double deltaPct = ((me.clientX - resizeStartX) / resizeTableWidth) * 100.0;
        double newLeft  = Math.max(minPct, Math.min(total - minPct, resizeOrigLeft + deltaPct));
        double newRight = total - newLeft;

        // Apply exact float % to CSS so the handle tracks the pointer without a
        // 1% jump threshold (rounding only happens at mouseup for model storage).
        Element colL = resizeTableEl.querySelector(
                "col[data-col-index='" + resizeColIndex + "']");
        Element colR = resizeTableEl.querySelector(
                "col[data-col-index='" + (resizeColIndex + 1) + "']");
        if (colL != null)
            ((elemental2.dom.HTMLElement) colL).style.setProperty("width", newLeft + "%");
        if (colR != null)
            ((elemental2.dom.HTMLElement) colR).style.setProperty("width", newRight + "%");

        // Record rounded values for persistence on mouseup.
        int roundedLeft = (int) Math.round(newLeft);
        resizeColWidths[resizeColIndex]     = roundedLeft;
        resizeColWidths[resizeColIndex + 1] = (int) total - roundedLeft;
    }

    /**
     * Ends the column resize drag. Removes document-level listeners, restores
     * text selection, and persists the new widths to the model via a silent
     * transaction.
     */
    private void endColResize() {
        DomGlobal.document.removeEventListener("mousemove", resizeMoveListener);
        DomGlobal.document.removeEventListener("mouseup",   resizeEndListener);
        resizeMoveListener = null;
        resizeEndListener  = null;

        elemental2.dom.HTMLElement body = Js.uncheckedCast(DomGlobal.document.body);
        body.style.removeProperty("user-select");
        body.style.removeProperty("-webkit-user-select");

        if ((resizeTableIndex < 0) || (resizeColWidths == null))
            return;

        // Persist to model.
        List<FormattedBlock> blocks = resizeCtx.state().doc().getBlocks();
        if (resizeTableIndex < blocks.size()) {
            FormattedBlock tableClone = blocks.get(resizeTableIndex).clone();
            tableClone.meta("colwidths", joinColWidths(resizeColWidths));
            Transaction tr = Transaction.create();
            tr.step(new ReplaceBlockStep(resizeTableIndex, tableClone));
            tr.setSelection(resizeCtx.state().selection());
            resizeCtx.applyTransactionSilent(tr);
        }
        resizeTableIndex = -1;
        resizeTableEl    = null;
        resizeCtx        = null;
        resizeColWidths  = null;
    }

    /************************************************************************
     * Cell event helpers.
     ************************************************************************/

    /**
     * Computes the set of {@link FormatType}s active at the current DOM
     * selection within {@code cellEl}. For a cursor, checks the character
     * immediately before the cursor (or the first character if at offset 0).
     * For a range, returns only formats that span the entire selection.
     */
    private Set<FormatType> computeCellFormatState(elemental2.dom.Element cellEl, IEditorContext ctx) {
        Set<FormatType> active = new HashSet<>();
        int[] range = EditorSupport.selectionInCell(cellEl);
        if (range == null)
            return active;
        int from = Math.min(range[0], range[1]);
        int to = Math.max(range[0], range[1]);
        FormattedLine line = buildLineFromCellDom(cellEl, ctx);
        FormattedBlock tempCell = new FormattedBlock(BlockType.TCELL);
        tempCell.getLines().add(line);
        int len = to - from;
        for (FormatType fmt : ctx.formatClasses().keySet()) {
            if (len > 0) {
                // Range: format must span the entire selection.
                if (tempCell.hasFormat(from, len, fmt))
                    active.add(fmt);
            } else if (from > 0) {
                // Cursor: check character immediately before cursor.
                if (tempCell.hasFormat(from - 1, 1, fmt))
                    active.add(fmt);
            } else if (line.length() > 0) {
                // Cursor at start: check first character.
                if (tempCell.hasFormat(0, 1, fmt))
                    active.add(fmt);
            }
        }
        return active;
    }

    /**
     * Returns {@code true} if the event target is inside a table cell content
     * div (a {@code <div contenteditable="true">} bearing a
     * {@code data-table-index} attribute).
     */
    private boolean isInsideTableCell(elemental2.dom.EventTarget target, IEditorContext ctx) {
        elemental2.dom.Element el = Js.uncheckedCast(target);
        while ((el != null) && (el != ctx.editorEl())) {
            if (el.hasAttribute("data-table-index") && "true".equals(el.getAttribute("contenteditable")))
                return true;
            el = el.parentElement;
        }
        return false;
    }

    /**
     * Handles keydown inside a table cell. Stops propagation to prevent the
     * outer editor from firing shortcuts. Navigation rules:
     * <ul>
     * <li>Enter — next row, same column (do nothing at last row)</li>
     * <li>Tab / Shift+Tab — forward / backward by column (wraps rows)</li>
     * <li>ArrowDown / ArrowUp — next / previous row, same column</li>
     * <li>ArrowRight — next cell only when cursor is at end of text</li>
     * <li>ArrowLeft — previous cell only when cursor is at start of text</li>
     * </ul>
     */
    private void handleCellKeyDown(KeyboardEvent ke, int tableIndex, int row, int col,
            int numRows, int numCols, IEditorContext ctx) {
        ke.stopPropagation();
        if ("Enter".equals(ke.key) && !ke.shiftKey) {
            ke.preventDefault();
            if (row < (numRows - 1))
                focusCell(tableIndex, row + 1, col, true, ctx);  // entering from above → start
            // Last row: do nothing.
        } else if ("Tab".equals(ke.key)) {
            ke.preventDefault();
            if (ke.shiftKey) {
                // Shift+Tab: navigate backward → cursor at end.
                int prevRow = row;
                int prevCol = col - 1;
                if (prevCol < 0) {
                    prevCol = numCols - 1;
                    prevRow--;
                }
                if (prevRow >= 0)
                    focusCell(tableIndex, prevRow, prevCol, false, ctx);
            } else {
                // Tab: navigate forward → cursor at start.
                int nextRow = row;
                int nextCol = col + 1;
                if (nextCol >= numCols) {
                    nextCol = 0;
                    nextRow++;
                }
                if (nextRow < numRows)
                    focusCell(tableIndex, nextRow, nextCol, true, ctx);
            }
        } else if ("ArrowDown".equals(ke.key)) {
            ke.preventDefault();
            if (row < (numRows - 1))
                focusCell(tableIndex, row + 1, col, true, ctx);  // entering from above → start
        } else if ("ArrowUp".equals(ke.key)) {
            ke.preventDefault();
            if (row > 0)
                focusCell(tableIndex, row - 1, col, false, ctx);  // entering from below → end
        } else if ("ArrowRight".equals(ke.key)) {
            // Navigate to next cell only when cursor is at end of text.
            elemental2.dom.Element cell = Js.uncheckedCast(ke.target);
            int offset = EditorSupport.cursorOffsetInCell(cell);
            if (offset >= cell.textContent.length()) {
                ke.preventDefault();
                int nextRow = row, nextCol = col + 1;
                if (nextCol >= numCols) { nextCol = 0; nextRow++; }
                if (nextRow < numRows)
                    focusCell(tableIndex, nextRow, nextCol, true, ctx);  // entering from left → start
            }
            // Otherwise let browser move cursor right within the cell.
        } else if ("ArrowLeft".equals(ke.key)) {
            // Navigate to previous cell only when cursor is at start of text.
            elemental2.dom.Element cell = Js.uncheckedCast(ke.target);
            int offset = EditorSupport.cursorOffsetInCell(cell);
            if (offset <= 0) {
                ke.preventDefault();
                int prevRow = row, prevCol = col - 1;
                if (prevCol < 0) { prevCol = numCols - 1; prevRow--; }
                if (prevRow >= 0)
                    focusCell(tableIndex, prevRow, prevCol, false, ctx);  // entering from right → end
            }
            // Otherwise let browser move cursor left within the cell.
        } else if ((ke.ctrlKey || ke.metaKey) && !ke.shiftKey) {
            // Format shortcuts within the cell (ke.target is the cell element).
            FormatType fmt = null;
            if ("b".equals(ke.key))
                fmt = FormatType.BLD;
            else if ("i".equals(ke.key))
                fmt = FormatType.ITL;
            else if ("u".equals(ke.key))
                fmt = FormatType.UL;
            if (fmt != null) {
                ke.preventDefault();
                applyToggleFormatInCellEl(fmt, Js.uncheckedCast(ke.target), ctx);
            }
        }
    }

    /**
     * Focuses the cell at (tableIndex, row, col) and places the cursor at the
     * start ({@code atStart=true}) or end ({@code atStart=false}) of the
     * content.
     */
    private void focusCell(int tableIndex, int row, int col, boolean atStart, IEditorContext ctx) {
        String selector = "[data-table-index='" + tableIndex + "'][data-row='" + row
                + "'][data-col='" + col + "']";
        elemental2.dom.Element cell = ctx.editorEl().querySelector(selector);
        if (cell != null) {
            ((elemental2.dom.HTMLElement) cell).focus();
            if (atStart)
                EditorSupport.moveCursorToStart(cell);
            else
                EditorSupport.moveCursorToEnd(cell);
        }
    }

    /**
     * Syncs the focused cell (if any) to the model without re-rendering. Reads
     * the current cell element and delegates to {@link #syncCellToModel}.
     */
    private void syncFocusedCell(IEditorContext ctx) {
        if (focusedTableIndex < 0)
            return;
        String selector = "[data-table-index='" + focusedTableIndex + "'][data-row='"
                + focusedTableRow + "'][data-col='" + focusedTableCol + "']";
        elemental2.dom.Element cell = ctx.editorEl().querySelector(selector);
        if (cell != null)
            syncCellToModel(focusedTableIndex, focusedTableRow, focusedTableCol, cell, ctx);
    }

    /**
     * Syncs the content of a table cell element back to the model. Clones the
     * TABLE block, updates the target TCELL's first line, applies the change
     * via {@link ReplaceBlockStep}, and pushes the inverse to history. Does
     * NOT call {@code render()} — the DOM is already up to date.
     */
    private void syncCellToModel(int tableIndex, int row, int col,
            elemental2.dom.Element cellEl, IEditorContext ctx) {
        List<FormattedBlock> blocks = ctx.state().doc().getBlocks();
        if ((tableIndex < 0) || (tableIndex >= blocks.size()))
            return;
        FormattedBlock table = blocks.get(tableIndex);
        if (table.getType() != BlockType.TABLE)
            return;

        String text = cellEl.textContent;
        if (text == null)
            text = "";

        // If the plain text hasn't changed since focus, nothing to do (formatting
        // changes are applied immediately via applyToggleFormatInCellEl, not here).
        if (text.equals(focusedCellInitialContent))
            return;

        // Read the cell DOM to capture formatted content (typed text may have been
        // entered inside existing formatted spans, which we want to preserve).
        FormattedLine newLine = buildLineFromCellDom(cellEl, ctx);

        // Clone the table and update the target cell.
        FormattedBlock tableClone = table.clone();
        int ri = 0;
        outer:
        for (FormattedBlock tableRow : tableClone.getBlocks()) {
            if (tableRow.getType() != BlockType.TROW)
                continue;
            if (ri == row) {
                int ci = 0;
                for (FormattedBlock cell : tableRow.getBlocks()) {
                    if (cell.getType() != BlockType.TCELL)
                        continue;
                    if (ci == col) {
                        cell.getLines().clear();
                        cell.getLines().add(newLine.clone());
                        break outer;
                    }
                    ci++;
                }
            }
            ri++;
        }

        Transaction tr = Transaction.create();
        tr.step(new ReplaceBlockStep(tableIndex, tableClone));
        tr.setSelection(ctx.state().selection());
        ctx.applyTransactionSilent(tr);
        focusedCellInitialContent = text;
    }

    /**
     * Navigates to the TCELL at the given row/column within a TABLE block and
     * returns it, or {@code null} if not found.
     */
    private FormattedBlock getCellBlock(FormattedBlock table, int row, int col) {
        int ri = 0;
        for (FormattedBlock tableRow : table.getBlocks()) {
            if (tableRow.getType() != BlockType.TROW)
                continue;
            if (ri == row) {
                int ci = 0;
                for (FormattedBlock cell : tableRow.getBlocks()) {
                    if (cell.getType() != BlockType.TCELL)
                        continue;
                    if (ci == col)
                        return cell;
                    ci++;
                }
                return null;
            }
            ri++;
        }
        return null;
    }

    /**
     * Reads the DOM content of a cell element and reconstructs a
     * {@link FormattedLine} that preserves inline formatting (bold, italic,
     * etc.) encoded as CSS class names on {@code <span>} elements.
     */
    private FormattedLine buildLineFromCellDom(elemental2.dom.Element cellEl, IEditorContext ctx) {
        FormattedLine line = new FormattedLine();
        elemental2.dom.NodeList<elemental2.dom.Node> children = cellEl.childNodes;
        Map<FormatType, String> fmtClasses = ctx.formatClasses();
        for (int i = 0; i < children.length; i++) {
            elemental2.dom.Node node = children.item(i);
            if (node.nodeType == 3) {
                // Text node — plain text.
                String text = node.textContent;
                if ((text != null) && !text.isEmpty())
                    line.append(text);
            } else if (node.nodeType == 1) {
                elemental2.dom.Element el = Js.uncheckedCast(node);
                String tag = el.tagName;
                if ("SPAN".equalsIgnoreCase(tag)) {
                    String text = el.textContent;
                    if ((text != null) && !text.isEmpty()) {
                        List<FormatType> fmts = new ArrayList<>();
                        for (Map.Entry<FormatType, String> entry : fmtClasses.entrySet()) {
                            if (el.classList.contains(entry.getValue()))
                                fmts.add(entry.getKey());
                        }
                        line.append(text, fmts.toArray(new FormatType[0]));
                    }
                } else if (!"BR".equalsIgnoreCase(tag)) {
                    // Any other element (e.g. anchor) — fall back to plain text.
                    String text = el.textContent;
                    if ((text != null) && !text.isEmpty())
                        line.append(text);
                }
            }
        }
        return line;
    }

    /**
     * Clears and re-renders a cell element's content from the given cell block.
     * Does not trigger a full re-render.
     */
    private void renderCellContent(elemental2.dom.Element cellEl, FormattedBlock cellBlock,
            IEditorContext ctx) {
        while (cellEl.firstChild != null)
            cellEl.removeChild(cellEl.firstChild);
        List<FormattedLine> lines = cellBlock.getLines();
        if (lines.isEmpty()) {
            cellEl.appendChild(DomGlobal.document.createElement("br"));
            return;
        }
        ctx.renderLine(cellEl, lines.get(0));
        for (int i = 1; i < lines.size(); i++) {
            cellEl.appendChild(DomGlobal.document.createElement("br"));
            ctx.renderLine(cellEl, lines.get(i));
        }
    }

    /**
     * Toggles a format type on the current text selection within a table cell.
     * The cell element is passed directly (read from the DOM selection at call
     * time, not from stored focused-cell state) so this works even if the cell
     * has already received a blur event. Coordinates are read from the cell's
     * data attributes.
     */
    private void applyToggleFormatInCellEl(FormatType type, elemental2.dom.Element cellEl,
            IEditorContext ctx) {
        if (cellEl == null)
            return;

        // Read table/row/col from data attributes — no dependency on stored state.
        String tableIndexStr = cellEl.getAttribute("data-table-index");
        String rowStr = cellEl.getAttribute("data-row");
        String colStr = cellEl.getAttribute("data-col");
        if ((tableIndexStr == null) || (rowStr == null) || (colStr == null))
            return;
        int tableIndex, row, col;
        try {
            tableIndex = Integer.parseInt(tableIndexStr);
            row = Integer.parseInt(rowStr);
            col = Integer.parseInt(colStr);
        } catch (NumberFormatException e) {
            return;
        }

        int[] range = EditorSupport.selectionInCell(cellEl);
        if (range == null)
            return;
        int from = Math.min(range[0], range[1]);
        int to = Math.max(range[0], range[1]);
        int len = to - from;
        if (len <= 0)
            return;

        // Build the current cell content from the DOM, including any typed text
        // and already-applied inline formatting, so we work on the latest state.
        FormattedLine currentLine = buildLineFromCellDom(cellEl, ctx);
        FormattedBlock tempCell = new FormattedBlock(BlockType.TCELL);
        tempCell.getLines().add(currentLine);
        boolean alreadyHas = tempCell.hasFormat(from, len, type);
        if (alreadyHas)
            tempCell.removeFormat(from, len, type);
        else
            tempCell.addFormat(from, len, type);
        FormattedLine newLine = tempCell.getLines().get(0);

        // Clone the table and replace the target cell's content.
        List<FormattedBlock> blocks = ctx.state().doc().getBlocks();
        if (tableIndex >= blocks.size())
            return;
        FormattedBlock tableClone = blocks.get(tableIndex).clone();
        FormattedBlock cellClone = getCellBlock(tableClone, row, col);
        if (cellClone == null)
            return;
        cellClone.getLines().clear();
        cellClone.getLines().add(newLine.clone());

        // Apply to model (no full re-render needed).
        Transaction tr = Transaction.create();
        tr.step(new ReplaceBlockStep(tableIndex, tableClone));
        tr.setSelection(ctx.state().selection());
        ctx.applyTransactionSilent(tr);

        // Re-render the cell DOM and restore selection.
        renderCellContent(cellEl, cellClone, ctx);
        EditorSupport.setSelectionInCell(cellEl, from, to);

        // Reset baseline so blur does not re-sync unchanged text.
        focusedCellInitialContent = cellEl.textContent;

        // Update toolbar to reflect the new format state at the selection.
        ctx.notifyCellSelection(computeCellFormatState(cellEl, ctx));
    }

    /************************************************************************
     * Row / column context menus.
     ************************************************************************/

    /**
     * Shows the row context menu anchored to the right of {@code handleEl}.
     * When {@code rowIndex == 0} an additional toggle item is appended to
     * switch the first row between a header row and a normal row.
     */
    private void showRowMenu(elemental2.dom.Element handleEl, int tableIndex, int rowIndex, IEditorContext ctx) {
        ContextMenu menu = new ContextMenu()
                .item("Insert row above", () -> ctx.applyTransaction(Commands.insertTableRowAbove(ctx.state(), tableIndex, rowIndex)))
                .item("Insert row below", () -> ctx.applyTransaction(Commands.insertTableRowBelow(ctx.state(), tableIndex, rowIndex)))
                .sep()
                .item("Delete row", () -> ctx.applyTransaction(Commands.deleteTableRow(ctx.state(), tableIndex, rowIndex)));
        if (rowIndex == 0) {
            boolean isHeader = isTableHeaderRow(tableIndex, ctx);
            menu.sep();
            if (isHeader)
                menu.item("Make normal row", () -> ctx.applyTransaction(Commands.setTableHeaders(ctx.state(), tableIndex, 0)));
            else
                menu.item("Make header row", () -> ctx.applyTransaction(Commands.setTableHeaders(ctx.state(), tableIndex, 1)));
        }
        menu.showRight(handleEl);
    }

    /**
     * Returns {@code true} if the first row of the table at {@code tableIndex}
     * is currently a header row (i.e. {@code meta("headers")} is &gt; 0).
     */
    private boolean isTableHeaderRow(int tableIndex, IEditorContext ctx) {
        List<FormattedBlock> blocks = ctx.state().doc().getBlocks();
        if ((tableIndex < 0) || (tableIndex >= blocks.size()))
            return false;
        FormattedBlock table = blocks.get(tableIndex);
        if (table.getType() != BlockType.TABLE)
            return false;
        String headersStr = table.meta("headers");
        if (headersStr == null)
            return false;
        try {
            return Integer.parseInt(headersStr) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Shows the column context menu anchored below {@code handleEl}.
     */
    private void showColMenu(elemental2.dom.Element handleEl, int tableIndex, int colIndex, IEditorContext ctx) {
        new ContextMenu()
                .item("Insert column left", () -> ctx.applyTransaction(Commands.insertTableColumnLeft(ctx.state(), tableIndex, colIndex)))
                .item("Insert column right", () -> ctx.applyTransaction(Commands.insertTableColumnRight(ctx.state(), tableIndex, colIndex)))
                .sep()
                .item("Delete column", () -> ctx.applyTransaction(Commands.deleteTableColumn(ctx.state(), tableIndex, colIndex)))
                .showBelow(handleEl);
    }

    /************************************************************************
     * CSS.
     ************************************************************************/

    private ITableCSS styles() {
        return TableCSS.instance();
    }

    public static interface ITableCSS extends IComponentCSS {

        String tableWrapper();

        String table();

        String tableCell();

        String tableCellContent();

        String tableAddCol();

        String tableAddRow();

        String colResizeHandle();

        String tableRowHandle();

        String tableColHandle();
    }

    @CssResource(value = {
        IComponentCSS.COMPONENT_CSS
    }, stylesheet = """
        .tableWrapper {
            position: relative;
            margin: 0.5em 0;
            padding-right: 18px;
            padding-bottom: 18px;
            user-select: none;
        }
        .table {
            border-collapse: collapse;
            width: 100%;
        }
        .tableCell {
            position: relative;
            border: 1px solid #ddd;
            min-width: 40px;
            vertical-align: top;
        }
        .tableCellContent {
            display: block;
            width: 100%;
            padding: 6px 10px;
            min-height: 1.4em;
            box-sizing: border-box;
            outline: none;
            word-break: break-word;
        }
        .tableAddCol {
            position: absolute;
            right: 0;
            top: 0;
            bottom: 18px;
            width: 14px;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            opacity: 0;
            transition: opacity 0.15s;
            background: #f0f0f0;
            border-radius: 0 4px 4px 0;
            color: #666;
            font-size: 1.1em;
        }
        .tableWrapper:hover .tableAddCol {
            opacity: 1;
        }
        .tableAddCol:hover {
            background: #dbeafe;
            color: #1d4ed8;
        }
        .tableAddRow {
            position: absolute;
            left: 0;
            right: 18px;
            bottom: 0;
            height: 14px;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            opacity: 0;
            transition: opacity 0.15s;
            background: #f0f0f0;
            border-radius: 0 0 4px 4px;
            color: #666;
            font-size: 1.1em;
        }
        .tableWrapper:hover .tableAddRow {
            opacity: 1;
        }
        .tableAddRow:hover {
            background: #dbeafe;
            color: #1d4ed8;
        }
        .colResizeHandle {
            position: absolute;
            top: 0;
            right: -3px;
            bottom: 0;
            width: 6px;
            cursor: col-resize;
            z-index: 1;
        }
        .colResizeHandle:hover {
            background: rgba(59, 130, 246, 0.35);
        }
        .tableRowHandle {
            position: absolute;
            left: -3px;
            top: 50%;
            /* Resting: scaled-down pill at 60 % — small, unobtrusive hint. */
            transform: translateY(-50%) scale(0.6);
            width: 6px;
            height: 29px;
            border-radius: 3px;
            background: rgba(0, 0, 0, 0.18);
            cursor: pointer;
            opacity: 0;
            pointer-events: none;
            z-index: 2;
            transition: transform 0.12s ease, background 0.12s ease, opacity 0.15s;
        }
        .tableCell:focus-within .tableRowHandle {
            opacity: 1;
            pointer-events: auto;
        }
        /* Hover: expand to full size and turn blue. */
        .tableRowHandle:hover {
            transform: translateY(-50%) scale(1);
            background: #3b82f6;
        }
        .tableColHandle {
            position: absolute;
            top: -3px;
            left: 50%;
            /* Resting: scaled-down pill at 60 %. */
            transform: translateX(-50%) scale(0.6);
            width: 36px;
            height: 6px;
            border-radius: 3px;
            background: rgba(0, 0, 0, 0.18);
            cursor: pointer;
            opacity: 0;
            pointer-events: none;
            z-index: 2;
            transition: transform 0.12s ease, background 0.12s ease, opacity 0.15s;
        }
        .tableCell:focus-within .tableColHandle {
            opacity: 1;
            pointer-events: auto;
        }
        /* Hover: expand to full size and turn blue. */
        .tableColHandle:hover {
            transform: translateX(-50%) scale(1);
            background: #3b82f6;
        }
    """)
    public static abstract class TableCSS implements ITableCSS {

        private static TableCSS STYLES;

        public static ITableCSS instance() {
            if (STYLES == null) {
                STYLES = (TableCSS) GWT.create(TableCSS.class);
                STYLES.ensureInjected();
            }
            return STYLES;
        }
    }
}
