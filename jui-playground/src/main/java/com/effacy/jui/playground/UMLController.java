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
package com.effacy.jui.playground;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.View;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.sourceforge.plantuml.BlockUml;
import net.sourceforge.plantuml.ErrorUml;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.StringUtils;
import net.sourceforge.plantuml.code.Transcoder;
import net.sourceforge.plantuml.code.TranscoderUtil;
import net.sourceforge.plantuml.core.Diagram;
import net.sourceforge.plantuml.error.PSystemError;
import net.sourceforge.plantuml.preproc.Defines;
import net.sourceforge.plantuml.security.SecurityProfile;

/**
 * Endpoint for processing and serving up PlantUML images. Based in part on
 * {@link https://github.com/plantuml/plantuml-server/blob/master/src/main/java/net/sourceforge/plantuml/servlet/DiagramResponse.java}.
 */
@Controller
public class UMLController {

    /**
     * Determines if one should test for the resource not being modified since last
     * call.
     */
    private boolean testForNotModified = false;
    
    /**
     * Generates a UML image from an ecoded string.
     * 
     * @param model
     *                the model.
     * @param request
     * @return
     */
    @GetMapping("/uml/*")
    public View uml(Model model, HttpServletRequest request) {
         String url = request.getRequestURI ();
         String uml = extractUmlSource (extractEncodedDiagram (url));
         int idx = extractIndex (url);
        return new DiagramView (uml, idx);
    }

    /**
     * URL regex pattern to easily extract index and encoded diagram.
     */
    private static final Pattern URL_PATTERN = Pattern.compile ("/\\w+(?:/(?<idx>\\d+))?(?:/(?<encoded>[^/]+))?/?$");

    /**
     * Get encoded diagram source from URL.
     *
     * @param url
     *            URL to analyse, e.g., returned by `request.getRequestURI()`
     *
     * @return if exists diagram index; otherwise {@code ""}
     */
    public  String extractEncodedDiagram(String url) {
        Matcher matcher = URL_PATTERN.matcher (url);
        if (!matcher.find())
            return "";
        String encoded = matcher.group ("encoded");
        if (encoded == null)
            return "";
        return encoded;
    }

