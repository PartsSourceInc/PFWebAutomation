package core.practitest.model.instance;

import lombok.Builder;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@Builder
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonPropertyOrder({
        "data"
})
public class InstanceModel {

    @JsonProperty("data")
    private InstanceData data;

    /**
     * No args constructor for use in serialization
     */
    public InstanceModel() {
    }

    /**
     * @param data
     */
    public InstanceModel(InstanceData data) {
        super();
        this.data = data;
    }

    @JsonProperty("data")
    public InstanceData getData() {
        return data;
    }

    @JsonProperty("data")
    public void setData(InstanceData data) {
        this.data = data;
    }

}