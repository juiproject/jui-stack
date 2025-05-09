/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.effacy.jui.codeserver.gwt;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.dev.ArgProcessorBase;
import com.google.gwt.dev.cfg.ModuleDef;
import com.google.gwt.dev.codeserver.CodeServer;
import com.google.gwt.dev.jjs.JsOutputOption;
import com.google.gwt.dev.util.arg.ArgHandlerBindAddress;
import com.google.gwt.dev.util.arg.ArgHandlerClosureFormattedOutput;
import com.google.gwt.dev.util.arg.ArgHandlerFilterJsInteropExports;
import com.google.gwt.dev.util.arg.ArgHandlerGenerateJsInteropExports;
import com.google.gwt.dev.util.arg.ArgHandlerIncrementalCompile;
import com.google.gwt.dev.util.arg.ArgHandlerLogLevel;
import com.google.gwt.dev.util.arg.ArgHandlerMethodNameDisplayMode;
import com.google.gwt.dev.util.arg.ArgHandlerScriptStyle;
import com.google.gwt.dev.util.arg.ArgHandlerSetProperties;
import com.google.gwt.dev.util.arg.ArgHandlerSourceLevel;
import com.google.gwt.dev.util.arg.OptionBindAddress;
import com.google.gwt.dev.util.arg.OptionClosureFormattedOutput;
import com.google.gwt.dev.util.arg.OptionGenerateJsInteropExports;
import com.google.gwt.dev.util.arg.OptionIncrementalCompile;
import com.google.gwt.dev.util.arg.OptionLogLevel;
import com.google.gwt.dev.util.arg.OptionMethodNameDisplayMode;
import com.google.gwt.dev.util.arg.OptionScriptStyle;
import com.google.gwt.dev.util.arg.OptionSetProperties;
import com.google.gwt.dev.util.arg.OptionSourceLevel;
import com.google.gwt.dev.util.arg.SourceLevel;
import com.google.gwt.thirdparty.guava.common.collect.ImmutableList;
import com.google.gwt.thirdparty.guava.common.collect.ImmutableSet;
import com.google.gwt.thirdparty.guava.common.collect.LinkedListMultimap;
import com.google.gwt.thirdparty.guava.common.collect.ListMultimap;
import com.google.gwt.util.regexfilter.WhitelistRegexFilter;
import com.google.gwt.util.tools.ArgHandler;
import com.google.gwt.util.tools.ArgHandlerDir;
import com.google.gwt.util.tools.ArgHandlerExtra;
import com.google.gwt.util.tools.ArgHandlerFlag;
import com.google.gwt.util.tools.ArgHandlerInt;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Defines the command-line options for the {@link CodeServer CodeServer's} that
 * are ultimately extracted from the application arguments.
 */
public class Options {

    private ImmutableList<String> args;
    private Set<String> tags = new LinkedHashSet<String>();

    private boolean incremental = true;
    private boolean noPrecompile = false;
    private boolean isCompileTest = false;
    private File workDir;
    private File launcherDir;
    private final List<String> moduleNames = new ArrayList<String>();
    private boolean allowMissingSourceDir = false;

    /**
     * See {@link #getSourcePath()}.
     */
    private final List<File> sourcePath = new ArrayList<File>();

    private String bindAddress = ArgHandlerBindAddress.DEFAULT_BIND_ADDRESS;
    private String preferredHost = ArgHandlerBindAddress.DEFAULT_BIND_ADDRESS;
    private int port = 9876;

    private TreeLogger.Type logLevel = TreeLogger.Type.INFO;
    
    /**
     * See {@link #getSourceLevel()}.
     */
    private SourceLevel sourceLevel = SourceLevel.JAVA17;
    private boolean failOnError = false;
    private int compileTestRecompiles = 0;
    // The default is to generate JS Interop exports.
    private boolean generateJsInteropExports = true;
    private WhitelistRegexFilter jsInteropExportFilter = new WhitelistRegexFilter();
    private OptionMethodNameDisplayMode.Mode methodNameDisplayMode = OptionMethodNameDisplayMode.Mode.NONE;
    private boolean closureFormattedOutput = false;
    // Incremental superdevmod has different defaults than devmode and regular superdevmode; we use
    // null here means not set by the user (and the right default is computed by getOutput().
    private JsOutputOption output = null;

    private final ListMultimap<String, String> properties = LinkedListMultimap.create();

    /**
     * Sets each option to the appropriate value, based on command-line arguments.
     * If there is an error, prints error messages and/or usage to System.err.
     * 
     * @return true if the arguments were parsed successfully.
     */
    public boolean parseArgs(String[] args) {
        if (this.args != null)
            throw new IllegalStateException("parseArgs may only be called once");
        this.args = ImmutableList.copyOf(Arrays.asList(args));

        // At some point consider using one of the standard argument processing
        // packages that can provide help (et al).
        boolean ok = new ArgProcessor().processArgs(args);
        if (!ok)
            return false;

        if (isCompileTest && noPrecompile) {
            System.err.println ("Usage: -noprecompile and -compiletest are incompatible");
            return false;
        }

        if (moduleNames.isEmpty()) {
            System.err.println ("Usage: at least one module must be supplied");
            return false;
        }

        if (incremental && !noPrecompile) {
            System.out.println ("Turning off precompile in incremental mode.");
            noPrecompile = true;
        }

        // Set some tags automatically for migration tracking.
        if (isIncrementalCompileEnabled()) {
            addTags("incremental_on");
        } else {
            addTags("incremental_off");
        }

        if (getNoPrecompile()) {
            addTags("precompile_off");
        } else {
            addTags("precompile_on");
        }

        return true;
    }

    /**
     * Adds some user-defined tags that will be passed through to {@link JobEvent#getTags}.
     *
     * <p>A tag may not be null, contain whitespace, or be more than 100 characters.
     * If a tag was already added, it won't be added again.
     *
     * <p>This method may be called more than once, but compile jobs that are already running
     * will not have the new tags.
     */
    public synchronized void addTags(String... tags) {
        this.tags.addAll(JobEvent.checkTags(Arrays.asList(tags)));
    }

    /**
     * Returns the arguments passed to {@link #parseArgs}.
     */
    public ImmutableList<String> getArgs() {
        return args;
    }

    /**
     * Returns the tags passed to {@link #addTags}.
     */
    public synchronized Set<String> getTags() {
        return ImmutableSet.copyOf(tags);
    }

    /**
     * The top level of the directory tree where the code server keeps compiler output.
     */
    public File getWorkDir() {
        return workDir;
    }

    /**
     * A directory where each module's files for launching Super Dev Mode should be written,
     * or null if not supplied.
     * (For example, nocache.js and public resource files will go here.)
     */
    public File getLauncherDir() {
        return launcherDir;
    }

    /**
     * The names of the module that will be compiled (along with all its dependencies).
     */
    public List<String> getModuleNames() {
        return moduleNames;
    }

    /**
     * Whether the codeServer should allow missing source directories.
     */
    boolean shouldAllowMissingSourceDir() {
        return allowMissingSourceDir;
    }

    /**
     * Compiles faster by creating a JavaScript file per class. Can't be turned on at the same time as
     * shouldCompileIncremental().
     */
    boolean isIncrementalCompileEnabled() {
        return incremental;
    }

    /**
     * Whether the codeServer should start without precompiling modules.
     */
    boolean getNoPrecompile() {
        return noPrecompile;
    }

    /**
     * The tree logger level.
     */
    public TreeLogger.Type getLogLevel() {
        return logLevel;
    }

    /**
     * Java source level compatibility. The default is {@link SourceLevel#JAVA11}.
     */
    SourceLevel getSourceLevel() {
        return sourceLevel;
    }

    /**
     * If true, just compile the modules, then exit.
     */
    boolean isCompileTest() {
        return isCompileTest;
    }

    /**
     * The IP address where the code server should listen.
     */
    public String getBindAddress() {
        return bindAddress;
    }

    int getCompileTestRecompiles() {
        return compileTestRecompiles;
    }

    /**
     * The hostname to put in a URL pointing to the code server.
     */
    String getPreferredHost() {
        return preferredHost;
    }

    /**
     * The port where the code server will listen for HTTP requests.
     */
    public int getPort() {
        return port;
    }

    /**
     * These are overriding source locations that are to be prepended to the
     * classpath.
     * 
     * @return the source.
     */
    public List<File> getSourcePath() {
        return sourcePath;
    }

    /**
     * If true, run the compiler in "strict" mode, which fails the compile if any Java file
     * cannot be compiled, whether or not it is used.
     */
    boolean isFailOnError() {
        return failOnError;
    }

    public boolean shouldGenerateJsInteropExports() {
        return generateJsInteropExports;
    }

    public WhitelistRegexFilter getJsInteropExportFilter() {
        return jsInteropExportFilter;
    }

    public JsOutputOption getOutput() {
        if (output == null)
            return isIncrementalCompileEnabled() ? JsOutputOption.OBFUSCATED : JsOutputOption.PRETTY;
        return output;
    }

    ListMultimap<String, String> getProperties() {
        return properties;
    }

    public boolean isClosureFormattedOutput() {
        return closureFormattedOutput;
    }

    private class ArgProcessor extends ArgProcessorBase {

        public ArgProcessor() {
            registerHandler(new AllowMissingSourceDirFlag());
            registerHandler(new CompileTestFlag());
            registerHandler(new CompileTestRecompilesFlag());
            registerHandler(new FailOnErrorFlag());
            registerHandler(new ModuleNameArgument());
            registerHandler(new NoPrecompileFlag());
            registerHandler(new PortFlag());
            registerHandler(new SourceFlag());
            registerHandler(new WorkDirFlag());
            registerHandler(new LauncherDir());
            registerHandler(new ArgHandlerBindAddress(new OptionBindAddress() {
                @Override
                public String getBindAddress() {
                    return Options.this.bindAddress;
                }

                @Override
                public String getConnectAddress() {
                    return Options.this.preferredHost;
                }

                @Override
                public void setBindAddress(String bindAddress) {
                    Options.this.bindAddress = bindAddress;
                }

                @Override
                public void setConnectAddress(String connectAddress) {
                    Options.this.preferredHost = connectAddress;
                }
            }));
            registerHandler(new ArgHandlerScriptStyle(new OptionScriptStyle() {
                @Override
                public JsOutputOption getOutput() {
                    return Options.this.output;
                }

                @Override
                public void setOutput(JsOutputOption output) {
                    Options.this.output = output;
                }
            }));
            registerHandler(new ArgHandlerSetProperties(new OptionSetProperties() {

                @Override
                public void setPropertyValues(String name, Iterable<String> values) {
                    properties.replaceValues(name, values);
                }

                @Override
                public ListMultimap<String, String> getProperties() {
                    return properties;
                }

            }));
            registerHandler(new ArgHandlerIncrementalCompile(new OptionIncrementalCompile() {
                @Override
                public boolean isIncrementalCompileEnabled() {
                    return incremental;
                }

                @Override
                public void setIncrementalCompileEnabled(boolean enabled) {
                    incremental = enabled;
                }
            }));
            registerHandler(new ArgHandlerSourceLevel(new OptionSourceLevel() {
                @Override
                public SourceLevel getSourceLevel() {
                    return sourceLevel;
                }

                @Override
                public void setSourceLevel(SourceLevel sourceLevel) {
                    Options.this.sourceLevel = sourceLevel;
                }
            }));
            registerHandler(new ArgHandlerLogLevel(new OptionLogLevel() {
                @Override
                public TreeLogger.Type getLogLevel() {
                    return logLevel;
                }

                @Override
                public void setLogLevel(TreeLogger.Type logLevel) {
                    Options.this.logLevel = logLevel;
                }
            }));
            OptionGenerateJsInteropExports optionGenerateJsInteropExport =
            new OptionGenerateJsInteropExports() {
                @Override
                public boolean shouldGenerateJsInteropExports() {
                    return Options.this.generateJsInteropExports;
                }
                @Override
                public void setGenerateJsInteropExports(boolean generateExports) {
                    Options.this.generateJsInteropExports = generateExports;
                }
                @Override
                public WhitelistRegexFilter getJsInteropExportFilter() {
                    return Options.this.jsInteropExportFilter;
                }
            };
            registerHandler(new ArgHandlerGenerateJsInteropExports(optionGenerateJsInteropExport));
            registerHandler(new ArgHandlerFilterJsInteropExports(optionGenerateJsInteropExport));
            registerHandler(new ArgHandlerMethodNameDisplayMode(new OptionMethodNameDisplayMode() {
                @Override
                public OptionMethodNameDisplayMode.Mode getMethodNameDisplayMode() {
                    return Options.this.methodNameDisplayMode;
                }

                @Override
                    public void setMethodNameDisplayMode(Mode mode) {
                    Options.this.methodNameDisplayMode = mode;
                }
            }));
            registerHandler(new ArgHandlerClosureFormattedOutput(new OptionClosureFormattedOutput() {
                @Override
                public boolean isClosureCompilerFormatEnabled() {
                    return Options.this.closureFormattedOutput;
                }

                @Override
                public void setClosureCompilerFormatEnabled(boolean enabled) {
                    Options.this.closureFormattedOutput = enabled;
                }
            }));
        }

        @Override
        protected String getName() {
            return CodeServer.class.getName();
        }
    }

    private class NoPrecompileFlag extends ArgHandlerFlag {

        @Override
        public String getLabel() {
            return "precompile";
        }

        @Override
        public String getPurposeSnippet() {
            return "Precompile modules.";
        }

        @Override
        public boolean setFlag(boolean value) {
            noPrecompile = !value;
            return true;
        }

        @Override
        public boolean getDefaultValue() {
            return !noPrecompile;
        }
    }

    private class CompileTestFlag extends ArgHandlerFlag {

        @Override
        public String getLabel() {
            return "compileTest";
        }

        @Override
        public String getPurposeSnippet() {
            return "Exits after compiling the modules. The exit code will be 0 if the compile succeeded.";
        }

        @Override
        public boolean setFlag(boolean value) {
            isCompileTest = value;
            return true;
        }

        @Override
        public boolean getDefaultValue() {
            return isCompileTest;
        }
    }

    private class CompileTestRecompilesFlag extends ArgHandlerInt {

        @Override
        public String getTag() {
            return "-compileTestRecompiles";
        }

        @Override
        public String[] getTagArgs() {
            return new String[] { "count" };
        }

        @Override
        public String getPurpose() {
            return "The number of times to recompile (after the first one) during a compile test.";
        }

        @Override
        public void setInt(int value) {
            compileTestRecompiles = value;
        }
    }

    private class PortFlag extends ArgHandlerInt {

        @Override
        public String getTag() {
            return "-port";
        }

        @Override
        public String[] getTagArgs() {
            return new String[] {"port"};
        }


        @Override
        public String getPurpose() {
            return "The port where the code server will run.";
        }

        @Override
        public void setInt(int newValue) {
            port = newValue;
        }
    }

    private class WorkDirFlag extends ArgHandlerDir {

        @Override
        public String getTag() {
            return "-workDir";
        }

        @Override
        public String getPurpose() {
            return "The root of the directory tree where the code server will"
                + "write compiler output. If not supplied, a temporary directory"
                + "will be used.";
        }

        @Override
        public void setDir(File newValue) {
            workDir = newValue;
        }
    }

    private class FailOnErrorFlag extends ArgHandlerFlag {

        FailOnErrorFlag() {
            // Backward compatibility with -strict in the regular compiler.
            addTagValue("-strict", true);
        }

        @Override
        public String getLabel() {
            return "failOnError";
        }

        @Override
        public boolean getDefaultValue() {
            return false;
        }

        @Override
        public String getPurposeSnippet() {
            return "Stop compiling if a module has a Java file with a compile error, even if unused.";
        }

        @Override
        public boolean setFlag(boolean value) {
            failOnError = value;
            return true;
        }
    }

    private class AllowMissingSourceDirFlag extends ArgHandlerFlag {

        @Override
        public String getLabel() {
            return "allowMissingSrc";
        }

        @Override
        public String getPurposeSnippet() {
            return "Allows -src flags to reference missing directories.";
        }

        @Override
        public boolean setFlag(boolean value) {
            allowMissingSourceDir = value;
            return true;
        }

        @Override
        public boolean getDefaultValue() {
            return allowMissingSourceDir;
        }
    }

    private class SourceFlag extends ArgHandler {

        @Override
        public String getTag() {
            return "-src";
        }

        @Override
        public String[] getTagArgs() {
            return new String[]{"dir"};
        }

        @Override
        public String getPurpose() {
            return "A directory containing GWT source to be prepended to the classpath for compiling.";
        }

        @Override
        public int handle(String[] args, int startIndex) {
            if (startIndex + 1 >= args.length) {
                System.err.println(getTag() + " should be followed by the name of a directory");
                return -1;
            }

            File candidate = new File(args[startIndex + 1]);
            if (!allowMissingSourceDir && !candidate.isDirectory()) {
                System.err.println("not a directory: " + candidate);
                return -1;
            }

            sourcePath.add (candidate);
            return 1;
        }
    }

    private class LauncherDir extends ArgHandler {

        @Override
        public String getTag() {
            return "-launcherDir";
        }

        @Override
        public String[] getTags() {
            // add an alias since in DevMode this was "-war"
            return new String[] {getTag(), "-war"};
        }

        @Override
        public String[] getTagArgs() {
            return new String[0];
        }

        @Override
        public String getPurpose() {
            return "An output directory where files for launching Super Dev Mode will be written. "
                + "(Optional.)";
        }

        @Override
        public int handle(String[] args, int startIndex) {
            if (startIndex + 1 >= args.length) {
                System.err.println(getTag() + " should be followed by the name of a directory");
                return -1;
            }

            File candidate = new File(args[startIndex + 1]);
            if (candidate.exists() && !candidate.isDirectory()) {
                System.err.println("not a directory: " + candidate);
                return -1;
            }

            launcherDir = candidate;
            return 1;
        }
    }

    private class ModuleNameArgument extends ArgHandlerExtra {

        @Override
        public String[] getTagArgs() {
            return new String[] {"module"};
        }

        @Override
        public String getPurpose() {
            return "The GWT modules that the code server should compile. (Example: com.example.MyApp)";
        }

        @Override
        public boolean addExtraArg(String arg) {
            if (!ModuleDef.isValidModuleName(arg)) {
                System.err.println("Invalid module name: '" + arg + "'");
                return false;
            }
            moduleNames.add(arg);
            return true;
        }
    }

    public OptionMethodNameDisplayMode.Mode getMethodNameDisplayMode() {
        return methodNameDisplayMode;
    }
}
