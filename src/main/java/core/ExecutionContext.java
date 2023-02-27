package core;

import lombok.NonNull;

public interface ExecutionContext {

    String getTargetEnvironmentUrl();

    String getTargetEnvironmentUrlPrefix();

    default String getFormattedEnvironmentUrl(@NonNull String urlTemplate) {
        var formattedUrl = String.format(
                urlTemplate,
                getTargetEnvironmentUrlPrefix()
        );
        return formattedUrl;
    }
}
