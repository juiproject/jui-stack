EditorSupport = {}

EditorSupport.numberOfCharacters = function(el) {
    var lines = EditorSupport.lines (el,null);
    var count = 0;
    for (var i = 0; i < lines.length; i++) {
        if (i > 0)
            count++;
        count += lines[i].length;
    }
    return count;
}

EditorSupport.positionOfCursor = function(container,r) {
    return EditorSupport.positionAtStart(container,r);
}

EditorSupport.positionAtStart = function(container,r) {
    if (container == null)
        return -1;
    if (r == null)
        r = document.getSelection ().getRangeAt (0);
    if (r == null)
        return -1;
    return EditorSupport._offset (container, r.startContainer, r.startOffset);
}

EditorSupport.positionAtEnd = function(container,r) {
    if (container == null)
        return -1;
    if (r == null)
        r = document.getSelection ().getRangeAt (0);
    if (r == null)
        return -1;
    return EditorSupport._offset (container, r.endContainer, r.endOffset);
}

// Given a container, a node in the container and an offset
// into the node, determine absoluter charactor offset.
EditorSupport._offset = function (container,n,offset) {
    if (n.nodeType == 1) {
        n = n.childNodes[offset];
        offset = 0;
    }
    var lines = EditorSupport.lines (container,n);
    var count = 0;
    for (var i = 0; i < lines.length; i++) {
        if (i > 0)
            count++;
        count += lines[i].length;
    }
    count += offset;
    return count;
}

EditorSupport.positionCursorAtStart = function(el) {
    var r = new Range();
    r.setStart (el, 0);
    document.getSelection ().removeAllRanges ();
    document.getSelection ().addRange (r);
}

EditorSupport.positionCursorAtEnd = function(el) {
    var r = new Range();
    r.setStart (el, el.childNodes.length);
    document.getSelection ().removeAllRanges ();
    document.getSelection ().addRange (r);
}

EditorSupport.positionCursorAt = function(el,position) {
    if (el == null)
        return;
    if (position < 0) {
        EditorSupport.positionCursorAtEnd (el);
        return;
    }
    if (position == 0) {
        EditorSupport.positionCursorAtStart (el);
        return;
    }
    var r = EditorSupport._positionCursorAt (el, position);
    if (r != null) {
        document.getSelection ().removeAllRanges ();
        document.getSelection ().addRange (r);
    } else
        EditorSupport.positionCursorAtEnd (el);
}

EditorSupport._positionCursorAt = function(el,position) {
    var startNode = el;
    var startOffset = 0;
    var n = EditorSupport._firstNode (el);
    while (n != null) {
        if (n.nodeType == 3) {
            if (position <= n.textContent.length) {
                var r = new Range ();
                r.setStart (n, position);
                return r;
            }
            position -= n.textContent.length;
        } else if (n.tagName == "BR") {
            position--;
        }
        n = EditorSupport._nextNode (el,n);
    }
    return null;
}

// Gets the next node bounded by the given scope.
EditorSupport._nextNode = function(scope,n) {
    if (n == null)
        return null;
    if (n.childNodes.length != 0)
        return EditorSupport._firstNode (n);
    if (n.nextSibling == null) {
        while ((n = n.parentNode) != scope) {
            if (n.nextSibling != null)
                return EditorSupport._firstNode (n.nextSibling);
        }
    }
    return EditorSupport._firstNode (n.nextSibling);
}

EditorSupport._firstNode = function(n) {
    if (n == null)
        return null;
    while (n.childNodes.length != 0)
        n = n.childNodes[0];
    return n;
}

// Returns an array of text nodes sitting under the passed
// element upto (and excluding) the stop (unless exhausted).
EditorSupport._textNodes = function(el,stop) {
    var nodes = [];
    EditorSupport._textNodes_descend (el,stop,nodes);
    return nodes;
}

// Called by EditorSupport._textNodes
EditorSupport._textNodes_descend = function(el,stop,nodes) {
    if (el == stop)
        return true;
    if (el.nodeType == 3) {
        nodes.push (el);
        return false;
    }
    for (var i = 0; i < el.childNodes.length; i++) {
        if (EditorSupport._textNodes_descend (el.childNodes[i],stop,nodes))
            return true;
    }
    return false;
}

// Generates an array of lines divorced of text formatting.
EditorSupport.lines = function(el,stop) {
    var lines = [];
    lines.push ("");
    EditorSupport._lines (el, stop, lines);
    return lines;
}

