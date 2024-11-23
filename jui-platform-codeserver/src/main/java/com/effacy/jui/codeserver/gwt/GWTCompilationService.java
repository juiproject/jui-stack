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
package com.effacy.jui.codeserver.gwt;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;

import com.effacy.jui.codeserver.ICompilationService;
import com.effacy.jui.codeserver.ICompilationService.SourceMapDescriptor.Content;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.dev.MinimalRebuildCacheManager;
import com.google.gwt.dev.javac.UnitCache;
import com.google.gwt.dev.javac.UnitCacheSingleton;
import com.google.gwt.dev.util.DiskCachingUtil;
import com.google.gwt.thirdparty.guava.common.collect.ImmutableMap;

public class GWTCompilationService implements ICompilationService {

    private TreeLogger baseLogger;

    private ILogger logger;
    private JobRunner runner;
    private List<CompilerModule> modules;

    public static class CodeServerRunnerException extends Exception {
        public CodeServerRunnerException(String message) {
            super (message);
        }
    }

    /**
     * Construct an instance of the service.
     * 
     * @param logger
     *                   the logger to use.
     * @param options
     *                   the options to apply.
     * @param workingDir
     *                   the working directory to use.
     * @throws UnableToCompleteException on error.
     */
    public GWTCompilationService(TreeLogger logger, Options options, File workingDir) throws UnableToCompleteException {
        this.baseLogger = logger;
        this.logger = new ILogger () {
            public void info(String message) {
                baseLogger.log(TreeLogger.INFO, message);
            }

            public void warn(String message) {
                baseLogger.log(TreeLogger.WARN, message);
            }

            public void error(String message) {
                baseLogger.log(TreeLogger.ERROR, message);
            }
        };

        // Base cacge directory. This allows for use of pre-cached content.
        File baseCacheDir = DiskCachingUtil.computePreferredCacheDir (options.getModuleNames (), baseLogger);

        // GWT compiler.
        UnitCache unitCache = UnitCacheSingleton.get (baseLogger, null, baseCacheDir, new CompilerOptionsImpl (options));
        MinimalRebuildCacheManager minimalRebuildCacheManager = new MinimalRebuildCacheManager(logger, baseCacheDir, ImmutableMap.of (
            "sourceLevel", options.getSourceLevel().getStringValue(),
            "style", options.getOutput ().name (),
            "closureFormattedOutput", String.valueOf (options.isClosureFormattedOutput()),
            "generateJsInteropExports", String.valueOf (options.shouldGenerateJsInteropExports()),
            "exportFilters", options.getJsInteropExportFilter ().toString (),
            "methodDisplayMode", options.getMethodNameDisplayMode ().name ()));

        // Code server specific.
        baseLogger.log (Type.INFO, "Working in " + workingDir);
        baseLogger.log (Type.INFO, "Caching in " + baseCacheDir);
        this.modules = new ArrayList<>();
        for (String moduleName : options.getModuleNames ())
            this.modules.add (new CompilerModule (workingDir, unitCache, minimalRebuildCacheManager, options, baseLogger, moduleName));
        
        this.runner = new JobRunner (minimalRebuildCacheManager);
    }

    @Override
    public ILogger logger() {
        return logger;
    }

    @Override
    public ServiceDescriptor descriptor() {
        List<String> moduleNames = new ArrayList<> ();
        modules.forEach(module -> moduleNames.add (module.outputModuleName()));
        List<String> warnings = new ArrayList<> ();
        return new ServiceDescriptor(moduleNames, warnings);
    }

    @Override
    public IModule module(String moduleName) throws CodeServerRunnerException {
        Optional<CompilerModule> module = modules.stream ().filter (m -> m.outputModuleName ().equals (moduleName)).findFirst ();
        if (module.isEmpty())
            throw new CodeServerRunnerException ("No such module: " + module);
        return new Module (module.get());
    }
    
    /************************************************************************
     * General operations
     ************************************************************************/

    public CompilationStatus status() {
        JobEvent event = runner.getCompilingJobEvent ();
        if (event == null)
            return new CompilationStatus("idle", null, null, null, null);
        return new CompilationStatus(event.getStatus().jsonName, event.getJobId(), event.getMessage(), event.getInputModuleName(), event.getBindings());
    }

    

    class Module implements IModule {

        private CompilerModule box;

        Module(CompilerModule box) {
            this.box = box;
        }

        @Override
        public ModuleDescriptor descriptor() {
            List<FileRef> files = new ArrayList<> ();
            File[] fileList = new File (box.getWarDir(), box.outputModuleName ()).listFiles ();
            if (fileList != null) {
                Arrays.sort (fileList);
                for (File file : fileList) {
                    if (file.isFile ())
                        files.add (new FileRef(file.getName(), file.getName()));
                }
            }
            return new ModuleDescriptor(box.outputModuleName(), files, !box.containsStubCompile());
        }

        @Override
        public CompileLogDescriptor retrieveCompileLog() throws CodeServerRunnerException {
            if (box.containsStubCompile ())
                throw new CodeServerRunnerException ("This module hasn't been compiled yet.");
            File file = box.getCompileLog();
            if (!file.isFile ())
                throw new CodeServerRunnerException ("log file not found");
            return new CompileLogDescriptor (file);
        }

