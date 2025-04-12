package com.dmnapp.maven.plugin.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class PluginUtility {
    
    /**
     * Extracts the dmn relative path from a given resources path and dmn path
     * 
     * @param resourcesPath The base resources path
     * @param dmnPath The DMN file path
     * @return The relative path as a String, or null if the path doesn't contain the resources path
     */
    public static String getDmnRelativePath(Path resourcesPath, Path dmnPath) {
        if (dmnPath == null || resourcesPath == null) {
            return null;
        }
        
        String dmnPathStr = dmnPath.toString();
        String resourcesPathStr = resourcesPath.toString();
        
        int startIndex = dmnPathStr.indexOf(resourcesPathStr); 
        if(startIndex == -1) {
            return null;
        }
        startIndex = resourcesPathStr.length(); 
        
        // Get the rest of the path
        dmnPathStr = dmnPathStr.substring(startIndex, dmnPathStr.lastIndexOf(File.separator));
        return dmnPathStr.isEmpty() ? File.separator : dmnPathStr;
    }

    /**
     * Finds all DMN files in a given directory.
     * 
     * @param directory The directory to search for DMN files
     * @return A list of paths to all DMN files in the directory
     * @throws IOException If an I/O error occurs while searching for DMN files
     */
    public static List<Path> findDmnFiles(Path directory) throws IOException {
        return Files.walk(directory)
                .filter(path -> path.toString().endsWith(".dmn"))
                .collect(Collectors.toList());
    }
}
