package com.effacy.jui.maven;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.toolchain.Toolchain;
import org.apache.maven.toolchain.ToolchainManager;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * Executes Java subject to a supplied classpath and arguments.
 */
public class JavaRunner {

    private Log log;
    private MavenProject project;
    private MavenSession session;
    private ToolchainManager toolchainManager;
    private Map<String, String> toolchainRequirements;
    private String jvm;

    /**
     * Construct with execution context.
     */
    JavaRunner(Log log, MavenProject project, MavenSession session, ToolchainManager toolchainManager, Map<String, String> toolchainRequirements, String jvm) {
        this.log = log;
        this.project = project;
        this.session = session;
        this.toolchainManager = toolchainManager;
        this.toolchainRequirements = toolchainRequirements;
        this.jvm = jvm;
    }

    /**
     * Executes Java against the passed arguments given the supplied classpath.
     * 
     * @param classpath
     *                  the classpath to use (assigned to environment).
     * @param arguments
     *                  the arguments to pass to the Java executable.
     * @throws MojoExecutionException
     *                                on error.
     */
    public void execute(Iterable<String> classpath, List<String> arguments) throws MojoExecutionException {
        String cp = StringUtils.join(classpath.iterator(), File.pathSeparator);
        String[] args = arguments.toArray(new String[arguments.size()]);

        Commandline cl = new Commandline();
        cl.setWorkingDirectory(project.getBuild().getDirectory());
        cl.setExecutable(_executable());
        cl.addEnvironment("CLASSPATH", cp);
        cl.addArguments(args);

        if (log.isDebugEnabled()) {
            log.debug("Classpath: " + cp);
            StringBuffer sb = new StringBuffer();
            for (String arg : cl.getRawCommandline()) {
                sb.append(" '");
                sb.append(arg);
                sb.append('\'');
            }
            log.debug("Command: /bin/sh -c" + sb);
        }

        // Run the command.
        try {
            int result = CommandLineUtils.executeCommandLine(cl, s -> log.info(s), s -> log.warn(s));
            if (result != 0)
                throw new MojoExecutionException("Compiler exited with status " + result);
        } catch (CommandLineException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    /**
     * Determines the executable.
     * <p>
     * The comes from the {@code jvm} parameter (passed through the constructor) if
     * present. Otherwise the tool chain is queried for {@code java}. If that does
     * not resolve the issue then the system property {@code java.home} is
     * interrogated.
     * 
     * @return the Java executable.
     */
    private String _executable() {
        if (StringUtils.isNotBlank(jvm))
            return jvm;
        Toolchain tc = _toolchain();
        if (tc != null) {
            String executable = tc.findTool("java");
            if (StringUtils.isNotBlank(executable)) {
                if (log.isDebugEnabled())
                    log.debug("Toolchain: " + tc);
                return executable;
            }
        }
        return Paths.get(System.getProperty("java.home"), "bin", "java").toString();
    }

    /**
     * Used by {@link #_executable()} to resolve a toolchain.
     * 
     * @return the toolchain.
     */
    private Toolchain _toolchain() {
        Toolchain tc = null;
        if ((toolchainRequirements != null) && !toolchainRequirements.isEmpty()) {
            List<Toolchain> tcs = toolchainManager.getToolchains(session, "jdk", toolchainRequirements);
            if (tcs != null && !tcs.isEmpty())
                tc = tcs.get(0);
        }
        if (tc == null)
            tc = toolchainManager.getToolchainFromBuildContext("jdk", session);
        return tc;
    }
}
