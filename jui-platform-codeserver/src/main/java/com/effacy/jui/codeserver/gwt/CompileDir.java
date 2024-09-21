/*
 * Copyright 2011 Google Inc.
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

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.thirdparty.guava.common.collect.ImmutableList;

/**
 * Defines a directory tree used for compiling a GWT app one time. Each time we recompile
 * the app, we will create a new, empty CompileDir. This way, a failed compile doesn't
 * modify the last good compile.
 *
 * <p>The CompileDir gets created within the appropriate {@link CompilerModuleDir} for the app
 * being compiled.
 */
public class CompileDir {

    private final File dir;

    /**
     * @see #create
     */
    public CompileDir(File dir) {
        this.dir = dir;
    }

    /**
     * Top-level directory, containing the others.
     */
    public File getRoot() {
        return dir;
    }

    /**
     * The directory tree where the compiler saves output files that should be available
     * via HTTP. It will be in a subdirectory based on the module name, possibly after
     * renaming. Files will be accessible via HTTP until a new compile finishes successfully.
     */
    public File getWarDir() {
        return new File(dir, "war");
    }

    public File getDeployDir() {
        return new File(getWarDir(), "WEB-INF/deploy");
    }

    /**
     * The directory where the compiler saves auxiliary files that shouldn't be available via HTTP.
     */
    public File getExtraDir() {
        return new File(dir, "extras");
    }

    /**
     * The directory tree where the compiler saves source code created by GWT generators.
     * The source is also accessible via HTTP, for use by browser debuggers.
     */
    public File getGenDir() {
        return new File(dir, "gen");
    }

    public File getWorkDir() {
        return new File(dir, "work");
    }

    /**
     * The file where the GWT compiler writes compile errors and warnings.
     * Also accessible via HTTP.
     */
    public File getLogFile() {
        return new File(dir, "compile.log");
    }

    /**
     * Given the outputModuleName from the compiler, returns all the sourcemap
     * files generated, or null if the directory couldn't  be listed.
     */
    public List<File> findSourceMapFiles(String outputModuleName) {
        File symbolMapsDir = findSymbolMapDir(outputModuleName);
        if (symbolMapsDir == null) {
            return null;
        }
        File mapDir = symbolMapsDir.getAbsoluteFile();

        File[] files = mapDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(CompilerModule.SOURCEMAP_FILE_SUFFIX);
            }
        });

        return files == null ? null : ImmutableList.copyOf(files);
    }

    /**
     * Given the outputModuleName from the compiler, returns the location of
     * the symbol maps directory, or null if not available.
     */
    File findSymbolMapDir(String outputModuleName) {
        // The JUnit module moves the symbolMaps directory in a post linker.
        // TODO(skybrian) query this information from the compiler somehow?
        File[] candidates = {
            new File(getExtraDir(), outputModuleName + "/symbolMaps"),
            new File(getWarDir(), outputModuleName + "/.junit_symbolMaps")
        };

        for (File candidate : candidates) {
            if (candidate.isDirectory()) {
                return candidate;
            }
        }

        return null;
    }

    /**
     * Creates a new compileDir directory structure. The directory must not already exist,
     * but its parent should exist.
     * @throws UnableToCompleteException if unable to create the directory
     */
    static CompileDir create(File dir, TreeLogger logger)
        throws UnableToCompleteException {

        CompileDir result = new CompileDir(dir);

        mkdir(dir, logger);
        mkdir(result.getWarDir(), logger);

        return result;
    }

    private static void mkdir(File dirToCreate, TreeLogger logger) throws UnableToCompleteException {
        if (!dirToCreate.mkdir()) {
            logger.log(TreeLogger.Type.ERROR, "unable to create directory: " + dirToCreate);
            throw new UnableToCompleteException();
        }
    }
}
