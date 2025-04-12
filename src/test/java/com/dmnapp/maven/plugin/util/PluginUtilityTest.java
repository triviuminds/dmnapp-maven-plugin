package com.dmnapp.maven.plugin.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PluginUtilityTest {

    @TempDir
    Path tempDir;

    Path resourcesPath = new File(System.getProperty("user.dir"), "src/test/resources").toPath();

    @Test
    void testGetDmnRelativePath() {
        Path dmnPath = resourcesPath.resolve("com/triviuminds/dmn/messages.dmn"); 
        // Test the method
        String relativePath = PluginUtility.getDmnRelativePath(resourcesPath, dmnPath);
        
        // Verify the result
        assertEquals("/com/triviuminds/dmn", relativePath);
    }
    
    @Test
    void testGetDmnRelativePathWithNullInputs() {
        // Test with null resources path
        assertNull(PluginUtility.getDmnRelativePath(null, Path.of("test.dmn")));
        
        // Test with null dmn path
        assertNull(PluginUtility.getDmnRelativePath(Path.of("resources"), null));
        
        // Test with both null
        assertNull(PluginUtility.getDmnRelativePath(null, null));
    }
    
    @Test
    void testGetDmnRelativePathWithNonMatchingPaths() {
        Path resourcesPath = Path.of("/resources");
        Path dmnPath = Path.of("/other/path/test.dmn");
        
        // The dmn path doesn't contain the resources path
        assertNull(PluginUtility.getDmnRelativePath(resourcesPath, dmnPath));
    }
    
    @Test
    void testFindDmnFiles() throws IOException {
            // Test the method
        List<Path> dmnFiles = PluginUtility.findDmnFiles(resourcesPath);
        
        // Verify the results
        assertEquals(3, dmnFiles.size());
        assertTrue(dmnFiles.stream().anyMatch(path -> path.toString().contains("sum")));
        assertTrue(dmnFiles.stream().anyMatch(path -> path.toString().contains("messages")));
        assertTrue(dmnFiles.stream().anyMatch(path -> path.toString().contains("no-namespace")));
    }
    
    @Test
    void testFindDmnFilesWithEmptyDirectory() throws IOException {
        // Create an empty directory
        Path emptyDir = tempDir.resolve("empty");
        Files.createDirectory(emptyDir);
        
        // Test the method
        List<Path> dmnFiles = PluginUtility.findDmnFiles(emptyDir);
        
        // Verify the results
        assertTrue(dmnFiles.isEmpty());
    }
} 