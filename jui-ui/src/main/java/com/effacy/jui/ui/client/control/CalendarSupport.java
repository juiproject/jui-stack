package com.effacy.jui.ui.client.control;

/**
 * These will eventually be moved into a separate JS file rather than using the
 * GWT native construct.
 */
public class CalendarSupport {

    public enum FORMAT_WEEKDAY {
        NONE, SHORT, LONG;
    }

    public enum FORMAT_YEAR {
        NONE, NUMERIC;
    }

    public enum FORMAT_MONTH {
        NONE, SHORT, LONG;
    }
    
    public enum FORMAT_DAY {
        NONE, NUMERIC;
    }

    public static String nameOfMonth(String locale, int month) {
        return _formatDate(locale, 2000, month, 1, null, null, "long", null);
    }

    public static String formatDate(String locale, int year, int month, int day, FORMAT_WEEKDAY fWeekday, FORMAT_YEAR fyear, FORMAT_MONTH fMonth, FORMAT_DAY fDay) {
        return _formatDate(locale, year, month, day,
            ((fWeekday == null) || fWeekday == FORMAT_WEEKDAY.NONE) ? null : fWeekday.name().toLowerCase(),
            ((fyear == null) || fyear == FORMAT_YEAR.NONE) ? null : fyear.name().toLowerCase(),
            ((fMonth == null) || fMonth == FORMAT_MONTH.NONE) ? null : fMonth.name().toLowerCase(),
            ((fDay == null) || fDay == FORMAT_DAY.NONE) ? null : fDay.name().toLowerCase()
        );
    }

    public static native String _formatDate(String locale, int year, int month, int day, String fWeekday, String fYear, String fMonth, String fDay) /*-{
        if (locale == null)
            locale = 'en-us';
        var opts = {};
        if (fWeekday != null)
            opts.weekday = fWeekday;
        if (fYear != null)
            opts.year = fYear;
        if (fMonth != null)
            opts.month = fMonth;
        if (fDay != null)
            opts.day = fDay;
        if (opts.length === 0)
            return new Date(year,month - 1,day).toLocaleDateString();
        return new Date(year,month - 1,day).toLocaleDateString(locale, opts);
    }-*/;

    public static native CalendarDate normalise(int year, int month, int day) /*-{
        var date = new Date(year,month - 1,day);
        return new $wnd.com.effacy.jui.ui.client.control.CalendarDate(date.getFullYear(), date.getMonth() + 1, date.getDate());
    }-*/;

    /**
     * Determines the number of days in the given month.
     * 
     * @param year
     *              the year (i.e. 2021).
     * @param month
     *              the month in the year (i.e. 1 for Janurary).
     * @return the number of days in the given month.
     */
    public static native int daysInMonth(int year, int month) /*-{
        // The month passed is the following month (from 0) and setting date to 0 is end of prior month.
        return new Date(year,month,0).getDate();
    }-*/;

    public static native int dayInWeek(int year, int month, int day) /*-{
        return new Date(year,month - 1,day).getDay() + 1;
    }-*/;

    /**
     * Given a year and a month return an array of day-in-month values (indexed from
     * 1) so that the first entry is a sunday, that all days in the target month are
     * represented and the length is divisible by a week length (7 days).
     * <p>
     * Note that this means that leading and trailing days could be in the prior
     * month (leading) or following month (trailing). In all cases the first day 1
     * will always correspond to the target month.
     * 
     * @param year
     *              the desired year.
     * @param month
     *              the desired month (in the year, indexed from 1).
     * @return the array of days-in-month.
     */
    public static int[] dateTable(int year, int month) {
        int startDayOfWeek = CalendarSupport.dayInWeek(year, month, 1) - 1;
        int daysInLastMonth = CalendarSupport.daysInMonth(year, month - 1);
        int daysInThisMonth = CalendarSupport.daysInMonth(year, month);

        int length = startDayOfWeek + daysInThisMonth;
        if (length % 7 != 0)
            length += (7 - (length % 7));
        int[] dateTable = new int[length];
        int idx = 0;
        while (startDayOfWeek > 0)
            dateTable[idx++] = (daysInLastMonth - (--startDayOfWeek));
        for (int i = 1; i <= daysInThisMonth; i++)
            dateTable[idx++] = i;
        for (int i = 1; idx < length; i++)
            dateTable[idx++] = i;
        return dateTable;
    }

    /**
     * For the given date table find the index of the start of the month.
     * 
     * @param dateTable
     *                  the date table.
     * @return the index of the first day of the represented month.
     */
    public static int startOfMonth(int[] dateTable) {
        for (int i = 0; i < dateTable.length; i++) {
            if (dateTable[i] == 1)
                return i;
        }
        return -1;
    }

    /**
     * For the given date table find the index of the end of the month.
     * 
     * @param dateTable
     *                  the date table.
     * @return the index of the last day of the represented month.
     */
    public static int endOfMonth(int[] dateTable) {
        boolean found = false;
        for (int i = 0; i < dateTable.length; i++) {
            if (dateTable[i] == 1) {
                if (found)
                    return i-1;
                found = true;
            }
        }
        return (dateTable.length - 1);
    }

    /**
     * Given a date table representing the given date range, find the index into
     * that table for the given date.
     * 
     * @param dateTable
     *                  the date table to reference (if {@code null} then one will
     *                  be generated).
     * @param range
     *                  the associated range that generated it.
     * @param date
     *                  the date to map.
     * @return the index (or {@code -1}).
     */
    public static int indexOf(int[] dateTable, CalendarDate range, CalendarDate date) {
        if ((range == null) || (date == null))
            return -1;
        if (range.year() != date.year())
            return -1;
        if (range.month() != date.month())
            return -1;
        if (dateTable == null)
            dateTable = CalendarSupport.dateTable(range.year(), range.month());
        return date.day() + startOfMonth(dateTable) - 1;
    }
}
