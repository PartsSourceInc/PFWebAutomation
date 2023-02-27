package core.practitest.model.instance;

import lombok.Builder;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.List;

@Builder
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonPropertyOrder({
        "data"
})
public class InstanceModelList {

    @JsonProperty("data")
    private List<InstanceData> data;

    /**
     * No args constructor for use in serialization
     *
     */
    public InstanceModelList() {
    }

    /**
     *
     * @param data
     */
    public InstanceModelList(List<InstanceData> data) {
        super();
        this.data = data;
    }

    @JsonProperty("data")
    public List<InstanceData> getData() {
        return data;
    }

    @JsonProperty("data")
    public void setData(List<InstanceData> data) {
        this.data = data;
    }

}