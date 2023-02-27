package core.practitest.model.runs;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonPropertyOrder({
        "type",
        "attributes",
        "files",
        "steps"
})
public class Data {

    @JsonProperty("type")
    private String type;

    @JsonProperty("attributes")
    private Attributes attributes;

    @JsonProperty("files")
    private Files files;

    @JsonProperty("steps")
    private Steps steps;

    /**
     * No args constructor for use in serialization
     */
    public Data() {
    }

    /**
     * @param steps
     * @param attributes
     */
    public Data(String type, Attributes attributes, Steps steps) {
        super();
        this.type = "instances";
        this.attributes = attributes;
        this.steps = steps;
    }

    public Data(String type,
                Attributes attributes,
                Files files,
                Steps steps) {
        this.type = "instances";
        this.attributes = attributes;
        this.files = files;
        this.steps = steps;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("attributes")
    public Attributes getAttributes() {
        return attributes;
    }

    @JsonProperty("attributes")
    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    @JsonProperty("steps")
    public Steps getSteps() {
        return steps;
    }

    @JsonProperty("steps")
    public void setSteps(Steps steps) {
        this.steps = steps;
    }

    @JsonProperty("files")
    public Files getFiles() {
        return files;
    }

    @JsonProperty("files")
    public void setFiles(Files files) {
        this.files = files;
    }
}