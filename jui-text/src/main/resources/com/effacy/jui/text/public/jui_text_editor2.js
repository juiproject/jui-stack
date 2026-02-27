/**
 * JS support for editor2 (transaction-based rich text editor).
 *
 * Provides selection reading/setting and character counting for the
 * contenteditable editor. Block elements are identified by their
 * data-block-index attribute.
 */
EditorSupport2 = {}

/************************************************************************
 * Leaf-node traversal (shared helpers).
 ************************************************************************/

/**
 * Returns the first leaf node under the given node.
 */
EditorSupport2._firstLeaf = function (n) {
    if (n == null)
        return null;
    while (n.childNodes && n.childNodes.length > 0)
        n = n.childNodes[0];
    return n;
}

/**
 * Returns the last leaf node under the given node.
 */
EditorSupport2._lastLeaf = function (n) {
    if (n == null)
        return null;
    while (n.childNodes && n.childNodes.length > 0)
        n = n.childNodes[n.childNodes.length - 1];
    return n;
}

/**
 * Returns the next leaf node after n, bounded by scope.
 */
EditorSupport2._nextLeaf = function (scope, n) {
    if (n == null)
        return null;
    if (n.nextSibling)
        return EditorSupport2._firstLeaf(n.nextSibling);
    while ((n = n.parentNode) != scope && n != null) {
        if (n.nextSibling)
            return EditorSupport2._firstLeaf(n.nextSibling);
    }
    return null;
}

/************************************************************************
 * contenteditable="false" (CEF) helpers.
 ************************************************************************/

/**
 * Returns the nearest ancestor of node (exclusive) that has
 * contenteditable="false", stopping before scope (exclusive).
 * Returns null if no such ancestor exists.
 */
EditorSupport2._cefAncestor = function (scope, node) {
    var p = node.parentNode;
    while (p && p !== scope) {
        if (p.nodeType === 1 && p.getAttribute('contenteditable') === 'false')
            return p;
        p = p.parentNode;
    }
    return null;
}

/**
 * Returns the total length of all text nodes under el.
 */
EditorSupport2._textLength = function (el) {
    if (el.nodeType === 3)
        return el.textContent.length;
    var len = 0;
    for (var i = 0; i < el.childNodes.length; i++)
        len += EditorSupport2._textLength(el.childNodes[i]);
    return len;
}

/**
 * Returns the child index of child within parent.childNodes, or -1.
 */
EditorSupport2._childIndex = function (parent, child) {
    for (var i = 0; i < parent.childNodes.length; i++) {
        if (parent.childNodes[i] === child)
            return i;
    }
    return -1;
}

/************************************************************************
 * Lines (text segmentation within a block).
 ************************************************************************/

/**
 * Generates an array of line strings from the content of el, stopping
 * at the optional stop node. Lines are split on BR elements. Text
 * formatting (spans) is transparent.
 */
EditorSupport2._lines = function (el, stop) {
    var lines = [""];
    EditorSupport2._linesWalk(el, stop, lines);
    return lines;
}

EditorSupport2._linesWalk = function (el, stop, lines) {
    if (el === stop)
        return true;
    if (el.nodeType === 3) {
        lines[lines.length - 1] += el.textContent;
        return false;
    }
    if (el.tagName === 'BR') {
        // Skip trailing BR (rendering artifact for empty last lines).
        if (el.hasAttribute && el.hasAttribute('data-trailing'))
            return false;
        lines.push("");
        return false;
    }
    for (var i = 0; i < el.childNodes.length; i++) {
        if (EditorSupport2._linesWalk(el.childNodes[i], stop, lines))
            return true;
    }
    return false;
}

/************************************************************************
 * Character counting.
 ************************************************************************/

/**
 * Counts the total number of characters in a block element. Text node
 * characters are counted directly; BR elements count as 1 (line break).
 */
