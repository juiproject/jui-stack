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
package com.effacy.jui.ui.client.control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.gwtproject.safehtml.shared.SafeHtmlBuilder;

import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.control.Control;
import com.effacy.jui.core.client.dom.ActivationHandler;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.H5;
import com.effacy.jui.core.client.dom.builder.Input;
import com.effacy.jui.core.client.dom.builder.Table;
import com.effacy.jui.core.client.dom.builder.Tbody;
import com.effacy.jui.core.client.dom.builder.Td;
import com.effacy.jui.core.client.dom.builder.Text;
import com.effacy.jui.core.client.dom.builder.Th;
import com.effacy.jui.core.client.dom.builder.Thead;
import com.effacy.jui.core.client.dom.builder.Tr;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.Itr;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;

import elemental2.dom.Element;
import elemental2.dom.HTMLInputElement;

/**
 * Allows the user to select a date (either manually entering one or from a
 * calendar selector).
 * <p>
 * The date object managed is {@link CalendarDate}.
 */
public class CalendarControl extends Control<CalendarDate, CalendarControl.Config> {

    /**
     * The default style to employ when one is not assign explicitly.
     */
    public static Config.Style DEFAULT_STYLE = Config.Style.STANDARD;

    /**
     * The default locale to use for all date formats.
     */
    public static String DEFAULT_LOCALE = "en-us";

    /**
     * Collection of standard formatters that can be used to parse a date.
     * <p>
     * This is public so one can access this list for one's own purposes (i.e.
     * building a custom parser).
     */
    public static final List<DateTimeFormat> FORMATTERS = Arrays.asList(
        DateTimeFormat.getFormat("d/M/yyyy"),
        DateTimeFormat.getFormat("d-M-yyyy"),
        DateTimeFormat.getFormat("yyyy-M-d"),
        DateTimeFormat.getFormat("yyyy/M/d"),
        DateTimeFormat.getFormat("M/d/yyyy"),
        DateTimeFormat.getFormat("M-d-yyyy"),
        DateTimeFormat.getFormat("d MMM yyyy"),
        DateTimeFormat.getFormat("d MMMM yyyy"),
        DateTimeFormat.getFormat("d MMM"),
        DateTimeFormat.getFormat("d MMMM"),
        DateTimeFormat.getFormat("MMM d"),
        DateTimeFormat.getFormat("MMMM d"),
        DateTimeFormat.getFormat("d"),
        DateTimeFormat.getFormat("MMM"),
        DateTimeFormat.getFormat("MMMM"),
        DateTimeFormat.getFormat("d MMM, yyyy"),
        DateTimeFormat.getFormat("d MMMM, yyyy"),
        DateTimeFormat.getFormat("MMM d, yyyy"),
        DateTimeFormat.getFormat("MMMM d, yyyy")
    );

    public static record Option(boolean header, String label, CalendarDate value, Object reference) {}

    /**
     * Configuration for building a {@link CalendarControl}.
     */
    public static class Config extends Control.Config<CalendarDate, CalendarControl.Config> {

        /**
         * The format style for the date.
         */
        public enum FormatStyle {

            /**
             * Pure numeric form (i.e. 23/2/2021).
             */
            NUMERIC(null, null, null, null),

            /**
             * Month, date and year in short format.
             */
            SHORT(CalendarSupport.FORMAT_WEEKDAY.NONE, CalendarSupport.FORMAT_YEAR.NUMERIC, CalendarSupport.FORMAT_MONTH.SHORT, CalendarSupport.FORMAT_DAY.NUMERIC),
            
            /**
             * Month, date and year in long format.
             */
            LONG(CalendarSupport.FORMAT_WEEKDAY.NONE, CalendarSupport.FORMAT_YEAR.NUMERIC, CalendarSupport.FORMAT_MONTH.LONG, CalendarSupport.FORMAT_DAY.NUMERIC),
            
            /**
             * Day of week, month, date and year in short format.
             */
            SHORT_DAY(CalendarSupport.FORMAT_WEEKDAY.SHORT, CalendarSupport.FORMAT_YEAR.NUMERIC, CalendarSupport.FORMAT_MONTH.SHORT, CalendarSupport.FORMAT_DAY.NUMERIC),
            
