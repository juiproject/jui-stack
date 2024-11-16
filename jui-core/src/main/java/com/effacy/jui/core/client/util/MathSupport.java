package com.effacy.jui.core.client.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Simple mathematical functions and operations.
 */
public class MathSupport {

    public static class Bin {

        private int n;

        private double p = 1.0;

        private double left;

        private double right;

        /**
         * The number of items in the bin.
         * 
         * @return the number.
         */
        public int n() {
            return n;
        }

        /**
         * As a proprotion of the maximum.
         * <p>
         * If there are no data then this will necessarialy be 1 even though
         * {@link #n()} returns 0.
         * 
         * @return the proportion.
         */
        public double p() {
            return p;
        }

        /**
         * The left endpoint of the bin.
         * 
         * @return the left endpoint (inclusive).
         */
        public double left() {
            return left;
        }

        /**
         * The right endpoint of the bin.
         * 
         * @return the right endpoint (exclusive).
         */
        public double right() {
            return right;
        }
    }

    public static class BinStatistics {

        private Bin[] data;

        private int n;

        public Bin[] data() {
            return data;
        }

        public int n() {
            return n;
        }
    }

    /**
     * Capture summary statistics over a data sample.
     */
    public static class SummaryStatistics {

        private int n;
        private double average;
        private double std;
        private double median;
        private double q1;
        private double q3;

        /**
         * The sample size (number of datum).
         * 
         * @return the size.
         */
        public int n() {
            return n;
        }

        /**
         * The average of the sample.
         * 
         * @return the average.
         */
        public double average() {
            return average;
        }
        
        /**
         * The standard deviation of the sample.
         * 
         * @return the standard deviation.
         */
        public double std() {
            return std;
        }
        
        /**
         * The median of the sample.
         * 
         * @return the median.
         */
        public double median() {
            return median;
        }
        
        /**
         * The lower quartile (25th percentile) of the sample.
         * 
         * @return the lower quartile.
         */
        public double q1() {
            return q1;
        }
        
        /**
         * The upper quartile (75th percentile) of the sample.
         * 
         * @return the upper quartile.
         */
        public double q3() {
            return q3;
        }
        
    }

    /**
     * Simple binning mechanism that bins data into bins of a given width starting
     * at the start value and of the specified number of bins.
     * <p>
     * Note that data falling outside of the range will be attributed to the
     * respective bounding bin.
     * 
     * @param data
     *              the data to bin.
     * @param start
     *              the start of the first bin (left endpoint).
     * @param width
     *              the the width of the bin (determines the right endpoint of the
     *              current bin, which is exclusive, and the left endpoint of the
     *              next bin).
     * @param bins
     *              the total number of bins (empty bins will have a zero).
     * @return the resultant binning.
     */
    public static BinStatistics bin(List<Number> data, double start, double width, int bins) {
        if ((bins <= 0) || (width <= 0.0)) {
            BinStatistics results = new BinStatistics();
            results.data = new Bin [0];
            return results;
        }
        BinStatistics results = new BinStatistics();
        results.data = new Bin [bins];
        for (int i = 0; i < bins; i++) {
            Bin bin = new Bin();
            bin.left = start + (i * width);
            bin.right = bin.left + width;
            results.data[i] = bin;
        }
        if (data == null)
            return results;

        // Determine the bin counts.
        int max = 0;
        for (Number num : data) {
            if (num == null)
                continue;
            double val = num.doubleValue();
            int idx = (int) Math.floor((val - start) / width);
            if (idx < 0)
                idx = 0;
            else if (idx >= bins)
                idx = (bins - 1);
            results.data[idx].n++;
            if (max < results.data[idx].n)
                max = results.data[idx].n;
        }

        // Calculate the proportions and totals.
        if (max > 0) {
            for (int i = 0; i < bins; i++) {
                results.data[i].p = results.data[i].n / max;
                results.n += results.data[i].n;
            }
        }
        return results;
    }

