package com.dmnapp.maven.plugin.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({"name", "version", "dmnSpec", "apiSpec", "dmns"})
@Builder
@Data
public class DmnApplicationConfig {
    @JsonProperty("name")
    private String name;
    @JsonProperty("version")
    private String version;
    @JsonProperty("dmnSpec")
    private String dmnSpec;
    @JsonProperty("apiSpec")
    private String apiSpec;
    @JsonProperty("dmns")
    private List<DmnFileMetadata> dmns;
}
