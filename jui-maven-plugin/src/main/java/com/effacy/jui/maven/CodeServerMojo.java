package com.effacy.jui.maven;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.toolchain.ToolchainManager;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;

/**
 * Spins up the codeserver on the command line.
 */
@Mojo(name = "codeserver", defaultPhase = LifecyclePhase.PROCESS_SOURCES, requiresDependencyResolution = ResolutionScope.COMPILE)
@Execute(phase = LifecyclePhase.PROCESS_SOURCES)
public class CodeServerMojo extends AbstractMojo {

    /**
     * The port that the server runs on.
     */
    @Parameter(property = "jui.port", defaultValue = "9876")
    private int port;

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
     * The Java source level to compile against. Injected from passed parameter
     * {@code maven.compiler.source}.
     * <p>
     * The default is 17.
     */
    @Parameter(property = "maven.compiler.source", defaultValue = "17")
    private String sourceLevel;

    /**
     * Used to diagnose the classpath. If set then the codeserver will not run but
     * the classpath (and associated information) will be displayed.
     */
    @Parameter(property = "jui.diagnose", defaultValue = "false")
    private boolean diagnose;

    /**
     * Name of the modules to serve.
     */
    @Parameter(property = "jui.module", required = true)
    private List<String> module;

    /**
     * To explicitly specify the sources (relative directory).
     */
    @Parameter(property = "jui.sources")
    private List<String> sources;

    /**
     * Collection of classpath exclusion patterns.
     */
    @Parameter(property = "jui.exclusions")
    private List<String> exclusions;

    /**
     * Collection of classpath inclusion patterns.
     */
    @Parameter(property = "jui.inclusions")
    private List<String> inclusions;

    /**
     * Arguments to be passed to the forked JVM (e.g. {@code -Xmx})
     */
    @Parameter(property = "jvmArgs")
    private List<String> jvmArgs;

    /**
     * List of system properties to pass to the compiler.
     */
    @Parameter
    private Map<String, String> systemProperties;

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

    /**
     * Descriptor for the plugin itself.
     */
    @Parameter(defaultValue = "${plugin}", readonly = true, required = true)
    private PluginDescriptor pluginDescriptor;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        List<String> cp = new ArrayList<>();

        // Add to the classpath the source roots (either derived or specified).
        if ((sources != null) && !sources.isEmpty()) {
            sources.forEach(s -> {
                File f =  Paths.get(project.getBasedir() + "/" + s).normalize().toFile();
                if (f.exists())
                    cp.add (f.getAbsolutePath());
            });
        } else {
            cp.addAll (SourcesAsResourcesHelper.filterSourceRoots (
                getLog(),
                project.getResources(),
                project.getCompileSourceRoots()
            ));
        }

        // Add to the classpath those that arise from dependencies, subject to the
        // inclusion and exclusion filtering.
        try {
            inclusions.add ("com.effacy.jui:*:*");
            inclusions.add ("org.gwtproject:*:*");
            inclusions.add ("org.gwtproject.*:*:*");
            List<String> inclusionPatterns = inclusions.stream().map(v -> v.replace(".", "\\.").replace("*", ".*")).toList();
            List<String> exclusionPatters = exclusions.stream().map(v -> v.replace(".", "\\.").replace("*", ".*")).toList();
            Set<Artifact> artefacts = project.getArtifacts();
            for (String path : project.getCompileClasspathElements()) {
                if (include(inclusionPatterns, artefacts, path) && !exclude(exclusionPatters, artefacts, path))
                    cp.add(path);
            }
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        // Explictly remove anything under target/classes (which may pollute the spring
        // boot application).
        cp.removeIf(t -> t.contains("target/classes"));

        // Add in the codeserver jar (no need to resolve dependencies as they are
        // included).
        String groupId = pluginDescriptor.getGroupId();
        String artefactId = "jui-platform-codeserver";
        //cp.addAll(0, ArtifactsAsResourcesHelper.resolve(getLog(), repoSystem, repoSession, groupId, artefactId, pluginDescriptor.getVersion(), "jar-with-dependencies"));
        cp.addAll(0, ArtifactsAsResourcesHelper.retrieve(getLog(), repoSystem, repoSession, groupId, artefactId, pluginDescriptor.getVersion(), "jar-with-dependencies"));

        // Display classpath if in diagnose mode.
        if (diagnose) {
            getLog().info("Classpath: ");
            cp.forEach(c -> getLog().info("   " + c));
        }

        List<String> args = new ArrayList<>();
        if (jvmArgs != null)
            args.addAll(jvmArgs);
        if (systemProperties != null) {
            for (Map.Entry<String, String> entry : systemProperties.entrySet())
                args.add("-D" + entry.getKey() + "=" + entry.getValue());
        }

        args.add("com.effacy.jui.codeserver.CodeServer");
        args.add("-generateJsInteropExports");
        args.add("-logLevel");
        args.add(logLevel);
        args.add("-sourceLevel");
        args.add(sourceLevel);
        args.add("-port");
        args.add(Integer.toString (port));
        args.addAll(module);

        // Display the command being executed if in diagnose mode.
        if (diagnose) {
            getLog().info ("Command:");
            StringBuffer sb = new StringBuffer();
            args.forEach(a -> {
                sb.append("'");
                sb.append(a);
                sb.append("' ");
            });
            getLog().info ("  " + sb.toString());
        }

        // Stop if only diagnosing.
        if (diagnose)
            return;

        // Run the compilation job.
        new JavaRunner(getLog(), project, session, toolchainManager, jdkToolchain, jvm)
            .execute(cp, args);
    }

    /**
     * Determines if the given path matches any of the passed patterns. If there are
     * no patterns the exclusion is assumed not to apply.
     */
    protected boolean exclude(List<String> patterns, Set<Artifact> artefacts, String path ) {
        if (patterns.isEmpty())
            return false;
        for (Artifact artifact : artefacts) {
            if (path.contains(artifact.getFile().getAbsolutePath())) {
                String coordinates = artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion();
                for (String pattern : patterns) {
                    if (coordinates.matches(pattern))
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given path matches any of the passed patterns.
     */
    protected boolean include(List<String> patterns, Set<Artifact> artefacts, String path) {
        if (patterns.isEmpty())
            return false;
        for (Artifact artifact : artefacts) {
            if (path.contains(artifact.getFile().getAbsolutePath())) {
                String coordinates = artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion();
                for (String pattern : patterns) {
                    if (coordinates.matches(pattern))
                        return true;
                }
            }
        }
        return false;
    }

}
