package core.practitest.model.runs;

import lombok.Builder;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.List;

@Builder
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Files {

    @JsonProperty("data")
    private List<AttachmentFile> filesData;

    public Files(List<AttachmentFile> filesData) {
        this.filesData = filesData;
    }

    @JsonProperty("data")
    public List<AttachmentFile> getFilesData() {
        return filesData;
    }

    @JsonProperty("data")
    public void setFilesData(List<AttachmentFile> filesData) {
        this.filesData = filesData;
    }
}