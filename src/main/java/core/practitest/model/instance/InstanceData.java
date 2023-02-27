package core.practitest.model.instance;

import lombok.Builder;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@Builder
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonPropertyOrder({
        "attributes"
})
public class InstanceData {

    @JsonProperty("attributes")
    private InstanceAttributes attributes;

    /**
     * No args constructor for use in serialization
     */
    public InstanceData() {
    }

    /**
     * @param attributes
     */
    public InstanceData(InstanceAttributes attributes) {
        super();
        this.attributes = attributes;
    }

    @JsonProperty("attributes")
    public InstanceAttributes getAttributes() {
        return attributes;
    }

    @JsonProperty("attributes")
    public void setAttributes(InstanceAttributes attributes) {
        this.attributes = attributes;
    }

}
