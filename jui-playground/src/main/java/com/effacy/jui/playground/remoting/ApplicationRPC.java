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
package com.effacy.jui.playground.remoting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import com.effacy.jui.rpc.extdirect.ControllerRouter;
import com.effacy.jui.rpc.extdirect.csrf.XorCSRFHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class ApplicationRPC extends ControllerRouter {

    /**
     * Default constructor.
     */
    public ApplicationRPC() {
        setIncludeCreateBlank (false);
        setRemotingApi ("RemotingApi");
        setIncludeProviderStatement (false);
        setEnforceTransactions (false);
        setRetryCount (2);
        setRetryDelay (150);
        setDebugMode (true);
        setUseCsrfTokens (false);
        setUseCsrfSession (false);
        // setUseCsrfSecure (false);
        setUseCsrfHttpOnly (false);

        // Create the parser (localised to this).
        scanPackages ("com.effacy.jui.rpc.client",
            "com.effacy.jui.rpc.extdirect.client",
            "com.effacy.jui.rpc.handler.client",
            "com.effacy.jui.playground.dto");

        // Use the XOR encoder as used by Spring.
        setCsrfEncoder (new XorCSRFHandler ());
    }

    /**
     * Endpoint that returns remoting meta-data for use in constructing service
     * queries and the like.
     */
    @GetMapping("/app/rpc.json")
    public ModelAndView meta(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType ("application/javascript");
        return super.handleRequest (request, response);
    }

    /**
     * Endpoint for processing RPC queries.
     */
    @PostMapping("/app/rpc.json")
    public ModelAndView rpc(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return super.handleRequest (request, response);
    }

    /**
     * Assigns the service.
     * 
     * @param service
     *                the service.
     */
    @Autowired
    public void setWebApplicationService(ApplicationService service) {
        addAction (service);
    }

}

