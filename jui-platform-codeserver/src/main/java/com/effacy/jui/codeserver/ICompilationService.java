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
package com.effacy.jui.codeserver;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.effacy.jui.codeserver.gwt.GWTCompilationService.CodeServerRunnerException;
import com.effacy.jui.codeserver.gwt.ReverseSourceMap;

public interface ICompilationService {

    /**
     * Descriptor of the service.
     * 
     * @return the status.
     */
    public ServiceDescriptor descriptor();

    /**
     * Status of any any currently running (or last run) compilation.
     * 
     * @return the status.
     */
    public CompilationStatus status();

    /**
     * Obtains information about a module.
     */
    public IModule module(String module) throws CodeServerRunnerException;


    /************************************************************************
     * Logging.
     ************************************************************************/

     /**
      * Logger service.
      * 
      * @return the logger.
      */
    public ILogger logger();

    public interface ILogger {

        public void info(String message);

        public void warn(String message);

        public void error(String message);
    }

    /************************************************************************
     * Support classes.
     ************************************************************************/

    public interface IModule {

        /**
         * Obtains a descriptor for the module.
         * 
         * @return the outcome.
         */
        public ModuleDescriptor descriptor();

        /**
         * Cleans the cache for the given module.
         */
        public void clean() throws CodeServerRunnerException;

        /**
         * Triggers a recompilation.
         * 
         * @param properties
         *                   binding properties to pass to the compiler (these come from
         *                   the client).
         * @return the outcome.
         * @throws CodeServerRunnerException on error.
         */
        public RecompileOutcome recompile(Map<String, String> properties) throws CodeServerRunnerException;

        /**
         * TODO: What exactly is this?
         * @return the JavaScript to pass back.
         * @throws CodeServerRunnerException on error.
         */
        public String recompileRequester() throws CodeServerRunnerException;

        /**
         * Retrieve the most recent compilation log.
         * 
         * @return the associated log.
         * @throws CodeServerRunnerException on error (i.e. module not found).
         */
        public CompileLogDescriptor retrieveCompileLog() throws CodeServerRunnerException;

        /**
         * Obtains a source map for the given URI (this includes supporting structures
         * for sources such as directory and file lists).
         * 
         * @param uri
         *            the URI to retrieve.
         * @return the associated content in the most relevant format.
         * @throws CodeServerRunnerException on error (i.e. artefact not found).
         */
        public SourceMapDescriptor retrieveSourceMap(String uri) throws CodeServerRunnerException;

        /**
         * Obtains a symbol map for the given URI.
         * 
         * @param uri
         *            the URI to retrieve.
         * @return the associated content in the most relevant format.
         * @throws CodeServerRunnerException on error (i.e. artefact not found).
         */
        public SymbolMapDescriptor retrieveSymbolMap(String uri) throws CodeServerRunnerException;

        /**
         * Retrieves the compiler artefact from the specified URI. This will be of the
         * form <code>/{artefact}</code>.
         * 
         * @param uri
         *            the URI of the artefact.
         * @return a file referencing the artefacts content.
         * @throws CodeServerRunnerException on error (i.e. artefact not found).
         */
        public ArtefactDescriptor retrieveArtefact(String uri) throws CodeServerRunnerException;
    }

    /************************************************************************
     * Return types.
     ************************************************************************/

    /**
     * Represent a file artefact.
     */
    public record FileRef(String name, String link) {}

    /**
     * Captures the status of the service.
     */
    public record ServiceDescriptor(List<String> moduleNames, List<String> warnings) {}
    
    /**
     * A reference to a compiler artefact.
     */
    public record ArtefactDescriptor(File file, boolean gzipped, String module, String sourceMapUri) {}

    /**
     * Captures the progress of a compilation.
     */
    public record CompilationStatus(String status, String jobId, String message, String inputModule, Map<String,String> bindings) {}

    /**
     * Captures the outcome of a recompilation.
     */
    public record RecompileOutcome(List<String> moduleNames, boolean success) {}

    /**
     * Captues the content of a module.
     */
    public record ModuleDescriptor(String moduleName, List<FileRef> files, boolean compiled) {}

    /**
     * Captures structure of source maps.
     * 
     * @param type
     *                the type of resource being represented.
     * @param content
     *                access to the underlying content of the resource (type
     *                dependent).
     * @param path
     *                the path to the resourced.
     * @param module
     *                the module (for reference and for building URL's).
     */
    public record SourceMapDescriptor(SourceMapDescriptor.Type type, SourceMapDescriptor.Content content, String path, String module) {

        public enum Type {
            /**
             * A JSON source map file.
             * <p>
             * The {@link Content} will have the file in the file property (TODO: consider
             * supporting both file and stream).
             */
            MAP,
            
            /**
             * A JAVA source file.
             * <p>
             * The {@link Content} will have the file in the stream property (TODO: consider
             * supporting both file and stream).
             */
            SOURCE,
            
            /**
             * A list of directories.
             * <p>
             * The {@link Content} will the directors in the files property.
             */
            DIRECTORY_LIST,
            
            /**
             * A list of files.
             * <p>
             * The {@link Content} will the files in the files property.
             */
            FILE_LIST;
        }

        public record Content(List<FileRef> files, InputStream stream, File file, ReverseSourceMap sourceMap) {
            public Content(List<FileRef> files) {
                this (files, null, null, null);
            }
            public Content(InputStream stream, ReverseSourceMap sourceMap) {
                this (null, stream, null, sourceMap);
            }
            public Content(File file) {
                this (null, null, file, null);
            }
        }
    }

    public record SymbolMapDescriptor(File file) {}

    public record CompileLogDescriptor(File file) {}
}
