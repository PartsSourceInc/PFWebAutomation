package core.report;

import core.logging.LoggingFactory;
import io.qameta.allure.Attachment;
import io.qameta.allure.listener.StepLifecycleListener;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StatusDetails;
import io.qameta.allure.model.StepResult;
import io.vavr.Tuple2;
import lombok.SneakyThrows;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import ru.yandex.qatools.ashot.AShot;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ApolloStepListener implements StepLifecycleListener {

    public static ThreadLocal<WebDriver> webDriver = new ThreadLocal<>();
    public static boolean SHOULD_ENABLE_DEV_MODE = false;
    private final Logger logger = LoggingFactory.getLogger(ApolloStepListener.class.getName());

    @Override
    public void afterStepStart(StepResult result) {
        logger.debug(String.format(
                "STEP.afterStepStart: %s",
                result
                        .getName()
                        .toUpperCase()));
    }

    @Override
    public void beforeStepStart(final StepResult result) {
        logger.debug("");
        logger.debug(String.format(
                "STEP.beforeStepStart: %s",
                result
                        .getName()
                        .toUpperCase()));
        logger.debug("===============================================");
        logger.debug("");
    }

    @Override
    public void afterStepUpdate(StepResult result) {

        logger.debug(String.format(
                "STEP.afterStepUpdate: %s",
                result
                        .getName()
                        .toUpperCase()));

        final Status status = result.getStatus();

        try {
            if (isTestFailedOrBroken(status)) {
                logger.debug(String.format("Step FAILED: %s", result.getName()));
                takeFailureScreenShotApolloStepListener(webDriver.get());
                final StatusDetails statusDetails = result.getStatusDetails();
                logFailureMessage(statusDetails);
            }
        } catch (Exception e) {
        }

        final var resultText = status != null ?
                status
                        .value()
                        .toUpperCase() :
                "N/A";

        logger.debug("");
        logger.debug("===============================================");
        logger.debug(String.format("RESULT: %s", resultText));
        result
                .getAttachments()
                .stream()
                .map(attachment -> new Tuple2<>(attachment.getType(), attachment.getName()))
                .forEach(element -> {
                    var logEntry = String.format(" > ATTACHMENT (%s): '%s'", element._1, element._2);
                    logger.debug(logEntry);
                });

        logger.debug("");
    }

//  private void logBrowserConsoleErrors() {
//    final LogEntries browserLogEntries = webDriver
//      .manage()
//      .logs()
//      .get(LogType.BROWSER);
//
//    final List<LogEntry> browserLogEntryList = browserLogEntries.getAll();
//
//    final List<String> logEntries = browserLogEntryList
//      .stream()
//      .map(LogEntry::getMessage)
//      .collect(Collectors.toList());
//
//    if (!logEntries.isEmpty()) {
//      final String errorListContent = logEntries
//        .stream()
//        .map(entry -> entry + "\n")
//        .reduce("", (entry1, entry2) -> entry1 + entry2);
//      addFailureText("browser_logs", errorListContent);
//    }
//  }

    private void logFailureMessage(final StatusDetails statusDetails) {
        final String message = statusDetails.getMessage();
        addFailureText(message);

        final String trace = statusDetails.getTrace();
        addFailureText(trace);
    }

    private boolean isTestFailedOrBroken(final Status status) {
        return status == Status.FAILED || status == Status.BROKEN;
    }

    @Attachment(value = "{failureMessageName}", fileExtension = "txt", type = "text/plain")
    private String addFailureText(final String failureMessageContent) {
        return failureMessageContent;
    }

    @SneakyThrows
    @Attachment(value = "failure_screenshot", fileExtension = "png", type = "image/png")
    private byte[] takeFailureScreenShotApolloStepListener(WebDriver driver) {

        byte[] screenshotBytes;

        if (SHOULD_ENABLE_DEV_MODE) {
            screenshotBytes = takeFullBrowserWindowScreenshot(driver);
        } else {
            screenshotBytes = takeWebApplicationContentScreenshot(driver);
        }

        return screenshotBytes;
    }

    private byte[] takeFullBrowserWindowScreenshot(final WebDriver driver) throws AWTException, IOException {
        final byte[] screenshotBytes;
        final var point = driver
                .manage()
                .window()
                .getPosition();
        final var size = driver
                .manage()
                .window()
                .getSize();
        var captureRect = new Rectangle(point.getX(), point.getY(), size.getWidth(), size.getHeight());
        var screenFullImage = new Robot().createScreenCapture(captureRect);
        var byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(screenFullImage, "png", byteArrayOutputStream);
        screenshotBytes = byteArrayOutputStream.toByteArray();
        return screenshotBytes;
    }

    private byte[] takeWebApplicationContentScreenshot(final WebDriver driver) throws IOException {
        final byte[] screenshotBytes;
        final var aShot = new AShot();
        final var screenshot = aShot.takeScreenshot(driver);
        final var bufferedImage = screenshot.getImage();
        var byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
        screenshotBytes = byteArrayOutputStream.toByteArray();
        return screenshotBytes;
    }
}
