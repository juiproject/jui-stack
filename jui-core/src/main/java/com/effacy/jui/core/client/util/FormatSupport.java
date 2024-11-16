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
package com.effacy.jui.core.client.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import com.effacy.jui.platform.util.client.ComparisonSupport;
import com.effacy.jui.platform.util.client.DateSupport;
import com.effacy.jui.platform.util.client.Logger;
import com.google.gwt.i18n.client.NumberFormat;

/**
 * A collection of useful formatting utilities.
 *
 * @author Jeremy Buckley
 */
public final class FormatSupport {

    /**
     * Generalised formatter.
     */
    public interface IFormatter {

        /**
         * Takes an object and attempts to format the object.
         * 
         * @param obj
         *            the object to format.
         * @return the formatted object.
         */
        public String format(Object obj);


        /**
         * Determines if this formatter is able to format the given object type.
         * 
         * @param obj
         *            the object type.
         * @return {@code true} if it can format it.
         */
        public boolean accept(Object obj);


        /**
         * A lambda-friendly way of constructing a formatter.
         * 
         * @param acceptor
         *            an acceptance function.
         * @param converter
         *            a conversion function.
         * @return the formatter.
         */
        public static IFormatter construct(Function<Object, Boolean> acceptor, Function<Object, String> converter) {
            return new IFormatter () {

                @Override
                public String format(Object obj) {
                    if (converter == null)
                        return "";
                    return converter.apply (obj);
                }


                @Override
                public boolean accept(Object obj) {
                    if (acceptor == null)
                        return false;
                    return acceptor.apply (obj);
                }

            };
        }

    }

    /**
     * Collection of registered formatters.
     */
    private static final List<IFormatter> FORMATTERS = new ArrayList<> ();

    /**
     * Registers the given formatter.
     * <p>
     * Will be registered first so will override any other formatters.
     * 
     * @param formatter
     *            the formatter.
     */
    public static void register(IFormatter formatter) {
        if (formatter != null)
            FORMATTERS.add (0, formatter);
    }


    /**
     * Attempts to format a general object.
     * <p>
     * If the passed object is {@code null} then {@code null} will be returned.
     * Otherwise the first registered formatter will be used that accepts the
     * object. Failing that the objects {@link Object#toString()} result will be
     * returned (unless it is a common type in which case a common type version
     * will be returned).
     * <p>
     * Note that a {@link String} type will always be returned as-is.
     * 
     * @param obj
     *            the object to format.
     * @return the formatted object.
     */
    public static String format(Object obj) {
        if (obj == null)
            return null;
        if (obj instanceof String)
            return (String) obj;
        for (IFormatter formatter : FORMATTERS) {
            if (formatter.accept (obj))
                return formatter.format (obj);
        }
        if (obj instanceof Date)
            return asDate ((Date) obj);
        if (obj instanceof Number)
            return asNumber ((Number) obj);
        return obj.toString ();
    }


    /**
     * Attempts to format a general object.
     * 
     * @param obj
     *            the object to format.
     * @param def
     *            a default if the object could not be formatted.
     * @return the formatted object.
     */
    public static String format(Object obj, String def) {
        String format = format (obj);
        return (format != null) ? format : def;
    }

    /**
     * Displays the passed double value with the given number of decimal places.
     * 
     * @param value
     *              the value to display.
     * @param dp
     *              the number of decimal places.
     * @return the formatted number.
     */
    public static String asDouble(double value, int dp) {
        return Native.toFixed (value, dp);
    }


    /**
     * Renders the passed value as a percentage. This will multiple the value by
     * 100 and add a percentage sign.
     * 
     * @param value
     *            the value to render.
     * @return the rendering.
     */
    public static String asPercentage(double value, int dp) {
        return Native.toFixed (value * 100.0, dp) + "%";
    }


    /**
     * See {@link #asPercentage(double, int)} except that if the amount displays
     * less than the dp the write as much.
     * 
     * @param value
     *            the value to render.
     * @return the rendering.
     */
    public static String asPercentageWithLessThan(double value, int dp) {
        double dpScale = Math.pow (10, dp);
        if ((value > 0.0) && (value * dpScale) < 0.5)
            return "<" + Native.toFixed (1.0 / dpScale, dp) + "%";
        return Native.toFixed (value * 100.0, dp) + "%";
    }

