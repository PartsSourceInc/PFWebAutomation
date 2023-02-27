package core.practitest.model.runs;

import lombok.Builder;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@Builder
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonPropertyOrder({
        "filename",
        "content_encoded"
})
public class AttachmentFile {

    @JsonProperty("filename")
    private String fileName;

    @JsonProperty("content_encoded")
    private String contentEncoded;

    public AttachmentFile(String fileName, String contentEncoded) {
        this.fileName = fileName;
        this.contentEncoded = contentEncoded;
    }

    @JsonProperty("filename")
    public String getFileName() {
        return fileName;
    }

    @JsonProperty("filename")
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @JsonProperty("content_encoded")
    public String getContentEncoded() {
        return contentEncoded;
    }

    @JsonProperty("content_encoded")
    public void setContentEncoded(String contentEncoded) {
        this.contentEncoded = contentEncoded;
    }
}