            /**
             * Day of week, month, date and year in long format.
             */
            LONG_DAY(CalendarSupport.FORMAT_WEEKDAY.LONG, CalendarSupport.FORMAT_YEAR.NUMERIC, CalendarSupport.FORMAT_MONTH.LONG, CalendarSupport.FORMAT_DAY.NUMERIC);

            CalendarSupport.FORMAT_WEEKDAY weekday;
            CalendarSupport.FORMAT_YEAR year;
            CalendarSupport.FORMAT_MONTH month;
            CalendarSupport.FORMAT_DAY day;
            private FormatStyle(CalendarSupport.FORMAT_WEEKDAY weekday, CalendarSupport.FORMAT_YEAR year, CalendarSupport.FORMAT_MONTH month, CalendarSupport.FORMAT_DAY day) {
                this.weekday = weekday;
                this.year = year;
                this.month = month;
                this.day = day;
            }
        }

        /**
         * Style for the tab set (defines presentation configuration including CSS).
         */
        public interface Style {

            /**
             * The CSS styles.
             */
            public ILocalCSS styles();

            /**
             * Convenience to create a style.
             * 
             * @param styles
             *               the CSS styles.
             * @return the associated style.
             */
            public static Style create(final ILocalCSS styles) {
                return new Style () {

                    @Override
                    public ILocalCSS styles() {
                        return styles;
                    }

                };
            }

            public static final Style STANDARD = create(StandardLocalCSS.instance ());

        }

        /**
         * The styles to apply to the tab set.
         */
        private Style style = (DEFAULT_STYLE != null) ? DEFAULT_STYLE : Style.STANDARD;

        /**
         * See {@link #formatLocale(String)}.
         */
        private Supplier<String> formatLocale = () -> (DEFAULT_LOCALE != null) ? DEFAULT_LOCALE : "en-us";

        /**
         * See {@link #formatStyle(FormatStyle)},
         */
        private FormatStyle formatStyle = FormatStyle.SHORT;

        /**
         * See {@link #placeholder(String)}.
         */
        private String placeholder;

        /**
         * See {@link #selectorLeft(boolean)}.
         */
        private boolean selectorLeft;

        /**
         * See {@link #selectorTop(boolean)}.
         */
        private boolean selectorTop;

        /**
         * See {@link #selectorWidth(Length)}.
         */
        private Length selectorWidth;

        /**
         * See {@link #clearAction(boolean)}.
         */
        private boolean clearAction;

        /**
         * See {@link #dateFilter(Predicate)}.
         */
        private Predicate<CalendarDate> dateFilter;

        /**
         * See {@link #option(String, CalendarDate, Object)}.
         */
        private List<Option> options;

        /**
         * See {@link #optionsLoader(Consumer)}.
         */
        private Consumer<Config> optionsLoader;

        /**
         * See {@link #onoption(Consumer)}.
         */
        private Consumer<Option> onoption;

        /**
         * See {@link #dateParser(Function)}.
         */
        private Function<String,CalendarDate> dateParser = (str) -> {
            for (DateTimeFormat dtf : FORMATTERS) {
                try {
                    Date d = dtf.parse(str);
                    return new CalendarDate(d.getYear() + 1900, d.getMonth() + 1, d.getDate());
                } catch (IllegalArgumentException e) {
                    // Nothing.
                }
            }
            return null;
        };

        /**
         * Construct with a default style.
         */
        public Config() {
            super ();
        }

        /**
         * Construct with a style.
         * 
         * @param style
         *              the style.
         */
        public Config(Style style) {
            super ();
            if (style != null)
                this.style = style;
        }

        /**
         * Assigns the locale to use for formatting dates.
         * 
         * @param formatLocale
         *                    the locale to use (i.e. <code>en-us</code> or <code>en-gb</code>).
         * @return this configuration instance.
         */
        public Config formatLocale(String formatLocale) {
            if (formatLocale != null)
                this.formatLocale = () -> formatLocale;
            return this;
        }

