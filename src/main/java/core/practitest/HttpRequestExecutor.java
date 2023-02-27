package core.practitest;

import core.logging.LoggingFactory;
import lombok.SneakyThrows;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * Gateway class for HTTP request execution.
 * This class encapsulates an external HTTP library, so as to isolate the project code
 * and allow unit testing where static/final methods are used.
 */
public class HttpRequestExecutor {

    private static HttpRequestExecutor httpRequestExecutor;

    private final Logger logger = LoggingFactory.getLogger();

    private HttpRequestExecutor() {
    }

    public static HttpRequestExecutor createInstance() {
        if (httpRequestExecutor == null) {
            httpRequestExecutor = new HttpRequestExecutor();
        }
        return httpRequestExecutor;
    }

    String executePostAndReturnResult(
            String requestUrl,
            String postRequestData) {
        String result = null;
        try {
            result = doExecutePostAndReturnResult(requestUrl, postRequestData);
        } catch (IOException e) {
            logger.debug("Error submitting POST request", e);
            logger.debug(String.format("URL: '%s'", requestUrl));
            logger.debug("=== Post Request Data ===");
            logger.debug(postRequestData);
            logger.debug("=========================");
            throw new RuntimeException("Error submitting POST request", e);
        }
        return result;
    }

    private String doExecutePostAndReturnResult(
            String requestUrl,
            String postRequestData)
            throws IOException {
        return Request
                .Post(requestUrl)
                .setHeader("Authorization",
                        "Basic bWthbGlub3ZAcGFydHNzb3VyY2UuY29tOjhhZTc0ZTlmZTkzMmY1Y2M2ODAyZWNmZjVmNWQ5YWEyMzVhZWNjN2Y=")
                .addHeader("Content-Type", "application/json")
                .body(new StringEntity(postRequestData))
                .execute()
                .handleResponse(new BasicResponseHandler());
    }

    @SneakyThrows
    String executeGetAndReturnResult(String requestUrl) {
        String stringResponse = Request
                .Get(requestUrl)
                .setHeader("Authorization",
                        "Basic bWthbGlub3ZAcGFydHNzb3VyY2UuY29tOjhhZTc0ZTlmZTkzMmY1Y2M2ODAyZWNmZjVmNWQ5YWEyMzVhZWNjN2Y=")
                .addHeader("Content-Type", "application/json")
                .execute()
                .handleResponse(new BasicResponseHandler());
        return stringResponse;
    }
}
