/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.effacy.jui.codeserver.gwt;

import com.google.gwt.dev.json.JsonArray;
import com.google.gwt.dev.json.JsonException;
import com.google.gwt.dev.json.JsonObject;
import com.google.gwt.dev.util.Util;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * In-memory representation of a SourceMap.
 */
public class SourceMap {

    /**
     * Loads up the source map from a JSON file.
     * 
     * @param file
     *             the file to load.
     * @return the source map.
     */
    public static SourceMap load(File file) {
        String sourceMapJson = Util.readFileAsString (file);
        try {
            return new SourceMap (JsonObject.parse (new StringReader (sourceMapJson)));
        } catch (JsonException | IOException e) {
            throw new RuntimeException("can't parse sourcemap as json", e);
        } 
    }

    private final JsonObject json;

    private SourceMap(JsonObject json) {
        this.json = json;
    }

    /**
     * Returns a sorted list of all the directories containing at least one filename
     * in the source map.
     */
    public List<String> getSourceDirectories() {
        JsonArray sources = (JsonArray) json.get("sources");
        Set<String> directories = new HashSet<String>();
        for (int i = 0; i < sources.getLength (); i++) {
            String filename = sources.get (i).asString ().getString ();
            int lastSlashPos = filename.lastIndexOf ('/');
            directories.add (lastSlashPos < 0 ? "" : filename.substring(0, lastSlashPos));
        }
        List<String> result = new ArrayList<String>();
        result.addAll(directories);
        Collections.sort(result);
        return result;
    }

    /**
     * Returns a sorted list of all filenames in the given directory.
     */
    public List<String> getSourceFilesInDirectory(String parent) {
        if (!parent.endsWith ("/"))
            throw new IllegalArgumentException ("unexpected: " + parent);

        JsonArray sources = (JsonArray) json.get ("sources");
        List<String> result = new ArrayList<String> ();
        for (int i = 0; i < sources.getLength(); i++) {
            String candidate = sources.get(i).asString().getString();
            if (!candidate.startsWith (parent))
                continue;
            int nameStart = candidate.lastIndexOf ('/') + 1;
            if (nameStart == parent.length ())
                result.add (candidate.substring (nameStart));
        }

        return result;
    }
}
