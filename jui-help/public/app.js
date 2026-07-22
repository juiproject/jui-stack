/* ============================================================================
   The help system runtime — a tiny hash-routed single-page app.

   Reads only the globals loaded by index.html:
     window.HELP            (content.generated.js — config, categories, articles)
     window.ICONS / icon()  (icons.js)
     window.renderMarkdown   (md.js)

   Routes:
     #/            home — search + browse by area + popular
     #/c/<key>     a category listing
     #/a/<slug>    an article (markdown rendered on the fly, with a contents rail)

   No network calls: everything is already in memory, so it runs from a static
   server or straight from file://.
   ============================================================================ */
(function () {

    var CFG = HELP.config || {};
    var CATS = HELP.categories || [];
    var ARTS = HELP.articles || [];

    function cat(key) { return CATS.filter(function (c) { return c.key === key; })[0]; }
    function catTitle(key) { var c = cat(key); return c ? c.title : ''; }
    function bySlug(slug) { return ARTS.filter(function (a) { return a.slug === slug; })[0]; }
    function inCat(key) { return ARTS.filter(function (a) { return a.category === key; }); }
    function el(id) { return document.getElementById(id); }

    /* ---------- fragments ---------- */

    function rowHtml(a) {
        return '<a class="art-row" href="#/a/' + a.slug + '">' +
            '<div class="ic">' + icon(a.icon) + '</div>' +
            '<div><p class="t">' + a.title + '</p>' +
            '<p class="m">' + catTitle(a.category) + (a.description ? ' · ' + a.description : '') + '</p></div>' +
            '<span class="go">' + icon('arrow-right') + '</span></a>';
    }

    function catCardHtml(c) {
        var n = inCat(c.key).length;
        return '<a class="cat-card" href="#/c/' + c.key + '">' +
            '<div class="ic">' + icon(c.icon) + '</div>' +
            '<div><p class="nm">' + c.title + '</p>' +
            '<p class="ds">' + (c.description || '') + '</p>' +
            '<span class="ct">' + n + ' article' + (n === 1 ? '' : 's') + '</span></div></a>';
    }

    function crumb(parts) {
        return '<nav class="crumb">' + parts.map(function (p, i) {
            var sep = i > 0 ? icon('chevron-right') : '';
            return p[1] ? sep + '<a href="' + p[1] + '">' + p[0] + '</a>'
                : sep + '<span class="cur">' + p[0] + '</span>';
        }).join('') + '</nav>';
    }

    /* ---------- header ---------- */

    function renderHeader() {
        var back = CFG.backUrl
            ? '<a class="back" href="' + CFG.backUrl + '">' + icon('arrow-left') + '<span>' + (CFG.backLabel || 'Back') + '</span></a>'
            : '';
        el('top').innerHTML =
            '<a class="brand" href="#/"><span class="mark">' + icon('lifebuoy') + '</span>' +
            '<span>' + (CFG.title || 'Help') + '</span></a><div class="spring"></div>' + back;
        document.title = CFG.title || 'Help';
    }

    /* ---------- views ---------- */

    function renderHome() {
        el('view').innerHTML =
            '<section class="hero"><div class="inner">' +
            '<span class="eyebrow">' + (CFG.title || 'Help center') + '</span>' +
            '<h1>How can we <span class="serif">help?</span></h1>' +
            '<p>' + (CFG.tagline || '') + '</p>' +
            '<div class="searchbar">' + icon('search') +
            '<input id="q" placeholder="Search help…"></div>' +
            '</div></section><div class="wrap"><div id="browse"></div></div>';

        var q = el('q');
        q.value = state.q;
        q.addEventListener('input', function () { state.q = q.value.trim(); renderBrowse(); });
        renderBrowse();
        if (state.q) q.focus();
    }

    function renderBrowse() {
        var b = el('browse');
        if (state.q) {
            var ql = state.q.toLowerCase();
            var hits = ARTS.filter(function (a) {
                return (a.title + ' ' + a.description + ' ' + catTitle(a.category)).toLowerCase().indexOf(ql) >= 0;
            });
            b.innerHTML = '<div class="sec-title"><h2>' + hits.length + ' result' + (hits.length === 1 ? '' : 's') +
                ' for “' + state.q + '”</h2></div>' +
                (hits.length ? '<div class="panel">' + hits.map(rowHtml).join('') + '</div>'
                    : '<p class="empty">No articles match “' + state.q + '”. Try another word.</p>');
            return;
        }
        var popular = (CFG.popular || []).map(bySlug).filter(Boolean);
        b.innerHTML =
            '<div class="sec-title"><h2>Browse by area</h2></div>' +
            '<div class="cat-grid">' + CATS.map(catCardHtml).join('') + '</div>' +
            (popular.length
                ? '<div class="sec-title" style="margin-top:2.4rem"><h2>Popular articles</h2></div>' +
                  '<div class="panel">' + popular.map(rowHtml).join('') + '</div>'
                : '');
    }

    function renderCategory(key) {
        var c = cat(key);
        if (!c) return renderHome();
        el('view').innerHTML =
            '<div class="wrap">' +
            crumb([['Help center', '#/'], [c.title, null]]) +
            '<header class="cat-head"><div class="ic">' + icon(c.icon) + '</div>' +
            '<div><h1>' + c.title + '</h1><p>' + (c.description || '') + '</p></div></header>' +
            '<div class="panel">' + inCat(key).map(rowHtml).join('') + '</div></div>';
    }

    function renderArticle(slug) {
        var a = bySlug(slug);
        if (!a) {
            el('view').innerHTML = '<div class="wrap"><p class="empty">Article not found. <a href="#/">Back to the help center</a>.</p></div>';
            return;
        }
        var c = cat(a.category);
        var related = a.related.map(bySlug).filter(Boolean);

        el('view').innerHTML =
            '<div class="wrap">' +
            crumb([['Help center', '#/'], [c.title, '#/c/' + c.key], [a.title, null]]) +
            '<div class="cols">' +
            '<div><article>' +
            '<span class="eyebrow">' + c.title + '</span>' +
            '<h1>' + a.title + '</h1>' +
            (a.updated ? '<div class="meta"><span>' + a.updated + '</span></div>' : '') +
            '<div id="body">' + renderMarkdown(a.body) + '</div>' +
            '</article>' +
            '<div class="helpful"><span>Was this article helpful?</span>' +
            '<span class="btn ghost small">' + icon('thumb-up') + '<span>Yes</span></span>' +
            '<span class="btn ghost small">No</span></div>' +
            (related.length ? '<div class="related"><h3>Related articles</h3><div class="panel">' +
                related.map(rowHtml).join('') + '</div></div>' : '') +
            '</div>' +
            '<nav class="toc" id="toc"></nav>' +
            '</div></div>';

        buildToc();
    }

    /* Build the contents rail from the rendered section headings, and highlight
       the section in view as the reader scrolls. */
    function buildToc() {
        var heads = [].slice.call(document.querySelectorAll('#body h2[id]'));
        var toc = el('toc');
        if (!heads.length) { toc.style.display = 'none'; return; }
        toc.innerHTML = '<p class="lbl">On this page</p>' +
            heads.map(function (h, i) {
                return '<a data-sec="' + h.id + '"' + (i === 0 ? ' class="on"' : '') + '>' + h.textContent + '</a>';
            }).join('') +
            '<div class="side"><a href="#/">' + icon('arrow-left') + '<span>All help</span></a></div>';

        if (!('IntersectionObserver' in window)) return;
        var links = {};
        toc.querySelectorAll('a[data-sec]').forEach(function (l) { links[l.dataset.sec] = l; });
        var obs = new IntersectionObserver(function (entries) {
            entries.forEach(function (e) {
                if (!e.isIntersecting) return;
                Object.keys(links).forEach(function (k) { links[k].classList.remove('on'); });
                if (links[e.target.id]) links[e.target.id].classList.add('on');
            });
        }, { rootMargin: '-10% 0px -70% 0px' });
        heads.forEach(function (h) { obs.observe(h); });
    }

    /* ---------- router ---------- */

    var state = { q: '' };

    function render() {
        var parts = (location.hash || '#/').replace(/^#/, '').split('#')[0].split('/').filter(Boolean);
        window.scrollTo(0, 0);
        if (parts[0] === 'a' && parts[1]) return renderArticle(parts[1]);
        if (parts[0] === 'c' && parts[1]) return renderCategory(parts[1]);
        renderHome();
    }

    // Table-of-contents clicks scroll without touching the router hash.
    document.addEventListener('click', function (e) {
        var sec = e.target.closest('.toc a[data-sec]');
        if (sec) {
            e.preventDefault();
            var t = document.getElementById(sec.dataset.sec);
            if (t) t.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
    });

    window.addEventListener('hashchange', render);

    function start() { renderHeader(); render(); }
    if (document.readyState === 'loading')
        window.addEventListener('DOMContentLoaded', start);
    else
        start();

})();
