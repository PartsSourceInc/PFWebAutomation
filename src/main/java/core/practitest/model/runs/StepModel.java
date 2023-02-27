package core.practitest.model.runs;

import lombok.Builder;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@Builder
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonPropertyOrder({
        "name",
        "description",
        "status"
})
public class StepModel {

    @JsonProperty("name")
    private String name;
    @JsonProperty("expected-results")
    private String expectedResults;
    @JsonProperty("description")
    private String description;
    @JsonProperty("status")
    private String status;
    @JsonProperty("files")
    private Files files;

    public StepModel(final String name,
                     final String description,
                     final String status,
                     final Files files) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.files = files;
    }

    public StepModel(final String name,
                     final String description,
                     final String expectedResults,
                     final String status,
                     final Files files) {
        this.name = name;
        this.description = description;
        this.expectedResults = expectedResults;
        this.status = status;
        this.files = files;
    }

    public StepModel(final String name,
                     final String status,
                     final Files files) {
        this.name = name;
        this.status = status;
        this.files = files;
    }

    /**
     * No args constructor for use in serialization
     */

    public StepModel() {
    }

    /**
     * @param status
     * @param name
     */
    public StepModel(String name, String status) {
        this.name = name;
        this.status = status;
    }

    public Files getFiles() {
        return files;
    }

    public void setFiles(final Files files) {
        this.files = files;
    }

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(final String description) {
        this.description = description;
    }
}
