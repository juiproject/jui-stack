package com.effacy.jui.maven;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;

public class ArtifactsAsResourcesHelper {

    public static List<String> resolve(Log log, RepositorySystem repoSystem, RepositorySystemSession repoSession, String groupId, String artifactId, String version) throws MojoExecutionException {
        return resolve(log, repoSystem, repoSession, groupId, artifactId, version, null);
    }

    public static List<String> resolve(Log log, RepositorySystem repoSystem, RepositorySystemSession repoSession, String groupId, String artifactId, String version, String classifier) throws MojoExecutionException {
        try {
            List<String> cp = new ArrayList<>();
            DefaultArtifact artifactToResolve = (classifier == null) ? new DefaultArtifact(groupId + ":" + artifactId + ":" + version) : new DefaultArtifact(groupId, artifactId, classifier, "jar", version);
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
                    if (log.isDebugEnabled())
                        log.debug("GWT compiler dependency ADDED: " + path);
                    cp.add (path);
                } else if (log.isDebugEnabled())
                    log.debug("GWT compiler dependency EXCLUDED: " + path);
            }
            return cp;
        } catch (Throwable e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    public static List<String> retrieve(Log log, RepositorySystem repoSystem, RepositorySystemSession repoSession, String groupId, String artifactId, String version, String classifier) throws MojoExecutionException {
        try {
            List<String> cp = new ArrayList<>();
            DefaultArtifact artifactToResolve = (classifier == null) ? new DefaultArtifact(groupId + ":" + artifactId + ":" + version) : new DefaultArtifact(groupId, artifactId, classifier, "jar", version);
            ArtifactRequest request = new ArtifactRequest();
            request.setArtifact(artifactToResolve);
            List<ArtifactRequest> requests = new ArrayList<>();
            requests.add (request);
            List<ArtifactResult> resolvedArtifacts = repoSystem.resolveArtifacts(repoSession, requests);
            for (ArtifactResult result : resolvedArtifacts)
                cp.add (result.getArtifact().getFile().getAbsolutePath());
            return cp;
        } catch (Throwable e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
}