        /**
         * Assigns the locale to use for formatting dates.
         * 
         * @param formatLocale
         *                     supplier for the locale to use (i.e. <code>en-us</code>
         *                     or <code>en-gb</code>).
         * @return this configuration instance.
         */
        public Config formatLocale(Supplier<String> formatLocale) {
            if (formatLocale != null)
                this.formatLocale = formatLocale;
            return this;
        }

        /**
         * Assigns the style to use for formatting dates.
         * 
         * @param formatStyle
         *                    the format style to use.
         * @return this configuration instance.
         */
        public Config formatStyle(FormatStyle formatStyle) {
            if (formatStyle != null)
                this.formatStyle = formatStyle;
            return this;
        }

        /**
         * Assigns placeholder text to display when the field is empty.
         * 
         * @param placeholder
         *                    placeholder content to display.
         * @return this configuration instance.
         */
        public Config placeholder(String placeholder) {
            this.placeholder = placeholder;
            return this;
        }

        /**
         * Determines if the selector should display above.
         * 
         * @param selectorTop
         *                    {@code true} to display above.
         * @return this configuration instance.
         */
        public Config selectorTop(boolean selectorTop) {
            this.selectorTop = selectorTop;
            return this;
        }

        /**
         * See {@link #selectorTop(boolean)}. Convenience to pass {@code true}.
         * 
         * @return this configuration instance.
         */
        public Config selectorTop() {
            return selectorTop (true);
        }

        /**
         * Determines if the selector should display with overhang to the left (right
         * aligned).
         * 
         * @param selectorLeft
         *                     {@code true} to display to the left.
         * @return this configuration instance.
         */
        public Config selectorLeft(boolean selectorLeft) {
            this.selectorLeft = selectorLeft;
            return this;
        }

        /**
         * Sets the width of the selector.
         * <p>
         * This only makes sense when there is extra content.
         * 
         * @param selectorWidth
         *                      the width (for extra content).
         * @return this configuration instance.
         */
        public Config selectorWidth(Length selectorWidth) {
            this.selectorWidth = selectorWidth;
            return this;
        }
        

        /**
         * See {@link #selectorLeft(boolean)}. Convenience to pass {@code true}.
         * 
         * @return this configuration instance.
         */
        public Config selectorLeft() {
            return selectorLeft (true);
        }

        /**
         * Conveience to call {@link #clearAction(boolean)} passing {@code true}.
         */
        public Config clearAction() {
            return clearAction(true);
        }

        /**
         * Determines if an empty ({@code null}) value is allowed (i.e. the date can be
         * cleared).
         * 
         * @param clearAction
         *                    {@code true} to allow for an empty value and display of a
         *                    clear action.
         * @return this configuration instance.
         */
        public Config clearAction(boolean clearAction) {
            this.clearAction = clearAction;
            return this;
        }

        /**
         * Used to filter in permissible dates. If supplied the predicate should return
         * {@code true} for allowed date (dates that can be selected).
         * 
         * @param dateFilter
         *                   the date filter.
         * @return this configuration instance.
         */
        public Config dateFilter(Predicate<CalendarDate> dateFilter) {
            this.dateFilter = dateFilter;
            return this;
        }

        /**
         * Assigns a date parser to use to parser any manually entered date. The parser
         * should return {@code null} if the date could not be parsed.
         * <p>
         * This replaces the default parser.
         * 
         * @param dateParser
         *                   the parser to use.
         * @return this configuration instance.
         */
        public Config dateParser(Function<String,CalendarDate> dateParser) {
            if (dateParser != null)
                this.dateParser = dateParser;
            return this;
        }

        /**
         * See {@link #option(String, CalendarDate, Object)} but with no additional
         * reference data.
         */
        public Config option(String label, CalendarDate value) {
            return option(label, value, null);
        }

