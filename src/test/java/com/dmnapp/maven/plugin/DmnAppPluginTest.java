package com.dmnapp.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;

class DmnAppPluginTest {

    Path testResourcesPath = new File(System.getProperty("user.dir"), "src/test/resources").toPath();

    @TempDir
    Path tempDir;

    @Mock
    private MavenProject project;

    @Mock
    private Log log;

    private DmnAppPlugin plugin;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        // Set up the plugin
        plugin = new DmnAppPlugin();
        
        // Use reflection to set the mocked project and log
        java.lang.reflect.Field projectField = DmnAppPlugin.class.getDeclaredField("project");
        projectField.setAccessible(true);
        projectField.set(plugin, project);
        
        // Set the log directly since it's inherited from AbstractMojo
        plugin.setLog(log);
        
        // Mock the project behavior
        when(project.getBasedir()).thenReturn(tempDir.toFile());
        when(project.getArtifactId()).thenReturn("sample-dmnapp");
        when(project.getVersion()).thenReturn("1.0.0");

        java.lang.reflect.Field dmnSpec = DmnAppPlugin.class.getDeclaredField("dmnSpec");
        dmnSpec.setAccessible(true);
        dmnSpec.set(plugin,  "1.5");

        java.lang.reflect.Field apiSpecName = DmnAppPlugin.class.getDeclaredField("apiSpecName");
        apiSpecName.setAccessible(true);
        apiSpecName.set(plugin, "openapi.json");

    }

    @Test
    void testExecuteWithNoResourcesDirectory() throws MojoExecutionException {
        // Test when resources directory doesn't exist
        when(project.getBasedir()).thenReturn(tempDir.toFile());
        
        // Execute the plugin
        plugin.execute();
        
        // Verify that the plugin logged a debug message
        verify(log, atLeastOnce()).debug(contains("Resources directory not found"));
    }
    
    @Test
    void testExecuteWithEmptyResourcesDirectory() throws IOException, MojoExecutionException {
        // Create an empty resources directory
        Path resourcesDir = tempDir.resolve("src/main/resources");
        Path targetDir = tempDir.resolve("target/classes");
        Files.createDirectories(resourcesDir);
        Files.createDirectories(targetDir);
        // Execute the plugin
        plugin.execute();
        
        // Verify that the plugin logged a warning about no DMN files
        verify(log, atLeastOnce()).warn(contains("No DMN files found"));
    }
    
    @Test
    void testExecuteWithValidDmnFiles() throws IOException, MojoExecutionException, ParserConfigurationException, SAXException {
        // Create a resources directory with a valid DMN file
        Path resourcesDir = tempDir.resolve("src/main/resources");
        Files.createDirectories(resourcesDir);
        
        // Create a valid DMN file
        Path dmnPath = resourcesDir.resolve("sum.dmn");
        String dmnContent = Files.readString(testResourcesPath.resolve("sum.dmn"));
        Files.writeString(dmnPath, dmnContent);
        
        // Create target directory
        Path targetDir = tempDir.resolve("target/classes");
        Files.createDirectories(targetDir);
        
        // Execute the plugin
        plugin.execute();
        
        // Verify that the plugin created the output file
        Path outputPath = targetDir.resolve("dmnapp.json");
        assertTrue(Files.exists(outputPath));
        
        // Verify the content of the output file
        String outputContent = Files.readString(outputPath);
        System.out.println(outputContent);
        
        assertThat(outputContent, hasJsonPath("$.name", is("sample-dmnapp")));
        assertThat(outputContent, hasJsonPath("$.version", is("1.0.0")));
        assertThat(outputContent, hasJsonPath("$.dmnSpec", is("1.5")));
        assertThat(outputContent, hasJsonPath("$.apiSpec", is("openapi.json")));
        assertThat(outputContent, hasJsonPath("$.dmns[0].name", is("sum")));
        assertThat(outputContent, hasJsonPath("$.dmns[0].namespace", is("https://kiegroup.org/dmn/_D5C09393-48EC-41FB-AA30-ED6C0D2F5987")));
        assertThat(outputContent, hasJsonPath("$.dmns[0].relativePath", is("/")));
        
    }

    @Test
    void testExecuteWithInvalidDmnFile() throws IOException, MojoExecutionException {
        // Create a resources directory with an invalid DMN file
        Path resourcesDir = tempDir.resolve("src/main/resources");
        Files.createDirectories(resourcesDir);

        // Create an invalid DMN file
        Path dmnPath = resourcesDir.resolve("invalid.dmn");
        String invalidContent = "This is not a valid XML file";
        Files.writeString(dmnPath, invalidContent);

        // Create target directory
        Path targetDir = tempDir.resolve("target/classes");
        Files.createDirectories(targetDir);

        // Execute the plugin
        plugin.execute();

        // Verify that the plugin logged a warning about the invalid DMN file
        Path outputPath = targetDir.resolve("dmnapp.json");
        assertTrue(Files.exists(outputPath));

        // Verify the content of the output file
        String outputContent = Files.readString(outputPath);
        System.out.println(outputContent);

        assertThat(outputContent, hasJsonPath("$.name", is("sample-dmnapp")));
        assertThat(outputContent, hasJsonPath("$.version", is("1.0.0")));
        assertThat(outputContent, hasJsonPath("$.dmnSpec", is("1.5")));
        assertThat(outputContent, hasJsonPath("$.apiSpec", is("openapi.json")));
        assertFalse(outputContent.contains("dmns"));
    }
} 