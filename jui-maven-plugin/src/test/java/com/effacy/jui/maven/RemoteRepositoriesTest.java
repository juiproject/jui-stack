package com.effacy.jui.maven;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.eclipse.aether.repository.RemoteRepository;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Checks that the artifacts the plugin resolves itself (gwt-dev for
 * {@code jui:compile}, the code server for {@code jui:codeserver}) are resolved
 * against the build's remote repositories (JUI-7). Newer Maven resolvers reject
 * a cached artifact that did not come from one of the request's repositories,
 * so a request without them fails whatever the local repository holds. Maven
 * 3.9 does not, so building a project with the plugin does not catch this.
 */
public class RemoteRepositoriesTest {

    private static final List<RemoteRepository> REPOSITORIES = List.of(
        new RemoteRepository.Builder("project-repo", "default", "https://repo.example.com/maven2").build()
    );

    @Test
    public void compile_resolvesGwtDevAgainstRepositories() throws Exception {
        RecordingRepositorySystem repoSystem = new RecordingRepositorySystem();
        CompileMojo mojo = new CompileMojo();
        set(mojo, "repoSystem", repoSystem.proxy());
        set(mojo, "remoteRepositories", REPOSITORIES);
        set(mojo, "gwtVersion", "2.12.0");
        set(mojo, "module", "com.example.App");
        set(mojo, "webappDirectory", new File("target/test-webapp"));
        set(mojo, "workDir", new File("target/test-work"));
        set(mojo, "deploy", new File("target/test-deploy"));

        mojo.configureForGWT(new ArrayList<>());

        assertEquals(1, repoSystem.dependencyRequests.size());
        assertEquals("gwt-dev", repoSystem.dependencyRequests.get(0).getCollectRequest().getRoot().getArtifact().getArtifactId());
        assertEquals("2.12.0", repoSystem.dependencyRequests.get(0).getCollectRequest().getRoot().getArtifact().getVersion());
        assertEquals(REPOSITORIES, repoSystem.dependencyRequests.get(0).getCollectRequest().getRepositories());
    }

    @Test
    public void codeServer_retrievesCodeServerAgainstRepositories() throws Exception {
        RecordingRepositorySystem repoSystem = new RecordingRepositorySystem();
        PluginDescriptor descriptor = new PluginDescriptor();
        descriptor.setGroupId("com.effacy.jui");
        descriptor.setVersion("9.9.9");
        CodeServerMojo mojo = new CodeServerMojo();
        set(mojo, "repoSystem", repoSystem.proxy());
        set(mojo, "remotePluginRepositories", REPOSITORIES);
        set(mojo, "pluginDescriptor", descriptor);

        mojo.retrieveCodeServer();

        assertEquals(1, repoSystem.artifactRequests.size());
        assertEquals("jui-platform-codeserver", repoSystem.artifactRequests.get(0).getArtifact().getArtifactId());
        assertEquals("9.9.9", repoSystem.artifactRequests.get(0).getArtifact().getVersion());
        assertEquals(REPOSITORIES, repoSystem.artifactRequests.get(0).getRepositories());
    }

    @Test
    public void helper_resolvesAgainstRepositories() throws Exception {
        RecordingRepositorySystem repoSystem = new RecordingRepositorySystem();

        ArtifactsAsResourcesHelper.resolve(null, repoSystem.proxy(), null, REPOSITORIES, "org.example", "example", "1.0");

        assertEquals(1, repoSystem.dependencyRequests.size());
        assertEquals(REPOSITORIES, repoSystem.dependencyRequests.get(0).getCollectRequest().getRepositories());
    }

    /**
     * The repositories passed to the mojos must be the build's: the generated
     * plugin descriptor binds them to the project's repositories (for gwt-dev)
     * and plugin repositories (for the code server, released with the plugin).
     */
    @Test
    public void descriptor_bindsBuildRepositories() throws Exception {
        assertEquals("${project.remoteProjectRepositories}", defaultValue("compile", "remoteRepositories"));
        assertEquals("${project.remotePluginRepositories}", defaultValue("codeserver", "remotePluginRepositories"));
    }

    private static String defaultValue(String goal, String parameter) throws Exception {
        Document doc;
        try (InputStream in = RemoteRepositoriesTest.class.getResourceAsStream("/META-INF/maven/plugin.xml")) {
            assertNotNull(in, "plugin descriptor not generated");
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
        }
        NodeList mojos = doc.getElementsByTagName("mojo");
        for (int i = 0; i < mojos.getLength(); i++) {
            Element mojo = (Element) mojos.item(i);
            if (!goal.equals(text(mojo, "goal")))
                continue;
            NodeList configuration = ((Element) mojo.getElementsByTagName("configuration").item(0)).getElementsByTagName(parameter);
            assertTrue(configuration.getLength() > 0, "no configuration for " + goal + "/" + parameter);
            return ((Element) configuration.item(0)).getAttribute("default-value");
        }
        throw new AssertionError("no goal " + goal);
    }

    private static String text(Element parent, String child) {
        NodeList nodes = parent.getElementsByTagName(child);
        return (nodes.getLength() == 0) ? null : nodes.item(0).getTextContent();
    }

    private static void set(Object target, String name, Object value) throws Exception {
        Class<?> type = target.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                type = type.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }
}
