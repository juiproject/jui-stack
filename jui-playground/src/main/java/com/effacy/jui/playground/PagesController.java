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


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Handler presentation of all pages.
 *
 * @author Jeremy Buckley
 */
@Controller
public class PagesController {

    /**
     * Main page.
     * <p>
     * If the URL parameter {@code test} is passed with the value {@code true} the
     * the attribute {@code testMode} (with the value {@code true}) will be passed
     * to the template. The template will the put in the {@code gwt:test} meta tag
     * to enable test mode.
     */
    @GetMapping("/playground")
    public String main(Model model, HttpServletRequest request) {
        model.addAttribute ("testMode", "true".equals (request.getParameter("test")));
        return "playground";
    }
    
    /**
     * Documentation root (API root). Redirects to /docs/index.html.
     */
    @GetMapping("/error")
    public String error() {
        return "error";
    }
    
    /**
     * Documentation root (API root). Redirects to /docs/index.html.
     */
    @GetMapping("/")
    public String root() {
        return "redirect:/docs/index.html";
    }
    
    /**
     * Documentation root (API root). Redirects to /docs/index.html.
     */
    @GetMapping("/docs")
    public String docs() {
        return "redirect:/docs/index.html";
    }
}
