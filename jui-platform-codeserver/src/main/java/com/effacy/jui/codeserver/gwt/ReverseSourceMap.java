/*
 * Copyright 2013 Google Inc.
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

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.dev.util.Util;
import com.google.gwt.thirdparty.debugging.sourcemap.SourceMapConsumerV3;
import com.google.gwt.thirdparty.debugging.sourcemap.SourceMapParseException;

import java.io.File;

/**
 * A mapping from Java lines to JavaScript.
 */
public class ReverseSourceMap {

    /**
     * Reads a source map from disk and parses it into an in-memory representation.
     * If it can't be loaded, logs a warning and returns an empty source map.
     */
    public static ReverseSourceMap load(TreeLogger logger, File sourceMapFile) {
        SourceMapConsumerV3 consumer = new SourceMapConsumerV3();
        String unparsed = Util.readFileAsString(sourceMapFile);
        try {
            consumer.parse(unparsed);
            return new ReverseSourceMap(consumer);
        } catch (SourceMapParseException e) {
            logger.log(TreeLogger.WARN, "can't parse source map", e);
            return new ReverseSourceMap(null);
        }
    }

    private final SourceMapConsumerV3 consumer;

    private ReverseSourceMap(SourceMapConsumerV3 consumer) {
        this.consumer = consumer;
    }

    /**
     * Returns true if the given line in a Java file has any corresponding JavaScript in
     * the GWT compiler's output. (The source file's path is relative to the source root directory
     * where the GWT compiler found it.)
     */
    public  boolean appearsInJavaScript(String path, int lineNumber) {
        // TODO: getReverseMapping() seems to be off by one (lines numbered from zero). Why?
        return (consumer != null) && !consumer.getReverseMapping (path, lineNumber - 1, -1).isEmpty ();
    }
}
