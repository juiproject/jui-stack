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
import org.apache.maven.shared.transfer.artifact.DefaultArtifactCoordinate;
import org.apache.maven.toolchain.ToolchainManager;
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
import org.codehaus.plexus.compiler.util.scan.StaleSourceScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SourceMapping;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;

@Mojo(name = "compile", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE, threadSafe = true)
public class CompileMojo extends AbstractMojo implements ICompilerOptions {

    /**
     * The compiler that should be used.
     * <p>
     * Currently only {@code gwt} is supported.
     */
    @Parameter(property = "jui.compiler", defaultValue = "gwt")
    private String compiler;

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
     * Only succeed if no input files have errors.
     */
    @Parameter(property = "jui.failOnError")
    private Boolean failOnError;

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
     * List of system properties to pass to the compiler.
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

    /**
     * The GWT version to use (for use when using the GWT compiler).
     * <p>
     * Generally this should be fixed (as JUI depends on it) but we allow some
     * flexibility here.
     */
    @Parameter(property = "jui.gwtVersion", defaultValue="2.12.1")
    private String gwtVersion;

    /**
     * Path to the Java executable to use.
     * <p>
     * By default, will use the configured toolchain, or fallback to the same JVM as
     * the one used to run Maven.
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

    /**
     * Used to resolve the GWT dependencies.
     */
    @Component
    private RepositorySystem repoSystem;

    /**
     * Used to resolve the GWT dependencies.
     */
    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    private RepositorySystemSession repoSession;

    @Override
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
        
        // Add any compiler args that have been passed.
        if (compilerArgs != null)
            args.addAll(compilerArgs);

        // Add our module to the args.
        args.add(module);

        // Build out the classpath. Begin with te source roots, then add dependencies
        // the finally include gwt-dev (and its dependencies).
        Set<String> cp = new LinkedHashSet<>();
        cp.addAll(sourceRoots);
        try {
            cp.addAll(project.getCompileClasspathElements());
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
        try {
            String gwtGroup = "org.gwtproject";
            String gwtArtefact = "gwt-dev";
            DefaultArtifact artifactToResolve = new DefaultArtifact(gwtGroup + ":" + gwtArtefact + ":" + gwtVersion);
            CollectRequest collectRequest = new CollectRequest();
            collectRequest.setRoot(new Dependency(artifactToResolve, ""));
            // Assume maven central is accessible.
            // collectRequest.setRepositories(remoteRepos);
            DependencyRequest dependencyRequest = new DependencyRequest();
            dependencyRequest.setCollectRequest(collectRequest);
            List<ArtifactResult> resolvedArtifacts = repoSystem.resolveDependencies(repoSession, dependencyRequest).getArtifactResults();
            for (ArtifactResult result : resolvedArtifacts) {
                String path = result.getArtifact().getFile().getAbsolutePath();
                if (!path.contains("jetty")) { 
                    if (getLog().isDebugEnabled())
                        getLog().debug("GWT compiler dependency ADDED: " + path);
                    cp.add (path);
                } else if (getLog().isDebugEnabled())
                    getLog().debug("GWT compiler dependency EXCLUDED: " + path);
            }
        } catch (Throwable e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        // Run the compilation job.
        new JavaRunner(getLog(), project, session, toolchainManager, jdkToolchain, jvm)
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
     * Implementation of {@link ICompilerOptions} with the associated Maven
     * parameters.
     ************************************************************************/

    /**
     * See {@link #getLogLevel()}.
     */
    @Parameter(property = "jui.logLevel", defaultValue="INFO")
    private String logLevel;

    /**
     * See {@link #getStyle()}.
     */
    @Parameter(property = "jui.style")
    private String style;

    /**
     * See {@link #getOptimize()}.
     */
    @Parameter(property = "jui.optimize")
    private Integer optimize;

    /**
     * See {@link #getWarDir()}.
     */
    @Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}", required = true)
    private File webappDirectory;

    /**
     * See {@link #getWorkDir()}.
     */
    @Parameter(defaultValue = "${project.build.directory}/jui/work", required = true)
    private File workDir;

    /**
     * See {@link #getDeployDir()}.
     */
    @Parameter(defaultValue = "${project.build.directory}/jui/deploy", required = true)
    private File deploy;

    /**
     * See {@link #getExtraDir()}.
     */
    @Parameter
    private File extra;

    /**
     * See {@link #isDraftCompile()}.
     */
    @Parameter(property = "jui.draftCompile", defaultValue = "false")
    private boolean draftCompile;

    /**
     * See {@link #getLocalWorkers()}.
     */
    @Parameter(property = "jui.localWorkers", defaultValue="16")
    private String localWorkers;

    /**
     * See {@link #getSourceLevel()}.
     */
    @Parameter(property = "maven.compiler.source", defaultValue = "17")
    private String sourceLevel;

    /**
     * See {@lik #isGenerateJsInteropExports()}.
     */
    @Parameter(property = "jui.generateJsInteropExports", defaultValue="true")
    private boolean generateJsInteropExports;

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
