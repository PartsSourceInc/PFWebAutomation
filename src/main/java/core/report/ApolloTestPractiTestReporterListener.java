package core.report;

import core.logging.LoggingFactory;
import core.practitest.*;
import core.practitest.model.runs.AttachmentFile;
import io.qameta.allure.listener.TestLifecycleListener;
import io.qameta.allure.model.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static core.AllureLabels.PSCOM_USER_LABEL;
import static core.AllureLabels.SPRO_USER_LABEL;

public class ApolloTestPractiTestReporterListener implements TestLifecycleListener {

    public static final String SUBMIT_PRACTITEST_REGRESSION_RESULTS_TO_ID = "submitResultsToTestSetListWithId";
    public static final String SUBMIT_PRACTITEST_DAILY_RESULTS = "submitPractiTestDailyResults";
    public static final String ENABLE_DEV_MODE = "enableDevMode";

    public static final List<TestInstance> testInstances = new ArrayList<>();
    private final Logger logger = LoggingFactory.getLogger(ApolloTestPractiTestReporterListener.class.getName());
    private final String ALLURE_LABEL_PRACTI_TEST_ID = "practiTestId";
    private final List<StepResult> testStepResults;
    private final List<Attachment> testAttachments;
    private final Consumer<StepResult> stepContentPrinter = step -> {
        String stepStatus = step
                .getStatus()
                .value()
                .toUpperCase();
        String stepName = step.getName();
        logger.info(String.format("|     Step     | %s | %s", stepStatus, stepName));
        step
                .getAttachments()
                .forEach(attachment -> {
                    logger.info(String.format("| > attachment | %s", attachment.getName()));
                });
    };
    private final Consumer<Attachment> attachmentPrinter = attachment -> {
        logger.info(String.format("|  Attachment  | %s", attachment.getName()));
    };
    private String practiTestId;
    private TestResult testResult;
    private Optional<String> cartProductReferenceNumbers;
    private PractiTestApiClient practiTestApiClient;

    public ApolloTestPractiTestReporterListener() {
        testStepResults = new ArrayList<>();
        testAttachments = new ArrayList<>();
    }

    public static Optional<String> getOptionalRegressionFlag() {
        return getOptionalSystemProperty(SUBMIT_PRACTITEST_REGRESSION_RESULTS_TO_ID);
    }

    public static Optional<String> getOptionalDailyFlag() {
        return getOptionalSystemProperty(SUBMIT_PRACTITEST_DAILY_RESULTS);
    }

    private static Optional<String> getOptionalSystemProperty(final String optionalSystemProperty) {
        Optional<String> systemProperty = Optional.ofNullable(System.getProperty(optionalSystemProperty));
        return systemProperty;
    }

    @Override
    public void afterTestStop(TestResult result) {
        if (isTestFinishedAndHasPopulatedSteps(result)) {
            this.testResult = result;

            extractTestId();
            extractCartProductReferenceNumbers();
            extractTestStepResults();
            extractTestAttachments();
            printTestSteps();
            submitToPractiTestTestInstanceListIfFlagEnabled();
            submitPractiTestDailyRegressionResultsIfFlagEnabled();
        }
    }

    private void extractTestAttachments() {

        logger.debug("ATTACHMENTS =================");

        final Stream<Attachment> concatenatedAttachmentStream = Stream.concat(testStepResults
                        .stream()
                        .flatMap(extractStepAttachments()),
                testResult
                        .getAttachments()
                        .stream());

        final List<Attachment> allTestAttachments = concatenatedAttachmentStream
                .peek(attachment -> logger.debug(String.format("Attachment: name: %s, source: %s",
                        attachment.getName(),
                        attachment.getSource())))
                .collect(Collectors.toList());

        testAttachments.clear();
        testAttachments.addAll(allTestAttachments);

        logger.debug("ATTACHMENTS DONE ============");
    }

    private Function<StepResult, Stream<? extends Attachment>> extractStepAttachments() {
        return stepResult -> {
            var attachments = stepResult
                    .getAttachments()
                    .stream();
            return attachments;
        };
    }

    private String getResultsAbsolutePath() {
        String absolutePath = null;
        try {
            String path = "target/allure-results";
            File file = new File(path);
            absolutePath = file.getAbsolutePath();
        } catch (Exception e) {
            logger.debug(e.toString());
        }
        return absolutePath;
    }