        /**
         * Adds an option for display to the right of the calendar selector. Options are
         * displayed downwards and allow for selection of standard dates (such as
         * quarters).
         * 
         * @param label
         *                  the display label for the option.
         * @param value
         *                  the value to assign to the control when selected.
         * @param reference
         *                  an optional reference object that can be used if an
         *                  {@link #onoption(Consumer)} handler has been supplied.
         * @return this configuration instance.
         */
        public Config option(String label, CalendarDate value, Object reference) {
            if (this.options == null)
                this.options = new ArrayList<>();
            this.options.add (new Option(false, label, value, reference));
            return this;
        }

        /**
         * An option (see {@link #option(String, CalendarDate, Object)}) header. This is
         * not selectable.
         * 
         * @param header
         *               the header title.
         * @return this configuration instance.
         */
        public Config option(String header) {
            if (this.options == null)
                this.options = new ArrayList<>();
            this.options.add (new Option(true, header, null, null));
            return this;
        }

        /**
         * If this is present then each time the selector is opened the options set will
         * be cleared and this invoked to allow the options to be reloaded (refreshed).
         * 
         * @param optionsLoader
         *                      the loader to register.
         * @return this configuration instance.
         */
        public Config optionsLoader(Consumer<Config> optionsLoader) {
            this.optionsLoader = optionsLoader;
            return this;
        }

        /**
         * Registers a handler for processing options when selected (see
         * {@link #option(String, CalendarDate, Object)}).
         * 
         * @param onoption
         *                 the handler.
         * @return this configuration instance.
         */
        public Config onoption(Consumer<Option> onoption) {
            this.onoption = onoption;
            return this;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.component.Component.Config#build(com.effacy.jui.core.client.component.layout.LayoutData[])
         */
        @Override
        @SuppressWarnings("unchecked")
        public CalendarControl build(LayoutData... data) {
            return build (new CalendarControl (this), data);
        }

    }

    /**
     * Construct with configuration.
     * 
     * @param config
     *               the configuration.
     */
    public CalendarControl(CalendarControl.Config config) {
        super (config);
    }

    /************************************************************************
     * Rendering and styles.
     ************************************************************************/

    /**
     * The input element.
     */
    protected HTMLInputElement inputEl;

    /**
     * The element that contains the selector (see {@link #updateSelector()}).
     */
    protected Element selectorEl;

    /**
     * The currently held date as represented by the UI.
     */
    private CalendarDate date;

    /**
     * Manages display of selector.
     */
    private ActivationHandler selector;

