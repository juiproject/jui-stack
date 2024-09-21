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
package com.effacy.jui.platform.util.client;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;

/**
 * Date management utilities.
 *
 * @author Jeremy Buckley
 */
public final class DateSupport {

    /**
     * Default date format.
     */
    public static final DateTimeFormat DAY_FORMAT = DateTimeFormat.getFormat ("d");

    /**
     * Default date format.
     */
    public static final DateTimeFormat MONTH_FORMAT = DateTimeFormat.getFormat ("MMM");

    /**
     * Default date format.
     */
    public static final DateTimeFormat DATE_FORMAT = DateTimeFormat.getFormat ("d MMM yyyy");

    /**
     * Default month-day format.
     */
    public static final DateTimeFormat MONTHDAY_FORMAT = DateTimeFormat.getFormat ("d MMMM");

    /**
     * Default time format.
     */
    public static final DateTimeFormat TIME_FORMAT = DateTimeFormat.getFormat ("hh:mma");

    /**
     * Default time format.
     */
    public static final DateTimeFormat DATE_TIME_FORMAT = DateTimeFormat.getFormat ("d MMM yyyy hh:mma");

    /**
     * Default month-year format.
     */
    public static final DateTimeFormat MONTH_YEAR_FORMAT = DateTimeFormat.getFormat ("MMM yyyy");

    /**
     * Millis in an hour.
     */
    public static final long HOUR = 60 * 60 * 1000;

    /**
     * Millis in a day.
     */
    public static final long DAY = HOUR * 24;

    /**
     * Millis in a week.
     */
    public static final long WEEK = DAY * 7;

    /**
     * Millis in a year.
     */
    public static final long YEAR = DAY * 365;

    /**
     * Return the date as a string in a consistently formatted manner.
     * 
     * @param date
     *             the date to format.
     * @return the date with a format of 'd MMM yyyy', or a blank string if the date
     *         is null;
     */
    public static String formatDate(Date date) {
        if (date == null)
            return null;
        return DATE_FORMAT.format (date);
    }

    /**
     * Return the date as a string in a consistently formatted manner.
     * 
     * @param date
     *             the date to format.
     * @return the date with a format of 'd MMM', or a blank string if the date is
     *         null;
     */
    public static String formatMonthDay(Date date) {
        if (date == null)
            return null;
        return MONTHDAY_FORMAT.format (date);
    }

    /**
     * Return the date as a string in a consistently formatted manner.
     * 
     * @param date
     *             the date to format.
     * @return the date with a format of 'd', or a blank string if the date is
     *         {@code null}.
     */
    public static String formatDay(Date date) {
        if (date == null)
            return null;
        return DAY_FORMAT.format (date);
    }

    /**
     * Return the date as a string in a consistently formatted manner.
     * 
     * @param date
     *             the date to format.
     * @return the date with a format of 'MMM', or a blank string if the date is
     *         {@code null}.
     */
    public static String formatMonth(Date date) {
        if (date == null)
            return null;
        return MONTH_FORMAT.format (date);
    }

    /**
     * Return the date as a string in a consistently formatted manner.
     * 
     * @param date
     *             the date to format.
     * @return the date with a format of 'MMM yyyy', or a blank string if the date
     *         is {@code null}.
     */
    public static String formatMonthYear(Date date) {
        if (date == null)
            return null;
        return MONTH_YEAR_FORMAT.format (date);
    }

    /**
     * Formats the date and time components as a string in a consistent manner.
     * 
     * @param date
     *             the date and time to format.
     * @return the time with a format of 'hh:ss', or a blank string if the date is
     *         {@code null}.
     */
    public static String formatTime(Date date) {
        if (null == date)
            return null;
        return TIME_FORMAT.format (date).toLowerCase ();
    }

    /**
     * Converts a date to an expression representing how long ago the date occurred.
     * 
     * @param date
     *             the date to check.
     * @return An expression of the amount of time ago.
     */
    public static String formatDateTime(Date date) {
        return DATE_TIME_FORMAT.format (date);
    }

    /**
     * Parse a string of format 'd MMM yyyy' to a date.
     * 
     * @param dateString
     *                   the date to parse
     * @return a date, or null if the date
     */
    public static Date parseDate(String dateString) {
        if (null == dateString)
            return null;
        return DATE_FORMAT.parse (dateString);
    }

    /**
     * Determines if the given date is older than the given number of days from the
     * current date and time.
     * 
     * @param date
     *             the date to compare.
     * @param days
     *             the number of days to check age against.
     * @return {@code true} if the date is older than the current date but at least
     *         the given number of days.
     */
    public static boolean isOlderThanDays(Date date, int days) {
        if (days <= 0)
            return false;
        Date now = new Date ();
        long difference = now.getTime () - date.getTime ();
        if (difference > days * 24 * 60 * 60 * 1000L)
            return true;
        return false;
    }

    @SuppressWarnings("deprecation")
    public static boolean isWithinDays(Date date, int days) {
        if (days <= 0)
            return false;
        Date now = new Date ();
        if (date.before (now))
            return false;
        now.setDate (date.getDate () + days);
        return date.before (now);
    }

    /**
     * Perform a date comparison based only on the day and date.
     * 
     * @param date
     *             the date to compare with.
     * @param days
     *             the number of days to compare.
     * @return {@code true} if they are within each other.
     */
    @SuppressWarnings("deprecation")
    public static boolean isWithinDaysByDayAndMonth(Date date, int days) {
        if (days <= 0)
            return false;
        Date now = new Date ();
        date = new Date (date.getTime ());

        // Normalize to the same year.
        now.setYear (10);
        date.setYear (10);

        // If the date is after now we just need to check that now plus days is
        // after the date.
        if (date.after (now)) {
            now.setDate (now.getDate () + days);
            return now.after (date);
        }

        // Here we need to cater for modulus year.
        now.setDate (now.getDate () + days);
        if (now.getYear () == 10)
            return false;
        now.setYear (10);
        return now.after (date);
    }

    /**
     * Transform the passed date to the end of the day.
     * 
     * @param date
     *             the date to convert (if {@code null} the date will be calculated
     *             as now).
     * @return the date at the end of the day.
     */
    public static Date toEndOfDay(Date date) {
        if (date == null)
            date = new Date ();
        date.setHours (23);
        date.setMinutes (59);
        date.setSeconds (59);
        return date;
    }

    /**
     * Transform the passed date to the start of the day.
     * 
     * @param date
     *             the date to convert (if {@code null} the date will be calculated
     *             as now).
     * @return the date at the start of the day.
     */
    public static Date toStartOfDay(Date date) {
        if (date == null)
            date = new Date ();
        date.setHours (0);
        date.setMinutes (0);
        date.setSeconds (0);
        return date;
    }

    /**
     * Private constructor.
     */
    private DateSupport() {
        // Nothing.
    }
}