    private boolean isTestFinishedAndHasPopulatedSteps(TestResult result) {
        var isTestFinished = result.getStage() == Stage.FINISHED && !result.getSteps().isEmpty();
        return isTestFinished;
    }

    private void submitResultsToPractiTest(final String testSetId) {

        practiTestApiClient = buildPractiTestApiClient();

        TestDisplayId testDisplayId = new TestDisplayId(practiTestId);

        Optional<String> foundTestInstanceId = testInstances
                .stream()
                .filter(testInstance -> testInstance.getAttributes().testDisplayId.value.equalsIgnoreCase(testDisplayId.value))
                .map(testInstance -> testInstance.testInstanceId.value)
                .findFirst();

        foundTestInstanceId.ifPresentOrElse(instanceId -> {
            logger.debug(String.format("FOUND TEST INSTANCE ID: '%s'", instanceId));
            TestInstanceId testInstanceId = new TestInstanceId(instanceId);
            doSubmitResultsToPractiTestSetInstanceList(testInstanceId);
        }, () -> {
            throw new RuntimeException(String.format("Test with ID#'%s' NOT found in PractiTest", testDisplayId));
        });

        logger.debug(String.format("SUBMITTING %d steps for TestID# '%s' to TestSetFilterID# '%s'",
                testStepResults.size(),
                practiTestId,
                testSetId));
    }

    private void doSubmitResultsToPractiTestSetInstanceList(TestInstanceId testInstanceId) {
        List<TestStep> testSteps = this.testStepResults
                .stream()
                .map(this::convertToPractiTestStep)
                .collect(Collectors.toList());

        List<Optional<String>> executionOutput = new ArrayList<>();

        executionOutput.add(generateExecutionOutputMessage());

        if (retrieveTestResultLabel(PSCOM_USER_LABEL).isPresent()) {
            executionOutput.add(retrieveTestResultLabel(PSCOM_USER_LABEL));
        }
        if (retrieveTestResultLabel(SPRO_USER_LABEL).isPresent()) {
            executionOutput.add(retrieveTestResultLabel(SPRO_USER_LABEL));
        }
        final Optional<List<AttachmentFile>> optionalAttachments = convertAttachmentsAllureToPractiTest(testAttachments);
        practiTestApiClient.submitTestRun(testInstanceId, testSteps, executionOutput, optionalAttachments);
    }

    private Optional<List<AttachmentFile>> convertAttachmentsAllureToPractiTest(final List<Attachment> testAttachments) {

        List<AttachmentFile> attachmentFiles = testAttachments
                .stream()
                .map(this::allureToPractiTestAttachmentImage)
                .collect(Collectors.toList());

        return Optional.ofNullable(attachmentFiles.isEmpty() ? null : attachmentFiles);
    }

    private AttachmentFile allureToPractiTestAttachmentImage(final Attachment imageAttachment) {

        final String absolutePath = getResultsAbsolutePath();
        final String source = imageAttachment.getSource();
        final String fullImagePath = absolutePath + "\\" + source;
        final String base64Content = filenameToBase64(fullImagePath);

        String attachmentFileExtension;
        final String type = imageAttachment.getType();
        if (type.contains("image")) {
            attachmentFileExtension = ".png";
        } else if (type.contains("text")) {
            attachmentFileExtension = ".txt";
        } else {
            attachmentFileExtension = "";
        }
        final String attachmentFileName = imageAttachment.getName() + attachmentFileExtension;

        AttachmentFile attachmentFile = AttachmentFile
                .builder()
                .fileName(attachmentFileName)
                .contentEncoded(base64Content)
                .build();

        return Optional
                .ofNullable(attachmentFile)
                .orElseThrow();
    }

    private String filenameToBase64(final String fileName) {
        File originalFile = new File(fileName);
        String encodedBase64 = null;
        try {
            FileInputStream fileInputStreamReader = new FileInputStream(originalFile);
            byte[] bytes = new byte[(int) originalFile.length()];
            fileInputStreamReader.read(bytes);
            encodedBase64 = Base64
                    .getMimeEncoder()
                    .encodeToString(bytes);
        } catch (IOException e) {
            logger.debug(e.getMessage());
            throw new RuntimeException(e);
        }
        return encodedBase64;
    }

