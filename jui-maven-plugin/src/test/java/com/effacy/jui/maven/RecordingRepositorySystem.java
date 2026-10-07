package com.effacy.jui.maven;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResult;

/**
 * A {@link RepositorySystem} that records the resolution requests made of it
 * and resolves nothing.
 */
class RecordingRepositorySystem {

    final List<DependencyRequest> dependencyRequests = new ArrayList<>();

    final List<ArtifactRequest> artifactRequests = new ArrayList<>();

    @SuppressWarnings("unchecked")
    RepositorySystem proxy() {
        return (RepositorySystem) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { RepositorySystem.class }, (p, method, args) -> {
            if ("resolveDependencies".equals(method.getName())) {
                DependencyRequest request = (DependencyRequest) args[1];
                dependencyRequests.add(request);
                DependencyResult result = new DependencyResult(request);
                result.setArtifactResults(new ArrayList<>());
                return result;
            }
            if ("resolveArtifacts".equals(method.getName())) {
                artifactRequests.addAll((List<ArtifactRequest>) args[1]);
                return new ArrayList<>();
            }
            throw new UnsupportedOperationException(method.getName());
        });
    }
}
