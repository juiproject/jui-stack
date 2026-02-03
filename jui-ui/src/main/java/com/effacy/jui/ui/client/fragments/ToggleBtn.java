package com.effacy.jui.ui.client.fragments;

import java.util.HashMap;
import java.util.Map;

import com.effacy.jui.core.client.Invoker;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.platform.util.client.StringSupport;

/**
 * Renders a simple toggle button.
 */
public class ToggleBtn  {

    public static ToggleBtnFragment $() {
        return new ToggleBtnFragment ();
    }

    public static ToggleBtnFragment $(IDomInsertableContainer<?> parent) {
        ToggleBtnFragment frg = $ ();
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    /**
     * The visual form that the button takes.
     */
    public interface Variant {

        /**
         * Standard button presentation.
         */
        public static final Variant STANDARD = Variant.create("variant-standard");

        /**
         * A CSS class to apply in addition.
         */
        public String style();

        /**
         * Convenience to create an instance of a variant.
         */
        public static Variant create(String style) {
            return new Variant() {
                public String style() { return style; }
            };
        }
    }

    /**
     * Fragment implementation.
     */
    public static class ToggleBtnFragment extends BaseFragment<ToggleBtnFragment> {

        /**
         * See constructor.
         */
        private String label;

        /**
         * See {@link #variant(Variant)}.
         */
        private Variant variant = Variant.STANDARD;

        /**
         * See {@link #active(boolean)}.
         */
        public boolean active;

        /**
         * See {@link #onclick(Consumer<IButtonActionCallback>)}.
         */
        private Invoker onclick;

        /**
         * See {@link #testId(String)}.
         */
        private String testId;

        /**
         * See {@link #attr(String, String)}.
         */
        private Map<String,String> attributes;

        /**
         * The button variant.
         * 
         * @param variant
         *              the variant to apply.
         * @return the fragment instance.
         */
        public ToggleBtnFragment variant(Variant variant) {
            if (variant != null)
                this.variant = variant;
            return this;
        }

        /**
         * Sets whether the toggle is active.
         * 
         * @param active
         *              true if active, false otherwise.
         * @return the fragment instance.
         */
        public ToggleBtnFragment active(boolean active) {
            this.active = active;
            return this;
        }

        /**
         * The label to display alongside the toggle.
         * 
         * @param label
         *              the label.
         * @return the fragment instance.
         */
        public ToggleBtnFragment label(String label) {
            this.label = label;
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
        public ToggleBtnFragment attr(String name, String value) {
            if (attributes == null)
                attributes = new HashMap<>();
            if (value == null)
                attributes.remove(name);
            else
                attributes.put(name, value);
            return this;
        }

        /**
         * Adds an on-click handler to the button.
         * 
         * @param onclick
         *                the handler.
         * @return this icon instance.
         */
        public ToggleBtnFragment onclick(Invoker onclick) {
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
        public ToggleBtnFragment testId(String testId) {
            this.testId = testId;
            return this;
        }

        @Override
        protected void buildInto(ElementBuilder root) {
            if (attributes != null)
                attributes.forEach((k,v) -> root.attr(k, v));
            if (testId != null)
                root.testId (testId);
            root.style("juiToggleBtn", variant.style());
            if (active)
                root.style("juiToggleBtn_active");
            root.$(
                Div.$().style("juiToggleBtn_toggle").$(
                    Span.$().style("juiToggleBtn_knob"),
                    Span.$().style("juiToggleBtn_spacer")
                )
            );
            if (!StringSupport.empty(label))
                root.$(
                    Span.$().style("juiToggleBtn_label").text(label)
                );
            if (onclick != null)
                root.onclick (e -> onclick.invoke());
        }

    }
}