// Called by lines to descend.
EditorSupport._lines = function(el,stop,lines) {
    if (el == stop)
        return true;
    if (el.nodeType == 3) {
        lines[lines.length - 1] = lines[lines.length - 1] += el.textContent;
        return false;
    }
    if (el.tagName == "BR") {
        lines.push ("");
        return false;
    }
    for (var i = 0; i < el.childNodes.length; i++) {
        if (EditorSupport._lines (el.childNodes[i],stop,lines))
            return true;
    }
    return false;
}

EditorSupport.clean = function(el) {
    if (el == null)
        return;
    if (el.lastChild == null)
        return;
    // We cannot have a single line break at the end of the block.
    if (el.lastChild.tagName == "BR") {
        if ((el.lastChild.previousSibling == null) || (el.lastChild.previousSibling.tagName != "BR"))
            el.lastChild.remove ();
    }
    // Convert any leading space to a NBSP.
    if ((el.firstChild.nodeType == 3) && el.firstChild.textContent.startsWith (' '))
        el.firstChild.textContent = '\u00a0' + el.firstChild.textContent.substring (1);
}

EditorSupport.isCursorAtEnd = function(el,r) {
    if (r == null)
        r = document.getSelection ().getRangeAt (0);
    if (r == null)
        return false;
    var n = r.startContainer;
    if (n == null)
        return false;
    do {
        if (n == el) {
            if (n.nodeType == 3)
                return r.startOffset >= n.textContent.length;
            return r.startOffset >= n.childNodes.length;
        }
        el = el.lastChild;
    } while (el != null);
    return false;
}

EditorSupport.isCursorAtStart = function(el,r) {
    if (r == null)
        r = document.getSelection ().getRangeAt (0);
    if (r == null)
        return false;
    if (r.startOffset > 0)
        return false;
    var n = r.startContainer;
    if (n == null)
        return false;
    do {
        if (n == el)
            return true;
        el = el.firstChild;
    } while (el != null);
    return false;
}

EditorSupport.parent = function(el1,el2) {
    if (el1 == null)
        return el2;
    if (el2 == null)
        return el1;
    var nodes = [];
    while (el1 != null) {
        el1 = el1.parentNode;
        nodes.push (el1);
    }
    while (el2 != null) {
        el2 = el2.parentNode;
        if (nodes.filter (e => e === el2).length > 0)
            return el2;
    }
    return null;
}

EditorSupport.traverse = function(el,cb,nl) {
    var n = el.firstChild;
    var text = "";
    var style = [];
    while (n != null) {
        if (n.nodeType == 3) {
            text += n.textContent;
        } else if (n.tagName == "BR") {
            if (text !== "")
                cb (text, style);
            text = "";
            style = [];
            nl ();
        } else {
            if (text !== "")
                cb (text, style);
            for (var i = 0; i < n.classList.length; i++)
                style.push (n.classList.item (i));
            n = n.firstChild;
            text = n.textContent;
        }
        if ((n.nextSibling == null) && (n.parentNode != el)) {
            n = n.parentNode;
            cb (text, style);
            text = "";
            style = [];
        }
        n = n.nextSibling;
    }
    if (text !== "")
        cb (text, style);
}

// Returns a list of all styles that apply in their entirity for the given range.
EditorSupport.styles = function(container,r) {
    nr = EditorSupport._normaliseRange (container,r);
    if  (nr == null)
        return [];

    var ln = nr.startContainer;
    var lnp = nr.startOffset;
    var rn = nr.endContainer;
    var rnp = nr.endOffset;

    // Lift up any node that is below a span.
    if (ln.parentNode != container) {
        if ((ln.nextSibling == null) && (ln.textContent.length <= lnp))
            ln = ln.parentNode.nextSibling;
        else
            ln = ln.parentNode;
    }
    if (rn.parentNode != container) {
        if ((rn.previousSibling == null) && (rnp == 0))
            rn = rn.parentNode.previousSibling;
        else
            rn = rn.parentNode;
    }

    // Shuffle nodes that are at the ends of text nodes.
    if ((ln.nodeType == 3) && (ln.textContent.length <= lnp))
        ln = ln.nextSibling;
    if ((rn.nodeType == 3) && (rnp  == 0))
        rn = rn.previousSibling;

    var styles = [];
    do {
        // If this is the case then there was something wrong, so abort cleanly.
        if ((ln != rn ) && (ln.nextSibling == null))
            return [];
        if (ln.nodeType != 1)
            return [];
        if (ln.nodeName == "SPAN") {
            if (styles.length == 0) {
                for (var i = 0; i < ln.classList.length; i++)
                    styles.push (ln.classList.item (i));
            } else {
                styles = styles.filter (v => ln.classList.contains (v));
                if (styles.length == 0)
                    return [];
            }
        }
        ln = (ln == rn) ? null : ln.nextSibling;
    } while (ln != null);
    return styles;
}

