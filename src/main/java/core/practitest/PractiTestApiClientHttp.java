package core.practitest;

import com.jayway.restassured.path.json.JsonPath;
import core.logging.LoggingFactory;
import core.practitest.model.instance.InstanceAttributes;
import core.practitest.model.instance.InstanceData;
import core.practitest.model.instance.InstanceModelList;
import core.practitest.model.runs.*;
import lombok.Builder;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Implementation of the PractiTest Api using the Http protocol of communications.
 * <p>
 * It uses basic authentication
 */
@Builder
public class PractiTestApiClientHttp implements PractiTestApiClient {

    private final String projectId = "8289";

    private final String testUrl = "https://api.practitest.com/api/v2/projects/8289/tests.json?display-ids=";

    @Builder.Default
    private final Logger logger = LoggingFactory.getLogger();

    private final HttpRequestExecutor requestExecutor;

    private final int PRACTI_TEST_BATCH_LIMIT = 15;

    @Override
    public TestId retrieveTestId(TestDisplayId testDisplayId) {
        final String fullTestUrl = createTestUrl(testDisplayId);
        final String returnResult = requestExecutor.executeGetAndReturnResult(fullTestUrl);

        JsonPath jsonPath = JsonPath.from(returnResult);
        final String testIdValue = String.valueOf(jsonPath.getString("data[0].id"));
        final TestId testId = new TestId(testIdValue);
        return testId;
    }