EditorSupport2.charCount = function (el) {
    var lines = EditorSupport2._lines(el, null);
    var count = 0;
    for (var i = 0; i < lines.length; i++) {
        if (i > 0)
            count++;
        count += lines[i].length;
    }
    return count;
}

/************************************************************************
 * Character offset computation.
 ************************************************************************/

/**
 * Computes the character offset within a block element for a given
 * DOM node and offset (as returned by Selection.anchorNode/anchorOffset).
 *
 * When the browser positions the cursor next to a contenteditable="false"
 * element, it reports anchorNode as the parent element with anchorOffset
 * as a child index. This function handles both that case and the edge
 * case where offset equals childNodes.length (cursor after the last child).
 */
EditorSupport2._offsetInBlock = function (blockEl, node, offset) {
    if (node.nodeType === 1 && offset < node.childNodes.length) {
        node = node.childNodes[offset];
        offset = 0;
    } else if (node.nodeType === 1 && offset > 0 && offset >= node.childNodes.length) {
        // Cursor at end of an element (e.g. after a CEF span that is the
        // last child). Count all text within this element.
        if (node === blockEl)
            return EditorSupport2.charCount(blockEl);
        // Nested element: count text before it plus text inside it.
        var beforeLines = EditorSupport2._lines(blockEl, node);
        var count = 0;
        for (var i = 0; i < beforeLines.length; i++) {
            if (i > 0) count++;
            count += beforeLines[i].length;
        }
        return count + EditorSupport2._textLength(node);
    }
    var lines = EditorSupport2._lines(blockEl, node);
    var count = 0;
    for (var i = 0; i < lines.length; i++) {
        if (i > 0)
            count++;
        count += lines[i].length;
    }
    return count + offset;
}

/************************************************************************
 * Block element lookup.
 ************************************************************************/

/**
 * Finds the nearest block element (with data-block-index) containing
 * the given node. Returns null if the found element has
 * contenteditable="false" (e.g. a table wrapper) — cursor inside a table
 * cell is not in a selectable editor block.
 */
EditorSupport2._findBlockEl = function (editorEl, node) {
    while (node && node !== editorEl) {
        if (node.nodeType === 1 && node.hasAttribute && node.hasAttribute('data-block-index')) {
            if (node.getAttribute('contenteditable') === 'false')
                return null;
            return node;
        }
        node = node.parentNode;
    }
    return null;
}

/************************************************************************
 * Selection reading.
 ************************************************************************/

/**
 * Reads the current DOM selection mapped to block coordinates.
 *
 * @param editorEl the contenteditable editor element.
 * @return array [anchorBlock, anchorOffset, headBlock, headOffset] or null.
 */
EditorSupport2.readSelection = function (editorEl) {
    var sel = document.getSelection();
    if (!sel || sel.rangeCount === 0)
        return null;

    var anchorNode = sel.anchorNode;
    var focusNode = sel.focusNode;
    if (!anchorNode || !focusNode)
        return null;

    var anchorBlockEl = EditorSupport2._findBlockEl(editorEl, anchorNode);
    var focusBlockEl = EditorSupport2._findBlockEl(editorEl, focusNode);
    if (!anchorBlockEl || !focusBlockEl)
        return null;

    var anchorBlock = parseInt(anchorBlockEl.getAttribute('data-block-index'));
    var headBlock = parseInt(focusBlockEl.getAttribute('data-block-index'));
    var anchorOff = EditorSupport2._offsetInBlock(anchorBlockEl, anchorNode, sel.anchorOffset);
    var headOff = EditorSupport2._offsetInBlock(focusBlockEl, focusNode, sel.focusOffset);

    return [anchorBlock, anchorOff, headBlock, headOff];
}

/************************************************************************
 * Selection setting.
 ************************************************************************/

/**
 * Resolves a character offset within a block element to a DOM
 * node + offset pair suitable for Range.setStart/setEnd.
 *
 * contenteditable="false" elements (e.g. variable chips) are treated
 * as atomic: the cursor is placed before or after them rather than
 * inside their text content.
 */