    /**
     * Converts to a fraction.
     * 
     * @param numerator
     *                    the numerator (top).
     * @param denominator
     *                    the denominator (bottom).
     * @param nan
     *                    the value to return when the denominator is zero.
     */
    public static double fraction(int numerator, int denominator, double nan) {
        if (denominator == 0)
            return nan;
        return ((double) numerator) / ((double) denominator);
    }

    /**
     * Converts to a percentage.
     * 
     * @param numerator
     *                    the numerator (top).
     * @param denominator
     *                    the denominator (bottom).
     * @param nan
     *                    the value to return when the denominator is zero.
     */
    public static double percentage(int numerator, int denominator, double nan) {
        if (denominator == 0)
            return nan;
        return ((double) (100 * numerator)) / ((double) denominator);
    }

    /**
     * Display the number to the given number of decimal places.
     * 
     * @param number
     *                    the number to display.
     * @param dp
     *                    the precision in decimal places.
     * @return
     */
    public static String asNumber(Number number, int dp) {
        if (number == null)
            return "-";
        return Native.toFixed(number.doubleValue (), dp);
    }

    /**
     * Display the number to the given number of decimal places.
     * 
     * @param number
     *                    the number to display.
     * @param dp
     *                    the precision in decimal places.
     * @return
     */
    public static String asNumberPadded(Number number, int dp) {
        if (number == null)
            return "-";
        if (dp <= 0)
            return asNumber (number);
        if (dp > 9)
            dp = 9;
        return FORMAT_DP[dp].format (number);
    }

    /**
     * Display the number to the given number of decimal places.
     * 
     * @param number
     *                    the number to display.
     * @param dp
     *                    the precision in decimal places.
     * @return
     */
    public static String asNumberPaddedIfPresent(Number number, int dp) {
        if (number == null)
            return "-";
        if (dp < 0)
            return asNumber (number);
        if (dp > 9)
            dp = 9;
        double fractional = Math.abs(number.doubleValue ()) % 1;
        if (fractional < THRESHOOLD_TEN_POWERS_TABLE[dp])
            return FORMAT_DP[0].format (number);
        return FORMAT_DP[dp].format (number);
    }


    /**
     * Nicely formats a currency value. If {@code null} is passed then a "-" is
     * returned.
     * 
     * @param number
     *            the number to format.
     * @return the formatted number.
     */
    public static String asCurrency(Number number, String currencyCode) {
        if (number == null)
            return "-";
        if (currencyCode == null)
            return NumberFormat.getCurrencyFormat ().format (number);
        try {
            return NumberFormat.getCurrencyFormat (currencyCode.toUpperCase ()).format (number);
        } catch (Throwable e) {
            Logger.reportUncaughtException (e);
            return NumberFormat.getCurrencyFormat ().format (number);
        }
    }


    /**
     * Nicely formats a number. If {@code null} is passed then a "-" is
     * returned.
     * 
     * @param number
     *            the number to format.
     * @return the formatted number.
     */
    public static String asNumber(Number number) {
        if (number == null)
            return "-";
        return NumberFormat.getDecimalFormat ().format (number);
    }


    /**
     * A special case where the number is a long value that encodes a decimal.
     * The fractional part is encoded in the last places.
     * 
     * @param number
     *            the number to format.
     * @param dp
     *            the number of DP (maximum is 9).
     * @param padFraction
     *            {@code true} if to pad out to the right the fractional
     *            component to the give number of dp.
     * @return the formatted number.
     */
    public static String asEncodedDecimal(long number, int dp, boolean padFraction) {
        if (dp <= 0)
            return asNumber (number);
        if (dp > 9)
            dp = 9;
        double value = (double) number / (double) LONG_TEN_POWERS_TABLE[dp];
        if (!padFraction)
            return asNumber (value);
        return FORMAT_DP[dp].format (value);
    }


    /**
     * Formats a date.
     * 
     * @param date
     *            the date.
     * @return the formatter date.
     */
    public static String asDate(Date date) {
        return asDate (date, "No date");
    }