    private Optional<String> generateExecutionOutputMessage() {
        final StringBuilder executionMessage = new StringBuilder();
        executionMessage.append("ExecutionOutput: ");

        cartProductReferenceNumbers.ifPresent(cartRefNum -> executionMessage.append(String.format("Ref# %s", cartRefNum)));

        final Optional<String> executionOutput = Optional.of(executionMessage.toString());
        return executionOutput;
    }

    private PractiTestApiClient buildPractiTestApiClient() {
        HttpRequestExecutor httpRequestExecutor = HttpRequestExecutor.createInstance();
        return PractiTestApiClientHttp
                .builder()
                .requestExecutor(httpRequestExecutor)
                .build();
    }

    private void extractTestStepResults() {
        List<StepResult> extractedTestSteps = testResult
                .getSteps()
                .stream()
                .map(this::flattenSubStepAttachments)
                .map(this::flattenStepsToDescription)
                .collect(Collectors.toList());
        testStepResults.clear();

        testStepResults.addAll(extractedTestSteps);
    }

    private StepResult flattenSubStepAttachments(final StepResult stepResult) {

        List<StepResult> flattenedSteps = Stream
                .concat(
                        Stream.of(stepResult),
                        stepResult
                                .getSteps()
                                .stream()
                                .flatMap(this::flattenStep))
                .collect(Collectors.toList());

        Map<String, Attachment> attachmentList = flattenedSteps
                .stream()
                .flatMap(step -> step
                        .getAttachments()
                        .stream())
                .collect(Collectors.toMap(
                        Attachment::getName,
                        stepAttachment -> stepAttachment,
                        (attachment1, attachment2) -> attachment1));

        stepResult
                .setAttachments(new ArrayList<>(attachmentList.values()));

        return stepResult;
    }

    private Stream<StepResult> flattenStep(final StepResult stepResult) {
        Stream<StepResult> stepResultStream = Stream.concat(
                Stream.of(stepResult),
                stepResult
                        .getSteps()
                        .stream()
                        .flatMap(this::flattenStep));
        return stepResultStream;
    }

    private StepResult flattenStepsToDescription(final StepResult stepResult) {

        stepResult.setDescription("");

        trimStepNameIfTooLong(stepResult);

        if (!stepResult
                .getSteps()
                .isEmpty()) {
            stepResult.setDescription(stepResult.getDescription() + "\n**Step Actions**");
        }

        List<String> stepActions = stepResult
                .getSteps()
                .stream()
                .flatMap(stepAction -> flattenActionsToStepDescription(stepAction, 0))
                .peek(logger::debug)
                .collect(Collectors.toList());

        String flattenedActions = stepActions
                .stream()
                .reduce((prev, curr) -> String.format("%s\n%s", prev, curr))
                .orElse("");

        var stepDetails = String.format("%s\n%s", stepResult.getDescription(), flattenedActions);
        stepResult.setDescription(stepDetails);

        return stepResult;
    }

    private Stream<String> flattenActionsToStepDescription(StepResult stepAction, final int depthLevel) {
        String actionEntry = extractFormattedActionEntry(stepAction, depthLevel);

        Stream<String> actionEntryStream = Stream.concat(Stream.of(actionEntry),
                stepAction
                        .getSteps()
                        .stream()
                        .flatMap(action -> {
                            return flattenActionsToStepDescription(action, depthLevel + 1);
                        }));

        return actionEntryStream;
    }

    private String extractFormattedActionEntry(final StepResult stepAction, int depthLevel) {

        var actionStatus = stepAction
                .getStatus()
                .value()
                .toUpperCase();

        var formattedActionStatus = stepAction.getStatus() == Status.FAILED || stepAction.getStatus() == Status.BROKEN ?
                String.format("**%s**", actionStatus) :
                actionStatus;
        var actionName = stepAction.getName();
        var formattedEntry = String.format("%s %s", formattedActionStatus, actionName);
        var indentedFormattedEntry = "| " + StringUtils.leftPad(formattedEntry,
                formattedEntry.length() + depthLevel * 4,
                "- | ");

        return indentedFormattedEntry;
    }