    @Override
    public Map<TestDisplayId, TestId> retrieveTestIds(List<TestDisplayId> testDisplayIds) {

        final List<List<TestDisplayId>> testIdBatches = getBatches(testDisplayIds);

        final Map<TestDisplayId, TestId> testIdMap = testIdBatches.stream()
                .map(this::retrieveParsedTestIds)
                .flatMap(parsedTestIdMap -> parsedTestIdMap.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (key1, key2) -> key1));
        return testIdMap;
    }

    private <T> List<List<T>> getBatches(List<T> collection) {
        return IntStream
                .iterate(0, i -> i < collection.size(), i -> i + PRACTI_TEST_BATCH_LIMIT)
                .mapToObj(i -> collection.subList(i, Math.min(i + PRACTI_TEST_BATCH_LIMIT, collection.size())))
                .collect(Collectors.toList());
    }

    private Map<TestDisplayId, TestId> retrieveParsedTestIds(final List<TestDisplayId> testDisplayIds) {
        final String fullTestUrl = createTestUrl(testDisplayIds);
        final String returnResult = requestExecutor.executeGetAndReturnResult(fullTestUrl);

        JsonPath jsonPath = JsonPath.from(returnResult);

        List<Map<String, Object>> dataArray = jsonPath.getList("data");

        final Map<TestDisplayId, TestId> testIdMap = dataArray
                .stream()
                .collect(Collectors.toMap(
                        element -> {
                            final Map<String, Object> attributes = (Map<String, Object>) element.get("attributes");
                            final String displayId = String.valueOf(attributes.get("display-id"));
                            TestDisplayId testDisplayId = new TestDisplayId(displayId);
                            return testDisplayId;
                        },
                        element -> {
                            final String internalId = (String) (element.get("id"));
                            TestId testId = new TestId(internalId);
                            return testId;
                        }
                ));

        return testIdMap;
    }

    @SneakyThrows
    @Override
    public synchronized void submitTestRun(
            final TestInstanceId instanceId,
            final List<TestStep> testSteps,
            List<Optional<String>> executionOutputMessage,
            Optional<List<AttachmentFile>> attachments
    ) {

        final String runUrl = "https://api.practitest.com/api/v2/projects/8289/runs.json";

        Data runData = new Data();
        runData.setType("instances");
        int instanceIdInt = Integer.parseInt(instanceId.value);
        core.practitest.model.runs.Attributes attributes =
                core.practitest.model.runs.Attributes
                        .builder()
                        .instanceId(instanceIdInt)
                        .runDuration(calculateRunDuration(testSteps))
                        .automatedExecutionOutput(formatExecutionMessage(executionOutputMessage))
                        .build();
        runData.setAttributes(attributes);

        List<StepModel> practiTestSteps = testSteps
                .stream()
                .map(testStep -> StepModel.builder()
                        .name(testStep.name)
                        .description(testStep.description)
                        .expectedResults(testStep.expectedResults)
                        .status(testStep.status.label)
                        .files(getAttachmentFiles(testStep.getOptionalAttachments()))
                        .build())
                .collect(Collectors.toList());
        runData.setSteps(new Steps(practiTestSteps));
        RunsModel runsModel = new RunsModel(runData);
        String postRequestData = writeValueAsString(runsModel);
        logger.debug("=================");
        final String postResult = requestExecutor.executePostAndReturnResult(runUrl, postRequestData);
        logger.debug(String.format("POST request result: \n %s", postResult));
        requestLimitPause();
    }

    private String formatExecutionMessage(List<Optional<String>> executionOutputMessage) {
        StringBuilder x = new StringBuilder();

        for (Optional<String> xx : executionOutputMessage ) {
            x.append(xx
                    .map(message -> {
                        String trimmedMessage;
                        if (message.length() >= 255) {
                            trimmedMessage = message.substring(0, 255);
                        } else {
                            trimmedMessage = message;
                        }
                        return trimmedMessage;
                    })
                    .orElse(null))
                    .append("\n");
        }

        return x.toString();
    }

    private String calculateRunDuration(final List<TestStep> testSteps) {
        final int stepCount = testSteps.size();
        final String testRunDuration;
        if (stepCount > 0) {
            final TestStep lastStep = testSteps.get(stepCount - 1);
            final TestStep firstStep = testSteps.get(0);
            final long durationInMillis = lastStep.stop - firstStep.start;
            testRunDuration = DurationFormatUtils.formatDuration(durationInMillis, "HH:mm:ss");
        } else {
            testRunDuration = "N/A";
        }
        return testRunDuration;
    }

    private Files getAttachmentFiles(final Optional<List<AttachmentFile>> optionalAttachmentFiles) {
        Files files = null;
        if (optionalAttachmentFiles.isPresent()) {
            files = Files.builder()
                    .filesData(
                            optionalAttachmentFiles.get()
                                    .stream()
                                    .map(attachment -> AttachmentFile.builder()
                                            .fileName(attachment.getFileName())
                                            .contentEncoded(attachment.getContentEncoded())
                                            .build())
                                    .collect(Collectors.toList())
                    ).build();
        }

        return files;
    }

    @Override
    public List<TestInstance> retrieveTestInstances(final TestSetId testSetId) {
        String instancesTestSetUrl =
                "https://api.practitest.com/api/v2/projects/8289/instances.json?page%5Bnumber%5D=";
        String requestUrl = instancesTestSetUrl + 1 + "&set-ids=" + testSetId.value;
        int count = getTestInstancePagesCount(PractiTestRequestUrl.of(requestUrl));
        List<TestInstance> testInstances = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            instancesTestSetUrl =
                    "https://api.practitest.com/api/v2/projects/8289/instances.json?page%5Bnumber%5D=";
            requestUrl = instancesTestSetUrl + i + "&set-ids=" + testSetId.value;
            testInstances.addAll(retrieveTestInstances(PractiTestRequestUrl.of(requestUrl)));
        }

        return testInstances;
    }

    @Override
    public List<TestInstance> retrieveTestInstances(final SetFilterId setFilterId) {
        final String instancesFilterUrl =
                "https://api.practitest.com/api/v2/projects/8289/instances.json?set-filter-id=";
        final String requestUrl = instancesFilterUrl + setFilterId.value;

        List<TestInstance> testInstances = retrieveTestInstances(PractiTestRequestUrl.of(requestUrl));

        return testInstances;
    }

    private int getTestInstancePagesCount(final PractiTestRequestUrl url) {
        String returnResult = requestExecutor.executeGetAndReturnResult(url.value);
        JsonPath jsonDocument = JsonPath.from(returnResult);
        HashMap meta = jsonDocument.getJsonObject("meta");
        return (int) meta.get("total-pages");
    }

    private List<TestInstance> retrieveTestInstances(final PractiTestRequestUrl url) {
        String returnResult = requestExecutor.executeGetAndReturnResult(url.value);
        JsonPath jsonDocument = JsonPath.from(returnResult);

        List<Map> testInstanceList = jsonDocument.getList("data");

        return testInstanceList
                .stream()
                .map(testInstanceElement -> {
                    String testInstanceIdText = testInstanceElement.get("id").toString();
                    TestInstanceId testInstanceId = new TestInstanceId(testInstanceIdText);
                    String testDisplayIdText;
                    testDisplayIdText = ((Map) testInstanceElement.get("attributes")).get("test-display-id").toString();
                    TestDisplayId testDisplayId = new TestDisplayId(testDisplayIdText);
                    Attributes attributes = new Attributes(testDisplayId);
                    TestInstance testInstance = new TestInstance(testInstanceId, attributes);
                    return testInstance;
                })
                .collect(Collectors.toList());
    }

    private InstanceModelList getTestInstancesToCreate(@NonNull final String testSetInstanceId,
                                                       @NonNull final Collection<TestId> testIds) {
        final int setId = Integer.parseInt(testSetInstanceId);
        List<InstanceData> instanceData = new ArrayList<>();

        testIds.stream().map((TestId::getValue)).forEach(testIdString -> {
            logger.debug(String.format("Creating test instance for testId: '%s'...", testIdString));
            final int testId = Integer.parseInt(testIdString);
            instanceData.add(InstanceData
                    .builder()
                    .attributes(InstanceAttributes
                            .builder()
                            .setId(setId)
                            .testId(testId)
                            .build())
                    .build());
        });

        return InstanceModelList
                .builder()
                .data(instanceData)
                .build();
    }

    @Override
    public List<TestInstance> createTestInstancesAndReturnIds(
            @NonNull final String testSetInstanceId,
            @NonNull final Collection<TestId> testIds) {

        final String fullTestInstanceListCreationUrl = "https://api.practitest.com/api/v2/projects/8289/instances.json";

        InstanceModelList instanceModel = getTestInstancesToCreate(testSetInstanceId, testIds);

        final String jsonPostData = writeValueAsString(instanceModel);
        requestLimitPause();
        System.out.println(jsonPostData);
        final String result = requestExecutor.executePostAndReturnResult(fullTestInstanceListCreationUrl, jsonPostData);

        List<TestInstance> testInstances = new ArrayList<>();

        JSONArray array = new JSONObject(result).getJSONArray("data");
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            String testInstanceIdText = obj.getString("id");
            TestInstanceId testInstanceId = new TestInstanceId(testInstanceIdText);
            String testDisplayIdText;
            testDisplayIdText = String.valueOf(obj.getJSONObject("attributes").getInt("test-display-id"));
            TestDisplayId testDisplayId = new TestDisplayId(testDisplayIdText);
            Attributes attributes = new Attributes(testDisplayId);
            testInstances.add(new TestInstance(testInstanceId, attributes));
        }

