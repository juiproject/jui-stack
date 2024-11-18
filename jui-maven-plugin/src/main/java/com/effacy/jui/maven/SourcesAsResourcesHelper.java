package com.effacy.jui.maven;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.logging.Log;

public class SourcesAsResourcesHelper {
    
    public static List<String> filterSourceRoots(Log log, Iterable<Resource> resources, Iterable<String> sourceRoots) {
        Set<String> resourceDirs = _prepareResources(resources);
        List<String> filteredSourceRoots = new ArrayList<>();
        for (String sourceRoot : sourceRoots) {
            if (_checkSourceRoot(log, resourceDirs, sourceRoot))
                filteredSourceRoots.add(sourceRoot);
        }
        return filteredSourceRoots;
    }

    public static String ensureTrailingSlash(String directory) {
        if (directory.endsWith("/"))
            return directory;
        return directory + "/";
    }

    /************************************************************************
     * Supporting methods.
     ************************************************************************/

    private static Set<String> _prepareResources(Iterable<Resource> resources) {
        Set<String> resourceDirs = new LinkedHashSet<>();
        for (Resource resource : resources)
            resourceDirs.add(ensureTrailingSlash(resource.getDirectory()));
        return resourceDirs;
    }

    private static boolean _checkSourceRoot(Log log, Set<String> resourceDirs, String sourceRoot) {
        sourceRoot = ensureTrailingSlash(sourceRoot);
        for (String resourceDir : resourceDirs) {
            if (resourceDir.equals(sourceRoot)) {
                log.info(sourceRoot + " already added as a resource folder; skipping.");
                continue;
            }
            if (resourceDir.startsWith(sourceRoot) || sourceRoot.startsWith(resourceDir)) {
                log.warn(String.format(
                    "Conflicting path between source folder (%s, to be added as resource) and resource (%s); skipping.",
                    sourceRoot, resourceDir));
                return false;
            }
        }
        return true;
    }
}
