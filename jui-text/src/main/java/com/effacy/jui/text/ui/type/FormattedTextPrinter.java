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
package com.effacy.jui.text.ui.type;

import java.util.HashMap;
import java.util.Map;

import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.text.type.FormattedText;
import com.effacy.jui.text.ui.fragment.FText;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.HTMLImageElement;
import elemental2.dom.HTMLStyleElement;
import elemental2.dom.MutationObserver;
import elemental2.dom.MutationObserverInit;
import elemental2.dom.NodeList;
import jsinterop.base.Js;

/**
 * Prints a {@link FormattedText} — the document as the reader sees it, on paper (or
 * through the browser's <em>Save as PDF</em>, which is the same path).
 *
 * <pre>
 * FormattedTextPrinter.of (page)
 *     .title ("Operating model")
 *     .caption ("PAGE-7 · working copy · 6 Sep 2026")
 *     .print ();
 * </pre>
 *
 * <h2>It renders afresh rather than copying what is on screen</h2>
 *
 * The obvious implementation clones the rendered surface, and it is the wrong one here.
 * A surface on screen is a surface in whatever state its host left it — an editor with a
 * caret and a toolbar, a document showing the last thing that was rendered rather than
 * the current model, a pane whose Markdown view holds edits the rich view has not been
 * given. Rendering the model into the print container instead means what is printed is
 * the model, which is the only thing the caller can actually vouch for.
 *
 * <h2>The container is a child of {@code body}, and that is the whole trick</h2>
 *
 * A document in an application is laid out inside something: a scroller, a flex column, a
 * pane with a bounded height and a reading measure. Print it where it sits and the
 * browser prints the part that was visible, because the clipping ancestors clip on paper
 * too. The portal is appended directly to {@code body}, so it has no clipping ancestor,
 * no bounded height and no maximum width, and it paginates the way a plain document does.
 * It stays in the same document, so the stylesheets the renderer injects still apply and
 * the printed text is the text that was on screen.
 *
 * <h2>It waits for the document to finish drawing</h2>
 *
 * Fence renderers are not required to be synchronous and the interesting ones are not:
 * Mermaid fetches a library and resolves a promise, so a diagram's element is empty at
 * the moment the render call returns. Printing then would print the gaps. So the portal
 * is watched, and printing waits for it to go quiet — with a deadline, because a renderer
 * that never finishes must not mean a print that never happens. Images are waited on as
 * well: they are usually in cache and complete immediately, but "usually" prints a blank
 * box the one time it is not.
 *
 * <h2>The saved file's name</h2>
 *
 * Browsers name a <em>Save as PDF</em> after {@code document.title}, which in an
 * application is the application's name — so every document anyone saves arrives called
 * the same thing. The title is therefore set to the document's own name for the duration
 * of the print and put back afterwards. It is a suggestion, not a guarantee: browsers
 * sanitise it and the person saving can always type something else.
 *
 * <h2>What it does not do</h2>
 *
 * No running headers, no page numbers, no "page n of m" — those need {@code @page}
 * margin boxes and counters, whose support across browsers is not good enough to promise.
 * The title and caption are a block at the top of the first page, which is honest about
 * what it is. Nothing here is a substitute for server-side PDF generation where the
 * output must be produced without a browser or be byte-identical.
 */
public class FormattedTextPrinter {

    /************************************************************************
     * Construction.
     ************************************************************************/

    /**
     * Prints the given document.
     *
     * @param text
     *             the document (a {@code null} prints nothing).
     * @return the printer, to configure and then {@link #print()}.
     */
    public static FormattedTextPrinter of(FormattedText text) {
        return new FormattedTextPrinter (text);
    }

    private FormattedText text;

    private String title;

    private String caption;

    private ContentStyle contentStyle = ContentStyle.document ();

    private int topHeadingLevel = 1;

    private String filename;

    protected FormattedTextPrinter(FormattedText text) {
        this.text = text;
    }

    /**
     * A heading printed above the document, for a document whose title is chrome on
     * screen rather than part of the content (which is the usual case — a page's name is
     * in the application's header, and paper has no header).
     */
    public FormattedTextPrinter title(String title) {
        this.title = title;
        return this;
    }

