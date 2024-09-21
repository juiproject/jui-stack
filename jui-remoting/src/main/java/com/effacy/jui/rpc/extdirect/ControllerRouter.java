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
package com.effacy.jui.rpc.extdirect;

import java.util.Map;
import java.util.function.Consumer;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import com.effacy.jui.rpc.extdirect.annotation.AnnotatedRouter;

import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Controller that supports Spring model and view processing. This can be
 * included in a configured controller or as the base class for an annotated
 * controller. In this case you will likely want to override
 * {@link #handleRequest(HttpServletRequest, HttpServletResponse)} and annotate
 * the override with an entry point URL. The overriding method simply calls the
 * super-class method (so the sole point of the override is to annotate).
 * <p>
 * Note that this automatically tries to add the instance as an action handler,
 * so if you annotate any methods, then they will become available and method
 * end-points.
 * <p>
 * To add morphers, the easiest way is to configure a JSON parser in Spring and
 * add morphers to that, for example:
 * <p>
 * <tt>
 * <bean id="jsonParser" class="com.effacy.jui.remote.extjs.json.JsonLibJsonParser"&lt;
 *   <property name="morphers"&lt;
 *     <list&lt;
 *       ...
 *     </list&lt;
 *   </property&lt;
 * </bean&lt;
 * </tt>
 * <p>
 * then wire this into your controller through a setter (for example, using
 * auto-wiring):
 * <p>
 * <tt>
 * ...
 * &amp;Autowired
 * public void setJsonParser(IJsonParser parser) {
 *   super.setJsonParser(parser);
 * }
 * ...
 * </tt>
 * <p>
 * 
 * @author Jeremy Buckley
 */
public class ControllerRouter extends AnnotatedRouter {

    /**
     * The header to pass through the UI version.
     */
    public static final String HEADER_UI_VERSION = "Ui-Version";

    /**
     * The character encoding to use.
     */
    private String characterEncoding = "UTF-8";

    /**
     * To pass through on header to signal which UI version is acceptable.
     */
    private String uiVersion;

    /**
     * Default constructor, add this instance as an action handler.
     */
    public ControllerRouter() {
        addAction (this);
    }


    /**
     * Sets the character encoding that should be used for responses.
     * 
     * @param characterEncoding
     *            the character encoding.
     */
    public void setCharacterEncoding(String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }

    /**
     * A UI version specifier to guide the minimal accepted version of the UI to
     * accept.
     * <p>
     * This is passed throug on the "UiVersion" header.
     * 
     * @param uiVersion
     *                  the UI version.
     */
    public void setUiVersion(String uiVersion) {
        this.uiVersion = uiVersion;
    }


    /**
     * Handles an incoming request to return a Spring model and view response,
     * where the view generates output.
     * <p>
     * Note that the view is internally constructed to render JSON, so you do
     * not need to worry about view handling (i.e. mapping to a page).
     * 
     * @param request
     *            the incoming request.
     * @param response
     *            the outgoing response.
     * @return The model and view handler.
     * @throws Exception
     *             On error.
     */
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        StringView view = new StringView (processRequestToString (request, response));
        return new ModelAndView (view);
    }

    /**
     * A view the collects a list of objects and returns those objects as a JSON
     * array.
     */
    protected class StringView implements View {

        /**
         * The string response to return.
         */
        private String response;

        /**
         * Construct with a string response.
         * 
         * @param response
         *            the response as a string.
         */
        public StringView(String response) {
            this.response = response;
        }


        /**
         * {@inheritDoc}
         * 
         * @see org.springframework.web.servlet.View#getContentType()
         */
        @Override
        public String getContentType() {
            return "text/html";
        }


        /** {@inheritDoc}
         *
         * @see org.springframework.web.servlet.View#render(java.util.Map, jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
         */
        @Override
        public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
            response.setCharacterEncoding (characterEncoding);
            response.getWriter ().write (this.response);
            if (!StringUtils.isBlank (uiVersion))
                response.addHeader (HEADER_UI_VERSION, uiVersion);
        }

    }

    protected class ErrorView implements View {

        /**
         * The error status to return.
         */
        private HttpStatusCode status;

        /**
         * For additional configuration.
         */
        private Consumer<HttpServletResponse> configurer;

        /**
         * Construct with an error status.
         * 
         * @param status
         *            the error status.
         */
        public ErrorView(HttpStatusCode status) {
            this.status = status;
        }

        /**
         * Construct with an error status.
         * 
         * @param status
         *                   the error status.
         * @param configurer
         *                   used to provide additional configuration.
         */
        public ErrorView(HttpStatusCode status, Consumer<HttpServletResponse> configurer) {
            this.status = status;
            this.configurer = configurer;
        }


        /**
         * {@inheritDoc}
         * 
         * @see org.springframework.web.servlet.View#getContentType()
         */
        @Override
        public String getContentType() {
            return "text/plain";
        }

        /** {@inheritDoc}
         *
         * @see org.springframework.web.servlet.View#render(java.util.Map, jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
         */
        @Override
        public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
            if (configurer != null)
                configurer.accept (response);
            // Sending an error seems to only set a 403.
            //response.sendError (status.value ());
            response.setStatus(status.value());
        }


    }

}