EditorSupport2._resolvePosition = function (blockEl, position) {
    if (position <= 0) {
        var first = EditorSupport2._firstLeaf(blockEl);
        if (first && first.nodeType === 3) {
            // Don't position inside a CEF element at offset 0.
            var cef = EditorSupport2._cefAncestor(blockEl, first);
            if (cef) {
                var idx = EditorSupport2._childIndex(cef.parentNode, cef);
                return {node: cef.parentNode, offset: idx};
            }
            return {node: first, offset: 0};
        }
        return {node: blockEl, offset: 0};
    }
    var n = EditorSupport2._firstLeaf(blockEl);
    while (n != null) {
        if (n.nodeType === 3) {
            // Check if this text node is inside a CEF element.
            var cef = EditorSupport2._cefAncestor(blockEl, n);
            if (cef) {
                // Treat the CEF element as atomic.
                var cefLen = EditorSupport2._textLength(cef);
                var cefIdx = EditorSupport2._childIndex(cef.parentNode, cef);
                if (position < cefLen) {
                    // Position is within the CEF — place cursor after it.
                    return {node: cef.parentNode, offset: cefIdx + 1};
                }
                position -= cefLen;
                // Skip past all leaves inside the CEF.
                n = EditorSupport2._nextLeaf(blockEl, EditorSupport2._lastLeaf(cef));
                continue;
            }
            if (position <= n.textContent.length)
                return {node: n, offset: position};
            position -= n.textContent.length;
        } else if (n.tagName === 'BR') {
            if (n.hasAttribute && n.hasAttribute('data-trailing')) {
                // Trailing BR is not a content character. Position here
                // means cursor at the start of the (empty) last line.
                var parent = n.parentNode;
                for (var idx = 0; idx < parent.childNodes.length; idx++) {
                    if (parent.childNodes[idx] === n)
                        return {node: parent, offset: idx};
                }
            }
            position--;
        }
        n = EditorSupport2._nextLeaf(blockEl, n);
    }
    return {node: blockEl, offset: blockEl.childNodes.length};
}

/**
 * Sets the DOM selection to a cursor at the given block/offset.
 */
EditorSupport2.setCursor = function (editorEl, blockIndex, offset) {
    var blockEl = editorEl.querySelector('[data-block-index="' + blockIndex + '"]');
    if (!blockEl)
        return;
    var pos = EditorSupport2._resolvePosition(blockEl, offset);
    if (!pos)
        return;
    var sel = document.getSelection();
    sel.removeAllRanges();
    var r = new Range();
    r.setStart(pos.node, pos.offset);
    r.collapse(true);
    sel.addRange(r);
}

/**
 * Sets the DOM selection to a range spanning two block/offset pairs.
 */
EditorSupport2.setSelection = function (editorEl, anchorBlock, anchorOffset, headBlock, headOffset) {
    var anchorBlockEl = editorEl.querySelector('[data-block-index="' + anchorBlock + '"]');
    var headBlockEl = editorEl.querySelector('[data-block-index="' + headBlock + '"]');
    if (!anchorBlockEl || !headBlockEl)
        return;
    var anchorPos = EditorSupport2._resolvePosition(anchorBlockEl, anchorOffset);
    var headPos = EditorSupport2._resolvePosition(headBlockEl, headOffset);
    if (!anchorPos || !headPos)
        return;
    var sel = document.getSelection();
    sel.removeAllRanges();
    var r = new Range();
    r.setStart(anchorPos.node, anchorPos.offset);
    sel.addRange(r);
    sel.extend(headPos.node, headPos.offset);
}

/************************************************************************
 * Input event helpers.
 ************************************************************************/

/**
 * Returns the inputType from an InputEvent, or null.
 */
EditorSupport2.getInputType = function (evt) {
    return evt ? (evt.inputType || null) : null;
}

/**
 * Returns the data from an InputEvent, or null.
 */