// Removes a style from any spans in the range. Returns a revised range.
EditorSupport.clear = function(container,r,style) {
    nr = EditorSupport._normaliseRange (container,r);
    if  (nr == null)
        return r;

    // Get the range offsets (for later).
    var lp = EditorSupport._offset (container, nr.startContainer, nr.startOffset);
    var rp = EditorSupport._offset (container, nr.endContainer, nr.endOffset);

    // Check viability of ranges.
    if (lp >= rp)
        return r;

    // Extract the range data.
    var ln = nr.startContainer;
    var lnp = nr.startOffset;
    var rn = nr.endContainer;
    var rnp = nr.endOffset;
    
    // Special case where the start and end locii are the same.
    if (ln == rn) {
        // Otherwise check if the parent span contains the class
        // to be removed.
        if ((ln.parentNode.nodeName != "SPAN") || !ln.parentNode.classList.contains (style))
            return r;
        // We split the text node on the right if need to.
        if (rnp < ln.textContent.length) {
            var n = rn.splitText (rnp);
            var p = EditorSupport._createSpan (rn.parentNode.classList);
            if (rn.parentNode.nextSibling == null)
                rn.parentNode.parentNode.appendChild (p);
            else
                rn.parentNode.parentNode.insertBefore (p, rn.parentNode.nextSibling);
            while (n != null) {
                var next = n.nextSibling;
                p.appendChild (n);
                n = next;
            }
        }
        // Here we need to split the text node on the left if need be.
        if (lnp > 0) {
            ln = ln.splitText (lnp);
            var n = EditorSupport._createSpan (ln.parentNode.classList);
            ln.parentNode.parentNode.insertBefore (n, ln.parentNode);
            var q = ln.previousSibling;
            do {
                var next = q.previousSibling
                n.appendChild (q);
                q = next;
            } while (q != null);
        }
        // Now remove the style and the node if no styles are present.
        ln.parentNode.classList.remove (style);
        if (ln.parentNode.classList.length == 0)
            EditorSupport._removeNode (ln.parentNode);

        // Coalesce and generate range.
        EditorSupport._coalesce (container);
        return EditorSupport._createRange (container, lp, rp);
    }

    // Deal with left node in a span that needs to be split. If at the end of the node
    // then we move to the next sibling.
    if (ln.parentNode != container) {
        if ((ln.parentNode.nodeName == "SPAN") && ln.parentNode.classList.contains (style)) {
            if ((ln.nextSibling == null) && (lnp >= ln.textContent.length))
                ln = ln.parentNode.nextSibling;
            else
                ln = EditorSupport._splitAt (ln, lnp);
        } else
            ln = ln.parentNode;
        lnp = 0;
    }

    // Deal with right node in a span that needs to be split.
    if (rn.parentNode != container) {
        if ((rn.parentNode.nodeName == "SPAN") && rn.parentNode.classList.contains (style)) {
            if ((rn.previousSibling == null) && (rnp == 0))
                rn = rn.parentNode.previousSibling;
            else
                rn = EditorSupport._splitAt (rn, rnp, true);
        } else
            rn = rn.parentNode;
        rnp = 0;
    }

    // Traverse from ln to rn.
    var end = false;
    do {
        end = (ln == rn);
        var next = ln.nextSibling;
        if ((ln.nodeName === "SPAN") && ln.classList.contains (style)) {
            ln.classList.remove (style);
            if (ln.classList.length == 0) {
                var c = ln.firstChild;
                while (c != null) {
                    n = c.nextSibling;
                    ln.parentNode.insertBefore (c,ln);
                    c = n;
                }
                ln.remove ();
            }
        }
        ln = next;
    } while (!end && (ln != null));

    EditorSupport._coalesce (container);
    return EditorSupport._createRange (container, lp, rp);
}

