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
     * 
     * @param adjust
     *               the adjustment.
     * @return the date.
     */
    public CalendarDate month(int adjust) {
        return CalendarDate.from(year, month + adjust, day);
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
