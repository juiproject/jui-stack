package com.effacy.jui.maven;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;

/**
 * Checks that the code server classpath only drops the project's own build
 * output, retaining the build output of sibling modules that are referenced as
 * sources for their rebind classes (JUI-12). Previously any path containing
 * {@code target/classes} was removed, so the code server could not find rebind
 * classes unless an IDE had built them into {@code target-ide/classes}.
 */
public class CodeServerClasspathTest {

    private static final File BASEDIR = new File("/work/app/web");

    @Test
    public void removesProjectOutput() throws Exception {
        CodeServerMojo mojo = mojo();
        List<String> cp = new ArrayList<>(List.of(
            "/work/app/web/src/jui/java",
            "/work/app/web/target/classes",
            "/work/app/core/target/classes"
        ));

        mojo.removeProjectOutput(cp);

        assertEquals(List.of("/work/app/web/src/jui/java", "/work/app/core/target/classes"), cp);
    }

    @Test
    public void removesProjectOutput_unnormalised() throws Exception {
        CodeServerMojo mojo = mojo();
        List<String> cp = new ArrayList<>(List.of(
            "/work/app/web/../web/target/classes",
            "/work/app/web/../core/target/classes"
        ));

        mojo.removeProjectOutput(cp);

        assertEquals(List.of("/work/app/web/../core/target/classes"), cp);
    }

    private CodeServerMojo mojo() throws Exception {
        MavenProject project = new MavenProject();
        project.setFile(new File(BASEDIR, "pom.xml"));
        project.getBuild().setOutputDirectory(new File(BASEDIR, "target/classes").getAbsolutePath());
        CodeServerMojo mojo = new CodeServerMojo();
        Field field = CodeServerMojo.class.getDeclaredField("project");
        field.setAccessible(true);
        field.set(mojo, project);
        return mojo;
    }
}