// Applys a style.
EditorSupport.apply = function(container,r,style) {
    // Resulting ranges will point to text nodes.
    nr = EditorSupport._normaliseRange(container,r);
    if  (nr == null)
        return r;
    var ln = nr.startContainer;
    var lnp = nr.startOffset;
    var rn = nr.endContainer;
    var rnp = nr.endOffset;

    // Get the range offsets (for later).
    var lp = EditorSupport._offset (container, ln, lnp);
    var rp = EditorSupport._offset (container, rn, rnp);

    // Check viability of ranges.
    if (lp >= rp)
        return r;

    // First stage is to eliminate the offsets.
    if (ln === rn) {
        if (rnp < rn.textContent.length)
            rn.splitText (rnp);
        if (lnp > 0)
            ln = ln.splitText (lnp);
        rn = ln;
    } else {
        if (lnp > 0)
            ln = ln.splitText (lnp);
        if (rnp < rn.textContent.length)
            rn.splitText (rnp);
    }

    // Next stage is to split any existing spans so the ln and
    // rn are top-level.
    var same = (ln == rn);
    if (ln.parentNode != container) {
        // First we ensure that ln is maximal to the right.
        if (!same) {
            while ((ln.nextSibling != null) && (ln.nextSibling != rn)) {
                ln.textContent += ln.nextSibling.textContent;
                ln.nextSibling.remove ();
            }
        }

        // Now split (or lift).
        ln = EditorSupport._split (ln);

        // If the ln and rn were the same, they should remain the same.
        if (same)
            rn = ln;
    }
    if (rn.parentNode != container) {
        // First we ensure than rn is maximal to the left. Note that
        // ln is already at the top.
        while (rn.previousSibling != null) {
            rn.textContent = rn.previousSibling.textContent += rn.textContent;
            rn.previousSibling.remove ();
        }

        // Now split the node (or lift).
        rn = EditorSupport._split (rn);
    }

    // Now ln and rn are directly below the parent. We just traverse
    // siblings merging and wrapping as we go.
    var finished = false;
    var merge = null;
    var n = ln;
    do {
        finished = (n == rn);
        var next = n.nextSibling;
        if (n.nodeType == 3) {
            if (merge == null) {
                merge = document.createElement ("span");
                merge.classList.add (style);
                n.parentNode.insertBefore (merge, n);
            }
            merge.appendChild (n);
            if (ln == null)
                ln = merge;
        } else {
            merge = null;
            if (n.nodeName === "SPAN") {
                n.classList.add (style);
                if (ln == null)
                    ln = n;
            }
        } 
        n = next;
    } while (!finished);

    EditorSupport._coalesce (container);
    return EditorSupport._createRange (container,lp,rp);
}

// Removes the node lifting the children to the parent.
EditorSupport._removeNode = function (node) {
    if (node.childNodes.length != 0) {
        var c = node.firstChild;
        while (c != null) {
            n = c.nextSibling;
            node.parentNode.insertBefore (c,node);
            c = n;
        }
    }
    node.remove ();
}

// Creates a range from the left and right offsets
// into the given container.
EditorSupport._createRange = function(container, lp, rp) {
    var r = new Range ();
    var nodes = EditorSupport._textNodes (container);
    var foundStart = false;
    var foundEnd = false;
    nodes.forEach (n => {
        if (!foundStart && (n.textContent.length >= lp)) {
            r.setStart (n, lp);
            foundStart = true;
        }
        if (!foundEnd && (n.textContent.length >= rp)) {
            r.setEnd (n, rp);
            foundEnd = true;
        }
        lp -= n.textContent.length;
        rp -= n.textContent.length;
    });
    return r;
}

