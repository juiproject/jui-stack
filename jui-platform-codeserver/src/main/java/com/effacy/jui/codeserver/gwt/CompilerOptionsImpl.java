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

import java.io.File;
import java.util.List;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.dev.CompilerOptions;
import com.google.gwt.dev.cfg.Properties;
import com.google.gwt.dev.jjs.JsOutputOption;
import com.google.gwt.dev.js.JsNamespaceOption;
import com.google.gwt.dev.util.arg.OptionMethodNameDisplayMode;
import com.google.gwt.dev.util.arg.OptionOptimize;
import com.google.gwt.dev.util.arg.SourceLevel;
import com.google.gwt.thirdparty.guava.common.collect.LinkedListMultimap;
import com.google.gwt.thirdparty.guava.common.collect.ListMultimap;
import com.google.gwt.thirdparty.guava.common.collect.Lists;
import com.google.gwt.util.regexfilter.WhitelistRegexFilter;

/**
 * Defines the compiler settings that Super Dev Mode uses for compiling
 * GWT apps. For now, these settings are hard-coded to reasonable defaults.
 */
public class CompilerOptionsImpl implements CompilerOptions {
    private final CompileDir compileDir;
    private final boolean incremental;
    private final boolean failOnError;
    private final TreeLogger.Type logLevel;
    private final List<String> moduleNames;
    private final SourceLevel sourceLevel;
    private final boolean generateJsInteropExports;
    private final WhitelistRegexFilter jsInteropExportFilter;
    private final OptionMethodNameDisplayMode.Mode methodNameDisplayMode;
    private final ListMultimap<String, String> properties;
    private final boolean closureFormattedOutput;
    private final JsOutputOption output;

    public CompilerOptionsImpl(Options options) {
        this (null, null, options);
    }

    CompilerOptionsImpl(CompileDir compileDir, String moduleName, Options options) {
        this.compileDir = compileDir;
        this.incremental = options.isIncrementalCompileEnabled();
        this.moduleNames = Lists.newArrayList(moduleName);
        this.sourceLevel = options.getSourceLevel();
        this.failOnError = options.isFailOnError();
        this.logLevel = options.getLogLevel();
        this.generateJsInteropExports = options.shouldGenerateJsInteropExports();
        this.jsInteropExportFilter = new WhitelistRegexFilter();
        this.jsInteropExportFilter.addAll(options.getJsInteropExportFilter());
        this.methodNameDisplayMode = options.getMethodNameDisplayMode();
        this.properties = LinkedListMultimap.create(options.getProperties());
        this.closureFormattedOutput = options.isClosureFormattedOutput();
        this.output = options.getOutput();
    }

    @Override
    public File getDeployDir() {
        return compileDir.getDeployDir();
    }

    @Override
    public File getExtraDir() {
        return compileDir.getExtraDir();
    }

    @Override
    public Properties getFinalProperties() {
        return null; // handling this in a different way
    }

    @Override
    public int getFragmentCount() {
        return -1;
    }

    @Override
    public int getFragmentsMerge() {
        return -1;
    }

    @Override
    public File getGenDir() {
        return compileDir.getGenDir();
    }

    @Override
    public boolean shouldGenerateJsInteropExports() {
        return generateJsInteropExports;
    }

    @Override
    public WhitelistRegexFilter getJsInteropExportFilter() {
        return jsInteropExportFilter;
    }

    /**
     * Number of threads to use to compile permutations.
     */
    @Override
    public int getLocalWorkers() {
        return 1;
    }

    @Override
    public TreeLogger.Type getLogLevel() {
        return logLevel;
    }

    @Override
    public OptionMethodNameDisplayMode.Mode getMethodNameDisplayMode() {
        return methodNameDisplayMode;
    }

    @Override
    public List<String> getModuleNames() {
        return moduleNames;
    }

    @Override
    public JsNamespaceOption getNamespace() {
        return JsNamespaceOption.PACKAGE;
    }

    @Override
    public int getOptimizationLevel() {
        return OptionOptimize.OPTIMIZE_LEVEL_DRAFT;
    }

    @Override
    public JsOutputOption getOutput() {
        return output;
    }

    @Override
    public ListMultimap<String, String> getProperties() {
        return properties;
    }

    @Override
    public File getSaveSourceOutput() {
        return null;
    }

    @Override
    public SourceLevel getSourceLevel() {
        return sourceLevel;
    }

    @Override
    public String getSourceMapFilePrefix() {
        return Constants.SOURCEROOT_TEMPLATE_VARIABLE;
    }

    @Override
    public File getWarDir() {
        return compileDir.getWarDir();
    }

