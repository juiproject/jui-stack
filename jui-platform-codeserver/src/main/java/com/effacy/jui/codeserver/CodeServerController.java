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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.View;

import com.effacy.jui.codeserver.ICompilationService.ArtefactDescriptor;
import com.effacy.jui.codeserver.ICompilationService.CompilationStatus;
import com.effacy.jui.codeserver.ICompilationService.CompileLogDescriptor;
import com.effacy.jui.codeserver.ICompilationService.FileRef;
import com.effacy.jui.codeserver.ICompilationService.IModule;
import com.effacy.jui.codeserver.ICompilationService.ModuleDescriptor;
import com.effacy.jui.codeserver.ICompilationService.RecompileOutcome;
import com.effacy.jui.codeserver.ICompilationService.ServiceDescriptor;
import com.effacy.jui.codeserver.ICompilationService.SourceMapDescriptor;
import com.effacy.jui.codeserver.ICompilationService.SymbolMapDescriptor;
import com.effacy.jui.codeserver.gwt.GWTCompilationService.CodeServerRunnerException;
import com.effacy.jui.codeserver.view.BinaryViewBuilder;
import com.effacy.jui.codeserver.view.ErrorViewBuilder;
import com.effacy.jui.codeserver.view.HtmlSourceViewBuilder;
import com.effacy.jui.codeserver.view.HtmlViewBuilder;
import com.effacy.jui.codeserver.view.JsonViewBuilder;
import com.effacy.jui.codeserver.view.ViewBuilderSupport;
import com.google.gwt.dev.json.JsonArray;
import com.google.gwt.dev.json.JsonObject;
import com.google.gwt.thirdparty.guava.common.base.Charsets;
import com.google.gwt.thirdparty.guava.common.io.Files;
import com.google.gwt.thirdparty.guava.common.io.Resources;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class CodeServerController {

    @Autowired
    private ICompilationService compiler;

    static final Pattern SAFE_DIRECTORY = Pattern.compile ("([a-zA-Z0-9_-]+\\.)*[a-zA-Z0-9_-]+");

    static final Pattern SAFE_FILENAME = Pattern.compile ("([a-zA-Z0-9_-]+\\.)+[a-zA-Z0-9_-]+");
  
    static final Pattern SAFE_MODULE_PATH = Pattern.compile ("/(" + SAFE_DIRECTORY + ")/$");
  
    static final Pattern SAFE_DIRECTORY_PATH = Pattern.compile ("/(" + SAFE_DIRECTORY + "/)+$");
  
    static final Pattern SAFE_FILE_PATH = Pattern.compile ("/(" + SAFE_DIRECTORY + "/)+" + SAFE_FILENAME + "$");

    static final String TIME_IN_THE_PAST = "Mon, 01 Jan 1990 00:00:00 GMT";

    static final Pattern ERROR_PATTERN = Pattern.compile ("\\[ERROR\\]");

    /**
     * The front page (displays the setup instructions).
     */
    @GetMapping("/")
    public View frontPage() {
        ServiceDescriptor status = compiler.descriptor();
        JsonObject json = JsonObject.create();
        JsonArray moduleNames = new JsonArray();
        for (String moduleName : status.moduleNames ())
            moduleNames.add (moduleName);
        json.put ("moduleNames", moduleNames);
        return ViewBuilderSupport.build (new HtmlViewBuilder ("config", json, CodeServerController.class.getResource ("frontpage.html")));
    }

    /**
     * Retrieves the favicon.
     */
    @GetMapping("/favicon.ico")
    public View favicon() {
        InputStream faviconStream = getClass().getResourceAsStream("favicon.ico");
        if (faviconStream == null)
            return ViewBuilderSupport.build (new ErrorViewBuilder ("icon not found"));
        return ViewBuilderSupport.build (new BinaryViewBuilder ("image/x-icon", faviconStream));
    }

    /**
     * Retrieves the logo.
     */
    @GetMapping("/logo.png")
    public View logo() {
        InputStream faviconStream = getClass().getResourceAsStream("logo.png");
        if (faviconStream == null)
            return ViewBuilderSupport.build (new ErrorViewBuilder ("image not found"));
        return ViewBuilderSupport.build (new BinaryViewBuilder ("image/png", faviconStream));
    }

    /**
     * Retrieve the compliation log for the given module.
     * 
     * @param module
     *               the module name.
     */
    @GetMapping("/log/{module}")
    public View log(@PathVariable("module") String moduleName) {    
        try {
            CompileLogDescriptor descriptor = compiler.module (moduleName).retrieveCompileLog ();
            return ViewBuilderSupport.build ((request,response) -> {
                BufferedReader reader = new BufferedReader(new FileReader(descriptor.file ()));
                response.setStatus (HttpServletResponse.SC_OK);
                response.setContentType ("text/html");
                response.setHeader ("Content-Style-Type", "text/css");
                PrintWriter pw = response.getWriter ();
                pw.println ("""
<html>
    <head>
        <title>""" + StringEscapeUtils.escapeHtml4 (moduleName + " compile log") + " (JUI Code Server)</title>" + """
        <style>
            .error { color: red; font-weight: bold; }
        </style>
    </head>
    <body>
        <pre>""");
                try {
                    String line = reader.readLine ();
                    while (line != null) {  
                        if (ERROR_PATTERN.matcher (line).find ()) {
                            pw.print ("<span class=\"error\">");
                            pw.print (StringEscapeUtils.escapeHtml4 (line));
                            pw.println ("</span>");
                        } else
                            pw.println (StringEscapeUtils.escapeHtml4 (line));
                        line = reader.readLine ();
                    }
                } finally {
                    reader.close ();
                }
                pw.println ("""
        </pre>
    </body>
</html>""");
            });
        } catch (CodeServerRunnerException e) {
            return ViewBuilderSupport.build (new ErrorViewBuilder (e.getMessage()));
        }
    }

    /**
     * Clears the cache for the passed module.
     * 
     * @param module
     *               the module to clean the cache for.
     */
    @GetMapping("/clean/{module}")
    public View clean(@PathVariable(value="module") String moduleName) {
        try {
            compiler.module (moduleName).clean ();
            return ViewBuilderSupport.build (new JsonViewBuilder (() -> {
                JsonObject obj = new JsonObject();
                obj.put ("status", "ok");
                obj.put ("message", "Cleaned disk caches.");
                return obj;
            }));
        } catch (CodeServerRunnerException e) {
            return ViewBuilderSupport.build (new JsonViewBuilder (() -> {
                JsonObject obj = new JsonObject();
                obj.put ("status", "error");
                obj.put ("message", e.getMessage());
                return obj;
            }));
        }
    }

    /**
     * Retrieve the JS to run in SCRIPT element that presents the compilation UI
     * (which handles the triggering of a compile, tracking its progress and
     * handling and error state). This is initiated by the "Dev Mode On" bookmark.
     */
    @GetMapping("/dev_mode_on.js")
    public View devmode() {
        URL resource = CodeServerController.class.getResource ("dev_mode_on.js");
        if (resource == null)
            return ViewBuilderSupport.build (new ErrorViewBuilder ("Unable to find dev_mode_on.js"));
        ServiceDescriptor status = compiler.descriptor ();
        JsonObject json = JsonObject.create ();
        JsonArray moduleNames = new JsonArray ();
        for (String moduleName : status.moduleNames ())
            moduleNames.add (moduleName);
        json.put ("moduleNames", moduleNames);
        JsonArray warnings = new JsonArray();
        for (String warning : status.warnings ())
            warnings.add (warning);
        json.put ("warnings", warnings);
        return ViewBuilderSupport.build ((request, response) -> {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType ("application/javascript");
            ServletOutputStream outBytes = response.getOutputStream();
            Writer out = new OutputStreamWriter (outBytes, "UTF-8");
            out.append ("window." + "__gwt_codeserver_config" + " = ");
            json.write (out);
            out.append (";\n");
            out.flush ();
            Resources.copy (resource, outBytes);
        });
    }

    /**
     * Obtains the current progress status as a JSON object.
     */
    @GetMapping("/progress")
    public View progress() {
        CompilationStatus result = compiler.status ();
        return ViewBuilderSupport.build (new JsonViewBuilder (() -> {
            JsonObject obj = new JsonObject ();
            obj.put ("status", result.status());
            if (result.jobId() != null)
                obj.put ("jobId", result.jobId());
            if (result.message() != null)
                obj.put ("message", result.message());
            if (result.inputModule() != null)
                obj.put ("inputModule", result.inputModule());
            if (result.bindings() != null) {
                JsonObject bindings = new JsonObject();
                for (String name : result.bindings().keySet())
                    bindings.put(name, result.bindings().get(name));
                obj.put ("bindings", bindings);
            }
            return obj;
        }));
    }

    /**
     * Performs a recompilation of the given module and returns a status (as a JSON)
     * object.
     * 
     * @param module
     *                the module name to recompile.
     * @param request
     *                requests data to extract parameters from.
     */
    @GetMapping("/recompile/{module}")
    public View recompile(@PathVariable(value="module") String moduleName, @RequestParam(value = "clean", required=false) String clean, HttpServletRequest request) {
        try {
            IModule module = compiler.module (moduleName);
            if ((clean != null) && "true".equals ("true"))
                module.clean ();
            Map<String, String> properties = new HashMap<> ();
            request.getParameterMap ().keySet ().forEach (key -> {
                if (!"_callback".equals (key))
                    properties.put (key, request.getParameter (key));
            });
            RecompileOutcome result = module.recompile (properties);
            return ViewBuilderSupport.build (new JsonViewBuilder (() -> {
                JsonObject obj = new JsonObject ();
                JsonArray moduleNames = new JsonArray();
                for (String name : result.moduleNames ())
                    moduleNames.add (name);
                obj.put("moduleNames", moduleNames);
                obj.put("status", result.success () ? "ok" : "failed");
                return obj;
            }));
        } catch (CodeServerRunnerException e) {
            return ViewBuilderSupport.build (new ErrorViewBuilder (e.getMessage()));
        }
    }

    /**
     * Used to generate some JS that invoked a recompilation.
     * 
     * @param module
     *               the module.
     */
    @GetMapping("/recompile-requester/{module}")
    public View recompileRequester(@PathVariable(value="module") String moduleName) {
        try {
            IModule module = compiler.module (moduleName);
            String jsScript = module.recompileRequester ();
            return ViewBuilderSupport.build ((request,response) -> {
                response.setStatus (HttpServletResponse.SC_OK);
                response.setContentType ("application/javascript");
                ServletOutputStream outBytes = response.getOutputStream ();
                Writer out = new OutputStreamWriter (outBytes, "UTF-8");
                out.write(jsScript);
                out.close();
            });
        } catch (CodeServerRunnerException e) {
            return ViewBuilderSupport.build (new ErrorViewBuilder (e.getMessage ()));
        }
    }

    /**
     * Deliver the information page for the given module.
     */
    @GetMapping("/{module}/")
    public View module(@PathVariable("module") String moduleName, HttpServletRequest request) throws IOException {
        String url = request.getRequestURI ().toString ();
        try {
            if (!SAFE_MODULE_PATH.matcher(url).find())
                throw new CodeServerRunnerException ("");
            IModule module = compiler.module (moduleName);
            ModuleDescriptor status = module.descriptor ();
            JsonObject json = new JsonObject ();
            json.put ("moduleName", status.moduleName ());
            JsonArray files = new JsonArray ();
            status.files ().forEach (file -> {
                JsonObject map = new JsonObject ();
                map.put ("name", file.name());
                map.put ("link", file.link());
                files.add (map);
            });
            json.put ("files", files);
            json.put ("isCompiled", status.compiled());
            return ViewBuilderSupport.build (new HtmlViewBuilder("config", json, CodeServerController.class.getResource ("modulepage.html")));
        } catch (CodeServerRunnerException ex) {
            compiler.logger ().warn ("Unable to serve: " + url);
            return ViewBuilderSupport.build (new ErrorViewBuilder (ex.getMessage ()));
        }
    }

    /**
     * Delivers requested sourcemap.
     */
    @GetMapping({ "/sourcemaps/{module}/", "/sourcemaps/{module}/*/**" })
    public View sourcemaps(@PathVariable("module") String moduleName, HttpServletRequest request) throws IOException {
        String url = request.getRequestURI ().toString ();
        compiler.logger ().info ("sourcemaps: " + url);

        // Validate the URL is safe.
        if (!SAFE_DIRECTORY_PATH.matcher(url).matches() && !SAFE_FILE_PATH.matcher(url).matches()) {
            compiler.logger ().warn ("Unable to serve: " + url);
            return ViewBuilderSupport.build (new ErrorViewBuilder ("Unable to serve the resource: " + url));
        }

        // Request to retrieve compiler and linker output artefacts (including source
        // maps).
        try {
            IModule module = compiler.module (moduleName);
            // We need to remove the prefix (/sourcemaps/{module}/).
            url = url.substring (url.indexOf ('/', 12) + 1);
            String query = request.getQueryString ();
            boolean html = ((query != null) && query.equals ("html"));
            return location_sourcemap (module, url, html);
        } catch (CodeServerRunnerException ex) {
            compiler.logger ().warn ("Unable to serve: " + url);
            return ViewBuilderSupport.build (new ErrorViewBuilder (ex.getMessage ()));
        }
    }

    /**
     * Delivers requested symbolmap.
     * <p>
     * The origin of this seems to be related to older versions of the GWT code
     * server and the ability to resolve stack traces. It does not appear to be
     * used.
     */
    @GetMapping("/symbolmaps/{module}/*/**")
    public View symbolmaps(@PathVariable("module") String moduleName, HttpServletRequest request) throws IOException {
        String url = request.getRequestURI ().toString ();
        compiler.logger ().info ("symbolmaps: " + url);

        // Validate the URL is safe.
        if (!SAFE_FILE_PATH.matcher (url).matches ()) {
            compiler.logger ().warn ("Unable to serve: " + url);
            return ViewBuilderSupport.build (new ErrorViewBuilder ("Unable to serve the resource: " + url));
        }
        
        try {
            // We need to remove the prefix (/symbolmaps/{module}/).
            url = url.substring (url.indexOf ('/', 12) + 1);
            return location_symbolmap (compiler.module (moduleName), url);
        } catch (CodeServerRunnerException ex) {
            compiler.logger ().warn ("Unable to serve: " + url);
            return ViewBuilderSupport.build (new ErrorViewBuilder (ex.getMessage ()));
        }
    }

    /**
     * The default handler used to serve up javascript and source maps.
     */
    @GetMapping("/{module}/*/**")
    public View asset(@PathVariable("module") String moduleName, HttpServletRequest request) throws IOException {
        String url = request.getRequestURI ().toString ();
        compiler.logger ().info ("location: " + url);

        // Validate the URL is safe.
        if (!SAFE_FILE_PATH.matcher (url).matches ()) {
            compiler.logger ().warn ("Unable to serve: " + url);
            return ViewBuilderSupport.build (new ErrorViewBuilder ("Unable to serve the resource: " + url));
        }
            
        try {
            // We need to remove the prefix (/{module}/).
            url = url.substring (url.indexOf ('/', 1) + 1);
            return location_artefact (compiler.module (moduleName), url);
        } catch (CodeServerRunnerException ex) {
            compiler.logger ().warn ("Unable to serve: " + url);
            return ViewBuilderSupport.build (new ErrorViewBuilder (ex.getMessage ()));
        }
    }

    protected View location_sourcemap(IModule module, String uri, boolean html) throws CodeServerRunnerException{
        SourceMapDescriptor artefact = module.retrieveSourceMap (uri);
        if (SourceMapDescriptor.Type.DIRECTORY_LIST == artefact.type()) {
            JsonObject json = new JsonObject();
            json.put("moduleName", artefact.module());
            JsonArray directories = new JsonArray();
            for (FileRef file : artefact.content().files()) {
                JsonObject dir = new JsonObject();
                dir.put("name", file.name ());
                dir.put("link", file.link ());
                directories.add (dir);
            }
            json.put("directories", directories);
            return ViewBuilderSupport.build (new HtmlViewBuilder ("config", json, CodeServerController.class.getResource ("directorylist.html")));
        }
        if (SourceMapDescriptor.Type.FILE_LIST == artefact.type()) {
            JsonObject json = new JsonObject();
            json.put ("moduleName", artefact.module());
            json.put ("directory", artefact.path ());
            JsonArray files = new JsonArray();
            for (FileRef file : artefact.content().files()) {
                JsonObject fileJson = new JsonObject();
                fileJson.put ("name", file.name ());
                fileJson.put ("link", file.link ());
                files.add (fileJson);
            }
            json.put ("files", files);
            return ViewBuilderSupport.build (new HtmlViewBuilder ("config", json, CodeServerController.class.getResource ("filelist.html")));
        }
        if (SourceMapDescriptor.Type.SOURCE == artefact.type()) {
            if (html)
                return ViewBuilderSupport.build (new HtmlSourceViewBuilder (artefact.path(), artefact.content ().sourceMap (), artefact.content ().stream ()));
            return ViewBuilderSupport.build (new BinaryViewBuilder ("text/plain", artefact.content().stream ()));
        }
        if (SourceMapDescriptor.Type.MAP == artefact.type()) {
            return ViewBuilderSupport.build ((request, response) -> {
                String sourceRoot = "\"" + String.format ("http://%s:%d/sourcemaps/%s/", request.getServerName(), request.getServerPort(), artefact.module ()) + "\"";
                String sourceRootKey = "\"$sourceroot_goes_here$\"";
                BufferedReader reader = Files.newReader(artefact.content ().file (), Charsets.UTF_8);
                try {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType("application/json");
                    PrintWriter out = response.getWriter();
                    while (true) {
                        String line = reader.readLine();
                        if (line == null)
                            break;
                        line = line.replace (sourceRootKey, sourceRoot);
                        out.println (line);
                    }
                    out.flush ();
                } finally {
                    reader.close();
                }
            });
        }
        throw new CodeServerRunnerException("Unknown file: " + uri);
    }

    /**
     * Used by {@link #location(HttpServletRequest)} to serve up a symbol map.
     */
    protected View location_symbolmap(IModule module, String uri) throws CodeServerRunnerException {
        SymbolMapDescriptor descriptor = module.retrieveSymbolMap (uri);
        return ViewBuilderSupport.build ((request, response) -> {
            response.setStatus (HttpServletResponse.SC_OK);
            response.setContentType ("text/plain");
            Files.copy (descriptor.file (), response.getOutputStream ());
        });
    }

    /**
     * Used by {@link #location(HttpServletRequest)} to serve up a compiler artefact.
     */
    protected View location_artefact(IModule module, String uri) throws CodeServerRunnerException {
        ArtefactDescriptor artefact = module.retrieveArtefact (uri);
        return ViewBuilderSupport.build ((request, response) -> {
            response.setHeader ("Access-Control-Allow-Origin", "*");
            // Not sure why but if this is included then source maps don't seem to work (Chrome).
            // It is possible this never work (in the original code) so fixing it breaks it.
            // if (artefact.sourceMapUri () != null) {
            //     response.setHeader ("X-SourceMap", artefact.sourceMapUri ());
            //     response.setHeader ("SourceMap", artefact.sourceMapUri ());
            // }
            response.setHeader ("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
            response.setHeader ("Pragma", "no-cache");
            response.setHeader ("Expires", TIME_IN_THE_PAST);
            response.setDateHeader ("Date", new Date ().getTime ());
            if (artefact.gzipped()) {
                if (!request.getHeader ("Accept-Encoding").contains ("gzip")) {
                    response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
                    compiler.logger ().warn ("client doesn't accept gzip; bailing");
                    return;
                }
                response.setHeader ("Content-Encoding", "gzip");
            }
            response.setContentType (java.nio.file.Files.probeContentType (artefact.file().toPath ()));
            Files.copy (artefact.file(), response.getOutputStream ());
        });
    }
}