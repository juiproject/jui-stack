package com.effacy.jui.ui.client.fragments;

import java.util.function.Consumer;

import com.effacy.jui.core.client.dom.builder.ContainerBuilder;
import com.effacy.jui.core.client.dom.builder.Details;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.FragmentWithChildren;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.core.client.dom.builder.IFragmentCSS;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.builder.Summary;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;

/**
 * A collapsible panel with a header row (caret, optional icon, title and
 * optional summary slot) and a body that contains whatever children are
 * inserted into the fragment.
 * <p>
 * The collapse/expand behaviour is provided by the native HTML
 * {@code <details>} element — no script is required.
 * <p>
 * A dynamic summary slot below the title may be filled with any builder
 * content. Callers that need to refresh this content as the enclosed state
 * changes should capture the summary container via
 * {@code summary(s -> s.use(n -> ref = (Element) n))} and rebuild it on demand.
 */
public class Accordion extends FragmentWithChildren<Accordion> {

    public static Accordion $() {
        return new Accordion();
    }

    public static Accordion $(IDomInsertableContainer<?> parent) {
        Accordion frg = $();
        if (parent != null)
            parent.insert(frg);
        return frg;
    }

    private String title;

    private String icon;

    private boolean open = true;

    private boolean error = false;

    private boolean collapsible = true;

    private boolean visible = true;

    private boolean header = true;

    private Consumer<ElementBuilder> summary;

    private Element rootEl;

    private Element summaryEl;

    /**
     * The title shown in the header.
     */
    public Accordion title(String title) {
        this.title = title;
        return this;
    }

    /**
     * Optional icon (CSS class, typically from FontAwesome) shown beside the
     * title.
     */
    public Accordion icon(String icon) {
        this.icon = icon;
        return this;
    }

    /**
     * Opens or closes the accordion. When called before the fragment has been
     * rendered this acts as the initial state (default {@code true}); when
     * called after render the change is applied directly to the underlying
     * {@code <details>} element.
     * <p>
     * No-op when the accordion is non-collapsible
     * (see {@link #collapsible(boolean)}) — such accordions are always open by
     * definition.
     */
    public Accordion open(boolean open) {
        if (!collapsible) {
            this.open = true;
            return this;
        }
        this.open = open;
        if (rootEl != null) {
            if (open)
                rootEl.setAttribute("open", "");
            else
                rootEl.removeAttribute("open");
        }
        return this;
    }

    /**
     * Determines whether the accordion can be collapsed/expanded by the user.
     * When {@code false} the accordion is held open, the caret and the
     * collapse/expand interaction are suppressed, and the fragment renders as
     * a static "section card" with the same look. Default {@code true}.
     */
    public Accordion collapsible(boolean collapsible) {
        this.collapsible = collapsible;
        if (!collapsible)
            this.open = true;
        if (rootEl != null) {
            if (collapsible) {
                rootEl.classList.remove(styles().fixed());
            } else {
                rootEl.classList.add(styles().fixed());
                rootEl.setAttribute("open", "");
            }
        }
        return this;
    }

    /**
     * Determines whether the header (caret + icon + title + summary slot) is
     * rendered. When {@code false} the fragment renders only the body — useful
     * for a plain card with the section's framing but no heading. Default
     * {@code true}.
     */
    public Accordion header(boolean header) {
        this.header = header;
        if (rootEl != null) {
            if (header)
                rootEl.classList.remove(styles().noHeader());
            else
                rootEl.classList.add(styles().noHeader());
        }
        return this;
    }

    /**
     * Toggles the rendered visibility of the fragment via {@code display: none}.
     * Useful when a containing form needs to gate one accordion behind a choice
     * made elsewhere (e.g. only show after a type has been picked). Default
     * {@code true}.
     */
    public Accordion visible(boolean visible) {
        this.visible = visible;
        if (rootEl != null) {
            if (visible)
                rootEl.classList.remove(styles().hidden());
            else
                rootEl.classList.add(styles().hidden());
        }
        return this;
    }

