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

import java.util.HashSet;
import java.util.Set;

import com.effacy.jui.platform.util.client.Logger;

/**
 * Simple timer mechanism for performance analysis.
 *
 * @author Jeremy Buckley
 */
public class MetricsTimer {

    /**
     * If timer stamps are enabled.
     */
    private static boolean ENABLED;

    /**
     * The starting time.
     */
    private static long START;

    /**
     * The level of indentation.
     */
    private static int INDENT;

    /**
     * Enabled scopes.
     */
    private static Set<String> SCOPES = new HashSet<String> ();

    /**
     * The channels that have been enabled.
     */
    private static Set<String> CHANNELS = new HashSet<String> ();


    /**
     * Enables for the given scopes. This is additive.
     * 
     * @param scopes
     *            the scopes to enable.
     */
    public static void enable(String... scopes) {
        ENABLED = true;
        for (String scope : scopes) {
            if (!SCOPES.contains (scope))
                SCOPES.add (scope);
        }
    }


    /**
     * Disables the given scopes.
     * 
     * @param scopes
     *            the scopes to disable (an empty list will disable all scopes).
     */
    public static void disable(String... scopes) {
        ENABLED = false;
        if (scopes.length == 0) {
            SCOPES.clear ();
        } else {
            for (String scope : scopes)
                SCOPES.remove (scope);
        }
    }


    /**
     * Enable the given channels.
     * 
     * @param channels
     *            the channels to enable.
     */
    public static void channels(String... channels) {
        for (String channel : channels) {
            if (!CHANNELS.contains (channel))
                CHANNELS.add (channel);
        }
    }


    /**
     * Starts a timer session.
     * 
     * @param channels
     *            (optional) test for any of the given channels to enable.
     */
    public static void start(String... channels) {
        if (ENABLED && (channels.length > 0)) {
            boolean found = false;
            LOOP: for (String channel : channels) {
                if (CHANNELS.contains (channel)) {
                    found = true;
                    break LOOP;
                }
            }
            if (!found)
                return;
        }
        START = System.currentTimeMillis ();
    }


    /**
     * Ends the timer session.
     */
    public static void end() {
        START = -1;
    }


    /**
     * Creates a timer stamp event that is logged. Note that the timer needs to
     * be running in order for the stamp to take effect.
     * 
     * @param label
     *            a label for the stamp.
     */
    public static void stamp(String label) {
        stamp (null, label);
    }


    /**
     * Creates a timer stamp event that is logged. Note that the timer needs to
     * be running in order for the stamp to take effect.
     * 
     * @param scope
     *            the scope to operate in (requires the scope to be present, use
     *            {@code null} for no scope checking).
     * @param label
     *            the label for the stamp.
     */
    public static void stamp(String scope, String label) {
        if (ENABLED && (START > 0) && ((scope == null) || SCOPES.contains (scope)))
            log (label, false, false);
    }


    /**
     * Logs with an indent. Use this when starting logging in a method.
     * 
     * @param label
     *            the label to associate with the stamp.
     */
    public static void in(String label) {
        in (null, label);
    }


    /**
     * Logs with an indent. Use this when starting logging in a method.
     * 
     * @param scope
     *            the scope to test for (scope needs to be enabled).
     * @param label
     *            the label to associate with the stamp.
     */
    public static void in(String scope, String label) {
        if (ENABLED && (START > 0) && ((scope == null) || SCOPES.contains (scope))) {
            INDENT++;
            log (label, true, false);
        }
    }


    /**
     * Logs with an out-dent. Use this when ending logging in a method.
     * 
     * @param label
     *            the label to associate with the stamp.
     */
    public static void out(String label) {
        out (null, label);
    }


    /**
     * Logs with an out-dent. Use this when ending logging in a method.
     * 
     * @param scope
     *            the scope to test for (scope needs to be enabled).
     * @param label
     *            the label to associate with the stamp.
     */
    public static void out(String scope, String label) {
        if (ENABLED && (START > 0) && ((scope == null) || SCOPES.contains (scope))) {
            log (label, false, true);
            INDENT--;
        }
    }


    /**
     * Internal method to write a log entry.
     * 
     * @param label
     *            (optional) the display label.
     * @param in
     *            if marked for in (start).
     * @param out
     *            if marked for out (end).
     */
    private static void log(String label, boolean in, boolean out) {
        String message = "[Timer] ";
        for (int i = 0; i < INDENT; i++)
            message += " ";
        if (label != null)
            message += "{" + label + "}";
        if (in)
            message += ">";
        if (out)
            message += "<";
        message += " " + (System.currentTimeMillis () - START) + "ms";
        Logger.log (message);
    }
}
