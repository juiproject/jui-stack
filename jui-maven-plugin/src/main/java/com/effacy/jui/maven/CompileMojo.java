package com.effacy.jui.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.toolchain.ToolchainManager;
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
import org.codehaus.plexus.compiler.util.scan.StaleSourceScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SourceMapping;
import org.codehaus.plexus.util.StringUtils;

@Mojo(name = "compile", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE, threadSafe = true)
public class CompileMojo extends AbstractMojo implements ICompilerOptions {

    /**
     * The compiler that should be used.
     * <p>
     * Currently only GWT is supported.
     */
    @Parameter(property = "jui.compiler", defaultValue = "gwt")
    private String compiler;

    /**
     * Enable faster, but less-optimized, compilations.
     */
    @Parameter(property = "jui.draftCompile", defaultValue = "false")
    private boolean draftCompile;

    /**
     * The directory into which deployable but not servable output files will be written.
     */
    @Parameter(defaultValue = "${project.build.directory}/jui/deploy", required = true)
    private File deploy;

    /**
     * The directory into which extra files, not intended for deployment, will be written.
     */
    @Parameter
    private File extra;

    /**
     * The number of local workers to use when compiling permutations. When terminated
     * with "C", the number part is multiplied with the number of CPU cores. Floating
     * point values are only accepted together with "C".
     */
    @Parameter(property = "jui.localWorkers", defaultValue="16")
    private String localWorkers;

    /**
     * Sets the level of logging detail.
     */
    @Parameter(property = "jui.logLevel", defaultValue="INFO")
    private String logLevel;

    /**
     * Name of the module to compile.
     */
    @Parameter(required = true)
    private String module;

    /**
     * The short name of the module, used to name the output {@code .nocache.js} file.
     */
    @Parameter
    private String moduleShortName;

    /**
     * Sets the optimization level used by the compiler.  0=none 9=maximum.
     */
    @Parameter(property = "jui.optimize")
    private Integer optimize;

    /**
     * Specifies Java source level. Default is 17.
     */
    @Parameter(property = "maven.compiler.source", defaultValue = "17")
    private String sourceLevel;

    /**
     * Script output style: OBFUSCATED, PRETTY, or DETAILED.
     */
    @Parameter(property = "jui.style")
    private String style;

    /**
     * Only succeed if no input files have errors.
     */
    @Parameter(property = "jui.failOnError")
    private Boolean failOnError;