EditorSupport2.getInputData = function (evt) {
    return evt ? (evt.data || null) : null;
}

/************************************************************************
 * Clipboard helpers.
 ************************************************************************/

/**
 * Returns the plain-text content from a ClipboardEvent (paste/cut/copy).
 */
EditorSupport2.getClipboardText = function (evt) {
    if (!evt)
        return null;
    var cd = evt.clipboardData;
    if (!cd)
        return null;
    return cd.getData('text/plain') || null;
}

/************************************************************************
 * Table cell helpers.
 ************************************************************************/

/**
 * Moves the cursor to the start of the content of a contenteditable element.
 */
EditorSupport2.moveCursorToStart = function (el) {
    var range = document.createRange();
    range.selectNodeContents(el);
    range.collapse(true);
    var sel = window.getSelection();
    sel.removeAllRanges();
    sel.addRange(range);
}

/**
 * Moves the cursor to the end of the content of a contenteditable element.
 */
EditorSupport2.moveCursorToEnd = function (el) {
    var range = document.createRange();
    range.selectNodeContents(el);
    range.collapse(false);
    var sel = window.getSelection();
    sel.removeAllRanges();
    sel.addRange(range);
}

/**
 * Returns the character offset of the cursor within a contenteditable cell
 * element, or -1 if the cursor is not inside the element or the selection
 * is not collapsed.
 */
EditorSupport2.cursorOffsetInCell = function (cellEl) {
    var sel = window.getSelection();
    if (!sel || sel.rangeCount === 0 || !sel.isCollapsed)
        return -1;
    var n = sel.anchorNode;
    // Verify anchor is inside cellEl.
    var p = n;
    while (p && p !== cellEl)
        p = p.parentNode;
    if (!p)
        return -1;
    return EditorSupport2._offsetInBlock(cellEl, n, sel.anchorOffset);
}

/************************************************************************
 * Cell selection helpers (for inline formatting).
 ************************************************************************/

/**
 * Returns the inner cell content div (contenteditable="true" bearing a
 * data-table-index attribute) containing the current selection anchor,
 * searching upward within editorEl. Returns null if the selection is not
 * inside a table cell content div.
 */
EditorSupport2.cellFromSelection = function (editorEl) {
    var sel = window.getSelection();
    if (!sel || sel.rangeCount === 0)
        return null;
    var n = sel.anchorNode;
    while (n && n !== editorEl) {
        if (n.nodeType === 1 && n.getAttribute('contenteditable') === 'true' && n.hasAttribute('data-table-index'))
            return n;
        n = n.parentNode;
    }
    return null;
}

/**
 * Returns [anchorOffset, headOffset] character offsets of the current
 * selection within a cell element, or null if the selection is not inside.
 */
EditorSupport2.selectionInCell = function (cellEl) {
    var sel = window.getSelection();
    if (!sel || sel.rangeCount === 0)
        return null;
    // Verify anchor is inside cellEl.
    var p = sel.anchorNode;
    while (p && p !== cellEl)
        p = p.parentNode;
    if (!p)
        return null;
    var anchorOff = EditorSupport2._offsetInBlock(cellEl, sel.anchorNode, sel.anchorOffset);
    var focusOff = EditorSupport2._offsetInBlock(cellEl, sel.focusNode, sel.focusOffset);
    return [anchorOff, focusOff];
}

/**
 * Sets the DOM selection to a range within a cell element at the given
 * character offsets.
 */
EditorSupport2.setSelectionInCell = function (cellEl, from, to) {
    var fromPos = EditorSupport2._resolvePosition(cellEl, from);
    var toPos = EditorSupport2._resolvePosition(cellEl, to);
    if (!fromPos || !toPos)
        return;
    var sel = window.getSelection();
    sel.removeAllRanges();
    var r = new Range();
    r.setStart(fromPos.node, fromPos.offset);
    sel.addRange(r);
    sel.extend(toPos.node, toPos.offset);
}
