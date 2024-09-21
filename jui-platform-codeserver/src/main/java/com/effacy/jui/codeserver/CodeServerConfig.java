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
import java.io.IOException;

import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.effacy.jui.codeserver.gwt.GWTCompilationService;
import com.effacy.jui.codeserver.gwt.Options;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.dev.util.log.PrintWriterTreeLogger;
import com.google.gwt.util.tools.Utility;

@Configuration
public class CodeServerConfig {

    /**
     * Constructs a GWT compilation service.
     * 
     * @param args
     *             arguments passed to the application.
     * @return the service instance.
     * @throws Exception on error.
     */
    @Bean
    public ICompilationService compilationService(ApplicationArguments args) throws Exception {
        Options options = new Options();
        options.parseArgs (args.getNonOptionArgs ().toArray (new String [0]));

        PrintWriterTreeLogger logger = new PrintWriterTreeLogger();
        logger.setMaxDetail (options.getLogLevel ());

        File workDir = options.getWorkDir ();
        if (workDir == null) {
            workDir = Utility.makeTemporaryDirectory (null, "jui-codeserver-");
        } else {
            if (!workDir.isDirectory())
                throw new IOException("Workspace directory doesn't exist: " + workDir);
        }

        TreeLogger startupLogger = logger.branch (Type.INFO, "Code server starting up");
        return new GWTCompilationService (startupLogger, options, workDir);
    }
}