    private void trimStepNameIfTooLong(final StepResult stepResult) {
        var stepName = stepResult.getName();
        var stepNameLength = stepName.length();

        if (stepNameLength > 250) {
            var trimmedStepName = stepName.substring(0, 240) + "...";
            stepResult.setName(trimmedStepName);
            stepResult.setDescription(String.format("__(%s)__", stepName));
        }
    }

    private TestStep convertToPractiTestStep(StepResult step) {
        Status status = step.getStatus();
        String name = step.getName();
        String expectedResults = Strings.EMPTY;
        String description = step.getDescription();
        final List<Attachment> attachments = step.getAttachments();

        if (!step.getParameters().isEmpty() && step.getParameters().get(0).getName().equalsIgnoreCase("steps")) {
            status = Status.SKIPPED;
            description = step.getParameters().get(1).getValue();
            expectedResults = step.getParameters().get(0).getValue();
            return TestStep
                    .builder()
                    .name(name)
                    .description(description)
                    .expectedResults(expectedResults)
                    .status(convertToPractiTestStatus(status))
//        .attachments((convertAttachmentsAllureToPractiTest(attachments).orElse(null)))
                    .start(step.getStart())
                    .stop(step.getStop())
                    .build();
        }

        return TestStep
                .builder()
                .name(name)
                .description(description)
                .expectedResults(expectedResults)
                .status(convertToPractiTestStatus(status))
                .attachments((convertAttachmentsAllureToPractiTest(attachments).orElse(null)))
                .start(step.getStart())
                .stop(step.getStop())
                .build();
    }

    private TestStep.Status convertToPractiTestStatus(Status stepStatus) {

        TestStep.Status practiTestStatus;

        switch (stepStatus) {
            case PASSED:
                practiTestStatus = TestStep.Status.PASSED;
                break;
            case FAILED:
            case BROKEN:
                practiTestStatus = TestStep.Status.FAILED;
                break;
            case SKIPPED:
                practiTestStatus = TestStep.Status.BLOCKED;
                break;
            default:
                throw new RuntimeException("Unsupported status:");
        }
        return practiTestStatus;
    }

    private void printTestSteps() {
        logger.info("");
        logger.info("========================================================");
        logger.info(String.format("  START #%s : %s",
                practiTestId,
                testResult
                        .getFullName()
                        .toUpperCase()));
        logger.info("========================================================");
        logger.info("");

        testStepResults.forEach(stepContentPrinter);

        logger.info("Attachments =================");
        testAttachments.forEach(attachmentPrinter);
        logger.info("=============================");

        logger.info("");
        logger.info("========================================================");
        logger.info(String.format("  END #%s : %s",
                practiTestId,
                testResult
                        .getStatus()
                        .value()
                        .toUpperCase()));
        logger.info("========================================================");
        logger.info("");
    }

    private void extractTestId() {
        Optional<String> testIdOptional = retrieveTestResultLabel(ALLURE_LABEL_PRACTI_TEST_ID);
        testIdOptional.ifPresent(testId -> practiTestId = testId);
    }

    private void extractCartProductReferenceNumbers() {
        final String cartRefNumLabel = "cartProductRefs";
        final Optional<String> retrievedProductReferenceNumbers = retrieveTestResultLabel(cartRefNumLabel);
        this.cartProductReferenceNumbers = retrievedProductReferenceNumbers;
    }

    private void submitToPractiTestTestInstanceListIfFlagEnabled() {
        getOptionalRegressionFlag().ifPresentOrElse(this::submitResultsToPractiTest,
                () -> logger.debug("NO - PractiTest REGRESSION flag and ID."));
    }

    private void submitPractiTestDailyRegressionResultsIfFlagEnabled() {
        getOptionalDailyFlag().ifPresentOrElse(this::submitDailyResultsToPractiTest,
                () -> logger.debug("NO - PractiTest DAILY flag."));
    }

    private void submitDailyResultsToPractiTest(String submitDailyResultsFlag) {
        submitResultsToPractiTest(submitDailyResultsFlag);
    }

    private Optional<String> retrieveTestResultLabel(String testResultLabel) {
        return testResult
                .getLabels()
                .stream()
                .filter(label -> testResultLabel.equalsIgnoreCase(label.getName()))
                .map(Label::getValue)
                .findFirst();
    }
}
