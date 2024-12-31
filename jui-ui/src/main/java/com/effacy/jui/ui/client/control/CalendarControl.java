package com.effacy.jui.ui.client.control;

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
import com.effacy.jui.core.client.dom.builder.Input;
import com.effacy.jui.core.client.dom.builder.Table;
import com.effacy.jui.core.client.dom.builder.Tbody;
import com.effacy.jui.core.client.dom.builder.Td;
import com.effacy.jui.core.client.dom.builder.Text;
import com.effacy.jui.core.client.dom.builder.Th;
import com.effacy.jui.core.client.dom.builder.Thead;
import com.effacy.jui.core.client.dom.builder.Tr;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;
import elemental2.dom.HTMLInputElement;

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
         * See {@link #selectorLeft(boolean)}. Convenience to pass {@code true}.
         * 
         * @return this configuration instance.
         */
        public Config selectorLeft() {
            return selectorLeft (true);
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

    protected Element selectorEl;

    /**
     * The currently held date as represented by the UI.
     */
    private CalendarDate date;

    private ActivationHandler selector;

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
                        // if (!filterKeyPress (e.getKeyCode (), e.getKey(), inputEl.value))
                        //     e.stopEvent ();
                    }, UIEventType.ONKEYPRESS);
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

    protected void refreshDate() {
        if (this.date == null) {
            inputEl.value = "";
        } else {
            inputEl.value = CalendarSupport.formatDate(config().formatLocale.get(), this.date.year(), this.date.month(), this.date.day(), config().formatStyle.weekday,  config().formatStyle.year,  config().formatStyle.month,  config().formatStyle.day);
        }
    }

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
        }
    }

    protected void updateSelector() {
        String month = CalendarSupport.nameOfMonth(config().formatLocale.get(), selectorDate.month());
        int[] datesInTable = CalendarSupport.dateTable (selectorDate.year(), selectorDate.month());

        // Final versions for reference in the builder.
        int fStartIdx = CalendarSupport.startOfMonth(datesInTable);
        int fEndIdx = CalendarSupport.endOfMonth(datesInTable);
        int fSelectedDate = CalendarSupport.indexOf(datesInTable, selectorDate, date);
        int fCurrentDate = CalendarSupport.indexOf(datesInTable, selectorDate, CalendarDate.now());
        
        Wrap.buildInto(selectorEl, selector -> {
            Div.$(selector).$ (
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

        public String inner();

        public String open();

        public String selector_top();

        public String selector_left();

        public String months();

        public String dates();

        public String outside();

        public String selected();

        public String current();
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