    /**
     * A line beneath the title for what the reader of the paper cannot otherwise find
     * out: which revision this is, when it was taken, where it came from.
     * <p>
     * Worth filling in wherever the document is versioned. A printout with no such line
     * is indistinguishable from a printout of any other revision, which is how a stale
     * one ends up in circulation.
     */
    public FormattedTextPrinter caption(String caption) {
        this.caption = caption;
        return this;
    }

    /**
     * The content style, as {@link FText#contentStyle(ContentStyle)}. Defaults to
     * {@link ContentStyle#document()} — print is a document context by definition.
     */
    public FormattedTextPrinter contentStyle(ContentStyle contentStyle) {
        this.contentStyle = contentStyle;
        return this;
    }

    /**
     * The heading level the document's H1 maps to, as
     * {@link FText#topHeadingLevel(int)}.
     */
    public FormattedTextPrinter topHeadingLevel(int topHeadingLevel) {
        this.topHeadingLevel = topHeadingLevel;
        return this;
    }

    /**
     * The name to suggest for a saved PDF. Defaults to {@link #title(String)}, which is
     * normally what is wanted — set this only where the printed heading and the file
     * should differ (a reference number as the file name, say, with a prose heading on
     * the page).
     * <p>
     * A suggestion rather than a setting: it works by putting the name in
     * {@code document.title}, which is what browsers offer in the save dialog, and each
     * of them sanitises it in its own way. Characters that filesystems object to are
     * replaced here first, so what is offered is at least predictable.
     */
    public FormattedTextPrinter filename(String filename) {
        this.filename = filename;
        return this;
    }

    /************************************************************************
     * Printing.
     ************************************************************************/

    /**
     * Renders the document into the print portal and, once it has finished drawing,
     * opens the browser's print dialog. The portal is removed when printing ends.
     */
    public void print() {
        if (text == null)
            return;
        injectStyles ();
        // Defensive: a portal left behind by a print that never reported finishing would
        // otherwise be printed alongside this one.
        removePortal ();

        Element portal = DomGlobal.document.createElement ("div");
        portal.classList.add (PORTAL_STYLE);
        DomGlobal.document.body.appendChild (portal);
        Wrap.buildInto (portal, root -> {
            if (!StringSupport.empty (title) || !StringSupport.empty (caption)) {
                Div.$ (root).style (HEAD_STYLE).$ (h -> {
                    if (!StringSupport.empty (title))
                        Div.$ (h).style (TITLE_STYLE).text (title);
                    if (!StringSupport.empty (caption))
                        Div.$ (h).style (CAPTION_STYLE).text (caption);
                });
            }
            FText.$ (root, text)
                .contentStyle (contentStyle)
                .topHeadingLevel (topHeadingLevel);
        });

        whenSettled (portal, () -> {
            isolateSvgIds (portal);
            bindAfterPrint ();
            applyDocumentTitle ();
            DomGlobal.document.body.classList.add (BODY_STYLE);
            DomGlobal.window.print ();
        });
    }

    /**
     * Runs {@code then} once the portal has stopped changing and its images have loaded,
     * or once the deadline passes — whichever comes first.
     * <p>
     * A quiet period rather than a completion signal, because there is no completion
     * signal to have: {@link com.effacy.jui.text.ui.editor.IFenceRenderer} draws into an
     * element and reports nothing, and adding a contract for it would oblige every
     * renderer to implement one to be printable. Watching the container instead costs
     * those renderers nothing and covers whatever is registered next.
     * <p>
     * The deadline is what makes that safe. A renderer that fails silently — a CDN that
     * does not answer, a promise that never settles — would otherwise take the print
     * with it; past the deadline the document prints with a gap where the diagram was,
     * which is a worse printout but is still a printout.
     */
    private void whenSettled(Element portal, Runnable then) {
        long[] lastChange = { System.currentTimeMillis () };
        MutationObserver observer = new MutationObserver ((records, obs) -> {
            lastChange[0] = System.currentTimeMillis ();
            return null;
        });
        MutationObserverInit options = MutationObserverInit.create ();
        options.setChildList (true);
        options.setSubtree (true);
        options.setCharacterData (true);
        observer.observe (portal, options);

        long start = System.currentTimeMillis ();
        Runnable[] tick = new Runnable[1];
        tick[0] = () -> DomGlobal.setTimeout (args -> {
            long now = System.currentTimeMillis ();
            boolean quiet = (now - lastChange[0]) >= QUIET_MS;
            boolean overdue = (now - start) >= DEADLINE_MS;
            if (overdue || (quiet && imagesReady (portal))) {
                observer.disconnect ();
                then.run ();
                return;
            }
            tick[0].run ();
        }, POLL_MS);
        tick[0].run ();
    }

