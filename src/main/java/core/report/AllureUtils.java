package core.report;

import io.qameta.allure.Attachment;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;

import java.io.File;
import java.io.IOException;

public final class AllureUtils {
    private AllureUtils() {
    }

    @Attachment(value = "{0}", type = "image/png")
    public static synchronized byte[] makeScreenshot(String name, WebDriver driver) {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    }

    @Attachment(value = "{1}", type = "text/js")
    public static synchronized byte[] getBytesFrom(File file, String name) {
        try {
            return FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            System.out.println("In AllureUtils.getBytesFrom:\n" + e.getMessage());
            return new byte[0];
        }
    }

    @Attachment("Current URL")
    public static String getCurrentUrl(WebDriver driver) {
        return "\nCurrent URL: " + driver.getCurrentUrl() + "\n";
    }

    @Attachment("Browser Logs")
    public static String attachBrowserLogs(LogEntries logEntries) {
        StringBuilder sb = new StringBuilder();
        for (LogEntry entry : logEntries) {
            sb.append(entry).append("\n");
        }
        return sb.toString();
    }

    @Attachment("{0}")
    public static String attach(String name, String attachment) {
        return attachment;
    }

}