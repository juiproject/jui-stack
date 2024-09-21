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
package com.effacy.jui.playground.ui.tutorial;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.pepstock.charba.client.AbstractChart;
import org.pepstock.charba.client.Charba;

import com.effacy.jui.core.client.component.Component;
import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.dom.ActivationHandler;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.UIEvent;
import com.effacy.jui.core.client.dom.builder.DomBuilder;
import com.effacy.jui.core.client.util.UID;

import elemental2.dom.Element;

/**
 * Starter class for a re-usable chart component.
 */
public class ChartComponent extends Component<ChartComponent.Config> {

    /**
     * Configuration for the component.
     */
    public static class Config extends Component.Config {

        /**
         * Different chart types.
         */
        public enum Type {
            RADAR, BAR;
        }

        /**
         * See {@link #type(Type)}.
         */
        private Type type = Type.RADAR;

        /**
         * See {@link #labels(String...)}.
         */
        private String[] labels;

        /**
         * See {@link #title(String)}.
         */
        private String title;

        /**
         * See {@link #option(String, String, Supplier)}
         */
        private List<Option> options = new ArrayList<> ();

        /**
         * Specifies the type of chart to display.
         * 
         * @param type
         *             the type.
         * @return this configuration instance.
         */
        public Config type(Type type) {
            if (type != null)
                this.type = type;
            return this;
        }

        /**
         * Specifies the data item labels (i.e. the columns or datum names).
         * 
         * @param labels
         *               the labels for the data items.
         * @return this configuration instance.
         */
        public Config labels(String... labels) {
            this.labels = labels;
            return this;
        }

        /**
         * The title of the component (that appears at the top left of the component).
         * 
         * @param title
         *              the title.
         * @return this configuration instance.
         */
        public Config title(String title) {
            this.title = title;
            return this;
        }

        /**
         * Adds a option to appear in the selection list (that appears at the right left
         * of the component). This changes the data source used to populate the chart.
         * 
         * @param label
         *              the display label on the option.
         * @param data
         *              the source of data.
         * @return this configuration instance.
         */
        public Config option(String label, Supplier<double[]> data) {
            Option option = new Option (label, data);
            options.add (option);
            return this;
        }

        /**
         * Simple data carrier for representing an option.
         */
        private class Option {

            /**
             * Unique reference to the option.
             */
            final String reference = UID.createUID ();

            /**
             * Display label for the option.
             */
            final String label;

            /**
             * Source of data for the option.
             */
            final Supplier<double[]> data;

            /**
             * Consruct with the data being encapsulated.
             */
            public Option(String label, Supplier<double[]> data) {
                this.label = label;
                this.data = data;
            }
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.component.Component.Config#build(com.effacy.jui.core.client.component.layout.LayoutData[])
         */
        @Override
        @SuppressWarnings("unchecked")
        public ChartComponent build(LayoutData... data) {
            return new ChartComponent (this);
        }

    }

    /**
     * To initialise the chart library.
     */
    static {
        Charba.enable ();
    }

    /**
     * Chart instance.
     */
    private AbstractChart chart;

    /**
     * Element that holds the selected menu option.
     */
    private Element menuLabelEl;

    /**
     * Element encompassing the menu (and is the activator).
     */
    private Element menuEl;

    /**
     * Manages the activation of the context menu.
     */
    protected ActivationHandler menuHandler;


    /**
     * Construct a chart instance.
     * 
     * @param config
     *               configuration for the component.
     */
    public ChartComponent(Config config) {
        super (config);

        // TODO: Create the chart instance.
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#onAfterRender()
     */
    @Override
    protected void onAfterRender() {
        super.onAfterRender ();

        // TODO: Load up the initial range (being the first option).
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#buildNode(elemental2.dom.Element,
     *      com.effacy.jui.core.client.component.Component.Config)
     */
    @Override
    protected INodeProvider buildNode(Element el, ChartComponent.Config data) {
        return DomBuilder.el (el, root -> {
            // TODO: Build out the content.
        }).build (tree -> {
            // TODO: Extract references.
        });
    }

    /**
     * Handle a click on the menu activator.
     */
    protected void onMenuClick(UIEvent event) {
        // TODO: Handle the menu activation and de-activation.
    }

    /**
     * Handler a click on a menu selector option.
     */
    protected void onMenuOptionClick(UIEvent event) {
        // TODO: Handle click on a menu option.
    }

    /**
     * Loads data matching the given reference.
     * 
     * @param reference
     *                  the reference (from the configuration options).
     * @return {@code true} if successfully mapped.
     */
    protected boolean load(String reference) {
        // TODO: Search the options for the reference, update the menu label and update
        // the data.
        return false;
    }

    /**
     * Internal method to update the data on the chart.
     * 
     * @param values
     *               the values to update.
     */
    protected void update(double... values) {
        // TODO: Update the chart based on the char type and the passed data.
    }
}