    /**
     * Makes the ids inside the portal's SVGs unique to this print, so the printed
     * graphics reference their own definitions rather than somebody else's.
     * <p>
     * <b>An SVG's internal references are document-wide.</b> A diagram renderer emits
     * markers, gradients and clip paths into {@code <defs>} under fixed names — Mermaid's
     * entity-relationship arrowheads are {@code ONLY_ONE_END}, {@code ZERO_OR_ONE_START}
     * and so on — and refers to them as {@code url(#ONLY_ONE_END)}. That resolves against
     * the whole document, taking the <em>first</em> match. Print the same document twice
     * over (once on screen, once here) and the printed copy's arrowheads point at the
     * on-screen copy's definitions — which are, at that moment, inside a subtree the print
     * stylesheet has set to {@code display: none}. The lines print; what marks their ends
     * does not.
     * <p>
     * Renaming every descendant id and rewriting the references to match leaves the
     * printed SVG self-contained, whatever else the page happens to hold.
     * <p>
     * The root {@code <svg>} element's own id is deliberately left alone: a renderer
     * typically scopes the stylesheet it embeds by that id ({@code #mmd-3 .entityBox
     * {...}}), and renaming it would take the diagram's colours with it. Nothing in
     * {@code <defs>} is referenced from that stylesheet, so the two do not meet.
     */
    private static void isolateSvgIds(Element portal) {
        NodeList<Element> graphics = portal.querySelectorAll ("svg");
        for (int i = 0; i < graphics.length; i++) {
            Element svg = graphics.getAt (i);
            String prefix = "jtp" + (SEQ++) + "-";
            Map<String, String> renamed = new HashMap<> ();
            NodeList<Element> identified = svg.querySelectorAll ("[id]");
            for (int j = 0; j < identified.length; j++) {
                Element el = identified.getAt (j);
                String id = el.getAttribute ("id");
                if (StringSupport.empty (id))
                    continue;
                String replacement = prefix + id;
                renamed.put (id, replacement);
                el.setAttribute ("id", replacement);
            }
            if (renamed.isEmpty ())
                continue;
            NodeList<Element> all = svg.querySelectorAll ("*");
            for (int j = 0; j < all.length; j++)
                rewriteReferences (all.getAt (j), renamed);
        }
    }

    /**
     * The attributes through which one SVG element names another. Enumerated rather than
     * scanning every attribute because a rewrite is a destructive act and the set of
     * attributes that carry a reference is small, known and stable.
     */
    private static final String[] REFERENCE_ATTRIBUTES = {
        "marker-start", "marker-mid", "marker-end",
        "fill", "stroke", "clip-path", "mask", "filter",
        "href", "xlink:href", "style"
    };

    private static void rewriteReferences(Element el, Map<String, String> renamed) {
        for (String attribute : REFERENCE_ATTRIBUTES) {
            String value = el.getAttribute (attribute);
            if (StringSupport.empty (value) || (value.indexOf ('#') < 0))
                continue;
            String updated = value;
            for (Map.Entry<String, String> entry : renamed.entrySet ()) {
                // Both delimited forms, so an id that is a prefix of another id is not
                // caught by it: "url(#a)" never matches inside "url(#ab)", and the bare
                // form is only ever the whole value.
                updated = updated.replace ("url(#" + entry.getKey () + ")", "url(#" + entry.getValue () + ")");
                if (updated.equals ("#" + entry.getKey ()))
                    updated = "#" + entry.getValue ();
            }
            if (!updated.equals (value))
                el.setAttribute (attribute, updated);
        }
    }

    /** Distinguishes one print's renamed ids from the next's. */
    private static int SEQ = 0;

    /**
     * Whether every image in the portal has loaded. A fresh {@code img} for a source the
     * page already showed is normally complete within a frame (it is in cache), but an
     * incomplete one prints as an empty box.
     */
    private static boolean imagesReady(Element portal) {
        NodeList<Element> images = portal.querySelectorAll ("img");
        for (int i = 0; i < images.length; i++) {
            HTMLImageElement image = Js.uncheckedCast (images.getAt (i));
            if (!image.complete)
                return false;
        }
        return true;
    }

