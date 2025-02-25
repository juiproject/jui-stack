package com.effacy.jui.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
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
import org.apache.maven.plugins.annotations.Execute;
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
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;

@Mojo(name = "compile", defaultPhase = LifecyclePhase.COMPILE, requiresDependencyResolution = ResolutionScope.COMPILE, threadSafe = true)
@Execute(phase = LifecyclePhase.COMPILE)
public class CompileMojo extends AbstractMojo {

    /**
     * The logging level to use.
     * <p>
     * For GWT the options are {@code INFO} (default), {@code DEBUG} and
     * {@code TRACE}.
     * <p>
     * Injected from passed parameter {@code jui.logLevel}.
     */
    @Parameter(property = "jui.logLevel", defaultValue="INFO")
    private String logLevel;

    /**
     * The output style.
     * <p>
     * For GWT the options are {@code OBFUSCATED}, {@code PRETTY}, or
     * {@code DETAILED}.
     * <p>
     * Injected from passed parameter {@code jui.style}.
     */
    @Parameter(property = "jui.style")
    private String style;

    /**
     * The optimization level used by the compiler.
     * <p>
     * For GWT the options range from 0 (none) to 9 (maximum).
     * <p>
     * Injected from passed parameter {@code jui.optimize}.
     */
    @Parameter(property = "jui.optimize")
    private Integer optimize;

