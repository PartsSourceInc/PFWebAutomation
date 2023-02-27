package core.practitest.model.instance;

import lombok.Builder;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@Builder
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonPropertyOrder({
        "set-id",
        "test-id"
})
public class InstanceAttributes {

    @JsonProperty("set-id")
    private int setId;
    @JsonProperty("test-id")
    private int testId;

    /**
     * No args constructor for use in serialization
     */
    public InstanceAttributes() {
    }

    /**
     * @param testId
     * @param setId
     */
    public InstanceAttributes(int setId, int testId) {
        super();
        this.setId = setId;
        this.testId = testId;
    }

    @JsonProperty("set-id")
    public int getSetId() {
        return setId;
    }

    @JsonProperty("set-id")
    public void setSetId(int setId) {
        this.setId = setId;
    }

    @JsonProperty("test-id")
    public int getTestId() {
        return testId;
    }

    @JsonProperty("test-id")
    public void setTestId(int testId) {
        this.testId = testId;
    }

}