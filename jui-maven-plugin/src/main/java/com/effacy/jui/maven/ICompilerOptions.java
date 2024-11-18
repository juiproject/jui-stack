package com.effacy.jui.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.logging.Log;

public interface ICompilerOptions {

    /**
     * The logging level to use.
     * <p>
     * For GWT the options are {@code INFO} (default), {@code DEBUG} and
     * {@code TRACE}.
     * <p>
     * Injected from passed parameter {@code jui.logLevel}.
     */
    public String getLogLevel();

    /**
     * The output style.
     * <p>
     * For GWT the options are {@code OBFUSCATED}, {@code PRETTY}, or
     * {@code DETAILED}.
     * <p>
     * Injected from passed parameter {@code jui.style}.
     */
    public String getStyle();

    /**
     * The optimization level used by the compiler.
     * <p>
     * For GWT the options range from 0 (none) to 9 (maximum).
     * <p>
     * Injected from passed parameter {@code jui.optimize}.
     */
    public Integer getOptimize();

    /**
     * The location where the build artefacts will be written to (with the
     * assumption being that this location adheres to a WAR file with top-level
     * artefacts being servable). Artefacts are segmented into modules with each
     * module artefacts being written into a directory named as per the module name
     * (full specified with package).
     * <p>
     * Defaults to {@code ${project.build.directory}/${project.build.finalName}}).
     */
    public File getWarDir();

    /**
     * A suitable working directory (must be writable).
     * <p>
     * Defaults to {@code ${project.build.directory}/jui/work} and this is usually
     * sufficient.
     */
    public File getWorkDir();

    /**
     * Compiler specific location where additional (but non-servable) artefacts are
     * generated (such as symbol maps).
     * <p>
     * Defaults to {@code ${project.build.directory}/jui/deploy} and this is usually
     * sufficient (unless you are intending to use any of these artefacts).
     */
    public File getDeployDir();

    /**
     * Compiler specific location where extra (non-servable) artefacts are
     * generated. No default and not required.
     */
    public File getExtraDir();

    /**
     * Compiler specifc draft compilation (i.e. faster, but less-optimized,
     * compilations.)
     * <p>
     * Defaults to {@code false}.
     * <p>
     * Injected from passed parameter {@code jui.draftCompile}.
     */
    public boolean isDraftCompile();

    /**
     * For GWT. The number of local workers to use when compiling permutations. When terminated
     * with "C", the number part is multiplied with the number of CPU cores. Floating
     * point values are only accepted together with "C".
     */
    public String getLocalWorkers();

    /**
     * The Java source level to compile against. Injected from passed parameter
     * {@code maven.compiler.source}.
     * <p>
     * The default is 17.
     */
    public String getSourceLevel();

    /**
     * To generate JsInterop compatible exports. Injected from passed parameter
     * {@code jui.generateJsInteropExports}.
     * <p>
     * The default is {@code true}.
     */
    public boolean isGenerateJsInteropExports();

    default public List<String> buildArgs(Log log) {
        List<String> args = new ArrayList<>();
        if (getLogLevel() != null) {
            args.add("-logLevel");
            args.add(getLogLevel());
        }
        args.add("-war");
        args.add(getWarDir().getAbsolutePath());
        args.add("-workDir");
        args.add(getWorkDir().getAbsolutePath());
        args.add("-deploy");
        args.add(getDeployDir().getAbsolutePath());
        if (getExtraDir() != null) {
            args.add("-extra");
            args.add(getExtraDir().getAbsolutePath());
        }
        if (getStyle() != null) {
            args.add("-style");
            args.add(getStyle());
        }
        if (isGenerateJsInteropExports())
            args.add("-generateJsInteropExports");
        else
            args.add("-nogenerateJsInteropExports");
        if (getLocalWorkers() != null) {
            args.add("-localWorkers");
            int workers;
            if (getLocalWorkers().contains("C")) {
                workers = (int) (Float.valueOf(getLocalWorkers().replace("C", "")) * Runtime.getRuntime().availableProcessors());
            } else {
                workers = Integer.valueOf(getLocalWorkers());
            }
            args.add(String.valueOf(workers));
        }
        args.add("-XnocheckCasts");
        args.add("-XfragmentCount");
        args.add("-1");
        if (isDraftCompile()) {
            args.add("-draftCompile");
        } else if (getOptimize() != null) {
            args.add("-optimize");
            args.add(String.valueOf(getOptimize().intValue()));
        }
        if (getSourceLevel() != null) {
            args.add("-sourceLevel");
            args.add(getSourceLevel());
        }
        return args;
    }
}
