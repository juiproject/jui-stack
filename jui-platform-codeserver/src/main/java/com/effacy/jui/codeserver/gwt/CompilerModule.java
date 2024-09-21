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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.effacy.jui.codeserver.gwt.Job.Result;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.dev.MinimalRebuildCacheManager;
import com.google.gwt.dev.javac.UnitCache;

/**
 * Holds the compiler output for one module.
 * TODO(skybrian) there will later be a separate Outbox for each set of binding properties.
 */
public class CompilerModule {

    /**
     * The suffix that the GWT compiler uses when writing a sourcemap file.
     */
    static final String SOURCEMAP_FILE_SUFFIX = "_sourceMap0.json";

    private final Recompiler recompiler;
    private final Options options;

    private final AtomicReference<Result> published = new AtomicReference<Result>();
    private Job publishedJob; // may be null if the Result wasn't created by a Job.

    public CompilerModule(File workingDir, UnitCache unitCache, MinimalRebuildCacheManager minimalRebuildCacheManager, Options options, TreeLogger logger, String moduleName) throws UnableToCompleteException {
        try {
            CompilerModuleDir moduleDir = CompilerModuleDir.create (new File (workingDir, moduleName), logger);
            this.recompiler = new Recompiler (moduleDir, moduleName, options, unitCache, minimalRebuildCacheManager);
            this.options = options;

            // If we are not pre-compiling then we initialise appropriately and finish up.
            if (options.getNoPrecompile()) {
                publish (recompiler.initWithoutPrecompile (logger), null);
                return;
            }

            // We are pre-compiling so we need to run a dummy job (not externally visible
            // but will be logged).
            Map<String, String> defaultProps = new HashMap<String, String> ();
            defaultProps.put ("user.agent", "safari");
            defaultProps.put ("locale", "en");
            Job job = makeJob (defaultProps, logger);
            job.onSubmitted (new JobEventTable ());
            publish (recompiler.precompile (job), job);

            // If we are just running a test compilation then we respond to any errors.
            if (options.isCompileTest()) {
                Throwable error = job.getListenerFailure ();
                if (error != null) {
                    UnableToCompleteException e = new UnableToCompleteException ();
                    e.initCause (error);
                    throw e;
                }
            }
        } catch (UnableToCompleteException e) {
            throw e;
        } catch ( IOException e) {
            logger.branch (TreeLogger.ERROR, "problem creating output directory", e);
            UnableToCompleteException ex = new UnableToCompleteException ();
            ex.initCause (ex);
            throw ex;
        }
    }

    /**
     * Forces the next recompile even if no input files have changed.
     */
    public void forceNextRecompile() {
        recompiler.forceNextRecompile ();
    }

    /**
     * Creates a Job whose output will be saved in this outbox.
     */
    public Job makeJob(Map<String, String> bindingProperties, TreeLogger parentLogger) {
        return new Job(this, bindingProperties, parentLogger, options);
    }

    /**
     * Compiles the module again, possibly changing the output directory.
     * After returning, the result of the compile can be found via {@link Job#waitForResult}
     */
    public void recompile(Job job) {
        if (!job.wasSubmitted() || job.isDone()) {
            throw new IllegalStateException("tried to recompile using a job in the wrong state:"  + job.getId());
        }

        Result result = recompiler.recompile(job);

        if (result.isOk()) {
            publish(result, job);
        } else {
            job.getLogger().log(TreeLogger.Type.WARN, "continuing to serve previous version");
        }
    }

    /**
     * Makes the result of a compile downloadable via HTTP.
     * @param job the job that created this result, or null if none.
     */
    private synchronized void publish(Result result, Job job) {
        if (publishedJob != null) {
            publishedJob.onGone();
        }
        publishedJob = job;
        published.set(result);
    }

    /**
     * Returns true if we haven't done a real compile yet, so the Outbox contains
     * a stub that will automatically start a compile.
     */
    public synchronized boolean containsStubCompile() {
        return publishedJob == null;
    }

    /**
     * Returns the module name that will be sent to the compiler (before renaming).
     */
    public String inputModuleName() {
        return recompiler.getInputModuleName();
    }

    /**
     * The (output) module name.
     * 
     * @return the name.
     */
    public String outputModuleName() {
        return recompiler.getOutputModuleName();
    }