// We want the range to point only to text nodes.
EditorSupport._normaliseRange = function(container,r) {
    // The assumption is that the range is contained in a element
    // that contains only text nodes or span nodes. The span nodes
    // contain only text nodes.
    var ln = r.startContainer;
    var lnp = r.startOffset;
    var rn = r.endContainer;
    var rnp = r.endOffset;

    // Ensure the range endpoints are valid.
    if ((ln == null) || (rn == null))
        return null;
    if ((ln != container) && (ln.parentNode != container) && (ln.parentNode.parentNode != container))
        return null;
    if ((rn != container) && (rn.parentNode != container) && (rn.parentNode.parentNode != container))
        return null;

    // Clean up the offsets.
    if ((rn.nodeType == 1) && (rnp > rn.childNodes.length))
        rnp = rn.childNodes.length;
    if ((rn.nodeType == 3) && (rnp > rn.textContent.length))
        rnp = rn.textContent.length;
    if ((ln.nodeType == 1) && (lnp > ln.childNodes.length))
        lnp = ln.childNodes.length;
    if ((ln.nodeType == 3) && (lnp > ln.textContent.length))
        lnp = ln.textContent.length;

    // If a range endpoint is the container then migrate down 
    // to the most appropriate child element.
    if (ln == container) {
        if (lnp >= ln.childNodes.length)
            return null;
        ln = ln.childNodes[lnp];
        lnp = 0;
    }
    if (rn == container) {
        if (rnp == 0)
            return null;
        rn = rn.childNodes[rnp - 1];
        rnp = (rn.nodeType == 3) ? rn.textContent.length : rn.childNodes.length;
    }

    // If any are BR's the migrate.
    while ((ln != null) && (ln.nodeName === "BR")) {
        ln = ln.nextSibling;
        lnp = 0;
    }
    if (ln == null)
        return null;
    while ((rn != null) && (rn.nodeName === "BR")) {
        rn = rn.previousSibling;
        rnp = (rn.nodeType == 3) ? rn.textContent.length : 0;
    }
    if (rn == null)
        return null;

    // If any are spans then migrate to the inner text.
    if ((ln.nodeName == "SPAN") && (ln.childNodes.length > 0)) {
        if (lnp >= ln.childNodes.length) {
            ln = ln.lastChild;
            lnp = ln.textContent.length;
        } else {
            ln = ln.childNodes[lnp]
            lnp = 0;
        }
    }
    if ((rn.nodeName == "SPAN") && (rn.childNodes.length > 0)) {
        if (rnp == 0) {
            rn = rn.firstChild;
            rnp = 0;
        } else {
            rn = rn.childNodes[rnp - 1]
            rnp = rn.textContent.length;
        }
    }

    r = new Range();
    r.setStart(ln, lnp);
    r.setEnd (rn, rnp);
    return r;
}

// Split the parent node at the child and at the position in
// the child. Returns the new node (to the right unless left is specified). If no node
// is created then the original is returned.
EditorSupport._splitAt = function (child, p, left) {
    var parent = child.parentNode;
    if ((child == null) || (child.nodeType != 3))
        return parent;
    if ((child.previousSibling == null) && (p == 0))
        return parent;
    if ((child.nextSibling == null) && (p >= child.textContent.length))
        return parent;
    child = child.splitText (p);
    var n = EditorSupport._createSpan (parent.classList);
    if (parent.nextSibling == null)
        parent.parentNode.appendChild (n);
    else
        parent.parentNode.insertBefore (n, parent.nextSibling);
    while (child != null) {
        var next = child.nextSibling;
        n.appendChild (child);
        child = next;
    }
    return (left) ? parent : n;
}

// Split a node so that the child is in its own. Return the
// span that the child now belongs to.
EditorSupport._split = function (child) {
    span = child.parentNode;
    if (span.childNodes.length == 1)
        return span;
    if (child.nextSibling != null) {
        var n = document.createElement ("span");
        if (span.nextSibling == null)
            span.parentNode.appendChild (n);
        else
            span.parentNode.insertBefore (n, span.nextSibling);
        for (var k = 0; k < span.classList.length; k++)
            n.classList.add (span.classList.item (k));
        while (child.nextSibling != null)
            n.appendChild (child.nextSibling);
    }
    if (span.childNodes.length == 1)
        return span;
    var n = document.createElement ("span");
    if (span.nextSibling == null)
        span.parentNode.appendChild (n);
    else
        span.parentNode.insertBefore (n, span.nextSibling);
    for (var k = 0; k < span.classList.length; k++)
        n.classList.add (span.classList.item (k));
    n.appendChild (child);
    return n;
}

EditorSupport._createSpan = function (classlist) {
    var n = document.createElement ("span");
    if (classlist) {
        for (var k = 0; k < classlist.length; k++)
            n.classList.add (classlist.item (k));
    }
    return n;
}

EditorSupport._sameClasses = function (n1,n2) {
    if ((n1 == null) || (n2 == null))
        return false;
    if ((n1.classList == null) || (n2.classList == null))
        return false;
    if (n1.classList.length != n2.classList.length)
        return false;
    var items = [];
    for (var i = 0; i < n1.classList.length; i++)
        items.push (n1.classList.item (i));
    for (var i = 0; i < n2.classList.length; i++) {
        var item = n2.classList.item (i);
        if (!items.includes (item))
            return false;
    }
    return true;
}