    @Override
    public File getWorkDir() {
        return compileDir.getWorkDir();
    }

    @Override
    public boolean isClassMetadataDisabled() {
        return false;
    }

    @Override
    public boolean isCompilerMetricsEnabled() {
        return false;
    }

    @Override
    public boolean isEnableAssertions() {
        return true;
    }

    @Override
    public boolean isEnabledGeneratingOnShards() {
        return true;
    }

    @Override
    public boolean isIncrementalCompileEnabled() {
        return incremental;
    }

    @Override
    public  boolean isJsonSoycEnabled() {
        return false;
    }

    @Override
    public boolean isRunAsyncEnabled() {
        return false;
    }

    @Override
    public boolean isSoycEnabled() {
        return false;
    }

    @Override
    public boolean isSoycExtra() {
        return false;
    }

    @Override
    public boolean isSoycHtmlDisabled() {
        return true;
    }

    @Override
    public boolean isStrict() {
        return failOnError;
    }

    @Override
    public boolean isValidateOnly() {
        return false;
    }

    @Override
    public boolean shouldAddRuntimeChecks() {
        // Not needed since no optimizations are on.
        return false;
    }

    @Override
    public boolean shouldClusterSimilarFunctions() {
        return false;
    }

    @Override
    public boolean shouldInlineLiteralParameters() {
        return false;
    }

    @Override
    public boolean shouldJDTInlineCompileTimeConstants() {
        return !isIncrementalCompileEnabled();
    }

    @Override
    public boolean shouldOptimizeDataflow() {
        return false;
    }

    @Override
    public boolean shouldOrdinalizeEnums() {
        return false;
    }

    @Override
    public boolean shouldRemoveDuplicateFunctions() {
        return false;
    }

    @Override
    public boolean shouldSaveSource() {
        return false; // handling this a different way
    }

    @Override
    public boolean useDetailedTypeIds() {
        return false;
    }

    @Override
    public boolean isClosureCompilerFormatEnabled() {
        return closureFormattedOutput;
    }

    
    /************************************************************************
     * Setters are not supported for this.
     ************************************************************************/

    @Override
    public final void addModuleName(String moduleName) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public void setAddRuntimeChecks(boolean enabled) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public final void setClassMetadataDisabled(boolean disabled) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public void setClusterSimilarFunctions(boolean enabled) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public void setIncrementalCompileEnabled(boolean enabled) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public final void setCompilerMetricsEnabled(boolean enabled) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public final void setDeployDir(File dir) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public final void setEnableAssertions(boolean enableAssertions) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public final void setEnabledGeneratingOnShards(boolean allowed) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public final void setExtraDir(File extraDir) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public final void setFinalProperties(Properties finalProperties) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public void setFragmentCount(int numFragments) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public void setFragmentsMerge(int numFragments) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public final void setGenDir(File dir) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public void setInlineLiteralParameters(boolean inlineLiteralParameters) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public void setGenerateJsInteropExports(boolean generateExports) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public void setJsonSoycEnabled(boolean jsonSoycEnabled) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public final void setLocalWorkers(int localWorkers) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public final void setLogLevel(TreeLogger.Type logLevel) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public final void setModuleNames(List<String> moduleNames) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public void setNamespace(JsNamespaceOption newValue) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public final void setOptimizationLevel(int level) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public void setOptimizeDataflow(boolean enabled) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public void setOrdinalizeEnums(boolean enabled) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public final void setOutput(JsOutputOption obfuscated) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public void setRemoveDuplicateFunctions(boolean enabled) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public final void setRunAsyncEnabled(boolean enabled) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public void setSaveSource(boolean enabled) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public void setSaveSourceOutput(File debugDir) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public final void setSourceLevel(SourceLevel sourceLevel) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public void setSourceMapFilePrefix(String path) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public final void setSoycEnabled(boolean enabled) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public final void setSoycExtra(boolean soycExtra) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public final void setSoycHtmlDisabled(boolean disabled) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public final void setStrict(boolean strict) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public void setUseDetailedTypeIds(boolean enabled) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public final void setValidateOnly(boolean validateOnly) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public final void setWarDir(File dir) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public final void setWorkDir(File dir) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public void setMethodNameDisplayMode(OptionMethodNameDisplayMode.Mode methodNameDisplayMode) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public void setPropertyValues(String name, Iterable<String> value) {
        throw new UnsupportedOperationException();
    }
  
  
    @Override
    public void setClosureCompilerFormatEnabled(boolean enabled) {
        throw new UnsupportedOperationException();
    }

}
