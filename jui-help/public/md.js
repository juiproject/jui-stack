/* ============================================================================
   Minimal, dependency-free markdown -> HTML renderer.

   Supports the subset a help site needs: headings (with ids for the table of
   contents), paragraphs, bold / italic / inline code, links, images, ordered and
   unordered lists, blockquotes (rendered as callouts), fenced code blocks,
   tables and horizontal rules. Deliberately small and predictable.

   Bare links like [text](my-slug) — no scheme, slash or dot — resolve to another
   article (#/a/my-slug). Anything with a scheme, a slash, a dot, or a leading #
   is left untouched (external links, assets/…, anchors).

   Usable in the browser (window.renderMarkdown) and in Node (module.exports),
   so the build could pre-render if ever wanted.
   ============================================================================ */
(function (root) {

    // Private-use sentinels wrapping code-span placeholders (kept out of literals
    // so they can never collide with authored text).
    var C0 = String.fromCharCode(0xE000);
    var C1 = String.fromCharCode(0xE001);

    function escapeHtml(s) {
        return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    }

    function slugify(s) {
        return s.toLowerCase().replace(/`/g, '').replace(/[*_]/g, '')
            .trim().replace(/[^\w]+/g, '-').replace(/^-+|-+$/g, '');
    }

    function resolveHref(href) {
        if (/^(https?:|mailto:|tel:|#|\/|assets\/)/.test(href))
            return href;
        if (href.indexOf('/') >= 0 || href.indexOf('.') >= 0)
            return href;                         // a relative path / asset
        return '#/a/' + href;                    // a bare article slug
    }

    /* ---- inline ---- */
    function inline(text) {
        var t = escapeHtml(text);
        var codes = [];
        t = t.replace(/`([^`]+)`/g, function (m, c) { codes.push('<code>' + c + '</code>'); return C0 + (codes.length - 1) + C1; });
        t = t.replace(/!\[([^\]]*)\]\(([^)\s]+)\)/g, function (m, alt, src) { return '<img src="' + src + '" alt="' + alt + '">'; });
        t = t.replace(/\[([^\]]+)\]\(([^)\s]+)\)/g, function (m, txt, href) { return '<a href="' + resolveHref(href) + '">' + txt + '</a>'; });
        t = t.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');
        t = t.replace(/__([^_]+)__/g, '<strong>$1</strong>');
        t = t.replace(/\*([^*]+)\*/g, '<em>$1</em>');
        t = t.replace(/(^|[^\w])_([^_]+)_(?=[^\w]|$)/g, '$1<em>$2</em>');
        t = t.replace(new RegExp(C0 + '(\\d+)' + C1, 'g'), function (m, n) { return codes[+n]; });
        return t;
    }

    var isBlank = function (l) { return /^\s*$/.test(l); };
    var isHeading = function (l) { return /^#{1,6}\s+/.test(l); };
    var isHr = function (l) { return /^\s*(-{3,}|\*{3,}|_{3,})\s*$/.test(l); };
    var isFence = function (l) { return /^\s*```/.test(l); };
    var isQuote = function (l) { return /^\s*>/.test(l); };
    var isUl = function (l) { return /^\s*[-*+]\s+/.test(l); };
    var isOl = function (l) { return /^\s*\d+\.\s+/.test(l); };
    var isBlockStart = function (l) { return isHeading(l) || isHr(l) || isFence(l) || isQuote(l) || isUl(l) || isOl(l); };

    function isTable(lines, i) {
        return (lines[i].indexOf('|') >= 0) && (i + 1 < lines.length)
            && /^\s*\|?[\s:|-]*-[\s:|-]*$/.test(lines[i + 1]) && (lines[i + 1].indexOf('-') >= 0);
    }

    function cells(row) {
        return row.replace(/^\s*\|/, '').replace(/\|\s*$/, '').split('|').map(function (c) { return c.trim(); });
    }

    function renderMarkdown(src) {
        var lines = String(src).replace(/\r\n?/g, '\n').split('\n');
        var out = [];
        var i = 0;

        while (i < lines.length) {
            var line = lines[i];

            if (isBlank(line)) { i++; continue; }

            if (isFence(line)) {
                var buf = [];
                i++;
                while ((i < lines.length) && !isFence(lines[i])) { buf.push(lines[i]); i++; }
                i++;                                 // closing fence
                out.push('<pre><code>' + escapeHtml(buf.join('\n')) + '</code></pre>');
                continue;
            }

            var h = /^(#{1,6})\s+(.*)$/.exec(line);
            if (h) {
                var level = h[1].length;
                out.push('<h' + level + ' id="' + slugify(h[2]) + '">' + inline(h[2].trim()) + '</h' + level + '>');
                i++;
                continue;
            }

            if (isHr(line)) { out.push('<hr>'); i++; continue; }

            if (isQuote(line)) {
                var q = [];
                while ((i < lines.length) && isQuote(lines[i])) { q.push(lines[i].replace(/^\s*>\s?/, '')); i++; }
                out.push('<blockquote class="callout">' + renderMarkdown(q.join('\n')) + '</blockquote>');
                continue;
            }

            if (isTable(lines, i)) {
                var head = cells(lines[i]);
                i += 2;                              // header + separator
                var tbody = [];
                while ((i < lines.length) && (lines[i].indexOf('|') >= 0) && !isBlank(lines[i])) {
                    tbody.push(cells(lines[i])); i++;
                }
                var tbl = '<table><thead><tr>' + head.map(function (c) { return '<th>' + inline(c) + '</th>'; }).join('') + '</tr></thead><tbody>';
                tbody.forEach(function (r) { tbl += '<tr>' + r.map(function (c) { return '<td>' + inline(c) + '</td>'; }).join('') + '</tr>'; });
                out.push(tbl + '</tbody></table>');
                continue;
            }

            if (isUl(line) || isOl(line)) {
                var ordered = isOl(line);
                var match = ordered ? isOl : isUl;
                var strip = ordered ? /^\s*\d+\.\s+/ : /^\s*[-*+]\s+/;
                var items = [];
                while ((i < lines.length) && match(lines[i])) {
                    items.push(inline(lines[i].replace(strip, ''))); i++;
                }
                var tag = ordered ? 'ol' : 'ul';
                out.push('<' + tag + '>' + items.map(function (t) { return '<li>' + t + '</li>'; }).join('') + '</' + tag + '>');
                continue;
            }

            // paragraph
            var para = [];
            while ((i < lines.length) && !isBlank(lines[i]) && !isBlockStart(lines[i])) {
                para.push(lines[i]); i++;
            }
            out.push('<p>' + inline(para.join(' ')) + '</p>');
        }

        return out.join('\n');
    }

    root.renderMarkdown = renderMarkdown;
    if (typeof module !== 'undefined' && module.exports)
        module.exports = { renderMarkdown };

})(typeof window !== 'undefined' ? window : globalThis);