    /**
     * Marks the accordion as being in an error state — typically used to draw
     * attention to a section that contains invalid content. Recolours the
     * border, caret, icon and title via the {@code --cpt-accordion-error-*}
     * tokens. When called before the fragment has been rendered this acts as
     * the initial state (default {@code false}); when called after render the
     * change is applied directly to the underlying element.
     */
    public Accordion error(boolean error) {
        this.error = error;
        if (rootEl != null) {
            if (error)
                rootEl.classList.add(styles().error());
            else
                rootEl.classList.remove(styles().error());
        }
        return this;
    }

    /**
     * Builder for the content shown immediately below the title. Useful for
     * showing a summary of the enclosed content (e.g. "Manager · Peer · 2 of
     * 6"). The consumer is invoked once at build time; for dynamic updates
     * capture a reference via {@code s.use(n -> ref = (Element) n)} and
     * populate it externally.
     */
    public Accordion summary(Consumer<ElementBuilder> summary) {
        this.summary = summary;
        return this;
    }

    @Override
    protected ElementBuilder createRoot(ContainerBuilder<?> parent) {
        ElementBuilder el = Details.$(parent).style(styles().fragment());
        if (open || !collapsible)
            el.attr("open", "");
        if (error)
            el.style(styles().error());
        if (!collapsible)
            el.style(styles().fixed());
        if (!header)
            el.style(styles().noHeader());
        if (!visible)
            el.style(styles().hidden());
        el.use(n -> rootEl = (Element) n);
        return el;
    }

    @Override
    protected void buildInto(ElementBuilder root) {
        Summary.$(root).style(styles().summary()).use(n -> {
            summaryEl = (Element) n;
            if (!collapsible) {
                // Calling preventDefault on the summary's click cancels the
                // browser's default <details> toggle action. This stops the
                // body from briefly collapsing-and-re-expanding (the "flash")
                // that a post-toggle reopen would produce. Covers both mouse
                // clicks and keyboard activation (Enter/Space generate clicks
                // on the focused summary).
                summaryEl.addEventListener("click", evt -> evt.preventDefault());
            }
        }).$(head -> {
            Span.$(head).style(styles().caret()).$(c -> Em.$(c).style(FontAwesome.chevronRight()));
            if (!StringSupport.empty(icon))
                Div.$(head).style(styles().icon()).$(ico -> Em.$(ico).style(icon));
            Div.$(head).style(styles().head()).$(h -> {
                if (!StringSupport.empty(title))
                    Div.$(h).style(styles().title()).text(title);
                if (summary != null)
                    Div.$(h).style(styles().sum()).$(s -> summary.accept(s));
            });
        });
        Div.$(root).style(styles().body()).$(bodyEl -> {
            children.forEach(child -> child.insertInto(bodyEl));
        });
    }

    /********************************************************************
     * Styles.
     ********************************************************************/

    protected ILocalCSS styles() {
        return LocalCSS.instance();
    }

    public static interface ILocalCSS extends IFragmentCSS {

        String summary();

        String caret();

        String icon();

        String head();

        String title();

        String sum();

        String body();

        String error();

        String fixed();

        String hidden();

        String noHeader();
    }