    /**
     * Calculates the summary statistics of a list of numbers.
     * 
     * @param data
     *             the data to calculate.
     * @return the stats.
     */
    public static SummaryStatistics summaryStatistics(List<Number> data) {
        SummaryStatistics stats = new SummaryStatistics();
        List<Double> sorted = sort(data);
        stats.n = sorted.size();
        if (stats.n == 0)
            return stats;

        // Calculate quartiles.
        stats.q1 = percentile(data, 25);
        stats.q3 = percentile(data, 75);

        // Calculate average.
        double sum = 0.0;
        for (double num : sorted)
            sum += num;
        stats.average = (sum / (double) stats.n);

        // Calculate standard deviation.
        double varianceSum = 0.0;
        for (double num : sorted)
            varianceSum += Math.pow (num - stats.average, 2);
        stats.std = Math.sqrt(varianceSum / (double) stats.n);

        // Calculate median.
        if ((stats.n % 2) == 1)
            stats.median = sorted.get(stats.n / 2);
        else
            stats.median = (sorted.get(stats.n / 2 - 1) + sorted.get(stats.n / 2)) / 2.0;

        return stats;
    }

    /**
     * Calculates the given percentile for the list of numbers.
     * 
     * @param data
     *                   the data to process.
     * @param percentile
     *                   the percentile to calculate (i.e. 25 or 75).
     * @return the percentile.
     */
    public static double percentile(List<Number> data, int percentile) {
        List<Double> sorted = sort(data);
        if (sorted.isEmpty())
            return 0.0;
        return _percentile(sorted, percentile);
    }

    /**
     * Used to calculated the given percentile for a list of sorted doubles.
     */
    private static double _percentile(List<Double> sorted, int percentile) {
        int n = sorted.size();
        if (n == 0)
            return 0.0;
        if (n == 1)
            return sorted.get(0);

        double rank = ((((double) percentile) * (n - 1)) / 100);
        int floorIndex = (int) Math.floor(rank);
        double fractionalPart = rank - floorIndex;

        // If the fractional part is sufficiently small, then just use the indexed value.
        if (fractionalPart < 0.05)
            return sorted.get(floorIndex);
        
        // Otherwise we interpolate.
        double lowerValue = sorted.get(floorIndex);
        double upperValue = sorted.get(floorIndex + 1);
        return lowerValue + fractionalPart * (upperValue - lowerValue);
    }

    /**
     * Safely converts a list of numbers to a list of double values and sorts them.
     * Any {@code null} value is removed.
     * 
     * @param data
     *             the data to sort.
     * @return the converted and sorted data.
     */
    public static List<Double> sort(List<Number> data) {
        if ((data == null) || data.isEmpty())
            return new ArrayList<>();
        List<Double> sorted = new ArrayList<>();
        data.forEach(num -> {
            if (num != null)
                sorted.add (num.doubleValue());
        });
        Collections.sort(sorted);
        return sorted;
    }
    
    /**
     * Converts a generic list of items to numbers.
     * 
     * @param items
     *                  the items to convert.
     * @param converter
     *                  to convert an item to a number ({@code null} is interpreted
     *                  as no conversion and not added).
     * @return the converted list.
     */
    public static <T> List<Number> convert(List<T> items, Function<T,Number> converter) {
        List<Number> data = new ArrayList<>();
        if (items != null) {
            items.forEach(item -> {
                Number num = converter.apply(item);
                if (num != null)
                    data.add(num);
            });
        }
        return data;
    }

    /**
     * Calculate the proportion a value is between a minimumn and a maximum. For
     * example, a value of 4 between 2 and 6 has a proportion of is 0.5.
     * 
     * @param value
     *              the value to evaluate.
     * @param min
     *              the minimum value.
     * @param max
     *              the maximumn value.
     * @return the proportion that the value represents as a split between the
     *         minimum and maximum values.
     */
    public static double proportion(double value, int min, int max) {
        int range = max - min;
        if (range == 0)
            return 0.0;
        return ((double)(value - min)) / ((double) range);
    }

}