/**
 * Merges adjacent spans with the same classes, merges
 * text nodes and removes empty spans.
 */
EditorSupport._coalesce = function (el) {
    // First pass merges text nodes in spans.
    var n = el.firstChild;
    while (n != null) {
        if ((n.nodeType == 1) && (n.childNodes.length > 0)) {
            var c = n.firstChild;
            while (c.nextSibling != null) {
                c.textContent += c.nextSibling.textContent;
                c.nextSibling.remove ();
            }
            if (c.textContent.length == 0)
                c.remove ();
        }
        n = n.nextSibling;
    }

    // Merge adjacent spans.
    n = el.firstChild;
    while ((n != null) && (n.nextSibling != null)) {
        var l = n;
        var r = n.nextSibling;
        if ((l.nodeName === "SPAN") && (r.nodeName === "SPAN") && EditorSupport._sameClasses (l,r)) {
            var p = r.firstChild;
            while (p != null) {
                var q = p.nextSibling;
                l.appendChild (p);
                p = q;
            }
            r.remove ();
        } else
            n = n.nextSibling;
    }

    // Merge adjacent text nodes and remove any empty spans under the container.
    n = el.firstChild;
    while (n != null) {
        if ((n.childNodes.length == 0) && (n.nodeName === "SPAN")) {
            n = n.nextSibling;
            if (n != null)
                n.previousSibling.remove ();
        } else if ((n.nodeType == 3) && (n.textContent.length == 0)) {
            n = n.nextSibling;
            if (n != null)
                n.previousSibling.remove ();
        } else if ((n.nodeType == 3) && (n.nextSibling != null) && (n.nextSibling.nodeType == 3)) {
            n.textContent += n.nextSibling.textContent;
            n.nextSibling.remove ();
        } else {
            n = n.nextSibling;
        }
    }
}

EditorSupport._print = function(prefix,n) {
    if (n == null) {
        console.log (prefix + "null");
        return;
    }
    var i = 0;
    if (n.parentNode != null) {
        var m = n.parentNode.firstChild;
        while ((m != n) && (m != null)) {
            i++;
            m = m.nextSibling;
        }
    }
    if (n.nodeType == 3) {
        console.log (prefix + "Text[" + i + "][\"" + n.textContent + "\"]");
    } else {
        console.log (prefix + n.tagName + "[" + i + "]");
    }
}

EditorSupport.latex = function(el,text,displayMode) {
    try {
        katex.render (text, el, {
            throwOnError: true,
            displayMode: displayMode
        });
        return null;
    } catch (e) {
        if (e instanceof katex.ParseError)
            return e.message;
        return e;
    }
}

EditorSupport.diagram = function(baseurl,text) {
    return baseurl + plantumlEncoder.encode (text);
}

EditorSupport.paste = function(clipboard) {
    const validClasses = [ 'edt-b',' edt-u', 'edt-i', 'edt-sup', 'edt-sub', 'edt-code', 'edt-strike', 'edt-hl' ];
    console.log ("A1: " + clipboard);
    var doc = new DOMParser().parseFromString(clipboard, 'text/html');
    if (doc == null)
        return null;
    var n = document.createElement("div");
    var c = doc.body.firstChild;
    var t;
    while (c != null) {
        var next = c.nextSibling;
        if (c.tagName == "SPAN") {
            var klasses = [];
            for (var i = 0; i < c.classList.length; i++) {
                var cl = c.classList.item (i);
                if (validClasses.includes (cl))
                    klasses.push (cl);
            }
            if (klasses.length == 0) {
                if (t == null) {
                    t = document.createTextNode (EditorSupport._paste_text (c));
                    n.appendChild (t);
                } else
                    t.textContent += EditorSupport._paste_text (c);
            } else {
                t = null;
                var s = document.createElement ("SPAN");
                n.appendChild (s);
                s.textContent = EditorSupport._paste_text (c);
                klasses.forEach (cl => s.classList.add (cl));
            }
        } else {
            if (t == null) {
                t = document.createTextNode (EditorSupport._paste_text (c));
                n.appendChild (t);
            } else
                t.textContent += EditorSupport._paste_text (c);
        }
        c = c.nextSibling;
    }
    if (n.childNodes.length == 0)
        n.textContent = EditorSupport._paste_text (doc.body);
    return n;
}

EditorSupport._paste_text = function (n) {
    return n.textContent.replace ('\u00a0', ' ');
}