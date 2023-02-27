package core.report;

import core.abstracts.AbstractApp;
import io.qameta.allure.Allure;
import io.qameta.allure.TmsLink;
import org.apache.commons.io.FileUtils;
import org.apache.velocity.VelocityContext;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.uncommons.reportng.HTMLReporter;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static core.report.ReportUtils.SCREENSHOT_RESULT_ATTRIBUTE;
import static core.report.ReportUtils.TEST_CASE_ID_ATTRIBUTE;


public class BaseHTMLReporter extends HTMLReporter implements ITestListener {
    private static final String UTILS_KEY = "utils";
    private static final ReportUtils REPORT_UTILS = new ReportUtils();

    protected VelocityContext createContext() {
        VelocityContext context = super.createContext();
        context.put(UTILS_KEY, REPORT_UTILS);
        return context;
    }

    private void updateTestName(ITestResult result) {
        Allure.getLifecycle().updateTestCase(tr -> tr.setHistoryId(null));
        Allure.getLifecycle().updateTestCase(tr -> tr.setName(getTestName(result)));
    }

    private String getTestName(ITestResult result) {
        return result.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTestStart(ITestResult result) {
        try {
            System.out.println("Start: " + getTestName(result));
            System.out.println("Time: " + LocalDateTime.now().toLocalTime());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTestSuccess(ITestResult result) {
        updateTestName(result);
        testId(result);
        createScreenshot(result, "Success Screenshot");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTestFailure(ITestResult result) {
        updateTestName(result);
        testId(result);
        createScreenshot(result, "Failure Screenshot");
        try {
            System.out.println("Failed: " + result.getInstanceName() + " -> " + getTestName(result));
        } catch (Exception ignored) {
            System.out.println("In BaseHTMLReporter#onTestSuccess");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTestSkipped(ITestResult result) {
        testId(result);
        createScreenshot(result, "Skip Screenshot");
        try {
            System.out.println("Skipped: " + result.getInstanceName() + " : " + result.getName());
        } catch (Exception ignored) {
            System.out.println("In BaseHTMLReporter#onTestSkipped");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStart(ITestContext context) {
        try {
            System.out.println("Start test: " + context.getName());
        } catch (Exception e) {
            System.out.println("In BaseHTMLReporter#onStart:\n" + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onFinish(ITestContext context) {
        try {
            System.out.println("Finish: " + context.getName());
        } catch (Exception e) {
            System.out.println("In BaseHTMLReporter#onFinish:\n" + e.getMessage());
        }
    }

    /**
     * Adds the Test Case Id attribute for TestResult to eventually appear in
     * ReportNG report.
     *
     * @param result
     */
    private void testId(ITestResult result) {
        try {
            TmsLink id = result.getMethod().getConstructorOrMethod().getMethod().getAnnotation(TmsLink.class);

            if (id != null)
                result.setAttribute(TEST_CASE_ID_ATTRIBUTE, id.value());
        } catch (Exception e) {
            System.out.println("In BaseHTMLReporter#testId:\n" + e.getMessage());
        }
    }

    /**
     * Adds the screenshot safely to the Allure report
     *
     * @param result
     * @param name
     */
    private void createScreenshot(ITestResult result, String name) {
        try {
            // 1st approach
            File srcFile = File.createTempFile("screenshot", ".png");
            FileUtils.writeByteArrayToFile(srcFile, AllureUtils.makeScreenshot(name, AbstractApp.getDriver()));

            String baseFolder = "target" + File.separator + "surefire-reports" + File.separator;
            String screenFilePath = "html" + File.separator + LocalDate.now() + File.separator + result.getEndMillis()
                    + ".png";
            String relativePathToFile = baseFolder + screenFilePath;
            FileUtils.copyFile(srcFile, new File(relativePathToFile));

            result.setAttribute(SCREENSHOT_RESULT_ATTRIBUTE, screenFilePath);
        } catch (Exception e) {
            System.out.println("In BaseHTMLReporter#createScreenshot:\n" + e.getMessage());
        }
    }

}
