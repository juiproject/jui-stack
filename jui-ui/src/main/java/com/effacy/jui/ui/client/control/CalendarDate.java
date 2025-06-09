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

import java.util.Date;

import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsProperty;

/**
 * Holds a local date (explicit year, month and day).
 * <p>
 * Note that this is exposed via the JSInterop standard so requires the
 * requisite compilation options to export (this is only so as to be able to
 * construct and return an instance from JS).
 */
public class CalendarDate {

    /**
     * See {@link #year()}.
     */
    @JsProperty
    private int year;

    /**
     * See {@link #month()}.
     */
    @JsProperty
    private int month;

    /**
     * See {@link #day()}.
     */
    @JsProperty
    private int day;

    /**
     * Construct with initial date components.
     * 
     * @param year
     *              the year (i.e. 2018).
     * @param month
     *              the month in the year (i.e. 1 for January).
     * @param day
     *              the day of the month (i.e. 1 for the first day).
     */
    @JsConstructor
    public CalendarDate(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    /**
     * The year (Gregorian).
     * 
     * @return the year.
     */
    public int year() {
        return year;
    }

    /**
     * The month of the year (starting from 1 and extending to 12, inclusive).
     * 
     * @return the month.
     */
    public int month() {
        return month;
    }

    /**
     * The day of the month (starting from 1).
     * 
     * @return the day.
     */
    public int day() {
        return day;
    }

    /**
     * Adjusts the date by the given number of months.
     * <p>
     * Note that this is day-safe, if the resulting day does not lie in the month
     * then it will be adjusted to be the end of the month.
     * 
     * @param adjust
     *               the adjustment.
     * @return the date.
     */
    public CalendarDate month(int adjust) {
        CalendarDate d = CalendarDate.from(year, month + adjust + 1, -1);
        if (d.day <= day)
            return d;
        d.day = day;
        return d;
    }

    /**
     * Adjusts the date by the given number of days.
     * 
     * @param adjust
     *               the adjustment.
     * @return the date.
     */
    public CalendarDate day(int adjust) {
        return CalendarDate.from(year, month, day + adjust);
    }

    /**
     * Returns a date that is the start of the month.
     * 
     * @return the date.
     */
    public CalendarDate monthStart() {
        return new CalendarDate(year, month, 1);
    }

    /**
     * Returns a date that is the end of the month.
     * 
     * @return the date.
     */
    public CalendarDate monthEnd() {
        return new CalendarDate(year, month + 1, 1).day(-1);
    }

    /**
     * Determines if this date is before (strictly) the passed.
     * 
     * @param compare
     *                the date to compare to (if {@code null} then returns
     *                {@code true}).
     * @return {@code true} if this is before the passed.
     */
    public boolean before(CalendarDate compare) {
        if (compare == null)
            return false;
        if (year < compare.year)
            return true;
        if (year > compare.year)
            return false;
        if (month < compare.month)
            return true;
        if (month > compare.month)
            return false;
        return (day < compare.day);
    }

    /**
     * Determines if this date is after (strictly) the passed.
     * 
     * @param compare
     *                the date to compare to (if {@code null} then returns
     *                {@code true}).
     * @return {@code true} if this is before the passed.
     */
    public boolean after(CalendarDate compare) {
        if (compare == null)
            return false;
        if (year > compare.year)
            return true;
        if (year < compare.year)
            return false;
        if (month > compare.month)
            return true;
        if (month < compare.month)
            return false;
        return (day > compare.day);
    }

    @Override
    public String toString() {
        return day + ":" + month + ":" + year;
    }

    /**
     * Convert to a Java data.
     * 
     * @return the date.
     */
    @SuppressWarnings("deprecation")
    public Date toDate() {
        return new Date(year - 1900, month - 1, day);
    }

    /**
     * Converts a Java date to a calendar date.
     * 
     * @param date
     *             the date to convert.
     * @return the calendar date.
     */
    @SuppressWarnings("deprecation")
    public static CalendarDate from(Date date) {
        if (date == null)
            return null;
        return new CalendarDate(date.getYear() + 1900, date.getMonth() + 1, date.getDate());
    }

    /**
     * Create a normalised date (i.e. will adjust to a valid date).
     * <p>
     * Note that the constructor does not do this.
     * 
     * @param year
     *              the year.
     * @param month
     *              the month (indexed from 1).
     * @param day
     *              the day in month (indexed from 1).
     * @return the calendar date.
     */
    public static CalendarDate from(int year, int month, int day) {
        return CalendarSupport.normalise(year, month, day);
    }

    /**
     * Obtains the current date.
     * 
     * @return the current date.
     */
    public static CalendarDate now() {
        Date date = new Date();
        return new CalendarDate(date.getYear() + 1900, date.getMonth() + 1, date.getDate());
    }
}