//    final String fullTestInstanceListCreationUrl = "https://api.practitest.com/api/v2/projects/8289/instances.json";
//
//    final List<TestInstance> testInstances = testIds
//      .stream()
//      .map((TestId::getValue))
//      .map(testIdString -> {
//        logger.debug(String.format("Creating test instance for testId: '%s'...", testIdString));
//        final int setId = Integer.parseInt(testSetInstanceId);
//        final int testId = Integer.parseInt(testIdString);
//
//        InstanceModel instanceModel = InstanceModel
//          .builder()
//          .data(
//            InstanceData
//              .builder()
//              .attributes(InstanceAttributes
//                .builder()
//                .setId(setId)
//                .testId(testId)
//                .build())
//              .build())
//          .build();
//        final String jsonPostData = writeValueAsString(instanceModel);
//        final String result;
//        result = requestExecutor.executePostAndReturnResult(fullTestInstanceListCreationUrl, jsonPostData);
//
//        requestLimitPause();
//
//        logger.debug(String.format("Creating test instance for testId: '%s'...DONE", testIdString));
//
//        return result;
//      })
//      .map(instanceResult -> {
//        JsonPath jsonDocument = JsonPath.from(instanceResult);
//        final Map<String, Object> testInstanceJson = jsonDocument.get("data");
//        String testInstanceIdText = testInstanceJson.get("id").toString();
//        TestInstanceId testInstanceId = new TestInstanceId(testInstanceIdText);
//        String testDisplayIdText;
//        testDisplayIdText = ((Map) testInstanceJson.get("attributes")).get("test-display-id").toString();
//        TestDisplayId testDisplayId = new TestDisplayId(testDisplayIdText);
//        Attributes attributes = new Attributes(testDisplayId);
//        TestInstance testInstance = new TestInstance(testInstanceId, attributes);
//        return testInstance;
//      })
//      .collect(Collectors.toList());

        return testInstances;
    }

    @Override
    public String retrieveTestPreconditions(final TestDisplayId testDisplayId) {
        var testId = retrieveTestId(testDisplayId);
        var testIdValue = testId.getValue();
        var testRequestUrl = String.format("https://api.practitest.com/api/v2/projects/8289/tests/%s.json", testIdValue);
        logger.debug(testRequestUrl);
        final String returnResult = requestExecutor.executeGetAndReturnResult(testRequestUrl);
        var jsonPath = JsonPath.from(returnResult);
        var preconditions = jsonPath.getString("data.attributes.preconditions");
        return preconditions;
    }

    @SneakyThrows
    private void requestLimitPause() {
        TimeUnit.SECONDS.sleep(5);
    }

    @SneakyThrows
    private String writeValueAsString(Object jsonObjectValue) {
        ObjectMapper mapperObj = new ObjectMapper();
        mapperObj.enable(Feature.INDENT_OUTPUT);
        return mapperObj.writeValueAsString(jsonObjectValue);
    }

    private String createTestUrl(final TestDisplayId testId) {
        final String fullTestUrl = testUrl + testId.value;
        return fullTestUrl;
    }

    private String createTestUrl(final List<TestDisplayId> testIds) {
        final Optional<String> testIdString = testIds
                .stream()
                .map(TestDisplayId::getValue)
                .reduce((s1, s2) -> s1 + "," + s2);
        final String fullTestUrl = testUrl + testIdString.orElseGet(() -> "");

        return fullTestUrl;
    }
}