    /**
     * Returns the source map file from the most recent recompile,
     * assuming there is one permutation.
     *
     * @throws RuntimeException if unable
     */
    public File findSourceMapForOnePermutation() {
        String moduleName = recompiler.getOutputModuleName ();

        List<File> sourceMapFiles = getOutputDir ().findSourceMapFiles (moduleName);
        if (sourceMapFiles == null)
            throw new RuntimeException ("Can't find sourcemap files.");
        if (sourceMapFiles.size () > 1)
            throw new RuntimeException ("Multiple fragment 0 sourcemaps found. Too many permutations.");
        if (sourceMapFiles.isEmpty ())
            throw new RuntimeException ("No sourcemaps found. Not enabled?");

        return sourceMapFiles.get (0);
    }

    /**
     * Returns the source map file given a strong name.
     *
     * @throws RuntimeException if unable
     */
    public File findSourceMap(String strongName) {
        File dir = findSymbolMapDir();
        File file = new File(dir, strongName + SOURCEMAP_FILE_SUFFIX);
        if (!file.isFile()) {
            throw new RuntimeException("Sourcemap file doesn't exist for " + strongName);
        }
        return file;
    }

    /**
     * Returns the symbol map file given a strong name.
     *
     * @throws RuntimeException if unable
     */
    public File findSymbolMap(String strongName) {
        File dir = findSymbolMapDir();
        File file = new File(dir, strongName + ".symbolMap");
        if (!file.isFile()) {
            throw new RuntimeException("Symbolmap file doesn't exist for " + strongName);
        }
        return file;
    }

    /**
     * Returns the symbols map folder for this modulename.
     * 
     * @throws RuntimeException if unable
     */
    private File findSymbolMapDir() {
        String moduleName = recompiler.getOutputModuleName();
        File symbolMapsDir = getOutputDir().findSymbolMapDir(moduleName);
        if (symbolMapsDir == null) {
            throw new RuntimeException("Can't find symbol map directory for " + moduleName);
        }
        return symbolMapsDir;
    }

    /**
     * Finds a source file (or other resource) that's either in this module's source
     * path, or is a generated file.
     * 
     * @param path
     *             location of the file relative to its directory in the classpath,
     *             or (if it starts with "gen/"), a generated file.
     * @return bytes in the file, or null if there's no such source file.
     */
    public InputStream openSourceFile(String path) throws IOException {

        // Check for the generated case. These sources are generated by the compiler
        // during the rebinding process and are written to the compilation directory
        // under `gen`.
        if (path.startsWith ("gen/")) {
            String rest = path.substring ("gen/".length ());
            File fileInGenDir = new File (getGenDir (), rest);
            if (!fileInGenDir.isFile())
                return null;
            return new BufferedInputStream(new FileInputStream (fileInGenDir));
        }

        // Regular source files are retrieved from the classpath. This is OK since we
        // are a code server so will have the file on said classpath as we would have
        // needed it for compilation.
        URL resource = recompiler.getResourceLoader ().getResource (path);
        if (resource == null)
            return null;
        return resource.openStream ();
    }

    /**
     * Returns the location of a file in the compiler's output directory from the
     * last time this module was recompiled. The location will change after a
     * successful
     * recompile.
     * 
     * @param urlPath
     *                the path to the file. This should be a relative path beginning
     *                with the module name (after renaming).
     * @return the location of the file, which might not actually exist.
     */
    public  File getOutputFile(String urlPath) {
        return new File (getOutputDir().getWarDir(), urlPath);
    }

    /**
     * Returns the log file from the last time this module was recompiled. This changes
     * after each compile.
     * 
     * @return the file.
     */
    public File getCompileLog() {
        return recompiler.getLastLog();
    }

    /**
     * The directory that contains the generated sources file (arising from rebinding).
     * 
     * @return the location.
     */
    public File getGenDir() {
        return getOutputDir().getGenDir();
    }

    /**
     * The directory that contains the assets generated during the last compilation
     * as they would appear relative to the root of the WAR.
     * 
     * @return the location.
     */
    public File getWarDir() {
        return getOutputDir().getWarDir();
    }

    /**
     * Return fresh JS that knows how to request the specific permutation recompile.
     */
    public String getRecompileJs(TreeLogger logger) throws UnableToCompleteException {
        return recompiler.getRecompileJs(logger);
    }

    private CompileDir getOutputDir() {
        return published.get().outputDir;
    }
}