    /**
     * Specifies the location of the target war directory.
     */
    @Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}", required = true)
    private File webappDirectory;

    /**
     * The compiler work directory (must be writeable).
     */
    @Parameter(defaultValue = "${project.build.directory}/jui/work", required = true)
    private File workDir;

    /**
     * Additional arguments to be passed to the compiler.
     */
    @Parameter
    private List<String> compilerArgs;

    /**
     * Arguments to be passed to the forked JVM (e.g. {@code -Xmx})
     */
    @Parameter
    private List<String> jvmArgs;

    /**
     * List of system properties to pass to the GWT compiler.
     */
    @Parameter
    private Map<String, String> systemProperties;

    /**
     * Sets the granularity in milliseconds of the last modification
     * date for testing whether the module needs recompilation.
     */
    @Parameter(property = "lastModGranularityMs", defaultValue = "0")
    private int staleMillis;

    /**
     * Require the GWT plugin to compile the GWT module even if none of the
     * sources appear to have changed. By default, this plugin looks to see if
     * the output *.nocache.js exists and inputs (POM, sources and dependencies)
     * have not changed.
     */
    @Parameter(property = "jui.forceCompilation", defaultValue="false")
    private boolean forceCompilation;

    /**
     * Require the plugin to skip compilation. This can be useful to quickly
     * package an incomplete or stale application that's used as a dependency (an
     * overlay generally) in a war, for example to launch that war in a container
     * and then launch DevMode for this GWT application.
     */
    @Parameter(property = "jui.skipCompilation", defaultValue="false")
    private boolean skipCompilation;

    @Parameter(property = "jui.generateJsInteropExports", defaultValue="true")
    private boolean generateJsInteropExports;

    /**
     * Path to the Java executable to use.
     * By default, will use the configured toolchain, or fallback to the same JVM as the one used to run Maven.
     */
    @Parameter
    private String jvm;

    /**
     * Requirements for this jdk toolchain, if {@link #jvm} is not set.
     * <p>
     * This overrides the toolchain selected by the maven-toolchains-plugin.
     */
    @Parameter
    private Map<String, String> jdkToolchain;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    protected MavenSession session;

    @Component
    protected ToolchainManager toolchainManager;

    public void execute() throws MojoExecutionException {
        if (skipCompilation) {
            getLog().info("JUI compilation is being skipped");
            return;
        }

        List<String> sourceRoots = SourcesAsResourcesHelper.filterSourceRoots (
            getLog(),
            project.getResources(),
            project.getCompileSourceRoots()
        );

        if (!forceCompilation && !isStale(sourceRoots)) {
            getLog().info("Compilation output seems upto date. JUI compilation skipped.");
            return;
        }

        List<String> args = new ArrayList<>();
        if (jvmArgs != null)
            args.addAll(jvmArgs);
        if (systemProperties != null) {
            for (Map.Entry<String, String> entry : systemProperties.entrySet())
                args.add("-D" + entry.getKey() + "=" + entry.getValue());
        }
        if ("gwt".equals(compiler)) {
            args.add("com.google.gwt.dev.Compiler");
        } else {
            getLog().info("Invalid compiler specified: \"" + compiler + "\". JUI compilation skipped.");
            return;
        }

        args.addAll(buildArgs(getLog()));
        if (failOnError != null)
            args.add(failOnError ? "-failOnError" : "-nofailOnError");
        
        if (compilerArgs != null)
            args.addAll(compilerArgs);
        args.add(module);

        Set<String> cp = new LinkedHashSet<>();
        cp.addAll(sourceRoots);
        try {
            cp.addAll(project.getCompileClasspathElements());
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        // Run the compilation job.
        new CommandLineRunner(getLog(), project, session, toolchainManager, jdkToolchain, jvm)
            .execute(cp, args);

        // XXX: workaround for GWT 2.7.0 not setting nocache.js lastModified correctly.
        // if (isStale(sourceRoots)) {
        //     String shortName = getModuleShortName();
        //     File nocacheJs = new File(webappDirectory, shortName + File.separator + shortName + ".nocache.js");
        //     nocacheJs.setLastModified(System.currentTimeMillis());
        // }
    }

    private boolean isStale(List<String> sourceRoots) throws MojoExecutionException {
        if (!webappDirectory.exists())
            return true;

        // TODO: take various flags into account
        String shortName = getModuleShortName();
        File nocacheJs = new File(webappDirectory, shortName + File.separator + shortName + ".nocache.js");
        if (!nocacheJs.isFile()) {
            getLog().debug(nocacheJs.getPath() + " file found or is not a file: recompiling");
            return true;
        }
        if (getLog().isDebugEnabled())
            getLog().debug("Found *.nocache.js at " + nocacheJs.getAbsolutePath());

        StaleSourceScanner scanner = new StaleSourceScanner(staleMillis);
        scanner.addSourceMapping(new SourceMapping() {
            Set<File> targetFiles = Collections.singleton(nocacheJs);

            @Override
            public Set<File> getTargetFiles(File targetDir, String source) throws InclusionScanException {
                return targetFiles;
            }
        });

        // sources (incl. generated ones)
        for (String sourceRoot : sourceRoots) {
            if (isStale(scanner, new File(sourceRoot), nocacheJs))
                return true;
        }
        // compiled (processed) classes and resources (incl. processed and generated ones)
        if (isStale(scanner, new File(project.getBuild().getOutputDirectory()), nocacheJs))
            return true;

        // POM
        if (isStale(scanner, project.getFile(), nocacheJs))
            return true;
        
        // dependencies
        ScopeArtifactFilter artifactFilter = new ScopeArtifactFilter(Artifact.SCOPE_COMPILE);
        for (Artifact artifact : project.getArtifacts()) {
            if (!artifactFilter.include(artifact))
                continue;
            if (isStale(scanner, artifact.getFile(), nocacheJs))
                return true;
        }

        return false;
    }

    private boolean isStale(StaleSourceScanner scanner, File sourceFile, File targetFile) throws MojoExecutionException {
        if (!sourceFile.isDirectory()) {
            boolean stale = (targetFile.lastModified() + staleMillis < sourceFile.lastModified());
            if (stale && getLog().isDebugEnabled())
                getLog().debug("Source file is newer than nocache.js, recompiling: " + sourceFile.getAbsolutePath());
            return stale;
        }

        try {
            Set<File> sourceFiles = scanner.getIncludedSources(sourceFile, webappDirectory);
            boolean stale = !sourceFiles.isEmpty();
            if (stale && getLog().isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                for (File source : sourceFiles)
                    sb.append("\n - ").append(source.getAbsolutePath());
                getLog().debug("Source files are newer than nocache.js, recompiling: " + sb.toString());
            }
            return stale;
        } catch (InclusionScanException e) {
            throw new MojoExecutionException("Error scanning source root: \'" + sourceFile.getPath() + "\' for stale files to recompile.", e);
        }
    }

    private String getModuleShortName() {
        if (StringUtils.isBlank(moduleShortName)) {
            // TODO: load ModuleDef to get target name
            return module;
        }
        return moduleShortName;
    }

    /************************************************************************
     * Implementation of {@link ICompilerOptions}.
     ************************************************************************/

    @Override
    public String getLogLevel() {
        return logLevel;
    }

    @Override
    public String getStyle() {
        return style;
    }

    @Override
    public Integer getOptimize() {
        return optimize;
    }

    @Override
    public File getWarDir() {
        return webappDirectory;
    }

    @Override
    public File getWorkDir() {
        return workDir;
    }

    @Override
    public File getDeployDir() {
        return deploy;
    }

    @Override
    public File getExtraDir() {
        return extra;
    }

    @Override
    public boolean isDraftCompile() {
        return draftCompile;
    }

    @Override
    public String getLocalWorkers() {
        return localWorkers;
    }

    @Override
    public String getSourceLevel() {
        return sourceLevel;
    }

    @Override
    public boolean isGenerateJsInteropExports() {
        return generateJsInteropExports;
    }
    
}
