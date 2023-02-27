package core.practitest;

import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor(staticName = "of")
public class PractiTestRequestUrl {
    public String value;
}
