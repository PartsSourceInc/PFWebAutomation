package core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Property {
    ENV("testEnvironment"),
    BROWSER("browser");

    @Getter
    private final String value;
}