    /**
     * The date as managed by the selector.
     */
    private CalendarDate selectorDate;

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.control.Control#valueFromSource()
     */
    @Override
    public CalendarDate valueFromSource() {
        return date;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.control.Control#valueToSource(java.lang.Object)
     */
    @Override
    public void valueToSource(CalendarDate value) {
        this.date = value;
        refreshDate();
    }

    /**
     * Used to flag that there was an attempt at manual entry.
     */
    private boolean manualEntry = false;

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#buildNode(com.effacy.jui.core.client.component.Component.Config)
     */
    @Override
    protected INodeProvider buildNode(Element el, Config data) {
        return Wrap.$ (el).$ (root -> {
            Div.$ (root).style (styles ().inner ()).by("activator").$ (
                Em.$ ()
                    .style (styles ().read_only (), FontAwesome.lock ()),
                Input.$ ("text").$ (input -> {
                    input.on (e -> modified (), UIEventType.ONKEYUP, UIEventType.ONPASTE);
                    input.ref ("input");
                    input.on (e -> {
                        if (manualEntry) {
                            // Try to parse the date.
                            if (StringSupport.empty(inputEl.value)) {
                                this.date = null;
                            } else {
                                CalendarDate d = config().dateParser.apply(inputEl.value);
                                if (d != null)
                                    this.date = d;
                            }
                            refreshDate();
                            modified();
                        }
                        manualEntry = false;
                    }, UIEventType.ONBLUR);
                    input.on (e -> {
                        manualEntry = true;
                    }, UIEventType.ONKEYDOWN);
                    if (StringSupport.empty (data.getName ()))
                        input.attr ("name", "" + getUUID ());
                    else
                        input.attr ("name", data.getName ());
                    if (!StringSupport.empty (data.placeholder))
                        input.attr ("placeholder", new SafeHtmlBuilder ().appendEscaped (data.placeholder));
                    else 
                        input.attr ("placeholder", "No date");
                    input.testId (buildTestId ("input")).testRef ("input");
                }),
                Em.$ ().iff (data.clearAction)
                    .style (styles ().clear (), FontAwesome.times ())
                    .testId (buildTestId ("clear")).testRef ("clear")
                    .on (e -> {
                        this.date = null;
                        selector.close();
                        refreshDate();
                        modified ();
                    }, UIEventType.ONCLICK),
                Em.$ ().style (FontAwesome.calendar())
            )
            .onclick(e -> {
                selector.toggle();
                e.stopEvent();
            });
            Div.$ (root).$ (selector -> {
                selector.id ("selector").by ("selector");
                if (data.selectorTop)
                    selector.style (styles ().selector_top ());
                if (data.selectorLeft)
                    selector.style (styles ().selector_left ());
                selector.onclick(e -> {
                    if (!e.getTarget().classList.contains(styles().disabled()))
                        applyAction (e.getTarget().getAttribute("action"), e.getTarget().getAttribute("item"));
                });
            });
        }).build (tree -> {
            // Register the input as the focus element (we only have one).
            inputEl = (HTMLInputElement) manageFocusEl (tree.first ("input"));
            selector = new ActivationHandler(tree.first("activator"), el, styles().open())
                .listen(open -> {
                    if (open) {
                        // Set the selector date to be at the start of the month (this allows us to
                        // adjust by date selected to get the desired date).
                        selectorDate = (date == null) ? CalendarDate.now() : date;
                        selectorDate = new CalendarDate (selectorDate.year(), selectorDate.month(), 1);
                        updateSelector();
                    }
                });
            selector.exclude(selectorEl = tree.first("selector"));
        });
    }

    /**
     * Applies the store date to the UI.
     */
    protected void refreshDate() {
        // Clear the manual entry flag.
        manualEntry = false;
        
        // Update the clear action based on the current date value.
        if (config ().clearAction) {
            if (this.date == null)
                getRoot ().classList.remove (styles ().clear ());
            else
                getRoot ().classList.add (styles ().clear ());
        }

        // Update the UI to reflect the new date value.
        if (this.date == null) {
            inputEl.value = "";
        } else {
            inputEl.value = CalendarSupport.formatDate(config().formatLocale.get(), this.date.year(), this.date.month(), this.date.day(), config().formatStyle.weekday,  config().formatStyle.year,  config().formatStyle.month,  config().formatStyle.day);
        }
    }

    /**
     * Applies an action from a click event in the selector.
     * 
     * @param action
     *               the action performed (e.g. "month-prev" or "date-select").
     * @param item
     *               the item data for the action (i.e. the date).
     */
    protected void applyAction(String action, String item) {
        if ("month-prev".equals(action)) {
            selectorDate = selectorDate.month(-1);
            updateSelector();
        } else if ("month-next".equals(action)) {
            selectorDate = selectorDate.month(1);
            updateSelector();
        } else if ("date-select".equals(action)) {
            try {
                int adjust = Integer.parseInt(item);
                date = selectorDate.day(adjust);
                refreshDate();
                modified();
                selector.close();
            } catch (NumberFormatException e) {
                // Nothing.
            }
        } else if ("option-select".equals(action)) {
            try {
                int idx = Integer.parseInt(item);
                Option option = config().options.get(idx);
                date = option.value();
                refreshDate();
                modified();
                selector.close();
                if (config().onoption != null)
                    config().onoption.accept(option);
            } catch (NumberFormatException e) {
                // Nothing.
            }
        }
    }

    /**
     * Refreshes the selector display.
     */
    protected void updateSelector() {
        // Reload any options.
        if (config().optionsLoader != null) {
            config().options = null;
            config().optionsLoader.accept(config());
        }

        String month = CalendarSupport.nameOfMonth(config().formatLocale.get(), selectorDate.month());
        int[] datesInTable = CalendarSupport.dateTable (selectorDate.year(), selectorDate.month());

        // Final versions for reference in the builder.
        int fStartIdx = CalendarSupport.startOfMonth(datesInTable);
        int fEndIdx = CalendarSupport.endOfMonth(datesInTable);
        int fSelectedDate = CalendarSupport.indexOf(datesInTable, selectorDate, date);
        int fCurrentDate = CalendarSupport.indexOf(datesInTable, selectorDate, CalendarDate.now());

        CSS.MIN_WIDTH.apply(selectorEl,config().selectorWidth);
        Wrap.buildInto(selectorEl, selector -> {
            Div.$(selector).$ (outer -> {
                Div.$(outer).style(styles().calendar()).$ (
                    Div.$ ().style(styles().months()).$ (
                        Em.$().style(FontAwesome.angleLeft())
                            .attr("action", "month-prev"),
                        Div.$().text (month + " " + selectorDate.year()),
                        Em.$().style(FontAwesome.angleRight())
                            .attr("action", "month-next")
                    ),
                    Div.$ ().style(styles().dates()).$ (dates -> {
                        Table.$(dates).$ (table -> {
                            Thead.$(table).$ (
                                Th.$ ().text ("Su"),
                                Th.$ ().text ("Mo"),
                                Th.$ ().text ("Tu"),
                                Th.$ ().text ("We"),
                                Th.$ ().text ("Th"),
                                Th.$ ().text ("Fr"),
                                Th.$ ().text ("Sa")
                            );
                            Tbody.$ (table).$ (body -> {
                                for (int i = 0; i < (datesInTable.length / 7); i++) {
                                    int fi = i;
                                    Tr.$ (body).$ (row -> {
                                        for (int j = 0; j < 7; j++) {
                                            int idx = j+7*fi;
                                            Td.$ (row).$ (cell -> {
                                                Text.$ (cell, "" + datesInTable[idx]);
                                                if ((idx < fStartIdx) || (fEndIdx < idx))
                                                    cell.style(styles().outside());
                                                // Apply any date filtering.
                                                if (config().dateFilter != null) {
                                                    CalendarDate d = new CalendarDate(selectorDate.year(), selectorDate.month(), datesInTable[idx]);
                                                    if (idx < fStartIdx)
                                                        d = d.month(-1);
                                                    else if (idx > fEndIdx)
                                                        d = d.month(1);
                                                    if (!config().dateFilter.test(d))
                                                        cell.style(styles().disabled());
                                                }
                                                cell.attr ("action", "date-select");
                                                cell.attr ("item", "" + (idx - fStartIdx));
                                                if (idx == fSelectedDate)
                                                    cell.style(styles().selected());
                                                if (idx == fCurrentDate) {
                                                    cell.style(styles().current());
                                                    Em.$(cell);
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        });
                    })
                );
                if ((config().options != null) && !config().options.isEmpty()) {
                    Div.$(outer).style(styles().extra()).$ (options -> {
                        Itr.forEach(config().options, (ctx,option) -> {
                            if (option.header) {
                                H5.$(options)
                                    .text(option.label);
                            } else {
                                Div.$(options)
                                    .text(option.label)
                                    .attr ("action", "option-select")
                                    .attr ("item", "" + ctx.index());
                            }
                        });

                    });
                }
            });
        });
    }

    /********************************************************************
     * CSS with standard styles.
     ********************************************************************/

    /**
     * Styles (made available to selection).
     */
    protected ILocalCSS styles() {
        return config ().style.styles ();
    }

    public static interface ILocalCSS extends IControlCSS {

        public String calendar();

        public String extra();

        public String inner();

        public String open();

        public String selector_top();

        public String selector_left();

        public String months();

        public String dates();

        public String outside();

        public String selected();

        public String current();

        public String clear();
    }

    /**
     * Component CSS (horizontal).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        "com/effacy/jui/ui/client/control/Control.css",
        "com/effacy/jui/ui/client/control/CalendarControl.css",
        "com/effacy/jui/ui/client/control/CalendarControl_Override.css"
    })
    public static abstract class StandardLocalCSS implements ILocalCSS {

        private static StandardLocalCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (StandardLocalCSS) GWT.create (StandardLocalCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }
}
