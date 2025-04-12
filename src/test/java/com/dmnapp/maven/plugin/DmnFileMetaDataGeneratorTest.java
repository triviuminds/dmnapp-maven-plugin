package com.dmnapp.maven.plugin;

import com.dmnapp.maven.plugin.model.DmnFileMetadata;
import org.apache.maven.plugin.logging.Log;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class DmnFileMetaDataGeneratorTest {

    Path resourcesPath = new File(System.getProperty("user.dir"), "src/test/resources").toPath();

    @TempDir
    Path tempDir;

    @Mock
    private Log log;

    private DmnFileMetaDataGenerator generator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        generator = new DmnFileMetaDataGenerator(log);
    }

    @Test
    void testParseDmnWithNestedValidFile() throws IOException, ParserConfigurationException, SAXException {
        
        Path dmnPath = resourcesPath.resolve("com/triviuminds/dmn/messages.dmn");
        // Test the method
        DmnFileMetadata metadata = generator.parseDmn(resourcesPath, dmnPath);
        
        // Verify the results
        assertNotNull(metadata);
        assertEquals("messages", metadata.getName());
        assertEquals("com.triviuminds.dmn", metadata.getNamespace());
        assertEquals("/com/triviuminds/dmn", metadata.getRelativePath());
        
        // Verify that log methods were called
        verify(log, atLeastOnce()).debug(anyString());
        verify(log, atLeastOnce()).info(anyString());
    }

    @Test
    void testParseDmnWithValidFile() throws IOException, ParserConfigurationException, SAXException {
        
        Path dmnPath = resourcesPath.resolve("sum.dmn");
        // Test the method
        DmnFileMetadata metadata = generator.parseDmn(resourcesPath, dmnPath);
        
        // Verify the results
        assertNotNull(metadata);
        assertEquals("sum", metadata.getName());
        assertEquals("https://kiegroup.org/dmn/_D5C09393-48EC-41FB-AA30-ED6C0D2F5987", metadata.getNamespace());
        assertEquals("/", metadata.getRelativePath());
        
        // Verify that log methods were called
        verify(log, atLeastOnce()).debug(anyString());
        verify(log, atLeastOnce()).info(anyString());
    }
    
    @Test
    void testParseDmnWithInvalidFile() throws IOException {
        // Create a resources directory and an invalid DMN file
        Path resourcesPath = tempDir.resolve("src/main/resources");
        Path dmnPath = resourcesPath.resolve("invalid.dmn");
        
        // Create directories
        Files.createDirectories(dmnPath.getParent());
        
        // Create an invalid DMN file content
        String invalidContent = "This is not a valid XML file";
        
        // Write the content to the file
        Files.writeString(dmnPath, invalidContent);
        
        // Test the method and expect an exception
        assertThrows(SAXException.class, () -> {
            generator.parseDmn(resourcesPath, dmnPath);
        });
    }
    
    @Test
    void testParseDmnWithNullFile() {
        // Test with null file path
        assertThrows(IllegalArgumentException.class, () -> {
            generator.parseDmn(tempDir, null);
        });
        
        // Verify that log.error was called
        verify(log, times(1)).error(anyString());
    }
    
    @Test
    void testParseDmnWithNoNamespace() throws IOException, ParserConfigurationException, SAXException {
        Path dmnPath = resourcesPath.resolve("no-namespace.dmn");
        
        // Test the method
        DmnFileMetadata metadata = generator.parseDmn(resourcesPath, dmnPath);
        
        // Verify the results
        assertNotNull(metadata);
        assertEquals("no-namespace", metadata.getName());
        assertNull(metadata.getNamespace());
        assertEquals("/", metadata.getRelativePath());
        
        // Verify that log.warn was called
        verify(log, atLeastOnce()).warn(anyString());
    }
} 