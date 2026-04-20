package com.effacy.jui.ui.client.fragments;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.effacy.jui.core.client.Invoker;
import com.effacy.jui.core.client.dom.builder.ContainerBuilder;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.platform.css.client.CssDeclaration;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.ui.client.button.IButtonHandler.IButtonActionCallback;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;
import elemental2.dom.HTMLButtonElement;

/**
 * Creates a button that displays an icon and supports an on-click handler. The
 * button is styled as a circular icon button, and the icon is specified using a
 * CSS class (e.g. from FontAwesome).
 */
public class IconBtn {

    public static IconBtnFragment $(String icon) {
        return new IconBtnFragment (icon);
    }

    public static IconBtnFragment $(IDomInsertableContainer<?> parent, String icon) {
        IconBtnFragment frg = $ (icon);
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    /**
     * Fragment implementation.
     */
    public static class IconBtnFragment extends BaseFragment<IconBtnFragment> {

        /**
         * See {@link #icon(String)}.
         */
        private String icon;

        /**
         * See {@link #size(Length)}.
         */
        private Length size;

        /**
         * See {@link #onclick(Consumer<IButtonActionCallback>)}.
         */
        private Consumer<IButtonActionCallback> onclick;

        /**
         * See {@link #testId(String)}.
         */
        private String testId;

        /**
         * See {@link #attr(String, String)}.
         */
        private Map<String,String> attributes;

        /**
         * Construct with the label for the button.
         * 
         * @param icon  
         *             the icon.
         */
        public IconBtnFragment(String icon) {
            this.icon = icon;
        }

        /**
         * The font size.
         * 
         * @param size
         *              the size to apply.
         * @return the fragment instance.
         */
        public IconBtnFragment size(Length size) {
            this.size = size;
            return this;
        }

        /**
         * Adds an attribute to add to the root element.
         * 
         * @param name
         *              the name of the attribute.
         * @param value
         *              the value of the attribute.
         * @return the fragment instance.
         */
        public IconBtnFragment attr(String name, String value) {
            if (attributes == null)
                attributes = new HashMap<>();
            if (value == null)
                attributes.remove(name);
            else
                attributes.put(name, value);
            return this;
        }

        /**
         * Adds an on-click handler to the icon.
         * 
         * @param onclick
         *                the handler.
         * @return this icon instance.
         */
        public IconBtnFragment onclick(Invoker onclick) {
            this.onclick = (cb -> {
                onclick.invoke();
                cb.complete();
            });
            return this;
        }

        /**
         * Adds an on-click handler to the icon.
         * 
         * @param onclick
         *                the handler.
         * @return this icon instance.
         */
        public IconBtnFragment onclick(Consumer<IButtonActionCallback> onclick) {
            this.onclick = onclick;
            return this;
        }

        /**
         * Assigns a test ID to the action.
         * 
         * @param testId
         *                test ID.
         * @return this icon instance.
         */
        public IconBtnFragment testId(String testId) {
            this.testId = testId;
            return this;
        }

        @Override
        protected ElementBuilder createRoot(ContainerBuilder<?> parent) {
            if (icon == null)
                return null;
            ILocalCSS style = LocalCSS.instance ();
            ElementBuilder btn = com.effacy.jui.core.client.dom.builder.Button.$(parent);
            btn.style(style.fragment());
            if (attributes != null)
                attributes.forEach((k,v) -> btn.attr(k, v));
            Em.$ (btn).style (icon);
            if (testId != null)
                btn.testId (testId);
            if (size != null)
                btn.css (CSS.FONT_SIZE, size);
            if (onclick != null) {
                btn.onclick ((e, n) -> {
                    ((HTMLButtonElement)n).disabled = true;
                    ((Element)n).classList.add(style.running());
                    onclick.accept(() -> {
                        ((Element)n).classList.remove(style.running());
                        ((HTMLButtonElement)n).disabled = false;
                    });
                });
            }
            return btn;
        }

    }

    /********************************************************************
     * CSS
     ********************************************************************/
    
    public static interface ILocalCSS extends CssDeclaration {

        String fragment();

        String running();
    }

    /**
     * Component CSS (standard pattern).
     */
    @CssResource(stylesheet = """
        .fragment {
            --jui-iconbtn-color: #777;
            --jui-iconbtn-color-hover: #444;
            --jui-iconbtn-size: 1.2em;
            --jui-iconbtn-bg-hover: #efefef;
            --jui-iconbtn-dimension: 2em;
        }
        .fragment {
            border-radius: 1em;
            border: none;
            background: transparent;
            display: flex;
            align-items: center;
            justify-content: center;
            width: var(--jui-iconbtn-dimension);
            height: var(--jui-iconbtn-dimension);
            cursor: pointer;
            font-size: var(--jui-iconbtn-size);
            color: var(--jui-iconbtn-color);
            transition: background 0.2s, color 0.2s;
        }
        .fragment > em {
            width: 1em !important;
            height: 1em !important;
            display: block;
        }
        .fragment:hover {
            background: var(--jui-iconbtn-bg-hover);
            color: var(--jui-iconbtn-color-hover);
        }
        .fragment.running {

        }
    """)
    public static abstract class LocalCSS implements ILocalCSS {

        private static LocalCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (LocalCSS) GWT.create (LocalCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }
}