package core.practitest.model.runs;

import lombok.Builder;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@Builder
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonPropertyOrder({
        "instance-id"
})
public class Attributes {

    @JsonProperty("instance-id")
    private int instanceId;

    @JsonProperty("automated-execution-output")
    private String automatedExecutionOutput;

    @JsonProperty("run-duration")
    private String runDuration;

    /**
     * No args constructor for use in serialization
     */
    public Attributes() {
    }

    public Attributes(int instanceId, String automatedExecutionOutput) {
        super();
        this.instanceId = instanceId;
        this.automatedExecutionOutput = automatedExecutionOutput;
    }

    public Attributes(final int instanceId, final String automatedExecutionOutput, final String runDuration) {
        this.instanceId = instanceId;
        this.automatedExecutionOutput = automatedExecutionOutput;
        this.runDuration = runDuration;
    }

    /**
     * @param instanceId
     */

    public Attributes(int instanceId) {
        super();
        this.instanceId = instanceId;
    }

    public int getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(final int instanceId) {
        this.instanceId = instanceId;
    }

    public String getRunDuration() {
        return runDuration;
    }

    public void setRunDuration(final String runDuration) {
        this.runDuration = runDuration;
    }

    @JsonProperty("automated-execution-output")
    public String getAutomatedExecutionOutput() {
        return automatedExecutionOutput;
    }

    @JsonProperty("automated-execution-output")
    public void setAutomatedExecutionOutput(String automatedExecutionOutput) {
        this.automatedExecutionOutput = automatedExecutionOutput;
    }

    @JsonProperty("instance-id")
    public int getInstanceid() {
        return instanceId;
    }

    @JsonProperty("instance-id")
    public void setInstanceid(int instanceId) {
        this.instanceId = instanceId;
    }

}
