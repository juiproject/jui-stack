package com.effacy.jui.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.logging.Log;

public interface ICompilerOptions {

    public String getLogLevel();

    public String getStyle();

    public Integer getOptimize();

    public File getWarDir();

    public File getWorkDir();

    public File getDeployDir();

    public File getExtraDir();

    public boolean isDraftCompile();

    public String getLocalWorkers();

    public String getSourceLevel();

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
