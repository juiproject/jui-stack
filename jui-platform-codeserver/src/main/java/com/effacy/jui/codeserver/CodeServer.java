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

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.effacy.jui.codeserver.gwt.Options;
import com.google.gwt.dev.util.arg.SourceLevel;

@SpringBootApplication
public class CodeServer {
    public static void main(String... args) {
        // This is just a verification of the args.
        Options options = new Options();

        // We need to override the default source level. The simplest approach is to
        // inject the argument if it is not present.
        List<String> extendedArgs = new ArrayList<>();
        boolean foundSourceLevel = false;
        for (String arg : args) {
            foundSourceLevel = foundSourceLevel || "-sourceLevel".equals(arg);
            extendedArgs.add (arg);
        }
        if (!foundSourceLevel) {
            extendedArgs.add(0, SourceLevel.JAVA17.getStringValue());
            extendedArgs.add(0, "-sourceLevel");
        }
        args = extendedArgs.toArray(new String [extendedArgs.size()]);

        // Validate the args.
        if (!options.parseArgs (args))
            System.exit (1);

        // Start up the server and run with args.
        SpringApplication app = new SpringApplication (CodeServer.class);
        app.setBannerMode (Mode.OFF);
        app.setRegisterShutdownHook(true);
        try {
            app.run (args);
        } catch (Throwable e) {
            System.exit(1);
        }
    }
}
