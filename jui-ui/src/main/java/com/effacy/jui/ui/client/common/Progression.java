package com.effacy.jui.ui.client.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.effacy.jui.core.client.component.Component;
import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.platform.css.client.CssResource;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;

/**
 * Component to display progression through a series of steps (e.g. in a
 * wizard). The current step is highlighted and prior steps are visually
 * distinguished from future steps. Optionally steps can be clickable (e.g. to
 * navigate to a prior step) and completed steps can display an icon in place of
 * the step number.
 */
public class Progression extends Component<Progression.Config> {

    /************************************************************************
     * Configuration
     ************************************************************************/

    public static class Config extends Component.Config {

        /**
         * Predefined variants.
         */
        @FunctionalInterface
        public interface Variant {
        
            /**
             * Configure the progression by variant.
             */
            void configure(Config config);
        }

        /**
         * When there are no description this increases the vertical gap between the
         * steps.
         */
        public static final Variant NO_DESCRIPTION = config -> {
            config.css("--cpt-progression-step-vgap: 2em;");
        };

        /**
         * Configure by variant.
         * 
         * @param variant
         *                the variant.
         * @return this configuration instance.
         */
        public Config variant(Variant variant) {
            if (variant != null)
                variant.configure(this);
            return this;
        }

        /**
         * Describes a step.
         */
        record Step(String label, String description, Object reference) {}

        public enum Location {
            PRIOR, CURRENT, FUTURE;
        }
        public record StepClick(Object reference, int index, Location location) {}

        /**
         * The steps in the progression.
         */
        private List<Step> steps = new ArrayList<>();

        /**
         * CSS icon class to display on completed steps in place of the step
         * number (e.g. {@code FontAwesome.check()}).
         */
        private String iconForComplete;

        /**
         * Handler invoked when a step is clicked. Receives the step's reference
         * if non-null, otherwise the step index as an {@link Integer}.
         */
        private Consumer<StepClick> onclick;

        /**
         * Determines whether a step is clickable (i.e. has a click handler and is not
         * the current step). If not set then all steps are clickable by default (except
         * the current step).
         */
        private Function<StepClick,Boolean> clickable;

        /**
         * Assigns a CSS icon class to display on completed steps in place of the
         * step number (e.g. {@code FontAwesome.check()}).
         *
         * @param iconForComplete
         *                        the CSS class for the icon.
         * @return this configuration instance.
         */
        public Config iconForComplete(String iconForComplete) {
            this.iconForComplete = iconForComplete;
            return this;
        }

        /**
         * Assigns a click handler for step clicks.
         *
         * @param onclick
         *                     the handler.
         * @return this configuration instance.
         */
        public Config onclick(Consumer<StepClick> onclick) {
            this.onclick = onclick;
            return this;
        }

        /**
         * Assigns a function to determine whether a step is clickable (i.e. has a click
         * handler and is not the current step). If not set then only prior steps are
         * clickable by default.
         * 
         * @param clickable
         *                  the function to determine whether a step is clickable
         *                  (receives the step reference if non-null, otherwise the step
         *                  index as an {@link Integer}, and the step location relative
         *                  to the current step as a {@link Location}).
         * @return this configuration instance.
         */
        public Config clickable(Function<StepClick,Boolean> clickable) {
            this.clickable = clickable;
            return this;
        }

        /**
         * Convenience to assign steps from simple labels (each label is used as
         * both label and description).
         *
         * @param labels
         *               the step labels.
         * @return this configuration instance.
         */
        public Config labels(String... labels) {
            return labels(Arrays.asList(labels));
        }

        /**
         * Convenience to assign steps from simple labels (each label is used as
         * both label and description).
         *
         * @param labels
         *               the step labels.
         * @return this configuration instance.
         */
        public Config labels(List<String> labels) {
            this.steps = new ArrayList<>();
            if (labels != null)
                labels.forEach(l -> this.steps.add(new Step(l, "", null)));
            return this;
        }

        /**
         * Adds a step to the progression.
         *
         * @param label
         *                    the step label.
         * @param description
         *                    the step description.
         * @return this configuration instance.
         */
        public Config step(String label, String description) {
            this.steps.add(new Step(label, description, null));
            return this;
        }