    /**
     * The location where the build artefacts will be written to (with the
     * assumption being that this location adheres to a WAR file with top-level
     * artefacts being servable). Artefacts are segmented into modules with each
     * module artefacts being written into a directory named as per the module name
     * (full specified with package).
     * <p>
     * Defaults to {@code ${project.build.directory}/${project.build.finalName}}).
     */
    @Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}", required = true)
    private File webappDirectory;

    /**
     * A suitable working directory (must be writable).
     * <p>
     * Defaults to {@code ${project.build.directory}/jui/work} and this is usually
     * sufficient.
     */
    @Parameter(defaultValue = "${project.build.directory}/jui/work", required = true)
    private File workDir;

    /**
     * Compiler specific location where additional (but non-servable) artefacts are
     * generated (such as symbol maps).
     * <p>
     * Defaults to {@code ${project.build.directory}/jui/deploy} and this is usually
     * sufficient (unless you are intending to use any of these artefacts).
     */
    @Parameter(defaultValue = "${project.build.directory}/jui/deploy", required = true)
    private File deploy;

    /**
     * Compiler specific location where extra (non-servable) artefacts are
     * generated. No default and not required.
     */
    @Parameter
    private File extra;

    /**
     * Compiler specifc draft compilation (i.e. faster, but less-optimized,
     * compilations.)
     * <p>
     * Defaults to {@code false}.
     * <p>
     * Injected from passed parameter {@code jui.draftCompile}.
     */
    @Parameter(property = "jui.draftCompile", defaultValue = "false")
    private boolean draftCompile;

    /**
     * For GWT. The number of local workers to use when compiling permutations. When terminated
     * with "C", the number part is multiplied with the number of CPU cores. Floating
     * point values are only accepted together with "C".
     */
    @Parameter(property = "jui.localWorkers", defaultValue="16")
    private String localWorkers;

    /**
     * The Java source level to compile against. Injected from passed parameter
     * {@code maven.compiler.source}.
     * <p>
     * The default is 17.
     */
    @Parameter(property = "maven.compiler.source", defaultValue = "17")
    private String sourceLevel;

    /**
     * To generate JsInterop compatible exports. Injected from passed parameter
     * {@code jui.generateJsInteropExports}.
     * <p>
     * The default is {@code true}.
     */
    @Parameter(property = "jui.generateJsInteropExports", defaultValue="true")
    private boolean generateJsInteropExports;

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

    /**
     * The current project.
     */
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    /**
     * The current session.
     */
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    protected MavenSession session;

    /**
     * To resolve the JDK.
     */
    @Component
    protected ToolchainManager toolchainManager;

    /**
     * Used to resolve dependencies.
     */
    @Component
    private RepositorySystem repoSystem;

    /**
     * Used to resolve dependencies.
     */
    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    private RepositorySystemSession repoSession;

    @Override
    public void execute() throws MojoExecutionException {
        // If skipping, then quit.
        if (skipCompilation) {
            getLog().info("JUI compilation is being skipped");
            return;
        }

        // Obtain the source locations for the module.
        List<String> sourceRoots = SourcesAsResourcesHelper.filterSourceRoots (
            getLog(),
            project.getResources(),
            project.getCompileSourceRoots()
        );

        // Skip compilation if not being forced and there is no stale source.
        if (!forceCompilation && !stale(sourceRoots)) {
            getLog().info("Compilation output seems upto date. JUI compilation skipped.");
            return;
        }

        // Build the JVM args and pass through any system properties (i.e. as -D...=...).
        List<String> args = new ArrayList<>();
        if (jvmArgs != null)
            args.addAll(jvmArgs);
        if (systemProperties != null) {
            for (Map.Entry<String, String> entry : systemProperties.entrySet())
                args.add("-D" + entry.getKey() + "=" + entry.getValue());
        }

        // Configure the compiler.
        List<String> cp = new ArrayList<>();
        cp.addAll(sourceRoots);
        try {
            cp.addAll(project.getCompileClasspathElements());
            // getLog().info("Compile classpath: ");
            // project.getCompileClasspathElements().forEach(c -> getLog().info("   " + c));
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
        if ("gwt".equalsIgnoreCase(compiler)) {
            cp.addAll(configureForGWT (args));
        } else {
            getLog().info("Invalid compiler specified: \"" + compiler + "\". JUI compilation skipped.");
            return;
        }

        // Run the compilation job.
        new JavaRunner(getLog(), project, session, toolchainManager, jdkToolchain, jvm)
            .execute(cp, args);
    }

    private boolean stale(List<String> sourceRoots) throws MojoExecutionException {
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
            if (stale(scanner, new File(sourceRoot), nocacheJs))
                return true;
        }
        // compiled (processed) classes and resources (incl. processed and generated ones)
        if (stale(scanner, new File(project.getBuild().getOutputDirectory()), nocacheJs))
            return true;

        // POM
        if (stale(scanner, project.getFile(), nocacheJs))
            return true;
        
        // dependencies
        ScopeArtifactFilter artifactFilter = new ScopeArtifactFilter(Artifact.SCOPE_COMPILE);
        for (Artifact artifact : project.getArtifacts()) {
            if (!artifactFilter.include(artifact))
                continue;
            if (stale(scanner, artifact.getFile(), nocacheJs))
                return true;
        }

        return false;
    }

    private boolean stale(StaleSourceScanner scanner, File sourceFile, File targetFile) throws MojoExecutionException {
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

    protected List<String> configureForGWT(List<String> args) throws MojoExecutionException {
        args.add("com.google.gwt.dev.Compiler");
        
        if (!StringUtils.isBlank(logLevel)) {
            args.add("-logLevel");
            args.add(logLevel);
        }
        args.add("-war");
        args.add(webappDirectory.getAbsolutePath());
        args.add("-workDir");
        args.add(workDir.getAbsolutePath());
        args.add("-deploy");
        args.add(deploy.getAbsolutePath());
        if (extra != null) {
            args.add("-extra");
            args.add(extra.getAbsolutePath());
        }
        if (!StringUtils.isBlank(style)) {
            args.add("-style");
            args.add(style);
        }
        if (generateJsInteropExports)
            args.add("-generateJsInteropExports");
        else
            args.add("-nogenerateJsInteropExports");
        if (!StringUtils.isBlank(localWorkers)) {
            args.add("-localWorkers");
            int workers;
            if (localWorkers.contains("C"))
                workers = (int) (Float.valueOf(localWorkers.replace("C", "")) * Runtime.getRuntime().availableProcessors());
            else
                workers = Integer.valueOf(localWorkers);
            args.add(String.valueOf(workers));
        }
        args.add("-XnocheckCasts");
        args.add("-XfragmentCount");
        args.add("-1");
        if (draftCompile) {
            args.add("-draftCompile");
        } else if (optimize != null) {
            args.add("-optimize");
            args.add(String.valueOf(optimize.intValue()));
        }
        if (!StringUtils.isBlank(sourceLevel)) {
            args.add("-sourceLevel");
            args.add(sourceLevel);
        }


        if (failOnError != null)
            args.add(failOnError ? "-failOnError" : "-nofailOnError");
        
        // Add any compiler args that have been passed.
        if (compilerArgs != null)
            args.addAll(compilerArgs);

        // Add our module to the args.
        args.add(module);

        // Build out the classpath. Begin with te source roots, then add dependencies
        // the finally include gwt-dev (and its dependencies).
        List<String> cp = new ArrayList<>();
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
                if (!path.contains("jetty") && !path.contains("jasper") && !path.contains("htmlunit")) { 
                    if (getLog().isDebugEnabled())
                        getLog().debug("GWT compiler dependency ADDED: " + path);
                    cp.add (path);
                } else if (getLog().isDebugEnabled())
                    getLog().debug("GWT compiler dependency EXCLUDED: " + path);
            }
        } catch (Throwable e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
        return cp;
    }
    
}