    /************************************************************************
     * The portal, and the stylesheet that reveals it.
     ************************************************************************/

    /** The print container, hidden on screen and the only thing shown in print. */
    private static final String PORTAL_STYLE = "juiTextPrintPortal";

    /** Marks {@code body} while printing, so the sheet below is inert the rest of the time. */
    private static final String BODY_STYLE = "juiTextPrinting";

    private static final String HEAD_STYLE = "juiTextPrintHead";
    private static final String TITLE_STYLE = "juiTextPrintTitle";
    private static final String CAPTION_STYLE = "juiTextPrintCaption";

    /** How long the portal must go unchanged before it counts as drawn. */
    private static final int QUIET_MS = 150;

    /** How long to wait for a renderer that may never finish. */
    private static final int DEADLINE_MS = 5000;

    private static final int POLL_MS = 60;

    private static boolean STYLES_INJECTED = false;

    private static boolean AFTER_PRINT_BOUND = false;

    /**
     * The print stylesheet.
     * <p>
     * A plain sheet in the document head rather than a {@code CssResource}, so the class
     * names it uses are the literal ones this class puts on the DOM. Everything is gated
     * on {@code body.juiTextPrinting}, so an ordinary Ctrl+P anywhere else in the
     * application is untouched by it.
     * <p>
     * The rules inside the portal address elements by <b>tag</b> rather than by class.
     * The rendered content's classes belong to the renderer and could be renamed or
     * scoped; its tags are the point of rendering to semantic HTML in the first place,
     * and they are what the paging rules are really about.
     */
    private static final String PRINT_CSS = """
.juiTextPrintPortal { display: none; }
@media print {
    @page { margin: 16mm; }

    body.juiTextPrinting > *:not(.juiTextPrintPortal) { display: none !important; }
    body.juiTextPrinting .juiTextPrintPortal { display: block !important; }

    /* The document was laid out inside a scroller with a bounded height and a reading
       measure. On paper the page is the constraint, and a clip is a truncation.

       Addressed by tag, and NEVER as `*`. A blanket rule reaches inside SVG, where
       `height` is geometry rather than layout: `height: auto` on a `<rect>` resolves to
       zero, so every box in a diagram silently disappears while its text and connecting
       lines print normally. Nothing in an SVG needs releasing — it has no scrollers and
       no reading measure — so nothing in an SVG is matched here. */
    body.juiTextPrinting .juiTextPrintPortal > *,
    body.juiTextPrinting .juiTextPrintPortal div,
    body.juiTextPrinting .juiTextPrintPortal p,
    body.juiTextPrinting .juiTextPrintPortal pre,
    body.juiTextPrinting .juiTextPrintPortal table,
    body.juiTextPrinting .juiTextPrintPortal ul,
    body.juiTextPrinting .juiTextPrintPortal ol,
    body.juiTextPrinting .juiTextPrintPortal blockquote,
    body.juiTextPrinting .juiTextPrintPortal figure {
        overflow: visible !important;
        max-height: none !important;
        max-width: none !important;
    }

    /* There is no sideways scroll on paper: an unwrapped code line is simply lost at
       the page edge. */
    body.juiTextPrinting .juiTextPrintPortal pre,
    body.juiTextPrinting .juiTextPrintPortal code {
        white-space: pre-wrap !important;
        word-break: break-word !important;
    }

    /* A heading stranded at the foot of a page is the commonest bad break, and the
       cheapest to prevent. */
    body.juiTextPrinting .juiTextPrintPortal h1,
    body.juiTextPrinting .juiTextPrintPortal h2,
    body.juiTextPrinting .juiTextPrintPortal h3,
    body.juiTextPrinting .juiTextPrintPortal h4,
    body.juiTextPrinting .juiTextPrintPortal h5,
    body.juiTextPrinting .juiTextPrintPortal h6 {
        break-after: avoid;
        break-inside: avoid;
    }

    /* Things that read as one object and are ruined by being halved. */
    body.juiTextPrinting .juiTextPrintPortal table,
    body.juiTextPrinting .juiTextPrintPortal figure,
    body.juiTextPrinting .juiTextPrintPortal pre,
    body.juiTextPrinting .juiTextPrintPortal blockquote,
    body.juiTextPrinting .juiTextPrintPortal li,
    body.juiTextPrinting .juiTextPrintPortal svg {
        break-inside: avoid;
    }

    /* Graphics are sized to the column they sat in. Overrides the blanket max-width
       release above, which is why it comes after it. */
    body.juiTextPrinting .juiTextPrintPortal img,
    body.juiTextPrinting .juiTextPrintPortal svg {
        max-width: 100% !important;
        height: auto !important;
    }

    /* Code grounds and callout washes carry meaning; browsers drop backgrounds by
       default. */
    body.juiTextPrinting .juiTextPrintPortal,
    body.juiTextPrinting .juiTextPrintPortal * {
        -webkit-print-color-adjust: exact;
        print-color-adjust: exact;
    }

    .juiTextPrintHead {
        margin: 0 0 1.4em;
        padding-bottom: 0.7em;
        border-bottom: 1px solid #999;
    }
    .juiTextPrintTitle {
        font-size: 1.5em;
        font-weight: 600;
        line-height: 1.3;
    }
    .juiTextPrintCaption {
        margin-top: 0.35em;
        font-size: 0.85em;
        color: #555;
    }
}
""";

