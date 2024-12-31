package com.effacy.jui.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.codehaus.plexus.util.xml.Xpp3Dom;

public class ModuleHelper {

    public static List<ModuleData> modules(MavenProject project, MavenSession session, ProjectBuilder projectBuilder) throws MojoExecutionException {
        List<ModuleData> modules = new ArrayList<>();
        if (project == null)
            return modules;
        for (String module : project.getModules()) {
            try {
                File modulePom = new File(project.getBasedir(), module + "/pom.xml");
                if (modulePom.exists()) {
                    ProjectBuildingRequest buildingRequest = session.getProjectBuildingRequest();
                    MavenProject moduleProject = projectBuilder.build(modulePom, buildingRequest).getProject();

                    ModuleData data = new ModuleData(moduleProject.getGroupId(), moduleProject.getArtifactId(), null);
                    modules.add(data);

                    // Add in the sources.
                    moduleProject.getCompileSourceRoots().forEach(r -> data.sources().add(r));
                    moduleProject.getBuildPlugins().forEach(p -> {
                        if ("build-helper-maven-plugin".equals(p.getArtifactId())) {
                            p.getExecutions().forEach(e -> {
                                modules_find((Xpp3Dom) e.getConfiguration(), v -> "source".equals(v.getName()))
                                    .ifPresent(v -> data.sources.add(v.getValue()));
                            });
                        }
                    });
                }
            } catch (ProjectBuildingException e) {
                throw new MojoExecutionException("Error building project for module: " + module, e);
            }
        }
        return modules;
    }

    protected static Optional<Xpp3Dom> modules_find(Xpp3Dom parent, Predicate<Xpp3Dom> matcher) {
        if (parent == null)
            return Optional.empty();
        if (matcher.test(parent))
            return Optional.of(parent);
        for (Xpp3Dom child : parent.getChildren()) {
            Optional<Xpp3Dom> outcome = modules_find(child, matcher);
            if (outcome.isPresent())
                return outcome;
        }
        return Optional.empty();
    }

    public record ModuleData(String groupId, String artefactId, List<String> sources) {
        public ModuleData {
            if (sources == null)
                sources = new ArrayList<>();
        }
        public boolean matches(String path) {
            return path.contains(groupId.replace(".", "/") + "/" + artefactId);
        }
    }
}
