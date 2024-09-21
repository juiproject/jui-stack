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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.effacy.jui.playground.remoting.ApplicationService;

/**
 * Additional configuration for MVC.
 *
 * @author Jeremy Buckley
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * The underlying web service endpoint.
     * 
     * @return the service.
     */
    @Bean
    public ApplicationService webApplicationService() {
        return new ApplicationService ();
    }

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurer#addResourceHandlers(org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry)
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map to the docs resource on the classpath.
        registry.addResourceHandler ("/docs/**").addResourceLocations ("classpath:/docs/");
    }

}
