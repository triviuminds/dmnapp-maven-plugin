package com.dmnapp.maven.plugin;

import com.dmnapp.maven.plugin.model.DmnApplicationConfig;
import com.dmnapp.maven.plugin.model.DmnFileMetadata;
import com.dmnapp.maven.plugin.util.PluginUtility;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Mojo(name = "generate-dmnapp",
      requiresDependencyResolution = ResolutionScope.RUNTIME,
        requiresProject = true,
      defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST,
      threadSafe = true)
public class DmnAppPlugin extends AbstractMojo {
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;
    @Parameter(property = "dmn.spec.version", defaultValue = "1.5")
    private String dmnSpec;
    @Parameter(property = "api.spec.name", defaultValue = "openapi.json")
    private String apiSpecName;

    private String outputFile = "dmnapp.json";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DmnFileMetaDataGenerator dmnFileMetaDataGenerator;

    public DmnAppPlugin() {
        this.dmnFileMetaDataGenerator = new DmnFileMetaDataGenerator(getLog());
    }

    @Override
    public void execute() throws MojoExecutionException {
        getLog().info("Starting DMN processing for project: " + project.getArtifactId());
        
        try {
            // Get the resources directory
            File resourcesDir = new File(project.getBasedir(), "src/main/resources");
            if (!resourcesDir.exists()) {
                getLog().debug("Resources directory not found at: " + resourcesDir.getAbsolutePath() + ". Skipping DMN processing.");
                return;
            }
            
            //process dmn files and generate dmnapp.json
            generateDmnapp(resourcesDir);
           
        } catch (Exception e) {
            String errorMessage = "Error processing DMN files: " + e.getMessage();
            getLog().error(errorMessage, e);
            throw new MojoExecutionException(errorMessage, e);
        }
    }

    private void generateDmnapp(File resourcesDir) throws IOException, ParserConfigurationException, SAXException {
        List<DmnFileMetadata> metadataList = new ArrayList<>();
        // Find all DMN files
        List<Path> dmnFiles = PluginUtility.findDmnFiles(resourcesDir.toPath());
        
        if (dmnFiles.isEmpty()) {
            getLog().warn("No DMN files found in " + resourcesDir.getAbsolutePath() + " directory");
        } else {
            getLog().debug("Found " + dmnFiles.size() + " DMN files to process");
            
            // Process DMN files and create metadata
            for (Path dmnFile : dmnFiles) {
                getLog().debug("Processing DMN file: " + dmnFile);
                try {
                    DmnFileMetadata metadata = dmnFileMetaDataGenerator.parseDmn(resourcesDir.toPath(), dmnFile);
                    if (metadata != null) {
                        metadataList.add(metadata);
                        getLog().debug("Successfully processed DMN file: " + dmnFile);
                    } else {
                        getLog().warn("Failed to extract metadata from DMN file: " + dmnFile);
                    }
                } catch (Exception e) {
                    getLog().error("Failed to process DMN file: " + dmnFile, e);
                    // Continue processing other files instead of failing the entire build
                    getLog().warn("Continuing with other DMN files...");
                }
            }
        }
       
        getLog().debug("Using DMN specification version: " + dmnSpec);
        getLog().debug("Using API specification: " + apiSpecName);

        DmnApplicationConfig dmnapp = DmnApplicationConfig.builder()
            .name(project.getArtifactId())
            .version(project.getVersion())
            .dmnSpec(dmnSpec)
            .apiSpec(apiSpecName)
            .dmns(metadataList.size() > 0 ? metadataList : null)
            .build();

        getLog().debug("dmnapp created with " + (metadataList.size() > 0 ? metadataList.size() : "no") + " DMN files");

        writeDmnapp(dmnapp);
    }

    private void writeDmnapp(DmnApplicationConfig dmnapp) throws IOException {
        File targetDir = new File(project.getBasedir(), "target/classes");
        Path outputPath = Paths.get(targetDir.getAbsolutePath(), outputFile);
        
        // Ensure the directory exists
        Files.createDirectories(outputPath.getParent());
        
        getLog().debug("Writing DMN application configuration to: " + outputPath.toAbsolutePath());
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputPath.toFile(), dmnapp);
        getLog().info("'dmnapp.json' successfully generated and written to: " + outputPath.toAbsolutePath());
    }
} 