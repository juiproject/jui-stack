package com.effacy.jui.text.ui.editor;

import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.platform.css.client.CssResource;
import com.google.gwt.core.client.GWT;

/**
 * Styles for the editor's floating affordances that are appended to {@code body}
 * (outside the component's scoped DOM) — the image-selection overlay and the link
 * hover card. These cannot use the component's scoped CSS, so they carry their own
 * injected sheet.
 * <p>
 * All colours are driven by <b>design tokens</b> with layered fallbacks, so they
 * follow the theme (light/dark, app accent) by default and can be restyled
 * application-wide by overriding a handful of custom properties on {@code :root}:
 * <ul>
 * <li>{@code --jui-editor-popover-bg} / {@code --jui-editor-popover-border} /
 *     {@code --jui-editor-popover-muted} / {@code --jui-editor-popover-shadow} — the
 *     floating surface;</li>
 * <li>{@code --jui-editor-accent} — the interactive accent (Open / Edit / align);</li>
 * <li>{@code --jui-editor-neutral} — secondary controls (margin steppers);</li>
 * <li>{@code --jui-editor-on-accent} — text/glyph colour on an accent fill.</li>
 * </ul>
 * Each falls back to an existing jui token (e.g. {@code --jui-ctl-focus},
 * {@code --jui-color-neutral*}) and finally a literal, so the defaults are sensible
 * with no configuration.
 */
public interface EditorOverlayCSS extends IComponentCSS {

    String linkCard();

    String linkCardUrl();

    String linkCardActions();

    String linkCardBtn();

    String imgOverlay();

    String imgHandle();

    String imgToolbar();

    String imgBtn();

    String imgBtnMuted();

    @CssResource(value = {
        IComponentCSS.COMPONENT_CSS
    }, stylesheet = """
        .linkCard {
            position: fixed;
            display: none;
            z-index: 1000;
            flex-direction: column;
            gap: 6px;
            max-width: 340px;
            padding: 8px 10px;
            font-size: 0.8125rem;
            background: var(--jui-editor-popover-bg, var(--jui-color-aux-white, #fff));
            border: 1px solid var(--jui-editor-popover-border, var(--jui-color-neutral20, #e5e7eb));
            border-radius: 6px;
            box-shadow: var(--jui-editor-popover-shadow, 0 6px 20px rgba(0, 0, 0, 0.14));
        }
        .linkCardUrl {
            color: var(--jui-editor-popover-muted, var(--jui-color-neutral60, #6b7280));
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            max-width: 320px;
        }
        .linkCardActions {
            display: flex;
            gap: 14px;
        }
        .linkCardBtn {
            cursor: pointer;
            font-weight: 500;
            user-select: none;
            color: var(--jui-editor-accent, var(--jui-ctl-focus, #2563eb));
        }
        .linkCardBtn:hover {
            text-decoration: underline;
        }
        .imgOverlay {
            position: fixed;
            display: none;
            pointer-events: none;
            box-sizing: border-box;
            z-index: 1000;
            border: 1px solid var(--jui-editor-accent, var(--jui-ctl-focus, #4a90d9));
        }
        .imgHandle {
            position: absolute;
            width: 10px;
            height: 10px;
            box-sizing: border-box;
            pointer-events: auto;
            background: var(--jui-editor-popover-bg, var(--jui-color-aux-white, #fff));
            border: 1px solid var(--jui-editor-accent, var(--jui-ctl-focus, #4a90d9));
        }
        .imgToolbar {
            position: absolute;
            top: -30px;
            left: 0;
            display: flex;
            gap: 2px;
            pointer-events: auto;
        }
        .imgBtn {
            pointer-events: auto;
            cursor: pointer;
            border: none;
            border-radius: 2px;
            width: 22px;
            height: 22px;
            font-size: 12px;
            line-height: 22px;
            text-align: center;
            color: var(--jui-editor-on-accent, #fff);
            background: var(--jui-editor-accent, var(--jui-ctl-focus, #4a90d9));
        }
        .imgBtn:hover {
            filter: brightness(0.92);
        }
        .imgBtnMuted {
            background: var(--jui-editor-neutral, var(--jui-color-neutral60, #7a7a7a));
            font-size: 14px;
        }
    """)
    public static abstract class Styles implements EditorOverlayCSS {

        private static Styles STYLES;

        public static EditorOverlayCSS instance() {
            if (STYLES == null) {
                STYLES = (Styles) GWT.create(Styles.class);
                STYLES.ensureInjected();
            }
            return STYLES;
        }
    }
}
