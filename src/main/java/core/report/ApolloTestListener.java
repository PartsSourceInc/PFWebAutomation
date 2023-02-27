package core.report;

import core.abstracts.AbstractApp;
import core.abstracts.AutomationTest;
import core.annotations.TestID;
import core.practitest.*;
import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import io.qameta.allure.testng.AllureTestNg;
import lombok.extern.slf4j.Slf4j;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.qameta.allure.util.ResultsUtils.getStatusDetails;

/**
 * Apollo Test TestNG Listener. Allows hooks into the TestNG execution lifecycle for
 * logging and reporting purposes.
 */
@Slf4j
public class ApolloTestListener extends AllureTestNg {

    private String currentTestName;

    private String currentTestId;

    private PractiTestApiClient practiTestClient;

//  private void initializeDevModeFlag() {
//    var optionalEnableDevModeFlag = getOptionalEnableDevModeFlag();
//    optionalEnableDevModeFlag.ifPresent(
//       enableDevModeFlag -> ApolloStepListener.SHOULD_ENABLE_DEV_MODE = true);
//  }

//  public static Optional<String> getOptionalEnableDevModeFlag(){
//    Optional<String> systemProperty = Optional.ofNullable(System.getProperty(ENABLE_DEV_MODE));
//    return systemProperty;
//  }

    @Override
    public void onTestStart(ITestResult result) {
        super.onTestStart(result);

        var methodName = result.getName();
        if ("executeTestSteps".equalsIgnoreCase(methodName)) {
            try {
                Method executePreconditionSteps = result.getInstance().getClass().getMethod("executePreconditionSteps");
                Class<?> declaringClass = executePreconditionSteps.getDeclaringClass();
                String declaringClassName = declaringClass.getSimpleName();
                if (!"AutomationTest".equalsIgnoreCase(declaringClassName)) {
                    step("Precondition Steps", () -> {
//                        try {
                            log.info("Test HAS precondition steps. Executing...");
                            ApolloStepListener.webDriver.set(AbstractApp.getDriverForChrome());
                            String currentTestcaseOrStep = Allure.getLifecycle().getCurrentTestCaseOrStep().orElse("none");
                            log.info(String.format("onTestStart#currentTestOrStep: '%s'", currentTestcaseOrStep));
                            final AutomationTest startingTest = (AutomationTest) result.getInstance();
                            startingTest.executePreconditionSteps();
//                        } catch (Exception e) {
//                            log.warn("Error executing precondition steps", e);
//                            Assertions.fail("Error executing precondition steps", e);
//                        } finally {
//                            AbstractApp.closeApp();
//                        }
                    });
                } else {
                    log.info("Test does NOT have precondition steps. Continuing...");
                }
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Unable to find executePreconditionSteps method definition", e);
            }
        }
        ApolloStepListener.webDriver.set(AbstractApp.getDriver());

        extractTestName(result);
        extractTestDescription(result);
        extractTestId(result);


        log.debug("");
        log.debug("");
        log.debug(String.format("          ----------- '%s'(ID %s) -----------", currentTestName.toUpperCase(), currentTestId));
        log.debug("");
    }

    private void step(final String name, final StepBody body) {
        final String uuid = UUID.randomUUID().toString();
        final StepResult result = new StepResult().setName(name);
        getLifecycle().startStep(uuid, result);
        try {
            body.execute();
            getLifecycle().updateStep(uuid, s -> s.setStatus(Status.PASSED));
        } catch (SkipException e) {
            System.out.println("skip exception");
            getLifecycle().updateStep(uuid, s -> s
                    .setStatus(Optional.of(getStatus(e)).orElse(Status.SKIPPED))
                    .setStatusDetails(getStatusDetails(e).orElse(null)));
            throw e;
        } catch (Exception e) {
            System.out.println("exception exception");
            getLifecycle().updateStep(uuid, s -> s
                    .setStatus(Optional.of(getStatus(e)).orElse(Status.BROKEN))
                    .setStatusDetails(getStatusDetails(e).orElse(null)));
            throw e;
        } finally {
            AbstractApp.closeApp();
            getLifecycle().stopStep(uuid);
        }
    }

    private void extractTestName(ITestResult result) {
        final Optional<String> testName = Optional.of(result.getTestClass().getRealClass().getSimpleName());
        currentTestName = testName.orElse("N/A");
    }

    private void extractTestId(ITestResult result) {
        Method currentTestMethod = result.getMethod().getConstructorOrMethod().getMethod();
        Optional<TestID> optionalTestID = Optional.ofNullable(currentTestMethod.getAnnotation(TestID.class));
        optionalTestID.ifPresent(testID -> this.currentTestId = testID.value());
    }