        /**
         * Adds a step to the progression.
         *
         * @param label
         *                    the step label.
         * @param description
         *                    the step description.
         * @param reference
         *                    an optional reference object to associate with the step
         *                    (e.g. for retrieval on click).
         * @return this configuration instance.
         */
        public Config step(String label, String description, Object reference) {
            this.steps.add(new Step(label, description, reference));
            return this;
        }

        /**
         * Assigns the steps in the progression.
         *
         * @param steps
         *              the steps.
         * @return this configuration instance.
         */
        public Config steps(Step... steps) {
            this.steps = Arrays.asList(steps);
            return this;
        }

        /**
         * See {@link #steps(Step...)}.
         */
        public Config steps(List<Step> steps) {
            this.steps = (steps == null) ? new ArrayList<>() : steps;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Progression build(LayoutData... data) {
            return (Progression) super.build(new Progression(this), data);
        }
    }

    /************************************************************************
     * Construction
     ************************************************************************/

    /**
     * The current step.
     */
    private int step = 0;

    /**
     * Construct with configuration.
     *
     * @param config
     *               the configuration.
     */
    public Progression(Config config) {
        super(config);
    }

    /**
     * See {@link #Progression(List)}.
     */
    public Progression(String... steps) {
        this(Arrays.asList(steps));
    }

    /**
     * Construct with the steps in the progression (the default active step will be
     * the first, i.e. <code>0</code>).
     *
     * @param steps
     *              the steps.
     */
    public Progression(List<String> steps) {
        super(new Config().labels(steps));
    }

    /************************************************************************
     * Behaviour
     ************************************************************************/

    /**
     * Updates the step (and renders).
     *
     * @param step
     *             the revised step.
     */
    public void update(int step) {
        this.step = step;
        rerender();
    }

    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$(el).$(root -> {
            if (config().onclick != null)
                root.style(styles().navigable());
            List<Config.Step> steps = config().steps;
            for (int i = 0; i < steps.size(); i++) {
                int idx = i;
                String spec = (idx < step) ? styles().prior() : ((idx == step) ? styles().current() : styles().next());
                Div.$(root).style(styles().step(), spec, (idx < steps.size() - 1) ? styles().connected() : null).$(step_el -> {
                    if (config().onclick != null) {
                        Config.Location loc = (idx < step) ? Config.Location.PRIOR : ((idx == step) ? Config.Location.CURRENT : Config.Location.FUTURE);
                        Config.StepClick click = new Config.StepClick(steps.get(idx).reference(), idx, loc);
                        boolean isClickable = (config().clickable == null) ? (loc != Config.Location.CURRENT) : config().clickable.apply(click);
                        if (isClickable) {
                            step_el.style(styles().navigable());
                            step_el.onclick(e -> config().onclick.accept(click));
                        }
                    }
                    if ((idx < step) && (config().iconForComplete != null))
                        Em.$(step_el).style(config().iconForComplete);
                    else
                        Em.$(step_el).text("" + (idx + 1));
                    if (steps.get(idx).description() != null) {
                        Div.$(step_el).style(styles().labels()).$(labels -> {
                            Span.$(labels).text(steps.get(idx).label());
                            Span.$(labels).style(styles().description()).text(steps.get(idx).description());
                        });
                    } else {
                        Span.$(step_el).text(steps.get(idx).label());
                    }
                });
            }
        }).build();
    }

    /************************************************************************
     * CSS
     ************************************************************************/

    /**
     * Styles (made available to selection).
     */
    protected ILocalCSS styles() {
        return LocalCSS.instance();
    }

    public static interface ILocalCSS extends IComponentCSS {
        public String navigable();
        public String step();
        public String connected();
        public String description();
        public String labels();
        public String prior();
        public String current();
        public String next();
    }

    /**
     * Component CSS (standard pattern).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        "com/effacy/jui/ui/client/common/Progression.css",
        "com/effacy/jui/ui/client/common/Progression_Override.css"
    })
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