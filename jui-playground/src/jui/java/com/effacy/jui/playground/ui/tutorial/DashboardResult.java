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

import com.effacy.jui.playground.ui.tutorial.DashboardResult.Data.Period;

/**
 * Data transfer object for the dashboard.
 */
public class DashboardResult {

    /**
     * See {@link #getTrafficData()}.
     */
    private List<Data> trafficData = new ArrayList<> ();

    /**
     * See {@link #getUserData()}.
     */
    private List<Data> userData = new ArrayList<> ();

    /**
     * See {@link #getTopPages()}.
     */
    private List<Page> topPages = new ArrayList<> ();

    /**
     * The data sets for traffic.
     * 
     * @return the data sets.
     */
    public List<Data> getTrafficData() {
        return trafficData;
    }

    /**
     * Finds a data set for traffic that matches the given period.
     * 
     * @param period
     *               the period to find.
     * @return the matching data set.
     */
    public double[] findTrafficData(Period period) {
        for (Data data : trafficData) {
            if (data.period == period)
                return data.data;
        }
        return null;
    }

    /**
     * The data sets for unique users.
     * 
     * @return the data sets.
     */
    public List<Data> getUserData() {
        return userData;
    }

    /**
     * Finds a data set for unique users that matches the given period.
     * 
     * @param period
     *               the period to find.
     * @return the matching data set.
     */
    public double[] findUserData(Period period) {
        for (Data data : userData) {
            if (data.period == period)
                return data.data;
        }
        return null;
    }

    /**
     * The top pages by access.
     * 
     * @return the pages.
     */
    public List<Page> getTopPages() {
        return topPages;
    }

    public static class Page {
        private String name;
        private String description;
        private int percentage;
        private int hits;

        public Page(String name, String description, int percentage, int hits) {
            this.name = name;
            this.description = description;
            this.percentage = percentage;
            this.hits = hits;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public int getPercentage() {
            return percentage;
        }

        public void setPercentage(int percentage) {
            this.percentage = percentage;
        }

        public int getHits() {
            return hits;
        }

        public void setHits(int hits) {
            this.hits = hits;
        }

    }

    /**
     * Captures segmented data.
     */
    public static class Data {

        /**
         * Various periods to which data is associated.
         */
        public enum Period {
            THIS_WEEK, LAST_WEEK, WEEKLY_AV, MONTHLY_AV;
        }

        /**
         * See {@link #getPeriod()}.
         */
        private Period period;

        /**
         * See {@link #getData()}.
         */
        private double[] data;

        /**
         * Construct with initial data.
         * 
         * @param period
         *               the period.
         * @param data
         *               the data for the period.
         */
        public Data(Period period, double... data) {
            this.period = period;
            this.data = data;
        }

        /**
         * The period the data is associated with.
         * 
         * @return the period.
         */
        public Period getPeriod() {
            return period;
        }

        /**
         * The data associated with the period.
         * 
         * @return the data.
         */
        public double[] getData() {
            return data;
        }

    }

}
