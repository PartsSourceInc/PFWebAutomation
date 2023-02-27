package core.practitest;

import core.practitest.model.runs.AttachmentFile;
import lombok.Builder;

import java.util.List;
import java.util.Optional;

@Builder
public class TestStep {

    public final String name;
    public final String description;
    public final String expectedResults;
    public final String actualResults;
    public final Status status;
    public final List<AttachmentFile> attachments;
    public final Long start;
    public final Long stop;

    public Optional<List<AttachmentFile>> getOptionalAttachments() {
        return Optional.ofNullable(attachments);
    }

    public enum Status {
        PASSED("PASSED"),
        FAILED("FAILED"),
        BLOCKED("BLOCKED"),
        N_A("N/A"),
        NO_RUN("NO_RUN"),
        ;

        public final String label;

        Status(String label) {
            this.label = label;
        }
    }
}