    /**
     * Build the complete UML source from the compressed source extracted from the
     * HTTP URI.
     *
     * @param source
     *               the last part of the URI containing the compressed UML
     *
     * @return the textual UML source
     */
    static public String extractUmlSource(String source) {
        // build the UML source from the compressed part of the URL
        String text;
        try {
            text = URLDecoder.decode(source, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            text = "' invalid encoded string";
        }
        Transcoder transcoder = TranscoderUtil.getDefaultTranscoder();
        try {
            text = transcoder.decode(text);
        } catch (IOException ioe) {
            text = "' unable to decode string";
        }

        // encapsulate the UML syntax if necessary
        String uml;
        if (text.startsWith("@start")) {
            uml = text;
        } else {
            StringBuilder plantUmlSource = new StringBuilder();
            plantUmlSource.append("@startuml\n");
            plantUmlSource.append(text);
            if (text.endsWith("\n") == false)
                plantUmlSource.append("\n");
            plantUmlSource.append("@enduml");
            uml = plantUmlSource.toString();
        }
        return uml;
    }

    /**
     * Get diagram index from URL.
     *
     * @param url
     *            URL to analyse, e.g., returned by `request.getRequestURI()`
     *
     * @return if exists diagram index; otherwise {@code 0}
     */
    public static int extractIndex(String url) {
        Matcher matcher = URL_PATTERN.matcher (url);
        if (!matcher.find())
            return 0;
        String idx = matcher.group("idx");
        if (idx == null)
            return 0;
        return Integer.parseInt(idx);
    }

    public class DiagramView implements View {

        private String uml;

        private int index;

        public DiagramView(String uml, int idx) {
            this.uml = uml;
            this.index = idx;
        }

        @Override
        public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
            response.addHeader ("Access-Control-Allow-Origin", "*");
            response.setContentType (getContentType ());

            if (index < 0) {
                response.sendError (HttpServletResponse.SC_BAD_REQUEST, String.format ("Invalid diagram index: {0}", index));
                return;
            }

            // Read in the UML diagram source.
            SourceStringReader reader = new SourceStringReader (Defines.createEmpty(), uml, CONFIG);
            if (reader.getBlocks ().isEmpty ()) {
                uml = "@startuml\n" + uml + "\n@enduml";
                reader = new SourceStringReader (Defines.createEmpty(), uml, CONFIG);
                if (reader.getBlocks ().isEmpty())
                    reader = null;
            }
            if (reader == null) {
                response.sendError (HttpServletResponse.SC_BAD_REQUEST, "No UML diagram found");
                return;
            }

            // Process the diagram.
            BlockSelection blockSelection = generateOutputBlockSelection(reader, index);
            if (blockSelection == null) {
                response.sendError (HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            // Check is the diagram has not been modified.
            if (testForNotModified && notModified (blockSelection.block, request)) {
                addHeaderForCache (blockSelection.block, response);
                response.sendError (HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }

            // Apply cache headers if applicable.
            if (StringUtils.isDiagramCacheable (uml))
                addHeaderForCache (blockSelection.block, response);

            // Generate the dialog and export.
            Diagram diagram = blockSelection.block.getDiagram ();
            if (diagram instanceof PSystemError)
                response.setStatus (HttpServletResponse.SC_BAD_REQUEST);
            diagram.exportDiagram (response.getOutputStream (), blockSelection.systemIdx, new FileFormatOption (FileFormat.PNG));
        }

        @Override
        public String getContentType() {
            return FileFormat.PNG.getMimeType ();
        }

        /**
         * Is block uml unmodified?
         *
         * @param blockUml block uml
         *
         * @return true if unmodified; otherwise false
         */
        private boolean notModified(BlockUml blockUml, HttpServletRequest request) {
            String ifNoneMatch = request.getHeader ("If-None-Match");
            long ifModifiedSince = request.getDateHeader ("If-Modified-Since");
            if ((ifModifiedSince != -1) && (ifModifiedSince != blockUml.lastModified ()))
                return false;
            String etag = blockUml.etag ();
            if (ifNoneMatch == null)
                return false;
            return ifNoneMatch.contains (etag);
        }

        static class BlockSelection {
            private BlockUml block;
            private int systemIdx;
    
            BlockSelection(BlockUml blk, int idx) {
                block = blk;
                systemIdx = idx;
            }
        }

        private BlockSelection generateOutputBlockSelection(SourceStringReader reader, int numImage) {
            if (numImage < 0)
                return null;

            Collection<BlockUml> blocks = reader.getBlocks();
            if (blocks.isEmpty())
                return null;

            for (BlockUml b : blocks) {
                Diagram system = b.getDiagram ();
                int nbInSystem = system.getNbImages ();
                if (numImage < nbInSystem)
                    return new BlockSelection (b, numImage);
                numImage -= nbInSystem;
            }

            return null;
        }

        /**
         * Add default header including cache headers to response.
         *
         * @param blockUml response block uml
         */
        private void addHeaderForCache(BlockUml blockUml, HttpServletResponse response) {
            long today = System.currentTimeMillis ();

            // Add http headers to force the browser to cache the image
            int maxAge = 3600 * 24 * 5;
            response.addDateHeader ("Expires", today + 1000L * maxAge);
            response.addDateHeader ("Date", today);

            response.addDateHeader ("Last-Modified", blockUml.lastModified ());
            response.addHeader ("Cache-Control", "public, max-age=" + maxAge);
            response.addHeader ("Etag", "\"" + blockUml.etag () + "\"");
            final Diagram diagram = blockUml.getDiagram ();
            response.addHeader("X-PlantUML-Diagram-Description", diagram.getDescription ().getDescription ());
            if (diagram instanceof PSystemError) {
                final PSystemError error = (PSystemError) diagram;
                for (ErrorUml err : error.getErrorsUml()) {
                    response.addHeader ("X-PlantUML-Diagram-Error", err.getError ());
                    response.addHeader ("X-PlantUML-Diagram-Error-Line", "" + err.getLineLocation ().getPosition ());
                }
            }
        }

    }

    /**
     * Additional configuration to apply during processing. You can add to this to
     * provide defaults for diagrams. See
     * {@link https://forum.plantuml.net/4266/configuration-file-specification} for
     * some comments on this.
     */
    private static List<String> CONFIG = new ArrayList<>();
    static {
        // set headless mode manually since otherwise Windows 11 seems to have some issues with it
        // see Issue#311 :: https://github.com/plantuml/plantuml-server/issues/311
        // NOTE: This can only be set before any awt/X11/... related stuff is loaded
        System.setProperty ("java.awt.headless", System.getProperty ("java.awt.headless", "true"));
        
        // Set default level of security.
        System.setProperty ("PLANTUML_SECURITY_PROFILE", SecurityProfile.INTERNET.toString ());

        // TODO: If desired setup additional CONFIG here.
    }
}