        @Override
        public void clean() throws CodeServerRunnerException {
            // We actually don't use the module, everything gets cleaned.
            try {
                runner.clean (baseLogger, modules);
            } catch (ExecutionException e) {
                throw new CodeServerRunnerException (e.getMessage ());
            }
        }

        @Override
        public RecompileOutcome recompile(Map<String, String> properties) throws CodeServerRunnerException {
            Job job = box.makeJob (properties, baseLogger);
            runner.submit (job);
            Job.Result result = job.waitForResult();
            List<String> moduleNames = new ArrayList<> ();
            modules.forEach (module -> moduleNames.add (module.outputModuleName ()));
            return new RecompileOutcome (moduleNames, result.isOk());
        }

        @Override
        public String recompileRequester() throws CodeServerRunnerException {
            try {
                return runner.getRecompileJs (baseLogger, box);
            } catch (ExecutionException e) {
                // Already logged.
                throw new CodeServerRunnerException ("Failed to generate the Js recompile requester.");
            }
        }

        @Override
        public SourceMapDescriptor retrieveSourceMap(String uri) throws CodeServerRunnerException {
            if (box.containsStubCompile())
                throw new CodeServerRunnerException ("This module hasn't been compiled yet.");

            String rootDir = Constants.SOURCEMAP_PATH + box.outputModuleName () + "/";

            // Sources directly list (top-level).
            if (uri.isEmpty()) {
                SourceMap map = SourceMap.load (box.findSourceMapForOnePermutation());
                List<FileRef> directories = new ArrayList<>();
                for (String name : map.getSourceDirectories())
                    directories.add (new FileRef (name, name + "/"));
                return new SourceMapDescriptor(SourceMapDescriptor.Type.DIRECTORY_LIST, new SourceMapDescriptor.Content (directories), null, box.outputModuleName ());
            }

            // File list.
            if (uri.endsWith("/")) {
                SourceMap map = SourceMap.load (box.findSourceMapForOnePermutation ());
                List<FileRef> files = new ArrayList<>();
                for (String name : map.getSourceFilesInDirectory (uri))
                    files.add (new FileRef (name, name + "?html"));
                return new SourceMapDescriptor(SourceMapDescriptor.Type.FILE_LIST, new SourceMapDescriptor.Content (files), uri, box.outputModuleName ());
            } 

            // JAVA source file.
            if (uri.endsWith(".java")) {
                try {
                    InputStream pageBytes = box.openSourceFile (uri);
                    if (pageBytes == null)
                        throw new CodeServerRunnerException ("unknown source file: " + rootDir + uri);
                    return new SourceMapDescriptor(SourceMapDescriptor.Type.SOURCE, new Content (pageBytes, ReverseSourceMap.load (baseLogger, box.findSourceMapForOnePermutation())), uri, box.outputModuleName ());
                } catch (IOException e) {
                    baseLogger.branch (TreeLogger.ERROR, "Unabled to open " + rootDir + uri, e);
                    throw new CodeServerRunnerException ("Unable to open resource: " + rootDir + uri);
                }
            }
            
            // Possible source map file.
            Matcher matcher = Constants.SOURCEMAP_FILENAME_PATTERN.matcher (uri);
            String strongName =  matcher.matches() ? matcher.group(1) : null;
            if (strongName != null) {
                File file = box.findSourceMap (strongName).getAbsoluteFile ();
                return new SourceMapDescriptor(SourceMapDescriptor.Type.MAP, new Content (file), null, box.outputModuleName ());
            }

            throw new CodeServerRunnerException("page not found");
        }

        public SymbolMapDescriptor retrieveSymbolMap (String uri) throws CodeServerRunnerException {
            if (box.containsStubCompile())
                throw new CodeServerRunnerException("This module hasn't been compiled yet.");

            if (uri.isEmpty())
                throw new CodeServerRunnerException("Missing permutation id");
            if (uri.endsWith("/"))
                throw new CodeServerRunnerException("Can not list directory");

            Matcher matcher = Constants.SYMBOLMAP_FILENAME_PATTERN.matcher(uri);
            String strongName =  matcher.matches () ? matcher.group (1) : null;
            if (strongName == null)
                throw new CodeServerRunnerException("page not found");

            File symbolMap = box.findSymbolMap (strongName).getAbsoluteFile ();
            if (!symbolMap.isFile())
                throw new CodeServerRunnerException ("file not found: " + symbolMap.toString ());

            return new SymbolMapDescriptor(symbolMap);
        }

        @Override
        public ArtefactDescriptor retrieveArtefact(String uri) throws CodeServerRunnerException {
            String sourceMapUri = null;
            Matcher match = Constants.CACHE_JS_FILE.matcher (uri);
            if (match.find ()) {
                String strongName = match.group (1);
                sourceMapUri = Constants.SOURCEMAP_PATH + box.outputModuleName () + "/" + strongName + Constants.SOURCEMAP_URL_SUFFIX;
            }

            uri = "/" + box.outputModuleName () + "/" + uri;
            boolean gzipped = false;
            File file = box.getOutputFile (uri);
            if (!file.isFile ()) {
                file = box.getOutputFile (uri + ".gz");
                if (!file.isFile ())
                    throw new CodeServerRunnerException ("not found: " + file.toString ());
                gzipped = true;
            }

            return new ArtefactDescriptor (file, gzipped, box.outputModuleName (), sourceMapUri);
        }
    }
}
