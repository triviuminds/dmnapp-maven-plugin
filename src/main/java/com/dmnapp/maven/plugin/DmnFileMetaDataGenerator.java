package com.dmnapp.maven.plugin;

import com.dmnapp.maven.plugin.model.DmnFileMetadata;
import com.dmnapp.maven.plugin.util.PluginUtility;
import org.apache.maven.plugin.logging.Log;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Generator for DMN application configuration.
 * This class is responsible for parsing DMN files and generating dmn metadata
 */
public class DmnFileMetaDataGenerator {
    private final Log log;

    public DmnFileMetaDataGenerator(Log log) {
        this.log = log;
    }

    /**
     * Parses a DMN file and extracts its metadata.
     *  
     * @param resourcesPath The base resources path
     * @param dmnFile The path to the DMN file to parse
     * @return A DmnFileMetadata object containing the parsed information
     * @throws IOException If there is an error reading the file
     * @throws ParserConfigurationException If there is an error configuring the XML parser
     * @throws SAXException If there is an error parsing the XML
     * @throws IllegalArgumentException If the DMN file path is null
     */
    public DmnFileMetadata parseDmn(Path resourcesPath, Path dmnFile) throws IOException, ParserConfigurationException, SAXException {
        if (dmnFile == null) {
            String errorMsg = "DMN file path cannot be null";
            log.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        log.debug("Starting to parse DMN file: " + dmnFile);
        
        // Read file content
        String content = Files.readString(dmnFile);
        log.debug("Successfully read DMN file content");
        
        // Parse XML document
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(content)));
        document.getDocumentElement().normalize();
        log.debug("Successfully parsed dmn XML document");
        
        // Extract namespace from definitions element
        String namespace = extractNamespace(document);
        log.debug("Extracted namespace: " + (namespace != null ? namespace : "null"));
        
        // Create and return DmnFileMetadata object using builder pattern
        DmnFileMetadata metadata = DmnFileMetadata.builder()
                .name(extractFileNameWithoutExtension(dmnFile))
                .namespace(namespace)
                .relativePath(PluginUtility.getDmnRelativePath(resourcesPath, dmnFile))
                .build();
                
        log.info("Successfully generated metadata for DMN file: " + dmnFile);
        return metadata;
    }
    
    /**
     * Extracts namespace from the definitions element in the DMN document.
     * Tries different possible tag names for the definitions element.
     * 
     * @param document The parsed XML document
     * @return The namespace string if found, null otherwise
     */
    private String extractNamespace(Document document) {
        String[] possibleTagNames = {"definitions", "dmn:definitions", "semantic:definitions"};
        
        for (String tagName : possibleTagNames) {
            log.debug("Trying to find namespace with tag name: " + tagName);
            NodeList nodeList = document.getElementsByTagName(tagName);
            if (nodeList != null && nodeList.getLength() > 0) {
                var namespaceAttr = nodeList.item(0).getAttributes().getNamedItem("namespace");
                if (namespaceAttr != null) {
                    log.debug("Found namespace using tag: " + tagName);
                    return namespaceAttr.getNodeValue();
                }
            }
        }
        log.warn("No namespace found in DMN document");
        return null;
    }

    /**
     * Extracts the filename without extension from a Path object.
     * 
     * @param filePath The path to the file
     * @return The filename without extension
     */
    private String extractFileNameWithoutExtension(Path filePath) {
        String fileName = filePath.getFileName().toString();
        int lastDotIndex = fileName.lastIndexOf(".");
        return lastDotIndex > 0 ? fileName.substring(0, lastDotIndex) : fileName;
    }
} 