    @CssResource(stylesheet = """
        .fragment {
            --cpt-accordion-bg: #ffffff;
            --cpt-accordion-border: #d4d4d4;
            --cpt-accordion-radius: 8px;
            --cpt-accordion-summary-padding: 0.875em 1em;
            --cpt-accordion-summary-gap: 1em;
            --cpt-accordion-summary-hover-bg: #fafafa;
            --cpt-accordion-caret-color: #9a9a9a;
            --cpt-accordion-icon-size: 3em;
            --cpt-accordion-icon-radius: 6px;
            --cpt-accordion-icon-bg: #f0f0f0;
            --cpt-accordion-icon-color: #555555;
            --cpt-accordion-title-color: #222222;
            --cpt-accordion-title-size: 1.2em;
            --cpt-accordion-title-weight: 600;
            --cpt-accordion-sum-color: #6b6b6b;
            --cpt-accordion-sum-size: 1em;
            --cpt-accordion-sum-gap: 6px;
            --cpt-accordion-body-padding: 1.5em;
            --cpt-accordion-body-border: #ececec;

            /* Error-state palette — applied when .error is present on the
               fragment root. Override these tokens to retheme the indicator. */
            --cpt-accordion-error-border: #c0432a;
            --cpt-accordion-error-caret-color: #c0432a;
            --cpt-accordion-error-icon-bg: #fae3d8;
            --cpt-accordion-error-icon-color: #c0432a;
            --cpt-accordion-error-title-color: #a13720;

            background: var(--cpt-accordion-bg);
            border: 1px solid var(--cpt-accordion-border);
            border-radius: var(--cpt-accordion-radius);
        }
        .fragment > .summary {
            list-style: none;
            cursor: pointer;
            padding: var(--cpt-accordion-summary-padding);
            display: flex;
            align-items: center;
            gap: var(--cpt-accordion-summary-gap);
            border-radius: var(--cpt-accordion-radius) var(--cpt-accordion-radius);
        }
        .fragment[open] > .summary {
            border-radius: var(--cpt-accordion-radius) var(--cpt-accordion-radius) 0 0;
        }
        .fragment > .summary::-webkit-details-marker {
            display: none;
        }
        .fragment > .summary:hover {
            background: var(--cpt-accordion-summary-hover-bg);
        }
        .fragment > .summary .caret {
            color: var(--cpt-accordion-caret-color);
            display: inline-flex;
            transition: transform 0.15s ease;
            flex-shrink: 0;
        }
        .fragment[open] > .summary .caret {
            transform: rotate(90deg);
        }
        .fragment > .summary .icon {
            width: var(--cpt-accordion-icon-size);
            height: var(--cpt-accordion-icon-size);
            border-radius: var(--cpt-accordion-icon-radius);
            display: inline-flex;
            align-items: center;
            justify-content: center;
            background: var(--cpt-accordion-icon-bg);
            color: var(--cpt-accordion-icon-color);
            flex-shrink: 0;
        }
        .fragment > .summary .icon em {
            font-size: calc(var(--cpt-accordion-icon-size) * 0.55);
        }
        .fragment > .summary .head {
            flex: 1;
            min-width: 0;
        }
        .fragment > .summary .head .title {
            font-weight: var(--cpt-accordion-title-weight);
            font-size: var(--cpt-accordion-title-size);
            color: var(--cpt-accordion-title-color);
        }
        .fragment > .summary .head .sum {
            font-size: var(--cpt-accordion-sum-size);
            color: var(--cpt-accordion-sum-color);
            margin-top: 0.2em;
            display: flex;
            align-items: center;
            gap: var(--cpt-accordion-sum-gap);
            flex-wrap: wrap;
        }
        .fragment > .body {
            padding: var(--cpt-accordion-body-padding);
            border-top: 1px solid var(--cpt-accordion-body-border);
        }
        /* Error state — toggled via {@link Accordion#error(boolean)}. */
        .fragment.error {
            border-color: var(--cpt-accordion-error-border);
        }
        .fragment.error > .summary .caret {
            color: var(--cpt-accordion-error-caret-color);
        }
        .fragment.error > .summary .icon {
            background: var(--cpt-accordion-error-icon-bg);
            color: var(--cpt-accordion-error-icon-color);
        }
        .fragment.error > .summary .head .title {
            color: var(--cpt-accordion-error-title-color);
        }
        /* Non-collapsible variant — toggled via {@link Accordion#collapsible(boolean)}.
           Body always shown; summary loses its caret and click affordance. */
        .fragment.fixed > .summary {
            cursor: default;
        }
        .fragment.fixed > .summary:hover {
            background: var(--cpt-accordion-bg);
        }
        .fragment.fixed > .summary .caret {
            display: none;
        }
        /* Hidden variant — toggled via {@link Accordion#visible(boolean)}. */
        .fragment.hidden {
            display: none;
        }
        /* No-header variant — toggled via {@link Accordion#header(boolean)}.
           The body becomes the only visible content; we still render an empty
           summary so the <details> element stays valid HTML and the user agent
           doesn't synthesise a default "Details" widget. */
        .fragment.noHeader > .summary {
            display: none;
        }
        .fragment.noHeader > .body {
            border-top: none;
        }
    """)
    public static abstract class LocalCSS implements ILocalCSS {

        private static LocalCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (LocalCSS) GWT.create(LocalCSS.class);
                STYLES.ensureInjected();
            }
            return STYLES;
        }
    }
}