    private void extractTestDescription(ITestResult result) {
        Test test = result.getTestClass().getRealClass().getAnnotation(Test.class);
        if (test != null) {
            String currentTestDescription = test.description();
            log.debug(String.format("Description: '%s'", currentTestDescription));
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        super.onTestSuccess(result);
        AbstractApp.closeApp();
//      browserController.get().stopBrowser();
        log.debug("");
        log.debug(String.format("          >>>>>>>>>>> SUCCESS (%s) <<<<<<<<<<<", this.currentTestName.toUpperCase()));
        log.debug("");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        super.onTestFailure(result);

        Optional<String> optionalTestName = Optional.ofNullable(currentTestName);
        var testName = optionalTestName.orElse("N/A");
        var testNameUpperCased = testName.toUpperCase();
        AbstractApp.closeApp();
//     browserController.get().stopBrowser();
        log.debug("");
        log.debug(String.format("          >>>>>>>>>>> FAIL (%s) <<<<<<<<<<<", testNameUpperCased));
        log.debug("");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        super.onTestSkipped(result);

        Optional<String> optionalTestName = Optional.ofNullable(currentTestName);
        var testName = optionalTestName.orElse("N/A");
        var testNameUpperCased = testName.toUpperCase();
        AbstractApp.closeApp();
//    browserController.get().stopBrowser();
        log.debug("");
        log.debug(String.format("          >>>>>>>>>>> SKIP (%s) <<<<<<<<<<<", testNameUpperCased));
        log.debug("");
    }

    @Override
    public void onStart(ITestContext context) {
        super.onStart(context);

        prepareSuiteForParallelRun(context);

//     initializeDevModeFlag();
        initializePractiTestClient();
        prepareRegressionTestInstanceListIfFlagEnabled();
        final ITestNGMethod[] allTestMethods = context.getAllTestMethods();
        prepareDailyTestInstanceListIfFladEnabled(allTestMethods);

        List<ITestNGMethod> methodsWithCustomAnnotation =
                Arrays.stream(context.getAllTestMethods())
                        .filter(
                                iTestNGMethod ->
                                        iTestNGMethod
                                                .getConstructorOrMethod()
                                                .getMethod()
                                                .getAnnotation(TestID.class)
                                                != null)
                        .collect(Collectors.toList());
        log.debug(String.format("test count: '%d'", methodsWithCustomAnnotation.size()));
    }

    @Override
    public void onFinish(ITestContext context) {
        super.onFinish(context);
        log.debug(String.format("DONE executing test suite with test count: '%d'", context.getAllTestMethods().length));
    }

    private void prepareSuiteForParallelRun(ITestContext context) {
        int threadCount = 1;
        if (Objects.nonNull(System.getProperty("threads"))) {
            threadCount = Integer.parseInt(System.getProperty("threads"));
        }
        XmlSuite suite = context.getSuite().getXmlSuite();
        suite.setParallel(XmlSuite.ParallelMode.CLASSES);
        suite.setThreadCount(threadCount);
    }

    private void initializePractiTestClient() {
        practiTestClient = PractiTestApiClientHttp
                .builder()
                .requestExecutor(HttpRequestExecutor.createInstance())
                .build();
    }

    private void prepareRegressionTestInstanceListIfFlagEnabled() {
        ApolloTestPractiTestReporterListener.getOptionalRegressionFlag()
                .ifPresent(this::retrieveAndPopulateTestInstanceList);
    }

    private void retrieveAndPopulateTestInstanceList(String testSetId) {
        SetFilterId setFilterId = new SetFilterId(testSetId);
        List<TestInstance> testInstances = practiTestClient.retrieveTestInstances(setFilterId);
        ApolloTestPractiTestReporterListener.testInstances.addAll(testInstances);
    }

    private void prepareDailyTestInstanceListIfFladEnabled(ITestNGMethod[] testNGMethods) {
        ApolloTestPractiTestReporterListener
                .getOptionalDailyFlag()
                .ifPresent(
                        flag -> prepareDailyTestInstanceList(flag, testNGMethods));
    }

    private void prepareDailyTestInstanceList(
            String testSetId,
            ITestNGMethod[] testContext) {

        final List<TestDisplayId> testDisplayIds = Stream
                .of(testContext)
                .map(getAnnotationTestIdValue())
                .filter(onlyNonZeroIds())
                .map(String::trim)
                .map(TestDisplayId::new)
                .collect(Collectors.toList());

        final List<TestInstance> existingTestInstances = practiTestClient.retrieveTestInstances(TestSetId.of(testSetId));
        final List<TestDisplayId> existingTestIds = existingTestInstances
                .stream()
                .map(instance -> instance.getAttributes()
                        .getTestDisplayId())
                .collect(Collectors.toList());

        final Map<TestDisplayId, TestId> runListTestIds = retrieveTestIds(testDisplayIds);

        final List<TestId> filteredTestIdList = runListTestIds
                .keySet()
                .stream()
                .filter(testDisplayId ->
                        existingTestIds
                                .stream()
                                .noneMatch(existingTestDisplayId -> existingTestDisplayId.equals(testDisplayId)))
                .map(runListTestIds::get)
                .collect(Collectors.toList());

        List<TestInstance> createdTestInstanceList = new ArrayList<>();

        if (!filteredTestIdList.isEmpty()) {
            createdTestInstanceList = practiTestClient
                    .createTestInstancesAndReturnIds(testSetId, filteredTestIdList);
        }

        final List<TestInstance> combinedTestInstanceList = Stream
                .concat(
                        existingTestInstances.stream(),
                        createdTestInstanceList.stream())
                .collect(Collectors.toList());

        ApolloTestPractiTestReporterListener
                .testInstances.addAll(combinedTestInstanceList);
    }

    private Map<TestDisplayId, TestId> retrieveTestIds(final List<TestDisplayId> testDisplayIds) {
        final Map<TestDisplayId, TestId> testIdMap = practiTestClient.retrieveTestIds(testDisplayIds);
        return testIdMap;
    }

    private Predicate<String> onlyNonZeroIds() {
        return testId -> !testId.equalsIgnoreCase("0");
    }

    private Function<ITestNGMethod, String> getAnnotationTestIdValue() {
        return iTestNGMethod -> iTestNGMethod
                .getConstructorOrMethod()
                .getMethod()
                .getDeclaredAnnotation(TestID.class)
                .value();
    }

    public interface StepBody {
        void execute();
    }
}