    private static void injectStyles() {
        if (STYLES_INJECTED)
            return;
        STYLES_INJECTED = true;
        HTMLStyleElement style = Js.uncheckedCast (DomGlobal.document.createElement ("style"));
        style.textContent = PRINT_CSS;
        DomGlobal.document.head.appendChild (style);
    }

    /**
     * Cleans up when printing ends. Bound once and left bound: {@code afterprint} is the
     * only signal that the dialog has gone, and it is fired whether the user printed or
     * cancelled.
     */
    private static void bindAfterPrint() {
        if (AFTER_PRINT_BOUND)
            return;
        AFTER_PRINT_BOUND = true;
        DomGlobal.window.addEventListener ("afterprint", e -> {
            DomGlobal.document.body.classList.remove (BODY_STYLE);
            restoreDocumentTitle ();
            removePortal ();
        });
    }

    /**
     * The document title while the print dialog is open, or {@code null} when it has not
     * been changed.
     * <p>
     * Held statically because it is put back by the {@code afterprint} listener, which is
     * bound once and outlives any one printer.
     */
    private static String PRIOR_TITLE = null;

    /**
     * Lends the document the printed document's name, so a saved PDF arrives called
     * something other than the application.
     * <p>
     * Only for as long as the dialog is open. The title is the tab's name as well, so
     * leaving it changed would rename the application on the strength of one page having
     * been printed from it.
     */
    private void applyDocumentTitle() {
        String name = sanitiseFilename (StringSupport.empty (filename) ? title : filename);
        if (StringSupport.empty (name))
            return;
        PRIOR_TITLE = DomGlobal.document.title;
        DomGlobal.document.title = name;
    }

    private static void restoreDocumentTitle() {
        if (PRIOR_TITLE == null)
            return;
        DomGlobal.document.title = PRIOR_TITLE;
        PRIOR_TITLE = null;
    }

    /**
     * Makes a document name safe to be offered as a file name: the characters filesystems
     * and browsers object to become spaces, runs of whitespace collapse, and the result is
     * capped.
     * <p>
     * Replaced rather than removed, because a name is normally words: dropping the
     * separator between two of them runs them together, where a space leaves the name
     * readable.
     */
    private static String sanitiseFilename(String name) {
        if (StringSupport.empty (name))
            return null;
        StringBuilder sb = new StringBuilder ();
        for (int i = 0; i < name.length (); i++) {
            char c = name.charAt (i);
            if ((c < ' ') || (ILLEGAL_IN_FILENAME.indexOf (c) >= 0))
                sb.append (' ');
            else
                sb.append (c);
        }
        String cleaned = sb.toString ().replaceAll ("\\s+", " ").trim ();
        if (cleaned.length () > FILENAME_LIMIT)
            cleaned = cleaned.substring (0, FILENAME_LIMIT).trim ();
        return cleaned.isEmpty () ? null : cleaned;
    }

    private static final String ILLEGAL_IN_FILENAME = "/\\:*?\"<>|";

    /** Long enough for any reasonable document name, short of what a filesystem refuses. */
    private static final int FILENAME_LIMIT = 120;

    private static void removePortal() {
        Element existing = DomGlobal.document.querySelector ("." + PORTAL_STYLE);
        if (existing != null)
            existing.remove ();
    }

}
