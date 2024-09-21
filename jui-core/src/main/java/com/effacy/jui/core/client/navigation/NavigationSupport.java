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
package com.effacy.jui.core.client.navigation;

import java.util.ArrayList;
import java.util.List;

/**
 * A collection of utilities to work (primarily) with navigation paths.
 */
public final class NavigationSupport {

    /**
     * Performs a safe copy of the passed path (so a {@code null} is turned into an
     * empty list).
     * 
     * @param path
     *             the path to copy.
     * @return the copy of the path (never {@code null}).
     */
    public static List<String> copy(List<String> path) {
        if (path == null)
            return new ArrayList<>();
        return new ArrayList<>(path);
    }

    /**
     * See {@link #build(List)}.
     * 
     * @param path
     *             the path to construct (as an array of components).
     * @return the printable path.
     */
    public static String build(String... path) {
        String printable = "";
        if (path != null) {
            for (String item : path) {
                if (item == null)
                    continue;
                item = item.trim ();
                if (item.length() == 0)
                    continue;
                printable += "/" + item.trim ();
            }
        }
        return printable;
    }

    /**
     * Convenience to build a printable path. Elements are separated by a forward
     * slash and the path commences with a forward slash, unless there are no
     * elements in the path in which case the empty string is returned
     * <p>
     * Note that {@code null} values and blank components are treated as
     * non-existent.
     * 
     * @param path
     *             the path (as a list of components).
     * @return the printable path.
     */
    public static String build(List<String> path) {
        return build (path, 0);
    }

    /**
     * Convenience to build a printable path. Elements are separated by a forward
     * slash and the path commences with a forward slash, unless there are no
     * elements in the path in which case the empty string is returned
     * <p>
     * Note that {@code null} values and blank components are treated as
     * non-existent.
     * 
     * @param path
     *             the path (as a list of components).
     * @param max
     *             the maximum number of components to convert (less than or equal to 0 is no
     *             limit).
     * @return the printable path.
     */
    public static String build(List<String> path, int max) {
        String printable = "";
        if (path != null) {
            int count = 0;
            for (String item : path) {
                if (item == null)
                    continue;
                item = item.trim ();
                if (item.length() == 0)
                    continue;
                printable += "/" + item.trim ();
                if ((max > 0) && (++count >= max))
                    break;
            }
        }
        return printable;
    }

    /**
     * Convenience to split a path across the forward-slash separator into
     * components.
     * <p>
     * This correctly deals with leading and trailing slashes as well as multiple
     * slashes (in this instance multiples are treated as a single slash).
     * 
     * @param path
     *             the path to split.
     * @return the path components.
     */
    public static List<String> split(String path) {
        if ((path == null) || path.trim ().isEmpty ())
            return new ArrayList<> ();
        List<String> parts = new ArrayList<> ();
        for (String part : path.split ("/")) {
            part = part.trim ();
            if (part.isEmpty ())
                continue;
            parts.add (part);
        }
        return parts;
    }
}