    /**
     * Formats a date.
     * 
     * @param date
     *            the date.
     * @param def
     *            a default if the date is {@code null}.
     * @return the formatter date.
     */
    public static String asDate(Date date, String def) {
        if (date == null)
            return def;
        return DateSupport.formatDate (date);
    }


    /**
     * Formats a date as a month and day.
     * 
     * @param date
     *            the date.
     * @return the formatter date.
     */
    public static String asMonthDay(Date date) {
        return asMonthDay (date, "No date");
    }


    /**
     * Formats a date as a month and day.
     * 
     * @param date
     *            the date.
     * @param def
     *            a default if the date is {@code null}.
     * @return the formatter date.
     */
    public static String asMonthDay(Date date, String def) {
        if (date == null)
            return def;
        return DateSupport.formatMonthDay (date);
    }


    /**
     * Formats as a file size.
     * 
     * @param size
     *            the size of the file.
     * @return a formatted file size.
     */
    public static String asFileSize(Number size) {
        return SizeSupport.convertToDataSize (size.intValue (), 1);
    }


    /**
     * Simple mechanism to select the first non-null value.
     * 
     * @param values
     *            the value to select from.
     * @return the first non-null value.
     */
    @SafeVarargs
    public static <T> T nonNull(T... values) {
        for (T value : values) {
            if (value != null)
                return value;
        }
        return null;
    }


    /**
     * Simple mechanism to select the first non-empty (see
     * {@link ComparisonSupport#empty(Object)}) value.
     * 
     * @param values
     *            the value to select from.
     * @return the first non-empty value.
     */
    @SafeVarargs
    public static <T> T nonEmpty(T... values) {
        for (T value : values) {
            if (!ComparisonSupport.empty (value))
                return value;
        }
        return null;
    }


    /**
     * Private constructor.
     */
    private FormatSupport() {
        // Nothing.
    }

    /**
     * Native support methods for formatting.
     */
    public final static class Native {

        /**
         * Rounds a double to the given number of decimal places and returns as
         * a string (see also {@link RoundFunction#round(double, int)} which
         * returns a number).
         * <p>
         * Uses the JavaScripy Number.toFixed function.
         * 
         * @param d
         *            the number to format.
         * @param decimalPlaces
         *            the number of decimal places to round to.
         * @return the formatted number.
         */
        public static native String toFixed(double d, int decimalPlaces)
        /*-{
            return d.toFixed(decimalPlaces);
        }-*/;
    }

    private static final long [] LONG_TEN_POWERS_TABLE = {
            1, // 0 / 10^0
            10, // 1 / 10^1
            100, // 2 / 10^2
            1000, // 3 / 10^3
            10000, // 4 / 10^4
            100000, // 5 / 10^5
            1000000, // 6 / 10^6
            10000000, // 7 / 10^7
            100000000, // 8 / 10^8
            1000000000, // 9 / 10^9
    };

    private static final double [] THRESHOOLD_TEN_POWERS_TABLE = {
            0.5, // 0 / 10^0
            0.05, // 1 / 10^1
            0.005, // 2 / 10^2
            0.0005, // 3 / 10^3
            0.00005, // 4 / 10^4
            0.000005, // 5 / 10^5
            0.0000005, // 6 / 10^6
            0.00000005, // 7 / 10^7
            0.000000005, // 8 / 10^8
            0.0000000005, // 9 / 10^9
    };

    private static final NumberFormat [] FORMAT_DP = {
            NumberFormat.getFormat ("#,##0;-#,##0"),
            NumberFormat.getFormat ("#,##0.0;-#,##0.0"),
            NumberFormat.getFormat ("#,##0.00;-#,##0.00"),
            NumberFormat.getFormat ("#,##0.000;-#,##0.000"),
            NumberFormat.getFormat ("#,##0.0000;-#,##0.0000"),
            NumberFormat.getFormat ("#,##0.00000;-#,##0.00000"),
            NumberFormat.getFormat ("#,##0.000000;-#,##0.000000"),
            NumberFormat.getFormat ("#,##0.0000000;-#,##0.0000000"),
            NumberFormat.getFormat ("#,##0.00000000;-#,##0.00000000"),
    };
}
